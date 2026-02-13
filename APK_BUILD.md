# Build APK

This project builds an Android APK using Gradle.

## Prerequisites

- JDK 17 (`JAVA_HOME` should point to Java 17)
- Android SDK installed (`ANDROID_HOME` or `ANDROID_SDK_ROOT` configured)
- Network access to Maven repositories (`google()` and `mavenCentral()`)

## Build debug APK

```bash
scripts/build-apk.sh
```

If successful, the APK will be generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Build release APK

```bash
gradle assembleRelease
```

Release APK output path:

```text
app/build/outputs/apk/release/app-release.apk
```

> Note: If you need a signed Play Store artifact, configure signing and build an AAB with `gradle bundleRelease`.
