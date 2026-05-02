package com.example.kelimeezberleme;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {
    public static final String PREFS_NAME = "AppSettings";
    public static final String KEY_THEME_MODE = "theme_mode";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    private ThemeManager() {
    }

    public static void applySavedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        applyTheme(prefs.getString(KEY_THEME_MODE, THEME_LIGHT));
    }

    public static void saveAndApplyTheme(Context context, String themeMode) {
        String safeThemeMode = THEME_DARK.equals(themeMode) ? THEME_DARK : THEME_LIGHT;
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_THEME_MODE, safeThemeMode)
                .apply();
        applyTheme(safeThemeMode);
    }

    public static String getSavedTheme(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_THEME_MODE, THEME_LIGHT);
    }

    private static void applyTheme(String themeMode) {
        int nightMode = THEME_DARK.equals(themeMode)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
}
