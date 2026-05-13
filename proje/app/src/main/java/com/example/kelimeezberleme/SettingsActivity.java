package com.example.kelimeezberleme;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.slider.Slider;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class SettingsActivity extends BottomNavActivity {
    Slider sliderQuestionLimit;
    TextView tvQuestionLimitValue;
    MaterialButtonToggleGroup toggleTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sliderQuestionLimit = findViewById(R.id.sliderQuestionLimit);
        tvQuestionLimitValue = findViewById(R.id.tvQuestionLimitValue);
        toggleTheme = findViewById(R.id.toggleTheme);

        int currentLimit = AppSettings.getQuizLimit(this);
        sliderQuestionLimit.setValue(currentLimit);
        updateQuestionLimitText(currentLimit);

        sliderQuestionLimit.addOnChangeListener((slider, value, fromUser) -> updateQuestionLimitText(Math.round(value)));

        setupThemeSelection();

        findViewById(R.id.btnSaveSettings).setOnClickListener(v -> {
            int newLimit = AppSettings.clampQuizLimit(Math.round(sliderQuestionLimit.getValue()));
            AppSettings.setQuizLimit(this, newLimit);
            updateQuestionLimitText(newLimit);
            Toast.makeText(SettingsActivity.this, "Ayarlar kaydedildi", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateQuestionLimitText(int limit) {
        tvQuestionLimitValue.setText(String.valueOf(AppSettings.clampQuizLimit(limit)));
    }

    private void setupThemeSelection() {
        String savedTheme = ThemeManager.getSavedTheme(this);
        toggleTheme.check(ThemeManager.THEME_DARK.equals(savedTheme)
                ? R.id.btnDarkTheme
                : R.id.btnLightTheme);

        toggleTheme.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnDarkTheme) {
                ThemeManager.saveAndApplyTheme(SettingsActivity.this, ThemeManager.THEME_DARK);
            } else if (checkedId == R.id.btnLightTheme) {
                ThemeManager.saveAndApplyTheme(SettingsActivity.this, ThemeManager.THEME_LIGHT);
            }
        });
    }
}
