package io.github.timur0812.pokerodds.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardTest {

    @Test
    void parsesRankAndSuit() {
        assertThat(Card.parse("As")).isEqualTo(new Card(Rank.ACE, Suit.SPADES));
        assertThat(Card.parse("7c")).isEqualTo(new Card(Rank.SEVEN, Suit.CLUBS));
    }

    @Test
    void parsesTenAsTOrTen() {
        assertThat(Card.parse("Td")).isEqualTo(new Card(Rank.TEN, Suit.DIAMONDS));
        assertThat(Card.parse("10h")).isEqualTo(new Card(Rank.TEN, Suit.HEARTS));
    }

    @Test
    void parsingIsCaseInsensitiveAndIgnoresSurroundingSpaces() {
        assertThat(Card.parse(" kH ")).isEqualTo(new Card(Rank.KING, Suit.HEARTS));
    }

    @Test
    void parsesUnicodeSuitSymbols() {
        assertThat(Card.parse("A♠")).isEqualTo(new Card(Rank.ACE, Suit.SPADES));
        assertThat(Card.parse("10♦")).isEqualTo(new Card(Rank.TEN, Suit.DIAMONDS));
    }

    @Test
    void notationRoundTripsForEveryCard() {
        for (Card card : Deck.full()) {
            assertThat(Card.parse(card.notation())).isEqualTo(card);
        }
    }

    @Test
    void notationAndToString() {
        Card card = new Card(Rank.TEN, Suit.SPADES);

        assertThat(card.notation()).isEqualTo("Ts");
        assertThat(card).hasToString("T♠");
    }

    @Test
    void cardsWithSameRankAndSuitAreEqual() {
        assertThat(Card.parse("Qh"))
                .isEqualTo(new Card(Rank.QUEEN, Suit.HEARTS))
                .hasSameHashCodeAs(new Card(Rank.QUEEN, Suit.HEARTS))
                .isNotEqualTo(Card.parse("Qd"));
    }

    @Test
    void rejectsNullRankOrSuit() {
        assertThatThrownBy(() -> new Card(null, Suit.SPADES)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Card(Rank.ACE, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsInvalidText() {
        assertThatThrownBy(() -> Card.parse("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Card.parse("A")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Card.parse("Asd")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Card.parse("1s")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Card.parse("Ax")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Card.parse("100")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullText() {
        assertThatThrownBy(() -> Card.parse(null)).isInstanceOf(NullPointerException.class);
    }
}
