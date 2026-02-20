# LLM-Generated Unit Tests

This folder contains LLM-generated JUnit 5 unit tests for the `calculator` project.

## Test files

| File | Class under test | Description |
|------|-----------------|-------------|
| `com/example/calculator/CalculatorMathTest.java` | `CalculatorMath` | Pure math helpers: factorial, gcd, lcm, fibonacci, permutation, combination, statistics, isPrime |
| `com/example/calculator/StatementSplitterTest.java` | `StatementSplitter` | Semicolon-based statement splitting with parenthesis depth tracking |
| `com/example/calculator/NumberFormatUtilTest.java` | `NumberFormatUtil` | Double-to-string formatting, trailing-zero stripping, special values |
| `com/example/calculator/ExpressionParserTest.java` | `ExpressionParser` | Tokenisation and recursive-descent parsing: arithmetic, comparisons, logical ops, functions |
| `com/example/calculator/CalculatorEngineTest.java` | `CalculatorEngine` | Full engine integration: constants, variables, compound assignment, memory, multi-statement, all built-in functions |
| `com/example/calculator/CalculatorCommandProcessorTest.java` | `CalculatorCommandProcessor` | REPL commands: expressions, meta commands (`:help`, `:vars`, …), memory commands (MS/MR/M+/M-/MC), history recall |

## How it works

The `llm/` directory is registered as an additional Maven **test source root** via `build-helper-maven-plugin` in `pom.xml`.  
Because the test classes are in the same package (`com.example.calculator`) as the production code, they can access package-private members.

No changes to the production source tree are required.

## Running the tests

```powershell
.\llm\run-tests.ps1
```

Or run manually with Maven:

```powershell
mvn test
```

Coverage reports (HTML) are generated automatically by JaCoCo and written to:

```
target/site/jacoco/index.html
```
