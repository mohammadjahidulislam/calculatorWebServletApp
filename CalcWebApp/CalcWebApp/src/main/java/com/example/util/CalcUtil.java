package com.example.util;

import java.util.Set;

public class CalcUtil {

    /** Operations that need two numbers. */
    public static final Set<String> BINARY = Set.of("ADD", "SUB", "MUL", "DIV", "MOD", "POW");

    /** Operations that need one number. */
    public static final Set<String> UNARY = Set.of("SQRT", "ABS", "FACT");

    private CalcUtil() { }

    /** Holds either a value or an error message. */
    public static class Result {
        public final Double value;
        public final String error;

        public Result(Double value, String error) {
            this.value = value;
            this.error = error;
        }
    }

    public static boolean isValidOp(String op) {
        return op != null && (BINARY.contains(op) || UNARY.contains(op));
    }

    public static boolean needsSecondNumber(String op) {
        return BINARY.contains(op);
    }

    /** All the math lives here. b is null for single-number operations. */
    public static Result calculate(String op, double a, Double b) {
        switch (op) {
            case "ADD": return new Result(a + b, null);
            case "SUB": return new Result(a - b, null);
            case "MUL": return new Result(a * b, null);
            case "DIV":
                if (b == 0) return new Result(null, "Division by zero");
                return new Result(a / b, null);
            case "MOD":
                if (b == 0) return new Result(null, "Modulo by zero");
                return new Result(a % b, null);
            case "POW":
                double p = Math.pow(a, b);
                if (Double.isNaN(p) || Double.isInfinite(p))
                    return new Result(null, "Invalid power result");
                return new Result(p, null);
            case "SQRT":
                if (a < 0) return new Result(null, "Square root of a negative number");
                return new Result(Math.sqrt(a), null);
            case "ABS":
                return new Result(Math.abs(a), null);
            case "FACT":
                if (a < 0 || a != Math.floor(a) || a > 20)
                    return new Result(null, "Factorial: enter a whole number from 0 to 20");
                double f = 1;
                for (int i = 2; i <= (int) a; i++) f *= i;
                return new Result(f, null);
            default:
                return new Result(null, "Unknown operation: " + op);
        }
    }

    /** Symbol used when showing an expression, e.g. ADD -> "+". */
    public static String symbol(String op) {
        switch (op) {
            case "ADD": return "+";
            case "SUB": return "-";
            case "MUL": return "*";
            case "DIV": return "/";
            case "MOD": return "%";
            case "POW": return "^";
            default:    return op;
        }
    }

    /** Console helper: "+" -> "ADD", "sqrt" -> "SQRT", etc. Returns null if unknown. */
    public static String fromSymbol(String s) {
        if (s == null) return null;
        switch (s.trim().toLowerCase()) {
            case "+":    return "ADD";
            case "-":    return "SUB";
            case "*":    return "MUL";
            case "/":    return "DIV";
            case "%":    return "MOD";
            case "^":    return "POW";
            case "sqrt": return "SQRT";
            case "abs":  return "ABS";
            case "fact": return "FACT";
            default:     return null;
        }
    }

    /** 15.0 -> "15", 2.5 -> "2.5" */
    public static String format(double d) {
        if (d == Math.rint(d) && Math.abs(d) < 1e15) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }

    /** Human readable expression, e.g. "10 + 5" or "SQRT(144)". */
    public static String describe(String op, double a, Double b) {
        if (BINARY.contains(op) && b != null) {
            return format(a) + " " + symbol(op) + " " + format(b);
        }
        return op + "(" + format(a) + ")";
    }
}
