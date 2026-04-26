package com.example.kelimeezberleme;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
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
                showQuizSettingsDialog();
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

    private void showQuizSettingsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quiz_settings, null);
        EditText etLimit = dialogView.findViewById(R.id.etQuestionLimit);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setPositiveButton("Sınava Başla", (dialog, which) -> {
            String val = etLimit.getText().toString();
            int limit = val.isEmpty() ? 10 : Integer.parseInt(val);
            
            Intent intent = new Intent(MainActivity.this, QuizActivity.class);
            intent.putExtra("limit", limit);
            startActivity(intent);
        });
        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());
        
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        builder.show();
    }
}