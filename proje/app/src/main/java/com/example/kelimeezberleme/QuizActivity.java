package com.example.kelimeezberleme;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class QuizActivity extends BottomNavActivity {
    private static final int BUBBLE_SIZE_DP = 18;
    private static final int RING_SIZE_DP = 30;
    private static final int MIN_BUBBLE_GAP_DP = 3;
    private static final int MAX_BUBBLE_GAP_DP = 7;

    DatabaseHelper db;
    List<QuizQuestion> quizQuestions = new ArrayList<>();
    List<QuizQuestion> reviewQuestions = new ArrayList<>();
    ArrayList<IncorrectWord> incorrectWords = new ArrayList<>();
    ArrayList<Integer> usedSampleIds = new ArrayList<>();

    int currentIndex = 0;
    int correctCount = 0;
    boolean reviewMode = false;
    boolean slowPronunciation = false;
    boolean ttsReady = false;

    TextView tvEngWord, tvFeedback, tvSampleSentence, tvPronunciation, tvReviewTitle;
    TextView tvLevel;
    ImageView ivQuizImage;
    ImageButton btnPronunciation, btnSlowPronunciation;
    LinearLayout bubbleContainer;
    HorizontalScrollView progressScroll;
    View progressFadeLeft, progressFadeRight;
    View currentBubbleRing;
    View vQuizBottomSpacer;
    final List<View> bubbleViews = new ArrayList<>();
    final Handler handler = new Handler(Looper.getMainLooper());
    TextToSpeech textToSpeech;

    MaterialCardView[] cardOptions = new MaterialCardView[4];
    TextView[] tvOptions = new TextView[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        int limit = getIntent().getIntExtra("limit", 10);
        db = new DatabaseHelper(this);
        List<Word> words = db.getWordsForQuiz(limit);

        if (words == null || words.isEmpty()) {
            Toast.makeText(this, "S\u0131nav i\u00e7in uygun kelime bulunamad\u0131.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        for (int i = 0; i < words.size(); i++) {
            DatabaseHelper.SampleSentence sample = db.getNextUnusedSampleForWord(words.get(i).id);
            QuizQuestion question = new QuizQuestion(words.get(i), sample, i);
            quizQuestions.add(question);
            if (sample != null) usedSampleIds.add(sample.id);
        }

        bindViews();
        setupProgressBubbles();
        setupTextToSpeech();
        showCurrentQuestion(false);
    }

    private void bindViews() {
        tvEngWord = findViewById(R.id.tvEngWord);
        tvFeedback = findViewById(R.id.tvFeedback);
        tvSampleSentence = findViewById(R.id.tvSampleSentence);
        tvPronunciation = findViewById(R.id.tvPronunciation);
        tvLevel = findViewById(R.id.tvLevel);
        tvReviewTitle = findViewById(R.id.tvReviewTitle);
        ivQuizImage = findViewById(R.id.ivQuizImage);
        btnPronunciation = findViewById(R.id.btnPronunciation);
        btnSlowPronunciation = findViewById(R.id.btnSlowPronunciation);
        bubbleContainer = findViewById(R.id.bubbleContainer);
        progressScroll = findViewById(R.id.progressScroll);
        progressFadeLeft = findViewById(R.id.progressFadeLeft);
        progressFadeRight = findViewById(R.id.progressFadeRight);
        currentBubbleRing = findViewById(R.id.currentBubbleRing);
        vQuizBottomSpacer = findViewById(R.id.vQuizBottomSpacer);
        updateBottomSpacer(0);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (view, insets) -> {
            int navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            updateBottomSpacer(navBottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(findViewById(android.R.id.content));

        progressScroll.setOnScrollChangeListener((view, scrollX, scrollY, oldScrollX, oldScrollY) -> updateProgressFade());

        cardOptions[0] = findViewById(R.id.cardOption1);
        cardOptions[1] = findViewById(R.id.cardOption2);
        cardOptions[2] = findViewById(R.id.cardOption3);
        cardOptions[3] = findViewById(R.id.cardOption4);

        tvOptions[0] = findViewById(R.id.tvOption1);
        tvOptions[1] = findViewById(R.id.tvOption2);
        tvOptions[2] = findViewById(R.id.tvOption3);
        tvOptions[3] = findViewById(R.id.tvOption4);

        btnPronunciation.setOnClickListener(v -> {
            btnSlowPronunciation.setVisibility(View.VISIBLE);
            playPronunciation(slowPronunciation);
        });
        btnSlowPronunciation.setOnClickListener(v -> {
            slowPronunciation = !slowPronunciation;
            updateSlowButtonState();
        });

        for (int i = 0; i < 4; i++) {
            final int index = i;
            cardOptions[i].setOnClickListener(v -> checkAnswer(tvOptions[index].getText().toString(), cardOptions[index]));
        }
    }

    private void updateBottomSpacer(int navBottomPx) {
        if (vQuizBottomSpacer == null) return;
        int spacerHeight = getBottomNavBarHeightPx() + (navBottomPx * 2);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                spacerHeight
        );
        vQuizBottomSpacer.setLayoutParams(params);
    }

    private void setupTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
                ttsReady = true;
                playPronunciation(false);
            }
        });
    }

    private void setupProgressBubbles() {
        bubbleContainer.removeAllViews();
        bubbleViews.clear();
        progressScroll.post(() -> {
            int count = quizQuestions.size();
            int availableWidth = progressScroll.getWidth() - dp(8);
            int bubbleSize = dp(BUBBLE_SIZE_DP);
            int minGap = dp(MIN_BUBBLE_GAP_DP);
            int maxGap = dp(MAX_BUBBLE_GAP_DP);
            int sideGap = maxGap;
            int contentWidth = count * bubbleSize + count * sideGap * 2;

            if (count > 0 && contentWidth > availableWidth) {
                sideGap = Math.max(minGap, (availableWidth - (count * bubbleSize)) / (count * 2));
                contentWidth = count * bubbleSize + count * sideGap * 2;
            }

            int startPadding = Math.max(0, (availableWidth - contentWidth) / 2);
            bubbleContainer.setPadding(startPadding, 0, startPadding, 0);

            for (int i = 0; i < count; i++) {
                View bubble = new View(this);
                bubble.setBackground(makeBubbleDrawable(Color.rgb(203, 213, 225)));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(bubbleSize, bubbleSize);
                params.setMargins(sideGap, 0, sideGap, 0);
                bubbleContainer.addView(bubble, params);
                bubbleViews.add(bubble);
            }

            FrameLayout.LayoutParams ringParams = new FrameLayout.LayoutParams(dp(RING_SIZE_DP), dp(RING_SIZE_DP));
            ringParams.gravity = android.view.Gravity.CENTER_VERTICAL;
            currentBubbleRing.setLayoutParams(ringParams);
            bubbleContainer.post(() -> moveRingToBubble(0, false));
            updateProgressFade();
        });
    }

    private void updateProgressFade() {
        if (progressScroll == null || progressFadeLeft == null || progressFadeRight == null) return;
        boolean canScrollLeft = progressScroll.canScrollHorizontally(-1);
        boolean canScrollRight = progressScroll.canScrollHorizontally(1);

        progressFadeLeft.setVisibility(canScrollLeft ? View.VISIBLE : View.GONE);
        progressFadeRight.setVisibility(canScrollRight ? View.VISIBLE : View.GONE);
    }

    private void showCurrentQuestion(boolean animateRing) {
        List<QuizQuestion> activeQuestions = getActiveQuestions();
        if (currentIndex >= activeQuestions.size()) return;

        QuizQuestion question = activeQuestions.get(currentIndex);
        Word currentWord = question.word;
        tvReviewTitle.setVisibility(reviewMode ? View.VISIBLE : View.GONE);
        tvEngWord.setText(currentWord.eng);
        tvPronunciation.setText(getPronunciationText(currentWord.eng));
        tvLevel.setText(getLevelText(currentWord.stepCount));
        tvFeedback.setVisibility(View.INVISIBLE);
        resetPronunciationControls();

        for (MaterialCardView card : cardOptions) {
            card.setEnabled(true);
            card.setStrokeColor(getResources().getColor(R.color.primary));
            card.setCardBackgroundColor(getResources().getColor(R.color.surface));
        }

        WordImageLoader.load(ivQuizImage, currentWord.pic);
        if (question.sampleText == null || question.sampleText.isEmpty()) {
            tvSampleSentence.setVisibility(View.GONE);
        } else {
            tvSampleSentence.setText(question.sampleText);
            tvSampleSentence.setVisibility(View.VISIBLE);
        }

        setupOptions(currentWord);
        moveRingToBubble(question.originalIndex, animateRing);
        playPronunciation(false);
    }

    private List<QuizQuestion> getActiveQuestions() {
        return reviewMode ? reviewQuestions : quizQuestions;
    }

    private void setupOptions(Word correctWord) {
        List<String> options = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        options.add(correctWord.tur);
        seen.add(correctWord.tur.toLowerCase());

        for (String wrong : db.getRandomWrongAnswers(correctWord.id)) {
            if (wrong != null && seen.add(wrong.toLowerCase())) {
                options.add(wrong);
            }
        }

        while (options.size() < 4) {
            options.add("---");
        }

        Collections.shuffle(options);
        for (int i = 0; i < 4; i++) {
            tvOptions[i].setText(options.get(i));
        }
    }

    private void checkAnswer(String selectedAnswer, MaterialCardView selectedCard) {
        QuizQuestion question = getActiveQuestions().get(currentIndex);
        Word currentWord = question.word;
        boolean isCorrect = selectedAnswer.equalsIgnoreCase(currentWord.tur);

        for (MaterialCardView card : cardOptions) card.setEnabled(false);
        tvFeedback.setVisibility(View.VISIBLE);

        if (isCorrect) {
            tvFeedback.setText(reviewMode ? "Bu kez do\u011fru!" : "Do\u011fru!");
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            selectedCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            setBubbleColor(question.originalIndex, Color.rgb(34, 197, 94));
            question.everAnsweredCorrect = true;
            if (!reviewMode && !question.everWrong) {
                question.answeredCorrect = true;
                correctCount++;
            }
        } else {
            tvFeedback.setText(reviewMode ? "Tekrar not edildi." : "Yanl\u0131\u015f.");
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            selectedCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            markCorrectOption(currentWord.tur);
            question.everWrong = true;
            if (!reviewMode) {
                question.answeredCorrect = false;
                reviewQuestions.add(question);
                incorrectWords.add(new IncorrectWord(currentWord.eng, currentWord.tur, selectedAnswer, question.sampleText));
                setBubbleColor(question.originalIndex, Color.rgb(239, 68, 68));
            }
        }

        handler.postDelayed(() -> {
            currentIndex++;
            if (currentIndex < getActiveQuestions().size()) {
                showCurrentQuestion(true);
            } else if (!reviewMode && !reviewQuestions.isEmpty()) {
                reviewMode = true;
                currentIndex = 0;
                showCurrentQuestion(true);
            } else {
                finishQuiz();
            }
        }, 900);
    }

    private void markCorrectOption(String correctAnswer) {
        for (int i = 0; i < tvOptions.length; i++) {
            if (tvOptions[i].getText().toString().equalsIgnoreCase(correctAnswer)) {
                cardOptions[i].setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                return;
            }
        }
    }

    private void finishQuiz() {
        for (QuizQuestion question : quizQuestions) {
            boolean shouldAdvance = question.answeredCorrect && !question.everWrong;
            db.updateWordProgress(question.word.id, question.word.stepCount, shouldAdvance);
            if (shouldAdvance) {
                AppSettings.recordCorrectWord(this, question.word.id);
            }
        }
        db.markSamplesUsed(usedSampleIds);

        Intent intent = new Intent(QuizActivity.this, QuizResultActivity.class);
        intent.putExtra("correct", correctCount);
        intent.putExtra("total", quizQuestions.size());
        intent.putExtra("incorrects", incorrectWords);
        startActivity(intent);
        finish();
    }

    private void confirmExit() {
        new AlertDialog.Builder(this)
                .setTitle("Sınavdan çıkılsın mı?")
                .setMessage("Bu testteki ilerlemen kaybolacak, sınavdan çıkmak istediğine emin misin?")
                .setNegativeButton("Vazgeç", null)
                .setPositiveButton("Evet", (dialog, which) -> finish())
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        confirmExit();
    }

    private void resetPronunciationControls() {
        slowPronunciation = false;
        btnSlowPronunciation.setVisibility(View.GONE);
        updateSlowButtonState();
    }

    private void updateSlowButtonState() {
        btnSlowPronunciation.setSelected(slowPronunciation);
        btnSlowPronunciation.setColorFilter(getResources().getColor(slowPronunciation ? R.color.primary : R.color.text_secondary));
    }

    private void playPronunciation(boolean slow) {
        if (!ttsReady || textToSpeech == null) return;
        QuizQuestion question = getActiveQuestions().isEmpty() ? null : getActiveQuestions().get(Math.min(currentIndex, getActiveQuestions().size() - 1));
        if (question == null) return;
        textToSpeech.setSpeechRate(slow ? 0.45f : 1.0f);
        textToSpeech.setPitch(1.0f);
        textToSpeech.speak(question.word.eng, TextToSpeech.QUEUE_FLUSH, null, "word_" + question.word.id);
    }

    private void moveRingToBubble(int bubbleIndex, boolean animate) {
        if (bubbleIndex < 0 || bubbleIndex >= bubbleViews.size()) return;
        View bubble = bubbleViews.get(bubbleIndex);
        float targetX = bubbleContainer.getLeft() + bubble.getLeft() + (bubble.getWidth() / 2f) - (dp(RING_SIZE_DP) / 2f);
        if (animate) {
            currentBubbleRing.animate().cancel();
            currentBubbleRing.animate().x(targetX).setDuration(260).start();
        } else {
            currentBubbleRing.setX(targetX);
        }
        progressScroll.post(() -> {
            int scrollX = (int) Math.max(0, targetX - (progressScroll.getWidth() / 2f) + (dp(RING_SIZE_DP) / 2f));
            progressScroll.smoothScrollTo(scrollX, 0);
        });
    }

    private void setBubbleColor(int bubbleIndex, int color) {
        if (bubbleIndex < 0 || bubbleIndex >= bubbleViews.size()) return;
        bubbleViews.get(bubbleIndex).setBackground(makeBubbleDrawable(color));
    }

    @NonNull
    private GradientDrawable makeBubbleDrawable(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @NonNull
    private String getLevelText(int stepCount) {
        if (stepCount <= 0) {
            return "Seviye 0";
        }
        return "Seviye " + stepCount;
    }

    @NonNull
    private String getPronunciationText(@NonNull String word) {
        switch (word) {
            case "Apple": return "/AP-uhl/";
            case "Book": return "/buuk/";
            case "Computer": return "/kuhm-PYOO-ter/";
            case "Water": return "/WAW-ter/";
            case "School": return "/skool/";
            case "Pen": return "/pen/";
            case "Door": return "/dor/";
            case "Window": return "/WIN-doh/";
            case "Table": return "/TAY-buhl/";
            case "Chair": return "/chair/";
            case "Friend": return "/frend/";
            case "Family": return "/FAM-uh-lee/";
            case "Heart": return "/hart/";
            case "Sun": return "/sun/";
            case "Moon": return "/moon/";
            case "Star": return "/star/";
            case "Time": return "/tym/";
            case "City": return "/SIT-ee/";
            case "Country": return "/KUN-tree/";
            case "Money": return "/MUN-ee/";
            case "Work": return "/wurk/";
            case "Sleep": return "/sleep/";
            case "Happy": return "/HAP-ee/";
            case "Sad": return "/sad/";
            case "Beautiful": return "/BYOO-tuh-fuhl/";
            case "Big": return "/big/";
            case "Small": return "/smawl/";
            case "New": return "/noo/";
            case "Old": return "/ohld/";
            case "Good": return "/guud/";
            case "Bad": return "/bad/";
            case "Fast": return "/fast/";
            case "Slow": return "/sloh/";
            case "Hot": return "/hot/";
            case "Cold": return "/kohld/";
            case "Easy": return "/EE-zee/";
            case "Hard": return "/hard/";
            case "Read": return "/reed/";
            case "Write": return "/ryt/";
            case "Listen": return "/LIS-uhn/";
            case "Speak": return "/speek/";
            case "Run": return "/run/";
            case "Walk": return "/wawk/";
            case "Eat": return "/eet/";
            case "Drink": return "/drink/";
            case "Language": return "/LANG-gwij/";
            case "Bird": return "/burd/";
            case "Dog": return "/dawg/";
            case "Cat": return "/kat/";
            case "Flower": return "/FLOW-er/";
            default: return "/" + word.toLowerCase(Locale.US) + "/";
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private static class QuizQuestion {
        final Word word;
        final String sampleText;
        final int sampleId;
        final int originalIndex;
        boolean answeredCorrect = false;
        boolean everAnsweredCorrect = false;
        boolean everWrong = false;

        QuizQuestion(Word word, DatabaseHelper.SampleSentence sample, int originalIndex) {
            this.word = word;
            this.sampleText = sample == null ? "" : sample.text;
            this.sampleId = sample == null ? -1 : sample.id;
            this.originalIndex = originalIndex;
        }
    }
}
