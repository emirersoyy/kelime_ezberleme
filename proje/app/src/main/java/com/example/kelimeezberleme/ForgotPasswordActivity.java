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
                String user = etUsername.getText().toString();
                String newPass = etNewPassword.getText().toString();

                if (user.equals("") || newPass.equals("")) {
                    Toast.makeText(ForgotPasswordActivity.this, "Tüm alanları doldurun", Toast.LENGTH_SHORT).show();
                } else {
                    if (db.updatePassword(user, newPass)) {
                        Toast.makeText(ForgotPasswordActivity.this, "Şifre Güncellendi", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Kullanıcı bulunamadı", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}