package com.example.kelimeezberleme;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
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
    private static final String TAG = "WordleActivity";
    private static final int MAX_ATTEMPTS = 5;
    private static final int DATE_BUTTON_COUNT = 7;
    private static final String WORDLE_PREFS = "WordlePrefs";

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

        selectedDate = getTodayKey();

        createDateSelector();
        showGameForDate(selectedDate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String todayKey = getTodayKey();
        if (llDateSelector != null && !isDateVisible(todayKey)) {
            selectedDate = todayKey;
            createDateSelector();
            showGameForDate(todayKey);
        }
    }

    private void showGameForDate(String date) {
        String previousDate = selectedDate;
        try {
            setupGameForDate(date, false);
        } catch (RuntimeException e) {
            Log.e(TAG, "Wordle date could not be loaded: " + date, e);
            resetSavedGame(date);
            try {
                setupGameForDate(date, true);
                Toast.makeText(this, "Bu günün eski bulmaca kaydı yenilendi.", Toast.LENGTH_SHORT).show();
            } catch (RuntimeException secondError) {
                Log.e(TAG, "Wordle date reload failed: " + date, secondError);
                selectedDate = previousDate;
                updateDateSelectorStyles();
                glWordle.removeAllViews();
                llKeyboard.removeAllViews();
                tvResult.setText("Bulmaca yüklenemedi.");
                Toast.makeText(this, "Bulmaca yüklenirken hata oluştu.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupGameForDate(String date, boolean ignoreSavedAttempts) {
        this.selectedDate = date;
        this.currentAttempt = 0;
        this.isGameOver = false;
        this.currentGuess.setLength(0);
        this.tvResult.setText("");

        SharedPreferences pref = getSharedPreferences(WORDLE_PREFS, MODE_PRIVATE);
        // İlgili tarih için kelime var mı bak, yoksa o tarihe özel seç
        targetWord = normalizeWord(pref.getString(date + "_word", null));
        if (!isValidWord(targetWord)) {
            clearSavedGame(date);
            targetWord = null;
        }

        if (targetWord == null) {
            targetWord = normalizeWord(db.getRandomWordForWordle());
            if (isValidWord(targetWord)) {
                pref.edit().putString(date + "_word", targetWord).apply();
            } else {
                targetWord = null;
            }
        }

        if (targetWord == null) {
            Toast.makeText(this, "Yeterli kelime bulunamadı!", Toast.LENGTH_SHORT).show();
            return;
        }

        wordLength = targetWord.length();
        cells = new TextView[MAX_ATTEMPTS][wordLength];
        cards = new MaterialCardView[MAX_ATTEMPTS][wordLength];
        
        glWordle.setColumnCount(wordLength);
        glWordle.setRowCount(MAX_ATTEMPTS);

        createGrid();
        createVirtualKeyboard();
        updateDateSelectorStyles();
        if (ignoreSavedAttempts) {
            setKeyboardEnabled(true);
        } else {
            loadPreviousAttempts(date);
        }
    }

    private void createDateSelector() {
        llDateSelector.removeAllViews();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -(DATE_BUTTON_COUNT - 1));
        Locale trLocale = new Locale("tr", "TR");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat showDf = new SimpleDateFormat("d MMMM", trLocale);

        for (int i = 0; i < DATE_BUTTON_COUNT; i++) {
            String dateKey = df.format(cal.getTime());
            String dateShow = showDf.format(cal.getTime());

            Button btn = new Button(this, null, android.R.attr.buttonStyleSmall);
            btn.setText(dateShow);
            btn.setAllCaps(false);
            btn.setTag(dateKey);
            
            if (dateKey.equals(selectedDate)) {
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
                btn.setTextColor(Color.WHITE);
            } else {
                btn.setBackgroundColor(Color.LTGRAY);
                btn.setTextColor(Color.BLACK);
            }

            btn.setOnClickListener(v -> {
                if (!dateKey.equals(selectedDate)) {
                    showGameForDate(dateKey);
                }
            });
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics()),
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            btn.setLayoutParams(params);
            llDateSelector.addView(btn);
            cal.add(Calendar.DATE, 1);
        }

        llDateSelector.post(() -> {
            View parent = (View) llDateSelector.getParent();
            if (parent instanceof HorizontalScrollView) {
                ((HorizontalScrollView) parent).fullScroll(View.FOCUS_RIGHT);
            }
        });
    }

    private void updateDateSelectorStyles() {
        for (int i = 0; i < llDateSelector.getChildCount(); i++) {
            View child = llDateSelector.getChildAt(i);
            Object tag = child.getTag();
            if (!(child instanceof Button) || !(tag instanceof String)) continue;

            Button btn = (Button) child;
            boolean selected = tag.equals(selectedDate);
            if (selected) {
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
                btn.setTextColor(Color.WHITE);
            } else {
                btn.setBackgroundColor(Color.LTGRAY);
                btn.setTextColor(Color.BLACK);
            }
        }
    }

    private String getTodayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private boolean isDateVisible(String dateKey) {
        for (int i = 0; i < llDateSelector.getChildCount(); i++) {
            Object tag = llDateSelector.getChildAt(i).getTag();
            if (dateKey.equals(tag)) return true;
        }
        return false;
    }

    private void loadPreviousAttempts(String date) {
        try {
            SharedPreferences pref = getSharedPreferences(WORDLE_PREFS, MODE_PRIVATE);
            String savedGuesses = pref.getString(date + "_guesses", "");
            boolean isFinished = pref.getBoolean(date + "_finished", false);

            if (!savedGuesses.isEmpty()) {
                String[] guesses = savedGuesses.split(",");
                for (String g : guesses) {
                    if (!g.isEmpty() && currentAttempt < MAX_ATTEMPTS && g.length() == wordLength) {
                        fillRow(g);
                    }
                }
            }

            currentGuess.setLength(0);

            if (isFinished || currentAttempt >= MAX_ATTEMPTS) {
                isGameOver = true;
                setKeyboardEnabled(false);
                tvResult.setText("Doğru Kelime: " + targetWord);
                tvResult.setTextColor(ContextCompat.getColor(this, R.color.primary));
                if (!isFinished) {
                    markFinished();
                }
            } else {
                setKeyboardEnabled(true);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Saved Wordle attempts could not be loaded: " + date, e);
            clearSavedAttempts(date);
            currentAttempt = 0;
            currentGuess.setLength(0);
            createGrid();
            setKeyboardEnabled(true);
        }
    }

    private void fillRow(String guess) {
        if (currentAttempt >= MAX_ATTEMPTS || cells == null || cards == null) return;
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

        for (int r = 0; r < MAX_ATTEMPTS; r++) {
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
        if (isGameOver || currentAttempt >= MAX_ATTEMPTS || currentGuess.length() >= wordLength) return;
        currentGuess.append(c);
        cells[currentAttempt][currentGuess.length()-1].setText(String.valueOf(c));
    }

    private void removeLetter() {
        if (isGameOver || currentAttempt >= MAX_ATTEMPTS || currentGuess.length() == 0) return;
        cells[currentAttempt][currentGuess.length()-1].setText("");
        currentGuess.deleteCharAt(currentGuess.length()-1);
    }

    private void submitGuess() {
        if (isGameOver || currentAttempt >= MAX_ATTEMPTS || currentGuess.length() != wordLength) return;
        String guess = currentGuess.toString();
        saveGuess(guess);
        fillRow(guess);

        if (guess.equals(targetWord) || currentAttempt == MAX_ATTEMPTS) {
            isGameOver = true;
            markFinished();
            tvResult.setText("Doğru Kelime: " + targetWord);
            tvResult.setTextColor(ContextCompat.getColor(this, R.color.primary));
            setKeyboardEnabled(false);
        } else {
            currentGuess.setLength(0);
        }
    }

    private void saveGuess(String guess) {
        SharedPreferences pref = getSharedPreferences(WORDLE_PREFS, MODE_PRIVATE);
        String saved = pref.getString(selectedDate + "_guesses", "");
        pref.edit().putString(selectedDate + "_guesses", saved + (saved.isEmpty() ? "" : ",") + guess).apply();
    }

    private void markFinished() {
        getSharedPreferences(WORDLE_PREFS, MODE_PRIVATE).edit().putBoolean(selectedDate + "_finished", true).apply();
    }

    private void clearSavedGame(String date) {
        getSharedPreferences(WORDLE_PREFS, MODE_PRIVATE)
                .edit()
                .remove(date + "_word")
                .remove(date + "_guesses")
                .remove(date + "_finished")
                .commit();
    }

    private void clearSavedAttempts(String date) {
        getSharedPreferences(WORDLE_PREFS, MODE_PRIVATE)
                .edit()
                .remove(date + "_guesses")
                .remove(date + "_finished")
                .commit();
    }

    private void resetSavedGame(String date) {
        String freshWord = normalizeWord(db.getRandomWordForWordle());
        SharedPreferences.Editor editor = getSharedPreferences(WORDLE_PREFS, MODE_PRIVATE)
                .edit()
                .remove(date + "_word")
                .remove(date + "_guesses")
                .remove(date + "_finished");
        if (isValidWord(freshWord)) {
            editor.putString(date + "_word", freshWord);
        }
        editor.commit();
    }

    private String normalizeWord(String word) {
        if (word == null) return null;
        String cleanWord = word.trim().toUpperCase(Locale.US);
        return cleanWord.isEmpty() ? null : cleanWord;
    }

    private boolean isValidWord(String word) {
        return word != null && word.length() >= 3 && word.length() <= 7;
    }

    private void setKeyboardEnabled(boolean enabled) {
        llKeyboard.setAlpha(enabled ? 1.0f : 0.3f);
        for (int i = 0; i < llKeyboard.getChildCount(); i++) {
            View rowView = llKeyboard.getChildAt(i);
            if (!(rowView instanceof LinearLayout)) continue;
            LinearLayout row = (LinearLayout) rowView;
            for (int j = 0; j < row.getChildCount(); j++) {
                row.getChildAt(j).setEnabled(enabled);
            }
        }
    }
}
