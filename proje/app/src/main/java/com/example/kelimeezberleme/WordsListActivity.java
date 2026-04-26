package com.example.kelimeezberleme;

import android.net.Uri;
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
            
            // Eğer resim varsa göster
            if (word.pic != null && !word.pic.isEmpty()) {
                try {
                    holder.ivWord.setImageURI(Uri.parse(word.pic));
                    holder.ivWord.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    holder.ivWord.setVisibility(View.GONE);
                }
            } else {
                holder.ivWord.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return words.size();
        }

        class WordViewHolder extends RecyclerView.ViewHolder {
            TextView tvEng, tvTur;
            ImageView ivWord;

            WordViewHolder(View itemView) {
                super(itemView);
                tvEng = itemView.findViewById(R.id.tvEng);
                tvTur = itemView.findViewById(R.id.tvTur);
                ivWord = itemView.findViewById(R.id.ivWord);
            }
        }
    }
}