# 🍽️ Meal Cycle & Payment Tracker

A complete native Android app in Kotlin for tracking 90-meal delivery cycles with multi-user support, calorie-free math, and a polished Material 3 UI.

---

## 📋 Prerequisites

| Tool | Version |
|---|---|
| Android Studio | Hedgehog (2023.1.1) or newer |
| JDK | 17 |
| Android SDK | 34 |
| Minimum SDK | 26 (Android 8.0) |

---

## 🚀 Setup & Build

### 1. Open in Android Studio
```
File → Open → select the project root folder (containing settings.gradle.kts)
```

### 2. Sync Gradle
Click **"Sync Now"** in the yellow banner, or go to:
```
File → Sync Project with Gradle Files
```

### 3. Run on device / emulator
- Select a device running **API 26+**
- Click ▶ **Run** (`Shift+F10`)

> **Note:** The app downloads the Poppins font from Google Fonts on first launch. An internet connection is required on the first run. After that the font is cached.

### 4. Build release APK
```
Build → Generate Signed App Bundle / APK → APK
→ Create or select your keystore → Build
```
Output: `app/release/app-release.apk`

### 5. Command-line build
```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```

---

## 📁 Project Structure

```
app/src/main/
├── java/com/mealcycle/app/
│   ├── MainActivity.kt                   ← Entry point + NavHost
│   ├── MealCycleApp.kt                   ← Hilt Application
│   │
│   ├── data/
│   │   ├── db/
│   │   │   ├── AppDatabase.kt
│   │   │   ├── MealEntryDao.kt
│   │   │   ├── PlanHistoryDao.kt
│   │   │   └── UserDao.kt
│   │   ├── model/
│   │   │   ├── MealEntry.kt
│   │   │   ├── PlanHistory.kt
│   │   │   └── User.kt
│   │   ├── repository/
│   │   │   ├── MealRepository.kt         ← 90-meal cap enforced here
│   │   │   ├── HistoryRepository.kt
│   │   │   └── UserRepository.kt
│   │   └── datastore/
│   │       └── UserPreferences.kt
│   │
│   ├── di/
│   │   └── DatabaseModule.kt             ← Hilt Room bindings
│   │
│   ├── ui/
│   │   ├── theme/
│   │   │   ├── Color.kt
│   │   │   ├── Type.kt                   ← Poppins via Google Fonts provider
│   │   │   └── Theme.kt
│   │   ├── navigation/
│   │   │   └── NavGraph.kt
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   ├── HomeViewModel.kt
│   │   │   ├── MealButtons.kt
│   │   │   ├── StatsCards.kt
│   │   │   ├── ProgressBar.kt
│   │   │   └── EndPlanDialog.kt
│   │   ├── calendar/
│   │   │   └── CalendarView.kt           ← Custom native calendar
│   │   ├── history/
│   │   │   ├── HistoryScreen.kt
│   │   │   └── HistoryViewModel.kt
│   │   └── profile/
│   │       ├── ProfileScreen.kt
│   │       └── ProfileViewModel.kt
│   │
│   └── utils/
│       └── MealCalculations.kt           ← Pure calculation functions
│
└── res/
    ├── values/
    │   ├── strings.xml
    │   ├── colors.xml
    │   ├── themes.xml
    │   └── font_certs.xml                ← Google Fonts certificates
    ├── drawable/
    │   └── ic_launcher_foreground.xml
    └── mipmap-anydpi-v26/
        └── ic_launcher.xml
```

---

## 🧠 Core Rules (enforced in code)

| Rule | Where enforced |
|---|---|
| 90-meal hard cap — never exceeded | `MealRepository.toggleMeal()` |
| Future dates blocked | `HomeViewModel.goToNextDay()`, `DatePicker.dateValidator` |
| All calculations pure (no averages) | `MealCalculations.kt` |
| All data isolated per userId | Every DAO query has `WHERE userId = :userId` |
| StateFlow + collectAsStateWithLifecycle | All ViewModels |

---

## 🎨 Design System

| Token | Value |
|---|---|
| Primary | `#4F46E5` (Indigo) |
| Delivered | `#22C55E` (Green) |
| Undelivered | `#D1D5DB` (Grey) |
| Background | `#F8F9FB` |
| Card | `#FFFFFF` |
| Font | Poppins (Google Fonts) |

---

## 🔢 Calculation Logic

All calculations are **purely meal-count driven**:

```kotlin
val remaining       = 90 - deliveredMeals
val remainingDays   = remaining / 3
val remainingExtra  = remaining % 3
val amountSpent     = deliveredMeals * 50   // ₹50 per meal
val refundAmount    = remaining * 50
```

> ❌ No day-based logic. ❌ No averages. ✅ 90 meals = 1 complete cycle.

---

## 👥 Multi-User Support

- Each user has a unique UUID stored in the `users` Room table
- Active user is stored in Jetpack DataStore
- All DAO queries filter by `userId`
- Profile screen: add, switch, delete users with photo picker support

---

## 🗄️ Database

Room database `meal_cycle_db` with 3 tables:

| Table | Purpose |
|---|---|
| `meal_entries` | All meal toggle records |
| `plan_history` | Completed cycle summaries |
| `users` | User profiles |
