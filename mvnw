#!/bin/sh
# Maven Wrapper Script

if [ -z "$JAVA_HOME" ]; then
  JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java) 2>/dev/null || which java)))
fi

MAVEN_WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES=".mvn/wrapper/maven-wrapper.properties"

# Download maven wrapper jar if not present
if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
  WRAPPER_URL=$(grep wrapperUrl "$MAVEN_WRAPPER_PROPERTIES" | cut -d= -f2)
  curl -o "$MAVEN_WRAPPER_JAR" "$WRAPPER_URL" 2>/dev/null || wget -O "$MAVEN_WRAPPER_JAR" "$WRAPPER_URL" 2>/dev/null
fi

# Get distribution URL
DIST_URL=$(grep distributionUrl "$MAVEN_WRAPPER_PROPERTIES" | cut -d= -f2)
MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-3.9.6"

if [ ! -d "$MAVEN_HOME/bin" ]; then
  mkdir -p "$MAVEN_HOME"
  TEMP_ZIP="$MAVEN_HOME/maven.zip"
  curl -L -o "$TEMP_ZIP" "$DIST_URL" 2>/dev/null
  unzip -q "$TEMP_ZIP" -d "$MAVEN_HOME" 2>/dev/null
  rm -f "$TEMP_ZIP"
  EXTRACTED=$(ls "$MAVEN_HOME" | grep apache-maven | head -1)
  if [ -n "$EXTRACTED" ]; then
    mv "$MAVEN_HOME/$EXTRACTED"/* "$MAVEN_HOME/" 2>/dev/null
    rm -rf "$MAVEN_HOME/$EXTRACTED" 2>/dev/null
  fi
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
