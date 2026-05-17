package com.example.kelimeezberleme;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    private ThemeManager() {
    }

    public static void applySavedTheme(Context context) {
        SharedPreferences prefs = AppSettings.prefs(context);
        applyTheme(prefs.getString(AppSettings.KEY_THEME_MODE, THEME_LIGHT));
    }

    public static void saveAndApplyTheme(Context context, String themeMode) {
        String safeThemeMode = THEME_DARK.equals(themeMode) ? THEME_DARK : THEME_LIGHT;
        AppSettings.prefs(context)
                .edit()
                .putString(AppSettings.KEY_THEME_MODE, safeThemeMode)
                .apply();
        applyTheme(safeThemeMode);
    }

    public static String getSavedTheme(Context context) {
        return AppSettings.prefs(context).getString(AppSettings.KEY_THEME_MODE, THEME_LIGHT);
    }

    private static void applyTheme(String themeMode) {
        int nightMode = THEME_DARK.equals(themeMode)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
}
