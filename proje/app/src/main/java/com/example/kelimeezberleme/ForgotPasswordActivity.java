package com.example.kelimeezberleme;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {
    EditText etUsername, etNewPassword;
    Button btnUpdate;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        db = new DatabaseHelper(this);
        etUsername = findViewById(R.id.etForgotUsername);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnUpdate = findViewById(R.id.btnUpdatePassword);

        findViewById(R.id.btnForgotBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etUsername.getText().toString().trim();
                String newPass = etNewPassword.getText().toString();
                String passwordError = AccountSecurity.validatePassword(user, newPass);

                if (user.equals("") || newPass.equals("")) {
                    Toast.makeText(ForgotPasswordActivity.this, "T\u00fcm alanlar\u0131 doldurun", Toast.LENGTH_SHORT).show();
                } else if (passwordError != null) {
                    Toast.makeText(ForgotPasswordActivity.this, passwordError, Toast.LENGTH_LONG).show();
                } else {
                    if (db.updatePassword(user, newPass)) {
                        Toast.makeText(ForgotPasswordActivity.this, "\u015eifre G\u00fcncellendi", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Kullan\u0131c\u0131 bulunamad\u0131", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
