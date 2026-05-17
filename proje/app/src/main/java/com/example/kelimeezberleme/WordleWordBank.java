package com.example.kelimeezberleme;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

final class WordleWordBank {
    private static final Random RANDOM = new Random();

    private WordleWordBank() {
    }

    static String pickRandomWord(List<Word> allWords, Set<String> eligibleWordIds) {
        List<String> pool = buildPool(allWords, eligibleWordIds, false);
        if (pool.isEmpty()) {
            pool = buildPool(allWords, null, true);
        }
        if (pool.isEmpty()) {
            return null;
        }
        return pool.get(RANDOM.nextInt(pool.size())).toUpperCase(Locale.US);
    }

    static boolean containsGuess(List<Word> allWords, String guess) {
        if (guess == null) {
            return false;
        }
        String normalizedGuess = guess.trim().toLowerCase(Locale.US);
        if (normalizedGuess.length() != 5) {
            return false;
        }
        return buildBank(allWords, null, true).contains(normalizedGuess);
    }

    static List<String> extraWords() {
        List<String> words = new ArrayList<>(EXTRA_COMMON_WORDS.length);
        for (String word : EXTRA_COMMON_WORDS) {
            String normalized = normalize(word);
            if (normalized.length() == 5) {
                words.add(normalized);
            }
        }
        return words;
    }

    static List<Word> mergeDisplayWords(List<Word> allWords) {
        List<Word> merged = new ArrayList<>();
        if (allWords != null) {
            merged.addAll(allWords);
        }

        Set<String> existing = new LinkedHashSet<>();
        for (Word word : merged) {
            if (word != null && word.eng != null) {
                existing.add(normalize(word.eng));
            }
        }

        int syntheticId = -1;
        for (String extra : extraWords()) {
            if (!existing.add(extra)) {
                continue;
            }
            merged.add(new Word(
                    syntheticId--,
                    capitalize(extra),
                    "Wordle için eklenen kelime",
                    SeedWordCatalog.pictureRefForWord(extra, "Wordle"),
                    0,
                    Long.MAX_VALUE,
                    "Wordle",
                    0,
                    0
            ));
        }
        return merged;
    }

