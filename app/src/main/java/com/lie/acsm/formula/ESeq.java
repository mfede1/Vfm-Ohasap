package com.lie.acsm.formula;

import java.util.ArrayList;
import java.util.List;

public class ESeq {

    public final ENode owner;
    public final int slotIndex;
    final List<ENode> nodes = new ArrayList<>();

    ESeq(ENode owner, int slotIndex) {
        this.owner = owner;
        this.slotIndex = slotIndex;
    }

    public int size() {
        return nodes.size();
    }

    public ENode get(int i) {
        return nodes.get(i);
    }

    public void insert(int pos, ENode n) {
        n.parentSeq = this;
        nodes.add(pos, n);
    }

    public void remove(int pos) {
        nodes.remove(pos);
    }
}
