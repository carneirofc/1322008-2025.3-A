package com.example.calculator;

import java.util.ArrayList;
import java.util.List;

final class ExpressionParser {
    private static final double EQ_EPS = 1e-9;

    @FunctionalInterface
    interface SymbolResolver {
        double resolve(String symbol);
    }

    @FunctionalInterface
    interface FunctionResolver {
        double invoke(String functionName, List<Double> args);
    }

    private final SymbolResolver symbolResolver;
    private final FunctionResolver functionResolver;
    private final List<Token> tokens;
    private int current;

    ExpressionParser(String input, SymbolResolver symbolResolver, FunctionResolver functionResolver) {
        this.symbolResolver = symbolResolver;
        this.functionResolver = functionResolver;
        this.tokens = tokenize(input);
        this.current = 0;
    }

    double parse() {
        double result = parseExpression();
        consume(TokenType.EOF, "Unexpected trailing input.");
        return result;
    }

    private double parseExpression() {
        return parseOr();
    }

    private double parseOr() {
        double value = parseAnd();
        while (match(TokenType.OR_OR)) {
            double rhs = parseAnd();
            value = (CalculatorMath.toBoolean(value) || CalculatorMath.toBoolean(rhs)) ? 1d : 0d;
        }
        return value;
    }

    private double parseAnd() {
        double value = parseEquality();
        while (match(TokenType.AND_AND)) {
            double rhs = parseEquality();
            value = (CalculatorMath.toBoolean(value) && CalculatorMath.toBoolean(rhs)) ? 1d : 0d;
        }
        return value;
    }

    private double parseEquality() {
        double value = parseComparison();
        while (true) {
            if (match(TokenType.EQUAL_EQUAL)) {
                double rhs = parseComparison();
                value = almostEqual(value, rhs) ? 1d : 0d;
            } else if (match(TokenType.BANG_EQUAL)) {
                double rhs = parseComparison();
                value = almostEqual(value, rhs) ? 0d : 1d;
            } else {
                break;
            }
        }
        return value;
    }

    private double parseComparison() {
        double value = parseAddSub();
        while (true) {
            if (match(TokenType.GREATER)) {
                value = value > parseAddSub() ? 1d : 0d;
            } else if (match(TokenType.GREATER_EQUAL)) {
                value = value >= parseAddSub() ? 1d : 0d;
            } else if (match(TokenType.LESS)) {
                value = value < parseAddSub() ? 1d : 0d;
            } else if (match(TokenType.LESS_EQUAL)) {
                value = value <= parseAddSub() ? 1d : 0d;
            } else {
                break;
            }
        }
        return value;
    }

    private boolean almostEqual(double a, double b) {
        return Math.abs(a - b) <= EQ_EPS;
    }

    private double parseAddSub() {
        double value = parseMulDiv();
        while (true) {
            if (match(TokenType.PLUS)) {
                value += parseMulDiv();
            } else if (match(TokenType.MINUS)) {
                value -= parseMulDiv();
            } else {
                break;
            }
        }
        return value;
    }

    private double parseMulDiv() {
        double value = parseUnary();
        while (true) {
            if (match(TokenType.STAR)) {
                value *= parseUnary();
            } else if (match(TokenType.SLASH)) {
                double divisor = parseUnary();
                if (divisor == 0d) {
                    throw error(peek(), "Division by zero.");
                }
                value /= divisor;
            } else if (match(TokenType.PERCENT)) {
                double divisor = parseUnary();
                if (divisor == 0d) {
                    throw error(peek(), "Modulo by zero.");
                }
                value %= divisor;
            } else {
                break;
            }
        }
        return value;
    }

    private double parsePower() {
        double value = parsePostfix();
        if (match(TokenType.CARET)) {
            double exponent = parseUnary();
            value = Math.pow(value, exponent);
        }
        return value;
    }

    private double parseUnary() {
        if (match(TokenType.PLUS)) {
            return parseUnary();
        }
        if (match(TokenType.MINUS)) {
            return -parseUnary();
        }
        return parsePower();
    }

    private double parsePostfix() {
        double value = parsePrimary();
        while (match(TokenType.BANG)) {
            value = CalculatorMath.factorial(value);
        }
        return value;
    }

    private double parsePrimary() {
        if (match(TokenType.NUMBER)) {
            return previous().number;
        }
        if (match(TokenType.IDENTIFIER)) {
            String symbol = previous().text;
            if (match(TokenType.LPAREN)) {
                List<Double> args = new ArrayList<>();
                if (!check(TokenType.RPAREN)) {
                    do {
                        args.add(parseExpression());
                    } while (match(TokenType.COMMA));
                }
                consume(TokenType.RPAREN, "Missing ')' after function arguments.");
                return functionResolver.invoke(symbol, args);
            }
            return symbolResolver.resolve(symbol);
        }
        if (match(TokenType.LPAREN)) {
            double value = parseExpression();
            consume(TokenType.RPAREN, "Missing ')' to close expression.");
            return value;
        }
        throw error(peek(), "Expected a number, variable, function call, or parenthesized expression.");
    }

