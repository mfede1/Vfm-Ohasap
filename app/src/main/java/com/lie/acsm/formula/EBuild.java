package com.lie.acsm.formula;

import java.util.ArrayList;
import java.util.List;

public class EBuild {

    private static final String[] NAMED = {"asin", "acos", "atan", "sin", "cos", "tan", "log", "ln"};

    public static ETree fromTex(String s) {
        ETree t = new ETree();
        if (s == null || s.length() == 0) return t;
        texSeq(t.root, s, new int[]{0}, false);
        return t;
    }

    private static void texSeq(ESeq seq, String s, int[] p, boolean inGroup) {
        while (p[0] < s.length()) {
            char c = s.charAt(p[0]);
            if (inGroup && c == '}') {
                p[0]++;
                return;
            }
            if (c == '\\' && p[0] + 1 < s.length()) {
                char k = s.charAt(p[0] + 1);
                if (k == 'f' || k == 's' || k == 'n' || k == 'p') {
                    p[0] += 2;
                    ENode n;
                    if (k == 'f') n = ENode.ofFrac();
                    else if (k == 's') n = ENode.ofSqrt();
                    else if (k == 'n') n = ENode.ofNroot();
                    else n = ENode.ofPower();
                    for (int i = 0; i < n.slots.length; i++) {
                        if (p[0] < s.length() && s.charAt(p[0]) == '{') {
                            p[0]++;
                            texSeq(n.slots[i], s, p, true);
                        }
                    }
                    seq.insert(seq.size(), n);
                    continue;
                }
                if (k == '\\') {
                    p[0] += 2;
                    seq.insert(seq.size(), ENode.ofChar('\\'));
                    continue;
                }
            }
            p[0]++;
            seq.insert(seq.size(), ENode.ofChar(c));
        }
    }

    public static ETree fromPlain(String s) {
        ETree t = new ETree();
        if (s == null || s.length() == 0) return t;
        try {
            List<ENode> out = plainSeq(s, new int[]{0}, false);
            for (int i = 0; i < out.size(); i++) {
                t.root.insert(t.root.size(), out.get(i));
            }
            return t;
        } catch (Exception e) {
            ETree f = new ETree();
            for (int i = 0; i < s.length(); i++) {
                f.root.insert(f.root.size(), ENode.ofChar(s.charAt(i)));
            }
            return f;
        }
    }

    private static List<ENode> plainSeq(String s, int[] p, boolean stop) {
        List<ENode> out = new ArrayList<ENode>();
        while (p[0] < s.length()) {
            char c = s.charAt(p[0]);
            if (stop && (c == ')' || c == ',')) break;
            int before = p[0];
            out.addAll(plainExpr(s, p));
            if (p[0] == before) {
                out.add(ENode.ofChar(c));
                p[0]++;
            }
        }
        return out;
    }

    private static List<ENode> plainExpr(String s, int[] p) {
        List<ENode> left = plainTerm(s, p);
        while (p[0] < s.length()) {
            char c = s.charAt(p[0]);
            if (c == '+' || c == '-' || c == '−') {
                p[0]++;
                left.add(ENode.ofChar(c == '−' ? '-' : c));
                left.addAll(plainTerm(s, p));
            } else {
                break;
            }
        }
        return left;
    }

    private static List<ENode> plainTerm(String s, int[] p) {
        List<ENode> left = plainFactor(s, p);
        while (p[0] < s.length()) {
            char c = s.charAt(p[0]);
            if (c == '/' || c == '÷') {
                p[0]++;
                List<ENode> right = plainFactor(s, p);
                ENode frac = ENode.ofFrac();
                fill(frac.slots[0], left);
                fill(frac.slots[1], right);
                left = new ArrayList<ENode>();
                left.add(frac);
            } else if (c == '*' || c == '×' || c == '·') {
                p[0]++;
                left.add(ENode.ofChar('×'));
                left.addAll(plainFactor(s, p));
            } else if (startsAtom(s, p[0])) {
                int before = p[0];
                List<ENode> more = plainFactor(s, p);
                if (p[0] == before) break;
                left.addAll(more);
            } else {
                break;
            }
        }
        return left;
    }

    private static List<ENode> plainFactor(String s, int[] p) {
        List<ENode> base = plainAtom(s, p);
        if (p[0] < s.length() && s.charAt(p[0]) == '^' && !base.isEmpty()) {
            p[0]++;
            List<ENode> exp = plainFactor(s, p);
            ENode pw = ENode.ofPower();
            fill(pw.slots[0], base);
            fill(pw.slots[1], exp);
            List<ENode> r = new ArrayList<ENode>();
            r.add(pw);
            return r;
        }
        return base;
    }

    private static List<ENode> plainAtom(String s, int[] p) {
        List<ENode> out = new ArrayList<ENode>();
        if (p[0] >= s.length()) return out;
        char c = s.charAt(p[0]);
        if (c == '-' || c == '+' || c == '−') {
            p[0]++;
            out.add(ENode.ofChar(c == '−' ? '-' : c));
            out.addAll(plainAtom(s, p));
            return out;
        }
        if (c == '(') {
            p[0]++;
            out.add(ENode.ofChar('('));
            out.addAll(plainSeq(s, p, true));
            if (p[0] < s.length() && s.charAt(p[0]) == ')') p[0]++;
            out.add(ENode.ofChar(')'));
            return out;
        }
        if (c == '√') {
            p[0]++;
            ENode sq = ENode.ofSqrt();
            fill(sq.slots[0], radicand(s, p));
            out.add(sq);
            return out;
        }
        if (s.startsWith("frac(", p[0])) {
            p[0] += 5;
            out.add(twoSlot(ENode.ofFrac(), s, p));
            return out;
        }
        if (s.startsWith("pow(", p[0])) {
            p[0] += 4;
            out.add(twoSlot(ENode.ofPower(), s, p));
            return out;
        }
        if (s.startsWith("nroot(", p[0])) {
            p[0] += 6;
            out.add(twoSlot(ENode.ofNroot(), s, p));
            return out;
        }
        if (s.startsWith("sqrt(", p[0])) {
            p[0] += 5;
            ENode n = ENode.ofSqrt();
            fill(n.slots[0], plainSeq(s, p, true));
            if (p[0] < s.length() && s.charAt(p[0]) == ')') p[0]++;
            out.add(n);
            return out;
        }
        return plainWord(s, p, out);
    }

