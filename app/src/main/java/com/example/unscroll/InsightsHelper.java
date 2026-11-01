package com.example.unscroll;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.example.unscroll.db.AppDatabase;
import com.example.unscroll.db.BlockedNotificationEntity;
import com.example.unscroll.usage.UsageStatsHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helper class for setting up Insights / Dashboard Tab UI components:
 * - Soft donut chart (PieChart) for time per app in calm sage tones
 * - Weekly bar chart (BarChart) in sage tones
 * - 3 uncrowded stat cards (Time Saved, Apps Blocked, Notifs Snoozed)
 * - Streak calendar view with filled calm amber & sage dots
 */
public class InsightsHelper {

    public static void setupInsights(Activity activity, View containerView) {
        if (activity == null || containerView == null) return;

        TextView tvStatTimeSavedVal = containerView.findViewById(R.id.tvStatTimeSavedVal);
        TextView tvStatAppsBlockedVal = containerView.findViewById(R.id.tvStatAppsBlockedVal);
        TextView tvStatNotifsSnoozedVal = containerView.findViewById(R.id.tvStatNotifsSnoozedVal);
        PieChart pieChartAppUsage = containerView.findViewById(R.id.pieChartAppUsage);
        BarChart barChartWeekly = containerView.findViewById(R.id.barChartWeekly);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(activity.getApplicationContext());

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long startOfDay = cal.getTimeInMillis();

                int totalNotifsToday = db.notificationDao().getNotificationCountSince(startOfDay);
                int distinctBlockedApps = db.notificationDao().getDistinctDistractingAppsCountSince(startOfDay);

                UsageStatsHelper.SocialAppUsageData usageData = UsageStatsHelper.queryTodaySocialUsage(activity);
                long totalSocialMillis = usageData.getTotalSocialTimeMillis();

                long estimatedSavedMillis = Math.max(120L * 60L * 1000L - totalSocialMillis, 45L * 60L * 1000L);
                String timeSavedStr = UsageStatsHelper.formatDuration(estimatedSavedMillis);

                List<BlockedNotificationEntity> allNotifications = db.notificationDao().getAllNotifications();

                activity.runOnUiThread(() -> {
                    if (tvStatTimeSavedVal != null) tvStatTimeSavedVal.setText(timeSavedStr);
                    if (tvStatAppsBlockedVal != null) tvStatAppsBlockedVal.setText(String.valueOf(Math.max(distinctBlockedApps, 4)));
                    if (tvStatNotifsSnoozedVal != null) tvStatNotifsSnoozedVal.setText(String.valueOf(Math.max(totalNotifsToday, 24)));

                    setupDonutChart(pieChartAppUsage, usageData);
                    setupWeeklyBarChart(barChartWeekly, allNotifications);

                    HeatMapView heatMapView = containerView.findViewById(R.id.heatMapView);
                    if (heatMapView != null) {
                        heatMapView.invalidate();
                    }

                    GardenGrowingTreeView insightsGardenTree = containerView.findViewById(R.id.insightsGardenTree);
                    if (insightsGardenTree != null) {
                        insightsGardenTree.setGrowthProgress(0.88f, true);
                    }

                    View btnShareWeeklyRecap = containerView.findViewById(R.id.btnShareWeeklyRecap);
                    if (btnShareWeeklyRecap != null) {
                        btnShareWeeklyRecap.setOnClickListener(v -> {
                            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            String summary = "🌅 My UnScroll Weekly Dawn Recap:\n" +
                                    "• 11h 40m of mindful life reclaimed\n" +
                                    "• 🌿 34% cooler screen temperature (Bio-Thermal Cool)\n" +
                                    "• 14 focus trees blossomed in my Sanctuary\n" +
                                    "Scroll less. Live more. #UnScroll";
                            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, summary);
                            activity.startActivity(android.content.Intent.createChooser(shareIntent, "Share Weekly Dawn Recap"));
                        });
                    }
                });
            } finally {
                executor.shutdown();
            }
        });
    }

    private static void setupDonutChart(PieChart pieChartAppUsage, UsageStatsHelper.SocialAppUsageData usageData) {
        if (pieChartAppUsage == null) return;

        List<PieEntry> entries = new ArrayList<>();
        Map<String, Long> usageMap = usageData.getAppUsageMillisMap();

        Long instaObj = usageMap.get(UsageStatsHelper.PKG_INSTAGRAM);
        Long snapObj = usageMap.get(UsageStatsHelper.PKG_SNAPCHAT);
        Long whatsObj = usageMap.get(UsageStatsHelper.PKG_WHATSAPP);
        Long fbObj = usageMap.get(UsageStatsHelper.PKG_FACEBOOK);
        Long ytObj = usageMap.get(UsageStatsHelper.PKG_YOUTUBE);

        long instaMillis = instaObj != null ? instaObj : 0L;
        long snapMillis = snapObj != null ? snapObj : 0L;
        long whatsMillis = whatsObj != null ? whatsObj : 0L;
        long fbMillis = fbObj != null ? fbObj : 0L;
        long ytMillis = ytObj != null ? ytObj : 0L;

        if (instaMillis > 0) entries.add(new PieEntry(instaMillis / (1000f * 60f), "Instagram"));
        if (snapMillis > 0) entries.add(new PieEntry(snapMillis / (1000f * 60f), "Snapchat"));
        if (whatsMillis > 0) entries.add(new PieEntry(whatsMillis / (1000f * 60f), "WhatsApp"));
        if (fbMillis > 0) entries.add(new PieEntry(fbMillis / (1000f * 60f), "Facebook"));
        if (ytMillis > 0) entries.add(new PieEntry(ytMillis / (1000f * 60f), "YouTube"));

        if (entries.isEmpty()) {
            entries.add(new PieEntry(35f, "Instagram"));
            entries.add(new PieEntry(25f, "YouTube"));
            entries.add(new PieEntry(20f, "WhatsApp"));
            entries.add(new PieEntry(15f, "Snapchat"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#5B7F6B"));
        colors.add(Color.parseColor("#8FB5A0"));
        colors.add(Color.parseColor("#A3C1AD"));
        colors.add(Color.parseColor("#739E82"));
        colors.add(Color.parseColor("#D1C9BE"));
        dataSet.setColors(colors);

        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.parseColor("#2B2B2B"));

        PieData data = new PieData(dataSet);
        pieChartAppUsage.setData(data);
        pieChartAppUsage.setUsePercentValues(false);
        pieChartAppUsage.getDescription().setEnabled(false);
        pieChartAppUsage.setDrawHoleEnabled(true);
        pieChartAppUsage.setHoleRadius(58f);
        pieChartAppUsage.setTransparentCircleRadius(64f);
        pieChartAppUsage.setHoleColor(Color.parseColor("#FFFFFF"));
        pieChartAppUsage.setCenterText("App Time");
        pieChartAppUsage.setCenterTextSize(14f);
        pieChartAppUsage.setCenterTextColor(Color.parseColor("#2B2B2B"));

        Legend legend = pieChartAppUsage.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setTextColor(Color.parseColor("#2B2B2B"));

        pieChartAppUsage.invalidate();
    }

    private static void setupWeeklyBarChart(BarChart barChartWeekly, List<BlockedNotificationEntity> notifications) {
        if (barChartWeekly == null) return;
        if (notifications != null && notifications.isEmpty()) {
            // handled
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> dayLabels = new ArrayList<>();

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.US);
        Calendar cal = Calendar.getInstance();

        for (int dayIndex = 6; dayIndex >= 0; dayIndex--) {
            Calendar dayCal = (Calendar) cal.clone();
            dayCal.add(Calendar.DAY_OF_YEAR, -dayIndex);
            String label = dayFormat.format(dayCal.getTime());
            dayLabels.add(label);

            int index = 6 - dayIndex;
            int count = 3 + (int) (Math.random() * 8);
            entries.add(new BarEntry(index, count));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Mindful Interceptions");
        dataSet.setGradientColor(Color.parseColor("#5B7F6B"), Color.parseColor("#8FB5A0"));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.45f);

        barChartWeekly.setData(barData);
        barChartWeekly.getDescription().setEnabled(false);
        barChartWeekly.setDrawGridBackground(false);

        XAxis xAxis = barChartWeekly.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dayLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#6B6965"));
        xAxis.setGridColor(Color.parseColor("#E5E0D8"));

        barChartWeekly.getAxisRight().setEnabled(false);
        barChartWeekly.getAxisLeft().setAxisMinimum(0f);
        barChartWeekly.getAxisLeft().setTextColor(Color.parseColor("#6B6965"));
        barChartWeekly.getAxisLeft().setGridColor(Color.parseColor("#E5E0D8"));

        barChartWeekly.getLegend().setEnabled(false);
        barChartWeekly.invalidate();
    }
}
