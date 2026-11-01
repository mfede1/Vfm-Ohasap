package com.lie.acsm.formula;

public class ECursor {

    public ESeq seq;
    public int   pos;

    public ECursor(ESeq seq, int pos) {
        this.seq = seq;
        this.pos = pos;
    }

    public void set(ESeq seq, int pos) {
        this.seq = seq;
        this.pos = pos;
    }

    public void clamp() {
        if (pos < 0) pos = 0;
        if (pos > seq.size()) pos = seq.size();
    }

    public ENode nodeLeft() {
        if (pos > 0) return seq.get(pos - 1);
        return null;
    }

    public ENode nodeRight() {
        if (pos < seq.size()) return seq.get(pos);
        return null;
    }

    public boolean moveLeft(ESeq root) {
        if (pos > 0) {
            ENode n = seq.get(pos - 1);
            if (n.kind != ENode.Kind.CHAR && n.slots != null) {
                ESeq last = n.slots[n.slots.length - 1];
                seq = last;
                pos = last.size();
                return true;
            }
            pos--;
            return true;
        }
        if (seq.owner == null) return false;
        ESeq parent = seq.owner.parentSeq;
        if (parent == null) return false;
        int ownerIdx = parent.nodes.indexOf(seq.owner);
        if (ownerIdx < 0) return false;
        int slotIdx = seq.slotIndex;
        if (slotIdx > 0) {
            ESeq prev = seq.owner.slots[slotIdx - 1];
            seq = prev;
            pos = prev.size();
        } else {
            seq = parent;
            pos = ownerIdx;
        }
        return true;
    }

    public boolean moveRight(ESeq root) {
        if (pos < seq.size()) {
            ENode n = seq.get(pos);
            if (n.kind != ENode.Kind.CHAR && n.slots != null) {
                ESeq first = n.slots[0];
                seq = first;
                pos = 0;
                return true;
            }
            pos++;
            return true;
        }
        if (seq.owner == null) return false;
        ESeq parent = seq.owner.parentSeq;
        if (parent == null) return false;
        int ownerIdx = parent.nodes.indexOf(seq.owner);
        if (ownerIdx < 0) return false;
        int slotIdx = seq.slotIndex;
        if (slotIdx < seq.owner.slots.length - 1) {
            ESeq next = seq.owner.slots[slotIdx + 1];
            seq = next;
            pos = 0;
        } else {
            seq = parent;
            pos = ownerIdx + 1;
        }
        return true;
    }

    public boolean delete(ESeq root) {
        if (pos > 0) {
            ENode n = seq.get(pos - 1);
            if (n.kind != ENode.Kind.CHAR && n.slots != null) {
                ESeq last = n.slots[n.slots.length - 1];
                seq = last;
                pos = last.size();
                return true;
            }
            seq.remove(pos - 1);
            pos--;
            return true;
        }
        if (seq.owner == null) return false;
        ENode owner = seq.owner;
        ESeq parent = owner.parentSeq;
        if (parent == null) return false;
        int ownerIdx = parent.nodes.indexOf(owner);
        if (ownerIdx < 0) return false;
        if (seq.slotIndex > 0) {
            ESeq prev = owner.slots[seq.slotIndex - 1];
            seq = prev;
            pos = prev.size();
            return true;
        }
        parent.nodes.remove(ownerIdx);
        int insertAt = ownerIdx;
        for (int si = 0; si < owner.slots.length; si++) {
            ESeq s = owner.slots[si];
            if (si > 0 && s.size() > 0 && insertAt > ownerIdx) {
                char sep = joinChar(owner.kind);
                if (sep != '\0') {
                    ENode sn = ENode.ofChar(sep);
                    sn.parentSeq = parent;
                    parent.nodes.add(insertAt++, sn);
                }
            }
            for (int i = 0; i < s.size(); i++) {
                ENode child = s.get(i);
                child.parentSeq = parent;
                parent.nodes.add(insertAt++, child);
            }
        }
        seq = parent;
        pos = ownerIdx;
        return true;
    }

    private static char joinChar(ENode.Kind kind) {
        if (kind == ENode.Kind.FRAC) return '/';
        if (kind == ENode.Kind.POWER) return '^';
        if (kind == ENode.Kind.NROOT) return '√';
        return '\0';
    }

    public boolean moveUp(ESeq root) {
        ESeq s = seq;
        while (s != null && s.owner != null) {
            ENode ow = s.owner;
            int si = s.slotIndex;
            if (ow.kind == ENode.Kind.FRAC && si == 1) return enter(ow.slots[0]);
            if (ow.kind == ENode.Kind.POWER && si == 0) return enter(ow.slots[1]);
            if (ow.kind == ENode.Kind.NROOT && si == 1) return enter(ow.slots[0]);
            s = ow.parentSeq;
        }
        return false;
    }

    public boolean moveDown(ESeq root) {
        ESeq s = seq;
        while (s != null && s.owner != null) {
            ENode ow = s.owner;
            int si = s.slotIndex;
            if (ow.kind == ENode.Kind.FRAC && si == 0) return enter(ow.slots[1]);
            if (ow.kind == ENode.Kind.POWER && si == 1) return enter(ow.slots[0]);
            if (ow.kind == ENode.Kind.NROOT && si == 0) return enter(ow.slots[1]);
            s = ow.parentSeq;
        }
        return false;
    }

    private boolean enter(ESeq target) {
        seq = target;
        pos = target.size();
        return true;
    }

    public void moveHome() {
        pos = 0;
    }

    public void moveEnd() {
        pos = seq.size();
    }

    public void insert(ENode n) {
        if (n.kind != ENode.Kind.CHAR && n.slots != null) {
            n.parentSeq = seq;
            seq.insert(pos, n);
            seq = n.slots[0];
            pos = 0;
        } else {
            n.parentSeq = seq;
            seq.insert(pos, n);
            pos++;
        }
    }

    public void insertFrac() {
        ENode frac = ENode.ofFrac();
        frac.parentSeq = seq;
        seq.insert(pos, frac);
        seq = frac.slots[0];
        pos = 0;
    }
}
