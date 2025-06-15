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
 * This program evaluates arithmetic expressions with support for various mathematical functions,
 * constants, and operations. It uses a tokenizer to break the expression into tokens, converts
 * the infix notation to postfix, and then evaluates the postfix expression.
 */
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

public class ArithmeticExpressionEvaluator {

    // Tokenizer to split the expression into tokens
    private final Tokenizer tokenizer = new Tokenizer();
    // Converter to change infix to postfix notation
    private final PostfixConverter converter = new PostfixConverter();
    // Evaluator to compute the postfix expression
    private final PostfixEvaluator evaluator = new PostfixEvaluator();

    /**
     * Evaluates the given arithmetic expression.
     *
     * @param expression The arithmetic expression to evaluate.
     * @param useDegrees Whether to use degrees for trigonometric functions (true for degrees, false for radians).
     * @return The result of the expression as a double.
     * @throws Exception If there is an error in the expression or during evaluation.
     */
    public double evaluate(String expression, boolean useDegrees) throws Exception {
        if (expression == null || expression.trim().isEmpty()) {
            throw new InvalidExpressionException("Expression cannot be null or empty");
        }
        // Tokenize the expression
        List<String> tokens = tokenizer.tokenize(expression);
        // Convert to postfix notation
        List<String> postfix = converter.toPostfix(tokens);
        // Evaluate the postfix expression
        BigDecimal result = evaluator.evaluatePostfix(postfix, useDegrees);
        // Return the result as a double
        return result.doubleValue();
    }

    /**
     * Exception thrown when division by zero is attempted.
     */
    static class ZeroDivisionException extends ArithmeticException {
        public ZeroDivisionException(String msg) {
            super(msg);
        }
    }

    /**
     * Exception thrown when the expression is invalid.
     */
    static class InvalidExpressionException extends Exception {
        public InvalidExpressionException(String msg) {
            super(msg);
        }
    }

    /**
     * Exception thrown when a function is applied outside its domain.
     */
    static class OutOfDomainException extends ArithmeticException {
        public OutOfDomainException(String msg) {
            super(msg);
        }
    }
}

/**
 * Tokenizer class responsible for breaking the input expression into tokens.
 */
class Tokenizer {
    // Supported mathematical functions
    static final Set<String> FUNCTIONS = Set.of("sin", "cos", "tan", "ln", "log", "asin", "acos", "atan",
            "nrt", "ceil", "floor", "abs", "min", "max", "nPr", "nCr", "gcd", "sinh", "cosh", "tanh", "asinh", "acosh", "atanh");
    // Supported constants
    private static final Set<String> CONSTANTS = Set.of("pi", "e");

