package com.example.unscroll;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

/**
 * Onboarding Activity featuring 3 calm screens with warm off-white background,
 * deep sage primary, charcoal text, generous padding, and zero shaming language.
 */
public class OnboardingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "unscroll_prefs";
    private static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete";

    private ViewPager2 viewPagerOnboarding;
    private MaterialButton btnNext;
    private TextView btnSkip;
    private View dot1, dot2, dot3;

    private final OnboardingItem[] items = new OnboardingItem[] {
        new OnboardingItem(
            "🌿",
            "Notice where your time goes",
            "Endless feeds and reactive notifications can drain our energy before we realize it. It's not about lack of willpower—our phones are simply designed to capture attention."
        ),
        new OnboardingItem(
            "🧘",
            "A gentle pause for peace of mind",
            "UnScroll introduces brief, mindful breaths when you open distracting apps. Enjoy calming focus sessions and quiet spaces to reconnect with what truly matters to you."
        ),
        new OnboardingItem(
            "🕊️",
            "Set up at your own pace",
            "To protect your time and guide your focus, UnScroll needs a few simple permissions. We explain every step clearly, with zero pressure or rush."
        )
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPagerOnboarding = findViewById(R.id.viewPagerOnboarding);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);

        OnboardingAdapter adapter = new OnboardingAdapter(items);
        viewPagerOnboarding.setAdapter(adapter);

        viewPagerOnboarding.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDots(position);
                if (position == items.length - 1) {
                    btnNext.setText("Get Started");
                } else {
                    btnNext.setText("Next");
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPagerOnboarding.getCurrentItem();
            if (current < items.length - 1) {
                viewPagerOnboarding.setCurrentItem(current + 1);
            } else {
                finishOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void updateDots(int position) {
        int activeColor = getResources().getColor(R.color.primary, getTheme());
        int inactiveColor = getResources().getColor(R.color.card_border, getTheme());

        dot1.setBackgroundColor(position == 0 ? activeColor : inactiveColor);
        dot2.setBackgroundColor(position == 1 ? activeColor : inactiveColor);
        dot3.setBackgroundColor(position == 2 ? activeColor : inactiveColor);
    }

    private void finishOnboarding() {
        Intent intent = new Intent(OnboardingActivity.this, PermissionsSetupActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Whether the user has gone through onboarding and tapped Continue on the permission setup screen.
     */
    public static boolean isComplete(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_ONBOARDING_COMPLETE, false);
    }

    public static void markComplete(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ONBOARDING_COMPLETE, true)
                .apply();
    }

    private static class OnboardingItem {
        final String emoji;
        final String title;
        final String description;

        OnboardingItem(String emoji, String title, String description) {
            this.emoji = emoji;
            this.title = title;
            this.description = description;
        }
    }

    private static class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {
        private final OnboardingItem[] data;

        OnboardingAdapter(OnboardingItem[] data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding_page, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OnboardingItem item = data[position];
            holder.tvEmoji.setText(item.emoji);
            holder.tvTitle.setText(item.title);
            holder.tvDescription.setText(item.description);
        }

        @Override
        public int getItemCount() {
            return data.length;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvTitle, tvDescription;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tvEmoji);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDescription = itemView.findViewById(R.id.tvDescription);
            }
        }
    }
}
