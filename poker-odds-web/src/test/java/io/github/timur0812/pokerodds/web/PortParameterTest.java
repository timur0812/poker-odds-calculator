package io.github.timur0812.pokerodds.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

// --port sets the server port and the browser URL.
@SpringBootTest(args = "--port=9123", properties = "poker.open-browser=false")
class PortParameterTest {

    @Value("${server.port}")
    private int serverPort;

    @Autowired
    private BrowserLauncher browserLauncher;

    @Test
    void portArgumentSetsTheServerPort() {
        assertThat(serverPort).isEqualTo(9123);
        assertThat(browserLauncher.url()).isEqualTo("http://localhost:9123");
    }
}
