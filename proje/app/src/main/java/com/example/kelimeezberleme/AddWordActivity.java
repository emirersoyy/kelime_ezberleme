package com.example.kelimeezberleme;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AddWordActivity extends AppCompatActivity {
    EditText etEngWord, etTurWord, etSamples, etCategory;
    Button btnSave, btnSelectImage;
    TextView tvImagePath;
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

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnSave.setOnClickListener(v -> saveWord());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
}
