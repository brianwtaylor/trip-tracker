# Opening Trip Tracker in Android Studio

Follow these steps to open and build the project in Android Studio.

---

## Step 1: Install Android Studio (if not installed)

### Check if Android Studio is installed:
```bash
ls -la "/Applications" | grep -i android
```

### If not installed:
1. Download from: https://developer.android.com/studio
2. Install Android Studio
3. During setup, install:
   - Android SDK
   - Android SDK Platform (API 34)
   - Android SDK Build-Tools
   - Android Emulator

---

## Step 2: Open the Project

### Method 1: From Finder
1. Open **Android Studio**
2. Click **"Open"** or **"Open an Existing Project"**
3. Navigate to: `/Users/briantaylor/trip-tracker`
4. Click **"Open"**

### Method 2: From Terminal
```bash
# If Android Studio is installed and in PATH:
open -a "Android Studio" /Users/briantaylor/trip-tracker

# Or launch Android Studio first, then use File → Open
```

---

## Step 3: Configure Google Maps API Key

**IMPORTANT:** Before building, you need a Maps API key.

1. Get API Key from [Google Cloud Console](https://console.cloud.google.com/):
   - Create/select project
   - Enable "Maps SDK for Android"
   - Create API Key

2. Edit `local.properties`:
   ```properties
   sdk.dir=/Users/briantaylor/Library/Android/sdk
   MAPS_API_KEY=YOUR_ACTUAL_API_KEY_HERE
   ```

---

## Step 4: Gradle Sync

When you first open the project:

1. Android Studio will show: **"Gradle project sync in progress..."**
2. This may take 2-5 minutes the first time
3. Wait for: **"Gradle sync finished"** message

### If sync fails:
- Check internet connection
- Verify JDK 17 is installed: `java -version`
- Click: **File → Invalidate Caches / Restart**

---

## Step 5: Build the Project

### Option A: Via Android Studio UI
1. Click **Build → Make Project** (⌘F9 on Mac, Ctrl+F9 on Windows)
2. Wait for build to complete
3. Check **Build** tab at bottom - should say: **"BUILD SUCCESSFUL"**

### Option B: Via Terminal (inside Android Studio)
```bash
./gradlew build
```

---

## Step 6: Run the App

### On Android Emulator:
1. Click **Device Manager** (phone icon on right side)
2. If no emulators exist, create one:
   - Click **"Create Device"**
   - Select **Pixel 6** (or any device)
   - Select System Image: **API 34 (Android 14)**
   - Click **"Finish"**
3. Click **Run ▶️** button (or Shift+F10)
4. Select your emulator
5. App should launch!

### On Physical Device:
1. Enable Developer Options on your device:
   - Settings → About Phone → Tap "Build Number" 7 times
2. Enable USB Debugging in Developer Options
3. Connect device via USB
4. Click **Run ▶️** and select your device

---

## Step 7: Verify It's Working

### Expected Result:
- ✅ App launches successfully
- ✅ You see: **"Trips List Screen - Coming Soon"**
- ✅ Blue Material 3 theme applied
- ✅ No crashes in Logcat

### What's Working:
- Navigation system
- Material 3 theme with dark mode
- Database schema (Room)
- All foundation components

### What's NOT Working Yet (Expected):
- ⏳ Location tracking (Phase 1)
- ⏳ Trip recording (Phase 1)
- ⏳ Maps display (Phase 1)
- ⏳ Real UI screens (Phase 1)

This is **normal** - we're at Phase 0 (Foundation) completion!

---

## Troubleshooting

### Error: "Gradle sync failed"
```bash
# Clean and rebuild
./gradlew clean build --refresh-dependencies
```

### Error: "SDK not found"
1. Open **File → Project Structure → SDK Location**
2. Set to: `/Users/briantaylor/Library/Android/sdk`
3. Click **Apply**

### Error: "Maps API key not configured"
- Verify `local.properties` has correct API key
- Restart Android Studio
- Clean project: **Build → Clean Project**

### Error: "Java version mismatch"
```bash
# Check Java version (must be 17)
java -version

# If wrong version, install JDK 17
brew install openjdk@17
```

---

## Project View in Android Studio

Once opened, you'll see:

```
📁 TripTracker
  📁 app
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
```

---

## Useful Android Studio Shortcuts

| Action | Mac | Windows/Linux |
|--------|-----|---------------|
| Build Project | ⌘F9 | Ctrl+F9 |
| Run App | ⇧F10 | Shift+F10 |
| Debug App | ⌃D | Ctrl+D |
| Find File | ⇧⇧ (double shift) | Shift+Shift |
| Sync Gradle | ⌘⇧O | Ctrl+Shift+O |

---

## Quick Verification Checklist

Before running, verify:
- [ ] Android Studio installed
- [ ] Project opened successfully
- [ ] Gradle sync completed (no errors)
- [ ] Maps API key added to `local.properties`
- [ ] JDK 17 configured
- [ ] Emulator created or device connected
- [ ] Build successful

---

## Next Steps After Successful Run

Once the app runs:
1. ✅ Verify the validation report: `cat VALIDATION_REPORT.md`
2. ✅ Check implementation progress: `cat ../trip-tracker-docs/04-implementation-progress.md`
3. 🚀 Start Phase 1: Location Service implementation

---

**You're all set! The project should open and build successfully in Android Studio.** 🎉
