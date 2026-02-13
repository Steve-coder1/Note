#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

if ! command -v gradle >/dev/null 2>&1 && [ ! -x "./gradlew" ]; then
  echo "Error: neither 'gradle' nor './gradlew' is available." >&2
  exit 1
fi

java_major_version() {
  local java_cmd="${1:-java}"
  "$java_cmd" -version 2>&1 | awk -F '[".]' '/version/ {print $2; exit}'
}

JAVA_OK=false
if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  if [ "$(java_major_version "$JAVA_HOME/bin/java")" = "17" ]; then
    JAVA_OK=true
  fi
fi

if [ "$JAVA_OK" = false ]; then
  for CANDIDATE in \
    /root/.local/share/mise/installs/java/17.0.2 \
    /root/.local/share/mise/installs/java/17 \
    /usr/lib/jvm/java-17-openjdk-amd64 \
    /usr/lib/jvm/java-17-openjdk \
    /Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
  do
    if [ -x "$CANDIDATE/bin/java" ] && [ "$(java_major_version "$CANDIDATE/bin/java")" = "17" ]; then
      export JAVA_HOME="$CANDIDATE"
      export PATH="$JAVA_HOME/bin:$PATH"
      JAVA_OK=true
      break
    fi
  done
fi

if [ "$JAVA_OK" = false ]; then
  echo "Error: Java 17 is required. Set JAVA_HOME to a JDK 17 installation." >&2
  exit 1
fi

if [ -z "${ANDROID_HOME:-}" ] && [ -n "${ANDROID_SDK_ROOT:-}" ]; then
  export ANDROID_HOME="$ANDROID_SDK_ROOT"
fi

if [ -z "${ANDROID_HOME:-}" ]; then
  echo "Warning: ANDROID_HOME is not set. Make sure Android SDK is configured in your environment/local.properties." >&2
fi

GRADLE_CMD="./gradlew"
if [ ! -x "$GRADLE_CMD" ]; then
  GRADLE_CMD="gradle"
fi

echo "Using JAVA_HOME=${JAVA_HOME}"
echo "Using $GRADLE_CMD to build APK..."

"$GRADLE_CMD" assembleDebug

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
  echo "APK built successfully: $ROOT_DIR/$APK_PATH"
else
  echo "Build finished, but APK was not found at expected path: $APK_PATH" >&2
  exit 2
fi
