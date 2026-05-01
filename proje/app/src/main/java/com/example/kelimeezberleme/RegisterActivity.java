package com.example.kelimeezberleme;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    EditText etUsername, etPassword, etConfirmPassword;
    Button btnRegister;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);
        etUsername = findViewById(R.id.etRegUsername);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etUsername.getText().toString().trim();
                String pass = etPassword.getText().toString();
                String confirmPass = etConfirmPassword.getText().toString();
                String usernameError = AccountSecurity.validateUsername(user);
                String passwordError = AccountSecurity.validatePassword(user, pass);

                if (user.equals("") || pass.equals("") || confirmPass.equals("")) {
                    Toast.makeText(RegisterActivity.this, "T\u00fcm alanlar\u0131 doldurun", Toast.LENGTH_SHORT).show();
                } else if (usernameError != null) {
                    Toast.makeText(RegisterActivity.this, usernameError, Toast.LENGTH_LONG).show();
                } else if (passwordError != null) {
                    Toast.makeText(RegisterActivity.this, passwordError, Toast.LENGTH_LONG).show();
                } else if (db.isUsernameTaken(user)) {
                    Toast.makeText(RegisterActivity.this, "Bu kullan\u0131c\u0131 ad\u0131 zaten kullan\u0131l\u0131yor", Toast.LENGTH_SHORT).show();
                } else {
                    if (pass.equals(confirmPass)) {
                        if (db.addUser(user, pass)) {
                            Toast.makeText(RegisterActivity.this, "Kay\u0131t Ba\u015far\u0131l\u0131", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Kay\u0131t Ba\u015far\u0131s\u0131z", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "\u015eifreler uyu\u015fmuyor", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
