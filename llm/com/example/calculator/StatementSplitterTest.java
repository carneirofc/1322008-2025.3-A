package com.example.calculator;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatementSplitterTest {

    @Test
    void split_singleStatement_returnsSingleItem() {
        List<String> result = StatementSplitter.split("1 + 2");
        assertEquals(1, result.size());
        assertEquals("1 + 2", result.get(0));
    }

    @Test
    void split_twoStatements_returnsBoth() {
        List<String> result = StatementSplitter.split("x = 5; y = 3");
        assertEquals(2, result.size());
        assertEquals("x = 5", result.get(0));
        assertEquals("y = 3", result.get(1));
    }

    @Test
    void split_threeStatements_returnsAll() {
        List<String> result = StatementSplitter.split("1; 2; 3");
        assertEquals(3, result.size());
    }

    @Test
    void split_semicolonInsideParentheses_notSplit() {
        // semicolons inside parentheses should not split – this is a degenerate case
        // but the splitter only respects depth==0 for semicolons
        List<String> result = StatementSplitter.split("f(a; b)");
        // depth>0 when ; is encountered, so only one statement
        assertEquals(1, result.size());
    }

    @Test
    void split_nestedParentheses_treatedAsSingleStatement() {
        List<String> result = StatementSplitter.split("max(min(1, 2), 3)");
        assertEquals(1, result.size());
        assertEquals("max(min(1, 2), 3)", result.get(0));
    }

    @Test
    void split_trailingSemicolon_ignoredAsBlank() {
        List<String> result = StatementSplitter.split("1 + 2;");
        assertEquals(1, result.size());
        assertEquals("1 + 2", result.get(0));
    }

    @Test
    void split_leadingSemicolon_ignoredAsBlank() {
        List<String> result = StatementSplitter.split("; 1 + 2");
        assertEquals(1, result.size());
        assertEquals("1 + 2", result.get(0));
    }

    @Test
    void split_multipleSemicolonsNoContent_returnsEmpty() {
        List<String> result = StatementSplitter.split(";;;");
        assertEquals(0, result.size());
    }

    @Test
    void split_emptyInput_returnsEmpty() {
        List<String> result = StatementSplitter.split("");
        assertEquals(0, result.size());
    }

    @Test
    void split_whitespaceOnly_returnsEmpty() {
        List<String> result = StatementSplitter.split("   ");
        assertEquals(0, result.size());
    }

    @Test
    void split_unmatchedCloseParen_throws() {
        assertThrows(CalculatorException.class, () -> StatementSplitter.split("1 + )2"));
    }

    @Test
    void split_unmatchedOpenParen_throws() {
        assertThrows(CalculatorException.class, () -> StatementSplitter.split("(1 + 2"));
    }

    @Test
    void split_statementsPreserveWhitespaceTrimmed() {
        List<String> result = StatementSplitter.split("  a = 1  ;  b = 2  ");
        assertEquals(2, result.size());
        assertEquals("a = 1", result.get(0));
        assertEquals("b = 2", result.get(1));
    }
}