    /**
     * Tokenizes the given arithmetic expression into a list of tokens.
     *
     * @param expression The expression to tokenize.
     * @return A list of tokens.
     * @throws ArithmeticExpressionEvaluator.InvalidExpressionException If the expression contains invalid tokens.
     */
    public List<String> tokenize(String expression) throws ArithmeticExpressionEvaluator.InvalidExpressionException {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        String lastToken = null;

        while (i < expression.length()) {
            char c = expression.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (Character.isLetter(c)) {
                StringBuilder func = new StringBuilder();
                int startPos = i;
                while (i < expression.length() && Character.isLetter(expression.charAt(i))) {
                    func.append(expression.charAt(i));
                    i++;
                }
                String funcName = func.toString();
                if (!FUNCTIONS.contains(funcName) && !CONSTANTS.contains(funcName)) {
                    throw new ArithmeticExpressionEvaluator.InvalidExpressionException(
                            String.format("Invalid function or constant '%s' at position %d", funcName, startPos));
                }
                tokens.add(funcName);
                i--;
            } else if (Character.isDigit(c) || c == '.' || (c == '-' && (i == 0 || isOpeningDelimiter(lastToken) || isOperator(lastToken)))) {
                StringBuilder number = new StringBuilder();
                int startPos = i;
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
                    throw new ArithmeticExpressionEvaluator.InvalidExpressionException(
                            String.format("Invalid number format '%s' at position %d", number, startPos));
                }
                i--;
            } else if (c == '+' && (i == 0 || isOpeningDelimiter(lastToken) || isOperator(lastToken))) {
                tokens.add("u+"); // Unary plus
            } else if (c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']' || c == '√' || c == '!' || c == '%' || c == ',') {
                tokens.add(String.valueOf(c));
            } else if (isOperator(String.valueOf(c))) {
                tokens.add(String.valueOf(c));
            } else {
                throw new ArithmeticExpressionEvaluator.InvalidExpressionException(String.format("Invalid character '%c' at position %d", c, i));
            }

            if (tokens.size() > 1) {
                String currentToken = tokens.get(tokens.size() - 1);
                String prevToken = tokens.get(tokens.size() - 2);
                if ((isNumber(prevToken) && (CONSTANTS.contains(currentToken) || FUNCTIONS.contains(currentToken) || isOpeningDelimiter(currentToken))) ||
                        (isClosingDelimiter(prevToken) && (isNumber(currentToken) || CONSTANTS.contains(currentToken) || FUNCTIONS.contains(currentToken)))) {
                    tokens.add(tokens.size() - 1, "*"); // Implicit multiplication
                }
            }
            lastToken = tokens.get(tokens.size() - 1);
            i++;
        }
        return tokens;
    }

    /**
     * Checks if the token is a number.
     *
     * @param token The token to check.
     * @return True if the token is a number, false otherwise.
     */
    private boolean isNumber(String token) {
        return token != null && token.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Checks if the token is an operator.
     *
     * @param token The token to check.
     * @return True if the token is an operator, false otherwise.
     */
    private boolean isOperator(String token) {
        return token != null && token.length() == 1 && "+-*/^".contains(token);
    }

    /**
     * Checks if the token is an opening delimiter.
     *
     * @param token The token to check.
     * @return True if the token is an opening delimiter, false otherwise.
     */
    private boolean isOpeningDelimiter(String token) {
        return token != null && (token.equals("(") || token.equals("{") || token.equals("["));
    }

    /**
     * Checks if the token is a closing delimiter.
     *
     * @param token The token to check.
     * @return True if the token is a closing delimiter, false otherwise.
     */
    private boolean isClosingDelimiter(String token) {
        return token != null && (token.equals(")") || token.equals("}") || token.equals("]"));
    }
}

/**
 * PostfixConverter class responsible for converting infix notation to postfix notation.
 */
class PostfixConverter {
    // Constants
    private static final Set<String> CONSTANTS = Set.of("pi", "e");
    // Functions from Tokenizer
    private static final Set<String> FUNCTIONS = Tokenizer.FUNCTIONS;
    // Matching delimiters
    private static final Map<String, String> DELIMITER_PAIRS = Map.of(")", "(", "}", "{", "]", "[");

