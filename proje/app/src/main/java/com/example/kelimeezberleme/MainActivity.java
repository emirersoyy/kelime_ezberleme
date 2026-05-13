package com.example.kelimeezberleme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class MainActivity extends BottomNavActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseHelper db = new DatabaseHelper(this);
        try {
            db.seedDatabase();
        } catch (RuntimeException e) {
            Log.e(TAG, "Seed database failed", e);
            Toast.makeText(this, "Varsayılan kelimeler hazırlanamadı.", Toast.LENGTH_SHORT).show();
        }

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        TextView tvCurrentUser = findViewById(R.id.tvCurrentUser);
        ImageView ivProfileAvatar = findViewById(R.id.ivProfileAvatar);

        String currentUser = AppSettings.getCurrentUser(this);
        tvGreeting.setText(buildGreeting(currentUser));
        tvCurrentUser.setText(currentUser == null || currentUser.trim().isEmpty() ? "Kullanıcı" : currentUser.trim());
        ivProfileAvatar.setContentDescription("Hesap fotoğrafı");

        findViewById(R.id.btnStartQuiz).setOnClickListener(v -> startQuiz());

        findViewById(R.id.btnAddWordMenu).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddWordActivity.class)));

        findViewById(R.id.btnWordsListMenu).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, WordsListActivity.class)));

        findViewById(R.id.btnAnalysisMenu).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AnalysisActivity.class)));

        findViewById(R.id.btnWordleMenu).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, WordleActivity.class)));

        findViewById(R.id.btnAiMenu).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AiAssistantActivity.class)));
    }

    private void startQuiz() {
        Intent intent = new Intent(MainActivity.this, QuizActivity.class);
        intent.putExtra("limit", AppSettings.getQuizLimit(this));
        startActivity(intent);
    }

    private String buildGreeting(String user) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String prefix;
        if (hour >= 5 && hour < 12) {
            prefix = "Günaydın";
        } else if (hour >= 12 && hour < 18) {
            prefix = "İyi günler";
        } else if (hour >= 18 && hour < 22) {
            prefix = "İyi akşamlar";
        } else {
            prefix = "İyi geceler";
        }

        if (user == null || user.trim().isEmpty()) {
            return prefix;
        }
        return prefix + ", " + user.trim();
    }
}
