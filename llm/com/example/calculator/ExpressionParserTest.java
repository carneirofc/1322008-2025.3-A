package com.example.calculator;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionParserTest {

    private static double noSymbol(String symbol) {
        throw new CalculatorException("Unknown symbol: " + symbol);
    }

    private static double noFunction(String name, java.util.List<Double> args) {
        throw new CalculatorException("Unknown function: " + name);
    }

    private double parse(String input) {
        return new ExpressionParser(input, ExpressionParserTest::noSymbol, ExpressionParserTest::noFunction).parse();
    }

    // ---- basic arithmetic ----

    @Test
    void parse_integerLiteral() {
        assertEquals(42.0, parse("42"));
    }

    @Test
    void parse_floatLiteral() {
        assertEquals(3.14, parse("3.14"), 1e-15);
    }

    @Test
    void parse_addition() {
        assertEquals(7.0, parse("3 + 4"));
    }

    @Test
    void parse_subtraction() {
        assertEquals(1.0, parse("4 - 3"));
    }

    @Test
    void parse_multiplication() {
        assertEquals(12.0, parse("3 * 4"));
    }

    @Test
    void parse_division() {
        assertEquals(2.5, parse("5 / 2"));
    }

    @Test
    void parse_modulo() {
        assertEquals(1.0, parse("7 % 3"));
    }

    @Test
    void parse_power() {
        assertEquals(8.0, parse("2 ^ 3"));
    }

    @Test
    void parse_factorial() {
        assertEquals(6.0, parse("3!"));
    }

    @Test
    void parse_doubleFactorial() {
        // (3!)! = 6! = 720
        assertEquals(720.0, parse("3!!"));
    }

    // ---- unary ----

    @Test
    void parse_unaryMinus() {
        assertEquals(-5.0, parse("-5"));
    }

    @Test
    void parse_unaryPlus() {
        assertEquals(5.0, parse("+5"));
    }

    @Test
    void parse_doubleUnaryMinus() {
        assertEquals(5.0, parse("--5"));
    }

    // ---- precedence ----

    @Test
    void parse_additionAndMultiplication_respectsPrecedence() {
        assertEquals(14.0, parse("2 + 3 * 4"));
    }

    @Test
    void parse_parenthesesOverridePrecedence() {
        assertEquals(20.0, parse("(2 + 3) * 4"));
    }

    // ---- comparison operators ----

    @Test
    void parse_equalEqual_true() {
        assertEquals(1.0, parse("3 == 3"));
    }

    @Test
    void parse_equalEqual_false() {
        assertEquals(0.0, parse("3 == 4"));
    }

    @Test
    void parse_bangEqual_true() {
        assertEquals(1.0, parse("3 != 4"));
    }

    @Test
    void parse_less_true() {
        assertEquals(1.0, parse("2 < 3"));
    }

    @Test
    void parse_less_false() {
        assertEquals(0.0, parse("3 < 2"));
    }

    @Test
    void parse_lessEqual_equal_true() {
        assertEquals(1.0, parse("3 <= 3"));
    }

    @Test
    void parse_greater_true() {
        assertEquals(1.0, parse("4 > 3"));
    }

    @Test
    void parse_greaterEqual_equal_true() {
        assertEquals(1.0, parse("3 >= 3"));
    }

    // ---- logical operators ----

    @Test
    void parse_andAnd_bothTrue_returnsOne() {
        assertEquals(1.0, parse("1 && 1"));
    }

    @Test
    void parse_andAnd_oneFalse_returnsZero() {
        assertEquals(0.0, parse("1 && 0"));
    }

    @Test
    void parse_orOr_oneFalse_returnsOne() {
        assertEquals(1.0, parse("0 || 1"));
    }

    @Test
    void parse_orOr_bothFalse_returnsZero() {
        assertEquals(0.0, parse("0 || 0"));
    }

    // ---- division by zero ----

    @Test
    void parse_divisionByZero_throws() {
        assertThrows(CalculatorException.class, () -> parse("5 / 0"));
    }

    @Test
    void parse_moduloByZero_throws() {
        assertThrows(CalculatorException.class, () -> parse("5 % 0"));
    }

    // ---- symbol resolver ----

    @Test
    void parse_symbol_resolved() {
        ExpressionParser parser = new ExpressionParser(
                "x + 1",
                symbol -> {
                    if ("x".equals(symbol)) return 10.0;
                    throw new CalculatorException("Unknown: " + symbol);
                },
                ExpressionParserTest::noFunction
        );
        assertEquals(11.0, parser.parse());
    }

    @Test
    void parse_unknownSymbol_throws() {
        assertThrows(CalculatorException.class, () -> parse("unknownVar"));
    }

    // ---- function resolver ----

    @Test
    void parse_functionCall_invoked() {
        ExpressionParser parser = new ExpressionParser(
                "double(5)",
                ExpressionParserTest::noSymbol,
                (name, args) -> {
                    if ("double".equals(name)) return args.get(0) * 2.0;
                    throw new CalculatorException("Unknown: " + name);
                }
        );
        assertEquals(10.0, parser.parse());
    }

    @Test
    void parse_functionCallNoArgs() {
        ExpressionParser parser = new ExpressionParser(
                "zero()",
                ExpressionParserTest::noSymbol,
                (name, args) -> 0.0
        );
        assertEquals(0.0, parser.parse());
    }

    @Test
    void parse_functionCallMultipleArgs() {
        ExpressionParser parser = new ExpressionParser(
                "add(3, 4)",
                ExpressionParserTest::noSymbol,
                (name, args) -> args.get(0) + args.get(1)
        );
        assertEquals(7.0, parser.parse());
    }

    // ---- errors ----

    @Test
    void parse_trailingInput_throws() {
        assertThrows(CalculatorException.class, () -> parse("3 + 4 5"));
    }

    @Test
    void parse_missingCloseParen_throws() {
        assertThrows(CalculatorException.class, () -> parse("(3 + 4"));
    }

    @Test
    void parse_missingFunctionCloseParen_throws() {
        assertThrows(CalculatorException.class, () -> parse("f(3"));
    }

    @Test
    void parse_unexpectedCharacter_throws() {
        assertThrows(CalculatorException.class, () -> parse("3 @ 4"));
    }

    @Test
    void parse_emptyInput_throws() {
        assertThrows(CalculatorException.class, () -> parse(""));
    }

    // ---- scientific notation ----

    @Test
    void parse_scientificNotation_positive() {
        assertEquals(1000.0, parse("1e3"));
    }

    @Test
    void parse_scientificNotation_negative_exponent() {
        assertEquals(0.001, parse("1e-3"), 1e-15);
    }

    @Test
    void parse_invalidExponent_throws() {
        assertThrows(CalculatorException.class, () -> parse("1e"));
    }
}
