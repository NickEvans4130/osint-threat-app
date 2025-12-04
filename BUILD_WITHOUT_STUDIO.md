# Building Without Android Studio - Troubleshooting

Unfortunately, there's a known issue with KSP (Kotlin Symbol Processing) on Windows when building via command line that causes the error:
```
java.io.IOException: The filename, directory name, or volume label syntax is incorrect
```

This is a Windows-specific path handling bug in the KSP Gradle plugin that occurs outside of Android Studio.

## Solutions

### Option 1: Use Android Studio (Recommended - Works 100%)

The easiest and most reliable way to build the APK:

1. Download Android Studio: https://developer.android.com/studio
2. Install and open Android Studio
3. Open this project: `File → Open → Select this folder`
4. Wait for Gradle sync (5-10 minutes first time)
5. Build: `Build → Build Bundle(s) / APK(s) → Build APK(s)`
6. APK will be at: `app\build\outputs\apk\debug\app-debug.apk`

### Option 2: Use Android Studio CLI (After Installing Studio)

After installing Android Studio, you can use its bundled Gradle:

```bash
cd "C:\Users\nicke\Documents\Projects\Personal\OSINT App"

# Use Android Studio's gradle
"C:\Program Files\Android\Android Studio\jbr\bin\java.exe" -jar gradle\wrapper\gradle-wrapper.jar assembleDebug
```

### Option 3: Modify Project to Remove KSP

If you must build via command line, you'll need to modify the project:

1. Remove KSP from `app/build.gradle.kts`:
   - Remove line: `id("com.google.devtools.ksp")`
   - Change `ksp("androidx.room:room-compiler:2.6.1")` to `kapt("androidx.room:room-compiler:2.6.1")`
   - Add kapt plugin: `id("kotlin-kapt")`

2. Then rebuild:
   ```bash
   ./gradlew clean assembleDebug
   ```

However, this is not recommended as KAPT is deprecated and slower than KSP.

### Option 4: Use GitHub Actions (Cloud Build)

You can set up GitHub Actions to build the APK in the cloud (Linux environment where this issue doesn't occur).

Create `.github/workflows/build.yml`:
```yaml
name: Build APK

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build with Gradle
        run: ./gradlew assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

Push to GitHub and the APK will build automatically and be available for download from the Actions tab.

## Why This Happens

The KSP (Kotlin Symbol Processing) Gradle plugin has issues with Windows path handling when:
- Building from Git Bash or similar Unix-like shells on Windows
- Using certain path configurations with spaces or special characters
- Running outside of Android Studio's environment

Android Studio handles these path issues internally, which is why it works there.

## Recommended Solution

**Install Android Studio** - it's free, handles all the complexity, and provides:
- Built-in emulators for testing
- Code completion and debugging
- Visual UI designer
- APK installation to devices
- Log viewing (Logcat)

Even if you prefer command-line development, having Android Studio installed provides the most reliable build environment for Android projects.

## Alternative: Build on Linux/Mac

If you have access to Linux or Mac, the command-line build works fine:
```bash
./gradlew assembleDebug
```

The KSP path issue only affects Windows command-line builds.
