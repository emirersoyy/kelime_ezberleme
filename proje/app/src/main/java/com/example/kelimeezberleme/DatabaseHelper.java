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
    public static final int DATABASE_VERSION = 14;
    private static final String PASSWORD_HASH_PREFIX = "pbkdf2";
    private static final int PASSWORD_HASH_ITERATIONS = 120000;
    private static final int PASSWORD_SALT_BYTES = 16;
    private static final int PASSWORD_KEY_BITS = 256;
    private static final String SQL_CREATE_TABLE = "CREATE TABLE ";
    private static final String SQL_CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";
    private static final String SQL_PRIMARY_KEY_AUTOINCREMENT = " INTEGER PRIMARY KEY AUTOINCREMENT, ";
    private static final String SQL_TEXT_COLUMN = " TEXT, ";
    private static final String SQL_INTEGER_DEFAULT_0 = "INTEGER DEFAULT 0";
    private static final String SQL_WHERE = " WHERE ";
    private static final String SQL_WHERE_LOWER = " WHERE LOWER(";
    private static final String SQL_LOWER_OPEN = "LOWER(";
    private static final String SQL_LOWER_CLOSE_LIMIT_ONE = ") = LOWER(?) LIMIT 1";
    private static final String SQL_LOWER_CLOSE = ") = LOWER(?)";
    private static final String SQL_LOWER_NOT_EQUAL_LIMIT_ONE = ") != LOWER(?) LIMIT 1";
    private static final String SQL_SELECT = "SELECT ";
    private static final String SQL_FROM = " FROM ";
    private static final String SQL_SELECT_STAR_FROM = "SELECT * FROM ";
    private static final String SQL_LOWER_TRIM = "lower(trim(";
    private static final String SQL_LOWER_TRIM_EQUALS = ")) = lower(trim(?))";
    private static final String SQL_LOWER_TRIM_EQUALS_LIMIT_ONE = ")) = lower(trim(?)) LIMIT 1";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String DEFAULT_CATEGORY = "Genel";
    private static final String CATEGORY_EGITIM = "Eğitim";
    private static final String CATEGORY_EGITIM_ASCII = "Egitim";
    private static final String CATEGORY_TEKNOLOJI = "Teknoloji";
    private static final String CATEGORY_DOGA = "Doğa";
    private static final String CATEGORY_SOSYAL = "Sosyal";
    private static final String CATEGORY_SOYUT = "Soyut";
    private static final String CATEGORY_ZAMAN = "Zaman";
    private static final String CATEGORY_IS_DUNYASI = "İş Dünyası";
    private static final String CATEGORY_DUYGULAR = "Duygular";
    private static final String CATEGORY_SIFATLAR = "Sıfatlar";
    private static final String CATEGORY_SIFATLAR_ASCII = "Sifatlar";
    private static final String CATEGORY_FIILLER = "Fiiller";
    private static final String CATEGORY_HAYVANLAR = "Hayvanlar";
    private static final String CATEGORY_SANAT = "Sanat";
    private static final String CATEGORY_RENKLER = "Renkler";
    private static final String CATEGORY_YIYECEK = "Yiyecek";
    private static final String CATEGORY_MEYVELER = "Meyveler";

    public static final String TABLE_USERS = "Users";
    public static final String COL_USER_ID = "UserID";
    public static final String COL_USER_NAME = "UserName";
    public static final String COL_PASSWORD = "Password";
    public static final String COL_FULL_NAME = "FullName";
    public static final String COL_PROFILE_IMAGE = "ProfileImagePath";
    public static final String COL_CREATED_AT = "CreatedAt";

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
        db.execSQL(SQL_CREATE_TABLE + TABLE_USERS + " (" +
                COL_USER_ID + SQL_PRIMARY_KEY_AUTOINCREMENT +
                COL_USER_NAME + SQL_TEXT_COLUMN +
                COL_PASSWORD + SQL_TEXT_COLUMN +
                COL_FULL_NAME + " TEXT DEFAULT '', " +
                COL_PROFILE_IMAGE + " TEXT DEFAULT '', " +
                COL_CREATED_AT + " LONG DEFAULT 0)");
        db.execSQL(SQL_CREATE_TABLE + TABLE_WORDS + " (" +
                COL_WORD_ID + SQL_PRIMARY_KEY_AUTOINCREMENT +
                COL_ENG_WORD + SQL_TEXT_COLUMN +
                COL_TUR_WORD + SQL_TEXT_COLUMN +
                COL_PICTURE + SQL_TEXT_COLUMN +
                COL_STEP_COUNT + " " + SQL_INTEGER_DEFAULT_0 + ", " +
                COL_NEXT_QUIZ_DATE + " LONG DEFAULT 0, " +
                COL_CATEGORY + " TEXT DEFAULT '" + DEFAULT_CATEGORY + "', " +
                COL_TOTAL_ATTEMPTS + " " + SQL_INTEGER_DEFAULT_0 + ", " +
                COL_CORRECT_ATTEMPTS + " " + SQL_INTEGER_DEFAULT_0 + ")");
        db.execSQL(SQL_CREATE_TABLE + TABLE_SAMPLES + " (" +
                COL_SAMPLE_ID + SQL_PRIMARY_KEY_AUTOINCREMENT +
                COL_WORD_ID + " INTEGER, " +
                COL_SAMPLE_TEXT + SQL_TEXT_COLUMN +
                COL_SAMPLE_USED + " " + SQL_INTEGER_DEFAULT_0 + ", " +
                "FOREIGN KEY(" + COL_WORD_ID + ") REFERENCES " + TABLE_WORDS + "(" + COL_WORD_ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ensureCurrentSchema(db);
    }

    private void ensureCurrentSchema(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_IF_NOT_EXISTS + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NAME + " TEXT, " +
                COL_PASSWORD + " TEXT)");
        db.execSQL(SQL_CREATE_TABLE_IF_NOT_EXISTS + TABLE_WORDS + " (" +
                COL_WORD_ID + SQL_PRIMARY_KEY_AUTOINCREMENT +
                COL_ENG_WORD + SQL_TEXT_COLUMN +
                COL_TUR_WORD + SQL_TEXT_COLUMN +
                COL_PICTURE + " TEXT)");
        db.execSQL(SQL_CREATE_TABLE_IF_NOT_EXISTS + TABLE_SAMPLES + " (" +
                COL_SAMPLE_ID + SQL_PRIMARY_KEY_AUTOINCREMENT +
                COL_WORD_ID + " INTEGER, " +
                COL_SAMPLE_TEXT + " TEXT)");

        ensureColumn(db, TABLE_WORDS, COL_STEP_COUNT, SQL_INTEGER_DEFAULT_0);
        ensureColumn(db, TABLE_WORDS, COL_NEXT_QUIZ_DATE, "LONG DEFAULT 0");
        ensureColumn(db, TABLE_WORDS, COL_CATEGORY, "TEXT DEFAULT '" + DEFAULT_CATEGORY + "'");
        ensureColumn(db, TABLE_WORDS, COL_TOTAL_ATTEMPTS, SQL_INTEGER_DEFAULT_0);
        ensureColumn(db, TABLE_WORDS, COL_CORRECT_ATTEMPTS, SQL_INTEGER_DEFAULT_0);
        ensureColumn(db, TABLE_SAMPLES, COL_SAMPLE_USED, SQL_INTEGER_DEFAULT_0);
        ensureColumn(db, TABLE_USERS, COL_FULL_NAME, "TEXT DEFAULT ''");
        ensureColumn(db, TABLE_USERS, COL_PROFILE_IMAGE, "TEXT DEFAULT ''");
        ensureColumn(db, TABLE_USERS, COL_CREATED_AT, "LONG DEFAULT 0");
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
        contentValues.put(COL_CATEGORY, (category == null || category.isEmpty()) ? DEFAULT_CATEGORY : category);
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
        Cursor cursor = db.rawQuery(SQL_SELECT + COL_SAMPLE_TEXT + SQL_FROM + TABLE_SAMPLES +
                        SQL_WHERE + COL_WORD_ID + "=? ORDER BY " + COL_SAMPLE_ID,
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
        Cursor cursor = db.rawQuery(SQL_SELECT + COL_SAMPLE_ID + ", " + COL_SAMPLE_TEXT +
                        SQL_FROM + TABLE_SAMPLES +
                        SQL_WHERE + COL_WORD_ID + "=? AND " + COL_SAMPLE_USED + "=0" +
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

        // First bring in every overdue review item so missed study days do not skip scheduled repetitions.
        Cursor dueCursor = db.rawQuery(SQL_SELECT_STAR_FROM + TABLE_WORDS +
                        SQL_WHERE + COL_NEXT_QUIZ_DATE + " <= ? AND " + COL_STEP_COUNT + " < 6" +
                        " AND " + COL_TOTAL_ATTEMPTS + " > 0" +
                        " ORDER BY " + COL_NEXT_QUIZ_DATE + " ASC",
                new String[]{String.valueOf(currentTime)});
        appendWordsFromCursor(words, dueCursor);

        // Then add only unseen/new words according to the configured new-question limit.
        Cursor newCursor = db.rawQuery(SQL_SELECT_STAR_FROM + TABLE_WORDS +
                        SQL_WHERE + COL_STEP_COUNT + " = 0 AND " + COL_TOTAL_ATTEMPTS + " = 0" +
                        " ORDER BY RANDOM() LIMIT ?",
                new String[]{String.valueOf(Math.max(0, limit))});
        appendWordsFromCursor(words, newCursor);

        return words;
    }

    private void appendWordsFromCursor(List<Word> target, Cursor cursor) {
        if (cursor == null) {
            return;
        }
        try {
            if (!cursor.moveToFirst()) {
                return;
            }
            do {
                target.add(new Word(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                        cursor.getInt(4), cursor.getLong(5), cursor.getString(6), cursor.getInt(7), cursor.getInt(8)));
            } while (cursor.moveToNext());
        } finally {
            cursor.close();
        }
    }

    public List<String> getRandomWrongAnswers(int correctWordId) {
        List<String> wrongs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT + COL_TUR_WORD + SQL_FROM + TABLE_WORDS +
                        SQL_WHERE + COL_WORD_ID + " != ? ORDER BY RANDOM() LIMIT 3",
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

        Cursor cursor = db.rawQuery(SQL_SELECT + COL_TOTAL_ATTEMPTS + ", " + COL_CORRECT_ATTEMPTS +
                SQL_FROM + TABLE_WORDS + SQL_WHERE + COL_WORD_ID + "=?", new String[]{String.valueOf(wordId)});
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
        contentValues.put(COL_CREATED_AT, System.currentTimeMillis());
        long result = db.insert(TABLE_USERS, null, contentValues);
        return result != -1;
    }

    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT + "1" + SQL_FROM + TABLE_USERS + SQL_WHERE_LOWER + COL_USER_NAME + SQL_LOWER_CLOSE_LIMIT_ONE,
                new String[]{username == null ? "" : username.trim()});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean checkUser(String username, String password) {
        String cleanUsername = username == null ? "" : username.trim();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT + COL_PASSWORD + SQL_FROM + TABLE_USERS + SQL_WHERE_LOWER + COL_USER_NAME + SQL_LOWER_CLOSE_LIMIT_ONE,
                new String[]{cleanUsername});
        boolean exists = false;
        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(0);
            exists = verifyPassword(password, storedPassword);
            if (exists && !isHashedPassword(storedPassword)) {
                ContentValues values = new ContentValues();
                values.put(COL_PASSWORD, hashPassword(password));
                db.update(TABLE_USERS, values, SQL_LOWER_OPEN + COL_USER_NAME + SQL_LOWER_CLOSE, new String[]{cleanUsername});
            }
        }
        cursor.close();
        return exists;
    }

    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PASSWORD, hashPassword(newPassword));
        int result = db.update(TABLE_USERS, contentValues, SQL_LOWER_OPEN + COL_USER_NAME + SQL_LOWER_CLOSE, new String[]{username == null ? "" : username.trim()});
        return result > 0;
    }

    public UserProfile getUserProfile(String username) {
        String cleanUsername = username == null ? "" : username.trim();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT + COL_USER_NAME + ", " + COL_FULL_NAME + ", " + COL_PROFILE_IMAGE +
                        SQL_FROM + TABLE_USERS + SQL_WHERE_LOWER + COL_USER_NAME + SQL_LOWER_CLOSE_LIMIT_ONE,
                new String[]{cleanUsername});

        UserProfile profile = null;
        if (cursor.moveToFirst()) {
            profile = new UserProfile(cursor.getString(0), cursor.getString(1), cursor.getString(2));
        }
        cursor.close();
        return profile;
    }

    public long getUserCreatedAt(String username) {
        String cleanUsername = username == null ? "" : username.trim();
        if (cleanUsername.isEmpty()) {
            return 0L;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                SQL_SELECT + COL_CREATED_AT + SQL_FROM + TABLE_USERS +
                        SQL_WHERE_LOWER + COL_USER_NAME + SQL_LOWER_CLOSE_LIMIT_ONE,
                new String[]{cleanUsername}
        );

        long createdAt = 0L;
        if (cursor.moveToFirst()) {
            createdAt = cursor.getLong(0);
        }
        cursor.close();
        return createdAt;
    }

    public long ensureUserCreatedAt(String username) {
        String cleanUsername = username == null ? "" : username.trim();
        if (cleanUsername.isEmpty()) {
            return 0L;
        }

        long createdAt = getUserCreatedAt(cleanUsername);
        if (createdAt > 0L) {
            return createdAt;
        }

        long now = System.currentTimeMillis();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CREATED_AT, now);
        db.update(TABLE_USERS, values, SQL_LOWER_OPEN + COL_USER_NAME + SQL_LOWER_CLOSE, new String[]{cleanUsername});
        return now;
    }

    public boolean isUsernameTakenByOtherUser(String username, String currentUsername) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT + "1" + SQL_FROM + TABLE_USERS +
                        SQL_WHERE_LOWER + COL_USER_NAME + SQL_LOWER_CLOSE +
                        " AND " + SQL_LOWER_OPEN + COL_USER_NAME + SQL_LOWER_NOT_EQUAL_LIMIT_ONE,
                new String[]{
                        username == null ? "" : username.trim(),
                        currentUsername == null ? "" : currentUsername.trim()
                });
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean updateUserProfile(String currentUsername, String newUsername, String fullName, String newPassword, String profileImagePath) {
        String cleanCurrentUsername = currentUsername == null ? "" : currentUsername.trim();
        String cleanNewUsername = newUsername == null ? "" : newUsername.trim();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USER_NAME, cleanNewUsername);
        contentValues.put(COL_FULL_NAME, fullName == null ? "" : fullName.trim());
        contentValues.put(COL_PROFILE_IMAGE, profileImagePath == null ? "" : profileImagePath);
        if (newPassword != null && !newPassword.isEmpty()) {
            contentValues.put(COL_PASSWORD, hashPassword(newPassword));
        }
        int result = db.update(TABLE_USERS, contentValues, SQL_LOWER_OPEN + COL_USER_NAME + SQL_LOWER_CLOSE, new String[]{cleanCurrentUsername});
        return result > 0;
    }

    public static class UserProfile {
        public final String username;
        public final String fullName;
        public final String profileImagePath;

        public UserProfile(String username, String fullName, String profileImagePath) {
            this.username = username == null ? "" : username;
            this.fullName = fullName == null ? "" : fullName;
            this.profileImagePath = profileImagePath == null ? "" : profileImagePath;
        }
    }

    private String hashPassword(String password) {
        try {
            byte[] salt = new byte[PASSWORD_SALT_BYTES];
            SECURE_RANDOM.nextBytes(salt);
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
        Cursor cursor = db.rawQuery(SQL_SELECT_STAR_FROM + TABLE_WORDS, null);
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
        return WordleWordBank.pickRandomWord(getAllWords(), null);
    }

    public String getRandomWordForWordle(Set<String> eligibleWordIds) {
        return WordleWordBank.pickRandomWord(getAllWords(), eligibleWordIds);
    }

    public boolean isValidWordleGuess(String guess) {
        return WordleWordBank.containsGuess(getAllWords(), guess);
    }
    public void seedDatabase() {
        String[][] seedWords = {
                {"Apple", "Elma", CATEGORY_MEYVELER}, {"Book", "Kitap", CATEGORY_EGITIM}, {"Computer", "Bilgisayar", CATEGORY_TEKNOLOJI},
                {"Water", "Su", CATEGORY_DOGA}, {"School", "Okul", CATEGORY_EGITIM}, {"Pen", "Kalem", CATEGORY_EGITIM},
                {"Door", "Kap\u0131", "Ev"}, {"Window", "Pencere", "Ev"}, {"Table", "Masa", "Ev"},
                {"Chair", "Sandalye", "Ev"}, {"Friend", "Arkada\u015f", CATEGORY_SOSYAL}, {"Family", "Aile", CATEGORY_SOSYAL},
                {"Heart", "Kalp", "V\u00fccut"}, {"Sun", "G\u00fcne\u015f", CATEGORY_DOGA}, {"Moon", "Ay", CATEGORY_DOGA},
                {"Star", "Y\u0131ld\u0131z", CATEGORY_DOGA}, {"Time", CATEGORY_ZAMAN, CATEGORY_SOYUT}, {"City", "\u015eehir", "Yer"},
                {"Country", "\u00dclke", "Yer"}, {"Money", "Para", "Ekonomi"}, {"Work", "\u0130\u015f", CATEGORY_IS_DUNYASI},
                {"Sleep", "Uyku", "Sa\u011fl\u0131k"}, {"Happy", "Mutlu", CATEGORY_DUYGULAR}, {"Sad", "\u00dczg\u00fcn", CATEGORY_DUYGULAR},
                {"Beautiful", "G\u00fczel", CATEGORY_SIFATLAR}, {"Big", "B\u00fcy\u00fck", CATEGORY_SIFATLAR}, {"Small", "K\u00fc\u00e7\u00fck", CATEGORY_SIFATLAR},
                {"New", "Yeni", CATEGORY_SIFATLAR}, {"Old", "Eski", CATEGORY_SIFATLAR}, {"Good", "\u0130yi", CATEGORY_SIFATLAR},
                {"Bad", "K\u00f6t\u00fc", CATEGORY_SIFATLAR}, {"Fast", "H\u0131zl\u0131", CATEGORY_SIFATLAR}, {"Slow", "Yava\u015f", CATEGORY_SIFATLAR},
                {"Hot", "S\u0131cak", CATEGORY_SIFATLAR}, {"Cold", "So\u011fuk", CATEGORY_SIFATLAR}, {"Easy", "Kolay", CATEGORY_SIFATLAR},
                {"Hard", "Zor", CATEGORY_SIFATLAR_ASCII}, {"Read", "Okumak", CATEGORY_FIILLER}, {"Write", "Yazmak", CATEGORY_FIILLER},
                {"Listen", "Dinlemek", CATEGORY_FIILLER}, {"Speak", "Konu\u015fmak", CATEGORY_FIILLER}, {"Run", "Ko\u015fmak", CATEGORY_FIILLER},
                {"Walk", "Y\u00fcr\u00fcmek", CATEGORY_FIILLER}, {"Eat", "Yemek Yemek", CATEGORY_FIILLER}, {"Drink", "\u0130\u00e7mek", CATEGORY_FIILLER},
                {"Language", "Dil", CATEGORY_EGITIM}, {"Bird", "Ku\u015f", CATEGORY_HAYVANLAR}, {"Dog", "K\u00f6pek", CATEGORY_HAYVANLAR},
                {"Cat", "Kedi", CATEGORY_HAYVANLAR}, {"Flower", "\u00c7i\u00e7ek", CATEGORY_DOGA},
                {"About", "Hakk\u0131nda", DEFAULT_CATEGORY}, {"Above", "\u00dcst\u00fcnde", "Yer"}, {"After", "Sonra", CATEGORY_ZAMAN},
                {"Again", "Tekrar", CATEGORY_ZAMAN}, {"Agent", "Temsilci", CATEGORY_IS_DUNYASI}, {"Agree", "Kat\u0131lmak", CATEGORY_FIILLER},
                {"Alarm", "Alarm", DEFAULT_CATEGORY}, {"Album", "Alb\u00fcm", CATEGORY_SANAT}, {"Alive", "Canl\u0131", CATEGORY_SIFATLAR},
                {"Allow", "\u0130zin vermek", CATEGORY_FIILLER}, {"Alone", "Yaln\u0131z", CATEGORY_SIFATLAR}, {"Along", "Boyunca", "Yer"},
                {"Angel", "Melek", DEFAULT_CATEGORY}, {"Angry", "K\u0131zg\u0131n", CATEGORY_DUYGULAR}, {"Arena", "Arena", "Yer"},
                {"Beach", "Plaj", "Yer"}, {"Begin", "Ba\u015flamak", CATEGORY_FIILLER}, {"Black", "Siyah", CATEGORY_RENKLER},
                {"Brave", "Cesur", CATEGORY_SIFATLAR}, {"Bread", "Ekmek", CATEGORY_YIYECEK}, {"Bring", "Getirmek", CATEGORY_FIILLER},
                {"Brown", "Kahverengi", CATEGORY_RENKLER}, {"Build", "\u0130n\u015fa etmek", CATEGORY_FIILLER}, {"Candy", "\u015eeker", CATEGORY_YIYECEK},
                {"Carry", "Ta\u015f\u0131mak", CATEGORY_FIILLER}, {"Catch", "Yakalamak", CATEGORY_FIILLER}, {"Cause", "Sebep", CATEGORY_SOYUT},
                {"Clean", "Temiz", CATEGORY_SIFATLAR}, {"Clear", "A\u00e7\u0131k", CATEGORY_SIFATLAR}, {"Cloud", "Bulut", CATEGORY_DOGA},
                {"Coast", "Sahil", "Yer"}, {"Cover", "\u00d6rtmek", CATEGORY_FIILLER}, {"Cream", "Krema", CATEGORY_YIYECEK},
                {"Dance", "Dans", CATEGORY_SANAT}, {"Dream", "R\u00fcya", CATEGORY_SOYUT}, {"Drive", "S\u00fcrmek", CATEGORY_FIILLER},
                {"Earth", "D\u00fcnya", CATEGORY_DOGA}, {"Empty", "Bo\u015f", CATEGORY_SIFATLAR}, {"Enjoy", "Keyif almak", CATEGORY_FIILLER},
                {"Enter", "Girmek", CATEGORY_FIILLER}, {"Event", "Etkinlik", DEFAULT_CATEGORY}, {"Every", "Her", DEFAULT_CATEGORY},
                {"Field", "Alan", "Yer"}, {"Floor", "Zemin", "Ev"}, {"Focus", "Odak", CATEGORY_SOYUT},
                {"Force", "G\u00fc\u00e7", CATEGORY_SOYUT}, {"Fresh", "Taze", "S\u0131fatlar"}, {"Front", "\u00d6n", "Yer"},
                {"Fruit", "Meyve", CATEGORY_YIYECEK}, {"Glass", "Cam", "Ev"}, {"Grace", "Zarafet", CATEGORY_SOYUT},
                {"Grain", "Tah\u0131l", CATEGORY_YIYECEK}, {"Green", "Ye\u015fil", CATEGORY_RENKLER}, {"Group", "Grup", CATEGORY_SOSYAL},
                {"Guard", "Koruma", "\u0130\u015f D\u00fcnyas\u0131"}, {"Guess", "Tahmin", DEFAULT_CATEGORY}, {"Guide", "Rehber", CATEGORY_SOSYAL},
                {"House", "Ev", "Ev"}, {"Human", "\u0130nsan", CATEGORY_SOSYAL}, {"Ideal", "\u0130deal", "S\u0131fatlar"},
                {"Image", "G\u00f6rsel", CATEGORY_TEKNOLOJI}, {"Issue", "Konu", DEFAULT_CATEGORY}, {"Knife", "B\u0131\u00e7ak", "Ev"},
                {"Laugh", "G\u00fclmek", CATEGORY_FIILLER}, {"Learn", "\u00d6\u011frenmek", "E\u011fitim"}, {"Light", "I\u015f\u0131k", "Do\u011fa"},
                {"Local", "Yerel", "S\u0131fatlar"}, {"Magic", "Sihir", CATEGORY_SOYUT}, {"March", "Mart", CATEGORY_ZAMAN},
                {"Match", "E\u015fle\u015fme", DEFAULT_CATEGORY}, {"Maybe", "Belki", DEFAULT_CATEGORY}, {"Metal", "Metal", "Malzemeler"},
                {"Music", "M\u00fczik", CATEGORY_SANAT}, {"Night", "Gece", CATEGORY_ZAMAN}, {"Noise", "G\u00fcr\u00fclt\u00fc", DEFAULT_CATEGORY},
                {"North", "Kuzey", "Yer"}, {"Ocean", "Okyanus", "Do\u011fa"}, {"Offer", "Teklif", "\u0130\u015f D\u00fcnyas\u0131"},
                {"Order", "Sipari\u015f", "\u0130\u015f D\u00fcnyas\u0131"}, {"Paint", "Boya", CATEGORY_SANAT}, {"Paper", "Ka\u011f\u0131t", "E\u011fitim"},
                {"Party", "Parti", CATEGORY_SOSYAL}, {"Peace", "Bar\u0131\u015f", CATEGORY_SOYUT}, {"Phone", "Telefon", CATEGORY_TEKNOLOJI},
                {"Place", "Yer", "Yer"}, {"Plant", "Bitki", "Do\u011fa"}, {"Plate", "Tabak", "Ev"},
                {"Point", "Nokta", DEFAULT_CATEGORY}, {"Power", "G\u00fc\u00e7", CATEGORY_SOYUT}, {"Price", "Fiyat", "Ekonomi"},
                {"Pride", "Gurur", CATEGORY_DUYGULAR}, {"Queen", "Krali\u00e7e", CATEGORY_SOSYAL}, {"Quick", "\u00c7abuk", "S\u0131fatlar"},
                {"Quiet", "Sessiz", "S\u0131fatlar"}, {"Radio", "Radyo", CATEGORY_TEKNOLOJI}, {"Reach", "Ula\u015fmak", CATEGORY_FIILLER},
                {"Right", "Do\u011fru", "S\u0131fatlar"}, {"River", "Nehir", "Do\u011fa"}, {"Round", "Yuvarlak", "\u015eekiller"},
                {"Serve", "Hizmet etmek", CATEGORY_FIILLER}, {"Shape", "\u015eekil", "\u015eekiller"}, {"Share", "Payla\u015fmak", CATEGORY_FIILLER},
                {"Short", "K\u0131sa", "S\u0131fatlar"}, {"Skill", "Beceri", "E\u011fitim"}, {"Smile", "G\u00fcl\u00fcmseme", CATEGORY_DUYGULAR},
                {"Sound", "Ses", DEFAULT_CATEGORY}, {"South", "G\u00fcney", "Yer"}, {"Space", "Uzay", "Do\u011fa"},
                {"Sport", "Spor", "Sa\u011fl\u0131k"}, {"Story", "Hikaye", "E\u011fitim"}, {"Sugar", "\u015eeker", CATEGORY_YIYECEK},
                {"Sweet", "Tatl\u0131", "S\u0131fatlar"}, {"Teach", "\u00d6\u011fretmek", "E\u011fitim"}, {"Theme", "Tema", DEFAULT_CATEGORY},
                {"Thing", "\u015eey", DEFAULT_CATEGORY}, {"Think", "D\u00fc\u015f\u00fcnmek", CATEGORY_FIILLER}, {"Touch", "Dokunmak", CATEGORY_FIILLER},
                {"Train", "Tren", "Ula\u015f\u0131m"}, {"Trust", "G\u00fcven", CATEGORY_DUYGULAR}, {"Voice", "Ses", "V\u00fccut"},
                {"Watch", "Saat", DEFAULT_CATEGORY}, {"White", "Beyaz", CATEGORY_RENKLER}, {"World", "D\u00fcnya", "Yer"},
                {"Youth", "Gen\u00e7lik", CATEGORY_SOSYAL}
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
        values.put(COL_PICTURE, SeedWordCatalog.pictureRefForWord(english, category));
        values.put(COL_TUR_WORD, turkish);
        values.put(COL_CATEGORY, category);
        dbWrite.update(
                TABLE_WORDS,
                values,
                SQL_LOWER_TRIM + COL_ENG_WORD + SQL_LOWER_TRIM_EQUALS,
                new String[]{english}
        );
    }

    private void syncSeedWordSamples(SQLiteDatabase dbWrite, String english, String turkish, String category) {
        Cursor cursor = dbWrite.rawQuery(
                SQL_SELECT + COL_WORD_ID + SQL_FROM + TABLE_WORDS +
                        SQL_WHERE + SQL_LOWER_TRIM + COL_ENG_WORD + SQL_LOWER_TRIM_EQUALS_LIMIT_ONE,
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
                SQL_SELECT + "1" + SQL_FROM + TABLE_WORDS + SQL_WHERE + SQL_LOWER_TRIM + COL_ENG_WORD + SQL_LOWER_TRIM_EQUALS_LIMIT_ONE,
                new String[]{english}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        if (exists) return;

        ContentValues cv = new ContentValues();
        cv.put(COL_ENG_WORD, english);
        cv.put(COL_TUR_WORD, turkish);
        cv.put(COL_PICTURE, SeedWordCatalog.pictureRefForWord(english, category));
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
            case CATEGORY_FIILLER:
                templates = new String[]{
                        "I try to %s for a few minutes every day.",
                        "She plans to %s before dinner tonight.",
                        "They learned how to %s during class.",
                        "We need to %s carefully to finish on time.",
                        "He forgot to %s until the teacher reminded him.",
                        "The coach asked us to %s together after school."
                };
                return pickTemplates(templates, lowerWord);
            case CATEGORY_SIFATLAR_ASCII:
            case CATEGORY_SIFATLAR:
                templates = new String[]{
                        "The room felt %s after the windows were opened.",
                        "She chose a %s style for her project.",
                        "His voice sounded calm but %s.",
                        "We noticed a %s detail in the design.",
                        "That jacket looks simple and %s.",
                        "The teacher gave a %s example to explain the idea."
                };
                return pickTemplates(templates, lowerWord);
            case CATEGORY_RENKLER:
                templates = new String[]{
                        "She painted the wall %s before the guests arrived.",
                        "He wore a %s shirt to the concert.",
                        "The artist added a %s line across the canvas.",
                        "We chose a %s cover for the notebook.",
                        "The car looked bright in its %s color.",
                        "A %s light flashed near the door."
                };
                return pickTemplates(templates, lowerWord);
            case CATEGORY_HAYVANLAR:
                templates = new String[]{
                        "The %s moved quietly across the garden.",
                        "We saw a %s near the lake this morning.",
                        "The child pointed at the %s with excitement.",
                        "A small %s rested in the shade.",
                        "The %s made a sound from behind the fence.",
                        "Everyone watched the %s for a few seconds."
                };
                return pickTemplates(templates, lowerWord);
            case "Bilim":
                templates = new String[]{
                        "We studied %s during the science lesson.",
                        "The lab report mentioned %s in detail.",
                        "Our teacher used %s to explain the topic.",
                        "They observed %s during the experiment.",
                        "A short article described %s clearly.",
                        "The students asked several questions about %s."
                };
                return pickTemplates(templates, lowerWord);
            case "Çevre":
                templates = new String[]{
                        "The guide explained how %s affects daily life.",
                        "We noticed %s while walking through the park.",
                        "A poster about %s was hanging in the classroom.",
                        "The trip helped us appreciate %s more deeply.",
                        "Everyone talked about %s after the presentation.",
                        "The photo captured the beauty of %s."
                };
                return pickTemplates(templates, lowerWord);
            case "İklim":
                templates = new String[]{
                        "Today's report focused on %s in our region.",
                        "We compared %s across different seasons.",
                        "The teacher explained how %s can change quickly.",
                        "A chart showed the effect of %s over time.",
                        "The travelers checked %s before leaving home.",
                        "News about %s shaped our weekend plans."
                };
                return pickTemplates(templates, lowerWord);
            case "Uzay":
                templates = new String[]{
                        "The documentary described %s in simple language.",
                        "We talked about %s while watching the night sky.",
                        "The lesson connected %s to space science.",
                        "A model helped the students imagine %s better.",
                        "The class became curious after hearing about %s.",
                        "The presenter used a photo to explain %s."
                };
                return pickTemplates(templates, lowerWord);
            case CATEGORY_YIYECEK:
                templates = new String[]{
                        "She bought fresh %s from the market.",
                        "We served %s with tea in the afternoon.",
                        "The plate was filled with warm %s.",
                        "He tasted the %s before adding more salt.",
                        "They packed %s for the picnic.",
                        "The smell of %s came from the kitchen."
                };
                return pickTemplates(templates, lowerWord);
            case "Dil":
                templates = new String[]{
                        "We practiced %s during the language lesson.",
                        "The teacher asked us to use %s in a sentence.",
                        "A short exercise helped us remember %s.",
                        "She wrote %s neatly in her notebook.",
                        "The class reviewed %s before the quiz.",
                        "Learning %s made the topic easier to understand."
                };
                return pickTemplates(templates, lowerWord);
            case CATEGORY_DOGA:
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
            case CATEGORY_EGITIM:
            case "Okul":
                templates = new String[]{
                        "The teacher wrote %s on the board.",
                        "She used %s in her homework sentence.",
                        "We reviewed %s again before the quiz.",
                        "The student remembered %s from yesterday's lesson.",
                        "A note about %s stayed in his notebook.",
                        "They practiced %s during English class."
                };
                return pickTemplates(templates, lowerWord);
            case "Akademi":
                templates = new String[]{
                        "The university seminar focused on %s this week.",
                        "Her academic notes included a section on %s.",
                        "The article introduced %s in a formal way.",
                        "Students discussed %s during the seminar.",
                        "A professor recommended reading more about %s.",
                        "The department meeting briefly mentioned %s."
                };
                return pickTemplates(templates, lowerWord);
            case CATEGORY_IS_DUNYASI:
            case "Ofis":
                templates = new String[]{
                        "The manager discussed the %s during the meeting.",
                        "She prepared the %s before noon.",
                        "They reviewed the %s in the office.",
                        "A short report mentioned the %s twice.",
                        "The team improved the %s step by step.",
                        "He brought the %s to the manager's desk."
                };
                return pickTemplates(templates, lowerWord);
            case "Yönetim":
                templates = new String[]{
                        "The team reviewed %s before making a decision.",
                        "Good %s helped the project move faster.",
                        "The manager improved %s across the department.",
                        "A meeting was scheduled to discuss %s.",
                        "Their plan depended on clear %s from the start.",
                        "The report explained why %s mattered to the team."
                };
                return pickTemplates(templates, lowerWord);
            case "Lojistik":
                templates = new String[]{
                        "The company tracked %s throughout the day.",
                        "A delay in %s changed the schedule.",
                        "The warehouse team checked %s carefully.",
                        "They improved %s to save time.",
                        "Everyone monitored %s before the delivery.",
                        "The report highlighted a problem with %s."
                };
                return pickTemplates(templates, lowerWord);
            case "Kariyer":
                templates = new String[]{
                        "She thought %s would help her career grow.",
                        "The interview panel asked about %s directly.",
                        "They discussed %s during the hiring process.",
                        "Strong %s can open new job opportunities.",
                        "His mentor gave useful advice about %s.",
                        "The workshop focused on building %s."
                };
                return pickTemplates(templates, lowerWord);
            case "Üretim":
                templates = new String[]{
                        "The factory improved %s this month.",
                        "A delay in %s affected the final plan.",
                        "The team checked %s before starting work.",
                        "They measured %s to improve efficiency.",
                        "The supervisor reviewed %s on the production floor.",
                        "A report showed how %s changed over time."
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
            case CATEGORY_DUYGULAR:
                templates = new String[]{
                        "The news brought a feeling of %s to the room.",
                        "You could see %s on his face.",
                        "Her voice was full of %s after the result.",
                        "The story ended with a sense of %s.",
                        "They spoke openly about %s during the conversation.",
                        "That moment created real %s between the friends."
                };
                return pickTemplates(templates, lowerWord);
            case "İletişim":
                templates = new String[]{
                        "Clear %s made the conversation easier.",
                        "They worked on %s during the group activity.",
                        "A misunderstanding about %s slowed things down.",
                        "The speaker used %s very effectively.",
                        "Good %s helped everyone stay connected.",
                        "We discussed %s during the communication workshop."
                };
                return pickTemplates(templates, lowerWord);
            case "Toplum":
                templates = new String[]{
                        "The article explored %s in modern society.",
                        "People talked about %s during the event.",
                        "The lesson showed why %s matters to communities.",
                        "A documentary presented different views on %s.",
                        "Students shared examples of %s from daily life.",
                        "The discussion connected %s with social change."
                };
                return pickTemplates(templates, lowerWord);
            case CATEGORY_TEKNOLOJI:
            case "Yazılım":
                templates = new String[]{
                        "The new %s worked faster than the old one.",
                        "She checked the %s before the presentation.",
                        "A problem with the %s delayed our work.",
                        "They tested the %s in the lab.",
                        "The %s stayed on the desk all day.",
                        "He learned how to use the %s last week."
                };
                return pickTemplates(templates, lowerWord);
            case "Donanım":
                templates = new String[]{
                        "The technician tested the %s before installation.",
                        "A problem with the %s interrupted our work.",
                        "They replaced the %s with a newer model.",
                        "The manual explained how to maintain the %s.",
                        "We compared the size and weight of each %s.",
                        "The engineer checked the %s twice for safety."
                };
                return pickTemplates(templates, lowerWord);
            case "Hastane":
                templates = new String[]{
                        "The nurse prepared the %s before the appointment.",
                        "Doctors explained the role of %s to the patient.",
                        "The hospital staff checked the %s carefully.",
                        "They discussed %s during the medical visit.",
                        "A note about %s was added to the file.",
                        "The clinic used %s as part of the treatment plan."
                };
                return pickTemplates(templates, lowerWord);
            case DEFAULT_CATEGORY:
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
        int sampleCount = Math.abs(value.hashCode()) % 2 == 0 ? 1 : 2;
        String[] samples = new String[sampleCount];
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
