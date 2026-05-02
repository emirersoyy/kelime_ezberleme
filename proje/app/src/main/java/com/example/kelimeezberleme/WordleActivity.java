package com.example.kelimeezberleme;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class WordleActivity extends AppCompatActivity {
    DatabaseHelper db;
    String targetWord;
    int currentAttempt = 0;
    GridLayout glWordle;
    EditText etGuess;
    Button btnSubmit;
    TextView[][] cells = new TextView[5][5];
    MaterialCardView[][] cards = new MaterialCardView[5][5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wordle);

        db = new DatabaseHelper(this);
        targetWord = db.getRandomFiveLetterWord();

        if (targetWord == null) {
            Toast.makeText(this, "Sistemde 5 harfli kelime bulunamadı!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        glWordle = findViewById(R.id.glWordle);
        etGuess = findViewById(R.id.etGuess);
        btnSubmit = findViewById(R.id.btnSubmitGuess);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        createGrid();

        btnSubmit.setOnClickListener(v -> {
            String guess = etGuess.getText().toString().toUpperCase().trim();
            if (guess.length() != 5) {
                Toast.makeText(WordleActivity.this, "Lütfen 5 harfli bir kelime girin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentAttempt < 5) {
                processGuess(guess);
            }
        });
    }

    private void createGrid() {
        glWordle.removeAllViews();
        int cellSize = 130;
        int margin = 8;

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                MaterialCardView card = new MaterialCardView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.setMargins(margin, margin, margin, margin);
                card.setLayoutParams(params);
                card.setCardCornerRadius(12f);
                card.setCardElevation(2f);
                card.setStrokeWidth(2);
                card.setStrokeColor(Color.LTGRAY);

                TextView tv = new TextView(this);
                tv.setLayoutParams(new MaterialCardView.LayoutParams(cellSize, cellSize));
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(20);
                tv.setTextColor(Color.BLACK);
                tv.setText("");

                card.addView(tv);
                glWordle.addView(card);
                cells[r][c] = tv;
                cards[r][c] = card;
            }
        }
    }

    private void processGuess(String guess) {
        for (int i = 0; i < 5; i++) {
            char gChar = guess.charAt(i);
            cells[currentAttempt][i].setText(String.valueOf(gChar));
            
            if (gChar == targetWord.charAt(i)) {
                cards[currentAttempt][i].setCardBackgroundColor(Color.parseColor("#6AAA64")); // Yeşil
                cells[currentAttempt][i].setTextColor(Color.WHITE);
            } else if (targetWord.contains(String.valueOf(gChar))) {
                cards[currentAttempt][i].setCardBackgroundColor(Color.parseColor("#C9B458")); // Sarı
                cells[currentAttempt][i].setTextColor(Color.WHITE);
            } else {
                cards[currentAttempt][i].setCardBackgroundColor(Color.parseColor("#787C7E")); // Gri
                cells[currentAttempt][i].setTextColor(Color.WHITE);
            }
        }

        if (guess.equals(targetWord)) {
            Toast.makeText(this, "Tebrikler! Kelimeyi buldun: " + targetWord, Toast.LENGTH_LONG).show();
            btnSubmit.setEnabled(false);
        } else {
            currentAttempt++;
            etGuess.setText("");
            if (currentAttempt == 5) {
                Toast.makeText(this, "Oyun Bitti! Kelime: " + targetWord, Toast.LENGTH_LONG).show();
                btnSubmit.setEnabled(false);
            }
        }
    }
}