package io.github.timur0812.pokerodds.model;

public enum Rank {
    TWO(2, '2'),
    THREE(3, '3'),
    FOUR(4, '4'),
    FIVE(5, '5'),
    SIX(6, '6'),
    SEVEN(7, '7'),
    EIGHT(8, '8'),
    NINE(9, '9'),
    TEN(10, 'T'),
    JACK(11, 'J'),
    QUEEN(12, 'Q'),
    KING(13, 'K'),
    ACE(14, 'A');

    private final int value;
    private final char symbol;

    Rank(int value, char symbol) {
        this.value = value;
        this.symbol = symbol;
    }

    // 2 to 14, ace is 14.
    public int value() {
        return value;
    }

    // 2-9, T, J, Q, K, A.
    public char symbol() {
        return symbol;
    }

    // Case-insensitive. "10" is handled in Card.parse.
    public static Rank fromChar(char c) {
        char upper = Character.toUpperCase(c);
        for (Rank rank : values()) {
            if (rank.symbol == upper) {
                return rank;
            }
        }
        throw new IllegalArgumentException("Unknown rank: '" + c + "'");
    }

    @Override
    public String toString() {
        return String.valueOf(symbol);
    }
}
