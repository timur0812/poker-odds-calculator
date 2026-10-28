package io.github.timur0812.pokerodds.eval;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static io.github.timur0812.pokerodds.eval.HandCategory.*;
import static org.assertj.core.api.Assertions.assertThat;

class HandCategoryTest {

    @Test
    void declarationOrderIsWeakestToStrongest() {
        assertThat(Arrays.asList(HandCategory.values())).containsExactly(
                HIGH_CARD, PAIR, TWO_PAIR, THREE_OF_A_KIND, STRAIGHT,
                FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH);
    }

    @Test
    void everyCategoryBeatsAllWeakerOnes() {
        HandCategory[] all = HandCategory.values();
        for (int stronger = 0; stronger < all.length; stronger++) {
            for (int weaker = 0; weaker < stronger; weaker++) {
                assertThat(all[stronger].beats(all[weaker]))
                        .as("%s beats %s", all[stronger], all[weaker])
                        .isTrue();
                assertThat(all[weaker].beats(all[stronger])).isFalse();
            }
        }
    }

    @Test
    void categoryDoesNotBeatItself() {
        assertThat(FLUSH.beats(FLUSH)).isFalse();
    }

    @Test
    void displayNameIsUsedAsToString() {
        assertThat(FULL_HOUSE.displayName()).isEqualTo("Full house");
        assertThat(FULL_HOUSE).hasToString("Full house");
        assertThat(STRAIGHT_FLUSH).hasToString("Straight flush");
    }
}
