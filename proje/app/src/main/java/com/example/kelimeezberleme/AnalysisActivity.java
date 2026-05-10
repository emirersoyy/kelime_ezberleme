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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AnalysisActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private LinearLayout llCategoryStats;
    private TextView tvOverallStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        db = new DatabaseHelper(this);
        llCategoryStats = findViewById(R.id.llCategoryStats);
        tvOverallStats = findViewById(R.id.tvOverallStats);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnPrint).setOnClickListener(v -> printReport());
        findViewById(R.id.btnResetData).setOnClickListener(v -> confirmReset());

        calculateAndShowStats();
    }

    private void calculateAndShowStats() {
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

        addStatusSection("Öğrenilmekte Olan", learning, R.color.accent);
        addStatusSection("Öğrenilmiş", learned, R.color.success);
        addStatusSection("Daha Başlanmamış", notStarted, R.color.text_secondary);
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

        TextView header = new TextView(this);
        header.setText(title + " (" + items.size() + ")");
        header.setTextColor(accentColor);
        header.setTextSize(16);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setPadding(0, 16, 0, 8);
        llCategoryStats.addView(header);

        if (items.isEmpty()) {
            addEmptyState("Bu bölümde kelime yok.");
            return;
        }

        for (Word word : items) {
            addWordRow(word, accentColor);
        }
    }

    private void addWordRow(Word word, int accentColor) {
        String category = word.category == null || word.category.trim().isEmpty() ? "Genel" : word.category.trim();
        String english = word.eng == null || word.eng.trim().isEmpty() ? "-" : word.eng.trim();
        String turkish = word.tur == null || word.tur.trim().isEmpty() ? "-" : word.tur.trim();

        TextView text = new TextView(this);
        text.setText(english + "  •  " + turkish + "\n"
                + "Kategori: " + category + "   |   Quiz seviyesi: " + Math.max(word.stepCount, 0));
        text.setTextColor(getResources().getColor(R.color.text_primary));
        text.setTextSize(15);
        text.setPadding(16, 14, 16, 14);
        text.setBackgroundResource(R.drawable.soft_chip_bg);
        text.setLineSpacing(0f, 1.15f);
        text.setIncludeFontPadding(false);
        llCategoryStats.addView(text);

        View divider = new View(this);
        divider.setBackgroundColor(accentColor);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.topMargin = 10;
        params.bottomMargin = 10;
        divider.setLayoutParams(params);
        llCategoryStats.addView(divider);
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
}
