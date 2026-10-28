package io.github.timur0812.pokerodds.model;

import java.util.Objects;

// A playing card.
public record Card(Rank rank, Suit suit) {

    public Card {
        Objects.requireNonNull(rank, "rank");
        Objects.requireNonNull(suit, "suit");
    }

    // Parses "As", "Td", "10h" or "A♠" (any case). Throws IAE if invalid.
    public static Card parse(String text) {
        Objects.requireNonNull(text, "text");
        String s = text.strip();
        if (s.length() == 3 && s.startsWith("10")) {
            return new Card(Rank.TEN, Suit.fromChar(s.charAt(2)));
        }
        if (s.length() != 2) {
            throw new IllegalArgumentException("Invalid card: '" + text + "'");
        }
        return new Card(Rank.fromChar(s.charAt(0)), Suit.fromChar(s.charAt(1)));
    }

    // e.g. "As" or "Td", the format parse() reads.
    public String notation() {
        return "" + rank.symbol() + suit.code();
    }

    // e.g. "A♠"
    @Override
    public String toString() {
        return rank.toString() + suit;
    }
}