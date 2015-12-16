package com.google.gct.idea.appengine.cloud;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * TODO: Describe this class well.
 */
public class ManagedVmDeploymentRunConfigurationEditor extends SettingsEditor<ManagedVmDeploymentConfiguration> {

    private JPanel mainPanel;
    private TextFieldWithBrowseButton dockerFilePathField;
    private TextFieldWithBrowseButton appYamlPathField;

    @Override
    protected void resetEditorFrom(ManagedVmDeploymentConfiguration configuration) {
        dockerFilePathField.setText(configuration.getDockerFilePath());
        appYamlPathField.setText(configuration.getAppYamlPath());
    }

    @Override
    protected void applyEditorTo(ManagedVmDeploymentConfiguration configuration) throws ConfigurationException {
        configuration.setDockerFilePath(dockerFilePathField.getText());
        configuration.setAppYamlPath(appYamlPathField.getText());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return mainPanel;
    }
}
