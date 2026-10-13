# LIFEOS (Native Android)

> **Your Life. Your Goals. One Place.**  
> Offline-first personal productivity and life-management native Android application.

---

## 1. Project Overview

**LIFEOS** is an offline-first personal operating system designed to manage tasks, notes, personal finances, daily habits, and long-term milestone goals seamlessly in one unified interface.

- **Primary Languages Supported**: Bengali (বাংলা) and English.
- **Visual Design**: Premium near-black dark interface (`#080B0D`), elevated cards (`#11171A`), neon lime (`#A3FF5C`) & cyan (`#39D9FF`) accents.
- **Architectural Paradigm**: Offline-First, Single Source of Truth via Room SQLite Database, Unidirectional Data Flow (MVVM) with Kotlin StateFlow & Coroutines, Material Design 3.

---

## 2. Technology Stack & Toolchain

- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose (BOM 2024.09.00) & Material Design 3
- **Local Database**: AndroidX Room 2.7.0 with Kotlin Symbol Processing (KSP 2.3.5)
- **Preferences Storage**: AndroidX DataStore Preferences 1.1.7
- **Navigation**: Jetpack Navigation Compose 2.8.9
- **Lifecycle**: Lifecycle Runtime Compose & ViewModel 2.8.7
- **Testing**: JUnit 4.13.2, Robolectric 4.16.1, AndroidX Test Core & Compose UI Test
- **Build System**: Gradle 9.1.1 (Kotlin DSL) with Version Catalog (`libs.versions.toml`)
- **Android SDK Configuration**:
  - `compileSdk`: 36
  - `targetSdk`: 36
  - `minSdk`: 24 (Android 7.0+)

---

## 3. How to Build & Run in Android Studio

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 17 or JDK 21 configured as Gradle JDK in Android Studio

### Steps:
1. **Open Project**: Launch Android Studio, select **Open**, and choose the `LIFEOS` root folder.
2. **Sync Project with Gradle Files**: Click `File -> Sync Project with Gradle Files` (or the Elephant icon in the top toolbar).
3. **Run on an Emulator or Physical Device**:
   - Create or select an Android Virtual Device (AVD with API 24–36).
   - Click the green **Run (Shift + F10)** button.
4. **Generate Debug APK**:
   - In Android Studio: `Build -> Build Bundle(s) / APK(s) -> Build APK(s)`.
   - Or via command line:
     ```bash
     gradle :app:assembleDebug
     ```
   - **Output Location**: `app/build/outputs/apk/debug/app-debug.apk`
5. **Run Automated Unit & Database Tests**:
   - Run via command line:
     ```bash
     gradle :app:testDebugUnitTest
     ```

---

## 4. Current MVP Implemented Features

1. **First-Launch Onboarding**:
   - Bilingual language selector (English & Bengali).
   - Instant entry without requiring an account or cloud login.
   - Onboarding state persisted via DataStore Preferences.
2. **Home Dashboard**:
   - Time-based greeting (Good morning/afternoon/evening/night in EN/BN).
   - Real-time date display.
   - Live metrics calculated from Room: Today's tasks count, completed count, active habits checked in, upcoming tasks, pinned notes, and active goals.
   - Monthly Financial Pulse: Total income, total expenses, net balance, remaining budget progress bar.
   - 5 Quick Action launchers (Task, Note, Transaction, Habit, Goal).
3. **Task Management (Full CRUD)**:
   - Create, edit, mark complete/incomplete, delete with confirmation.
   - Optional due date and description.
   - Priority levels: Low, Medium, High.
   - Filter by status (All, Pending, Completed, Overdue).
   - Sort by Due Date, Priority, or Recently Created.
   - Real-time search across task titles and descriptions.
4. **Notes Module (Full CRUD)**:
   - Instant note capture and markdown/bullet text.
   - Pin and unpin notes (pinned notes float to top and display on Home).
   - Real-time search filtering across title and body.
   - Edit, delete with confirmation.
5. **Personal Finance**:
   - Add Income and Expense transactions.
   - Exact integer minor-units (`amountCents`) monetary precision.
   - Default currency BDT (with user preference options for USD, EUR, GBP, INR).
   - Category assignment and optional remarks.
   - Monthly budget limit setting with visual consumption bar.
   - Over-budget alert indicator.
   - Filter by month, type, and keyword search.
6. **Habits Tracker**:
   - Habit creation, editing, archive/reactivate, delete.
   - Daily check-in checkbox.
   - Date-based streak calculation algorithm (`currentStreak` and `longestStreak`) accounting for day boundaries and yesterday continuity.
   - Unique completion constraints preventing duplicate records per habit and calendar day.
7. **Goals Module**:
   - Target goal setting with descriptions.
   - Progress slider/stepper (0% to 100%).
   - Visual progress bars.
   - Status filters (All, Active, Completed).
8. **Settings & Localization**:
   - In-app language toggle between English and Bengali without losing data.
   - Theme toggle (Dark default).
   - Default currency picker.
   - Full JSON data export (shareable or copyable).
   - Safe JSON data import with schema validation.
   - Clear All Data with destructive action confirmation dialog.
   - Comprehensive Privacy policy and local storage transparency.

---

## 5. Security & Privacy Notes

- **Device-Only Sandboxed Storage**: All tasks, notes, finance transactions, and habits are stored inside the app-private SQLite database managed by Room.
- **No Unnecessary Permissions**: No camera, microphone, contacts, location, or telemetry permissions requested.
- **Zero Remote Tracking**: No external analytics, third-party trackers, or cloud calls in the MVP.
- **Local Data Limitations**: Uninstalling the app from the device will remove unexported local records. Users can export backups anytime via `Settings -> Export Data (JSON)`.

---

## 6. Features Intentionally Deferred & Future Roadmap

- **End-to-End Encrypted Cloud Synchronization**: Multi-device sync will be introduced in Phase 2 alongside zero-trust Firebase Firestore integration and Google Sign-In.
- **Push Reminders & Exact Alarms**: Exact alarm scheduling via AlarmManager with notification permissions.
- **LIFEOS Premium Entitlements**: Advanced interactive cash flow charts, automated Google Drive backups, and custom widgets via Google Play Billing.
