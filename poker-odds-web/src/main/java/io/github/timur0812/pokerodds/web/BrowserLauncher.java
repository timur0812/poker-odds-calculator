package io.github.timur0812.pokerodds.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

// Opens the browser when the app is ready (poker.open-browser).
@Component
public class BrowserLauncher {

    private static final Logger log = LoggerFactory.getLogger(BrowserLauncher.class);

    private final boolean openBrowser;
    private final String url;

    public BrowserLauncher(@Value("${poker.open-browser:true}") boolean openBrowser,
                           @Value("${server.port:8080}") int port) {
        this.openBrowser = openBrowser;
        this.url = "http://localhost:" + port;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        if (!openBrowser) {
            return;
        }
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            log.info("Poker Odds Calculator is running at {}", url);
            return;
        }
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            log.info("Could not open a browser ({}). Poker Odds Calculator is running at {}", e.getMessage(), url);
        }
    }

    boolean isEnabled() {
        return openBrowser;
    }

    String url() {
        return url;
    }
}
