# Java Calculator

Feature-rich command-line calculator built with Java + Maven.

## Requirements

- JDK 17+
- Maven 3.9+

This repo also includes standalone Windows build scripts that use:
`C:\Users\claud\microsoft-jdk-21.0.10-windows-x64\jdk-21.0.10+7`

## Features

- Arithmetic: `+`, `-`, `*`, `/`, `%`, `^`, factorial `!`
- Comparisons: `<`, `<=`, `>`, `>=`, `==`, `!=` (returns `1` or `0`)
- Logical: `&&`, `||` (returns `1` or `0`)
- Multi-statement execution in one line: `x=5; y=2; x^y`
- Parentheses and operator precedence
- Constants: `pi`, `e`, `tau`, `phi`
- Variables and assignments:
  - `x = 10`
  - `x += 2`, `x -= 1`, `x *= 3`, `x /= 2`, `x %= 7`, `x ^= 2`
- Built-in values: `ans`, `memory` (or `mem`)
- Functions:
  - Trig: `sin`, `cos`, `tan`, `asin`, `acos`, `atan`, `sinh`, `cosh`, `tanh`
  - Algebra: `sqrt`, `cbrt`, `pow`, `root`, `abs`, `round`, `floor`, `ceil`, `exp`, `ln`, `log`, `logn`, `lerp`, `pct`
  - Utility/statistics: `sum`, `prod`, `avg`, `median`, `var`, `std`, `min`, `max`, `clamp`, `atan2`, `hypot`, `mod`, `sign`, `if`, `between`, `not`
  - Integer/combinatorics: `gcd`, `lcm`, `perm`, `comb`, `fact`, `fib`, `isprime`
  - Random: `rand()`, `rand(max)`, `rand(min, max)`, `randi(max)`, `randi(min, max)`
- Memory commands: `MS`, `MR`, `M+`, `M-`, `MC`
- REPL commands: `:help`, `:vars`, `:const`, `:funcs`, `:history [n]`, `:ans`, `:memory`, `:clear`, `:reset`, `:del <var>`, `:seed <n>`, `!<historyIndex>`, `:load <file>`
- Batch/script mode via `--file`

## Architecture

- `CalculatorEngine`: pure evaluation layer (expressions, assignments, symbols, functions).
- `CalculatorCommandProcessor`: command layer (meta commands, memory operations, history, replay).
- `CalculatorApp`: thin CLI I/O layer (REPL, file mode, argument parsing).
- This split makes behavior easier to unit-test without console/file coupling.

## Run

```bash
mvn test
mvn exec:java
```

Or using the included PowerShell script (no Maven required for app build/run):

```powershell
.\build.ps1 clean
.\build.ps1 jar
.\build.ps1 run
.\build.ps1 -Task run -AppArgs "--eval","2 + 2"
```

## Usage

Evaluate one expression:

```bash
mvn exec:java -Dexec.args="--eval \"(2 + 3) * 4\""
```

Run a script file:

```bash
mvn exec:java -Dexec.args="--file script.calc"
```

Example script (`script.calc`):

```text
# comments supported
x = 12
y = sqrt(81)
sum(x, y, 5)
MS
M+ 3
MR
```
