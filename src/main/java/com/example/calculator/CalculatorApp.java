package com.example.calculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalculatorApp {
    private final CalculatorEngine engine = new CalculatorEngine();
    private final List<HistoryEntry> history = new ArrayList<>();
    private int historyCounter;

    public static void main(String[] args) {
        new CalculatorApp().run(args);
    }

    private void run(String[] args) {
        if (args.length == 0) {
            runRepl();
            return;
        }

        String first = args[0];
        switch (first) {
            case "-h", "--help" -> printCliHelp();
            case "-e", "--eval" -> {
                if (args.length < 2) {
                    System.err.println("Missing expression for --eval.");
                    return;
                }
                evaluateAndPrint(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
            }
            case "-f", "--file" -> {
                if (args.length < 2) {
                    System.err.println("Missing path for --file.");
                    return;
                }
                runScript(Path.of(args[1]));
            }
            default -> evaluateAndPrint(String.join(" ", args));
        }
    }

    private void runRepl() {
        printReplBanner();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            while (true) {
                System.out.print("> ");
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (isExitCommand(line)) {
                    System.out.println("Bye.");
                    break;
                }
                try {
                    executeLine(line, true);
                } catch (CalculatorException ex) {
                    System.err.println("Error: " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex.getMessage());
        }
    }

    private void runScript(Path path) {
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.err.println("Could not read script file: " + path + " (" + ex.getMessage() + ")");
            return;
        }

        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i);
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                continue;
            }
            if (isExitCommand(line)) {
                return;
            }
            try {
                executeLine(line, true);
            } catch (CalculatorException ex) {
                System.err.println("Line " + (i + 1) + ": " + ex.getMessage());
            }
        }
    }

    private void executeLine(String line, boolean printResult) {
        String normalized = line.trim();

        if (handleMetaCommand(normalized)) {
            return;
        }
        if (handleMemoryCommand(normalized, printResult)) {
            return;
        }

        double result = engine.evaluate(normalized);
        String output = NumberFormatUtil.format(result);
        addHistory(normalized, output);
        if (printResult) {
            System.out.println(output);
        }
    }

    private boolean handleMetaCommand(String line) {
        if (!line.startsWith(":")) {
            return false;
        }
        String[] parts = line.split("\\s+", 2);
        String command = parts[0].toLowerCase(Locale.ROOT);

        switch (command) {
            case ":help" -> printReplHelp();
            case ":vars" -> printMap("Variables", engine.getVariablesSnapshot());
            case ":const", ":constants" -> printMap("Constants", engine.getConstantsSnapshot());
            case ":funcs", ":functions" -> printFunctions();
            case ":history" -> printHistory();
            case ":ans" -> System.out.println("ans = " + NumberFormatUtil.format(engine.getAns()));
            case ":memory" -> System.out.println("memory = " + NumberFormatUtil.format(engine.memoryRecall()));
            case ":clear" -> {
                history.clear();
                historyCounter = 0;
                System.out.println("History cleared.");
            }
            case ":reset" -> {
                history.clear();
                historyCounter = 0;
                engine.reset();
                System.out.println("Calculator reset.");
            }
            case ":load" -> {
                if (parts.length < 2 || parts[1].isBlank()) {
                    System.err.println(":load requires a script path.");
                } else {
                    runScript(Path.of(parts[1].trim()));
                }
            }
            default -> System.err.println("Unknown command: " + command + " (use :help)");
        }
        return true;
    }

    private boolean handleMemoryCommand(String line, boolean printResult) {
        String[] parts = line.split("\\s+", 2);
        String command = parts[0].toUpperCase(Locale.ROOT);
        String tail = parts.length > 1 ? parts[1].trim() : "";

        if ("MR".equals(command)) {
            String output = NumberFormatUtil.format(engine.memoryRecall());
            addHistory(line, output);
            if (printResult) {
                System.out.println(output);
            }
            return true;
        }
        if ("MC".equals(command)) {
            engine.memoryClear();
            if (printResult) {
                System.out.println("memory = 0");
            }
            return true;
        }
        if ("MS".equals(command)) {
            double value = tail.isBlank() ? engine.getAns() : engine.evaluate(tail);
            engine.memoryStore(value);
            if (printResult) {
                System.out.println("memory = " + NumberFormatUtil.format(engine.memoryRecall()));
            }
            return true;
        }
        if ("M+".equals(command)) {
            double value = tail.isBlank() ? engine.getAns() : engine.evaluate(tail);
            engine.memoryAdd(value);
            if (printResult) {
                System.out.println("memory = " + NumberFormatUtil.format(engine.memoryRecall()));
            }
            return true;
        }
        if ("M-".equals(command)) {
            double value = tail.isBlank() ? engine.getAns() : engine.evaluate(tail);
            engine.memorySubtract(value);
            if (printResult) {
                System.out.println("memory = " + NumberFormatUtil.format(engine.memoryRecall()));
            }
            return true;
        }
        return false;
    }

    private boolean isExitCommand(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        return "exit".equals(lower)
                || "quit".equals(lower)
                || ":exit".equals(lower)
                || ":quit".equals(lower);
    }

    private void evaluateAndPrint(String expression) {
        try {
            executeLine(expression, true);
        } catch (CalculatorException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }

    private void printMap(String title, Map<String, Double> entries) {
        if (entries.isEmpty()) {
            System.out.println(title + ": <empty>");
            return;
        }
        System.out.println(title + ":");
        for (Map.Entry<String, Double> entry : entries.entrySet()) {
            System.out.printf("  %s = %s%n", entry.getKey(), NumberFormatUtil.format(entry.getValue()));
        }
    }

    private void printFunctions() {
        Map<String, String> functions = engine.getFunctionsHelp();
        System.out.println("Functions:");
        for (String line : functions.values()) {
            System.out.println("  " + line);
        }
    }

    private void printHistory() {
        if (history.isEmpty()) {
            System.out.println("History: <empty>");
            return;
        }
        for (HistoryEntry entry : history) {
            System.out.printf("%d) %s => %s%n", entry.index(), entry.input(), entry.output());
        }
    }

    private void addHistory(String input, String output) {
        historyCounter++;
        history.add(new HistoryEntry(historyCounter, input, output));
    }

    private void printCliHelp() {
        System.out.println("Calculator CLI");
        System.out.println("  java -jar calculator.jar --eval \"2 + 2\"");
        System.out.println("  java -jar calculator.jar --file script.calc");
        System.out.println("  mvn exec:java -Dexec.args=\"--eval \\\"sqrt(9)+1\\\"\"");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -h, --help     Show help");
        System.out.println("  -e, --eval     Evaluate a single expression");
        System.out.println("  -f, --file     Execute expressions from a file");
        System.out.println();
        printReplHelp();
    }

    private void printReplBanner() {
        System.out.println("Java Calculator REPL");
        System.out.println("Type :help for commands. Type exit to quit.");
    }

    private void printReplHelp() {
        System.out.println("Expression features:");
        System.out.println("  Operators: + - * / % ^ !");
        System.out.println("  Parentheses, variables, constants (pi, e, tau, phi), ans/memory");
        System.out.println("  Assignment: x = 10, x += 2, x *= 3, x ^= 2");
        System.out.println("  Functions: :funcs");
        System.out.println();
        System.out.println("Memory commands:");
        System.out.println("  MS [expr]  Store expression or ans in memory");
        System.out.println("  MR         Recall memory");
        System.out.println("  M+ [expr]  Add expression or ans to memory");
        System.out.println("  M- [expr]  Subtract expression or ans from memory");
        System.out.println("  MC         Clear memory");
        System.out.println();
        System.out.println("REPL commands:");
        System.out.println("  :help, :vars, :const, :funcs, :history, :ans, :memory");
        System.out.println("  :clear (history), :reset (all state), :load <file>");
    }
}
