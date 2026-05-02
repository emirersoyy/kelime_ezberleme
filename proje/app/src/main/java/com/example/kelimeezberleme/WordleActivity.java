package com.example.kelimeezberleme;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
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

    GridLayout glWordle;
    EditText etHiddenInput;
    Button btnSubmit;

    TextView[][] cells;
    MaterialCardView[][] cards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wordle);

        db = new DatabaseHelper(this);
        setupDailyWord(); // Günlük kelimeyi ayarla

        if (targetWord == null) {
            Toast.makeText(this, "Yeterli kelime bulunamadı!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        wordLength = targetWord.length();
        cells = new TextView[5][wordLength];
        cards = new MaterialCardView[5][wordLength];

        glWordle = findViewById(R.id.glWordle);
        etHiddenInput = findViewById(R.id.etHiddenInput);
        btnSubmit = findViewById(R.id.btnSubmitGuess);

        glWordle.setColumnCount(wordLength);
        glWordle.setRowCount(5);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        // Klavye tetikleyici
        View.OnClickListener keyboardTrigger = v -> {
            etHiddenInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etHiddenInput, InputMethodManager.SHOW_IMMEDIATE);
            }
        };
        findViewById(android.R.id.content).setOnClickListener(keyboardTrigger);
        glWordle.setOnClickListener(keyboardTrigger);

        createGrid();
        setupInputLogic();

        btnSubmit.setOnClickListener(v -> {
            String guess = etHiddenInput.getText().toString().toUpperCase().trim();
            if (guess.length() != wordLength) {
                Toast.makeText(WordleActivity.this, "Lütfen " + wordLength + " harf girin", Toast.LENGTH_SHORT).show();
                return;
            }
            processGuess(guess);
        });

        // Sayfa açılır açılmaz klavyeyi açmaya çalış
        etHiddenInput.postDelayed(() -> keyboardTrigger.onClick(null), 500);
    }

    private void setupDailyWord() {
        SharedPreferences pref = getSharedPreferences("WordlePrefs", MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastDate = pref.getString("last_date", "");
        
        if (today.equals(lastDate)) {
            // Bugün zaten bir kelime seçilmiş, onu kullan
            targetWord = pref.getString("daily_word", null);
        } else {
            // Yeni gün, yeni kelime seç ve kaydet
            targetWord = db.getRandomWordForWordle();
            if (targetWord != null) {
                pref.edit()
                    .putString("last_date", today)
                    .putString("daily_word", targetWord)
                    .putInt("attempt_count", 0) // Yeni gün denemeleri sıfırla
                    .apply();
            }
        }
        
        // Eğer kullanıcı bugün hakkını bitirdiyse (isteğe bağlı eklenebilir)
        // currentAttempt = pref.getInt("attempt_count", 0);
    }

    private void createGrid() {
        glWordle.removeAllViews();
        int cellSize = (wordLength > 5) ? 100 : 130; 
        int margin = 6;

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < wordLength; c++) {
                MaterialCardView card = new MaterialCardView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.setMargins(margin, margin, margin, margin);
                card.setLayoutParams(params);
                card.setRadius(16f);
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

    private void setupInputLogic() {
        etHiddenInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (currentAttempt >= 5) return;
                String currentText = s.toString().toUpperCase();
                for (int i = 0; i < wordLength; i++) {
                    if (i < currentText.length()) {
                        cells[currentAttempt][i].setText(String.valueOf(currentText.charAt(i)));
                        cards[currentAttempt][i].setStrokeColor(Color.parseColor("#6366F1"));
                    } else {
                        cells[currentAttempt][i].setText("");
                        cards[currentAttempt][i].setStrokeColor(Color.LTGRAY);
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
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

        if (correctChars == wordLength) {
            Toast.makeText(this, "MÜKEMMEL! 🏆", Toast.LENGTH_LONG).show();
            btnSubmit.setEnabled(false);
            etHiddenInput.setEnabled(false);
        } else {
            currentAttempt++;
            etHiddenInput.setText("");
            if (currentAttempt == 5) {
                Toast.makeText(this, "Hakkın bitti! Kelime: " + targetWord, Toast.LENGTH_LONG).show();
                btnSubmit.setEnabled(false);
                etHiddenInput.setEnabled(false);
            }
        }
    }
}