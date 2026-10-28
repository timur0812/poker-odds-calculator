package io.github.timur0812.pokerodds.web;

import java.util.List;

// Cards like "As"; the board has 3 to 5 cards.
public record AnalyzeRequest(List<String> hole, List<String> board) {
}
