package com.google.gct.idea.appengine.util;

import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.util.SystemInfo;

import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * TODO: Add class comments.
 */
public class CloudSdkUtil {

  private static final String UNIX_COMMAND = "gcloud";
  private static final String WIN_COMMAND = "gcloud.cmd";

  @Nullable
  public static String findCloudSdkPath() {
    File gcloudPath = PathEnvironmentVariableUtil
        .findInPath(SystemInfo.isWindows ? WIN_COMMAND : UNIX_COMMAND);
    if (gcloudPath != null) {
      return gcloudPath.getAbsolutePath();
    }
    return null;
  }
}
