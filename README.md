# Poker Odds Calculator

A small local web app for No-Limit Texas Hold'em. Pick your two cards and the board,
and it shows your current hand, your outs and the exact chances of improving.
Based on the ideas from "Poker Math Made Easy" by Roy Rounder

## Stack

- Java 20, Maven (multi-module)
- Spring Boot 4.1 (web module only)
- Plain HTML, CSS and JavaScript (no frameworks, no build step)
- JUnit 6, AssertJ

## How it works

- `poker-odds-core` is a pure Java library: cards, hand evaluation and outs.
- `poker-odds-web` is a Spring Boot app with one page and one endpoint, `POST /api/analyze`.
- On the flop and turn, every unseen card is tried to find the outs. On the flop, all
  1,081 turn + river pairs are also tried for the by-river chances. No approximations.
- A card counts as an out only if your own cards play: pairing the board or pairing a
  card lower than the board doesn't count.
- On the river it just shows your final hand.

## How to run

You need JDK 20 or newer and Maven 3.9+.

```bash
mvn clean install
java -jar poker-odds-web/target/poker-odds-web-1.0-SNAPSHOT.jar
```

The browser opens http://localhost:8080 automatically. To use another port:

```bash
java -jar poker-odds-web/target/poker-odds-web-1.0-SNAPSHOT.jar --port=9090
```

In IntelliJ IDEA you can also run `PokerOddsApplication` directly.

The server only listens on `127.0.0.1`, so it is not reachable from other computers.

Run the tests with `mvn clean verify`.
