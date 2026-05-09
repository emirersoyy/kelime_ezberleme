package com.example.kelimeezberleme;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class AppSettings {
    public static final String PREFS_NAME = "AppSettings";
    public static final String KEY_CURRENT_USER = "current_user";
    public static final String KEY_REMEMBER_LOGIN = "remember_login";
    public static final String KEY_REMEMBERED_USER = "remembered_user";
    public static final String KEY_THEME_MODE = "theme_mode";
    public static final String KEY_WORDS_SORT = "words_sort";
    public static final String KEY_QUIZ_LIMIT = "quiz_limit";
    public static final String KEY_CORRECT_WORD_IDS = "correct_word_ids";
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

    public static void setRememberedLogin(Context context, String username, boolean remember) {
        SharedPreferences.Editor editor = prefs(context).edit();
        if (remember && username != null && !username.trim().isEmpty()) {
            editor.putBoolean(KEY_REMEMBER_LOGIN, true);
            editor.putString(KEY_REMEMBERED_USER, username.trim());
        } else {
            editor.remove(KEY_REMEMBER_LOGIN);
            editor.remove(KEY_REMEMBERED_USER);
        }
        editor.apply();
    }

    public static boolean isRememberedLoginEnabled(Context context) {
        return prefs(context).getBoolean(KEY_REMEMBER_LOGIN, false);
    }

    public static String getRememberedUser(Context context) {
        return prefs(context).getString(KEY_REMEMBERED_USER, "");
    }

    public static void clearRememberedLogin(Context context) {
        prefs(context).edit()
                .remove(KEY_REMEMBER_LOGIN)
                .remove(KEY_REMEMBERED_USER)
                .apply();
    }

    public static String getWordsSortOrder(Context context) {
        return prefs(context).getString(getWordsSortKey(context), "alpha_asc");
    }

    public static void setWordsSortOrder(Context context, String sortOrder) {
        prefs(context).edit().putString(getWordsSortKey(context), sortOrder == null ? "alpha_asc" : sortOrder).apply();
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
        return KEY_QUIZ_LIMIT + "_" + normalizeUserKey(currentUser);
    }

    public static void recordCorrectWord(Context context, int wordId) {
        if (wordId <= 0) return;

        Set<String> ids = new HashSet<>(prefs(context).getStringSet(getCorrectWordIdsKey(context), new HashSet<>()));
        ids.add(String.valueOf(wordId));
        prefs(context).edit().putStringSet(getCorrectWordIdsKey(context), ids).apply();
    }

    public static Set<String> getCorrectWordIds(Context context) {
        return new HashSet<>(prefs(context).getStringSet(getCorrectWordIdsKey(context), new HashSet<>()));
    }

    public static String getCurrentUserKey(Context context) {
        String currentUser = getCurrentUser(context);
        if (currentUser == null || currentUser.trim().isEmpty()) {
            return "guest";
        }
        return normalizeUserKey(currentUser);
    }

    private static String getCorrectWordIdsKey(Context context) {
        return KEY_CORRECT_WORD_IDS + "_" + getCurrentUserKey(context);
    }

    private static String getWordsSortKey(Context context) {
        return KEY_WORDS_SORT + "_" + getCurrentUserKey(context);
    }

    private static String normalizeUserKey(String username) {
        return username.trim().toLowerCase(Locale.US);
    }
}
