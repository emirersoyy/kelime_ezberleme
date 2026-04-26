package com.example.kelimeezberleme;

public class Word {
    int id;
    String eng, tur, pic;
    int stepCount;
    long nextQuizDate;

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
}