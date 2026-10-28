package io.github.timur0812.pokerodds.web;

import io.github.timur0812.pokerodds.web.AnalyzeResponse.ByRiverChance;
import io.github.timur0812.pokerodds.web.AnalyzeResponse.OutsGroup;
import io.github.timur0812.pokerodds.web.AnalyzeResponse.Street;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class AnalysisServiceTest {

    private static final double RUNOUTS = 1081;   // turn-river pairs from the flop

    private final AnalysisService service = new AnalysisService();

    private AnalyzeResponse analyze(String hole, String board) {
        return service.analyze(new AnalyzeRequest(cards(hole), cards(board)));
    }

    private static List<String> cards(String text) {
        return Arrays.asList(text.trim().split("\\s+"));
    }

    @Test
    void flopBookExample1() {
        AnalyzeResponse response = analyze("As Ks", "5s Jc Qd");

        assertThat(response.street()).isEqualTo(Street.FLOP);
        assertThat(response.combination()).isEqualTo("High card");
        assertThat(response.unseenCards()).isEqualTo(47);
        assertThat(response.outsCount()).isEqualTo(10);
        assertThat(response.outs()).extracting(OutsGroup::combination).containsExactly("Straight", "Pair");
        assertThat(response.outs().get(0).cards()).containsExactlyInAnyOrder("Tc", "Td", "Th", "Ts");
        assertThat(response.outs().get(1).cards()).containsExactlyInAnyOrder("Ac", "Ad", "Ah", "Kc", "Kd", "Kh");
        assertThat(response.nextCardProbability()).isCloseTo(10.0 / 47, within(1e-12));
        assertThat(response.byRiverProbability()).isCloseTo(451 / RUNOUTS, within(1e-12));
        assertThat(response.runnerRunnerProbability()).isCloseTo(36 / RUNOUTS, within(1e-12));
    }

    @Test
    void flopByRiverIsStrongestFirst() {
        List<ByRiverChance> byRiver = analyze("As Ks", "5s Jc Qd").byRiver();

        assertThat(byRiver.get(0).combination()).isEqualTo("Flush");
        assertThat(byRiver.get(0).probability()).isCloseTo(45 / RUNOUTS, within(1e-12));
        assertThat(byRiver.get(1).combination()).isEqualTo("Straight");
        assertThat(byRiver.get(1).probability()).isCloseTo(169 / RUNOUTS, within(1e-12));
        assertThat(byRiver.stream().mapToDouble(ByRiverChance::probability).sum())
                .isCloseTo(451 / RUNOUTS, within(1e-12));
    }

    @Test
    void turnHasNoByRiverFields() {
        AnalyzeResponse response = analyze("As Ks", "5s Jc Qd 2s");

        assertThat(response.street()).isEqualTo(Street.TURN);
        assertThat(response.unseenCards()).isEqualTo(46);
        assertThat(response.outsCount()).isEqualTo(18);
        assertThat(response.outs()).extracting(OutsGroup::combination)
                .containsExactly("Flush", "Straight", "Pair");
        assertThat(response.nextCardProbability()).isCloseTo(18.0 / 46, within(1e-12));
        assertThat(response.byRiverProbability()).isNull();
        assertThat(response.runnerRunnerProbability()).isNull();
        assertThat(response.byRiver()).isNull();
    }

    @Test
    void riverOnlyReportsTheCombination() {
        AnalyzeResponse response = analyze("As Ks", "5s Jc Qd 2h Tc");

        assertThat(response).isEqualTo(AnalyzeResponse.river("Straight"));
        assertThat(response.street()).isEqualTo(Street.RIVER);
        assertThat(response.outs()).isNull();
        assertThat(response.nextCardProbability()).isNull();
    }

    @Test
    void acceptsTenWrittenAs10() {
        assertThat(analyze("As Ks", "5s Jc Qd 2h 10c").combination()).isEqualTo("Straight");
    }

    @Test
    void rejectsWrongCardCounts() {
        assertThatThrownBy(() -> analyze("As", "5s Jc Qd"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2 hole cards");
        assertThatThrownBy(() -> analyze("As Ks", "5s Jc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3, 4 or 5");
        assertThatThrownBy(() -> analyze("As Ks", "5s Jc Qd 2h Tc 3d"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsDuplicateCardsOnEveryStreet() {
        assertThatThrownBy(() -> analyze("As Ks", "As Jc Qd"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> analyze("As Ks", "5s Jc Qd 5s"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> analyze("As Ks", "5s Jc Qd 2h Ks"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidMissingOrNullCards() {
        assertThatThrownBy(() -> analyze("As Kx", "5s Jc Qd"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.analyze(new AnalyzeRequest(null, cards("5s Jc Qd"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hole");
        assertThatThrownBy(() -> service.analyze(new AnalyzeRequest(cards("As Ks"), Arrays.asList("5s", null, "Qd"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("board");
        assertThatThrownBy(() -> service.analyze(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
