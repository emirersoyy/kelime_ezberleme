package com.example.kelimeezberleme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class BottomNavBarView extends MaterialCardView {
    private MaterialButton btnHome;
    private MaterialButton btnAccount;
    private final AccelerateDecelerateInterpolator ease = new AccelerateDecelerateInterpolator();
    private final OvershootInterpolator overshoot = new OvershootInterpolator(1.35f);

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
        setCardElevation(dp(4f));
        setRadius(dp(20));
        setStrokeColor(getResources().getColor(R.color.divider));
        setStrokeWidth(1);
        setUseCompatPadding(true);
        setPreventCornerOverlap(true);

        btnHome.setIconResource(R.drawable.ic_home_outline_24);
        btnAccount.setIconResource(R.drawable.ic_account_outline_24);
        btnHome.setAlpha(1f);
        btnAccount.setAlpha(1f);
        btnHome.setScaleX(1f);
        btnHome.setScaleY(1f);
        btnAccount.setScaleX(1f);
        btnAccount.setScaleY(1f);

        String screen = context.getClass().getSimpleName();
        if ("MainActivity".equals(screen)) {
            btnHome.setIconResource(R.drawable.ic_home_filled_24);
            btnHome.setOnClickListener(null);
        } else {
            btnHome.setIconResource(R.drawable.ic_home_outline_24);
            btnHome.setOnClickListener(v -> navigateWithQuizWarning(context, MainActivity.class, btnHome,
                    R.drawable.ic_home_filled_24, R.drawable.ic_home_outline_24));
        }

        if ("AccountActivity".equals(screen)) {
            btnAccount.setIconResource(R.drawable.ic_account_filled_24);
            btnAccount.setOnClickListener(null);
        } else {
            btnAccount.setIconResource(R.drawable.ic_account_outline_24);
            btnAccount.setOnClickListener(v -> navigateWithQuizWarning(context, AccountActivity.class, btnAccount,
                    R.drawable.ic_account_filled_24, R.drawable.ic_account_outline_24));
        }
    }

    private void navigate(Context context, Class<?> target) {
        Intent intent = new Intent(context, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    private void navigateWithQuizWarning(Context context, Class<?> target, MaterialButton button, int filledIconRes, int outlineIconRes) {
        if (isQuizScreen(context)) {
            new AlertDialog.Builder(context)
                    .setTitle("Sınavdan çıkılsın mı?")
                    .setMessage("Bu testteki ilerlemen kaybolacak, sınavdan çıkmak istediğine emin misin?")
                    .setNegativeButton("Vazgeç", null)
                    .setPositiveButton("Evet", (dialog, which) -> animateAndNavigate(context, target, button, filledIconRes, outlineIconRes))
                    .show();
            return;
        }
        animateAndNavigate(context, target, button, filledIconRes, outlineIconRes);
    }

    private void animateAndNavigate(Context context, Class<?> target, MaterialButton button, int filledIconRes, int outlineIconRes) {
        button.setClickable(false);
        button.setIconResource(filledIconRes);
        button.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(60)
                .setInterpolator(ease)
                .withEndAction(() -> button.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(120)
                        .setInterpolator(overshoot)
                        .start())
                .start();
        navigate(context, target);
    }

    private boolean isQuizScreen(Context context) {
        return "QuizActivity".equals(context.getClass().getSimpleName());
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
