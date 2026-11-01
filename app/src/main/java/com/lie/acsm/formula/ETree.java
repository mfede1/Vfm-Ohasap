package com.lie.acsm.formula;

public class ETree {

    public final ESeq root;

    public ETree() {
        root = new ESeq(null, -1);
    }

    public static ETree fromString(String s) {
        ETree t = new ETree();
        if (s == null || s.isEmpty()) return t;
        parseSeq(t.root, s, new int[]{0});
        return t;
    }

    private static void parseSeq(ESeq seq, String s, int[] pos) {
        while (pos[0] < s.length()) {
            char c = s.charAt(pos[0]);
            if (c == ')' || c == ',') break;

            String func = peekFunc(s, pos[0]);
            if (func != null) {
                pos[0] += func.length();
                if (pos[0] < s.length() && s.charAt(pos[0]) == '(') pos[0]++;
                ENode n = buildFunc(func);
                if (n != null) {
                    parseFuncArgs(n, s, pos);
                    n.parentSeq = seq;
                    seq.nodes.add(n);
                } else {
                    String name = func;
                    while (pos[0] < s.length() && s.charAt(pos[0]) != ')') pos[0]++;
                    if (pos[0] < s.length()) pos[0]++;
                    for (char ch : (name + "()").toCharArray()) {
                        ENode ch2 = ENode.ofChar(ch);
                        ch2.parentSeq = seq;
                        seq.nodes.add(ch2);
                    }
                }
                continue;
            }

            if (c == '^') {
                pos[0]++;
                ENode pow = ENode.ofPower();
                ENode base = seq.nodes.isEmpty() ? ENode.ofChar('?') : seq.nodes.remove(seq.nodes.size() - 1);
                base.parentSeq = pow.slots[0];
                pow.slots[0].nodes.add(base);
                if (pos[0] < s.length() && s.charAt(pos[0]) == '{') {
                    pos[0]++;
                    parseSeq(pow.slots[1], s, pos);
                    if (pos[0] < s.length() && s.charAt(pos[0]) == '}') pos[0]++;
                } else {
                    char nc = pos[0] < s.length() ? s.charAt(pos[0]++) : '?';
                    ENode en = ENode.ofChar(nc);
                    en.parentSeq = pow.slots[1];
                    pow.slots[1].nodes.add(en);
                }
                pow.parentSeq = seq;
                seq.nodes.add(pow);
                continue;
            }

            if (c == '/' ) {
                pos[0]++;
                ENode frac = ENode.ofFrac();
                ENode num = seq.nodes.isEmpty() ? ENode.ofChar('?') : seq.nodes.remove(seq.nodes.size() - 1);
                num.parentSeq = frac.slots[0];
                frac.slots[0].nodes.add(num);
                parseDenSimple(frac.slots[1], s, pos);
                frac.parentSeq = seq;
                seq.nodes.add(frac);
                continue;
            }

            if (c == '\u221A') {
                pos[0]++;
                ENode sq = ENode.ofSqrt();
                if (pos[0] < s.length() && s.charAt(pos[0]) == '(') {
                    pos[0]++;
                    parseSeq(sq.slots[0], s, pos);
                    if (pos[0] < s.length() && s.charAt(pos[0]) == ')') pos[0]++;
                } else {
                    char nc = pos[0] < s.length() ? s.charAt(pos[0]++) : '?';
                    ENode en = ENode.ofChar(nc);
                    en.parentSeq = sq.slots[0];
                    sq.slots[0].nodes.add(en);
                }
                sq.parentSeq = seq;
                seq.nodes.add(sq);
                continue;
            }

            pos[0]++;
            ENode en = ENode.ofChar(c);
            en.parentSeq = seq;
            seq.nodes.add(en);
        }
    }

    private static String peekFunc(String s, int pos) {
        String[] funcs = {"frac", "sqrt", "nroot", "pow",
                "sin", "cos", "tan", "asin", "acos", "atan", "ln", "log"};
        for (String f : funcs) {
            int end = pos + f.length();
            if (end < s.length() && s.substring(pos, end).equals(f) && s.charAt(end) == '(') {
                return f;
            }
        }
        return null;
    }

    private static ENode buildFunc(String func) {
        switch (func) {
            case "frac":  return ENode.ofFrac();
            case "sqrt":  return ENode.ofSqrt();
            case "nroot": return ENode.ofNroot();
            case "pow":   return ENode.ofPower();
            default:      return null;
        }
    }

    private static void parseFuncArgs(ENode n, String s, int[] pos) {
        switch (n.kind) {
            case FRAC:
                parseSeq(n.slots[0], s, pos);
                if (pos[0] < s.length() && s.charAt(pos[0]) == ',') pos[0]++;
                parseSeq(n.slots[1], s, pos);
                if (pos[0] < s.length() && s.charAt(pos[0]) == ')') pos[0]++;
                break;
            case SQRT:
                parseSeq(n.slots[0], s, pos);
                if (pos[0] < s.length() && s.charAt(pos[0]) == ')') pos[0]++;
                break;
            case NROOT:
                parseSeq(n.slots[0], s, pos);
                if (pos[0] < s.length() && s.charAt(pos[0]) == ',') pos[0]++;
                parseSeq(n.slots[1], s, pos);
                if (pos[0] < s.length() && s.charAt(pos[0]) == ')') pos[0]++;
                break;
            case POWER:
                parseSeq(n.slots[0], s, pos);
                if (pos[0] < s.length() && s.charAt(pos[0]) == ',') pos[0]++;
                parseSeq(n.slots[1], s, pos);
                if (pos[0] < s.length() && s.charAt(pos[0]) == ')') pos[0]++;
                break;
            default:
                break;
        }
    }

    private static void parseDenSimple(ESeq seq, String s, int[] pos) {
        while (pos[0] < s.length()) {
            char c = s.charAt(pos[0]);
            if (c == '+' || c == '-' || c == ')') break;
            ENode en = ENode.ofChar(c);
            en.parentSeq = seq;
            seq.nodes.add(en);
            pos[0]++;
        }
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        serializeSeq(root, sb);
        return sb.toString();
    }

    private static void serializeSeq(ESeq seq, StringBuilder sb) {
        for (ENode n : seq.nodes) {
            serializeNode(n, sb);
        }
    }

    private static void serializeNode(ENode n, StringBuilder sb) {
        switch (n.kind) {
            case CHAR:
                sb.append(n.ch);
                break;
            case FRAC:
                sb.append("frac(");
                serializeSeq(n.slots[0], sb);
                sb.append(',');
                serializeSeq(n.slots[1], sb);
                sb.append(')');
                break;
            case SQRT:
                sb.append("sqrt(");
                serializeSeq(n.slots[0], sb);
                sb.append(')');
                break;
            case NROOT:
                sb.append("nroot(");
                serializeSeq(n.slots[0], sb);
                sb.append(',');
                serializeSeq(n.slots[1], sb);
                sb.append(')');
                break;
            case POWER:
                sb.append("pow(");
                serializeSeq(n.slots[0], sb);
                sb.append(',');
                serializeSeq(n.slots[1], sb);
                sb.append(')');
                break;
        }
    }
}
