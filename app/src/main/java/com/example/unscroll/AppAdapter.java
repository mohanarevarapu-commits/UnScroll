package com.example.unscroll;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unscroll.db.AppDatabase;
import com.example.unscroll.db.AppEntity;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * RecyclerView adapter for rendering categorized installed apps with soft toggle switches and search filtering.
 */
public class AppAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public static class AppItem {
        private final String packageName;
        private final String appName;
        private final Drawable icon;
        private boolean isBlocked;
        private final String category;

        public AppItem(String packageName, String appName, Drawable icon, boolean isBlocked, String category) {
            this.packageName = packageName;
            this.appName = appName;
            this.icon = icon;
            this.isBlocked = isBlocked;
            this.category = category;
        }

        public String getPackageName() { return packageName; }
        public String getAppName() { return appName; }
        public Drawable getIcon() { return icon; }
        public boolean isBlocked() { return isBlocked; }
        public void setBlocked(boolean blocked) { isBlocked = blocked; }
        public String getCategory() { return category; }
    }

    public static class ListItem {
        private int type;
        private String headerTitle;
        private AppItem appItem;

        public static ListItem createHeader(String title) {
            ListItem item = new ListItem();
            item.type = TYPE_HEADER;
            item.headerTitle = title;
            return item;
        }

        public static ListItem createApp(AppItem app) {
            ListItem item = new ListItem();
            item.type = TYPE_ITEM;
            item.appItem = app;
            return item;
        }

        public int getType() { return type; }
        public String getHeaderTitle() { return headerTitle; }
        public AppItem getAppItem() { return appItem; }
    }

    private final List<AppItem> allApps;
    private final List<ListItem> displayList;

    public AppAdapter(List<AppItem> allApps) {
        this.allApps = allApps;
        this.displayList = new ArrayList<>();
        filterAndGroup("");
    }

    public void filterAndGroup(String query) {
        displayList.clear();
        String q = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";

        Map<String, List<AppItem>> categoryMap = new LinkedHashMap<>();
        categoryMap.put("Social", new ArrayList<>());
        categoryMap.put("Video", new ArrayList<>());
        categoryMap.put("Shopping", new ArrayList<>());
        categoryMap.put("Other", new ArrayList<>());

        for (AppItem item : allApps) {
            if (!q.isEmpty()) {
                boolean matchName = item.getAppName().toLowerCase(Locale.ROOT).contains(q);
                boolean matchPkg = item.getPackageName().toLowerCase(Locale.ROOT).contains(q);
                if (!matchName && !matchPkg) {
                    continue;
                }
            }
            String cat = item.getCategory();
            if (!categoryMap.containsKey(cat)) {
                cat = "Other";
            }
            categoryMap.get(cat).add(item);
        }

        for (Map.Entry<String, List<AppItem>> entry : categoryMap.entrySet()) {
            List<AppItem> list = entry.getValue();
            if (!list.isEmpty()) {
                Collections.sort(list, (a, b) -> a.getAppName().compareToIgnoreCase(b.getAppName()));
                displayList.add(ListItem.createHeader(entry.getKey()));
                for (AppItem appItem : list) {
                    displayList.add(ListItem.createApp(appItem));
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return displayList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_category_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
            return new AppViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItem listItem = displayList.get(position);

        if (listItem.getType() == TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvCategoryTitle.setText(listItem.getHeaderTitle());
        } else {
            AppViewHolder appHolder = (AppViewHolder) holder;
            AppItem item = listItem.getAppItem();

            appHolder.tvAppName.setText(item.getAppName());
            appHolder.tvPackageName.setText(item.getPackageName());
            if (item.getIcon() != null) {
                appHolder.ivAppIcon.setImageDrawable(item.getIcon());
            } else {
                appHolder.ivAppIcon.setImageResource(R.drawable.placeholder);
            }
            appHolder.switchBlockApp.setChecked(item.isBlocked());

            View.OnClickListener toggleListener = v -> {
                boolean newState = !item.isBlocked();
                item.setBlocked(newState);
                appHolder.switchBlockApp.setChecked(newState);

                // Persist in Room Database on background thread
                AppDatabase db = AppDatabase.getInstance(v.getContext().getApplicationContext());
                android.os.AsyncTask.execute(() -> {
                    db.appDao().insertOrUpdate(new AppEntity(item.getPackageName(), item.getAppName(), newState));
                });
            };

            appHolder.itemView.setOnClickListener(toggleListener);
            appHolder.switchBlockApp.setOnClickListener(toggleListener);
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryTitle;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryTitle = itemView.findViewById(R.id.tvCategoryTitle);
        }
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAppIcon;
        TextView tvAppName;
        TextView tvPackageName;
        MaterialSwitch switchBlockApp;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            tvPackageName = itemView.findViewById(R.id.tvPackageName);
            switchBlockApp = itemView.findViewById(R.id.switchBlockApp);
        }
    }
}
