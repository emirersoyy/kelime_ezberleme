package com.example.kelimeezberleme;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AddWordActivity extends BottomNavActivity {
    private static final int MAX_WORD_LENGTH = 25;
    private static final long MAX_IMAGE_SIZE_BYTES = 2L * 1024L * 1024L;
    EditText etEngWord, etTurWord, etSamples, etCategory;
    Button btnSave, btnSelectImage;
    TextView tvImagePath, tvEngWordCount, tvTurWordCount, tvCategoryCount;
    DatabaseHelper db;
    String selectedImagePath = "";
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        db = new DatabaseHelper(this);
        etEngWord = findViewById(R.id.etEngWord);
        etTurWord = findViewById(R.id.etTurWord);
        etSamples = findViewById(R.id.etSamples);
        etCategory = findViewById(R.id.etCategory);
        btnSave = findViewById(R.id.btnSaveWord);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        tvImagePath = findViewById(R.id.tvImagePath);
        tvEngWordCount = findViewById(R.id.tvEngWordCount);
        tvTurWordCount = findViewById(R.id.tvTurWordCount);
        tvCategoryCount = findViewById(R.id.tvCategoryCount);

        etEngWord.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_WORD_LENGTH)});
        etTurWord.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_WORD_LENGTH)});
        etCategory.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_WORD_LENGTH)});

        attachCounter(etEngWord, tvEngWordCount);
        attachCounter(etTurWord, tvTurWordCount);
        attachCounter(etCategory, tvCategoryCount);
        updateCounter(tvEngWordCount, etEngWord);
        updateCounter(tvTurWordCount, etTurWord);
        updateCounter(tvCategoryCount, etCategory);

        btnSelectImage.setOnClickListener(v -> selectImage());
        btnSave.setOnClickListener(v -> saveWord());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void saveWord() {
        String eng = etEngWord.getText().toString().trim();
        String tur = etTurWord.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String samplesText = etSamples.getText().toString().trim();

        if (eng.isEmpty() || tur.isEmpty()) {
            Toast.makeText(this, "Lütfen İngilizce ve Türkçe alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eng.length() > MAX_WORD_LENGTH || tur.length() > MAX_WORD_LENGTH) {
            Toast.makeText(this, "Kelime uzunluğu en fazla " + MAX_WORD_LENGTH + " harf olabilir.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImagePath != null && !selectedImagePath.isEmpty() && !isImageWithinLimit(Uri.parse(selectedImagePath))) {
            Toast.makeText(this, "Fotoğraf en fazla 2 MB olabilir.", Toast.LENGTH_SHORT).show();
            return;
        }

        long wordId = db.addWord(eng, tur, selectedImagePath, category);
        if (wordId == -1) {
            Toast.makeText(this, "Hata oluştu", Toast.LENGTH_SHORT).show();
            return;
        }

        saveSamples(wordId, samplesText);
        Toast.makeText(this, "Kelime başarıyla eklendi", Toast.LENGTH_SHORT).show();
        clearFields();
    }

    private void saveSamples(long wordId, String samplesText) {
        if (samplesText.isEmpty()) {
            return;
        }
        String[] samplesArray = samplesText.split(",");
        for (String sample : samplesArray) {
            String cleanedSample = sample.trim();
            if (!cleanedSample.isEmpty()) {
                db.addSample(wordId, cleanedSample);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
                // Some providers do not offer persistable permissions.
            }

            if (!isImageWithinLimit(imageUri)) {
                selectedImagePath = "";
                tvImagePath.setText("Resim seçilmedi");
                Toast.makeText(this, "Seçilen fotoğraf 2 MB'den büyük. Daha küçük bir dosya seç.", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedImagePath = imageUri.toString();
            tvImagePath.setText("Resim seçildi");
        }
    }

    private void clearFields() {
        etEngWord.setText("");
        etTurWord.setText("");
        etCategory.setText("");
        etSamples.setText("");
        tvImagePath.setText("Resim seçilmedi");
        selectedImagePath = "";
    }

    private void attachCounter(EditText editText, TextView counterView) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCounter(counterView, editText);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void updateCounter(TextView counterView, EditText editText) {
        int length = editText.getText() == null ? 0 : editText.getText().toString().length();
        counterView.setText(length + "/" + MAX_WORD_LENGTH);
    }

    private boolean isImageWithinLimit(Uri uri) {
        if (uri == null) {
            return false;
        }

        long sizeBytes = -1L;
        try (AssetFileDescriptor afd = getContentResolver().openAssetFileDescriptor(uri, "r")) {
            if (afd != null) {
                sizeBytes = afd.getLength();
            }
        } catch (Exception ignored) {
            // Fallback to stream counting below.
        }

        if (sizeBytes >= 0) {
            return sizeBytes <= MAX_IMAGE_SIZE_BYTES;
        }

        long count = 0L;
        byte[] buffer = new byte[8 * 1024];
        try (java.io.InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) {
                return false;
            }
            int read;
            while ((read = in.read(buffer)) != -1) {
                count += read;
                if (count > MAX_IMAGE_SIZE_BYTES) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
