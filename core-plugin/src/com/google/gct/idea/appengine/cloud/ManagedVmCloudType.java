package com.google.gct.idea.appengine.cloud;

import com.google.gct.idea.ui.GoogleCloudToolsIcons;
import com.google.gct.idea.util.GctBundle;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ArtifactManager;
import com.intellij.remoteServer.RemoteServerConfigurable;
import com.intellij.remoteServer.ServerType;
import com.intellij.remoteServer.configuration.RemoteServer;
import com.intellij.remoteServer.configuration.deployment.DeploymentConfigurator;
import com.intellij.remoteServer.configuration.deployment.DeploymentSource;
import com.intellij.remoteServer.configuration.deployment.JavaDeploymentSourceUtil;
import com.intellij.remoteServer.runtime.ServerConnector;
import com.intellij.remoteServer.runtime.ServerTaskExecutor;
import com.intellij.remoteServer.runtime.deployment.DeploymentLogManager;
import com.intellij.remoteServer.runtime.deployment.DeploymentRuntime;
import com.intellij.remoteServer.runtime.deployment.DeploymentTask;
import com.intellij.remoteServer.runtime.deployment.ServerRuntimeInstance;
import com.intellij.remoteServer.runtime.log.LoggingHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;


/**
 *
 */
public class ManagedVmCloudType extends ServerType<ManagedVmServerConfiguration> {

  private final Logger logger = Logger.getInstance(ManagedVmCloudType.class);

  public ManagedVmCloudType() {
    super("app-engine-managed-vm");
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return GctBundle.message("appengine.managedvm.name");
  }

  @NotNull
  @Override
  public Icon getIcon() {
    return GoogleCloudToolsIcons.APP_ENGINE;
  }

  @NotNull
  @Override
  public ManagedVmServerConfiguration createDefaultConfiguration() {
    return new ManagedVmServerConfiguration();
  }

  @NotNull
  @Override
  public RemoteServerConfigurable createServerConfigurable(
      @NotNull ManagedVmServerConfiguration configuration) {
    return new ManagedVmCloudConfigurable(configuration, null);
  }

  @NotNull
  @Override
  public DeploymentConfigurator<?, ManagedVmServerConfiguration> createDeploymentConfigurator(
      Project project) {
    return new ManagedVmDeploymentConfigurator(project);
  }

  @NotNull
  @Override
  public ServerConnector<?> createConnector(@NotNull ManagedVmServerConfiguration configuration,
      @NotNull ServerTaskExecutor asyncTasksExecutor) {
    return new ManagedVmServerConnector(configuration);
  }

  private static class ManagedVmDeploymentConfigurator extends
      DeploymentConfigurator<ManagedVmDeploymentConfiguration, ManagedVmServerConfiguration> {

    private final Project project;

    public ManagedVmDeploymentConfigurator(Project project) {
      this.project = project;
    }

    @NotNull
    @Override
    public List<DeploymentSource> getAvailableDeploymentSources() {
      return JavaDeploymentSourceUtil
          .getInstance().createArtifactDeploymentSources(project, getJarsAndWars());
    }

    private List<Artifact> getJarsAndWars() {
      List<Artifact> jarsAndWars = new ArrayList<Artifact>();
      for (Artifact artifact : ArtifactManager.getInstance(project).getArtifacts()) {
        if (artifact.getArtifactType().getId().equalsIgnoreCase("jar")
            || artifact.getArtifactType().getId().equalsIgnoreCase("war")) {
          jarsAndWars.add(artifact);
        }
      }

      Collections.sort(jarsAndWars, ArtifactManager.ARTIFACT_COMPARATOR);
      return jarsAndWars;
    }

    @NotNull
    @Override
    public ManagedVmDeploymentConfiguration createDefaultConfiguration(
        @NotNull DeploymentSource source) {
      return new ManagedVmDeploymentConfiguration();
    }

    @Nullable
    @Override
    public SettingsEditor<ManagedVmDeploymentConfiguration> createEditor(
        @NotNull DeploymentSource source,
        @NotNull RemoteServer<ManagedVmServerConfiguration> server) {
      return new ManagedVmDeploymentRunConfigurationEditor(project);
    }
  }

  private class ManagedVmServerConnector extends ServerConnector<ManagedVmDeploymentConfiguration> {

    private ManagedVmServerConfiguration configuration;

    public ManagedVmServerConnector(
        ManagedVmServerConfiguration configuration) {
      this.configuration = configuration;
    }

    @Override
    public void connect(@NotNull ConnectionCallback<ManagedVmDeploymentConfiguration> callback) {
      callback.connected(new ManagedVmRuntimeInstance(configuration));
    }
  }

