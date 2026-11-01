# UnScroll — Scroll less. Live more.

**UnScroll** is a mindful Android digital wellbeing app designed to help you reclaim focus from short-form social media feeds and noisy e-commerce notifications — using positive, calm design rather than guilt or shaming.

---

## ✨ Core Features

### 1. Quiet Social Media & Short-Form Feeds
- **Selective App Quieting:** Pause distractions from **Instagram, Snapchat, WhatsApp, Facebook, YouTube, and YouTube Shorts** for a set schedule or focus session.
- **Mindful Pause Overlay (`BlockOverlayActivity`):** Instead of harsh "BLOCKED" warnings, UnScroll greets you with positive phrasing (*"Instagram is quiet until 6:00 PM"*), a breathing animation, and an optional mindful reflection prompt.
- **Strict Mode:** Prevents impulsive unblocking while a focus session or quiet window is active.

### 2. E-Commerce & Delivery Notification Digest
- **Smart Spam Snoozer (`NotificationSnoozerService`):** Automatically intercepts promotional notification spam from **Flipkart, Myntra, Swiggy, Zomato, Zepto, and Blinkit**.
- **Quiet Digest (`NotificationDigestActivity`):** Collects snoozed notifications into a calm, reviewable digest so you never miss important updates while keeping your lock screen quiet.

### 3. Heat-to-Cool Screen Time Progression
- **Accessible Thermal Palette (`ThemeManager`):** As your daily screen time decreases relative to your goal, the app's visual accents transition smoothly from **Hot Red-Orange** to **Cool Blue**.
- **Always Paired with Labels:** Every thermal state includes a clear text label (*Hot — High Screen Time*, *Warm*, *Moderate*, *Cool*, *Coolest — Optimal Balance*) so color is never the only indicator.

| State | Usage Ratio | Hex Color | Meaning |
| :--- | :--- | :--- | :--- |
| **Coolest** | `< 20%` | `#5E81AC` (Cool Blue) | Optimal balance |
| **Cool** | `20% – 45%` | `#8AB89E` (Mint Sage) | Mindful screen time |
| **Moderate** | `45% – 70%` | `#E9C46A` (Pale Yellow) | Approaching daily target |
| **Warm** | `70% – 95%` | `#F4A261` (Warm Amber) | Nearing daily limit |
| **Hot** | `≥ 95%` | `#E76F51` (Red-Orange) | High screen time |

### 4. Custom "Dawn" Graphics & Visuals
- **Sunrise Progress Ring (`CircularProgressView`):** Smooth 12dp ring on a warm sand track displaying remaining focus time and current Heat-to-Cool status.
- **Focus Garden (`GardenGrowingTreeView`):** Custom botanical canvas illustration that grows from a seed into a full-canopy tree during focus sessions (`FocusSessionActivity`) and across your weekly focus history.
- **7×4 Weekly Heat Map (`HeatMapView`):** Visualizes screen-time intensity across Morning, Afternoon, Evening, and Night blocks for every day of the week.
- **Streaks & Milestones (`StreakTracker`):** Tracks consecutive mindful days with **3-day, 7-day, and 30-day** milestone badges and a shareable weekly recap.

---

## 🎨 Theme & Design System: "Dawn"

- **Concept:** Your day is a sunrise, and every focused minute gives you more light.
- **Surface Palette:**
  - **Background:** Warm Ivory (`#F6F3EE`)
  - **Cards:** Crisp White (`#FFFFFF`) with `24dp` rounded corners and soft elevation
  - **Primary Accent:** Sage Green (`#7FB69A`) to Deep Teal (`#3E8E7E`) gradient buttons with pure white text
  - **Secondary Accents:** Soft Lavender (`#EAE4F2`), Warm Sand (`#EDE8E1`), and Terracotta Clay (`#D9886E`)
- **Typography:** Serif display headings and numbers (`Fraunces` style) paired with clean sans-serif body copy (`Inter` style).
- **Navigation:** Simple 3-tab bottom navigation (**Home**, **Insights**, **Settings**).

---

## 🏗️ Architecture & Project Structure

```text
app/src/main/java/com/example/unscroll/
├── MainActivity.java                 # 3-tab host (Home, Insights, Settings)
├── FocusSessionActivity.java         # Active timer with growing botanical tree
├── BlockOverlayActivity.java         # Mindful breathing & reflection overlay
├── NotificationDigestActivity.java   # Quiet digest for snoozed notifications
├── AppPickerActivity.java            # Select apps to quiet or snooze
├── PermissionsSetupActivity.java     # Gentle step-by-step permission onboarding
├── OnboardingActivity.java           # First-launch Dawn introduction
├── ThemeManager.java                 # Heat-to-Cool color & accessibility label engine
├── InsightsHelper.java               # Populates Insights tab, tree, & heat map
├── CircularProgressView.java         # Custom Canvas Sunrise Progress Ring
├── GardenGrowingTreeView.java        # Custom Canvas 5-stage botanical tree
├── HeatMapView.java                  # Custom Canvas 7x4 thermal usage matrix
├── db/                               # Room Database, DAOs, and Entities
│   ├── AppDatabase.java
│   ├── AppEntity.java & AppDao.java
│   ├── DailyUsageEntity.java & DailyUsageDao.java
│   ├── FocusSessionEntity.java & SessionDao.java
│   ├── BlockedNotificationEntity.java & NotificationDao.java
│   └── ReflectionEntity.java & ReflectionDao.java
├── service/
│   ├── AppBlockerService.java        # AccessibilityService detecting foreground apps & Shorts
│   └── NotificationSnoozerService.java # NotificationListenerService intercepting spam
├── usage/
│   └── UsageStatsHelper.java         # Queries Android UsageStatsManager
├── session/
│   └── SessionManager.java           # Manages active focus timers & Strict Mode
├── streak/
│   └── StreakTracker.java            # Tracks daily streaks & 3/7/30-day badges
└── worker/
    └── UsageSyncWorker.java          # Periodic WorkManager sync for usage & streaks
```

For deep-dive technical details on services, database schema, and custom views, see [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

---

## 🔐 Required Android Permissions

UnScroll uses standard Android system APIs and keeps **100% of your data on-device** in a local Room database:

1. **Usage Access (`PACKAGE_USAGE_STATS`):** Measures daily screen time per app and calculates your Heat-to-Cool level.
2. **Accessibility Service (`AppBlockerService`):** Detects when a quieted app or short-form video feed (such as YouTube Shorts) is opened so UnScroll can show the mindful pause screen.
3. **Notification Access (`NotificationSnoozerService`):** Silences promotional notifications from selected shopping and food delivery apps and stores them in your Quiet Digest.

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Ladybug or newer recommended)
- **JDK 11+**
- **Android SDK:** `minSdk = 24` (Android 7.0), `targetSdk = 37`

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/mohanarevarapu-commits/UnScroll.git
   cd UnScroll
   ```
2. Build the debug APK via Gradle Wrapper:
   ```bash
   ./gradlew assembleDebug
   ```
3. Install and launch on a connected device or emulator:
   ```bash
   ./gradlew installDebug
   ```

---

## 📄 License

This project is built for mindful digital wellbeing on Android.
