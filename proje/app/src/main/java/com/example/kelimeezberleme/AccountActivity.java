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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
    private static final int ANALYSIS_PAGE_SIZE = 75;
    private static final int ANALYSIS_PREFETCH_DISTANCE = 20;
    private static final int ANALYSIS_KEEP_BEFORE = 225;
    private static final int ANALYSIS_KEEP_AFTER = 175;
    private static final String DEFAULT_CATEGORY_LABEL = "Genel";
    private static final String LEVEL_FILTER_PREFIX = "level_";
    private static final String LEVEL_LABEL_PREFIX = "Seviye ";
    private static final String WORD_COUNT_SUFFIX = " kelime";
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
    private View layoutEmbeddedFilterControls;
    private View viewEmbeddedFilterDivider;
    private View layoutEmbeddedFilterSummary;
    private NestedScrollView svAccountRoot;
    private MaterialButton btnScrollAccountTop;
    private MaterialCardView cardEmbeddedAnalysisContent;
    private MaterialCardView cardStickyAnalysisFilters;
    private MaterialCardView cardStickyWordsFilters;
    private MaterialButton btnEmbeddedLoadMoreAnalysis;

    private LinearLayout llEmbeddedAllChip;
    private LinearLayout llEmbeddedSummaryChips;
    private LinearLayout llStickyAllChip;
    private LinearLayout llStickySummaryChips;
    private LinearLayout llEmbeddedCategoryStats;
    private TextView tvEmbeddedFilterSummary;
    private TextView tvStickyFilterSummary;
    private TextView tvStickyWordsCount;
    private AppCompatTextView tvEmbeddedAnalysisSort;
    private AppCompatTextView tvStickyAnalysisSort;
    private AppCompatTextView tvStickyWordsSort;
    private HorizontalScrollView hsvEmbeddedSummaryChips;
    private HorizontalScrollView hsvStickySummaryChips;
    private View viewEmbeddedFadeLeft;
    private View viewEmbeddedFadeRight;
    private View layoutEmbeddedWordsHeader;
    private View layoutEmbeddedWordsFilter;

    private AppCompatTextView tvEmbeddedWordsSort;
    private TextView tvEmbeddedWordsCountInline;
    private RecyclerView rvEmbeddedWords;
    private CategoryAdapter categoryAdapter;
    private final List<CategoryItem> allCategories = new ArrayList<>();
    private boolean showingAnalysis = true;
    private String selectedAnalysisFilter = ANALYSIS_FILTER_ALL;
    private boolean analysisSortAscending = true;
    private List<Word> analysisSourceWords = new ArrayList<>();
    private int analysisWindowStart = 0;
    private int analysisWindowEnd = 0;
    private boolean analysisWindowUpdateLocked = false;
    private boolean skipInitialResumeRefresh = false;
    private boolean wordsContentLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        db = new DatabaseHelper(this);
        bindProfileHeader();
        bindEmbeddedSections();

        loadProfile();
        setupWordsSection();
        showSection(true);
        refreshAnalysisContent();
        skipInitialResumeRefresh = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (skipInitialResumeRefresh) {
            skipInitialResumeRefresh = false;
            return;
        }
        loadProfile();
        analysisSortAscending = true;
        refreshAnalysisContent();
        wordsContentLoaded = false;
        if (!showingAnalysis) {
            refreshWordsContent();
        }
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
        layoutEmbeddedFilterControls = findViewById(R.id.layoutEmbeddedFilterControls);
        viewEmbeddedFilterDivider = findViewById(R.id.viewEmbeddedFilterDivider);
        layoutEmbeddedFilterSummary = findViewById(R.id.layoutEmbeddedFilterSummary);
        layoutEmbeddedWordsHeader = findViewById(R.id.layoutEmbeddedWordsHeader);
        layoutEmbeddedWordsFilter = findViewById(R.id.layoutEmbeddedWordsFilter);
        svAccountRoot = findViewById(R.id.svAccountRoot);
        btnScrollAccountTop = findViewById(R.id.btnScrollAccountTop);
        cardEmbeddedAnalysisContent = findViewById(R.id.cardEmbeddedAnalysisContent);
        cardStickyAnalysisFilters = findViewById(R.id.cardStickyAnalysisFilters);
        cardStickyWordsFilters = findViewById(R.id.cardStickyWordsFilters);

        llEmbeddedAllChip = findViewById(R.id.llEmbeddedAllChip);
        llEmbeddedSummaryChips = findViewById(R.id.llEmbeddedSummaryChips);
        llStickyAllChip = findViewById(R.id.llStickyAllChip);
        llStickySummaryChips = findViewById(R.id.llStickySummaryChips);
        llEmbeddedCategoryStats = findViewById(R.id.llEmbeddedCategoryStats);
        tvEmbeddedFilterSummary = findViewById(R.id.tvEmbeddedFilterSummary);
        tvStickyFilterSummary = findViewById(R.id.tvStickyFilterSummary);
        tvStickyWordsCount = findViewById(R.id.tvStickyWordsCount);
        tvEmbeddedAnalysisSort = findViewById(R.id.tvEmbeddedAnalysisSort);
        tvStickyAnalysisSort = findViewById(R.id.tvStickyAnalysisSort);
        tvStickyWordsSort = findViewById(R.id.tvStickyWordsSort);
        tvEmbeddedWordsCountInline = findViewById(R.id.tvEmbeddedWordsCountInline);
        hsvEmbeddedSummaryChips = findViewById(R.id.hsvEmbeddedSummaryChips);
        hsvStickySummaryChips = findViewById(R.id.hsvStickySummaryChips);
        viewEmbeddedFadeLeft = findViewById(R.id.viewEmbeddedFadeLeft);
        viewEmbeddedFadeRight = findViewById(R.id.viewEmbeddedFadeRight);
        btnEmbeddedLoadMoreAnalysis = findViewById(R.id.btnEmbeddedLoadMoreAnalysis);
        MaterialButton btnEmbeddedPrint = findViewById(R.id.btnEmbeddedPrint);

        tvEmbeddedWordsSort = findViewById(R.id.tvEmbeddedWordsSort);
        rvEmbeddedWords = findViewById(R.id.rvEmbeddedWords);
        MaterialButton btnEmbeddedAddWord = findViewById(R.id.btnEmbeddedAddWord);

        btnTabAnalysis.setOnClickListener(v -> showSection(true));
        btnTabWords.setOnClickListener(v -> showSection(false));
        btnEmbeddedPrint.setOnClickListener(v -> printReport());
        tvEmbeddedAnalysisSort.setOnClickListener(v -> {
            analysisSortAscending = !analysisSortAscending;
            refreshAnalysisContent();
        });
        tvStickyAnalysisSort.setOnClickListener(v -> {
            analysisSortAscending = !analysisSortAscending;
            refreshAnalysisContent();
        });
        tvStickyWordsSort.setOnClickListener(v -> {
            String nextSort = getNextCategorySortOrder(AppSettings.getCategorySortOrder(AccountActivity.this));
            AppSettings.setCategorySortOrder(AccountActivity.this, nextSort);
            applyWordSorting(nextSort);
        });
        tvEmbeddedWordsSort.setOnClickListener(v -> {
            String nextSort = getNextCategorySortOrder(AppSettings.getCategorySortOrder(AccountActivity.this));
            AppSettings.setCategorySortOrder(AccountActivity.this, nextSort);
            applyWordSorting(nextSort);
        });
        hsvEmbeddedSummaryChips.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) ->
                updateSummaryChipFadeState());
        btnEmbeddedAddWord.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, AddWordActivity.class)));
        btnEmbeddedLoadMoreAnalysis.setVisibility(View.GONE);
        btnScrollAccountTop.setOnClickListener(v -> scrollAnalysisToTop());
        svAccountRoot.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
        updateScrollToTopButton();
        updateStickyAnalysisFiltersState();
        updateStickyWordsFiltersState();
        handleAnalysisScrollWindow();
    });
    }

    private void showSection(boolean analysis) {
        showingAnalysis = analysis;
        layoutAnalysisSection.setVisibility(analysis ? View.VISIBLE : View.GONE);
        layoutWordsSection.setVisibility(analysis ? View.GONE : View.VISIBLE);
        if (!analysis && !wordsContentLoaded) {
            refreshWordsContent();
        }
        updateStickyAnalysisFiltersState();
        updateStickyWordsFiltersState();
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

    private void refreshAnalysisContent() {
        llEmbeddedAllChip.removeAllViews();
        llEmbeddedSummaryChips.removeAllViews();
        llStickyAllChip.removeAllViews();
        llStickySummaryChips.removeAllViews();
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
        addMetricChip(llStickyAllChip, "Tümü", R.color.primary, ANALYSIS_FILTER_ALL);
        for (int level = 0; level <= MAX_ANALYSIS_LEVEL; level++) {
            addMetricChip(llEmbeddedSummaryChips, "Seviye " + level, getLevelAccentColorRes(level), createLevelFilterKey(level));
            addMetricChip(llStickySummaryChips, "Seviye " + level, getLevelAccentColorRes(level), createLevelFilterKey(level));
        }

        List<Word> filteredWords = new ArrayList<>();
        String summaryText;
        if (ANALYSIS_FILTER_ALL.equals(selectedAnalysisFilter)) {
            filteredWords.addAll(words);
            summaryText = words.size() + WORD_COUNT_SUFFIX;
        } else {
            Integer selectedLevel = parseLevelFilter(selectedAnalysisFilter);
            if (selectedLevel != null && wordsByLevel.containsKey(selectedLevel)) {
                filteredWords.addAll(wordsByLevel.get(selectedLevel));
            }
            summaryText = filteredWords.size() + WORD_COUNT_SUFFIX;
        }

        Collections.sort(filteredWords, analysisSortAscending
                ? Comparator.comparing(w -> w.eng == null ? "" : w.eng.toLowerCase(Locale.US))
                : (left, right) -> {
                    String a = left == null || left.eng == null ? "" : left.eng.toLowerCase(Locale.US);
                    String b = right == null || right.eng == null ? "" : right.eng.toLowerCase(Locale.US);
                    return b.compareTo(a);
                });

        analysisSourceWords = filteredWords;
        analysisWindowStart = 0;
        analysisWindowEnd = 0;
        analysisWindowUpdateLocked = false;
        btnEmbeddedLoadMoreAnalysis.setVisibility(View.GONE);

        tvEmbeddedFilterSummary.setText(summaryText);
        tvStickyFilterSummary.setText(summaryText);
        updateAnalysisSortView();
        hsvEmbeddedSummaryChips.post(this::updateSummaryChipFadeState);
        syncStickyFilterScroll();
        svAccountRoot.post(this::updateStickyAnalysisFiltersState);
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

        renderAnalysisWindow(0, Math.min(ANALYSIS_PAGE_SIZE, items.size()), false);
    }

    private void renderAnalysisWindow(int start, int end, boolean keepFirstVisibleStable) {
        if (analysisSourceWords == null) {
            return;
        }

        int safeStart = Math.max(0, Math.min(start, analysisSourceWords.size()));
        int safeEnd = Math.max(safeStart, Math.min(end, analysisSourceWords.size()));
        int anchorIndex = keepFirstVisibleStable ? findFirstVisibleAnalysisIndex() : -1;
        int anchorOffset = anchorIndex >= 0 ? getAnalysisCardTopInScroll(anchorIndex) - svAccountRoot.getScrollY() : 0;

        analysisWindowUpdateLocked = true;
        llEmbeddedCategoryStats.removeAllViews();
        for (int i = safeStart; i < safeEnd; i++) {
            llEmbeddedCategoryStats.addView(createTaggedAnalysisWordCard(i));
        }
        analysisWindowStart = safeStart;
        analysisWindowEnd = safeEnd;
        updateScrollToTopButton();

        if (anchorIndex >= safeStart && anchorIndex < safeEnd) {
            llEmbeddedCategoryStats.post(() -> {
                int newAnchorTop = getAnalysisCardTopInScroll(anchorIndex);
                svAccountRoot.scrollTo(0, Math.max(0, newAnchorTop - anchorOffset));
                analysisWindowUpdateLocked = false;
                updateScrollToTopButton();
            });
        } else {
            analysisWindowUpdateLocked = false;
        }
    }

    private void handleAnalysisScrollWindow() {
        if (analysisWindowUpdateLocked
                || !showingAnalysis
                || analysisSourceWords == null
                || analysisSourceWords.size() <= ANALYSIS_PAGE_SIZE
                || llEmbeddedCategoryStats.getChildCount() == 0) {
            return;
        }

        int firstVisible = findFirstVisibleAnalysisIndex();
        int lastVisible = findLastVisibleAnalysisIndex();
        if (firstVisible < 0 || lastVisible < 0) {
            return;
        }

        if (lastVisible >= analysisWindowEnd - ANALYSIS_PREFETCH_DISTANCE) {
            appendAnalysisWindow(Math.min(analysisSourceWords.size(), analysisWindowEnd + ANALYSIS_PAGE_SIZE));
        }
        if (firstVisible <= analysisWindowStart + ANALYSIS_PREFETCH_DISTANCE) {
            prependAnalysisWindow(Math.max(0, analysisWindowStart - ANALYSIS_PAGE_SIZE));
        }

        pruneAnalysisWindow(firstVisible, lastVisible);
        updateScrollToTopButton();
    }

    private MaterialCardView createTaggedAnalysisWordCard(int index) {
        Word word = analysisSourceWords.get(index);
        MaterialCardView card = createAnalysisWordCard(word, getLevelAccentColor(word.stepCount));
        card.setTag(index);
        return card;
    }

    private void appendAnalysisWindow(int targetEnd) {
        if (targetEnd <= analysisWindowEnd) {
            return;
        }

        analysisWindowUpdateLocked = true;
        for (int i = analysisWindowEnd; i < targetEnd; i++) {
            llEmbeddedCategoryStats.addView(createTaggedAnalysisWordCard(i));
        }
        analysisWindowEnd = targetEnd;
        analysisWindowUpdateLocked = false;
    }

    private void prependAnalysisWindow(int targetStart) {
        if (targetStart >= analysisWindowStart) {
            return;
        }

        analysisWindowUpdateLocked = true;
        int previousHeight = llEmbeddedCategoryStats.getHeight();
        for (int i = analysisWindowStart - 1; i >= targetStart; i--) {
            llEmbeddedCategoryStats.addView(createTaggedAnalysisWordCard(i), 0);
        }
        analysisWindowStart = targetStart;
        llEmbeddedCategoryStats.post(() -> {
            int addedHeight = Math.max(0, llEmbeddedCategoryStats.getHeight() - previousHeight);
            svAccountRoot.scrollTo(0, svAccountRoot.getScrollY() + addedHeight);
            analysisWindowUpdateLocked = false;
            updateScrollToTopButton();
        });
    }

    private void pruneAnalysisWindow(int firstVisible, int lastVisible) {
        int minStart = Math.max(0, firstVisible - ANALYSIS_KEEP_BEFORE);
        int maxEnd = Math.min(analysisSourceWords.size(), lastVisible + ANALYSIS_KEEP_AFTER);

        if (minStart > analysisWindowStart + ANALYSIS_PAGE_SIZE) {
            removeAnalysisCardsBefore(minStart);
        }
        if (maxEnd < analysisWindowEnd - ANALYSIS_PAGE_SIZE) {
            removeAnalysisCardsAfter(maxEnd);
        }
    }

    private void removeAnalysisCardsBefore(int targetStart) {
        analysisWindowUpdateLocked = true;
        int removedHeight = 0;
        while (llEmbeddedCategoryStats.getChildCount() > 0) {
            View firstChild = llEmbeddedCategoryStats.getChildAt(0);
            int index = getAnalysisIndex(firstChild);
            if (index < 0 || index >= targetStart) {
                break;
            }
            removedHeight += firstChild.getHeight();
            ViewGroup.MarginLayoutParams params = firstChild.getLayoutParams() instanceof ViewGroup.MarginLayoutParams
                    ? (ViewGroup.MarginLayoutParams) firstChild.getLayoutParams()
                    : null;
            if (params != null) {
                removedHeight += params.topMargin + params.bottomMargin;
            }
            llEmbeddedCategoryStats.removeViewAt(0);
            analysisWindowStart = index + 1;
        }
        if (removedHeight > 0) {
            svAccountRoot.scrollTo(0, Math.max(0, svAccountRoot.getScrollY() - removedHeight));
        }
        analysisWindowUpdateLocked = false;
    }

    private void removeAnalysisCardsAfter(int targetEnd) {
        analysisWindowUpdateLocked = true;
        while (llEmbeddedCategoryStats.getChildCount() > 0) {
            int lastChildPosition = llEmbeddedCategoryStats.getChildCount() - 1;
            View lastChild = llEmbeddedCategoryStats.getChildAt(lastChildPosition);
            int index = getAnalysisIndex(lastChild);
            if (index < 0 || index < targetEnd) {
                break;
            }
            llEmbeddedCategoryStats.removeViewAt(lastChildPosition);
            analysisWindowEnd = index;
        }
        analysisWindowUpdateLocked = false;
    }

    private int findFirstVisibleAnalysisIndex() {
        int viewportTop = svAccountRoot.getScrollY();
        int viewportBottom = viewportTop + svAccountRoot.getHeight();
        for (int i = 0; i < llEmbeddedCategoryStats.getChildCount(); i++) {
            View child = llEmbeddedCategoryStats.getChildAt(i);
            int childTop = getViewTopInScroll(child);
            int childBottom = childTop + child.getHeight();
            if (childBottom > viewportTop && childTop < viewportBottom) {
                return getAnalysisIndex(child);
            }
        }
        return -1;
    }

    private int findLastVisibleAnalysisIndex() {
        int viewportTop = svAccountRoot.getScrollY();
        int viewportBottom = viewportTop + svAccountRoot.getHeight();
        for (int i = llEmbeddedCategoryStats.getChildCount() - 1; i >= 0; i--) {
            View child = llEmbeddedCategoryStats.getChildAt(i);
            int childTop = getViewTopInScroll(child);
            int childBottom = childTop + child.getHeight();
            if (childBottom > viewportTop && childTop < viewportBottom) {
                return getAnalysisIndex(child);
            }
        }
        return -1;
    }

    private int getAnalysisCardTopInScroll(int analysisIndex) {
        for (int i = 0; i < llEmbeddedCategoryStats.getChildCount(); i++) {
            View child = llEmbeddedCategoryStats.getChildAt(i);
            if (getAnalysisIndex(child) == analysisIndex) {
                return getViewTopInScroll(child);
            }
        }
        return svAccountRoot.getScrollY();
    }

    private int getAnalysisIndex(View view) {
        Object tag = view.getTag();
        return tag instanceof Integer ? (Integer) tag : -1;
    }

    private int getViewTopInScroll(View view) {
        int top = view.getTop();
        View parent = (View) view.getParent();
        while (parent != null && parent != svAccountRoot.getChildAt(0)) {
            top += parent.getTop();
            if (!(parent.getParent() instanceof View)) {
                break;
            }
            parent = (View) parent.getParent();
        }
        if (parent == svAccountRoot.getChildAt(0)) {
            top += parent.getTop();
        }
        return top;
    }

    private void updateScrollToTopButton() {
        if (btnScrollAccountTop == null || svAccountRoot == null || llEmbeddedCategoryStats == null) {
            return;
        }
        boolean firstWordGone = false;
        if (showingAnalysis && analysisSourceWords != null && !analysisSourceWords.isEmpty()) {
            firstWordGone = svAccountRoot.getScrollY() > 0 && analysisWindowStart > 0;
            if (!firstWordGone) {
                for (int i = 0; i < llEmbeddedCategoryStats.getChildCount(); i++) {
                    View child = llEmbeddedCategoryStats.getChildAt(i);
                    if (getAnalysisIndex(child) == 0) {
                        firstWordGone = svAccountRoot.getScrollY() > 0
                                && getViewTopInScroll(child) + child.getHeight() <= svAccountRoot.getScrollY();
                        break;
                    }
                }
            }
        }
        btnScrollAccountTop.setVisibility(firstWordGone ? View.VISIBLE : View.GONE);
    }

    private void scrollAnalysisToTop() {
        analysisWindowUpdateLocked = true;
        svAccountRoot.stopNestedScroll();
        svAccountRoot.scrollTo(0, 0);
        if (analysisSourceWords != null && !analysisSourceWords.isEmpty()) {
            renderAnalysisWindow(0, Math.min(ANALYSIS_PAGE_SIZE, analysisSourceWords.size()), false);
        }
        svAccountRoot.post(() -> {
            svAccountRoot.scrollTo(0, 0);
            analysisWindowUpdateLocked = false;
            updateScrollToTopButton();
        });
    }

    private MaterialCardView createAnalysisWordCard(Word word, int accentColor) {
        DisplayTextNormalizer.normalizeWordForDisplay(word);
        String category = word.category == null || word.category.trim().isEmpty() ? DEFAULT_CATEGORY_LABEL : word.category.trim();
        String english = word.eng == null || word.eng.trim().isEmpty() ? "-" : word.eng.trim();
        String turkish = word.tur == null || word.tur.trim().isEmpty() ? "-" : word.tur.trim();
        boolean showLevelBadge = ANALYSIS_FILTER_ALL.equals(selectedAnalysisFilter);

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
        levelText.setText(LEVEL_LABEL_PREFIX + Math.max(word.stepCount, 0));
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
        sampleText.setTextColor(getResources().getColor(R.color.text_primary));
        sampleText.setTextSize(13);
        sampleText.setVisibility(View.GONE);

        ImageView wordImage = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dp(76), dp(76));
        imageParams.leftMargin = dp(12);
        wordImage.setLayoutParams(imageParams);
        wordImage.setBackgroundResource(R.drawable.soft_chip_bg);
        wordImage.setClipToOutline(true);
        wordImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        wordImage.setVisibility(View.GONE);

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
            if (word.expanded && sampleText.getTag() == null) {
                List<String> samples = db.getDisplaySamplesForWord(word);
                sampleText.setText(formatSamples(samples));
                sampleText.setVisibility(samples == null || samples.isEmpty() ? View.GONE : View.VISIBLE);
                WordImageLoader.load(wordImage, word.pic);
                sampleText.setTag(Boolean.TRUE);
            }
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
        return LEVEL_FILTER_PREFIX + level;
    }

    private Integer parseLevelFilter(String filterKey) {
        if (filterKey == null || !filterKey.startsWith(LEVEL_FILTER_PREFIX)) {
            return null;
        }
        try {
            return Integer.parseInt(filterKey.substring(LEVEL_FILTER_PREFIX.length()));
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
        applyAnalysisSortView(tvEmbeddedAnalysisSort, accentColor);
        applyAnalysisSortView(tvStickyAnalysisSort, accentColor);
    }

    private void applyAnalysisSortView(AppCompatTextView sortView, int accentColor) {
        if (sortView == null) {
            return;
        }
        sortView.setText("Alfabeye göre");
        sortView.setBackground(createRoundedChipBackground(accentColor, false));
        sortView.setCompoundDrawablePadding(dp(6));
        sortView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                analysisSortAscending ? R.drawable.ic_chevron_down_18 : R.drawable.ic_chevron_up_18,
                0
        );
    }

    private void syncStickyFilterScroll() {
        if (hsvEmbeddedSummaryChips == null || hsvStickySummaryChips == null) {
            return;
        }
        hsvStickySummaryChips.post(() -> hsvStickySummaryChips.scrollTo(hsvEmbeddedSummaryChips.getScrollX(), 0));
    }

    private void updateStickyAnalysisFiltersState() {
        if (cardStickyAnalysisFilters == null || cardEmbeddedAnalysisContent == null || svAccountRoot == null) {
            return;
        }
        boolean shouldStick = showingAnalysis
                && layoutAnalysisSection.getVisibility() == View.VISIBLE
                && svAccountRoot.getScrollY() >= Math.max(0, getViewTopInScroll(cardEmbeddedAnalysisContent) - dp(8));
        cardStickyAnalysisFilters.setVisibility(shouldStick ? View.VISIBLE : View.GONE);
        int embeddedVisibility = shouldStick ? View.INVISIBLE : View.VISIBLE;
        layoutEmbeddedFilterControls.setVisibility(embeddedVisibility);
        viewEmbeddedFilterDivider.setVisibility(embeddedVisibility);
        layoutEmbeddedFilterSummary.setVisibility(embeddedVisibility);
    }

    private void updateStickyWordsFiltersState() {
        if (cardStickyWordsFilters == null || layoutEmbeddedWordsFilter == null || svAccountRoot == null) {
            return;
        }
        boolean shouldStick = !showingAnalysis
                && layoutWordsSection.getVisibility() == View.VISIBLE
                && svAccountRoot.getScrollY() >= Math.max(0, getViewTopInScroll(layoutEmbeddedWordsFilter) - dp(8));
        cardStickyWordsFilters.setVisibility(shouldStick ? View.VISIBLE : View.GONE);
        layoutEmbeddedWordsFilter.setVisibility(shouldStick ? View.INVISIBLE : View.VISIBLE);
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
                try (FileOutputStream outputStream = new FileOutputStream(destination.getFileDescriptor())) {
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

                        pdfDocument.writeTo(outputStream);
                    } finally {
                        pdfDocument.close();
                    }
                } catch (IOException e) {
                    callback.onWriteFailed(e.toString());
                    return;
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
        updateWordsSortView(AppSettings.getCategorySortOrder(this));
    }

    private void refreshWordsContent() {
        allCategories.clear();
        Map<String, CategoryItem> grouped = new LinkedHashMap<>();
        boolean defaultWide = getResources().getConfiguration().screenWidthDp >= 600;
        for (Word word : WordleWordBank.mergeDisplayWords(db.getAllWords())) {
            DisplayTextNormalizer.normalizeWordForDisplay(word);
            String category = DisplayTextNormalizer.normalizeCategoryName(word.category);
            CategoryItem item = grouped.get(category);
            if (item == null) {
                item = new CategoryItem(category, new ArrayList<>(), defaultWide);
                grouped.put(category, item);
            }
            item.words.add(word);
        }
        allCategories.addAll(grouped.values());
        updateWordsCountViews();
        applyWordSorting(AppSettings.getCategorySortOrder(this));
        wordsContentLoaded = true;
    }

    private void applyWordSorting(String sortOrder) {
        List<CategoryItem> sorted = new ArrayList<>(allCategories);
        switch (sortOrder) {
            case SORT_ALPHA_DESC:
                Collections.sort(sorted, (a, b) -> b.name.toLowerCase(Locale.US).compareTo(a.name.toLowerCase(Locale.US)));
                break;
            case SORT_LEVEL_DESC:
                Collections.sort(sorted, Comparator.comparingDouble((CategoryItem item) -> getKnownRatio(item.words))
                        .reversed()
                        .thenComparing(item -> item.name.toLowerCase(Locale.US)));
                break;
            case SORT_LEVEL_ASC:
                Collections.sort(sorted, Comparator.comparingDouble((CategoryItem item) -> getKnownRatio(item.words))
                        .thenComparing(item -> item.name.toLowerCase(Locale.US)));
                break;
            case SORT_ALPHA_ASC:
            default:
                Collections.sort(sorted, Comparator.comparing(item -> item.name.toLowerCase(Locale.US)));
                break;
        }
        updateWordsSortView(sortOrder);
        categoryAdapter.updateItems(sorted);
        updateStickyWordsFiltersState();
    }

    private int compareEngSafe(Word left, Word right) {
        String a = left == null || left.eng == null ? "" : left.eng.toLowerCase(Locale.US);
        String b = right == null || right.eng == null ? "" : right.eng.toLowerCase(Locale.US);
        return a.compareTo(b);
    }

    private void updateWordsSortView(String sortOrder) {
        if (tvEmbeddedWordsSort == null && tvStickyWordsSort == null) {
            return;
        }
        int accentColor = getResources().getColor(R.color.text_secondary);
        applyWordsSortView(tvEmbeddedWordsSort, sortOrder, accentColor);
        applyWordsSortView(tvStickyWordsSort, sortOrder, accentColor);
    }

    private void applyWordsSortView(AppCompatTextView sortView, String sortOrder, int accentColor) {
        if (sortView == null) {
            return;
        }
        sortView.setText(getWordsSortLabel(sortOrder));
        sortView.setBackground(createRoundedChipBackground(accentColor, false));
        sortView.setCompoundDrawablePadding(dp(6));
        sortView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_chevron_down_18,
                0
        );
    }

    private String getWordsSortLabel(String sortOrder) {
        if (SORT_ALPHA_DESC.equals(sortOrder)) return "Z'den A'ya";
        if (SORT_LEVEL_DESC.equals(sortOrder)) return "En çok bilinen";
        if (SORT_LEVEL_ASC.equals(sortOrder)) return "En az bilinen";
        return "Alfabeye göre";
    }

    private String getNextCategorySortOrder(String currentSortOrder) {
        if (SORT_ALPHA_ASC.equals(currentSortOrder)) return SORT_ALPHA_DESC;
        if (SORT_ALPHA_DESC.equals(currentSortOrder)) return SORT_LEVEL_DESC;
        if (SORT_LEVEL_DESC.equals(currentSortOrder)) return SORT_LEVEL_ASC;
        return SORT_ALPHA_ASC;
    }

    private void updateWordsCountViews() {
        String countText = allCategories.size() + " kategori";
        if (tvEmbeddedWordsCountInline != null) {
            tvEmbeddedWordsCountInline.setText(countText);
        }
        if (tvStickyWordsCount == null) {
            return;
        }
        tvStickyWordsCount.setText(countText);
    }

    private double getKnownRatio(List<Word> words) {
        if (words == null || words.isEmpty()) {
            return 0d;
        }
        double total = 0d;
        for (Word word : words) {
            int level = word == null ? 0 : Math.max(0, Math.min(word.stepCount, MAX_ANALYSIS_LEVEL));
            total += level;
        }
        return total / (words.size() * (double) MAX_ANALYSIS_LEVEL);
    }

    private void updateQuestionLimitBubble(Slider slider, TextView bubble) {
        View parent = (View) bubble.getParent();
        if (parent == null || slider.getWidth() == 0 || parent.getWidth() == 0 || bubble.getWidth() == 0) {
            return;
        }

        float valueRange = slider.getValueTo() - slider.getValueFrom();
        float progress = valueRange == 0 ? 0f : (slider.getValue() - slider.getValueFrom()) / valueRange;
        int trackStart = slider.getLeft() + slider.getTrackSidePadding();
        int trackWidth = slider.getTrackWidth();
        float thumbCenter = trackStart + trackWidth * progress;
        float targetX = thumbCenter - bubble.getWidth() / 2f;
        float maxX = Math.max(0, parent.getWidth() - bubble.getWidth());
        bubble.setTranslationX(Math.max(0, Math.min(targetX, maxX)));
    }

    private void showEditProfileDialog() {
        EditProfileDialogState state = createEditProfileDialogState();
        bindEditProfileDialogState(state);
        AlertDialog dialog = createEditProfileDialog(state);
        dialog.setOnDismissListener(d -> dialogProfileImage = null);
        dialog.show();
        applyRoundedDialogCorners(dialog);
    }

    private EditProfileDialogState createEditProfileDialogState() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        EditProfileDialogState state = new EditProfileDialogState();
        state.dialogView = dialogView;
        state.profileImageView = dialogView.findViewById(R.id.imgEditProfile);
        dialogProfileImage = state.profileImageView;
        state.showPasswordResetButton = dialogView.findViewById(R.id.btnShowPasswordReset);
        state.logoutButton = dialogView.findViewById(R.id.btnDialogLogout);
        state.usernameLayout = dialogView.findViewById(R.id.tilEditUsername);
        state.fullNameLayout = dialogView.findViewById(R.id.tilEditFullName);
        state.usernameEditText = dialogView.findViewById(R.id.etEditUsername);
        state.fullNameEditText = dialogView.findViewById(R.id.etEditFullName);
        state.themeToggle = dialogView.findViewById(R.id.toggleDialogTheme);
        state.questionLimitSlider = dialogView.findViewById(R.id.sliderDialogQuestionLimit);
        state.questionLimitValue = dialogView.findViewById(R.id.tvDialogQuestionLimitValue);
        return state;
    }

    private void bindEditProfileDialogState(EditProfileDialogState state) {
        DatabaseHelper.UserProfile profile = db.getUserProfile(currentUser);
        String profileUsername = profile == null ? currentUser : profile.username;
        selectedProfileImagePath = profile == null ? "" : profile.profileImagePath;
        state.usernameEditText.setText(profileUsername);
        state.fullNameEditText.setText(profile == null ? "" : profile.fullName);
        applyProfileImage(state.profileImageView, selectedProfileImagePath);

        int currentLimit = AppSettings.getQuizLimit(this);
        state.questionLimitSlider.setValue(currentLimit);
        state.questionLimitValue.setText(String.valueOf(currentLimit));
        state.questionLimitSlider.post(() -> updateQuestionLimitBubble(state.questionLimitSlider, state.questionLimitValue));
        state.questionLimitSlider.addOnChangeListener((slider, value, fromUser) -> {
            state.questionLimitValue.setText(String.valueOf(AppSettings.clampQuizLimit(Math.round(value))));
            updateQuestionLimitBubble(slider, state.questionLimitValue);
        });

        String savedTheme = ThemeManager.getSavedTheme(this);
        state.themeToggle.check(ThemeManager.THEME_DARK.equals(savedTheme)
                ? R.id.btnDialogDarkTheme
                : R.id.btnDialogLightTheme);

        state.profileImageView.setOnClickListener(v -> showProfileImageOptions());
        state.showPasswordResetButton.setOnClickListener(v -> showResetPasswordDialog(getText(state.usernameEditText)));
    }

    private AlertDialog createEditProfileDialog(EditProfileDialogState state) {
        TextView dialogTitle = new TextView(this);
        dialogTitle.setText("Profili Düzenle");
        dialogTitle.setTextColor(getResources().getColor(R.color.text_primary));
        dialogTitle.setTextSize(18);
        dialogTitle.setTypeface(Typeface.DEFAULT_BOLD);
        dialogTitle.setPadding(dp(24), dp(20), dp(24), dp(4));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCustomTitle(dialogTitle)
                .setView(state.dialogView)
                .setNegativeButton("Vazgeç", null)
                .setPositiveButton("Kaydet", null)
                .create();

        state.logoutButton.setOnClickListener(v -> handleLogoutFromProfileDialog(dialog));
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> handleEditProfileSave(dialog, state)));
        return dialog;
    }

    private void handleLogoutFromProfileDialog(AlertDialog dialog) {
        dialog.dismiss();
        AppSettings.clearCurrentUser(this);
        AppSettings.clearRememberedLogin(this);
        Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleEditProfileSave(AlertDialog dialog, EditProfileDialogState state) {
        state.usernameLayout.setError(null);
        state.fullNameLayout.setError(null);

        String newUsername = getText(state.usernameEditText);
        String fullName = getText(state.fullNameEditText);
        int newLimit = AppSettings.clampQuizLimit(Math.round(state.questionLimitSlider.getValue()));
        String selectedTheme = state.themeToggle.getCheckedButtonId() == R.id.btnDialogDarkTheme
                ? ThemeManager.THEME_DARK
                : ThemeManager.THEME_LIGHT;

        String usernameError = AccountSecurity.validateUsername(newUsername);
        if (usernameError != null) {
            state.usernameLayout.setError(usernameError);
            return;
        }
        if (db.isUsernameTakenByOtherUser(newUsername, currentUser)) {
            state.usernameLayout.setError("Bu kullanıcı adı zaten kullanılıyor.");
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
    }

    private void showResetPasswordDialog(String usernameForValidation) {
        ResetPasswordDialogState state = createResetPasswordDialogState();
        AlertDialog dialog = createResetPasswordDialog(state, usernameForValidation);
        dialog.show();
        applyRoundedDialogCorners(dialog);
    }

    private ResetPasswordDialogState createResetPasswordDialogState() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reset_password, null);
        ResetPasswordDialogState state = new ResetPasswordDialogState();
        state.dialogView = dialogView;
        state.currentPasswordLayout = dialogView.findViewById(R.id.tilCurrentPassword);
        state.newPasswordLayout = dialogView.findViewById(R.id.tilNewPassword);
        state.confirmPasswordLayout = dialogView.findViewById(R.id.tilConfirmPassword);
        state.currentPasswordEditText = dialogView.findViewById(R.id.etCurrentPassword);
        state.newPasswordEditText = dialogView.findViewById(R.id.etNewPassword);
        state.confirmPasswordEditText = dialogView.findViewById(R.id.etConfirmPassword);
        return state;
    }

    private AlertDialog createResetPasswordDialog(ResetPasswordDialogState state, String usernameForValidation) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Şifremi Sıfırla")
                .setView(state.dialogView)
                .setNegativeButton("Vazgeç", null)
                .setPositiveButton("Kaydet", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> handleResetPasswordSave(dialog, state, usernameForValidation)));
        return dialog;
    }

    private void handleResetPasswordSave(AlertDialog dialog, ResetPasswordDialogState state, String usernameForValidation) {
        state.currentPasswordLayout.setError(null);
        state.newPasswordLayout.setError(null);
        state.confirmPasswordLayout.setError(null);

        String currentPassword = getText(state.currentPasswordEditText);
        String newPassword = getText(state.newPasswordEditText);
        String confirmPassword = getText(state.confirmPasswordEditText);
        String username = usernameForValidation == null || usernameForValidation.trim().isEmpty()
                ? currentUser
                : usernameForValidation.trim();

        if (!db.checkUser(currentUser, currentPassword)) {
            state.currentPasswordLayout.setError("Eski şifre hatalı.");
            return;
        }
        String passwordError = AccountSecurity.validatePassword(username, newPassword);
        if (passwordError != null) {
            state.newPasswordLayout.setError(passwordError);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            state.confirmPasswordLayout.setError("Yeni şifreler eşleşmiyor.");
            return;
        }

        if (db.updatePassword(currentUser, newPassword)) {
            Toast.makeText(this, "Şifre güncellendi.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        } else {
            Toast.makeText(this, "Şifre güncellenemedi.", Toast.LENGTH_SHORT).show();
        }
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

    private static final class EditProfileDialogState {
        View dialogView;
        ImageView profileImageView;
        MaterialButton showPasswordResetButton;
        MaterialButton logoutButton;
        TextInputLayout usernameLayout;
        TextInputLayout fullNameLayout;
        TextInputEditText usernameEditText;
        TextInputEditText fullNameEditText;
        MaterialButtonToggleGroup themeToggle;
        Slider questionLimitSlider;
        TextView questionLimitValue;
    }

    private static final class ResetPasswordDialogState {
        View dialogView;
        TextInputLayout currentPasswordLayout;
        TextInputLayout newPasswordLayout;
        TextInputLayout confirmPasswordLayout;
        TextInputEditText currentPasswordEditText;
        TextInputEditText newPasswordEditText;
        TextInputEditText confirmPasswordEditText;
    }

    private static class CategoryItem {
        final String name;
        final List<Word> words;
        boolean expanded;

        CategoryItem(String name, List<Word> words, boolean expanded) {
            this.name = name;
            this.words = words;
            this.expanded = expanded;
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
            return new CategoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            CategoryItem item = items.get(position);
            List<Word> sortedWords = sortWords(item.words);

            holder.tvCategoryName.setText(item.name);
            holder.tvCategoryCount.setText(item.words.size() + WORD_COUNT_SUFFIX);
            holder.tvCategorySubtitle.setVisibility(View.GONE);

            holder.llCategoryProgress.setVisibility(item.expanded ? View.GONE : View.VISIBLE);
            holder.cgCategoryWords.setVisibility(item.expanded ? View.VISIBLE : View.GONE);

            if (item.expanded) {
                bindWordChips(holder.cgCategoryWords, sortedWords);
            } else {
                bindLevelProgress(holder.llCategoryProgress, sortedWords);
            }

            holder.layoutCategoryHeader.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) {
                    return;
                }
                item.expanded = !item.expanded;
                notifyItemChanged(adapterPosition);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private List<Word> sortWords(List<Word> words) {
            List<Word> sorted = new ArrayList<>();
            if (words != null) {
                sorted.addAll(words);
            }
            Collections.sort(sorted, AccountActivity.this::compareEngSafe);
            return sorted;
        }

        private void bindLevelProgress(LinearLayout progressContainer, List<Word> words) {
            progressContainer.removeAllViews();
            if (words == null) {
                return;
            }

            int[] counts = new int[MAX_ANALYSIS_LEVEL + 1];
            int total = 0;
            for (Word word : words) {
                int level = word == null ? 0 : Math.max(0, Math.min(word.stepCount, MAX_ANALYSIS_LEVEL));
                counts[level]++;
                total++;
            }
            if (total == 0) {
                return;
            }

            List<Integer> displayOrder = new ArrayList<>();
            for (int level = 1; level <= MAX_ANALYSIS_LEVEL; level++) {
                if (counts[level] > 0) {
                    displayOrder.add(level);
                }
            }
            if (counts[0] > 0) {
                displayOrder.add(0);
            }
            if (displayOrder.isEmpty()) {
                return;
            }

            int firstLevel = displayOrder.get(0);
            int lastLevel = displayOrder.get(displayOrder.size() - 1);

            for (int level : displayOrder) {
                if (counts[level] == 0) {
                    continue;
                }
                View segment = new View(AccountActivity.this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, counts[level]);
                segment.setLayoutParams(params);
                segment.setBackground(createLevelBarDrawable(
                        getLevelAccentColor(level),
                        level == firstLevel,
                        level == lastLevel
                ));
                progressContainer.addView(segment);
            }
        }

        private void bindWordChips(ChipGroup chipGroup, List<Word> words) {
            chipGroup.removeAllViews();
            if (words == null) {
                return;
            }

            for (Word word : words) {
                Chip chip = new Chip(AccountActivity.this);
                chip.setEnsureMinTouchTargetSize(false);
                chip.setClickable(false);
                chip.setCheckable(false);
                String english = word == null || word.eng == null || word.eng.trim().isEmpty() ? "-" : word.eng.trim();
                String turkish = word == null || word.tur == null || word.tur.trim().isEmpty() ? "-" : word.tur.trim();
                chip.setText(english + "  " + turkish);
                chip.setTextColor(getResources().getColor(R.color.text_primary));
                chip.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.surface_variant)));
                chip.setChipStrokeWidth(dpFloat(1));
                chip.setChipStrokeColor(ColorStateList.valueOf(getLevelAccentColor(word == null ? 0 : word.stepCount)));
                chip.setChipCornerRadius(dpFloat(14));
                chip.setChipStartPadding(dpFloat(10));
                chip.setChipEndPadding(dpFloat(10));
                chip.setChipMinHeight(dpFloat(34));
                chip.setChipIcon(createLevelIndicatorDrawable(word == null ? 0 : word.stepCount));
                chip.setChipIconVisible(true);
                chip.setChipIconSize(dpFloat(8));
                chip.setChipIconTint(null);
                chipGroup.addView(chip);
            }
        }

        private GradientDrawable createLevelIndicatorDrawable(int stepCount) {
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(getLevelAccentColor(stepCount));
            return dot;
        }

        private GradientDrawable createLevelBarDrawable(int color, boolean roundStart, boolean roundEnd) {
            float radius = dpFloat(7);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(color);
            drawable.setCornerRadii(new float[]{
                    roundStart ? radius : 0f, roundStart ? radius : 0f,
                    roundEnd ? radius : 0f, roundEnd ? radius : 0f,
                    roundEnd ? radius : 0f, roundEnd ? radius : 0f,
                    roundStart ? radius : 0f, roundStart ? radius : 0f
            });
            return drawable;
        }

        class CategoryViewHolder extends RecyclerView.ViewHolder {
            TextView tvCategoryName, tvCategorySubtitle, tvCategoryCount;
            View layoutCategoryHeader;
            LinearLayout llCategoryProgress;
            ChipGroup cgCategoryWords;

            CategoryViewHolder(View itemView) {
                super(itemView);
                tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
                tvCategorySubtitle = itemView.findViewById(R.id.tvCategorySubtitle);
                tvCategoryCount = itemView.findViewById(R.id.tvCategoryCount);
                layoutCategoryHeader = itemView.findViewById(R.id.layoutCategoryHeader);
                llCategoryProgress = itemView.findViewById(R.id.llCategoryProgress);
                cgCategoryWords = itemView.findViewById(R.id.cgCategoryWords);
            }
        }
    }
}
