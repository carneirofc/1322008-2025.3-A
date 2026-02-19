package com.example.calculator;

final class HistoryEntry {
    private final int index;
    private final String input;
    private final String output;

    HistoryEntry(int index, String input, String output) {
        this.index = index;
        this.input = input;
        this.output = output;
    }

    int index() {
        return index;
    }

    String input() {
        return input;
    }

    String output() {
        return output;
    }
}
