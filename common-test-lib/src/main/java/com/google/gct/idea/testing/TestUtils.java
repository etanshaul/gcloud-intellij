package com.google.gct.idea.testing;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import com.intellij.mock.MockApplicationEx;
import com.intellij.mock.MockProject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Getter;
import com.intellij.openapi.vfs.encoding.EncodingManager;
import com.intellij.openapi.vfs.encoding.EncodingManagerImpl;
import com.intellij.util.pico.DefaultPicoContainer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mockito.Mockito;
import org.picocontainer.PicoContainer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Test utilities.
 */
public class TestUtils {

  @NotNull
  public static Project mockProject() {
    return mockProject(null);
  }

  static class PluginMockApplication extends MockApplicationEx {

    private final ListeningExecutorService executor = MoreExecutors.sameThreadExecutor();

    public PluginMockApplication(@NotNull Disposable parentDisposable) {
      super(parentDisposable);
    }

    @NotNull
    @Override
    public Future<?> executeOnPooledThread(@NotNull Runnable action) {
      return executor.submit(action);
    }

    @NotNull
    @Override
    public <T> Future<T> executeOnPooledThread(@NotNull Callable<T> action) {
      return executor.submit(action);
    }
  }

  // For every #createMockApplication there needs to be a corresponding call to
  // #disposeMockApplication when the test is complete
  public static void createMockApplication() {
    Disposable disposable = Mockito.mock(Disposable.class);

    final PluginMockApplication instance = new PluginMockApplication(disposable);

    ApplicationManager.setApplication(instance,
        new Getter<FileTypeRegistry>() {
          @Override
          public FileTypeRegistry get() {
            return FileTypeManager.getInstance();
          }
        },
        disposable);
    instance.registerService(EncodingManager.class, EncodingManagerImpl.class);
  }

  public static void disposeMockApplication() {
    // Disposer.dispose(MockApplicationEx) does not do anything and setApplication takes a
    // @NotNull, so we just set the application to an empty mock. That should cause all future
    // tests to fail if they need an Application but did not set one up.
    Disposable disposable = Mockito.mock(Disposable.class);
    final MockApplicationEx instance = new MockApplicationEx(disposable);
    ApplicationManager.setApplication(instance,
        new Getter<FileTypeRegistry>() {
          @Override
          public FileTypeRegistry get() {
            return FileTypeManager.getInstance();
          }
        },
        disposable);
  }

  @NotNull
  public static MockProject mockProject(@Nullable PicoContainer container) {
    Extensions.registerAreaClass("IDEA_PROJECT", null);
    container = container != null
        ? container
        : new DefaultPicoContainer();
    return new MockProject(container, Mockito.mock(Disposable.class));
  }

  public static void assertIsSerializable(@NotNull Serializable object) {
    ObjectOutputStream out = null;
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try {
      out = new ObjectOutputStream(byteArrayOutputStream);
      out.writeObject(object);
    } catch (NotSerializableException e) {
      fail("An object is not serializable: " + e.getMessage());
    } catch (IOException e) {
      fail("Could not serialize object: " + e.getMessage());
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }
}
