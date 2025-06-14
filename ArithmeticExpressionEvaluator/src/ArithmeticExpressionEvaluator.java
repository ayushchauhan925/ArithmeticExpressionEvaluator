/*
 * MIT License
 *
 * Copyright (c) 2025 Ayush Chauhan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * ArithmeticExpressionEvaluator is a Java class that evaluates mathematical expressions provided as strings.
 * It supports basic arithmetic operations (+, -, *, /, ^), trigonometric functions (sin, cos, tan, etc.),
 * constants (pi, e), and special operations (factorial, square root, percentage). The evaluator now
 * recognizes { and [ as alternative delimiters to parentheses for grouping expressions.
 * It uses high-precision arithmetic via BigDecimal and supports degree or radian modes for trigonometric functions.
 *
 * @author Ayush Chauhan
 * @version 1.1
 * @since 2025-07-01
 * @updated 2025-06-14
 */
import java.util.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class ArithmeticExpressionEvaluator {

    private static final MathContext PRECISION = new MathContext(30, RoundingMode.HALF_UP);
    private static final Set<String> FUNCTIONS = Set.of("sin", "cos", "tan", "ln", "log", "asin", "acos", "atan");
    private static final Set<String> CONSTANTS = Set.of("pi", "e");

    /**
     * Evaluates a mathematical expression and returns the result as a double.
     *
     * @param expression The mathematical expression as a string (e.g., "{5*5}-[({8-7})]").
     * @param useDegrees If true, trigonometric functions use degrees; otherwise, radians.
     * @return The evaluated result as a double.
     * @throws Exception If the expression is invalid or an error occurs during evaluation.
     */
    public static double evaluate(String expression, boolean useDegrees) throws Exception {
        if (expression == null || expression.trim().isEmpty()) {
            throw new InvalidExpressionException("Expression cannot be null or empty");
        }
        List<String> tokens = tokenize(expression);
        List<String> postfix = toPostfix(tokens);
        BigDecimal result = evaluatePostfix(postfix, useDegrees);
        return result.doubleValue();
    }

    /**
     * Tokenizes the input expression into a list of tokens (numbers, operators, functions, etc.).
     * Now supports { and [ as delimiters in addition to parentheses.
     *
     * @param expression The input expression string.
     * @return A list of tokens.
     * @throws InvalidExpressionException If the expression contains invalid characters or tokens.
     */
    private static List<String> tokenize(String expression) throws InvalidExpressionException {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < expression.length()) {
            char c = expression.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
            }
            if (Character.isLetter(c)) {
                StringBuilder func = new StringBuilder();
                int start = i;
                while (i < expression.length() && Character.isLetter(expression.charAt(i))) {
                    func.append(expression.charAt(i));
                    i++;
                }
                String funcName = func.toString();
                if (FUNCTIONS.contains(funcName) || CONSTANTS.contains(funcName)) {
                    tokens.add(funcName);
                } else {
                    throw new InvalidExpressionException("Invalid function or constant '" + funcName + "' at position " + start);
                }
                i--;
            } else if (Character.isDigit(c) || c == '.' || (c == '-' && (i == 0 || expression.charAt(i - 1) == '(' || expression.charAt(i - 1) == '{' || expression.charAt(i - 1) == '[' || isOperator(String.valueOf(expression.charAt(i - 1)))))) {
                StringBuilder number = new StringBuilder();
                int start = i;
                if (c == '-') {
                    number.append(c);
                    i++;
                }
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    number.append(expression.charAt(i));
                    i++;
                }
                try {
                    new BigDecimal(number.toString());
                    tokens.add(number.toString());
                } catch (NumberFormatException e) {
                    throw new InvalidExpressionException("Invalid number format '" + number + "' at position " + start);
                }
            } else if (c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']' || c == '√' || c == '!' || c == '%') {
                tokens.add(String.valueOf(c));
                i++;
            } else if (isOperator(String.valueOf(c))) {
                tokens.add(String.valueOf(c));
                i++;
            } else {
                throw new InvalidExpressionException("Invalid character '" + c + "' at position " + i);
            }
            i++;
        }
        return tokens;
    }

    /**
     * Converts infix tokens to postfix notation using the Shunting Yard algorithm.
     * Now supports { and [ as opening delimiters and } and ] as closing delimiters.
     *
     * @param tokens The list of infix tokens.
     * @return A list of tokens in postfix order.
     * @throws InvalidExpressionException If delimiters are mismatched or tokens are invalid.
     */
    private static List<String> toPostfix(List<String> tokens) throws InvalidExpressionException {
        Stack<String> stack = new Stack<>();
        List<String> output = new ArrayList<>();
        for (String token : tokens) {
            if (isNumber(token)) {
                output.add(token);
            } else if (CONSTANTS.contains(token)) {
                if (token.equals("pi")) {
                    output.add(String.valueOf(Math.PI));
                } else if (token.equals("e")) {
                    output.add(String.valueOf(Math.E));
                }
            } else if (FUNCTIONS.contains(token) || token.equals("√")) {
                stack.push(token);
            } else if (token.equals("(") || token.equals("{") || token.equals("[")) {
                stack.push(token);
            } else if (token.equals(")") || token.equals("}") || token.equals("]")) {
                while (!stack.isEmpty() && !isOpeningDelimiter(stack.peek())) {
                    output.add(stack.pop());
                }
                if (stack.isEmpty()) {
                    throw new InvalidExpressionException("Mismatched delimiters");
                }
                stack.pop();
                if (!stack.isEmpty() && (FUNCTIONS.contains(stack.peek()) || stack.peek().equals("√"))) {
                    output.add(stack.pop());
                }
            } else if (isOperator(token)) {
                while (!stack.isEmpty() && !isOpeningDelimiter(stack.peek()) && precedence(stack.peek()) >= precedence(token)) {
                    output.add(stack.pop());
                }
                stack.push(token);
            } else if (token.equals("!") || token.equals("%")) {
                output.add(token);
            } else {
                throw new InvalidExpressionException("Invalid token: " + token);
            }
        }
        while (!stack.isEmpty()) {
            if (isOpeningDelimiter(stack.peek())) {
                throw new InvalidExpressionException("Mismatched delimiters");
            }
            output.add(stack.pop());
        }
        return output;
    }

    /**
     * Helper method to check if a token is an opening delimiter.
     *
     * @param token The token to check.
     * @return True if the token is an opening delimiter, false otherwise.
     */
    private static boolean isOpeningDelimiter(String token) {
        return token.equals("(") || token.equals("{") || token.equals("[");
    }

    /**
     * Evaluates a postfix expression and returns the result.
     *
     * @param postfix The list of tokens in postfix order.
     * @param useDegrees If true, trigonometric functions use degrees; otherwise, radians.
     * @return The result as a BigDecimal.
     * @throws Exception If an error occurs during evaluation (e.g., division by zero, invalid domain).
     */
    private static BigDecimal evaluatePostfix(List<String> postfix, boolean useDegrees) throws Exception {
        Stack<BigDecimal> stack = new Stack<>();
        for (String token : postfix) {
            if (isNumber(token)) {
                stack.push(new BigDecimal(token, PRECISION));
            } else if (token.equals("!")) {
                BigDecimal val = stack.pop();
                stack.push(new BigDecimal(factorial(val.intValueExact()), PRECISION));
            } else if (token.equals("√")) {
                BigDecimal val = stack.pop();
                if (val.compareTo(BigDecimal.ZERO) < 0) {
                    throw new OutOfDomainException("Square root of negative number");
                }
                stack.push(new BigDecimal(Math.sqrt(val.doubleValue()), PRECISION));
            } else if (token.equals("%")) {
                BigDecimal val = stack.pop();
                stack.push(val.divide(BigDecimal.valueOf(100), PRECISION));
            } else if (FUNCTIONS.contains(token)) {
                BigDecimal arg = stack.pop();
                double angle = useDegrees ? Math.toRadians(arg.doubleValue()) : arg.doubleValue();
                switch (token) {
                    case "sin":
                        stack.push(new BigDecimal(Math.sin(angle), PRECISION));
                        break;
                    case "cos":
                        stack.push(new BigDecimal(Math.cos(angle), PRECISION));
                        break;
                    case "tan":
                        if (Math.abs(Math.cos(angle)) < 1e-10) {
                            throw new ArithmeticException("Undefined tan() value");
                        }
                        stack.push(new BigDecimal(Math.tan(angle), PRECISION));
                        break;
                    case "asin":
                        if (arg.compareTo(BigDecimal.valueOf(-1)) < 0 || arg.compareTo(BigDecimal.valueOf(1)) > 0) {
                            throw new OutOfDomainException("asin input out of range [-1, 1]");
                        }
                        double asinResult = Math.asin(arg.doubleValue());
                        if (useDegrees) {
                            asinResult = Math.toDegrees(asinResult);
                        }
                        stack.push(new BigDecimal(asinResult, PRECISION));
                        break;
                    case "acos":
                        if (arg.compareTo(BigDecimal.valueOf(-1)) < 0 || arg.compareTo(BigDecimal.valueOf(1)) > 0) {
                            throw new OutOfDomainException("acos input out of range [-1, 1]");
                        }
                        double acosResult = Math.acos(arg.doubleValue());
                        if (useDegrees) {
                            acosResult = Math.toDegrees(acosResult);
                        }
                        stack.push(new BigDecimal(acosResult, PRECISION));
                        break;
                    case "atan":
                        double atanResult = Math.atan(arg.doubleValue());
                        if (useDegrees) {
                            atanResult = Math.toDegrees(atanResult);
                        }
                        stack.push(new BigDecimal(atanResult, PRECISION));
                        break;
                    case "ln":
                        if (arg.compareTo(BigDecimal.ZERO) <= 0) {
                            throw new ArithmeticException("ln of non-positive number");
                        }
                        stack.push(new BigDecimal(Math.log(arg.doubleValue()), PRECISION));
                        break;
                    case "log":
                        if (arg.compareTo(BigDecimal.ZERO) <= 0) {
                            throw new ArithmeticException("log of non-positive number");
                        }
                        stack.push(new BigDecimal(Math.log10(arg.doubleValue()), PRECISION));
                        break;
                }
            } else if (isOperator(token)) {
                BigDecimal b = stack.pop();
                BigDecimal a = stack.pop();
                switch (token) {
                    case "+":
                        stack.push(a.add(b, PRECISION));
                        break;
                    case "-":
                        stack.push(a.subtract(b, PRECISION));
                        break;
                    case "*":
                        stack.push(a.multiply(b, PRECISION));
                        break;
                    case "/":
                        if (b.compareTo(BigDecimal.ZERO) == 0) {
                            throw new ZeroDivisionException("Division by zero");
                        }
                        stack.push(a.divide(b, PRECISION));
                        break;
                    case "^":
                        if (a.compareTo(BigDecimal.ZERO) >= 0) {
                            double result = Math.pow(a.doubleValue(), b.doubleValue());
                            stack.push(new BigDecimal(result, PRECISION));
                        } else {
                            if (b.stripTrailingZeros().scale() <= 0) {
                                double result = Math.pow(a.doubleValue(), b.doubleValue());
                                stack.push(new BigDecimal(result, PRECISION));
                            } else {
                                double epsilon = 1e-10;
                                int max_q = 100;
                                boolean foundOddDenominator = false;
                                for (int q = 1; q <= max_q; q += 2) {
                                    double qb = q * b.doubleValue();
                                    long p = Math.round(qb);
                                    if (Math.abs(qb - p) < epsilon) {
                                        double absA = -a.doubleValue();
                                        double root = Math.pow(absA, 1.0 / q);
                                        double aRoot = -root;
                                        double result = Math.pow(aRoot, p);
                                        stack.push(new BigDecimal(result, PRECISION));
                                        foundOddDenominator = true;
                                        break;
                                    }
                                }
                                if (!foundOddDenominator) {
                                    throw new OutOfDomainException("Negative base raised to a fractional exponent with even denominator or non-rational exponent");
                                }
                            }
                        }
                        break;
                }
            }
        }
        if (stack.size() != 1) {
            throw new InvalidExpressionException("Invalid expression: incorrect number of operands or operators");
        }
        return stack.pop();
    }

    /**
     * Checks if a token is an operator (+, -, *, /, ^).
     *
     * @param token The token to check.
     * @return True if the token is an operator, false otherwise.
     */
    private static boolean isOperator(String token) {
        return token.length() == 1 && "+-*/^".indexOf(token.charAt(0)) != -1;
    }

    /**
     * Checks if a token represents a valid number.
     *
     * @param token The token to check.
     * @return True if the token is a valid number, false otherwise.
     */
    private static boolean isNumber(String token) {
        return token.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Returns the precedence of an operator or function.
     *
     * @param op The operator or function token.
     * @return The precedence level (higher means higher priority).
     */
    private static int precedence(String op) {
        return switch (op) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            case "^" -> 3;
            case "√" -> 4;
            default -> 0;
        };
    }

    /**
     * Computes the factorial of a non-negative integer.
     *
     * @param n The input integer.
     * @return The factorial as a double.
     * @throws ArithmeticException If n is negative.
     */
    private static double factorial(int n) {
        if (n < 0) {
            throw new ArithmeticException("Factorial of negative number");
        }
        double result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * Exception thrown when a division by zero is attempted.
     */
    static class ZeroDivisionException extends ArithmeticException {
        public ZeroDivisionException(String msg) {
            super(msg);
        }
    }

    /**
     * Exception thrown when the expression is invalid (e.g., syntax error, mismatched delimiters).
     */
    static class InvalidExpressionException extends Exception {
        public InvalidExpressionException(String msg) {
            super(msg);
        }
    }

    /**
     * Exception thrown when a mathematical operation is out of the function's domain (e.g., square root of negative number).
     */
    static class OutOfDomainException extends ArithmeticException {
        public OutOfDomainException(String msg) {
            super(msg);
        }
    }
}
