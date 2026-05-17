package com.example.kelimeezberleme;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Paint;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    EditText etUsername, etPassword;
    Button btnLogin;
    CheckBox cbRememberMe;
    TextView tvRegister, tvForgotPassword;
    TextInputLayout tilUsername, tilPassword;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        if (tryAutoLogin()) {
            return;
        }

        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        tvRegister.setPaintFlags(tvRegister.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvForgotPassword.setPaintFlags(tvForgotPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        cbRememberMe.setChecked(AppSettings.isRememberedLoginEnabled(this));

        String rememberedUser = AppSettings.getRememberedUser(this);
        if (!rememberedUser.isEmpty()) {
            etUsername.setText(rememberedUser);
            etUsername.setSelection(rememberedUser.length());
        }

        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
    }

    private boolean tryAutoLogin() {
        if (!AppSettings.isRememberedLoginEnabled(this)) {
            return false;
        }

        String rememberedUser = AppSettings.getRememberedUser(this);
        if (rememberedUser == null || rememberedUser.trim().isEmpty()) {
            AppSettings.clearRememberedLogin(this);
            return false;
        }

        db.ensureUserCreatedAt(rememberedUser);
        AppSettings.setCurrentUser(this, rememberedUser);
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
        return true;
    }

    private void attemptLogin() {
        tilUsername.setError(null);
        tilPassword.setError(null);

        String user = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString();

        if (user.isEmpty()) {
            tilUsername.setError("Kullanıcı adını girin");
            etUsername.requestFocus();
            return;
        }

        if (pass.isEmpty()) {
            tilPassword.setError("Şifrenizi girin");
            etPassword.requestFocus();
            return;
        }

        if (db.checkUser(user, pass)) {
            db.ensureUserCreatedAt(user);
            AppSettings.setCurrentUser(this, user);
            AppSettings.setRememberedLogin(this, user, cbRememberMe.isChecked());
            Toast.makeText(LoginActivity.this, "Giriş başarılı", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            tilPassword.setError("Kullanıcı adı veya şifre hatalı");
            etPassword.requestFocus();
        }
    }
}
