package io.github.timur0812.pokerodds.web;

import java.util.List;

// Probabilities are 0-1. On the river only street and combination are set.
public record AnalyzeResponse(Street street,
                              String combination,
                              Integer unseenCards,
                              Integer outsCount,
                              List<OutsGroup> outs,
                              Double nextCardProbability,
                              Double byRiverProbability,
                              Double runnerRunnerProbability,
                              List<ByRiverChance> byRiver) {

    public enum Street { FLOP, TURN, RIVER }

    // e.g. Straight: Tc Td Th Ts
    public record OutsGroup(String combination, List<String> cards) {
    }

    // Chance to end with this combination by the river.
    public record ByRiverChance(String combination, double probability) {
    }

    static AnalyzeResponse river(String combination) {
        return new AnalyzeResponse(Street.RIVER, combination, null, null, null, null, null, null, null);
    }
}
