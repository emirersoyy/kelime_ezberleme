package com.example.kelimeezberleme;

import android.content.Intent;
import android.os.Bundle;
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

        findViewById(R.id.btnStartQuiz).setOnClickListener(v -> startQuiz());

        findViewById(R.id.btnAddWordMenu).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddWordActivity.class)));

        findViewById(R.id.btnWordsListMenu).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, WordsListActivity.class)));

        findViewById(R.id.btnAnalysisMenu).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AnalysisActivity.class)));

        findViewById(R.id.btnWordleMenu).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, WordleActivity.class)));

        findViewById(R.id.btnSettingsMenu).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
    }

    private void startQuiz() {
        Intent intent = new Intent(MainActivity.this, QuizActivity.class);
        intent.putExtra("limit", AppSettings.getQuizLimit(this));
        startActivity(intent);
    }
}