    private static List<ENode> plainWord(String s, int[] p, List<ENode> out) {
        char c = s.charAt(p[0]);
        String fn = named(s, p[0]);
        if (fn != null) {
            for (int i = 0; i < fn.length(); i++) out.add(ENode.ofChar(fn.charAt(i)));
            p[0] += fn.length() + 1;
            out.add(ENode.ofChar('('));
            out.addAll(plainSeq(s, p, true));
            if (p[0] < s.length() && s.charAt(p[0]) == ')') p[0]++;
            out.add(ENode.ofChar(')'));
            return out;
        }
        if (isNum(c)) {
            while (p[0] < s.length() && isNum(s.charAt(p[0]))) {
                out.add(ENode.ofChar(s.charAt(p[0])));
                p[0]++;
            }
            return out;
        }
        if (Character.isLetter(c)) {
            out.add(ENode.ofChar(c));
            p[0]++;
        }
        return out;
    }

    private static ENode twoSlot(ENode n, String s, int[] p) {
        fill(n.slots[0], plainSeq(s, p, true));
        if (p[0] < s.length() && s.charAt(p[0]) == ',') p[0]++;
        fill(n.slots[1], plainSeq(s, p, true));
        if (p[0] < s.length() && s.charAt(p[0]) == ')') p[0]++;
        return n;
    }

    private static List<ENode> radicand(String s, int[] p) {
        if (p[0] < s.length() && s.charAt(p[0]) == '(') {
            p[0]++;
            List<ENode> r = plainSeq(s, p, true);
            if (p[0] < s.length() && s.charAt(p[0]) == ')') p[0]++;
            return r;
        }
        return plainAtom(s, p);
    }

    private static void fill(ESeq slot, List<ENode> list) {
        for (int i = 0; i < list.size(); i++) {
            slot.insert(slot.size(), list.get(i));
        }
    }

    private static boolean isNum(char c) {
        return (c >= '0' && c <= '9') || c == '.';
    }

    private static boolean startsAtom(String s, int i) {
        if (i >= s.length()) return false;
        char c = s.charAt(i);
        if (isNum(c) || c == '(' || c == '√') return true;
        return Character.isLetter(c);
    }

    private static String named(String s, int i) {
        for (int k = 0; k < NAMED.length; k++) {
            String f = NAMED[k];
            int end = i + f.length();
            if (end < s.length() && s.startsWith(f, i) && s.charAt(end) == '(') return f;
        }
        return null;
    }

    public static String toPlain(ESeq seq) {
        StringBuilder sb = new StringBuilder();
        plainOut(seq, sb);
        return sb.toString();
    }

    private static void plainOut(ESeq seq, StringBuilder sb) {
        for (int i = 0; i < seq.size(); i++) {
            nodeOut(seq.get(i), sb);
        }
    }

    private static void nodeOut(ENode n, StringBuilder sb) {
        switch (n.kind) {
            case CHAR:
                sb.append(n.ch);
                break;
            case FRAC:
                factorOut(n.slots[0], sb);
                sb.append('/');
                factorOut(n.slots[1], sb);
                break;
            case SQRT:
                sb.append('√');
                factorOut(n.slots[0], sb);
                break;
            case NROOT:
                factorOut(n.slots[1], sb);
                sb.append("^(1/");
                plainOut(n.slots[0], sb);
                sb.append(')');
                break;
            case POWER:
                atomOut(n.slots[0], sb);
                sb.append('^');
                atomOut(n.slots[1], sb);
                break;
            default:
                break;
        }
    }

    private static void atomOut(ESeq seq, StringBuilder sb) {
        String inner = toPlain(seq);
        if (needParen(inner)) {
            sb.append('(');
            sb.append(inner);
            sb.append(')');
        } else {
            sb.append(inner);
        }
    }

    private static void factorOut(ESeq seq, StringBuilder sb) {
        String inner = toPlain(seq);
        if (needParenFactor(inner)) {
            sb.append('(');
            sb.append(inner);
            sb.append(')');
        } else {
            sb.append(inner);
        }
    }

    private static boolean needParenFactor(String s) {
        if (s.length() <= 1) return false;
        if (wrapped(s)) return false;
        int d = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') {
                d++;
            } else if (c == ')') {
                d--;
            } else if (d == 0) {
                if (c == '+' || c == '/' || c == '^' || c == '×' || c == '*' || c == '÷') return true;
                if (c == '-' && i > 0) return true;
            }
        }
        return false;
    }

    private static boolean needParen(String s) {
        if (s.length() <= 1) return false;
        if (wrapped(s)) return false;
        for (int i = 0; i < s.length(); i++) {
            if (!isNum(s.charAt(i))) return true;
        }
        return false;
    }

    private static boolean wrapped(String s) {
        if (s.length() < 2 || s.charAt(0) != '(' || s.charAt(s.length() - 1) != ')') return false;
        int d = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') d++;
            else if (c == ')') {
                d--;
                if (d == 0 && i < s.length() - 1) return false;
            }
        }
        return d == 0;
    }
}
