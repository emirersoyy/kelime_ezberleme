package com.example.kelimeezberleme;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class WordsListActivity extends BottomNavActivity {
    RecyclerView rvWords;
    Spinner spSort;
    DatabaseHelper db;
    WordAdapter adapter;
    List<Word> allWords = new ArrayList<>();

    private static final String SORT_ALPHA_ASC = "alpha_asc";
    private static final String SORT_ALPHA_DESC = "alpha_desc";
    private static final String SORT_LEVEL_DESC = "level_desc";
    private static final String SORT_LEVEL_ASC = "level_asc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words_list);

        db = new DatabaseHelper(this);
        rvWords = findViewById(R.id.rvWords);
        rvWords.setLayoutManager(new LinearLayoutManager(this));
        spSort = findViewById(R.id.spSort);
        findViewById(R.id.btnAddWordFromWords).setOnClickListener(v ->
                startActivity(new Intent(WordsListActivity.this, AddWordActivity.class)));

        setupSortSpinner();
        allWords = WordleWordBank.mergeDisplayWords(db.getAllWords());
        adapter = new WordAdapter(new ArrayList<>());
        rvWords.setAdapter(adapter);
        applySorting(AppSettings.getWordsSortOrder(this));
    }

    private void setupSortSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"A-Z", "Z-A", "Yüksek seviye", "Düşük seviye"}
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSort.setAdapter(spinnerAdapter);

        String savedSort = AppSettings.getWordsSortOrder(this);
        spSort.setSelection(getSortPosition(savedSort), false);

        spSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sortOrder = getSortKey(position);
                AppSettings.setWordsSortOrder(WordsListActivity.this, sortOrder);
                applySorting(sortOrder);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void applySorting(String sortOrder) {
        List<Word> sorted = new ArrayList<>(allWords);
        switch (sortOrder) {
            case SORT_ALPHA_DESC:
                Collections.sort(sorted, (a, b) -> compareEng(b, a));
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
        adapter.updateWords(sorted);
    }

    private int compareEng(Word left, Word right) {
        return compareEngSafe(left, right);
    }

    private int compareEngSafe(Word left, Word right) {
        String a = left == null || left.eng == null ? "" : left.eng.toLowerCase(Locale.US);
        String b = right == null || right.eng == null ? "" : right.eng.toLowerCase(Locale.US);
        return a.compareTo(b);
    }

    private String getSortKey(int position) {
        switch (position) {
            case 1: return SORT_ALPHA_DESC;
            case 2: return SORT_LEVEL_DESC;
            case 3: return SORT_LEVEL_ASC;
            case 0:
            default: return SORT_ALPHA_ASC;
        }
    }

    private int getSortPosition(String sortOrder) {
        if (SORT_ALPHA_DESC.equals(sortOrder)) return 1;
        if (SORT_LEVEL_DESC.equals(sortOrder)) return 2;
        if (SORT_LEVEL_ASC.equals(sortOrder)) return 3;
        return 0;
    }

    class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {
        List<Word> words;

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
            DisplayTextNormalizer.normalizeWordForDisplay(word);
            holder.tvEng.setText(word.eng);
            holder.tvTur.setText(isSyntheticWord(word) ? "Wordle için eklenen kelime" : word.tur);
            holder.tvLevel.setText(isSyntheticWord(word) ? "Wordle" : getLevelText(word.stepCount));
            List<String> samples = isSyntheticWord(word)
                    ? WordleWordBank.previewSamples(word.eng)
                    : db.getDisplaySamplesForWord(word);
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
                builder.append(i + 1)
                        .append(". ")
                        .append(samples.get(i).trim());
            }
            return builder.toString();
        }

        private String getLevelText(int stepCount) {
            if (stepCount <= 0) {
                return "Seviye 0";
            }
            return "Seviye " + stepCount;
        }

        private boolean isSyntheticWord(Word word) {
            return word != null && word.id < 0;
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
