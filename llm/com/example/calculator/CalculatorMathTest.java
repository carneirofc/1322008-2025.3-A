package com.example.calculator;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorMathTest {

    // ---- requireInteger ----

    @Test
    void requireInteger_wholeDouble_returnsLong() {
        assertEquals(5L, CalculatorMath.requireInteger(5.0, "x"));
    }

    @Test
    void requireInteger_negativeWhole_returnsLong() {
        assertEquals(-3L, CalculatorMath.requireInteger(-3.0, "x"));
    }

    @Test
    void requireInteger_nonWhole_throws() {
        assertThrows(CalculatorException.class, () -> CalculatorMath.requireInteger(1.5, "x"));
    }

    @Test
    void requireInteger_infinite_throws() {
        assertThrows(CalculatorException.class, () -> CalculatorMath.requireInteger(Double.POSITIVE_INFINITY, "x"));
    }

    @Test
    void requireInteger_nan_throws() {
        assertThrows(CalculatorException.class, () -> CalculatorMath.requireInteger(Double.NaN, "x"));
    }

    // ---- requireNonNegativeInteger ----

    @Test
    void requireNonNegativeInteger_zero_returnsZero() {
        assertEquals(0L, CalculatorMath.requireNonNegativeInteger(0.0, "x"));
    }

    @Test
    void requireNonNegativeInteger_positive_returnsLong() {
        assertEquals(7L, CalculatorMath.requireNonNegativeInteger(7.0, "x"));
    }

    @Test
    void requireNonNegativeInteger_negative_throws() {
        assertThrows(CalculatorException.class, () -> CalculatorMath.requireNonNegativeInteger(-1.0, "x"));
    }

    // ---- factorial ----

    @Test
    void factorial_zero_returnsOne() {
        assertEquals(1.0, CalculatorMath.factorial(0.0));
    }

    @Test
    void factorial_one_returnsOne() {
        assertEquals(1.0, CalculatorMath.factorial(1.0));
    }

    @Test
    void factorial_five_returns120() {
        assertEquals(120.0, CalculatorMath.factorial(5.0));
    }

    @Test
    void factorial_ten_returns3628800() {
        assertEquals(3628800.0, CalculatorMath.factorial(10.0));
    }

    @Test
    void factorial_170_finiteResult() {
        assertTrue(Double.isFinite(CalculatorMath.factorial(170.0)));
    }

    @Test
    void factorial_171_throws() {
        assertThrows(CalculatorException.class, () -> CalculatorMath.factorial(171.0));
    }

    @Test
    void factorial_negative_throws() {
        assertThrows(CalculatorException.class, () -> CalculatorMath.factorial(-1.0));
    }

    @Test
    void factorial_nonInteger_throws() {
        assertThrows(CalculatorException.class, () -> CalculatorMath.factorial(2.5));
    }

    // ---- toBoolean ----

    @Test
    void toBoolean_zero_returnsFalse() {
        assertFalse(CalculatorMath.toBoolean(0.0));
    }

    @Test
    void toBoolean_smallEpsilon_returnsFalse() {
        assertFalse(CalculatorMath.toBoolean(1e-13));
    }

    @Test
    void toBoolean_one_returnsTrue() {
        assertTrue(CalculatorMath.toBoolean(1.0));
    }

    @Test
    void toBoolean_negative_returnsTrue() {
        assertTrue(CalculatorMath.toBoolean(-5.0));
    }

    // ---- clamp ----

    @Test
    void clamp_valueInRange_returnsValue() {
        assertEquals(5.0, CalculatorMath.clamp(5.0, 1.0, 10.0));
    }

    @Test
    void clamp_valueBelowMin_returnsMin() {
        assertEquals(1.0, CalculatorMath.clamp(-3.0, 1.0, 10.0));
    }

    @Test
    void clamp_valueAboveMax_returnsMax() {
        assertEquals(10.0, CalculatorMath.clamp(20.0, 1.0, 10.0));
    }

    @Test
    void clamp_minGreaterThanMax_throws() {
        assertThrows(CalculatorException.class, () -> CalculatorMath.clamp(5.0, 10.0, 1.0));
    }

    @Test
    void clamp_minEqualsMax_returnsThatValue() {
        assertEquals(5.0, CalculatorMath.clamp(3.0, 5.0, 5.0));
    }

    // ---- gcd ----

    @Test
    void gcd_basicCase() {
        assertEquals(6L, CalculatorMath.gcd(12L, 18L));
    }

    @Test
    void gcd_withZero_returnsOtherValue() {
        assertEquals(7L, CalculatorMath.gcd(7L, 0L));
        assertEquals(7L, CalculatorMath.gcd(0L, 7L));
    }

    @Test
    void gcd_sameValues_returnsValue() {
        assertEquals(5L, CalculatorMath.gcd(5L, 5L));
    }

    @Test
    void gcd_coprimes_returnsOne() {
        assertEquals(1L, CalculatorMath.gcd(7L, 13L));
    }

    // ---- gcdOf ----

    @Test
    void gcdOf_twoArgs_correct() {
        assertEquals(4.0, CalculatorMath.gcdOf(Arrays.asList(12.0, 8.0)));
    }

    @Test
    void gcdOf_threeArgs_correct() {
        assertEquals(3.0, CalculatorMath.gcdOf(Arrays.asList(9.0, 12.0, 6.0)));
    }

    @Test
    void gcdOf_lessThanTwo_throws() {
        assertThrows(CalculatorException.class, () -> CalculatorMath.gcdOf(Collections.singletonList(5.0)));
    }

    @Test
    void gcdOf_nonInteger_throws() {
        assertThrows(CalculatorException.class, () -> CalculatorMath.gcdOf(Arrays.asList(1.5, 2.0)));
    }

    // ---- lcmOf ----

    @Test
    void lcmOf_twoArgs_correct() {
        assertEquals(12.0, CalculatorMath.lcmOf(Arrays.asList(4.0, 6.0)));
    }

    @Test
    void lcmOf_withZero_returnsZero() {
        assertEquals(0.0, CalculatorMath.lcmOf(Arrays.asList(0.0, 5.0)));
    }

    @Test
    void lcmOf_lessThanTwo_throws() {
        assertThrows(CalculatorException.class, () -> CalculatorMath.lcmOf(Collections.singletonList(5.0)));
    }

    // ---- productOf ----

    @Test
    void productOf_basic() {
        assertEquals(24.0, CalculatorMath.productOf(Arrays.asList(2.0, 3.0, 4.0)));
    }

    @Test
    void productOf_singleElement() {
        assertEquals(7.0, CalculatorMath.productOf(Collections.singletonList(7.0)));
    }

    // ---- averageOf ----

    @Test
    void averageOf_basic() {
        assertEquals(3.0, CalculatorMath.averageOf(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0)));
    }

    @Test
    void averageOf_twoValues() {
        assertEquals(5.0, CalculatorMath.averageOf(Arrays.asList(4.0, 6.0)));
    }

    // ---- medianOf ----

    @Test
    void medianOf_oddCount_returnsMiddle() {
        assertEquals(3.0, CalculatorMath.medianOf(Arrays.asList(5.0, 1.0, 3.0)));
    }

    @Test
    void medianOf_evenCount_returnsAvgMiddleTwo() {
        // sorted: [1, 2, 4, 5] → (2+4)/2 = 3.0
        assertEquals(3.0, CalculatorMath.medianOf(Arrays.asList(1.0, 2.0, 5.0, 4.0)));
    }

    @Test
    void medianOf_singleElement() {
        assertEquals(9.0, CalculatorMath.medianOf(Collections.singletonList(9.0)));
    }

    // ---- varianceOf ----

    @Test
    void varianceOf_constantValues_returnsZero() {
        assertEquals(0.0, CalculatorMath.varianceOf(Arrays.asList(3.0, 3.0, 3.0)));
    }

    @Test
    void varianceOf_basic() {
        // mean=3, deviations: (-2)^2 + (-1)^2 + 0 + 1^2 + 2^2 = 10, variance = 10/5 = 2
        assertEquals(2.0, CalculatorMath.varianceOf(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0)));
    }

    // ---- standardDeviationOf ----

    @Test
    void standardDeviationOf_basic() {
        assertEquals(Math.sqrt(2.0), CalculatorMath.standardDeviationOf(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0)), 1e-12);
    }

    // ---- permutation ----

    @Test
    void permutation_nEquals5_kEquals2_returns20() {
        assertEquals(20.0, CalculatorMath.permutation(5.0, 2.0));
    }

    @Test
    void permutation_kEqualsZero_returnsOne() {
        assertEquals(1.0, CalculatorMath.permutation(10.0, 0.0));
    }

    @Test
    void permutation_kEqualsN_returnsFactorialN() {
        assertEquals(CalculatorMath.factorial(4.0), CalculatorMath.permutation(4.0, 4.0));
    }

    @Test
    void permutation_kGreaterThanN_throws() {
        assertThrows(CalculatorException.class, () -> CalculatorMath.permutation(3.0, 5.0));
    }

    // ---- combination ----

    @Test
    void combination_nEquals5_kEquals2_returns10() {
        assertEquals(10.0, CalculatorMath.combination(5.0, 2.0));
    }

    @Test
    void combination_kEqualsZero_returnsOne() {
        assertEquals(1.0, CalculatorMath.combination(10.0, 0.0));
    }

    @Test
    void combination_kEqualsN_returnsOne() {
        assertEquals(1.0, CalculatorMath.combination(5.0, 5.0));
    }

    @Test
    void combination_kGreaterThanN_throws() {
        assertThrows(CalculatorException.class, () -> CalculatorMath.combination(3.0, 5.0));
    }

    // ---- fibonacci ----

    @Test
    void fibonacci_zero_returnsZero() {
        assertEquals(0.0, CalculatorMath.fibonacci(0.0));
    }

    @Test
    void fibonacci_one_returnsOne() {
        assertEquals(1.0, CalculatorMath.fibonacci(1.0));
    }

    @Test
    void fibonacci_two_returnsOne() {
        assertEquals(1.0, CalculatorMath.fibonacci(2.0));
    }

    @Test
    void fibonacci_ten_returns55() {
        assertEquals(55.0, CalculatorMath.fibonacci(10.0));
    }

    @Test
    void fibonacci_92_finiteResult() {
        assertTrue(Double.isFinite(CalculatorMath.fibonacci(92.0)));
    }

    @Test
    void fibonacci_93_throws() {
        assertThrows(CalculatorException.class, () -> CalculatorMath.fibonacci(93.0));
    }

    @Test
    void fibonacci_negative_throws() {
        assertThrows(CalculatorException.class, () -> CalculatorMath.fibonacci(-1.0));
    }

    // ---- isPrime ----

    @Test
    void isPrime_two_returnsTrue() {
        assertTrue(CalculatorMath.isPrime(2.0));
    }

    @Test
    void isPrime_one_returnsFalse() {
        assertFalse(CalculatorMath.isPrime(1.0));
    }

    @Test
    void isPrime_zero_returnsFalse() {
        assertFalse(CalculatorMath.isPrime(0.0));
    }

    @Test
    void isPrime_negative_returnsFalse() {
        assertFalse(CalculatorMath.isPrime(-5.0));
    }

    @Test
    void isPrime_four_returnsFalse() {
        assertFalse(CalculatorMath.isPrime(4.0));
    }

    @Test
    void isPrime_seventeen_returnsTrue() {
        assertTrue(CalculatorMath.isPrime(17.0));
    }

    @Test
    void isPrime_largeComposite_returnsFalse() {
        assertFalse(CalculatorMath.isPrime(100.0));
    }

    @Test
    void isPrime_nonInteger_throws() {
        assertThrows(CalculatorException.class, () -> CalculatorMath.isPrime(2.5));
    }
}
