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

        if (!setupDailyGame()) return;

        wordLength = targetWord.length();
        cells = new TextView[5][wordLength];
        cards = new MaterialCardView[5][wordLength];

        glWordle.setColumnCount(wordLength);
        glWordle.setRowCount(5);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        createGrid();
        createVirtualKeyboard();
        
        // Eğer oyun zaten bitmişse, eski tahminleri yükle ve kilitle
        checkIfAlreadyPlayed();
    }

    private boolean setupDailyGame() {
        SharedPreferences pref = getSharedPreferences("WordlePrefs", MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastDate = pref.getString("last_date", "");
        
        if (today.equals(lastDate)) {
            targetWord = pref.getString("daily_word", null);
        } else {
            targetWord = db.getRandomWordForWordle();
            if (targetWord != null) {
                pref.edit()
                    .putString("last_date", today)
                    .putString("daily_word", targetWord)
                    .putString("guesses", "") // Yeni gün tahminleri sıfırla
                    .putBoolean("is_finished", false)
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

    private void checkIfAlreadyPlayed() {
        SharedPreferences pref = getSharedPreferences("WordlePrefs", MODE_PRIVATE);
        boolean isFinished = pref.getBoolean("is_finished", false);
        String savedGuesses = pref.getString("guesses", "");

        if (!savedGuesses.isEmpty()) {
            String[] guesses = savedGuesses.split(",");
            for (String g : guesses) {
                if (!g.isEmpty()) {
                    fillSavedGuess(g);
                }
            }
        }

        if (isFinished) {
            isGameOver = true;
            btnSubmit.setEnabled(false);
            llKeyboard.setAlpha(0.5f); // Klavyeyi soluklaştır
            Toast.makeText(this, "Bugünkü bulmacayı tamamladın!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fillSavedGuess(String guess) {
        for (int i = 0; i < wordLength; i++) {
            char gChar = guess.charAt(i);
            cells[currentAttempt][i].setText(String.valueOf(gChar));
            applyColor(currentAttempt, i, gChar);
        }
        currentAttempt++;
    }

    private void applyColor(int row, int col, char c) {
        if (c == targetWord.charAt(col)) {
            cards[row][col].setCardBackgroundColor(Color.parseColor("#6AAA64"));
        } else if (targetWord.contains(String.valueOf(c))) {
            cards[row][col].setCardBackgroundColor(Color.parseColor("#C9B458"));
        } else {
            cards[row][col].setCardBackgroundColor(Color.parseColor("#787C7E"));
        }
        cells[row][col].setTextColor(Color.WHITE);
    }

    private void createGrid() {
        glWordle.removeAllViews();
        int displayWidth = getResources().getDisplayMetrics().widthPixels;
        int screenPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
        int cellSize = (displayWidth - screenPadding) / Math.max(wordLength, 5);
        if (cellSize > 120) cellSize = 120;
        
        int margin = 4;

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
                tv.setTextSize(20);
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

        int displayWidth = getResources().getDisplayMetrics().widthPixels;
        int keyWidth = (displayWidth - 40) / 10;

        for (int i = 0; i < rows.length; i++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setGravity(Gravity.CENTER);
            if (i == 2) {
                Button btnEnt = createKey("ENT", keyWidth + (keyWidth/2));
                btnEnt.setOnClickListener(v -> submitGuess());
                rowLayout.addView(btnEnt);
            }
            for (char c : rows[i].toCharArray()) {
                Button btn = createKey(String.valueOf(c), keyWidth);
                btn.setOnClickListener(v -> addLetter(c));
                rowLayout.addView(btn);
            }
            if (i == 2) {
                Button btnDel = createKey("DEL", keyWidth + (keyWidth/2));
                btnDel.setOnClickListener(v -> removeLetter());
                rowLayout.addView(btnDel);
            }
            llKeyboard.addView(rowLayout);
        }
    }

    private Button createKey(String text, int width) {
        Button btn = new Button(this, null, android.R.attr.buttonStyleSmall);
        btn.setText(text);
        btn.setLayoutParams(new LinearLayout.LayoutParams(width, 130));
        btn.setPadding(0,0,0,0);
        btn.setTextSize(11);
        return btn;
    }

    private void addLetter(char c) {
        if (isGameOver || currentGuess.length() >= wordLength) return;
        currentGuess.append(c);
        cells[currentAttempt][currentGuess.length() - 1].setText(String.valueOf(c));
    }

    private void removeLetter() {
        if (isGameOver || currentGuess.length() == 0) return;
        cells[currentAttempt][currentGuess.length() - 1].setText("");
        currentGuess.deleteCharAt(currentGuess.length() - 1);
    }

    private void submitGuess() {
        if (isGameOver) return;
        if (currentGuess.length() != wordLength) return;
        processGuess(currentGuess.toString());
    }

    private void processGuess(String guess) {
        saveGuess(guess);
        for (int i = 0; i < wordLength; i++) {
            applyColor(currentAttempt, i, guess.charAt(i));
        }

        if (guess.equals(targetWord) || currentAttempt == 4) {
            isGameOver = true;
            saveGameOver();
            Toast.makeText(this, guess.equals(targetWord) ? "TEBRİKLER! 🏆" : "Bitti! Kelime: " + targetWord, Toast.LENGTH_LONG).show();
            btnSubmit.setEnabled(false);
        } else {
            currentAttempt++;
            currentGuess.setLength(0);
        }
    }

    private void saveGuess(String guess) {
        SharedPreferences pref = getSharedPreferences("WordlePrefs", MODE_PRIVATE);
        String saved = pref.getString("guesses", "");
        pref.edit().putString("guesses", saved + (saved.isEmpty() ? "" : ",") + guess).apply();
    }

    private void saveGameOver() {
        getSharedPreferences("WordlePrefs", MODE_PRIVATE).edit().putBoolean("is_finished", true).apply();
    }
}