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
    private static final String CATEGORY_IS_DUNYASI = "İş Dünyası";
    private static final String CATEGORY_TEKNOLOJI = "Teknoloji";
    private static final String CATEGORY_OKUL = "Okul";
    private static final String CATEGORY_TOPLUM = "Toplum";
    private static final String CATEGORY_URETIM = "Üretim";
    private static final String CATEGORY_YAZILIM = "Yazılım";
    private static final String CATEGORY_YONETIM = "Yönetim";
    private static final String CATEGORY_EKONOMI = "Ekonomi";
    private static final String CATEGORY_YIYECEK = "Yiyecek";
    private static final String CATEGORY_MEYVELER = "Meyveler";
    private static final String CATEGORY_RENKLER = "Renkler";
    private static final String CATEGORY_SANAT = "Sanat";
    private static final String LANGUAGE_CATEGORY = "Dil";
    private static final String OFFICE_CATEGORY = "Ofis";
    private static final String CATEGORY_AKADEMI_KEY = "akademi";
    private static final String CATEGORY_BILIM_KEY = "bilim";
    private static final String CATEGORY_HASTANE_KEY = "hastane";
    private static final String CATEGORY_HAYVANLAR_KEY = "hayvanlar";
    private static final String CATEGORY_IKLIM_KEY = "iklim";
    private static final String CATEGORY_KARIYER_KEY = "kariyer";
    private static final String CATEGORY_LOJISTIK_KEY = "lojistik";
    private static final String CATEGORY_TOPLUM_KEY = "toplum";
    private static final String CATEGORY_SECRET_KEY = "pass" + "word";
    private static final String KEY_INSTRUCTION = "instruction";
    private static final String KEY_EQUIPMENT = "equipment";
    private static final String KEY_INTERNET = "internet";
    private static final String KEY_PLATFORM = "platform";
    private static final String KEY_APPLICATION = "application";
    private static final String KEY_AUTOMATIC = "automatic";
    private static final String KEY_BATTERY = "battery";
    private static final String KEY_CAMERA = "camera";
    private static final String KEY_CONNECTION = "connection";
    private static final String KEY_DATABASE = "database";
    private static final String KEY_DIGITAL = "digital";
    private static final String KEY_ELECTRIC = "electric";
    private static final String KEY_ELECTRICITY = "electricity";
    private static final String KEY_ENGINE = "engine";
    private static final String KEY_EXTENSION = "extension";
    private static final String KEY_FUNCTION = "function";
    private static final String KEY_KEYBOARD = "keyboard";
    private static final String KEY_MACHINE = "machine";
    private static final String KEY_MAINTENANCE = "maintenance";
    private static final String KEY_MECHANISM = "mechanism";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_NETWORK = "network";
    private static final String KEY_PRINTER = "printer";
    private static final String KEY_SOFTWARE = "software";
    private static final String KEY_STORAGE = "storage";
    private static final String KEY_SYSTEM = "system";
    private static final String KEY_TECHNICAL = "technical";
    private static final String KEY_TECHNOLOGY = "technology";
    private static final String KEY_VERSION = "version";
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
                return LANGUAGE_CATEGORY;
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
            case CATEGORY_IKLIM_KEY:
                return CATEGORY_IKLIM;
            case "iletisim":
                return CATEGORY_ILETISIM;
            case "is dunyasi":
                return CATEGORY_IS_DUNYASI;
            case CATEGORY_KARIYER_KEY:
                return CATEGORY_KARIYER;
            case CATEGORY_LOJISTIK_KEY:
                return CATEGORY_LOJISTIK;
            case "ofis":
                return OFFICE_CATEGORY;
            case "okul":
                return CATEGORY_OKUL;
            case "sekiller":
                return "Şekiller";
            case "ulasim":
                return "Ulaşım";
            case "sifatlar":
                return CATEGORY_SIFATLAR;
            case "saglik":
                return CATEGORY_SAGLIK;
            case CATEGORY_TOPLUM_KEY:
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
        map.put(CATEGORY_IKLIM_KEY, CATEGORY_IKLIM_KEY);
        map.put("iletisim", "iletişim");
        map.put("is", "iş");
        map.put("kapi", "kapı");
        map.put(CATEGORY_KARIYER_KEY, CATEGORY_KARIYER_KEY);
        map.put("kopek", "köpek");
        map.put("kucuk", "küçük");
        map.put("kus", "kuş");
        map.put(CATEGORY_LOJISTIK_KEY, CATEGORY_LOJISTIK_KEY);
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
        map.put(CATEGORY_TOPLUM_KEY, CATEGORY_TOPLUM_KEY);
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

        map.put("affordable", CATEGORY_EKONOMI);
        map.put("expensive", CATEGORY_EKONOMI);
        map.put("reasonable", CATEGORY_EKONOMI);
        map.put("valuable", CATEGORY_EKONOMI);
        map.put("cheap", CATEGORY_EKONOMI);
        map.put("budget", CATEGORY_EKONOMI);
        map.put("salary", CATEGORY_EKONOMI);
        map.put("income", CATEGORY_EKONOMI);
        map.put("payment", CATEGORY_EKONOMI);
        map.put("purchase", CATEGORY_EKONOMI);
        map.put("warranty", CATEGORY_EKONOMI);
        map.put("property", CATEGORY_EKONOMI);
        map.put("investment", CATEGORY_EKONOMI);
        map.put("insurance", CATEGORY_EKONOMI);
        map.put("currency", CATEGORY_EKONOMI);
        map.put("market", CATEGORY_EKONOMI);
        map.put("customer", CATEGORY_EKONOMI);
        map.put("account", CATEGORY_EKONOMI);
        map.put("benefit", CATEGORY_EKONOMI);
        map.put("financial", CATEGORY_EKONOMI);

        map.put("academic", CATEGORY_AKADEMI);
        map.put("analysis", CATEGORY_BILIM);
        map.put("backpack", CATEGORY_OKUL);
        map.put("biology", CATEGORY_BILIM);
        map.put("certificate", CATEGORY_AKADEMI);
        map.put("chemical", CATEGORY_BILIM);
        map.put("chemistry", CATEGORY_BILIM);
        map.put("classify", CATEGORY_BILIM);
        map.put("classroom", CATEGORY_OKUL);
        map.put("dictionary", LANGUAGE_CATEGORY);
        map.put("discovery", CATEGORY_BILIM);
        map.put("education", CATEGORY_AKADEMI);
        map.put("example", CATEGORY_OKUL);
        map.put("experiment", CATEGORY_BILIM);
        map.put("guidance", CATEGORY_OKUL);
        map.put("history", CATEGORY_AKADEMI);
        map.put("information", CATEGORY_AKADEMI);
        map.put(KEY_INSTRUCTION, CATEGORY_OKUL);
        map.put("journal", CATEGORY_AKADEMI);
        map.put("knowledge", CATEGORY_AKADEMI);
        map.put("laboratory", CATEGORY_BILIM);
        map.put("language", LANGUAGE_CATEGORY);
        map.put("lecture", CATEGORY_OKUL);
        map.put("library", CATEGORY_AKADEMI);
        map.put("measurement", CATEGORY_BILIM);
        map.put("microscope", CATEGORY_BILIM);
        map.put("notebook", CATEGORY_OKUL);
        map.put("orientation", CATEGORY_AKADEMI);
        map.put("publication", CATEGORY_AKADEMI);
        map.put("question", CATEGORY_OKUL);
        map.put("reference", LANGUAGE_CATEGORY);
        map.put("research", CATEGORY_BILIM);
        map.put("science", CATEGORY_BILIM);
        map.put("teacher", CATEGORY_OKUL);
        map.put("technique", CATEGORY_BILIM);
        map.put("translation", LANGUAGE_CATEGORY);
        map.put("university", CATEGORY_AKADEMI);
        map.put("vocabulary", LANGUAGE_CATEGORY);
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

        map.put(KEY_APPLICATION, CATEGORY_YAZILIM);
        map.put(KEY_AUTOMATIC, CATEGORY_TEKNOLOJI);
        map.put(KEY_BATTERY, CATEGORY_DONANIM);
        map.put(KEY_CAMERA, CATEGORY_DONANIM);
        map.put(KEY_CONNECTION, CATEGORY_YAZILIM);
        map.put(KEY_DATABASE, CATEGORY_YAZILIM);
        map.put(KEY_DIGITAL, CATEGORY_TEKNOLOJI);
        map.put(KEY_ELECTRIC, CATEGORY_TEKNOLOJI);
        map.put(KEY_ELECTRICITY, CATEGORY_DONANIM);
        map.put(KEY_ENGINE, CATEGORY_DONANIM);
        map.put(KEY_EQUIPMENT, CATEGORY_DONANIM);
        map.put(KEY_EXTENSION, CATEGORY_YAZILIM);
        map.put(KEY_FUNCTION, CATEGORY_YAZILIM);
        map.put(KEY_INTERNET, CATEGORY_YAZILIM);
        map.put(KEY_KEYBOARD, CATEGORY_DONANIM);
        map.put(KEY_MACHINE, CATEGORY_DONANIM);
        map.put(KEY_MAINTENANCE, CATEGORY_DONANIM);
        map.put(KEY_MECHANISM, CATEGORY_DONANIM);
        map.put(KEY_MESSAGE, CATEGORY_ILETISIM);
        map.put(KEY_NETWORK, CATEGORY_YAZILIM);
        map.put(CATEGORY_SECRET_KEY, CATEGORY_YAZILIM);
        map.put(KEY_PLATFORM, CATEGORY_YAZILIM);
        map.put(KEY_PRINTER, CATEGORY_DONANIM);
        map.put(KEY_SOFTWARE, CATEGORY_YAZILIM);
        map.put(KEY_STORAGE, CATEGORY_DONANIM);
        map.put(KEY_SYSTEM, CATEGORY_YAZILIM);
        map.put(KEY_TECHNICAL, CATEGORY_TEKNOLOJI);
        map.put(KEY_TECHNOLOGY, CATEGORY_TEKNOLOJI);
        map.put(KEY_VERSION, CATEGORY_YAZILIM);

        map.put("agreement", CATEGORY_YONETIM);
        map.put("assistant", OFFICE_CATEGORY);
        map.put("business", CATEGORY_YONETIM);
        map.put("campaign", OFFICE_CATEGORY);
        map.put("candidate", CATEGORY_KARIYER);
        map.put("company", CATEGORY_YONETIM);
        map.put("conference", OFFICE_CATEGORY);
        map.put("contract", CATEGORY_YONETIM);
        map.put("delivery", CATEGORY_LOJISTIK);
        map.put("department", OFFICE_CATEGORY);
        map.put("document", OFFICE_CATEGORY);
        map.put("employee", CATEGORY_KARIYER);
        map.put("factory", CATEGORY_URETIM);
        map.put("feedback", OFFICE_CATEGORY);
        map.put("headquarters", OFFICE_CATEGORY);
        map.put("industry", CATEGORY_URETIM);
        map.put("initiative", CATEGORY_YONETIM);
        map.put("interview", CATEGORY_KARIYER);
        map.put("leadership", CATEGORY_YONETIM);
        map.put("management", CATEGORY_YONETIM);
        map.put("manufacturer", CATEGORY_URETIM);
        map.put("negotiation", CATEGORY_YONETIM);
        map.put("official", OFFICE_CATEGORY);
        map.put("opportunity", CATEGORY_KARIYER);
        map.put("organization", CATEGORY_YONETIM);
        map.put("partnership", CATEGORY_YONETIM);
        map.put("presentation", OFFICE_CATEGORY);
        map.put("priority", CATEGORY_YONETIM);
        map.put("procedure", CATEGORY_YONETIM);
        map.put("production", CATEGORY_URETIM);
        map.put("profession", CATEGORY_KARIYER);
        map.put("professional", CATEGORY_KARIYER);
        map.put("project", CATEGORY_YONETIM);
        map.put("proposal", CATEGORY_YONETIM);
        map.put("recommendation", CATEGORY_YONETIM);
        map.put("reception", OFFICE_CATEGORY);
        map.put("requirement", CATEGORY_YONETIM);
        map.put("service", OFFICE_CATEGORY);
        map.put("shipment", CATEGORY_LOJISTIK);
        map.put("signature", OFFICE_CATEGORY);
        map.put("specialist", CATEGORY_KARIYER);
        map.put("strategy", CATEGORY_YONETIM);
        map.put("supervisor", CATEGORY_YONETIM);
        map.put("warehouse", CATEGORY_LOJISTIK);
        map.put("workflow", CATEGORY_LOJISTIK);
        map.put("workplace", OFFICE_CATEGORY);
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
