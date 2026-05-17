package com.example.kelimeezberleme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class QuizResultActivity extends BottomNavActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        int correct = getIntent().getIntExtra("correct", 0);
        int total = getIntent().getIntExtra("total", 0);
        ArrayList<IncorrectWord> incorrects = (ArrayList<IncorrectWord>) getIntent().getSerializableExtra("incorrects");

        TextView tvSummary = findViewById(R.id.tvSummary);
        tvSummary.setText(total + " soruda " + correct + " do\u011fru cevap verdin.");

        if (incorrects != null && !incorrects.isEmpty()) {
            findViewById(R.id.tvWrongTitle).setVisibility(View.VISIBLE);
            RecyclerView rv = findViewById(R.id.rvIncorrect);
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(new IncorrectAdapter(incorrects));
        }

        findViewById(R.id.btnFinishQuiz).setOnClickListener(v -> finish());
    }

    class IncorrectAdapter extends RecyclerView.Adapter<IncorrectAdapter.VH> {
        ArrayList<IncorrectWord> list;

        IncorrectAdapter(ArrayList<IncorrectWord> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            IncorrectWord item = list.get(position);
            holder.t1.setText(item.eng + " -> " + item.tur);
            holder.t1.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            String answer = item.userAnswer.isEmpty() ? "(Bo\u015f)" : item.userAnswer;
            String sentence = item.sentence == null || item.sentence.isEmpty() ? "" : "\n" + item.sentence;
            holder.t2.setText("Senin cevab\u0131n: " + answer + sentence);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView t1, t2;

            VH(View v) {
                super(v);
                t1 = v.findViewById(android.R.id.text1);
                t2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}
