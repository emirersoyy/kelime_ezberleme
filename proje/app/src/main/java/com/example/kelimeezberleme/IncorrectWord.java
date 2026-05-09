package com.example.kelimeezberleme;

import java.io.Serializable;

public class IncorrectWord implements Serializable {
    public String eng, tur, userAnswer, sentence;

    public IncorrectWord(String eng, String tur, String userAnswer) {
        this(eng, tur, userAnswer, "");
    }

    public IncorrectWord(String eng, String tur, String userAnswer, String sentence) {
        this.eng = eng;
        this.tur = tur;
        this.userAnswer = userAnswer;
        this.sentence = sentence;
    }
}
