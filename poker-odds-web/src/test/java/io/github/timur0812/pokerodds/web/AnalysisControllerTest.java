package io.github.timur0812.pokerodds.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
@Import(AnalysisService.class)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions postAnalyze(String json) throws Exception {
        return mockMvc.perform(post("/api/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Test
    void flopRequestReturnsOutsAndProbabilities() throws Exception {
        postAnalyze("{ \"hole\": [\"As\",\"Ks\"], \"board\": [\"5s\",\"Jc\",\"Qd\"] }")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("FLOP"))
                .andExpect(jsonPath("$.combination").value("High card"))
                .andExpect(jsonPath("$.unseenCards").value(47))
                .andExpect(jsonPath("$.outsCount").value(10))
                .andExpect(jsonPath("$.outs[0].combination").value("Straight"))
                .andExpect(jsonPath("$.outs[0].cards.length()").value(4))
                .andExpect(jsonPath("$.outs[1].combination").value("Pair"))
                .andExpect(jsonPath("$.nextCardProbability").value(closeTo(10.0 / 47, 1e-9), Double.class))
                .andExpect(jsonPath("$.byRiverProbability").value(closeTo(451.0 / 1081, 1e-9), Double.class))
                .andExpect(jsonPath("$.runnerRunnerProbability").value(closeTo(36.0 / 1081, 1e-9), Double.class))
                .andExpect(jsonPath("$.byRiver[0].combination").value("Flush"));
    }

    @Test
    void riverRequestReturnsOnlyTheCombination() throws Exception {
        postAnalyze("{ \"hole\": [\"As\",\"Ks\"], \"board\": [\"5s\",\"Jc\",\"Qd\",\"2h\",\"Tc\"] }")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("RIVER"))
                .andExpect(jsonPath("$.combination").value("Straight"))
                .andExpect(jsonPath("$.outs").doesNotExist());
    }

    @Test
    void duplicateCardIsBadRequest() throws Exception {
        postAnalyze("{ \"hole\": [\"As\",\"Ks\"], \"board\": [\"As\",\"Jc\",\"Qd\"] }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Duplicate")));
    }

    @Test
    void wrongBoardSizeIsBadRequest() throws Exception {
        postAnalyze("{ \"hole\": [\"As\",\"Ks\"], \"board\": [\"5s\",\"Jc\"] }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("3, 4 or 5")));
    }

    @Test
    void invalidCardIsBadRequest() throws Exception {
        postAnalyze("{ \"hole\": [\"As\",\"Kx\"], \"board\": [\"5s\",\"Jc\",\"Qd\"] }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
