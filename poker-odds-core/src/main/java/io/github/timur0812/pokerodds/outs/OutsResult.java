package io.github.timur0812.pokerodds.outs;

import io.github.timur0812.pokerodds.eval.HandCategory;
import io.github.timur0812.pokerodds.model.Card;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Outs counts; the methods turn them into probabilities.
 *
 * @param current      hero's category now
 * @param unseenCards  47 on the flop, 46 on the turn
 * @param outs         next-card outs by category
 * @param runouts      turn + river pairs tried (0 on the turn)
 * @param byRiver      improving runouts by final category
 * @param runnerRunner improving runouts where neither card is an out
 */
public record OutsResult(HandCategory current,
                         int unseenCards,
                         Map<HandCategory, List<Card>> outs,
                         int runouts,
                         Map<HandCategory, Integer> byRiver,
                         int runnerRunner) {

    public OutsResult {
        Objects.requireNonNull(current, "current");
        outs = copyOuts(outs);
        byRiver = copyCounts(byRiver);
    }

    // Read-only copy, lists included.
    private static Map<HandCategory, List<Card>> copyOuts(Map<HandCategory, List<Card>> outs) {
        Map<HandCategory, List<Card>> copy = new EnumMap<>(HandCategory.class);
        outs.forEach((category, cards) -> copy.put(category, List.copyOf(cards)));
        return Collections.unmodifiableMap(copy);
    }

    private static Map<HandCategory, Integer> copyCounts(Map<HandCategory, Integer> counts) {
        Map<HandCategory, Integer> copy = new EnumMap<>(HandCategory.class);
        copy.putAll(counts);   // new EnumMap<>(map) fails on an empty Map.of()
        return Collections.unmodifiableMap(copy);
    }

    // Total number of outs on the next card.
    public int outsCount() {
        return outs.values().stream().mapToInt(List::size).sum();
    }

    // Chance to improve on the next card.
    public double nextCardProbability() {
        return (double) outsCount() / unseenCards;
    }

    // Chance to improve by the river. On the turn this is the next card.
    public double byRiverProbability() {
        if (runouts == 0) {
            return nextCardProbability();
        }
        int improving = byRiver.values().stream().mapToInt(Integer::intValue).sum();
        return (double) improving / runouts;
    }

    // Chance to finish with this combination by the river (flop only).
    public double byRiverProbability(HandCategory category) {
        return runouts == 0 ? 0 : (double) byRiver.getOrDefault(category, 0) / runouts;
    }

    // Chance to improve only with both turn and river (flop only).
    public double runnerRunnerProbability() {
        return runouts == 0 ? 0 : (double) runnerRunner / runouts;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder(String.format(Locale.ROOT,
                "Now: %s. Outs: %d of %d, next card %.2f%%",
                current, outsCount(), unseenCards, nextCardProbability() * 100));
        outs.forEach((category, cards) ->
                text.append(String.format(Locale.ROOT, "%n  %s: %d %s", category, cards.size(), cards)));
        if (runouts > 0) {
            text.append(String.format(Locale.ROOT, "%nBy river: %.2f%% (runner-runner %.2f%%)",
                    byRiverProbability() * 100, runnerRunnerProbability() * 100));
            byRiver.forEach((category, count) ->
                    text.append(String.format(Locale.ROOT, "%n  %s: %.2f%%", category, 100.0 * count / runouts)));
        }
        return text.toString();
    }
}