    /**
     * Converts the list of tokens from infix notation to postfix notation.
     *
     * @param tokens The list of tokens in infix notation.
     * @return The list of tokens in postfix notation.
     * @throws ArithmeticExpressionEvaluator.InvalidExpressionException If the expression is invalid.
     */
    public List<String> toPostfix(List<String> tokens) throws ArithmeticExpressionEvaluator.InvalidExpressionException {
        Deque<String> stack = new ArrayDeque<>();
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
            } else if (isOperator(token)) {
                while (!stack.isEmpty() && !isOpeningDelimiter(stack.peek()) &&
                        (precedence(stack.peek()) > precedence(token) ||
                                (precedence(stack.peek()) == precedence(token) && isLeftAssociative(token)))) {
                    output.add(stack.pop());
                }
                stack.push(token);
            } else if (isOpeningDelimiter(token)) {
                stack.push(token);
            } else if (token.equals(",")) {
                while (!stack.isEmpty() && !isOpeningDelimiter(stack.peek())) {
                    output.add(stack.pop());
                }
                if (stack.isEmpty() || !isOpeningDelimiter(stack.peek())) {
                    throw new ArithmeticExpressionEvaluator.InvalidExpressionException("Mismatched delimiters or missing opening delimiter");
                }
            } else if (isClosingDelimiter(token)) {
                while (!stack.isEmpty() && !stack.peek().equals(DELIMITER_PAIRS.get(token))) {
                    output.add(stack.pop());
                }
                if (stack.isEmpty() || !stack.peek().equals(DELIMITER_PAIRS.get(token))) {
                    throw new ArithmeticExpressionEvaluator.InvalidExpressionException("Mismatched delimiters");
                }
                stack.pop();
                if (!stack.isEmpty() && (FUNCTIONS.contains(stack.peek()) || stack.peek().equals("√"))) {
                    output.add(stack.pop());
                }
            } else if (token.equals("!") || token.equals("%") || token.equals("u+")) {
                output.add(token);
            } else {
                throw new ArithmeticExpressionEvaluator.InvalidExpressionException("Invalid token: " + token);
            }
        }

        while (!stack.isEmpty()) {
            if (isOpeningDelimiter(stack.peek())) {
                throw new ArithmeticExpressionEvaluator.InvalidExpressionException("Mismatched delimiters");
            }
            output.add(stack.pop());
        }
        return output;
    }

    /**
     * Checks if the token is a number.
     *
     * @param token The token to check.
     * @return True if the token is a number, false otherwise.
     */
    private boolean isNumber(String token) {
        return token.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Checks if the token is an operator.
     *
     * @param token The token to check.
     * @return True if the token is an operator, false otherwise.
     */
    private boolean isOperator(String token) {
        return token.length() == 1 && "+-*/^".contains(token);
    }

    /**
     * Checks if the token is an opening delimiter.
     *
     * @param token The token to check.
     * @return True if the token is an opening delimiter, false otherwise.
     */
    private boolean isOpeningDelimiter(String token) {
        return token.equals("(") || token.equals("{") || token.equals("[");
    }

    /**
     * Checks if the token is a closing delimiter.
     *
     * @param token The token to check.
     * @return True if the token is a closing delimiter, false otherwise.
     */
    private boolean isClosingDelimiter(String token) {
        return token.equals(")") || token.equals("}") || token.equals("]");
    }

    /**
     * Returns the precedence of the operator.
     *
     * @param op The operator.
     * @return The precedence level.
     */
    private int precedence(String op) {
        return switch (op) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            case "^" -> 3;
            case "√" -> 4;
            default -> throw new IllegalArgumentException("Invalid operator");
        };
    }

    /**
     * Checks if the operator is left associative.
     *
     * @param op The operator.
     * @return True if left associative, false otherwise.
     */
    private boolean isLeftAssociative(String op) {
        return !op.equals("^");
    }
}

/**
 * PostfixEvaluator class responsible for evaluating the postfix expression.
 */
class PostfixEvaluator {
    // Precision for calculations
    private static final MathContext PRECISION = new MathContext(30, RoundingMode.HALF_UP);
    // Number of arguments for each function
    private static final Map<String, Integer> FUNCTION_ARG_COUNTS = Map.ofEntries(
            Map.entry("sin", 1), Map.entry("cos", 1), Map.entry("tan", 1), Map.entry("ln", 1),
            Map.entry("log", 2), Map.entry("asin", 1), Map.entry("acos", 1), Map.entry("atan", 1),
            Map.entry("nrt", 2), Map.entry("ceil", 1), Map.entry("floor", 1), Map.entry("abs", 1),
            Map.entry("min", 2), Map.entry("max", 2), Map.entry("√", 1), Map.entry("nPr", 2),
            Map.entry("nCr", 2), Map.entry("gcd", 2), Map.entry("sinh", 1), Map.entry("cosh", 1),
            Map.entry("tanh", 1), Map.entry("asinh", 1), Map.entry("acosh", 1), Map.entry("atanh", 1)
    );

