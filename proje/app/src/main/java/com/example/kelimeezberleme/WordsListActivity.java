package com.example.kelimeezberleme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WordsListActivity extends AppCompatActivity {
    RecyclerView rvWords;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words_list);

        db = new DatabaseHelper(this);
        rvWords = findViewById(R.id.rvWords);
        rvWords.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        List<Word> wordList = db.getAllWords();
        WordAdapter adapter = new WordAdapter(wordList);
        rvWords.setAdapter(adapter);
    }

    class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {
        List<Word> words;

        WordAdapter(List<Word> words) {
            this.words = words;
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
                builder.append(i + 1)
                        .append(". ")
                        .append(samples.get(i).trim());
            }
            return builder.toString();
        }

        class WordViewHolder extends RecyclerView.ViewHolder {
            TextView tvEng, tvTur, tvSample, tvToggle;
            ImageView ivWord;
            View detailsContainer;

            WordViewHolder(View itemView) {
                super(itemView);
                tvEng = itemView.findViewById(R.id.tvEng);
                tvTur = itemView.findViewById(R.id.tvTur);
                tvSample = itemView.findViewById(R.id.tvSample);
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
