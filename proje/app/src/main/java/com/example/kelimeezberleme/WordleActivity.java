package com.example.kelimeezberleme;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WordleActivity extends AppCompatActivity {
    DatabaseHelper db;
    String targetWord;
    int currentAttempt = 0;
    int wordLength = 0;
    StringBuilder currentGuess = new StringBuilder();

    GridLayout glWordle;
    LinearLayout llKeyboard;
    Button btnSubmit;

    TextView[][] cells;
    MaterialCardView[][] cards;
    boolean isGameOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wordle);

        db = new DatabaseHelper(this);
        glWordle = findViewById(R.id.glWordle);
        llKeyboard = findViewById(R.id.llKeyboard);
        btnSubmit = findViewById(R.id.btnSubmitGuess);

        if (!checkAndSetupDailyWord()) return;

        wordLength = targetWord.length();
        cells = new TextView[5][wordLength];
        cards = new MaterialCardView[5][wordLength];

        glWordle.setColumnCount(wordLength);
        glWordle.setRowCount(5);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        createGrid();
        createVirtualKeyboard();
    }

    private boolean checkAndSetupDailyWord() {
        SharedPreferences pref = getSharedPreferences("WordlePrefs", MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastDate = pref.getString("last_date", "");
        boolean isPlayed = pref.getBoolean("is_played_" + today, false);

        if (today.equals(lastDate) && isPlayed) {
            Toast.makeText(this, "Bugün zaten oynadın!", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }

        if (today.equals(lastDate)) {
            targetWord = pref.getString("daily_word", null);
        } else {
            targetWord = db.getRandomWordForWordle();
            if (targetWord != null) {
                pref.edit()
                    .putString("last_date", today)
                    .putString("daily_word", targetWord)
                    .putBoolean("is_played_" + today, false)
                    .apply();
            }
        }

        if (targetWord == null) {
            Toast.makeText(this, "Yeterli kelime bulunamadı!", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }
        return true;
    }

    private void createGrid() {
        glWordle.removeAllViews();
        // Ekran genişliğine göre hücre boyutunu hesapla
        int displayWidth = getResources().getDisplayMetrics().widthPixels;
        int paddingTotal = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
        int cellSize = (displayWidth - paddingTotal) / Math.max(wordLength, 5);
        if (cellSize > 120) cellSize = 120; // Çok büyük olmasın
        
        int margin = 6;

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < wordLength; c++) {
                MaterialCardView card = new MaterialCardView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.setMargins(margin, margin, margin, margin);
                card.setLayoutParams(params);
                card.setRadius(12f);
                card.setStrokeWidth(2);
                card.setStrokeColor(Color.LTGRAY);
                card.setCardBackgroundColor(Color.WHITE);

                TextView tv = new TextView(this);
                tv.setLayoutParams(new MaterialCardView.LayoutParams(cellSize, cellSize));
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(22);
                tv.setAllCaps(true);
                tv.setTextColor(Color.BLACK);
                tv.setText("");

                card.addView(tv);
                glWordle.addView(card);
                cells[r][c] = tv;
                cards[r][c] = card;
            }
        }
    }

    private void createVirtualKeyboard() {
        String[] rows = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};
        llKeyboard.removeAllViews();

        for (int i = 0; i < rows.length; i++) {
            String row = rows[i];
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER);
            
            // Üçüncü satıra ENTER butonunu ekle
            if (i == 2) {
                Button btnEnter = createKey("ENTER", 140);
                btnEnter.setOnClickListener(v -> submitGuess());
                rowLayout.addView(btnEnter);
            }

            for (char c : row.toCharArray()) {
                Button btn = createKey(String.valueOf(c), 85);
                btn.setOnClickListener(v -> addLetter(c));
                rowLayout.addView(btn);
            }

            // Üçüncü satıra SİL butonunu ekle
            if (i == 2) {
                Button btnDelete = createKey("SİL", 110);
                btnDelete.setOnClickListener(v -> removeLetter());
                rowLayout.addView(btnDelete);
            }

            llKeyboard.addView(rowLayout);
        }
    }

    private Button createKey(String text, int width) {
        Button btn = new Button(this, null, android.R.attr.buttonStyleSmall);
        btn.setText(text);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 54, getResources().getDisplayMetrics())
        );
        params.setMargins(2, 2, 2, 2);
        btn.setLayoutParams(params);
        btn.setPadding(0, 0, 0, 0);
        btn.setAllCaps(false);
        btn.setTextSize(12);
        return btn;
    }

    private void addLetter(char c) {
        if (isGameOver || currentGuess.length() >= wordLength) return;
        currentGuess.append(c);
        cells[currentAttempt][currentGuess.length() - 1].setText(String.valueOf(c));
        cards[currentAttempt][currentGuess.length() - 1].setStrokeColor(Color.parseColor("#6366F1"));
    }

    private void removeLetter() {
        if (isGameOver || currentGuess.length() == 0) return;
        cards[currentAttempt][currentGuess.length() - 1].setStrokeColor(Color.LTGRAY);
        cells[currentAttempt][currentGuess.length() - 1].setText("");
        currentGuess.deleteCharAt(currentGuess.length() - 1);
    }

    private void submitGuess() {
        if (isGameOver) return;
        if (currentGuess.length() != wordLength) {
            Toast.makeText(this, "Kelimeyi tamamlayın", Toast.LENGTH_SHORT).show();
            return;
        }
        processGuess(currentGuess.toString());
    }

    private void processGuess(String guess) {
        int correctChars = 0;
        for (int i = 0; i < wordLength; i++) {
            char gChar = guess.charAt(i);
            if (gChar == targetWord.charAt(i)) {
                cards[currentAttempt][i].setCardBackgroundColor(Color.parseColor("#6AAA64"));
                correctChars++;
            } else if (targetWord.contains(String.valueOf(gChar))) {
                cards[currentAttempt][i].setCardBackgroundColor(Color.parseColor("#C9B458"));
            } else {
                cards[currentAttempt][i].setCardBackgroundColor(Color.parseColor("#787C7E"));
            }
            cells[currentAttempt][i].setTextColor(Color.WHITE);
        }

        if (correctChars == wordLength || currentAttempt == 4) {
            isGameOver = true;
            markTodayAsPlayed();
            if (correctChars == wordLength) {
                Toast.makeText(this, "TEBRİKLER! 🏆", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Bitti! Kelime: " + targetWord, Toast.LENGTH_LONG).show();
            }
        } else {
            currentAttempt++;
            currentGuess.setLength(0);
        }
    }

    private void markTodayAsPlayed() {
        SharedPreferences pref = getSharedPreferences("WordlePrefs", MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        pref.edit().putBoolean("is_played_" + today, true).apply();
    }
}