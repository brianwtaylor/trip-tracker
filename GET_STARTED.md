# 🚀 Trip Tracker - Get Started Guide

## 🎯 **Immediate Next Steps (This Week)**

### **Phase 1: Build & Validate Level 1**

---

## 📱 **Step 1: Open in Android Studio**

```bash
# Navigate to project directory
cd /Users/briantaylor/trip-tracker

# Open Android Studio
open -a "Android Studio" .
```

**Expected Issues & Fixes:**
- **Gradle sync errors**: Check JDK version (needs Java 17)
- **Missing dependencies**: Run `./gradlew build` to download
- **API key setup**: Add `local.properties` with Maps API key

---

## 🔑 **Step 2: Setup Google Maps API Key**

### **Get API Key:**
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create/select project
3. Enable "Maps SDK for Android"
4. Create API key with Android restrictions
5. Add your app's SHA-1 fingerprint

### **Add to Project:**
```bash
# Create local.properties if it doesn't exist
echo "MAPS_API_KEY=your_api_key_here" > local.properties
```

**Note:** `local.properties` is in `.gitignore` - keep it secure!

---

## 🏗️ **Step 3: Build the App**

### **In Android Studio:**
```
1. Wait for Gradle sync to complete
2. Build → Make Project (Ctrl+F9)
3. If errors: Check Build → Build Output
```

### **Common Build Issues:**

#### **Gradle Version Error:**
```
Error: Incompatible Java and Gradle versions
```
**Fix:** Update Gradle wrapper to 8.5:
```bash
./gradlew wrapper --gradle-version=8.5
```

#### **Missing Dependencies:**
```
Error: Could not resolve dependencies
```
**Fix:** Clean and rebuild:
```bash
./gradlew clean build
```

#### **Manifest Merger Issues:**
```
Error: Android resource linking failed
```
**Fix:** Check for missing API keys or permissions

---

## 📱 **Step 4: Run on Device/Emulator**

### **Setup Device:**
1. **Physical Device:**
   - Enable Developer Options
   - Enable USB Debugging
   - Allow location permissions

2. **Emulator:**
   - Create Android API 28+ emulator
   - Enable GPS/location simulation

### **Run App:**
```
Run → Run 'app' (Shift+F10)
```

---

## 🧪 **Step 5: Test Level 1 Functionality**

### **Test Checklist:**

#### **Basic App Launch**
- [ ] App opens without crashes
- [ ] Navigation works (bottom tabs)
- [ ] UI displays correctly

#### **Location Tracking**
- [ ] Request location permissions
- [ ] Start trip recording
- [ ] GPS data appears in logs
- [ ] Battery impact <5% initially

#### **Activity Recognition**
- [ ] Sensor data collection starts
- [ ] No crashes during movement
- [ ] Basic classifications logged
- [ ] Trip data includes role detection

---

## 🔍 **Step 6: Debug & Validate**

### **Check Logs:**
```bash
# In Android Studio: View → Tool Windows → Logcat
# Filter by package: com.triptracker
```

### **Expected Log Messages:**
```
✅ LocationTrackingService: Service started
✅ ActivityRepository: Activity recognition started
✅ DetectUserRoleUseCase: User role classified: DRIVER
✅ LocationClient: Location updates started
```

### **Validate Accuracy:**
1. **Stationary Test:** Place phone on dashboard → Should detect DRIVER
2. **Movement Test:** Hold phone while walking → Should detect PASSENGER
3. **Mixed Test:** Drive with phone in hand → May show UNKNOWN

---

## 📊 **Step 7: Collect Initial Data**

### **Data Collection Setup:**
1. **Enable logging:** Add detailed sensor logging
2. **Test scenarios:**
   - Dashboard mounting (driver)
   - Passenger seat usage
   - Hand-held usage
   - Different phone positions

3. **Record results:**
   - Actual vs. detected role
   - Confidence levels
   - Battery usage
   - Any crashes/issues

---

## 🎯 **Success Criteria (End of Week 1)**

### **Must-Have:**
- ✅ App builds and runs without crashes
- ✅ Location tracking starts successfully
- ✅ Activity recognition initializes
- ✅ Basic trip recording works
- ✅ Battery impact acceptable (<5%)

### **Nice-to-Have:**
- 🎯 Initial accuracy observations (70-80% expected)
- 🎯 Sensor data visible in logs
- 🎯 UI shows trip status
- 🎯 No major performance issues

---

## 🚨 **If Issues Occur**

### **Build Problems:**
```
1. Check Java version: java -version (should be 17)
2. Clean project: ./gradlew clean
3. Invalidate caches: Android Studio → File → Invalidate Caches
4. Update Gradle: ./gradlew wrapper --gradle-version=8.5
```

### **Runtime Issues:**
```
1. Check permissions in device settings
2. Verify API key is correct
3. Check device location/GPS settings
4. Monitor battery usage
5. Check Logcat for detailed errors
```

### **Accuracy Issues:**
```
1. Test in controlled environments first
2. Verify sensor availability on device
3. Check for background app restrictions
4. Validate phone position affects readings
```

---

## 📞 **Get Help**

### **Quick Debugging:**
- **Build issues:** Check `gradle.properties` and JDK version
- **Runtime crashes:** Check Logcat for stack traces
- **Permissions:** Verify all required permissions granted
- **GPS issues:** Test with Google Maps first

### **Validation Questions:**
- Does the app open? (Basic functionality)
- Does location tracking start? (GPS working)
- Do you see sensor logs? (Activity recognition active)
- Is battery usage reasonable? (<5% per hour)

---

## 🎯 **Week 1 Goal**

**Get Level 1 working reliably in controlled environments**

By end of week 1, you should have:
- ✅ Functional app with Level 1 detection
- ✅ Basic accuracy observations
- ✅ Understanding of system limitations
- ✅ Data collection started
- ✅ Clear path to Phase 2

---

## 📈 **Timeline Reminder**

```
Week 1: Build & basic testing ✅ (this week)
Week 2: Real driving validation
Week 3-5: Data collection & analytics
Week 6-8: UI enhancement & user testing
Week 9-14: Level 2 ML development
```

---

## 🎉 **Ready to Start?**

1. **Open Android Studio**
2. **Add your Maps API key**
3. **Build the project**
4. **Run on device**
5. **Test basic functionality**

**Let's get Level 1 working!** 🚗📱

---

**Questions?** Check the logs, verify permissions, and ensure your environment matches the setup requirements.

**Next: Open Android Studio and let's build!**
