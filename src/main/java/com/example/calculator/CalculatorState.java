package com.example.calculator;

import java.util.Map;
import java.util.TreeMap;

final class CalculatorState {
    private final Map<String, Double> variables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, Double> constants;

    private double ans;
    private double memory;

    CalculatorState(Map<String, Double> constants) {
        this.constants = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.constants.putAll(constants);
    }

    Map<String, Double> getVariablesSnapshot() {
        return new TreeMap<>(variables);
    }

    Map<String, Double> getConstantsSnapshot() {
        return new TreeMap<>(constants);
    }

    boolean isConstant(String name) {
        return constants.containsKey(name);
    }

    double getVariable(String name) {
        return variables.get(name);
    }

    boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    void putVariable(String name, double value) {
        variables.put(name, value);
    }

    boolean removeVariable(String name) {
        return variables.remove(name) != null;
    }

    double resolveConstant(String name) {
        return constants.get(name);
    }

    boolean hasConstant(String name) {
        return constants.containsKey(name);
    }

    double getAns() {
        return ans;
    }

    void setAns(double ans) {
        this.ans = ans;
    }

    double getMemory() {
        return memory;
    }

    void setMemory(double memory) {
        this.memory = memory;
    }

    void clearVariables() {
        variables.clear();
    }

    void reset() {
        variables.clear();
        ans = 0d;
        memory = 0d;
    }
}
