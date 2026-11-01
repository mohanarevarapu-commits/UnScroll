package com.example.unscroll;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unscroll.db.AppDatabase;
import com.example.unscroll.db.AppEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Activity for selecting which installed applications to block during focus sessions.
 * Features a categorized searchable list ("Social", "Video", "Shopping", "Other"),
 * soft toggle switches, and Room Database persistence.
 */
public class AppPickerActivity extends AppCompatActivity {

    private RecyclerView rvApps;
    private ProgressBar progressBar;
    private EditText etSearch;
    private AppAdapter appAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_picker);

        rvApps = findViewById(R.id.rvApps);
        progressBar = findViewById(R.id.progressBar);
        etSearch = findViewById(R.id.etSearch);

        rvApps.setLayoutManager(new LinearLayoutManager(this));

        setupSearch();
        loadInstalledApps();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (appAdapter != null) {
                    appAdapter.filterAndGroup(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadInstalledApps() {
        progressBar.setVisibility(View.VISIBLE);

        Executors.newSingleThreadExecutor().execute(() -> {
            PackageManager pm = getPackageManager();

            // Query all launchable apps
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);

            // Fetch blocked apps from Room DB
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<AppEntity> savedApps = db.appDao().getAllApps();
            Map<String, Boolean> blockedMap = new HashMap<>();
            for (AppEntity entity : savedApps) {
                blockedMap.put(entity.getPackageName(), entity.isBlocked());
            }

            List<AppAdapter.AppItem> items = new ArrayList<>();
            Set<String> seenPackages = new HashSet<>();
            String myPackage = getPackageName();

            for (ResolveInfo resolveInfo : resolveInfos) {
                if (resolveInfo.activityInfo == null || resolveInfo.activityInfo.packageName == null) {
                    continue;
                }

                String packageName = resolveInfo.activityInfo.packageName;

                // Exclude UnScroll itself
                if (packageName.equalsIgnoreCase(myPackage) || packageName.equalsIgnoreCase("com.example.unscroll")) {
                    continue;
                }

                // Deduplicate launchable activities
                if (seenPackages.contains(packageName)) {
                    continue;
                }
                seenPackages.add(packageName);

                CharSequence labelSeq = resolveInfo.loadLabel(pm);
                String appName = (labelSeq != null) ? labelSeq.toString() : packageName;

                Drawable icon = null;
                try {
                    icon = resolveInfo.loadIcon(pm);
                } catch (Exception ignored) {}

                boolean isBlocked = Boolean.TRUE.equals(blockedMap.get(packageName));
                String category = determineCategory(packageName, appName);

                items.add(new AppAdapter.AppItem(packageName, appName, icon, isBlocked, category));
            }

            // Sort overall items alphabetically initially
            Collections.sort(items, (a, b) -> a.getAppName().compareToIgnoreCase(b.getAppName()));

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                appAdapter = new AppAdapter(items);
                rvApps.setAdapter(appAdapter);
            });
        });
    }

    /**
     * Categorizes an application into "Social", "Video", "Shopping", or "Other".
     */
    private String determineCategory(String packageName, String appName) {
        String pkg = packageName != null ? packageName.toLowerCase(Locale.ROOT) : "";
        String name = appName != null ? appName.toLowerCase(Locale.ROOT) : "";

        // 1. Social
        if (pkg.contains("instagram") || pkg.contains("whatsapp") || pkg.contains("snapchat") ||
            pkg.contains("facebook") || pkg.contains("twitter") || pkg.contains("x.com") ||
            pkg.contains("telegram") || pkg.contains("reddit") || pkg.contains("discord") ||
            pkg.contains("linkedin") || pkg.contains("pinterest") || pkg.contains("threads") ||
            pkg.contains("signal") ||
            name.contains("instagram") || name.contains("whatsapp") || name.contains("snapchat") ||
            name.contains("facebook") || name.contains("twitter") || name.contains("telegram") ||
            name.contains("reddit") || name.contains("discord") || name.contains("linkedin") ||
            name.contains("threads") || name.contains("signal")) {
            return "Social";
        }

        // 2. Video
        if (pkg.contains("youtube") || pkg.contains("tiktok") || pkg.contains("netflix") ||
            pkg.contains("primevideo") || pkg.contains("disney") || pkg.contains("hotstar") ||
            pkg.contains("twitch") || pkg.contains("vlc") || pkg.contains("mxplayer") ||
            pkg.contains("spotify") || pkg.contains("prime") || pkg.contains("video") ||
            pkg.contains("player") ||
            name.contains("youtube") || name.contains("tiktok") || name.contains("netflix") ||
            name.contains("prime video") || name.contains("disney") || name.contains("hotstar") ||
            name.contains("twitch") || name.contains("spotify") || name.contains("vlc") ||
            name.contains("video") || name.contains("player")) {
            return "Video";
        }

        // 3. Shopping
        if (pkg.contains("flipkart") || pkg.contains("amazon") || pkg.contains("myntra") ||
            pkg.contains("swiggy") || pkg.contains("zomato") || pkg.contains("zepto") ||
            pkg.contains("blinkit") || pkg.contains("meesho") || pkg.contains("ajio") ||
            pkg.contains("bigbasket") || pkg.contains("grofers") || pkg.contains("paytm") ||
            pkg.contains("shop") || pkg.contains("store") ||
            name.contains("flipkart") || name.contains("amazon") || name.contains("myntra") ||
            name.contains("swiggy") || name.contains("zomato") || name.contains("zepto") ||
            name.contains("blinkit") || name.contains("meesho") || name.contains("ajio") ||
            name.contains("shopping") || name.contains("store") || name.contains("paytm")) {
            return "Shopping";
        }

        // 4. Other
        return "Other";
    }
}
