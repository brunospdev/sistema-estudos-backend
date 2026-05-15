#!/bin/sh
# Maven Wrapper Script
# Licensed to the Apache Software Foundation (ASF)

if [ -z "$JAVA_HOME" ] ; then
  JAVACMD=`which java`
else
  JAVACMD="$JAVA_HOME/bin/java"
fi

MAVEN_PROJECTBASEDIR="${0%/*}"
MAVEN_WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

# Download wrapper jar if not present
if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
  WRAPPER_URL=`grep wrapperUrl "$MAVEN_WRAPPER_PROPERTIES" | cut -d= -f2`
  echo "Downloading Maven Wrapper from: $WRAPPER_URL"
  if command -v curl > /dev/null; then
    curl -o "$MAVEN_WRAPPER_JAR" "$WRAPPER_URL" -s
  elif command -v wget > /dev/null; then
    wget "$WRAPPER_URL" -O "$MAVEN_WRAPPER_JAR" -q
  fi
fi

exec "$JAVACMD" -classpath "$MAVEN_WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
