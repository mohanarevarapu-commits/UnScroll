package com.example.unscroll;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unscroll.usage.UsageStatsHelper;

import java.util.List;

/**
 * RecyclerView adapter for rendering detailed app usage statistics, launch counts,
 * time spent, distraction impact levels, and blocked notification counts.
 */
public class AppUsageDetailAdapter extends RecyclerView.Adapter<AppUsageDetailAdapter.ViewHolder> {

    public static class AppUsageDetailItem {
        private final String packageName;
        private final String appName;
        private final Drawable icon;
        private final long timeSpentMillis;
        private final int openCount;
        private final int blockedNotificationsCount;
        private final String category;
        private final String impactLevel; // "High", "Medium", "Low"

        public AppUsageDetailItem(String packageName, String appName, Drawable icon, long timeSpentMillis, int openCount, int blockedNotificationsCount, String category, String impactLevel) {
            this.packageName = packageName;
            this.appName = appName;
            this.icon = icon;
            this.timeSpentMillis = timeSpentMillis;
            this.openCount = openCount;
            this.blockedNotificationsCount = blockedNotificationsCount;
            this.category = category;
            this.impactLevel = impactLevel;
        }

        public String getPackageName() { return packageName; }
        public String getAppName() { return appName; }
        public Drawable getIcon() { return icon; }
        public long getTimeSpentMillis() { return timeSpentMillis; }
        public int getOpenCount() { return openCount; }
        public int getBlockedNotificationsCount() { return blockedNotificationsCount; }
        public String getCategory() { return category; }
        public String getImpactLevel() { return impactLevel; }
    }

    private final List<AppUsageDetailItem> items;

    public AppUsageDetailAdapter(List<AppUsageDetailItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_usage_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppUsageDetailItem item = items.get(position);
        holder.tvAppName.setText(item.getAppName());
        holder.tvPackageName.setText(item.getPackageName());

        if (item.getIcon() != null) {
            holder.ivAppIcon.setImageDrawable(item.getIcon());
        } else {
            holder.ivAppIcon.setImageResource(R.drawable.placeholder);
        }

        holder.tvOpenCount.setText("🔄 " + item.getOpenCount() + " opens");
        holder.tvTimeSpent.setText("⏳ " + UsageStatsHelper.formatDuration(item.getTimeSpentMillis()));
        holder.tvBlockedNotifs.setText("🔔 " + item.getBlockedNotificationsCount() + " blocked");

        holder.tvImpactBadge.setText(item.getImpactLevel() + " Impact");

        // Set badge colors based on impact level
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(16f);
        if ("High".equalsIgnoreCase(item.getImpactLevel())) {
            badgeBg.setColor(Color.parseColor("#7F1D1D")); // dark red bg
            holder.tvImpactBadge.setTextColor(Color.parseColor("#FCA5A5")); // light red text
        } else if ("Medium".equalsIgnoreCase(item.getImpactLevel())) {
            badgeBg.setColor(Color.parseColor("#78350F")); // dark orange bg
            holder.tvImpactBadge.setTextColor(Color.parseColor("#FCD34D")); // light orange text
        } else {
            badgeBg.setColor(Color.parseColor("#064E3B")); // dark green bg
            holder.tvImpactBadge.setTextColor(Color.parseColor("#6EE7B7")); // light green text
        }
        holder.tvImpactBadge.setBackground(badgeBg);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAppIcon;
        TextView tvAppName;
        TextView tvPackageName;
        TextView tvImpactBadge;
        TextView tvOpenCount;
        TextView tvTimeSpent;
        TextView tvBlockedNotifs;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            tvPackageName = itemView.findViewById(R.id.tvPackageName);
            tvImpactBadge = itemView.findViewById(R.id.tvImpactBadge);
            tvOpenCount = itemView.findViewById(R.id.tvOpenCount);
            tvTimeSpent = itemView.findViewById(R.id.tvTimeSpent);
            tvBlockedNotifs = itemView.findViewById(R.id.tvBlockedNotifs);
        }
    }
}
