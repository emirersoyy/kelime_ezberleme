package com.example.kelimeezberleme;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity {
    DatabaseHelper db;
    List<Word> quizWords;
    ArrayList<IncorrectWord> incorrectWords = new ArrayList<>();
    int currentIndex = 0;
    int correctCount = 0;

    TextView tvEngWord, tvProgress, tvFeedback;
    ImageView ivQuizImage;
    
    MaterialCardView[] cardOptions = new MaterialCardView[4];
    TextView[] tvOptions = new TextView[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        int limit = getIntent().getIntExtra("limit", 10);
        db = new DatabaseHelper(this);
        quizWords = db.getWordsForQuiz(limit);

        if (quizWords == null || quizWords.isEmpty()) {
            Toast.makeText(this, "Sınav için yeterli kelime bulunamadı veya bugünlük bitti!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvEngWord = findViewById(R.id.tvEngWord);
        tvProgress = findViewById(R.id.tvProgress);
        tvFeedback = findViewById(R.id.tvFeedback);
        ivQuizImage = findViewById(R.id.ivQuizImage);

        // Şık Kartlarını Bağla
        cardOptions[0] = findViewById(R.id.cardOption1);
        cardOptions[1] = findViewById(R.id.cardOption2);
        cardOptions[2] = findViewById(R.id.cardOption3);
        cardOptions[3] = findViewById(R.id.cardOption4);

        // Şık Yazılarını Bağla
        tvOptions[0] = findViewById(R.id.tvOption1);
        tvOptions[1] = findViewById(R.id.tvOption2);
        tvOptions[2] = findViewById(R.id.tvOption3);
        tvOptions[3] = findViewById(R.id.tvOption4);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        for (int i = 0; i < 4; i++) {
            final int index = i;
            cardOptions[i].setOnClickListener(v -> checkAnswer(tvOptions[index].getText().toString(), cardOptions[index]));
        }

        showCurrentWord();
    }

    private void showCurrentWord() {
        if (currentIndex >= quizWords.size()) return;
        
        Word currentWord = quizWords.get(currentIndex);
        tvEngWord.setText(currentWord.eng);
        tvProgress.setText((currentIndex + 1) + "/" + quizWords.size());
        tvFeedback.setVisibility(View.INVISIBLE);
        
        for(MaterialCardView card : cardOptions) {
            card.setEnabled(true);
            card.setStrokeColor(getResources().getColor(R.color.primary));
            card.setCardBackgroundColor(getResources().getColor(R.color.surface));
        }

        if (currentWord.pic != null && !currentWord.pic.isEmpty()) {
            try {
                ivQuizImage.setImageURI(Uri.parse(currentWord.pic));
                ivQuizImage.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                ivQuizImage.setVisibility(View.GONE);
            }
        } else {
            ivQuizImage.setVisibility(View.GONE);
        }

        setupOptions(currentWord);
    }

    private void setupOptions(Word correctWord) {
        List<String> options = new ArrayList<>();
        options.add(correctWord.tur);
        List<String> wrongs = db.getRandomWrongAnswers(correctWord.id);
        options.addAll(wrongs);

        while (options.size() < 4) {
            options.add("---");
        }

        Collections.shuffle(options);

        for (int i = 0; i < 4; i++) {
            tvOptions[i].setText(options.get(i));
        }
    }

    private void checkAnswer(String selectedAnswer, MaterialCardView selectedCards) {
        Word currentWord = quizWords.get(currentIndex);

        for(MaterialCardView card : cardOptions) card.setEnabled(false);
        tvFeedback.setVisibility(View.VISIBLE);

        if (selectedAnswer.equalsIgnoreCase(currentWord.tur)) {
            tvFeedback.setText("Doğru! 🎉");
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            selectedCards.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            correctCount++;
            db.updateWordProgress(currentWord.id, currentWord.stepCount);
        } else {
            tvFeedback.setText("Kaydedildi...");
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            selectedCards.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            incorrectWords.add(new IncorrectWord(currentWord.eng, currentWord.tur, selectedAnswer));
            db.resetWordProgress(currentWord.id);
        }

        new Handler().postDelayed(() -> {
            currentIndex++;
            if (currentIndex < quizWords.size()) {
                showCurrentWord();
            } else {
                Intent intent = new Intent(QuizActivity.this, QuizResultActivity.class);
                intent.putExtra("correct", correctCount);
                intent.putExtra("total", quizWords.size());
                intent.putExtra("incorrects", incorrectWords);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }
}