    /**
     * Evaluates the postfix expression.
     *
     * @param postfix The list of tokens in postfix notation.
     * @param useDegrees Whether to use degrees for trigonometric functions.
     * @return The result of the expression as a BigDecimal.
     * @throws Exception If there is an error during evaluation.
     */
    public BigDecimal evaluatePostfix(List<String> postfix, boolean useDegrees) throws Exception {
        Deque<BigDecimal> stack = new ArrayDeque<>();

        for (String token : postfix) {
            if (isNumber(token)) {
                stack.push(new BigDecimal(token, PRECISION));
            } else if (token.equals("!")) {
                BigDecimal val = stack.pop();
                stack.push(MathFunctions.factorial(val));
            } else if (token.equals("√")) {
                BigDecimal val = stack.pop();
                if (val.compareTo(BigDecimal.ZERO) < 0) {
                    throw new ArithmeticExpressionEvaluator.OutOfDomainException("Square root of negative number");
                }
                stack.push(new BigDecimal(Math.sqrt(val.doubleValue()), PRECISION));
            } else if (token.equals("%")) {
                BigDecimal val = stack.pop();
                stack.push(val.divide(BigDecimal.valueOf(100), PRECISION));
            } else if (FUNCTION_ARG_COUNTS.containsKey(token)) {
                int argCount = FUNCTION_ARG_COUNTS.get(token);
                List<BigDecimal> args = new ArrayList<>();
                for (int j = 0; j < argCount; j++) {
                    if (stack.isEmpty()) {
                        throw new ArithmeticExpressionEvaluator.InvalidExpressionException("Insufficient arguments for function: " + token);
                    }
                    args.add(0, stack.pop());
                }
                BigDecimal result = MathFunctions.computeFunction(token, args, useDegrees);
                stack.push(result);
            } else if (isOperator(token)) {
                if (stack.size() < 2) {
                    throw new ArithmeticExpressionEvaluator.InvalidExpressionException("Insufficient operands for operator: " + token);
                }
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
                            throw new ArithmeticExpressionEvaluator.ZeroDivisionException("Division by zero");
                        }
                        stack.push(a.divide(b, PRECISION));
                        break;
                    case "^":
                        stack.push(power(a, b, PRECISION));
                        break;
                }
            } else if (token.equals("u+")) {
                // Unary +: no operation needed
            }
        }

        if (stack.size() != 1) {
            throw new ArithmeticExpressionEvaluator.InvalidExpressionException("Invalid expression: incorrect number of operands or operators");
        }
        return stack.pop();
    }

    /**
     * Checks if the token is a number.
     *
     * @param token The token to check.
     * @return True if the token is a number, false otherwise.
     */
    private boolean isNumber(String token) {
        return token.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Checks if the token is an operator.
     *
     * @param token The token to check.
     * @return True if the token is an operator, false otherwise.
     */
    private boolean isOperator(String token) {
        return token.length() == 1 && "+-*/^".contains(token);
    }

    /**
     * Computes the power of a base raised to an exponent.
     *
     * @param base The base.
     * @param exponent The exponent.
     * @param mc The MathContext for precision.
     * @return The result of base^exponent.
     */
    private BigDecimal power(BigDecimal base, BigDecimal exponent, MathContext mc) {
        if (exponent.stripTrailingZeros().scale() <= 0) {
            int expInt = exponent.intValueExact();
            BigDecimal result = BigDecimal.ONE;
            for (int i = 0; i < Math.abs(expInt); i++) {
                result = result.multiply(base, mc);
            }
            if (expInt < 0) {
                if (base.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticExpressionEvaluator.ZeroDivisionException("Division by zero in power");
                }
                result = BigDecimal.ONE.divide(result, mc);
            }
            return result;
        }
        throw new UnsupportedOperationException("Fractional exponents not yet supported");
    }
}

