package com.example.kelimeezberleme;

import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class SeedWordCatalog {
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
            case "equipment":
            case "example":
            case "exercise":
            case "experiment":
            case "extension":
            case "facility":
            case "festival":
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
            case "instruction":
            case "instrument":
            case "internet":
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
            case "platform":
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

        for (String[] v : VERBS) add(words, seen, cap(v[0]), v[1], "Fiiller");
        for (String[] a : ADJECTIVES) add(words, seen, cap(a[0]), a[1], "Sifatlar");
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
            {"describe", "tanımlamak"}, {"design", "tasarlamak"}, {"discover", "kesfetmek"}, {"discuss", "tartismak"},
            {"earn", "kazanmak"}, {"educate", "egitmek"}, {"encourage", "cesaretlendirmek"}, {"examine", "incelemek"},
            {"explain", "aciklamak"}, {"explore", "kesfetmek"}, {"fail", "basarisiz olmak"}, {"follow", "takip etmek"},
            {"forgive", "affetmek"}, {"gather", "toplanmak"}, {"handle", "yonetmek"}, {"imagine", "hayal etmek"},
            {"improve", "gelistirmek"}, {"include", "dahil etmek"}, {"increase", "artirmak"}, {"inform", "bilgilendirmek"},
            {"invite", "davet etmek"}, {"join", "katilmak"}, {"manage", "yonetmek"}, {"measure", "olcmek"},
            {"mention", "bahsetmek"}, {"notice", "fark etmek"}, {"observe", "gozlemlemek"}, {"organize", "duzenlemek"},
            {"prepare", "hazirlamak"}, {"prevent", "onlemek"}, {"protect", "korumak"}, {"provide", "saglamak"},
            {"receive", "almak"}, {"reduce", "azaltmak"}, {"remember", "hatirlamak"}, {"repair", "tamir etmek"},
            {"repeat", "tekrarlamak"}, {"replace", "degistirmek"}, {"reply", "yanitlamak"}, {"respect", "saygi duymak"},
            {"return", "geri donmek"}, {"save", "kaydetmek"}, {"search", "aramak"}, {"select", "secmek"},
            {"separate", "ayirmak"}, {"solve", "cozmek"}, {"support", "desteklemek"}, {"survive", "hayatta kalmak"},
            {"travel", "seyahat etmek"}, {"understand", "anlamak"}, {"update", "guncellemek"}, {"visit", "ziyaret etmek"},
            {"wait", "beklemek"}, {"wonder", "merak etmek"}, {"worry", "endişelenmek"}, {"compare", "kiyaslamak"},
            {"develop", "gelistirmek"}, {"divide", "bolmek"}, {"estimate", "tahmin etmek"}, {"identify", "tanımlamak"},
            {"introduce", "tanitmak"}, {"maintain", "surdurmek"}, {"operate", "calistirmak"}, {"perform", "yerine getirmek"},
            {"publish", "yayinlamak"}, {"recognize", "tanımak"}, {"recommend", "onermek"}, {"record", "kaydetmek"},
            {"remove", "kaldirmak"}, {"respond", "yanit vermek"}, {"review", "gozden gecirmek"}, {"schedule", "planlamak"},
            {"translate", "cevirmek"}, {"transport", "tasimak"}, {"verify", "dogrulamak"}, {"volunteer", "gonullu olmak"},
            {"whisper", "fisildamak"}, {"impress", "etkilemek"}, {"deliver", "ulastirmak"}, {"decorate", "suslemek"},
            {"adapt", "uyarlamak"}, {"adjust", "ayarlamak"}, {"announce", "duyurmak"}, {"apologize", "ozur dilemek"},
            {"apply", "basvurmak"}, {"argue", "tartismak"}, {"arrange", "duzenlemek"}, {"attach", "eklemek"},
            {"behave", "davranmak"}, {"belong", "ait olmak"}, {"cancel", "iptal etmek"}, {"capture", "yakalamak"},
            {"celebrate", "kutlamak"}, {"combine", "birleştirmek"}, {"command", "emretmek"}, {"communicate", "iletisim kurmak"},
            {"complain", "sikayet etmek"}, {"confirm", "onaylamak"}, {"contact", "iletisim kurmak"}, {"contain", "icermek"},
            {"copy", "kopyalamak"}, {"correct", "duzeltmek"}, {"damage", "zarar vermek"}, {"depend", "bagli olmak"},
            {"display", "gostermek"}, {"download", "indirmek"}, {"employ", "ise almak"}, {"enable", "etkinlestirmek"},
            {"enter", "girmek"}, {"escape", "kacmak"}, {"export", "disa aktarmak"}, {"express", "ifade etmek"},
            {"extend", "uzatmak"}, {"fill", "doldurmak"}, {"filter", "filtrelemek"}, {"finish", "bitirmek"},
            {"graduate", "mezun olmak"}, {"highlight", "vurgulamak"}, {"import", "ice aktarmak"}, {"install", "kurmak"},
            {"interview", "gorusme yapmak"}, {"invent", "icat etmek"}, {"invest", "yatirim yapmak"}, {"label", "etiketlemek"},
            {"locate", "yerini bulmak"}, {"lock", "kilitlemek"}, {"monitor", "izlemek"}, {"negotiate", "pazarlik etmek"},
            {"pack", "paketlemek"}, {"participate", "katilmak"}, {"pause", "duraklatmak"}, {"prefer", "tercih etmek"},
            {"print", "yazdirmak"}, {"process", "islemek"}, {"program", "programlamak"}, {"promise", "soz vermek"},
            {"raise", "yukseltmek"}, {"release", "serbest birakmak"}, {"request", "istemek"}, {"reserve", "ayirtmak"},
            {"restart", "yeniden baslatmak"}, {"share", "paylasmak"}, {"sign", "imzalamak"}, {"submit", "teslim etmek"},
            {"suggest", "onermek"}, {"upload", "yuklemek"}
    };

    private static final String[][] ADJECTIVES = {
            {"able", "yetenekli"}, {"accurate", "dogru"}, {"active", "aktif"}, {"ancient", "antik"}, {"basic", "temel"},
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
            {"ability", "yetenek", "Soyut"}, {"accident", "kaza", "Genel"}, {"address", "adres", "Yer"}, {"answer", "cevap", "Genel"},
            {"area", "alan", "Yer"}, {"artist", "sanatci", "Sanat"}, {"attention", "dikkat", "Soyut"}, {"battery", "pil", "Teknoloji"},
            {"business", "is", "Is Dunyasi"}, {"camera", "kamera", "Teknoloji"}, {"capital", "baskent", "Yer"}, {"choice", "secim", "Soyut"},
            {"culture", "kultur", "Sosyal"}, {"decision", "karar", "Soyut"}, {"energy", "enerji", "Doga"}, {"engine", "motor", "Teknoloji"},
            {"example", "ornek", "Egitim"}, {"exercise", "egzersiz", "Saglik"}, {"factory", "fabrika", "Is Dunyasi"}, {"garden", "bahce", "Doga"},
            {"history", "tarih", "Egitim"}, {"hospital", "hastane", "Saglik"}, {"internet", "internet", "Teknoloji"}, {"journey", "yolculuk", "Ulasim"},
            {"knowledge", "bilgi", "Egitim"}, {"library", "kutuphane", "Egitim"}, {"machine", "makine", "Teknoloji"}, {"market", "pazar", "Ekonomi"},
            {"memory", "hafiza", "Soyut"}, {"message", "mesaj", "Teknoloji"}, {"mountain", "dag", "Doga"}, {"neighbor", "komsu", "Sosyal"},
            {"opinion", "fikir", "Soyut"}, {"problem", "sorun", "Genel"}, {"project", "proje", "Is Dunyasi"}, {"quality", "kalite", "Soyut"},
            {"question", "soru", "Egitim"}, {"reason", "neden", "Soyut"}, {"research", "arastirma", "Egitim"}, {"result", "sonuc", "Genel"},
            {"science", "bilim", "Egitim"}, {"service", "hizmet", "Is Dunyasi"}, {"society", "toplum", "Sosyal"}, {"station", "istasyon", "Ulasim"},
            {"system", "sistem", "Teknoloji"}, {"teacher", "ogretmen", "Egitim"}, {"traffic", "trafik", "Ulasim"}, {"weather", "hava durumu", "Doga"}
    };

    private static final String[][] GENERAL_WORDS = {
            {"airport", "havalimani", "Ulasim"}, {"apartment", "daire", "Ev"}, {"appointment", "randevu", "Saglik"}, {"argument", "tartisma", "Sosyal"},
            {"audience", "seyirci", "Sanat"}, {"backpack", "sirt cantasi", "Egitim"}, {"bakery", "firin", "Yiyecek"}, {"bedroom", "yatak odasi", "Ev"},
            {"biology", "biyoloji", "Egitim"}, {"blanket", "battaniye", "Ev"}, {"calendar", "takvim", "Zaman"}, {"candidate", "aday", "Is Dunyasi"},
            {"capacity", "kapasite", "Genel"}, {"ceremony", "toren", "Sosyal"}, {"champion", "sampiyon", "Spor"}, {"chemical", "kimyasal", "Egitim"},
            {"climate", "iklim", "Doga"}, {"company", "sirket", "Is Dunyasi"}, {"conference", "konferans", "Is Dunyasi"}, {"connection", "baglanti", "Teknoloji"},
            {"contract", "sozlesme", "Is Dunyasi"}, {"customer", "musteri", "Ekonomi"}, {"database", "veritabani", "Teknoloji"}, {"department", "bolum", "Is Dunyasi"},
            {"dictionary", "sozluk", "Egitim"}, {"direction", "yon", "Yer"}, {"document", "belge", "Is Dunyasi"}, {"education", "egitim", "Egitim"},
            {"elevator", "asansor", "Ev"}, {"employee", "calisan", "Is Dunyasi"}, {"equipment", "ekipman", "Teknoloji"}, {"evidence", "kanit", "Genel"},
            {"experience", "deneyim", "Soyut"}, {"experiment", "deney", "Egitim"}, {"furniture", "mobilya", "Ev"}, {"government", "hukumet", "Sosyal"},
            {"grocery", "market urunu", "Yiyecek"}, {"headline", "manset", "Genel"}, {"identity", "kimlik", "Sosyal"}, {"industry", "endustri", "Is Dunyasi"},
            {"information", "bilgi", "Egitim"}, {"instruction", "talimat", "Egitim"}, {"insurance", "sigorta", "Ekonomi"}, {"interview", "gorusme", "Is Dunyasi"},
            {"investment", "yatirim", "Ekonomi"}, {"keyboard", "klavye", "Teknoloji"}, {"laboratory", "laboratuvar", "Egitim"}, {"landscape", "manzara", "Doga"},
            {"location", "konum", "Yer"}, {"medicine", "ilac", "Saglik"}, {"membership", "uyelik", "Sosyal"}, {"microscope", "mikroskop", "Egitim"},
            {"navigation", "navigasyon", "Ulasim"}, {"newspaper", "gazete", "Genel"}, {"notebook", "defter", "Egitim"}, {"operation", "operasyon", "Saglik"},
            {"organization", "kurulus", "Is Dunyasi"}, {"passenger", "yolcu", "Ulasim"}, {"password", "sifre", "Teknoloji"}, {"payment", "odeme", "Ekonomi"},
            {"permission", "izin", "Genel"}, {"photograph", "fotograf", "Sanat"}, {"population", "nufus", "Sosyal"}, {"president", "baskan", "Sosyal"},
            {"pressure", "basinc", "Doga"}, {"printer", "yazici", "Teknoloji"}, {"property", "mulkiyet", "Ekonomi"}, {"reception", "resepsiyon", "Is Dunyasi"},
            {"refrigerator", "buzdolabi", "Ev"}, {"relationship", "iliski", "Sosyal"}, {"restaurant", "restoran", "Yiyecek"}, {"schedule", "program", "Zaman"},
            {"signature", "imza", "Is Dunyasi"}, {"software", "yazilim", "Teknoloji"}, {"solution", "cozum", "Genel"}, {"strategy", "strateji", "Is Dunyasi"},
            {"temperature", "sicaklik", "Doga"}, {"tradition", "gelenek", "Sosyal"}, {"university", "universite", "Egitim"}, {"vegetable", "sebze", "Yiyecek"},
            {"vocabulary", "kelime hazinesi", "Egitim"}, {"warehouse", "depo", "Is Dunyasi"}, {"workshop", "atolye", "Is Dunyasi"}, {"zoology", "zooloji", "Egitim"},
            {"banana", "muz", "Meyveler"}, {"orange", "portakal", "Meyveler"}, {"grape", "uzum", "Meyveler"}, {"strawberry", "cilek", "Meyveler"},
            {"cherry", "kiraz", "Meyveler"}, {"peach", "seftali", "Meyveler"}, {"pear", "armut", "Meyveler"}, {"watermelon", "karpuz", "Meyveler"},
            {"joy", "nese", "Duygular"}, {"anger", "ofke", "Duygular"}, {"fear", "korku", "Duygular"}, {"hope", "umut", "Duygular"},
            {"pride", "gurur", "Duygular"}, {"sadness", "uzuntu", "Duygular"},
            {"athlete", "sporcu", "Spor"}, {"coach", "antrenor", "Spor"}, {"stadium", "stadyum", "Spor"}, {"training", "antrenman", "Spor"},
            {"victory", "zafer", "Spor"},
            {"wood", "ahsap", "Malzemeler"}, {"plastic", "plastik", "Malzemeler"}, {"fabric", "kumas", "Malzemeler"}, {"stone", "tas", "Malzemeler"},
            {"circle", "daire", "Sekiller"}, {"square", "kare", "Sekiller"}, {"triangle", "ucgen", "Sekiller"}, {"rectangle", "dikdortgen", "Sekiller"},
            {"abstract", "soyut", "Sifatlar"}, {"academic", "akademik", "Sifatlar"}, {"advanced", "ileri", "Sifatlar"}, {"affordable", "uygun fiyatli", "Sifatlar"},
            {"automatic", "otomatik", "Sifatlar"}, {"available", "mevcut", "Sifatlar"}, {"comfortable", "rahat", "Sifatlar"}, {"confident", "ozguvenli", "Sifatlar"},
            {"consistent", "tutarli", "Sifatlar"}, {"convenient", "elverisli", "Sifatlar"}, {"critical", "kritik", "Sifatlar"}, {"delicious", "lezzetli", "Sifatlar"},
            {"efficient", "verimli", "Sifatlar"}, {"electric", "elektrikli", "Sifatlar"}, {"emotional", "duygusal", "Sifatlar"}, {"excellent", "mukemmel", "Sifatlar"},
            {"expensive", "pahali", "Sifatlar"}, {"flexible", "esnek", "Sifatlar"}, {"independent", "bagimsiz", "Sifatlar"}, {"necessary", "gerekli", "Sifatlar"},
            {"official", "resmi", "Sifatlar"}, {"practical", "pratik", "Sifatlar"}, {"professional", "profesyonel", "Sifatlar"}, {"reasonable", "makul", "Sifatlar"},
            {"responsible", "sorumlu", "Sifatlar"}, {"sensitive", "hassas", "Sifatlar"}, {"temporary", "gecici", "Sifatlar"}, {"traditional", "geleneksel", "Sifatlar"},
            {"communicate", "iletisim kurmak", "Fiiller"}, {"coordinate", "koordine etmek", "Fiiller"}, {"demonstrate", "gostermek", "Fiiller"},
            {"emphasize", "vurgulamak", "Fiiller"}, {"estimate", "tahmin etmek", "Fiiller"}, {"evaluate", "degerlendirmek", "Fiiller"},
            {"generate", "uretmek", "Fiiller"}, {"implement", "uygulamak", "Fiiller"}, {"investigate", "arastirmak", "Fiiller"},
            {"participate", "katilmak", "Fiiller"}, {"prioritize", "oncelik vermek", "Fiiller"}, {"recommend", "onermek", "Fiiller"},
            {"represent", "temsil etmek", "Fiiller"}, {"specialize", "uzmanlasmak", "Fiiller"}, {"strengthen", "guclendirmek", "Fiiller"},
            {"account", "hesap", "Ekonomi"}, {"achievement", "basari", "Soyut"}, {"activity", "etkinlik", "Genel"}, {"adventure", "macera", "Sosyal"},
            {"agreement", "anlasma", "Is Dunyasi"}, {"analysis", "analiz", "Egitim"}, {"application", "uygulama", "Teknoloji"}, {"architecture", "mimari", "Sanat"},
            {"assistant", "asistan", "Is Dunyasi"}, {"atmosphere", "atmosfer", "Doga"}, {"behavior", "davranis", "Sosyal"}, {"benefit", "fayda", "Ekonomi"},
            {"boundary", "sinir", "Yer"}, {"breakfast", "kahvalti", "Yiyecek"}, {"building", "bina", "Yer"}, {"cabinet", "dolap", "Ev"},
            {"campaign", "kampanya", "Is Dunyasi"}, {"certificate", "sertifika", "Egitim"}, {"challenge", "zorluk", "Soyut"}, {"character", "karakter", "Sosyal"},
            {"chemistry", "kimya", "Egitim"}, {"childhood", "cocukluk", "Zaman"}, {"classroom", "sinif", "Egitim"}, {"community", "topluluk", "Sosyal"},
            {"competition", "yarısma", "Spor"}, {"component", "bilesen", "Teknoloji"}, {"concept", "kavram", "Soyut"}, {"condition", "durum", "Genel"},
            {"consequence", "sonuc", "Soyut"}, {"conversation", "konusma", "Sosyal"}, {"courtyard", "avlu", "Yer"}, {"currency", "para birimi", "Ekonomi"},
            {"deadline", "son tarih", "Zaman"}, {"delivery", "teslimat", "Is Dunyasi"}, {"destination", "varis noktasi", "Ulasim"}, {"development", "gelisim", "Soyut"},
            {"difference", "fark", "Soyut"}, {"difficulty", "zorluk", "Soyut"}, {"director", "yonetmen", "Sanat"}, {"discovery", "kesif", "Egitim"},
            {"discussion", "tartisma", "Sosyal"}, {"distance", "mesafe", "Yer"}, {"economy", "ekonomi", "Ekonomi"}, {"effort", "caba", "Soyut"},
            {"electricity", "elektrik", "Teknoloji"}, {"emergency", "acil durum", "Saglik"}, {"environment", "cevre", "Doga"}, {"equipment", "ekipman", "Teknoloji"},
            {"expression", "ifade", "Sosyal"}, {"extension", "uzanti", "Teknoloji"}, {"facility", "tesis", "Yer"}, {"failure", "basarisizlik", "Soyut"},
            {"feedback", "geri bildirim", "Is Dunyasi"}, {"festival", "festival", "Sanat"}, {"foundation", "temel", "Soyut"}, {"freedom", "ozgurluk", "Soyut"},
            {"function", "islev", "Teknoloji"}, {"generation", "nesil", "Sosyal"}, {"guidance", "rehberlik", "Egitim"}, {"headquarters", "merkez ofis", "Is Dunyasi"},
            {"heritage", "miras", "Sosyal"}, {"household", "ev halki", "Ev"}, {"imagination", "hayal gucu", "Soyut"}, {"improvement", "iyilesme", "Soyut"},
            {"independence", "bagimsizlik", "Soyut"}, {"influence", "etki", "Sosyal"}, {"ingredient", "malzeme", "Yiyecek"}, {"initiative", "girişim", "Is Dunyasi"},
            {"inspiration", "ilham", "Soyut"}, {"instruction", "talimat", "Egitim"}, {"instrument", "enstruman", "Sanat"}, {"intelligence", "zeka", "Soyut"},
            {"journal", "gunluk", "Egitim"}, {"landmark", "simgesel yer", "Yer"}, {"leadership", "liderlik", "Is Dunyasi"}, {"lecture", "ders", "Egitim"},
            {"lifestyle", "yasam tarzi", "Saglik"}, {"literature", "edebiyat", "Sanat"}, {"maintenance", "bakim", "Teknoloji"}, {"management", "yonetim", "Is Dunyasi"},
            {"manufacturer", "uretici", "Is Dunyasi"}, {"material", "malzeme", "Malzemeler"}, {"measurement", "olcum", "Egitim"}, {"mechanism", "mekanizma", "Teknoloji"},
            {"movement", "hareket", "Saglik"}, {"negotiation", "muzakere", "Is Dunyasi"}, {"network", "ag", "Teknoloji"}, {"obligation", "zorunluluk", "Soyut"},
            {"opportunity", "firsat", "Is Dunyasi"}, {"orientation", "oryantasyon", "Egitim"}, {"participant", "katilimci", "Sosyal"}, {"partnership", "ortaklik", "Is Dunyasi"},
            {"performance", "performans", "Sanat"}, {"perspective", "bakis acisi", "Soyut"}, {"pharmacy", "eczane", "Saglik"}, {"platform", "platform", "Teknoloji"},
            {"preference", "tercih", "Soyut"}, {"presentation", "sunum", "Is Dunyasi"}, {"priority", "oncelik", "Is Dunyasi"}, {"procedure", "prosedur", "Is Dunyasi"},
            {"production", "uretim", "Is Dunyasi"}, {"profession", "meslek", "Is Dunyasi"}, {"proposal", "teklif", "Is Dunyasi"}, {"publication", "yayin", "Egitim"},
            {"recognition", "taninma", "Sosyal"}, {"recommendation", "onerı", "Is Dunyasi"}, {"reference", "referans", "Egitim"}, {"reflection", "yansima", "Soyut"},
            {"reliability", "guvenilirlik", "Soyut"}, {"replacement", "yedek", "Genel"}, {"requirement", "gereksinim", "Is Dunyasi"}, {"reservation", "rezervasyon", "Sosyal"},
            {"resource", "kaynak", "Genel"}, {"responsibility", "sorumluluk", "Soyut"}, {"satisfaction", "memnuniyet", "Duygular"}, {"selection", "secim", "Genel"},
            {"shelter", "barinak", "Yer"}, {"shipment", "sevkiyat", "Is Dunyasi"}, {"specialist", "uzman", "Is Dunyasi"}, {"statement", "aciklama", "Genel"},
            {"storage", "depolama", "Teknoloji"}, {"structure", "yapi", "Genel"}, {"substance", "madde", "Malzemeler"}, {"suggestion", "oneri", "Genel"},
            {"supervisor", "amir", "Is Dunyasi"}, {"technique", "teknik", "Egitim"}, {"technology", "teknoloji", "Teknoloji"}, {"tournament", "turnuva", "Spor"},
            {"translation", "ceviri", "Egitim"}, {"transportation", "ulasim", "Ulasim"}, {"treatment", "tedavi", "Saglik"}, {"vacation", "tatil", "Zaman"},
            {"variation", "cesitlilik", "Genel"}, {"vehicle", "arac", "Ulasim"}, {"version", "surum", "Teknoloji"}, {"volunteer", "gonullu", "Sosyal"},
            {"warranty", "garanti", "Ekonomi"}, {"wildlife", "yaban hayati", "Doga"}, {"workflow", "is akisi", "Is Dunyasi"}, {"workplace", "isyeri", "Is Dunyasi"},
            {"abandon", "terk etmek", "Fiiller"}, {"absorb", "emmek", "Fiiller"}, {"accelerate", "hizlandirmak", "Fiiller"}, {"accompany", "eslik etmek", "Fiiller"},
            {"accuse", "suclamak", "Fiiller"}, {"activate", "etkinlestirmek", "Fiiller"}, {"admire", "hayran olmak", "Fiiller"}, {"adopt", "benimsemek", "Fiiller"},
            {"advertise", "reklam yapmak", "Fiiller"}, {"analyze", "analiz etmek", "Fiiller"}, {"approve", "onaylamak", "Fiiller"}, {"assemble", "bir araya getirmek", "Fiiller"},
            {"assign", "atamak", "Fiiller"}, {"broadcast", "yayinlamak", "Fiiller"}, {"clarify", "netlestirmek", "Fiiller"}, {"classify", "siniflandirmak", "Fiiller"},
            {"collaborate", "is birligi yapmak", "Fiiller"}, {"compete", "yarismak", "Fiiller"}, {"concentrate", "odaklanmak", "Fiiller"}, {"conclude", "sonuclandirmak", "Fiiller"},
            {"conduct", "yurutmek", "Fiiller"}, {"construct", "insa etmek", "Fiiller"}, {"contribute", "katkida bulunmak", "Fiiller"}, {"convince", "ikna etmek", "Fiiller"},
            {"criticize", "elestirmek", "Fiiller"}, {"decrease", "azalmak", "Fiiller"}, {"define", "tanımlamak", "Fiiller"}, {"detect", "tespit etmek", "Fiiller"},
            {"determine", "belirlemek", "Fiiller"}, {"distribute", "dagitmak", "Fiiller"}, {"donate", "bagislamak", "Fiiller"}, {"eliminate", "ortadan kaldirmak", "Fiiller"},
            {"establish", "kurmak", "Fiiller"}, {"expand", "genisletmek", "Fiiller"}, {"forecast", "tahmin etmek", "Fiiller"}, {"hesitate", "tereddut etmek", "Fiiller"},
            {"illustrate", "gostermek", "Fiiller"}, {"interpret", "yorumlamak", "Fiiller"}, {"launch", "baslatmak", "Fiiller"}, {"motivate", "motive etmek", "Fiiller"},
            {"preserve", "korumak", "Fiiller"}, {"purchase", "satin almak", "Fiiller"}, {"qualify", "nitelendirmek", "Fiiller"}, {"rebuild", "yeniden yapmak", "Fiiller"},
            {"recover", "iyilesmek", "Fiiller"}, {"recycle", "geri donusturmek", "Fiiller"}, {"reform", "duzenlemek", "Fiiller"}, {"register", "kayit olmak", "Fiiller"},
            {"relax", "rahatlamak", "Fiiller"}, {"remind", "hatirlatmak", "Fiiller"}, {"renovate", "yenilemek", "Fiiller"}, {"rescue", "kurtarmak", "Fiiller"},
            {"restore", "geri yuklemek", "Fiiller"}, {"satisfy", "memnun etmek", "Fiiller"}, {"simulate", "benzetmek", "Fiiller"}, {"stabilize", "dengelemek", "Fiiller"},
            {"subscribe", "abone olmak", "Fiiller"}, {"transform", "donusturmek", "Fiiller"}, {"upgrade", "yukseltmek", "Fiiller"}, {"validate", "dogrulamak", "Fiiller"},
            {"accurate", "dogru", "Sifatlar"}, {"additional", "ek", "Sifatlar"}, {"adequate", "yeterli", "Sifatlar"}, {"aggressive", "saldirgan", "Sifatlar"},
            {"ambitious", "hirsli", "Sifatlar"}, {"appropriate", "uygun", "Sifatlar"}, {"artificial", "yapay", "Sifatlar"}, {"attractive", "cekici", "Sifatlar"},
            {"balanced", "dengeli", "Sifatlar"}, {"brilliant", "parlak", "Sifatlar"}, {"capable", "yetenekli", "Sifatlar"}, {"complex", "karmasik", "Sifatlar"},
            {"constant", "sabit", "Sifatlar"}, {"cultural", "kulturel", "Sifatlar"}, {"digital", "dijital", "Sifatlar"}, {"dramatic", "dramatik", "Sifatlar"},
            {"dynamic", "dinamik", "Sifatlar"}, {"effective", "etkili", "Sifatlar"}, {"enormous", "kocaman", "Sifatlar"}, {"essential", "temel", "Sifatlar"},
            {"external", "harici", "Sifatlar"}, {"familiar", "tanidik", "Sifatlar"}, {"financial", "finansal", "Sifatlar"}, {"frequent", "sik", "Sifatlar"},
            {"internal", "dahili", "Sifatlar"}, {"logical", "mantikli", "Sifatlar"}, {"massive", "devasa", "Sifatlar"}, {"medical", "tibbi", "Sifatlar"},
            {"minimum", "asgari", "Sifatlar"}, {"negative", "olumsuz", "Sifatlar"}, {"obvious", "bariz", "Sifatlar"}, {"ordinary", "siradan", "Sifatlar"},
            {"positive", "olumlu", "Sifatlar"}, {"previous", "onceki", "Sifatlar"}, {"primary", "birincil", "Sifatlar"}, {"reliable", "guvenilir", "Sifatlar"},
            {"remote", "uzak", "Sifatlar"}, {"suitable", "uygun", "Sifatlar"}, {"technical", "teknik", "Sifatlar"}, {"visible", "görunur", "Sifatlar"}
    };
}
