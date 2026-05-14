package com.example.kelimeezberleme;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends BottomNavActivity {
    private static final String TAG = "MainActivity";
    private DatabaseHelper db;
    private TextView tvGreeting;
    private TextView tvCurrentUser;
    private ImageView ivProfileAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        try {
            db.seedDatabase();
        } catch (RuntimeException e) {
            Log.e(TAG, "Seed database failed", e);
            Toast.makeText(this, "Varsayılan kelimeler hazırlanamadı.", Toast.LENGTH_SHORT).show();
        }

        tvGreeting = findViewById(R.id.tvGreeting);
        tvCurrentUser = findViewById(R.id.tvCurrentUser);
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        ivProfileAvatar.setContentDescription("Hesap fotoğrafı");
        refreshHeader();

        findViewById(R.id.btnStartQuiz).setOnClickListener(v -> startQuiz());

        findViewById(R.id.btnWordsListMenu).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, WordsListActivity.class)));

        findViewById(R.id.btnAnalysisMenu).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AnalysisActivity.class)));

        findViewById(R.id.btnWordleMenu).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, WordleActivity.class)));

        findViewById(R.id.btnAiMenu).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AiAssistantActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHeader();
    }

    private void startQuiz() {
        Intent intent = new Intent(MainActivity.this, QuizActivity.class);
        intent.putExtra("limit", AppSettings.getQuizLimit(this));
        startActivity(intent);
    }

    private String buildGreeting(String displayName) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String prefix;
        if (hour >= 5 && hour < 12) {
            prefix = "Günaydın";
        } else if (hour >= 12 && hour < 17) {
            prefix = "İyi günler";
        } else if (hour >= 17 && hour < 22) {
            prefix = "İyi akşamlar";
        } else {
            prefix = "İyi geceler";
        }

        if (displayName == null || displayName.trim().isEmpty()) {
            return prefix;
        }
        return prefix + ", " + displayName.trim();
    }

    private String getDisplayName(DatabaseHelper.UserProfile profile, String username) {
        if (profile != null && profile.fullName != null && !profile.fullName.trim().isEmpty()) {
            return profile.fullName.trim();
        }
        if (profile != null && profile.username != null && !profile.username.trim().isEmpty()) {
            return profile.username.trim();
        }
        return username == null ? "" : username.trim();
    }

    private void refreshHeader() {
        String currentUser = AppSettings.getCurrentUser(this);
        DatabaseHelper.UserProfile profile = db.getUserProfile(currentUser);
        String displayName = getDisplayName(profile, currentUser);
        tvGreeting.setText(buildGreeting(displayName));
        tvCurrentUser.setText(currentUser == null || currentUser.trim().isEmpty() ? "Kullanıcı" : currentUser.trim());
        applyProfileImage(ivProfileAvatar, profile == null ? "" : profile.profileImagePath);
    }

    private void applyProfileImage(ImageView imageView, String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            imageView.setPadding(dp(12), dp(12), dp(12), dp(12));
            imageView.setImageResource(R.drawable.ic_person_circle_24);
            return;
        }
        imageView.setPadding(0, 0, 0, 0);
        imageView.setImageURI(Uri.parse(imagePath));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
