package com.example.kelimeezberleme;

public final class AccountSecurity {
    private AccountSecurity() {
    }

    public static String validateUsername(String username) {
        String cleanUsername = username == null ? "" : username.trim();
        if (cleanUsername.length() < 3 || cleanUsername.length() > 20) {
            return "Kullanici adi 3-20 karakter olmalidir";
        }
        if (!cleanUsername.matches("^[A-Za-z][A-Za-z0-9._]*$")) {
            return "Kullanici adi harfle baslamali; harf, rakam, nokta veya alt cizgi icermelidir";
        }
        if (cleanUsername.contains("..") || cleanUsername.contains("__")) {
            return "Kullanici adinda tekrarlayan nokta veya alt cizgi kullanmayin";
        }
        return null;
    }

    public static String validatePassword(String username, String password) {
        String cleanUsername = username == null ? "" : username.trim().toLowerCase();
        String pass = password == null ? "" : password;

        if (pass.length() < 8 || pass.length() > 64) {
            return "Sifre 8-64 karakter olmalidir";
        }
        if (pass.matches(".*\\s.*")) {
            return "Sifre bosluk icermemelidir";
        }
        if (!cleanUsername.isEmpty() && pass.toLowerCase().contains(cleanUsername)) {
            return "Sifre kullanici adini icermemelidir";
        }
        if (!pass.matches(".*[a-z].*")) {
            return "Sifre en az bir kucuk harf icermelidir";
        }
        if (!pass.matches(".*[A-Z].*")) {
            return "Sifre en az bir buyuk harf icermelidir";
        }
        if (!pass.matches(".*\\d.*")) {
            return "Sifre en az bir rakam icermelidir";
        }
        if (!pass.matches(".*[^A-Za-z0-9].*")) {
            return "Sifre en az bir ozel karakter icermelidir";
        }
        return null;
    }
}
