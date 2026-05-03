package com.example.kelimeezberleme;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WordleActivity extends AppCompatActivity {
    private static final String TAG = "WordleActivity";
    private static final int MAX_ATTEMPTS = 5;
    private static final int DATE_BUTTON_COUNT = 7;
    private static final String WORDLE_PREFS = "WordlePrefs";
    private static final int LETTER_UNKNOWN = 0;
    private static final int LETTER_ABSENT = 1;
    private static final int LETTER_PRESENT = 2;
    private static final int LETTER_CORRECT = 3;
    private static final int WORDLE_GREEN = Color.parseColor("#6AAA64");
    private static final int WORDLE_YELLOW = Color.parseColor("#C9B458");
    private static final int WORDLE_GRAY = Color.parseColor("#787C7E");

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
    Map<Character, MaterialButton> keyboardButtons = new HashMap<>();
    Map<Character, Integer> keyboardStatuses = new HashMap<>();
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
        int status = getLetterStatus(col, c);
        cards[row][col].setCardBackgroundColor(getStatusColor(status));
        cells[row][col].setTextColor(Color.WHITE);
        updateKeyboardKey(c, status);
    }

    private int getLetterStatus(int col, char c) {
        if (c == targetWord.charAt(col)) return LETTER_CORRECT;
        if (targetWord.contains(String.valueOf(c))) return LETTER_PRESENT;
        return LETTER_ABSENT;
    }

    private int getStatusColor(int status) {
        if (status == LETTER_CORRECT) return WORDLE_GREEN;
        if (status == LETTER_PRESENT) return WORDLE_YELLOW;
        return WORDLE_GRAY;
    }

    private void updateKeyboardKey(char c, int newStatus) {
        MaterialButton key = keyboardButtons.get(c);
        if (key == null) return;

        int currentStatus = keyboardStatuses.containsKey(c) ? keyboardStatuses.get(c) : LETTER_UNKNOWN;
        if (newStatus < currentStatus) return;

        keyboardStatuses.put(c, newStatus);
        key.setBackgroundTintList(ColorStateList.valueOf(getStatusColor(newStatus)));
        key.setStrokeColor(ColorStateList.valueOf(getStatusColor(newStatus)));
        key.setTextColor(Color.WHITE);
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
        keyboardButtons.clear();
        keyboardStatuses.clear();

        for (int i = 0; i < rows.length; i++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowParams.setMargins(0, dp(3), 0, dp(3));
            rowLayout.setLayoutParams(rowParams);

            if (i == 1) {
                rowLayout.addView(createKeyboardSpacer(0.5f));
            }
            if (i == 2) {
                MaterialButton bEnt = createKey("ENT", 1.5f);
                applyActionKeyStyle(bEnt);
                bEnt.setOnClickListener(v -> submitGuess());
                rowLayout.addView(bEnt);
            }
            for (char c : rows[i].toCharArray()) {
                MaterialButton b = createKey(String.valueOf(c), 1f);
                keyboardButtons.put(c, b);
                b.setOnClickListener(v -> addLetter(c));
                rowLayout.addView(b);
            }
            if (i == 2) {
                MaterialButton bDel = createKey("DEL", 1.5f);
                applyActionKeyStyle(bDel);
                bDel.setOnClickListener(v -> removeLetter());
                rowLayout.addView(bDel);
            }
            if (i == 1) {
                rowLayout.addView(createKeyboardSpacer(0.5f));
            }
            llKeyboard.addView(rowLayout);
        }
    }

    private MaterialButton createKey(String text, float weight) {
        MaterialButton btn = new MaterialButton(this);
        btn.setText(text);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(56), weight);
        params.setMargins(dp(1), 0, dp(1), 0);
        btn.setLayoutParams(params);
        btn.setMinWidth(0);
        btn.setMinimumWidth(0);
        btn.setMinHeight(0);
        btn.setMinimumHeight(0);
        btn.setInsetTop(0);
        btn.setInsetBottom(0);
        btn.setPadding(0, 0, 0, 0);
        btn.setAllCaps(false);
        btn.setSingleLine(true);
        btn.setTextSize(text.length() > 1 ? 11 : 15);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setCornerRadius(dp(8));
        btn.setStrokeWidth(dp(1));
        resetKeyStyle(btn);
        return btn;
    }

    private View createKeyboardSpacer(float weight) {
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, dp(56), weight));
        return spacer;
    }

    private void resetKeyStyle(MaterialButton btn) {
        btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.surface_variant)));
        btn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.divider)));
        btn.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
    }

    private void applyActionKeyStyle(MaterialButton btn) {
        int primary = ContextCompat.getColor(this, R.color.primary);
        btn.setBackgroundTintList(ColorStateList.valueOf(primary));
        btn.setStrokeColor(ColorStateList.valueOf(primary));
        btn.setTextColor(Color.WHITE);
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
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
