package com.example.calculator;

import java.util.Arrays;
import java.util.List;

final class CalculatorMath {
    private static final double BOOL_EPS = 1e-12;

    private CalculatorMath() {
    }

    static long requireInteger(double value, String label) {
        if (!Double.isFinite(value)) {
            throw new CalculatorException(label + " must be finite.");
        }
        double nearest = Math.rint(value);
        if (Math.abs(value - nearest) > 1e-9) {
            throw new CalculatorException(label + " must be an integer.");
        }
        return (long) nearest;
    }

    static long requireNonNegativeInteger(double value, String label) {
        long integer = requireInteger(value, label);
        if (integer < 0) {
            throw new CalculatorException(label + " must be non-negative.");
        }
        return integer;
    }

    static double factorial(double value) {
        long n = requireNonNegativeInteger(value, "Factorial input");
        if (n > 170) {
            throw new CalculatorException("Factorial input too large (max 170).");
        }
        double result = 1d;
        for (long i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    static boolean toBoolean(double value) {
        return Math.abs(value) > BOOL_EPS;
    }

    static double clamp(double value, double min, double max) {
        if (min > max) {
            throw new CalculatorException("clamp(min, max): min cannot be greater than max.");
        }
        return Math.max(min, Math.min(max, value));
    }

    static long gcd(long a, long b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            long next = a % b;
            a = b;
            b = next;
        }
        return a;
    }

    static double gcdOf(List<Double> values) {
        if (values.size() < 2) {
            throw new CalculatorException("gcd requires at least 2 arguments.");
        }
        long result = requireInteger(values.get(0), "gcd argument");
        for (int i = 1; i < values.size(); i++) {
            long current = requireInteger(values.get(i), "gcd argument");
            result = gcd(result, current);
        }
        return Math.abs(result);
    }

    static double lcmOf(List<Double> values) {
        if (values.size() < 2) {
            throw new CalculatorException("lcm requires at least 2 arguments.");
        }
        long result = Math.abs(requireInteger(values.get(0), "lcm argument"));
        for (int i = 1; i < values.size(); i++) {
            long current = Math.abs(requireInteger(values.get(i), "lcm argument"));
            if (result == 0 || current == 0) {
                result = 0;
                continue;
            }
            long gcdValue = gcd(result, current);
            long scaled = result / gcdValue;
            try {
                result = Math.multiplyExact(scaled, current);
            } catch (ArithmeticException ex) {
                throw new CalculatorException("lcm overflowed long range.", ex);
            }
        }
        return result;
    }

    static double productOf(List<Double> values) {
        double result = 1d;
        for (double value : values) {
            result *= value;
            if (!Double.isFinite(result)) {
                throw new CalculatorException("prod overflowed.");
            }
        }
        return result;
    }

    static double averageOf(List<Double> values) {
        double total = 0d;
        for (double value : values) {
            total += value;
        }
        return total / values.size();
    }

    static double medianOf(List<Double> values) {
        double[] copy = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            copy[i] = values.get(i);
        }
        Arrays.sort(copy);
        int middle = copy.length / 2;
        if (copy.length % 2 == 1) {
            return copy[middle];
        }
        return (copy[middle - 1] + copy[middle]) / 2d;
    }

    static double varianceOf(List<Double> values) {
        double mean = averageOf(values);
        double total = 0d;
        for (double value : values) {
            double diff = value - mean;
            total += diff * diff;
        }
        return total / values.size();
    }

    static double standardDeviationOf(List<Double> values) {
        return Math.sqrt(varianceOf(values));
    }

    static double permutation(double nValue, double kValue) {
        long n = requireNonNegativeInteger(nValue, "perm n");
        long k = requireNonNegativeInteger(kValue, "perm k");
        if (k > n) {
            throw new CalculatorException("perm(n, k) requires k <= n.");
        }
        double result = 1d;
        for (long i = 0; i < k; i++) {
            result *= (n - i);
            if (!Double.isFinite(result)) {
                throw new CalculatorException("perm(n, k) overflowed.");
            }
        }
        return result;
    }

    static double combination(double nValue, double kValue) {
        long n = requireNonNegativeInteger(nValue, "comb n");
        long k = requireNonNegativeInteger(kValue, "comb k");
        if (k > n) {
            throw new CalculatorException("comb(n, k) requires k <= n.");
        }
        k = Math.min(k, n - k);
        double result = 1d;
        for (long i = 1; i <= k; i++) {
            result = result * (n - k + i) / i;
            if (!Double.isFinite(result)) {
                throw new CalculatorException("comb(n, k) overflowed.");
            }
        }
        return Math.rint(result);
    }

    static double fibonacci(double nValue) {
        long n = requireNonNegativeInteger(nValue, "fib n");
        if (n > 92) {
            throw new CalculatorException("fib(n): n too large (max 92).");
        }
        if (n <= 1) {
            return n;
        }
        long a = 0;
        long b = 1;
        for (long i = 2; i <= n; i++) {
            long next = a + b;
            a = b;
            b = next;
        }
        return b;
    }

    static boolean isPrime(double value) {
        long n = requireInteger(value, "Prime input");
        if (n < 2) {
            return false;
        }
        if (n % 2 == 0) {
            return n == 2;
        }
        long limit = (long) Math.sqrt(n);
        for (long i = 3; i <= limit; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
}