  private static class ManagedVmRuntimeInstance extends
      ServerRuntimeInstance<ManagedVmDeploymentConfiguration> {

    private final Logger logger = Logger.getInstance(ManagedVmRuntimeInstance.class);

    private ManagedVmServerConfiguration configuration;

    public ManagedVmRuntimeInstance(
        ManagedVmServerConfiguration configuration) {
      this.configuration = configuration;
    }

    @Override
    public void deploy(@NotNull final DeploymentTask<ManagedVmDeploymentConfiguration> task,
        @NotNull final DeploymentLogManager logManager, @NotNull final DeploymentOperationCallback callback) {
      FileDocumentManager.getInstance().saveAllDocuments();

      final Runnable runnable = new Runnable() {
        public void run() {
          LoggingHandler deploymentLoggingHandler = logManager.getMainLoggingHandler();
          File deployJar = task.getSource().getFile();
          if (deployJar == null) {
            return;
          }
          File stagingDirectory;
          try {
            stagingDirectory = FileUtil.createTempDirectory("gae-mvm", null, true);
            consoleLogLn(
                "Created temporary staging directory: " + stagingDirectory.getAbsolutePath(),
                deploymentLoggingHandler
            );
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          try {
            File destination = new File(stagingDirectory, "target.jar");
            FileUtil.copy(deployJar, destination);
            consoleLogLn("Copied target artifact %s to %s", deploymentLoggingHandler, deployJar.getAbsolutePath(), destination.getAbsolutePath());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          String appYamlPath = task.getConfiguration().getAppYamlPath();
          File appYaml = new File(appYamlPath);
          if (!appYaml.exists()) {
            throw new RuntimeException(appYamlPath + " does not exist");
          }
          String dockerFilePath = task.getConfiguration().getDockerFilePath();
          File dockerFile = new File(dockerFilePath);
          if (!dockerFile.exists()) {
            throw new RuntimeException(dockerFilePath + " does not exist");
          }
          try {
            File destination = new File(stagingDirectory, "app.yaml");
            FileUtil.copy(appYaml, destination);
            consoleLogLn("Copied app.yaml %s to %s", deploymentLoggingHandler, appYaml.getAbsolutePath(), destination.getAbsolutePath());

          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          try {
            File destination = new File(stagingDirectory, "Dockerfile");
            FileUtil.copy(dockerFile, destination);
            consoleLogLn("Copied Dockerfile %s to %s", deploymentLoggingHandler, dockerFile.getAbsolutePath(), destination.getAbsolutePath());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }

          GeneralCommandLine commandLine = new GeneralCommandLine(
              configuration.getCloudSdkLocation());
          commandLine.addParameters("preview", "app", "deploy", "app.yaml", "--promote", "--quiet");
          commandLine.addParameter("--project=" + configuration.getCloudProjectName());
          commandLine.withWorkDirectory(stagingDirectory);
          consoleLogLn("Working directory set to: " + stagingDirectory.getAbsolutePath(),
              deploymentLoggingHandler);
          commandLine.withParentEnvironmentType(ParentEnvironmentType.CONSOLE);
          Process process = null;
          try {
            consoleLogLn("Executing: " + commandLine.getCommandLineString(),
                deploymentLoggingHandler
            );
            process = commandLine.createProcess();
          } catch (ExecutionException e) {
            throw new RuntimeException(e);
          }
/*          logManager
              .addTerminal("Managed VM Deployment", process.getInputStream(),
                  process.getOutputStream());*/
          final ProcessHandler processHandler = new OSProcessHandler(process,
              commandLine.getCommandLineString());
          deploymentLoggingHandler.attachToProcess(processHandler);
          processHandler.addProcessListener(new ProcessAdapter() {
            @Override
            public void processTerminated(ProcessEvent event) {
              if (event.getExitCode() == 0) {
                callback.succeeded(new DeploymentRuntime() {
                  @Override
                  public boolean isUndeploySupported() {
                    return false;
                  }

                  @Override
                  public void undeploy(@NotNull UndeploymentTaskCallback callback) {
                    throw new UnsupportedOperationException();
                  }
                });
              } else {
                callback.errorOccurred("Deployment failed with exit code: " + event.getExitCode());
              }
            }
          });
          processHandler.startNotify();
        }
      };
      ProgressManager.getInstance().run(new Task.Backgroundable(task.getProject(), "Deploying to MVM", true,
          null) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          ApplicationManager.getApplication().invokeLater(runnable);
        }
      });
    }

    private void consoleLogLn(String message, LoggingHandler deploymentLoggingHandler,
        String... arguments) {
      deploymentLoggingHandler.print(String.format(message + "\n", arguments));
    }


    @Override
    public void computeDeployments(@NotNull ComputeDeploymentsCallback callback) {
    }

    @Override
    public void disconnect() {
    }
  }
}
