package com.example.calculator;

import java.util.ArrayList;
import java.util.List;

final class ExpressionParser {
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
        return parseAddSub();
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

            TokenType type = switch (c) {
                case '+' -> TokenType.PLUS;
                case '-' -> TokenType.MINUS;
                case '*' -> TokenType.STAR;
                case '/' -> TokenType.SLASH;
                case '%' -> TokenType.PERCENT;
                case '^' -> TokenType.CARET;
                case '!' -> TokenType.BANG;
                case '(' -> TokenType.LPAREN;
                case ')' -> TokenType.RPAREN;
                case ',' -> TokenType.COMMA;
                default -> null;
            };
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
        EOF
    }

    private record Token(TokenType type, String text, double number, int position) {
    }
}
