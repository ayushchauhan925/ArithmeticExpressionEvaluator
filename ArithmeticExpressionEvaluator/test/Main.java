/**
 * Main class to test the ArithmeticExpressionEvaluator with a range of mathematical expressions,
 * from simple to complex, to verify functionality and error handling.
 *
 * @author Ayush Chauhan
 * @since 2025-07-01
 */
public class Main {
    public static void main(String[] args) {
        // Define test expressions and corresponding degree/radian modes
        String[] expressions = {
            "2 + 2",                        // Simple addition
            "10 - 3 * 2",                  // Basic arithmetic with precedence
            "4 / 2 + 3 * 2",               // Mixed arithmetic operations
            "(2 + 3) * 4",                 // Parentheses for grouping
            "pi",                          // Constant evaluation
            "e ^ 2",                       // Exponentiation with constant
            "sin(90)",                     // Trigonometric function (degrees)
            "cos(pi)",                     // Trigonometric function (radians)
            "tan(45)",                     // Trigonometric function (degrees)
            "ln(e)",                       // Natural logarithm
            "log(100)",                    // Base-10 logarithm
            "sqrt(16)",                    // Square root
            "5!",                          // Factorial
            "50%",                         // Percentage
            "(-8)^(1/3)",                 // Cube root of negative number
            "(-4)^(1/2)",                 // Square root of negative number (should throw exception)
            "4 / 0",                       // Division by zero (should throw exception)
            "asin(0.5)",                   // Inverse trigonometric function (degrees)
            "2 ^ (3 + 2)",                // Nested exponentiation
            "sin(cos(0))",                 // Nested trigonometric functions
            "ln(2 + 3) * sqrt(9)",         // Combined log and square root
            "(-2)^(-5/7)",                // Negative base with odd-denominator fraction
            "(-3)^(-5/6)",                // Negative base with even-denominator fraction (should throw exception)
            "(2 + sin(pi / 2)) ^ (1 / 3)", // Complex expression with trig and exponent
            "5! * log(100) + sqrt(16)",    // Complex mix of factorial, log, and square root
            "asin(sin(30)) + acos(cos(60))", // Nested inverse and direct trig functions
            "e ^ (ln(2) * 3) - pi",        // Complex exponential with log and constant
            "1..2"                         // Invalid number format (should throw exception)
        };
        boolean[] useDegrees = {
            false, false, false, false, false, false,
            true,  false, true,  false, false, false,
            false, false, false, false, false, true,
            false, false, false, false, false, false,
            false, true,  false, false
        };

        System.out.println("Testing ArithmeticExpressionEvaluator...");
        System.out.println("----------------------------------------");

        for (int i = 0; i < expressions.length; i++) {
            try {
                double result = ArithmeticExpressionEvaluator.evaluate(expressions[i], useDegrees[i]);
                System.out.printf("Expression: %-30s Result: %.6f%n", expressions[i], result);
            } catch (Exception e) {
                System.out.printf("Expression: %-30s Error: %s%n", expressions[i], e.getMessage());
            }
        }

        System.out.println("----------------------------------------");
        System.out.println("Testing complete.");
    }
}
