package com.example.calculator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorEngineTest {
    private static final double EPS = 1e-9;

    @Test
    void handlesArithmeticPrecedenceAndFactorial() {
        CalculatorEngine engine = new CalculatorEngine();

        assertEquals(14d, engine.evaluate("2 + 3 * 4"), EPS);
        assertEquals(20d, engine.evaluate("(2 + 3) * 4"), EPS);
        assertEquals(512d, engine.evaluate("2 ^ 3 ^ 2"), EPS);
        assertEquals(120d, engine.evaluate("5!"), EPS);
    }

    @Test
    void supportsVariablesAndCompoundAssignment() {
        CalculatorEngine engine = new CalculatorEngine();

        assertEquals(10d, engine.evaluate("x = 10"), EPS);
        assertEquals(15d, engine.evaluate("x += 5"), EPS);
        assertEquals(30d, engine.evaluate("x *= 2"), EPS);
        assertEquals(900d, engine.evaluate("x ^= 2"), EPS);
        assertEquals(900d, engine.evaluate("x"), EPS);
    }

    @Test
    void supportsFunctionsAndConstants() {
        CalculatorEngine engine = new CalculatorEngine();

        assertEquals(Math.PI, engine.evaluate("pi"), EPS);
        assertEquals(4d, engine.evaluate("sqrt(16)"), EPS);
        assertEquals(10d, engine.evaluate("sum(1,2,3,4)"), EPS);
        assertEquals(2.5d, engine.evaluate("avg(1,2,3,4)"), EPS);
        assertEquals(252d, engine.evaluate("perm(10,3)"), EPS);
        assertEquals(120d, engine.evaluate("comb(10,3)"), EPS);
    }

    @Test
    void supportsMemoryOperations() {
        CalculatorEngine engine = new CalculatorEngine();

        engine.memoryStore(10d);
        engine.memoryAdd(2.5d);
        assertEquals(12.5d, engine.memoryRecall(), EPS);

        engine.memorySubtract(1.5d);
        assertEquals(11d, engine.memoryRecall(), EPS);

        engine.memoryClear();
        assertEquals(0d, engine.memoryRecall(), EPS);
    }

    @Test
    void rejectsInvalidExpressions() {
        CalculatorEngine engine = new CalculatorEngine();

        assertThrows(CalculatorException.class, () -> engine.evaluate("2 / 0"));
        assertThrows(CalculatorException.class, () -> engine.evaluate("unknown_var + 1"));
        assertThrows(CalculatorException.class, () -> engine.evaluate("(-2)!"));
    }
}
