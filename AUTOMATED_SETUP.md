# Automated Setup Script

The automated setup script (`setup.sh`) simplifies the installation and configuration process for Trip Tracker.

---

## What It Does

The script automates:

### ✅ Automatic Installation
1. **Homebrew** - Package manager (if not installed)
2. **Java JDK 17** - Required to build Android apps
3. **Maps API Key** - Interactive configuration

### ℹ️ Guided Installation
4. **Android Studio** - Opens download page (manual install)
5. **Android SDK** - Installed via Android Studio

### 🔍 Verification
6. **System Check** - Detects what's already installed
7. **Project Validation** - Runs validation checks
8. **Summary Report** - Shows what's ready

---

## How to Run

### Quick Start:
```bash
cd /Users/briantaylor/trip-tracker
./setup.sh
```

That's it! The script will guide you through everything.

---

## What Happens Step-by-Step

### 1. Welcome Screen
```
================================
Trip Tracker - Automated Setup
================================

This script will help you set up your development environment.

It will install/configure:
  • Homebrew (if needed)
  • Java JDK 17
  • Google Maps API key

Continue with automated setup? (y/n):
```

### 2. System Check
The script checks for:
- ✓ Homebrew
- ✓ Java JDK 17
- ✓ Android Studio
- ✓ Android SDK
- ✓ Maps API Key

Example output:
```
Step 1: System Check
====================

Checking prerequisites...

⚠ Homebrew: Not installed
⚠ Java JDK 17: Not installed
⚠ Android Studio: Not found
⚠ Android SDK: Not found
⚠ Maps API Key: Not configured
```

### 3. Install Homebrew (if needed)
```
Installing Homebrew
===================

Homebrew is a package manager for macOS

Install Homebrew now? (y/n): y

[Downloads and installs Homebrew]

✓ Homebrew installed successfully!
```

### 4. Install Java JDK 17 (if needed)
```
Installing Java JDK 17
=====================

JDK 17 is required to build Android apps

Install JDK 17 via Homebrew? (y/n): y

ℹ Installing OpenJDK 17...
[Downloads and installs JDK 17]

ℹ Linking JDK to system...
[Sets up system links]

✓ Java installed successfully: openjdk version "17.0.9"
```

### 5. Android Studio Guide
```
Step 3: Android Studio
=====================

⚠ Android Studio is not installed

Android Studio must be installed manually:
1. Download from: https://developer.android.com/studio
2. Open the .dmg file
3. Drag Android Studio to Applications
4. Launch and complete the setup wizard
5. Select 'Standard' installation

Open download page in browser? (y/n):
```

### 6. Maps API Key Setup
```
Google Maps API Key Setup
=========================

You need a Google Maps API key to run this app.

Steps to get an API key:
1. Go to: https://console.cloud.google.com/
2. Create a new project (or select existing)
3. Enable 'Maps SDK for Android'
4. Go to Credentials → Create Credentials → API Key
5. Copy the API key

Do you have a Google Maps API key ready? (y/n): y
Enter your Maps API key: AIzaSy...

✓ API key saved to local.properties
```

### 7. Project Validation
```
Step 5: Project Validation
==========================

ℹ Running project validation...

[Runs validate.sh]

✓ Found 26 Kotlin files
✓ Found 3 XML files
...
48/48 checks passed
```

### 8. Summary & Next Steps
```
Setup Summary
=============

Installation Status:

✓ Homebrew
✓ Java JDK 17
✓ Android Studio
✓ Android SDK
✓ Maps API Key

Next Steps
==========

✓ All prerequisites met! You're ready to build.

To open the project:
  1. Launch Android Studio
  2. Click 'Open'
  3. Select: /Users/briantaylor/trip-tracker
  4. Wait for Gradle sync
  5. Click Run ▶️

Open project in Android Studio now? (y/n):
```

---

## Features

### 🤖 Fully Interactive
- Asks before installing anything
- Explains what each component is for
- Provides options to skip or install manually

### 🛡️ Safe & Non-Destructive
- Checks what's already installed
- Won't reinstall existing components
- Creates backups when modifying files
- Can be run multiple times safely

### 🎨 Color-Coded Output
- 🟢 Green: Success/Installed
- 🟡 Yellow: Warning/Missing
- 🔴 Red: Error
- 🔵 Blue: Information
- 🔷 Cyan: Headers

### 📊 Progress Tracking
- Shows current step (Step 1 of 6)
- Displays what's being done
- Reports success/failure for each action

---

## What Gets Installed

### Via Homebrew:
- **Homebrew itself** (~400 MB)
- **OpenJDK 17** (~180 MB)

### Manually (guided):
- **Android Studio** (~1.1 GB)
- **Android SDK** (~2-3 GB, via Android Studio)

