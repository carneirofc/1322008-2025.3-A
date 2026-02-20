package com.example.calculator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumberFormatUtilTest {

    @Test
    void format_integerValue_noDecimalPoint() {
        assertEquals("5", NumberFormatUtil.format(5.0));
    }

    @Test
    void format_negativeInteger() {
        assertEquals("-3", NumberFormatUtil.format(-3.0));
    }

    @Test
    void format_decimalValue_trailingZerosStripped() {
        assertEquals("1.5", NumberFormatUtil.format(1.50));
    }

    @Test
    void format_zeroPointFive() {
        assertEquals("0.5", NumberFormatUtil.format(0.5));
    }

    @Test
    void format_largeInteger() {
        assertEquals("1000000", NumberFormatUtil.format(1_000_000.0));
    }

    @Test
    void format_negativeZero_returnsZero() {
        assertEquals("0", NumberFormatUtil.format(-0.0));
    }

    @Test
    void format_positiveZero_returnsZero() {
        assertEquals("0", NumberFormatUtil.format(0.0));
    }

    @Test
    void format_nan_returnsNaN() {
        assertEquals("NaN", NumberFormatUtil.format(Double.NaN));
    }

    @Test
    void format_positiveInfinity_returnsInfinity() {
        assertEquals("Infinity", NumberFormatUtil.format(Double.POSITIVE_INFINITY));
    }

    @Test
    void format_negativeInfinity_returnsNegativeInfinity() {
        assertEquals("-Infinity", NumberFormatUtil.format(Double.NEGATIVE_INFINITY));
    }

    @Test
    void format_verySmallDecimal() {
        String result = NumberFormatUtil.format(0.001);
        assertEquals("0.001", result);
    }

    @Test
    void format_pi_hasDecimalDigits() {
        String result = NumberFormatUtil.format(Math.PI);
        assertTrue(result.contains("."), "PI should have decimal digits");
    }
}
