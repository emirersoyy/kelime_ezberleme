package com.example.kelimeezberleme;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalysisActivity extends AppCompatActivity {
    DatabaseHelper db;
    LinearLayout llCategoryStats;
    TextView tvOverallStats;
    View reportView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        db = new DatabaseHelper(this);
        llCategoryStats = findViewById(R.id.llCategoryStats);
        tvOverallStats = findViewById(R.id.tvOverallStats);
        reportView = findViewById(R.id.llReportContainer);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnPrint).setOnClickListener(v -> printReport());

        calculateAndShowStats();
    }

    private void calculateAndShowStats() {
        List<Word> words = db.getAllWords();
        if (words.isEmpty()) {
            tvOverallStats.setText("Henüz çözülmüş kelime yok.");
            return;
        }

        Map<String, int[]> stats = new HashMap<>();
        int totalSolved = 0;

        for (Word w : words) {
            if (w.totalAttempts > 0) {
                totalSolved += w.totalAttempts;
                String cat = w.category == null ? "Genel" : w.category;
                if (!stats.containsKey(cat)) stats.put(cat, new int[]{0, 0});
                stats.get(cat)[0] += w.totalAttempts;
                stats.get(cat)[1] += w.correctAttempts;
            }
        }

        tvOverallStats.setText("Toplam " + totalSolved + " soru çözüldü.");

        for (Map.Entry<String, int[]> entry : stats.entrySet()) {
            addCategoryRow(entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
        }
    }

    private void addCategoryRow(String name, int total, int correct) {
        int percent = (int) (((double) correct / total) * 100);

        TextView text = new TextView(this);
        text.setText(name + ": %" + percent + " Başarı (" + correct + "/" + total + ")");
        text.setTextColor(getResources().getColor(R.color.text_primary));
        text.setTextSize(15);
        text.setPadding(0, 12, 0, 8);

        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        pb.setProgress(percent);
        pb.setPadding(0, 0, 0, 18);

        llCategoryStats.addView(text);
        llCategoryStats.addView(pb);
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
                        .setPageCount(1)
                        .build();
                callback.onLayoutFinished(info, true);
            }

            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
                PdfDocument pdfDocument = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);

                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                paint.setTextSize(18);

                canvas.drawText("KELİME EZBERLEME SİSTEMİ - ANALİZ RAPORU", 50, 50, paint);
                paint.setTextSize(14);
                canvas.drawText(tvOverallStats.getText().toString(), 50, 100, paint);

                int y = 150;
                canvas.drawText("Konu Bazlı Başarılar:", 50, y, paint);
                y += 30;

                for (int i = 0; i < llCategoryStats.getChildCount(); i++) {
                    View child = llCategoryStats.getChildAt(i);
                    if (child instanceof TextView) {
                        canvas.drawText(((TextView) child).getText().toString(), 70, y, paint);
                        y += 25;
                    }
                }

                pdfDocument.finishPage(page);

                try {
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
}
