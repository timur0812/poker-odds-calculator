package io.github.timur0812.pokerodds.eval;

/**
 * Hand categories, weakest first.
 * Don't reorder: compareTo is used as hand strength. Royal flush is just the top straight flush.
 */
public enum HandCategory {
    HIGH_CARD("High card"),
    PAIR("Pair"),
    TWO_PAIR("Two pair"),
    THREE_OF_A_KIND("Three of a kind"),
    STRAIGHT("Straight"),
    FLUSH("Flush"),
    FULL_HOUSE("Full house"),
    FOUR_OF_A_KIND("Four of a kind"),
    STRAIGHT_FLUSH("Straight flush");

    private final String displayName;

    HandCategory(String displayName) {
        this.displayName = displayName;
    }

    // e.g. "Full house"
    public String displayName() {
        return displayName;
    }

    // True if this category is stronger than the other.
    public boolean beats(HandCategory other) {
        return compareTo(other) > 0;
    }

    @Override
    public String toString() {
        return displayName;
    }
}