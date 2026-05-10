package com.example.kelimeezberleme;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class AccountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        TextView tvCurrentUser = findViewById(R.id.tvCurrentUser);
        String currentUser = AppSettings.getCurrentUser(this);
        tvCurrentUser.setText(currentUser == null || currentUser.trim().isEmpty() ? "Bilinmiyor" : currentUser);

        MaterialButton btnSettings = findViewById(R.id.btnAccountSettings);
        MaterialButton btnResetPassword = findViewById(R.id.btnAccountResetPassword);
        MaterialButton btnLogout = findViewById(R.id.btnAccountLogout);

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, SettingsActivity.class)));

        btnResetPassword.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, ForgotPasswordActivity.class)));

        btnLogout.setOnClickListener(v -> {
            AppSettings.clearCurrentUser(this);
            AppSettings.clearRememberedLogin(this);
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

}
