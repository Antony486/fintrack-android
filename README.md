# FinTrack Android

Kotlin + Jetpack Compose + Koin Android app for FinTrack personal finance tracker.

## Tech Stack
- **UI**: Jetpack Compose + Material3
- **DI**: Koin (NO Hilt, NO XML)
- **Networking**: Retrofit + OkHttp + Kotlinx Serialization
- **Local DB**: Room
- **Session**: DataStore Preferences
- **Navigation**: Compose Navigation
- **Payments**: Stripe Android SDK + M-Pesa via backend

## Requirements
- Android Studio Hedgehog or newer
- JDK 17
- Android device or emulator (minSdk 26 = Android 8.0)

## Setup
1. Open the `fintrack-android` folder in Android Studio
2. Let Gradle sync
3. Ensure your device is on the same WiFi as your laptop (192.168.8.112)
4. Make sure the backend is running: `cd fintrack-backend && ./run.sh`
5. Run the app on your physical device

## Configuration
`BASE_URL` is set in `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "BASE_URL", "\"http://192.168.8.112:8080/api/v1/\"")
```
Change this if your laptop IP changes.

## Project Structure
```
app/src/main/kotlin/com/fintrack/
├── data/
│   ├── local/
│   │   ├── dao/          # Room DAOs
│   │   ├── entities/     # Room entities
│   │   ├── FinTrackDatabase.kt
│   │   └── SessionManager.kt
│   └── remote/
│       ├── api/          # Retrofit interfaces + NetworkModule
│       └── dto/          # API request/response models
├── di/
│   └── AppModule.kt      # Koin DI module
├── domain/
│   ├── model/            # Domain models + Mappers
│   └── repository/       # All repositories
├── ui/
│   ├── auth/             # Login, Register, AuthViewModel
│   ├── dashboard/        # Dashboard screen + ViewModel
│   ├── transactions/     # Transaction list, add form, ViewModel
│   ├── budgets/          # Budget screen + ViewModel
│   ├── bills/            # Bill screen + ViewModel
│   ├── payments/         # M-Pesa + Stripe screen + ViewModel
│   ├── navigation/       # NavGraph
│   └── theme/            # Colors, Typography, Theme
├── FinTrackApp.kt        # Application class (Koin init)
└── MainActivity.kt
```

## Screens
1. **Login / Register** — JWT auth with token saved to DataStore
2. **Dashboard** — Monthly summary card, quick actions, budget status, upcoming bills
3. **Transactions** — List with filter chips, add form with dropdowns
4. **Budgets** — Budget status with progress bars, over-budget warnings
5. **Bills** — Recurring bills, mark as paid, due date tracking
6. **Payments** — M-Pesa STK Push + Stripe payment intent creation
 
