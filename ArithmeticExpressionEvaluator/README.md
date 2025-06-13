# ArithmeticExpressionEvaluator

A Java-based mathematical expression evaluator that parses and computes string-based mathematical expressions with high precision. It supports arithmetic operations, trigonometric functions, constants, and special operations like factorial and square root.

## Features
- **Arithmetic Operations**: Addition (+), subtraction (-), multiplication (*), division (/), exponentiation (^).
- **Trigonometric Functions**: sin, cos, tan, asin, acos, atan (supports degrees or radians).
- **Logarithmic Functions**: Natural logarithm (ln), base-10 logarithm (log).
- **Constants**: pi, e.
- **Special Operations**: Square root (√), factorial (!), percentage (%).
- **High Precision**: Uses `BigDecimal` with 30-digit precision and `HALF_UP` rounding.
- **Error Handling**: Throws exceptions for invalid expressions, division by zero, and out-of-domain operations (e.g., square root of negative numbers, negative base with even-denominator fractional exponents).

## Prerequisites
- Java Development Kit (JDK) 8 or higher.

## Directory Structure
```
ArithmeticExpressionEvaluator/
├── src/
│   └── ArithmeticExpressionEvaluator.java
├── test/
│   └── Main.java
├── README.md
├── .gitignore
```

## Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Ayush-Chauhan/ArithmeticExpressionEvaluator.git
   ```
2. Navigate to the project directory:
   ```bash
   cd ArithmeticExpressionEvaluator
   ```
3. Compile the source and test files:
   ```bash
   javac -d bin src/ArithmeticExpressionEvaluator.java test/Main.java
   ```

## Usage
Run the test harness to evaluate example expressions:
```bash
java -cp bin Main
```

Alternatively, use `ArithmeticExpressionEvaluator` in your own code:
```java
try {
    double result = ArithmeticExpressionEvaluator.evaluate("2 + 3 * sin(pi / 2)", false);
    System.out.println("Result: " + result); // Output: 5.0
} catch (Exception e) {
    System.err.println("Error: " + e.getMessage());
}
```

## Examples
- `2 + 3 * 4` → `14.0`
- `sin(pi / 2)` (radians) → `1.0`
- `sin(90)` (degrees) → `1.0`
- `(-8)^(1/3)` → `-2.0`
- `(-4)^(1/2)` → Throws `OutOfDomainException` (even denominator).
- `sqrt(16)` → `4.0`
- `5!` → `120.0`

## Contributing
Contributions are welcome! Please fork the repository and submit a pull request with your changes. Ensure that your code follows Java coding conventions and includes appropriate documentation.

## License
This project is licensed under the MIT License. Copyright (c) 2025 Ayush Chauhan. See the [LICENSE](LICENSE.txt) file for details.

## Contact
For questions or feedback, please open an issue on GitHub or contact Ayush Chauhan via GitHub.
