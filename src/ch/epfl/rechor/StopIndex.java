package ch.epfl.rechor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
/**
 * Provides auto-completion functionality for stop names.
 * Builds an index of stop names and aliases, and matches queries to stops with scoring.
 *
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 * @version 1.0
 * @since 1.0
 */
public final class StopIndex {
    /** Regex flags for case-insensitive and Unicode-aware matching of sub-queries. */
    private static final int FLAGS= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

    /** Map of base characters to regex character classes matching accented variants. */
    private static final Map<Character, String> EQUIVALENT_CHARS = Map.of(
            'a', "[aáàâä]",
            'e', "[eéèêë]",
            'i', "[iíìîï]",
            'o', "[oóòôö]",
            'u', "[uúùûü]",
            'c', "[cç]"
    );

    private final Map<String,String> newMap;

    /**
     * Constructs a StopIndex from a list of stop names and alias mappings.
     *
     * @param stopNames list of official stop names
     * @param aliases mapping of alias names to official stop names
     */
    public StopIndex(List<String> stopNames, Map<String,String> aliases){
        Map<String, String> map = new HashMap<>();

        for(String name : stopNames){
            map.put(name,name);
        }

        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            if (map.containsKey(entry.getValue())) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        this.newMap = Map.copyOf(map);


    }
    /**
     * Returns a list of stop names matching the given query, ordered by match score.
     *
     * @param query the search string to match against stop names
     * @param maxResults maximum number of matching results to return (must be >0)
     * @return list of matching stop names up to maxResults
     */
    public List<String> stopsMatching(String query, int maxResults){
        Preconditions.checkArgument(maxResults > 0);

        if (query == null || query.trim().isEmpty()) {
            return newMap.values().stream() // we extract all the main names
                    .distinct()
                    .sorted()               // alphabetic order
                    .limit(maxResults)
                    .collect(Collectors.toList());
        }

        String[] subQueries = Pattern.compile("[\\s,]+").split(query.trim());

        List<Pattern> patterns = Arrays.stream(subQueries)
                .filter(subQuery -> !subQuery.isEmpty())
                .map(this::buildPatternForSubQuery)
                .collect(Collectors.toList());

        if (patterns.isEmpty()) {
            return Collections.emptyList();
        }
        // 1. Calculates the scores for every (name, mainName)
        Map<String, Double> scores = new HashMap<>();

        for (Map.Entry<String,String> entry : newMap.entrySet()) {
            double score = computeScore(entry.getKey(), patterns);
            if (score > 0) {
                scores.merge(entry.getValue(), score, Math::max);
            }
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    /**
     * Builds a regex pattern for a sub-query, handling accents and case sensitivity.
     *
     * @param subQuery the sub-string to create a pattern for
     * @return a compiled Pattern for matching the sub-query
     */
    private Pattern buildPatternForSubQuery(String subQuery) {
        if (subQuery.chars().anyMatch(Character::isUpperCase)) {
            return Pattern.compile(Pattern.quote(subQuery));
        }
        if (subQuery.isEmpty()) {
            return Pattern.compile(".*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }

        // Check if the query contains an uppercase letter after the first character
        boolean hasUpperCaseAfterFirst = subQuery.length() > 1 &&
                Pattern.compile("[A-Z]").matcher(subQuery.substring(1)).find();

        int flags = hasUpperCaseAfterFirst ? Pattern.UNICODE_CASE : FLAGS;

        String query = hasUpperCaseAfterFirst ? subQuery : subQuery.toLowerCase();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);

            if (i == 0 && !hasUpperCaseAfterFirst) {
                char lowerC = Character.toLowerCase(c);

                sb.append(EQUIVALENT_CHARS.getOrDefault(lowerC,
                        "[" + Pattern.quote(String.valueOf(lowerC)) +
                        Pattern.quote(String.valueOf(Character.toUpperCase(lowerC))) + "]"));

            } else {
                sb.append(EQUIVALENT_CHARS.getOrDefault(Character.toLowerCase(c),
                        Pattern.quote(String.valueOf(c))));
            }
        }
        return Pattern.compile(sb.toString(), flags);
    }
    /**
     * Computes a relevance score for a stop name against multiple patterns.
     * Score is based on match length and word boundaries, with multipliers.
     *
     * @param name the stop name to score
     * @param patterns the list of compiled patterns to apply
     * @return a non-negative score; zero indicates no match
     */
    private double computeScore(String name, List<Pattern> patterns){
        if (patterns.isEmpty()) {
            return 0.0;
        }

        double finalScore = 0.0;

        for (Pattern p : patterns) {
            Matcher matcher = p.matcher(name);
            if (!matcher.find()) {
                return 0.0;
            }

            int start = matcher.start();
            int end = matcher.end();
            int matchLength = end - start;

            // Base score: (match length / name length) * 100, rounded down
            double baseScore = Math.floor(((double) matchLength / name.length()) * 100);

            // Multipliers
            int multiplier = 1;
            // Word start: at beginning or previous character is not a letter
            if (start == 0 || (start > 0 && !Character.isLetter(name.charAt(start - 1)))) {
                multiplier *= 4;
            }
            // Word end: at end or next character is not a letter
            if (end == name.length() || (end < name.length()
                    && !Character.isLetter(name.charAt(end)))) {
                multiplier *= 2;
            }

            finalScore += baseScore * multiplier;
        }

        return finalScore;
    }

}
