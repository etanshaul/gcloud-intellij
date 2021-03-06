# Contributing to Cloud Tools for IntelliJ IDEA

Cloud Tools for IntelliJ IDEA is an open source project.
We appreciate your help!


## Filing issues

When filing an issue, please answer these questions:

1. What version of IDEA are you using?
2. What version of Java are you using?
3. What did you do?
4. What did you expect to see?
5. What did you see instead?


## Building

All code can be checked out from our Github repository in the usual way.
That is, clone the repository with HTTPS or SSH:


```
$ git clone https://github.com/GoogleCloudPlatform/gcloud-intellij.git
Cloning into 'gcloud-intellij'...
```

There are four modules:

* google-account-plugin:
* core-plugin: Cloud debugger and code inspections
* common-lib: Code shared between the above two
* common-test-lib: test infrastructure code

When adding new dependencies, the jar files are loaded from Maven Central
when available. If Maven Central doesn't have the necessary version,
the jar is placed in the third_party directory.

On Linux/Mac OS X we use gradle as our build system. 
Gradle is self-installing. This one command

$ ./gradlew buildPlugin

compiles everything, runs the tests, and builds the plugins. The output appears in
google-account-plugin/build/distributions and core-plugin/build/distributions.

Other useful targets while developing include:

* $ ./gradlew compileJava: compile
* $ ./gradlew test: run tests
* $ ./gradlew check: run static analysis tools
* $ ./gradlew clean: remove all build artifacts
* $ ./gradlew runIdea: run IntelliJ preconfigured with the plugins from this project.

## Configuring and Debugging in IntelliJ

### Import Project 

1. 'New project from existing sources'
1. Select the root build.gradle file to import
1. Git revert changes to the .idea folder because IDEA Gradle import blows it away
 (https://youtrack.jetbrains.com/issue/IDEA-146295)
1. Run or debug the **Cloud Tools on IntelliJ** run configuration

## Contributing code

1. First, please [sign either individual or corporate contributor license agreement](https://cla.developers.google.com/), whichever is applicable.
2. Set your git user.email property to the address used for step 1. E.g.
   ```
   git config --global user.email "janedoe@google.com"
   ```
   If you're a Googler or other corporate contributor, 
   use your corporate email address here, not your personal address. 
3. Fork the repository into your own Github account.
4. Please include unit tests for all new code. (Yes, we know not all 
   existing code has tests. We're slowly fixing that, and contributions of tests
   for existing code are much appreciated.
5. Make sure all existing tests pass. (gradlew test)
6. Associate the change with an existing issue or [file a new issue](https://github.com/GoogleCloudPlatform/gcloud-intellij/issues/new). 
7. Create a pull request and send it to gcloud-intellij:master. 


Unless otherwise noted, our source files are distributed under
the Apache license found in the LICENSE file.

A number of issues in the 
[issue tracker](https://github.com/GoogleCloudPlatform/gcloud-intellij/issues/new)
have been tagged as "[Help Wanted](https://github.com/GoogleCloudPlatform/gcloud-intellij/labels/help%20wanted)." 
These are relatively small, self-contained changes that are good places to start. 

## FAQ

### java.lang.OutOfMemoryError: PermGen space when running the tests inside IDEA

From the Run menu select “Edit Configurations...” In the "VM options" field add -XX:MaxPermSize=256m (and if that doesn't work try 512m instead).


