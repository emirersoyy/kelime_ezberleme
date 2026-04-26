package com.example.kelimeezberleme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {
    EditText etLimit;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        etLimit = findViewById(R.id.etSettingsQuestionLimit);

        // Mevcut ayarı yükle (Varsayılan 10)
        int currentLimit = sharedPref.getInt("quiz_limit", 10);
        etLimit.setText(String.valueOf(currentLimit));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnLightTheme).setOnClickListener(v -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO));
        findViewById(R.id.btnDarkTheme).setOnClickListener(v -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES));

        findViewById(R.id.btnSaveSettings).setOnClickListener(v -> {
            String val = etLimit.getText().toString();
            if (!val.isEmpty()) {
                int newLimit = Integer.parseInt(val);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("quiz_limit", newLimit);
                editor.apply();
                Toast.makeText(SettingsActivity.this, "Ayarlar kaydedildi", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}