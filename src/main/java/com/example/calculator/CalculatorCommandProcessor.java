package com.example.calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class CalculatorCommandProcessor {
    private final CalculatorEngine engine;
    private final List<HistoryEntry> history = new ArrayList<>();
    private int historyCounter;

    CalculatorCommandProcessor(CalculatorEngine engine) {
        this.engine = engine;
    }

    CommandResult process(String line) {
        if (line == null || line.trim().isEmpty()) {
            return CommandResult.empty();
        }
        String normalized = line.trim();
        if (isExitCommand(normalized)) {
            return CommandResult.exit();
        }
        if (normalized.startsWith("!")) {
            return processHistoryRecall(normalized);
        }
        if (normalized.startsWith(":")) {
            return processMetaCommand(normalized);
        }
        CommandResult memoryResult = processMemoryCommand(normalized);
        if (memoryResult != null) {
            return memoryResult;
        }
        return processExpression(normalized);
    }

    List<HistoryEntry> historySnapshot() {
        return List.copyOf(history);
    }

    private CommandResult processHistoryRecall(String normalized) {
        String text = normalized.substring(1).trim();
        if (text.isEmpty()) {
            throw new CalculatorException("History recall requires an index. Example: !3");
        }
        int target;
        try {
            target = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            throw new CalculatorException("Invalid history index: " + text);
        }
        String recalled = findHistoryInput(target);
        if (recalled == null) {
            throw new CalculatorException("History index not found: " + target);
        }
        return process(recalled);
    }

    private String findHistoryInput(int index) {
        for (HistoryEntry entry : history) {
            if (entry.index() == index) {
                return entry.input();
            }
        }
        return null;
    }

    private CommandResult processMetaCommand(String line) {
        String[] parts = line.split("\\s+", 2);
        String command = parts[0].toLowerCase(Locale.ROOT);
        String tail = parts.length > 1 ? parts[1].trim() : "";

        return switch (command) {
            case ":help" -> CommandResult.ofLines(helpLines());
            case ":vars" -> CommandResult.ofLines(formatMap("Variables", engine.getVariablesSnapshot()));
            case ":const", ":constants" -> CommandResult.ofLines(formatMap("Constants", engine.getConstantsSnapshot()));
            case ":funcs", ":functions" -> CommandResult.ofLines(formatFunctions(engine.getFunctionsHelp()));
            case ":history" -> CommandResult.ofLines(formatHistory(tail));
            case ":ans" -> CommandResult.ofLines(List.of("ans = " + NumberFormatUtil.format(engine.getAns())));
            case ":memory" -> CommandResult.ofLines(List.of("memory = " + NumberFormatUtil.format(engine.memoryRecall())));
            case ":clear" -> {
                history.clear();
                historyCounter = 0;
                yield CommandResult.ofLines(List.of("History cleared."));
            }
            case ":reset" -> {
                history.clear();
                historyCounter = 0;
                engine.reset();
                yield CommandResult.ofLines(List.of("Calculator reset."));
            }
            case ":del" -> {
                if (tail.isBlank()) {
                    throw new CalculatorException(":del requires a variable name.");
                }
                boolean removed = engine.deleteVariable(tail);
                yield CommandResult.ofLines(List.of(removed ? "Deleted variable: " + tail : "Variable not found: " + tail));
            }
            case ":seed" -> {
                if (tail.isBlank()) {
                    throw new CalculatorException(":seed requires an integer value.");
                }
                long seed;
                try {
                    seed = Long.parseLong(tail);
                } catch (NumberFormatException ex) {
                    throw new CalculatorException("Invalid seed value: " + tail);
                }
                engine.setRandomSeed(seed);
                yield CommandResult.ofLines(List.of("Random seed set to " + seed));
            }
            default -> throw new CalculatorException("Unknown command: " + command + " (use :help)");
        };
    }

    private List<String> formatHistory(String tail) {
        if (history.isEmpty()) {
            return List.of("History: <empty>");
        }
        int limit = history.size();
        if (!tail.isBlank()) {
            try {
                limit = Integer.parseInt(tail);
            } catch (NumberFormatException ex) {
                throw new CalculatorException("Invalid history count: " + tail);
            }
            if (limit <= 0) {
                throw new CalculatorException("History count must be positive.");
            }
        }
        int start = Math.max(0, history.size() - limit);
        List<String> lines = new ArrayList<>();
        for (int i = start; i < history.size(); i++) {
            HistoryEntry entry = history.get(i);
            lines.add(entry.index() + ") " + entry.input() + " => " + entry.output());
        }
        return lines;
    }

    private List<String> formatFunctions(Map<String, String> functions) {
        List<String> lines = new ArrayList<>();
        lines.add("Functions:");
        for (String line : functions.values()) {
            lines.add("  " + line);
        }
        return lines;
    }

    private List<String> formatMap(String title, Map<String, Double> entries) {
        if (entries.isEmpty()) {
            return List.of(title + ": <empty>");
        }
        List<String> lines = new ArrayList<>();
        lines.add(title + ":");
        for (Map.Entry<String, Double> entry : entries.entrySet()) {
            lines.add("  " + entry.getKey() + " = " + NumberFormatUtil.format(entry.getValue()));
        }
        return lines;
    }

    private CommandResult processMemoryCommand(String line) {
        String[] parts = line.split("\\s+", 2);
        String command = parts[0].toUpperCase(Locale.ROOT);
        String tail = parts.length > 1 ? parts[1].trim() : "";

        if ("MR".equals(command)) {
            String output = NumberFormatUtil.format(engine.memoryRecall());
            addHistory("MR", output);
            return CommandResult.ofLines(List.of(output));
        }
        if ("MC".equals(command)) {
            engine.memoryClear();
            return CommandResult.ofLines(List.of("memory = 0"));
        }
        if ("MS".equals(command)) {
            double value = tail.isBlank() ? engine.getAns() : engine.evaluate(tail);
            engine.memoryStore(value);
            String output = "memory = " + NumberFormatUtil.format(engine.memoryRecall());
            addHistory(line, output);
            return CommandResult.ofLines(List.of(output));
        }
        if ("M+".equals(command)) {
            double value = tail.isBlank() ? engine.getAns() : engine.evaluate(tail);
            engine.memoryAdd(value);
            String output = "memory = " + NumberFormatUtil.format(engine.memoryRecall());
            addHistory(line, output);
            return CommandResult.ofLines(List.of(output));
        }
        if ("M-".equals(command)) {
            double value = tail.isBlank() ? engine.getAns() : engine.evaluate(tail);
            engine.memorySubtract(value);
            String output = "memory = " + NumberFormatUtil.format(engine.memoryRecall());
            addHistory(line, output);
            return CommandResult.ofLines(List.of(output));
        }
        return null;
    }

    private CommandResult processExpression(String expression) {
        List<Double> results = engine.evaluateAll(expression);
        List<String> outputLines = new ArrayList<>();
        List<String> statements = StatementSplitter.split(expression);
        for (int i = 0; i < results.size(); i++) {
            String output = NumberFormatUtil.format(results.get(i));
            addHistory(statements.get(i), output);
            outputLines.add(output);
        }
        return CommandResult.ofLines(outputLines);
    }

    private void addHistory(String input, String output) {
        historyCounter++;
        history.add(new HistoryEntry(historyCounter, input, output));
    }

    private boolean isExitCommand(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        return "exit".equals(lower)
                || "quit".equals(lower)
                || ":exit".equals(lower)
                || ":quit".equals(lower);
    }

    static List<String> helpLines() {
        return List.of(
                "Expression features:",
                "  Operators: + - * / % ^ !",
                "  Comparisons: < <= > >= == !=",
                "  Logical: && ||",
                "  Multiple statements: x=5; y=2; x^y",
                "  Variables, constants (pi, e, tau, phi), ans/memory",
                "  Assignment: x = 10, x += 2, x *= 3, x ^= 2",
                "  Functions: :funcs",
                "",
                "Memory commands:",
                "  MS [expr]  Store expression or ans in memory",
                "  MR         Recall memory",
                "  M+ [expr]  Add expression or ans to memory",
                "  M- [expr]  Subtract expression or ans from memory",
                "  MC         Clear memory",
                "",
                "REPL commands:",
                "  :help, :vars, :const, :funcs, :history [n], :ans, :memory",
                "  :clear (history), :reset (all state), :del <var>, :seed <n>, !<index>"
        );
    }
}
