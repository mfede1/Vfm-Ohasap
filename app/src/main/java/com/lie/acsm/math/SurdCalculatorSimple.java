package com.lie.acsm.math;

public class SurdCalculatorSimple {

    public static String toSurdText(double value) {
        try {
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return "NaN";
            }

            if (value == (long) value) {
                return String.valueOf((long) value);
            }

            return String.format("%.2f", value);
        } catch (Exception e) {
            return "0";
        }
    }
}