/**
 * MathFunctions class containing methods for computing various mathematical functions.
 */
class MathFunctions {
    // Supported functions
    public static final Set<String> FUNCTIONS = Set.of("sin", "cos", "tan", "ln", "log", "asin", "acos", "atan",
            "nrt", "ceil", "floor", "abs", "min", "max", "√", "nPr", "nCr", "gcd", "sinh", "cosh", "tanh", "asinh", "acosh", "atanh");
    // Precision for calculations
    private static final MathContext PRECISION = new MathContext(30);

    /**
     * Computes the specified mathematical function with the given arguments.
     *
     * @param function The name of the function.
     * @param args The arguments to the function.
     * @param useDegrees Whether to use degrees for trigonometric functions.
     * @return The result of the function.
     */
    public static BigDecimal computeFunction(String function, List<BigDecimal> args, boolean useDegrees) {
        return switch (function) {
            case "sin" -> {
                if (args.size() != 1) throw new IllegalArgumentException("sin expects 1 argument");
                double angle = useDegrees ? Math.toRadians(args.get(0).doubleValue()) : args.get(0).doubleValue();
                yield new BigDecimal(Math.sin(angle), PRECISION);
            }
            case "cos" -> {
                if (args.size() != 1) throw new IllegalArgumentException("cos expects 1 argument");
                double angle = useDegrees ? Math.toRadians(args.get(0).doubleValue()) : args.get(0).doubleValue();
                yield new BigDecimal(Math.cos(angle), PRECISION);
            }
            case "tan" -> {
                if (args.size() != 1) throw new IllegalArgumentException("tan expects 1 argument");
                double angle = useDegrees ? Math.toRadians(args.get(0).doubleValue()) : args.get(0).doubleValue();
                if (Math.abs(Math.cos(angle)) < 1e-10) {
                    throw new ArithmeticException("Undefined tan() value");
                }
                yield new BigDecimal(Math.tan(angle), PRECISION);
            }
            case "asin" -> {
                if (args.size() != 1) throw new IllegalArgumentException("asin expects 1 argument");
                BigDecimal arg = args.get(0);
                if (arg.compareTo(BigDecimal.valueOf(-1)) < 0 || arg.compareTo(BigDecimal.valueOf(1)) > 0) {
                    throw new ArithmeticExpressionEvaluator.OutOfDomainException("asin input out of range [-1, 1]");
                }
                double result = Math.asin(arg.doubleValue());
                if (useDegrees) result = Math.toDegrees(result);
                yield new BigDecimal(result, PRECISION);
            }
            case "acos" -> {
                if (args.size() != 1) throw new IllegalArgumentException("acos expects 1 argument");
                BigDecimal arg = args.get(0);
                if (arg.compareTo(BigDecimal.valueOf(-1)) < 0 || arg.compareTo(BigDecimal.valueOf(1)) > 0) {
                    throw new ArithmeticExpressionEvaluator.OutOfDomainException("acos input out of range [-1, 1]");
                }
                double result = Math.acos(arg.doubleValue());
                if (useDegrees) result = Math.toDegrees(result);
                yield new BigDecimal(result, PRECISION);
            }
            case "atan" -> {
                if (args.size() != 1) throw new IllegalArgumentException("atan expects 1 argument");
                double result = Math.atan(args.get(0).doubleValue());
                if (useDegrees) result = Math.toDegrees(result);
                yield new BigDecimal(result, PRECISION);
            }
            case "ln" -> {
                if (args.size() != 1) throw new IllegalArgumentException("ln expects 1 argument");
                BigDecimal arg = args.get(0);
                if (arg.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ArithmeticException("ln of non-positive number");
                }
                yield new BigDecimal(Math.log(arg.doubleValue()), PRECISION);
            }
            case "log" -> {
                if (args.size() != 2) throw new IllegalArgumentException("log expects 2 arguments");
                BigDecimal base = args.get(0);
                BigDecimal value = args.get(1);
                if (base.compareTo(BigDecimal.ZERO) <= 0 || value.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ArithmeticException("log arguments must be positive");
                }
                yield new BigDecimal(Math.log(value.doubleValue()) / Math.log(base.doubleValue()), PRECISION);
            }
            case "nrt" -> {
                if (args.size() != 2) throw new IllegalArgumentException("nrt expects 2 arguments");
                BigDecimal n = args.get(0);
                BigDecimal x = args.get(1);
                if (x.compareTo(BigDecimal.ZERO) < 0 && n.intValue() % 2 == 0) {
                    throw new ArithmeticException("Even root of negative number");
                }
                double result = Math.pow(x.doubleValue(), 1.0 / n.doubleValue());
                yield new BigDecimal(result, PRECISION);
            }
            case "ceil" -> {
                if (args.size() != 1) throw new IllegalArgumentException("ceil expects 1 argument");
                yield args.get(0).setScale(0, RoundingMode.CEILING);
            }
            case "floor" -> {
                if (args.size() != 1) throw new IllegalArgumentException("floor expects 1 argument");
                yield args.get(0).setScale(0, RoundingMode.FLOOR);
            }
            case "abs" -> {
                if (args.size() != 1) throw new IllegalArgumentException("abs expects 1 argument");
                yield args.get(0).abs();
            }
            case "min" -> {
                if (args.size() != 2) throw new IllegalArgumentException("min expects 2 arguments");
                yield args.get(0).min(args.get(1));
            }
            case "max" -> {
                if (args.size() != 2) throw new IllegalArgumentException("max expects 2 arguments");
                yield args.get(0).max(args.get(1));
            }
            case "√" -> {
                if (args.size() != 1) throw new IllegalArgumentException("√ expects 1 argument");
                BigDecimal x = args.get(0);
                if (x.compareTo(BigDecimal.ZERO) < 0) {
                    throw new ArithmeticException("Square root of negative number");
                }
                yield new BigDecimal(Math.sqrt(x.doubleValue()), PRECISION);
            }
            case "nPr" -> permutation(args.get(0), args.get(1));
            case "nCr" -> combination(args.get(0), args.get(1));
            case "gcd" -> gcd(args.get(0), args.get(1));
            case "sinh" -> new BigDecimal(Math.sinh(args.get(0).doubleValue()), PRECISION);
            case "cosh" -> new BigDecimal(Math.cosh(args.get(0).doubleValue()), PRECISION);
            case "tanh" -> new BigDecimal(Math.tanh(args.get(0).doubleValue()), PRECISION);
            case "asinh" -> new BigDecimal(asinh(args.get(0).doubleValue()), PRECISION);
            case "acosh" -> new BigDecimal(acosh(args.get(0).doubleValue()), PRECISION);
            case "atanh" -> new BigDecimal(atanh(args.get(0).doubleValue()), PRECISION);
            default -> throw new UnsupportedOperationException("Function not supported: " + function);
        };
    }

