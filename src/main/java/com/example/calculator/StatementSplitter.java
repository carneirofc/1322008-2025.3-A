package com.example.calculator;

import java.util.ArrayList;
import java.util.List;

final class StatementSplitter {
    private StatementSplitter() {
    }

    static List<String> split(String input) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '(') {
                depth++;
                current.append(c);
                continue;
            }
            if (c == ')') {
                depth--;
                if (depth < 0) {
                    throw new CalculatorException("Unmatched ')' near position " + i);
                }
                current.append(c);
                continue;
            }
            if (c == ';' && depth == 0) {
                addIfNotBlank(statements, current.toString());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }

        if (depth != 0) {
            throw new CalculatorException("Unmatched '(' in expression.");
        }
        addIfNotBlank(statements, current.toString());
        return statements;
    }

    private static void addIfNotBlank(List<String> statements, String statement) {
        String trimmed = statement.trim();
        if (!trimmed.isEmpty()) {
            statements.add(trimmed);
        }
    }
}
