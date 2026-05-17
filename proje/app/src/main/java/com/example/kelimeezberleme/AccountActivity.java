package com.example.kelimeezberleme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AccountActivity extends BottomNavActivity {
    private static final int REQUEST_PICK_PROFILE_IMAGE = 42;
    private static final long MAX_PROFILE_IMAGE_BYTES = 2L * 1024L * 1024L;
    private static final int MAX_PROFILE_IMAGE_SIDE = 2048;
    private static final String SORT_ALPHA_ASC = "alpha_asc";
    private static final String SORT_ALPHA_DESC = "alpha_desc";
    private static final String SORT_LEVEL_DESC = "level_desc";
    private static final String SORT_LEVEL_ASC = "level_asc";
    private static final String ANALYSIS_FILTER_ALL = "all";
    private static final int MAX_ANALYSIS_LEVEL = 6;
    private static final int MAX_ANALYSIS_WORD_CARDS = 120;

    private DatabaseHelper db;
    private ImageView imgAccountProfile;
    private TextView tvCurrentUser;
    private TextView tvFullName;
    private String currentUser;
    private String selectedProfileImagePath = "";
    private ImageView dialogProfileImage;

    private MaterialButton btnTabAnalysis;
    private MaterialButton btnTabWords;
    private View layoutAnalysisSection;
    private View layoutWordsSection;
    private MaterialButton btnEmbeddedLoadMoreAnalysis;

    private LinearLayout llEmbeddedAllChip;
    private LinearLayout llEmbeddedSummaryChips;
    private LinearLayout llEmbeddedCategoryStats;
    private TextView tvEmbeddedFilterSummary;
    private AppCompatTextView tvEmbeddedAnalysisSort;
    private HorizontalScrollView hsvEmbeddedSummaryChips;
    private View viewEmbeddedFadeLeft;
    private View viewEmbeddedFadeRight;

    private Spinner spEmbeddedSort;
    private RecyclerView rvEmbeddedWords;
    private CategoryAdapter categoryAdapter;
    private final List<CategoryItem> allCategories = new ArrayList<>();
    private boolean showingAnalysis = true;
    private String selectedAnalysisFilter = ANALYSIS_FILTER_ALL;
    private boolean analysisSortAscending = true;
    private List<Word> analysisSourceWords = new ArrayList<>();
    private int analysisRenderedCount = 0;
    private boolean analysisPagingLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        db = new DatabaseHelper(this);
        bindProfileHeader();
        bindEmbeddedSections();

        loadProfile();
        setupWordsSection();
        setupAnalysisSection();
        showSection(true);
        refreshEmbeddedContent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
        analysisSortAscending = true;
        refreshEmbeddedContent();
        updateTabStyles();
    }

    private void bindProfileHeader() {
        imgAccountProfile = findViewById(R.id.imgAccountProfile);
        tvCurrentUser = findViewById(R.id.tvCurrentUser);
        tvFullName = findViewById(R.id.tvFullName);
        MaterialButton btnEditProfile = findViewById(R.id.btnEditProfile);

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
    }

    private void bindEmbeddedSections() {
        btnTabAnalysis = findViewById(R.id.btnTabAnalysis);
        btnTabWords = findViewById(R.id.btnTabWords);
        layoutAnalysisSection = findViewById(R.id.layoutAnalysisSection);
        layoutWordsSection = findViewById(R.id.layoutWordsSection);

        llEmbeddedAllChip = findViewById(R.id.llEmbeddedAllChip);
        llEmbeddedSummaryChips = findViewById(R.id.llEmbeddedSummaryChips);
        llEmbeddedCategoryStats = findViewById(R.id.llEmbeddedCategoryStats);
        tvEmbeddedFilterSummary = findViewById(R.id.tvEmbeddedFilterSummary);
        tvEmbeddedAnalysisSort = findViewById(R.id.tvEmbeddedAnalysisSort);
        hsvEmbeddedSummaryChips = findViewById(R.id.hsvEmbeddedSummaryChips);
        viewEmbeddedFadeLeft = findViewById(R.id.viewEmbeddedFadeLeft);
        viewEmbeddedFadeRight = findViewById(R.id.viewEmbeddedFadeRight);
        btnEmbeddedLoadMoreAnalysis = findViewById(R.id.btnEmbeddedLoadMoreAnalysis);
        ImageButton btnEmbeddedPrint = findViewById(R.id.btnEmbeddedPrint);

        spEmbeddedSort = findViewById(R.id.spEmbeddedSort);
        rvEmbeddedWords = findViewById(R.id.rvEmbeddedWords);
        MaterialButton btnEmbeddedAddWord = findViewById(R.id.btnEmbeddedAddWord);

        btnTabAnalysis.setOnClickListener(v -> showSection(true));
        btnTabWords.setOnClickListener(v -> showSection(false));
        btnEmbeddedPrint.setOnClickListener(v -> printReport());
        tvEmbeddedAnalysisSort.setOnClickListener(v -> {
            analysisSortAscending = !analysisSortAscending;
            refreshAnalysisContent();
        });
        hsvEmbeddedSummaryChips.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) ->
                updateSummaryChipFadeState());
        btnEmbeddedAddWord.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, AddWordActivity.class)));
        btnEmbeddedLoadMoreAnalysis.setOnClickListener(v -> loadMoreAnalysisWords());
    }

    private void showSection(boolean analysis) {
        showingAnalysis = analysis;
        layoutAnalysisSection.setVisibility(analysis ? View.VISIBLE : View.GONE);
        layoutWordsSection.setVisibility(analysis ? View.GONE : View.VISIBLE);
        updateTabStyles();
    }

    private void updateTabStyles() {
        styleTab(btnTabAnalysis, showingAnalysis);
        styleTab(btnTabWords, !showingAnalysis);
    }

    private void styleTab(MaterialButton button, boolean active) {
        int background = getResources().getColor(active ? R.color.primary : R.color.surface);
        int textColor = getResources().getColor(active ? R.color.white : R.color.text_primary);
        button.setBackgroundTintList(ColorStateList.valueOf(background));
        button.setTextColor(textColor);
        button.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.divider)));
        button.setStrokeWidth(active ? 0 : 1);
    }

    private void loadProfile() {
        currentUser = AppSettings.getCurrentUser(this);
        DatabaseHelper.UserProfile profile = db.getUserProfile(currentUser);
        String username = profile == null ? currentUser : profile.username;
        String fullName = profile == null ? "" : profile.fullName;
        selectedProfileImagePath = profile == null ? "" : profile.profileImagePath;

        String safeUsername = username == null || username.trim().isEmpty() ? "Bilinmiyor" : username.trim();
        if (fullName == null || fullName.trim().isEmpty()) {
            tvFullName.setText(safeUsername);
            tvCurrentUser.setVisibility(View.GONE);
        } else {
            tvFullName.setText(fullName.trim());
            tvCurrentUser.setText(safeUsername);
            tvCurrentUser.setVisibility(View.VISIBLE);
        }
        applyProfileImage(imgAccountProfile, selectedProfileImagePath);
    }

    private void refreshEmbeddedContent() {
        refreshAnalysisContent();
        refreshWordsContent();
    }

    private void setupAnalysisSection() {
        refreshAnalysisContent();
    }

    private void refreshAnalysisContent() {
        llEmbeddedAllChip.removeAllViews();
        llEmbeddedSummaryChips.removeAllViews();
        llEmbeddedCategoryStats.removeAllViews();

        List<Word> words = WordleWordBank.mergeDisplayWords(db.getAllWords());
        if (words.isEmpty()) {
            addEmbeddedEmptyState("Gösterilecek veri yok.");
            return;
        }

        Collections.sort(words, Comparator.comparing(w -> w.eng == null ? "" : w.eng.toLowerCase(Locale.US)));

        Map<Integer, List<Word>> wordsByLevel = new LinkedHashMap<>();
        for (Word word : words) {
            int level = Math.max(word.stepCount, 0);
            List<Word> levelWords = wordsByLevel.get(level);
            if (levelWords == null) {
                levelWords = new ArrayList<>();
                wordsByLevel.put(level, levelWords);
            }
            levelWords.add(word);
        }

        addMetricChip(llEmbeddedAllChip, "Tümü", R.color.primary, ANALYSIS_FILTER_ALL);
        for (int level = 0; level <= MAX_ANALYSIS_LEVEL; level++) {
            addMetricChip(llEmbeddedSummaryChips, "Seviye " + level, getLevelAccentColorRes(level), createLevelFilterKey(level));
        }

        List<Word> filteredWords = new ArrayList<>();
        String summaryText;
        if (ANALYSIS_FILTER_ALL.equals(selectedAnalysisFilter)) {
            filteredWords.addAll(words);
            summaryText = words.size() + " kelime";
        } else {
            Integer selectedLevel = parseLevelFilter(selectedAnalysisFilter);
            if (selectedLevel != null && wordsByLevel.containsKey(selectedLevel)) {
                filteredWords.addAll(wordsByLevel.get(selectedLevel));
            }
            summaryText = filteredWords.size() + " kelime";
        }

        Collections.sort(filteredWords, analysisSortAscending
                ? Comparator.comparing(w -> w.eng == null ? "" : w.eng.toLowerCase(Locale.US))
                : (left, right) -> {
                    String a = left == null || left.eng == null ? "" : left.eng.toLowerCase(Locale.US);
                    String b = right == null || right.eng == null ? "" : right.eng.toLowerCase(Locale.US);
                    return b.compareTo(a);
                });

        analysisSourceWords = filteredWords;
        analysisRenderedCount = 0;
        analysisPagingLocked = false;
        btnEmbeddedLoadMoreAnalysis.setVisibility(filteredWords.size() > MAX_ANALYSIS_WORD_CARDS ? View.VISIBLE : View.GONE);

        tvEmbeddedFilterSummary.setText(summaryText);
        updateAnalysisSortView();
        hsvEmbeddedSummaryChips.post(this::updateSummaryChipFadeState);
        addAnalysisWordCards(filteredWords);
    }

    private void addMetricChip(LinearLayout container, String label, int colorResId, String filterKey) {
        int color = getResources().getColor(colorResId);
        boolean isSelected = filterKey.equals(selectedAnalysisFilter);
        AppCompatTextView chip = new AppCompatTextView(this);
        LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        chipParams.rightMargin = dp(8);
        chip.setLayoutParams(chipParams);
        chip.setText(label);
        chip.setTextSize(12);
        chip.setTypeface(Typeface.DEFAULT_BOLD);
        chip.setPadding(dp(14), dp(8), dp(14), dp(8));
        chip.setBackground(createRoundedChipBackground(color, isSelected));
        chip.setTextColor(getResources().getColor(isSelected ? R.color.white : colorResId));
        chip.setClickable(true);
        chip.setFocusable(true);
        chip.setOnClickListener(v -> {
            if (!filterKey.equals(selectedAnalysisFilter)) {
                selectedAnalysisFilter = filterKey;
                refreshAnalysisContent();
            }
        });
        container.addView(chip);
    }

    private void addEmbeddedEmptyState(String message) {
        MaterialCardView emptyCard = new MaterialCardView(this);
        emptyCard.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        emptyCard.setCardBackgroundColor(getResources().getColor(R.color.background));
        emptyCard.setCardElevation(dpFloat(0.5f));
        emptyCard.setRadius(getResources().getDimension(R.dimen.radius_sm));
        emptyCard.setStrokeWidth(0);

        TextView text = new TextView(this);
        text.setText(message);
        text.setTextColor(getResources().getColor(R.color.text_secondary));
        text.setTextSize(15);
        text.setGravity(android.view.Gravity.CENTER);
        text.setPadding(dp(16), dp(18), dp(16), dp(18));
        emptyCard.addView(text);
        llEmbeddedCategoryStats.addView(emptyCard);
    }

    private void addAnalysisWordCards(List<Word> items) {
        if (items.isEmpty()) {
            if (ANALYSIS_FILTER_ALL.equals(selectedAnalysisFilter)) {
                addEmbeddedEmptyState("Gösterilecek veri yok.");
            } else {
                Integer selectedLevel = parseLevelFilter(selectedAnalysisFilter);
                addEmbeddedEmptyState(selectedLevel == null
                        ? "Gösterilecek veri yok."
                        : "Henüz " + selectedLevel + ". seviye bir kelimeye sahip değilsiniz.");
            }
            return;
        }

        loadMoreAnalysisWords();
    }

    private void appendAnalysisWordCards(List<Word> items, int start, int end) {
        if (items == null) {
            return;
        }
        int safeStart = Math.max(0, start);
        int safeEnd = Math.max(safeStart, Math.min(items.size(), end));
        for (int i = safeStart; i < safeEnd; i++) {
            Word word = items.get(i);
            llEmbeddedCategoryStats.addView(createAnalysisWordCard(word, getLevelAccentColor(word.stepCount)));
        }
    }

    private void loadMoreAnalysisWords() {
        if (analysisPagingLocked || analysisSourceWords == null || analysisRenderedCount >= analysisSourceWords.size()) {
            return;
        }

        int nextCount = Math.min(analysisSourceWords.size(), analysisRenderedCount + MAX_ANALYSIS_WORD_CARDS);
        appendAnalysisWordCards(analysisSourceWords, analysisRenderedCount, nextCount);
        analysisRenderedCount = nextCount;
        if (analysisRenderedCount >= analysisSourceWords.size()) {
            analysisPagingLocked = true;
            btnEmbeddedLoadMoreAnalysis.setVisibility(View.GONE);
        } else {
            btnEmbeddedLoadMoreAnalysis.setVisibility(View.VISIBLE);
        }
    }

    private MaterialCardView createAnalysisWordCard(Word word, int accentColor) {
        String category = word.category == null || word.category.trim().isEmpty() ? "Genel" : word.category.trim();
        String english = word.eng == null || word.eng.trim().isEmpty() ? "-" : word.eng.trim();
        String turkish = word.tur == null || word.tur.trim().isEmpty() ? "-" : word.tur.trim();
        boolean showLevelBadge = ANALYSIS_FILTER_ALL.equals(selectedAnalysisFilter);
        List<String> samples = db.getDisplaySamplesForWord(word);

        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(10);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(getResources().getColor(R.color.background));
        card.setCardElevation(dpFloat(0.5f));
        card.setRadius(getResources().getDimension(R.dimen.radius_sm));
        card.setStrokeWidth(0);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(14), dp(16), dp(14));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView englishText = new TextView(this);
        englishText.setText(english);
        englishText.setTextColor(getResources().getColor(R.color.text_primary));
        englishText.setTextSize(16);
        englishText.setTypeface(Typeface.DEFAULT_BOLD);
        englishText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView levelText = new TextView(this);
        levelText.setText("Seviye " + Math.max(word.stepCount, 0));
        levelText.setTextColor(accentColor);
        levelText.setTextSize(12);
        levelText.setTypeface(Typeface.DEFAULT_BOLD);
        levelText.setPadding(dp(14), dp(8), dp(14), dp(8));
        levelText.setBackground(createRoundedChipBackground(accentColor, false));
        levelText.setVisibility(showLevelBadge ? View.VISIBLE : View.GONE);

        topRow.addView(englishText);
        topRow.addView(levelText);

        TextView turkishText = new TextView(this);
        turkishText.setText(turkish);
        turkishText.setTextColor(getResources().getColor(R.color.text_secondary));
        turkishText.setTextSize(14);
        turkishText.setPadding(0, dp(6), 0, 0);

        TextView metaText = new TextView(this);
        metaText.setText("Kategori: " + category);
        metaText.setTextColor(getResources().getColor(R.color.text_secondary));
        metaText.setTextSize(12);
        metaText.setPadding(0, dp(8), 0, 0);

        LinearLayout detailsContainer = new LinearLayout(this);
        detailsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        detailsContainer.setOrientation(LinearLayout.HORIZONTAL);
        detailsContainer.setGravity(android.view.Gravity.CENTER_VERTICAL);
        detailsContainer.setPadding(0, dp(12), 0, 0);
        detailsContainer.setVisibility(word.expanded ? View.VISIBLE : View.GONE);

        LinearLayout detailsTextColumn = new LinearLayout(this);
        detailsTextColumn.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        detailsTextColumn.setOrientation(LinearLayout.VERTICAL);

        TextView sampleText = new TextView(this);
        sampleText.setText(formatSamples(samples));
        sampleText.setTextColor(getResources().getColor(R.color.text_primary));
        sampleText.setTextSize(13);
        sampleText.setVisibility(samples == null || samples.isEmpty() ? View.GONE : View.VISIBLE);

        ImageView wordImage = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dp(76), dp(76));
        imageParams.leftMargin = dp(12);
        wordImage.setLayoutParams(imageParams);
        wordImage.setBackgroundResource(R.drawable.soft_chip_bg);
        wordImage.setClipToOutline(true);
        wordImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        WordImageLoader.load(wordImage, word.pic);

        detailsTextColumn.addView(sampleText);
        detailsContainer.addView(detailsTextColumn);
        detailsContainer.addView(wordImage);

        content.addView(topRow);
        content.addView(turkishText);
        content.addView(metaText);
        content.addView(detailsContainer);
        card.addView(content);
        card.setOnClickListener(v -> {
            word.expanded = !word.expanded;
            detailsContainer.setVisibility(word.expanded ? View.VISIBLE : View.GONE);
        });
        return card;
    }

    private String formatSamples(List<String> samples) {
        if (samples == null || samples.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < samples.size(); i++) {
            if (i > 0) {
                builder.append("\n\n");
            }
            builder.append(i + 1).append(". ").append(samples.get(i).trim());
        }
        return builder.toString();
    }

    private String createLevelFilterKey(int level) {
        return "level_" + level;
    }

    private Integer parseLevelFilter(String filterKey) {
        if (filterKey == null || !filterKey.startsWith("level_")) {
            return null;
        }
        try {
            return Integer.parseInt(filterKey.substring("level_".length()));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private int getLevelAccentColor(int stepCount) {
        return getResources().getColor(getLevelAccentColorRes(Math.max(stepCount, 0)));
    }

    private int getLevelAccentColorRes(int level) {
        switch (Math.max(0, Math.min(level, MAX_ANALYSIS_LEVEL))) {
            case 0:
                return R.color.level_0;
            case 1:
                return R.color.level_1;
            case 2:
                return R.color.level_2;
            case 3:
                return R.color.level_3;
            case 4:
                return R.color.level_4;
            case 5:
                return R.color.level_5;
            case 6:
            default:
                return R.color.level_6;
        }
    }

    private GradientDrawable createRoundedChipBackground(int accentColor, boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(getResources().getDimension(R.dimen.radius_md));
        drawable.setColor(selected ? accentColor : getResources().getColor(R.color.surface_variant));
        drawable.setStroke(dp(1), accentColor);
        return drawable;
    }

    private void updateAnalysisSortView() {
        if (tvEmbeddedAnalysisSort == null) {
            return;
        }
        int accentColor = getResources().getColor(R.color.text_secondary);
        tvEmbeddedAnalysisSort.setText("Alfabeye göre");
        tvEmbeddedAnalysisSort.setBackground(createRoundedChipBackground(accentColor, false));
        tvEmbeddedAnalysisSort.setCompoundDrawablePadding(dp(6));
        tvEmbeddedAnalysisSort.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                analysisSortAscending ? R.drawable.ic_chevron_down_18 : R.drawable.ic_chevron_up_18,
                0
        );
    }

    private void updateSummaryChipFadeState() {
        if (hsvEmbeddedSummaryChips == null || viewEmbeddedFadeLeft == null || viewEmbeddedFadeRight == null) {
            return;
        }
        int childWidth = hsvEmbeddedSummaryChips.getChildCount() > 0
                ? hsvEmbeddedSummaryChips.getChildAt(0).getWidth()
                : 0;
        int viewportWidth = hsvEmbeddedSummaryChips.getWidth();
        int maxScroll = Math.max(0, childWidth - viewportWidth);
        int scrollX = hsvEmbeddedSummaryChips.getScrollX();

        viewEmbeddedFadeLeft.setVisibility(scrollX <= 0 ? View.GONE : View.VISIBLE);
        viewEmbeddedFadeRight.setVisibility(scrollX >= maxScroll ? View.GONE : View.VISIBLE);
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("Verileri sıfırla")
                .setMessage("Emin misiniz? Analiz geçmişi sıfırlanacak.")
                .setNegativeButton("İptal", null)
                .setPositiveButton("Evet, sıfırla", (dialog, which) -> {
                    db.resetAnalysisStatistics();
                    refreshAnalysisContent();
                    Toast.makeText(this, "Analiz geçmişi sıfırlandı", Toast.LENGTH_SHORT).show();
                })
                .show();
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
        for (int i = 0; i < llEmbeddedCategoryStats.getChildCount(); i++) {
            View child = llEmbeddedCategoryStats.getChildAt(i);
            if (child instanceof MaterialCardView) {
                collectTextFromView(child, lines);
            } else if (child instanceof TextView) {
                lines.add(((TextView) child).getText().toString());
            }
        }
        return lines;
    }

    private void collectTextFromView(View view, List<String> lines) {
        if (view instanceof TextView) {
            String text = ((TextView) view).getText().toString().trim();
            if (!text.isEmpty()) {
                lines.add(text);
            }
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                collectTextFromView(group.getChildAt(i), lines);
            }
        }
    }

    private void setupWordsSection() {
        rvEmbeddedWords.setLayoutManager(new LinearLayoutManager(this));
        rvEmbeddedWords.setNestedScrollingEnabled(false);
        categoryAdapter = new CategoryAdapter(new ArrayList<>());
        rvEmbeddedWords.setAdapter(categoryAdapter);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"A-Z", "Z-A", "En çok kelime", "En az kelime"}
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEmbeddedSort.setAdapter(spinnerAdapter);

        String savedSort = AppSettings.getWordsSortOrder(this);
        spEmbeddedSort.setSelection(getSortPosition(savedSort), false);
        spEmbeddedSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sortOrder = getSortKey(position);
                AppSettings.setWordsSortOrder(AccountActivity.this, sortOrder);
                applyWordSorting(sortOrder);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void refreshWordsContent() {
        allCategories.clear();
        Map<String, List<Word>> grouped = new LinkedHashMap<>();
        for (Word word : WordleWordBank.mergeDisplayWords(db.getAllWords())) {
            String category = word.category == null || word.category.trim().isEmpty() ? "Genel" : word.category.trim();
            List<Word> words = grouped.get(category);
            if (words == null) {
                words = new ArrayList<>();
                grouped.put(category, words);
            }
            words.add(word);
        }
        for (Map.Entry<String, List<Word>> entry : grouped.entrySet()) {
            allCategories.add(new CategoryItem(entry.getKey(), entry.getValue()));
        }
        applyWordSorting(AppSettings.getWordsSortOrder(this));
    }

    private void applyWordSorting(String sortOrder) {
        List<CategoryItem> sorted = new ArrayList<>(allCategories);
        switch (sortOrder) {
            case SORT_ALPHA_DESC:
                Collections.sort(sorted, (a, b) -> b.name.toLowerCase(Locale.US).compareTo(a.name.toLowerCase(Locale.US)));
                break;
            case SORT_LEVEL_DESC:
                Collections.sort(sorted, Comparator.comparingInt((CategoryItem item) -> item.words.size())
                        .reversed()
                        .thenComparing(item -> item.name.toLowerCase(Locale.US)));
                break;
            case SORT_LEVEL_ASC:
                Collections.sort(sorted, Comparator.comparingInt((CategoryItem item) -> item.words.size())
                        .thenComparing(item -> item.name.toLowerCase(Locale.US)));
                break;
            case SORT_ALPHA_ASC:
            default:
                Collections.sort(sorted, Comparator.comparing(item -> item.name.toLowerCase(Locale.US)));
                break;
        }
        categoryAdapter.updateItems(sorted);
    }

    private int compareEngSafe(Word left, Word right) {
        String a = left == null || left.eng == null ? "" : left.eng.toLowerCase(Locale.US);
        String b = right == null || right.eng == null ? "" : right.eng.toLowerCase(Locale.US);
        return a.compareTo(b);
    }

    private String getSortKey(int position) {
        switch (position) {
            case 1:
                return SORT_ALPHA_DESC;
            case 2:
                return SORT_LEVEL_DESC;
            case 3:
                return SORT_LEVEL_ASC;
            case 0:
            default:
                return SORT_ALPHA_ASC;
        }
    }

    private int getSortPosition(String sortOrder) {
        if (SORT_ALPHA_DESC.equals(sortOrder)) return 1;
        if (SORT_LEVEL_DESC.equals(sortOrder)) return 2;
        if (SORT_LEVEL_ASC.equals(sortOrder)) return 3;
        return 0;
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        dialogProfileImage = dialogView.findViewById(R.id.imgEditProfile);
        MaterialButton btnShowPasswordReset = dialogView.findViewById(R.id.btnShowPasswordReset);
        MaterialButton btnDialogLogout = dialogView.findViewById(R.id.btnDialogLogout);
        TextInputLayout tilUsername = dialogView.findViewById(R.id.tilEditUsername);
        TextInputLayout tilFullName = dialogView.findViewById(R.id.tilEditFullName);
        TextInputEditText etUsername = dialogView.findViewById(R.id.etEditUsername);
        TextInputEditText etFullName = dialogView.findViewById(R.id.etEditFullName);
        MaterialButtonToggleGroup toggleTheme = dialogView.findViewById(R.id.toggleDialogTheme);
        Slider sliderQuestionLimit = dialogView.findViewById(R.id.sliderDialogQuestionLimit);
        TextView tvQuestionLimitValue = dialogView.findViewById(R.id.tvDialogQuestionLimitValue);

        DatabaseHelper.UserProfile profile = db.getUserProfile(currentUser);
        String profileUsername = profile == null ? currentUser : profile.username;
        selectedProfileImagePath = profile == null ? "" : profile.profileImagePath;
        etUsername.setText(profileUsername);
        etFullName.setText(profile == null ? "" : profile.fullName);
        applyProfileImage(dialogProfileImage, selectedProfileImagePath);

        int currentLimit = AppSettings.getQuizLimit(this);
        sliderQuestionLimit.setValue(currentLimit);
        tvQuestionLimitValue.setText(String.valueOf(currentLimit));
        sliderQuestionLimit.addOnChangeListener((slider, value, fromUser) ->
                tvQuestionLimitValue.setText(String.valueOf(AppSettings.clampQuizLimit(Math.round(value)))));

        String savedTheme = ThemeManager.getSavedTheme(this);
        toggleTheme.check(ThemeManager.THEME_DARK.equals(savedTheme)
                ? R.id.btnDialogDarkTheme
                : R.id.btnDialogLightTheme);

        dialogProfileImage.setOnClickListener(v -> showProfileImageOptions());
        btnShowPasswordReset.setOnClickListener(v -> showResetPasswordDialog(getText(etUsername)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Profili Düzenle")
                .setView(dialogView)
                .setNegativeButton("Vazgeç", null)
                .setPositiveButton("Kaydet", null)
                .create();

        btnDialogLogout.setOnClickListener(v -> {
            dialog.dismiss();
            AppSettings.clearCurrentUser(this);
            AppSettings.clearRememberedLogin(this);
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            tilUsername.setError(null);
            tilFullName.setError(null);

            String newUsername = getText(etUsername);
            String fullName = getText(etFullName);
            int newLimit = AppSettings.clampQuizLimit(Math.round(sliderQuestionLimit.getValue()));
            String selectedTheme = toggleTheme.getCheckedButtonId() == R.id.btnDialogDarkTheme
                    ? ThemeManager.THEME_DARK
                    : ThemeManager.THEME_LIGHT;

            String usernameError = AccountSecurity.validateUsername(newUsername);
            if (usernameError != null) {
                tilUsername.setError(usernameError);
                return;
            }
            if (db.isUsernameTakenByOtherUser(newUsername, currentUser)) {
                tilUsername.setError("Bu kullanıcı adı zaten kullanılıyor.");
                return;
            }

            if (db.updateUserProfile(currentUser, newUsername, fullName, "", selectedProfileImagePath)) {
                updateStoredUsername(currentUser, newUsername);
                currentUser = newUsername;
                AppSettings.setQuizLimit(this, newLimit);
                ThemeManager.saveAndApplyTheme(this, selectedTheme);
                loadProfile();
                Toast.makeText(this, "Profil güncellendi.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Profil güncellenemedi.", Toast.LENGTH_SHORT).show();
            }
        }));

        dialog.setOnDismissListener(d -> dialogProfileImage = null);
        dialog.show();
        applyRoundedDialogCorners(dialog);
    }

    private void showResetPasswordDialog(String usernameForValidation) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reset_password, null);
        TextInputLayout tilCurrentPassword = dialogView.findViewById(R.id.tilCurrentPassword);
        TextInputLayout tilNewPassword = dialogView.findViewById(R.id.tilNewPassword);
        TextInputLayout tilConfirmPassword = dialogView.findViewById(R.id.tilConfirmPassword);
        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Şifremi Sıfırla")
                .setView(dialogView)
                .setNegativeButton("Vazgeç", null)
                .setPositiveButton("Kaydet", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            tilCurrentPassword.setError(null);
            tilNewPassword.setError(null);
            tilConfirmPassword.setError(null);

            String currentPassword = getText(etCurrentPassword);
            String newPassword = getText(etNewPassword);
            String confirmPassword = getText(etConfirmPassword);
            String username = usernameForValidation == null || usernameForValidation.trim().isEmpty()
                    ? currentUser
                    : usernameForValidation.trim();

            if (!db.checkUser(currentUser, currentPassword)) {
                tilCurrentPassword.setError("Eski şifre hatalı.");
                return;
            }
            String passwordError = AccountSecurity.validatePassword(username, newPassword);
            if (passwordError != null) {
                tilNewPassword.setError(passwordError);
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                tilConfirmPassword.setError("Yeni şifreler eşleşmiyor.");
                return;
            }

            if (db.updatePassword(currentUser, newPassword)) {
                Toast.makeText(this, "Şifre güncellendi.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Şifre güncellenemedi.", Toast.LENGTH_SHORT).show();
            }
        }));

        dialog.show();
        applyRoundedDialogCorners(dialog);
    }

    private void applyRoundedDialogCorners(AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            window.getDecorView().setBackgroundResource(R.drawable.dialog_rounded_bg);
            window.getDecorView().setPadding(dp(4), dp(4), dp(4), dp(4));
        }
    }

    private void openProfileImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_PICK_PROFILE_IMAGE);
    }

    private void showProfileImageOptions() {
        String[] options = selectedProfileImagePath == null || selectedProfileImagePath.trim().isEmpty()
                ? new String[]{"Galeriden Seç"}
                : new String[]{"Galeriden Seç", "Mevcut Resmi Kaldır"};
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setItems(options, (selectionDialog, which) -> {
                    if (which == 0) {
                        openProfileImagePicker();
                        return;
                    }
                    selectedProfileImagePath = "";
                    if (dialogProfileImage != null) {
                        applyProfileImage(dialogProfileImage, selectedProfileImagePath);
                    }
                })
                .create();
        dialog.show();
        applyRoundedDialogCorners(dialog);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_PROFILE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            if (!isProfileImageAllowed(uri)) {
                return;
            }
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
            }
            selectedProfileImagePath = uri.toString();
            if (dialogProfileImage != null) {
                applyProfileImage(dialogProfileImage, selectedProfileImagePath);
            }
        }
    }

    private boolean isProfileImageAllowed(Uri uri) {
        if (getUriSize(uri) > MAX_PROFILE_IMAGE_BYTES) {
            showProfileImageWarning("Seçilen resim dosyası çok büyük.");
            return false;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(inputStream, null, options);
        } catch (IOException ignored) {
            showProfileImageWarning("Resim okunamadı.");
            return false;
        }

        if (options.outWidth <= 0 || options.outHeight <= 0) {
            showProfileImageWarning("Geçerli bir resim seç.");
            return false;
        }
        if (options.outWidth > MAX_PROFILE_IMAGE_SIDE || options.outHeight > MAX_PROFILE_IMAGE_SIDE) {
            showProfileImageWarning("Seçilen resmin çözünürlüğü çok yüksek.");
            return false;
        }
        return true;
    }

    private long getUriSize(Uri uri) {
        try (AssetFileDescriptor descriptor = getContentResolver().openAssetFileDescriptor(uri, "r")) {
            if (descriptor != null) {
                return descriptor.getLength();
            }
        } catch (IOException ignored) {
            return -1L;
        }
        return -1L;
    }

    private void showProfileImageWarning(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateStoredUsername(String oldUsername, String newUsername) {
        AppSettings.setCurrentUser(this, newUsername);
        if (AppSettings.isRememberedLoginEnabled(this)
                && oldUsername != null
                && oldUsername.equalsIgnoreCase(AppSettings.getRememberedUser(this))) {
            AppSettings.setRememberedLogin(this, newUsername, true);
        }
    }

    private void applyProfileImage(ImageView imageView, String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            imageView.setPadding(dp(12), dp(12), dp(12), dp(12));
            imageView.setImageResource(R.drawable.ic_person_circle_24);
            return;
        }
        imageView.setPadding(0, 0, 0, 0);
        imageView.setImageURI(Uri.parse(imagePath));
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private float dpFloat(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private static class CategoryItem {
        final String name;
        final List<Word> words;
        boolean expanded;

        CategoryItem(String name, List<Word> words) {
            this.name = name;
            this.words = words;
        }
    }

    class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
        private List<CategoryItem> items;

        CategoryAdapter(List<CategoryItem> items) {
            this.items = items;
        }

        void updateItems(List<CategoryItem> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_item, parent, false);
            return new CategoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            CategoryItem item = items.get(position);
            holder.tvEng.setText(item.name);
            holder.tvTur.setText(item.words.size() + " kelime");
            holder.tvLevel.setText("Kelime listesi");
            holder.tvSample.setText(formatCategoryWords(item.words));
            holder.ivWord.setVisibility(View.GONE);
            holder.bindExpandedState(item.expanded);

            View.OnClickListener toggleListener = v -> {
                item.expanded = !item.expanded;
                holder.bindExpandedState(item.expanded);
            };
            holder.itemView.setOnClickListener(toggleListener);
            holder.tvToggle.setOnClickListener(toggleListener);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private String formatCategoryWords(List<Word> words) {
            if (words == null || words.isEmpty()) {
                return "";
            }

            List<Word> sorted = new ArrayList<>(words);
            Collections.sort(sorted, AccountActivity.this::compareEngSafe);
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < sorted.size(); i++) {
                Word word = sorted.get(i);
                if (i > 0) {
                    builder.append("\n");
                }
                String eng = word.eng == null || word.eng.trim().isEmpty() ? "-" : word.eng.trim();
                String tur = word.tur == null || word.tur.trim().isEmpty() ? "-" : word.tur.trim();
                builder.append(i + 1).append(". ").append(eng).append(" - ").append(tur);
            }
            return builder.toString();
        }

        class CategoryViewHolder extends RecyclerView.ViewHolder {
            TextView tvEng, tvTur, tvSample, tvLevel, tvToggle;
            ImageView ivWord;
            View detailsContainer;

            CategoryViewHolder(View itemView) {
                super(itemView);
                tvEng = itemView.findViewById(R.id.tvEng);
                tvTur = itemView.findViewById(R.id.tvTur);
                tvSample = itemView.findViewById(R.id.tvSample);
                tvLevel = itemView.findViewById(R.id.tvLevel);
                tvToggle = itemView.findViewById(R.id.tvToggle);
                ivWord = itemView.findViewById(R.id.ivWord);
                detailsContainer = itemView.findViewById(R.id.detailsContainer);
            }

            void bindExpandedState(boolean expanded) {
                detailsContainer.setVisibility(expanded ? View.VISIBLE : View.GONE);
                tvToggle.setText(expanded ? "Gizle -" : "Detay +");
            }
        }
    }
}
