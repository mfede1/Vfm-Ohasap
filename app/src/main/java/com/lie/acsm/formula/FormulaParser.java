package com.lie.acsm.formula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FormulaParser {

    private String src;
    private int pos;

    public FormulaNode parse(String input) {
        this.src = (input == null) ? "" : input;
        this.pos = 0;
        List<FormulaNode> nodes = parseSequence(false, false);
        return FormulaNode.sequence(nodes.isEmpty()
                ? Collections.singletonList(FormulaNode.text(""))
                : nodes);
    }

    private List<FormulaNode> parseSequence(boolean stopAtComma, boolean stopAtParen) {
        List<FormulaNode> nodes = new ArrayList<>();
        StringBuilder textBuf = new StringBuilder();

        while (pos < src.length()) {
            char c = src.charAt(pos);

            if (stopAtParen && c == ')') break;
            if (stopAtComma && c == ',') break;

            String func = peekFunction();
            if (func != null) {
                flushText(textBuf, nodes);
                pos += func.length();
                skipChar('(');
                FormulaNode node = parseFunction(func);
                node = maybeChainPower(node, nodes);
                nodes.add(node);
                continue;
            }

            if (c == '\u221A') {
                flushText(textBuf, nodes);
                pos++;
                FormulaNode arg;
                if (pos < src.length() && src.charAt(pos) == '(') {
                    pos++;
                    arg = FormulaNode.sequence(parseSequence(false, true));
                    skipChar(')');
                } else {
                    arg = FormulaNode.text(readSimpleToken());
                }
                FormulaNode node = FormulaNode.sqrt(arg);
                node = maybeChainPower(node, nodes);
                nodes.add(node);
                continue;
            }

            if (c == '^') {
                flushText(textBuf, nodes);
                pos++;
                FormulaNode base = popLastOrEmpty(nodes);
                FormulaNode exp = parsePowerArg();
                nodes.add(FormulaNode.power(base, exp));
                continue;
            }

            if (c == '/' && !stopAtParen && !stopAtComma) {
                String numText = textBuf.toString().trim();
                textBuf.setLength(0);
                FormulaNode num = numText.isEmpty()
                        ? popLastOrEmpty(nodes)
                        : FormulaNode.text(numText);
                pos++;
                FormulaNode den = parseDenominator();
                nodes.add(FormulaNode.fraction(num, den));
                continue;
            }

            textBuf.append(c);
            pos++;
        }

        flushText(textBuf, nodes);
        return nodes;
    }

    private FormulaNode parseFunction(String func) {
        switch (func) {
            case "frac": {
                FormulaNode num = FormulaNode.sequence(parseSequence(true, false));
                skipChar(',');
                FormulaNode den = FormulaNode.sequence(parseSequence(false, true));
                skipChar(')');
                return FormulaNode.fraction(num, den);
            }
            case "sqrt": {
                FormulaNode arg = FormulaNode.sequence(parseSequence(false, true));
                skipChar(')');
                return FormulaNode.sqrt(arg);
            }
            case "nroot": {
                FormulaNode idx = FormulaNode.sequence(parseSequence(true, false));
                skipChar(',');
                FormulaNode arg = FormulaNode.sequence(parseSequence(false, true));
                skipChar(')');
                return FormulaNode.nroot(idx, arg);
            }
            case "pow": {
                FormulaNode base = FormulaNode.sequence(parseSequence(true, false));
                skipChar(',');
                FormulaNode exp = FormulaNode.sequence(parseSequence(false, true));
                skipChar(')');
                return FormulaNode.power(base, exp);
            }
            default: {
                StringBuilder sb = new StringBuilder(func).append('(');
                List<FormulaNode> inner = parseSequence(false, true);
                skipChar(')');
                sb.append(flattenToString(inner)).append(')');
                return FormulaNode.text(sb.toString());
            }
        }
    }

    private String peekFunction() {
        String[] funcs = {"frac", "sqrt", "nroot", "pow",
                          "sin", "cos", "tan", "asin", "acos", "atan",
                          "ln", "log"};
        for (String f : funcs) {
            int end = pos + f.length();
            if (end < src.length() && src.substring(pos, end).equals(f)
                    && src.charAt(end) == '(') {
                return f;
            }
        }
        return null;
    }

    private FormulaNode parsePowerArg() {
        if (pos < src.length() && src.charAt(pos) == '{') {
            pos++;
            List<FormulaNode> nodes = parseSequence(false, false);
            skipChar('}');
            return FormulaNode.sequence(nodes);
        }
        return FormulaNode.text(readSimpleToken());
    }

    private FormulaNode parseDenominator() {
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c == '+' || c == '-' || c == ')') break;
            sb.append(c);
            pos++;
        }
        return sb.length() == 0 ? FormulaNode.text("?") : FormulaNode.text(sb.toString().trim());
    }

    private String readSimpleToken() {
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (Character.isLetterOrDigit(c) || c == '.' || c == '_') {
                sb.append(c);
                pos++;
            } else break;
        }
        return sb.length() > 0 ? sb.toString() : (pos < src.length() ? String.valueOf(src.charAt(pos++)) : "");
    }

    private FormulaNode maybeChainPower(FormulaNode node, List<FormulaNode> nodes) {
        if (pos < src.length() && src.charAt(pos) == '^') {
            pos++;
            FormulaNode exp = parsePowerArg();
            return FormulaNode.power(node, exp);
        }
        return node;
    }

    private static void flushText(StringBuilder buf, List<FormulaNode> nodes) {
        if (buf.length() > 0) {
            nodes.add(FormulaNode.text(buf.toString()));
            buf.setLength(0);
        }
    }

    private static FormulaNode popLastOrEmpty(List<FormulaNode> nodes) {
        if (nodes.isEmpty()) return FormulaNode.text("");
        return nodes.remove(nodes.size() - 1);
    }

    private void skipChar(char ch) {
        if (pos < src.length() && src.charAt(pos) == ch) pos++;
    }

    private static String flattenToString(List<FormulaNode> nodes) {
        StringBuilder sb = new StringBuilder();
        for (FormulaNode n : nodes) {
            if (n.type == FormulaNode.Type.TEXT) sb.append(n.text);
        }
        return sb.toString();
    }
}
