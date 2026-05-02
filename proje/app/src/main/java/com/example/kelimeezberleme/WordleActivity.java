package com.example.kelimeezberleme;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WordleActivity extends AppCompatActivity {
    DatabaseHelper db;
    String targetWord;
    int currentAttempt = 0;
    int wordLength = 0;
    StringBuilder currentGuess = new StringBuilder();

    GridLayout glWordle;
    LinearLayout llKeyboard, llDateSelector;
    TextView tvResult;

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
        tvResult = findViewById(R.id.tvResult);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        setupGameForDate(selectedDate);
    }

    private void setupGameForDate(String date) {
        this.selectedDate = date;
        this.currentAttempt = 0;
        this.isGameOver = false;
        this.currentGuess.setLength(0);
        this.tvResult.setText("");

        SharedPreferences pref = getSharedPreferences("WordlePrefs", MODE_PRIVATE);
        // İlgili tarih için kelime var mı bak, yoksa o tarihe özel seç
        targetWord = pref.getString(date + "_word", null);
        if (targetWord == null) {
            targetWord = db.getRandomWordForWordle();
            if (targetWord != null) {
                pref.edit().putString(date + "_word", targetWord).apply();
            }
        }

        if (targetWord == null) {
            Toast.makeText(this, "Yeterli kelime bulunamadı!", Toast.LENGTH_SHORT).show();
            return;
        }

        wordLength = targetWord.length();
        cells = new TextView[5][wordLength];
        cards = new MaterialCardView[5][wordLength];
        
        glWordle.setColumnCount(wordLength);
        glWordle.setRowCount(5);

        createGrid();
        createVirtualKeyboard();
        createDateSelector();
        loadPreviousAttempts(date);
    }

    private void createDateSelector() {
        llDateSelector.removeAllViews();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat showDf = new SimpleDateFormat("dd MMM", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            String dateKey = df.format(cal.getTime());
            String dateShow = (i == 0) ? "Bugün" : showDf.format(cal.getTime());

            Button btn = new Button(this, null, android.R.attr.buttonStyleSmall);
            btn.setText(dateShow);
            btn.setAllCaps(false);
            
            if (dateKey.equals(selectedDate)) {
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
                btn.setTextColor(Color.WHITE);
            } else {
                btn.setBackgroundColor(Color.LTGRAY);
                btn.setTextColor(Color.BLACK);
            }

            btn.setOnClickListener(v -> setupGameForDate(dateKey));
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics()),
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            btn.setLayoutParams(params);
            llDateSelector.addView(btn);
            cal.add(Calendar.DATE, -1);
        }
    }

    private void loadPreviousAttempts(String date) {
        SharedPreferences pref = getSharedPreferences("WordlePrefs", MODE_PRIVATE);
        String savedGuesses = pref.getString(date + "_guesses", "");
        boolean isFinished = pref.getBoolean(date + "_finished", false);

        if (!savedGuesses.isEmpty()) {
            String[] guesses = savedGuesses.split(",");
            for (String g : guesses) {
                if (!g.isEmpty() && currentAttempt < 5 && g.length() == wordLength) {
                    fillRow(g);
                }
            }
        }

        if (isFinished) {
            isGameOver = true;
            llKeyboard.setAlpha(0.3f);
            tvResult.setText("Doğru Kelime: " + targetWord);
            tvResult.setTextColor(ContextCompat.getColor(this, R.color.primary));
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
        int cellSize = (displayWidth - 140) / Math.max(wordLength, 5);
        if (cellSize > 120) cellSize = 120;
        int margin = 4;

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < wordLength; c++) {
                MaterialCardView card = new MaterialCardView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize; params.height = cellSize;
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
        int keyWidth = (getResources().getDisplayMetrics().widthPixels - 80) / 10;

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
            tvResult.setText("Doğru Kelime: " + targetWord);
            tvResult.setTextColor(ContextCompat.getColor(this, R.color.primary));
            llKeyboard.setAlpha(0.3f);
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