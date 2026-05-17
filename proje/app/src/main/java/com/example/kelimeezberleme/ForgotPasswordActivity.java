package com.example.kelimeezberleme;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {
    EditText etUsername, etNewPassword;
    Button btnUpdate;
    TextInputLayout tilUsername, tilNewPassword;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        db = new DatabaseHelper(this);
        tilUsername = findViewById(R.id.tilForgotUsername);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        etUsername = findViewById(R.id.etForgotUsername);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnUpdate = findViewById(R.id.btnUpdatePassword);

        etNewPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updatePassword();
                return true;
            }
            return false;
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword();
            }
        });
    }

    private void updatePassword() {
        tilUsername.setError(null);
        tilNewPassword.setError(null);

        String user = etUsername.getText().toString().trim();
        String newPass = etNewPassword.getText().toString();

        if (user.isEmpty()) {
            tilUsername.setError("Kullanıcı adını girin");
            etUsername.requestFocus();
            return;
        }

        if (newPass.isEmpty()) {
            tilNewPassword.setError("Yeni şifrenizi girin");
            etNewPassword.requestFocus();
            return;
        }

        String passwordError = AccountSecurity.validatePassword(user, newPass);
        if (passwordError != null) {
            tilNewPassword.setError(passwordError);
            etNewPassword.requestFocus();
            return;
        }

        if (db.updatePassword(user, newPass)) {
            Toast.makeText(ForgotPasswordActivity.this, "Şifre güncellendi", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            tilUsername.setError("Bu kullanıcı adı bulunamadı");
            etUsername.requestFocus();
        }
    }
}
