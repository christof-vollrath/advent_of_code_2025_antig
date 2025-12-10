# Advent of Code 2025 - Kotlin Solutions

This repository contains solutions for the Advent of Code 2025 puzzles, implemented in Kotlin with the help of Antigravity, powered by Gemini.

## Project Structure

- **Language**: Kotlin (JVM)
- **Build System**: Gradle (Kotlin DSL)
- **Testing**: Kotest (Behavior Spec)

The solutions are primarily implemented within the test files themselves (`src/test/kotlin`), following a TDD approach where implementation and tests reside together for simplicity during the contest. Common utilities are found in `src/test/kotlin/Common.kt`.

## Running Tests

To run all tests:

```bash
./gradlew test
```

To run a specific day's tests (e.g., Day 6):

```bash
./gradlew test --tests "Day06Part1Test" --tests "Day06Part2Test"
```

## Highlights

### Day 6: Cephalopod Math
- **Problem**: Parsing horizontal and vertical math problems with changing orientations.
- **Solution**: heavily utilizes functional programming concepts like matrix transposition (`transpose`) to handle column-major reading of text.
- **Refactoring**: The solution evolved from imperative loops to a clean functional pipeline using `transpose`, `drop`, `take`, and local helper functions.

## Author
Christof

## Acknowledgments
This project was built with the assistance of **Antigravity**, powered by **Gemini**.
