package com.example.kelimeezberleme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class BottomNavBarView extends MaterialCardView {
    private MaterialButton btnHome;
    private MaterialButton btnAccount;

    public BottomNavBarView(Context context) {
        super(context);
        init(context);
    }

    public BottomNavBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BottomNavBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_bottom_nav_bar, this, true);
        btnHome = findViewById(R.id.btnBottomHome);
        btnAccount = findViewById(R.id.btnBottomAccount);

        setCardBackgroundColor(getResources().getColor(R.color.surface));
        setCardElevation(dp(8f));
        setRadius(dp(36f));
        setStrokeColor(getResources().getColor(R.color.divider));
        setStrokeWidth(1);
        setUseCompatPadding(true);
        setPreventCornerOverlap(true);

        btnHome.setIconResource(R.drawable.ic_home_outline_24);
        btnAccount.setIconResource(R.drawable.ic_account_outline_24);

        String screen = context.getClass().getSimpleName();
        if ("MainActivity".equals(screen)) {
            setActiveState(btnHome, R.drawable.ic_home_filled_24, true);
            setInactiveState(btnAccount, R.drawable.ic_account_outline_24, true);
        } else {
            setInactiveState(btnHome, R.drawable.ic_home_outline_24, false);
        }

        if ("AccountActivity".equals(screen)) {
            setActiveState(btnAccount, R.drawable.ic_account_filled_24, true);
            setInactiveState(btnHome, R.drawable.ic_home_outline_24, true);
        } else {
            setInactiveState(btnAccount, R.drawable.ic_account_outline_24, false);
        }

        if (!"MainActivity".equals(screen)) {
            btnHome.setOnClickListener(v -> navigateWithQuizWarning(context, MainActivity.class));
        } else {
            btnHome.setOnClickListener(null);
        }

        if (!"AccountActivity".equals(screen)) {
            btnAccount.setOnClickListener(v -> navigateWithQuizWarning(context, AccountActivity.class));
        } else {
            btnAccount.setOnClickListener(null);
        }
    }

    private void setActiveState(MaterialButton button, int iconRes, boolean clickable) {
        button.setIconResource(iconRes);
        button.setAlpha(1f);
        button.setClickable(clickable);
    }

    private void setInactiveState(MaterialButton button, int iconRes, boolean clickable) {
        button.setIconResource(iconRes);
        button.setAlpha(0.55f);
        button.setClickable(clickable);
    }


    private void navigate(Context context, Class<?> target) {
        Intent intent = new Intent(context, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    private void navigateWithQuizWarning(Context context, Class<?> target) {
        if (isQuizScreen(context)) {
            new AlertDialog.Builder(context)
                    .setTitle("Sınavdan çıkılsın mı?")
                    .setMessage("Bu testteki ilerlemen kaybolacak, sınavdan çıkmak istediğine emin misin?")
                    .setNegativeButton("Vazgeç", null)
                    .setPositiveButton("Evet", (dialog, which) -> navigate(context, target))
                    .show();
            return;
        }
        navigate(context, target);
    }

    private boolean isQuizScreen(Context context) {
        return "QuizActivity".equals(context.getClass().getSimpleName());
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
