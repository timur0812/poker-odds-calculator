package io.github.timur0812.pokerodds.web;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class PokerOddsApplication {

    public static void main(String[] args) {
        // Desktop needs headless off
        new SpringApplicationBuilder(PokerOddsApplication.class)
                .headless(false)
                .run(args);
    }
}
