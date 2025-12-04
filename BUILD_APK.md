# How to Build the APK

Since the Gradle wrapper JAR file is a binary that needs to be generated, you have two options to build the APK:

## Option 1: Build with Android Studio (Recommended)

### Steps:
1. **Open Android Studio**
2. **Open the project:**
   - File → Open
   - Navigate to: `C:\Users\nicke\Documents\Projects\Personal\OSINT App`
   - Click OK
3. **Wait for Gradle sync to complete** (first time will take several minutes to download dependencies)
4. **Build the APK:**
   - Go to: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
   - Or use menu: **Build → Generate Signed Bundle / APK** (for release APK)
5. **Find your APK:**
   - After build completes, click the notification "locate" link
   - Or manually go to: `app\build\outputs\apk\debug\app-debug.apk`

### Build Variants:
- **Debug APK**: For testing, includes debugging symbols
  - Location: `app\build\outputs\apk\debug\app-debug.apk`
- **Release APK**: Optimized for production (requires signing)
  - Location: `app\build\outputs\apk\release\app-release.apk`

---

## Option 2: Build with Command Line (After Gradle Wrapper Setup)

### Prerequisites:
You need Android SDK installed and `ANDROID_HOME` environment variable set.

### Setup Gradle Wrapper (One-time):
Open Android Studio once and let it generate the gradle wrapper, or download gradle-wrapper.jar manually:

1. Download from: https://services.gradle.org/distributions/gradle-8.2-bin.zip
2. Extract and copy `lib/gradle-wrapper.jar` to: `gradle/wrapper/gradle-wrapper.jar`

### Build Commands:
```bash
# Navigate to project directory
cd "C:\Users\nicke\Documents\Projects\Personal\OSINT App"

# Build debug APK
.\gradlew.bat assembleDebug

# Build release APK (unsigned)
.\gradlew.bat assembleRelease

# Clean build
.\gradlew.bat clean assembleDebug
```

### Output Location:
- Debug: `app\build\outputs\apk\debug\app-debug.apk`
- Release: `app\build\outputs\apk\release\app-release-unsigned.apk`

---

## Option 3: Quick Setup via Android Studio Terminal

1. Open Android Studio
2. Open the project
3. Open **Terminal** tab (bottom of Android Studio)
4. Run:
   ```bash
   gradle wrapper --gradle-version 8.2
   ```
5. This generates the complete gradle wrapper
6. Then run:
   ```bash
   .\gradlew.bat assembleDebug
   ```

---

## Installing the APK on Your Device

### Method 1: Via Android Studio (Easiest)
1. Connect your Google Pixel via USB
2. Enable USB debugging on your phone
3. Click the green **Run** button (▶) in Android Studio
4. App installs and launches automatically

### Method 2: Via ADB
```bash
# Make sure device is connected
adb devices

# Install the APK
adb install "app\build\outputs\apk\debug\app-debug.apk"

# Or to replace existing installation
adb install -r "app\build\outputs\apk\debug\app-debug.apk"
```

### Method 3: Via File Transfer
1. Copy the APK to your phone (via USB or cloud storage)
2. On your phone, open the APK file
3. Tap **Install** (you may need to enable "Install from unknown sources" in Settings)

---

## Troubleshooting

### "Gradle sync failed"
- Make sure you have Java 17 installed
- Check your internet connection (Gradle needs to download dependencies)
- Try: File → Invalidate Caches → Invalidate and Restart

### "SDK location not found"
Create a `local.properties` file in the project root with:
```
sdk.dir=C\:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk
```
(Replace with your actual Android SDK path)

### Build takes too long
First build will take 5-10 minutes to download all dependencies. Subsequent builds are much faster.

---

## Recommended: Use Android Studio

For the easiest experience, I recommend using **Android Studio** (Option 1). It handles all the Gradle wrapper setup, dependency downloads, and APK signing automatically.

Download Android Studio: https://developer.android.com/studio
