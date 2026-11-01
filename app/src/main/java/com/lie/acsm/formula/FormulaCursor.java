package com.lie.acsm.formula;

import java.util.ArrayList;
import java.util.List;

public class FormulaCursor {

    public static class Slot {
        public final FormulaNode node;
        public final int childIndex;
        public final List<Integer> path;

        public Slot(FormulaNode node, int childIndex, List<Integer> path) {
            this.node = node;
            this.childIndex = childIndex;
            this.path = new ArrayList<>(path);
        }

        public boolean isRoot() {
            return path.isEmpty();
        }

    }

    public FormulaNode root;
    public int slotIndex;
    public int textOffset;

    public FormulaCursor(FormulaNode root) {
        this.root = root;
        this.slotIndex = 0;
        this.textOffset = 0;
    }

    public List<Slot> getSlots() {
        List<Slot> slots = new ArrayList<>();
        collectSlots(root, new ArrayList<Integer>(), slots);
        return slots;
    }

    private void collectSlots(FormulaNode node, List<Integer> path, List<Slot> slots) {
        if (node == null) return;
        switch (node.type) {
            case TEXT:
                slots.add(new Slot(node, 0, path));
                break;
            case FRACTION:
                slots.add(new Slot(node, 0, extendPath(path, 0)));
                slots.add(new Slot(node, 1, extendPath(path, 1)));
                break;
            case SQRT:
                slots.add(new Slot(node, 0, extendPath(path, 0)));
                break;
            case NROOT:
                slots.add(new Slot(node, 0, extendPath(path, 0)));
                slots.add(new Slot(node, 1, extendPath(path, 1)));
                break;
            case POWER:
                slots.add(new Slot(node, 0, extendPath(path, 0)));
                slots.add(new Slot(node, 1, extendPath(path, 1)));
                break;
            case SEQUENCE:
                for (int i = 0; i < node.children.size(); i++) {
                    collectSlots(node.children.get(i), extendPath(path, i), slots);
                }
                break;
        }
    }

    private List<Integer> extendPath(List<Integer> base, int idx) {
        List<Integer> p = new ArrayList<>(base);
        p.add(idx);
        return p;
    }

    public FormulaNode resolvePath(List<Integer> path) {
        FormulaNode current = root;
        for (int i = 1; i < path.size(); i++) {
            if (current == null || path.get(i) >= current.children.size()) return null;
            current = current.children.get(path.get(i));
        }
        return current;
    }

    public Slot getCurrentSlot() {
        List<Slot> slots = getSlots();
        if (slotIndex < 0) slotIndex = 0;
        if (slotIndex >= slots.size()) slotIndex = slots.size() - 1;
        if (slots.isEmpty()) return null;
        return slots.get(slotIndex);
    }

    public boolean moveLeft() {
        if (slotIndex > 0) {
            slotIndex--;
            textOffset = 0;
            return true;
        }
        return false;
    }

    public boolean moveRight() {
        List<Slot> slots = getSlots();
        if (slotIndex < slots.size() - 1) {
            slotIndex++;
            textOffset = 0;
            return true;
        }
        return false;
    }

    public void moveToSlot(int idx) {
        List<Slot> slots = getSlots();
        slotIndex = Math.max(0, Math.min(idx, slots.size() - 1));
        textOffset = 0;
    }

    public String slotText() {
        Slot slot = getCurrentSlot();
        if (slot == null || slot.node.text == null) return "";
        return slot.node.text;
    }

    public void insertTextAtCursor(String text) {
        Slot slot = getCurrentSlot();
        if (slot == null) return;
        String current = slot.node.text != null ? slot.node.text : "";
        int off = Math.min(textOffset, current.length());
        String newText = current.substring(0, off) + text + current.substring(off);
        replaceSlot(slot, FormulaNode.text(newText));
        textOffset = off + text.length();
    }

    public void deleteCharBeforeCursor() {
        Slot slot = getCurrentSlot();
        if (slot == null) return;
        String current = slot.node.text != null ? slot.node.text : "";
        if (current.isEmpty() || (textOffset == 0 && slotIndex == 0)) {
            if (slotIndex > 0) {
                slotIndex--;
                deleteCharBeforeCursor();
            }
            return;
        }
        int off = Math.max(0, textOffset - 1);
        String newText = current.substring(0, off) + current.substring(textOffset);
        textOffset = off;
        replaceSlot(slot, FormulaNode.text(newText));
    }

    public void deleteCharAfterCursor() {
        Slot slot = getCurrentSlot();
        if (slot == null) return;
        String current = slot.node.text != null ? slot.node.text : "";
        if (textOffset >= current.length()) {
            List<Slot> slots = getSlots();
            if (slotIndex < slots.size() - 1) {
                Slot nextSlot = slots.get(slotIndex + 1);
                FormulaNode nextNode = FormulaNode.text("");
                replaceSlot(nextSlot, nextNode);
                if (nextNode.text.isEmpty() && nextSlot.node.children.isEmpty()) {
                    removeEmptySlot(slotIndex + 1);
                }
            }
            return;
        }
        int off = textOffset;
        String newText = current.substring(0, off) + current.substring(off + 1);
        replaceSlot(slot, FormulaNode.text(newText));
    }

