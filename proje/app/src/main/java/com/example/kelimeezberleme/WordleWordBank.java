package com.example.kelimeezberleme;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class WordleWordBank {
    private static final SecureRandom RANDOM = new SecureRandom();

    private WordleWordBank() {
    }

    static String pickRandomWord(List<Word> allWords, Set<String> eligibleWordIds) {
        List<String> pool = buildPool(allWords, eligibleWordIds);
        if (pool.isEmpty() && eligibleWordIds != null && !eligibleWordIds.isEmpty()) {
            pool = buildPool(allWords, null);
        }
        if (pool.isEmpty()) {
            return null;
        }
        int randomIndex = RANDOM.nextInt(pool.size());
        return pool.get(randomIndex).toUpperCase(Locale.US);
    }

    static boolean containsGuess(List<Word> allWords, String guess) {
        if (guess == null) {
            return false;
        }
        String normalizedGuess = guess.trim().toLowerCase(Locale.US);
        if (normalizedGuess.length() != 5) {
            return false;
        }
        return buildBank(allWords, null).contains(normalizedGuess);
    }

    static List<Word> mergeDisplayWords(List<Word> allWords) {
        List<Word> merged = new ArrayList<>();
        if (allWords != null) {
            merged.addAll(allWords);
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

        int sampleCount = normalized.length() % 2 == 0 ? 1 : 2;
        int start = normalized.length() % templates.length;
        List<String> samples = new ArrayList<>(sampleCount);
        for (int i = 0; i < sampleCount; i++) {
            String template = templates[(start + i) % templates.length];
            samples.add(String.format(Locale.US, template, normalized));
        }
        return samples;
    }

    private static List<String> buildPool(List<Word> allWords, Set<String> eligibleWordIds) {
        return new ArrayList<>(buildBank(allWords, eligibleWordIds));
    }

    private static LinkedHashSet<String> buildBank(List<Word> allWords, Set<String> eligibleWordIds) {
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
}
