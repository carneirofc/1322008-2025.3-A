package com.example.calculator;

import java.util.List;

record FunctionDefinition(
        String name,
        int minArgs,
        int maxArgs,
        String description,
        FunctionImplementation implementation
) {
    @FunctionalInterface
    interface FunctionImplementation {
        double apply(List<Double> args);
    }
}
