package com.example.calculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class CommandResult {
    private final List<String> outputLines;
    private final boolean exitRequested;

    CommandResult(List<String> outputLines, boolean exitRequested) {
        this.outputLines = Collections.unmodifiableList(new ArrayList<String>(outputLines));
        this.exitRequested = exitRequested;
    }

    List<String> outputLines() {
        return outputLines;
    }

    boolean exitRequested() {
        return exitRequested;
    }

    static CommandResult ofLines(List<String> outputLines) {
        return new CommandResult(outputLines, false);
    }

    static CommandResult exit() {
        return new CommandResult(Collections.<String>emptyList(), true);
    }

    static CommandResult empty() {
        return new CommandResult(Collections.<String>emptyList(), false);
    }
}
