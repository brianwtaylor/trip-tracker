#!/bin/bash

echo "=================================="
echo "Trip Tracker - Implementation Validation"
echo "=================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Counters
PASS=0
WARN=0
FAIL=0

# Function to check file exists
check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $2"
        ((PASS++))
    else
        echo -e "${RED}✗${NC} $2"
        ((FAIL++))
    fi
}

# Function to check directory exists
check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $2"
        ((PASS++))
    else
        echo -e "${RED}✗${NC} $2"
        ((FAIL++))
    fi
}

# Function to count files
count_files() {
    local count=$(find "$1" -name "$2" -type f 2>/dev/null | wc -l | tr -d ' ')
    echo -e "${GREEN}✓${NC} Found $count $3"
    ((PASS++))
}

echo "1. PROJECT STRUCTURE"
echo "===================="
check_dir "app" "App module"
check_dir "core/common" "Core Common module"
check_dir "core/domain" "Core Domain module"
check_dir "core/ui" "Core UI module"
check_dir "service/location" "Location Service module"
check_dir "service/activity" "Activity Service module"
check_dir "service/database" "Database Service module"
check_dir "feature/trips" "Trips Feature module"
check_dir "feature/trip-detail" "Trip Detail Feature module"
check_dir "feature/active-trip" "Active Trip Feature module"
echo ""

echo "2. BUILD CONFIGURATION"
echo "======================"
check_file "build.gradle.kts" "Root build.gradle.kts"
check_file "settings.gradle.kts" "settings.gradle.kts"
check_file "gradle.properties" "gradle.properties"
check_file "gradle/libs.versions.toml" "Version catalog"
check_file ".gitignore" ".gitignore"
echo ""

echo "3. APP MODULE FILES"
echo "==================="
check_file "app/build.gradle.kts" "App build.gradle.kts"
check_file "app/src/main/AndroidManifest.xml" "AndroidManifest.xml"
check_file "app/src/main/java/com/triptracker/TripTrackerApplication.kt" "Application class"
check_file "app/src/main/java/com/triptracker/MainActivity.kt" "MainActivity"
check_file "app/src/main/java/com/triptracker/navigation/NavGraph.kt" "Navigation Graph"
check_file "app/src/main/java/com/triptracker/navigation/NavigationRoutes.kt" "Navigation Routes"
check_file "app/src/main/java/com/triptracker/di/AppModule.kt" "App DI Module"
echo ""

echo "4. CORE MODULES"
echo "==============="
check_file "core/domain/src/main/java/com/triptracker/core/domain/model/Trip.kt" "Trip model"
check_file "core/domain/src/main/java/com/triptracker/core/domain/model/TripLocation.kt" "TripLocation model"
check_file "core/domain/src/main/java/com/triptracker/core/domain/model/TripStatus.kt" "TripStatus enum"
check_file "core/common/src/main/java/com/triptracker/core/common/result/Result.kt" "Result wrapper"
check_file "core/common/src/main/java/com/triptracker/core/common/constants/Constants.kt" "Constants"
check_file "core/common/src/main/java/com/triptracker/core/common/util/DistanceCalculator.kt" "Distance Calculator"
check_file "core/common/src/main/java/com/triptracker/core/common/util/SpeedConverter.kt" "Speed Converter"
check_file "core/common/src/main/java/com/triptracker/core/common/util/DateTimeUtils.kt" "DateTime Utils"
echo ""

echo "5. UI THEME"
echo "==========="
check_file "core/ui/src/main/java/com/triptracker/core/ui/theme/Color.kt" "Colors"
check_file "core/ui/src/main/java/com/triptracker/core/ui/theme/Theme.kt" "Theme"
check_file "core/ui/src/main/java/com/triptracker/core/ui/theme/Type.kt" "Typography"
check_file "core/ui/src/main/java/com/triptracker/core/ui/components/BottomNavigationBar.kt" "Bottom Navigation"
echo ""

echo "6. DATABASE SERVICE"
echo "==================="
check_file "service/database/src/main/java/com/triptracker/service/database/TripDatabase.kt" "Trip Database"
check_file "service/database/src/main/java/com/triptracker/service/database/dao/TripDao.kt" "Trip DAO"
check_file "service/database/src/main/java/com/triptracker/service/database/dao/LocationDao.kt" "Location DAO"
check_file "service/database/src/main/java/com/triptracker/service/database/entity/TripEntity.kt" "Trip Entity"
check_file "service/database/src/main/java/com/triptracker/service/database/entity/LocationEntity.kt" "Location Entity"
check_file "service/database/src/main/java/com/triptracker/service/database/entity/TripWithLocations.kt" "Trip with Locations"
check_file "service/database/src/main/java/com/triptracker/service/database/mapper/TripMapper.kt" "Trip Mapper"
check_file "service/database/src/main/java/com/triptracker/service/database/mapper/LocationMapper.kt" "Location Mapper"
check_file "service/database/src/main/java/com/triptracker/service/database/di/DatabaseModule.kt" "Database DI Module"
echo ""

echo "7. FILE COUNTS"
echo "=============="
count_files "." "*.kt" "Kotlin files"
count_files "." "*.xml" "XML files"
count_files "." "build.gradle.kts" "Gradle build files"
echo ""

echo "8. RESOURCES"
echo "============"
check_file "app/src/main/res/values/strings.xml" "Strings resources"
check_file "app/src/main/res/values/themes.xml" "Theme resources"
echo ""

echo "=================================="
echo "SUMMARY"
echo "=================================="
echo -e "${GREEN}Passed: $PASS${NC}"
if [ $WARN -gt 0 ]; then
    echo -e "${YELLOW}Warnings: $WARN${NC}"
fi
if [ $FAIL -gt 0 ]; then
    echo -e "${RED}Failed: $FAIL${NC}"
else
    echo -e "${GREEN}All checks passed!${NC}"
fi
echo ""

TOTAL=$((PASS + WARN + FAIL))
PERCENTAGE=$((PASS * 100 / TOTAL))
echo "Completion: $PERCENTAGE% ($PASS/$TOTAL checks passed)"
echo ""

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}✓ Project structure is valid and ready for development!${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Open project in Android Studio"
    echo "2. Add your Google Maps API key to local.properties"
    echo "3. Sync Gradle files"
    echo "4. Start implementing Phase 1 features"
else
    echo -e "${YELLOW}⚠ Some files are missing. Review the failed checks above.${NC}"
fi

echo ""
echo "=================================="
