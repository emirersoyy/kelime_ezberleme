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
            default: drawGeneric(canvas, paint, key);
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
}