### Total Download: ~4-5 GB
### Total Time: ~30-40 minutes

---

## Supported Scenarios

### Scenario 1: Nothing Installed
Script will:
1. Install Homebrew
2. Install Java JDK 17
3. Guide you to install Android Studio
4. Help configure Maps API key

### Scenario 2: Homebrew Already Installed
Script will:
1. Skip Homebrew installation
2. Install Java JDK 17
3. Continue with remaining steps

### Scenario 3: Everything Installed
Script will:
1. Detect all installed components
2. Only configure Maps API key if missing
3. Run validation
4. Offer to open Android Studio

### Scenario 4: Partial Installation
Script will:
1. Detect what's installed
2. Only install missing components
3. Skip what's already present

---

## Advanced Options

### Run Without Installing Anything (Check Only):
```bash
# Just see what's installed
./setup.sh
# Answer 'n' to all install prompts
```

### Install Specific Components:
```bash
# Install only Java
./setup.sh
# Answer 'y' to Java, 'n' to others
```

### Skip to Validation:
```bash
# If everything is installed
./setup.sh
# All checks pass, jumps to validation
```

---

## Troubleshooting

### Issue: "Permission denied"
```bash
chmod +x setup.sh
./setup.sh
```

### Issue: Homebrew install fails
**Possible causes:**
- No internet connection
- macOS too old (need 10.15+)
- Xcode Command Line Tools missing

**Solution:**
```bash
# Install Xcode CLT first
xcode-select --install

# Then run setup again
./setup.sh
```

### Issue: Java install fails
**Solution:**
```bash
# Manual install from Adoptium
# Download: https://adoptium.net/temurin/releases/?version=17
# Then run setup again to configure
```

### Issue: Script hangs
**Solution:**
- Press Ctrl+C to cancel
- Check internet connection
- Run again with verbose mode:
```bash
bash -x setup.sh
```

---

## What the Script Does NOT Do

❌ **Does not modify system settings** (except linking Java)  
❌ **Does not delete or remove anything**  
❌ **Does not require root password** (except for Java linking)  
❌ **Does not send any data anywhere**  
❌ **Does not install Android Studio** (guided manual install)

---

## Manual Alternative

If you prefer not to use the automated script:

1. **Install Homebrew:**
   ```bash
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
   ```

2. **Install Java:**
   ```bash
   brew install openjdk@17
   sudo ln -sfn $(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
   ```

3. **Install Android Studio:**
   - Download: https://developer.android.com/studio
   - Install manually

4. **Configure API Key:**
   - Edit `local.properties`
   - Add: `MAPS_API_KEY=your_key`

See [SETUP_PREREQUISITES.md](./SETUP_PREREQUISITES.md) for full manual instructions.

---

## After Setup Complete

Once the script finishes successfully:

### ✅ What You Can Do:
1. Open project in Android Studio
2. Sync Gradle (automatic)
3. Build the app
4. Run on emulator/device

### 📚 Next Documentation:
- **OPEN_IN_ANDROID_STUDIO.md** - How to open and build
- **HOW_TO_RUN.md** - Running and troubleshooting
- **VALIDATION_REPORT.md** - What's implemented

---

## Script Output Example

```
=================================
Trip Tracker - Automated Setup
=================================

Continue with automated setup? (y/n): y

=================================
Step 1: System Check
=================================

✓ Homebrew: Installed
✓ Java JDK 17: Installed (openjdk version "17.0.9")
⚠ Android Studio: Not found
⚠ Android SDK: Not found
⚠ Maps API Key: Not configured

=================================
Step 3: Android Studio
=================================

[Installation guide displayed]

=================================
Step 4: Google Maps API Key
=================================

✓ API key saved to local.properties

=================================
Step 5: Project Validation
=================================

✓ Found 26 Kotlin files
...
Completion: 100% (48/48 checks passed)

=================================
Setup Summary
=================================

✓ Homebrew
✓ Java JDK 17
⚠ Android Studio (manual install required)
✓ Maps API Key

=================================
Setup Complete!
=================================
```

---

## Benefits vs Manual Install

| Aspect | Automated Script | Manual |
|--------|-----------------|---------|
| **Time** | Hands-off installation | Step-by-step manual |
| **Errors** | Auto-detects issues | Must troubleshoot yourself |
| **Verification** | Validates everything | Manual checking |
| **Consistency** | Same result every time | Can miss steps |
| **Learning** | Less visibility | See each step |

**Recommendation:** Use automated script for speed, manual for learning.

---

**Ready to try it? Just run:**
```bash
cd /Users/briantaylor/trip-tracker
./setup.sh
```

🚀 The script will handle the rest!
