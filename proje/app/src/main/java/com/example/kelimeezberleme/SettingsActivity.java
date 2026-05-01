package com.example.kelimeezberleme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.slider.Slider;

public class SettingsActivity extends AppCompatActivity {
    private static final int MIN_QUESTION_LIMIT = 5;
    private static final int MAX_QUESTION_LIMIT = 15;
    private static final int DEFAULT_QUESTION_LIMIT = 10;

    Slider sliderQuestionLimit;
    TextView tvQuestionLimitValue;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        sliderQuestionLimit = findViewById(R.id.sliderQuestionLimit);
        tvQuestionLimitValue = findViewById(R.id.tvQuestionLimitValue);

        int currentLimit = clampQuestionLimit(sharedPref.getInt(getQuizLimitKey(), DEFAULT_QUESTION_LIMIT));
        sliderQuestionLimit.setValue(currentLimit);
        updateQuestionLimitText(currentLimit);

        sliderQuestionLimit.addOnChangeListener((slider, value, fromUser) -> updateQuestionLimitText(Math.round(value)));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnLightTheme).setOnClickListener(v -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO));
        findViewById(R.id.btnDarkTheme).setOnClickListener(v -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES));

        findViewById(R.id.btnSaveSettings).setOnClickListener(v -> {
            int newLimit = clampQuestionLimit(Math.round(sliderQuestionLimit.getValue()));
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getQuizLimitKey(), newLimit);
            editor.apply();
            updateQuestionLimitText(newLimit);
            Toast.makeText(SettingsActivity.this, "Ayarlar kaydedildi", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            sharedPref.edit().remove("current_user").apply();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void updateQuestionLimitText(int limit) {
        tvQuestionLimitValue.setText(String.valueOf(clampQuestionLimit(limit)));
    }

    private int clampQuestionLimit(int limit) {
        return Math.max(MIN_QUESTION_LIMIT, Math.min(MAX_QUESTION_LIMIT, limit));
    }

    private String getQuizLimitKey() {
        String currentUser = sharedPref.getString("current_user", "");
        if (currentUser == null || currentUser.trim().isEmpty()) {
            return "quiz_limit";
        }
        return "quiz_limit_" + currentUser.trim().toLowerCase();
    }
}
