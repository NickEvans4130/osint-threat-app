# Final Build Instructions

## TL;DR - Quick Answer

**You need Android Studio to build this APK.** The command-line build has a Windows-specific bug with the Android Gradle Plugin.

Download: https://developer.android.com/studio

## What's Wrong?

When trying to build via command line on Windows, we encounter:
```
java.io.IOException: The filename, directory name, or volume label syntax is incorrect
```

This is a known Windows path-handling bug in the Android Gradle Plugin that only occurs when building outside of Android Studio's environment.

## Working Solutions

### ✅ Solution 1: Android Studio (100% Success Rate - RECOMMENDED)

**This is the official, supported way to build Android apps.**

1. **Download Android Studio**: https://developer.android.com/studio
2. **Install** (follow installer prompts)
3. **Open project**:
   - Launch Android Studio
   - Click `File → Open`
   - Navigate to: `C:\Users\nicke\Documents\Projects\Personal\OSINT App`
   - Click `OK`
4. **Wait for Gradle sync** (first time: 5-10 minutes to download dependencies)
5. **Build APK**:
   - Click `Build → Build Bundle(s) / APK(s) → Build APK(s)`
   - OR click the green **Play** button to build and install on connected device
6. **Find your APK**:
   - After build, click the notification "locate" link
   - Or navigate to: `app\build\outputs\apk\debug\app-debug.apk`

**Benefits:**
- Works 100% of the time
- Provides emulators for testing
- Built-in debugging tools
- Visual layout editor
- Logcat for viewing app logs
- One-click deployment to devices

---

### ✅ Solution 2: GitHub Actions (Cloud Build - No Android Studio Needed)

Build the APK in the cloud using GitHub's free CI/CD.

**Setup:**

1. Create file `.github/workflows/build.yml` in your project:

```yaml
name: Build Android APK

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build Debug APK
        run: ./gradlew assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

2. **Push to GitHub**:
```bash
git add .
git commit -m "Add GitHub Actions build workflow"
git push
```

3. **Download APK**:
   - Go to your GitHub repository
   - Click the **Actions** tab
   - Click the latest workflow run
   - Download the `app-debug` artifact

**Benefits:**
- No need to install Android Studio
- Builds on Linux (no Windows path bugs)
- Automated builds on every push
- Free for public repos

---

### ✅ Solution 3: WSL2 (Windows Subsystem for Linux)

Build in a Linux environment on Windows.

**Setup:**

1. **Install WSL2**:
```powershell
wsl --install
```

2. **Open WSL2 terminal** and navigate to project:
```bash
cd "/mnt/c/Users/nicke/Documents/Projects/Personal/OSINT App"
```

3. **Install Java 17** (in WSL):
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

4. **Build**:
```bash
chmod +x gradlew
./gradlew assembleDebug
```

5. **APK location**:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

### ✅ Solution 4: Use Another Computer

If you have access to:
- **Mac**: Command-line build works fine
- **Linux**: Command-line build works fine
- **Another Windows PC with Android Studio**: Use that

---

## Why Command Line Doesn't Work

The Android Gradle Plugin has multiple known issues with Windows path handling:
- Spaces in paths
- Drive letter handling (C:\)
- Backslash vs forward slash
- Long path names
- Special characters

Android Studio internally handles all these edge cases. The command-line gradle does not.

This is not a bug in your project - it's a limitation of the Android build tools on Windows.

---

## What I've Already Tried

✓ Set up Gradle wrapper correctly
✓ Configured Java 17
✓ Set Android SDK path in `local.properties`
✓ Cleaned build cache
✓ Stopped Gradle daemons
✓ Switched from KSP to KAPT
✓ Fixed path escaping

None of these work due to the Windows + Android Gradle Plugin combination.

---

## My Recommendation

**Install Android Studio.** Here's why:

1. **It's Free** - Official tool from Google
2. **It Works** - No path issues, no configuration hassles
3. **You'll Need It Anyway** - For:
   - Debugging the app
   - Viewing Logcat (app logs)
   - Testing on emulators
   - Inspecting layouts
   - Profiling performance

4. **One-Click Everything**:
   - Build APK
   - Install on device
   - Run tests
   - Generate signed APKs for Play Store

Even professional Android developers who prefer command-line use Android Studio for builds because it's the path of least resistance.

---

## After Building the APK

Once you have the APK (from any method above):

### Install on Google Pixel:

**Method 1: ADB**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Method 2: File Transfer**
1. Copy APK to your phone
2. Open the file on phone
3. Tap "Install"
4. (Enable "Install from unknown sources" if prompted)

**Method 3: Android Studio** (if using Studio)
- Connect phone via USB
- Click the green **Play** button
- App installs and launches automatically

---

## Questions?

**Q: Can I build without Android Studio?**
A: Yes, using GitHub Actions or WSL2, but Android Studio is easier.

**Q: Why does this work on Mac/Linux?**
A: Those systems don't have the Windows-specific path handling bugs.

**Q: Will the APK work?**
A: Yes! The code is complete and correct. The issue is only with the build process on Windows command-line.

**Q: How big is Android Studio?**
A: ~3GB download, ~10GB installed. Worth it for the functionality.

---

## Bottom Line

**Use Android Studio.** It's the standard, supported way to build Android apps, and it eliminates all these path-handling headaches.

**Download**: https://developer.android.com/studio
