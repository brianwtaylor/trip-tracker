#!/bin/bash

echo "🧹 Clearing Gradle caches to fix plugin issues..."
echo ""

# Close Android Studio warning
echo "⚠️  IMPORTANT: Close Android Studio completely before running this script"
echo ""

# Confirm
read -p "Have you closed Android Studio? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Please close Android Studio first, then run this script again."
    exit 1
fi

echo "🗑️  Removing Gradle caches..."

# Remove global Gradle caches
echo "Removing global Gradle caches..."
rm -rf ~/.gradle/caches/
rm -rf ~/.gradle/wrapper/

# Remove project-specific caches
echo "Removing project caches..."
cd /Users/briantaylor/trip-tracker
rm -rf .gradle/
rm -rf */build/
rm -rf build/

echo ""
echo "✅ Cache clearing complete!"
echo ""
echo "🚀 Next steps:"
echo "1. Reopen Android Studio"
echo "2. File → Sync Project with Gradle Files"
echo "3. Build → Make Project"
echo ""
echo "The @Parcelize plugin should now work correctly!"
