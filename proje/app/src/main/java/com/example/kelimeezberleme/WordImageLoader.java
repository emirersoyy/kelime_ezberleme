package com.example.kelimeezberleme;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

public final class WordImageLoader {
    private static final String DRAWABLE_PREFIX = "drawable:";

    private WordImageLoader() {
    }

    public static void load(ImageView imageView, String pictureRef) {
        if (pictureRef == null || pictureRef.isEmpty()) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            imageView.setVisibility(View.VISIBLE);
            return;
        }

        try {
            if (pictureRef.startsWith(DRAWABLE_PREFIX)) {
                String drawableName = pictureRef.substring(DRAWABLE_PREFIX.length());
                Context context = imageView.getContext();
                int resId = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
                if (resId != 0) {
                    imageView.setImageResource(resId);
                    imageView.setVisibility(View.VISIBLE);
                    return;
                }
            } else {
                imageView.setImageURI(Uri.parse(pictureRef));
                imageView.setVisibility(View.VISIBLE);
                return;
            }
        } catch (Exception ignored) {
            // Hide broken image references.
        }

        imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        imageView.setVisibility(View.VISIBLE);
    }
}
