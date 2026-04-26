package com.example.kelimeezberleme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "KelimeEzberleme.db";
    public static final int DATABASE_VERSION = 6; // Sürümü 6 yapıyoruz (Analiz Modülü)

    public static final String TABLE_USERS = "Users";
    public static final String COL_USER_ID = "UserID";
    public static final String COL_USER_NAME = "UserName";
    public static final String COL_PASSWORD = "Password";

    public static final String TABLE_WORDS = "Words";
    public static final String COL_WORD_ID = "WordID";
    public static final String COL_ENG_WORD = "EngWordName";
    public static final String COL_TUR_WORD = "TurWordName";
    public static final String COL_PICTURE = "Picture";
    public static final String COL_STEP_COUNT = "StepCount";
    public static final String COL_NEXT_QUIZ_DATE = "NextQuizDate";
    public static final String COL_CATEGORY = "Category"; // Yeni: Konu
    public static final String COL_TOTAL_ATTEMPTS = "TotalAttempts"; // Yeni: Toplam Çözülme
    public static final String COL_CORRECT_ATTEMPTS = "CorrectAttempts"; // Yeni: Doğru Bilinme

    public static final String TABLE_SAMPLES = "WordSamples";
    public static final String COL_SAMPLE_ID = "WordSamplesID";
    public static final String COL_SAMPLE_TEXT = "Samples";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (UserID INTEGER PRIMARY KEY AUTOINCREMENT, UserName TEXT, Password TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_WORDS + " (" +
                COL_WORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ENG_WORD + " TEXT, " +
                COL_TUR_WORD + " TEXT, " +
                COL_PICTURE + " TEXT, " +
                COL_STEP_COUNT + " INTEGER DEFAULT 0, " +
                COL_NEXT_QUIZ_DATE + " LONG DEFAULT 0, " +
                COL_CATEGORY + " TEXT DEFAULT 'Genel', " +
                COL_TOTAL_ATTEMPTS + " INTEGER DEFAULT 0, " +
                COL_CORRECT_ATTEMPTS + " INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE " + TABLE_SAMPLES + " (" +
                COL_SAMPLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_WORD_ID + " INTEGER, " +
                COL_SAMPLE_TEXT + " TEXT, " +
                "FOREIGN KEY(" + COL_WORD_ID + ") REFERENCES " + TABLE_WORDS + "(" + COL_WORD_ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAMPLES);
        onCreate(db);
    }

    public long addWord(String eng, String tur, String picPath, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ENG_WORD, eng);
        contentValues.put(COL_TUR_WORD, tur);
        contentValues.put(COL_PICTURE, picPath);
        contentValues.put(COL_CATEGORY, (category == null || category.isEmpty()) ? "Genel" : category);
        contentValues.put(COL_STEP_COUNT, 0);
        contentValues.put(COL_NEXT_QUIZ_DATE, System.currentTimeMillis());
        contentValues.put(COL_TOTAL_ATTEMPTS, 0);
        contentValues.put(COL_CORRECT_ATTEMPTS, 0);
        return db.insert(TABLE_WORDS, null, contentValues);
    }

    public boolean addSample(long wordId, String sample) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_WORD_ID, (int)wordId);
        contentValues.put(COL_SAMPLE_TEXT, sample);
        long result = db.insert(TABLE_SAMPLES, null, contentValues);
        return result != -1;
    }

    public List<Word> getWordsForQuiz(int limit) {
        List<Word> words = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        long currentTime = System.currentTimeMillis();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_WORDS + " WHERE NextQuizDate <= ? AND StepCount < 6 ORDER BY RANDOM() LIMIT ?", 
                new String[]{String.valueOf(currentTime), String.valueOf(limit)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                words.add(new Word(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), 
                        cursor.getInt(4), cursor.getLong(5), cursor.getString(6), cursor.getInt(7), cursor.getInt(8)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return words;
    }

    public List<String> getRandomWrongAnswers(int correctWordId) {
        List<String> wrongs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_TUR_WORD + " FROM " + TABLE_WORDS + " WHERE " + COL_WORD_ID + " != ? ORDER BY RANDOM() LIMIT 3", 
                new String[]{String.valueOf(correctWordId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                wrongs.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return wrongs;
    }

    public void updateWordProgress(int wordId, int currentStep, boolean isCorrect) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Önce mevcut değerleri alalım
        Cursor cursor = db.rawQuery("SELECT " + COL_TOTAL_ATTEMPTS + ", " + COL_CORRECT_ATTEMPTS + " FROM " + TABLE_WORDS + " WHERE " + COL_WORD_ID + "=?", new String[]{String.valueOf(wordId)});
        int total = 0, correct = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
            correct = cursor.getInt(1);
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_TOTAL_ATTEMPTS, total + 1);
        if (isCorrect) {
            values.put(COL_CORRECT_ATTEMPTS, correct + 1);
            int nextStep = currentStep + 1;
            values.put(COL_STEP_COUNT, nextStep);
            values.put(COL_NEXT_QUIZ_DATE, calculateNextDate(nextStep));
        } else {
            values.put(COL_STEP_COUNT, 0);
            values.put(COL_NEXT_QUIZ_DATE, System.currentTimeMillis());
        }

        db.update(TABLE_WORDS, values, COL_WORD_ID + "=?", new String[]{String.valueOf(wordId)});
    }

    private long calculateNextDate(int step) {
        long now = System.currentTimeMillis();
        long day = 24 * 60 * 60 * 1000L;
        switch (step) {
            case 1: return now + day;
            case 2: return now + (7 * day);
            case 3: return now + (30 * day);
            case 4: return now + (90 * day);
            case 5: return now + (180 * day);
            case 6: return now + (365 * day);
            default: return now;
        }
    }

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USER_NAME, username);
        contentValues.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, contentValues);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE UserName=? AND Password=?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PASSWORD, newPassword);
        int result = db.update(TABLE_USERS, contentValues, "UserName=?", new String[]{username});
        return result > 0;
    }

    public List<Word> getAllWords() {
        List<Word> words = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_WORDS, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                words.add(new Word(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), 
                        cursor.getInt(4), cursor.getLong(5), cursor.getString(6), cursor.getInt(7), cursor.getInt(8)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return words;
    }

    public void seedDatabase() {
        SQLiteDatabase dbRead = this.getReadableDatabase();
        Cursor cursor = dbRead.rawQuery("SELECT COUNT(*) FROM " + TABLE_WORDS, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count == 0) {
            String[][] seedWords = {
                // {İngilizce, Türkçe, Kategori}
                {"Apple", "Elma", "Meyveler"}, {"Book", "Kitap", "Eğitim"}, {"Computer", "Bilgisayar", "Teknoloji"}, 
                {"Water", "Su", "Doğa"}, {"School", "Okul", "Eğitim"}, {"Pen", "Kalem", "Eğitim"}, 
                {"Door", "Kapı", "Ev"}, {"Window", "Pencere", "Ev"}, {"Table", "Masa", "Ev"}, 
                {"Chair", "Sandalye", "Ev"}, {"Friend", "Arkadaş", "Sosyal"}, {"Family", "Aile", "Sosyal"}, 
                {"Heart", "Kalp", "Vücut"}, {"Sun", "Güneş", "Doğa"}, {"Moon", "Ay", "Doğa"}, 
                {"Star", "Yıldız", "Doğa"}, {"Time", "Zaman", "Soyut"}, {"City", "Şehir", "Yer"}, 
                {"Country", "Ülke", "Yer"}, {"Money", "Para", "Ekonomi"}, {"Work", "İş", "İş Dünyası"}, 
                {"Sleep", "Uyku", "Sağlık"}, {"Happy", "Mutlu", "Duygular"}, {"Sad", "Üzgün", "Duygular"}, 
                {"Beautiful", "Güzel", "Sıfatlar"}, {"Big", "Büyük", "Sıfatlar"}, {"Small", "Küçük", "Sıfatlar"}, 
                {"New", "Yeni", "Sıfatlar"}, {"Old", "Eski", "Sıfatlar"}, {"Good", "İyi", "Sıfatlar"}, 
                {"Bad", "Kötü", "Sıfatlar"}, {"Fast", "Hızlı", "Sıfatlar"}, {"Slow", "Yavaş", "Sıfatlar"}, 
                {"Hot", "Sıcak", "Sıfatlar"}, {"Cold", "Soğuk", "Sıfatlar"}, {"Easy", "Kolay", "Sıfatlar"}, 
                {"Hard", "Zor", "Sıfatlar"}, {"Read", "Okumak", "Fiiller"}, {"Write", "Yazmak", "Fiiller"}, 
                {"Listen", "Dinlemek", "Fiiller"}, {"Speak", "Konuşmak", "Fiiller"}, {"Run", "Koşmak", "Fiiller"}, 
                {"Walk", "Yürümek", "Fiiller"}, {"Eat", "Yemek Yemek", "Fiiller"}, {"Drink", "İçmek", "Fiiller"}, 
                {"Language", "Dil", "Eğitim"}, {"Bird", "Kuş", "Hayvanlar"}, {"Dog", "Köpek", "Hayvanlar"}, 
                {"Cat", "Kedi", "Hayvanlar"}, {"Flower", "Çiçek", "Doğa"}
            };

            SQLiteDatabase dbWrite = this.getWritableDatabase();
            for (String[] w : seedWords) {
                ContentValues cv = new ContentValues();
                cv.put(COL_ENG_WORD, w[0]);
                cv.put(COL_TUR_WORD, w[1]);
                cv.put(COL_PICTURE, "");
                cv.put(COL_CATEGORY, w[2]);
                cv.put(COL_STEP_COUNT, 0);
                cv.put(COL_NEXT_QUIZ_DATE, System.currentTimeMillis());
                cv.put(COL_TOTAL_ATTEMPTS, 0);
                cv.put(COL_CORRECT_ATTEMPTS, 0);
                dbWrite.insert(TABLE_WORDS, null, cv);
            }
        }
    }
}