package io.github.timur0812.pokerodds.outs;

import io.github.timur0812.pokerodds.eval.HandCategory;
import io.github.timur0812.pokerodds.eval.HandEvaluator;
import io.github.timur0812.pokerodds.model.Card;
import io.github.timur0812.pokerodds.model.Deck;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Counts hero's outs on the flop or turn.
 *
 * An out must beat hero's hand now and the board alone. A new pair only
 * counts with a hole card above the board; two pair and trips need a new
 * card that matches a hole card.
 */
public final class OutsCalculator {

    private final HandEvaluator evaluator = new HandEvaluator();

    // hole: 2 cards, board: 3 (flop) or 4 (turn).
    public OutsResult calculate(List<Card> hole, List<Card> board) {
        validate(hole, board);
        HandCategory now = evaluator.evaluate(join(hole, board));
        List<Card> unseen = Deck.full().without(join(hole, board)).cards();

        // next card
        Map<HandCategory, List<Card>> outs = new EnumMap<>(HandCategory.class);
        for (Card card : unseen) {
            HandCategory made = improvement(hole, board, now, List.of(card));
            if (made != null) {
                outs.computeIfAbsent(made, category -> new ArrayList<>()).add(card);
            }
        }

        // every turn + river pair (flop only)
        Map<HandCategory, Integer> byRiver = new EnumMap<>(HandCategory.class);
        int runouts = 0;
        int runnerRunner = 0;
        if (board.size() == 3) {
            Set<Card> outCards = allCards(outs);
            for (int i = 0; i < unseen.size(); i++) {
                for (int j = i + 1; j < unseen.size(); j++) {
                    Card turn = unseen.get(i);
                    Card river = unseen.get(j);
                    runouts++;
                    HandCategory made = improvement(hole, board, now, List.of(turn, river));
                    if (made != null) {
                        byRiver.merge(made, 1, Integer::sum);
                        if (!outCards.contains(turn) && !outCards.contains(river)) {
                            runnerRunner++;   // neither card alone was an out
                        }
                    }
                }
            }
        }

        return new OutsResult(now, unseen.size(), outs, runouts, byRiver, runnerRunner);
    }

    // Category hero improves to, or null.
    private HandCategory improvement(List<Card> hole, List<Card> board,
                                     HandCategory now, List<Card> newCards) {
        List<Card> newBoard = join(board, newCards);
        HandCategory made = evaluator.evaluate(join(hole, newBoard));
        HandCategory boardAlone = evaluator.evaluate(newBoard);

        if (!made.beats(now) || !made.beats(boardAlone)) {
            return null;
        }
        if (made == HandCategory.PAIR && !pairsOvercard(hole, board, newCards)) {
            return null;   // pair must be with an overcard
        }
        if ((made == HandCategory.TWO_PAIR || made == HandCategory.THREE_OF_A_KIND)
                && !matchesHoleCard(hole, newCards)) {
            return null;   // a board pair doesn't count
        }
        return made;
    }

    // New card pairs a hole card above the board.
    private static boolean pairsOvercard(List<Card> hole, List<Card> board, List<Card> newCards) {
        int highestOnBoard = board.stream().mapToInt(card -> card.rank().value()).max().orElse(0);
        return newCards.stream().anyMatch(newCard -> hole.stream().anyMatch(holeCard ->
                holeCard.rank() == newCard.rank() && holeCard.rank().value() > highestOnBoard));
    }

    // New card matches a hole card's rank.
    private static boolean matchesHoleCard(List<Card> hole, List<Card> newCards) {
        return newCards.stream().anyMatch(newCard ->
                hole.stream().anyMatch(holeCard -> holeCard.rank() == newCard.rank()));
    }

    private static Set<Card> allCards(Map<HandCategory, List<Card>> outs) {
        Set<Card> cards = new HashSet<>();
        outs.values().forEach(cards::addAll);
        return cards;
    }

    private static List<Card> join(List<Card> first, List<Card> second) {
        List<Card> all = new ArrayList<>(first);
        all.addAll(second);
        return all;
    }

    private static void validate(List<Card> hole, List<Card> board) {
        if (hole == null || hole.size() != 2) {
            throw new IllegalArgumentException("Expected 2 hole cards");
        }
        if (board == null || (board.size() != 3 && board.size() != 4)) {
            throw new IllegalArgumentException("Board must have 3 or 4 cards");
        }
        // nulls and duplicates are caught by the evaluator
    }
}