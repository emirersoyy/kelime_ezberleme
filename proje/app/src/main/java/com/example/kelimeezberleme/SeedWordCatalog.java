package com.example.kelimeezberleme;

import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class SeedWordCatalog {
    private static final String CATEGORY_GENERAL = "Genel";
    private static final String CATEGORY_VERBS = "Fiiller";
    private static final String CATEGORY_ADJECTIVES = "Sifatlar";
    private static final String CATEGORY_ABSTRACT = "Soyut";
    private static final String CATEGORY_ART = "Sanat";
    private static final String CATEGORY_TECHNOLOGY = "Teknoloji";
    private static final String CATEGORY_BUSINESS = "Is Dunyasi";
    private static final String CATEGORY_SOCIAL = "Sosyal";
    private static final String CATEGORY_EDUCATION = "Egitim";
    private static final String CATEGORY_HEALTH = "Saglik";
    private static final String CATEGORY_TRANSPORT = "Ulasim";
    private static final String CATEGORY_ECONOMY = "Ekonomi";
    private static final String CATEGORY_FOOD = "Yiyecek";
    private static final String CATEGORY_TIME = "Zaman";
    private static final String CATEGORY_FRUITS = "Meyveler";
    private static final String CATEGORY_EMOTIONS = "Duygular";
    private static final String CATEGORY_MATERIALS = "Malzemeler";
    private static final String CATEGORY_SHAPES = "Sekiller";
    private static final String WORD_EQUIPMENT = "equipment";
    private static final String WORD_FESTIVAL = "festival";
    private static final String WORD_INSTRUCTION = "instruction";
    private static final String WORD_INTERNET = "internet";
    private static final String WORD_PLATFORM = "platform";
    private static final String WORD_DEFINE = "tanımlamak";
    private static final String WORD_JOIN = "katilmak";
    private static final String WORD_ORGANIZE = "duzenlemek";
    private static final String WORD_ESTIMATE = "tahmin etmek";
    private static final String WORD_RECOMMEND = "onermek";
    private static final String WORD_COMMUNICATE = "iletisim kurmak";
    private static final String WORD_DISPLAY = "gostermek";
    private static final String WORD_BASIC = "temel";

    private SeedWordCatalog() {}

    static String pictureRefForWord(String english, String category) {
        return "drawable:" + drawableNameForWord(english);
    }

    private static boolean hasCuratedImage(String english) {
        String word = english == null ? "" : english.trim().toLowerCase(Locale.US);
        switch (word) {
            case "above":
            case "address":
            case "airport":
            case "album":
            case "along":
            case "analysis":
            case "apartment":
            case "apple":
            case "application":
            case "appointment":
            case "architecture":
            case "area":
            case "arena":
            case "artist":
            case "atmosphere":
            case "audience":
            case "backpack":
            case "bakery":
            case "battery":
            case "beach":
            case "bedroom":
            case "biology":
            case "bird":
            case "black":
            case "blanket":
            case "book":
            case "boundary":
            case "bread":
            case "breakfast":
            case "brown":
            case "building":
            case "cabinet":
            case "camera":
            case "candy":
            case "capital":
            case "cat":
            case "certificate":
            case "chair":
            case "chemical":
            case "chemistry":
            case "city":
            case "classroom":
            case "climate":
            case "coast":
            case "component":
            case "computer":
            case "connection":
            case "country":
            case "courtyard":
            case "cream":
            case "dance":
            case "database":
            case "destination":
            case "dictionary":
            case "direction":
            case "director":
            case "discovery":
            case "distance":
            case "dog":
            case "door":
            case "education":
            case "electricity":
            case "elevator":
            case "emergency":
            case "energy":
            case "engine":
            case "environment":
            case WORD_EQUIPMENT:
            case "example":
            case "exercise":
            case "experiment":
            case "extension":
            case "facility":
            case WORD_FESTIVAL:
            case "field":
            case "floor":
            case "flower":
            case "front":
            case "fruit":
            case "function":
            case "furniture":
            case "garden":
            case "glass":
            case "grain":
            case "green":
            case "grocery":
            case "guidance":
            case "history":
            case "hospital":
            case "house":
            case "household":
            case "image":
            case "information":
            case "ingredient":
            case WORD_INSTRUCTION:
            case "instrument":
            case WORD_INTERNET:
            case "journal":
            case "journey":
            case "keyboard":
            case "knife":
            case "knowledge":
            case "laboratory":
            case "landmark":
            case "landscape":
            case "lecture":
            case "library":
            case "lifestyle":
            case "literature":
            case "location":
            case "machine":
            case "maintenance":
            case "material":
            case "measurement":
            case "mechanism":
            case "medicine":
            case "message":
            case "metal":
            case "microscope":
            case "money":
            case "mountain":
            case "movement":
            case "music":
            case "navigation":
            case "network":
            case "north":
            case "notebook":
            case "operation":
            case "orientation":
            case "paint":
            case "passenger":
            case "password":
            case "pen":
            case "performance":
            case "pharmacy":
            case "phone":
            case "photograph":
            case "place":
            case "plate":
            case WORD_PLATFORM:
            case "pressure":
            case "printer":
            case "publication":
            case "question":
            case "radio":
            case "reference":
            case "refrigerator":
            case "research":
            case "restaurant":
            case "school":
            case "science":
            case "shelter":
            case "software":
            case "south":
            case "station":
            case "storage":
            case "substance":
            case "sugar":
            case "system":
            case "table":
            case "teacher":
            case "technique":
            case "technology":
            case "temperature":
            case "traffic":
            case "train":
            case "translation":
            case "transportation":
            case "treatment":
            case "university":
            case "vegetable":
            case "vehicle":
            case "version":
            case "vocabulary":
            case "weather":
            case "white":
            case "wildlife":
            case "window":
            case "world":
            case "zoology":
                return true;
            default:
                return false;
        }
    }
    private static String drawableNameForWord(String english) {
        String clean = english == null ? "" : english.trim().toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "_");
        clean = clean.replaceAll("^_+|_+$", "");
        if (clean.isEmpty()) clean = "general";
        return "word_img_" + clean;
    }

    static List<String[]> extraWords() {
        List<String[]> words = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String[] v : VERBS) add(words, seen, cap(v[0]), v[1], CATEGORY_VERBS);
        for (String[] a : ADJECTIVES) add(words, seen, cap(a[0]), a[1], CATEGORY_ADJECTIVES);
        for (String[] n : NOUNS) add(words, seen, cap(n[0]), n[1], n[2]);
        for (String[] w : GENERAL_WORDS) add(words, seen, cap(w[0]), w[1], w[2]);

        return words;
    }

    static void removeOldGeneratedWords(SQLiteDatabase db) {
        List<String> generatedWords = new ArrayList<>();
        Set<String> generated = new HashSet<>();
        Set<String> base = new HashSet<>();

        for (String[] v : VERBS) {
            base.add(v[0].toLowerCase(Locale.US));
            addGenerated(generatedWords, generated, thirdPerson(v[0]));
            addGenerated(generatedWords, generated, past(v[0]));
            addGenerated(generatedWords, generated, ing(v[0]));
        }
        for (String[] a : ADJECTIVES) {
            base.add(a[0].toLowerCase(Locale.US));
            addGenerated(generatedWords, generated, comparative(a[0]));
            addGenerated(generatedWords, generated, superlative(a[0]));
        }
        for (String[] n : NOUNS) {
            base.add(n[0].toLowerCase(Locale.US));
            addGenerated(generatedWords, generated, plural(n[0]));
        }

        for (String word : generatedWords) {
            if (base.contains(word.toLowerCase(Locale.US))) continue;
            db.execSQL(
                    "DELETE FROM " + DatabaseHelper.TABLE_SAMPLES +
                            " WHERE " + DatabaseHelper.COL_WORD_ID + " IN (" +
                            "SELECT " + DatabaseHelper.COL_WORD_ID + " FROM " + DatabaseHelper.TABLE_WORDS +
                            " WHERE lower(trim(" + DatabaseHelper.COL_ENG_WORD + ")) = lower(trim(?))" +
                            ")",
                    new Object[]{word}
            );
            db.execSQL(
                    "DELETE FROM " + DatabaseHelper.TABLE_WORDS +
                            " WHERE lower(trim(" + DatabaseHelper.COL_ENG_WORD + ")) = lower(trim(?))",
                    new Object[]{word}
            );
        }
    }

    private static void add(List<String[]> words, Set<String> seen, String english, String turkish, String category) {
        String key = english.toLowerCase(Locale.US);
        if (seen.add(key)) {
            words.add(new String[]{english, turkish, category});
        }
    }

    private static void addGenerated(List<String> words, Set<String> seen, String english) {
        String key = english.toLowerCase(Locale.US);
        if (seen.add(key)) words.add(english);
    }

    private static String cap(String word) {
        return word.substring(0, 1).toUpperCase(Locale.US) + word.substring(1);
    }

    private static String thirdPerson(String verb) {
        if (verb.endsWith("y") && !isVowel(beforeLast(verb))) return verb.substring(0, verb.length() - 1) + "ies";
        if (verb.endsWith("s") || verb.endsWith("x") || verb.endsWith("z") || verb.endsWith("ch") || verb.endsWith("sh") || verb.endsWith("o")) return verb + "es";
        return verb + "s";
    }

    private static String past(String verb) {
        if (verb.endsWith("e")) return verb + "d";
        if (verb.endsWith("y") && !isVowel(beforeLast(verb))) return verb.substring(0, verb.length() - 1) + "ied";
        return verb + "ed";
    }

    private static String ing(String verb) {
        if (verb.endsWith("ie")) return verb.substring(0, verb.length() - 2) + "ying";
        if (verb.endsWith("e") && !verb.endsWith("ee")) return verb.substring(0, verb.length() - 1) + "ing";
        return verb + "ing";
    }

    private static String comparative(String adjective) {
        if (adjective.endsWith("y") && !isVowel(beforeLast(adjective))) return adjective.substring(0, adjective.length() - 1) + "ier";
        if (adjective.length() <= 5) return adjective + "er";
        return "more " + adjective;
    }

    private static String superlative(String adjective) {
        if (adjective.endsWith("y") && !isVowel(beforeLast(adjective))) return adjective.substring(0, adjective.length() - 1) + "iest";
        if (adjective.length() <= 5) return adjective + "est";
        return "most " + adjective;
    }

    private static String plural(String noun) {
        if (noun.endsWith("y") && !isVowel(beforeLast(noun))) return noun.substring(0, noun.length() - 1) + "ies";
        if (noun.endsWith("s") || noun.endsWith("x") || noun.endsWith("z") || noun.endsWith("ch") || noun.endsWith("sh")) return noun + "es";
        return noun + "s";
    }

    private static char beforeLast(String value) {
        return value.length() < 2 ? 'a' : value.charAt(value.length() - 2);
    }

    private static boolean isVowel(char c) {
        return "aeiou".indexOf(Character.toLowerCase(c)) >= 0;
    }

    private static final String[][] VERBS = {
            {"accept", "kabul etmek"}, {"achieve", "basarmak"}, {"admire", "hayran olmak"}, {"advise", "tavsiye etmek"},
            {"answer", "cevaplamak"}, {"arrive", "varmak"}, {"assist", "yardim etmek"}, {"attack", "saldirmak"},
            {"avoid", "kacinmak"}, {"balance", "dengelemek"}, {"believe", "inanmak"}, {"borrow", "odunc almak"},
            {"breathe", "nefes almak"}, {"calculate", "hesaplamak"}, {"celebrate", "kutlamak"}, {"change", "degistirmek"},
            {"choose", "secmek"}, {"collect", "toplamak"}, {"compare", "karsilastirmak"}, {"complete", "tamamlamak"},
            {"connect", "baglamak"}, {"consider", "dusunmek"}, {"continue", "devam etmek"}, {"control", "kontrol etmek"},
            {"cook", "pisirmek"}, {"create", "olusturmak"}, {"decide", "karar vermek"}, {"deliver", "teslim etmek"},
            {"describe", WORD_DEFINE}, {"design", "tasarlamak"}, {"discover", "kesfetmek"}, {"discuss", "tartismak"},
            {"earn", "kazanmak"}, {"educate", "egitmek"}, {"encourage", "cesaretlendirmek"}, {"examine", "incelemek"},
            {"explain", "aciklamak"}, {"explore", "kesfetmek"}, {"fail", "basarisiz olmak"}, {"follow", "takip etmek"},
            {"forgive", "affetmek"}, {"gather", "toplanmak"}, {"handle", "yonetmek"}, {"imagine", "hayal etmek"},
            {"improve", "gelistirmek"}, {"include", "dahil etmek"}, {"increase", "artirmak"}, {"inform", "bilgilendirmek"},
            {"invite", "davet etmek"}, {"join", WORD_JOIN}, {"manage", "yonetmek"}, {"measure", "olcmek"},
            {"mention", "bahsetmek"}, {"notice", "fark etmek"}, {"observe", "gozlemlemek"}, {"organize", WORD_ORGANIZE},
            {"prepare", "hazirlamak"}, {"prevent", "onlemek"}, {"protect", "korumak"}, {"provide", "saglamak"},
            {"receive", "almak"}, {"reduce", "azaltmak"}, {"remember", "hatirlamak"}, {"repair", "tamir etmek"},
            {"repeat", "tekrarlamak"}, {"replace", "degistirmek"}, {"reply", "yanitlamak"}, {"respect", "saygi duymak"},
            {"return", "geri donmek"}, {"save", "kaydetmek"}, {"search", "aramak"}, {"select", "secmek"},
            {"separate", "ayirmak"}, {"solve", "cozmek"}, {"support", "desteklemek"}, {"survive", "hayatta kalmak"},
            {"travel", "seyahat etmek"}, {"understand", "anlamak"}, {"update", "guncellemek"}, {"visit", "ziyaret etmek"},
            {"wait", "beklemek"}, {"wonder", "merak etmek"}, {"worry", "endişelenmek"}, {"compare", "kiyaslamak"},
            {"develop", "gelistirmek"}, {"divide", "bolmek"}, {"estimate", WORD_ESTIMATE}, {"identify", WORD_DEFINE},
            {"introduce", "tanitmak"}, {"maintain", "surdurmek"}, {"operate", "calistirmak"}, {"perform", "yerine getirmek"},
            {"publish", "yayinlamak"}, {"recognize", "tanımak"}, {"recommend", WORD_RECOMMEND}, {"record", "kaydetmek"},
            {"remove", "kaldirmak"}, {"respond", "yanit vermek"}, {"review", "gozden gecirmek"}, {"schedule", "planlamak"},
            {"translate", "cevirmek"}, {"transport", "tasimak"}, {"verify", "dogrulamak"}, {"volunteer", "gonullu olmak"},
            {"whisper", "fisildamak"}, {"impress", "etkilemek"}, {"deliver", "ulastirmak"}, {"decorate", "suslemek"},
            {"adapt", "uyarlamak"}, {"adjust", "ayarlamak"}, {"announce", "duyurmak"}, {"apologize", "ozur dilemek"},
            {"apply", "basvurmak"}, {"argue", "tartismak"}, {"arrange", WORD_ORGANIZE}, {"attach", "eklemek"},
            {"behave", "davranmak"}, {"belong", "ait olmak"}, {"cancel", "iptal etmek"}, {"capture", "yakalamak"},
            {"celebrate", "kutlamak"}, {"combine", "birleştirmek"}, {"command", "emretmek"}, {"communicate", WORD_COMMUNICATE},
            {"complain", "sikayet etmek"}, {"confirm", "onaylamak"}, {"contact", WORD_COMMUNICATE}, {"contain", "icermek"},
            {"copy", "kopyalamak"}, {"correct", "duzeltmek"}, {"damage", "zarar vermek"}, {"depend", "bagli olmak"},
            {"display", WORD_DISPLAY}, {"download", "indirmek"}, {"employ", "ise almak"}, {"enable", "etkinlestirmek"},
            {"enter", "girmek"}, {"escape", "kacmak"}, {"export", "disa aktarmak"}, {"express", "ifade etmek"},
            {"extend", "uzatmak"}, {"fill", "doldurmak"}, {"filter", "filtrelemek"}, {"finish", "bitirmek"},
            {"graduate", "mezun olmak"}, {"highlight", "vurgulamak"}, {"import", "ice aktarmak"}, {"install", "kurmak"},
            {"interview", "gorusme yapmak"}, {"invent", "icat etmek"}, {"invest", "yatirim yapmak"}, {"label", "etiketlemek"},
            {"locate", "yerini bulmak"}, {"lock", "kilitlemek"}, {"monitor", "izlemek"}, {"negotiate", "pazarlik etmek"},
            {"pack", "paketlemek"}, {"participate", WORD_JOIN}, {"pause", "duraklatmak"}, {"prefer", "tercih etmek"},
            {"print", "yazdirmak"}, {"process", "islemek"}, {"program", "programlamak"}, {"promise", "soz vermek"},
            {"raise", "yukseltmek"}, {"release", "serbest birakmak"}, {"request", "istemek"}, {"reserve", "ayirtmak"},
            {"restart", "yeniden baslatmak"}, {"share", "paylasmak"}, {"sign", "imzalamak"}, {"submit", "teslim etmek"},
            {"suggest", WORD_RECOMMEND}, {"upload", "yuklemek"}
    };

    private static final String[][] ADJECTIVES = {
            {"able", "yetenekli"}, {"accurate", "dogru"}, {"active", "aktif"}, {"ancient", "antik"}, {"basic", WORD_BASIC},
            {"careful", "dikkatli"}, {"central", "merkezi"}, {"common", "yaygin"}, {"creative", "yaratici"}, {"curious", "merakli"},
            {"daily", "gunluk"}, {"dangerous", "tehlikeli"}, {"deep", "derin"}, {"difficult", "zor"}, {"direct", "dogrudan"},
            {"early", "erken"}, {"equal", "esit"}, {"famous", "unlu"}, {"final", "son"}, {"foreign", "yabanci"},
            {"formal", "resmi"}, {"free", "ozgur"}, {"friendly", "arkadasca"}, {"global", "kuresel"}, {"healthy", "saglikli"},
            {"heavy", "agir"}, {"honest", "durust"}, {"important", "onemli"}, {"kind", "nazik"}, {"large", "buyuk"},
            {"late", "gec"}, {"modern", "modern"}, {"natural", "dogal"}, {"normal", "normal"}, {"patient", "sabirli"},
            {"personal", "kisisel"}, {"popular", "populer"}, {"possible", "mumkun"}, {"public", "kamusal"}, {"ready", "hazir"},
            {"regular", "duzenli"}, {"safe", "guvenli"}, {"serious", "ciddi"}, {"simple", "basit"}, {"special", "ozel"},
            {"strong", "guclu"}, {"successful", "basarili"}, {"useful", "faydali"}, {"valuable", "degerli"}, {"young", "genc"}
    };

    private static final String[][] NOUNS = {
            {"ability", "yetenek", CATEGORY_ABSTRACT}, {"accident", "kaza", CATEGORY_GENERAL}, {"address", "adres", "Yer"}, {"answer", "cevap", CATEGORY_GENERAL},
            {"area", "alan", "Yer"}, {"artist", "sanatci", CATEGORY_ART}, {"attention", "dikkat", CATEGORY_ABSTRACT}, {"battery", "pil", CATEGORY_TECHNOLOGY},
            {"business", "is", CATEGORY_BUSINESS}, {"camera", "kamera", CATEGORY_TECHNOLOGY}, {"capital", "baskent", "Yer"}, {"choice", "secim", CATEGORY_ABSTRACT},
            {"culture", "kultur", CATEGORY_SOCIAL}, {"decision", "karar", CATEGORY_ABSTRACT}, {"energy", "enerji", "Doga"}, {"engine", "motor", CATEGORY_TECHNOLOGY},
            {"example", "ornek", CATEGORY_EDUCATION}, {"exercise", "egzersiz", CATEGORY_HEALTH}, {"factory", "fabrika", CATEGORY_BUSINESS}, {"garden", "bahce", "Doga"},
            {"history", "tarih", CATEGORY_EDUCATION}, {"hospital", "hastane", CATEGORY_HEALTH}, {WORD_INTERNET, WORD_INTERNET, CATEGORY_TECHNOLOGY}, {"journey", "yolculuk", CATEGORY_TRANSPORT},
            {"knowledge", "bilgi", CATEGORY_EDUCATION}, {"library", "kutuphane", CATEGORY_EDUCATION}, {"machine", "makine", CATEGORY_TECHNOLOGY}, {"market", "pazar", CATEGORY_ECONOMY},
            {"memory", "hafiza", CATEGORY_ABSTRACT}, {"message", "mesaj", CATEGORY_TECHNOLOGY}, {"mountain", "dag", "Doga"}, {"neighbor", "komsu", CATEGORY_SOCIAL},
            {"opinion", "fikir", CATEGORY_ABSTRACT}, {"problem", "sorun", CATEGORY_GENERAL}, {"project", "proje", CATEGORY_BUSINESS}, {"quality", "kalite", CATEGORY_ABSTRACT},
            {"question", "soru", CATEGORY_EDUCATION}, {"reason", "neden", CATEGORY_ABSTRACT}, {"research", "arastirma", CATEGORY_EDUCATION}, {"result", "sonuc", CATEGORY_GENERAL},
            {"science", "bilim", CATEGORY_EDUCATION}, {"service", "hizmet", CATEGORY_BUSINESS}, {"society", "toplum", CATEGORY_SOCIAL}, {"station", "istasyon", CATEGORY_TRANSPORT},
            {"system", "sistem", CATEGORY_TECHNOLOGY}, {"teacher", "ogretmen", CATEGORY_EDUCATION}, {"traffic", "trafik", CATEGORY_TRANSPORT}, {"weather", "hava durumu", "Doga"}
    };

    private static final String[][] GENERAL_WORDS = {
            {"airport", "havalimani", CATEGORY_TRANSPORT}, {"apartment", "daire", "Ev"}, {"appointment", "randevu", CATEGORY_HEALTH}, {"argument", "tartisma", CATEGORY_SOCIAL},
            {"audience", "seyirci", CATEGORY_ART}, {"backpack", "sirt cantasi", CATEGORY_EDUCATION}, {"bakery", "firin", CATEGORY_FOOD}, {"bedroom", "yatak odasi", "Ev"},
            {"biology", "biyoloji", CATEGORY_EDUCATION}, {"blanket", "battaniye", "Ev"}, {"calendar", "takvim", CATEGORY_TIME}, {"candidate", "aday", CATEGORY_BUSINESS},
            {"capacity", "kapasite", CATEGORY_GENERAL}, {"ceremony", "toren", CATEGORY_SOCIAL}, {"champion", "sampiyon", "Spor"}, {"chemical", "kimyasal", CATEGORY_EDUCATION},
            {"climate", "iklim", "Doga"}, {"company", "sirket", CATEGORY_BUSINESS}, {"conference", "konferans", CATEGORY_BUSINESS}, {"connection", "baglanti", CATEGORY_TECHNOLOGY},
            {"contract", "sozlesme", CATEGORY_BUSINESS}, {"customer", "musteri", CATEGORY_ECONOMY}, {"database", "veritabani", CATEGORY_TECHNOLOGY}, {"department", "bolum", CATEGORY_BUSINESS},
            {"dictionary", "sozluk", CATEGORY_EDUCATION}, {"direction", "yon", "Yer"}, {"document", "belge", CATEGORY_BUSINESS}, {"education", "egitim", CATEGORY_EDUCATION},
            {"elevator", "asansor", "Ev"}, {"employee", "calisan", CATEGORY_BUSINESS}, {WORD_EQUIPMENT, "ekipman", CATEGORY_TECHNOLOGY}, {"evidence", "kanit", CATEGORY_GENERAL},
            {"experience", "deneyim", CATEGORY_ABSTRACT}, {"experiment", "deney", CATEGORY_EDUCATION}, {"furniture", "mobilya", "Ev"}, {"government", "hukumet", CATEGORY_SOCIAL},
            {"grocery", "market urunu", CATEGORY_FOOD}, {"headline", "manset", CATEGORY_GENERAL}, {"identity", "kimlik", CATEGORY_SOCIAL}, {"industry", "endustri", CATEGORY_BUSINESS},
            {"information", "bilgi", CATEGORY_EDUCATION}, {WORD_INSTRUCTION, "talimat", CATEGORY_EDUCATION}, {"insurance", "sigorta", CATEGORY_ECONOMY}, {"interview", "gorusme", CATEGORY_BUSINESS},
            {"investment", "yatirim", CATEGORY_ECONOMY}, {"keyboard", "klavye", CATEGORY_TECHNOLOGY}, {"laboratory", "laboratuvar", CATEGORY_EDUCATION}, {"landscape", "manzara", "Doga"},
            {"location", "konum", "Yer"}, {"medicine", "ilac", CATEGORY_HEALTH}, {"membership", "uyelik", CATEGORY_SOCIAL}, {"microscope", "mikroskop", CATEGORY_EDUCATION},
            {"navigation", "navigasyon", CATEGORY_TRANSPORT}, {"newspaper", "gazete", CATEGORY_GENERAL}, {"notebook", "defter", CATEGORY_EDUCATION}, {"operation", "operasyon", CATEGORY_HEALTH},
            {"organization", "kurulus", CATEGORY_BUSINESS}, {"passenger", "yolcu", CATEGORY_TRANSPORT}, {"password", "sifre", CATEGORY_TECHNOLOGY}, {"payment", "odeme", CATEGORY_ECONOMY},
            {"permission", "izin", CATEGORY_GENERAL}, {"photograph", "fotograf", CATEGORY_ART}, {"population", "nufus", CATEGORY_SOCIAL}, {"president", "baskan", CATEGORY_SOCIAL},
            {"pressure", "basinc", "Doga"}, {"printer", "yazici", CATEGORY_TECHNOLOGY}, {"property", "mulkiyet", CATEGORY_ECONOMY}, {"reception", "resepsiyon", CATEGORY_BUSINESS},
            {"refrigerator", "buzdolabi", "Ev"}, {"relationship", "iliski", CATEGORY_SOCIAL}, {"restaurant", "restoran", CATEGORY_FOOD}, {"schedule", "program", CATEGORY_TIME},
            {"signature", "imza", CATEGORY_BUSINESS}, {"software", "yazilim", CATEGORY_TECHNOLOGY}, {"solution", "cozum", CATEGORY_GENERAL}, {"strategy", "strateji", CATEGORY_BUSINESS},
            {"temperature", "sicaklik", "Doga"}, {"tradition", "gelenek", CATEGORY_SOCIAL}, {"university", "universite", CATEGORY_EDUCATION}, {"vegetable", "sebze", CATEGORY_FOOD},
            {"vocabulary", "kelime hazinesi", CATEGORY_EDUCATION}, {"warehouse", "depo", CATEGORY_BUSINESS}, {"workshop", "atolye", CATEGORY_BUSINESS}, {"zoology", "zooloji", CATEGORY_EDUCATION},
            {"banana", "muz", CATEGORY_FRUITS}, {"orange", "portakal", CATEGORY_FRUITS}, {"grape", "uzum", CATEGORY_FRUITS}, {"strawberry", "cilek", CATEGORY_FRUITS},
            {"cherry", "kiraz", CATEGORY_FRUITS}, {"peach", "seftali", CATEGORY_FRUITS}, {"pear", "armut", CATEGORY_FRUITS}, {"watermelon", "karpuz", CATEGORY_FRUITS},
            {"joy", "nese", CATEGORY_EMOTIONS}, {"anger", "ofke", CATEGORY_EMOTIONS}, {"fear", "korku", CATEGORY_EMOTIONS}, {"hope", "umut", CATEGORY_EMOTIONS},
            {"pride", "gurur", CATEGORY_EMOTIONS}, {"sadness", "uzuntu", CATEGORY_EMOTIONS},
            {"athlete", "sporcu", "Spor"}, {"coach", "antrenor", "Spor"}, {"stadium", "stadyum", "Spor"}, {"training", "antrenman", "Spor"},
            {"victory", "zafer", "Spor"},
            {"wood", "ahsap", CATEGORY_MATERIALS}, {"plastic", "plastik", CATEGORY_MATERIALS}, {"fabric", "kumas", CATEGORY_MATERIALS}, {"stone", "tas", CATEGORY_MATERIALS},
            {"circle", "daire", CATEGORY_SHAPES}, {"square", "kare", CATEGORY_SHAPES}, {"triangle", "ucgen", CATEGORY_SHAPES}, {"rectangle", "dikdortgen", CATEGORY_SHAPES},
            {"abstract", "soyut", CATEGORY_ADJECTIVES}, {"academic", "akademik", CATEGORY_ADJECTIVES}, {"advanced", "ileri", CATEGORY_ADJECTIVES}, {"affordable", "uygun fiyatli", CATEGORY_ADJECTIVES},
            {"automatic", "otomatik", CATEGORY_ADJECTIVES}, {"available", "mevcut", CATEGORY_ADJECTIVES}, {"comfortable", "rahat", CATEGORY_ADJECTIVES}, {"confident", "ozguvenli", CATEGORY_ADJECTIVES},
            {"consistent", "tutarli", CATEGORY_ADJECTIVES}, {"convenient", "elverisli", CATEGORY_ADJECTIVES}, {"critical", "kritik", CATEGORY_ADJECTIVES}, {"delicious", "lezzetli", CATEGORY_ADJECTIVES},
            {"efficient", "verimli", CATEGORY_ADJECTIVES}, {"electric", "elektrikli", CATEGORY_ADJECTIVES}, {"emotional", "duygusal", CATEGORY_ADJECTIVES}, {"excellent", "mukemmel", CATEGORY_ADJECTIVES},
            {"expensive", "pahali", CATEGORY_ADJECTIVES}, {"flexible", "esnek", CATEGORY_ADJECTIVES}, {"independent", "bagimsiz", CATEGORY_ADJECTIVES}, {"necessary", "gerekli", CATEGORY_ADJECTIVES},
            {"official", "resmi", CATEGORY_ADJECTIVES}, {"practical", "pratik", CATEGORY_ADJECTIVES}, {"professional", "profesyonel", CATEGORY_ADJECTIVES}, {"reasonable", "makul", CATEGORY_ADJECTIVES},
            {"responsible", "sorumlu", CATEGORY_ADJECTIVES}, {"sensitive", "hassas", CATEGORY_ADJECTIVES}, {"temporary", "gecici", CATEGORY_ADJECTIVES}, {"traditional", "geleneksel", CATEGORY_ADJECTIVES},
            {"communicate", WORD_COMMUNICATE, CATEGORY_VERBS}, {"coordinate", "koordine etmek", CATEGORY_VERBS}, {"demonstrate", WORD_DISPLAY, CATEGORY_VERBS},
            {"emphasize", "vurgulamak", CATEGORY_VERBS}, {"estimate", WORD_ESTIMATE, CATEGORY_VERBS}, {"evaluate", "degerlendirmek", CATEGORY_VERBS},
            {"generate", "uretmek", CATEGORY_VERBS}, {"implement", "uygulamak", CATEGORY_VERBS}, {"investigate", "arastirmak", CATEGORY_VERBS},
            {"participate", WORD_JOIN, CATEGORY_VERBS}, {"prioritize", "oncelik vermek", CATEGORY_VERBS}, {"recommend", WORD_RECOMMEND, CATEGORY_VERBS},
            {"represent", "temsil etmek", CATEGORY_VERBS}, {"specialize", "uzmanlasmak", CATEGORY_VERBS}, {"strengthen", "guclendirmek", CATEGORY_VERBS},
            {"account", "hesap", CATEGORY_ECONOMY}, {"achievement", "basari", CATEGORY_ABSTRACT}, {"activity", "etkinlik", CATEGORY_GENERAL}, {"adventure", "macera", CATEGORY_SOCIAL},
            {"agreement", "anlasma", CATEGORY_BUSINESS}, {"analysis", "analiz", CATEGORY_EDUCATION}, {"application", "uygulama", CATEGORY_TECHNOLOGY}, {"architecture", "mimari", CATEGORY_ART},
            {"assistant", "asistan", CATEGORY_BUSINESS}, {"atmosphere", "atmosfer", "Doga"}, {"behavior", "davranis", CATEGORY_SOCIAL}, {"benefit", "fayda", CATEGORY_ECONOMY},
            {"boundary", "sinir", "Yer"}, {"breakfast", "kahvalti", CATEGORY_FOOD}, {"building", "bina", "Yer"}, {"cabinet", "dolap", "Ev"},
            {"campaign", "kampanya", CATEGORY_BUSINESS}, {"certificate", "sertifika", CATEGORY_EDUCATION}, {"challenge", "zorluk", CATEGORY_ABSTRACT}, {"character", "karakter", CATEGORY_SOCIAL},
            {"chemistry", "kimya", CATEGORY_EDUCATION}, {"childhood", "cocukluk", CATEGORY_TIME}, {"classroom", "sinif", CATEGORY_EDUCATION}, {"community", "topluluk", CATEGORY_SOCIAL},
            {"competition", "yarısma", "Spor"}, {"component", "bilesen", CATEGORY_TECHNOLOGY}, {"concept", "kavram", CATEGORY_ABSTRACT}, {"condition", "durum", CATEGORY_GENERAL},
            {"consequence", "sonuc", CATEGORY_ABSTRACT}, {"conversation", "konusma", CATEGORY_SOCIAL}, {"courtyard", "avlu", "Yer"}, {"currency", "para birimi", CATEGORY_ECONOMY},
            {"deadline", "son tarih", CATEGORY_TIME}, {"delivery", "teslimat", CATEGORY_BUSINESS}, {"destination", "varis noktasi", CATEGORY_TRANSPORT}, {"development", "gelisim", CATEGORY_ABSTRACT},
            {"difference", "fark", CATEGORY_ABSTRACT}, {"difficulty", "zorluk", CATEGORY_ABSTRACT}, {"director", "yonetmen", CATEGORY_ART}, {"discovery", "kesif", CATEGORY_EDUCATION},
            {"discussion", "tartisma", CATEGORY_SOCIAL}, {"distance", "mesafe", "Yer"}, {"economy", "ekonomi", CATEGORY_ECONOMY}, {"effort", "caba", CATEGORY_ABSTRACT},
            {"electricity", "elektrik", CATEGORY_TECHNOLOGY}, {"emergency", "acil durum", CATEGORY_HEALTH}, {"environment", "cevre", "Doga"}, {WORD_EQUIPMENT, "ekipman", CATEGORY_TECHNOLOGY},
            {"expression", "ifade", CATEGORY_SOCIAL}, {"extension", "uzanti", CATEGORY_TECHNOLOGY}, {"facility", "tesis", "Yer"}, {"failure", "basarisizlik", CATEGORY_ABSTRACT},
            {"feedback", "geri bildirim", CATEGORY_BUSINESS}, {WORD_FESTIVAL, WORD_FESTIVAL, CATEGORY_ART}, {"foundation", WORD_BASIC, CATEGORY_ABSTRACT}, {"freedom", "ozgurluk", CATEGORY_ABSTRACT},
            {"function", "islev", CATEGORY_TECHNOLOGY}, {"generation", "nesil", CATEGORY_SOCIAL}, {"guidance", "rehberlik", CATEGORY_EDUCATION}, {"headquarters", "merkez ofis", CATEGORY_BUSINESS},
            {"heritage", "miras", CATEGORY_SOCIAL}, {"household", "ev halki", "Ev"}, {"imagination", "hayal gucu", CATEGORY_ABSTRACT}, {"improvement", "iyilesme", CATEGORY_ABSTRACT},
            {"independence", "bagimsizlik", CATEGORY_ABSTRACT}, {"influence", "etki", CATEGORY_SOCIAL}, {"ingredient", "malzeme", CATEGORY_FOOD}, {"initiative", "girişim", CATEGORY_BUSINESS},
            {"inspiration", "ilham", CATEGORY_ABSTRACT}, {WORD_INSTRUCTION, "talimat", CATEGORY_EDUCATION}, {"instrument", "enstruman", CATEGORY_ART}, {"intelligence", "zeka", CATEGORY_ABSTRACT},
            {"journal", "gunluk", CATEGORY_EDUCATION}, {"landmark", "simgesel yer", "Yer"}, {"leadership", "liderlik", CATEGORY_BUSINESS}, {"lecture", "ders", CATEGORY_EDUCATION},
            {"lifestyle", "yasam tarzi", CATEGORY_HEALTH}, {"literature", "edebiyat", CATEGORY_ART}, {"maintenance", "bakim", CATEGORY_TECHNOLOGY}, {"management", "yonetim", CATEGORY_BUSINESS},
            {"manufacturer", "uretici", CATEGORY_BUSINESS}, {"material", "malzeme", CATEGORY_MATERIALS}, {"measurement", "olcum", CATEGORY_EDUCATION}, {"mechanism", "mekanizma", CATEGORY_TECHNOLOGY},
            {"movement", "hareket", CATEGORY_HEALTH}, {"negotiation", "muzakere", CATEGORY_BUSINESS}, {"network", "ag", CATEGORY_TECHNOLOGY}, {"obligation", "zorunluluk", CATEGORY_ABSTRACT},
            {"opportunity", "firsat", CATEGORY_BUSINESS}, {"orientation", "oryantasyon", CATEGORY_EDUCATION}, {"participant", "katilimci", CATEGORY_SOCIAL}, {"partnership", "ortaklik", CATEGORY_BUSINESS},
            {"performance", "performans", CATEGORY_ART}, {"perspective", "bakis acisi", CATEGORY_ABSTRACT}, {"pharmacy", "eczane", CATEGORY_HEALTH}, {WORD_PLATFORM, WORD_PLATFORM, CATEGORY_TECHNOLOGY},
            {"preference", "tercih", CATEGORY_ABSTRACT}, {"presentation", "sunum", CATEGORY_BUSINESS}, {"priority", "oncelik", CATEGORY_BUSINESS}, {"procedure", "prosedur", CATEGORY_BUSINESS},
            {"production", "uretim", CATEGORY_BUSINESS}, {"profession", "meslek", CATEGORY_BUSINESS}, {"proposal", "teklif", CATEGORY_BUSINESS}, {"publication", "yayin", CATEGORY_EDUCATION},
            {"recognition", "taninma", CATEGORY_SOCIAL}, {"recommendation", "onerı", CATEGORY_BUSINESS}, {"reference", "referans", CATEGORY_EDUCATION}, {"reflection", "yansima", CATEGORY_ABSTRACT},
            {"reliability", "guvenilirlik", CATEGORY_ABSTRACT}, {"replacement", "yedek", CATEGORY_GENERAL}, {"requirement", "gereksinim", CATEGORY_BUSINESS}, {"reservation", "rezervasyon", CATEGORY_SOCIAL},
            {"resource", "kaynak", CATEGORY_GENERAL}, {"responsibility", "sorumluluk", CATEGORY_ABSTRACT}, {"satisfaction", "memnuniyet", CATEGORY_EMOTIONS}, {"selection", "secim", CATEGORY_GENERAL},
            {"shelter", "barinak", "Yer"}, {"shipment", "sevkiyat", CATEGORY_BUSINESS}, {"specialist", "uzman", CATEGORY_BUSINESS}, {"statement", "aciklama", CATEGORY_GENERAL},
            {"storage", "depolama", CATEGORY_TECHNOLOGY}, {"structure", "yapi", CATEGORY_GENERAL}, {"substance", "madde", CATEGORY_MATERIALS}, {"suggestion", "oneri", CATEGORY_GENERAL},
            {"supervisor", "amir", CATEGORY_BUSINESS}, {"technique", "teknik", CATEGORY_EDUCATION}, {"technology", "teknoloji", CATEGORY_TECHNOLOGY}, {"tournament", "turnuva", "Spor"},
            {"translation", "ceviri", CATEGORY_EDUCATION}, {"transportation", "ulasim", CATEGORY_TRANSPORT}, {"treatment", "tedavi", CATEGORY_HEALTH}, {"vacation", "tatil", CATEGORY_TIME},
            {"variation", "cesitlilik", CATEGORY_GENERAL}, {"vehicle", "arac", CATEGORY_TRANSPORT}, {"version", "surum", CATEGORY_TECHNOLOGY}, {"volunteer", "gonullu", CATEGORY_SOCIAL},
            {"warranty", "garanti", CATEGORY_ECONOMY}, {"wildlife", "yaban hayati", "Doga"}, {"workflow", "is akisi", CATEGORY_BUSINESS}, {"workplace", "isyeri", CATEGORY_BUSINESS},
            {"abandon", "terk etmek", CATEGORY_VERBS}, {"absorb", "emmek", CATEGORY_VERBS}, {"accelerate", "hizlandirmak", CATEGORY_VERBS}, {"accompany", "eslik etmek", CATEGORY_VERBS},
            {"accuse", "suclamak", CATEGORY_VERBS}, {"activate", "etkinlestirmek", CATEGORY_VERBS}, {"admire", "hayran olmak", CATEGORY_VERBS}, {"adopt", "benimsemek", CATEGORY_VERBS},
            {"advertise", "reklam yapmak", CATEGORY_VERBS}, {"analyze", "analiz etmek", CATEGORY_VERBS}, {"approve", "onaylamak", CATEGORY_VERBS}, {"assemble", "bir araya getirmek", CATEGORY_VERBS},
            {"assign", "atamak", CATEGORY_VERBS}, {"broadcast", "yayinlamak", CATEGORY_VERBS}, {"clarify", "netlestirmek", CATEGORY_VERBS}, {"classify", "siniflandirmak", CATEGORY_VERBS},
            {"collaborate", "is birligi yapmak", CATEGORY_VERBS}, {"compete", "yarismak", CATEGORY_VERBS}, {"concentrate", "odaklanmak", CATEGORY_VERBS}, {"conclude", "sonuclandirmak", CATEGORY_VERBS},
            {"conduct", "yurutmek", CATEGORY_VERBS}, {"construct", "insa etmek", CATEGORY_VERBS}, {"contribute", "katkida bulunmak", CATEGORY_VERBS}, {"convince", "ikna etmek", CATEGORY_VERBS},
            {"criticize", "elestirmek", CATEGORY_VERBS}, {"decrease", "azalmak", CATEGORY_VERBS}, {"define", WORD_DEFINE, CATEGORY_VERBS}, {"detect", "tespit etmek", CATEGORY_VERBS},
            {"determine", "belirlemek", CATEGORY_VERBS}, {"distribute", "dagitmak", CATEGORY_VERBS}, {"donate", "bagislamak", CATEGORY_VERBS}, {"eliminate", "ortadan kaldirmak", CATEGORY_VERBS},
            {"establish", "kurmak", CATEGORY_VERBS}, {"expand", "genisletmek", CATEGORY_VERBS}, {"forecast", WORD_ESTIMATE, CATEGORY_VERBS}, {"hesitate", "tereddut etmek", CATEGORY_VERBS},
            {"illustrate", WORD_DISPLAY, CATEGORY_VERBS}, {"interpret", "yorumlamak", CATEGORY_VERBS}, {"launch", "baslatmak", CATEGORY_VERBS}, {"motivate", "motive etmek", CATEGORY_VERBS},
            {"preserve", "korumak", CATEGORY_VERBS}, {"purchase", "satin almak", CATEGORY_VERBS}, {"qualify", "nitelendirmek", CATEGORY_VERBS}, {"rebuild", "yeniden yapmak", CATEGORY_VERBS},
            {"recover", "iyilesmek", CATEGORY_VERBS}, {"recycle", "geri donusturmek", CATEGORY_VERBS}, {"reform", WORD_ORGANIZE, CATEGORY_VERBS}, {"register", "kayit olmak", CATEGORY_VERBS},
            {"relax", "rahatlamak", CATEGORY_VERBS}, {"remind", "hatirlatmak", CATEGORY_VERBS}, {"renovate", "yenilemek", CATEGORY_VERBS}, {"rescue", "kurtarmak", CATEGORY_VERBS},
            {"restore", "geri yuklemek", CATEGORY_VERBS}, {"satisfy", "memnun etmek", CATEGORY_VERBS}, {"simulate", "benzetmek", CATEGORY_VERBS}, {"stabilize", "dengelemek", CATEGORY_VERBS},
            {"subscribe", "abone olmak", CATEGORY_VERBS}, {"transform", "donusturmek", CATEGORY_VERBS}, {"upgrade", "yukseltmek", CATEGORY_VERBS}, {"validate", "dogrulamak", CATEGORY_VERBS},
            {"accurate", "dogru", CATEGORY_ADJECTIVES}, {"additional", "ek", CATEGORY_ADJECTIVES}, {"adequate", "yeterli", CATEGORY_ADJECTIVES}, {"aggressive", "saldirgan", CATEGORY_ADJECTIVES},
            {"ambitious", "hirsli", CATEGORY_ADJECTIVES}, {"appropriate", "uygun", CATEGORY_ADJECTIVES}, {"artificial", "yapay", CATEGORY_ADJECTIVES}, {"attractive", "cekici", CATEGORY_ADJECTIVES},
            {"balanced", "dengeli", CATEGORY_ADJECTIVES}, {"brilliant", "parlak", CATEGORY_ADJECTIVES}, {"capable", "yetenekli", CATEGORY_ADJECTIVES}, {"complex", "karmasik", CATEGORY_ADJECTIVES},
            {"constant", "sabit", CATEGORY_ADJECTIVES}, {"cultural", "kulturel", CATEGORY_ADJECTIVES}, {"digital", "dijital", CATEGORY_ADJECTIVES}, {"dramatic", "dramatik", CATEGORY_ADJECTIVES},
            {"dynamic", "dinamik", CATEGORY_ADJECTIVES}, {"effective", "etkili", CATEGORY_ADJECTIVES}, {"enormous", "kocaman", CATEGORY_ADJECTIVES}, {"essential", WORD_BASIC, CATEGORY_ADJECTIVES},
            {"external", "harici", CATEGORY_ADJECTIVES}, {"familiar", "tanidik", CATEGORY_ADJECTIVES}, {"financial", "finansal", CATEGORY_ADJECTIVES}, {"frequent", "sik", CATEGORY_ADJECTIVES},
            {"internal", "dahili", CATEGORY_ADJECTIVES}, {"logical", "mantikli", CATEGORY_ADJECTIVES}, {"massive", "devasa", CATEGORY_ADJECTIVES}, {"medical", "tibbi", CATEGORY_ADJECTIVES},
            {"minimum", "asgari", CATEGORY_ADJECTIVES}, {"negative", "olumsuz", CATEGORY_ADJECTIVES}, {"obvious", "bariz", CATEGORY_ADJECTIVES}, {"ordinary", "siradan", CATEGORY_ADJECTIVES},
            {"positive", "olumlu", CATEGORY_ADJECTIVES}, {"previous", "onceki", CATEGORY_ADJECTIVES}, {"primary", "birincil", CATEGORY_ADJECTIVES}, {"reliable", "guvenilir", CATEGORY_ADJECTIVES},
            {"remote", "uzak", CATEGORY_ADJECTIVES}, {"suitable", "uygun", CATEGORY_ADJECTIVES}, {"technical", "teknik", CATEGORY_ADJECTIVES}, {"visible", "görunur", CATEGORY_ADJECTIVES}
    };
}

