package com.example.unscroll;

/**
 * Intelligence helper for classifying applications and incoming notifications into
 * E-commerce & Marketing, Social Media / Distracting, and Important categories.
 */
public class AppIntelligence {

    public static final String CATEGORY_MARKETING = "MARKETING";
    public static final String CATEGORY_SOCIAL = "SOCIAL";
    public static final String CATEGORY_IMPORTANT = "IMPORTANT";

    // E-commerce & Marketing / Over-spending prevention target apps
    public static final String[] ECOMMERCE_PACKAGES = {
            "com.flipkart.android",          // Flipkart
            "com.myntra.android",           // Myntra
            "in.amazon.mShop.android.shopping", // Amazon
            "in.swiggy.android",            // Swiggy
            "com.application.zomato",       // Zomato
            "com.zeptoconsumerapp",         // Zepto
            "com.grofers.customerapp"       // Blinkit
    };

    // Social Media / Distracting apps
    public static final String[] SOCIAL_PACKAGES = {
            "com.instagram.android",        // Instagram
            "com.snapchat.android",         // Snapchat
            "com.whatsapp",                 // WhatsApp
            "com.facebook.katana",          // Facebook
            "com.google.android.youtube"    // YouTube
    };

    /**
     * Classifies a given package name into MARKETING, SOCIAL, or IMPORTANT.
     */
    public static String classifyPackage(String packageName) {
        if (packageName == null) {
            return CATEGORY_IMPORTANT;
        }

        for (String pkg : ECOMMERCE_PACKAGES) {
            if (pkg.equalsIgnoreCase(packageName)) {
                return CATEGORY_MARKETING;
            }
        }

        for (String pkg : SOCIAL_PACKAGES) {
            if (pkg.equalsIgnoreCase(packageName)) {
                return CATEGORY_SOCIAL;
            }
        }

        return CATEGORY_IMPORTANT;
    }

    public static boolean isECommerceOrMarketing(String packageName) {
        return CATEGORY_MARKETING.equals(classifyPackage(packageName));
    }

    public static boolean isSocial(String packageName) {
        return CATEGORY_SOCIAL.equals(classifyPackage(packageName));
    }
}
