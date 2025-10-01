# Trip Tracker Android App

An Android application that automatically detects and tracks vehicle trips using GPS and activity recognition.

## Features

- 🚗 **Automatic Trip Detection** - Start tracking when you begin driving
- 📍 **GPS Tracking** - Record your route with high accuracy
- 📊 **Trip Statistics** - View distance, duration, speed metrics
- 🗺️ **Map Visualization** - See your routes on Google Maps
- 💾 **Local Storage** - All data stored securely on your device
- 🔋 **Battery Optimized** - Efficient background tracking

## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Architecture:** Clean Architecture + MVVM
- **Dependency Injection:** Hilt
- **Database:** Room
- **Async:** Kotlin Coroutines & Flow
- **Location:** Google Play Services Location API
- **Maps:** Google Maps Android SDK
- **Min SDK:** 26 (Android 8.0+)
- **Target SDK:** 34

## Project Structure

```
trip-tracker/
├── app/                          # Main application module
├── core/
│   ├── common/                   # Shared utilities and extensions
│   ├── domain/                   # Domain models and interfaces
│   └── ui/                       # Shared UI components and theme
├── service/
│   ├── location/                 # Location tracking service
│   ├── activity/                 # Activity recognition service
│   └── database/                 # Room database implementation
└── feature/
    ├── trips/                    # Trip list feature
    ├── trip-detail/              # Trip detail with map
    └── active-trip/              # Active trip monitoring
```

## Setup

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34
- Google Maps API Key

### Installation

1. Clone the repository
```bash
git clone <repository-url>
cd trip-tracker
```

2. Get a Google Maps API Key
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or select existing one
   - Enable "Maps SDK for Android"
   - Create credentials (API Key)

3. Add your Maps API key to `local.properties`
```properties
MAPS_API_KEY=your_actual_api_key_here
```

4. Open project in Android Studio

5. Sync Gradle and build

## Permissions

The app requires the following permissions:

- **Location (Fine & Coarse)** - Track GPS coordinates
- **Background Location** - Continue tracking when app is closed
- **Activity Recognition** - Detect driving vs. stopped
- **Foreground Service** - Run tracking service
- **Notifications** - Show active trip notification

## Building

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Run Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Architecture

The app follows Clean Architecture principles with clear separation of concerns:

- **Presentation Layer** - Jetpack Compose UI + ViewModels
- **Domain Layer** - Use cases and business logic
- **Data Layer** - Repositories, data sources, Room database

## Development

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use ktlint for formatting
- Maintain >70% code coverage

### Branch Strategy
- `main` - Production-ready code
- `develop` - Integration branch
- `feature/*` - Feature branches
- `bugfix/*` - Bug fix branches

## Documentation

- [Requirements Analysis](../trip-tracker-docs/01-requirements-analysis.md)
- [System Architecture](../trip-tracker-docs/02-system-architecture.md)
- [Implementation Plan](../trip-tracker-docs/03-implementation-plan.md)

## License

Copyright © 2025. All rights reserved.

## Contact

For questions or support, please open an issue.
