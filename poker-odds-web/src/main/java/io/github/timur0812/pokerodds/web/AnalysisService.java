package io.github.timur0812.pokerodds.web;

import io.github.timur0812.pokerodds.eval.HandEvaluator;
import io.github.timur0812.pokerodds.model.Card;
import io.github.timur0812.pokerodds.outs.OutsCalculator;
import io.github.timur0812.pokerodds.outs.OutsResult;
import io.github.timur0812.pokerodds.web.AnalyzeResponse.ByRiverChance;
import io.github.timur0812.pokerodds.web.AnalyzeResponse.OutsGroup;
import io.github.timur0812.pokerodds.web.AnalyzeResponse.Street;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Turns the request into core calls. No poker math here.
@Service
public class AnalysisService {

    private final OutsCalculator outsCalculator = new OutsCalculator();
    private final HandEvaluator evaluator = new HandEvaluator();

    public AnalyzeResponse analyze(AnalyzeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be empty");
        }
        List<Card> hole = parse(request.hole(), "hole");
        List<Card> board = parse(request.board(), "board");
        if (hole.size() != 2) {
            throw new IllegalArgumentException("Expected 2 hole cards but got " + hole.size());
        }
        if (board.size() < 3 || board.size() > 5) {
            throw new IllegalArgumentException("Board must have 3, 4 or 5 cards but got " + board.size());
        }

        if (board.size() == 5) {
            List<Card> all = new ArrayList<>(hole);
            all.addAll(board);
            return AnalyzeResponse.river(evaluator.evaluate(all).displayName());
        }

        OutsResult result = outsCalculator.calculate(hole, board);
        boolean flop = board.size() == 3;
        return new AnalyzeResponse(
                flop ? Street.FLOP : Street.TURN,
                result.current().displayName(),
                result.unseenCards(),
                result.outsCount(),
                outsGroups(result),
                result.nextCardProbability(),
                flop ? result.byRiverProbability() : null,
                flop ? result.runnerRunnerProbability() : null,
                flop ? byRiverChances(result) : null);
    }

    // Strongest first (OutsResult is weakest first).
    private static List<OutsGroup> outsGroups(OutsResult result) {
        List<OutsGroup> groups = new ArrayList<>();
        result.outs().forEach((category, cards) -> groups.add(
                new OutsGroup(category.displayName(), cards.stream().map(Card::notation).toList())));
        Collections.reverse(groups);
        return groups;
    }

    private static List<ByRiverChance> byRiverChances(OutsResult result) {
        List<ByRiverChance> chances = new ArrayList<>();
        result.byRiver().keySet().forEach(category -> chances.add(
                new ByRiverChance(category.displayName(), result.byRiverProbability(category))));
        Collections.reverse(chances);
        return chances;
    }

    private static List<Card> parse(List<String> cards, String name) {
        if (cards == null) {
            throw new IllegalArgumentException("Missing " + name + " cards");
        }
        List<Card> parsed = new ArrayList<>();
        for (String card : cards) {
            if (card == null) {
                throw new IllegalArgumentException("Empty card in " + name);
            }
            parsed.add(Card.parse(card));
        }
        return parsed;
    }
}
