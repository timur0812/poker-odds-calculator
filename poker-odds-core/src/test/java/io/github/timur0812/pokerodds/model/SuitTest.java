package io.github.timur0812.pokerodds.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SuitTest {

    @Test
    void fourSuits() {
        assertThat(Suit.values()).containsExactly(Suit.CLUBS, Suit.DIAMONDS, Suit.HEARTS, Suit.SPADES);
    }

    @Test
    void codesAndSymbols() {
        assertThat(Suit.CLUBS.code()).isEqualTo('c');
        assertThat(Suit.DIAMONDS.code()).isEqualTo('d');
        assertThat(Suit.HEARTS.code()).isEqualTo('h');
        assertThat(Suit.SPADES.code()).isEqualTo('s');
        assertThat(Suit.SPADES.symbol()).isEqualTo('♠');
        assertThat(Suit.HEARTS.symbol()).isEqualTo('♥');
    }

    @Test
    void fromCharReadsLettersInEitherCase() {
        assertThat(Suit.fromChar('s')).isEqualTo(Suit.SPADES);
        assertThat(Suit.fromChar('S')).isEqualTo(Suit.SPADES);
        assertThat(Suit.fromChar('D')).isEqualTo(Suit.DIAMONDS);
    }

    @Test
    void fromCharReadsSymbols() {
        for (Suit suit : Suit.values()) {
            assertThat(Suit.fromChar(suit.symbol())).isEqualTo(suit);
        }
    }

    @Test
    void fromCharRejectsUnknownCharacters() {
        assertThatThrownBy(() -> Suit.fromChar('x'))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown suit");
    }

    @Test
    void toStringIsTheSymbol() {
        assertThat(Suit.CLUBS).hasToString("♣");
    }
}
