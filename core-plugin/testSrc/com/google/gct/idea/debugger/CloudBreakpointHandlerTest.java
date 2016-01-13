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
package com.google.gct.idea.debugger;

import com.google.api.services.clouddebugger.model.Breakpoint;
import com.google.api.services.clouddebugger.model.SourceLocation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gct.idea.debugger.CloudDebugProcessStateController.SetBreakpointHandler;

import com.intellij.ide.projectWizard.ModuleTypeCategory.Java;
import com.intellij.mock.MockProjectEx;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.IdeaTestCase;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.google.gct.idea.debugger.CloudBreakpointHandler}
 */
public class CloudBreakpointHandlerTest extends UsefulTestCase {

  private final Ref<Breakpoint> myAddedBp = new Ref<Breakpoint>();
  private final Ref<String> myRemovedBp = new Ref<String>();
  private final String NO_CONDITION = null;
  private final String[] NO_WATCHES = null;
  private MockProjectEx myProject;
  private CloudDebugProcess myProcess;
  private ArrayList<Breakpoint> myExistingBreakpoints;
  private PsiManager myPsiManager;
  private String myDesiredresultId;
  private IdeaProjectTestFixture myFixture;
  private CloudBreakpointHandler myHandler;
  private CloudDebugProcessStateController stateController;
  private XBreakpointManager breakpointManager;

  @SuppressWarnings("JUnitTestCaseWithNonTrivialConstructors")
  public CloudBreakpointHandlerTest() {
    IdeaTestCase.initPlatformPrefix();
  }


  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(
        getTestName(true)).getFixture();
    myFixture.setUp();

    myProject = new MockProjectEx(getTestRootDisposable());

    myPsiManager = Mockito.mock(PsiManager.class);
    myProject.registerService(PsiManager.class, myPsiManager);

    XDebugSession session = Mockito.mock(XDebugSession.class);
    when(session.getProject()).thenReturn(myProject);

    myProcess = Mockito.mock(CloudDebugProcess.class);
    when(myProcess.getXDebugSession()).thenReturn(session);
    CloudDebugProcessState processState = Mockito.mock(CloudDebugProcessState.class);
    myExistingBreakpoints = new ArrayList<Breakpoint>();
    when(processState.getCurrentServerBreakpointList()).thenReturn(
      ContainerUtil.immutableList(myExistingBreakpoints));
    when(myProcess.getProcessState()).thenReturn(processState);

