package com.google.gct.idea.appengine.cloud;

import com.google.gct.idea.util.GctBundle;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * TODO: Describe this class well.
 */
public class ManagedVmDeploymentRunConfigurationEditor extends
    SettingsEditor<ManagedVmDeploymentConfiguration> {

  private final Project project;

  private JPanel mainPanel;
  private TextFieldWithBrowseButton dockerFilePathField;
  private TextFieldWithBrowseButton appYamlPathField;

  public ManagedVmDeploymentRunConfigurationEditor(Project project) {
    this.project = project;

    dockerFilePathField.addBrowseFolderListener(
        GctBundle.message("appengine.dockerfile.location.browse.button"),
        null,
        project,
        FileChooserDescriptorFactory.createSingleFileDescriptor());
    appYamlPathField.addBrowseFolderListener(
        GctBundle.message("appengine.appyaml.location.browse.button"),
        null,
        project,
        FileChooserDescriptorFactory.createSingleFileDescriptor());
  }


  @Override
  protected void resetEditorFrom(ManagedVmDeploymentConfiguration configuration) {
    dockerFilePathField.setText(configuration.getDockerFilePath());
    appYamlPathField.setText(configuration.getAppYamlPath());
  }

  @Override
  protected void applyEditorTo(ManagedVmDeploymentConfiguration configuration)
      throws ConfigurationException {
    configuration.setDockerFilePath(dockerFilePathField.getText());
    configuration.setAppYamlPath(appYamlPathField.getText());
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return mainPanel;
  }
}
