package io.github.timur0812.pokerodds.eval;

import io.github.timur0812.pokerodds.model.Card;
import io.github.timur0812.pokerodds.model.Rank;
import io.github.timur0812.pokerodds.model.Suit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Finds the best hand category for 1 to 7 cards (no kickers).
 * Fewer than 5 cards is fine, so the board can be checked on its own.
 */
public final class HandEvaluator {

    public static final int MIN_CARDS = 1;
    public static final int MAX_CARDS = 7;

    private static final int CARDS_IN_STRAIGHT = 5;
    private static final int CARDS_IN_FLUSH = 5;

    // Best category of the cards. Throws IAE on null, duplicates or not 1-7 cards.
    public HandCategory evaluate(Collection<Card> cards) {
        validate(cards);

        Map<Rank, Integer> rankCounts = countRanks(cards);
        Map<Suit, List<Card>> cardsBySuit = groupBySuit(cards);
        List<Integer> counts = countsLargestFirst(rankCounts);

        if (isStraightFlush(cardsBySuit))  return HandCategory.STRAIGHT_FLUSH;
        if (isFourOfAKind(counts))         return HandCategory.FOUR_OF_A_KIND;
        if (isFullHouse(counts))           return HandCategory.FULL_HOUSE;
        if (isFlush(cardsBySuit))          return HandCategory.FLUSH;
        if (isStraight(cards))             return HandCategory.STRAIGHT;
        if (isThreeOfAKind(counts))        return HandCategory.THREE_OF_A_KIND;
        if (isTwoPair(counts))             return HandCategory.TWO_PAIR;
        if (isPair(counts))                return HandCategory.PAIR;
        return HandCategory.HIGH_CARD;
    }

    // --- category checks ---

    // 5+ cards of one suit that also make a straight.
    private static boolean isStraightFlush(Map<Suit, List<Card>> cardsBySuit) {
        for (List<Card> suited : cardsBySuit.values()) {
            if (suited.size() >= CARDS_IN_FLUSH && containsStraight(suited)) {
                return true;
            }
        }
        return false;
    }

    // Some rank appears four times.
    private static boolean isFourOfAKind(List<Integer> counts) {
        return counts.get(0) >= 4;
    }

    // Trips plus a pair (two sets of trips count too).
    private static boolean isFullHouse(List<Integer> counts) {
        return counts.size() >= 2 && counts.get(0) >= 3 && counts.get(1) >= 2;
    }

    // Five or more cards of one suit.
    private static boolean isFlush(Map<Suit, List<Card>> cardsBySuit) {
        return cardsBySuit.values().stream().anyMatch(suited -> suited.size() >= CARDS_IN_FLUSH);
    }

    // Five consecutive ranks, in any suits.
    private static boolean isStraight(Collection<Card> cards) {
        return containsStraight(cards);
    }

    // Some rank appears three times.
    private static boolean isThreeOfAKind(List<Integer> counts) {
        return counts.get(0) >= 3;
    }

    // Two different pairs (three pairs count too).
    private static boolean isTwoPair(List<Integer> counts) {
        return counts.size() >= 2 && counts.get(0) >= 2 && counts.get(1) >= 2;
    }

    // Some rank appears twice.
    private static boolean isPair(List<Integer> counts) {
        return counts.get(0) >= 2;
    }

    // --- helpers ---

    // Five ranks in a row. Ace plays high or low.
    private static boolean containsStraight(Collection<Card> cards) {
        Set<Integer> values = new HashSet<>();
        for (Card card : cards) {
            values.add(card.rank().value());
        }
        if (values.contains(Rank.ACE.value())) {
            values.add(1);   // ace low
        }

        int inARow = 0;
        for (int value = 1; value <= Rank.ACE.value(); value++) {
            inARow = values.contains(value) ? inARow + 1 : 0;
            if (inARow == CARDS_IN_STRAIGHT) {
                return true;
            }
        }
        return false;
    }

    // How many times each rank appears, e.g. {K=3, 4=2}.
    private static Map<Rank, Integer> countRanks(Collection<Card> cards) {
        Map<Rank, Integer> counts = new EnumMap<>(Rank.class);
        for (Card card : cards) {
            counts.merge(card.rank(), 1, Integer::sum);
        }
        return counts;
    }

    // The cards split by suit.
    private static Map<Suit, List<Card>> groupBySuit(Collection<Card> cards) {
        Map<Suit, List<Card>> bySuit = new EnumMap<>(Suit.class);
        for (Card card : cards) {
            bySuit.computeIfAbsent(card.suit(), suit -> new ArrayList<>()).add(card);
        }
        return bySuit;
    }

    // The rank counts sorted from largest to smallest, e.g. K-K-K-4-4-9 gives [3, 2, 1].
    private static List<Integer> countsLargestFirst(Map<Rank, Integer> rankCounts) {
        return rankCounts.values().stream()
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    // --- validation ---

    private static void validate(Collection<Card> cards) {
        if (cards == null) {
            throw new IllegalArgumentException("Cards must not be null");
        }
        if (cards.size() < MIN_CARDS || cards.size() > MAX_CARDS) {
            throw new IllegalArgumentException(
                    "Expected " + MIN_CARDS + " to " + MAX_CARDS
                            + " cards but got " + cards.size());
        }
        if (cards.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Cards must not contain null");
        }
        if (new HashSet<>(cards).size() != cards.size()) {
            throw new IllegalArgumentException("Duplicate card in " + cards);
        }
    }
}