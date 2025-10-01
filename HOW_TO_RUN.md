# How to Run Trip Tracker

This guide walks you through opening and running the Trip Tracker app in Android Studio.

---

## Prerequisites

- ✅ Android Studio Hedgehog (2023.1.1) or later
- ✅ JDK 17
- ✅ Android SDK 34
- ✅ Google Maps API Key

---

## Step 1: Get Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable **"Maps SDK for Android"** API
4. Go to **Credentials** → **Create Credentials** → **API Key**
5. Copy your API key

---

## Step 2: Configure API Key

1. Open `local.properties` in the project root
2. Replace `YOUR_MAPS_API_KEY_HERE` with your actual API key:

```properties
sdk.dir=/Users/briantaylor/Library/Android/sdk
MAPS_API_KEY=AIzaSyABC123...your-actual-key
```

---

## Step 3: Open Project in Android Studio

### Option A: Command Line
```bash
cd /Users/briantaylor/trip-tracker
open -a "Android Studio" .
```

### Option B: Android Studio UI
1. Open Android Studio
2. Click **"Open"**
3. Navigate to `/Users/briantaylor/trip-tracker`
4. Click **"Open"**

---

## Step 4: Gradle Sync

1. Android Studio will automatically start Gradle sync
2. Wait for dependencies to download (first time takes 2-5 minutes)
3. You should see: **"Gradle sync finished"** in the status bar

### If Gradle Sync Fails:
- Check internet connection
- Verify JDK 17 is selected: **File → Project Structure → SDK Location**
- Click **File → Sync Project with Gradle Files**

---

## Step 5: Build the Project

### Via Android Studio:
- Click **Build → Make Project** (Cmd+F9 / Ctrl+F9)

### Via Command Line:
```bash
cd /Users/briantaylor/trip-tracker
./gradlew build
```

Expected output: `BUILD SUCCESSFUL`

---

## Step 6: Run the App

### On Emulator:
1. Click **Device Manager** (phone icon)
2. Create a new virtual device if needed:
   - **Device:** Pixel 6
   - **System Image:** API 34 (Android 14)
   - Click **Finish**
3. Click **Run** (green play button) or press Shift+F10
4. Select your emulator
5. App should launch!

### On Physical Device:
1. Enable **Developer Options** on your Android device:
   - Go to **Settings → About Phone**
   - Tap **Build Number** 7 times
2. Enable **USB Debugging** in Developer Options
3. Connect device via USB
4. Click **Run** and select your device

---

## Step 7: Verify It's Working

### What You Should See:
- App launches with "Trips List Screen - Coming Soon" text
- Material 3 theme applied (blue primary color)
- No crashes or errors

### Expected Behavior:
Since we're in **Phase 0 (Foundation)**, you'll see:
- ✅ App launches successfully
- ✅ Theme and navigation working
- ✅ Placeholder screens
- ⏳ No actual trip tracking yet (Phase 1)

---

## Troubleshooting

### Issue: "Gradle sync failed"
**Solution:**
- Check JDK version: `java -version` (should be 17)
- Verify internet connection
- Delete `.gradle` folder and retry sync

### Issue: "Maps API key not found"
**Solution:**
- Verify `local.properties` has correct API key
- Restart Android Studio
- Clean and rebuild: **Build → Clean Project**

### Issue: "SDK not found"
**Solution:**
- Open **File → Project Structure → SDK Location**
- Set Android SDK location (usually `~/Library/Android/sdk` on Mac)
- Click **Apply**

### Issue: Build errors with Room/Hilt
**Solution:**
- These dependencies use KSP (Kotlin Symbol Processing)
- Should work automatically, but if issues:
  ```bash
  ./gradlew clean build --refresh-dependencies
  ```

---

## Project Structure in Android Studio

You should see this in the Project panel (Android view):

```
📁 app
  📁 java
    📁 com.triptracker
      - MainActivity
      - TripTrackerApplication
      📁 navigation
      📁 di
  📁 res
    📁 values
      - strings.xml
      - themes.xml
  - AndroidManifest.xml

📁 core
  📁 common
  📁 domain
  📁 ui

📁 service
  📁 database
  📁 location
  📁 activity

📁 feature
  📁 trips
  📁 trip-detail
  📁 active-trip

📁 Gradle Scripts
  - build.gradle.kts (Project)
  - build.gradle.kts (Module: app)
  - settings.gradle.kts
  - libs.versions.toml
```

---

## Running Validation Script

To verify everything is set up correctly:

```bash
cd /Users/briantaylor/trip-tracker
./validate.sh
```

Should output: **"✓ Project structure is valid and ready for development!"**

---

## What's Next?

Once the app runs successfully, you're ready to start **Phase 1: MVP Development**

### Next Implementation Steps:
1. **Location Service** - GPS tracking and foreground service
2. **Activity Recognition** - Automatic trip detection
3. **Trip Management** - Core business logic
4. **UI Screens** - Replace placeholders with real screens

---

## Useful Commands

```bash
# Build project
./gradlew build

# Clean build
./gradlew clean

# Run tests
./gradlew test

# Check for updates
./gradlew dependencyUpdates

# List all tasks
./gradlew tasks
```

---

## Getting Help

- **Documentation:** See `/docs` folder
- **Validation Report:** `VALIDATION_REPORT.md`
- **Architecture:** `../trip-tracker-docs/02-system-architecture.md`

---

**You're all set! The foundation is complete and ready for feature development.** 🚀
