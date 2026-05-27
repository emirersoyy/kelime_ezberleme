package com.example.kelimeezberleme;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class DisplayTextNormalizer {
    private static final Locale TURKISH = new Locale("tr", "TR");
    private static final String GENERAL_CATEGORY = "Genel";
    private static final String CATEGORY_AKADEMI = "Akademi";
    private static final String CATEGORY_BILIM = "Bilim";
    private static final String CATEGORY_CEVRE = "Çevre";
    private static final String CATEGORY_DONANIM = "Donanım";
    private static final String CATEGORY_HASTANE = "Hastane";
    private static final String CATEGORY_HAYVANLAR = "Hayvanlar";
    private static final String CATEGORY_FIILLER = "Fiiller";
    private static final String CATEGORY_SIFATLAR = "Sıfatlar";
    private static final String CATEGORY_IKLIM = "İklim";
    private static final String CATEGORY_ILETISIM = "İletişim";
    private static final String CATEGORY_KARIYER = "Kariyer";
    private static final String CATEGORY_LOJISTIK = "Lojistik";
    private static final String CATEGORY_SAGLIK = "Sağlık";
    private static final String CATEGORY_TEKNOLOJI = "Teknoloji";
    private static final String CATEGORY_TOPLUM = "Toplum";
    private static final String CATEGORY_URETIM = "Üretim";
    private static final String CATEGORY_YAZILIM = "Yazılım";
    private static final String CATEGORY_YONETIM = "Yönetim";
    private static final String CATEGORY_EKONOMI = "Ekonomi";
    private static final String CATEGORY_YIYECEK = "Yiyecek";
    private static final String CATEGORY_MEYVELER = "Meyveler";
    private static final String CATEGORY_RENKLER = "Renkler";
    private static final String CATEGORY_SANAT = "Sanat";
    private static final String CATEGORY_AKADEMI_KEY = "akademi";
    private static final String CATEGORY_BILIM_KEY = "bilim";
    private static final String CATEGORY_HASTANE_KEY = "hastane";
    private static final String CATEGORY_HAYVANLAR_KEY = "hayvanlar";
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
        if (CATEGORY_FIILLER.equals(normalizedOriginal) || CATEGORY_SIFATLAR.equals(normalizedOriginal)) {
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
            return GENERAL_CATEGORY;
        }

        switch (toAsciiKey(collapsed)) {
            case CATEGORY_AKADEMI_KEY:
                return CATEGORY_AKADEMI;
            case CATEGORY_BILIM_KEY:
                return CATEGORY_BILIM;
            case "cevre":
                return CATEGORY_CEVRE;
            case "dil":
                return "Dil";
            case "doga":
                return "Doğa";
            case "donanim":
                return CATEGORY_DONANIM;
            case "egitim":
                return "Eğitim";
            case CATEGORY_HASTANE_KEY:
                return CATEGORY_HASTANE;
            case CATEGORY_HAYVANLAR_KEY:
                return CATEGORY_HAYVANLAR;
            case "iklim":
                return CATEGORY_IKLIM;
            case "iletisim":
                return CATEGORY_ILETISIM;
            case "is dunyasi":
                return "İş Dünyası";
            case "kariyer":
                return CATEGORY_KARIYER;
            case "lojistik":
                return CATEGORY_LOJISTIK;
            case "ofis":
                return "Ofis";
            case "okul":
                return "Okul";
            case "sekiller":
                return "Şekiller";
            case "ulasim":
                return "Ulaşım";
            case "sifatlar":
                return CATEGORY_SIFATLAR;
            case "saglik":
                return CATEGORY_SAGLIK;
            case "toplum":
                return CATEGORY_TOPLUM;
            case "uretim":
                return CATEGORY_URETIM;
            case "uzay":
                return "Uzay";
            case "vucut":
                return "Vücut";
            case "yazilim":
                return CATEGORY_YAZILIM;
            case "yonetim":
                return CATEGORY_YONETIM;
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
        map.put(CATEGORY_AKADEMI_KEY, CATEGORY_AKADEMI_KEY);
        map.put("arkadas", "arkadaş");
        map.put("atolye", "atölye");
        map.put("basinc", "basınç");
        map.put(CATEGORY_BILIM_KEY, CATEGORY_BILIM_KEY);
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
        map.put(CATEGORY_HASTANE_KEY, CATEGORY_HASTANE_KEY);
        map.put(CATEGORY_HAYVANLAR_KEY, CATEGORY_HAYVANLAR_KEY);
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

        map.put("academic", CATEGORY_AKADEMI);
        map.put("analysis", CATEGORY_BILIM);
        map.put("backpack", "Okul");
        map.put("biology", CATEGORY_BILIM);
        map.put("certificate", CATEGORY_AKADEMI);
        map.put("chemical", CATEGORY_BILIM);
        map.put("chemistry", CATEGORY_BILIM);
        map.put("classify", CATEGORY_BILIM);
        map.put("classroom", "Okul");
        map.put("dictionary", "Dil");
        map.put("discovery", CATEGORY_BILIM);
        map.put("education", CATEGORY_AKADEMI);
        map.put("example", "Okul");
        map.put("experiment", CATEGORY_BILIM);
        map.put("guidance", "Okul");
        map.put("history", CATEGORY_AKADEMI);
        map.put("information", CATEGORY_AKADEMI);
        map.put("instruction", "Okul");
        map.put("journal", CATEGORY_AKADEMI);
        map.put("knowledge", CATEGORY_AKADEMI);
        map.put("laboratory", CATEGORY_BILIM);
        map.put("language", "Dil");
        map.put("lecture", "Okul");
        map.put("library", CATEGORY_AKADEMI);
        map.put("measurement", CATEGORY_BILIM);
        map.put("microscope", CATEGORY_BILIM);
        map.put("notebook", "Okul");
        map.put("orientation", CATEGORY_AKADEMI);
        map.put("publication", CATEGORY_AKADEMI);
        map.put("question", "Okul");
        map.put("reference", "Dil");
        map.put("research", CATEGORY_BILIM);
        map.put("science", CATEGORY_BILIM);
        map.put("teacher", "Okul");
        map.put("technique", CATEGORY_BILIM);
        map.put("translation", "Dil");
        map.put("university", CATEGORY_AKADEMI);
        map.put("vocabulary", "Dil");
        map.put("zoology", CATEGORY_BILIM);

        map.put("delicious", CATEGORY_YIYECEK);
        map.put("ingredient", CATEGORY_YIYECEK);
        map.put("grocery", CATEGORY_YIYECEK);
        map.put("restaurant", CATEGORY_YIYECEK);
        map.put("vegetable", CATEGORY_YIYECEK);
        map.put("bakery", CATEGORY_YIYECEK);
        map.put("apple", CATEGORY_MEYVELER);
        map.put("banana", CATEGORY_MEYVELER);
        map.put("cherry", CATEGORY_MEYVELER);
        map.put("fruit", CATEGORY_MEYVELER);
        map.put("grape", CATEGORY_MEYVELER);
        map.put("orange", CATEGORY_MEYVELER);
        map.put("peach", CATEGORY_MEYVELER);
        map.put("pear", CATEGORY_MEYVELER);
        map.put("strawberry", CATEGORY_MEYVELER);
        map.put("watermelon", CATEGORY_MEYVELER);

        map.put("appointment", CATEGORY_HASTANE);
        map.put("emergency", CATEGORY_HASTANE);
        map.put("healthy", CATEGORY_SAGLIK);
        map.put("hospital", CATEGORY_HASTANE);
        map.put("lifestyle", CATEGORY_SAGLIK);
        map.put("medical", CATEGORY_SAGLIK);
        map.put("medicine", CATEGORY_HASTANE);
        map.put("movement", CATEGORY_SAGLIK);
        map.put("operation", CATEGORY_SAGLIK);
        map.put("pharmacy", CATEGORY_HASTANE);
        map.put("treatment", CATEGORY_SAGLIK);

        map.put("application", CATEGORY_YAZILIM);
        map.put("automatic", CATEGORY_TEKNOLOJI);
        map.put("battery", CATEGORY_DONANIM);
        map.put("camera", CATEGORY_DONANIM);
        map.put("connection", CATEGORY_YAZILIM);
        map.put("database", CATEGORY_YAZILIM);
        map.put("digital", CATEGORY_TEKNOLOJI);
        map.put("electric", CATEGORY_TEKNOLOJI);
        map.put("electricity", CATEGORY_DONANIM);
        map.put("engine", CATEGORY_DONANIM);
        map.put("equipment", CATEGORY_DONANIM);
        map.put("extension", CATEGORY_YAZILIM);
        map.put("function", CATEGORY_YAZILIM);
        map.put("internet", CATEGORY_YAZILIM);
        map.put("keyboard", CATEGORY_DONANIM);
        map.put("machine", CATEGORY_DONANIM);
        map.put("maintenance", CATEGORY_DONANIM);
        map.put("mechanism", CATEGORY_DONANIM);
        map.put("message", CATEGORY_ILETISIM);
        map.put("network", CATEGORY_YAZILIM);
        map.put("password", CATEGORY_YAZILIM);
        map.put("platform", CATEGORY_YAZILIM);
        map.put("printer", CATEGORY_DONANIM);
        map.put("software", CATEGORY_YAZILIM);
        map.put("storage", CATEGORY_DONANIM);
        map.put("system", CATEGORY_YAZILIM);
        map.put("technical", CATEGORY_TEKNOLOJI);
        map.put("technology", CATEGORY_TEKNOLOJI);
        map.put("version", CATEGORY_YAZILIM);

        map.put("agreement", CATEGORY_YONETIM);
        map.put("assistant", "Ofis");
        map.put("business", CATEGORY_YONETIM);
        map.put("campaign", "Ofis");
        map.put("candidate", CATEGORY_KARIYER);
        map.put("company", CATEGORY_YONETIM);
        map.put("conference", "Ofis");
        map.put("contract", CATEGORY_YONETIM);
        map.put("delivery", CATEGORY_LOJISTIK);
        map.put("department", "Ofis");
        map.put("document", "Ofis");
        map.put("employee", CATEGORY_KARIYER);
        map.put("factory", CATEGORY_URETIM);
        map.put("feedback", "Ofis");
        map.put("headquarters", "Ofis");
        map.put("industry", CATEGORY_URETIM);
        map.put("initiative", CATEGORY_YONETIM);
        map.put("interview", CATEGORY_KARIYER);
        map.put("leadership", CATEGORY_YONETIM);
        map.put("management", CATEGORY_YONETIM);
        map.put("manufacturer", CATEGORY_URETIM);
        map.put("negotiation", CATEGORY_YONETIM);
        map.put("official", "Ofis");
        map.put("opportunity", CATEGORY_KARIYER);
        map.put("organization", CATEGORY_YONETIM);
        map.put("partnership", CATEGORY_YONETIM);
        map.put("presentation", "Ofis");
        map.put("priority", CATEGORY_YONETIM);
        map.put("procedure", CATEGORY_YONETIM);
        map.put("production", CATEGORY_URETIM);
        map.put("profession", CATEGORY_KARIYER);
        map.put("professional", CATEGORY_KARIYER);
        map.put("project", CATEGORY_YONETIM);
        map.put("proposal", CATEGORY_YONETIM);
        map.put("recommendation", CATEGORY_YONETIM);
        map.put("reception", "Ofis");
        map.put("requirement", CATEGORY_YONETIM);
        map.put("service", "Ofis");
        map.put("shipment", CATEGORY_LOJISTIK);
        map.put("signature", "Ofis");
        map.put("specialist", CATEGORY_KARIYER);
        map.put("strategy", CATEGORY_YONETIM);
        map.put("supervisor", CATEGORY_YONETIM);
        map.put("warehouse", CATEGORY_LOJISTIK);
        map.put("workflow", CATEGORY_LOJISTIK);
        map.put("workplace", "Ofis");
        map.put("workshop", CATEGORY_URETIM);

        map.put("adventure", CATEGORY_TOPLUM);
        map.put("argument", CATEGORY_ILETISIM);
        map.put("behavior", CATEGORY_ILETISIM);
        map.put("character", CATEGORY_ILETISIM);
        map.put("community", CATEGORY_TOPLUM);
        map.put("conversation", CATEGORY_ILETISIM);
        map.put("cultural", CATEGORY_TOPLUM);
        map.put("culture", CATEGORY_TOPLUM);
        map.put("discussion", CATEGORY_ILETISIM);
        map.put("expression", CATEGORY_ILETISIM);
        map.put("generation", CATEGORY_TOPLUM);
        map.put("government", CATEGORY_TOPLUM);
        map.put("heritage", CATEGORY_TOPLUM);
        map.put("identity", CATEGORY_ILETISIM);
        map.put("influence", CATEGORY_TOPLUM);
        map.put("membership", CATEGORY_TOPLUM);
        map.put("neighbor", CATEGORY_TOPLUM);
        map.put("participant", CATEGORY_TOPLUM);
        map.put("population", CATEGORY_TOPLUM);
        map.put("president", CATEGORY_TOPLUM);
        map.put("recognition", CATEGORY_ILETISIM);
        map.put("relationship", CATEGORY_ILETISIM);
        map.put("reservation", CATEGORY_ILETISIM);
        map.put("society", CATEGORY_TOPLUM);
        map.put("tradition", CATEGORY_TOPLUM);
        map.put("volunteer", CATEGORY_TOPLUM);

        map.put("atmosphere", "Uzay");
        map.put("climate", CATEGORY_IKLIM);
        map.put("energy", CATEGORY_BILIM);
        map.put("environment", CATEGORY_CEVRE);
        map.put("garden", CATEGORY_CEVRE);
        map.put("landscape", CATEGORY_CEVRE);
        map.put("pressure", CATEGORY_BILIM);
        map.put("temperature", CATEGORY_IKLIM);
        map.put("weather", CATEGORY_IKLIM);
        map.put("wildlife", CATEGORY_HAYVANLAR);

        return map;
    }
}
