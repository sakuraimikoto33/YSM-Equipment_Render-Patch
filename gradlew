#!/bin/sh

##############################################################################
#
# Gradle startup script for POSIX shells.
#
##############################################################################

APP_HOME=${0%"${0##*/}"}
APP_HOME=$(cd "${APP_HOME:-./}" > /dev/null && pwd -P) || exit
APP_BASE_NAME=${0##*/}

DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ]; then
    JAVACMD=$JAVA_HOME/bin/java
    if [ ! -x "$JAVACMD" ]; then
        echo
        echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
        echo
        echo "Please set the JAVA_HOME variable in your environment to match the"
        echo "location of your Java installation."
        exit 1
    fi
else
    JAVACMD=java
    if ! command -v java >/dev/null 2>&1; then
        echo
        echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
        echo
        echo "Please set the JAVA_HOME variable in your environment to match the"
        echo "location of your Java installation."
        exit 1
    fi
fi

# shellcheck disable=SC2086
exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
    "-Dorg.gradle.appname=$APP_BASE_NAME" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
