package com.example.kelimeezberleme;

import java.io.Serializable;

public class IncorrectWord implements Serializable {
    public String eng, tur, userAnswer;

    public IncorrectWord(String eng, String tur, String userAnswer) {
        this.eng = eng;
        this.tur = tur;
        this.userAnswer = userAnswer;
    }
}