package io.github.timur0812.pokerodds.model;

public enum Suit {
    CLUBS('c', '\u2663'),
    DIAMONDS('d', '\u2666'),
    HEARTS('h', '\u2665'),
    SPADES('s', '\u2660');

    private final char code;
    private final char symbol;

    Suit(char code, char symbol) {
        this.code = code;
        this.symbol = symbol;
    }

    // c, d, h or s.
    public char code() {
        return code;
    }

    // ♣, ♦, ♥ or ♠.
    public char symbol() {
        return symbol;
    }

    // Accepts the letter (any case) or the symbol.
    public static Suit fromChar(char c) {
        for (Suit suit : values()) {
            if (suit.code == Character.toLowerCase(c) || suit.symbol == c) {
                return suit;
            }
        }
        throw new IllegalArgumentException("Unknown suit: '" + c + "'");
    }

    @Override
    public String toString() {
        return String.valueOf(symbol);
    }
}
