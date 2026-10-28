package io.github.timur0812.pokerodds.outs;

import io.github.timur0812.pokerodds.model.Card;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.timur0812.pokerodds.eval.HandCategory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.within;

class OutsCalculatorTest {

    private static final int FLOP_RUNOUTS = 47 * 46 / 2;   // 1,081 turn-river pairs

    private final OutsCalculator calculator = new OutsCalculator();

    private static List<Card> cards(String text) {
        return Arrays.stream(text.trim().split("\\s+")).map(Card::parse).toList();
    }

    private OutsResult calculate(String hole, String board) {
        return calculator.calculate(cards(hole), cards(board));
    }

    // --- book example 1: A♠K♠ on 5♠ J♣ Q♦ -> 10 outs ---

    @Test
    void bookExample1TenOutsOnTheFlop() {
        OutsResult result = calculate("As Ks", "5s Jc Qd");

        assertThat(result.current()).isEqualTo(HIGH_CARD);
        assertThat(result.unseenCards()).isEqualTo(47);
        assertThat(result.outsCount()).isEqualTo(10);
        assertThat(result.outs().get(STRAIGHT)).containsExactlyInAnyOrderElementsOf(cards("Tc Td Th Ts"));
        assertThat(result.outs().get(PAIR)).containsExactlyInAnyOrderElementsOf(cards("Ac Ad Ah Kc Kd Kh"));
        assertThat(result.nextCardProbability()).isCloseTo(10.0 / 47, within(1e-12));
    }

    @Test
    void cardsThatOnlyPairTheBoardAreNotOuts() {
        List<Card> outCards = allOuts(calculate("As Ks", "5s Jc Qd"));

        assertThat(outCards).doesNotContainAnyElementsOf(cards("5c 5d 5h Jd Jh Js Qc Qh Qs"));
    }

    @Test
    void bookExample1ByTheRiver() {
        OutsResult result = calculate("As Ks", "5s Jc Qd");

        // 415 runouts hit one of the 10 outs, plus 36 runner-runner flushes
        assertThat(result.runouts()).isEqualTo(FLOP_RUNOUTS);
        assertThat(result.runnerRunner()).isEqualTo(36);
        assertThat(improvingRunouts(result)).isEqualTo(451);
        assertThat(result.byRiver().get(FLUSH)).isEqualTo(45);       // any two of the 10 spades
        assertThat(result.byRiver().get(STRAIGHT)).isEqualTo(169);   // a ten, but not T♠ with another spade
        assertThat(result.byRiverProbability()).isCloseTo(451.0 / FLOP_RUNOUTS, within(1e-12));
        assertThat(result.runnerRunnerProbability()).isCloseTo(36.0 / FLOP_RUNOUTS, within(1e-12));
    }

    @Test
    void byRiverWithoutRunnerRunnerMatchesTheProbabilityFormula() {
        OutsResult result = calculate("As Ks", "5s Jc Qd");
        int outs = result.outsCount();
        int unseen = result.unseenCards();
        double formula = 1 - (double) (unseen - outs) / unseen * (unseen - 1 - outs) / (unseen - 1);
        assertThat((double) (improvingRunouts(result) - result.runnerRunner()) / result.runouts())
                .isCloseTo(formula, within(1e-12));
    }

    // --- book example 2: same hand, turn 2♠ -> 18 outs ---

    @Test
    void bookExample2EighteenOutsOnTheTurn() {
        OutsResult result = calculate("As Ks", "5s Jc Qd 2s");

        assertThat(result.unseenCards()).isEqualTo(46);
        assertThat(result.outsCount()).isEqualTo(18);
        assertThat(result.outs().get(FLUSH)).hasSize(9).contains(Card.parse("Ts"));   // T♠ counted once
        assertThat(result.outs().get(STRAIGHT)).containsExactlyInAnyOrderElementsOf(cards("Tc Td Th"));
        assertThat(result.outs().get(PAIR)).hasSize(6);
    }

    @Test
    void onTheTurnThereAreNoRunouts() {
        OutsResult result = calculate("As Ks", "5s Jc Qd 2s");

        assertThat(result.runouts()).isZero();
        assertThat(result.byRiver()).isEmpty();
        assertThat(result.runnerRunner()).isZero();
        assertThat(result.byRiverProbability()).isEqualTo(result.nextCardProbability());
        assertThat(result.runnerRunnerProbability()).isZero();
    }

    // --- book example 3: 9♥8♥ on 2♣ 6♠ 7♦ -> 14 outs ---

