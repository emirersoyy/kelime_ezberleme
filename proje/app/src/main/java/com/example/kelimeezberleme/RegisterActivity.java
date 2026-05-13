package com.example.kelimeezberleme;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {
    EditText etUsername, etPassword, etConfirmPassword;
    Button btnRegister;
    TextInputLayout tilUsername, tilPassword, tilConfirmPassword;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);
        tilUsername = findViewById(R.id.tilRegUsername);
        tilPassword = findViewById(R.id.tilRegPassword);
        tilConfirmPassword = findViewById(R.id.tilRegConfirmPassword);
        etUsername = findViewById(R.id.etRegUsername);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        etConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptRegister();
                return true;
            }
            return false;
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });
    }

    private void attemptRegister() {
        tilUsername.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        String user = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString();
        String confirmPass = etConfirmPassword.getText().toString();

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

        if (confirmPass.isEmpty()) {
            tilConfirmPassword.setError("Şifrenizi tekrar girin");
            etConfirmPassword.requestFocus();
            return;
        }

        String usernameError = AccountSecurity.validateUsername(user);
        if (usernameError != null) {
            tilUsername.setError(usernameError);
            etUsername.requestFocus();
            return;
        }

        String passwordError = AccountSecurity.validatePassword(user, pass);
        if (passwordError != null) {
            tilPassword.setError(passwordError);
            etPassword.requestFocus();
            return;
        }

        if (!pass.equals(confirmPass)) {
            tilConfirmPassword.setError("Şifreler uyuşmuyor");
            etConfirmPassword.requestFocus();
            return;
        }

        if (db.isUsernameTaken(user)) {
            tilUsername.setError("Bu kullanıcı adı zaten kullanılıyor");
            etUsername.requestFocus();
            return;
        }

        if (db.addUser(user, pass)) {
            Toast.makeText(RegisterActivity.this, "Kayıt başarılı", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(RegisterActivity.this, "Kayıt başarısız", Toast.LENGTH_SHORT).show();
        }
    }
}