    static List<String> previewSamples(String word) {
        String normalized = normalize(word);
        if (normalized.isEmpty()) {
            return new ArrayList<>();
        }

        String[] templates = {
                "We practiced %s in a short word drill.",
                "She wrote %s on a flash card for review.",
                "The class remembered %s after a quick example.",
                "He saw %s in a simple practice sentence."
        };

        int sampleCount = Math.abs(normalized.hashCode()) % 2 == 0 ? 1 : 2;
        int start = Math.abs(normalized.hashCode()) % templates.length;
        List<String> samples = new ArrayList<>(sampleCount);
        for (int i = 0; i < sampleCount; i++) {
            String template = templates[(start + i) % templates.length];
            samples.add(String.format(Locale.US, template, normalized));
        }
        return samples;
    }

    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.substring(0, 1).toUpperCase(Locale.US) + value.substring(1);
    }

    private static List<String> buildPool(List<Word> allWords, Set<String> eligibleWordIds, boolean includeExtras) {
        return new ArrayList<>(buildBank(allWords, eligibleWordIds, includeExtras));
    }

    private static LinkedHashSet<String> buildBank(List<Word> allWords, Set<String> eligibleWordIds, boolean includeExtras) {
        LinkedHashSet<String> bank = new LinkedHashSet<>();
        Set<Integer> eligibleIds = parseEligibleIds(eligibleWordIds);
        boolean filterByEligible = eligibleIds != null && !eligibleIds.isEmpty();

        if (allWords != null) {
            for (Word word : allWords) {
                if (word == null || word.eng == null) {
                    continue;
                }
                if (filterByEligible && !eligibleIds.contains(word.id)) {
                    continue;
                }
                addWord(bank, word.eng);
                addDerivedForms(bank, word.eng);
            }
        }

        if (includeExtras) {
            for (String word : EXTRA_COMMON_WORDS) {
                addWord(bank, word);
            }
        }

        return bank;
    }

    private static Set<Integer> parseEligibleIds(Set<String> eligibleWordIds) {
        if (eligibleWordIds == null || eligibleWordIds.isEmpty()) {
            return null;
        }

        Set<Integer> ids = new LinkedHashSet<>();
        for (String id : eligibleWordIds) {
            if (id == null) {
                continue;
            }
            try {
                ids.add(Integer.parseInt(id.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    private static void addDerivedForms(LinkedHashSet<String> bank, String english) {
        String word = normalize(english);
        int length = word.length();
        if (length == 0) {
            return;
        }

        if (length == 3) {
            addWord(bank, word + "ed");
            addWord(bank, word + "es");
            return;
        }

        if (length == 4) {
            addWord(bank, word + "s");
            if (word.endsWith("e")) {
                addWord(bank, word + "d");
            }
            return;
        }

        if (length == 5 && word.endsWith("e")) {
            addWord(bank, word + "s");
        }
    }

    private static void addWord(LinkedHashSet<String> bank, String candidate) {
        String normalized = normalize(candidate);
        if (normalized.length() == 5) {
            bank.add(normalized);
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.US).replaceAll("[^a-z]", "");
    }

    private static final String[] EXTRA_COMMON_WORDS = {
            "aback", "abase", "abate", "abbey", "abbot", "abhor", "abide", "abler", "abode", "abuse",
            "acorn", "acrid", "acted", "actor", "acute", "adapt", "added", "adder", "adept", "admit",
            "adobe", "adopt", "adore", "adult", "affix", "afire", "afoot", "again", "agent", "agile",
            "aging", "aglow", "agony", "agree", "ahead", "aisle", "alarm", "album", "alert", "alien",
            "alike", "alive", "allow", "alloy", "alone", "along", "aloof", "alpha", "altar", "amass",
            "amaze", "amber", "amend", "amiss", "amity", "among", "ample", "amuse", "angel", "anger",
            "angle", "angry", "anime", "ankle", "annex", "annoy", "annul", "antic", "anvil", "apart",
            "aping", "apple", "apply", "apron", "arbor", "arise", "armed", "armor", "aroma", "arrow",
            "aside", "asked", "askew", "asset", "atlas", "atoll", "attic", "audio", "audit", "avail",
            "awake", "award", "aware", "awful", "axial", "axiom", "bacon", "badge", "badly", "baker",
            "balmy", "banal", "banjo", "barge", "baron", "basic", "basin", "basis", "batch", "bathe",
            "baton", "beach", "beard", "beast", "beech", "began", "begin", "belly", "below", "bench",
            "berry", "birth", "bison", "black", "blade", "blame", "blank", "blare", "blast", "blaze",
            "bleak", "bleed", "blend", "bless", "blind", "blink", "block", "blond", "blood", "bloom",
            "blown", "blows", "blunt", "blush", "board", "boast", "bobby", "bogus", "boils", "bonus",
            "booth", "boost", "boots", "borax", "bored", "bores", "borne", "bosom", "bough", "bound",
            "bowed", "bowl", "boxed", "boxer", "brace", "brain", "brand", "brave", "brawl", "bread",
            "break", "breed", "brief", "bring", "brisk", "broad", "broke", "brook", "broom", "broth",
            "brown", "brush", "brute", "build", "built", "bulge", "bulky", "bunch", "burly", "burns",
            "burst", "cabin", "cable", "cacao", "cache", "cadet", "caged", "cages", "camel", "candy",
            "canoe", "canon", "caper", "cared", "cargo", "carol", "carry", "carve", "caste", "catch",
            "cause", "cedar", "chafe", "chain", "chair", "chalk", "champ", "chant", "charm", "chart",
            "chase", "cheap", "cheat", "check", "cheek", "cheer", "chess", "chest", "chide", "chief",
            "child", "chill", "chime", "china", "choir", "choke", "chord", "cider", "cigar", "cinch",
            "claim", "clamp", "clash", "clasp", "class", "clean", "clear", "clerk", "click", "cliff",
            "climb", "cling", "cloak", "clock", "clone", "close", "cloth", "cloud", "clown", "coach",
            "coast", "cobra", "cocoa", "comic", "count", "court", "coven", "craft", "crane", "crash",
            "crate", "crave", "crawl", "craze", "cream", "creed", "creek", "creep", "crime", "crisp",
            "crook", "cross", "crowd", "crown", "crude", "crumb", "crush", "curve", "cycle", "daily",
            "dance", "death", "delay", "delta", "demon", "depot", "dirty", "doubt", "dough", "dream",
            "drink", "drive", "earth", "eager", "eagle", "early", "entry", "equal", "error", "event",
            "every", "faith", "fancy", "feast", "fever", "fiber", "field", "final", "flame", "flare",
            "flash", "fleet", "flesh", "float", "flock", "floor", "flour", "flute", "focus", "force",
            "forge", "frame", "fresh", "fries", "front", "frost", "fruit", "giant", "glade", "glare",
            "glass", "glide", "globe", "grace", "grade", "grain", "grand", "grant", "grape", "graph",
            "grasp", "grass", "green", "greet", "grind", "grove", "guard", "guess", "guest", "guide",
            "habit", "happy", "harsh", "hasty", "heart", "heavy", "honey", "honor", "house", "human",
            "humor", "ideal", "image", "index", "ingot", "inner", "input", "issue", "jelly", "jewel",
            "joint", "judge", "knack", "kneel", "knife", "label", "large", "laser", "latch", "later",
            "laugh", "layer", "learn", "lemon", "light", "limit", "logic", "lunar", "lunch", "major",
            "march", "match", "maybe", "media", "metal", "meter", "mimic", "minor", "model", "money",
            "moral", "motor", "mount", "music", "nasty", "noble", "nurse", "oasis", "occur", "olive",
            "opera", "orbit", "order", "other", "panel", "paper", "party", "peace", "pearl", "piano",
            "piece", "pilot", "plain", "plane", "plant", "plate", "plaza", "point", "pride", "prime",
            "print", "prior", "prize", "probe", "punch", "queen", "quick", "quiet", "quote", "radar",
            "rainy", "ratio", "reach", "ready", "realm", "relay", "right", "rival", "river", "robot",
            "rough", "round", "royal", "salad", "scale", "scarf", "scout", "seize", "shade", "shake",
            "shape", "share", "sharp", "sheep", "shelf", "shine", "shock", "shout", "sight", "skill",
            "sleep", "smile", "smoke", "snake", "solid", "sound", "south", "space", "speak", "speed",
            "spice", "spike", "spoil", "sport", "stack", "stage", "stair", "stare", "state", "steam",
            "steel", "steep", "stick", "still", "stock", "store", "storm", "story", "stove", "strap",
            "straw", "strong", "study", "style", "sugar", "suite", "sweet", "swell", "sword", "table",
            "teach", "thank", "theme", "thing", "thick", "thief", "throw", "tight", "title", "toast",
            "trace", "track", "trade", "trail", "train", "treat", "trend", "trial", "trust", "truth",
            "twist", "union", "until", "urban", "value", "vapor", "video", "voice", "waste", "watch",
            "weary", "whale", "wheat", "wheel", "where", "white", "whole", "woman", "world", "worry",
            "worth", "wound", "write", "wrong", "young", "zesty"
    };
}
