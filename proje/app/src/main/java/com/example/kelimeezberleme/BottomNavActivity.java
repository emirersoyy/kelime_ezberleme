package com.example.kelimeezberleme;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BottomNavActivity extends AppCompatActivity {
    private FrameLayout rootLayout;
    private FrameLayout contentFrame;
    private BottomNavBarView bottomNavBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createBottomNavShell();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        if (contentFrame == null) {
            super.setContentView(layoutResID);
            return;
        }

        contentFrame.removeAllViews();
        getLayoutInflater().inflate(layoutResID, contentFrame, true);
    }

    @Override
    public void setContentView(View view) {
        if (contentFrame == null) {
            super.setContentView(view);
            return;
        }

        contentFrame.removeAllViews();
        contentFrame.addView(view, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (contentFrame == null) {
            super.setContentView(view, params);
            return;
        }

        contentFrame.removeAllViews();
        contentFrame.addView(view, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void createBottomNavShell() {
        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(getResources().getColor(R.color.background));

        contentFrame = new FrameLayout(this);
        rootLayout.addView(contentFrame, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        bottomNavBar = new BottomNavBarView(this);
        FrameLayout.LayoutParams navParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);
        int horizontalMargin = Math.round(24 * getResources().getDisplayMetrics().density);
        navParams.setMargins(horizontalMargin, 0, horizontalMargin, 0);
        rootLayout.addView(bottomNavBar, navParams);

        super.setContentView(rootLayout);
    }

    protected int getBottomNavBarHeightPx() {
        if (bottomNavBar != null && bottomNavBar.getHeight() > 0) {
            return bottomNavBar.getHeight();
        }
        return Math.round(76 * getResources().getDisplayMetrics().density);
    }

}
