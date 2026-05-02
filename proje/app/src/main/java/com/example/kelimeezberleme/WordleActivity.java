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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WordleActivity extends AppCompatActivity {
    DatabaseHelper db;
    String targetWord;
    int currentAttempt = 0;
    int wordLength = 0;
    StringBuilder currentGuess = new StringBuilder();

    GridLayout glWordle;
    LinearLayout llKeyboard, llDateSelector;
    Button btnSubmit;

    TextView[][] cells;
    MaterialCardView[][] cards;
    boolean isGameOver = false;
    String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wordle);

        db = new DatabaseHelper(this);
        glWordle = findViewById(R.id.glWordle);
        llKeyboard = findViewById(R.id.llKeyboard);
        llDateSelector = findViewById(R.id.llDateSelector);
        btnSubmit = findViewById(R.id.btnSubmitGuess);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Varsayılan olarak bugünü seç
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        setupGameForDate(selectedDate);
        createDateSelector();
    }

    private void setupGameForDate(String date) {
        this.selectedDate = date;
        this.currentAttempt = 0;
        this.isGameOver = false;
        this.currentGuess.setLength(0);

        SharedPreferences pref = getSharedPreferences("WordlePrefs", MODE_PRIVATE);
        targetWord = pref.getString(date + "_word", null);

        if (targetWord == null) {
            targetWord = db.getRandomWordForWordle();
            if (targetWord != null) {
                pref.edit().putString(date + "_word", targetWord).apply();
            }
        }

        if (targetWord == null) {
            Toast.makeText(this, "Yeterli kelime bulunamadı!", Toast.LENGTH_LONG).show();
            return;
        }

        wordLength = targetWord.length();
        cells = new TextView[5][wordLength];
        cards = new MaterialCardView[5][wordLength];
        
        glWordle.setColumnCount(wordLength);
        glWordle.setRowCount(5);

        createGrid();
        createVirtualKeyboard();
        loadPreviousAttempts(date);
    }

    private void createDateSelector() {
        llDateSelector.removeAllViews();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat showDf = new SimpleDateFormat("dd MMM", Locale.getDefault());

        // Bugün dahil son 7 gün
        for (int i = 0; i < 7; i++) {
            String dateKey = df.format(cal.getTime());
            String dateShow = showDf.format(cal.getTime());
            if (i == 0) dateShow = "Bugün";

            Button btn = new Button(this, null, android.R.attr.buttonStyleSmall);
            btn.setText(dateShow);
            btn.setAllCaps(false);
            
            if (dateKey.equals(selectedDate)) {
                btn.setBackgroundColor(getResources().getColor(R.color.primary));
                btn.setTextColor(Color.WHITE);
            }

            btn.setOnClickListener(v -> setupGameForDate(dateKey));
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            btn.setLayoutParams(params);
            
            llDateSelector.addView(btn);
            cal.add(Calendar.DATE, -1); // Bir gün geriye git
        }
    }

    private void loadPreviousAttempts(String date) {
        SharedPreferences pref = getSharedPreferences("WordlePrefs", MODE_PRIVATE);
        String savedGuesses = pref.getString(date + "_guesses", "");
        boolean isFinished = pref.getBoolean(date + "_finished", false);

        if (!savedGuesses.isEmpty()) {
            String[] guesses = savedGuesses.split(",");
            for (String g : guesses) {
                if (!g.isEmpty()) fillRow(g);
            }
        }

        if (isFinished) {
            isGameOver = true;
            llKeyboard.setAlpha(0.5f);
        } else {
            llKeyboard.setAlpha(1.0f);
        }
    }

    private void fillRow(String guess) {
        for (int i = 0; i < wordLength; i++) {
            char c = guess.charAt(i);
            cells[currentAttempt][i].setText(String.valueOf(c));
            applyColor(currentAttempt, i, c);
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
        int cellSize = (displayWidth - 100) / Math.max(wordLength, 5);
        if (cellSize > 110) cellSize = 110;
        int margin = 4;

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < wordLength; c++) {
                MaterialCardView card = new MaterialCardView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize; params.height = cellSize;
                params.setMargins(margin, margin, margin, margin);
                card.setLayoutParams(params);
                card.setRadius(10f);
                card.setStrokeWidth(2);
                card.setStrokeColor(Color.LTGRAY);
                card.setCardBackgroundColor(Color.WHITE);

                TextView tv = new TextView(this);
                tv.setLayoutParams(new MaterialCardView.LayoutParams(cellSize, cellSize));
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(18);
                tv.setTextColor(Color.BLACK);
                card.addView(tv);
                glWordle.addView(card);
                cells[r][c] = tv; cards[r][c] = card;
            }
        }
    }

    private void createVirtualKeyboard() {
        String[] rows = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};
        llKeyboard.removeAllViews();
        int keyWidth = (getResources().getDisplayMetrics().widthPixels - 40) / 10;

        for (int i = 0; i < rows.length; i++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setGravity(Gravity.CENTER);
            if (i == 2) {
                Button bEnt = createKey("ENT", (int)(keyWidth*1.5));
                bEnt.setOnClickListener(v -> submitGuess());
                rowLayout.addView(bEnt);
            }
            for (char c : rows[i].toCharArray()) {
                Button b = createKey(String.valueOf(c), keyWidth);
                b.setOnClickListener(v -> addLetter(c));
                rowLayout.addView(b);
            }
            if (i == 2) {
                Button bDel = createKey("DEL", (int)(keyWidth*1.5));
                bDel.setOnClickListener(v -> removeLetter());
                rowLayout.addView(bDel);
            }
            llKeyboard.addView(rowLayout);
        }
    }

    private Button createKey(String text, int width) {
        Button btn = new Button(this, null, android.R.attr.buttonStyleSmall);
        btn.setText(text);
        btn.setLayoutParams(new LinearLayout.LayoutParams(width, 130));
        btn.setPadding(0,0,0,0); btn.setTextSize(10);
        return btn;
    }

    private void addLetter(char c) {
        if (isGameOver || currentGuess.length() >= wordLength) return;
        currentGuess.append(c);
        cells[currentAttempt][currentGuess.length()-1].setText(String.valueOf(c));
    }

    private void removeLetter() {
        if (isGameOver || currentGuess.length() == 0) return;
        cells[currentAttempt][currentGuess.length()-1].setText("");
        currentGuess.deleteCharAt(currentGuess.length()-1);
    }

    private void submitGuess() {
        if (isGameOver || currentGuess.length() != wordLength) return;
        String guess = currentGuess.toString();
        
        saveGuess(guess);
        fillRow(guess);

        if (guess.equals(targetWord) || currentAttempt == 5) {
            isGameOver = true;
            markFinished();
            Toast.makeText(this, guess.equals(targetWord) ? "TEBRİKLER! 🏆" : "Bitti! Kelime: " + targetWord, Toast.LENGTH_LONG).show();
            llKeyboard.setAlpha(0.5f);
        } else {
            currentGuess.setLength(0);
        }
    }

    private void saveGuess(String guess) {
        SharedPreferences pref = getSharedPreferences("WordlePrefs", MODE_PRIVATE);
        String saved = pref.getString(selectedDate + "_guesses", "");
        pref.edit().putString(selectedDate + "_guesses", saved + (saved.isEmpty() ? "" : ",") + guess).apply();
    }

    private void markFinished() {
        getSharedPreferences("WordlePrefs", MODE_PRIVATE).edit().putBoolean(selectedDate + "_finished", true).apply();
    }
}