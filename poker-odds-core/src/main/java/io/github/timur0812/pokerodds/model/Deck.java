package io.github.timur0812.pokerodds.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

// Immutable deck. Start with full() and remove the cards you can see.
public final class Deck implements Iterable<Card> {

    private static final int FULL_SIZE = 52;

    private final List<Card> cards;

    private Deck(List<Card> cards) {
        this.cards = List.copyOf(cards);
    }

    // A complete 52-card deck.
    public static Deck full() {
        List<Card> all = new ArrayList<>(FULL_SIZE);
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                all.add(new Card(rank, suit));
            }
        }
        return new Deck(all);
    }

    // New deck without these cards. Throws IAE if a card is missing or repeated.
    public Deck without(Collection<Card> toRemove) {
        Objects.requireNonNull(toRemove, "toRemove");
        List<Card> remaining = new ArrayList<>(cards);
        for (Card card : toRemove) {
            Objects.requireNonNull(card, "card");
            if (!remaining.remove(card)) {
                throw new IllegalArgumentException(
                        "Card is duplicated or already removed: " + card);
            }
        }
        return new Deck(remaining);
    }

    // Convenience overload of {@link #without(Collection)}.
    public Deck without(Card... toRemove) {
        return without(List.of(toRemove));
    }

    public boolean contains(Card card) {
        return cards.contains(card);
    }

    public int size() {
        return cards.size();
    }

    // Read-only list of the cards.
    public List<Card> cards() {
        return cards;
    }

    @Override
    public Iterator<Card> iterator() {
        return cards.iterator();
    }

    @Override
    public String toString() {
        return "Deck" + cards;
    }
}