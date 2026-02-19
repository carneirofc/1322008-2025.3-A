package com.example.calculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalculatorEngine {
    private static final Pattern ASSIGNMENT_PATTERN = Pattern.compile(
            "^([A-Za-z_][A-Za-z0-9_]*)\\s*(=|\\+=|-=|\\*=|/=|%=|\\^=)\\s*(.+)$"
    );

    private final Random random;
    private final CalculatorState state;
    private final Map<String, FunctionDefinition> functions;

    public CalculatorEngine() {
        this(new Random());
    }

    CalculatorEngine(Random random) {
        this.random = random;
        this.state = new CalculatorState(defaultConstants());
        this.functions = BuiltinFunctionCatalog.create(random);
    }

    public synchronized double evaluate(String statement) {
        List<Double> results = evaluateAll(statement);
        return results.get(results.size() - 1);
    }

    public synchronized List<Double> evaluateAll(String statement) {
        String input = statement == null ? "" : statement.trim();
        if (input.isEmpty()) {
            throw new CalculatorException("Input is empty.");
        }

        List<String> statements = StatementSplitter.split(input);
        if (statements.isEmpty()) {
            throw new CalculatorException("Input is empty.");
        }

        List<Double> results = new ArrayList<>(statements.size());
        for (String current : statements) {
            double result = evaluateSingle(current);
            results.add(result);
        }
        return Collections.unmodifiableList(results);
    }

    public synchronized Map<String, Double> getVariablesSnapshot() {
        return state.getVariablesSnapshot();
    }

    public synchronized Map<String, Double> getConstantsSnapshot() {
        return Collections.unmodifiableMap(state.getConstantsSnapshot());
    }

    public synchronized Map<String, String> getFunctionsHelp() {
        Map<String, String> details = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (FunctionDefinition definition : functions.values()) {
            details.put(definition.name(), definition.description());
        }
        return details;
    }

    public synchronized void reset() {
        state.reset();
    }

    public synchronized double getAns() {
        return state.getAns();
    }

    public synchronized double memoryRecall() {
        return state.getMemory();
    }

    public synchronized void memoryStore(double value) {
        state.setMemory(validateFinite(value, "Memory value"));
    }

    public synchronized void memoryAdd(double value) {
        state.setMemory(validateFinite(state.getMemory() + value, "Memory value"));
    }

    public synchronized void memorySubtract(double value) {
        state.setMemory(validateFinite(state.getMemory() - value, "Memory value"));
    }

    public synchronized void memoryClear() {
        state.setMemory(0d);
    }

    public synchronized void setRandomSeed(long seed) {
        random.setSeed(seed);
    }

    public synchronized boolean deleteVariable(String name) {
        if (state.isConstant(name)) {
            throw new CalculatorException("Cannot delete constant: " + name);
        }
        return state.removeVariable(name);
    }

    private double evaluateSingle(String input) {
        Matcher assignment = ASSIGNMENT_PATTERN.matcher(input);
        if (assignment.matches()) {
            String name = assignment.group(1);
            String operator = assignment.group(2);
            String expression = assignment.group(3);
            return assign(name, operator, expression);
        }

        double result = parseExpression(input);
        state.setAns(validateFinite(result, "Expression result"));
        return state.getAns();
    }

    private double assign(String name, String operator, String expression) {
        if (isReservedName(name)) {
            throw new CalculatorException("Cannot assign to reserved name: " + name);
        }
        double rhs = parseExpression(expression);
        double current = state.hasVariable(name) ? state.getVariable(name) : 0d;
        double result;
        switch (operator) {
            case "=":
                result = rhs;
                break;
            case "+=":
                result = current + rhs;
                break;
            case "-=":
                result = current - rhs;
                break;
            case "*=":
                result = current * rhs;
                break;
            case "/=":
                if (rhs == 0d) {
                    throw new CalculatorException("Division by zero in '/=' assignment.");
                }
                result = current / rhs;
                break;
            case "%=":
                if (rhs == 0d) {
                    throw new CalculatorException("Modulo by zero in '%=' assignment.");
                }
                result = current % rhs;
                break;
            case "^=":
                result = Math.pow(current, rhs);
                break;
            default:
                throw new CalculatorException("Unsupported assignment operator: " + operator);
        }
        result = validateFinite(result, "Assignment result");
        state.putVariable(name, result);
        state.setAns(result);
        return result;
    }

    private boolean isReservedName(String name) {
        return "ans".equalsIgnoreCase(name)
                || "memory".equalsIgnoreCase(name)
                || "mem".equalsIgnoreCase(name)
                || state.isConstant(name)
                || functions.containsKey(name);
    }

    private double parseExpression(String expression) {
        ExpressionParser parser = new ExpressionParser(
                expression,
                this::resolveSymbol,
                this::invokeFunction
        );
        return validateFinite(parser.parse(), "Expression result");
    }

    private double resolveSymbol(String symbol) {
        if ("ans".equalsIgnoreCase(symbol)) {
            return state.getAns();
        }
        if ("memory".equalsIgnoreCase(symbol) || "mem".equalsIgnoreCase(symbol)) {
            return state.getMemory();
        }
        if (state.hasVariable(symbol)) {
            return state.getVariable(symbol);
        }
        if (state.hasConstant(symbol)) {
            return state.resolveConstant(symbol);
        }
        throw new CalculatorException("Unknown symbol: " + symbol);
    }

    private double invokeFunction(String name, List<Double> args) {
        FunctionDefinition definition = functions.get(name);
        if (definition == null) {
            throw new CalculatorException("Unknown function: " + name);
        }
        if (args.size() < definition.minArgs()) {
            throw new CalculatorException(name + " requires at least " + definition.minArgs() + " argument(s).");
        }
        if (definition.maxArgs() >= 0 && args.size() > definition.maxArgs()) {
            throw new CalculatorException(name + " accepts at most " + definition.maxArgs() + " argument(s).");
        }
        return validateFinite(definition.implementation().apply(args), "Function result");
    }

    private static double validateFinite(double value, String label) {
        if (!Double.isFinite(value)) {
            throw new CalculatorException(label + " is not finite.");
        }
        return value;
    }

    private static Map<String, Double> defaultConstants() {
        Map<String, Double> constants = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        constants.put("pi", Math.PI);
        constants.put("e", Math.E);
        constants.put("tau", Math.PI * 2d);
        constants.put("phi", (1d + Math.sqrt(5d)) / 2d);
        return constants;
    }
}
