package com.example.kelimeezberleme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "KelimeEzberleme.db";
    public static final int DATABASE_VERSION = 12;
    private static final String PASSWORD_HASH_PREFIX = "pbkdf2";
    private static final int PASSWORD_HASH_ITERATIONS = 120000;
    private static final int PASSWORD_SALT_BYTES = 16;
    private static final int PASSWORD_KEY_BITS = 256;

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
    public static final String COL_CATEGORY = "Category";
    public static final String COL_TOTAL_ATTEMPTS = "TotalAttempts";
    public static final String COL_CORRECT_ATTEMPTS = "CorrectAttempts";

    public static final String TABLE_SAMPLES = "WordSamples";
    public static final String COL_SAMPLE_ID = "WordSamplesID";
    public static final String COL_SAMPLE_TEXT = "Samples";
    public static final String COL_SAMPLE_USED = "IsUsed";

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
                COL_SAMPLE_USED + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + COL_WORD_ID + ") REFERENCES " + TABLE_WORDS + "(" + COL_WORD_ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ensureCurrentSchema(db);
    }

    private void ensureCurrentSchema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (UserID INTEGER PRIMARY KEY AUTOINCREMENT, UserName TEXT, Password TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_WORDS + " (" +
                COL_WORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ENG_WORD + " TEXT, " +
                COL_TUR_WORD + " TEXT, " +
                COL_PICTURE + " TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SAMPLES + " (" +
                COL_SAMPLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_WORD_ID + " INTEGER, " +
                COL_SAMPLE_TEXT + " TEXT)");

        ensureColumn(db, TABLE_WORDS, COL_STEP_COUNT, "INTEGER DEFAULT 0");
        ensureColumn(db, TABLE_WORDS, COL_NEXT_QUIZ_DATE, "LONG DEFAULT 0");
        ensureColumn(db, TABLE_WORDS, COL_CATEGORY, "TEXT DEFAULT 'Genel'");
        ensureColumn(db, TABLE_WORDS, COL_TOTAL_ATTEMPTS, "INTEGER DEFAULT 0");
        ensureColumn(db, TABLE_WORDS, COL_CORRECT_ATTEMPTS, "INTEGER DEFAULT 0");
        ensureColumn(db, TABLE_SAMPLES, COL_SAMPLE_USED, "INTEGER DEFAULT 0");
    }

    private void ensureColumn(SQLiteDatabase db, String tableName, String columnName, String columnDefinition) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        boolean exists = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (columnName.equalsIgnoreCase(cursor.getString(1))) {
                        exists = true;
                        break;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        if (!exists) {
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
        }
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
        contentValues.put(COL_WORD_ID, (int) wordId);
        contentValues.put(COL_SAMPLE_TEXT, sample);
        contentValues.put(COL_SAMPLE_USED, 0);
        long result = db.insert(TABLE_SAMPLES, null, contentValues);
        return result != -1;
    }

    public List<String> getSamplesForWord(int wordId) {
        List<String> samples = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_SAMPLE_TEXT + " FROM " + TABLE_SAMPLES +
                        " WHERE " + COL_WORD_ID + "=? ORDER BY " + COL_SAMPLE_ID,
                new String[]{String.valueOf(wordId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                samples.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return samples;
    }

    public List<String> getDisplaySamplesForWord(Word word) {
        List<String> currentSamples = getSamplesForWord(word.id);
        if (shouldRefreshSamples(currentSamples, word.eng)) {
            String[] replacements = samplesForWord(word.eng, word.tur, word.category);
            replaceSamplesForWord(word.id, replacements);
            currentSamples = new ArrayList<>();
            for (String replacement : replacements) {
                currentSamples.add(replacement);
            }
        }
        return currentSamples;
    }

    public SampleSentence getNextUnusedSampleForWord(int wordId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_SAMPLE_ID + ", " + COL_SAMPLE_TEXT +
                        " FROM " + TABLE_SAMPLES +
                        " WHERE " + COL_WORD_ID + "=? AND " + COL_SAMPLE_USED + "=0" +
                        " ORDER BY " + COL_SAMPLE_ID + " LIMIT 1",
                new String[]{String.valueOf(wordId)});
        if (cursor != null && cursor.moveToFirst()) {
            SampleSentence sample = new SampleSentence(cursor.getInt(0), cursor.getString(1));
            cursor.close();
            return sample;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public void markSamplesUsed(List<Integer> sampleIds) {
        if (sampleIds == null || sampleIds.isEmpty()) return;

        Set<Integer> uniqueIds = new HashSet<>(sampleIds);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SAMPLE_USED, 1);
        for (Integer sampleId : uniqueIds) {
            if (sampleId != null && sampleId > 0) {
                db.update(TABLE_SAMPLES, values, COL_SAMPLE_ID + "=?", new String[]{String.valueOf(sampleId)});
            }
        }
    }

    public List<Word> getWordsForQuiz(int limit) {
        List<Word> words = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        long currentTime = System.currentTimeMillis();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_WORDS +
                        " WHERE " + COL_NEXT_QUIZ_DATE + " <= ? AND " + COL_STEP_COUNT + " < 6" +
                        " ORDER BY RANDOM() LIMIT ?",
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
        Cursor cursor = db.rawQuery("SELECT " + COL_TUR_WORD + " FROM " + TABLE_WORDS +
                        " WHERE " + COL_WORD_ID + " != ? ORDER BY RANDOM() LIMIT 3",
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

        Cursor cursor = db.rawQuery("SELECT " + COL_TOTAL_ATTEMPTS + ", " + COL_CORRECT_ATTEMPTS +
                " FROM " + TABLE_WORDS + " WHERE " + COL_WORD_ID + "=?", new String[]{String.valueOf(wordId)});
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

    public void resetAnalysisStatistics() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TOTAL_ATTEMPTS, 0);
        values.put(COL_CORRECT_ATTEMPTS, 0);
        db.update(TABLE_WORDS, values, null, null);
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
        String cleanUsername = username == null ? "" : username.trim();
        if (cleanUsername.isEmpty() || isUsernameTaken(cleanUsername)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USER_NAME, cleanUsername);
        contentValues.put(COL_PASSWORD, hashPassword(password));
        long result = db.insert(TABLE_USERS, null, contentValues);
        return result != -1;
    }

    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_USERS + " WHERE LOWER(" + COL_USER_NAME + ") = LOWER(?) LIMIT 1",
                new String[]{username == null ? "" : username.trim()});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean checkUser(String username, String password) {
        String cleanUsername = username == null ? "" : username.trim();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_PASSWORD + " FROM " + TABLE_USERS + " WHERE LOWER(" + COL_USER_NAME + ") = LOWER(?) LIMIT 1",
                new String[]{cleanUsername});
        boolean exists = false;
        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(0);
            exists = verifyPassword(password, storedPassword);
            if (exists && !isHashedPassword(storedPassword)) {
                ContentValues values = new ContentValues();
                values.put(COL_PASSWORD, hashPassword(password));
                db.update(TABLE_USERS, values, "LOWER(" + COL_USER_NAME + ") = LOWER(?)", new String[]{cleanUsername});
            }
        }
        cursor.close();
        return exists;
    }

    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PASSWORD, hashPassword(newPassword));
        int result = db.update(TABLE_USERS, contentValues, "LOWER(" + COL_USER_NAME + ") = LOWER(?)", new String[]{username == null ? "" : username.trim()});
        return result > 0;
    }

    private String hashPassword(String password) {
        try {
            byte[] salt = new byte[PASSWORD_SALT_BYTES];
            new SecureRandom().nextBytes(salt);
            byte[] hash = pbkdf2(password == null ? "" : password, salt, PASSWORD_HASH_ITERATIONS);
            return PASSWORD_HASH_PREFIX + ":" + PASSWORD_HASH_ITERATIONS + ":" +
                    Base64.encodeToString(salt, Base64.NO_WRAP) + ":" +
                    Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }

    private boolean verifyPassword(String password, String storedPassword) {
        if (storedPassword == null) return false;
        if (!isHashedPassword(storedPassword)) {
            return storedPassword.equals(password);
        }

        try {
            String[] parts = storedPassword.split(":");
            if (parts.length != 4) return false;
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.decode(parts[2], Base64.NO_WRAP);
            byte[] expectedHash = Base64.decode(parts[3], Base64.NO_WRAP);
            byte[] actualHash = pbkdf2(password == null ? "" : password, salt, iterations);
            return constantTimeEquals(expectedHash, actualHash);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isHashedPassword(String storedPassword) {
        return storedPassword != null && storedPassword.startsWith(PASSWORD_HASH_PREFIX + ":");
    }

    private byte[] pbkdf2(String password, byte[] salt, int iterations) throws Exception {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, PASSWORD_KEY_BITS);
        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        } catch (Exception ignored) {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        }
        return factory.generateSecret(spec).getEncoded();
    }

    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) return false;
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
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

    public String getRandomWordForWordle() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_ENG_WORD + " FROM " + TABLE_WORDS + " WHERE length(trim(" + COL_ENG_WORD + ")) = 5 ORDER BY RANDOM() LIMIT 1", null);
        String word = null;
        if (cursor.moveToFirst()) {
            word = cursor.getString(0).toUpperCase(Locale.US);
        }
        cursor.close();
        return word;
    }

    public String getRandomWordForWordle(Set<String> eligibleWordIds) {
        if (eligibleWordIds == null || eligibleWordIds.isEmpty()) return null;

        List<String> cleanIds = new ArrayList<>();
        for (String id : eligibleWordIds) {
            if (id != null && id.matches("\\d+")) {
                cleanIds.add(id);
            }
        }
        if (cleanIds.isEmpty()) return null;

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < cleanIds.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_ENG_WORD + " FROM " + TABLE_WORDS +
                        " WHERE " + COL_WORD_ID + " IN (" + placeholders + ")" +
                        " AND length(trim(" + COL_ENG_WORD + ")) = 5" +
                        " ORDER BY RANDOM() LIMIT 1",
                cleanIds.toArray(new String[0])
        );
        String word = null;
        if (cursor.moveToFirst()) {
            word = cursor.getString(0).toUpperCase(Locale.US);
        }
        cursor.close();
        return word;
    }

    public boolean isValidWordleGuess(String guess) {
        if (guess == null || guess.trim().length() != 5) return false;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_WORDS +
                        " WHERE lower(trim(" + COL_ENG_WORD + ")) = lower(trim(?))" +
                        " AND length(trim(" + COL_ENG_WORD + ")) = 5" +
                        " LIMIT 1",
                new String[]{guess}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }
    public void seedDatabase() {
        String[][] seedWords = {
                {"Apple", "Elma", "Meyveler"}, {"Book", "Kitap", "E\u011fitim"}, {"Computer", "Bilgisayar", "Teknoloji"},
                {"Water", "Su", "Do\u011fa"}, {"School", "Okul", "E\u011fitim"}, {"Pen", "Kalem", "E\u011fitim"},
                {"Door", "Kap\u0131", "Ev"}, {"Window", "Pencere", "Ev"}, {"Table", "Masa", "Ev"},
                {"Chair", "Sandalye", "Ev"}, {"Friend", "Arkada\u015f", "Sosyal"}, {"Family", "Aile", "Sosyal"},
                {"Heart", "Kalp", "V\u00fccut"}, {"Sun", "G\u00fcne\u015f", "Do\u011fa"}, {"Moon", "Ay", "Do\u011fa"},
                {"Star", "Y\u0131ld\u0131z", "Do\u011fa"}, {"Time", "Zaman", "Soyut"}, {"City", "\u015eehir", "Yer"},
                {"Country", "\u00dclke", "Yer"}, {"Money", "Para", "Ekonomi"}, {"Work", "\u0130\u015f", "\u0130\u015f D\u00fcnyas\u0131"},
                {"Sleep", "Uyku", "Sa\u011fl\u0131k"}, {"Happy", "Mutlu", "Duygular"}, {"Sad", "\u00dczg\u00fcn", "Duygular"},
                {"Beautiful", "G\u00fczel", "S\u0131fatlar"}, {"Big", "B\u00fcy\u00fck", "S\u0131fatlar"}, {"Small", "K\u00fc\u00e7\u00fck", "S\u0131fatlar"},
                {"New", "Yeni", "S\u0131fatlar"}, {"Old", "Eski", "S\u0131fatlar"}, {"Good", "\u0130yi", "S\u0131fatlar"},
                {"Bad", "K\u00f6t\u00fc", "S\u0131fatlar"}, {"Fast", "H\u0131zl\u0131", "S\u0131fatlar"}, {"Slow", "Yava\u015f", "S\u0131fatlar"},
                {"Hot", "S\u0131cak", "S\u0131fatlar"}, {"Cold", "So\u011fuk", "S\u0131fatlar"}, {"Easy", "Kolay", "S\u0131fatlar"},
                {"Hard", "Zor", "Sifatlar"}, {"Read", "Okumak", "Fiiller"}, {"Write", "Yazmak", "Fiiller"},
                {"Listen", "Dinlemek", "Fiiller"}, {"Speak", "Konu\u015fmak", "Fiiller"}, {"Run", "Ko\u015fmak", "Fiiller"},
                {"Walk", "Y\u00fcr\u00fcmek", "Fiiller"}, {"Eat", "Yemek Yemek", "Fiiller"}, {"Drink", "\u0130\u00e7mek", "Fiiller"},
                {"Language", "Dil", "E\u011fitim"}, {"Bird", "Ku\u015f", "Hayvanlar"}, {"Dog", "K\u00f6pek", "Hayvanlar"},
                {"Cat", "Kedi", "Hayvanlar"}, {"Flower", "\u00c7i\u00e7ek", "Do\u011fa"},
                {"About", "Hakk\u0131nda", "Genel"}, {"Above", "\u00dcst\u00fcnde", "Yer"}, {"After", "Sonra", "Zaman"},
                {"Again", "Tekrar", "Zaman"}, {"Agent", "Temsilci", "\u0130\u015f D\u00fcnyas\u0131"}, {"Agree", "Kat\u0131lmak", "Fiiller"},
                {"Alarm", "Alarm", "Genel"}, {"Album", "Alb\u00fcm", "Sanat"}, {"Alive", "Canl\u0131", "S\u0131fatlar"},
                {"Allow", "\u0130zin vermek", "Fiiller"}, {"Alone", "Yaln\u0131z", "S\u0131fatlar"}, {"Along", "Boyunca", "Yer"},
                {"Angel", "Melek", "Genel"}, {"Angry", "K\u0131zg\u0131n", "Duygular"}, {"Arena", "Arena", "Yer"},
                {"Beach", "Plaj", "Yer"}, {"Begin", "Ba\u015flamak", "Fiiller"}, {"Black", "Siyah", "Renkler"},
                {"Brave", "Cesur", "S\u0131fatlar"}, {"Bread", "Ekmek", "Yiyecek"}, {"Bring", "Getirmek", "Fiiller"},
                {"Brown", "Kahverengi", "Renkler"}, {"Build", "\u0130n\u015fa etmek", "Fiiller"}, {"Candy", "\u015eeker", "Yiyecek"},
                {"Carry", "Ta\u015f\u0131mak", "Fiiller"}, {"Catch", "Yakalamak", "Fiiller"}, {"Cause", "Sebep", "Soyut"},
                {"Clean", "Temiz", "S\u0131fatlar"}, {"Clear", "A\u00e7\u0131k", "S\u0131fatlar"}, {"Cloud", "Bulut", "Do\u011fa"},
                {"Coast", "Sahil", "Yer"}, {"Cover", "\u00d6rtmek", "Fiiller"}, {"Cream", "Krema", "Yiyecek"},
                {"Dance", "Dans", "Sanat"}, {"Dream", "R\u00fcya", "Soyut"}, {"Drive", "S\u00fcrmek", "Fiiller"},
                {"Earth", "D\u00fcnya", "Do\u011fa"}, {"Empty", "Bo\u015f", "S\u0131fatlar"}, {"Enjoy", "Keyif almak", "Fiiller"},
                {"Enter", "Girmek", "Fiiller"}, {"Event", "Etkinlik", "Genel"}, {"Every", "Her", "Genel"},
                {"Field", "Alan", "Yer"}, {"Floor", "Zemin", "Ev"}, {"Focus", "Odak", "Soyut"},
                {"Force", "G\u00fc\u00e7", "Soyut"}, {"Fresh", "Taze", "S\u0131fatlar"}, {"Front", "\u00d6n", "Yer"},
                {"Fruit", "Meyve", "Yiyecek"}, {"Glass", "Cam", "Ev"}, {"Grace", "Zarafet", "Soyut"},
                {"Grain", "Tah\u0131l", "Yiyecek"}, {"Green", "Ye\u015fil", "Renkler"}, {"Group", "Grup", "Sosyal"},
                {"Guard", "Koruma", "\u0130\u015f D\u00fcnyas\u0131"}, {"Guess", "Tahmin", "Genel"}, {"Guide", "Rehber", "Sosyal"},
                {"House", "Ev", "Ev"}, {"Human", "\u0130nsan", "Sosyal"}, {"Ideal", "\u0130deal", "S\u0131fatlar"},
                {"Image", "G\u00f6rsel", "Teknoloji"}, {"Issue", "Konu", "Genel"}, {"Knife", "B\u0131\u00e7ak", "Ev"},
                {"Laugh", "G\u00fclmek", "Fiiller"}, {"Learn", "\u00d6\u011frenmek", "E\u011fitim"}, {"Light", "I\u015f\u0131k", "Do\u011fa"},
                {"Local", "Yerel", "S\u0131fatlar"}, {"Magic", "Sihir", "Soyut"}, {"March", "Mart", "Zaman"},
                {"Match", "E\u015fle\u015fme", "Genel"}, {"Maybe", "Belki", "Genel"}, {"Metal", "Metal", "Malzemeler"},
                {"Music", "M\u00fczik", "Sanat"}, {"Night", "Gece", "Zaman"}, {"Noise", "G\u00fcr\u00fclt\u00fc", "Genel"},
                {"North", "Kuzey", "Yer"}, {"Ocean", "Okyanus", "Do\u011fa"}, {"Offer", "Teklif", "\u0130\u015f D\u00fcnyas\u0131"},
                {"Order", "Sipari\u015f", "\u0130\u015f D\u00fcnyas\u0131"}, {"Paint", "Boya", "Sanat"}, {"Paper", "Ka\u011f\u0131t", "E\u011fitim"},
                {"Party", "Parti", "Sosyal"}, {"Peace", "Bar\u0131\u015f", "Soyut"}, {"Phone", "Telefon", "Teknoloji"},
                {"Place", "Yer", "Yer"}, {"Plant", "Bitki", "Do\u011fa"}, {"Plate", "Tabak", "Ev"},
                {"Point", "Nokta", "Genel"}, {"Power", "G\u00fc\u00e7", "Soyut"}, {"Price", "Fiyat", "Ekonomi"},
                {"Pride", "Gurur", "Duygular"}, {"Queen", "Krali\u00e7e", "Sosyal"}, {"Quick", "\u00c7abuk", "S\u0131fatlar"},
                {"Quiet", "Sessiz", "S\u0131fatlar"}, {"Radio", "Radyo", "Teknoloji"}, {"Reach", "Ula\u015fmak", "Fiiller"},
                {"Right", "Do\u011fru", "S\u0131fatlar"}, {"River", "Nehir", "Do\u011fa"}, {"Round", "Yuvarlak", "\u015eekiller"},
                {"Serve", "Hizmet etmek", "Fiiller"}, {"Shape", "\u015eekil", "\u015eekiller"}, {"Share", "Payla\u015fmak", "Fiiller"},
                {"Short", "K\u0131sa", "S\u0131fatlar"}, {"Skill", "Beceri", "E\u011fitim"}, {"Smile", "G\u00fcl\u00fcmseme", "Duygular"},
                {"Sound", "Ses", "Genel"}, {"South", "G\u00fcney", "Yer"}, {"Space", "Uzay", "Do\u011fa"},
                {"Sport", "Spor", "Sa\u011fl\u0131k"}, {"Story", "Hikaye", "E\u011fitim"}, {"Sugar", "\u015eeker", "Yiyecek"},
                {"Sweet", "Tatl\u0131", "S\u0131fatlar"}, {"Teach", "\u00d6\u011fretmek", "E\u011fitim"}, {"Theme", "Tema", "Genel"},
                {"Thing", "\u015eey", "Genel"}, {"Think", "D\u00fc\u015f\u00fcnmek", "Fiiller"}, {"Touch", "Dokunmak", "Fiiller"},
                {"Train", "Tren", "Ula\u015f\u0131m"}, {"Trust", "G\u00fcven", "Duygular"}, {"Voice", "Ses", "V\u00fccut"},
                {"Watch", "Saat", "Genel"}, {"White", "Beyaz", "Renkler"}, {"World", "D\u00fcnya", "Yer"},
                {"Youth", "Gen\u00e7lik", "Sosyal"}
        };

        SQLiteDatabase dbWrite = this.getWritableDatabase();
        ensureCurrentSchema(dbWrite);
        SeedWordCatalog.removeOldGeneratedWords(dbWrite);
        for (String[] w : seedWords) {
            insertSeedWordIfMissing(dbWrite, w[0], w[1], w[2]);
            syncSeedWordVisuals(dbWrite, w[0], w[1], w[2]);
            syncSeedWordSamples(dbWrite, w[0], w[1], w[2]);
        }
        for (String[] w : SeedWordCatalog.extraWords()) {
            insertSeedWordIfMissing(dbWrite, w[0], w[1], w[2]);
            syncSeedWordVisuals(dbWrite, w[0], w[1], w[2]);
            syncSeedWordSamples(dbWrite, w[0], w[1], w[2]);
        }
    }

    private void syncSeedWordVisuals(SQLiteDatabase dbWrite, String english, String turkish, String category) {
        ContentValues values = new ContentValues();
        values.put(COL_PICTURE, "word:" + english.toLowerCase(Locale.US));
        values.put(COL_TUR_WORD, turkish);
        values.put(COL_CATEGORY, category);
        dbWrite.update(
                TABLE_WORDS,
                values,
                "lower(trim(" + COL_ENG_WORD + ")) = lower(trim(?))",
                new String[]{english}
        );
    }

    private void syncSeedWordSamples(SQLiteDatabase dbWrite, String english, String turkish, String category) {
        Cursor cursor = dbWrite.rawQuery(
                "SELECT " + COL_WORD_ID + " FROM " + TABLE_WORDS +
                        " WHERE lower(trim(" + COL_ENG_WORD + ")) = lower(trim(?)) LIMIT 1",
                new String[]{english}
        );
        if (!cursor.moveToFirst()) {
            cursor.close();
            return;
        }

        int wordId = cursor.getInt(0);
        cursor.close();

        List<String> currentSamples = getSamplesForWord(wordId);
        if (!shouldRefreshSamples(currentSamples, english)) {
            return;
        }

        replaceSamplesForWord(wordId, samplesForWord(english, turkish, category));
    }

    private void insertSeedWordIfMissing(SQLiteDatabase dbWrite, String english, String turkish, String category) {
        Cursor cursor = dbWrite.rawQuery(
                "SELECT 1 FROM " + TABLE_WORDS + " WHERE lower(trim(" + COL_ENG_WORD + ")) = lower(trim(?)) LIMIT 1",
                new String[]{english}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        if (exists) return;

        ContentValues cv = new ContentValues();
        cv.put(COL_ENG_WORD, english);
        cv.put(COL_TUR_WORD, turkish);
        cv.put(COL_PICTURE, "word:" + english.toLowerCase(Locale.US));
        cv.put(COL_CATEGORY, category);
        cv.put(COL_STEP_COUNT, 0);
        cv.put(COL_NEXT_QUIZ_DATE, System.currentTimeMillis());
        cv.put(COL_TOTAL_ATTEMPTS, 0);
        cv.put(COL_CORRECT_ATTEMPTS, 0);
        long wordId = dbWrite.insert(TABLE_WORDS, null, cv);
        if (wordId == -1) return;

        for (String sample : samplesForWord(english, turkish, category)) {
            ContentValues sampleValues = new ContentValues();
            sampleValues.put(COL_WORD_ID, (int) wordId);
            sampleValues.put(COL_SAMPLE_TEXT, sample);
            sampleValues.put(COL_SAMPLE_USED, 0);
            dbWrite.insert(TABLE_SAMPLES, null, sampleValues);
        }
    }

    private void replaceSamplesForWord(int wordId, String[] samples) {
        SQLiteDatabase dbWrite = this.getWritableDatabase();
        dbWrite.delete(TABLE_SAMPLES, COL_WORD_ID + "=?", new String[]{String.valueOf(wordId)});

        for (String sample : samples) {
            ContentValues sampleValues = new ContentValues();
            sampleValues.put(COL_WORD_ID, wordId);
            sampleValues.put(COL_SAMPLE_TEXT, sample);
            sampleValues.put(COL_SAMPLE_USED, 0);
            dbWrite.insert(TABLE_SAMPLES, null, sampleValues);
        }
    }

    private boolean shouldRefreshSamples(List<String> samples, String word) {
        if (samples == null || samples.isEmpty() || samples.size() < 3) {
            return true;
        }

        Set<String> normalizedSamples = new HashSet<>();
        boolean allPlaceholders = true;
        for (String sample : samples) {
            if (sample == null || sample.trim().isEmpty()) {
                return true;
            }

            String normalized = sample.trim().toLowerCase(Locale.US);
            normalizedSamples.add(normalized);
            if (!isPlaceholderSample(normalized, word)) {
                allPlaceholders = false;
            }
        }

        return allPlaceholders || normalizedSamples.size() < samples.size();
    }

    private boolean isPlaceholderSample(String sample, String word) {
        String lowerWord = word == null ? "" : word.toLowerCase(Locale.US);
        return sample.equals(("i saw the word " + lowerWord + " in a useful sentence.").toLowerCase(Locale.US))
                || sample.equals(("the meaning of " + lowerWord + " is important to learn.").toLowerCase(Locale.US))
                || sample.equals(("she practiced " + lowerWord + " with a clear example.").toLowerCase(Locale.US));
    }

    private String[] samplesForWord(String word, String turkish, String category) {
        switch (word) {
            case "Apple": return new String[]{"She packed a red apple for lunch.", "The apple fell from the tree.", "I sliced the apple into small pieces."};
            case "Book": return new String[]{"He opened the book to chapter three.", "The book explains the history of the city.", "She borrowed a book from the library."};
            case "Computer": return new String[]{"My computer saves every file automatically.", "The computer screen turned on quickly.", "She uses a computer to edit photos."};
            case "Water": return new String[]{"Please drink water after running.", "The glass is full of cold water.", "Plants need water to grow."};
            case "School": return new String[]{"The children walked to school together.", "Our school has a large library.", "She studies math at school every morning."};
            case "Pen": return new String[]{"I signed the paper with a blue pen.", "The pen ran out of ink.", "He keeps a pen in his pocket."};
            case "Door": return new String[]{"Please close the door quietly.", "A wooden door leads to the garden.", "She knocked on the door twice."};
            case "Window": return new String[]{"Sunlight came through the window.", "He opened the window for fresh air.", "Rain tapped against the window."};
            case "Table": return new String[]{"Dinner is ready on the table.", "The table is made of wood.", "She placed the keys on the table."};
            case "Chair": return new String[]{"He pulled a chair closer to the desk.", "The chair is comfortable to sit on.", "Please put the chair back in its place."};
            case "Friend": return new String[]{"My friend helped me study.", "She called her friend after class.", "A good friend listens carefully."};
            case "Family": return new String[]{"My family eats dinner together.", "Her family lives in Ankara.", "The photo shows the whole family."};
            case "Heart": return new String[]{"The heart pumps blood through the body.", "He drew a heart on the card.", "Her heart beat faster during the race."};
            case "Sun": return new String[]{"The sun rises in the east.", "The sun warmed the beach.", "Do not look directly at the sun."};
            case "Moon": return new String[]{"The moon looked bright tonight.", "Clouds moved across the moon.", "The moon reflects light from the sun."};
            case "Star": return new String[]{"A bright star shone in the sky.", "The child pointed at a star.", "We watched the first star appear."};
            case "Time": return new String[]{"We need more time to finish.", "What time does the movie start?", "Time passes quickly during vacation."};
            case "City": return new String[]{"The city is crowded in the morning.", "They moved to a new city.", "The city has many museums."};
            case "Country": return new String[]{"Turkey is a beautiful country.", "Each country has its own flag.", "They traveled across the country by train."};
            case "Money": return new String[]{"She saved money for a new phone.", "Money cannot buy every kind of happiness.", "He counted the money in his wallet."};
            case "Work": return new String[]{"I have a lot of work today.", "She goes to work at nine.", "Hard work helped him improve."};
            case "Sleep": return new String[]{"Children need enough sleep.", "I could not sleep because of the noise.", "Good sleep helps you feel better."};
            case "Happy": return new String[]{"She felt happy after hearing the news.", "The happy child smiled all day.", "I am happy to see you."};
            case "Sad": return new String[]{"He felt sad when the game ended.", "The sad story made her quiet.", "She looked sad after saying goodbye."};
            case "Beautiful": return new String[]{"The garden looks beautiful in spring.", "She wore a beautiful dress.", "That song has a beautiful melody."};
            case "Big": return new String[]{"They live in a big house.", "A big vehicle stopped outside.", "The big box was hard to carry."};
            case "Small": return new String[]{"A small bird sat on the branch.", "She bought a small notebook.", "The room is small but bright."};
            case "New": return new String[]{"He bought a new jacket.", "The new student joined our class.", "This is my new phone."};
            case "Old": return new String[]{"The old bridge crosses the river.", "She found an old photo album.", "My grandfather owns an old radio."};
            case "Good": return new String[]{"That was a good answer.", "Fresh fruit is good for you.", "He is good at solving puzzles."};
            case "Bad": return new String[]{"The milk had a bad smell.", "It was a bad idea to wait outside.", "Bad weather delayed the flight."};
            case "Fast": return new String[]{"The fast train arrived early.", "She is a fast swimmer.", "A fast car passed us on the road."};
            case "Slow": return new String[]{"The slow bus stopped often.", "Please speak in a slow voice.", "The slow turtle crossed the path."};
            case "Hot": return new String[]{"The soup is too hot to eat.", "It was a hot summer day.", "The hot tea warmed my hands."};
            case "Cold": return new String[]{"The water in the bottle is cold.", "She wore a coat in the cold wind.", "The cold room needed a heater."};
            case "Easy": return new String[]{"The first question was easy.", "This recipe is easy to follow.", "It is easy to find the station."};
            case "Hard": return new String[]{"The exam was hard for everyone.", "This chair is hard and uncomfortable.", "He worked hard to learn English."};
            case "Read": return new String[]{"I read a story before bed.", "Please read the instructions carefully.", "She likes to read in the park."};
            case "Write": return new String[]{"Write your name at the top.", "He can write with both hands.", "She will write a letter tonight."};
            case "Listen": return new String[]{"Listen to the teacher carefully.", "I listen to music while walking.", "They listen for the doorbell."};
            case "Speak": return new String[]{"Please speak clearly during the call.", "He can speak English well.", "She wants to speak with the manager."};
            case "Run": return new String[]{"They run around the track.", "I run every morning.", "The children run to the playground."};
            case "Walk": return new String[]{"We walk to the market on Sundays.", "She likes to walk by the river.", "Please walk slowly on the wet floor."};
            case "Eat": return new String[]{"We eat breakfast at seven.", "The baby can eat soft food.", "They eat dinner with their family."};
            case "Drink": return new String[]{"Drink your milk before school.", "He likes to drink coffee in the morning.", "Athletes drink water during practice."};
            case "Language": return new String[]{"English is a useful language.", "She is learning a new language.", "Language helps people share ideas."};
            case "Bird": return new String[]{"A bird built a nest in the tree.", "The bird sang at sunrise.", "A small bird flew over the roof."};
            case "Dog": return new String[]{"The dog waited by the door.", "My dog likes to play outside.", "A friendly dog followed us home."};
            case "Cat": return new String[]{"The cat slept on the sofa.", "Her cat chased a toy mouse.", "A black cat crossed the street."};
            case "Flower": return new String[]{"The flower opened in the sunlight.", "She picked a yellow flower.", "A bee landed on the flower."};
            default: return buildGeneratedSamples(word, turkish, category);
        }
    }

    private String[] buildGeneratedSamples(String word, String turkish, String category) {
        String cleanWord = word == null ? "word" : word.trim();
        String lowerWord = cleanWord.toLowerCase(Locale.US);
        String cleanCategory = category == null ? "" : category.trim();

        String[] templates;
        switch (cleanCategory) {
            case "Fiiller":
                templates = new String[]{
                        "I try to %s for a few minutes every day.",
                        "She plans to %s before dinner tonight.",
                        "They learned how to %s during class.",
                        "We need to %s carefully to finish on time.",
                        "He forgot to %s until the teacher reminded him.",
                        "The coach asked us to %s together after school."
                };
                return pickTemplates(templates, lowerWord);
            case "Sifatlar":
            case "Sıfatlar":
                templates = new String[]{
                        "The room felt %s after the windows were opened.",
                        "She chose a %s style for her project.",
                        "His voice sounded calm but %s.",
                        "We noticed a %s detail in the design.",
                        "That jacket looks simple and %s.",
                        "The teacher gave a %s example to explain the idea."
                };
                return pickTemplates(templates, lowerWord);
            case "Renkler":
                templates = new String[]{
                        "She painted the wall %s before the guests arrived.",
                        "He wore a %s shirt to the concert.",
                        "The artist added a %s line across the canvas.",
                        "We chose a %s cover for the notebook.",
                        "The car looked bright in its %s color.",
                        "A %s light flashed near the door."
                };
                return pickTemplates(templates, lowerWord);
            case "Hayvanlar":
                templates = new String[]{
                        "The %s moved quietly across the garden.",
                        "We saw a %s near the lake this morning.",
                        "The child pointed at the %s with excitement.",
                        "A small %s rested in the shade.",
                        "The %s made a sound from behind the fence.",
                        "Everyone watched the %s for a few seconds."
                };
                return pickTemplates(templates, lowerWord);
            case "Yiyecek":
                templates = new String[]{
                        "She bought fresh %s from the market.",
                        "We served %s with tea in the afternoon.",
                        "The plate was filled with warm %s.",
                        "He tasted the %s before adding more salt.",
                        "They packed %s for the picnic.",
                        "The smell of %s came from the kitchen."
                };
                return pickTemplates(templates, lowerWord);
            case "Doğa":
                templates = new String[]{
                        "We watched the %s during our trip.",
                        "The %s changed the view outside the window.",
                        "Children learned about the %s in science class.",
                        "The guide spoke about the %s for several minutes.",
                        "A photo of the %s hung on the wall.",
                        "The quiet sound of the %s made the place peaceful."
                };
                return pickTemplates(templates, lowerWord);
            case "Ev":
                templates = new String[]{
                        "The %s in the house needed cleaning.",
                        "She left the %s near the living room wall.",
                        "We moved the %s to make more space.",
                        "The %s matched the rest of the room.",
                        "He fixed the %s before the visitors arrived.",
                        "The new %s changed the look of the house."
                };
                return pickTemplates(templates, lowerWord);
            case "Eğitim":
                templates = new String[]{
                        "The teacher wrote %s on the board.",
                        "She used %s in her homework sentence.",
                        "We reviewed %s again before the quiz.",
                        "The student remembered %s from yesterday's lesson.",
                        "A note about %s stayed in his notebook.",
                        "They practiced %s during English class."
                };
                return pickTemplates(templates, lowerWord);
            case "İş Dünyası":
                templates = new String[]{
                        "The manager discussed the %s during the meeting.",
                        "She prepared the %s before noon.",
                        "They reviewed the %s in the office.",
                        "A short report mentioned the %s twice.",
                        "The team improved the %s step by step.",
                        "He brought the %s to the manager's desk."
                };
                return pickTemplates(templates, lowerWord);
            case "Yer":
                templates = new String[]{
                        "They reached the %s before sunset.",
                        "We talked about the %s during the trip.",
                        "A map showed the %s clearly.",
                        "The bus stopped near the %s.",
                        "She took a photo of the %s in the morning.",
                        "Everyone asked how to find the %s."
                };
                return pickTemplates(templates, lowerWord);
            case "Duygular":
                templates = new String[]{
                        "The news brought a feeling of %s to the room.",
                        "You could see %s on his face.",
                        "Her voice was full of %s after the result.",
                        "The story ended with a sense of %s.",
                        "They spoke openly about %s during the conversation.",
                        "That moment created real %s between the friends."
                };
                return pickTemplates(templates, lowerWord);
            case "Teknoloji":
                templates = new String[]{
                        "The new %s worked faster than the old one.",
                        "She checked the %s before the presentation.",
                        "A problem with the %s delayed our work.",
                        "They tested the %s in the lab.",
                        "The %s stayed on the desk all day.",
                        "He learned how to use the %s last week."
                };
                return pickTemplates(templates, lowerWord);
            case "Genel":
            default:
                templates = new String[]{
                        "We heard the word %s in today's lesson.",
                        "She used %s in a short conversation.",
                        "The example with %s was easy to remember.",
                        "He wrote %s on a small card for practice.",
                        "Everyone understood %s after the teacher explained it.",
                        "A simple sentence with %s helped the class."
                };
                return pickTemplates(templates, lowerWord);
        }
    }

    private String[] pickTemplates(String[] templates, String value) {
        String[] samples = new String[3];
        int start = Math.abs(value.hashCode()) % templates.length;
        for (int i = 0; i < samples.length; i++) {
            String template = templates[(start + (i * 2)) % templates.length];
            samples[i] = String.format(Locale.US, template, value);
        }
        return samples;
    }

    public static class SampleSentence {
        public final int id;
        public final String text;

        public SampleSentence(int id, String text) {
            this.id = id;
            this.text = text;
        }
    }
}
