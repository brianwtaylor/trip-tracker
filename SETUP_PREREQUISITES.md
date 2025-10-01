# Setup Prerequisites for Trip Tracker

Before you can build and run the Trip Tracker app, you need to install the required tools.

---

## ❌ Current System Status

Based on system checks:
- ❌ **Java (JDK 17):** Not installed
- ❓ **Android Studio:** Not found in standard location
- ✅ **Project Files:** All created and validated

---

## Required Installation Steps

### 1. Install Java JDK 17

**Option A: Using Homebrew (Recommended for Mac)**
```bash
# Install Homebrew if not installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install JDK 17
brew install openjdk@17

# Link it
sudo ln -sfn $(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk

# Verify installation
java -version
```

**Option B: Manual Download**
1. Download from: https://adoptium.net/temurin/releases/?version=17
2. Select:
   - Version: **17**
   - Operating System: **macOS**
   - Architecture: **x64** (or **aarch64** for M1/M2 Macs)
3. Install the .pkg file
4. Verify: `java -version`

**Expected Output:**
```
openjdk version "17.0.x"
```

---

### 2. Install Android Studio

**Download and Install:**
1. Go to: https://developer.android.com/studio
2. Download **Android Studio Hedgehog** or later
3. Open the .dmg file
4. Drag Android Studio to Applications folder
5. Launch Android Studio

**First-Time Setup:**
During initial setup, Android Studio will ask to install:
- [x] Android SDK
- [x] Android SDK Platform (API 34)
- [x] Android SDK Build-Tools
- [x] Android Virtual Device (Emulator)
- [x] Intel HAXM (for emulator acceleration)

**Select:** Standard installation and let it download everything (~2-4 GB)

---

### 3. Get Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project:
   - Name: "Trip Tracker"
   - Click **Create**
3. Enable Maps SDK:
   - Go to **APIs & Services → Library**
   - Search for "Maps SDK for Android"
   - Click **Enable**
4. Create API Key:
   - Go to **APIs & Services → Credentials**
   - Click **Create Credentials → API Key**
   - Copy the key (starts with `AIza...`)
5. (Optional) Restrict the key:
   - Click on the API key
   - Under "Application restrictions," select "Android apps"
   - Add package name: `com.triptracker`

---

### 4. Configure the Project

Once Java and Android Studio are installed:

1. **Add Maps API Key:**
   ```bash
   cd /Users/briantaylor/trip-tracker
   # Edit local.properties and replace YOUR_MAPS_API_KEY_HERE with your actual key
   ```

2. **Verify SDK Path:**
   ```bash
   # Check if Android SDK is installed
   ls ~/Library/Android/sdk
   ```
   
   If it exists, your `local.properties` is already configured correctly!

---

## Verification Steps

### Step 1: Verify Java
```bash
java -version
# Should show: openjdk version "17.0.x"
```

### Step 2: Verify Android Studio
```bash
ls -la "/Applications" | grep -i android
# Should show: Android Studio.app
```

### Step 3: Verify Android SDK
```bash
ls ~/Library/Android/sdk
# Should show: build-tools, platforms, platform-tools, etc.
```

### Step 4: Verify Project
```bash
cd /Users/briantaylor/trip-tracker
./validate.sh
# Should show: 48/48 checks passed
```

---

## Installation Time Estimates

| Component | Download Size | Install Time |
|-----------|--------------|--------------|
| JDK 17 | ~180 MB | 2-3 minutes |
| Android Studio | ~1.1 GB | 5-10 minutes |
| Android SDK Components | ~2-3 GB | 10-15 minutes |
| **Total** | **~4-5 GB** | **20-30 minutes** |

---

## Quick Start Script

Once JDK 17 and Android Studio are installed, run this:

```bash
#!/bin/bash

echo "Setting up Trip Tracker..."

# Navigate to project
cd /Users/briantaylor/trip-tracker

# Verify Java
echo "Checking Java version..."
java -version

# Verify project structure
echo "Validating project..."
./validate.sh

# Instructions
echo ""
echo "✓ Setup complete!"
echo ""
echo "Next steps:"
echo "1. Add your Maps API key to local.properties"
echo "2. Open project in Android Studio:"
echo "   open -a 'Android Studio' ."
echo "3. Wait for Gradle sync"
echo "4. Click Run ▶️"
```

---

## What to Do Next

### If you DON'T have JDK 17:
1. Install it using instructions above
2. Come back and continue

### If you DON'T have Android Studio:
1. Download and install from android.com/studio
2. Run the first-time setup wizard
3. Come back and continue

### If you HAVE both installed:
1. Add your Maps API key to `local.properties`
2. Follow: [OPEN_IN_ANDROID_STUDIO.md](./OPEN_IN_ANDROID_STUDIO.md)
3. Build and run! 🚀

---

## Troubleshooting

### Issue: "java: command not found"
- JDK 17 not installed or not in PATH
- Solution: Install JDK 17 using Homebrew or manual download

### Issue: "Android Studio not found"
- Not installed or not in Applications folder
- Solution: Download from developer.android.com/studio

### Issue: "SDK not found"
- Android SDK not installed during Android Studio setup
- Solution: Open Android Studio → Preferences → Appearance & Behavior → System Settings → Android SDK → Install missing components

---

## System Requirements

**Minimum:**
- macOS 10.14 or later
- 8 GB RAM
- 8 GB free disk space
- Internet connection

**Recommended:**
- macOS 11+ (Big Sur or later)
- 16 GB RAM
- 20 GB free disk space
- Fast internet (for downloads)

---

**Once you have JDK 17 and Android Studio installed, you're ready to build the app!** 🎉
