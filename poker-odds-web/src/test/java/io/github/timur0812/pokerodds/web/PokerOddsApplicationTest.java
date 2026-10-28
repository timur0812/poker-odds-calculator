package io.github.timur0812.pokerodds.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "poker.open-browser=false")
@AutoConfigureMockMvc
class PokerOddsApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BrowserLauncher browserLauncher;

    @Test
    void rootForwardsToThePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));
    }

    @Test
    void servesThePlaceholderPage() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Poker Odds Calculator")));
    }

    @Test
    void browserIsNotOpenedInTests() {
        assertThat(browserLauncher.isEnabled()).isFalse();
        assertThat(browserLauncher.url()).isEqualTo("http://localhost:8080");
    }
}
