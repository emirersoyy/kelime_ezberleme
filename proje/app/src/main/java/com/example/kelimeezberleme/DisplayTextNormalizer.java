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
        String normalizedOriginal = normalizeCategoryName(originalCategory);
        if ("Fiiller".equals(normalizedOriginal) || "Sıfatlar".equals(normalizedOriginal)) {
            return normalizedOriginal;
        }
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
            case "akademi":
                return "Akademi";
            case "bilim":
                return "Bilim";
            case "cevre":
                return "Çevre";
            case "dil":
                return "Dil";
            case "doga":
                return "Doğa";
            case "donanim":
                return "Donanım";
            case "egitim":
                return "Eğitim";
            case "hastane":
                return "Hastane";
            case "hayvanlar":
                return "Hayvanlar";
            case "iklim":
                return "İklim";
            case "iletisim":
                return "İletişim";
            case "is dunyasi":
                return "İş Dünyası";
            case "kariyer":
                return "Kariyer";
            case "lojistik":
                return "Lojistik";
            case "ofis":
                return "Ofis";
            case "okul":
                return "Okul";
            case "sekiller":
                return "Şekiller";
            case "ulasim":
                return "Ulaşım";
            case "sifatlar":
                return "Sıfatlar";
            case "saglik":
                return "Sağlık";
            case "toplum":
                return "Toplum";
            case "uretim":
                return "Üretim";
            case "uzay":
                return "Uzay";
            case "vucut":
                return "Vücut";
            case "yazilim":
                return "Yazılım";
            case "yonetim":
                return "Yönetim";
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
        map.put("akademi", "akademi");
        map.put("arkadas", "arkadaş");
        map.put("atolye", "atölye");
        map.put("basinc", "basınç");
        map.put("bilim", "bilim");
        map.put("buyuk", "büyük");
        map.put("cevre", "çevre");
        map.put("cicek", "çiçek");
        map.put("cocuk", "çocuk");
        map.put("dil", "dil");
        map.put("doga", "doğa");
        map.put("donanim", "donanım");
        map.put("dogru", "doğru");
        map.put("dunyasi", "dünyası");
        map.put("egitim", "eğitim");
        map.put("gunes", "güneş");
        map.put("guzel", "güzel");
        map.put("hastane", "hastane");
        map.put("hayvanlar", "hayvanlar");
        map.put("icmek", "içmek");
        map.put("iklim", "iklim");
        map.put("iletisim", "iletişim");
        map.put("is", "iş");
        map.put("kapi", "kapı");
        map.put("kariyer", "kariyer");
        map.put("kopek", "köpek");
        map.put("kucuk", "küçük");
        map.put("kus", "kuş");
        map.put("lojistik", "lojistik");
        map.put("mulkiyet", "mülkiyet");
        map.put("muzik", "müzik");
        map.put("ofis", "ofis");
        map.put("okul", "okul");
        map.put("saglik", "sağlık");
        map.put("sehir", "şehir");
        map.put("sicak", "sıcak");
        map.put("sifatlar", "sıfatlar");
        map.put("soguk", "soğuk");
        map.put("sozluk", "sözlük");
        map.put("toplum", "toplum");
        map.put("ucak", "uçak");
        map.put("ulke", "ülke");
        map.put("universite", "üniversite");
        map.put("uretim", "üretim");
        map.put("urun", "ürün");
        map.put("uzay", "uzay");
        map.put("uzgun", "üzgün");
        map.put("vucut", "vücut");
        map.put("yazici", "yazıcı");
        map.put("yazilim", "yazılım");
        map.put("yon", "yön");
        map.put("yonetim", "yönetim");
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

        map.put("academic", "Akademi");
        map.put("analysis", "Bilim");
        map.put("backpack", "Okul");
        map.put("biology", "Bilim");
        map.put("certificate", "Akademi");
        map.put("chemical", "Bilim");
        map.put("chemistry", "Bilim");
        map.put("classify", "Bilim");
        map.put("classroom", "Okul");
        map.put("dictionary", "Dil");
        map.put("discovery", "Bilim");
        map.put("education", "Akademi");
        map.put("example", "Okul");
        map.put("experiment", "Bilim");
        map.put("guidance", "Okul");
        map.put("history", "Akademi");
        map.put("information", "Akademi");
        map.put("instruction", "Okul");
        map.put("journal", "Akademi");
        map.put("knowledge", "Akademi");
        map.put("laboratory", "Bilim");
        map.put("language", "Dil");
        map.put("lecture", "Okul");
        map.put("library", "Akademi");
        map.put("measurement", "Bilim");
        map.put("microscope", "Bilim");
        map.put("notebook", "Okul");
        map.put("orientation", "Akademi");
        map.put("publication", "Akademi");
        map.put("question", "Okul");
        map.put("reference", "Dil");
        map.put("research", "Bilim");
        map.put("science", "Bilim");
        map.put("teacher", "Okul");
        map.put("technique", "Bilim");
        map.put("translation", "Dil");
        map.put("university", "Akademi");
        map.put("vocabulary", "Dil");
        map.put("zoology", "Bilim");

        map.put("delicious", "Yiyecek");
        map.put("ingredient", "Yiyecek");
        map.put("grocery", "Yiyecek");
        map.put("restaurant", "Yiyecek");
        map.put("vegetable", "Yiyecek");
        map.put("bakery", "Yiyecek");
        map.put("apple", "Meyveler");
        map.put("banana", "Meyveler");
        map.put("cherry", "Meyveler");
        map.put("fruit", "Meyveler");
        map.put("grape", "Meyveler");
        map.put("orange", "Meyveler");
        map.put("peach", "Meyveler");
        map.put("pear", "Meyveler");
        map.put("strawberry", "Meyveler");
        map.put("watermelon", "Meyveler");

        map.put("appointment", "Hastane");
        map.put("emergency", "Hastane");
        map.put("healthy", "Sağlık");
        map.put("hospital", "Hastane");
        map.put("lifestyle", "Sağlık");
        map.put("medical", "Sağlık");
        map.put("medicine", "Hastane");
        map.put("movement", "Sağlık");
        map.put("operation", "Sağlık");
        map.put("pharmacy", "Hastane");
        map.put("treatment", "Sağlık");

        map.put("application", "Yazılım");
        map.put("automatic", "Teknoloji");
        map.put("battery", "Donanım");
        map.put("camera", "Donanım");
        map.put("connection", "Yazılım");
        map.put("database", "Yazılım");
        map.put("digital", "Teknoloji");
        map.put("electric", "Teknoloji");
        map.put("electricity", "Donanım");
        map.put("engine", "Donanım");
        map.put("equipment", "Donanım");
        map.put("extension", "Yazılım");
        map.put("function", "Yazılım");
        map.put("internet", "Yazılım");
        map.put("keyboard", "Donanım");
        map.put("machine", "Donanım");
        map.put("maintenance", "Donanım");
        map.put("mechanism", "Donanım");
        map.put("message", "İletişim");
        map.put("network", "Yazılım");
        map.put("password", "Yazılım");
        map.put("platform", "Yazılım");
        map.put("printer", "Donanım");
        map.put("software", "Yazılım");
        map.put("storage", "Donanım");
        map.put("system", "Yazılım");
        map.put("technical", "Teknoloji");
        map.put("technology", "Teknoloji");
        map.put("version", "Yazılım");

        map.put("agreement", "Yönetim");
        map.put("assistant", "Ofis");
        map.put("business", "Yönetim");
        map.put("campaign", "Ofis");
        map.put("candidate", "Kariyer");
        map.put("company", "Yönetim");
        map.put("conference", "Ofis");
        map.put("contract", "Yönetim");
        map.put("delivery", "Lojistik");
        map.put("department", "Ofis");
        map.put("document", "Ofis");
        map.put("employee", "Kariyer");
        map.put("factory", "Üretim");
        map.put("feedback", "Ofis");
        map.put("headquarters", "Ofis");
        map.put("industry", "Üretim");
        map.put("initiative", "Yönetim");
        map.put("interview", "Kariyer");
        map.put("leadership", "Yönetim");
        map.put("management", "Yönetim");
        map.put("manufacturer", "Üretim");
        map.put("negotiation", "Yönetim");
        map.put("official", "Ofis");
        map.put("opportunity", "Kariyer");
        map.put("organization", "Yönetim");
        map.put("partnership", "Yönetim");
        map.put("presentation", "Ofis");
        map.put("priority", "Yönetim");
        map.put("procedure", "Yönetim");
        map.put("production", "Üretim");
        map.put("profession", "Kariyer");
        map.put("professional", "Kariyer");
        map.put("project", "Yönetim");
        map.put("proposal", "Yönetim");
        map.put("recommendation", "Yönetim");
        map.put("reception", "Ofis");
        map.put("requirement", "Yönetim");
        map.put("service", "Ofis");
        map.put("shipment", "Lojistik");
        map.put("signature", "Ofis");
        map.put("specialist", "Kariyer");
        map.put("strategy", "Yönetim");
        map.put("supervisor", "Yönetim");
        map.put("warehouse", "Lojistik");
        map.put("workflow", "Lojistik");
        map.put("workplace", "Ofis");
        map.put("workshop", "Üretim");

        map.put("adventure", "Toplum");
        map.put("argument", "İletişim");
        map.put("behavior", "İletişim");
        map.put("character", "İletişim");
        map.put("community", "Toplum");
        map.put("conversation", "İletişim");
        map.put("cultural", "Toplum");
        map.put("culture", "Toplum");
        map.put("discussion", "İletişim");
        map.put("expression", "İletişim");
        map.put("generation", "Toplum");
        map.put("government", "Toplum");
        map.put("heritage", "Toplum");
        map.put("identity", "İletişim");
        map.put("influence", "Toplum");
        map.put("membership", "Toplum");
        map.put("neighbor", "Toplum");
        map.put("participant", "Toplum");
        map.put("population", "Toplum");
        map.put("president", "Toplum");
        map.put("recognition", "İletişim");
        map.put("relationship", "İletişim");
        map.put("reservation", "İletişim");
        map.put("society", "Toplum");
        map.put("tradition", "Toplum");
        map.put("volunteer", "Toplum");

        map.put("atmosphere", "Uzay");
        map.put("climate", "İklim");
        map.put("energy", "Bilim");
        map.put("environment", "Çevre");
        map.put("garden", "Çevre");
        map.put("landscape", "Çevre");
        map.put("pressure", "Bilim");
        map.put("temperature", "İklim");
        map.put("weather", "İklim");
        map.put("wildlife", "Hayvanlar");

        return map;
    }
}
