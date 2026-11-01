package com.lie.acsm.formula;

public class ENode {

    public enum Kind {
        CHAR,
        FRAC,
        SQRT,
        NROOT,
        POWER
    }

    public final Kind kind;
    public final char ch;
    public final ESeq[] slots;

    public ESeq parentSeq;

    private ENode(Kind kind, char ch, int numSlots) {
        this.kind = kind;
        this.ch = ch;
        if (numSlots > 0) {
            slots = new ESeq[numSlots];
            for (int i = 0; i < numSlots; i++) {
                slots[i] = new ESeq(this, i);
            }
        } else {
            slots = null;
        }
    }

    public static ENode ofChar(char c) {
        return new ENode(Kind.CHAR, c, 0);
    }

    public static ENode ofFrac() {
        return new ENode(Kind.FRAC, '\0', 2);
    }

    public static ENode ofSqrt() {
        return new ENode(Kind.SQRT, '\0', 1);
    }

    public static ENode ofNroot() {
        return new ENode(Kind.NROOT, '\0', 2);
    }

    public static ENode ofPower() {
        return new ENode(Kind.POWER, '\0', 2);
    }
}