    stateController = Mockito.mock(CloudDebugProcessStateController.class);
    when(myProcess.getStateController()).thenReturn(stateController);

    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        myAddedBp.set((Breakpoint)invocation.getArguments()[0]);
        SetBreakpointHandler handler = (SetBreakpointHandler)invocation.getArguments()[1];
        handler.onSuccess(myDesiredresultId);
        return null;
      }
    }).when(stateController).setBreakpointAsync(
        any(Breakpoint.class), any(SetBreakpointHandler.class));

    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        myRemovedBp.set((String)invocation.getArguments()[0]);
        return null;
      }
    }).when(stateController).deleteBreakpointAsync(anyString());

    myHandler = new CloudBreakpointHandler(myProcess);

    XDebuggerManager debuggerManager = Mockito.mock(XDebuggerManager.class);
    myProject.addComponent(XDebuggerManager.class, debuggerManager);
    breakpointManager = Mockito.mock(XBreakpointManager.class);
    when(debuggerManager.getBreakpointManager()).thenReturn(breakpointManager);
  }

  @Override
  protected void tearDown() throws Exception {
    myFixture.tearDown();
    myFixture = null;
    super.tearDown();
  }

  public void testSimpleBreakpointRegister() {
    registerMockBreakpoint(new String[]{"foowatch1"}, "condition == true", 123,
        "foo.java", "com.google", false, "b_id");

    assertNull(myRemovedBp.get());
    assertNotNull(myAddedBp.get());
    assertContainsElements(myAddedBp.get().getExpressions(), "foowatch1");
    assertTrue(myAddedBp.get().getLocation().getLine() == 124);
    assertTrue(myAddedBp.get().getLocation().getPath().equals("com/google/foo.java"));
    assertTrue(myAddedBp.get().getCondition().equals("condition == true"));
  }

  public void ignore_testRegisterGetAndDelete() {
    registerMockBreakpoint(new String[]{"foowatch1"}, "condition == true", 123,
        "foo.java", "com.google", false, "b_id");

    assertNull(myRemovedBp.get());
    assertNotNull(myAddedBp.get());
    assertContainsElements(myAddedBp.get().getExpressions(), "foowatch1");
    assertTrue(myAddedBp.get().getLocation().getLine() == 124);
    assertTrue(myAddedBp.get().getLocation().getPath().equals("com/google/foo.java"));
    assertTrue(myAddedBp.get().getCondition().equals("condition == true"));

    myAddedBp.get().setId("b_id");

    XBreakpoint xideBreakpoint = myHandler.getXBreakpoint(myAddedBp.get());
    assertNotNull(xideBreakpoint);
    myHandler.deleteBreakpoint(myAddedBp.get());

    assertNotNull(myRemovedBp.get());
    assertTrue(myRemovedBp.get() == myAddedBp.get().getId());
  }

  public void ignore_testServerCreation() {
    registerMockBreakpoint(new String[]{"foowatch1"}, "condition == true", 123,
        "foo.java", "com.google", true, "b_id");

    assertNull(myAddedBp.get());
  }

  public void ignore_testConflictingRegister() {
    Breakpoint existingServerBp = new Breakpoint();
    SourceLocation location = new SourceLocation();
    location.setPath("com/google/foo.java");
    location.setLine(124);
    existingServerBp.setLocation(location);
    existingServerBp.setId("todelete");
    myExistingBreakpoints.add(existingServerBp);

    registerMockBreakpoint(new String[]{"foowatch1"}, "condition == true", 123,
        "foo.java", "com.google", false, "b_id");

    myExistingBreakpoints.clear();

    assertNotNull(myAddedBp.get());
    assertContainsElements(myAddedBp.get().getExpressions(), "foowatch1");
    assertTrue(myAddedBp.get().getLocation().getLine() == 124);
    assertTrue(myAddedBp.get().getLocation().getPath().equals("com/google/foo.java"));
    assertTrue(myAddedBp.get().getCondition().equals("condition == true"));
  }

  // Tries to register an already registered breakpoint and only first registration goes through.
  // https://github.com/GoogleCloudPlatform/gcloud-intellij/issues/142
  public void testRegisterRegisteredBreakpoint() {
    XLineBreakpointImpl breakpoint = registerMockBreakpoint(
        NO_WATCHES, NO_CONDITION, 13, "fileName", "packageName", false, "12abc");
    myHandler.registerBreakpoint(breakpoint);
    verify(stateController, times(1)).setBreakpointAsync(
        isA(Breakpoint.class), isA(SetBreakpointHandler.class));
  }

  @SuppressWarnings("unchecked")
  private XLineBreakpointImpl registerMockBreakpoint(String[] watches,
                                      String condition,
                                      int sourceLine,
                                      String shortFileName,
                                      String packageName,
                                      boolean createdByServer,
                                      String desiredresultId) {
    myDesiredresultId = desiredresultId;
    myAddedBp.set(null);
    myRemovedBp.set(null);

    CloudLineBreakpointProperties properties = new CloudLineBreakpointProperties();
    properties.setWatchExpressions(watches);
    properties.setCreatedByServer(createdByServer);

    XLineBreakpointImpl lineBreakpoint = Mockito.mock(XLineBreakpointImpl.class);
    XExpression expression = Mockito.mock(XExpression.class);
    when(expression.getExpression()).thenReturn(condition);
    when(lineBreakpoint.getConditionExpression()).thenReturn(expression);
    when(lineBreakpoint.getProperties()).thenReturn(properties);
    when(lineBreakpoint.isEnabled()).thenReturn(Boolean.TRUE);
    when(lineBreakpoint.getType()).thenReturn(CloudLineBreakpointType.getInstance());

    VirtualFile mockFile = Mockito.mock(VirtualFile.class);
    XSourcePosition sourcePosition = Mockito.mock(XSourcePosition.class);
    when(sourcePosition.getLine()).thenReturn(sourceLine);
    when(sourcePosition.getFile()).thenReturn(mockFile);
    when(mockFile.isValid()).thenReturn(Boolean.TRUE);

    when(lineBreakpoint.getSourcePosition()).thenReturn(sourcePosition);

    PsiJavaFile psiJavaFile = Mockito.mock(PsiJavaFile.class);
    when(psiJavaFile.getPackageName()).thenReturn(packageName);
    when(psiJavaFile.getName()).thenReturn(shortFileName);
    when(myPsiManager.findFile(mockFile)).thenReturn(psiJavaFile);
    myHandler.setPsiManager(myPsiManager);

    CloudLineBreakpointType.CloudLineBreakpoint javaBreakpoint =
      (CloudLineBreakpointType.CloudLineBreakpoint) CloudLineBreakpointType
          .getInstance().createJavaBreakpoint(myProject, lineBreakpoint);
    when(lineBreakpoint.getUserData(
        com.intellij.debugger.ui.breakpoints.Breakpoint.DATA_KEY)).thenReturn(javaBreakpoint);

    myHandler.registerBreakpoint(lineBreakpoint);
    return lineBreakpoint;
  }

  public void testCropCloudPath() {
    assertEquals(myHandler.cropCloudPath("app/module/src/main/java/bar/foo/package/Class.java"),
        "bar/foo/package/Class.java");

    assertEquals(myHandler.cropCloudPath("app/module/src/abcdefg/java/bar/foo/package/Class.java"),
        "app/module/src/abcdefg/java/bar/foo/package/Class.java");
  }

  public void testCreateIdeRepresentationsIfNecessary() {
    List<Breakpoint> breakpoints = ImmutableList.of(
        new Breakpoint().setId("expected_path").setLocation(
            new SourceLocation().setLine(1).setPath("app/mod/src/main/java/b/f/pkg/Class.java")),
        new Breakpoint().setId("unexpected_path").setLocation(
            new SourceLocation().setLine(2).setPath("app/mod/src/m/j/a/b/c/Class.java")),
        new Breakpoint().setId("unexpected_good_path").setLocation(
            new SourceLocation().setLine(3).setPath("b/f/pkg/Class.java")
        ));
    when(breakpointManager.findBreakpointAtLine(
        isA(XLineBreakpointType.class), isA(VirtualFile.class), anyInt())).thenReturn(null);
    XLineBreakpoint mockLineBreakpoint = Mockito.mock(XLineBreakpoint.class);
    when(breakpointManager.addLineBreakpoint(isA(XLineBreakpointType.class), anyString(), anyInt(),
        isA(CloudLineBreakpointProperties.class))).thenReturn(mockLineBreakpoint);
    when(mockLineBreakpoint.getProperties()).thenReturn(new CloudLineBreakpointProperties());

    VirtualFile virtualFile = Mockito.mock(VirtualFile.class);
    when(virtualFile.getUrl()).thenReturn("URL");
    Map<String, VirtualFile> locations = ImmutableMap.of("b/f/pkg/Class.java", virtualFile);
    JavaUtil.initializeLocationsOnUnitTest(myProject, locations);

    myHandler.createIdeRepresentationsIfNecessary(breakpoints);

    verify(breakpointManager, times(2)).addLineBreakpoint(isA(XLineBreakpointType.class),
        anyString(), anyInt(), isA(XBreakpointProperties.class));
  }
}
