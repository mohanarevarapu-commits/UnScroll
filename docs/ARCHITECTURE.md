# UnScroll Technical Architecture

This document describes the internal architecture, Jetpack Compose **Thermal Aurora (Blue Light & Shade)** UI layer, **Phone Lockdown Mode (`15 mins` to `5 hours`)**, background system services, and local Room persistence layer of **UnScroll**.

---

## 1. High-Level System Flow

```mermaid
flowchart TD
    MA["MainActivity (Compose Host)"]
    CPA["CoolingPlanetApp (Root State & Navigation)"]
    HS["ThermalHomeScreen"]
    FSS["ThermalFocusSessionScreen"]
    LSS["ThermalLockdownSetupScreen (15m - 5h)"]
    LAS["ThermalActiveLockdownScreen (Calls & SOS Only)"]
    HMS["ThermalHeatMapScreen (4-Week Grid)"]
    ABS_UI["ThermalAppBlockingScreen & ThermalBlockedScreen"]
    BOA["BlockOverlayActivity (Lockscreen & Overlay Host)"]

    SM["SessionManager (Focus & Lockdown Persistence)"]
    UH["UsageStatsHelper (UsageStatsManager)"]
    ST["StreakTracker (Streaks & Badges)"]

    ABS["AppBlockerService (AccessibilityService)"]
    NSS["NotificationSnoozerService (NotificationListenerService)"]
    BR["BootReceiver (ACTION_BOOT_COMPLETED)"]
    USW["UsageSyncWorker (WorkManager)"]
    DB[("AppDatabase (Room SQLite)")]

    MA --> CPA
    CPA --> HS
    CPA --> FSS
    CPA --> LSS
    CPA --> LAS
    CPA --> HMS
    CPA --> ABS_UI

    LSS --> SM
    LAS --> SM
    ABS --> SM
    ABS --> DB
    ABS --> BOA
    BOA --> LAS
    BOA --> ABS_UI

    BR --> SM
    BR --> BOA
    BR --> USW

    NSS --> DB
    USW --> UH
    USW --> ST
    USW --> DB
```

---

## 2. Phone Lockdown Mode Architecture (`15 mins` to `5 hours`)

```mermaid
sequenceDiagram
    participant User
    participant LockdownUI as ThermalLockdownSetupScreen
    participant SessionMgr as SessionManager
    participant BlockerSvc as AppBlockerService
    participant OverlayAct as BlockOverlayActivity
    participant Dialer as System Dialer / Emergency SOS

    User->>LockdownUI: Selects duration (15m to 5h) & taps "Lockdown Phone"
    LockdownUI->>SessionMgr: startLockdownSeconds(durationSeconds)
    SessionMgr-->>SessionMgr: Persist start_time & duration_seconds in SharedPreferences
    LockdownUI->>User: Display ThermalActiveLockdownScreen (BackHandler blocks back button)

    alt User tries to exit to Home Launcher or another app
        BlockerSvc->>SessionMgr: isLockdownActive() == true
        BlockerSvc->>BlockerSvc: isPackageAllowedDuringLockdown(pkg) == false
        BlockerSvc->>OverlayAct: startActivity(EXTRA_LOCKDOWN_MODE = true)
        OverlayAct->>User: Re-displays ThermalActiveLockdownScreen over lockscreen
    else Incoming Phone Call or User taps "Emergency SOS" / "Phone Calls"
        User->>Dialer: ACTION_DIAL / tel:112 / Incoming Call UI
        BlockerSvc->>BlockerSvc: isPackageAllowedDuringLockdown(pkg) == true (incallui/dialer/telecom/emergency)
        Dialer->>User: Phone Call or Emergency SOS proceeds normally
    else Countdown reaches 00:00
        OverlayAct->>SessionMgr: clearExpiredLockdown()
        OverlayAct->>User: Shows "Unlock & Return Home" button
    end
```

### Key Safeguards in Lockdown Mode
1. **Duration Clamping (`SessionManager.clampLockdownMinutes`):** Enforces the `15` to `300` minute (`15m` – `5h`) range for standard lockdown sessions while also supporting a `15s Demo` mode for quick testing.
2. **Call & Emergency Allowlist (`AppBlockerService.isPackageAllowedDuringLockdown`):**
   - Permits `incallui`, `dialer`, `telecom`, `emergency`, `safetyhub`, `com.android.phone`, and `com.android.systemui`.
   - Intercepts all other foreground packages (launchers, browsers, social apps, settings) on `TYPE_WINDOW_STATE_CHANGED` and routes back to `BlockOverlayActivity` in Lockdown Mode.
3. **Lockscreen & Reboot Persistence (`BlockOverlayActivity` & `BootReceiver`):**
   - `MainActivity` and `BlockOverlayActivity` enable `setShowWhenLocked(true)` and `setTurnScreenOn(true)` on API 27+.
   - `BootReceiver` checks `sessionManager.isLockdownActive()` on `ACTION_BOOT_COMPLETED` and immediately launches `BlockOverlayActivity` if a lockdown timer is still active.

---

## 3. Jetpack Compose UI Layer ("Thermal Aurora — Blue Light & Shade")

- **[`AuroraBackground.kt`](../app/src/main/java/com/example/unscroll/ui/AuroraBackground.kt):**
  - Renders a full-screen vertical gradient (`#102A52` → `#091933` → `#040B18`) with three slowly drifting light spheres.
  - Uses `Modifier.blur(..., edgeTreatment = BlurredEdgeTreatment.Unbounded)` and explicit pixel-radius `Canvas` radial gradients (`radius = size.minDimension / 2f`) so soft glows never clip against rectangular bounds.
- **[`GlassComponents.kt`](../app/src/main/java/com/example/unscroll/ui/GlassComponents.kt):**
  - Provides `GlassCard`, `GlassChip`, `AuroraPrimaryButton`, `GlassOutlineButton`, and `GlassFloatingBottomBar` (`68dp` height, `34dp` corner radius, minimum `48dp` touch targets).

---

## 4. Local Persistence (`com.example.unscroll.db`)

UnScroll stores all user preferences, quieted apps, focus sessions, and snoozed notifications locally on the device using **Room (`AppDatabase`)**:

| Entity | DAO | Purpose |
| :--- | :--- | :--- |
| `AppEntity` | `AppDao` | Stores package name, app name, block status (`isBlocked`), notification snooze status (`isSnoozed`), and time limits. |
| `DailyUsageEntity` | `DailyUsageDao` | Records daily total screen time, saved time, and thermal state history for insights and heat map visualization. |
| `FocusSessionEntity` | `SessionDao` | Logs completed and active focus sessions (`startTime`, `durationMinutes`, `completed`). |
| `BlockedNotificationEntity` | `NotificationDao` | Stores intercepted promotional notifications (`packageName`, `title`, `text`, `timestamp`) for the Quiet Digest. |
| `ReflectionEntity` | `ReflectionDao` | Saves user reflections entered when pausing before opening a quieted app. |
