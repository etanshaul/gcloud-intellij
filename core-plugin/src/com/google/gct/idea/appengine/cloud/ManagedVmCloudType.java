package com.google.gct.idea.appengine.cloud;

import com.google.gct.idea.ui.GoogleCloudToolsIcons;
import com.google.gct.idea.util.GctBundle;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ArtifactManager;
import com.intellij.remoteServer.RemoteServerConfigurable;
import com.intellij.remoteServer.ServerType;
import com.intellij.remoteServer.configuration.RemoteServer;
import com.intellij.remoteServer.configuration.deployment.DeploymentConfigurator;
import com.intellij.remoteServer.configuration.deployment.DeploymentSource;
import com.intellij.remoteServer.configuration.deployment.DummyDeploymentConfiguration;
import com.intellij.remoteServer.configuration.deployment.JavaDeploymentSourceUtil;
import com.intellij.remoteServer.runtime.ServerConnector;
import com.intellij.remoteServer.runtime.ServerTaskExecutor;
import com.intellij.remoteServer.runtime.deployment.DeploymentLogManager;
import com.intellij.remoteServer.runtime.deployment.DeploymentTask;
import com.intellij.remoteServer.runtime.deployment.ServerRuntimeInstance;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
      return new ManagedVmDeploymentRunConfigurationEditor();
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
    public void deploy(@NotNull DeploymentTask<ManagedVmDeploymentConfiguration> task,
        @NotNull DeploymentLogManager logManager, @NotNull DeploymentOperationCallback callback) {
        logger.info("Fastest deployment ever!");
    }


    @Override
    public void computeDeployments(@NotNull ComputeDeploymentsCallback callback) {

    }

    @Override
    public void disconnect() {

    }
  }
}
