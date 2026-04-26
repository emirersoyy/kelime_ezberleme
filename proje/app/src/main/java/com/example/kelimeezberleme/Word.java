package com.example.kelimeezberleme;

public class Word {
    int id;
    String eng, tur, pic, category;
    int stepCount;
    long nextQuizDate;
    int totalAttempts, correctAttempts;

    public Word(int id, String eng, String tur, String pic) {
        this.id = id;
        this.eng = eng;
        this.tur = tur;
        this.pic = pic;
    }

    public Word(int id, String eng, String tur, String pic, int stepCount, long nextQuizDate) {
        this.id = id;
        this.eng = eng;
        this.tur = tur;
        this.pic = pic;
        this.stepCount = stepCount;
        this.nextQuizDate = nextQuizDate;
    }

    public Word(int id, String eng, String tur, String pic, int stepCount, long nextQuizDate, String category, int totalAttempts, int correctAttempts) {
        this.id = id;
        this.eng = eng;
        this.tur = tur;
        this.pic = pic;
        this.stepCount = stepCount;
        this.nextQuizDate = nextQuizDate;
        this.category = category;
        this.totalAttempts = totalAttempts;
        this.correctAttempts = correctAttempts;
    }
}