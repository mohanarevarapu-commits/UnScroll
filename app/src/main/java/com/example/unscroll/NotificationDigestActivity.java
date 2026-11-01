package com.example.unscroll;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unscroll.db.AppDatabase;
import com.example.unscroll.db.BlockedNotificationEntity;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * Activity presenting the summary digest of all notifications captured and snoozed during focus sessions,
 * grouped by app with clean "Clear all" button and zero shaming language.
 */
public class NotificationDigestActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private TextView tvEmptyState;
    private TextView tvDigestSummary;
    private Button btnClearAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_digest);

        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvDigestSummary = findViewById(R.id.tvDigestSummary);
        btnClearAll = findViewById(R.id.btnClearAll);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        btnClearAll.setOnClickListener(v -> clearAllNotifications());

        loadNotifications();
    }

    private void loadNotifications() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<BlockedNotificationEntity> notifications = db.notificationDao().getAllNotifications();

            runOnUiThread(() -> {
                if (notifications.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvNotifications.setVisibility(View.GONE);
                    tvDigestSummary.setText("Quiet space. No notifications intercepted.");
                    tvEmptyState.setText("Quiet space. No notifications intercepted during focus hours.");
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    rvNotifications.setVisibility(View.VISIBLE);
                    tvDigestSummary.setText(notifications.size() + " notification" + (notifications.size() == 1 ? "" : "s") + " saved during focus");
                    rvNotifications.setAdapter(new NotificationAdapter(notifications));
                }
            });
        });
    }

    private void clearAllNotifications() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.notificationDao().deleteAll();

            runOnUiThread(() -> {
                Toast.makeText(this, "Notification digest cleared", Toast.LENGTH_SHORT).show();
                loadNotifications();
            });
        });
    }
}
