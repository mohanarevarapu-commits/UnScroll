package com.example.unscroll;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.unscroll.session.SessionManager;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

/**
 * Activity for configuring and running a focus session with strict mode PIN verification / 5-min wait lock.
 */
public class FocusSessionActivity extends AppCompatActivity {

    private TextView tvSessionStatus;
    private TextView tvTimerDisplay;
    private GardenGrowingTreeView gardenGrowingTree;

    private LinearLayout layoutSetup;
    private RadioGroup rgDuration;
    private MaterialSwitch switchStrictMode;
    private TextInputLayout tilPinCode;
    private EditText etPinCode;
    private Button btnStartSession;

    private LinearLayout layoutActiveControls;
    private Button btnEndSession;

    private SessionManager sessionManager;
    private Handler timerHandler;
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_session);

        sessionManager = SessionManager.getInstance(this);
        timerHandler = new Handler(Looper.getMainLooper());

        tvSessionStatus = findViewById(R.id.tvSessionStatus);
        tvTimerDisplay = findViewById(R.id.tvTimerDisplay);
        gardenGrowingTree = findViewById(R.id.gardenGrowingTree);

        layoutSetup = findViewById(R.id.layoutSetup);
        rgDuration = findViewById(R.id.rgDuration);
        switchStrictMode = findViewById(R.id.switchStrictMode);
        tilPinCode = findViewById(R.id.tilPinCode);
        etPinCode = findViewById(R.id.etPinCode);
        btnStartSession = findViewById(R.id.btnStartSession);

        layoutActiveControls = findViewById(R.id.layoutActiveControls);
        btnEndSession = findViewById(R.id.btnEndSession);

        switchStrictMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tilPinCode.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnStartSession.setOnClickListener(v -> onStartSessionClicked());
        btnEndSession.setOnClickListener(v -> onEndSessionClicked());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimerLoop();
    }

    private void updateUI() {
        if (sessionManager.isSessionActive()) {
            layoutSetup.setVisibility(View.GONE);
            layoutActiveControls.setVisibility(View.VISIBLE);
            tvSessionStatus.setText("⚡ Focus Session in Progress");
            startTimerLoop();
        } else {
            layoutSetup.setVisibility(View.VISIBLE);
            layoutActiveControls.setVisibility(View.GONE);
            tvSessionStatus.setText("Ready to Focus");
            if (gardenGrowingTree != null) {
                gardenGrowingTree.setGrowthProgress(0.15f, false);
            }
            int selectedMins = getSelectedDurationMinutes();
            formatAndDisplayTime(selectedMins * 60L * 1000L);
            stopTimerLoop();
        }
    }

    private int getSelectedDurationMinutes() {
        int id = rgDuration.getCheckedRadioButtonId();
        if (id == R.id.rb15) return 15;
        if (id == R.id.rb45) return 45;
        if (id == R.id.rb60) return 60;
        return 25; // default 25 mins
    }

    private void onStartSessionClicked() {
        int durationMins = getSelectedDurationMinutes();
        boolean strictMode = switchStrictMode.isChecked();
        String pinCode = etPinCode.getText() != null ? etPinCode.getText().toString().trim() : "";

        if (strictMode && TextUtils.isEmpty(pinCode)) {
            Toast.makeText(this, "Please enter a 4-digit PIN for Strict Mode", Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.startSession(durationMins, strictMode, pinCode);
        Toast.makeText(this, "Focus Session Started! Stay Focused.", Toast.LENGTH_SHORT).show();
        updateUI();
    }

    private void onEndSessionClicked() {
        if (!sessionManager.isSessionActive()) {
            return;
        }

        if (sessionManager.isStrictMode()) {
            showStrictModeEarlyEndDialog();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("End Session Early?")
                    .setMessage("Are you sure you want to give up on your focus session?")
                    .setPositiveButton("End Session", (dialog, which) -> {
                        sessionManager.endSession(false);
                        Toast.makeText(this, "Focus Session Cancelled", Toast.LENGTH_SHORT).show();
                        openNotificationDigest();
                        updateUI();
                    })
                    .setNegativeButton("Keep Focusing", null)
                    .show();
        }
    }

    private void showStrictModeEarlyEndDialog() {
        String[] options = {"Verify Security PIN", "Wait 5 Minutes Countdown"};
        new AlertDialog.Builder(this)
                .setTitle("Strict Mode Active")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        promptPinVerification();
                    } else {
                        startWaitCountdownModal();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void promptPinVerification() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("Enter Security PIN")
                .setMessage("Enter your 4-digit PIN to unlock and end early:")
                .setView(input)
                .setPositiveButton("Verify", (dialog, which) -> {
                    String pin = input.getText().toString().trim();
                    if (sessionManager.verifyPin(pin)) {
                        sessionManager.endSession(false);
                        Toast.makeText(this, "PIN Verified. Session ended.", Toast.LENGTH_SHORT).show();
                        openNotificationDigest();
                        updateUI();
                    } else {
                        Toast.makeText(this, "Incorrect PIN!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startWaitCountdownModal() {
        final AlertDialog waitDialog = new AlertDialog.Builder(this)
                .setTitle("Strict Mode Wait Lock")
                .setMessage("Please wait 5 minutes before session can end early:\n5:00 remaining")
                .setCancelable(false)
                .setNegativeButton("Cancel Early End", null)
                .create();

        waitDialog.show();

        new CountDownTimer(5 * 60 * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long mins = millisUntilFinished / (1000 * 60);
                long secs = (millisUntilFinished / 1000) % 60;
                waitDialog.setMessage(String.format(Locale.US, "Please wait 5 minutes before session can end early:\n%02d:%02d remaining", mins, secs));
            }

            @Override
            public void onFinish() {
                waitDialog.dismiss();
                sessionManager.endSession(false);
                Toast.makeText(FocusSessionActivity.this, "Wait period complete. Session ended.", Toast.LENGTH_SHORT).show();
                openNotificationDigest();
                updateUI();
            }
        }.start();
    }

    private void startTimerLoop() {
        stopTimerLoop();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (sessionManager.isSessionActive()) {
                    long remaining = sessionManager.getRemainingTimeMillis();
                    int totalMins = sessionManager.getDurationMinutes();
                    long totalDuration = totalMins * 60L * 1000L;
                    if (gardenGrowingTree != null && totalDuration > 0) {
                        float progressFraction = (float) (totalDuration - remaining) / totalDuration;
                        gardenGrowingTree.setGrowthProgress(Math.max(0.15f, progressFraction), true);
                    }
                    formatAndDisplayTime(remaining);
                    timerHandler.postDelayed(this, 1000);
                } else {
                    if (gardenGrowingTree != null) {
                        gardenGrowingTree.setGrowthProgress(1.0f, true);
                    }
                    Toast.makeText(FocusSessionActivity.this, "🎉 Focus Session Completed!", Toast.LENGTH_LONG).show();
                    openNotificationDigest();
                    updateUI();
                }
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopTimerLoop() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
            timerRunnable = null;
        }
    }

    private void formatAndDisplayTime(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        tvTimerDisplay.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
    }

    private void openNotificationDigest() {
        Intent intent = new Intent(this, NotificationDigestActivity.class);
        startActivity(intent);
    }
}
