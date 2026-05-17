package com.example.kelimeezberleme;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AiAssistantActivity extends BottomNavActivity {
    private static final String TAG = "AiAssistantActivity";
    private static final String TEXT_ENDPOINT = "https://text.pollinations.ai/openai";
    private static final String IMAGE_ENDPOINT = "https://image.pollinations.ai/prompt/";
    private static final String SYSTEM_PROMPT =
            "Sen Türkçe konuşan bir kelime hikayesi üreticisisin. Sana verilen 5 İngilizce kelimeyi " +
            "tam bu sırayla, her biri hikayede görünür olacak şekilde kısa ve akıcı bir Türkçe hikayede kullan. " +
            "Hikaye tek paragraf olsun, doğal dursun ve 2-4 cümleyi geçmesin. " +
            "Markdown kullanma, kalın yazı, yıldız, başlık ve madde işareti ekleme.";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private DatabaseHelper db;
    private LinearLayout wordsContainer;
    private TextView tvStory;
    private ImageView ivStoryImage;
    private TextView tvStatus;
    private ProgressBar progressBar;
    private MaterialButton btnGenerate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_assistant);

        db = new DatabaseHelper(this);
        wordsContainer = findViewById(R.id.wordsContainer);
        tvStory = findViewById(R.id.tvStory);
        ivStoryImage = findViewById(R.id.ivStoryImage);
        tvStatus = findViewById(R.id.tvStatus);
        progressBar = findViewById(R.id.progressBar);
        btnGenerate = findViewById(R.id.btnGenerate);

        btnGenerate.setOnClickListener(v -> generateStoryChain());
        restoreSavedStory();
    }

    private void restoreSavedStory() {
        String savedWords = AppSettings.getWordChainWords(this);
        String savedStory = AppSettings.getWordChainStory(this);
        String imagePath = AppSettings.getWordChainImagePath(this);

        if (!TextUtils.isEmpty(savedWords)) {
            showWords(parseWords(savedWords));
        }
        if (!TextUtils.isEmpty(savedStory)) {
            tvStory.setText(savedStory);
        }
        if (!TextUtils.isEmpty(imagePath) && new File(imagePath).exists()) {
            Glide.with(this).load(new File(imagePath)).into(ivStoryImage);
            tvStatus.setText("Son hikaye yüklendi");
        } else {
            tvStatus.setText("5 kelime seçip hikaye oluştur");
        }
    }

    private void generateStoryChain() {
        executor.execute(() -> {
            try {
                List<Word> words = pickRandomWords(5);
                if (words.size() < 5) {
                    mainHandler.post(() -> Toast.makeText(this, "Hikaye için yeterli kelime yok.", Toast.LENGTH_SHORT).show());
                    return;
                }

                String prompt = buildStoryPrompt(words);
                String story = requestText(prompt);
                File imageFile = saveGeneratedImage(story, words);

                String wordsText = joinWords(words);
                AppSettings.saveWordChainState(this, wordsText, story, imageFile.getAbsolutePath());

                mainHandler.post(() -> {
                    showWords(words);
                    tvStory.setText(story);
                    Glide.with(this).load(imageFile).into(ivStoryImage);
                    tvStatus.setText("Hikaye ve görsel kaydedildi");
                });
            } catch (Exception e) {
                Log.e(TAG, "Story chain generation failed", e);
                mainHandler.post(() -> {
                    tvStatus.setText("Hikaye oluşturulamadı");
                    Toast.makeText(this, e.getMessage() == null ? "Hikaye oluşturulamadı." : e.getMessage(), Toast.LENGTH_LONG).show();
                });
            } finally {
                mainHandler.post(() -> setLoading(false));
            }
        });
        setLoading(true);
        tvStatus.setText("Oluşturuluyor...");
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGenerate.setEnabled(!loading);
    }

    @NonNull
    private List<Word> pickRandomWords(int count) {
        List<Word> allWords = WordleWordBank.mergeDisplayWords(db.getAllWords());
        List<Word> pool = new ArrayList<>();
        for (Word word : allWords) {
            if (word != null && !TextUtils.isEmpty(word.eng)) {
                pool.add(word);
            }
        }
        Collections.shuffle(pool);
        if (pool.size() > count) {
            return new ArrayList<>(pool.subList(0, count));
        }
        return pool;
    }

    private void showWords(List<Word> words) {
        wordsContainer.removeAllViews();
        for (int i = 0; i < words.size(); i++) {
            TextView chip = new TextView(this);
            chip.setText(String.format(Locale.US, "%d. %s", i + 1, words.get(i).eng));
            chip.setTextColor(getResources().getColor(R.color.primary));
            chip.setTextSize(13f);
            chip.setPadding(dp(10), dp(6), dp(10), dp(6));
            chip.setBackgroundResource(R.drawable.soft_chip_bg);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.rightMargin = dp(8);
            params.bottomMargin = dp(8);
            wordsContainer.addView(chip, params);
        }
    }

    private String joinWords(List<Word> words) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) builder.append(" | ");
            builder.append(words.get(i).eng);
        }
        return builder.toString();
    }

    private List<Word> parseWords(String savedWords) {
        List<Word> words = new ArrayList<>();
        String[] parts = savedWords.split("\\|");
        int id = 0;
        for (String part : parts) {
            String clean = part.trim();
            if (!clean.isEmpty()) {
                words.add(new Word(++id, clean, "", ""));
            }
        }
        return words;
    }

    private String buildStoryPrompt(List<Word> words) {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) list.append(", ");
            list.append(words.get(i).eng);
        }
        return "Aşağıdaki 5 kelimeyi sırayla kullanarak kısa bir Türkçe hikaye yaz: " +
                list +
                ". Hikaye doğal, akıcı ve tek paragraf olsun. Kelimeler görünür şekilde geçsin. " +
                "Markdown kullanma, ** veya * ile biçimlendirme yapma.";
    }

    @NonNull
    private String requestText(String prompt) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(TEXT_ENDPOINT);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);

            JSONObject body = new JSONObject();
            body.put("model", "openai");
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", SYSTEM_PROMPT));
            messages.put(new JSONObject().put("role", "user").put("content", prompt));
            body.put("messages", messages);
            body.put("temperature", 0.8);
            body.put("max_tokens", 450);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = connection.getResponseCode();
            String response = readResponse(connection, code);

            if (code < 200 || code >= 300) {
                throw new IllegalStateException("Pollinations metin isteği başarısız oldu. HTTP " + code + ": " + response);
            }

            return extractMessage(response);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @NonNull
    private File saveGeneratedImage(String story, List<Word> words) throws Exception {
        String imagePrompt = buildImagePrompt(story, words);
        String encodedPrompt = URLEncoder.encode(imagePrompt, StandardCharsets.UTF_8.name());
        String imageUrl = IMAGE_ENDPOINT + encodedPrompt + "?width=1024&height=1024&nologo=true";

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            connection = (HttpURLConnection) new URL(imageUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            int code = connection.getResponseCode();
            inputStream = new BufferedInputStream(
                    code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream()
            );

            if (code < 200 || code >= 300) {
                throw new IllegalStateException("Pollinations görsel isteği başarısız oldu. HTTP " + code + ": " + readStream(inputStream));
            }

            File dir = new File(getFilesDir(), "word_chains");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File imageFile = new File(dir, "chain_" + System.currentTimeMillis() + ".png");
            try (FileOutputStream out = new FileOutputStream(imageFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
            return imageFile;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @NonNull
    private String buildImagePrompt(String story, List<Word> words) {
        StringBuilder wordLine = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) wordLine.append(", ");
            wordLine.append(words.get(i).eng);
        }
        return "Create a single, clear, colorful storybook illustration for this Turkish story. " +
                "Show one readable scene with the 5 words represented naturally: " + wordLine +
                ". Story: " + story +
                ". Clean composition, no collage, no text in image, no blur, no extra objects, high detail.";
    }

    @NonNull
    private String extractMessage(String raw) {
        try {
            JSONObject json = new JSONObject(raw);
            if (json.has("choices")) {
                JSONArray choices = json.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    if (choice.has("message")) {
                        JSONObject message = choice.getJSONObject("message");
                        if (message.has("content")) {
                            return message.getString("content").trim();
                        }
                    }
                    if (choice.has("text")) {
                        return choice.getString("text").trim();
                    }
                }
            }
            if (json.has("response")) {
                return json.getString("response").trim();
            }
            if (json.has("content")) {
                return json.getString("content").trim();
            }
        } catch (Exception ignored) {
        }
        return raw == null || raw.trim().isEmpty() ? "Boş cevap döndü." : cleanStoryText(raw.trim());
    }

    @NonNull
    private String cleanStoryText(String text) {
        String cleaned = text
                .replace("**", "")
                .replace("__", "")
                .replace("*", "")
                .replace("`", "")
                .replaceAll("(?m)^#+\\s*", "")
                .replaceAll("(?m)^-\\s*", "")
                .replaceAll("(?m)^\\d+\\.\\s*", "");
        return cleaned.trim();
    }

    @NonNull
    private String readResponse(HttpURLConnection connection, int code) throws Exception {
        InputStream stream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
        return readStream(stream);
    }

    @NonNull
    private String readStream(InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
