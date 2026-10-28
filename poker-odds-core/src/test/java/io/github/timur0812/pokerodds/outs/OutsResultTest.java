package io.github.timur0812.pokerodds.outs;

import io.github.timur0812.pokerodds.eval.HandCategory;
import io.github.timur0812.pokerodds.model.Card;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.github.timur0812.pokerodds.eval.HandCategory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class OutsResultTest {

    private static final Card TEN_CLUBS = Card.parse("Tc");
    private static final Card TEN_HEARTS = Card.parse("Th");
    private static final Card ACE_CLUBS = Card.parse("Ac");

    private static OutsResult flopResult() {
        Map<HandCategory, List<Card>> outs = new EnumMap<>(HandCategory.class);
        outs.put(STRAIGHT, List.of(TEN_CLUBS, TEN_HEARTS));
        outs.put(PAIR, List.of(ACE_CLUBS));
        Map<HandCategory, Integer> byRiver = new EnumMap<>(HandCategory.class);
        byRiver.put(STRAIGHT, 80);
        byRiver.put(PAIR, 40);
        return new OutsResult(HIGH_CARD, 47, outs, 1081, byRiver, 12);
    }

    private static OutsResult turnResult() {
        return new OutsResult(HIGH_CARD, 46, Map.of(PAIR, List.of(ACE_CLUBS)), 0, Map.of(), 0);
    }

    @Test
    void outsCountAddsUpEveryGroup() {
        assertThat(flopResult().outsCount()).isEqualTo(3);
    }

    @Test
    void nextCardProbabilityIsOutsOverUnseen() {
        assertThat(flopResult().nextCardProbability()).isCloseTo(3.0 / 47, within(1e-12));
    }

    @Test
    void byRiverProbabilityIsImprovingRunoutsOverAllRunouts() {
        assertThat(flopResult().byRiverProbability()).isCloseTo(120.0 / 1081, within(1e-12));
    }

    @Test
    void byRiverProbabilityPerCategory() {
        OutsResult result = flopResult();

        assertThat(result.byRiverProbability(STRAIGHT)).isCloseTo(80.0 / 1081, within(1e-12));
        assertThat(result.byRiverProbability(FLUSH)).isZero();
    }

    @Test
    void runnerRunnerProbability() {
        assertThat(flopResult().runnerRunnerProbability()).isCloseTo(12.0 / 1081, within(1e-12));
    }

    @Test
    void onTheTurnByRiverIsTheNextCard() {
        OutsResult result = turnResult();

        assertThat(result.byRiverProbability()).isEqualTo(result.nextCardProbability());
        assertThat(result.byRiverProbability(PAIR)).isZero();
        assertThat(result.runnerRunnerProbability()).isZero();
    }

    @Test
    void acceptsEmptyMapsOfAnyType() {
        OutsResult result = new OutsResult(HIGH_CARD, 47, Map.of(), 1081, Map.of(), 0);

        assertThat(result.outsCount()).isZero();
        assertThat(result.byRiverProbability()).isZero();
    }

    @Test
    void isImmutableAndDetachedFromItsInput() {
        List<Card> straightOuts = new ArrayList<>(List.of(TEN_CLUBS));
        Map<HandCategory, List<Card>> outs = new EnumMap<>(HandCategory.class);
        outs.put(STRAIGHT, straightOuts);
        OutsResult result = new OutsResult(HIGH_CARD, 47, outs, 0, Map.of(), 0);

        straightOuts.add(TEN_HEARTS);
        outs.put(PAIR, List.of(ACE_CLUBS));

        assertThat(result.outsCount()).isEqualTo(1);
        assertThatThrownBy(() -> result.outs().get(STRAIGHT).add(TEN_HEARTS))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.outs().put(FLUSH, List.of()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.byRiver().put(FLUSH, 1))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void groupsAreOrderedWeakestCategoryFirst() {
        assertThat(flopResult().outs().keySet()).containsExactly(PAIR, STRAIGHT);
    }

    @Test
    void rejectsNullCurrentCategory() {
        assertThatThrownBy(() -> new OutsResult(null, 47, Map.of(), 0, Map.of(), 0))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toStringSummarisesTheResult() {
        assertThat(flopResult().toString())
                .contains("Now: High card")
                .contains("Outs: 3 of 47")
                .contains("Straight: 2")
                .contains("By river");
        assertThat(turnResult().toString()).doesNotContain("By river");
    }

    @Test
    void toStringUsesADotWhateverTheDefaultLocale() {
        Locale before = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("ru-RU"));
        try {
            assertThat(flopResult().toString()).contains("6.38%");   // 3 / 47
        } finally {
            Locale.setDefault(before);
        }
    }
}
