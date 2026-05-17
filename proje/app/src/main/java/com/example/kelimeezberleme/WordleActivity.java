package com.example.kelimeezberleme;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
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

public class WordleActivity extends BottomNavActivity {
    private static final String TAG = "WordleActivity";
    private static final int MAX_ATTEMPTS = 5;
    private static final int WORDLE_WORD_LENGTH = 5;
    private static final String WORDLE_PREFS = "WordlePrefs";
    private static final String STATUS_CORRECT = "correct";
    private static final String STATUS_FAILED = "failed";
    private static final String STATUS_PARTIAL = "partial";
    private static final int LETTER_UNKNOWN = 0;
    private static final int LETTER_ABSENT = 1;
    private static final int LETTER_PRESENT = 2;
    private static final int LETTER_CORRECT = 3;
    private static final int WORDLE_GREEN = Color.parseColor("#6BAA64");
    private static final int WORDLE_YELLOW = Color.parseColor("#D6A63A");
    private static final int WORDLE_GRAY = Color.parseColor("#8A9099");
    private static final int WORDLE_RED = Color.parseColor("#DC2626");
    private static final int CALENDAR_GRAY = Color.parseColor("#E5E7EB");
    private static final int CALENDAR_LOCKED_GRAY = Color.parseColor("#4B5563");
    private static final int KEYBOARD_KEY_BG = Color.parseColor("#D1D5DB");
    private static final int KEYBOARD_KEY_STROKE = Color.parseColor("#A8B0BA");
    private static final int KEYBOARD_ABSENT_BG = Color.parseColor("#6B7280");
    private static final int KEYBOARD_ABSENT_STROKE = Color.parseColor("#4B5563");
    private static final int KEYBOARD_ACTION_BG = Color.parseColor("#2563EB");
    private static final long DAY_MS = 24L * 60L * 60L * 1000L;

    DatabaseHelper db;
    String targetWord;
    int currentAttempt = 0;
    int wordLength = 0;
    StringBuilder currentGuess = new StringBuilder();

    GridLayout glWordle;
    LinearLayout llKeyboard;
    TextView tvResult;
    MaterialButton btnSelectedDate;

