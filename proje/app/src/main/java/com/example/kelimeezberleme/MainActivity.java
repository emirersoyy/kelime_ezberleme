package com.example.kelimeezberleme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        DatabaseHelper db = new DatabaseHelper(this);
        db.seedDatabase();

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Hoş Geldiniz!");

        findViewById(R.id.btnStartQuiz).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ayarlar'dan soru limitini al
                SharedPreferences sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
                int limit = sharedPref.getInt("quiz_limit", 10);
                
                Intent intent = new Intent(MainActivity.this, QuizActivity.class);
                intent.putExtra("limit", limit);
                startActivity(intent);
            }
        });

        findViewById(R.id.btnAddWordMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddWordActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btnWordsListMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WordsListActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btnSettingsMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }
}