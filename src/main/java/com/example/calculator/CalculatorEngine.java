package com.example.calculator;

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

    private final Map<String, Double> variables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, Double> constants = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, FunctionSpec> functions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Random random = new Random();

    private double ans;
    private double memory;

    public CalculatorEngine() {
        registerConstants();
        registerFunctions();
    }

    public synchronized double evaluate(String statement) {
        String input = statement == null ? "" : statement.trim();
        if (input.isEmpty()) {
            throw new CalculatorException("Input is empty.");
        }

        Matcher assignment = ASSIGNMENT_PATTERN.matcher(input);
        if (assignment.matches()) {
            String name = assignment.group(1);
            String operator = assignment.group(2);
            String expression = assignment.group(3);
            return assign(name, operator, expression);
        }

        double result = parseExpression(input);
        ans = validateFinite(result, "Expression result");
        return ans;
    }

    public synchronized Map<String, Double> getVariablesSnapshot() {
        return new TreeMap<>(variables);
    }

    public synchronized Map<String, Double> getConstantsSnapshot() {
        return Collections.unmodifiableMap(new TreeMap<>(constants));
    }

    public synchronized Map<String, String> getFunctionsHelp() {
        Map<String, String> details = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (FunctionSpec spec : functions.values()) {
            details.put(spec.name, spec.description);
        }
        return details;
    }

    public synchronized void reset() {
        variables.clear();
        ans = 0d;
        memory = 0d;
    }

    public synchronized double getAns() {
        return ans;
    }

    public synchronized double memoryRecall() {
        return memory;
    }

    public synchronized void memoryStore(double value) {
        memory = validateFinite(value, "Memory value");
    }

    public synchronized void memoryAdd(double value) {
        memory = validateFinite(memory + value, "Memory value");
    }

    public synchronized void memorySubtract(double value) {
        memory = validateFinite(memory - value, "Memory value");
    }

    public synchronized void memoryClear() {
        memory = 0d;
    }

    private double assign(String name, String operator, String expression) {
        if (isReservedName(name)) {
            throw new CalculatorException("Cannot assign to reserved name: " + name);
        }
        double rhs = parseExpression(expression);
        double current = variables.getOrDefault(name, 0d);
        double result = switch (operator) {
            case "=" -> rhs;
            case "+=" -> current + rhs;
            case "-=" -> current - rhs;
            case "*=" -> current * rhs;
            case "/=" -> {
                if (rhs == 0d) {
                    throw new CalculatorException("Division by zero in '/=' assignment.");
                }
                yield current / rhs;
            }
            case "%=" -> {
                if (rhs == 0d) {
                    throw new CalculatorException("Modulo by zero in '%=' assignment.");
                }
                yield current % rhs;
            }
            case "^=" -> Math.pow(current, rhs);
            default -> throw new CalculatorException("Unsupported assignment operator: " + operator);
        };
        result = validateFinite(result, "Assignment result");
        variables.put(name, result);
        ans = result;
        return result;
    }

    private boolean isReservedName(String name) {
        return "ans".equalsIgnoreCase(name)
                || "memory".equalsIgnoreCase(name)
                || "mem".equalsIgnoreCase(name)
                || constants.containsKey(name)
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
            return ans;
        }
        if ("memory".equalsIgnoreCase(symbol) || "mem".equalsIgnoreCase(symbol)) {
            return memory;
        }
        if (variables.containsKey(symbol)) {
            return variables.get(symbol);
        }
        if (constants.containsKey(symbol)) {
            return constants.get(symbol);
        }
        throw new CalculatorException("Unknown symbol: " + symbol);
    }

    private double invokeFunction(String name, List<Double> args) {
        FunctionSpec spec = functions.get(name);
        if (spec == null) {
            throw new CalculatorException("Unknown function: " + name);
        }
        if (args.size() < spec.minArgs) {
            throw new CalculatorException(name + " requires at least " + spec.minArgs + " argument(s).");
        }
        if (spec.maxArgs >= 0 && args.size() > spec.maxArgs) {
            throw new CalculatorException(name + " accepts at most " + spec.maxArgs + " argument(s).");
        }
        return validateFinite(spec.implementation.apply(args), "Function result");
    }

    private static double validateFinite(double value, String label) {
        if (!Double.isFinite(value)) {
            throw new CalculatorException(label + " is not finite.");
        }
        return value;
    }

    private void registerConstants() {
        constants.put("pi", Math.PI);
        constants.put("e", Math.E);
        constants.put("tau", Math.PI * 2d);
        constants.put("phi", (1d + Math.sqrt(5d)) / 2d);
    }

    private void registerFunctions() {
        register("abs", 1, 1, "abs(x)", a -> Math.abs(a.get(0)));
        register("sqrt", 1, 1, "sqrt(x)", a -> Math.sqrt(a.get(0)));
        register("cbrt", 1, 1, "cbrt(x)", a -> Math.cbrt(a.get(0)));
        register("sin", 1, 1, "sin(x)", a -> Math.sin(a.get(0)));
        register("cos", 1, 1, "cos(x)", a -> Math.cos(a.get(0)));
        register("tan", 1, 1, "tan(x)", a -> Math.tan(a.get(0)));
        register("asin", 1, 1, "asin(x)", a -> Math.asin(a.get(0)));
        register("acos", 1, 1, "acos(x)", a -> Math.acos(a.get(0)));
        register("atan", 1, 1, "atan(x)", a -> Math.atan(a.get(0)));
        register("sinh", 1, 1, "sinh(x)", a -> Math.sinh(a.get(0)));
        register("cosh", 1, 1, "cosh(x)", a -> Math.cosh(a.get(0)));
        register("tanh", 1, 1, "tanh(x)", a -> Math.tanh(a.get(0)));
        register("floor", 1, 1, "floor(x)", a -> Math.floor(a.get(0)));
        register("ceil", 1, 1, "ceil(x)", a -> Math.ceil(a.get(0)));
        register("round", 1, 1, "round(x)", a -> Math.rint(a.get(0)));
        register("ln", 1, 1, "ln(x)", a -> Math.log(a.get(0)));
        register("log", 1, 1, "log(x)  // base-10", a -> Math.log10(a.get(0)));
        register("exp", 1, 1, "exp(x)", a -> Math.exp(a.get(0)));
        register("sign", 1, 1, "sign(x)", a -> Math.signum(a.get(0)));
        register("deg", 1, 1, "deg(radians)", a -> Math.toDegrees(a.get(0)));
        register("rad", 1, 1, "rad(degrees)", a -> Math.toRadians(a.get(0)));
        register("fact", 1, 1, "fact(n)", a -> CalculatorMath.factorial(a.get(0)));

        register("pow", 2, 2, "pow(base, exponent)", a -> Math.pow(a.get(0), a.get(1)));
        register("root", 2, 2, "root(value, degree)", a -> {
            double value = a.get(0);
            double degree = a.get(1);
            if (degree == 0d) {
                throw new CalculatorException("root(value, degree): degree cannot be zero.");
            }
            if (value < 0d && Math.abs(Math.rint(degree) - degree) < 1e-9 && ((long) Math.rint(degree)) % 2 == 0) {
                throw new CalculatorException("Even root of negative number is not real.");
            }
            return Math.pow(value, 1d / degree);
        });
        register("min", 2, -1, "min(a, b, ...)", a -> {
            double min = Double.POSITIVE_INFINITY;
            for (double value : a) {
                min = Math.min(min, value);
            }
            return min;
        });
        register("max", 2, -1, "max(a, b, ...)", a -> {
            double max = Double.NEGATIVE_INFINITY;
            for (double value : a) {
                max = Math.max(max, value);
            }
            return max;
        });
        register("sum", 1, -1, "sum(a, b, ...)", a -> {
            double total = 0d;
            for (double value : a) {
                total += value;
            }
            return total;
        });
        register("avg", 1, -1, "avg(a, b, ...)", a -> {
            double total = 0d;
            for (double value : a) {
                total += value;
            }
            return total / a.size();
        });
        register("clamp", 3, 3, "clamp(value, min, max)", a -> CalculatorMath.clamp(a.get(0), a.get(1), a.get(2)));
        register("atan2", 2, 2, "atan2(y, x)", a -> Math.atan2(a.get(0), a.get(1)));
        register("hypot", 2, 2, "hypot(x, y)", a -> Math.hypot(a.get(0), a.get(1)));
        register("mod", 2, 2, "mod(a, b)", a -> {
            if (a.get(1) == 0d) {
                throw new CalculatorException("mod(a, b): b cannot be zero.");
            }
            return a.get(0) % a.get(1);
        });
        register("gcd", 2, -1, "gcd(a, b, ...)", CalculatorMath::gcdOf);
        register("lcm", 2, -1, "lcm(a, b, ...)", CalculatorMath::lcmOf);
        register("perm", 2, 2, "perm(n, k)", a -> CalculatorMath.permutation(a.get(0), a.get(1)));
        register("comb", 2, 2, "comb(n, k)", a -> CalculatorMath.combination(a.get(0), a.get(1)));

        register("rand", 0, 2, "rand() | rand(max) | rand(min, max)", a -> {
            if (a.isEmpty()) {
                return random.nextDouble();
            }
            if (a.size() == 1) {
                return random.nextDouble() * a.get(0);
            }
            double min = a.get(0);
            double max = a.get(1);
            if (max < min) {
                throw new CalculatorException("rand(min, max): max cannot be less than min.");
            }
            return min + random.nextDouble() * (max - min);
        });
    }

    private void register(String name, int minArgs, int maxArgs, String description, FunctionImplementation implementation) {
        functions.put(name, new FunctionSpec(name, minArgs, maxArgs, description, implementation));
    }

    private record FunctionSpec(
            String name,
            int minArgs,
            int maxArgs,
            String description,
            FunctionImplementation implementation
    ) {
    }

    @FunctionalInterface
    private interface FunctionImplementation {
        double apply(List<Double> args);
    }
}
