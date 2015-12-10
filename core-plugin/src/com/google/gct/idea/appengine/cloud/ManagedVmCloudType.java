package com.google.gct.idea.appengine.cloud;

import com.google.gct.idea.ui.GoogleCloudToolsIcons;
import com.google.gct.idea.util.GctBundle;

import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.remoteServer.RemoteServerConfigurable;
import com.intellij.remoteServer.ServerType;
import com.intellij.remoteServer.configuration.RemoteServer;
import com.intellij.remoteServer.configuration.deployment.DeploymentConfigurator;
import com.intellij.remoteServer.configuration.deployment.DeploymentSource;
import com.intellij.remoteServer.configuration.deployment.DummyDeploymentConfiguration;
import com.intellij.remoteServer.runtime.ServerConnector;
import com.intellij.remoteServer.runtime.ServerTaskExecutor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.swing.*;


/**
 *
 */
public class ManagedVmCloudType extends ServerType<ManagedVmServerConfiguration> {
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
    public RemoteServerConfigurable createServerConfigurable(@NotNull ManagedVmServerConfiguration configuration) {
        return new ManagedVmCloudConfigurable(configuration, null);
    }

    @NotNull
    @Override
    public DeploymentConfigurator<?, ManagedVmServerConfiguration> createDeploymentConfigurator(Project project) {
        return null;
    }

    @NotNull
    @Override
    public ServerConnector<?> createConnector(@NotNull ManagedVmServerConfiguration configuration, @NotNull ServerTaskExecutor asyncTasksExecutor) {
        return null;
    }

    private static class ManagedVmDeploymentConfigurator extends DeploymentConfigurator<DummyDeploymentConfiguration, ManagedVmServerConfiguration> {

        private final Project project;

        public ManagedVmDeploymentConfigurator(Project project) {
            this.project = project;
        }

        @NotNull
        @Override
        public List<DeploymentSource> getAvailableDeploymentSources() {
            return null;
        }

        @NotNull
        @Override
        public DummyDeploymentConfiguration createDefaultConfiguration(@NotNull DeploymentSource source) {
            return null;
        }

        @Nullable
        @Override
        public SettingsEditor<DummyDeploymentConfiguration> createEditor(@NotNull DeploymentSource source, @NotNull RemoteServer<ManagedVmServerConfiguration> server) {
            return null;
        }
    }

}
