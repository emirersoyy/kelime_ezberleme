package com.example.kelimeezberleme;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AnalysisActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private LinearLayout llSummaryChips;
    private LinearLayout llCategoryStats;
    private TextView tvOverallStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        db = new DatabaseHelper(this);
        llSummaryChips = findViewById(R.id.llSummaryChips);
        llCategoryStats = findViewById(R.id.llCategoryStats);
        tvOverallStats = findViewById(R.id.tvOverallStats);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnPrint).setOnClickListener(v -> printReport());
        findViewById(R.id.btnResetData).setOnClickListener(v -> confirmReset());

        calculateAndShowStats();
    }

    private void calculateAndShowStats() {
        llSummaryChips.removeAllViews();
        llCategoryStats.removeAllViews();

        List<Word> words = db.getAllWords();
        if (words.isEmpty()) {
            tvOverallStats.setText("Henüz kelime eklenmemiş.");
            addEmptyState("Gösterilecek veri yok.");
            return;
        }

        Collections.sort(words, Comparator.comparing(w -> w.eng == null ? "" : w.eng.toLowerCase()));

        List<Word> notStarted = new ArrayList<>();
        List<Word> learning = new ArrayList<>();
        List<Word> learned = new ArrayList<>();

        for (Word word : words) {
            if (word.stepCount <= 0) {
                notStarted.add(word);
            } else if (word.stepCount < 6) {
                learning.add(word);
            } else {
                learned.add(word);
            }
        }

        tvOverallStats.setText("Toplam " + words.size()
                + " kelime • Başlanmamış " + notStarted.size()
                + " • Öğrenilmekte " + learning.size()
                + " • Öğrenilmiş " + learned.size());

        addMetricChip("Toplam", String.valueOf(words.size()), R.color.primary);
        addMetricChip("Öğreniliyor", String.valueOf(learning.size()), R.color.accent);
        addMetricChip("Öğrenildi", String.valueOf(learned.size()), R.color.success);

        addStatusSection("Öğrenilmekte Olan", learning, R.color.accent);
        addStatusSection("Öğrenilmiş", learned, R.color.success);
        addStatusSection("Daha Başlanmamış", notStarted, R.color.text_secondary);
    }

    private void addMetricChip(String label, String value, int colorResId) {
        int color = getResources().getColor(colorResId);
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        cardParams.rightMargin = 10;
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(getResources().getColor(R.color.surface));
        card.setCardElevation(dp(2f));
        card.setRadius(dp(18));
        card.setStrokeColor(getResources().getColor(R.color.divider));
        card.setStrokeWidth(1);
        card.setUseCompatPadding(true);
        card.setPreventCornerOverlap(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(18, 16, 18, 16);

        TextView valueText = new TextView(this);
        valueText.setText(value);
        valueText.setTextColor(color);
        valueText.setTextSize(22);
        valueText.setTypeface(Typeface.DEFAULT_BOLD);

        TextView labelText = new TextView(this);
        labelText.setText(label);
        labelText.setTextColor(getResources().getColor(R.color.text_primary));
        labelText.setTextSize(13);

        content.addView(valueText);
        content.addView(labelText);
        card.addView(content);
        llSummaryChips.addView(card);
    }

    private void addEmptyState(String message) {
        TextView text = new TextView(this);
        text.setText(message);
        text.setTextColor(getResources().getColor(R.color.text_secondary));
        text.setTextSize(15);
        text.setPadding(0, 12, 0, 12);
        llCategoryStats.addView(text);
    }

    private void addStatusSection(String title, List<Word> items, int accentColorResId) {
        int accentColor = getResources().getColor(accentColorResId);

        MaterialCardView sectionCard = new MaterialCardView(this);
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sectionParams.bottomMargin = 14;
        sectionCard.setLayoutParams(sectionParams);
        sectionCard.setCardBackgroundColor(getResources().getColor(R.color.surface));
        sectionCard.setCardElevation(dp(2f));
        sectionCard.setRadius(dp(18));
        sectionCard.setStrokeColor(getResources().getColor(R.color.divider));
        sectionCard.setStrokeWidth(1);
        sectionCard.setUseCompatPadding(true);
        sectionCard.setPreventCornerOverlap(true);

        LinearLayout sectionContent = new LinearLayout(this);
        sectionContent.setOrientation(LinearLayout.VERTICAL);
        sectionContent.setPadding(18, 16, 18, 10);

        TextView header = new TextView(this);
        header.setText(title + " (" + items.size() + ")");
        header.setTextColor(accentColor);
        header.setTextSize(16);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        sectionContent.addView(header);

        if (items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Bu bölümde kelime yok.");
            empty.setTextColor(getResources().getColor(R.color.text_secondary));
            empty.setTextSize(14);
            empty.setPadding(0, 10, 0, 4);
            sectionContent.addView(empty);
            sectionCard.addView(sectionContent);
            llCategoryStats.addView(sectionCard);
            return;
        }

        TextView subtitle = new TextView(this);
        subtitle.setText("İngilizce • Türkçe • Kategori • Seviye");
        subtitle.setTextColor(getResources().getColor(R.color.text_secondary));
        subtitle.setTextSize(12);
        subtitle.setPadding(0, 2, 0, 12);
        sectionContent.addView(subtitle);

        for (Word word : items) {
            sectionContent.addView(createWordCard(word, accentColor));
        }

        sectionCard.addView(sectionContent);
        llCategoryStats.addView(sectionCard);
    }

    private MaterialCardView createWordCard(Word word, int accentColor) {
        String category = word.category == null || word.category.trim().isEmpty() ? "Genel" : word.category.trim();
        String english = word.eng == null || word.eng.trim().isEmpty() ? "-" : word.eng.trim();
        String turkish = word.tur == null || word.tur.trim().isEmpty() ? "-" : word.tur.trim();

        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = 10;
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(getResources().getColor(R.color.background));
        card.setCardElevation(dp(0.5f));
        card.setRadius(dp(14));
        card.setStrokeColor(getResources().getColor(R.color.divider));
        card.setStrokeWidth(1);
        card.setUseCompatPadding(true);
        card.setPreventCornerOverlap(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(16, 14, 16, 14);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView englishText = new TextView(this);
        englishText.setText(english);
        englishText.setTextColor(getResources().getColor(R.color.text_primary));
        englishText.setTextSize(16);
        englishText.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams englishParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        englishText.setLayoutParams(englishParams);

        TextView levelText = new TextView(this);
        levelText.setText("Seviye " + Math.max(word.stepCount, 0));
        levelText.setTextColor(accentColor);
        levelText.setTextSize(12);
        levelText.setTypeface(Typeface.DEFAULT_BOLD);
        levelText.setPadding(14, 8, 14, 8);
        levelText.setBackgroundResource(R.drawable.soft_chip_bg);

        topRow.addView(englishText);
        topRow.addView(levelText);

        TextView turkishText = new TextView(this);
        turkishText.setText(turkish);
        turkishText.setTextColor(getResources().getColor(R.color.text_secondary));
        turkishText.setTextSize(14);
        turkishText.setPadding(0, 6, 0, 0);

        TextView metaText = new TextView(this);
        metaText.setText("Kategori: " + category);
        metaText.setTextColor(getResources().getColor(R.color.text_secondary));
        metaText.setTextSize(12);
        metaText.setPadding(0, 8, 0, 0);

        content.addView(topRow);
        content.addView(turkishText);
        content.addView(metaText);
        card.addView(content);
        return card;
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("Verileri sıfırla")
                .setMessage("Emin misiniz? Analiz geçmişi sıfırlanacak.")
                .setNegativeButton("İptal", null)
                .setPositiveButton("Evet, sıfırla", (dialog, which) -> resetAnalysisData())
                .show();
    }

    private void resetAnalysisData() {
        db.resetAnalysisStatistics();
        calculateAndShowStats();
        Toast.makeText(this, "Analiz geçmişi sıfırlandı", Toast.LENGTH_SHORT).show();
    }

    private void printReport() {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        String jobName = getString(R.string.app_name) + " Analiz Raporu";

        printManager.print(jobName, new PrintDocumentAdapter() {
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
                if (cancellationSignal.isCanceled()) {
                    callback.onLayoutCancelled();
                    return;
                }
                PrintDocumentInfo info = new PrintDocumentInfo.Builder("rapor.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build();
                callback.onLayoutFinished(info, true);
            }

            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
                PdfDocument pdfDocument = new PdfDocument();

                try {
                    List<String> lines = collectPrintableLines();
                    final int pageWidth = 595;
                    final int pageHeight = 842;
                    final int left = 50;
                    final int top = 50;
                    final int contentStartY = 150;
                    final int bottomMargin = 50;
                    final int lineHeight = 22;
                    final int linesPerPage = (pageHeight - contentStartY - bottomMargin) / lineHeight;

                    Paint titlePaint = new Paint();
                    titlePaint.setColor(Color.BLACK);
                    titlePaint.setTextSize(18);
                    titlePaint.setTypeface(Typeface.DEFAULT_BOLD);

                    Paint bodyPaint = new Paint();
                    bodyPaint.setColor(Color.BLACK);
                    bodyPaint.setTextSize(14);

                    int pageNumber = 1;
                    int index = 0;
                    while (index < lines.size()) {
                        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                        Canvas canvas = page.getCanvas();

                        canvas.drawText("KELIME EZBERLEME SISTEMI - ANALIZ RAPORU", left, top, titlePaint);
                        canvas.drawText(tvOverallStats.getText().toString(), left, 100, bodyPaint);
                        canvas.drawText("Sayfa " + pageNumber, 500, 24, bodyPaint);

                        int y = contentStartY;
                        int drawn = 0;
                        while (index < lines.size() && drawn < linesPerPage) {
                            String line = lines.get(index++);
                            canvas.drawText(line, left, y, bodyPaint);
                            y += line.contains("•") ? 24 : 28;
                            drawn++;
                        }

                        pdfDocument.finishPage(page);
                        pageNumber++;
                    }

                    pdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
                } catch (IOException e) {
                    callback.onWriteFailed(e.toString());
                    return;
                } finally {
                    pdfDocument.close();
                }
                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            }
        }, null);
    }

    private List<String> collectPrintableLines() {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < llCategoryStats.getChildCount(); i++) {
            View child = llCategoryStats.getChildAt(i);
            if (child instanceof TextView) {
                String text = ((TextView) child).getText().toString();
                String[] split = text.split("\\n");
                Collections.addAll(lines, split);
            }
        }
        return lines;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
