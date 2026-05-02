package com.example.kelimeezberleme;

public final class AccountSecurity {
    private AccountSecurity() {
    }

    public static String validateUsername(String username) {
        String cleanUsername = username == null ? "" : username.trim();
        if (cleanUsername.length() < 3 || cleanUsername.length() > 20) {
            return "Kullanıcı adı 3-20 karakter arasında olmalıdır";
        }
        if (!cleanUsername.matches("^[A-Za-z][A-Za-z0-9._]*$")) {
            return "Kullanıcı adı harf ile başlamalı; harf, rakam, nokta veya alt çizgi içermelidir";
        }
        if (cleanUsername.contains("..") || cleanUsername.contains("__")) {
            return "Kullanıcı adında ardışık nokta veya alt çizgi kullanılamaz";
        }
        return null;
    }

    public static String validatePassword(String username, String password) {
        String cleanUsername = username == null ? "" : username.trim().toLowerCase();
        String pass = password == null ? "" : password;

        if (pass.length() < 8 || pass.length() > 64) {
            return "Şifre 8-64 karakter arasında olmalıdır";
        }
        if (pass.matches(".*\\s.*")) {
            return "Şifre boşluk içermemelidir";
        }
        if (!cleanUsername.isEmpty() && pass.toLowerCase().contains(cleanUsername)) {
            return "Şifre kullanıcı adınızı içermemelidir";
        }
        if (!pass.matches(".*[a-z].*")) {
            return "Şifre en az bir küçük harf içermelidir";
        }
        if (!pass.matches(".*[A-Z].*")) {
            return "Şifre en az bir büyük harf içermelidir";
        }
        if (!pass.matches(".*\\d.*")) {
            return "Şifre en az bir rakam içermelidir";
        }
        if (!pass.matches(".*[^A-Za-z0-9].*")) {
            return "Şifre en az bir özel karakter içermelidir";
        }
        return null;
    }
}
