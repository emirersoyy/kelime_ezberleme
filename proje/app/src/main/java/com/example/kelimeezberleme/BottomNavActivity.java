package com.example.kelimeezberleme;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BottomNavActivity extends AppCompatActivity {
    private LinearLayout rootLayout;
    private FrameLayout contentFrame;

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
        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(getResources().getColor(R.color.background));

        contentFrame = new FrameLayout(this);
        rootLayout.addView(contentFrame, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f));

        BottomNavBarView bottomNavBar = new BottomNavBarView(this);
        rootLayout.addView(bottomNavBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        super.setContentView(rootLayout);
    }

}
