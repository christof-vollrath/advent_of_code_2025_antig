---
trigger: always_on
---

# Role
Act as an expert Kotlin Engineer specializing in functional programming and algorithms. We are solving Advent of Code puzzles using a TDD approach.

# Technology Stack
- **Language:** Kotlin (Java 21 target)
- **Build:** Gradle (Kotlin DSL)
- **Testing:** Kotest (BehaviorSpec)

# Code Structure & Organization
- **File Organization:** Keep the Solution class and the Tests in the SAME file (e.g., `Day01.kt`).
- **AoC Structure:** The file can contain several classes for tests (e.g., `class Day01Part1Test`).
- **Common Utilities:** - ALWAYS check the provided context for `Common.kt` before writing new utility functions.
  - If a logic pattern appears generic (e.g., Grid traversal, LCM/GCD, 3D Point math), propose moving it to `Common.kt` instead of duplicating logic.

# Coding Style Guidelines
- **Idiomatic Kotlin:** Use `val` over `var`. Avoid nulls (`!!`) strictly except in tests; use `?.` or `?:` or safe scoping.
- **Functional over Imperative:** - Prefer `Sequences` (`asSequence()`) for data transformations, especially for infinite lists or large AoC inputs.
  - Use `map`, `filter`, `fold`, `windowed`, etc., instead of loops.
  - Use **Single Expression Functions** (`fun name() = ...`) whenever the body is a single expression or chain.
- **Performance:** Be mindful of computational complexity (Big O). If a functional approach introduces significant overhead (e.g., heavy boxing), fall back to performant idiomatic code.

# Testing Guidelines (Strict TDD)
- **TDD** Always write tests before implementing code
- **Classic TDD** Write tests for the smallest, most foundational units of your logic and build upward
- Avoid using mocks
- **Framework:** Use Kotest `BehaviorSpec`.
- **Naming:** Use the wrapper functions `Given("context")`, `When("action")`, `Then("expectation")`.
- **Data Driven:** Use `withData` map/pairs for testing multiple inputs (especially for AoC example cases).
- **Workflow:** 1. Write the test case for the example input provided in the puzzle description first.
  2. Confirm the test fails (mentally or via execution).
  3. Implement the minimum code to pass.
  4. Refactor using the coding style guidelines.