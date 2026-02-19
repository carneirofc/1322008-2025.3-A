package com.example.calculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CalculatorApp {
    private final CalculatorCommandProcessor processor;

    public CalculatorApp() {
        this.processor = new CalculatorCommandProcessor(new CalculatorEngine());
    }

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
            case "-h":
            case "--help":
                printCliHelp();
                break;
            case "-e":
            case "--eval":
                if (args.length < 2) {
                    System.err.println("Missing expression for --eval.");
                    return;
                }
                runSingleLine(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                break;
            case "-f":
            case "--file":
                if (args.length < 2) {
                    System.err.println("Missing path for --file.");
                    return;
                }
                runScript(Paths.get(args[1]));
                break;
            default:
                runSingleLine(String.join(" ", args));
                break;
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
                if (!executeLine(line.trim())) {
                    System.out.println("Bye.");
                    break;
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
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                continue;
            }
            try {
                if (!executeLine(line)) {
                    return;
                }
            } catch (CalculatorException ex) {
                System.err.println("Line " + (i + 1) + ": " + ex.getMessage());
            }
        }
    }

    private boolean executeLine(String line) {
        if (line.isEmpty()) {
            return true;
        }

        if (line.equalsIgnoreCase(":load")) {
            throw new CalculatorException(":load requires a script path.");
        }
        if (line.toLowerCase(Locale.ROOT).startsWith(":load ")) {
            String pathText = line.substring(6).trim();
            if (pathText.isEmpty()) {
                throw new CalculatorException(":load requires a script path.");
            }
            runScript(Paths.get(pathText));
            return true;
        }

        CommandResult result = processor.process(line);
        printLines(result.outputLines());
        return !result.exitRequested();
    }

    private void runSingleLine(String line) {
        try {
            executeLine(line);
        } catch (CalculatorException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }

    private void printLines(List<String> lines) {
        for (String output : lines) {
            System.out.println(output);
        }
    }

    private void printCliHelp() {
        System.out.println("Calculator CLI");
        System.out.println("  java -jar calculator.jar --eval \"2 + 2\"");
        System.out.println("  java -jar calculator.jar --file script.calc");
        System.out.println("  mvn exec:java -Dexec.args=\"--eval \\\"sqrt(9)+1\\\"\"");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -h, --help     Show help");
        System.out.println("  -e, --eval     Evaluate expression(s)");
        System.out.println("  -f, --file     Execute expressions from a file");
        System.out.println();
        printReplHelp();
    }

    private void printReplBanner() {
        System.out.println("Java Calculator REPL");
        System.out.println("Type :help for commands. Type exit to quit.");
    }

    private void printReplHelp() {
        for (String line : CalculatorCommandProcessor.helpLines()) {
            System.out.println(line);
        }
        System.out.println("  :load <file>");
    }
}
