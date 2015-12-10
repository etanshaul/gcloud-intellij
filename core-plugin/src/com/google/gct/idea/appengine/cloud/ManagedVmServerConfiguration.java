package com.google.gct.idea.appengine.cloud;

import com.intellij.remoteServer.configuration.ServerConfigurationBase;
import com.intellij.util.xmlb.annotations.Attribute;

/**
 * TODO: Add class comments.
 */
public class ManagedVmServerConfiguration extends ServerConfigurationBase<ManagedVmServerConfiguration> {
    private String cloudSdkLocation;
    private String cloudProjectName;


    @Attribute("cloudSdkLocation")
    public String getCloudSdkLocation() {
        return cloudSdkLocation;
    }

    @Attribute("cloudProjectName")
    public String getCloudProjectName() {
        return cloudProjectName;
    }

    public void setCloudSdkLocation(String cloudSdkLocation) {
        this.cloudSdkLocation = cloudSdkLocation;
    }

    public void setCloudProjectName(String cloudProjectName) {
        this.cloudProjectName = cloudProjectName;
    }
}
