package com.example.calculator;

import java.util.List;

record CommandResult(List<String> outputLines, boolean exitRequested) {
    static CommandResult ofLines(List<String> outputLines) {
        return new CommandResult(List.copyOf(outputLines), false);
    }

    static CommandResult exit() {
        return new CommandResult(List.of(), true);
    }

    static CommandResult empty() {
        return new CommandResult(List.of(), false);
    }
}
