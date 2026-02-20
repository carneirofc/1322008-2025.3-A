package com.example.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorCommandProcessorTest {

    private CalculatorEngine engine;
    private CalculatorCommandProcessor processor;

    @BeforeEach
    void setUp() {
        engine = new CalculatorEngine();
        processor = new CalculatorCommandProcessor(engine);
    }

    // ---- empty / null ----

    @Test
    void process_nullInput_returnsEmpty() {
        CommandResult result = processor.process(null);
        assertFalse(result.exitRequested());
        assertTrue(result.outputLines().isEmpty());
    }

    @Test
    void process_blankInput_returnsEmpty() {
        CommandResult result = processor.process("   ");
        assertFalse(result.exitRequested());
        assertTrue(result.outputLines().isEmpty());
    }

    // ---- exit commands ----

    @Test
    void process_exit_requestsExit() {
        assertTrue(processor.process("exit").exitRequested());
    }

    @Test
    void process_quit_requestsExit() {
        assertTrue(processor.process("quit").exitRequested());
    }

    @Test
    void process_colonExit_requestsExit() {
        assertTrue(processor.process(":exit").exitRequested());
    }

    @Test
    void process_colonQuit_requestsExit() {
        assertTrue(processor.process(":quit").exitRequested());
    }

    // ---- expression ----

    @Test
    void process_simpleExpression_returnsFormattedResult() {
        CommandResult result = processor.process("2 + 3");
        assertFalse(result.exitRequested());
        assertEquals(1, result.outputLines().size());
        assertEquals("5", result.outputLines().get(0));
    }

    @Test
    void process_multiStatementExpression_returnsMultipleLines() {
        CommandResult result = processor.process("1 + 1; 2 + 2");
        assertEquals(2, result.outputLines().size());
        assertEquals("2", result.outputLines().get(0));
        assertEquals("4", result.outputLines().get(1));
    }

    // ---- meta commands ----

    @Test
    void process_helpCommand_returnsLines() {
        CommandResult result = processor.process(":help");
        assertFalse(result.outputLines().isEmpty());
        assertTrue(result.outputLines().stream().anyMatch(l -> l.contains("Operators")));
    }

    @Test
    void process_varsCommand_empty_returnsEmptyMessage() {
        CommandResult result = processor.process(":vars");
        assertEquals(1, result.outputLines().size());
        assertTrue(result.outputLines().get(0).contains("empty"));
    }

    @Test
    void process_varsCommand_withVariable_showsVariable() {
        processor.process("myVar = 42");
        CommandResult result = processor.process(":vars");
        assertTrue(result.outputLines().stream().anyMatch(l -> l.contains("myVar")));
    }

    @Test
    void process_constCommand_showsBuiltins() {
        CommandResult result = processor.process(":const");
        assertTrue(result.outputLines().stream().anyMatch(l -> l.contains("pi")));
    }

    @Test
    void process_constantsAlias_showsBuiltins() {
        CommandResult result = processor.process(":constants");
        assertTrue(result.outputLines().stream().anyMatch(l -> l.contains("e")));
    }

    @Test
    void process_funcsCommand_showsFunctions() {
        CommandResult result = processor.process(":funcs");
        assertTrue(result.outputLines().stream().anyMatch(l -> l.contains("abs")));
    }

    @Test
    void process_functionsAlias_showsFunctions() {
        CommandResult result = processor.process(":functions");
        assertTrue(result.outputLines().stream().anyMatch(l -> l.contains("sqrt")));
    }

    @Test
    void process_historyCommand_emptyAtStart() {
        CommandResult result = processor.process(":history");
        assertEquals(1, result.outputLines().size());
        assertTrue(result.outputLines().get(0).contains("empty"));
    }

    @Test
    void process_historyCommand_afterExpressions() {
        processor.process("1 + 1");
        processor.process("2 + 2");
        CommandResult result = processor.process(":history");
        assertEquals(2, result.outputLines().size());
    }

    @Test
    void process_historyCommandWithLimit() {
        processor.process("1");
        processor.process("2");
        processor.process("3");
        CommandResult result = processor.process(":history 2");
        assertEquals(2, result.outputLines().size());
    }

    @Test
    void process_historyCommandInvalidCount_throws() {
        processor.process("1");   // ensure history is non-empty
        assertThrows(CalculatorException.class, () -> processor.process(":history abc"));
    }

    @Test
    void process_historyCommandZeroCount_throws() {
        processor.process("1");   // ensure history is non-empty
        assertThrows(CalculatorException.class, () -> processor.process(":history 0"));
    }

    @Test
    void process_ansCommand_returnsCurrentAns() {
        processor.process("5 + 5");
        CommandResult result = processor.process(":ans");
        assertTrue(result.outputLines().get(0).contains("10"));
    }

    @Test
    void process_memoryCommand_returnsCurrentMemory() {
        processor.process("MS 7");
        CommandResult result = processor.process(":memory");
        assertTrue(result.outputLines().get(0).contains("7"));
    }

    @Test
    void process_clearCommand_clearsHistory() {
        processor.process("1 + 1");
        processor.process(":clear");
        CommandResult result = processor.process(":history");
        assertTrue(result.outputLines().get(0).contains("empty"));
    }

    @Test
    void process_resetCommand_resetsEverything() {
        processor.process("x = 99");
        processor.process(":reset");
        CommandResult result = processor.process(":vars");
        assertTrue(result.outputLines().get(0).contains("empty"));
    }

    @Test
    void process_delCommand_deletesVariable() {
        processor.process("x = 5");
        CommandResult result = processor.process(":del x");
        assertTrue(result.outputLines().get(0).contains("Deleted"));
    }

    @Test
    void process_delCommand_variableNotFound() {
        CommandResult result = processor.process(":del nonexistent");
        assertTrue(result.outputLines().get(0).contains("not found"));
    }

    @Test
    void process_delCommandNoArg_throws() {
        assertThrows(CalculatorException.class, () -> processor.process(":del"));
    }

    @Test
    void process_seedCommand_setsRandomSeed() {
        CommandResult result = processor.process(":seed 42");
        assertTrue(result.outputLines().get(0).contains("42"));
    }

    @Test
    void process_seedCommandInvalidValue_throws() {
        assertThrows(CalculatorException.class, () -> processor.process(":seed notanumber"));
    }

    @Test
    void process_seedCommandNoArg_throws() {
        assertThrows(CalculatorException.class, () -> processor.process(":seed"));
    }

    @Test
    void process_unknownMetaCommand_throws() {
        assertThrows(CalculatorException.class, () -> processor.process(":unknown"));
    }

    // ---- memory commands ----

    @Test
    void process_mrCommand_returnsMemoryValue() {
        engine.memoryStore(5.0);
        CommandResult result = processor.process("MR");
        assertEquals("5", result.outputLines().get(0));
    }

    @Test
    void process_mcCommand_clearsMemory() {
        engine.memoryStore(9.0);
        processor.process("MC");
        assertEquals(0.0, engine.memoryRecall());
    }

    @Test
    void process_msCommand_storesAns() {
        processor.process("10");
        processor.process("MS");
        assertEquals(10.0, engine.memoryRecall());
    }

    @Test
    void process_msCommandWithExpression_storesExpression() {
        processor.process("MS 7 + 3");
        assertEquals(10.0, engine.memoryRecall());
    }

    @Test
    void process_mPlusCommand_addsToMemory() {
        engine.memoryStore(5.0);
        processor.process("M+ 3");
        assertEquals(8.0, engine.memoryRecall());
    }

    @Test
    void process_mMinusCommand_subtractsFromMemory() {
        engine.memoryStore(10.0);
        processor.process("M- 3");
        assertEquals(7.0, engine.memoryRecall());
    }

    @Test
    void process_mPlusNoArg_usesAns() {
        processor.process("5");
        engine.memoryStore(2.0);
        processor.process("M+");
        assertEquals(7.0, engine.memoryRecall());
    }

    // ---- history recall ----

    @Test
    void process_historyRecall_replaysEntry() {
        processor.process("3 + 4");
        CommandResult result = processor.process("!1");
        assertEquals("7", result.outputLines().get(0));
    }

    @Test
    void process_historyRecallInvalidIndex_throws() {
        assertThrows(CalculatorException.class, () -> processor.process("!99"));
    }

    @Test
    void process_historyRecallNonNumeric_throws() {
        assertThrows(CalculatorException.class, () -> processor.process("!abc"));
    }

    @Test
    void process_historyRecallEmpty_throws() {
        assertThrows(CalculatorException.class, () -> processor.process("!"));
    }

    // ---- historySnapshot ----

    @Test
    void historySnapshot_afterExpressions_hasEntries() {
        processor.process("5");
        processor.process("10");
        List<HistoryEntry> snapshot = processor.historySnapshot();
        assertEquals(2, snapshot.size());
        assertEquals(1, snapshot.get(0).index());
        assertEquals(2, snapshot.get(1).index());
    }

    @Test
    void historySnapshot_isUnmodifiable() {
        processor.process("5");
        List<HistoryEntry> snapshot = processor.historySnapshot();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(new HistoryEntry(99, "x", "y")));
    }

    // ---- helpLines static method ----

    @Test
    void helpLines_notEmpty() {
        List<String> lines = CalculatorCommandProcessor.helpLines();
        assertFalse(lines.isEmpty());
    }
}
