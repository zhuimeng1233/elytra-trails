#!/bin/sh
APP_HOME="$( cd "$( dirname "$( readlink -f "$0" 2>/dev/null || echo "$0" )" )" && pwd )"
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
exec java -cp "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
