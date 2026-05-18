package com.example.kelimeezberleme;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class DisplayTextNormalizer {
    private static final Locale TURKISH = new Locale("tr", "TR");
    private static final Map<String, String> TOKEN_CORRECTIONS = buildTokenCorrections();
    private static final Map<String, String> CATEGORY_OVERRIDES = buildCategoryOverrides();

    private DisplayTextNormalizer() {
    }

    static void normalizeWordForDisplay(Word word) {
        if (word == null) {
            return;
        }
        word.category = normalizeCategoryName(resolveCategory(word.eng, word.category));
        if (word.tur != null && !word.tur.trim().isEmpty()) {
            word.tur = normalizeTurkishText(word.tur);
        }
    }

    private static String resolveCategory(String english, String originalCategory) {
        String key = english == null ? "" : english.trim().toLowerCase(Locale.US);
        if (CATEGORY_OVERRIDES.containsKey(key)) {
            return CATEGORY_OVERRIDES.get(key);
        }
        return originalCategory;
    }

    static String normalizeCategoryName(String value) {
        String collapsed = collapseSpaces(value);
        if (collapsed.isEmpty()) {
            return "Genel";
        }

        switch (toAsciiKey(collapsed)) {
            case "doga":
                return "Doğa";
            case "egitim":
                return "Eğitim";
            case "is dunyasi":
                return "İş Dünyası";
            case "sifatlar":
                return "Sıfatlar";
            case "saglik":
                return "Sağlık";
            case "vucut":
                return "Vücut";
            default:
                return toTitleCaseTurkish(applyTokenCorrections(collapsed));
        }
    }

    static String normalizeTurkishText(String value) {
        String collapsed = collapseSpaces(value);
        if (collapsed.isEmpty()) {
            return "-";
        }
        return toTitleCaseTurkish(applyTokenCorrections(collapsed));
    }

    private static String collapseSpaces(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private static String applyTokenCorrections(String value) {
        StringBuilder result = new StringBuilder();
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isLetter(ch)) {
                token.append(ch);
            } else {
                appendCorrectedToken(result, token);
                result.append(ch);
            }
        }
        appendCorrectedToken(result, token);
        return result.toString();
    }

    private static void appendCorrectedToken(StringBuilder result, StringBuilder token) {
        if (token.length() == 0) {
            return;
        }
        String lower = token.toString().toLowerCase(Locale.US);
        result.append(TOKEN_CORRECTIONS.containsKey(lower) ? TOKEN_CORRECTIONS.get(lower) : lower);
        token.setLength(0);
    }

    private static String toTitleCaseTurkish(String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean newWord = true;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isLetter(ch)) {
                if (newWord) {
                    result.append(String.valueOf(ch).toUpperCase(TURKISH));
                    newWord = false;
                } else {
                    result.append(ch);
                }
            } else {
                result.append(ch);
                newWord = Character.isWhitespace(ch) || ch == '-' || ch == '/' || ch == '\'';
            }
        }
        return result.toString();
    }

    private static String toAsciiKey(String value) {
        return collapseSpaces(value)
                .toLowerCase(TURKISH)
                .replace('ç', 'c')
                .replace('ğ', 'g')
                .replace('ı', 'i')
                .replace('ö', 'o')
                .replace('ş', 's')
                .replace('ü', 'u');
    }

    private static Map<String, String> buildTokenCorrections() {
        Map<String, String> map = new HashMap<>();
        map.put("arkadas", "arkadaş");
        map.put("atolye", "atölye");
        map.put("basinc", "basınç");
        map.put("buyuk", "büyük");
        map.put("cicek", "çiçek");
        map.put("cocuk", "çocuk");
        map.put("doga", "doğa");
        map.put("dogru", "doğru");
        map.put("dunyasi", "dünyası");
        map.put("egitim", "eğitim");
        map.put("gunes", "güneş");
        map.put("guzel", "güzel");
        map.put("icmek", "içmek");
        map.put("is", "iş");
        map.put("kapi", "kapı");
        map.put("kopek", "köpek");
        map.put("kucuk", "küçük");
        map.put("kus", "kuş");
        map.put("mulkiyet", "mülkiyet");
        map.put("muzik", "müzik");
        map.put("saglik", "sağlık");
        map.put("sehir", "şehir");
        map.put("sicak", "sıcak");
        map.put("sifatlar", "sıfatlar");
        map.put("soguk", "soğuk");
        map.put("sozluk", "sözlük");
        map.put("ucak", "uçak");
        map.put("ulke", "ülke");
        map.put("universite", "üniversite");
        map.put("urun", "ürün");
        map.put("uzgun", "üzgün");
        map.put("vucut", "vücut");
        map.put("yazici", "yazıcı");
        map.put("yon", "yön");
        map.put("yurumek", "yürümek");
        return map;
    }

    private static Map<String, String> buildCategoryOverrides() {
        Map<String, String> map = new HashMap<>();

        map.put("affordable", "Ekonomi");
        map.put("expensive", "Ekonomi");
        map.put("reasonable", "Ekonomi");
        map.put("valuable", "Ekonomi");
        map.put("cheap", "Ekonomi");
        map.put("budget", "Ekonomi");
        map.put("salary", "Ekonomi");
        map.put("income", "Ekonomi");
        map.put("payment", "Ekonomi");
        map.put("purchase", "Ekonomi");
        map.put("warranty", "Ekonomi");
        map.put("property", "Ekonomi");
        map.put("investment", "Ekonomi");
        map.put("insurance", "Ekonomi");
        map.put("currency", "Ekonomi");
        map.put("market", "Ekonomi");
        map.put("customer", "Ekonomi");
        map.put("account", "Ekonomi");
        map.put("benefit", "Ekonomi");
        map.put("financial", "Ekonomi");

        map.put("academic", "Eğitim");
        map.put("classify", "Eğitim");
        map.put("instruction", "Eğitim");
        map.put("journal", "Eğitim");
        map.put("lecture", "Eğitim");
        map.put("measurement", "Eğitim");
        map.put("orientation", "Eğitim");
        map.put("publication", "Eğitim");
        map.put("reference", "Eğitim");
        map.put("technique", "Eğitim");
        map.put("translation", "Eğitim");
        map.put("vocabulary", "Eğitim");
        map.put("zoology", "Eğitim");

        map.put("delicious", "Yiyecek");
        map.put("ingredient", "Yiyecek");
        map.put("grocery", "Yiyecek");
        map.put("restaurant", "Yiyecek");
        map.put("vegetable", "Yiyecek");
        map.put("bakery", "Yiyecek");

        map.put("healthy", "Sağlık");
        map.put("lifestyle", "Sağlık");
        map.put("medical", "Sağlık");
        map.put("movement", "Sağlık");
        map.put("operation", "Sağlık");
        map.put("pharmacy", "Sağlık");
        map.put("treatment", "Sağlık");

        map.put("automatic", "Teknoloji");
        map.put("digital", "Teknoloji");
        map.put("electric", "Teknoloji");
        map.put("keyboard", "Teknoloji");
        map.put("printer", "Teknoloji");
        map.put("software", "Teknoloji");
        map.put("technical", "Teknoloji");
        map.put("technology", "Teknoloji");

        map.put("professional", "İş Dünyası");
        map.put("official", "İş Dünyası");
        map.put("presentation", "İş Dünyası");
        map.put("procedure", "İş Dünyası");
        map.put("proposal", "İş Dünyası");
        map.put("workflow", "İş Dünyası");
        map.put("workplace", "İş Dünyası");

        map.put("cultural", "Sosyal");

        return map;
    }
}
