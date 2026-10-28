package io.github.timur0812.pokerodds.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeckTest {

    private static List<Card> cards(String text) {
        return Arrays.stream(text.split(" ")).map(Card::parse).toList();
    }

    @Test
    void fullDeckHasFiftyTwoDistinctCards() {
        Deck deck = Deck.full();

        assertThat(deck.size()).isEqualTo(52);
        assertThat(new HashSet<>(deck.cards())).hasSize(52);
    }

    @Test
    void fullDeckHasThirteenOfEachSuitAndFourOfEachRank() {
        List<Card> all = Deck.full().cards();

        for (Suit suit : Suit.values()) {
            assertThat(all).filteredOn(card -> card.suit() == suit).hasSize(13);
        }
        for (Rank rank : Rank.values()) {
            assertThat(all).filteredOn(card -> card.rank() == rank).hasSize(4);
        }
    }

    @Test
    void withoutHoleCardsAndFlopLeavesFortySevenUnseen() {
        Deck unseen = Deck.full().without(cards("As Ks 5s Jc Qd"));

        assertThat(unseen.size()).isEqualTo(47);
        assertThat(unseen.contains(Card.parse("As"))).isFalse();
        assertThat(unseen.contains(Card.parse("Ah"))).isTrue();
    }

    @Test
    void withoutHoleCardsAndTurnLeavesFortySixUnseen() {
        assertThat(Deck.full().without(cards("As Ks 5s Jc Qd 2s")).size()).isEqualTo(46);
    }

    @Test
    void varargsOverloadMatchesCollectionVersion() {
        Deck byVarargs = Deck.full().without(Card.parse("As"), Card.parse("Kd"));
        Deck byList = Deck.full().without(cards("As Kd"));

        assertThat(byVarargs.cards()).isEqualTo(byList.cards());
    }

    @Test
    void withoutDoesNotChangeTheOriginalDeck() {
        Deck full = Deck.full();

        full.without(cards("As Kd"));

        assertThat(full.size()).isEqualTo(52);
    }

    @Test
    void withoutNothingKeepsEveryCard() {
        assertThat(Deck.full().without(List.of()).size()).isEqualTo(52);
    }

    @Test
    void rejectsTheSameCardTwice() {
        assertThatThrownBy(() -> Deck.full().without(cards("As Kd As")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicated");
    }

    @Test
    void rejectsACardAlreadyRemoved() {
        Deck unseen = Deck.full().without(cards("As"));

        assertThatThrownBy(() -> unseen.without(cards("As")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullInput() {
        assertThatThrownBy(() -> Deck.full().without((List<Card>) null))
                .isInstanceOf(NullPointerException.class);
        List<Card> withNull = new ArrayList<>(cards("As"));
        withNull.add(null);
        assertThatThrownBy(() -> Deck.full().without(withNull))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void cardsAreUnmodifiable() {
        assertThatThrownBy(() -> Deck.full().cards().remove(0))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void iteratesOverTheCards() {
        List<Card> seen = new ArrayList<>();
        Deck.full().without(cards("As")).forEach(seen::add);

        assertThat(seen).hasSize(51).doesNotContain(Card.parse("As"));
    }
}
