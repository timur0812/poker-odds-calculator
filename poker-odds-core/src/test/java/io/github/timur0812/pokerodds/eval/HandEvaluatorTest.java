package io.github.timur0812.pokerodds.eval;

import io.github.timur0812.pokerodds.model.Card;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import static io.github.timur0812.pokerodds.eval.HandCategory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HandEvaluatorTest {

    private final HandEvaluator evaluator = new HandEvaluator();

    private static List<Card> cards(String text) {
        return Arrays.stream(text.trim().split("\\s+")).map(Card::parse).toList();
    }

    private HandCategory eval(String cards) {
        return evaluator.evaluate(cards(cards));
    }

    // --- one case per category (five cards) ---

    @Test
    void highCard() {
        assertThat(eval("As Kd 9c 7h 4s")).isEqualTo(HIGH_CARD);
    }

    @Test
    void pair() {
        assertThat(eval("Ks Kd Ac Th 4s")).isEqualTo(PAIR);
    }

    @Test
    void twoPair() {
        assertThat(eval("4s 9d Kc 9h Ks")).isEqualTo(TWO_PAIR);
    }

    @Test
    void threeOfAKind() {
        assertThat(eval("7s 7d 7c Ah 4s")).isEqualTo(THREE_OF_A_KIND);
    }

    @Test
    void straight() {
        assertThat(eval("5s 6d 7c 8h 9s")).isEqualTo(STRAIGHT);
    }

    @Test
    void flush() {
        assertThat(eval("As 9s 7s 4s 2s")).isEqualTo(FLUSH);
    }

    @Test
    void fullHouse() {
        assertThat(eval("Ks Kd Kc 4h 4s")).isEqualTo(FULL_HOUSE);
    }

    @Test
    void fourOfAKind() {
        assertThat(eval("9s 9d 9c 9h 2s")).isEqualTo(FOUR_OF_A_KIND);
    }

    @Test
    void straightFlush() {
        assertThat(eval("5h 6h 7h 8h 9h")).isEqualTo(STRAIGHT_FLUSH);
    }

    @Test
    void royalFlushIsAStraightFlush() {
        assertThat(eval("Th Jh Qh Kh Ah")).isEqualTo(STRAIGHT_FLUSH);
    }

    // --- straights and the ace ---

    @Test
    void wheelStraightAndWheelStraightFlush() {
        assertThat(eval("As 2d 3c 4h 5s")).isEqualTo(STRAIGHT);
        assertThat(eval("Ah 2h 3h 4h 5h")).isEqualTo(STRAIGHT_FLUSH);
    }

    @Test
    void broadwayStraight() {
        assertThat(eval("Ts Jd Qc Kh As")).isEqualTo(STRAIGHT);
    }

    @Test
    void aceDoesNotWrapAround() {
        assertThat(eval("Qs Kd Ac 2h 3s")).isEqualTo(HIGH_CARD);
    }

    @Test
    void gapMeansNoStraight() {
        assertThat(eval("5s 6d 7c 8h Ts")).isEqualTo(HIGH_CARD);
    }

    @Test
    void straightAmongPairedCards() {
        assertThat(eval("5s 6d 7c 7h 8s 9d 9c")).isEqualTo(STRAIGHT);
    }

    // --- strongest category among six or seven cards ---

    @Test
    void flushBeatsStraightInTheSameCards() {
        assertThat(eval("5h 6d 7h 8h 9h 2h As")).isEqualTo(FLUSH);
    }

    @Test
    void straightFlushWhenTheSuitedCardsThemselvesFormTheStraight() {
        assertThat(eval("5h 6h 7h 8h 9h Kh 9s")).isEqualTo(STRAIGHT_FLUSH);
    }

    @Test
    void offSuitStraightAndFlushDoNotMakeAStraightFlush() {
        assertThat(eval("5h 6h 7h 8h Kh 9s 2h")).isEqualTo(FLUSH);
    }

    @Test
    void sixOrSevenSuitedCardsAreAFlush() {
        assertThat(eval("Js 9s 7s 4s 3s 2s Ad")).isEqualTo(FLUSH);
        assertThat(eval("As Ks 9s 7s 5s 3s 2s")).isEqualTo(FLUSH);
    }

    @Test
    void twoSetsOfTripsMakeAFullHouse() {
        assertThat(eval("4h 4s 4d Ks Kd Kc 2c")).isEqualTo(FULL_HOUSE);
    }

    @Test
    void tripsAndTwoPairsMakeAFullHouse() {
        assertThat(eval("Ks Kd Kc 4h 4s 9d 9c")).isEqualTo(FULL_HOUSE);
    }

    @Test
    void fourOfAKindBeatsTheFullHouseInTheSameCards() {
        assertThat(eval("9s 9d 9c 9h Ks Kd Kc")).isEqualTo(FOUR_OF_A_KIND);
    }

    @Test
    void threePairsAreTwoPair() {
        assertThat(eval("As Ad 4c 4h 9s 9d Kc")).isEqualTo(TWO_PAIR);
    }

    @Test
    void fourSuitedCardsAreNotAFlush() {
        assertThat(eval("As 9s 7s 4s 2d Kd Qh")).isEqualTo(HIGH_CARD);
    }

    // --- fewer than five cards (the board alone) ---

    @Test
    void oneCardIsHighCard() {
        assertThat(eval("As")).isEqualTo(HIGH_CARD);
    }

    @Test
    void flopAlone() {
        assertThat(eval("5s Jc Qd")).isEqualTo(HIGH_CARD);
        assertThat(eval("5s 5c Qd")).isEqualTo(PAIR);
        assertThat(eval("5s 5c 5d")).isEqualTo(THREE_OF_A_KIND);
    }

    @Test
    void turnBoardAlone() {
        assertThat(eval("5s 5c Qd Qh")).isEqualTo(TWO_PAIR);
        assertThat(eval("5s 5c 5d 5h")).isEqualTo(FOUR_OF_A_KIND);
        assertThat(eval("5s 5c 5d Qh")).isEqualTo(THREE_OF_A_KIND);
    }

    @Test
    void fewerThanFiveCardsAreNeverAStraightOrFlush() {
        assertThat(eval("6s 7s 8s 9s")).isEqualTo(HIGH_CARD);
        assertThat(eval("As 2s 3s 4s")).isEqualTo(HIGH_CARD);
    }

    // --- book example 1: A♠K♠ on 5♠ J♣ Q♦ with different turn cards ---

    @Test
    void bookExampleCategories() {
        assertThat(eval("As Ks 5s Jc Qd")).isEqualTo(HIGH_CARD);
        assertThat(eval("As Ks 5s Jc Qd Th")).isEqualTo(STRAIGHT);
        assertThat(eval("As Ks 5s Jc Qd Kh")).isEqualTo(PAIR);
        assertThat(eval("As Ks 5s Jc Qd 2s 7s")).isEqualTo(FLUSH);
    }

    // --- input handling ---

    @Test
    void cardLimitsAreOneAndSeven() {
        assertThat(HandEvaluator.MIN_CARDS).isEqualTo(1);
        assertThat(HandEvaluator.MAX_CARDS).isEqualTo(7);
    }

    @Test
    void cardOrderDoesNotMatter() {
        assertThat(eval("2c 3d 4s 5h 6c Kd Ks")).isEqualTo(eval("Ks Kd 6c 5h 4s 3d 2c"));
    }

    @Test
    void acceptsAnyCollectionType() {
        assertThat(evaluator.evaluate(new LinkedHashSet<>(cards("As Ad 9c 7h 4s"))))
                .isEqualTo(PAIR);
    }

    @Test
    void doesNotModifyTheInput() {
        List<Card> input = new ArrayList<>(cards("As Kd 9c 7h 4s 3d 2c"));
        List<Card> before = List.copyOf(input);

        evaluator.evaluate(input);

        assertThat(input).isEqualTo(before);
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> evaluator.evaluate(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNoCards() {
        assertThatThrownBy(() -> evaluator.evaluate(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1 to 7");
    }

    @Test
    void rejectsEightCards() {
        assertThatThrownBy(() -> eval("As Kd 9c 7h 4s 3d 2c 5h"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsDuplicateCards() {
        assertThatThrownBy(() -> eval("As Kd 9c 7h As"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void rejectsNullCard() {
        List<Card> withNull = new ArrayList<>(cards("As Kd 9c 7h 4s"));
        withNull.set(2, null);

        assertThatThrownBy(() -> evaluator.evaluate(withNull))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