    TextView[][] cells;
    MaterialCardView[][] cards;
    Map<Character, MaterialButton> keyboardButtons = new HashMap<>();
    Map<Character, Integer> keyboardStatuses = new HashMap<>();
    boolean isGameOver = false;
    String selectedDate;
    long accountOpenedAtMillis;
    String accountOpenedDateKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wordle);

        db = new DatabaseHelper(this);
        glWordle = findViewById(R.id.glWordle);
        llKeyboard = findViewById(R.id.llKeyboard);
        tvResult = findViewById(R.id.tvResult);
        btnSelectedDate = findViewById(R.id.btnSelectedDate);

        btnSelectedDate.setOnClickListener(v -> showCalendarDialog());

        accountOpenedAtMillis = db.ensureUserCreatedAt(AppSettings.getCurrentUser(this));
        if (accountOpenedAtMillis <= 0L) {
            accountOpenedAtMillis = System.currentTimeMillis();
        }
        accountOpenedDateKey = formatDateKey(accountOpenedAtMillis);
        selectedDate = getTodayKey();

        showGameForDate(selectedDate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSelectedDateButton();
    }

    private void showGameForDate(String date) {
        if (isBeforeAccountOpened(date)) {
            Toast.makeText(this, "Bu tarih hesabın açılış gününden önce olamaz.", Toast.LENGTH_SHORT).show();
            updateSelectedDateButton();
            return;
        }

        if (isFutureDate(date)) {
            Toast.makeText(this, "Gelecek günlerin bulmacası henüz açılmadı.", Toast.LENGTH_SHORT).show();
            updateSelectedDateButton();
            return;
        }

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
                updateSelectedDateButton();
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
        targetWord = normalizeWord(pref.getString(prefKey(date, "_word"), null));
        if (!isValidWord(targetWord)) {
            clearSavedGame(date);
            targetWord = null;
        }

        if (targetWord == null) {
            targetWord = chooseWordleWord();
            if (isValidWord(targetWord)) {
                pref.edit().putString(prefKey(date, "_word"), targetWord).apply();
            } else {
                targetWord = null;
            }
        }

        if (targetWord == null) {
            Toast.makeText(this, "Wordle için 5 harfli kelime bulunamadı.", Toast.LENGTH_LONG).show();
            return;
        }

        wordLength = targetWord.length();
        cells = new TextView[MAX_ATTEMPTS][wordLength];
        cards = new MaterialCardView[MAX_ATTEMPTS][wordLength];
        
        glWordle.setColumnCount(wordLength);
        glWordle.setRowCount(MAX_ATTEMPTS);

        createGrid();
        createVirtualKeyboard();
        updateSelectedDateButton();
        if (ignoreSavedAttempts) {
            setKeyboardEnabled(true);
        } else {
            loadPreviousAttempts(date);
        }
    }

    private String getTodayKey() {
        return formatDateKey(System.currentTimeMillis());
    }

    private String prefKey(String dateKey, String suffix) {
        return AppSettings.getCurrentUserKey(this) + "_" + dateKey + suffix;
    }

    private void updateSelectedDateButton() {
        btnSelectedDate.setText(formatDisplayDate(selectedDate));
    }

    private String formatDisplayDate(String dateKey) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateKey);
            return new SimpleDateFormat("d MMMM", new Locale("tr", "TR")).format(date);
        } catch (Exception e) {
            return dateKey;
        }
    }

    private void showCalendarDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        Calendar visibleMonth = Calendar.getInstance();
        try {
            Date selected = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDate);
            visibleMonth.setTime(selected);
        } catch (Exception ignored) {
            visibleMonth.setTime(new Date());
        }
        visibleMonth.set(Calendar.DAY_OF_MONTH, 1);
        Calendar minMonth = getAccountOpenedCalendar();
        minMonth.set(Calendar.DAY_OF_MONTH, 1);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(16), dp(18), dp(18));
        root.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);

        MaterialButton prev = createCalendarNavButton("<");
        TextView title = new TextView(this);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        MaterialButton next = createCalendarNavButton(">");

        header.addView(prev);
        header.addView(title);
        header.addView(next);
        root.addView(header);

        LinearLayout weekHeader = new LinearLayout(this);
        weekHeader.setOrientation(LinearLayout.HORIZONTAL);
        weekHeader.setGravity(Gravity.CENTER);
        weekHeader.setPadding(0, dp(12), 0, dp(6));
        String[] days = {"Pzt", "Sal", "Car", "Per", "Cum", "Cmt", "Paz"};
        for (String day : days) {
            TextView tv = new TextView(this);
            tv.setGravity(Gravity.CENTER);
            tv.setText(day);
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            tv.setTextSize(12);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            weekHeader.addView(tv);
        }
        root.addView(weekHeader);

        GridLayout calendarGrid = new GridLayout(this);
        calendarGrid.setColumnCount(7);
        calendarGrid.setUseDefaultMargins(false);
        calendarGrid.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        root.addView(calendarGrid);

        Runnable[] renderCalendar = new Runnable[1];
        renderCalendar[0] = () -> {
            title.setText(new SimpleDateFormat("MMMM yyyy", new Locale("tr", "TR")).format(visibleMonth.getTime()));
            renderCalendarDays(calendarGrid, visibleMonth, dialog);
            updateCalendarNavState(prev, visibleMonth, minMonth);
        };

        prev.setOnClickListener(v -> {
            if (isSameMonth(visibleMonth, minMonth)) {
                return;
            }
            visibleMonth.add(Calendar.MONTH, -1);
            if (visibleMonth.before(minMonth)) {
                visibleMonth.setTime(minMonth.getTime());
            }
            renderCalendar[0].run();
        });
        next.setOnClickListener(v -> {
            visibleMonth.add(Calendar.MONTH, 1);
            renderCalendar[0].run();
        });

        renderCalendar[0].run();

        MaterialCardView card = new MaterialCardView(this);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        card.setRadius(getResources().getDimension(R.dimen.radius_lg));
        card.setCardElevation(dp(10));
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.surface));
        card.addView(root);
        dialog.setContentView(card);

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(window.getAttributes());
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92f);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }
    }

    private MaterialButton createCalendarNavButton(String text) {
        MaterialButton btn = new MaterialButton(this);
        btn.setText(text);
        btn.setTextSize(18);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setMinWidth(0);
        btn.setMinimumWidth(0);
        btn.setMinHeight(0);
        btn.setMinimumHeight(0);
        btn.setInsetTop(0);
        btn.setInsetBottom(0);
        btn.setPadding(0, 0, 0, dp(2));
        btn.setCornerRadius(Math.round(getResources().getDimension(R.dimen.radius_lg)));
        btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.surface_variant)));
        btn.setTextColor(ContextCompat.getColor(this, R.color.primary));
        btn.setLayoutParams(new LinearLayout.LayoutParams(dp(40), dp(40)));
        return btn;
    }

    private void renderCalendarDays(GridLayout calendarGrid, Calendar visibleMonth, Dialog dialog) {
        calendarGrid.removeAllViews();

        Calendar cursor = (Calendar) visibleMonth.clone();
        int month = cursor.get(Calendar.MONTH);
        int firstDayOffset = (cursor.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        int daysInMonth = cursor.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < firstDayOffset; i++) {
            calendarGrid.addView(createCalendarBlankCell());
        }

        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        for (int day = 1; day <= daysInMonth; day++) {
            cursor.set(Calendar.DAY_OF_MONTH, day);
            String dateKey = keyFormat.format(cursor.getTime());
            MaterialButton dayButton = createCalendarDayButton(day, dateKey);
            dayButton.setOnClickListener(v -> {
                dialog.dismiss();
                showGameForDate(dateKey);
            });
            calendarGrid.addView(dayButton);
        }
    }

    private View createCalendarBlankCell() {
        View view = new View(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = dp(42);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dp(2), dp(2), dp(2), dp(2));
        view.setLayoutParams(params);
        return view;
    }

    private MaterialButton createCalendarDayButton(int day, String dateKey) {
        MaterialButton btn = new MaterialButton(this);
        btn.setText(String.valueOf(day));
        btn.setTextSize(13);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setMinWidth(0);
        btn.setMinimumWidth(0);
        btn.setMinHeight(0);
        btn.setMinimumHeight(0);
        btn.setInsetTop(0);
        btn.setInsetBottom(0);
        btn.setPadding(0, 0, 0, 0);
        btn.setCornerRadius(Math.round(getResources().getDimension(R.dimen.radius_sm)));
        btn.setStrokeWidth(dateKey.equals(selectedDate) ? dp(2) : 0);
        btn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));

        boolean hasPuzzle = hasPuzzleForDate(dateKey);
        int background = getCalendarDayColor(dateKey);
        btn.setBackgroundTintList(ColorStateList.valueOf(background));
        String status = getGameStatus(dateKey);
        if (!hasPuzzle) {
            btn.setTextColor(Color.WHITE);
        } else if (status == null) {
            btn.setTextColor(ContextCompat.getColor(this, R.color.calendar_unplayed_text));
        } else {
            btn.setTextColor(Color.WHITE);
        }
        btn.setEnabled(hasPuzzle);
        btn.setAlpha(1.0f);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = dp(42);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dp(2), dp(2), dp(2), dp(2));
        btn.setLayoutParams(params);
        return btn;
    }

    private int getCalendarDayColor(String dateKey) {
        if (!hasPuzzleForDate(dateKey)) return CALENDAR_LOCKED_GRAY;

        String status = getGameStatus(dateKey);
        if (STATUS_CORRECT.equals(status)) return WORDLE_GREEN;
        if (STATUS_FAILED.equals(status)) return WORDLE_RED;
        if (STATUS_PARTIAL.equals(status)) return WORDLE_YELLOW;
        return CALENDAR_GRAY;
    }

    private boolean hasPuzzleForDate(String dateKey) {
        return !isBeforeAccountOpened(dateKey) && !isFutureDate(dateKey);
    }

    private boolean isFutureDate(String dateKey) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = df.parse(dateKey);
            Date today = df.parse(getTodayKey());
            return date != null && today != null && date.after(today);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isBeforeAccountOpened(String dateKey) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateKey);
            Date opened = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(accountOpenedDateKey);
            return date != null && opened != null && date.before(opened);
        } catch (Exception e) {
            return false;
        }
    }

    private Calendar getAccountOpenedCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(accountOpenedAtMillis > 0 ? accountOpenedAtMillis : System.currentTimeMillis());
        normalizeToStartOfDay(calendar);
        return calendar;
    }

    private void updateCalendarNavState(MaterialButton prev, Calendar visibleMonth, Calendar minMonth) {
        boolean canGoBack = !isSameMonth(visibleMonth, minMonth) && visibleMonth.after(minMonth);
        prev.setEnabled(canGoBack);
        prev.setAlpha(canGoBack ? 1.0f : 0.35f);
    }

    private boolean isSameMonth(Calendar left, Calendar right) {
        return left.get(Calendar.YEAR) == right.get(Calendar.YEAR)
                && left.get(Calendar.MONTH) == right.get(Calendar.MONTH);
    }

    private String getGameStatus(String dateKey) {
        SharedPreferences pref = getSharedPreferences(WORDLE_PREFS, MODE_PRIVATE);
        String savedStatus = pref.getString(prefKey(dateKey, "_status"), null);
        if (savedStatus != null) return savedStatus;

        String savedGuesses = pref.getString(prefKey(dateKey, "_guesses"), "");
        if (savedGuesses.isEmpty()) return null;

        String savedWord = normalizeWord(pref.getString(prefKey(dateKey, "_word"), null));
        boolean finished = pref.getBoolean(prefKey(dateKey, "_finished"), false);
        boolean solved = savedWord != null && containsGuess(savedGuesses, savedWord);
        if (solved) return STATUS_CORRECT;

        int guessCount = countGuesses(savedGuesses);
        if (finished || guessCount >= MAX_ATTEMPTS) return STATUS_FAILED;
        return STATUS_PARTIAL;
    }

    private boolean containsGuess(String savedGuesses, String word) {
        String[] guesses = savedGuesses.split(",");
        for (String guess : guesses) {
            if (word.equals(normalizeWord(guess))) return true;
        }
        return false;
    }

    private int countGuesses(String savedGuesses) {
        if (savedGuesses == null || savedGuesses.isEmpty()) return 0;
        int count = 0;
        String[] guesses = savedGuesses.split(",");
        for (String guess : guesses) {
            if (!guess.isEmpty()) count++;
        }
        return count;
    }

    private void loadPreviousAttempts(String date) {
        try {
            SharedPreferences pref = getSharedPreferences(WORDLE_PREFS, MODE_PRIVATE);
            String savedGuesses = pref.getString(prefKey(date, "_guesses"), "");
            boolean isFinished = pref.getBoolean(prefKey(date, "_finished"), false);

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
                boolean solved = containsGuess(savedGuesses, targetWord);
                saveGameStatus(solved ? STATUS_CORRECT : STATUS_FAILED);
                tvResult.setTextColor(solved ? WORDLE_GREEN : WORDLE_RED);
                if (!isFinished) {
                    markFinished(solved);
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
        if (newStatus == LETTER_ABSENT) {
            key.setBackgroundTintList(ColorStateList.valueOf(KEYBOARD_ABSENT_BG));
            key.setStrokeColor(ColorStateList.valueOf(KEYBOARD_ABSENT_STROKE));
            key.setTextColor(Color.parseColor("#1F2937"));
            return;
        }

        key.setBackgroundTintList(ColorStateList.valueOf(getStatusColor(newStatus)));
        key.setStrokeColor(ColorStateList.valueOf(getStatusColor(newStatus)));
        key.setTextColor(Color.WHITE);
    }

    private void createGrid() {
        glWordle.removeAllViews();
        glWordle.setUseDefaultMargins(false);
        glWordle.setAlignmentMode(GridLayout.ALIGN_MARGINS);

        int displayWidth = getResources().getDisplayMetrics().widthPixels;
        int horizontalPadding = dp(72);
        int margin = dp(1);
        int availableWidth = displayWidth - horizontalPadding;
        int totalMargins = wordLength * margin * 2;
        int cellSize = (availableWidth - totalMargins) / wordLength;
        if (cellSize > dp(63)) cellSize = dp(63);
        if (cellSize < dp(60)) cellSize = dp(60);

        for (int r = 0; r < MAX_ATTEMPTS; r++) {
            for (int c = 0; c < wordLength; c++) {
                MaterialCardView card = new MaterialCardView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize; params.height = cellSize;
                params.setMargins(margin, margin, margin, margin);
                card.setLayoutParams(params);
                card.setRadius(getResources().getDimension(R.dimen.radius_xs));
                card.setStrokeWidth(2);
                card.setStrokeColor(Color.LTGRAY);
                card.setCardBackgroundColor(Color.WHITE);

                TextView tv = new TextView(this);
                tv.setLayoutParams(new MaterialCardView.LayoutParams(cellSize, cellSize));
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(25);
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
                rowLayout.addView(createKeyboardSpacer(0.3f));
            }
            if (i == 2) {
                MaterialButton bEnt = createKey("ENT", 1.8f);
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
                MaterialButton bDel = createKey("DEL", 1.8f);
                applyActionKeyStyle(bDel);
                bDel.setOnClickListener(v -> removeLetter());
                rowLayout.addView(bDel);
            }
            if (i == 1) {
                rowLayout.addView(createKeyboardSpacer(0.3f));
            }
            llKeyboard.addView(rowLayout);
        }
    }

    private MaterialButton createKey(String text, float weight) {
        MaterialButton btn = new MaterialButton(this);
        btn.setText(text);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(58), weight);
        params.setMargins(dp(3), 0, dp(3), 0);
        btn.setLayoutParams(params);
        btn.setMinWidth(0);
        btn.setMinimumWidth(0);
        btn.setMinHeight(0);
        btn.setMinimumHeight(0);
        btn.setInsetTop(0);
        btn.setInsetBottom(0);
        btn.setPadding(0, 0, 0, dp(1));
        btn.setAllCaps(false);
        btn.setSingleLine(true);
        btn.setTextSize(text.length() > 1 ? 13 : 17);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setCornerRadius(dp(10));
        btn.setStrokeWidth(dp(1));
        resetKeyStyle(btn);
        return btn;
    }

    private View createKeyboardSpacer(float weight) {
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, dp(58), weight));
        return spacer;
    }

    private void resetKeyStyle(MaterialButton btn) {
        btn.setBackgroundTintList(ColorStateList.valueOf(KEYBOARD_KEY_BG));
        btn.setStrokeColor(ColorStateList.valueOf(KEYBOARD_KEY_STROKE));
        btn.setTextColor(Color.parseColor("#1F2937"));
    }

    private void applyActionKeyStyle(MaterialButton btn) {
        btn.setBackgroundTintList(ColorStateList.valueOf(KEYBOARD_ACTION_BG));
        btn.setStrokeColor(ColorStateList.valueOf(KEYBOARD_ACTION_BG));
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
        if (!db.isValidWordleGuess(guess)) {
            Toast.makeText(this, "Tahmin İngilizce sözlükte olan 5 harfli bir kelime olmalı.", Toast.LENGTH_SHORT).show();
            return;
        }

        saveGuess(guess);
        fillRow(guess);

        boolean solved = guess.equals(targetWord);
        if (solved || currentAttempt == MAX_ATTEMPTS) {
            isGameOver = true;
            markFinished(solved);
            tvResult.setText("Doğru Kelime: " + targetWord);
            tvResult.setTextColor(solved ? WORDLE_GREEN : WORDLE_RED);
            setKeyboardEnabled(false);
        } else {
            currentGuess.setLength(0);
        }
    }

    private void saveGuess(String guess) {
        SharedPreferences pref = getSharedPreferences(WORDLE_PREFS, MODE_PRIVATE);
        String saved = pref.getString(prefKey(selectedDate, "_guesses"), "");
        pref.edit().putString(prefKey(selectedDate, "_guesses"), saved + (saved.isEmpty() ? "" : ",") + guess).apply();
    }

    private void markFinished(boolean solved) {
        getSharedPreferences(WORDLE_PREFS, MODE_PRIVATE)
                .edit()
                .putBoolean(prefKey(selectedDate, "_finished"), true)
                .putString(prefKey(selectedDate, "_status"), solved ? STATUS_CORRECT : STATUS_FAILED)
                .apply();
    }

    private void saveGameStatus(String status) {
        getSharedPreferences(WORDLE_PREFS, MODE_PRIVATE)
                .edit()
                .putString(prefKey(selectedDate, "_status"), status)
                .apply();
    }

    private void clearSavedGame(String date) {
        getSharedPreferences(WORDLE_PREFS, MODE_PRIVATE)
                .edit()
                .remove(prefKey(date, "_word"))
                .remove(prefKey(date, "_guesses"))
                .remove(prefKey(date, "_finished"))
                .remove(prefKey(date, "_status"))
                .commit();
    }

    private void clearSavedAttempts(String date) {
        getSharedPreferences(WORDLE_PREFS, MODE_PRIVATE)
                .edit()
                .remove(prefKey(date, "_guesses"))
                .remove(prefKey(date, "_finished"))
                .remove(prefKey(date, "_status"))
                .commit();
    }

    private void resetSavedGame(String date) {
        String freshWord = chooseWordleWord();
        SharedPreferences.Editor editor = getSharedPreferences(WORDLE_PREFS, MODE_PRIVATE)
                .edit()
                .remove(prefKey(date, "_word"))
                .remove(prefKey(date, "_guesses"))
                .remove(prefKey(date, "_finished"))
                .remove(prefKey(date, "_status"));
        if (isValidWord(freshWord)) {
            editor.putString(prefKey(date, "_word"), freshWord);
        }
        editor.commit();
    }

    private String chooseWordleWord() {
        String learnedWord = normalizeWord(db.getRandomWordForWordle(AppSettings.getCorrectWordIds(this)));
        if (isValidWord(learnedWord)) {
            return learnedWord;
        }
        return normalizeWord(db.getRandomWordForWordle());
    }

    private String normalizeWord(String word) {
        if (word == null) return null;
        String cleanWord = word.trim().toUpperCase(Locale.US);
        return cleanWord.isEmpty() ? null : cleanWord;
    }

    private boolean isValidWord(String word) {
        return word != null && isValidWordleLength(word.length());
    }

    private boolean isValidWordleLength(int length) {
        return length == WORDLE_WORD_LENGTH;
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

    private String formatDateKey(long timeInMillis) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(timeInMillis));
    }

    private void normalizeToStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
