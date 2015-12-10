package com.google.gct.idea.appengine.cloud;

import com.google.gct.idea.elysium.ProjectSelector;
import com.google.gct.idea.util.GctBundle;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.remoteServer.RemoteServerConfigurable;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * TODO: Add class comments.
 */
public class ManagedVmCloudConfigurable extends RemoteServerConfigurable implements Configurable {

    private final ManagedVmServerConfiguration configuration;
    @Nullable
    private final Project project;

    private JPanel myMainPanel;
    private TextFieldWithBrowseButton cloudSdkLocationField;
    private ProjectSelector projectSelector;

    public ManagedVmCloudConfigurable(ManagedVmServerConfiguration configuration, @Nullable Project project) {
        this.configuration = configuration;
        this.project = project;

        cloudSdkLocationField.addBrowseFolderListener(
                GctBundle.message("appengine.cloudsdk.location.field"),
                null,
                project,
                FileChooserDescriptorFactory.createSingleFileDescriptor()
        );
    }

    @Nls
    @Override
    public String getDisplayName() {
        return GctBundle.message("appengine.managedvm.name");
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return myMainPanel;
    }

    @Override
    public boolean isModified() {
        return !Comparing.strEqual(getCloudSdkLocation(), configuration.getCloudSdkLocation()) ||
                !Comparing.strEqual(getCloudProjectName(), configuration.getCloudProjectName());
    }

    @Override
    public void apply() throws ConfigurationException {
        configuration.setCloudProjectName(projectSelector.getText());
        configuration.setCloudSdkLocation(cloudSdkLocationField.getText());
    }

    @Override
    public void reset() {
        cloudSdkLocationField.setText(configuration.getCloudSdkLocation());
        projectSelector.setText(configuration.getCloudProjectName());
    }

    /**
     * We don't need to test theconnection if we know the cloud SDK, user, and project ID are valid.
     */
    @Override
    public boolean canCheckConnection() {
        return false;
    }

    public String getCloudSdkLocation() {
        return cloudSdkLocationField.getText();
    }

    public String getCloudProjectName() {
        return projectSelector.getText();
    }
}
