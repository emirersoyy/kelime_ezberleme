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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AccountActivity extends BottomNavActivity {
    private static final int REQUEST_PICK_PROFILE_IMAGE = 42;
    private static final long MAX_PROFILE_IMAGE_BYTES = 2L * 1024L * 1024L;
    private static final int MAX_PROFILE_IMAGE_SIDE = 2048;
    private static final String SORT_ALPHA_ASC = "alpha_asc";
    private static final String SORT_ALPHA_DESC = "alpha_desc";
    private static final String SORT_LEVEL_DESC = "level_desc";
    private static final String SORT_LEVEL_ASC = "level_asc";

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

    private LinearLayout llEmbeddedSummaryChips;
    private LinearLayout llEmbeddedCategoryStats;
    private TextView tvEmbeddedOverallStats;

    private Spinner spEmbeddedSort;
    private RecyclerView rvEmbeddedWords;
    private WordAdapter wordAdapter;
    private final List<Word> allWords = new ArrayList<>();
    private boolean showingAnalysis = true;

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
        refreshEmbeddedContent();
        updateTabStyles();
    }

    private void bindProfileHeader() {
        imgAccountProfile = findViewById(R.id.imgAccountProfile);
        tvCurrentUser = findViewById(R.id.tvCurrentUser);
        tvFullName = findViewById(R.id.tvFullName);
        MaterialButton btnEditProfile = findViewById(R.id.btnEditProfile);
        MaterialButton btnSettings = findViewById(R.id.btnAccountSettings);

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, SettingsActivity.class)));
    }

    private void bindEmbeddedSections() {
        btnTabAnalysis = findViewById(R.id.btnTabAnalysis);
        btnTabWords = findViewById(R.id.btnTabWords);
        layoutAnalysisSection = findViewById(R.id.layoutAnalysisSection);
        layoutWordsSection = findViewById(R.id.layoutWordsSection);

        llEmbeddedSummaryChips = findViewById(R.id.llEmbeddedSummaryChips);
        llEmbeddedCategoryStats = findViewById(R.id.llEmbeddedCategoryStats);
        tvEmbeddedOverallStats = findViewById(R.id.tvEmbeddedOverallStats);
        ImageButton btnEmbeddedPrint = findViewById(R.id.btnEmbeddedPrint);
        MaterialButton btnEmbeddedResetData = findViewById(R.id.btnEmbeddedResetData);

        spEmbeddedSort = findViewById(R.id.spEmbeddedSort);
        rvEmbeddedWords = findViewById(R.id.rvEmbeddedWords);
        MaterialButton btnEmbeddedAddWord = findViewById(R.id.btnEmbeddedAddWord);

        btnTabAnalysis.setOnClickListener(v -> showSection(true));
        btnTabWords.setOnClickListener(v -> showSection(false));
        btnEmbeddedPrint.setOnClickListener(v -> printReport());
        btnEmbeddedResetData.setOnClickListener(v -> confirmReset());
        btnEmbeddedAddWord.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, AddWordActivity.class)));
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

        tvCurrentUser.setText(username == null || username.trim().isEmpty() ? "Bilinmiyor" : username);
        if (fullName == null || fullName.trim().isEmpty()) {
            tvFullName.setText("Gerçek ad eklenmedi");
        } else {
            tvFullName.setText(fullName.trim());
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
        llEmbeddedSummaryChips.removeAllViews();
        llEmbeddedCategoryStats.removeAllViews();

        List<Word> words = db.getAllWords();
        if (words.isEmpty()) {
            tvEmbeddedOverallStats.setText("Henüz kelime eklenmemiş.");
            addEmbeddedEmptyState("Gösterilecek veri yok.");
            return;
        }

        Collections.sort(words, Comparator.comparing(w -> w.eng == null ? "" : w.eng.toLowerCase(Locale.US)));

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

        tvEmbeddedOverallStats.setText("Toplam " + words.size()
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
        cardParams.rightMargin = dp(10);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(getResources().getColor(R.color.surface));
        card.setCardElevation(dpFloat(2f));
        card.setRadius(getResources().getDimension(R.dimen.radius_lg));
        card.setStrokeColor(getResources().getColor(R.color.divider));
        card.setStrokeWidth(1);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(16), dp(18), dp(16));

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
        llEmbeddedSummaryChips.addView(card);
    }

    private void addEmbeddedEmptyState(String message) {
        TextView text = new TextView(this);
        text.setText(message);
        text.setTextColor(getResources().getColor(R.color.text_secondary));
        text.setTextSize(15);
        text.setPadding(0, dp(12), 0, dp(12));
        llEmbeddedCategoryStats.addView(text);
    }

    private void addStatusSection(String title, List<Word> items, int accentColorResId) {
        int accentColor = getResources().getColor(accentColorResId);

        MaterialCardView sectionCard = new MaterialCardView(this);
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sectionParams.bottomMargin = dp(14);
        sectionCard.setLayoutParams(sectionParams);
        sectionCard.setCardBackgroundColor(getResources().getColor(R.color.surface));
        sectionCard.setCardElevation(dpFloat(2f));
        sectionCard.setRadius(getResources().getDimension(R.dimen.radius_lg));
        sectionCard.setStrokeColor(getResources().getColor(R.color.divider));
        sectionCard.setStrokeWidth(1);

        LinearLayout sectionContent = new LinearLayout(this);
        sectionContent.setOrientation(LinearLayout.VERTICAL);
        sectionContent.setPadding(dp(18), dp(16), dp(18), dp(10));

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
            empty.setPadding(0, dp(10), 0, dp(4));
            sectionContent.addView(empty);
            sectionCard.addView(sectionContent);
            llEmbeddedCategoryStats.addView(sectionCard);
            return;
        }

        TextView subtitle = new TextView(this);
        subtitle.setText("İngilizce • Türkçe • Kategori • Seviye");
        subtitle.setTextColor(getResources().getColor(R.color.text_secondary));
        subtitle.setTextSize(12);
        subtitle.setPadding(0, dp(2), 0, dp(12));
        sectionContent.addView(subtitle);

        for (Word word : items) {
            sectionContent.addView(createAnalysisWordCard(word, accentColor));
        }

        sectionCard.addView(sectionContent);
        llEmbeddedCategoryStats.addView(sectionCard);
    }

    private MaterialCardView createAnalysisWordCard(Word word, int accentColor) {
        String category = word.category == null || word.category.trim().isEmpty() ? "Genel" : word.category.trim();
        String english = word.eng == null || word.eng.trim().isEmpty() ? "-" : word.eng.trim();
        String turkish = word.tur == null || word.tur.trim().isEmpty() ? "-" : word.tur.trim();

        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(10);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(getResources().getColor(R.color.background));
        card.setCardElevation(dpFloat(0.5f));
        card.setRadius(getResources().getDimension(R.dimen.radius_sm));
        card.setStrokeColor(getResources().getColor(R.color.divider));
        card.setStrokeWidth(1);

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
        levelText.setBackgroundResource(R.drawable.soft_chip_bg);

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
                        canvas.drawText(tvEmbeddedOverallStats.getText().toString(), left, 100, bodyPaint);
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
        lines.add(tvEmbeddedOverallStats.getText().toString());
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
        wordAdapter = new WordAdapter(new ArrayList<>());
        rvEmbeddedWords.setAdapter(wordAdapter);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"A-Z", "Z-A", "Yüksek seviye", "Düşük seviye"}
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
        allWords.clear();
        allWords.addAll(db.getAllWords());
        applyWordSorting(AppSettings.getWordsSortOrder(this));
    }

    private void applyWordSorting(String sortOrder) {
        List<Word> sorted = new ArrayList<>(allWords);
        switch (sortOrder) {
            case SORT_ALPHA_DESC:
                Collections.sort(sorted, (a, b) -> compareEngSafe(b, a));
                break;
            case SORT_LEVEL_DESC:
                Collections.sort(sorted, Comparator.comparingInt((Word w) -> w.stepCount).reversed().thenComparing(this::compareEngSafe));
                break;
            case SORT_LEVEL_ASC:
                Collections.sort(sorted, Comparator.comparingInt((Word w) -> w.stepCount).thenComparing(this::compareEngSafe));
                break;
            case SORT_ALPHA_ASC:
            default:
                Collections.sort(sorted, this::compareEngSafe);
                break;
        }
        wordAdapter.updateWords(sorted);
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

        DatabaseHelper.UserProfile profile = db.getUserProfile(currentUser);
        String profileUsername = profile == null ? currentUser : profile.username;
        selectedProfileImagePath = profile == null ? "" : profile.profileImagePath;
        etUsername.setText(profileUsername);
        etFullName.setText(profile == null ? "" : profile.fullName);
        applyProfileImage(dialogProfileImage, selectedProfileImagePath);

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

    class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {
        private List<Word> words;

        WordAdapter(List<Word> words) {
            this.words = words;
        }

        void updateWords(List<Word> newWords) {
            this.words = newWords;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_item, parent, false);
            return new WordViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
            Word word = words.get(position);
            holder.tvEng.setText(word.eng);
            holder.tvTur.setText(word.tur);
            holder.tvLevel.setText(getLevelText(word.stepCount));
            List<String> samples = db.getDisplaySamplesForWord(word);
            holder.tvSample.setText(formatSamples(samples));
            WordImageLoader.load(holder.ivWord, word.pic);
            holder.bindExpandedState(word.expanded);

            View.OnClickListener toggleListener = v -> {
                word.expanded = !word.expanded;
                holder.bindExpandedState(word.expanded);
            };
            holder.itemView.setOnClickListener(toggleListener);
            holder.tvToggle.setOnClickListener(toggleListener);
        }

        @Override
        public int getItemCount() {
            return words.size();
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

        private String getLevelText(int stepCount) {
            if (stepCount <= 0) {
                return "Seviye 0";
            }
            return "Seviye " + stepCount;
        }

        class WordViewHolder extends RecyclerView.ViewHolder {
            TextView tvEng, tvTur, tvSample, tvLevel, tvToggle;
            ImageView ivWord;
            View detailsContainer;

            WordViewHolder(View itemView) {
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
