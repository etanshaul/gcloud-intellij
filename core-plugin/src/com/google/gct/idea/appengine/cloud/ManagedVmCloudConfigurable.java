package com.google.gct.idea.appengine.cloud;

import com.google.gct.idea.appengine.util.CloudSdkUtil;
import com.google.gct.idea.elysium.ProjectSelector;
import com.google.gct.idea.util.GctBundle;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.remoteServer.RemoteServerConfigurable;
import com.intellij.ui.DocumentAdapter;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * TODO: Add class comments.
 */
public class ManagedVmCloudConfigurable extends RemoteServerConfigurable implements Configurable {

    private final ManagedVmServerConfiguration configuration;
    @Nullable
    private final Project project;

    private String displayName = GctBundle.message("appengine.managedvm.name");
    private JPanel myMainPanel;
    private TextFieldWithBrowseButton cloudSdkLocationField;
    private ProjectSelector projectSelector;

    public ManagedVmCloudConfigurable(ManagedVmServerConfiguration configuration, @Nullable Project project) {
        this.configuration = configuration;
        this.project = project;

        String cloudSdkPath = CloudSdkUtil.findCloudSdkPath();
        if (cloudSdkPath != null && configuration.getCloudSdkLocation() == null) {
            configuration.setCloudSdkLocation(cloudSdkPath);
            cloudSdkLocationField.setText(cloudSdkPath);
        }
        cloudSdkLocationField.addBrowseFolderListener(
                GctBundle.message("appengine.cloudsdk.location.browse.button"),
                null,
                project,
                FileChooserDescriptorFactory.createSingleFileDescriptor()
        );
        projectSelector.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                if (projectSelector != null) {
                    displayName = projectSelector.getText() + " Deployment";
                }

            }
        });

    }

    @Nls
    @Override
    public String getDisplayName() {
        return displayName;
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
