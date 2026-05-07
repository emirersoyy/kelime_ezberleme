package com.example.kelimeezberleme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

public final class WordImageLoader {
    private static final String DRAWABLE_PREFIX = "drawable:";
    private static final String WORD_PREFIX = "word:";

    private WordImageLoader() {
    }

    public static void load(ImageView imageView, String pictureRef) {
        if (pictureRef == null || pictureRef.isEmpty()) {
            imageView.setVisibility(View.GONE);
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
            } else if (pictureRef.startsWith(WORD_PREFIX)) {
                imageView.setImageBitmap(createWordBitmap(pictureRef.substring(WORD_PREFIX.length())));
                imageView.setVisibility(View.VISIBLE);
                return;
            } else {
                imageView.setImageURI(Uri.parse(pictureRef));
                imageView.setVisibility(View.VISIBLE);
                return;
            }
        } catch (Exception ignored) {
            // Fall through and hide the broken image reference.
        }

        imageView.setVisibility(View.GONE);
    }

    private static Bitmap createWordBitmap(String rawKey) {
        String key = rawKey == null ? "" : rawKey.toLowerCase();
        Bitmap bitmap = Bitmap.createBitmap(480, 320, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setShader(new LinearGradient(0, 0, 480, 320, Color.rgb(239, 246, 255), Color.rgb(236, 253, 245), Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(0, 0, 480, 320), 34, 34, paint);
        paint.setShader(null);

        switch (key) {
            case "apple": drawApple(canvas, paint); break;
            case "book": drawBook(canvas, paint); break;
            case "computer": drawComputer(canvas, paint); break;
            case "water": drawWater(canvas, paint); break;
            case "school": drawSchool(canvas, paint); break;
            case "pen": drawPen(canvas, paint); break;
            case "door": drawDoor(canvas, paint); break;
            case "window": drawWindow(canvas, paint); break;
            case "table": drawTable(canvas, paint); break;
            case "chair": drawChair(canvas, paint); break;
            case "friend": drawPeople(canvas, paint, false); break;
            case "family": drawPeople(canvas, paint, true); break;
            case "heart": drawHeart(canvas, paint); break;
            case "sun": drawSun(canvas, paint); break;
            case "moon": drawMoon(canvas, paint); break;
            case "star": drawStar(canvas, paint); break;
            case "time": drawClock(canvas, paint); break;
            case "city": drawCity(canvas, paint); break;
            case "country": drawFlag(canvas, paint); break;
            case "money": drawMoney(canvas, paint); break;
            case "work": drawBriefcase(canvas, paint); break;
            case "sleep": drawBed(canvas, paint); break;
            case "happy": drawFace(canvas, paint, true); break;
            case "sad": drawFace(canvas, paint, false); break;
            case "beautiful": drawFlower(canvas, paint, true); break;
            case "big": drawScale(canvas, paint, true); break;
            case "small": drawScale(canvas, paint, false); break;
            case "new": drawSpark(canvas, paint); break;
            case "old": drawHourglass(canvas, paint); break;
            case "good": drawThumb(canvas, paint, true); break;
            case "bad": drawThumb(canvas, paint, false); break;
            case "fast": drawSpeed(canvas, paint, true); break;
            case "slow": drawSpeed(canvas, paint, false); break;
            case "hot": drawThermo(canvas, paint, true); break;
            case "cold": drawThermo(canvas, paint, false); break;
            case "easy": drawPuzzle(canvas, paint, true); break;
            case "hard": drawPuzzle(canvas, paint, false); break;
            case "read": drawBook(canvas, paint); break;
            case "write": drawPen(canvas, paint); break;
            case "listen": drawAudio(canvas, paint, false); break;
            case "speak": drawAudio(canvas, paint, true); break;
            case "run": drawMotion(canvas, paint, true); break;
            case "walk": drawMotion(canvas, paint, false); break;
            case "eat": drawEatDrink(canvas, paint, true); break;
            case "drink": drawEatDrink(canvas, paint, false); break;
            case "language": drawLanguage(canvas, paint); break;
            case "bird": drawSimpleAnimal(canvas, paint, "bird"); break;
            case "dog": drawSimpleAnimal(canvas, paint, "dog"); break;
            case "cat": drawSimpleAnimal(canvas, paint, "cat"); break;
            case "flower": drawFlower(canvas, paint, false); break;
            default: drawSemanticFallback(canvas, paint, key);
        }

        drawWordLabel(canvas, paint, rawKey);
        return bitmap;
    }

    private static void fill(Paint paint, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1f);
        paint.setColor(color);
    }

    private static void stroke(Paint paint, int color, float width) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(width);
        paint.setColor(color);
    }

    private static void drawWordLabel(Canvas canvas, Paint paint, String word) {
        fill(paint, Color.argb(210, 255, 255, 255));
        canvas.drawRoundRect(new RectF(150, 260, 330, 302), 20, 20, paint);
        fill(paint, Color.rgb(30, 41, 59));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(28);
        paint.setFakeBoldText(true);
        canvas.drawText(capitalize(word), 240, 290, paint);
        paint.setFakeBoldText(false);
    }

    private static String capitalize(String word) {
        if (word == null || word.isEmpty()) return "";
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }

    private static void drawApple(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(239, 68, 68));
        canvas.drawCircle(205, 145, 55, paint);
        canvas.drawCircle(275, 145, 55, paint);
        canvas.drawOval(new RectF(180, 110, 300, 240), paint);
        stroke(paint, Color.rgb(120, 53, 15), 14);
        canvas.drawLine(240, 92, 258, 56, paint);
        fill(paint, Color.rgb(34, 197, 94));
        canvas.drawOval(new RectF(260, 52, 326, 86), paint);
    }

    private static void drawBook(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(99, 102, 241));
        canvas.drawRoundRect(new RectF(122, 76, 236, 226), 10, 10, paint);
        fill(paint, Color.rgb(14, 165, 233));
        canvas.drawRoundRect(new RectF(244, 76, 358, 226), 10, 10, paint);
        stroke(paint, Color.WHITE, 5);
        canvas.drawLine(240, 82, 240, 226, paint);
        canvas.drawLine(150, 120, 210, 120, paint);
        canvas.drawLine(270, 120, 330, 120, paint);
        canvas.drawLine(150, 152, 210, 152, paint);
        canvas.drawLine(270, 152, 330, 152, paint);
    }

    private static void drawComputer(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(30, 41, 59));
        canvas.drawRoundRect(new RectF(118, 72, 362, 210), 18, 18, paint);
        fill(paint, Color.rgb(125, 211, 252));
        canvas.drawRoundRect(new RectF(138, 92, 342, 184), 10, 10, paint);
        fill(paint, Color.rgb(71, 85, 105));
        canvas.drawRect(218, 210, 262, 242, paint);
        canvas.drawRoundRect(new RectF(180, 238, 300, 254), 8, 8, paint);
    }

    private static void drawWater(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(14, 165, 233));
        Path drop = new Path();
        drop.moveTo(240, 60);
        drop.cubicTo(170, 140, 170, 210, 240, 230);
        drop.cubicTo(310, 210, 310, 140, 240, 60);
        canvas.drawPath(drop, paint);
        stroke(paint, Color.WHITE, 8);
        canvas.drawArc(new RectF(206, 132, 272, 204), 150, 70, false, paint);
    }

    private static void drawSchool(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(245, 158, 11));
        Path roof = new Path();
        roof.moveTo(126, 126);
        roof.lineTo(240, 58);
        roof.lineTo(354, 126);
        roof.close();
        canvas.drawPath(roof, paint);
        fill(paint, Color.rgb(255, 255, 255));
        canvas.drawRect(146, 126, 334, 230, paint);
        stroke(paint, Color.rgb(99, 102, 241), 6);
        canvas.drawLine(160, 158, 320, 158, paint);
        canvas.drawLine(160, 190, 320, 190, paint);
        fill(paint, Color.rgb(99, 102, 241));
        canvas.drawRect(222, 178, 258, 230, paint);
    }

    private static void drawPen(Canvas canvas, Paint paint) {
        stroke(paint, Color.rgb(37, 99, 235), 26);
        canvas.drawLine(160, 214, 304, 70, paint);
        stroke(paint, Color.rgb(15, 23, 42), 10);
        canvas.drawLine(304, 70, 332, 42, paint);
        fill(paint, Color.rgb(251, 191, 36));
        Path tip = new Path();
        tip.moveTo(140, 234);
        tip.lineTo(166, 204);
        tip.lineTo(178, 246);
        tip.close();
        canvas.drawPath(tip, paint);
    }

    private static void drawDoor(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(146, 64, 14));
        canvas.drawRoundRect(new RectF(170, 58, 310, 238), 12, 12, paint);
        stroke(paint, Color.rgb(92, 45, 10), 7);
        canvas.drawRoundRect(new RectF(170, 58, 310, 238), 12, 12, paint);
        fill(paint, Color.rgb(250, 204, 21));
        canvas.drawCircle(285, 154, 8, paint);
    }

    private static void drawWindow(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(125, 211, 252));
        canvas.drawRoundRect(new RectF(150, 70, 330, 224), 12, 12, paint);
        stroke(paint, Color.WHITE, 9);
        canvas.drawLine(240, 74, 240, 224, paint);
        canvas.drawLine(154, 148, 326, 148, paint);
        stroke(paint, Color.rgb(37, 99, 235), 8);
        canvas.drawRoundRect(new RectF(150, 70, 330, 224), 12, 12, paint);
    }

    private static void drawTable(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(180, 83, 9));
        canvas.drawRoundRect(new RectF(116, 122, 364, 158), 10, 10, paint);
        canvas.drawRect(146, 158, 166, 242, paint);
        canvas.drawRect(314, 158, 334, 242, paint);
    }

    private static void drawChair(Canvas canvas, Paint paint) {
        stroke(paint, Color.rgb(79, 70, 229), 16);
        canvas.drawLine(184, 76, 184, 206, paint);
        canvas.drawLine(184, 142, 304, 142, paint);
        canvas.drawLine(304, 142, 304, 238, paint);
        canvas.drawLine(206, 190, 330, 190, paint);
    }

    private static void drawPeople(Canvas canvas, Paint paint, boolean family) {
        int[][] people = family ? new int[][]{{172, 128, 28}, {240, 110, 34}, {308, 128, 28}} : new int[][]{{210, 118, 32}, {278, 118, 32}};
        for (int[] p : people) {
            fill(paint, Color.rgb(99, 102, 241));
            canvas.drawCircle(p[0], p[1], p[2], paint);
            fill(paint, Color.rgb(14, 165, 233));
            canvas.drawRoundRect(new RectF(p[0] - p[2] - 16, p[1] + p[2], p[0] + p[2] + 16, 232), 26, 26, paint);
        }
    }

    private static void drawHeart(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(244, 63, 94));
        Path path = new Path();
        path.moveTo(240, 230);
        path.cubicTo(120, 150, 150, 70, 220, 92);
        path.cubicTo(232, 96, 240, 112, 240, 112);
        path.cubicTo(240, 112, 248, 96, 260, 92);
        path.cubicTo(330, 70, 360, 150, 240, 230);
        canvas.drawPath(path, paint);
    }

    private static void drawSun(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(250, 204, 21));
        canvas.drawCircle(240, 150, 58, paint);
        stroke(paint, Color.rgb(245, 158, 11), 10);
        for (int i = 0; i < 12; i++) {
            double a = Math.toRadians(i * 30);
            canvas.drawLine((float)(240 + Math.cos(a) * 78), (float)(150 + Math.sin(a) * 78),
                    (float)(240 + Math.cos(a) * 108), (float)(150 + Math.sin(a) * 108), paint);
        }
    }

    private static void drawMoon(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(148, 163, 184));
        canvas.drawCircle(240, 150, 76, paint);
        fill(paint, Color.rgb(236, 253, 245));
        canvas.drawCircle(274, 126, 72, paint);
    }

    private static void drawStar(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(234, 179, 8));
        Path path = new Path();
        for (int i = 0; i < 10; i++) {
            double a = Math.toRadians(-90 + i * 36);
            float r = i % 2 == 0 ? 86 : 36;
            float x = (float) (240 + Math.cos(a) * r);
            float y = (float) (150 + Math.sin(a) * r);
            if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
        }
        path.close();
        canvas.drawPath(path, paint);
    }

    private static void drawClock(Canvas canvas, Paint paint) {
        fill(paint, Color.WHITE);
        canvas.drawCircle(240, 150, 80, paint);
        stroke(paint, Color.rgb(30, 41, 59), 10);
        canvas.drawCircle(240, 150, 80, paint);
        canvas.drawLine(240, 150, 240, 100, paint);
        canvas.drawLine(240, 150, 288, 172, paint);
    }

    private static void drawCity(Canvas canvas, Paint paint) {
        int[] colors = {Color.rgb(30, 41, 59), Color.rgb(71, 85, 105), Color.rgb(99, 102, 241)};
        int[][] buildings = {{120, 110, 170}, {180, 72, 228}, {250, 100, 182}, {314, 82, 214}};
        for (int i = 0; i < buildings.length; i++) {
            fill(paint, colors[i % colors.length]);
            canvas.drawRect(buildings[i][0], buildings[i][1], buildings[i][0] + 48, 240, paint);
        }
        fill(paint, Color.rgb(250, 204, 21));
        for (int x = 132; x < 350; x += 34) {
            canvas.drawRect(x, 132, x + 10, 146, paint);
            canvas.drawRect(x, 176, x + 10, 190, paint);
        }
    }

    private static void drawFlag(Canvas canvas, Paint paint) {
        stroke(paint, Color.rgb(30, 41, 59), 8);
        canvas.drawLine(160, 72, 160, 244, paint);
        fill(paint, Color.rgb(239, 68, 68));
        canvas.drawRoundRect(new RectF(166, 78, 328, 166), 8, 8, paint);
        fill(paint, Color.WHITE);
        canvas.drawCircle(220, 122, 22, paint);
    }

    private static void drawMoney(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(34, 197, 94));
        canvas.drawRoundRect(new RectF(126, 92, 354, 214), 18, 18, paint);
        stroke(paint, Color.WHITE, 6);
        canvas.drawRoundRect(new RectF(146, 112, 334, 194), 10, 10, paint);
        fill(paint, Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(58);
        paint.setFakeBoldText(true);
        canvas.drawText("$", 240, 172, paint);
        paint.setFakeBoldText(false);
    }

    private static void drawBriefcase(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(120, 53, 15));
        canvas.drawRoundRect(new RectF(126, 112, 354, 230), 16, 16, paint);
        stroke(paint, Color.rgb(251, 191, 36), 8);
        canvas.drawRoundRect(new RectF(210, 78, 270, 118), 12, 12, paint);
        canvas.drawLine(126, 158, 354, 158, paint);
    }

    private static void drawBed(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(99, 102, 241));
        canvas.drawRoundRect(new RectF(110, 138, 370, 214), 16, 16, paint);
        fill(paint, Color.WHITE);
        canvas.drawRoundRect(new RectF(124, 112, 200, 156), 14, 14, paint);
        stroke(paint, Color.rgb(30, 41, 59), 8);
        canvas.drawLine(110, 214, 110, 240, paint);
        canvas.drawLine(370, 214, 370, 240, paint);
    }

    private static void drawFace(Canvas canvas, Paint paint, boolean happy) {
        fill(paint, Color.rgb(250, 204, 21));
        canvas.drawCircle(240, 150, 82, paint);
        fill(paint, Color.rgb(30, 41, 59));
        canvas.drawCircle(210, 132, 8, paint);
        canvas.drawCircle(270, 132, 8, paint);
        stroke(paint, Color.rgb(30, 41, 59), 8);
        RectF mouth = new RectF(198, happy ? 144 : 166, 282, happy ? 204 : 226);
        canvas.drawArc(mouth, happy ? 20 : 200, happy ? 140 : 140, false, paint);
    }

    private static void drawFlower(Canvas canvas, Paint paint, boolean extra) {
        stroke(paint, Color.rgb(34, 197, 94), 10);
        canvas.drawLine(240, 150, 240, 244, paint);
        fill(paint, Color.rgb(244, 114, 182));
        for (int i = 0; i < 6; i++) {
            double a = Math.toRadians(i * 60);
            canvas.drawCircle((float)(240 + Math.cos(a) * 42), (float)(124 + Math.sin(a) * 42), extra ? 32 : 26, paint);
        }
        fill(paint, Color.rgb(250, 204, 21));
        canvas.drawCircle(240, 124, 26, paint);
    }

    private static void drawScale(Canvas canvas, Paint paint, boolean big) {
        fill(paint, Color.rgb(99, 102, 241));
        canvas.drawCircle(big ? 215 : 190, 158, big ? 74 : 34, paint);
        fill(paint, Color.rgb(14, 165, 233));
        canvas.drawCircle(big ? 306 : 286, 182, big ? 30 : 62, paint);
    }

    private static void drawSpark(Canvas canvas, Paint paint) {
        drawStar(canvas, paint);
        fill(paint, Color.rgb(14, 165, 233));
        canvas.drawCircle(150, 96, 16, paint);
        canvas.drawCircle(342, 210, 20, paint);
    }

    private static void drawHourglass(Canvas canvas, Paint paint) {
        stroke(paint, Color.rgb(30, 41, 59), 8);
        canvas.drawRoundRect(new RectF(174, 64, 306, 236), 12, 12, paint);
        canvas.drawLine(186, 74, 294, 226, paint);
        canvas.drawLine(294, 74, 186, 226, paint);
        fill(paint, Color.rgb(245, 158, 11));
        canvas.drawOval(new RectF(210, 172, 270, 218), paint);
    }

    private static void drawThumb(Canvas canvas, Paint paint, boolean up) {
        fill(paint, up ? Color.rgb(34, 197, 94) : Color.rgb(244, 63, 94));
        RectF palm = up ? new RectF(178, 138, 304, 214) : new RectF(178, 96, 304, 172);
        canvas.drawRoundRect(palm, 28, 28, paint);
        RectF thumb = up ? new RectF(260, 84, 318, 154) : new RectF(260, 158, 318, 228);
        canvas.drawRoundRect(thumb, 24, 24, paint);
        fill(paint, Color.WHITE);
        canvas.drawRoundRect(new RectF(156, 152, 190, 224), 14, 14, paint);
    }

    private static void drawSpeed(Canvas canvas, Paint paint, boolean fast) {
        stroke(paint, fast ? Color.rgb(239, 68, 68) : Color.rgb(14, 165, 233), 12);
        canvas.drawArc(new RectF(150, 84, 330, 264), 200, 140, false, paint);
        canvas.drawLine(240, 174, fast ? 310 : 272, fast ? 112 : 138, paint);
        for (int i = 0; i < (fast ? 4 : 2); i++) {
            canvas.drawLine(110, 110 + i * 36, 160, 110 + i * 36, paint);
        }
    }

    private static void drawThermo(Canvas canvas, Paint paint, boolean hot) {
        stroke(paint, Color.rgb(30, 41, 59), 10);
        canvas.drawRoundRect(new RectF(220, 62, 260, 190), 20, 20, paint);
        fill(paint, hot ? Color.rgb(239, 68, 68) : Color.rgb(14, 165, 233));
        canvas.drawCircle(240, 210, 38, paint);
        canvas.drawRoundRect(new RectF(228, hot ? 98 : 142, 252, 204), 12, 12, paint);
    }

    private static void drawPuzzle(Canvas canvas, Paint paint, boolean easy) {
        fill(paint, easy ? Color.rgb(34, 197, 94) : Color.rgb(245, 158, 11));
        canvas.drawRoundRect(new RectF(156, 86, 324, 226), 16, 16, paint);
        fill(paint, Color.rgb(236, 253, 245));
        canvas.drawCircle(easy ? 240 : 300, easy ? 86 : 156, 24, paint);
        if (!easy) {
            stroke(paint, Color.rgb(30, 41, 59), 10);
            canvas.drawLine(188, 132, 292, 198, paint);
        }
    }

    private static void drawAudio(Canvas canvas, Paint paint, boolean speech) {
        fill(paint, Color.rgb(99, 102, 241));
        canvas.drawRoundRect(new RectF(142, 132, 198, 184), 10, 10, paint);
        Path cone = new Path();
        cone.moveTo(198, 132);
        cone.lineTo(260, 92);
        cone.lineTo(260, 224);
        cone.lineTo(198, 184);
        cone.close();
        canvas.drawPath(cone, paint);
        stroke(paint, Color.rgb(99, 102, 241), 8);
        canvas.drawArc(new RectF(250, 112, 330, 204), -45, 90, false, paint);
        if (speech) canvas.drawRoundRect(new RectF(300, 78, 366, 128), 18, 18, paint);
    }

    private static void drawMotion(Canvas canvas, Paint paint, boolean run) {
        stroke(paint, Color.rgb(30, 41, 59), 10);
        canvas.drawCircle(240, 92, 26, paint);
        canvas.drawLine(240, 118, 220, 170, paint);
        canvas.drawLine(220, 170, run ? 168 : 194, run ? 218 : 224, paint);
        canvas.drawLine(220, 170, run ? 290 : 262, run ? 222 : 224, paint);
        canvas.drawLine(226, 138, run ? 178 : 190, run ? 132 : 152, paint);
        canvas.drawLine(226, 138, run ? 292 : 270, run ? 126 : 154, paint);
        if (run) {
            stroke(paint, Color.rgb(99, 102, 241), 8);
            canvas.drawLine(110, 150, 166, 150, paint);
            canvas.drawLine(116, 188, 176, 188, paint);
        }
    }

    private static void drawEatDrink(Canvas canvas, Paint paint, boolean eat) {
        if (eat) {
            fill(paint, Color.WHITE);
            canvas.drawCircle(220, 154, 76, paint);
            stroke(paint, Color.rgb(30, 41, 59), 6);
            canvas.drawCircle(220, 154, 76, paint);
            fill(paint, Color.rgb(245, 158, 11));
            canvas.drawCircle(220, 154, 42, paint);
            stroke(paint, Color.rgb(30, 41, 59), 8);
            canvas.drawLine(314, 84, 314, 236, paint);
            canvas.drawLine(334, 84, 334, 236, paint);
        } else {
            fill(paint, Color.rgb(14, 165, 233));
            canvas.drawRoundRect(new RectF(184, 82, 296, 230), 18, 18, paint);
            fill(paint, Color.argb(120, 255, 255, 255));
            canvas.drawRect(196, 124, 284, 210, paint);
            stroke(paint, Color.rgb(30, 41, 59), 7);
            canvas.drawLine(292, 82, 330, 50, paint);
        }
    }

    private static void drawLanguage(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(255, 255, 255));
        canvas.drawRoundRect(new RectF(130, 78, 350, 210), 24, 24, paint);
        fill(paint, Color.rgb(99, 102, 241));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(62);
        paint.setFakeBoldText(true);
        canvas.drawText("A", 210, 156, paint);
        canvas.drawText("B", 270, 186, paint);
        paint.setFakeBoldText(false);
    }

    private static void drawSimpleAnimal(Canvas canvas, Paint paint, String kind) {
        int color = "bird".equals(kind) ? Color.rgb(14, 165, 233) : ("dog".equals(kind) ? Color.rgb(180, 83, 9) : Color.rgb(71, 85, 105));
        fill(paint, color);
        canvas.drawOval(new RectF(160, 116, 320, 218), paint);
        canvas.drawCircle(318, 126, 38, paint);
        if ("bird".equals(kind)) {
            fill(paint, Color.rgb(250, 204, 21));
            Path beak = new Path();
            beak.moveTo(350, 126);
            beak.lineTo(386, 142);
            beak.lineTo(350, 154);
            beak.close();
            canvas.drawPath(beak, paint);
        } else {
            fill(paint, color);
            canvas.drawCircle(290, 86, 20, paint);
            canvas.drawCircle(346, 88, 20, paint);
        }
        fill(paint, Color.WHITE);
        canvas.drawCircle(330, 120, 8, paint);
    }

    private static void drawSemanticFallback(Canvas canvas, Paint paint, String key) {
        switch (key) {
            case "about":
            case "story":
            case "theme":
            case "issue":
            case "thing":
                drawSpeechCard(canvas, paint);
                break;
            case "above":
            case "after":
            case "again":
            case "along":
            case "front":
            case "north":
            case "south":
            case "reach":
                drawArrowScene(canvas, paint);
                break;
            case "agent":
            case "guard":
            case "guide":
            case "human":
            case "youth":
                drawBadgePerson(canvas, paint);
                break;
            case "alarm":
            case "watch":
            case "march":
            case "night":
            case "event":
                drawAlarmBell(canvas, paint);
                break;
            case "album":
            case "image":
            case "paint":
                drawGallery(canvas, paint);
                break;
            case "alive":
            case "fresh":
            case "plant":
                drawLeaf(canvas, paint);
                break;
            case "allow":
            case "agree":
            case "share":
            case "trust":
                drawHandshake(canvas, paint);
                break;
            case "alone":
                drawSoloPerson(canvas, paint);
                break;
            case "angel":
                drawHalo(canvas, paint);
                break;
            case "angry":
                drawMood(canvas, paint, false, true);
                break;
            case "arena":
            case "sport":
            case "match":
                drawArena(canvas, paint);
                break;
            case "beach":
            case "coast":
            case "ocean":
            case "river":
                drawWaves(canvas, paint);
                break;
            case "begin":
            case "enter":
                drawOpenDoor(canvas, paint);
                break;
            case "black":
            case "brown":
            case "green":
            case "white":
                drawColorTile(canvas, paint, key);
                break;
            case "brave":
            case "pride":
                drawMedal(canvas, paint);
                break;
            case "bread":
            case "candy":
            case "cream":
            case "fruit":
            case "grain":
            case "sugar":
            case "sweet":
                drawFood(canvas, paint, key);
                break;
            case "bring":
            case "carry":
                drawCarryBox(canvas, paint);
                break;
            case "build":
            case "house":
                drawHome(canvas, paint);
                break;
            case "catch":
            case "guess":
                drawTarget(canvas, paint);
                break;
            case "cause":
            case "power":
            case "force":
                drawBolt(canvas, paint);
                break;
            case "clean":
            case "clear":
                drawSparkleCloth(canvas, paint);
                break;
            case "cloud":
                drawCloud(canvas, paint);
                break;
            case "cover":
                drawCover(canvas, paint);
                break;
            case "dance":
                drawDancer(canvas, paint);
                break;
            case "dream":
            case "magic":
                drawDream(canvas, paint);
                break;
            case "drive":
                drawCar(canvas, paint);
                break;
            case "earth":
            case "world":
                drawGlobe(canvas, paint);
                break;
            case "empty":
                drawEmptyBox(canvas, paint);
                break;
            case "enjoy":
            case "smile":
                drawMood(canvas, paint, true, false);
                break;
            case "every":
            case "point":
                drawGrid(canvas, paint);
                break;
            case "field":
                drawField(canvas, paint);
                break;
            case "floor":
                drawFloor(canvas, paint);
                break;
            case "focus":
                drawFocus(canvas, paint);
                break;
            case "glass":
                drawGlass(canvas, paint);
                break;
            case "grace":
                drawFeather(canvas, paint);
                break;
            case "group":
            case "party":
                drawCrowd(canvas, paint);
                break;
            case "ideal":
            case "right":
                drawCheck(canvas, paint);
                break;
            case "knife":
                drawKnife(canvas, paint);
                break;
            case "laugh":
                drawMood(canvas, paint, true, true);
                break;
            case "learn":
            case "skill":
            case "teach":
                drawKnowledge(canvas, paint);
                break;
            case "light":
                drawBulb(canvas, paint);
                break;
            case "local":
            case "place":
                drawMapPin(canvas, paint);
                break;
            case "maybe":
            case "think":
                drawThought(canvas, paint);
                break;
            case "metal":
                drawMetal(canvas, paint);
                break;
            case "music":
            case "sound":
            case "noise":
            case "voice":
            case "radio":
                drawSound(canvas, paint);
                break;
            case "offer":
            case "order":
                drawClipboard(canvas, paint);
                break;
            case "paper":
                drawPaper(canvas, paint);
                break;
            case "peace":
                drawPeace(canvas, paint);
                break;
            case "phone":
                drawPhone(canvas, paint);
                break;
            case "plate":
                drawPlate(canvas, paint);
                break;
            case "price":
                drawPriceTag(canvas, paint);
                break;
            case "queen":
                drawCrown(canvas, paint);
                break;
            case "quick":
                drawSpeed(canvas, paint, true);
                break;
            case "quiet":
                drawQuiet(canvas, paint);
                break;
            case "round":
            case "shape":
                drawShapes(canvas, paint);
                break;
            case "serve":
                drawServingTray(canvas, paint);
                break;
            case "short":
                drawMeasure(canvas, paint, false);
                break;
            case "space":
                drawSpace(canvas, paint);
                break;
            case "touch":
                drawTouch(canvas, paint);
                break;
            case "train":
                drawTrain(canvas, paint);
                break;
            default:
                drawGeneric(canvas, paint, key);
        }
    }

    private static void drawGeneric(Canvas canvas, Paint paint, String key) {
        fill(paint, Color.rgb(99, 102, 241));
        canvas.drawRoundRect(new RectF(160, 72, 320, 232), 36, 36, paint);
        fill(paint, Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(68);
        paint.setFakeBoldText(true);
        canvas.drawText(key == null || key.isEmpty() ? "?" : key.substring(0, 1).toUpperCase(), 240, 174, paint);
        paint.setFakeBoldText(false);
    }

    private static void drawSpeechCard(Canvas canvas, Paint paint) {
        fill(paint, Color.WHITE);
        canvas.drawRoundRect(new RectF(120, 84, 360, 210), 24, 24, paint);
        fill(paint, Color.rgb(99, 102, 241));
        Path tail = new Path();
        tail.moveTo(196, 210);
        tail.lineTo(170, 240);
        tail.lineTo(228, 220);
        tail.close();
        canvas.drawPath(tail, paint);
        stroke(paint, Color.rgb(99, 102, 241), 8);
        canvas.drawLine(154, 122, 314, 122, paint);
        canvas.drawLine(154, 156, 294, 156, paint);
        canvas.drawLine(154, 188, 258, 188, paint);
    }

    private static void drawArrowScene(Canvas canvas, Paint paint) {
        stroke(paint, Color.rgb(37, 99, 235), 14);
        canvas.drawLine(122, 198, 308, 198, paint);
        canvas.drawLine(270, 158, 308, 198, paint);
        canvas.drawLine(270, 238, 308, 198, paint);
        fill(paint, Color.rgb(14, 165, 233));
        canvas.drawCircle(160, 130, 28, paint);
        canvas.drawCircle(328, 118, 20, paint);
    }

    private static void drawBadgePerson(Canvas canvas, Paint paint) {
        drawPeople(canvas, paint, false);
        fill(paint, Color.rgb(251, 191, 36));
        canvas.drawCircle(314, 90, 26, paint);
        fill(paint, Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(26);
        paint.setFakeBoldText(true);
        canvas.drawText("*", 314, 99, paint);
        paint.setFakeBoldText(false);
    }

    private static void drawAlarmBell(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(239, 68, 68));
        canvas.drawRoundRect(new RectF(160, 116, 320, 220), 34, 34, paint);
        fill(paint, Color.WHITE);
        canvas.drawCircle(240, 168, 42, paint);
        stroke(paint, Color.rgb(30, 41, 59), 8);
        canvas.drawCircle(240, 168, 42, paint);
        canvas.drawLine(240, 168, 240, 138, paint);
        canvas.drawLine(240, 168, 266, 184, paint);
        stroke(paint, Color.rgb(239, 68, 68), 8);
        canvas.drawLine(178, 106, 154, 82, paint);
        canvas.drawLine(302, 106, 326, 82, paint);
    }

    private static void drawGallery(Canvas canvas, Paint paint) {
        fill(paint, Color.WHITE);
        canvas.drawRoundRect(new RectF(126, 84, 354, 222), 22, 22, paint);
        stroke(paint, Color.rgb(30, 41, 59), 7);
        canvas.drawRoundRect(new RectF(126, 84, 354, 222), 22, 22, paint);
        fill(paint, Color.rgb(14, 165, 233));
        canvas.drawCircle(178, 128, 16, paint);
        Path mountain = new Path();
        mountain.moveTo(154, 196);
        mountain.lineTo(214, 142);
        mountain.lineTo(252, 182);
        mountain.lineTo(290, 130);
        mountain.lineTo(334, 196);
        mountain.close();
        fill(paint, Color.rgb(99, 102, 241));
        canvas.drawPath(mountain, paint);
    }

    private static void drawLeaf(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(34, 197, 94));
        Path leaf = new Path();
        leaf.moveTo(240, 74);
        leaf.cubicTo(150, 94, 148, 222, 240, 236);
        leaf.cubicTo(332, 222, 330, 94, 240, 74);
        canvas.drawPath(leaf, paint);
        stroke(paint, Color.WHITE, 8);
        canvas.drawLine(240, 92, 240, 220, paint);
        canvas.drawLine(240, 142, 194, 170, paint);
        canvas.drawLine(240, 164, 286, 194, paint);
    }

    private static void drawHandshake(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(14, 165, 233));
        canvas.drawRoundRect(new RectF(114, 136, 210, 198), 18, 18, paint);
        fill(paint, Color.rgb(99, 102, 241));
        canvas.drawRoundRect(new RectF(270, 136, 366, 198), 18, 18, paint);
        fill(paint, Color.rgb(250, 204, 21));
        canvas.drawRoundRect(new RectF(182, 124, 298, 208), 34, 34, paint);
        stroke(paint, Color.rgb(30, 41, 59), 6);
        canvas.drawLine(204, 148, 274, 148, paint);
        canvas.drawLine(210, 172, 268, 172, paint);
    }

    private static void drawSoloPerson(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(99, 102, 241));
        canvas.drawCircle(240, 110, 34, paint);
        fill(paint, Color.rgb(14, 165, 233));
        canvas.drawRoundRect(new RectF(184, 148, 296, 238), 28, 28, paint);
        stroke(paint, Color.rgb(30, 41, 59), 6);
        canvas.drawArc(new RectF(132, 92, 348, 256), 32, 116, false, paint);
    }

    private static void drawHalo(Canvas canvas, Paint paint) {
        drawPeople(canvas, paint, false);
        stroke(paint, Color.rgb(250, 204, 21), 10);
        canvas.drawOval(new RectF(198, 52, 282, 84), paint);
    }

    private static void drawMood(Canvas canvas, Paint paint, boolean happy, boolean openMouth) {
        fill(paint, Color.rgb(250, 204, 21));
        canvas.drawCircle(240, 150, 82, paint);
        fill(paint, Color.rgb(30, 41, 59));
        if (happy) {
            canvas.drawCircle(210, 128, 8, paint);
            canvas.drawCircle(270, 128, 8, paint);
        } else {
            stroke(paint, Color.rgb(30, 41, 59), 6);
            canvas.drawLine(196, 126, 220, 118, paint);
            canvas.drawLine(260, 118, 284, 126, paint);
        }
        stroke(paint, Color.rgb(30, 41, 59), 8);
        if (openMouth) {
            if (happy) {
                fill(paint, Color.rgb(30, 41, 59));
                canvas.drawOval(new RectF(206, 164, 274, 216), paint);
            } else {
                canvas.drawArc(new RectF(198, 176, 282, 238), 200, 140, false, paint);
            }
        } else {
            RectF mouth = new RectF(198, happy ? 144 : 168, 282, happy ? 204 : 228);
            canvas.drawArc(mouth, happy ? 20 : 200, 140, false, paint);
        }
    }

    private static void drawArena(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(16, 185, 129));
        canvas.drawOval(new RectF(110, 96, 370, 228), paint);
        stroke(paint, Color.WHITE, 8);
        canvas.drawOval(new RectF(134, 116, 346, 208), paint);
        canvas.drawLine(240, 116, 240, 208, paint);
        canvas.drawCircle(240, 162, 20, paint);
    }

    private static void drawWaves(Canvas canvas, Paint paint) {
        stroke(paint, Color.rgb(14, 165, 233), 12);
        for (int i = 0; i < 3; i++) {
            canvas.drawArc(new RectF(96, 116 + (i * 36), 222, 178 + (i * 36)), 0, 180, false, paint);
            canvas.drawArc(new RectF(210, 116 + (i * 36), 338, 178 + (i * 36)), 0, 180, false, paint);
        }
        fill(paint, Color.rgb(250, 204, 21));
        canvas.drawCircle(338, 92, 24, paint);
    }

    private static void drawOpenDoor(Canvas canvas, Paint paint) {
        drawDoor(canvas, paint);
        stroke(paint, Color.rgb(14, 165, 233), 8);
        canvas.drawLine(112, 154, 156, 154, paint);
        canvas.drawLine(144, 138, 156, 154, paint);
        canvas.drawLine(144, 170, 156, 154, paint);
    }

    private static void drawColorTile(Canvas canvas, Paint paint, String key) {
        int color;
        switch (key) {
            case "black": color = Color.rgb(15, 23, 42); break;
            case "brown": color = Color.rgb(146, 64, 14); break;
            case "green": color = Color.rgb(34, 197, 94); break;
            case "white": color = Color.WHITE; break;
            default: color = Color.rgb(99, 102, 241);
        }
        fill(paint, color);
        canvas.drawRoundRect(new RectF(142, 82, 338, 230), 30, 30, paint);
        stroke(paint, Color.rgb(30, 41, 59), 6);
        canvas.drawRoundRect(new RectF(142, 82, 338, 230), 30, 30, paint);
        if ("white".equals(key)) {
            fill(paint, Color.rgb(148, 163, 184));
            canvas.drawCircle(240, 156, 38, paint);
        }
    }

    private static void drawMedal(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(59, 130, 246));
        Path left = new Path();
        left.moveTo(196, 84);
        left.lineTo(230, 142);
        left.lineTo(204, 212);
        left.close();
        canvas.drawPath(left, paint);
        Path right = new Path();
        right.moveTo(284, 84);
        right.lineTo(250, 142);
        right.lineTo(276, 212);
        right.close();
        canvas.drawPath(right, paint);
        fill(paint, Color.rgb(250, 204, 21));
        canvas.drawCircle(240, 164, 54, paint);
        fill(paint, Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(44);
        paint.setFakeBoldText(true);
        canvas.drawText("*", 240, 178, paint);
        paint.setFakeBoldText(false);
    }

    private static void drawFood(Canvas canvas, Paint paint, String key) {
        if ("bread".equals(key)) {
            fill(paint, Color.rgb(194, 120, 58));
            canvas.drawRoundRect(new RectF(154, 110, 326, 220), 60, 60, paint);
            fill(paint, Color.rgb(236, 177, 105));
            canvas.drawRoundRect(new RectF(170, 126, 310, 204), 44, 44, paint);
        } else if ("candy".equals(key) || "sweet".equals(key) || "sugar".equals(key)) {
            fill(paint, Color.rgb(244, 114, 182));
            canvas.drawCircle(240, 162, 46, paint);
            Path l = new Path();
            l.moveTo(172, 162);
            l.lineTo(128, 134);
            l.lineTo(128, 190);
            l.close();
            canvas.drawPath(l, paint);
            Path r = new Path();
            r.moveTo(308, 162);
            r.lineTo(352, 134);
            r.lineTo(352, 190);
            r.close();
            canvas.drawPath(r, paint);
        } else if ("cream".equals(key)) {
            fill(paint, Color.WHITE);
            canvas.drawRoundRect(new RectF(164, 170, 316, 218), 16, 16, paint);
            Path swirl = new Path();
            swirl.moveTo(240, 84);
            swirl.cubicTo(188, 110, 190, 156, 240, 176);
            swirl.cubicTo(290, 156, 292, 110, 240, 84);
            canvas.drawPath(swirl, paint);
        } else if ("grain".equals(key)) {
            stroke(paint, Color.rgb(180, 83, 9), 8);
            canvas.drawLine(240, 90, 240, 226, paint);
            for (int i = 0; i < 5; i++) {
                canvas.drawOval(new RectF(204, 104 + (i * 22), 232, 124 + (i * 22)), paint);
                canvas.drawOval(new RectF(248, 114 + (i * 22), 276, 134 + (i * 22)), paint);
            }
        } else {
            fill(paint, Color.rgb(249, 115, 22));
            canvas.drawCircle(212, 162, 52, paint);
            fill(paint, Color.rgb(245, 158, 11));
            canvas.drawCircle(272, 162, 44, paint);
            fill(paint, Color.rgb(34, 197, 94));
            canvas.drawOval(new RectF(236, 94, 290, 122), paint);
        }
    }

    private static void drawCarryBox(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(180, 83, 9));
        canvas.drawRoundRect(new RectF(154, 108, 326, 224), 18, 18, paint);
        stroke(paint, Color.rgb(251, 191, 36), 8);
        canvas.drawLine(154, 162, 326, 162, paint);
        canvas.drawLine(240, 108, 240, 224, paint);
    }

    private static void drawHome(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(239, 68, 68));
        Path roof = new Path();
        roof.moveTo(134, 146);
        roof.lineTo(240, 70);
        roof.lineTo(346, 146);
        roof.close();
        canvas.drawPath(roof, paint);
        fill(paint, Color.WHITE);
        canvas.drawRoundRect(new RectF(158, 146, 322, 232), 14, 14, paint);
        fill(paint, Color.rgb(37, 99, 235));
        canvas.drawRect(224, 182, 258, 232, paint);
        canvas.drawRect(182, 170, 210, 198, paint);
        canvas.drawRect(270, 170, 298, 198, paint);
    }

    private static void drawTarget(Canvas canvas, Paint paint) {
        fill(paint, Color.WHITE);
        canvas.drawCircle(240, 154, 82, paint);
        fill(paint, Color.rgb(239, 68, 68));
        canvas.drawCircle(240, 154, 62, paint);
        fill(paint, Color.WHITE);
        canvas.drawCircle(240, 154, 40, paint);
        fill(paint, Color.rgb(37, 99, 235));
        canvas.drawCircle(240, 154, 18, paint);
    }

    private static void drawBolt(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(250, 204, 21));
        Path bolt = new Path();
        bolt.moveTo(254, 74);
        bolt.lineTo(176, 172);
        bolt.lineTo(228, 172);
        bolt.lineTo(210, 246);
        bolt.lineTo(304, 136);
        bolt.lineTo(246, 136);
        bolt.close();
        canvas.drawPath(bolt, paint);
    }

    private static void drawSparkleCloth(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(14, 165, 233));
        Path cloth = new Path();
        cloth.moveTo(164, 202);
        cloth.lineTo(204, 114);
        cloth.lineTo(316, 138);
        cloth.lineTo(276, 226);
        cloth.close();
        canvas.drawPath(cloth, paint);
        fill(paint, Color.rgb(250, 204, 21));
        canvas.drawCircle(144, 114, 14, paint);
        canvas.drawCircle(330, 96, 18, paint);
        canvas.drawCircle(316, 228, 12, paint);
    }

    private static void drawCloud(Canvas canvas, Paint paint) {
        fill(paint, Color.WHITE);
        canvas.drawCircle(192, 166, 42, paint);
        canvas.drawCircle(240, 138, 54, paint);
        canvas.drawCircle(292, 168, 40, paint);
        canvas.drawRoundRect(new RectF(158, 166, 326, 214), 24, 24, paint);
        stroke(paint, Color.rgb(148, 163, 184), 6);
        canvas.drawRoundRect(new RectF(158, 166, 326, 214), 24, 24, paint);
    }

    private static void drawCover(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(99, 102, 241));
        canvas.drawRoundRect(new RectF(156, 102, 324, 222), 18, 18, paint);
        fill(paint, Color.rgb(14, 165, 233));
        canvas.drawRoundRect(new RectF(176, 122, 304, 202), 12, 12, paint);
        stroke(paint, Color.WHITE, 6);
        canvas.drawLine(190, 162, 290, 162, paint);
    }

    private static void drawDancer(Canvas canvas, Paint paint) {
        stroke(paint, Color.rgb(239, 68, 68), 10);
        canvas.drawCircle(240, 86, 24, paint);
        canvas.drawLine(240, 110, 216, 166, paint);
        canvas.drawLine(216, 166, 172, 220, paint);
        canvas.drawLine(216, 166, 296, 210, paint);
        canvas.drawLine(228, 132, 184, 116, paint);
        canvas.drawLine(228, 132, 298, 102, paint);
    }

    private static void drawDream(Canvas canvas, Paint paint) {
        drawCloud(canvas, paint);
        fill(paint, Color.rgb(250, 204, 21));
        canvas.drawCircle(310, 108, 18, paint);
        canvas.drawCircle(338, 88, 10, paint);
        canvas.drawCircle(352, 126, 8, paint);
    }

    private static void drawCar(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(239, 68, 68));
        canvas.drawRoundRect(new RectF(134, 138, 346, 214), 26, 26, paint);
        Path roof = new Path();
        roof.moveTo(180, 138);
        roof.lineTo(222, 102);
        roof.lineTo(294, 102);
        roof.lineTo(326, 138);
        roof.close();
        canvas.drawPath(roof, paint);
        fill(paint, Color.rgb(15, 23, 42));
        canvas.drawCircle(186, 220, 22, paint);
        canvas.drawCircle(294, 220, 22, paint);
    }

    private static void drawGlobe(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(14, 165, 233));
        canvas.drawCircle(240, 154, 84, paint);
        stroke(paint, Color.WHITE, 6);
        canvas.drawCircle(240, 154, 84, paint);
        canvas.drawArc(new RectF(180, 70, 300, 238), 90, 180, false, paint);
        canvas.drawArc(new RectF(140, 70, 340, 238), 90, 180, false, paint);
        canvas.drawLine(156, 154, 324, 154, paint);
        canvas.drawLine(168, 122, 312, 122, paint);
        canvas.drawLine(168, 186, 312, 186, paint);
    }

    private static void drawEmptyBox(Canvas canvas, Paint paint) {
        stroke(paint, Color.rgb(148, 163, 184), 8);
        canvas.drawRect(168, 114, 312, 220, paint);
        canvas.drawLine(168, 114, 240, 156, paint);
        canvas.drawLine(312, 114, 240, 156, paint);
        canvas.drawLine(240, 156, 240, 220, paint);
    }

    private static void drawGrid(Canvas canvas, Paint paint) {
        stroke(paint, Color.rgb(99, 102, 241), 8);
        for (int x = 148; x <= 332; x += 62) {
            canvas.drawLine(x, 98, x, 234, paint);
        }
        for (int y = 98; y <= 234; y += 46) {
            canvas.drawLine(148, y, 332, y, paint);
        }
        fill(paint, Color.rgb(250, 204, 21));
        canvas.drawCircle(240, 166, 12, paint);
    }

    private static void drawField(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(34, 197, 94));
        canvas.drawRoundRect(new RectF(116, 124, 364, 232), 18, 18, paint);
        stroke(paint, Color.WHITE, 8);
        canvas.drawLine(116, 178, 364, 178, paint);
        canvas.drawLine(240, 124, 240, 232, paint);
        canvas.drawCircle(240, 178, 24, paint);
    }

    private static void drawFloor(Canvas canvas, Paint paint) {
        stroke(paint, Color.rgb(180, 83, 9), 8);
        for (int y = 132; y <= 228; y += 32) {
            canvas.drawLine(124, y, 356, y, paint);
        }
        for (int x = 124; x <= 356; x += 58) {
            canvas.drawLine(x, 132, x, 228, paint);
        }
    }

    private static void drawFocus(Canvas canvas, Paint paint) {
        stroke(paint, Color.rgb(99, 102, 241), 10);
        canvas.drawCircle(240, 156, 56, paint);
        canvas.drawCircle(240, 156, 24, paint);
        canvas.drawLine(240, 76, 240, 110, paint);
        canvas.drawLine(240, 202, 240, 236, paint);
        canvas.drawLine(160, 156, 194, 156, paint);
        canvas.drawLine(286, 156, 320, 156, paint);
    }

    private static void drawGlass(Canvas canvas, Paint paint) {
        stroke(paint, Color.rgb(14, 165, 233), 8);
        Path glass = new Path();
        glass.moveTo(178, 84);
        glass.lineTo(302, 84);
        glass.lineTo(280, 228);
        glass.lineTo(200, 228);
        glass.close();
        canvas.drawPath(glass, paint);
        fill(paint, Color.argb(120, 125, 211, 252));
        canvas.drawRect(194, 148, 286, 224, paint);
    }

    private static void drawFeather(Canvas canvas, Paint paint) {
        fill(paint, Color.WHITE);
        Path feather = new Path();
        feather.moveTo(302, 84);
        feather.cubicTo(210, 88, 158, 180, 200, 232);
        feather.cubicTo(256, 212, 314, 158, 302, 84);
        canvas.drawPath(feather, paint);
        stroke(paint, Color.rgb(148, 163, 184), 6);
        canvas.drawLine(204, 224, 288, 98, paint);
    }

    private static void drawCrowd(Canvas canvas, Paint paint) {
        drawPeople(canvas, paint, true);
        fill(paint, Color.rgb(244, 114, 182));
        canvas.drawCircle(152, 146, 22, paint);
        canvas.drawCircle(328, 146, 22, paint);
    }

    private static void drawCheck(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(34, 197, 94));
        canvas.drawCircle(240, 156, 88, paint);
        stroke(paint, Color.WHITE, 14);
        canvas.drawLine(194, 158, 226, 190, paint);
        canvas.drawLine(226, 190, 290, 126, paint);
    }

    private static void drawKnowledge(Canvas canvas, Paint paint) {
        drawBook(canvas, paint);
        fill(paint, Color.rgb(250, 204, 21));
        canvas.drawCircle(240, 88, 18, paint);
    }

    private static void drawKnife(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(148, 163, 184));
        Path blade = new Path();
        blade.moveTo(136, 176);
        blade.lineTo(288, 108);
        blade.lineTo(318, 138);
        blade.lineTo(170, 204);
        blade.close();
        canvas.drawPath(blade, paint);
        fill(paint, Color.rgb(120, 53, 15));
        canvas.drawRoundRect(new RectF(292, 132, 354, 188), 16, 16, paint);
    }

    private static void drawBulb(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(250, 204, 21));
        canvas.drawCircle(240, 138, 60, paint);
        fill(paint, Color.rgb(71, 85, 105));
        canvas.drawRoundRect(new RectF(212, 188, 268, 234), 12, 12, paint);
        stroke(paint, Color.rgb(245, 158, 11), 8);
        canvas.drawLine(240, 52, 240, 76, paint);
        canvas.drawLine(176, 84, 192, 100, paint);
        canvas.drawLine(288, 100, 304, 84, paint);
    }

    private static void drawMapPin(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(239, 68, 68));
        Path pin = new Path();
        pin.moveTo(240, 68);
        pin.cubicTo(174, 68, 156, 146, 240, 242);
        pin.cubicTo(324, 146, 306, 68, 240, 68);
        canvas.drawPath(pin, paint);
        fill(paint, Color.WHITE);
        canvas.drawCircle(240, 124, 28, paint);
    }

    private static void drawThought(Canvas canvas, Paint paint) {
        fill(paint, Color.WHITE);
        canvas.drawCircle(206, 152, 46, paint);
        canvas.drawCircle(256, 142, 56, paint);
        canvas.drawCircle(304, 160, 42, paint);
        canvas.drawRoundRect(new RectF(184, 152, 322, 198), 22, 22, paint);
        fill(paint, Color.rgb(148, 163, 184));
        canvas.drawCircle(176, 220, 10, paint);
        canvas.drawCircle(158, 244, 6, paint);
    }

    private static void drawMetal(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(148, 163, 184));
        canvas.drawRoundRect(new RectF(148, 94, 332, 218), 20, 20, paint);
        stroke(paint, Color.WHITE, 6);
        canvas.drawLine(164, 122, 316, 122, paint);
        canvas.drawLine(164, 154, 316, 154, paint);
        canvas.drawLine(164, 186, 316, 186, paint);
    }

    private static void drawSound(Canvas canvas, Paint paint) {
        drawAudio(canvas, paint, false);
        stroke(paint, Color.rgb(14, 165, 233), 8);
        canvas.drawArc(new RectF(270, 100, 350, 216), -45, 90, false, paint);
    }

    private static void drawClipboard(Canvas canvas, Paint paint) {
        fill(paint, Color.WHITE);
        canvas.drawRoundRect(new RectF(160, 84, 320, 228), 18, 18, paint);
        fill(paint, Color.rgb(99, 102, 241));
        canvas.drawRoundRect(new RectF(206, 68, 274, 104), 12, 12, paint);
        stroke(paint, Color.rgb(30, 41, 59), 6);
        canvas.drawLine(190, 136, 290, 136, paint);
        canvas.drawLine(190, 170, 282, 170, paint);
        canvas.drawLine(190, 202, 264, 202, paint);
    }

    private static void drawPaper(Canvas canvas, Paint paint) {
        fill(paint, Color.WHITE);
        canvas.drawRoundRect(new RectF(164, 78, 316, 234), 14, 14, paint);
        stroke(paint, Color.rgb(148, 163, 184), 6);
        canvas.drawLine(192, 118, 288, 118, paint);
        canvas.drawLine(192, 148, 288, 148, paint);
        canvas.drawLine(192, 178, 268, 178, paint);
    }

    private static void drawPeace(Canvas canvas, Paint paint) {
        stroke(paint, Color.rgb(34, 197, 94), 10);
        canvas.drawCircle(240, 156, 80, paint);
        canvas.drawLine(240, 78, 240, 234, paint);
        canvas.drawLine(240, 156, 186, 214, paint);
        canvas.drawLine(240, 156, 294, 214, paint);
    }

    private static void drawPhone(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(30, 41, 59));
        canvas.drawRoundRect(new RectF(184, 74, 296, 238), 24, 24, paint);
        fill(paint, Color.rgb(125, 211, 252));
        canvas.drawRoundRect(new RectF(198, 96, 282, 206), 12, 12, paint);
        fill(paint, Color.WHITE);
        canvas.drawCircle(240, 222, 8, paint);
    }

    private static void drawPlate(Canvas canvas, Paint paint) {
        fill(paint, Color.WHITE);
        canvas.drawCircle(240, 156, 86, paint);
        stroke(paint, Color.rgb(148, 163, 184), 8);
        canvas.drawCircle(240, 156, 86, paint);
        canvas.drawCircle(240, 156, 48, paint);
    }

    private static void drawPriceTag(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(249, 115, 22));
        Path tag = new Path();
        tag.moveTo(168, 110);
        tag.lineTo(276, 110);
        tag.lineTo(332, 166);
        tag.lineTo(236, 262);
        tag.lineTo(148, 174);
        tag.close();
        canvas.drawPath(tag, paint);
        fill(paint, Color.WHITE);
        canvas.drawCircle(212, 142, 12, paint);
    }

    private static void drawCrown(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(250, 204, 21));
        Path crown = new Path();
        crown.moveTo(138, 212);
        crown.lineTo(162, 106);
        crown.lineTo(220, 158);
        crown.lineTo(240, 92);
        crown.lineTo(260, 158);
        crown.lineTo(318, 106);
        crown.lineTo(342, 212);
        crown.close();
        canvas.drawPath(crown, paint);
        fill(paint, Color.rgb(59, 130, 246));
        canvas.drawCircle(186, 148, 10, paint);
        canvas.drawCircle(240, 126, 10, paint);
        canvas.drawCircle(294, 148, 10, paint);
    }

    private static void drawQuiet(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(250, 204, 21));
        canvas.drawCircle(220, 154, 72, paint);
        fill(paint, Color.rgb(30, 41, 59));
        canvas.drawCircle(194, 134, 8, paint);
        canvas.drawCircle(246, 134, 8, paint);
        stroke(paint, Color.rgb(30, 41, 59), 8);
        canvas.drawLine(196, 184, 244, 184, paint);
        fill(paint, Color.WHITE);
        canvas.drawRoundRect(new RectF(264, 116, 320, 202), 18, 18, paint);
        fill(paint, Color.rgb(99, 102, 241));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(44);
        paint.setFakeBoldText(true);
        canvas.drawText("!", 292, 174, paint);
        paint.setFakeBoldText(false);
    }

    private static void drawShapes(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(239, 68, 68));
        canvas.drawCircle(174, 154, 42, paint);
        fill(paint, Color.rgb(34, 197, 94));
        canvas.drawRect(214, 114, 294, 194, paint);
        fill(paint, Color.rgb(59, 130, 246));
        Path tri = new Path();
        tri.moveTo(334, 198);
        tri.lineTo(302, 126);
        tri.lineTo(266, 198);
        tri.close();
        canvas.drawPath(tri, paint);
    }

    private static void drawServingTray(Canvas canvas, Paint paint) {
        stroke(paint, Color.rgb(30, 41, 59), 8);
        canvas.drawArc(new RectF(146, 114, 334, 212), 180, 180, false, paint);
        canvas.drawLine(132, 212, 348, 212, paint);
        canvas.drawLine(240, 98, 240, 132, paint);
        canvas.drawArc(new RectF(214, 74, 266, 118), 180, 180, false, paint);
    }

    private static void drawMeasure(Canvas canvas, Paint paint, boolean tall) {
        stroke(paint, Color.rgb(99, 102, 241), 10);
        canvas.drawLine(156, 220, 324, 220, paint);
        canvas.drawLine(180, 100, 180, 220, paint);
        canvas.drawLine(300, tall ? 80 : 142, 300, 220, paint);
    }

    private static void drawSpace(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(15, 23, 42));
        canvas.drawRoundRect(new RectF(116, 72, 364, 236), 30, 30, paint);
        fill(paint, Color.rgb(250, 204, 21));
        canvas.drawCircle(172, 116, 10, paint);
        canvas.drawCircle(318, 100, 12, paint);
        canvas.drawCircle(292, 204, 8, paint);
        fill(paint, Color.rgb(14, 165, 233));
        Path rocket = new Path();
        rocket.moveTo(240, 92);
        rocket.lineTo(280, 164);
        rocket.lineTo(240, 218);
        rocket.lineTo(200, 164);
        rocket.close();
        canvas.drawPath(rocket, paint);
    }

    private static void drawTouch(Canvas canvas, Paint paint) {
        stroke(paint, Color.rgb(30, 41, 59), 10);
        canvas.drawLine(204, 220, 204, 124, paint);
        canvas.drawLine(228, 220, 228, 106, paint);
        canvas.drawLine(252, 220, 252, 124, paint);
        canvas.drawLine(276, 220, 276, 146, paint);
        canvas.drawLine(180, 180, 300, 180, paint);
        fill(paint, Color.rgb(14, 165, 233));
        canvas.drawCircle(310, 112, 18, paint);
    }

    private static void drawTrain(Canvas canvas, Paint paint) {
        fill(paint, Color.rgb(37, 99, 235));
        canvas.drawRoundRect(new RectF(144, 96, 336, 214), 24, 24, paint);
        fill(paint, Color.WHITE);
        canvas.drawRoundRect(new RectF(166, 116, 238, 166), 12, 12, paint);
        canvas.drawRoundRect(new RectF(250, 116, 322, 166), 12, 12, paint);
        fill(paint, Color.rgb(15, 23, 42));
        canvas.drawCircle(188, 222, 18, paint);
        canvas.drawCircle(292, 222, 18, paint);
    }
}
