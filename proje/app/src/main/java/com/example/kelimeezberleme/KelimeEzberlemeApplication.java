package com.example.kelimeezberleme;

import android.app.Application;

public class KelimeEzberlemeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applySavedTheme(this);
    }
}