    @Test
    void bookExample3FourteenOuts() {
        OutsResult result = calculate("9h 8h", "2c 6s 7d");

        assertThat(result.outsCount()).isEqualTo(14);
        assertThat(result.outs().get(STRAIGHT)).hasSize(8);   // four fives and four tens
        assertThat(result.outs().get(PAIR)).hasSize(6);       // three nines and three eights
        assertThat(improvingRunouts(result)).isEqualTo(553);
    }

    // --- other situations ---

    @Test
    void setImprovesToFullHouseOrQuads() {
        OutsResult result = calculate("7s 7h", "7d Kc 2h");

        assertThat(result.current()).isEqualTo(THREE_OF_A_KIND);
        assertThat(result.outs().get(FULL_HOUSE)).hasSize(6);   // three kings and three twos
        assertThat(result.outs().get(FOUR_OF_A_KIND)).containsExactly(Card.parse("7c"));
        assertThat(result.outsCount()).isEqualTo(7);
    }

    @Test
    void cardsThatOnlyImproveTheBoardAreNotOuts() {
        // hearts and tens only help the board; 2s and 3s are low pairs
        OutsResult result = calculate("2c 3d", "Ah Kh Qh Jh");

        assertThat(result.outsCount()).isZero();
        assertThat(result.outs()).isEmpty();
    }

    // --- cards that only pair the board, and low pairs ---

    @Test
    void overpairOnlyImprovesWithTheTwoAces() {
        OutsResult result = calculate("As Ah", "Qd Jd 8s");

        assertThat(result.current()).isEqualTo(PAIR);
        assertThat(allOuts(result)).containsExactlyInAnyOrderElementsOf(cards("Ac Ad"));
        assertThat(result.outs().keySet()).containsExactly(THREE_OF_A_KIND);   // no TWO_PAIR group
        assertThat(result.nextCardProbability()).isCloseTo(2.0 / 47, within(1e-12));

        assertThat(result.runouts()).isEqualTo(FLOP_RUNOUTS);
        assertThat(improvingRunouts(result)).isEqualTo(116);
        assertThat(result.runnerRunner()).isEqualTo(25);
        assertThat(result.byRiver()).containsOnly(
                entry(THREE_OF_A_KIND, 72),
                entry(FULL_HOUSE, 27),
                entry(STRAIGHT, 16),
                entry(FOUR_OF_A_KIND, 1));
    }

    @Test
    void pairingAHoleCardBelowTheBoardIsNotAnOut() {
        // K and 7 are both below the ace
        OutsResult result = calculate("Kh 7h", "As Qd 3c");

        assertThat(result.outsCount()).isZero();
    }

    @Test
    void pocketPairOnlyImprovesWithASet() {
        // K, 8 or 2 just pairs the board
        OutsResult result = calculate("7s 7h", "Kc 8d 2h");

        assertThat(allOuts(result)).containsExactlyInAnyOrderElementsOf(cards("7c 7d"));
        assertThat(improvingRunouts(result)).isEqualTo(100);
    }

    @Test
    void eachCardIsListedOnceUnderTheBestCategoryItMakes() {
        List<Card> outCards = allOuts(calculate("As Ks", "5s Jc Qd 2s"));

        assertThat(outCards).doesNotHaveDuplicates();
    }

    @Test
    void noOutsWhenNothingCanImprove() {
        // already a royal flush
        OutsResult result = calculate("Ah Kh", "Qh Jh Th");

        assertThat(result.current()).isEqualTo(STRAIGHT_FLUSH);
        assertThat(result.outsCount()).isZero();
        assertThat(result.outs()).isEmpty();
        assertThat(result.byRiverProbability()).isZero();
    }

    // --- input handling ---

    @Test
    void rejectsWrongNumberOfHoleCards() {
        assertThatThrownBy(() -> calculate("As", "5s Jc Qd"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2 hole cards");
        assertThatThrownBy(() -> calculate("As Ks Kd", "5s Jc Qd"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calculator.calculate(null, cards("5s Jc Qd")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBoardOtherThanFlopOrTurn() {
        assertThatThrownBy(() -> calculate("As Ks", "5s Jc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3 or 4");
        assertThatThrownBy(() -> calculate("As Ks", "5s Jc Qd 2s 3h"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calculator.calculate(cards("As Ks"), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsACardOnBothHandAndBoard() {
        assertThatThrownBy(() -> calculate("As Ks", "As Jc Qd"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullCards() {
        List<Card> hole = new ArrayList<>(cards("As Ks"));
        hole.set(1, null);

        assertThatThrownBy(() -> calculator.calculate(hole, cards("5s Jc Qd")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static int improvingRunouts(OutsResult result) {
        return result.byRiver().values().stream().mapToInt(Integer::intValue).sum();
    }

    private static List<Card> allOuts(OutsResult result) {
        return result.outs().values().stream().flatMap(List::stream).toList();
    }
}