    /**
     * Computes the factorial of a number.
     *
     * @param n The number.
     * @return The factorial of n.
     */
    public static BigDecimal factorial(BigDecimal n) {
        if (n.stripTrailingZeros().scale() > 0) {
            throw new ArithmeticException("Factorial argument must be an integer");
        }
        BigInteger intN = n.toBigIntegerExact();
        if (intN.compareTo(BigInteger.ZERO) < 0) {
            throw new ArithmeticException("Factorial of negative number");
        }
        BigInteger result = BigInteger.ONE;
        for (BigInteger i = BigInteger.TWO; i.compareTo(intN) <= 0; i = i.add(BigInteger.ONE)) {
            result = result.multiply(i);
        }
        return new BigDecimal(result, PRECISION);
    }

    /**
     * Computes the permutation nPr.
     *
     * @param n The total number of items.
     * @param r The number of items to choose.
     * @return The permutation.
     */
    private static BigDecimal permutation(BigDecimal n, BigDecimal r) {
        if (n.stripTrailingZeros().scale() > 0 || r.stripTrailingZeros().scale() > 0) {
            throw new ArithmeticException("Permutation arguments must be integers");
        }
        BigInteger intN = n.toBigIntegerExact();
        BigInteger intR = r.toBigIntegerExact();
        if (intR.compareTo(BigInteger.ZERO) < 0 || intR.compareTo(intN) > 0) {
            return BigDecimal.ZERO;
        }
        BigInteger result = BigInteger.ONE;
        BigInteger temp = intN;
        for (BigInteger i = BigInteger.ZERO; i.compareTo(intR) < 0; i = i.add(BigInteger.ONE)) {
            result = result.multiply(temp);
            temp = temp.subtract(BigInteger.ONE);
        }
        return new BigDecimal(result, PRECISION);
    }

