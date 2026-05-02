package com.example.kelimeezberleme;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppSettings {
    public static final String PREFS_NAME = "AppSettings";
    public static final String KEY_CURRENT_USER = "current_user";
    public static final String KEY_THEME_MODE = "theme_mode";
    public static final String KEY_QUIZ_LIMIT = "quiz_limit";
    public static final int MIN_QUIZ_LIMIT = 5;
    public static final int MAX_QUIZ_LIMIT = 15;
    public static final int DEFAULT_QUIZ_LIMIT = 10;

    private AppSettings() {
    }

    public static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static String getCurrentUser(Context context) {
        return prefs(context).getString(KEY_CURRENT_USER, "");
    }

    public static void setCurrentUser(Context context, String username) {
        prefs(context).edit().putString(KEY_CURRENT_USER, username).apply();
    }

    public static void clearCurrentUser(Context context) {
        prefs(context).edit().remove(KEY_CURRENT_USER).apply();
    }

    public static int getQuizLimit(Context context) {
        return clampQuizLimit(prefs(context).getInt(getQuizLimitKey(context), DEFAULT_QUIZ_LIMIT));
    }

    public static void setQuizLimit(Context context, int limit) {
        prefs(context).edit().putInt(getQuizLimitKey(context), clampQuizLimit(limit)).apply();
    }

    public static int clampQuizLimit(int limit) {
        return Math.max(MIN_QUIZ_LIMIT, Math.min(MAX_QUIZ_LIMIT, limit));
    }

    private static String getQuizLimitKey(Context context) {
        String currentUser = getCurrentUser(context);
        if (currentUser == null || currentUser.trim().isEmpty()) {
            return KEY_QUIZ_LIMIT;
        }
        return KEY_QUIZ_LIMIT + "_" + currentUser.trim().toLowerCase();
    }
}
