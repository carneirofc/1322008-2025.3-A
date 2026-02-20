package com.example.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorEngineTest {

    private CalculatorEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CalculatorEngine();
    }

    // ---- basic arithmetic ----

    @Test
    void evaluate_addition() {
        assertEquals(5.0, engine.evaluate("2 + 3"));
    }

    @Test
    void evaluate_subtraction() {
        assertEquals(1.0, engine.evaluate("4 - 3"));
    }

    @Test
    void evaluate_multiplication() {
        assertEquals(12.0, engine.evaluate("3 * 4"));
    }

    @Test
    void evaluate_division() {
        assertEquals(2.5, engine.evaluate("5 / 2"));
    }

    @Test
    void evaluate_modulo() {
        assertEquals(1.0, engine.evaluate("7 % 3"));
    }

    @Test
    void evaluate_power() {
        assertEquals(8.0, engine.evaluate("2 ^ 3"));
    }

    @Test
    void evaluate_nestedParentheses() {
        assertEquals(10.0, engine.evaluate("2 * (1 + 4)"));
    }

    // ---- empty / null input ----

    @Test
    void evaluate_nullInput_throws() {
        assertThrows(CalculatorException.class, () -> engine.evaluate(null));
    }

    @Test
    void evaluate_blankInput_throws() {
        assertThrows(CalculatorException.class, () -> engine.evaluate("   "));
    }

    // ---- constants ----

    @Test
    void evaluate_pi_closeToPi() {
        assertEquals(Math.PI, engine.evaluate("pi"), 1e-15);
    }

    @Test
    void evaluate_e_closeToE() {
        assertEquals(Math.E, engine.evaluate("e"), 1e-15);
    }

    @Test
    void evaluate_tau_closeToTwoPi() {
        assertEquals(Math.PI * 2, engine.evaluate("tau"), 1e-15);
    }

    @Test
    void evaluate_phi_closeToGoldenRatio() {
        assertEquals((1 + Math.sqrt(5)) / 2, engine.evaluate("phi"), 1e-15);
    }

    @Test
    void getConstantsSnapshot_containsBuiltins() {
        Map<String, Double> consts = engine.getConstantsSnapshot();
        assertTrue(consts.containsKey("pi"));
        assertTrue(consts.containsKey("e"));
        assertTrue(consts.containsKey("tau"));
        assertTrue(consts.containsKey("phi"));
    }

    // ---- variables ----

    @Test
    void evaluate_variableAssignment_returnsValue() {
        assertEquals(10.0, engine.evaluate("x = 10"));
    }

    @Test
    void evaluate_variableUsedAfterAssignment() {
        engine.evaluate("x = 10");
        assertEquals(20.0, engine.evaluate("x * 2"));
    }

    @Test
    void evaluate_compoundAssignmentPlusEquals() {
        engine.evaluate("x = 5");
        assertEquals(8.0, engine.evaluate("x += 3"));
    }

    @Test
    void evaluate_compoundAssignmentMinusEquals() {
        engine.evaluate("x = 5");
        assertEquals(2.0, engine.evaluate("x -= 3"));
    }

    @Test
    void evaluate_compoundAssignmentTimesEquals() {
        engine.evaluate("x = 5");
        assertEquals(15.0, engine.evaluate("x *= 3"));
    }

    @Test
    void evaluate_compoundAssignmentDivideEquals() {
        engine.evaluate("x = 10");
        assertEquals(2.0, engine.evaluate("x /= 5"));
    }

    @Test
    void evaluate_compoundAssignmentModEquals() {
        engine.evaluate("x = 10");
        assertEquals(1.0, engine.evaluate("x %= 3"));
    }

    @Test
    void evaluate_compoundAssignmentPowerEquals() {
        engine.evaluate("x = 2");
        assertEquals(8.0, engine.evaluate("x ^= 3"));
    }

    @Test
    void getVariablesSnapshot_afterAssignment() {
        engine.evaluate("myVar = 42");
        Map<String, Double> vars = engine.getVariablesSnapshot();
        assertTrue(vars.containsKey("myVar"));
        assertEquals(42.0, vars.get("myVar"));
    }

    @Test
    void evaluate_assignToConstant_throws() {
        assertThrows(CalculatorException.class, () -> engine.evaluate("pi = 3"));
    }

    @Test
    void evaluate_assignToAns_throws() {
        assertThrows(CalculatorException.class, () -> engine.evaluate("ans = 5"));
    }

    @Test
    void evaluate_assignToDivisionByZero_throws() {
        engine.evaluate("x = 10");
        assertThrows(CalculatorException.class, () -> engine.evaluate("x /= 0"));
    }

    @Test
    void evaluate_assignToModuloByZero_throws() {
        engine.evaluate("x = 10");
        assertThrows(CalculatorException.class, () -> engine.evaluate("x %= 0"));
    }

    @Test
    void deleteVariable_existingVar_returnsTrue() {
        engine.evaluate("x = 5");
        assertTrue(engine.deleteVariable("x"));
    }

    @Test
    void deleteVariable_nonExistentVar_returnsFalse() {
        assertFalse(engine.deleteVariable("nonExistent"));
    }

    @Test
    void deleteVariable_constant_throws() {
        assertThrows(CalculatorException.class, () -> engine.deleteVariable("pi"));
    }

    // ---- ans ----

    @Test
    void evaluate_ansReferencesPreviousResult() {
        engine.evaluate("10 + 5");
        assertEquals(15.0, engine.evaluate("ans"));
    }

    @Test
    void evaluate_ansUsedInExpression() {
        engine.evaluate("7");
        assertEquals(14.0, engine.evaluate("ans * 2"));
    }

    @Test
    void getAns_initiallyZero() {
        assertEquals(0.0, engine.getAns());
    }

    // ---- memory ----

    @Test
    void memoryRecall_initiallyZero() {
        assertEquals(0.0, engine.memoryRecall());
    }

    @Test
    void memoryStore_thenRecall() {
        engine.memoryStore(42.0);
        assertEquals(42.0, engine.memoryRecall());
    }

    @Test
    void memoryAdd_addsToCurrentMemory() {
        engine.memoryStore(10.0);
        engine.memoryAdd(5.0);
        assertEquals(15.0, engine.memoryRecall());
    }

    @Test
    void memorySubtract_subtractsFromCurrentMemory() {
        engine.memoryStore(10.0);
        engine.memorySubtract(3.0);
        assertEquals(7.0, engine.memoryRecall());
    }

    @Test
    void memoryClear_setsToZero() {
        engine.memoryStore(99.0);
        engine.memoryClear();
        assertEquals(0.0, engine.memoryRecall());
    }

    @Test
    void evaluate_memorySymbol_returnsStoredValue() {
        engine.memoryStore(7.0);
        assertEquals(7.0, engine.evaluate("memory"));
    }

    @Test
    void evaluate_memSymbol_returnsStoredValue() {
        engine.memoryStore(3.0);
        assertEquals(3.0, engine.evaluate("mem"));
    }

    // ---- multiple statements ----

    @Test
    void evaluateAll_twoStatements_returnsBothResults() {
        List<Double> results = engine.evaluateAll("1 + 1; 2 + 2");
        assertEquals(2, results.size());
        assertEquals(2.0, results.get(0));
        assertEquals(4.0, results.get(1));
    }

    @Test
    void evaluateAll_singleStatement_returnsOneResult() {
        List<Double> results = engine.evaluateAll("5 * 5");
        assertEquals(1, results.size());
        assertEquals(25.0, results.get(0));
    }

    @Test
    void evaluate_multiStatement_returnsLastResult() {
        assertEquals(4.0, engine.evaluate("x = 2; x * 2"));
    }

    // ---- functions ----

    @Test
    void evaluate_absFunction() {
        assertEquals(5.0, engine.evaluate("abs(-5)"));
    }

    @Test
    void evaluate_sqrtFunction() {
        assertEquals(4.0, engine.evaluate("sqrt(16)"));
    }

    @Test
    void evaluate_sinFunction() {
        assertEquals(0.0, engine.evaluate("sin(0)"), 1e-15);
    }

    @Test
    void evaluate_cosFunction() {
        assertEquals(1.0, engine.evaluate("cos(0)"), 1e-15);
    }

    @Test
    void evaluate_floorFunction() {
        assertEquals(3.0, engine.evaluate("floor(3.7)"));
    }

    @Test
    void evaluate_ceilFunction() {
        assertEquals(4.0, engine.evaluate("ceil(3.2)"));
    }

    @Test
    void evaluate_roundFunction() {
        assertEquals(4.0, engine.evaluate("round(3.5)"));
    }

    @Test
    void evaluate_logFunction_base10() {
        assertEquals(2.0, engine.evaluate("log(100)"), 1e-12);
    }

    @Test
    void evaluate_lnFunction() {
        assertEquals(1.0, engine.evaluate("ln(e)"), 1e-12);
    }

    @Test
    void evaluate_expFunction() {
        assertEquals(Math.E, engine.evaluate("exp(1)"), 1e-12);
    }

    @Test
    void evaluate_minFunction() {
        assertEquals(2.0, engine.evaluate("min(5, 2, 8, 3)"));
    }

    @Test
    void evaluate_maxFunction() {
        assertEquals(8.0, engine.evaluate("max(5, 2, 8, 3)"));
    }

    @Test
    void evaluate_sumFunction() {
        assertEquals(15.0, engine.evaluate("sum(1, 2, 3, 4, 5)"));
    }

    @Test
    void evaluate_avgFunction() {
        assertEquals(3.0, engine.evaluate("avg(1, 2, 3, 4, 5)"));
    }

    @Test
    void evaluate_medianFunction() {
        assertEquals(3.0, engine.evaluate("median(5, 1, 3)"));
    }

    @Test
    void evaluate_clampFunction() {
        assertEquals(5.0, engine.evaluate("clamp(10, 0, 5)"));
    }

    @Test
    void evaluate_factFunction() {
        assertEquals(120.0, engine.evaluate("fact(5)"));
    }

    @Test
    void evaluate_fibFunction() {
        assertEquals(55.0, engine.evaluate("fib(10)"));
    }

    @Test
    void evaluate_isprimeFunction_prime() {
        assertEquals(1.0, engine.evaluate("isprime(17)"));
    }

    @Test
    void evaluate_isprimeFunction_notPrime() {
        assertEquals(0.0, engine.evaluate("isprime(4)"));
    }

    @Test
    void evaluate_gcdFunction() {
        assertEquals(4.0, engine.evaluate("gcd(12, 8)"));
    }

    @Test
    void evaluate_lcmFunction() {
        assertEquals(12.0, engine.evaluate("lcm(4, 6)"));
    }

    @Test
    void evaluate_permFunction() {
        assertEquals(20.0, engine.evaluate("perm(5, 2)"));
    }

    @Test
    void evaluate_combFunction() {
        assertEquals(10.0, engine.evaluate("comb(5, 2)"));
    }

    @Test
    void evaluate_ifFunction_true() {
        assertEquals(1.0, engine.evaluate("if(1, 1, 0)"));
    }

    @Test
    void evaluate_ifFunction_false() {
        assertEquals(0.0, engine.evaluate("if(0, 1, 0)"));
    }

    @Test
    void evaluate_notFunction_true() {
        assertEquals(0.0, engine.evaluate("not(1)"));
    }

    @Test
    void evaluate_notFunction_false() {
        assertEquals(1.0, engine.evaluate("not(0)"));
    }

    @Test
    void evaluate_pctFunction() {
        assertEquals(50.0, engine.evaluate("pct(50, 100)"), 1e-12);
    }

    @Test
    void evaluate_lerpFunction() {
        assertEquals(5.0, engine.evaluate("lerp(0, 10, 0.5)"));
    }

    @Test
    void evaluate_betweenFunction_inRange() {
        assertEquals(1.0, engine.evaluate("between(5, 1, 10)"));
    }

    @Test
    void evaluate_betweenFunction_outOfRange() {
        assertEquals(0.0, engine.evaluate("between(15, 1, 10)"));
    }

    @Test
    void evaluate_hypotFunction() {
        assertEquals(5.0, engine.evaluate("hypot(3, 4)"), 1e-12);
    }

    @Test
    void evaluate_atan2Function() {
        assertEquals(Math.PI / 4, engine.evaluate("atan2(1, 1)"), 1e-12);
    }

    @Test
    void evaluate_powFunction() {
        assertEquals(8.0, engine.evaluate("pow(2, 3)"));
    }

    @Test
    void evaluate_modFunction() {
        assertEquals(1.0, engine.evaluate("mod(7, 3)"));
    }

    @Test
    void evaluate_logNFunction() {
        assertEquals(3.0, engine.evaluate("logn(8, 2)"), 1e-12);
    }

    @Test
    void evaluate_cbrtFunction() {
        assertEquals(2.0, engine.evaluate("cbrt(8)"), 1e-12);
    }

    @Test
    void evaluate_unknownFunction_throws() {
        assertThrows(CalculatorException.class, () -> engine.evaluate("nonexistentFunc(1)"));
    }

    @Test
    void evaluate_unknownSymbol_throws() {
        assertThrows(CalculatorException.class, () -> engine.evaluate("undefinedVar"));
    }

    // ---- reset ----

    @Test
    void reset_clearsVariablesAndAns() {
        engine.evaluate("x = 5");
        engine.evaluate("10");
        engine.reset();
        assertEquals(0.0, engine.getAns());
        assertTrue(engine.getVariablesSnapshot().isEmpty());
    }

    // ---- getFunctionsHelp ----

    @Test
    void getFunctionsHelp_containsCommonFunctions() {
        Map<String, String> help = engine.getFunctionsHelp();
        assertTrue(help.containsKey("abs"));
        assertTrue(help.containsKey("sqrt"));
        assertTrue(help.containsKey("sin"));
        assertTrue(help.containsKey("gcd"));
    }
}