    private boolean match(TokenType type) {
        if (!check(type)) {
            return false;
        }
        advance();
        return true;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private CalculatorException error(Token token, String message) {
        return new CalculatorException(message + " (near position " + token.position + ")");
    }

    private static List<Token> tokenize(String input) {
        List<Token> tokenList = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            int start = i;
            if (Character.isDigit(c) || c == '.') {
                boolean hasDot = c == '.';
                i++;
                boolean hasDigit = Character.isDigit(c);
                while (i < input.length()) {
                    char current = input.charAt(i);
                    if (Character.isDigit(current)) {
                        hasDigit = true;
                        i++;
                        continue;
                    }
                    if (current == '.' && !hasDot) {
                        hasDot = true;
                        i++;
                        continue;
                    }
                    break;
                }

                if (i < input.length() && (input.charAt(i) == 'e' || input.charAt(i) == 'E')) {
                    int exponentStart = i++;
                    if (i < input.length() && (input.charAt(i) == '+' || input.charAt(i) == '-')) {
                        i++;
                    }
                    int digitsStart = i;
                    while (i < input.length() && Character.isDigit(input.charAt(i))) {
                        i++;
                    }
                    if (digitsStart == i) {
                        throw new CalculatorException("Invalid exponent near position " + exponentStart);
                    }
                    hasDigit = true;
                }

                if (!hasDigit) {
                    throw new CalculatorException("Invalid number near position " + start);
                }
                String numberText = input.substring(start, i);
                try {
                    tokenList.add(new Token(TokenType.NUMBER, numberText, Double.parseDouble(numberText), start));
                } catch (NumberFormatException ex) {
                    throw new CalculatorException("Invalid number: " + numberText, ex);
                }
                continue;
            }

            if (Character.isLetter(c) || c == '_') {
                i++;
                while (i < input.length()) {
                    char current = input.charAt(i);
                    if (Character.isLetterOrDigit(current) || current == '_') {
                        i++;
                        continue;
                    }
                    break;
                }
                String text = input.substring(start, i);
                tokenList.add(new Token(TokenType.IDENTIFIER, text, 0d, start));
                continue;
            }

            if (i + 1 < input.length()) {
                String two = input.substring(i, i + 2);
                TokenType twoChar;
                if ("==".equals(two)) {
                    twoChar = TokenType.EQUAL_EQUAL;
                } else if ("!=".equals(two)) {
                    twoChar = TokenType.BANG_EQUAL;
                } else if ("<=".equals(two)) {
                    twoChar = TokenType.LESS_EQUAL;
                } else if (">=".equals(two)) {
                    twoChar = TokenType.GREATER_EQUAL;
                } else if ("&&".equals(two)) {
                    twoChar = TokenType.AND_AND;
                } else if ("||".equals(two)) {
                    twoChar = TokenType.OR_OR;
                } else {
                    twoChar = null;
                }
                if (twoChar != null) {
                    tokenList.add(new Token(twoChar, two, 0d, i));
                    i += 2;
                    continue;
                }
            }

            TokenType type;
            switch (c) {
                case '+':
                    type = TokenType.PLUS;
                    break;
                case '-':
                    type = TokenType.MINUS;
                    break;
                case '*':
                    type = TokenType.STAR;
                    break;
                case '/':
                    type = TokenType.SLASH;
                    break;
                case '%':
                    type = TokenType.PERCENT;
                    break;
                case '^':
                    type = TokenType.CARET;
                    break;
                case '!':
                    type = TokenType.BANG;
                    break;
                case '(':
                    type = TokenType.LPAREN;
                    break;
                case ')':
                    type = TokenType.RPAREN;
                    break;
                case ',':
                    type = TokenType.COMMA;
                    break;
                case '<':
                    type = TokenType.LESS;
                    break;
                case '>':
                    type = TokenType.GREATER;
                    break;
                default:
                    type = null;
                    break;
            }
            if (type == null) {
                throw new CalculatorException("Unexpected character '" + c + "' at position " + i);
            }
            tokenList.add(new Token(type, String.valueOf(c), 0d, i));
            i++;
        }

        tokenList.add(new Token(TokenType.EOF, "", 0d, input.length()));
        return tokenList;
    }

    private enum TokenType {
        NUMBER,
        IDENTIFIER,
        PLUS,
        MINUS,
        STAR,
        SLASH,
        PERCENT,
        CARET,
        BANG,
        LPAREN,
        RPAREN,
        COMMA,
        LESS,
        LESS_EQUAL,
        GREATER,
        GREATER_EQUAL,
        EQUAL_EQUAL,
        BANG_EQUAL,
        AND_AND,
        OR_OR,
        EOF
    }

    private static final class Token {
        private final TokenType type;
        private final String text;
        private final double number;
        private final int position;

        private Token(TokenType type, String text, double number, int position) {
            this.type = type;
            this.text = text;
            this.number = number;
            this.position = position;
        }
    }
}
