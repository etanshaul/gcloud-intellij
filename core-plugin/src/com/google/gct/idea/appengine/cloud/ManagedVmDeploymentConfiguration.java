package com.google.gct.idea.appengine.cloud;

import com.intellij.remoteServer.util.CloudDeploymentNameConfiguration;
import com.intellij.util.xmlb.annotations.Attribute;

/**
 * TODO: Describe this class well.
 */
public class ManagedVmDeploymentConfiguration extends CloudDeploymentNameConfiguration<ManagedVmDeploymentConfiguration> {
    private String dockerFilePath;
    private String appYamlPath;

    @Attribute("dockerFilePath")
    public String getDockerFilePath() {
        return dockerFilePath;
    }

    @Attribute("appYamlPath")
    public String getAppYamlPath() {
        return appYamlPath;
    }

    public void setDockerFilePath(String dockerFilePath) {
        this.dockerFilePath = dockerFilePath;
    }

    public void setAppYamlPath(String appYamlPath) {
        this.appYamlPath = appYamlPath;
    }
}