    public void insertFrac() {
        Slot slot = getCurrentSlot();
        String sel = slot != null && slot.node.text != null ? slot.node.text : "";
        FormulaNode num = sel.isEmpty() ? FormulaNode.text("") : FormulaNode.text(sel);
        FormulaNode den = FormulaNode.text("");
        FormulaNode frac = FormulaNode.fraction(num, den);
        if (slot != null && !slot.isRoot()) {
            replaceSlot(slot, frac);
            List<Slot> slots = getSlots();
            for (int i = 0; i < slots.size(); i++) {
                Slot s = slots.get(i);
                if (s.path.size() == slot.path.size() + 1 &&
                        s.path.subList(0, slot.path.size()).equals(slot.path) &&
                        s.childIndex == 1) {
                    slotIndex = i;
                    textOffset = 0;
                    return;
                }
            }
        } else {
            root = frac;
            List<Slot> slots = getSlots();
            for (int i = 0; i < slots.size(); i++) {
                Slot s = slots.get(i);
                if (s.path.size() == 1 && s.childIndex == 1) {
                    slotIndex = i;
                    textOffset = 0;
                    return;
                }
            }
        }
        textOffset = 0;
    }

    public void insertSqrt() {
        Slot slot = getCurrentSlot();
        String sel = slot != null && slot.node.text != null ? slot.node.text : "";
        FormulaNode arg = sel.isEmpty() ? FormulaNode.text("") : FormulaNode.text(sel);
        FormulaNode sqrt = FormulaNode.sqrt(arg);
        if (slot != null && !slot.isRoot()) {
            replaceSlot(slot, sqrt);
        } else {
            root = sqrt;
        }
        List<Slot> slots = getSlots();
        for (int i = 0; i < slots.size(); i++) {
            Slot s = slots.get(i);
            if (s.path.size() == (slot != null && !slot.isRoot() ? slot.path.size() : 1) + 1 &&
                    (slot != null && !slot.isRoot() ? s.path.subList(0, slot.path.size()).equals(slot.path) : true) &&
                    s.childIndex == 0) {
                slotIndex = i;
                textOffset = 0;
                return;
            }
        }
        slotIndex = slots.size() - 1;
        textOffset = 0;
    }

    public void insertNroot() {
        Slot slot = getCurrentSlot();
        String sel = slot != null && slot.node.text != null ? slot.node.text : "";
        FormulaNode idx = FormulaNode.text("2");
        FormulaNode arg = sel.isEmpty() ? FormulaNode.text("") : FormulaNode.text(sel);
        FormulaNode nroot = FormulaNode.nroot(idx, arg);
        if (slot != null && !slot.isRoot()) {
            replaceSlot(slot, nroot);
        } else {
            root = nroot;
        }
        List<Slot> slots = getSlots();
        for (int i = 0; i < slots.size(); i++) {
            Slot s = slots.get(i);
            if (s.path.size() == (slot != null && !slot.isRoot() ? slot.path.size() : 1) + 1 &&
                    (slot != null && !slot.isRoot() ? s.path.subList(0, slot.path.size()).equals(slot.path) : true) &&
                    s.childIndex == 1) {
                slotIndex = i;
                textOffset = 0;
                return;
            }
        }
        slotIndex = 0;
        textOffset = 0;
    }

    public void insertPower() {
        Slot slot = getCurrentSlot();
        String sel = slot != null && slot.node.text != null ? slot.node.text : "";
        FormulaNode base = sel.isEmpty() ? FormulaNode.text("") : FormulaNode.text(sel);
        FormulaNode exp = FormulaNode.text("2");
        FormulaNode power = FormulaNode.power(base, exp);
        if (slot != null && !slot.isRoot()) {
            replaceSlot(slot, power);
        } else {
            root = power;
        }
        List<Slot> slots = getSlots();
        for (int i = 0; i < slots.size(); i++) {
            Slot s = slots.get(i);
            if (s.path.size() == (slot != null && !slot.isRoot() ? slot.path.size() : 1) + 1 &&
                    (slot != null && !slot.isRoot() ? s.path.subList(0, slot.path.size()).equals(slot.path) : true) &&
                    s.childIndex == 1) {
                slotIndex = i;
                textOffset = 0;
                return;
            }
        }
        slotIndex = slots.size() - 1;
        textOffset = 0;
    }

    private void replaceSlot(Slot slot, FormulaNode newNode) {
        if (slot.isRoot()) {
            root = newNode;
            return;
        }
        FormulaNode parent = resolvePath(slot.path.subList(0, slot.path.size() - 1));
        int idx = slot.path.get(slot.path.size() - 1);
        parent.children.set(idx, newNode);
    }

    private void removeEmptySlot(int idx) {
        List<Slot> slots = getSlots();
        if (idx < 0 || idx >= slots.size()) return;
        Slot slot = slots.get(idx);
        if (slot.isRoot()) return;
        FormulaNode parent = resolvePath(slot.path.subList(0, slot.path.size() - 1));
        int childIdx = slot.path.get(slot.path.size() - 1);
        FormulaNode childNode = slot.node;
        if (childNode.type == FormulaNode.Type.TEXT &&
                (childNode.text == null || childNode.text.isEmpty())) {
            FormulaNode replacement = FormulaNode.text("");
            parent.children.set(childIdx, replacement);
        }
    }

    public String toCode() {
        return nodeToCode(root);
    }

    private String nodeToCode(FormulaNode node) {
        if (node == null) return "";
        switch (node.type) {
            case TEXT:
                return node.text != null ? node.text : "";
            case FRACTION:
                return "frac(" + nodeToCode(node.children.get(0)) + "," +
                        nodeToCode(node.children.get(1)) + ")";
            case SQRT:
                return "sqrt(" + nodeToCode(node.children.get(0)) + ")";
            case NROOT:
                return "nroot(" + nodeToCode(node.children.get(0)) + "," +
                        nodeToCode(node.children.get(1)) + ")";
            case POWER:
                return "pow(" + nodeToCode(node.children.get(0)) + "," +
                        nodeToCode(node.children.get(1)) + ")";
            case SEQUENCE:
                StringBuilder sb = new StringBuilder();
                for (FormulaNode child : node.children) {
                    sb.append(nodeToCode(child));
                }
                return sb.toString();
            default:
                return "";
        }
    }
}
