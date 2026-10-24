#!/bin/bash
export JAVA_HOME=/workspace/tools/jdk17
export PATH=$JAVA_HOME/bin:$PATH
export ANDROID_HOME=/workspace/tools/android-sdk
export GRADLE_USER_HOME=/workspace/.gradle
cd /workspace/app
/workspace/tools/gradle-8.7/bin/gradle "$@" --console=plain --no-daemon
