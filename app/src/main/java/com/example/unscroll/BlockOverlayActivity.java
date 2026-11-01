package com.example.unscroll;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.unscroll.databinding.ActivityBlockOverlayBinding;
import com.example.unscroll.session.SessionManager;
import com.example.unscroll.usage.UsageStatsHelper;

import java.util.Locale;

/**
 * Full-screen mindfulness pause activity launched when a user attempts to open a target social media app while App Blocker is enabled.
 * Displays calm twilight styling, slow breathing animation circle, gentle mindful message, remaining session time,
 * and strict mode PIN / 5-minute wait security enforcement.
 */
public class BlockOverlayActivity extends AppCompatActivity {

    public static final String EXTRA_TARGET_PACKAGE = "extra_target_package";
    private ActivityBlockOverlayBinding binding;
    private CountDownTimer breathingTimer;
    private CountDownTimer sessionTimer;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        binding = ActivityBlockOverlayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);

        String targetPackage = getIntent().getStringExtra(EXTRA_TARGET_PACKAGE);
        String appName = getFriendlyAppName(targetPackage);

        // Format title gracefully: e.g. "Pausing YouTube for your peace of mind"
        String title = getString(R.string.title_block_overlay, appName);
        binding.tvBlockTitle.setText(title);

        // Start slow breathing animation on the breathing circle view
        Animation breathingAnim = AnimationUtils.loadAnimation(this, R.anim.breathing_anim);
        if (breathingAnim != null) {
            binding.flBreathingCircle.startAnimation(breathingAnim);
        }

        // Start session time remaining ticker
        startSessionTimeTicker();

        // Set listener to return safely to focus / home screen
        binding.btnReturnHome.setOnClickListener(v -> handleExitAttempt());

        // Set listener for Go back button
        binding.btnGoBack.setOnClickListener(v -> handleExitAttempt());

        // Set listener for Take 5 Deep Breaths mindful pause
        binding.btnTakeBreaths.setOnClickListener(v -> startMindfulBreathingExercise());

        // Override back press to enforce mindful pause & strict mode protection
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleExitAttempt();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (breathingTimer != null) {
            breathingTimer.cancel();
            breathingTimer = null;
        }
        if (sessionTimer != null) {
            sessionTimer.cancel();
            sessionTimer = null;
        }
    }

    /**
     * Updates session time remaining in real time.
     */
    private void startSessionTimeTicker() {
        long remainingMillis = sessionManager.getRemainingTimeMillis();
        if (remainingMillis <= 0) {
            binding.tvSessionTimeRemaining.setText("No active focus session");
            return;
        }

        sessionTimer = new CountDownTimer(remainingMillis, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long totalSeconds = millisUntilFinished / 1000L;
                long minutes = totalSeconds / 60L;
                long seconds = totalSeconds % 60L;
                binding.tvSessionTimeRemaining.setText(
                        String.format(Locale.US, "Quiet until focus completes • %02d:%02d remaining", minutes, seconds)
                );
            }

            @Override
            public void onFinish() {
                binding.tvSessionTimeRemaining.setText("Focus session completed • You're free to browse mindfully");
            }
        }.start();
    }

    /**
     * Resolves human-readable app name from package name.
     */
    private String getFriendlyAppName(String packageName) {
        if (packageName == null) {
            return "App";
        }

        switch (packageName.toLowerCase()) {
            case UsageStatsHelper.PKG_INSTAGRAM:
                return "Instagram";
            case UsageStatsHelper.PKG_SNAPCHAT:
                return "Snapchat";
            case UsageStatsHelper.PKG_WHATSAPP:
                return "WhatsApp";
            case UsageStatsHelper.PKG_FACEBOOK:
                return "Facebook";
            case UsageStatsHelper.PKG_YOUTUBE:
                return "YouTube";
            default:
                try {
                    PackageManager pm = getPackageManager();
                    ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
                    CharSequence label = pm.getApplicationLabel(ai);
                    return label.toString();
                } catch (PackageManager.NameNotFoundException e) {
                    return packageName;
                }
        }
    }

    /**
     * Handles exit attempt. If strict mode is active, requires entering 4-digit PIN or 5-minute wait.
     */
    private void handleExitAttempt() {
        if (sessionManager.isStrictMode() && sessionManager.isSessionActive()) {
            showStrictModeExitDialog();
        } else {
            navigateToHomeScreen();
        }
    }

    private void showStrictModeExitDialog() {
        String[] options = {"Enter 4-Digit PIN", "Wait 5-Minute Countdown"};
        new AlertDialog.Builder(this)
                .setTitle("Strict Mode Active")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        promptPinVerification();
                    } else {
                        startWaitCountdownModal();
                    }
                })
                .setNegativeButton("Stay Focused", null)
                .show();
    }

    private void promptPinVerification() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("4-Digit PIN");

        new AlertDialog.Builder(this)
                .setTitle("Security PIN Verification")
                .setMessage("Please enter your 4-digit PIN to exit:")
                .setView(input)
                .setPositiveButton("Verify", (dialog, which) -> {
                    String pin = input.getText().toString().trim();
                    if (sessionManager.verifyPin(pin)) {
                        Toast.makeText(this, "PIN Verified. Exiting session.", Toast.LENGTH_SHORT).show();
                        sessionManager.endSession(false);
                        navigateToHomeScreen();
                    } else {
                        Toast.makeText(this, "Incorrect PIN. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startWaitCountdownModal() {
        final AlertDialog waitDialog = new AlertDialog.Builder(this)
                .setTitle("Strict Mode Wait Lock")
                .setMessage("Please wait 5 minutes before exiting:\n05:00 remaining")
                .setCancelable(false)
                .setNegativeButton("Cancel", null)
                .create();

        waitDialog.show();

        new CountDownTimer(5 * 60 * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long mins = millisUntilFinished / (1000L * 60L);
                long secs = (millisUntilFinished / 1000L) % 60L;
                waitDialog.setMessage(String.format(Locale.US, "Please wait 5 minutes before exiting:\n%02d:%02d remaining", mins, secs));
            }

            @Override
            public void onFinish() {
                waitDialog.dismiss();
                Toast.makeText(BlockOverlayActivity.this, "Wait period complete. Exiting.", Toast.LENGTH_SHORT).show();
                sessionManager.endSession(false);
                navigateToHomeScreen();
            }
        }.start();
    }

    /**
     * Launches an interactive mindful breathing exercise dialog guiding the user through 5 deep breaths.
     */
    private void startMindfulBreathingExercise() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_mindful_breathing, null);
        TextView tvBreathCounter = dialogView.findViewById(R.id.tvBreathCounter);
        TextView tvBreathInstruction = dialogView.findViewById(R.id.tvBreathInstruction);
        String targetPackage = getIntent().getStringExtra(EXTRA_TARGET_PACKAGE);

        final AlertDialog breathingDialog = new AlertDialog.Builder(this)
                .setTitle("SOS Mindful Pause (5 Breaths)")
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("Complete & Access App (2-Min Break)", (dialog, which) -> {
                    sessionManager.grantEmergencyBypass(targetPackage, 2 * 60 * 1000L);
                    Toast.makeText(this, "Emergency 2-minute mindful break granted.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .setNegativeButton("Return to Focus", (dialog, which) -> navigateToHomeScreen())
                .create();

        breathingDialog.show();

        // 5 breaths total, 12 seconds per breath (4s inhale, 4s hold, 4s exhale) = 60,000 ms total
        breathingTimer = new CountDownTimer(60000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long elapsed = 60000L - millisUntilFinished;
                int currentBreath = (int) (elapsed / 12000L) + 1;
                if (currentBreath > 5) currentBreath = 5;

                long breathTimeInCycle = elapsed % 12000L;
                String phaseText;
                if (breathTimeInCycle < 4000L) {
                    phaseText = "🌱 Breathe In Deeply (4s)";
                } else if (breathTimeInCycle < 8000L) {
                    phaseText = "🪷 Hold Gently (4s)";
                } else {
                    phaseText = "🌊 Exhale Slowly (4s)";
                }

                tvBreathCounter.setText(String.format(Locale.US, "Breath %d of 5", currentBreath));
                tvBreathInstruction.setText(phaseText);
            }

            @Override
            public void onFinish() {
                tvBreathCounter.setText("✨ Complete!");
                tvBreathInstruction.setText("5 breaths complete. Emergency break unlocked.");
                Toast.makeText(BlockOverlayActivity.this, "5 mindful breaths complete. 2-minute emergency break unlocked.", Toast.LENGTH_LONG).show();
                sessionManager.grantEmergencyBypass(targetPackage, 2 * 60 * 1000L);
                finish();
            }
        }.start();
    }

    /**
     * Launches the home screen and finishes this activity.
     */
    private void navigateToHomeScreen() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        finish();
    }
}
