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
    public static final int DATABASE_VERSION = 5; // Sürümü 5 yapıyoruz

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
                COL_NEXT_QUIZ_DATE + " LONG DEFAULT 0)");
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

    public long addWord(String eng, String tur, String picPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ENG_WORD, eng);
        contentValues.put(COL_TUR_WORD, tur);
        contentValues.put(COL_PICTURE, picPath);
        contentValues.put(COL_STEP_COUNT, 0);
        contentValues.put(COL_NEXT_QUIZ_DATE, System.currentTimeMillis());
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
                words.add(new Word(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getLong(5)));
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

    public void updateWordProgress(int wordId, int currentStep) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        int nextStep = currentStep + 1;
        values.put(COL_STEP_COUNT, nextStep);
        values.put(COL_NEXT_QUIZ_DATE, calculateNextDate(nextStep));
        db.update(TABLE_WORDS, values, COL_WORD_ID + "=?", new String[]{String.valueOf(wordId)});
    }

    public void resetWordProgress(int wordId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STEP_COUNT, 0);
        values.put(COL_NEXT_QUIZ_DATE, System.currentTimeMillis());
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
                words.add(new Word(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getLong(5)));
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
                {"Apple", "Elma"}, {"Book", "Kitap"}, {"Computer", "Bilgisayar"}, {"Water", "Su"}, {"School", "Okul"},
                {"Pen", "Kalem"}, {"Door", "Kapı"}, {"Window", "Pencere"}, {"Table", "Masa"}, {"Chair", "Sandalye"},
                {"Friend", "Arkadaş"}, {"Family", "Aile"}, {"Heart", "Kalp"}, {"Sun", "Güneş"}, {"Moon", "Ay"},
                {"Star", "Yıldız"}, {"Time", "Zaman"}, {"City", "Şehir"}, {"Country", "Ülke"}, {"Money", "Para"},
                {"Work", "İş"}, {"Sleep", "Uyku"}, {"Happy", "Mutlu"}, {"Sad", "Üzgün"}, {"Beautiful", "Güzel"},
                {"Big", "Büyük"}, {"Small", "Küçük"}, {"New", "Yeni"}, {"Old", "Eski"}, {"Good", "İyi"},
                {"Bad", "Kötü"}, {"Fast", "Hızlı"}, {"Slow", "Yavaş"}, {"Hot", "Sıcak"}, {"Cold", "Soğuk"},
                {"Easy", "Kolay"}, {"Hard", "Zor"}, {"Read", "Okumak"}, {"Write", "Yazmak"}, {"Listen", "Dinlemek"},
                {"Speak", "Konuşmak"}, {"Run", "Koşmak"}, {"Walk", "Yürümek"}, {"Eat", "Yemek Yemek"}, {"Drink", "İçmek"},
                {"Language", "Dil"}, {"Bird", "Kuş"}, {"Dog", "Köpek"}, {"Cat", "Kedi"}, {"Flower", "Çiçek"}
            };

            SQLiteDatabase dbWrite = this.getWritableDatabase();
            for (String[] w : seedWords) {
                ContentValues cv = new ContentValues();
                cv.put(COL_ENG_WORD, w[0]);
                cv.put(COL_TUR_WORD, w[1]);
                cv.put(COL_PICTURE, "");
                cv.put(COL_STEP_COUNT, 0);
                cv.put(COL_NEXT_QUIZ_DATE, System.currentTimeMillis());
                dbWrite.insert(TABLE_WORDS, null, cv);
            }
        }
    }
}