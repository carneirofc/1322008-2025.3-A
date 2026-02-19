package com.example.calculator;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

final class BuiltinFunctionCatalog {
    private BuiltinFunctionCatalog() {
    }

    static Map<String, FunctionDefinition> create(Random random) {
        Map<String, FunctionDefinition> functions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        register(functions, "abs", 1, 1, "abs(x)", a -> Math.abs(a.get(0)));
        register(functions, "sqrt", 1, 1, "sqrt(x)", a -> Math.sqrt(a.get(0)));
        register(functions, "cbrt", 1, 1, "cbrt(x)", a -> Math.cbrt(a.get(0)));
        register(functions, "sin", 1, 1, "sin(x)", a -> Math.sin(a.get(0)));
        register(functions, "cos", 1, 1, "cos(x)", a -> Math.cos(a.get(0)));
        register(functions, "tan", 1, 1, "tan(x)", a -> Math.tan(a.get(0)));
        register(functions, "asin", 1, 1, "asin(x)", a -> Math.asin(a.get(0)));
        register(functions, "acos", 1, 1, "acos(x)", a -> Math.acos(a.get(0)));
        register(functions, "atan", 1, 1, "atan(x)", a -> Math.atan(a.get(0)));
        register(functions, "sinh", 1, 1, "sinh(x)", a -> Math.sinh(a.get(0)));
        register(functions, "cosh", 1, 1, "cosh(x)", a -> Math.cosh(a.get(0)));
        register(functions, "tanh", 1, 1, "tanh(x)", a -> Math.tanh(a.get(0)));
        register(functions, "floor", 1, 1, "floor(x)", a -> Math.floor(a.get(0)));
        register(functions, "ceil", 1, 1, "ceil(x)", a -> Math.ceil(a.get(0)));
        register(functions, "round", 1, 1, "round(x)", a -> Math.rint(a.get(0)));
        register(functions, "ln", 1, 1, "ln(x)", a -> Math.log(a.get(0)));
        register(functions, "log", 1, 1, "log(x)  // base-10", a -> Math.log10(a.get(0)));
        register(functions, "exp", 1, 1, "exp(x)", a -> Math.exp(a.get(0)));
        register(functions, "sign", 1, 1, "sign(x)", a -> Math.signum(a.get(0)));
        register(functions, "deg", 1, 1, "deg(radians)", a -> Math.toDegrees(a.get(0)));
        register(functions, "rad", 1, 1, "rad(degrees)", a -> Math.toRadians(a.get(0)));
        register(functions, "fact", 1, 1, "fact(n)", a -> CalculatorMath.factorial(a.get(0)));
        register(functions, "fib", 1, 1, "fib(n)", a -> CalculatorMath.fibonacci(a.get(0)));
        register(functions, "isprime", 1, 1, "isprime(n)", a -> CalculatorMath.isPrime(a.get(0)) ? 1d : 0d);
        register(functions, "not", 1, 1, "not(x)", a -> CalculatorMath.toBoolean(a.get(0)) ? 0d : 1d);

        register(functions, "pow", 2, 2, "pow(base, exponent)", a -> Math.pow(a.get(0), a.get(1)));
        register(functions, "root", 2, 2, "root(value, degree)", a -> {
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
        register(functions, "mod", 2, 2, "mod(a, b)", a -> {
            if (a.get(1) == 0d) {
                throw new CalculatorException("mod(a, b): b cannot be zero.");
            }
            return a.get(0) % a.get(1);
        });
        register(functions, "logn", 2, 2, "logn(value, base)", a -> {
            double value = a.get(0);
            double base = a.get(1);
            if (value <= 0d || base <= 0d || base == 1d) {
                throw new CalculatorException("logn(value, base): requires value > 0, base > 0, base != 1.");
            }
            return Math.log(value) / Math.log(base);
        });
        register(functions, "lerp", 3, 3, "lerp(start, end, t)", a -> a.get(0) + (a.get(1) - a.get(0)) * a.get(2));
        register(functions, "pct", 2, 2, "pct(part, total)", a -> {
            if (a.get(1) == 0d) {
                throw new CalculatorException("pct(part, total): total cannot be zero.");
            }
            return (a.get(0) / a.get(1)) * 100d;
        });
        register(functions, "if", 3, 3, "if(condition, whenTrue, whenFalse)", a ->
                CalculatorMath.toBoolean(a.get(0)) ? a.get(1) : a.get(2));
        register(functions, "between", 3, 3, "between(value, min, max)", a ->
                (a.get(0) >= a.get(1) && a.get(0) <= a.get(2)) ? 1d : 0d);

        register(functions, "min", 2, -1, "min(a, b, ...)", a -> {
            double min = Double.POSITIVE_INFINITY;
            for (double value : a) {
                min = Math.min(min, value);
            }
            return min;
        });
        register(functions, "max", 2, -1, "max(a, b, ...)", a -> {
            double max = Double.NEGATIVE_INFINITY;
            for (double value : a) {
                max = Math.max(max, value);
            }
            return max;
        });
        register(functions, "sum", 1, -1, "sum(a, b, ...)", a -> {
            double total = 0d;
            for (double value : a) {
                total += value;
            }
            return total;
        });
        register(functions, "prod", 1, -1, "prod(a, b, ...)", CalculatorMath::productOf);
        register(functions, "avg", 1, -1, "avg(a, b, ...)", CalculatorMath::averageOf);
        register(functions, "median", 1, -1, "median(a, b, ...)", CalculatorMath::medianOf);
        register(functions, "var", 1, -1, "var(a, b, ...)", CalculatorMath::varianceOf);
        register(functions, "std", 1, -1, "std(a, b, ...)", CalculatorMath::standardDeviationOf);
        register(functions, "clamp", 3, 3, "clamp(value, min, max)", a -> CalculatorMath.clamp(a.get(0), a.get(1), a.get(2)));
        register(functions, "atan2", 2, 2, "atan2(y, x)", a -> Math.atan2(a.get(0), a.get(1)));
        register(functions, "hypot", 2, 2, "hypot(x, y)", a -> Math.hypot(a.get(0), a.get(1)));
        register(functions, "gcd", 2, -1, "gcd(a, b, ...)", CalculatorMath::gcdOf);
        register(functions, "lcm", 2, -1, "lcm(a, b, ...)", CalculatorMath::lcmOf);
        register(functions, "perm", 2, 2, "perm(n, k)", a -> CalculatorMath.permutation(a.get(0), a.get(1)));
        register(functions, "comb", 2, 2, "comb(n, k)", a -> CalculatorMath.combination(a.get(0), a.get(1)));

        register(functions, "rand", 0, 2, "rand() | rand(max) | rand(min, max)", a -> {
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
        register(functions, "randi", 1, 2, "randi(max) | randi(min, max)", a -> {
            long min;
            long max;
            if (a.size() == 1) {
                min = 0L;
                max = CalculatorMath.requireInteger(a.get(0), "randi max");
            } else {
                min = CalculatorMath.requireInteger(a.get(0), "randi min");
                max = CalculatorMath.requireInteger(a.get(1), "randi max");
            }
            if (max < min) {
                throw new CalculatorException("randi(min, max): max cannot be less than min.");
            }
            long span = max - min + 1L;
            if (span <= 0L || span > Integer.MAX_VALUE) {
                throw new CalculatorException("randi range is too large.");
            }
            return min + random.nextInt((int) span);
        });

        return functions;
    }

    private static void register(
            Map<String, FunctionDefinition> functions,
            String name,
            int minArgs,
            int maxArgs,
            String description,
            FunctionDefinition.FunctionImplementation implementation
    ) {
        functions.put(name, new FunctionDefinition(name, minArgs, maxArgs, description, implementation));
    }
}