    /**
     * Computes the combination nCr.
     *
     * @param n The total number of items.
     * @param r The number of items to choose.
     * @return The combination.
     */
    private static BigDecimal combination(BigDecimal n, BigDecimal r) {
        if (n.stripTrailingZeros().scale() > 0 || r.stripTrailingZeros().scale() > 0) {
            throw new ArithmeticException("Combination arguments must be integers");
        }
        BigInteger intN = n.toBigIntegerExact();
        BigInteger intR = r.toBigIntegerExact();
        if (intR.compareTo(BigInteger.ZERO) < 0 || intR.compareTo(intN) > 0) {
            return BigDecimal.ZERO;
        }
        BigInteger k = intR.min(intN.subtract(intR));
        BigInteger result = BigInteger.ONE;
        for (BigInteger i = BigInteger.ZERO; i.compareTo(k) < 0; i = i.add(BigInteger.ONE)) {
            result = result.multiply(intN.subtract(i)).divide(i.add(BigInteger.ONE));
        }
        return new BigDecimal(result, PRECISION);
    }

    /**
     * Computes the greatest common divisor (GCD) of two numbers.
     *
     * @param a The first number.
     * @param b The second number.
     * @return The GCD.
     */
    private static BigDecimal gcd(BigDecimal a, BigDecimal b) {
        if (a.stripTrailingZeros().scale() > 0 || b.stripTrailingZeros().scale() > 0) {
            throw new ArithmeticException("GCD arguments must be integers");
        }
        BigInteger intA = a.toBigIntegerExact();
        BigInteger intB = b.toBigIntegerExact();
        BigInteger gcd = intA.gcd(intB);
        return new BigDecimal(gcd, PRECISION);
    }

    /**
     * Computes the inverse hyperbolic sine.
     *
     * @param x The value.
     * @return The inverse hyperbolic sine of x.
     */
    private static double asinh(double x) {
        return Math.log(x + Math.sqrt(x * x + 1));
    }

    /**
     * Computes the inverse hyperbolic cosine.
     *
     * @param x The value.
     * @return The inverse hyperbolic cosine of x.
     */
    private static double acosh(double x) {
        if (x < 1) {
            throw new ArithmeticExpressionEvaluator.OutOfDomainException("acosh input must be >=1");
        }
        return Math.log(x + Math.sqrt(x * x - 1));
    }

    /**
     * Computes the inverse hyperbolic tangent.
     *
     * @param x The value.
     * @return The inverse hyperbolic tangent of x.
     */
    private static double atanh(double x) {
        if (Math.abs(x) >= 1) {
            throw new ArithmeticExpressionEvaluator.OutOfDomainException("atanh input must be in (-1,1)");
        }
        return 0.5 * Math.log((1 + x) / (1 - x));
    }
}
