package com.example.unscroll.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.unscroll.db.AppDatabase;
import com.example.unscroll.db.FocusSessionEntity;

/**
 * Manager class maintaining focus session state, countdown timer calculations, and PIN lock verification.
 */
public class SessionManager {

    private static final String PREFS_NAME = "unscroll_session_prefs";
    private static final String KEY_ACTIVE_SESSION_ID = "active_session_id";
    private static final String KEY_IS_ACTIVE = "is_active";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_DURATION_MINS = "duration_mins";
    private static final String KEY_STRICT_MODE = "strict_mode";
    private static final String KEY_PIN_CODE = "pin_code";
    private static final String KEY_BYPASS_PACKAGE = "bypass_package";
    private static final String KEY_BYPASS_EXPIRY = "bypass_expiry";

    private static volatile SessionManager INSTANCE;
    private final SharedPreferences prefs;
    private final Context context;

    private SessionManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static SessionManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SessionManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SessionManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public synchronized void startSession(int durationMinutes, boolean strictMode, String pinCode) {
        long startTimeMillis = System.currentTimeMillis();
        long endTimeMillis = startTimeMillis + (durationMinutes * 60L * 1000L);

        FocusSessionEntity entity = new FocusSessionEntity(
                durationMinutes, strictMode, pinCode, false, startTimeMillis, endTimeMillis
        );

        AppDatabase db = AppDatabase.getInstance(context);
        long sessionId = db.sessionDao().insert(entity);

        prefs.edit()
                .putLong(KEY_ACTIVE_SESSION_ID, sessionId)
                .putBoolean(KEY_IS_ACTIVE, true)
                .putLong(KEY_START_TIME, startTimeMillis)
                .putInt(KEY_DURATION_MINS, durationMinutes)
                .putBoolean(KEY_STRICT_MODE, strictMode)
                .putString(KEY_PIN_CODE, pinCode != null ? pinCode : "")
                .apply();
    }

    public synchronized void endSession(boolean completed) {
        long sessionId = prefs.getLong(KEY_ACTIVE_SESSION_ID, -1);
        if (sessionId != -1) {
            AppDatabase db = AppDatabase.getInstance(context);
            FocusSessionEntity entity = db.sessionDao().getSessionById(sessionId);
            if (entity != null) {
                entity.setCompleted(completed);
                entity.setEndTimeMillis(System.currentTimeMillis());
                db.sessionDao().update(entity);
            }
        }

        prefs.edit()
                .putBoolean(KEY_IS_ACTIVE, false)
                .putLong(KEY_ACTIVE_SESSION_ID, -1)
                .apply();
    }

    public boolean isSessionActive() {
        if (!prefs.getBoolean(KEY_IS_ACTIVE, false)) {
            return false;
        }

        long remaining = getRemainingTimeMillis();
        if (remaining <= 0) {
            // Auto-complete expired session
            endSession(true);
            return false;
        }
        return true;
    }

    public long getRemainingTimeMillis() {
        if (!prefs.getBoolean(KEY_IS_ACTIVE, false)) {
            return 0L;
        }

        long startTime = prefs.getLong(KEY_START_TIME, 0L);
        int durationMins = prefs.getInt(KEY_DURATION_MINS, 0);
        long totalDurationMillis = durationMins * 60L * 1000L;
        long elapsedTime = System.currentTimeMillis() - startTime;
        long remaining = totalDurationMillis - elapsedTime;

        return Math.max(0L, remaining);
    }

    public boolean isStrictMode() {
        return prefs.getBoolean(KEY_STRICT_MODE, false);
    }

    public String getPinCode() {
        return prefs.getString(KEY_PIN_CODE, "");
    }

    public boolean verifyPin(String inputPin) {
        String savedPin = getPinCode();
        return savedPin.equals(inputPin);
    }

    public int getDurationMinutes() {
        return prefs.getInt(KEY_DURATION_MINS, 0);
    }

    public synchronized void grantEmergencyBypass(String packageName, long durationMillis) {
        long expiryTime = System.currentTimeMillis() + durationMillis;
        prefs.edit()
                .putString(KEY_BYPASS_PACKAGE, packageName != null ? packageName : "")
                .putLong(KEY_BYPASS_EXPIRY, expiryTime)
                .apply();
    }

    public boolean isEmergencyBypassActive(String packageName) {
        String bypassedPkg = prefs.getString(KEY_BYPASS_PACKAGE, "");
        long expiry = prefs.getLong(KEY_BYPASS_EXPIRY, 0L);
        if (bypassedPkg.isEmpty() || packageName == null) {
            return false;
        }
        if (bypassedPkg.equalsIgnoreCase(packageName) && System.currentTimeMillis() < expiry) {
            return true;
        }
        return false;
    }
}
