package com.agenthub.infrastructure.tools.function_tools.base_tools;

import com.agenthub.infrastructure.tools.function_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.util.Arrays;

@AgentTools(name = "MathTools", description = "数学计算工具，提供基础数学运算、三角函数、对数、阶乘、最大公约数、最小公倍数等数学功能", defaultEnable = false)
public class MathTools {

    @Tool(name = "math_add", description = "Add two numbers")
    public double add(double a, double b) {
        return a + b;
    }

    @Tool(name = "math_subtract", description = "Subtract two numbers")
    public double subtract(double a, double b) {
        return a - b;
    }

    @Tool(name = "math_multiply", description = "Multiply two numbers")
    public double multiply(double a, double b) {
        return a * b;
    }

    @Tool(name = "math_divide", description = "Divide two numbers")
    public double divide(double a, double b) {
        return a / b;
    }

    @Tool(name = "math_modulo", description = "Modulo operation")
    public double modulo(double a, double b) {
        return a % b;
    }

    @Tool(name = "math_power", description = "Raise to power")
    public double power(double base, double exponent) {
        return Math.pow(base, exponent);
    }

    @Tool(name = "math_sqrt", description = "Square root")
    public double sqrt(double value) {
        return Math.sqrt(value);
    }

    @Tool(name = "math_cbrt", description = "Cube root")
    public double cbrt(double value) {
        return Math.cbrt(value);
    }

    @Tool(name = "math_abs", description = "Absolute value")
    public double abs(double value) {
        return Math.abs(value);
    }

    @Tool(name = "math_round", description = "Round to nearest integer")
    public long round(double value) {
        return Math.round(value);
    }

    @Tool(name = "math_floor", description = "Floor value")
    public double floor(double value) {
        return Math.floor(value);
    }

    @Tool(name = "math_ceil", description = "Ceiling value")
    public double ceil(double value) {
        return Math.ceil(value);
    }

    @Tool(name = "math_max", description = "Maximum of two numbers")
    public double max(double a, double b) {
        return Math.max(a, b);
    }

    @Tool(name = "math_min", description = "Minimum of two numbers")
    public double min(double a, double b) {
        return Math.min(a, b);
    }

    @Tool(name = "math_sin", description = "Sine in radians")
    public double sin(double radians) {
        return Math.sin(radians);
    }

    @Tool(name = "math_cos", description = "Cosine in radians")
    public double cos(double radians) {
        return Math.cos(radians);
    }

    @Tool(name = "math_tan", description = "Tangent in radians")
    public double tan(double radians) {
        return Math.tan(radians);
    }

    @Tool(name = "math_log", description = "Natural logarithm")
    public double log(double value) {
        return Math.log(value);
    }

    @Tool(name = "math_log10", description = "Base-10 logarithm")
    public double log10(double value) {
        return Math.log10(value);
    }

    @Tool(name = "math_exp", description = "Euler's number to power")
    public double exp(double value) {
        return Math.exp(value);
    }

    @Tool(name = "math_factorial", description = "Factorial of integer")
    public long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) result *= i;
        return result;
    }

    @Tool(name = "math_gcd", description = "Greatest common divisor")
    public long gcd(long a, long b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    @Tool(name = "math_lcm", description = "Least common multiple")
    public long lcm(long a, long b) {
        return a * b / gcd(a, b);
    }

    @Tool(name = "math_sum", description = "Sum of array")
    public double sum(double[] numbers) {
        return Arrays.stream(numbers).sum();
    }

    @Tool(name = "math_average", description = "Average of array")
    public double average(double[] numbers) {
        return Arrays.stream(numbers).average().orElse(0);
    }

    @Tool(name = "math_is_prime", description = "Check if number is prime")
    public boolean isPrime(long n) {
        if (n < 2) return false;
        for (long i = 2; i * i <= n; i++) if (n % i == 0) return false;
        return true;
    }
}
