package com.example.calculator;

import java.util.List;

final class FunctionDefinition {
    private final String name;
    private final int minArgs;
    private final int maxArgs;
    private final String description;
    private final FunctionImplementation implementation;

    FunctionDefinition(
            String name,
            int minArgs,
            int maxArgs,
            String description,
            FunctionImplementation implementation
    ) {
        this.name = name;
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.description = description;
        this.implementation = implementation;
    }

    String name() {
        return name;
    }

    int minArgs() {
        return minArgs;
    }

    int maxArgs() {
        return maxArgs;
    }

    String description() {
        return description;
    }

    FunctionImplementation implementation() {
        return implementation;
    }

    @FunctionalInterface
    interface FunctionImplementation {
        double apply(List<Double> args);
    }
}
