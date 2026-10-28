package io.github.timur0812.pokerodds.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RankTest {

    @Test
    void thirteenRanksFromTwoToAce() {
        assertThat(Rank.values()).hasSize(13);
        assertThat(Rank.values()[0]).isEqualTo(Rank.TWO);
        assertThat(Rank.values()[12]).isEqualTo(Rank.ACE);
    }

    @Test
    void valuesRunFromTwoToFourteenInDeclarationOrder() {
        Rank[] ranks = Rank.values();
        for (int i = 0; i < ranks.length; i++) {
            assertThat(ranks[i].value()).isEqualTo(i + 2);
        }
    }

    @Test
    void symbols() {
        assertThat(Rank.TWO.symbol()).isEqualTo('2');
        assertThat(Rank.NINE.symbol()).isEqualTo('9');
        assertThat(Rank.TEN.symbol()).isEqualTo('T');
        assertThat(Rank.JACK.symbol()).isEqualTo('J');
        assertThat(Rank.QUEEN.symbol()).isEqualTo('Q');
        assertThat(Rank.KING.symbol()).isEqualTo('K');
        assertThat(Rank.ACE.symbol()).isEqualTo('A');
    }

    @Test
    void fromCharReadsEverySymbolBack() {
        for (Rank rank : Rank.values()) {
            assertThat(Rank.fromChar(rank.symbol())).isEqualTo(rank);
        }
    }

    @Test
    void fromCharIsCaseInsensitive() {
        assertThat(Rank.fromChar('t')).isEqualTo(Rank.TEN);
        assertThat(Rank.fromChar('a')).isEqualTo(Rank.ACE);
    }

    @Test
    void fromCharRejectsUnknownCharacters() {
        assertThatThrownBy(() -> Rank.fromChar('1'))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown rank");
        assertThatThrownBy(() -> Rank.fromChar('X'))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toStringIsTheSymbol() {
        assertThat(Rank.TEN).hasToString("T");
    }
}
