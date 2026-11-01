package com.lie.acsm.math;

import java.util.ArrayList;
import java.util.List;


public class SurdCalculator {

    public static String toFormulaString(double value) {
        return toFormulaString(value, 2);
    }

    public static String toFormulaString(double value, int decimalPlaces) {
        try {
            if (Double.isNaN(value))      return "NaN";
            if (Double.isInfinite(value)) return value > 0 ? "\u221E" : "-\u221E";
            if (value == 0)               return "0";

            boolean negative = value < 0;
            double abs = Math.abs(value);

            if (abs == Math.floor(abs) && abs < 1e15) {
                return String.valueOf((long) value);
            }

            long[] frac = tryFraction(abs);
            if (frac != null) {
                if (frac[1] == 1) return String.valueOf(negative ? -frac[0] : frac[0]);
                String sign = negative ? "-" : "";
                return sign + "frac(" + frac[0] + "," + frac[1] + ")";
            }

            String surd = trySurd(abs);
            if (surd != null) {
                return negative ? "-" + surd : surd;
            }

            return formatDecimal(value, decimalPlaces);
        } catch (Exception e) {
            return formatDecimal(value, decimalPlaces);
        }
    }

    public static String toSurdText(double value) {
        return toSurdText(value, 2);
    }

    public static String toTexString(double value) {
        return toTexString(value, 2);
    }

    public static String toTexString(double value, int decimalPlaces) {
        return funcToTex(toFormulaString(value, decimalPlaces));
    }

    public static String funcToTex(String formula) {
        if (formula == null) return "";
        StringBuilder out = new StringBuilder();
        texConv(formula, new int[]{0}, out, false);
        return out.toString();
    }

    private static void texConv(String s, int[] p, StringBuilder out, boolean stop) {
        while (p[0] < s.length()) {
            char c = s.charAt(p[0]);
            if (stop && (c == ',' || c == ')')) return;
            if (s.startsWith("frac(", p[0])) {
                p[0] += 5;
                texTwo(s, p, out, "\\f");
                continue;
            }
            if (s.startsWith("nroot(", p[0])) {
                p[0] += 6;
                texTwo(s, p, out, "\\n");
                continue;
            }
            if (s.startsWith("pow(", p[0])) {
                p[0] += 4;
                texTwo(s, p, out, "\\p");
                continue;
            }
            if (s.startsWith("sqrt(", p[0])) {
                p[0] += 5;
                out.append("\\s{");
                texConv(s, p, out, true);
                out.append('}');
                if (p[0] < s.length() && s.charAt(p[0]) == ')') p[0]++;
                continue;
            }
            out.append(c);
            p[0]++;
        }
    }

    private static void texTwo(String s, int[] p, StringBuilder out, String head) {
        out.append(head);
        out.append('{');
        texConv(s, p, out, true);
        out.append("}{");
        if (p[0] < s.length() && s.charAt(p[0]) == ',') p[0]++;
        texConv(s, p, out, true);
        out.append('}');
        if (p[0] < s.length() && s.charAt(p[0]) == ')') p[0]++;
    }

    public static String toSurdText(double value, int decimalPlaces) {
        String formula = toFormulaString(value, decimalPlaces);
        return formulaToPlain(formula);
    }

    public static String formulaToPlain(String formula) {
        if (formula == null) return "";
        formula = formula.replaceAll("frac\\(([^,]+),([^)]+)\\)", "$1/$2");
        formula = formula.replaceAll("sqrt\\(([^)]+)\\)", "\u221A$1");
        formula = formula.replaceAll("nroot\\(([^,]+),([^)]+)\\)", "$1\u221A$2");
        formula = formula.replaceAll("pow\\(([^,]+),([^)]+)\\)", "$1^$2");
        return formula;
    }


    public static long[] tryFraction(double abs) {
        if (abs > 1000) return null;
        double tol = 1e-9 * Math.max(1.0, abs);
        int maxDenom = 1000;
        for (int d = 1; d <= maxDenom; d++) {
            long n = Math.round(abs * d);
            if (n == 0) continue;
            double diff = Math.abs(abs - (double) n / d);
            if (diff < tol) {
                long g = gcd(n, d);
                return new long[]{n / g, d / g};
            }
        }
        return null;
    }


    private static boolean nearInt(double v, long target) {
        return Math.abs(v - target) <= 1e-9 * Math.max(1.0, Math.abs((double) target));
    }

    private static String trySurd(double abs) {
        if (abs < 1e-9 || abs > 1000) return null;

        double sq = abs * abs;
        long sqInt = Math.round(sq);
        if (nearInt(sq, sqInt) && sqInt > 0 && sqInt < 100000) {
            String simplified = simplifyRoot(sqInt);
            if (simplified != null) return simplified;
        }

        for (int d = 2; d <= 20; d++) {
            double val = abs * d;
            double valSq = val * val;
            long valSqInt = Math.round(valSq);
            if (nearInt(valSq, valSqInt) && valSqInt > 0 && valSqInt < 100000) {
                String surd = simplifyRoot(valSqInt);
                if (surd != null && !surd.matches("\\d+")) {
                    long coef = extractSurdCoefficient(surd);
                    if (coef > 1) {
                        long g = gcd(coef, d);
                        if (g > 1) {
                            long inner = extractSurdInner(surd);
                            long newCoef = coef / g;
                            long newDenom = d / g;
                            String newSurd = newCoef == 1 ? "sqrt(" + inner + ")" : newCoef + "sqrt(" + inner + ")";
                            if (newDenom == 1) return newSurd;
                            return "frac(" + newSurd + "," + newDenom + ")";
                        }
                    }
                    return "frac(" + surd + "," + d + ")";
                }
            }
        }

        for (int k = 2; k <= 20; k++) {
            double val = abs / k;
            double valSq = val * val;
            long valSqInt = Math.round(valSq);
            if (nearInt(valSq, valSqInt) && valSqInt > 0 && valSqInt < 100000) {
                String surd = simplifyRoot(valSqInt);
                if (surd != null && !surd.matches("\\d+")) {
                    long coef = extractSurdCoefficient(surd) * k;
                    long inner = extractSurdInner(surd);
                    if (coef == 1) return "sqrt(" + inner + ")";
                    return coef + "sqrt(" + inner + ")";
                }
            }
        }

        return null;
    }

    public static String simplifyRoot(long n) {
        if (n <= 0) return null;
        if (n == 1) return "1";

        long outer = 1;
        long inner = n;

        for (long p = 2; p * p <= inner; p++) {
            while (inner % (p * p) == 0) {
                outer *= p;
                inner /= (p * p);
            }
        }

        if (inner == 1) return String.valueOf(outer);
        if (outer == 1) return "sqrt(" + inner + ")";
        return outer + "sqrt(" + inner + ")";
    }

    private static long extractSurdCoefficient(String surd) {
        int idx = surd.indexOf("sqrt(");
        if (idx <= 0) return 1;
        try {
            return Long.parseLong(surd.substring(0, idx));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static long extractSurdInner(String surd) {
        int start = surd.indexOf("sqrt(") + 5;
        int end = surd.lastIndexOf(")");
        if (start < 5 || end < 0) return 1;
        try {
            return Long.parseLong(surd.substring(start, end));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long t = b;
            b = a % b;
            a = t;
        }
        return Math.abs(a);
    }

    private static String formatDecimal(double v, int places) {
        if (v == Math.floor(v) && Math.abs(v) < 1e15) {
            return String.valueOf((long) v);
        }
        if (places <= 0) return String.valueOf(Math.round(v));
        String s = String.format("%." + places + "f", v);
        s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }
}
