package com.example.unscroll;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unscroll.db.BlockedNotificationEntity;

import java.util.List;

/**
 * RecyclerView Adapter for displaying blocked/snoozed notifications grouped by app.
 */
public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private final List<BlockedNotificationEntity> notificationList;

    public NotificationAdapter(List<BlockedNotificationEntity> notificationList) {
        this.notificationList = notificationList;
        // Sort notifications by appName/packageName so items from the same app are grouped together
        this.notificationList.sort((a, b) -> {
            String appA = a.getAppName() != null ? a.getAppName() : a.getPackageName();
            String appB = b.getAppName() != null ? b.getAppName() : b.getPackageName();
            int cmp = appA.compareToIgnoreCase(appB);
            if (cmp != 0) return cmp;
            return Long.compare(b.getTimestamp(), a.getTimestamp());
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (isFirstItemForApp(position)) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_ITEM;
    }

    private boolean isFirstItemForApp(int position) {
        if (position == 0) return true;
        BlockedNotificationEntity curr = notificationList.get(position);
        BlockedNotificationEntity prev = notificationList.get(position - 1);
        String currApp = curr.getAppName() != null ? curr.getAppName() : curr.getPackageName();
        String prevApp = prev.getAppName() != null ? prev.getAppName() : prev.getPackageName();
        return !currApp.equalsIgnoreCase(prevApp);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_category_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new NotificationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BlockedNotificationEntity entity = notificationList.get(position);
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            String appName = entity.getAppName() != null ? entity.getAppName() : entity.getPackageName();
            headerHolder.tvCategoryTitle.setText(appName);
        } else if (holder instanceof NotificationViewHolder) {
            NotificationViewHolder notifHolder = (NotificationViewHolder) holder;
            notifHolder.tvAppName.setText(entity.getAppName() != null ? entity.getAppName() : entity.getPackageName());
            notifHolder.tvTitle.setText(entity.getTitle() != null ? entity.getTitle() : "Notification");
            notifHolder.tvText.setText(entity.getText() != null ? entity.getText() : "");

            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                    entity.getTimestamp(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            );
            notifHolder.tvTime.setText(timeAgo);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryTitle;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryTitle = itemView.findViewById(R.id.tvCategoryTitle);
        }
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvAppName;
        TextView tvTitle;
        TextView tvText;
        TextView tvTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvText = itemView.findViewById(R.id.tvText);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
