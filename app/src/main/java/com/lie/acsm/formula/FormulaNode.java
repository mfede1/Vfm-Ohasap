package com.lie.acsm.formula;

import java.util.ArrayList;
import java.util.List;

public class FormulaNode {

    public enum Type {
        TEXT,
        FRACTION,
        SQRT,
        NROOT,
        POWER,
        SEQUENCE
    }

    public final Type type;
    public final String text;
    public final List<FormulaNode> children;

    private FormulaNode(Type type, String text) {
        this.type = type;
        this.text = text;
        this.children = new ArrayList<>();
    }

    public static FormulaNode text(String s) {
        return new FormulaNode(Type.TEXT, s);
    }

    public static FormulaNode fraction(FormulaNode num, FormulaNode den) {
        FormulaNode n = new FormulaNode(Type.FRACTION, null);
        n.children.add(num);
        n.children.add(den);
        return n;
    }

    public static FormulaNode sqrt(FormulaNode arg) {
        FormulaNode n = new FormulaNode(Type.SQRT, null);
        n.children.add(arg);
        return n;
    }

    public static FormulaNode nroot(FormulaNode index, FormulaNode arg) {
        FormulaNode n = new FormulaNode(Type.NROOT, null);
        n.children.add(index);
        n.children.add(arg);
        return n;
    }

    public static FormulaNode power(FormulaNode base, FormulaNode exp) {
        FormulaNode n = new FormulaNode(Type.POWER, null);
        n.children.add(base);
        n.children.add(exp);
        return n;
    }

    public static FormulaNode sequence(List<FormulaNode> nodes) {
        if (nodes.size() == 1) return nodes.get(0);
        FormulaNode n = new FormulaNode(Type.SEQUENCE, null);
        n.children.addAll(nodes);
        return n;
    }
}
