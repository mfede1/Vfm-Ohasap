package com.lie.acsm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class PhysObj {
    int type;
    float x, y, w, h, vx, vy;
    double forceX, forceY;
    double appliedForceX = 0, appliedForceY = 0;
    float displayForceX = 0, displayForceY = 0;
    float constraintForceX = 0, constraintForceY = 0;
    float supportForceX = 0, supportForceY = 0;
    float rodSupportForceX = 0, rodSupportForceY = 0;
    float frictionForceX = 0, frictionForceY = 0;
    float reactionForceX = 0, reactionForceY = 0;
    float rotation = 0;
    float mass = 1f, friction = 0.3f, restitution = 0.3f;
    boolean fixed = false;
    boolean showState = false;
    boolean showForces = false;
    boolean showTrajectory = false;
    float naturalLen = 80, springK = 500f;
    boolean springBoundRight = false;
    float ropeLen = 100;
    boolean ropeBoundRight = false;
    float arcRadius = 180;
    PhysObj connectedObjA = null;
    PhysObj connectedObjB = null;
    boolean connectedToCeiling = false;
    int ceilingSegmentId = -1;
    float ceilingAnchorX = 0f;
    boolean connectedToGround = false;
    float groundAnchorX = 0;
    float realWidth = 0.6f;
    float realHeight = 0.4f;
    float realRadius = 0.2f;
    float realLength = 1.0f;
    float initialX = 0, initialY = 0;
    double initialVx = 0, initialVy = 0;
    boolean isHorizontal = true;
    float deformation = 0f;
    boolean springAtRest = false;
    float forceDuration = -1f;
    float forceTimer = 0f;
    ArrayList<float[]> cutVertices = new ArrayList<>();
    double accelX = 0, accelY = 0;
    float prevVx = 0, prevVy = 0;
    float prevX = 0, prevY = 0;
    double physX = 0, physY = 0, physVx = 0, physVy = 0;
    double prevPhysVx = 0, prevPhysVy = 0;
    double stepAccelX = 0, stepAccelY = 0;
    double stepOtherForceX = 0, stepOtherForceY = 0;
    boolean contactGround = false;
    boolean contactWallLeft = false;
    boolean contactWallRight = false;
    boolean contactRamp = false;
    boolean prevContactRamp = false;
    PhysObj rampContactObj = null;
    PhysObj prevRampContactObj = null;
    float prevRampNx = 0, prevRampNy = -1;
    float rampNx = 0, rampNy = -1;
    float rampSurfAngle = 0;
    boolean cutDecaying = false;
    float cutDecayTimer = 0f;
    float ropeElasticLimit = 200f;
    boolean isCutHalf = false;
    float cutHalfAnchorX, cutHalfAnchorY;
    float cutHalfFreeX, cutHalfFreeY;
    PhysObj cutHalfFollowObj = null;
    float cutHalfLen = 0;
    float density = 500.0f;
    float baseArea = 0.06f;
    boolean connectedToWallLeft = false;
    boolean connectedToWallRight = false;
    float wallAnchorY = 0f;
    float freeDirX = 0, freeDirY = 0;
    float freeEndX = 0f, freeEndY = 0f;
    float freeEndVx = 0, freeEndVy = 0;
    float freeEndInitVx = 0, freeEndInitVy = 0;
    boolean freeEndHasInitVel = false;
    float freeAppliedForceX = 0, freeAppliedForceY = 0;
    float rodLen = 100;
    boolean remoteLock = false;
    float chainTension = 0f;
    boolean chainSolved = false;
    int chainGroup = -1;
    PhysObj rodRing = null;
    PhysObj ringHostRod = null;
    float ringParam = 0.5f;
    ArrayList<float[]> trajectory = new ArrayList<>();
    float pressure = 0f;
    float containerWallThickness = 0.02f;
    float containerWaterLevel = 0f;
    boolean isWaterContainer = false;
    WaterSim water = null;
    float waterPrevSubA = 0f;
    float waterSubArea = 0f;
    ArrayList<Float> velocityHistory = new ArrayList<>();
    ArrayList<Float> positionHistory = new ArrayList<>();
    ArrayList<Float> accelHistory = new ArrayList<>();
    List<PhysObj> toRemove = new ArrayList<>();
    PhysObj pulleyBoundRamp = null;
    boolean ropeWrapped = false;
    boolean ropeTaut = false;
    ArrayList<float[]> ropeWrapPoints = new ArrayList<>();
    ArrayList<float[]> ropeParticles = new ArrayList<>();
    ArrayList<Float> ropeSegmentRestLengths = new ArrayList<>();
    boolean ropeParticlesInit = false;
    PhysObj ropeWrapPulley = null;
    float ropeWrapDir = 1f;
    boolean angleFixed = false;
    float fixedAngle = 0f;
    float arcAngle = (float) Math.PI;
    boolean pipeMode = false;
    float pipeDiameter = 0.3f;
    int symV0Level = 2;
    int symForceLevel = 2;
    int symOrdinal = 0;
    boolean symV0Set = false;
    HashMap<String, Boolean> symKnownMap = new HashMap<String, Boolean>();
    HashMap<String, String> symCustomMap = new HashMap<String, String>();
    HashMap<String, SymExpr> symFrozenExpr = new HashMap<String, SymExpr>();
    HashMap<String, Float> symFrozenTime = new HashMap<String, Float>();
    float symStateTime = 0f;
    String symStateKey = "";

    LogicCondition forceCondition = null;
    LogicCondition velocityCondition = null;
    LogicCondition bindACondition = null;
    LogicCondition bindBCondition = null;
    LogicCondition wrapCondition = null;
    LogicCondition fixedCondition = null;
    LogicCondition angleFixedCondition = null;

    static Paint mp(int color, Paint.Style style, float sw) {
        Paint p = new Paint();
        p.setColor(color);
        p.setStyle(style);
        p.setStrokeWidth(sw);
        p.setAntiAlias(true);
        return p;
    }
}

class CeilingSegment {
    float x1, y1, x2, y2;
    float length;
}

class SimulationState {
    float time;
    HashMap<PhysObj, ObjState> states = new HashMap<>();
}

class ObjState {
    float x, y, vx, vy, rotation;
    ObjState(float x, float y, float vx, float vy, float rotation) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.rotation = rotation;
    }
}

class LogicCondition {
    static final int TYPE_AFTER_TIME = 1;
    static final int TYPE_CONTACT_OBJ = 2;
    static final int TYPE_TIMER_REACH = 3;
    static final int TYPE_OBJ_NOT_EXIST = 4;
    static final int TYPE_CONTACT_OBJ_DURATION = 5;
    static final int TYPE_CONTACT_OBJ_TOTAL = 6;
    static final int TYPE_CONTACT_OBJ_AFTER = 7;
    static final int TYPE_COMPOUND = 8;
    static final int TYPE_COMPOUND_OR = 9;
    int type = TYPE_AFTER_TIME;
    float timeParam = 1f;
    int requiredCount = -1;
    PhysObj targetObj = null;
    ArrayList<LogicCondition> subConditions = new ArrayList<>();
    float totalTimer = 0f;
    boolean afterContactStarted = false;
    boolean triggered = false;
    int contactNth = 1;
    int contactCount = 0;
    float startElapsedTime = -1f;
    float totalContactElapsedStart = -1f;

    void reset() {
        totalTimer = 0f; afterContactStarted = false; triggered = false;
        contactCount = 0; startElapsedTime = -1f; totalContactElapsedStart = -1f;
        for (LogicCondition s : subConditions) s.reset();
    }

    boolean containsTouchType() {
        switch (type) {
            case TYPE_CONTACT_OBJ:
            case TYPE_CONTACT_OBJ_DURATION:
            case TYPE_CONTACT_OBJ_TOTAL:
            case TYPE_CONTACT_OBJ_AFTER:
                return true;
            case TYPE_COMPOUND:
            case TYPE_COMPOUND_OR:
                for (LogicCondition s : subConditions) {
                    if (s.containsTouchType()) return true;
                }
                return false;
            default:
                return false;
        }
    }

    boolean evaluate(float dt, PhysObj owner, MainActivity act) {
        if (triggered) return true;
        switch (type) {
            case TYPE_AFTER_TIME:
                if (startElapsedTime < 0) startElapsedTime = act.elapsedTime;
                if (act.elapsedTime - startElapsedTime >= timeParam) { triggered = true; return true; }
                return false;
            case TYPE_CONTACT_OBJ:
                if (targetObj == null || !act.objects.contains(targetObj)) return false;
                if (act.isInContact(owner, targetObj)) {
                    triggered = true;
                    act.conditionHighlightObj = null;
                    return true;
                }
                return false;
            case TYPE_TIMER_REACH:
                if (act.elapsedTime >= timeParam) { triggered = true; return true; }
                return false;
            case TYPE_OBJ_NOT_EXIST:
                if (targetObj == null || !act.objects.contains(targetObj)) { triggered = true; return true; }
                return false;
            case TYPE_CONTACT_OBJ_DURATION:
                if (targetObj == null || !act.objects.contains(targetObj)) return false;
                if (act.isInContact(owner, targetObj)) {
                    if (startElapsedTime < 0) startElapsedTime = act.elapsedTime;
                    if (act.elapsedTime - startElapsedTime >= timeParam) {
                        triggered = true;
                        act.conditionHighlightObj = null;
                        return true;
                    }
                } else {
                    startElapsedTime = -1f;
                }
                return false;
            case TYPE_CONTACT_OBJ_TOTAL:
                if (targetObj == null || !act.objects.contains(targetObj)) return false;
                if (act.isInContact(owner, targetObj)) {
                    if (totalContactElapsedStart < 0) totalContactElapsedStart = act.elapsedTime;
                } else {
                    if (totalContactElapsedStart >= 0) {
                        totalTimer += act.elapsedTime - totalContactElapsedStart;
                        totalContactElapsedStart = -1f;
                    }
                }
                float totalNow = totalTimer + (totalContactElapsedStart >= 0 ? act.elapsedTime - totalContactElapsedStart : 0);
                if (totalNow >= timeParam) {
                    triggered = true;
                    act.conditionHighlightObj = null;
                    return true;
                }
                return false;
case TYPE_CONTACT_OBJ_AFTER: {
    if (targetObj == null || !act.objects.contains(targetObj)) return false;
    boolean nowContact = act.isInContact(owner, targetObj);
    
    if (!afterContactStarted && nowContact) {
        afterContactStarted = true;
        contactCount++;
    }
    else if (afterContactStarted && !nowContact) {
        afterContactStarted = false;
        if (contactNth == -1 || contactCount == contactNth) {
            startElapsedTime = act.elapsedTime;
        }
    }
    
    if (startElapsedTime >= 0) {
        float elapsed = act.elapsedTime - startElapsedTime;
        if (elapsed >= timeParam) {
            triggered = true;
            act.conditionHighlightObj = null;
            return true;
        }
    }
    return false;
}
            case TYPE_COMPOUND:
            case TYPE_COMPOUND_OR: {
                if (subConditions.isEmpty()) return false;
                int need = requiredCount;
                if (need < 1) need = (type == TYPE_COMPOUND_OR) ? 1 : subConditions.size();
                if (need > subConditions.size()) need = subConditions.size();
                int activeNow = 0;
                for (LogicCondition s : subConditions) {
                    if (s.evaluate(dt, owner, act)) activeNow++;
                }
                if (activeNow >= need) {
                    triggered = true;
                    if (containsTouchType()) act.conditionHighlightObj = null;
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    String describe() {
        switch (type) {
            case TYPE_AFTER_TIME: return "经过" + timeParam + "s";
            case TYPE_CONTACT_OBJ: return "接触物体";
            case TYPE_TIMER_REACH: return "至" + timeParam + "s";
            case TYPE_OBJ_NOT_EXIST: return "物体不存在";
            case TYPE_CONTACT_OBJ_DURATION: return "连续接触物体" + timeParam + "s";
            case TYPE_CONTACT_OBJ_TOTAL: return "接触物体总计" + timeParam + "s";
            case TYPE_CONTACT_OBJ_AFTER:
                return "第" + (contactNth == -1 ? "最后" : String.valueOf(contactNth)) + "次接触物体" + timeParam + "s后";
            case TYPE_COMPOUND:
            case TYPE_COMPOUND_OR: {
                if (subConditions.isEmpty()) return "复合逻辑(空)";
                int need = requiredCount;
                if (need < 1) need = (type == TYPE_COMPOUND_OR) ? 1 : subConditions.size();
                StringBuilder sb = new StringBuilder("复合逻辑(需" + need + "个)[");
                for (int i = 0; i < subConditions.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(subConditions.get(i).describe());
                }
                return sb.append("]").toString();
            }
        }
        return "?";
    }
}

class SymVar {
    static final String[] LEVEL_NAMES = {"极小", "较小", "一般", "较大", "极大"};
    static final float[] LEVEL_FRAC = {0.08f, 0.28f, 0.5f, 0.72f, 0.92f};
    String key, sym, label, unit;
    float value, minV, maxV;
    int level;
    boolean known;
    SymVar(String key, String sym, String label, String unit,
           float value, float minV, float maxV, boolean known) {
        this.key = key;
        this.sym = sym;
        this.label = label;
        this.unit = unit;
        this.value = value;
        this.minV = minV;
        this.maxV = maxV;
        this.known = known;
        this.level = nearestLevel(value, minV, maxV);
    }

    static int nearestLevel(float value, float minV, float maxV) {
        float span = maxV - minV;
        if (Math.abs(span) < 1e-6f) return 2;
        float f = (value - minV) / span;
        int best = 2;
        float bestD = Float.MAX_VALUE;
        for (int i = 0; i < LEVEL_FRAC.length; i++) {
            float d = Math.abs(LEVEL_FRAC[i] - f);
            if (d < bestD) {
                bestD = d;
                best = i;
            }
        }
        return best;
    }

    static String levelName(int level) {
        if (level < 0) level = 0;
        if (level >= LEVEL_NAMES.length) level = LEVEL_NAMES.length - 1;
        return LEVEL_NAMES[level];
    }

    float valueForLevel(int lv) {
        if (lv < 0) lv = 0;
        if (lv >= LEVEL_FRAC.length) lv = LEVEL_FRAC.length - 1;
        return minV + (maxV - minV) * LEVEL_FRAC[lv];
    }

    String levelName() {
        return levelName(level);
    }
}

class SymExpr {
    String text;
    String tex;
    double value;
    int prec;
    SymExpr(String text, double value, int prec) {
        this(text, text, value, prec);
    }
    SymExpr(String text, String tex, double value, int prec) {
        this.text = text;
        this.tex = tex;
        this.value = value;
        this.prec = prec;
    }
}

class Ball {
    static PhysObj createObjAt(float x, float y, float pixelsPerMeter, float groundLength) {
        PhysObj o = new PhysObj();
        o.type = MainActivity.TYPE_BALL;
        o.x = o.initialX = x;
        o.y = o.initialY = y;
        o.mass = 1.0f;
        o.friction = o.restitution = 0.3f;
        o.realRadius = 0.2f * (groundLength / 10f);
        o.w = o.h = o.realRadius * pixelsPerMeter * 2;
        o.rotation = 0;
        o.initialVx = o.initialVy = 0;
        return o;
    }

    static void draw(Canvas c, PhysObj o, Paint bP, Paint fP) {
        Paint whiteFill = new Paint();
        whiteFill.setColor(Color.WHITE);
        whiteFill.setStyle(Paint.Style.FILL);
        whiteFill.setAntiAlias(true);
        if (!o.cutVertices.isEmpty()) {
            Path cp = new Path();
            boolean first = true;
            for (float[] v : o.cutVertices) {
                if (first) { cp.moveTo(v[0], v[1]); first = false; }
                else { cp.lineTo(v[0], v[1]); }
            }
            cp.close();
            c.drawPath(cp, whiteFill);
            c.drawPath(cp, bP);
            return;
        }
        float radius = o.w / 2;
        c.drawCircle(0, 0, radius, whiteFill);
        c.drawCircle(0, 0, radius, bP);
        Paint highlight = PhysObj.mp(0x60FFFFFF, Paint.Style.FILL, 0f);
        c.drawCircle(-radius / 3, -radius / 3, radius / 3, highlight);
    }

    static void drawMini(Canvas c, float cx, float cy, Paint iconB, Paint iconF) {
        c.drawCircle(cx, cy, 12, iconF);
        c.drawCircle(cx, cy, 12, iconB);
        Paint highlight = PhysObj.mp(0x60FFFFFF, Paint.Style.FILL, 0f);
        c.drawCircle(cx - 4, cy - 4, 4, highlight);
    }
}

class Block {
    static PhysObj createObjAt(float x, float y, float pixelsPerMeter, float groundLength) {
        PhysObj o = new PhysObj();
        o.type = MainActivity.TYPE_BLOCK;
        o.x = o.initialX = x;
        o.y = o.initialY = y;
        o.mass = 1.0f;
        o.friction = o.restitution = 0.3f;
        o.realWidth = 0.6f * (groundLength / 10f);
        o.realHeight = 0.4f * (groundLength / 10f);
        o.w = o.realWidth * pixelsPerMeter;
        o.h = o.realHeight * pixelsPerMeter;
        o.rotation = 0;
        o.initialVx = o.initialVy = 0;
        return o;
    }

    static void draw(Canvas c, PhysObj o, Paint bP, Paint fP) {
        if (o.cutVertices.isEmpty()) {
            float l = -o.w / 2, t = -o.h / 2, r = o.w / 2, b = o.h / 2;
            c.drawRect(l, t, r, b, fP);
            c.drawRect(l, t, r, b, bP);
            Paint wp = PhysObj.mp(Color.WHITE, Paint.Style.STROKE, 1f);
            for (float yLine = t + 8; yLine < b; yLine += 8) {
                c.drawLine(l + 2, yLine, r - 2, yLine, wp);
            }
        } else {
            Path p = new Path();
            boolean first = true;
            for (float[] v : o.cutVertices) {
                if (first) {
                    p.moveTo(v[0], v[1]);
                    first = false;
                } else {
                    p.lineTo(v[0], v[1]);
                }
            }
            p.close();
            c.drawPath(p, fP);
            c.drawPath(p, bP);
        }
    }

    static void drawMini(Canvas c, float cx, float cy, Paint iconB, Paint iconF) {
        c.drawRect(cx - 20, cy - 12, cx + 20, cy + 12, iconF);
        c.drawRect(cx - 20, cy - 12, cx + 20, cy + 12, iconB);
    }
}

class Board {
    static PhysObj createObjAt(float x, float y, float pixelsPerMeter, float groundLength) {
        PhysObj o = new PhysObj();
        o.type = MainActivity.TYPE_BOARD;
        o.x = o.initialX = x;
        o.y = o.initialY = y;
        o.mass = 1.0f;
        o.friction = o.restitution = 0.3f;
        o.realWidth = 1.6f * (groundLength / 10f);
        o.realHeight = 0.12f * (groundLength / 10f);
        o.w = o.realWidth * pixelsPerMeter;
        o.h = o.realHeight * pixelsPerMeter;
        o.rotation = 0;
        o.initialVx = o.initialVy = 0;
        return o;
    }

    static void draw(Canvas c, PhysObj o, Paint bP, Paint fP) {
        if (o.cutVertices.isEmpty()) {
            float l = -o.w / 2, t = -o.h / 2, r = o.w / 2, b = o.h / 2;
            c.drawRect(l, t, r, b, fP);
            c.drawRect(l, t, r, b, bP);
            Paint wp = PhysObj.mp(Color.WHITE, Paint.Style.STROKE, 1f);
            c.drawLine(l + 2, (t + b) / 2, r - 2, (t + b) / 2, wp);
        } else {
            Path p = new Path();
            boolean first = true;
            for (float[] v : o.cutVertices) {
                if (first) {
                    p.moveTo(v[0], v[1]);
                    first = false;
                } else {
                    p.lineTo(v[0], v[1]);
                }
            }
            p.close();
            c.drawPath(p, fP);
            c.drawPath(p, bP);
        }
    }

    static void drawMini(Canvas c, float cx, float cy, Paint iconB, Paint iconF) {
        c.drawRect(cx - 25, cy - 4, cx + 25, cy + 4, iconF);
        c.drawRect(cx - 25, cy - 4, cx + 25, cy + 4, iconB);
    }
}

class Spring {
    static PhysObj createObjAt(float x, float y, float pixelsPerMeter, boolean horizontal, float groundLength) {
        PhysObj o = new PhysObj();
        o.type = MainActivity.TYPE_SPRING;
        o.x = o.initialX = x;
        o.y = o.initialY = y;
        o.mass = 1.0f;
        o.friction = o.restitution = 0.3f;
        o.realLength = 0.8f * (groundLength / 10f);
        o.isHorizontal = horizontal;
        if (horizontal) {
            o.w = o.realLength * pixelsPerMeter;
            o.h = 20;
        } else {
            o.w = 20;
            o.h = o.realLength * pixelsPerMeter;
        }
        o.naturalLen = o.isHorizontal ? o.w : o.h;
        o.springK = o.ropeElasticLimit = 500f;
        o.springBoundRight = true;
        o.freeEndX = o.isHorizontal ? x - o.w / 2 : x;
        o.freeEndY = o.isHorizontal ? y : y - o.h / 2;
        o.initialVx = o.initialVy = 0;
        return o;
    }

    static void draw(Canvas c, PhysObj o, Paint bP, Paint fP) {
        if (o.isHorizontal) {
            drawHorizontal(c, o, bP, fP);
        } else {
            drawVertical(c, o, bP, fP);
        }
    }

    static void drawHorizontal(Canvas c, PhysObj o, Paint bP, Paint fP) {
        float l = -o.w / 2, r = o.w / 2, cy = 0;
        float unitLen = (o.realLength > 0.001f) ? o.naturalLen / o.realLength : 60f;
        float segW = unitLen / 6f;
        if (segW < 1f) segW = 1f;
        float amp = 7f;
        Path p = new Path();
        p.moveTo(l, cy);
        float x = l;
        while (x < r + segW) {
            p.lineTo(x + segW * 0.25f, cy - amp);
            p.lineTo(x + segW * 0.75f, cy + amp);
            p.lineTo(x + segW, cy);
            x += segW;
        }
        c.save();
        c.clipRect(l, cy - amp - 3f, r, cy + amp + 3f);
        bP.setStrokeWidth(2.5f);
        c.drawPath(p, bP);
        bP.setStrokeWidth(2f);
        c.restore();
        c.drawLine(l, cy - 10, l, cy + 10, bP);
        c.drawLine(r, cy - 10, r, cy + 10, bP);
    }

    static void drawVertical(Canvas c, PhysObj o, Paint bP, Paint fP) {
        float t = -o.h / 2, b = o.h / 2, cx = 0;
        float unitLen = (o.realLength > 0.001f) ? o.naturalLen / o.realLength : 60f;
        float segH = unitLen / 6f;
        if (segH < 1f) segH = 1f;
        float amp = 7f;
        Path p = new Path();
        p.moveTo(cx, t);
        float y = t;
        while (y < b + segH) {
            p.lineTo(cx - amp, y + segH * 0.25f);
            p.lineTo(cx + amp, y + segH * 0.75f);
            p.lineTo(cx, y + segH);
            y += segH;
        }
        c.save();
        c.clipRect(cx - amp - 3f, t, cx + amp + 3f, b);
        bP.setStrokeWidth(2.5f);
        c.drawPath(p, bP);
        bP.setStrokeWidth(2f);
        c.restore();
        c.drawLine(cx - 10, t, cx + 10, t, bP);
        c.drawLine(cx - 10, b, cx + 10, b, bP);
    }

    static void drawMini(Canvas c, float cx, float cy, Paint iconB, Paint iconF) {
        float l = cx - 22;
        Path p = new Path();
        p.moveTo(l, cy);
        float sw = 44f / 4;
        for (int i = 0; i < 4; i++) {
            p.lineTo(l + sw * i + sw * 0.25f, cy - 5);
            p.lineTo(l + sw * i + sw * 0.75f, cy + 5);
            p.lineTo(l + sw * (i + 1), cy);
        }
        c.drawPath(p, iconB);
        c.drawLine(l, cy - 6, l, cy + 6, iconB);
        c.drawLine(cx + 22, cy - 6, cx + 22, cy + 6, iconB);
    }
}

class Rope {
    static PhysObj createObjAt(float x, float y, float pixelsPerMeter, boolean horizontal, float groundLength) {
        PhysObj o = new PhysObj();
        o.type = MainActivity.TYPE_ROPE;
        o.x = o.initialX = x;
        o.y = o.initialY = y;
        o.mass = 1.0f;
        o.friction = o.restitution = 0.3f;
        o.realLength = groundLength / 10f;
        o.isHorizontal = horizontal;
        if (horizontal) {
            o.w = o.realLength * pixelsPerMeter;
            o.h = 10;
        } else {
            o.w = 10;
            o.h = o.realLength * pixelsPerMeter;
        }
        o.ropeLen = o.isHorizontal ? o.w : o.h;
        o.ropeBoundRight = true;
        o.springK = o.ropeElasticLimit = 1000f;
        o.freeEndX = o.isHorizontal ? x - o.w / 2 : x;
        o.freeEndY = o.isHorizontal ? y : y - o.h / 2;
        o.initialVx = o.initialVy = 0;
        return o;
    }

    static void draw(Canvas c, PhysObj o, Paint bP) {
        if (o.isHorizontal) {
            drawHorizontal(c, o, bP);
        } else {
            drawVertical(c, o, bP);
        }
    }

    static void drawHorizontal(Canvas c, PhysObj o, Paint bP) {
        float l = -o.w / 2, r = o.w / 2, cy = 0;
        bP.setStrokeWidth(2.5f);
        c.drawLine(l, cy, r, cy, bP);
        bP.setStrokeWidth(2f);
        c.drawCircle(l, cy, 3, bP);
        c.drawCircle(r, cy, 3, bP);
    }

    static void drawVertical(Canvas c, PhysObj o, Paint bP) {
        float t = -o.h / 2, btm = o.h / 2, cx = 0;
        bP.setStrokeWidth(2.5f);
        c.drawLine(cx, t, cx, btm, bP);
        bP.setStrokeWidth(2f);
        c.drawCircle(cx, t, 3, bP);
        c.drawCircle(cx, btm, 3, bP);
    }

    static void drawMini(Canvas c, float cx, float cy, Paint iconB, Paint iconF) {
        iconB.setStrokeWidth(2f);
        c.drawLine(cx - 22, cy, cx + 22, cy, iconB);
        iconB.setStrokeWidth(1.5f);
        c.drawCircle(cx - 22, cy, 2.5f, iconB);
        c.drawCircle(cx + 22, cy, 2.5f, iconB);
    }
}

class RampStraight {
    static PhysObj createObjAt(float x, float y, float pixelsPerMeter, float groundLength) {
        PhysObj o = new PhysObj();
        o.type = MainActivity.TYPE_RAMP_STRAIGHT;
        o.x = o.initialX = x;
        o.y = o.initialY = y;
        o.mass = 1.0f;
        o.friction = o.restitution = 0.3f;
        o.realWidth = 1.8f * (groundLength / 10f);
        o.realHeight = groundLength / 10f;
        o.w = o.realWidth * pixelsPerMeter;
        o.h = o.realHeight * pixelsPerMeter;
        o.initialVx = o.initialVy = 0;
        return o;
    }

    static void draw(Canvas c, PhysObj o, Paint bP, Paint fP) {
        float l = -o.w / 2, r = o.w / 2, t = -o.h / 2, b = o.h / 2;
        Path p = new Path();
        if (o.isHorizontal) {
            p.moveTo(l, b);
            p.lineTo(r, b);
            p.lineTo(r, t);
        } else {
            p.moveTo(r, b);
            p.lineTo(l, b);
            p.lineTo(l, t);
        }
        p.close();
        c.drawPath(p, fP);
        Paint wp = PhysObj.mp(Color.WHITE, Paint.Style.STROKE, 1f);
        float inset = 2f;
        for (float i = 1; i <= 4; i++) {
            float fr = i / 5f;
            float x1;
            float y1;
            float x2;
            float y2;
            if (o.isHorizontal) {
                x1 = l + o.w * fr;
                y1 = b;
                x2 = r;
                y2 = t + o.h * (1 - fr);
            } else {
                x1 = r - o.w * fr;
                y1 = b;
                x2 = l;
                y2 = t + o.h * (1 - fr);
            }
            float ux = x2 - x1;
            float uy = y2 - y1;
            float len = (float) Math.sqrt(ux * ux + uy * uy);
            if (len > 2f * inset + 1f) {
                ux /= len;
                uy /= len;
                c.drawLine(x1 + ux * inset, y1 + uy * inset, x2 - ux * inset, y2 - uy * inset, wp);
            } else {
                c.drawLine(x1, y1, x2, y2, wp);
            }
        }
        c.drawPath(p, bP);
    }

    static void drawMini(Canvas c, float cx, float cy, Paint iconB, Paint iconF) {
        Path p = new Path();
        p.moveTo(cx - 22, cy + 12);
        p.lineTo(cx + 22, cy + 12);
        p.lineTo(cx + 22, cy - 12);
        p.close();
        c.drawPath(p, iconF);
        c.drawPath(p, iconB);
    }
}

class RampArc {
    static PhysObj createObjAt(float x, float y, float pixelsPerMeter, float groundLength) {
        PhysObj o = new PhysObj();
        o.type = MainActivity.TYPE_RAMP_ARC;
        o.x = x;
        o.y = y;
        o.mass = 1.0f;
        o.friction = 0.3f;
        o.restitution = 0.3f;
        o.fixed = false;
        o.realWidth = 1.8f * (groundLength / 10f);
        o.realHeight = 1.0f * (groundLength / 10f);
        o.w = o.realWidth * pixelsPerMeter;
        o.h = o.realHeight * pixelsPerMeter;
        o.fixed = false;
        o.initialX = x;
        o.initialY = y;
        o.initialVx = 0;
        o.initialVy = 0;
        o.isHorizontal = true;
        o.arcAngle = (float) Math.PI;
        o.pipeMode = false;
        o.pipeDiameter = 0.3f;
        return o;
    }

    static void draw(Canvas c, PhysObj o, Paint bP, Paint fP) {
        float l = -o.w / 2, r = o.w / 2, t = -o.h / 2, b = o.h / 2;
        if (o.w <= 1f) {
            float R0 = Math.max(20f, o.h / 2f);
            float start = (float) Math.PI / 2f;
            float sweep = Math.max((float) Math.PI, Math.min((float) (2.0 * Math.PI), o.arcAngle));
            float cx0 = 0f;
            float cy0 = 0f;
            Path solid = new Path();
            int pts0 = Math.max(48, (int) (sweep * R0 / 8f));
            for (int i = 0; i <= pts0; i++) {
                float theta = start - sweep * i / pts0;
                float px = cx0 + (o.isHorizontal ? 1f : -1f) * R0 * (float) Math.cos(theta);
                float py = cy0 + R0 * (float) Math.sin(theta);
                if (i == 0) solid.moveTo(px, py); else solid.lineTo(px, py);
            }
            c.drawPath(solid, bP);
            if (o.pipeMode) {
                float dPx0 = Math.max(1f, o.pipeDiameter * (o.realHeight > 0.001f ? o.h / o.realHeight : 100f));
                float rInner0 = Math.max(1f, R0 - dPx0);
                Path inner0 = new Path();
                for (int ii = 0; ii <= pts0; ii++) {
                    float theta3 = start - sweep * ii / pts0;
                    float px3 = cx0 + (o.isHorizontal ? 1f : -1f) * rInner0 * (float) Math.cos(theta3);
                    float py3 = cy0 + rInner0 * (float) Math.sin(theta3);
                    if (ii == 0) inner0.moveTo(px3, py3); else inner0.lineTo(px3, py3);
                }
                c.drawPath(inner0, bP);
            }
            return;
        }
        float W = Math.max(0f, o.w), H = o.h < 1f ? 1f : o.h;
        float R = (W * W + H * H) / (2f * H);
        if (o.pipeMode || H > W) {
            float cx = o.isHorizontal ? l : r;
            float cy = b - R;
            float endA = (float) Math.atan2(t - cy, (o.isHorizontal ? r : l) - cx);
            float startA = (float) (Math.PI / 2.0);
            if (!o.isHorizontal && endA < startA) endA += 2f * (float) Math.PI;
            Path track = new Path();
            int pts2 = 80;
            for (int i3 = 0; i3 <= pts2; i3++) {
                float theta = o.isHorizontal
                        ? startA + (endA - startA) * i3 / pts2
                        : startA + (endA - startA) * i3 / pts2;
                float px = cx + R * (float) Math.cos(theta);
                float py = cy + R * (float) Math.sin(theta);
                if (i3 == 0) track.moveTo(px, py); else track.lineTo(px, py);
            }
            c.drawPath(track, bP);
            if (H > W && !o.pipeMode) {
                float crossY = b;
                float wallX = o.isHorizontal ? r : l;
                float dx = wallX - cx;
                float ds = R * R - dx * dx;
                if (ds >= 0f) {
                    float y1 = cy + (float) Math.sqrt(ds);
                    float y2 = cy - (float) Math.sqrt(ds);
                    crossY = Math.max(y1, y2);
                    if (crossY < t) crossY = t;
                    if (crossY > b) crossY = b;
                }
                c.drawLine(wallX, b, wallX, crossY, bP);
            }
            if (o.pipeMode) {
                float ppm = (o.realWidth > 0.001f) ? o.w / o.realWidth : 100f;
                float dPx = Math.max(1f, o.pipeDiameter * ppm);
                float innerR = Math.max(1f, R - dPx);
                Path inner = new Path();
                for (int k = 0; k <= pts2; k++) {
                    float theta4 = startA + (endA - startA) * k / pts2;
                    float px4 = cx + innerR * (float) Math.cos(theta4);
                    float py4 = cy + innerR * (float) Math.sin(theta4);
                    if (k == 0) inner.moveTo(px4, py4); else inner.lineTo(px4, py4);
                }
                c.drawPath(inner, bP);
            }
            return;
        }
        Path p = new Path();
        int pts = 60;
        if (o.isHorizontal) {
            float cx = l, cy = b - R;
            float endA = (float) Math.atan2(t - cy, r - cx);
            float startA = (float) (Math.PI / 2.0);
            p.moveTo(l, b);
            p.lineTo(r, b);
            p.lineTo(r, t);
            for (int i = 0; i <= pts; i++) {
                float theta = endA + (startA - endA) * i / pts;
                p.lineTo(cx + R * (float) Math.cos(theta), cy + R * (float) Math.sin(theta));
            }
        } else {
            float cx = r, cy = b - R;
            float endA = (float) Math.atan2(t - cy, l - cx);
            float startA = (float) (Math.PI / 2.0);
            if (endA < startA) endA += 2f * (float) Math.PI;
            p.moveTo(r, b);
            p.lineTo(l, b);
            p.lineTo(l, t);
            for (int i = 0; i <= pts; i++) {
                float theta = endA + (startA - endA) * i / pts;
                p.lineTo(cx + R * (float) Math.cos(theta), cy + R * (float) Math.sin(theta));
            }
        }
        p.close();
        c.drawPath(p, fP);
        c.drawPath(p, bP);
    }

    static void drawMini(Canvas c, float cx, float cy, Paint iconB, Paint iconF) {
        float halfW = 21.6f;
        float halfH = 12f;
        float l = cx - halfW, r = cx + halfW, b = cy + halfH, t = cy - halfH;
        float W = halfW * 2f, H = halfH * 2f;
        float R = (W * W + H * H) / (2f * H);
        float arcCx = l, arcCy = b - R;
        float endA = (float) Math.atan2(t - arcCy, r - arcCx);
        float startA = (float) (Math.PI / 2.0);
        Path p = new Path();
        p.moveTo(l, b);
        p.lineTo(r, b);
        p.lineTo(r, t);
        for (int i = 0; i <= 16; i++) {
            float theta = endA + (startA - endA) * i / 16f;
            p.lineTo(arcCx + R * (float) Math.cos(theta), arcCy + R * (float) Math.sin(theta));
        }
        p.close();
        c.drawPath(p, iconF);
        c.drawPath(p, iconB);
    }
}

class WaterSim {
    static final int NX = 60;
    static final float RHO = 1000f;
    static final float TANK_DEPTH = 0.3f;

    int n = NX;
    float[] d = new float[NX];
    float[] uf = new float[NX + 1];
    float[] solidA = new float[NX];
    float[] spread = new float[NX];
    float[] solidLow = new float[NX];
    boolean[] hasSolid = new boolean[NX];
    float[] eta = new float[NX];
    float[] prevEta = new float[NX];
    float[] etaDot = new float[NX];
    float[] foam = new float[NX];
    float[] tmp = new float[NX];
    float[] rsy = new float[NX];

    float innerW = 1f;
    float level = 0f;
    float dx = 1f / NX;
    float volume = 0f;
    boolean init = false;

    float prevCx = 0f;
    float prevCy = 0f;
    float cvx = 0f;
    float cvy = 0f;
    boolean haveCPrev = false;
    float gEff = 9.8f;
    float bodyAx = 0f;
    float simTime = 0f;
    float accXs = 0f;
    float accYs = 0f;
    boolean held = false;
    int[] shadeCol = new int[NX];
    float[] shadePos = new float[NX];
    float[] cellTop = new float[80];

    ArrayList<float[]> drops = new ArrayList<float[]>();
    ArrayList<float[]> bubbles = new ArrayList<float[]>();

    int rng = 1103515245;

    float rnd() {
        rng = rng * 1103515245 + 12345;
        return ((rng >>> 8) & 0xFFFF) / 65535f;
    }

    float rndS() {
        return rnd() * 2f - 1f;
    }
    static WaterSim of(PhysObj container) {
        if (container.water == null) container.water = new WaterSim();
        float iw = container.realWidth - 2f * container.containerWallThickness;
        if (iw < 0.02f) iw = 0.02f;
        float maxL = container.realHeight - container.containerWallThickness;
        if (maxL < 0f) maxL = 0f;
        float lv = container.containerWaterLevel;
        if (lv < 0f) lv = 0f;
        if (lv > maxL) lv = maxL;
        container.water.configure(iw, lv);
        return container.water;
    }

    void reset(float innerWm, float levelM) {
        innerW = innerWm < 0.02f ? 0.02f : innerWm;
        level = levelM < 0f ? 0f : levelM;
        dx = innerW / n;
        for (int i = 0; i < n; i++) {
            d[i] = level;
            eta[i] = level;
            prevEta[i] = level;
            etaDot[i] = 0f;
            foam[i] = 0f;
            solidA[i] = 0f;
            spread[i] = 0f;
            solidLow[i] = 0f;
            hasSolid[i] = false;
            tmp[i] = 0f;
            rsy[i] = 0f;
            uf[i] = 0f;
        }
        uf[n] = 0f;
        volume = level * innerW;
        drops.clear();
        bubbles.clear();
        haveCPrev = false;
        cvx = 0f;
        cvy = 0f;
        accXs = 0f;
        accYs = 0f;
        held = false;
        simTime = 0f;
        init = true;
    }
    void configure(float innerWm, float levelM) {
        if (innerWm < 0.02f) innerWm = 0.02f;
        if (levelM < 0f) levelM = 0f;
        if (!init) {
            reset(innerWm, levelM);
            return;
        }
        if (Math.abs(innerWm - innerW) > 0.0004f) {
            innerW = innerWm;
            dx = innerW / n;
            for (int i = 0; i < drops.size(); i++) {
                float[] p = drops.get(i);
                if (p[0] > innerW) p[0] = innerW;
            }
            for (int i = 0; i < bubbles.size(); i++) {
                float[] b = bubbles.get(i);
                if (b[0] > innerW) b[0] = innerW;
            }
        }
        if (Math.abs(levelM - level) > 0.0004f) {
            float delta = levelM - level;
            level = levelM;
            for (int i = 0; i < n; i++) {
                d[i] += delta;
                if (d[i] < 0f) d[i] = 0f;
                eta[i] = d[i];
                prevEta[i] = d[i];
                etaDot[i] = 0f;
            }
        }
        volume = level * innerW;
    }

    void clearSolids() {
        for (int i = 0; i < n; i++) {
            solidA[i] = 0f;
            solidLow[i] = 0f;
            hasSolid[i] = false;
        }
    }

    void addSolid(int i, float area, float lowM) {
        if (i < 0 || i >= n) return;
        if (area <= 0f) return;
        solidA[i] += area;
        if (!hasSolid[i] || lowM < solidLow[i]) solidLow[i] = lowM;
        hasSolid[i] = true;
    }
    void spreadSolids() {
        float tot = 0f;
        for (int i = 0; i < n; i++) {
            spread[i] = solidA[i] * 0.2f;
            tot += solidA[i];
        }
        if (tot <= 0f) return;
        for (int pass = 0; pass < 6; pass++) {
            for (int i = 0; i < n; i++) {
                float a = spread[i > 0 ? i - 1 : 0];
                float b = spread[i];
                float e = spread[i < n - 1 ? i + 1 : n - 1];
                tmp[i] = 0.25f * a + 0.5f * b + 0.25f * e;
            }
            for (int i = 0; i < n; i++) spread[i] = tmp[i];
        }
        float flat = tot * 0.8f / n;
        for (int i = 0; i < n; i++) spread[i] += flat;
    }

    void refreshEta() {
        float invDx = 1f / dx;
        for (int i = 0; i < n; i++) {
            float e = d[i] + spread[i] * invDx;
            if (e < 0f) e = 0f;
            eta[i] = e;
        }
    }

    void relaxFlat(float dt, float rate) {
        if (!init) return;
        if (dt <= 0.000001f) return;
        spreadSolids();
        float k = dt * rate;
        if (k < 0f) k = 0f;
        if (k > 1f) k = 1f;
        float sSum = 0f;
        for (int i = 0; i < n; i++) sSum += spread[i];
        float flat = (volume + sSum) / (innerW < 0.0001f ? 0.0001f : innerW);
        float invDx = 1f / dx;
        for (int i = 0; i < n; i++) {
            float target = flat - spread[i] * invDx;
            if (target < 0f) target = 0f;
            float v = d[i] + (target - d[i]) * k;
            if (v != v) v = target;
            d[i] = v < 0f ? 0f : v;
            etaDot[i] *= 1f - k;
        }
        for (int i = 0; i <= n; i++) uf[i] *= 1f - k;
        refreshEta();
    }

    int colAt(float xM) {
        int i = (int) (xM / dx);
        if (i < 0) i = 0;
        if (i >= n) i = n - 1;
        return i;
    }

    float clampX(float xM) {
        if (xM < 0.001f) return 0.001f;
        if (xM > innerW - 0.001f) return innerW - 0.001f;
        return xM;
    }

    float surfaceAt(float xM) {
        if (!init) return level;
        float t = xM / dx - 0.5f;
        int i0 = (int) Math.floor((double) t);
        float f = t - i0;
        if (i0 < 0) {
            i0 = 0;
            f = 0f;
        }
        if (i0 > n - 2) {
            i0 = n - 2;
            f = 1f;
        }
        if (i0 < 0) return eta[0];
        return eta[i0] * (1f - f) + eta[i0 + 1] * f;
    }
    void step(float dt, float g, float ax) {
        if (!init) return;
        if (dt <= 0.000001f) return;
        gEff = g < 0.05f ? 0.05f : g;
        if (gEff > 400f) gEff = 400f;
        bodyAx = ax;
        spreadSolids();
        float invDx = 1f / dx;
        for (int i = 0; i < n; i++) {
            float e = d[i] + spread[i] * invDx;
            prevEta[i] = e < 0f ? 0f : e;
        }
        if (volume > 0.0000005f) {
            float maxD = 0.01f;
            float maxU = 0.2f;
            for (int i = 0; i < n; i++) {
                if (d[i] > maxD) maxD = d[i];
                float au = uf[i] < 0f ? -uf[i] : uf[i];
                if (au > maxU) maxU = au;
            }
            float cw = (float) Math.sqrt((double) (gEff * maxD));
            int sub = (int) Math.ceil((double) (dt * (cw + maxU) / (0.42f * dx)));
            if (sub < 1) sub = 1;
            if (sub > 12) sub = 12;
            float sdt = dt / sub;
            for (int s = 0; s < sub; s++) subStep(sdt);
        }
        refreshEta();
        float inv = 1f / dt;
        for (int i = 0; i < n; i++) {
            float ed = (eta[i] - prevEta[i]) * inv;
            if (ed > 30f) ed = 30f;
            else if (ed < -30f) ed = -30f;
            etaDot[i] = ed;
        }
        updateFoam(dt);
        updateDrops(dt);
        updateBubbles(dt);
        simTime += dt;
        if (simTime > 100000f) simTime -= 100000f;
    }

    float faceFlux(int j) {
        if (j <= 0 || j >= n) return 0f;
        float v = uf[j];
        return v > 0f ? v * d[j - 1] : v * d[j];
    }
    void subStep(float dt) {
        refreshEta();
        float invDx = 1f / dx;
        for (int j = 1; j < n; j++) {
            float dAvg = 0.5f * (d[j - 1] + d[j]);
            if (dAvg < 0.0006f) {
                uf[j] *= 0.35f;
                continue;
            }
            float slope = (eta[j] - eta[j - 1]) * invDx;
            if (slope > 3f) slope = 3f;
            else if (slope < -3f) slope = -3f;
            float u = uf[j] + dt * (bodyAx - gEff * slope);
            u -= u * dt * 1.1f;
            float lim = 4f * (float) Math.sqrt((double) (gEff * dAvg)) + 1.2f;
            if (u > lim) u = lim;
            else if (u < -lim) u = -lim;
            if (u != u) u = 0f;
            uf[j] = u;
        }
        uf[0] = 0f;
        uf[n] = 0f;
        for (int i = 0; i < n; i++) {
            float nd = d[i] - dt * (faceFlux(i + 1) - faceFlux(i)) * invDx;
            if (nd != nd) nd = level;
            tmp[i] = nd < 0f ? 0f : nd;
        }
        for (int i = 0; i < n; i++) d[i] = tmp[i];
        float kd = 7f * dt;
        if (kd > 0.22f) kd = 0.22f;
        for (int i = 0; i < n; i++) {
            float a = d[i > 0 ? i - 1 : 0];
            float b = d[i];
            float e = d[i < n - 1 ? i + 1 : n - 1];
            float v = b + kd * (a + e - 2f * b);
            tmp[i] = v < 0f ? 0f : v;
        }
        for (int i = 0; i < n; i++) d[i] = tmp[i];
        float tot = 0f;
        for (int i = 0; i < n; i++) tot += d[i];
        tot *= dx;
        if (tot > 0.0000005f && volume > 0.0000005f) {
            float s = volume / tot;
            if (s > 0.25f && s < 4f) {
                for (int i = 0; i < n; i++) d[i] *= s;
            } else {
                for (int i = 0; i < n; i++) {
                    d[i] = level;
                    uf[i] = 0f;
                }
                uf[n] = 0f;
            }
        }
    }
    void updateFoam(float dt) {
        float invDx = 1f / dx;
        for (int i = 0; i < n; i++) {
            float shear = Math.abs(uf[i]) + Math.abs(uf[i + 1]);
            float gen = shear * 0.5f - 0.5f;
            if (gen > 0f) foam[i] += gen * dt * 1.5f;
            if (i > 0 && i < n - 1) {
                float sl = Math.abs(eta[i + 1] - eta[i - 1]) * 0.5f * invDx;
                if (sl > 0.75f) foam[i] += (sl - 0.75f) * dt * 0.8f;
            }
            float rise = etaDot[i];
            if (rise < -1.2f) foam[i] += (-rise - 1.2f) * dt * 0.35f;
            foam[i] -= dt * 0.62f;
            if (foam[i] < 0f) foam[i] = 0f;
            else if (foam[i] > 1f) foam[i] = 1f;
        }
        for (int i = 0; i < n; i++) {
            float a = foam[i > 0 ? i - 1 : 0];
            float b = foam[i];
            float e = foam[i < n - 1 ? i + 1 : n - 1];
            tmp[i] = 0.2f * a + 0.6f * b + 0.2f * e;
        }
        for (int i = 0; i < n; i++) foam[i] = tmp[i];
    }

    void addFoam(int i, float amount) {
        if (i < 0 || i >= n) return;
        foam[i] += amount;
        if (foam[i] > 1f) foam[i] = 1f;
    }

    void spawnBubble(float xM, float yM, float rM) {
        if (bubbles.size() > 150) return;
        if (yM < 0.004f) yM = 0.004f;
        float[] b = new float[5];
        b[0] = clampX(xM);
        b[1] = yM;
        b[2] = rM < 0.0018f ? 0.0018f : rM;
        b[3] = 1.4f + rnd() * 2.6f;
        b[4] = rnd() * 6.28f;
        bubbles.add(b);
    }
    void updateDrops(float dt) {
        for (int i = drops.size() - 1; i >= 0; i--) {
            float[] p = drops.get(i);
            p[3] -= gEff * dt;
            p[2] -= p[2] * dt * 0.35f;
            p[0] += p[2] * dt;
            p[1] += p[3] * dt;
            p[4] -= dt;
            if (p[0] < 0.0015f) {
                p[0] = 0.0015f;
                p[2] = -p[2] * 0.3f;
            } else if (p[0] > innerW - 0.0015f) {
                p[0] = innerW - 0.0015f;
                p[2] = -p[2] * 0.3f;
            }
            float sy = surfaceAt(p[0]);
            boolean landed = p[1] <= sy && p[3] < 0f;
            if (landed || p[4] <= 0f || p[1] < -0.02f) {
                int ci = colAt(p[0]);
                if (landed) {
                    addFoam(ci, 0.3f);
                    if (p[3] < -0.7f) {
                        spawnBubble(p[0], sy - 0.012f, 0.0025f + 0.004f * rnd());
                        int j = ci;
                        if (j > 0 && j < n) uf[j] += p[2] * 0.12f;
                    }
                }
                drops.remove(i);
            }
        }
    }

    void updateBubbles(float dt) {
        for (int i = bubbles.size() - 1; i >= 0; i--) {
            float[] b = bubbles.get(i);
            b[4] += dt * 7.5f;
            float v = 0.2f + b[2] * 22f;
            if (v > 0.65f) v = 0.65f;
            b[1] += v * dt;
            b[0] += (float) Math.sin((double) b[4]) * 0.07f * dt;
            b[3] -= dt;
            if (b[0] < 0.002f) b[0] = 0.002f;
            else if (b[0] > innerW - 0.002f) b[0] = innerW - 0.002f;
            float sy = surfaceAt(b[0]);
            if (b[3] <= 0f || b[1] + b[2] * 0.4f >= sy) {
                addFoam(colAt(b[0]), 0.16f);
                bubbles.remove(i);
            }
        }
    }
    void splash(float xM, float strength, float vxM, boolean entering) {
        if (!init) return;
        if (strength <= 0f) return;
        float s = strength > 3f ? 3f : strength;
        float yM = surfaceAt(xM);
        int cnt = (int) (s * (entering ? 8f : 4f));
        if (cnt > 16) cnt = 16;
        for (int k = 0; k < cnt; k++) {
            if (drops.size() > 240) break;
            float sp = (0.55f + 1.7f * rnd()) * (0.45f + s * 0.55f);
            float ang = 1.02f + rndS() * 0.5f;
            float dir = rnd() < 0.5f ? -1f : 1f;
            float[] p = new float[6];
            p[0] = clampX(xM + rndS() * 0.03f * (1f + s));
            p[1] = yM + 0.005f + rnd() * 0.01f;
            p[2] = dir * sp * (float) Math.cos((double) ang) + vxM * 0.3f;
            p[3] = sp * (float) Math.sin((double) ang) * (entering ? 1f : 0.7f);
            p[4] = 0.4f + rnd() * 0.9f;
            p[5] = 0.0028f + rnd() * 0.0055f;
            drops.add(p);
        }
        int ci = colAt(xM);
        for (int i = ci - 3; i <= ci + 3; i++) {
            int ad = i - ci;
            if (ad < 0) ad = -ad;
            addFoam(i, s * 0.42f * (1f - ad / 4.2f));
        }
        float dLoc = d[ci];
        if (dLoc < 0.01f) dLoc = 0.01f;
        float lay = 0.05f / dLoc;
        if (lay > 1f) lay = 1f;
        float imp = s * (entering ? 0.34f : 0.16f) * lay;
        if (!held) {
            for (int j = 1; j < n; j++) {
                int ad = j - ci;
                int aa = ad < 0 ? -ad : ad;
                if (aa == 0 || aa > 6) continue;
                float w = 1f - aa / 7f;
                uf[j] += (ad > 0 ? imp : -imp) * w;
            }
        }
        if (entering) {
            int bc = (int) (s * 4f);
            if (bc > 10) bc = 10;
            for (int k = 0; k < bc; k++) {
                spawnBubble(clampX(xM + rndS() * 0.05f), yM - 0.015f - rnd() * 0.06f, 0.0022f + rnd() * 0.006f);
            }
        }
    }
    static final float PIX_SNAP = 64f;
    static final float PIX_MAP_SCALE = 2f;
    static final float PIX_LIGHT_STEP = 0.3333f;

    static float pixelSize(float ppm) {
        float s = ppm * PIX_MAP_SCALE / PIX_SNAP;
        if (s < 2f) s = 2f;
        if (s > 10f) s = 10f;
        return s;
    }

    static float snapTo(float v, float base, float psz) {
        float k = (base - v) / psz;
        int ki = k >= 0f ? (int) (k + 0.5f) : (int) (k - 0.5f);
        return base - ki * psz;
    }

    static int pixCols(float wpx, float psz) {
        int cells = (int) (wpx / psz);
        if (cells < 3) cells = 3;
        if (cells > 64) cells = 64;
        return cells;
    }

    int colOfCell(int k, int cells) {
        int i = (int) ((k + 0.5f) * n / cells);
        if (i < 0) i = 0;
        if (i > n - 1) i = n - 1;
        return i;
    }

    static final int[] BODY_GRAD = new int[]{
        0xFFAEE5EE,
        0xFF92D9E8,
        0xFF77C7DF,
        0xFF5FB2D2,
        0xFF4E9CC0,
        0xFF4288AE
    };

    static int mixCol(int c0, int c1, float t) {
        if (t < 0f) t = 0f;
        if (t > 1f) t = 1f;
        int a0 = (c0 >>> 24) & 0xFF;
        int r0 = (c0 >>> 16) & 0xFF;
        int g0 = (c0 >>> 8) & 0xFF;
        int b0 = c0 & 0xFF;
        int a1 = (c1 >>> 24) & 0xFF;
        int r1 = (c1 >>> 16) & 0xFF;
        int g1 = (c1 >>> 8) & 0xFF;
        int b1 = c1 & 0xFF;
        int a = a0 + (int) ((a1 - a0) * t + 0.5f);
        int r = r0 + (int) ((r1 - r0) * t + 0.5f);
        int g = g0 + (int) ((g1 - g0) * t + 0.5f);
        int b = b0 + (int) ((b1 - b0) * t + 0.5f);
        if (a < 0) a = 0;
        if (a > 255) a = 255;
        if (r < 0) r = 0;
        if (r > 255) r = 255;
        if (g < 0) g = 0;
        if (g > 255) g = 255;
        if (b < 0) b = 0;
        if (b > 255) b = 255;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    static int bodyColor(float t) {
        if (t < 0f) t = 0f;
        if (t > 1f) t = 1f;
        float st = t * t * (3f - 2f * t);
        st = (st + t) * 0.5f;
        int seg = BODY_GRAD.length - 1;
        float f = st * seg;
        int i = (int) f;
        if (i < 0) i = 0;
        if (i > seg - 1) i = seg - 1;
        return mixCol(BODY_GRAD[i], BODY_GRAD[i + 1], f - i);
    }

    void render(Canvas c, float iL, float iR, float iT, float iB, float ppm) {
        if (!init) return;
        float wpx = iR - iL;
        float hpx = iB - iT;
        if (wpx < 2f || hpx < 2f || ppm < 0.001f) return;
        boolean hasBody = level > 0.0004f;
        if (!hasBody && drops.isEmpty() && bubbles.isEmpty()) return;
        float dxPx = wpx / n;
        float maxE = hpx / ppm;
        float topY = iB;
        for (int i = 0; i < n; i++) {
            float e = eta[i];
            if (e > maxE) e = maxE;
            if (e < 0f) e = 0f;
            float y = iB - e * ppm;
            rsy[i] = y;
            if (y < topY) topY = y;
        }
        int sc = c.save();
        c.clipRect(iL, iT - hpx * 3f - 200f, iR, iB);
        if (hasBody) {
            renderBody(c, iL, iR, iB, ppm, topY);
            renderInterior(c, iL, iB, dxPx, ppm, topY);
            renderSurface(c, iL, iR, iB, dxPx, ppm);
        }
        renderParticles(c, iL, iB, ppm);
        c.restoreToCount(sc);
    }

    void renderBody(Canvas c, float iL, float iR, float iB, float ppm, float topY) {
        float psz = pixelSize(ppm);
        float wpx = iR - iL;
        int cells = pixCols(wpx, psz);
        float cw = wpx / cells;
        Paint p = new Paint();
        p.setAntiAlias(false);
        p.setDither(false);
        p.setStyle(Paint.Style.FILL);
        float gTop = snapTo(topY, iB, psz);
        float span = iB - gTop;
        if (span < psz) span = psz;
        float refSpan = level * ppm;
        if (refSpan < psz) refSpan = psz;
        float refTop = iB - refSpan;
        float rh = psz;
        int rows = (int) (span / rh + 0.5f);
        if (rows < 1) rows = 1;
        while (rows > 200) {
            rh = rh * 2f;
            rows = (int) (span / rh + 0.5f);
            if (rows < 1) rows = 1;
        }
        for (int k = 0; k < cells; k++) {
            int i = colOfCell(k, cells);
            float yT = snapTo(rsy[i], iB, psz);
            if (yT < gTop) yT = gTop;
            if (yT > iB) yT = iB;
            cellTop[k] = yT;
        }
        for (int r = 0; r < rows; r++) {
            float bt = gTop + r * rh;
            float bb = bt + rh;
            if (bb > iB) bb = iB;
            if (bb <= bt) continue;
            float tc = ((bt + bb) * 0.5f - refTop) / refSpan;
            p.setColor(bodyColor(tc));
            int run = -1;
            for (int k = 0; k < cells; k++) {
                float yT = cellTop[k];
                if (yT <= bt) {
                    if (run < 0) run = k;
                    continue;
                }
                if (run >= 0) {
                    c.drawRect(iL + run * cw, bt, iL + k * cw, bb, p);
                    run = -1;
                }
                if (yT < bb) {
                    c.drawRect(iL + k * cw, yT, iL + (k + 1) * cw, bb, p);
                }
            }
            if (run >= 0) {
                c.drawRect(iL + run * cw, bt, iL + cells * cw, bb, p);
            }
        }
    }

    void renderInterior(Canvas c, float iL, float iB, float dxPx, float ppm, float topY) {
        float psz = pixelSize(ppm);
        float wpx = dxPx * n;
        int cells = pixCols(wpx, psz);
        float cw = wpx / cells;
        Paint p = new Paint();
        p.setAntiAlias(false);
        p.setDither(false);
        p.setStyle(Paint.Style.FILL);
        boolean anySolid = false;
        for (int i = 0; i < n; i++) {
            float den = dx * (eta[i] < 0.02f ? 0.02f : eta[i]);
            float frac = hasSolid[i] ? solidA[i] / den : 0f;
            if (frac != frac) frac = 0f;
            if (frac > 1f) frac = 1f;
            if (frac < 0f) frac = 0f;
            int lv = (int) (frac / PIX_LIGHT_STEP);
            if (lv > 2) lv = 2;
            int sa = hasSolid[i] ? 20 + lv * 15 : 0;
            shadeCol[i] = (sa << 24) | 0x002B7396;
            shadePos[i] = (i + 0.5f) / n;
            if (hasSolid[i]) anySolid = true;
        }
        if (anySolid) {
            for (int k = 0; k < cells; k++) {
                int i = colOfCell(k, cells);
                if (!hasSolid[i]) continue;
                float yTop = iB - solidLow[i] * ppm;
                if (yTop < rsy[i]) yTop = rsy[i];
                yTop = snapTo(yTop, iB, psz);
                if (yTop > iB - psz) continue;
                p.setColor(shadeCol[i]);
                c.drawRect(iL + k * cw, yTop, iL + (k + 1) * cw, iB, p);
            }
        }
        float bodyPx = iB - snapTo(topY, iB, psz);
        int glow = (int) (bodyPx * 0.22f / psz + 0.5f);
        if (glow < 3) glow = 3;
        if (glow > 10) glow = 10;
        for (int g = 0; g < glow; g++) {
            float ft = 1f - (float) g / glow;
            int a = (int) (28f * ft * ft + 0.5f);
            if (a < 2) a = 2;
            p.setColor((a << 24) | 0x00EAFBFF);
            int run = -1;
            float y0 = 0f;
            float y1 = 0f;
            for (int k = 0; k < cells; k++) {
                int i = colOfCell(k, cells);
                float yT = snapTo(rsy[i], iB, psz);
                float a0 = yT + (g + 2) * psz;
                float a1 = a0 + psz;
                boolean ok = a1 <= iB;
                if (ok && run >= 0 && a0 == y0) continue;
                if (run >= 0) {
                    c.drawRect(iL + run * cw, y0, iL + k * cw, y1, p);
                    run = -1;
                }
                if (ok) {
                    run = k;
                    y0 = a0;
                    y1 = a1;
                }
            }
            if (run >= 0) {
                c.drawRect(iL + run * cw, y0, iL + cells * cw, y1, p);
            }
        }
    }

    void renderSurface(Canvas c, float iL, float iR, float iB, float dxPx, float ppm) {
        float psz = pixelSize(ppm);
        float wpx = iR - iL;
        int cells = pixCols(wpx, psz);
        float cw = wpx / cells;
        Paint p = new Paint();
        p.setAntiAlias(false);
        p.setDither(false);
        p.setStyle(Paint.Style.FILL);
        int ph = (int) (simTime * 6f);
        if (ph < 0) ph = 0;
        for (int k = 0; k < cells; k++) {
            int i = colOfCell(k, cells);
            float yT = snapTo(rsy[i], iB, psz);
            float x0 = iL + k * cw;
            float x1 = x0 + cw;
            float y1 = yT + psz;
            if (y1 > iB) y1 = iB;
            if (y1 > yT) {
                p.setColor(0xFFDDF6FB);
                c.drawRect(x0, yT, x1, y1, p);
            }
            float y2 = y1 + psz;
            if (y2 > iB) y2 = iB;
            if (y2 > y1 && ((k + ph) % 3) == 0) {
                p.setColor(0xFFB4E5F0);
                c.drawRect(x0, y1, x1, y2, p);
            }
        }
        int fh = (int) (simTime * 4f);
        if (fh < 0) fh = 0;
        for (int k = 0; k < cells; k++) {
            int i = colOfCell(k, cells);
            float f = foam[i];
            if (f < 0.05f) continue;
            if (f > 1f) f = 1f;
            int lv = (int) (f / PIX_LIGHT_STEP) + 1;
            if (lv > 3) lv = 3;
            float yT = snapTo(rsy[i], iB, psz);
            float x0 = iL + k * cw;
            float x1 = x0 + cw;
            int a = 92 + lv * 36;
            if (a > 255) a = 255;
            p.setColor((a << 24) | 0x00F4FDFF);
            c.drawRect(x0, yT - psz, x1, yT, p);
            if (lv >= 2) {
                int a2 = 74 + lv * 26;
                if (a2 > 255) a2 = 255;
                p.setColor((a2 << 24) | 0x00F4FDFF);
                c.drawRect(x0, yT - psz * 2f, x1, yT - psz, p);
            }
            if (lv >= 3 && hash01(k * 7 + 3 + fh) > 0.45f) {
                p.setColor(0x6EF4FDFF);
                c.drawRect(x0, yT - psz * 3f, x1, yT - psz * 2f, p);
            }
        }
        p.setColor(0x70CDEFF7);
        float lT = snapTo(rsy[0], iB, psz);
        c.drawRect(iL, lT - psz * 2f, iL + psz, lT, p);
        float rT = snapTo(rsy[n - 1], iB, psz);
        c.drawRect(iR - psz, rT - psz * 2f, iR, rT, p);
    }

    static float hash01(int k) {
        int h = k * 374761393 + 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        return ((h >>> 8) & 0xFFFF) / 65535f;
    }

    void renderParticles(Canvas c, float iL, float iB, float ppm) {
        float psz = pixelSize(ppm);
        Paint p = new Paint();
        p.setAntiAlias(false);
        p.setDither(false);
        p.setStyle(Paint.Style.FILL);
        for (int i = 0; i < bubbles.size(); i++) {
            float[] b = bubbles.get(i);
            float br = b[2] * ppm;
            int steps = (int) (br * 2f / psz);
            if (steps < 1) steps = 1;
            if (steps > 3) steps = 3;
            float s = steps * psz;
            float bx = snapTo(iL + b[0] * ppm - s * 0.5f, iL, psz);
            float by = snapTo(iB - b[1] * ppm - s * 0.5f, iB, psz);
            p.setColor(0x2EF2FEFF);
            c.drawRect(bx, by, bx + s, by + s, p);
            p.setColor(0x8CEFFDFF);
            c.drawRect(bx, by, bx + s, by + psz, p);
            c.drawRect(bx, by, bx + psz, by + s, p);
            if (steps >= 2) {
                p.setColor(0xB2FFFFFF);
                c.drawRect(bx + psz, by + psz, bx + psz * 2f, by + psz * 2f, p);
            }
        }
        for (int i = 0; i < drops.size(); i++) {
            float[] d0 = drops.get(i);
            float rr = d0[5] * ppm;
            int steps = (int) (rr * 2f / psz);
            if (steps < 1) steps = 1;
            if (steps > 3) steps = 3;
            float s = steps * psz;
            float dxp = snapTo(iL + d0[0] * ppm - s * 0.5f, iL, psz);
            float dyp = snapTo(iB - d0[1] * ppm - s * 0.5f, iB, psz);
            float fade = d0[4] * 1.8f;
            if (fade > 1f) fade = 1f;
            if (fade < 0f) fade = 0f;
            int lv = (int) (fade / PIX_LIGHT_STEP);
            if (lv > 2) lv = 2;
            int a = 140 + lv * 38;
            if (a > 255) a = 255;
            p.setColor((a << 24) | 0x0095DEEF);
            c.drawRect(dxp, dyp, dxp + s, dyp + s, p);
            int a2 = 120 + lv * 40;
            if (a2 > 255) a2 = 255;
            p.setColor((a2 << 24) | 0x00FFFFFF);
            c.drawRect(dxp, dyp, dxp + psz, dyp + psz, p);
        }
    }
}

class Container {
    static PhysObj createObjAt(float x, float y, float pixelsPerMeter, float groundLength) {
        PhysObj o = new PhysObj();
        o.type = MainActivity.TYPE_CONTAINER;
        o.x = o.initialX = x;
        o.y = o.initialY = y;
        o.mass = 2.0f;
        o.friction = 0.3f;
        o.restitution = 0.1f;
        o.realWidth = 1.5f * (groundLength / 10f);
        o.realHeight = groundLength / 10f;
        o.w = o.realWidth * pixelsPerMeter;
        o.h = o.realHeight * pixelsPerMeter;
        o.containerWallThickness = 0.02f;
        o.containerWaterLevel = (o.realHeight - o.containerWallThickness) * 0.5f;
        o.isWaterContainer = true;
        o.initialVx = o.initialVy = 0;
        return o;
    }

    static void draw(Canvas c, PhysObj o, Paint bP, Paint fP) {
        float l = -o.w / 2, r = o.w / 2, t = -o.h / 2, b = o.h / 2;
        float ppm = (o.w > 0.001f && o.realWidth > 0.001f) ? o.w / o.realWidth : 100f;
        float wallThick = o.containerWallThickness * ppm;
        Path uPath = new Path();
        uPath.moveTo(l, t);
        uPath.lineTo(l, b);
        uPath.lineTo(r, b);
        uPath.lineTo(r, t);
        uPath.lineTo(r - wallThick, t);
        uPath.lineTo(r - wallThick, b - wallThick);
        uPath.lineTo(l + wallThick, b - wallThick);
        uPath.lineTo(l + wallThick, t);
        uPath.close();
        if (o.isWaterContainer) {
            float innerL = l + wallThick;
            float innerR = r - wallThick;
            float innerB = b - wallThick;
            if (innerR - innerL > 1f && innerB - t > 1f) {
                WaterSim ws = WaterSim.of(o);
                ws.render(c, innerL, innerR, t, innerB, ppm);
            }
        }
        c.drawPath(uPath, fP);
        c.drawPath(uPath, bP);
    }

    static void drawMini(Canvas c, float cx, float cy, Paint iconB, Paint iconF) {
        float w = 36, h = 24, l = cx - w / 2, r = cx + w / 2, b = cy + h / 2, t = cy - h / 2;
        float wt = 3f;
        Path uPath = new Path();
        uPath.moveTo(l, t);
        uPath.lineTo(l, b);
        uPath.lineTo(r, b);
        uPath.lineTo(r, t);
        uPath.lineTo(r - wt, t);
        uPath.lineTo(r - wt, b - wt);
        uPath.lineTo(l + wt, b - wt);
        uPath.lineTo(l + wt, t);
        uPath.close();
        c.drawPath(uPath, iconF);
        c.drawPath(uPath, iconB);
        Paint waterP = new Paint();
        waterP.setStyle(Paint.Style.FILL);
        waterP.setAntiAlias(false);
        waterP.setDither(false);
        waterP.setColor(0xFF60B4D3);
        c.drawRect(l + wt, cy + 4, r - wt, b - wt, waterP);
        waterP.setColor(0xFF9BDEEA);
        c.drawRect(l + wt, cy + 4, r - wt, cy + 7, waterP);
        waterP.setColor(0xFFDDF6FB);
        c.drawRect(l + wt, cy + 4, r - wt, cy + 6, waterP);
    }
}

class Pulley {
    static PhysObj createObjAt(float x, float y, float pixelsPerMeter, float groundLength) {
        PhysObj o = new PhysObj();
        o.type = MainActivity.TYPE_PULLEY;
        o.x = o.initialX = x;
        o.y = o.initialY = y;
        o.mass = 0.5f;
        o.friction = o.restitution = 0.1f;
        float r = 0.18f * (groundLength / 10f);
        o.w = o.h = r * 2 * pixelsPerMeter;
        o.realRadius = r;
        o.initialVx = o.initialVy = 0;
        return o;
    }

    static void draw(Canvas c, PhysObj o, Paint bP, Paint fP) {
        float r = o.w / 2;
        Paint grooveP = new Paint(); grooveP.setColor(0xFF888888); grooveP.setStyle(Paint.Style.STROKE); grooveP.setStrokeWidth(3f); grooveP.setAntiAlias(true);
        c.drawCircle(0, 0, r, fP);
        c.drawCircle(0, 0, r, bP);
        c.drawCircle(0, 0, r * 0.7f, grooveP);
        Paint axleP = new Paint(); axleP.setColor(Color.DKGRAY); axleP.setStyle(Paint.Style.FILL); axleP.setAntiAlias(true);
        c.drawCircle(0, 0, r * 0.18f, axleP);
    }

    static void drawMini(Canvas c, float cx, float cy, Paint iconB, Paint iconF) {
        c.drawCircle(cx, cy, 14, iconF);
        c.drawCircle(cx, cy, 14, iconB);
        Paint gp = new Paint(); gp.setColor(Color.GRAY); gp.setStyle(Paint.Style.STROKE); gp.setStrokeWidth(2f); gp.setAntiAlias(true);
        c.drawCircle(cx, cy, 9, gp);
        c.drawCircle(cx, cy, 2.5f, gp);
    }
}

class Rod {
    static PhysObj createObjAt(float x, float y, float pixelsPerMeter, boolean horizontal, float groundLength) {
        PhysObj o = new PhysObj();
        o.type = MainActivity.TYPE_ROD;
        o.x = o.initialX = x;
        o.y = o.initialY = y;
        o.mass = 1.0f;
        o.friction = o.restitution = 0.3f;
        o.realLength = 0.8f * (groundLength / 10f);
        o.realHeight = 0f;
        o.isHorizontal = horizontal;
        if (horizontal) {
            o.w = o.realLength * pixelsPerMeter;
            o.h = 0f;
        } else {
            o.w = 0f;
            o.h = o.realLength * pixelsPerMeter;
        }
        o.rodLen = o.realLength * pixelsPerMeter;
        o.initialVx = o.initialVy = 0;
        return o;
    }

    static void draw(Canvas c, PhysObj o, Paint bP, Paint fP) {
        float half = o.rodLen / 2;
        float ppm = (o.realLength > 0.0001f) ? (o.rodLen / o.realLength) : 100f;
        float thick = o.realHeight * ppm;
        if (thick < 6f) thick = 6f;
        float ht = thick / 2;
        Paint rodFill = new Paint();
        rodFill.setColor(Color.WHITE);
        rodFill.setStyle(Paint.Style.FILL);
        rodFill.setAntiAlias(true);
        if (o.isHorizontal || o.rotation != 0) {
            c.drawRect(-half, -ht, half, ht, rodFill);
            c.drawRect(-half, -ht, half, ht, bP);
            c.drawCircle(-half, 0, 3, bP);
            c.drawCircle(half, 0, 3, bP);
        } else {
            c.drawRect(-ht, -half, ht, half, rodFill);
            c.drawRect(-ht, -half, ht, half, bP);
            c.drawCircle(0, -half, 3, bP);
            c.drawCircle(0, half, 3, bP);
        }
    }

    static void drawMini(Canvas c, float cx, float cy, Paint iconB, Paint iconF) {
        Paint rodFill = new Paint();
        rodFill.setColor(Color.WHITE);
        rodFill.setStyle(Paint.Style.FILL);
        rodFill.setAntiAlias(true);
        c.drawRect(cx - 22, cy - 3, cx + 22, cy + 3, rodFill);
        c.drawRect(cx - 22, cy - 3, cx + 22, cy + 3, iconB);
        c.drawCircle(cx - 22, cy, 2.5f, iconB);
        c.drawCircle(cx + 22, cy, 2.5f, iconB);
    }
}

class Ring {
    static PhysObj createObjAt(float x, float y, float pixelsPerMeter, PhysObj host) {
        PhysObj o = new PhysObj();
        o.type = MainActivity.TYPE_RING;
        o.x = o.initialX = x;
        o.y = o.initialY = y;
        o.mass = 0.2f;
        o.friction = 0.3f;
        o.restitution = 0.3f;
        o.realRadius = 0.12f;
        o.w = o.h = o.realRadius * 2 * pixelsPerMeter;
        o.ringHostRod = host;
        o.ringParam = 0.5f;
        o.initialVx = o.initialVy = 0;
        return o;
    }

    static void draw(Canvas c, PhysObj o, Paint bP, Paint fP) {
        float radius = o.w / 2;
        Paint ringP = new Paint();
        ringP.setColor(0xFF444444);
        ringP.setStyle(Paint.Style.STROKE);
        ringP.setStrokeWidth(4f);
        ringP.setAntiAlias(true);
        c.drawCircle(0, 0, radius, ringP);
    }

    static void drawMini(Canvas c, float cx, float cy, Paint iconB, Paint iconF) {
        Paint ringP = new Paint();
        ringP.setColor(0xFF444444);
        ringP.setStyle(Paint.Style.STROKE);
        ringP.setStrokeWidth(3f);
        ringP.setAntiAlias(true);
        c.drawCircle(cx, cy, 12, ringP);
    }
}

class PhysicsView extends View {
    static final float SYMBOL_ARROW_LEN = 60f;
    Paint borderP, fillP, groundP, hatchP, ghostP, ghostFill;
    Paint arrowP, textP, ceilingP, cutP, wallP, ballFillP;
    MainActivity activity;
    float drawStartX = -1, drawStartY = -1, drawCurrentX = -1, drawCurrentY = -1;
    float eraseStartX = -1, eraseStartY = -1, eraseCurrentX = -1, eraseCurrentY = -1;
    android.os.Handler longPressHandler = new android.os.Handler();
    Runnable longPressRunnable;
    private boolean hudSyncPending = false;
    private boolean simEndedPending = false;
    private boolean panelCheckPending = false;

    private final Runnable hudSyncTask = new Runnable() {
        @Override
        public void run() {
            hudSyncPending = false;
            if (activity.timeLabel != null) {
                activity.timeLabel.setText(String.format("%.1f s", activity.elapsedTime));
            }
            if (activity.timeBar != null) {
                activity.timeBar.setProgress((int) (activity.elapsedTime * 10));
            }
            if (simEndedPending) {
                simEndedPending = false;
                if (activity.playBtn != null) activity.playBtn.setText("▶ 播放");
            }
        }
    };

    private final Runnable panelCheckTask = new Runnable() {
        @Override
        public void run() {
            panelCheckPending = false;
            if (activity.settingsPanelObj != null && !activity.objects.contains(activity.settingsPanelObj)) {
                activity.dismissSettingsPanel();
            }
        }
    };

private float magnitude(float dx, float dy) {
    return (float) Math.sqrt(dx * dx + dy * dy);
}

private float halfWidth(PhysObj o) {
    return o.w / 2;
}

private float halfHeight(PhysObj o) {
    return o.h / 2;
}

    public PhysicsView(Context ctx) {
        super(ctx);
        this.activity = (MainActivity) ctx;
        borderP = PhysObj.mp(Color.BLACK, Paint.Style.STROKE, 2f);
        fillP = PhysObj.mp(Color.WHITE, Paint.Style.FILL, 0f);
        groundP = PhysObj.mp(Color.BLACK, Paint.Style.STROKE, 2f);
        hatchP = PhysObj.mp(Color.BLACK, Paint.Style.STROKE, 1f);
        ghostP = PhysObj.mp(0x60000000, Paint.Style.STROKE, 1.5f);
        ghostFill = PhysObj.mp(0x30FFFFFF, Paint.Style.FILL, 0f);
        arrowP = PhysObj.mp(0xFFFF0000, Paint.Style.STROKE, 3f);
        textP = PhysObj.mp(Color.BLACK, Paint.Style.FILL, 0f);
        textP.setTextSize(12f);
        ceilingP = PhysObj.mp(0xFF666666, Paint.Style.STROKE, 3f);
        cutP = PhysObj.mp(0xFFFF0000, Paint.Style.STROKE, 2f);
        wallP = PhysObj.mp(0xFF888888, Paint.Style.STROKE, 3f);
        ballFillP = PhysObj.mp(0xFFFFA500, Paint.Style.FILL, 0f);
    }

    @Override
    protected void onMeasure(int wM, int hM) {
        super.onMeasure(wM, hM);
        activity.groundY = getMeasuredHeight() * MainActivity.GROUND_Y_RATIO;
        activity.wallLeftX = 0;
        activity.wallRightX = getMeasuredWidth();
        activity.updatePixelsPerMeter();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!panelCheckPending && activity.settingsPanelObj != null
                && !activity.objects.contains(activity.settingsPanelObj)) {
            panelCheckPending = post(panelCheckTask);
        }

        canvas.drawColor(Color.WHITE);

        canvas.drawLine(activity.wallLeftX, 0, activity.wallLeftX, activity.groundY, wallP);
        canvas.drawLine(activity.wallRightX, 0, activity.wallRightX, activity.groundY, wallP);

        for (float y = 20; y < activity.groundY; y += 12) {
            canvas.drawLine(activity.wallLeftX, y, activity.wallLeftX - 10, y - 8, hatchP);
            canvas.drawLine(activity.wallRightX, y, activity.wallRightX + 10, y - 8, hatchP);
        }

        canvas.drawLine(0, activity.groundY, getWidth(), activity.groundY, groundP);
        for (float x = 0; x < getWidth(); x += 12) {
            boolean erased = false;
            for (float[] range : activity.groundEraseRanges) {
                if (x >= range[0] && x <= range[1]) { erased = true; break; }
            }
            if (!erased) {
                canvas.drawLine(x, activity.groundY, x - 8, activity.groundY + 10, hatchP);
            }
        }

        for (CeilingSegment cs : activity.ceilingSegments) {
            canvas.drawLine(cs.x1, cs.y1, cs.x2, cs.y2, ceilingP);
            for (float x = Math.min(cs.x1, cs.x2); x < Math.max(cs.x1, cs.x2); x += 12) {
                canvas.drawLine(x, cs.y1, x + 8, cs.y1 - 10, hatchP);
            }
        }

        if (activity.currentMode == MainActivity.MODE_DRAW && drawStartX >= 0) {
            Paint drawPaint = PhysObj.mp(0xFF666666, Paint.Style.STROKE, 3f);
            canvas.drawLine(drawStartX, drawStartY, drawCurrentX, drawStartY, drawPaint);
            drawPointDistances(canvas, drawStartX, drawStartY, drawCurrentX, drawStartY);
        }

        if (activity.currentMode == MainActivity.MODE_ERASE && eraseStartX >= 0) {
            drawPointDistances(canvas, eraseStartX, eraseStartY, eraseCurrentX, eraseCurrentY);
        }

        if (activity.isCutting) {
            Paint cutLinePaint = PhysObj.mp(Color.RED, Paint.Style.STROKE, 2f);
            cutLinePaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{12, 6}, 0));
            canvas.drawLine(activity.cutStartX, activity.cutStartY, activity.cutEndX, activity.cutEndY, cutLinePaint);

            if (activity.cuttingObj != null &&
                    activity.cuttingObj.type != MainActivity.TYPE_RAMP_STRAIGHT &&
                    activity.cuttingObj.type != MainActivity.TYPE_RAMP_ARC) {
                float cutPercent = activity.calculateCutPercentageRealtime();
                Paint textPaint = PhysObj.mp(Color.RED, Paint.Style.FILL, 0f);
                textPaint.setTextSize(24f);
                canvas.drawText(String.format("切割: %.1f%%", cutPercent),
                        activity.cutEndX + 20, activity.cutEndY - 20, textPaint);

                PhysObj co = activity.cuttingObj;
                float cosR = (float) Math.cos(co.rotation), sinR = (float) Math.sin(co.rotation);
                float relX1 = activity.cutStartX - co.x, relY1 = activity.cutStartY - co.y;
                float lx1 = relX1 * cosR + relY1 * sinR, ly1 = -relX1 * sinR + relY1 * cosR;
                float relX2 = activity.cutEndX - co.x, relY2 = activity.cutEndY - co.y;
                float lx2 = relX2 * cosR + relY2 * sinR, ly2 = -relX2 * sinR + relY2 * cosR;

                ArrayList<float[]> verts = new ArrayList<>();
                if (co.type == MainActivity.TYPE_BALL && co.cutVertices.isEmpty()) {
                    float r = co.w / 2;
                    for (int i = 0; i < 48; i++) {
                        float a = (float) (2 * Math.PI * i / 48);
                        verts.add(new float[]{r * (float) Math.cos(a), r * (float) Math.sin(a)});
                    }
                } else if (!co.cutVertices.isEmpty()) {
                    verts.addAll(co.cutVertices);
                } else {
                    float hw = co.w / 2, hh = co.h / 2;
                    verts.add(new float[]{-hw, -hh}); verts.add(new float[]{hw, -hh});
                    verts.add(new float[]{hw, hh}); verts.add(new float[]{-hw, hh});
                }

                float cutNX = ly2 - ly1, cutNY = -(lx2 - lx1);
                float nLen = magnitude(cutNX, cutNY);
                if (nLen > 0.001f) { cutNX /= nLen; cutNY /= nLen; }

                ArrayList<float[]> clipped = new ArrayList<>();
                int n = verts.size();
                for (int i = 0; i < n; i++) {
                    float[] v1 = verts.get(i), v2 = verts.get((i + 1) % n);
                    float d1 = (v1[0] - lx1) * cutNX + (v1[1] - ly1) * cutNY;
                    float d2 = (v2[0] - lx1) * cutNX + (v2[1] - ly1) * cutNY;
                    if (d1 < 0) clipped.add(v1);
                    if ((d1 >= 0 && d2 < 0) || (d1 < 0 && d2 >= 0)) {
                        float tt = d1 / (d1 - d2);
                        clipped.add(new float[]{v1[0] + tt * (v2[0] - v1[0]), v1[1] + tt * (v2[1] - v1[1])});
                    }
                }

                if (clipped.size() >= 3) {
                    Path previewPath = new Path();
                    boolean first = true;
                    for (float[] v : clipped) {
                        float wx = v[0] * cosR - v[1] * sinR + co.x;
                        float wy = v[0] * sinR + v[1] * cosR + co.y;
                        if (first) { previewPath.moveTo(wx, wy); first = false; }
                        else { previewPath.lineTo(wx, wy); }
                    }
                    previewPath.close();
                    Paint dashFill = PhysObj.mp(0x44FF6600, Paint.Style.FILL, 0f);
                    canvas.drawPath(previewPath, dashFill);
                    Paint dashStroke = PhysObj.mp(0xFFFF6600, Paint.Style.STROKE, 2.5f);
                    dashStroke.setPathEffect(new android.graphics.DashPathEffect(new float[]{8, 4}, 0));
                    canvas.drawPath(previewPath, dashStroke);
                }
            }
        }

        for (PhysObj o : activity.objects) {
            if (o.type == MainActivity.TYPE_RING) continue;
            if (o.showTrajectory && !o.trajectory.isEmpty()) {
                Paint trajP = PhysObj.mp(0xFF4080FF, Paint.Style.STROKE, 1f);
                for (int i = 0; i < o.trajectory.size() - 1; i++) {
                    float[] p1 = o.trajectory.get(i);
                    float[] p2 = o.trajectory.get(i + 1);
                    canvas.drawLine(p1[0], p1[1], p2[0], p2[1], trajP);
                }
            }

            drawObj(canvas, o, borderP, fillP);
            boolean showObjectForces = o.showForces;
            boolean fixedRamp = o.fixed && (o.type == MainActivity.TYPE_RAMP_STRAIGHT
                    || o.type == MainActivity.TYPE_RAMP_ARC);
            if (showObjectForces && (!o.fixed || fixedRamp)) {
                drawForceArrows(canvas, o);
            }
        }

        for (PhysObj o : activity.objects) {
            if (o.type != MainActivity.TYPE_RING) continue;
            if (o.showTrajectory && !o.trajectory.isEmpty()) {
                Paint trajP = PhysObj.mp(0xFF4080FF, Paint.Style.STROKE, 1f);
                for (int i = 0; i < o.trajectory.size() - 1; i++) {
                    float[] p1 = o.trajectory.get(i);
                    float[] p2 = o.trajectory.get(i + 1);
                    canvas.drawLine(p1[0], p1[1], p2[0], p2[1], trajP);
                }
            }

            drawObj(canvas, o, borderP, fillP);
            if (o.showForces && !o.fixed) {
                drawForceArrows(canvas, o);
            }
        }

        if (activity.symbolMode) {
            Paint symTagP = PhysObj.mp(0xFFD84315, Paint.Style.FILL, 0f);
            symTagP.setTextSize(22f);
            symTagP.setTextAlign(Paint.Align.CENTER);
            for (PhysObj o : activity.objects) {
                if (!activity.symNumberable(o)) continue;
                String tag = activity.symObjTag(o);
                if (tag.length() == 0) continue;
                float halfSz = (o.type == MainActivity.TYPE_BALL)
                        ? o.w / 2 : Math.max(o.w, o.h) / 2;
                float ty = o.y - halfSz - 8;
                if (ty < 16) ty = o.y + halfSz + 20;
                canvas.drawText(tag, o.x, ty, symTagP);
            }
        }

        if (activity.dragging && activity.selectedObj != null) {
            PhysObj selObj = activity.selectedObj;
            if (selObj.type != MainActivity.TYPE_SPRING && selObj.type != MainActivity.TYPE_ROPE
                    && selObj.type != MainActivity.TYPE_ROD) {
                drawDragDistances(canvas, selObj);
            }
        }

        if (activity.isWrapDrawing && activity.wrapDrawingRope != null) {
            Paint wrapP = PhysObj.mp(0xFFFF6600, Paint.Style.STROKE, 3f);
            ArrayList<float[]> pts = activity.wrapDrawingRope.ropeWrapPoints;
            for (int wi = 0; wi < pts.size() - 1; wi++) {
                canvas.drawLine(pts.get(wi)[0], pts.get(wi)[1], pts.get(wi+1)[0], pts.get(wi+1)[1], wrapP);
            }
        }

        if (activity.draggingFromPanel && activity.dragFromPanelType >= 0) {
            PhysObj ghost = activity.createObjAt(activity.dragFromPanelType, activity.panelDragX, activity.panelDragY);
            drawObj(canvas, ghost, ghostP, ghostFill);
        }

        if (activity.isSettingForce && activity.selectedObj != null) {
            float len;
            String label;
            if (activity.symbolMode) {
                len = SYMBOL_ARROW_LEN;
                label = activity.symJoinLabel(
                        activity.symEffectiveSymbol(activity.selectedObj, "F"),
                        activity.symAppliedForceText()) + ", " + (int) activity.forceAngle + "°";
            } else {
                len = activity.forceMagnitude * activity.pixelsPerMeter / 50f;
                label = "F: " + activity.toSurdDisplay(activity.forceMagnitude) + "N, " + (int) activity.forceAngle + "°";
            }
            float fx = (float) Math.cos(activity.forceAngle * Math.PI / 180) * len;
            float fy = -(float) Math.sin(activity.forceAngle * Math.PI / 180) * len;
            drawArrowWithLabel(canvas, activity.selectedObj.x, activity.selectedObj.y,
                    activity.selectedObj.x + fx, activity.selectedObj.y + fy, label, true);
        }

        if (activity.isSettingVelocity && activity.selectedObj != null) {
            float len;
            String label;
            if (activity.symbolMode) {
                len = SYMBOL_ARROW_LEN;
                label = activity.symJoinLabel(
                        activity.symEffectiveSymbol(activity.selectedObj, "v₀"),
                        activity.symInitVelocityText()) + ", " + (int) activity.velAngle + "°";
            } else {
                len = activity.velMagnitude * activity.pixelsPerMeter * 0.12f;
                label = "v₀: " + activity.toSurdDisplay(activity.velMagnitude) + "m/s, " + (int) activity.velAngle + "°";
            }
            float vx = (float) Math.cos(activity.velAngle * Math.PI / 180) * len;
            float vy = -(float) Math.sin(activity.velAngle * Math.PI / 180) * len;
            drawArrowWithLabel(canvas, activity.selectedObj.x, activity.selectedObj.y,
                    activity.selectedObj.x + vx, activity.selectedObj.y + vy, label, true);
        }

        if (activity.isConnecting && activity.connectingObj != null) {
            canvas.drawLine(activity.connectingObj.x, activity.connectingObj.y,
                    activity.panelDragX, activity.panelDragY, ghostP);
        }

        if (activity.isSelectingConnect) {
            Paint infoP = PhysObj.mp(0xFF1565C0, Paint.Style.FILL, 0f);
            infoP.setTextSize(18f);
            String hint = activity.connectIsEndA ? "点击物体/天花板/地板/墙壁绑定端点A" : "点击物体/天花板/地板/墙壁绑定端点B";
            canvas.drawText(hint, 20, 30, infoP);
        }
        
        if (activity.isSelectingPulleyRamp) {
            Paint infoP = PhysObj.mp(0xFF1565C0, Paint.Style.FILL, 0f);
            infoP.setTextSize(18f);
            canvas.drawText("点击直斜面以绑定（点击空白取消）", 20, 30, infoP);
        }
        
        if (activity.isSelectingConditionTarget) {
            Paint infoP = PhysObj.mp(0xFF1565C0, Paint.Style.FILL, 0f);
            infoP.setTextSize(18f);
            canvas.drawText("点击物体选择（再次点击取消），点击空白取消", 20, 30, infoP);
        }

        if (activity.rotatingSpring && activity.rotatingObj != null) {
            PhysObj ro = activity.rotatingObj;
            float deg = (float) Math.toDegrees(ro.rotation);
            if (deg < 0) deg += 360;
            Paint rotP = PhysObj.mp(0xFF1565C0, Paint.Style.FILL, 0f);
            rotP.setTextSize(20f);
            canvas.drawText(activity.toSurdDisplay(deg) + "°", ro.x + 10, ro.y - 10, rotP);
        }
        if (activity.springRotatePendingObj != null) {
            Paint hintP = PhysObj.mp(0xFF1565C0, Paint.Style.FILL, 0f);
            hintP.setTextSize(18f);
            canvas.drawText("长按弹簧/绳子以旋转", 20, 30, hintP);
        }

        if (activity.simRunning) {
            long now = System.nanoTime();
            if (activity.lastTime > 0) {
                float frameTime = (now - activity.lastTime) / 1000000000f;
                if (frameTime > 0.05f) frameTime = 0.05f;
                activity.accumulator += frameTime;
                while (activity.accumulator >= MainActivity.FIXED_DT) {
                    activity.stepSimulation(MainActivity.FIXED_DT);
                    activity.elapsedTime += MainActivity.FIXED_DT;
                    activity.accumulator -= MainActivity.FIXED_DT;
                    if (activity.elapsedTime - activity.lastStateSaveTime >= 0.1f) {
                        activity.saveSimulationState();
                        activity.lastStateSaveTime = activity.elapsedTime;
                    }
                    if (activity.elapsedTime >= 60f) {
                        activity.elapsedTime = 60f;
                        activity.simRunning = false;
                        simEndedPending = true;
                        break;
                    }
                }
                if (!hudSyncPending) {
                    hudSyncPending = post(hudSyncTask);
                }
            }
            activity.lastTime = now;
            postInvalidateDelayed(16);
        }
    }

    void drawForceArrows(Canvas c, PhysObj o) {
        float ARROW_LEN = 42f;
        Paint pinkArrow = PhysObj.mp(0xFFFFB0CC, Paint.Style.STROKE, 2.5f);
        Paint pinkText = PhysObj.mp(0xFFE8609A, Paint.Style.FILL, 0f);
        pinkText.setTextSize(15f);
        Paint greenArrow = PhysObj.mp(0xFF90EE90, Paint.Style.STROKE, 2.5f);
        Paint greenText = PhysObj.mp(0xFF3CB371, Paint.Style.FILL, 0f);
        greenText.setTextSize(15f);

        drawForceArrow(c, o.x, o.y, o.x, o.y + ARROW_LEN, activity.symArrowLabel("G", o, 0), pinkArrow, pinkText);

        if (activity.objInWater(o)) {
            Paint blueArrow = PhysObj.mp(0xFF7FC5E8, Paint.Style.STROKE, 2.5f);
            Paint blueText = PhysObj.mp(0xFF1479B8, Paint.Style.FILL, 0f);
            blueText.setTextSize(15f);
            drawForceArrow(c, o.x, o.y, o.x, o.y - ARROW_LEN, "F浮", blueArrow, blueText);
        }

        if (activity.windSpeed > 0.001f) {
            float wfx = activity.windSpeed * (float) Math.cos(activity.windDirection * Math.PI / 180);
            float wfy = -activity.windSpeed * (float) Math.sin(activity.windDirection * Math.PI / 180);
            float wfLen = magnitude(wfx, wfy);
            if (wfLen > 0.001f) {
                drawForceArrow(c, o.x, o.y, o.x + wfx / wfLen * ARROW_LEN, o.y + wfy / wfLen * ARROW_LEN, "Fw", greenArrow, greenText);
            }
        }

        if (activity.airResistance > 0.001f) {
            float spd = magnitude(o.vx, o.vy);
            if (spd > activity.pixelsPerMeter * 0.1f) {
                drawForceArrow(c, o.x, o.y, o.x + (-o.vx / spd) * ARROW_LEN, o.y + (-o.vy / spd) * ARROW_LEN, "Fd", greenArrow, greenText);
            }
        }

        if (Math.abs(o.appliedForceX) > 0.001f || Math.abs(o.appliedForceY) > 0.001f) {
            float fLen = magnitude((float) o.appliedForceX, (float) o.appliedForceY);
            float nx = (float) (o.appliedForceX / fLen);
            float ny = (float) (o.appliedForceY / fLen);
            drawForceArrow(c, o.x, o.y, o.x + nx * ARROW_LEN, o.y + ny * ARROW_LEN, activity.symArrowLabel("F", o, 3), pinkArrow, pinkText);
        }

        boolean supportRestingOnGround;
        if (o.type == MainActivity.TYPE_BALL) {
            supportRestingOnGround = o.y + o.w / 2 >= activity.groundY - 8;
        } else {
            supportRestingOnGround = o.y + o.h / 2 >= activity.groundY - 8;
        }
        boolean skipNetSupportArrow = supportRestingOnGround && !o.contactRamp
                && o.supportForceY > 0.001f && hasBodyRestingOnTop(o);
        if (!skipNetSupportArrow
                && (Math.abs(o.supportForceX) > 0.001f || Math.abs(o.supportForceY) > 0.001f)) {
            float nLen = magnitude(o.supportForceX, o.supportForceY);
            drawForceArrow(c, o.x, o.y, o.x + o.supportForceX / nLen * ARROW_LEN, o.y + o.supportForceY / nLen * ARROW_LEN, activity.symArrowLabel("N", o, 1), pinkArrow, pinkText);
        }
        float[] rodAxialF = null;
        boolean rodSoftLink = false;
        for (PhysObj sl : activity.objects) {
            if (sl.type != MainActivity.TYPE_SPRING && sl.type != MainActivity.TYPE_ROPE) continue;
            if (sl.connectedObjA == o || sl.connectedObjB == o) { rodSoftLink = true; break; }
        }
        if (!rodSoftLink) {
            int rodLinkCount = 0;
            PhysObj rodLink = null;
            for (PhysObj link : activity.objects) {
                if (link.type != MainActivity.TYPE_ROD) continue;
                if (link.remoteLock) continue;
                if (link.connectedObjA != o && link.connectedObjB != o) continue;
                rodLinkCount++;
                rodLink = link;
            }
            if (rodLinkCount == 1) {
                float[] axfTry = activity.rodAxialOnObject(rodLink, o);
                if (axfTry != null && magnitude(axfTry[0], axfTry[1]) > 0.001f) {
                    rodAxialF = axfTry;
                }
            }
        }
        boolean rodAxialOnly = rodAxialF != null;

        if (!rodAxialOnly && (Math.abs(o.rodSupportForceX) > 0.001f || Math.abs(o.rodSupportForceY) > 0.001f)) {
            float nLen = magnitude(o.rodSupportForceX, o.rodSupportForceY);
            drawForceArrow(c, o.x, o.y, o.x + o.rodSupportForceX / nLen * ARROW_LEN, o.y + o.rodSupportForceY / nLen * ARROW_LEN, activity.symArrowLabel("N", o, 5), pinkArrow, pinkText);
        }
        if (o.friction > 0.0001f && (Math.abs(o.frictionForceX) > 0.001f || Math.abs(o.frictionForceY) > 0.001f)) {
            float fLen2 = magnitude(o.frictionForceX, o.frictionForceY);
            drawForceArrow(c, o.x, o.y, o.x + o.frictionForceX / fLen2 * ARROW_LEN, o.y + o.frictionForceY / fLen2 * ARROW_LEN, activity.symArrowLabel("Ff", o, 2), greenArrow, greenText);
        }
        if (!rodAxialOnly && (Math.abs(o.constraintForceX) > 0.001f || Math.abs(o.constraintForceY) > 0.001f)) {
            float cfx = o.constraintForceX;
            float cfy = o.constraintForceY;
            float[] wrapDir = activity.wrappedRopePullDir(o);
            if (wrapDir != null) {
                float cMag = magnitude(cfx, cfy);
                cfx = wrapDir[0] * cMag;
                cfy = wrapDir[1] * cMag;
            }
            float cLen = magnitude(cfx, cfy);
            drawForceArrow(c, o.x, o.y, o.x + cfx / cLen * ARROW_LEN, o.y + cfy / cLen * ARROW_LEN, activity.symArrowLabel("Fₜ", o, 4), pinkArrow, pinkText);
        }
        if (Math.abs(o.reactionForceX) > 0.001f || Math.abs(o.reactionForceY) > 0.001f) {
            float rLen = magnitude(o.reactionForceX, o.reactionForceY);
            drawForceArrow(c, o.x, o.y, o.x + o.reactionForceX / rLen * ARROW_LEN,
						   o.y + o.reactionForceY / rLen * ARROW_LEN, "N′", greenArrow, greenText);
        }

        boolean onGround = false;
        if (o.type == MainActivity.TYPE_BALL) {
            onGround = o.y + o.w / 2 >= activity.groundY - 8;
        } else {
            onGround = o.y + o.h / 2 >= activity.groundY - 8;
        }

        if (onGround && !o.contactRamp) {
            if (o.supportForceY >= -0.001f) {
                drawForceArrow(c, o.x, o.y, o.x, o.y - ARROW_LEN, activity.symArrowLabel("N", o, 1), pinkArrow, pinkText);
            }
            float vxMs = o.vx / activity.pixelsPerMeter;
            boolean hasHorizReact = (o.type == MainActivity.TYPE_RAMP_STRAIGHT || o.type == MainActivity.TYPE_RAMP_ARC)
                    && Math.abs(o.displayForceX) > activity.pixelsPerMeter * 0.5f;
            float staticFricDir = 0;
            if (!hasHorizReact && onGround && Math.abs(vxMs) <= 0.05f) {
                if (o.type == MainActivity.TYPE_RAMP_STRAIGHT || o.type == MainActivity.TYPE_RAMP_ARC) {
                    float rL = o.x - o.w / 2, rR = o.x + o.w / 2, rBot = o.y + o.h / 2;
                    float tendX = 0;
                    for (PhysObj other : activity.objects) {
                        if (other == o) continue;
                        float bCX = other.x;
                        if (bCX <= rL || bCX >= rR) continue;
                        float t = (bCX - rL) / o.w;
                        float slope, surfY;
                        if (o.type == MainActivity.TYPE_RAMP_STRAIGHT) {
                            float effT = o.isHorizontal ? t : 1f - t;
                            surfY = rBot - effT * o.h;
                            slope = o.isHorizontal ? -o.h / o.w : o.h / o.w;
                        } else {
                            float xloc = o.isHorizontal ? (bCX - o.x) : -(bCX - o.x);
                            float W_r = o.w, H_r = o.h < 1f ? 1f : o.h;
                            float Rarc = (W_r * W_r + H_r * H_r) / (2f * H_r);
                            float acx = -W_r / 2f, acy = H_r / 2f - Rarc;
                            float dSq = Rarc * Rarc - (xloc - acx) * (xloc - acx);
                            float yloc = (dSq >= 0) ? acy + (float) Math.sqrt(dSq) : acy + o.h * (0.5f - t);
                            surfY = o.y + yloc;
                            float denom = yloc - acy;
                            slope = Math.abs(denom) > 0.1f ? -(xloc - acx) / denom : -o.h / o.w;
                            if (!o.isHorizontal) slope = -slope;
                        }
                        float halfSize = other.type == MainActivity.TYPE_BALL ? halfWidth(other) : halfHeight(other);
                        if (Math.abs(other.y + halfSize - surfY) < 15) {
                            float angle = (float) Math.atan(slope);
                            float nx = (float) Math.sin(angle);
                            float N = other.mass * MainActivity.GRAVITY * (float) Math.abs(Math.cos(angle));
                            tendX += -nx * N;
                        }
                    }
                    if (Math.abs(tendX) > 0.01f) {
                        hasHorizReact = true;
                        staticFricDir = tendX > 0 ? -1f : 1f;
                    }
                } else {
                    float oTop = o.y - o.h / 2, oL = o.x - o.w / 2, oR = o.x + o.w / 2;
                    for (PhysObj other : activity.objects) {
                        if (other == o) continue;
                        float otherBot = other.type == MainActivity.TYPE_BALL ? other.y + other.w / 2 : other.y + other.h / 2;
                        float ow = Math.min(oR, other.x + other.w / 2) - Math.max(oL, other.x - other.w / 2);
                        if (Math.abs(otherBot - oTop) < 10 && ow > 2) {
                            float relVx = other.vx - o.vx;
                            if (Math.abs(relVx) > activity.pixelsPerMeter * 0.05f) {
                                hasHorizReact = true;
                                staticFricDir = relVx > 0 ? -1f : 1f;
                                break;
                            }
                        }
                    }
                }
            }
            boolean onErasedGround = false;
            for (float[] range : activity.groundEraseRanges) {
                if (o.x >= range[0] && o.x <= range[1]) { onErasedGround = true; break; }
            }
            if (!onErasedGround && o.friction > 0.0001f && (Math.abs(vxMs) > 0.05f || hasHorizReact)) {
                float fricDir;
                if (Math.abs(vxMs) > 0.05f) {
                    fricDir = o.vx > 0 ? -1f : 1f;
                } else if (Math.abs(o.displayForceX) > activity.pixelsPerMeter * 0.5f) {
                    fricDir = o.displayForceX > 0 ? -1f : 1f;
                } else {
                    fricDir = staticFricDir;
                }
                if (fricDir != 0) {
                    drawForceArrow(c, o.x, o.y, o.x + fricDir * ARROW_LEN, o.y, "Ff", greenArrow, greenText);
                }
            }
        }

        if (!onGround && !o.contactRamp) {
            float oBottom = o.type == MainActivity.TYPE_BALL ? o.y + o.w / 2 : o.y + o.h / 2;
            float oLeft = o.x - o.w / 2;
            float oRight = o.x + o.w / 2;
            for (PhysObj other : activity.objects) {
                if (other == o || activity.isConstraintBody(other)) continue;
                if (other.type == MainActivity.TYPE_RAMP_STRAIGHT || other.type == MainActivity.TYPE_RAMP_ARC) continue;
                float otherTop = other.y - other.h / 2;
                float overlapX = Math.min(oRight, other.x + other.w / 2) - Math.max(oLeft, other.x - other.w / 2);
                if (Math.abs(oBottom - otherTop) < 8 && overlapX > 0) {
                    drawForceArrow(c, o.x, o.y, o.x, o.y - ARROW_LEN, "N", pinkArrow, pinkText);
                    float vxMs2 = o.vx / activity.pixelsPerMeter;
                    if (o.friction > 0 && Math.abs(vxMs2) > 0.05f) {
                        float fricDir2 = o.vx > 0 ? -1f : 1f;
                        drawForceArrow(c, o.x, o.y, o.x + fricDir2 * ARROW_LEN, o.y, "Ff", pinkArrow, pinkText);
                    }
                    break;
                }
            }
        }

        if (o.contactRamp) {
            boolean hasRecordedSupport = Math.abs(o.supportForceX) > 0.001f
                    || Math.abs(o.supportForceY) > 0.001f;
            if (!hasRecordedSupport) {
                drawForceArrow(c, o.x, o.y, o.x + o.rampNx * ARROW_LEN,
                        o.y + o.rampNy * ARROW_LEN, "N", pinkArrow, pinkText);
            }

            float tangX = (float) Math.cos(o.rampSurfAngle);
            float tangY = (float) Math.sin(o.rampSurfAngle);
            float vTang = o.vx * tangX + o.vy * tangY;
            float gravityParallel = o.mass * MainActivity.GRAVITY * (float) Math.abs(Math.sin(o.rampSurfAngle));
            float normalForce = o.mass * MainActivity.GRAVITY * (float) Math.abs(Math.cos(o.rampSurfAngle));
            float maxFriction = o.friction * normalForce;
            float displayFriction = Math.min(maxFriction, gravityParallel);

            boolean hasRecordedFriction = Math.abs(o.frictionForceX) > 0.001f
                    || Math.abs(o.frictionForceY) > 0.001f;
            if (!hasRecordedFriction && o.friction > 0 && displayFriction > 0.01f) {
                float fricDirX, fricDirY;
                if (Math.abs(vTang) > 5f / activity.pixelsPerMeter) {
                    float s = vTang > 0 ? -1f : 1f;
                    fricDirX = s * tangX;
                    fricDirY = s * tangY;
                } else {
                    fricDirX = tangX;
                    fricDirY = tangY;
                }
                drawForceArrow(c, o.x, o.y, o.x + fricDirX * ARROW_LEN, o.y + fricDirY * ARROW_LEN, "Ff", pinkArrow, pinkText);
            }
        }

        boolean wallCheckable = !activity.isConstraintBody(o) && o.type != MainActivity.TYPE_RING
                && activity.wallRightX > activity.wallLeftX;
        boolean touchWallL = wallCheckable
                && (o.contactWallLeft || o.x - halfWidth(o) <= activity.wallLeftX + 2.5f);
        boolean touchWallR = wallCheckable
                && (o.contactWallRight || o.x + halfWidth(o) >= activity.wallRightX - 2.5f);
        if (touchWallL && pressedAgainstWall(o, -1f)) {
            drawForceArrow(c, o.x, o.y, o.x + ARROW_LEN, o.y, "N", pinkArrow, pinkText);
        }
        if (touchWallR && pressedAgainstWall(o, 1f)) {
            drawForceArrow(c, o.x, o.y, o.x - ARROW_LEN, o.y, "N", pinkArrow, pinkText);
        }

        if (o.type == MainActivity.TYPE_RAMP_STRAIGHT
                || (o.type == MainActivity.TYPE_RAMP_ARC && o.w > 1f)) {
            float rLeft = o.x - o.w / 2, rRight = o.x + o.w / 2;
            float rBottom = o.y + o.h / 2;
            for (PhysObj other : activity.objects) {
                if (other == o || other.fixed || activity.isConstraintBody(other)) continue;
                if (other.type == MainActivity.TYPE_RAMP_STRAIGHT || other.type == MainActivity.TYPE_RAMP_ARC) continue;
                float bCX = other.x;
                if (bCX < rLeft || bCX > rRight) continue;
                float t = (bCX - rLeft) / o.w;
                float slope, surfY;
                if (o.type == MainActivity.TYPE_RAMP_STRAIGHT) {
                    float effT = o.isHorizontal ? t : 1f - t;
                    surfY = rBottom - effT * o.h;
                    slope = o.isHorizontal ? -o.h / o.w : o.h / o.w;
                } else {
                    float xloc = o.isHorizontal ? (bCX - o.x) : -(bCX - o.x);
                    float W_r = o.w, H_r = o.h < 1f ? 1f : o.h;
                    float Rarc = (W_r * W_r + H_r * H_r) / (2f * H_r);
                    float acx = -W_r / 2f, acy = H_r / 2f - Rarc;
                    float dSq = Rarc * Rarc - (xloc - acx) * (xloc - acx);
                    float yloc = (dSq >= 0) ? acy + (float) Math.sqrt(dSq) : acy + o.h * (0.5f - t);
                    surfY = o.y + yloc;
                    float denom = yloc - acy;
                    slope = Math.abs(denom) > 0.1f ? -(xloc - acx) / denom : -o.h / o.w;
                    if (!o.isHorizontal) slope = -slope;
                }
                float halfSize = other.type == MainActivity.TYPE_BALL ? other.w / 2 : other.h / 2;
                if (Math.abs(other.y + halfSize - surfY) < 12) {
                    float angle = (float) Math.atan(slope);
                    float nx = (float) Math.sin(angle);
                    float ny = -(float) Math.cos(angle);
                    drawForceArrow(c, o.x, o.y, o.x - nx * ARROW_LEN * 0.7f, o.y - ny * ARROW_LEN * 0.7f, "N'", pinkArrow, pinkText);
                    float tangX = (float) Math.cos(angle);
                    float tangY = (float) Math.sin(angle);
                    float vTang = (other.vx - o.vx) * tangX + (other.vy - o.vy) * tangY;
                    if (other.friction > 0.0001f && Math.abs(vTang) > 5f / activity.pixelsPerMeter) {
                        float fricDir = vTang > 0 ? 1f : -1f;
                        drawForceArrow(c, o.x, o.y, o.x + fricDir * tangX * ARROW_LEN, o.y + fricDir * tangY * ARROW_LEN, "Ff'", pinkArrow, pinkText);
                    }
                }
            }
        } else if (o.type != MainActivity.TYPE_BALL) {
            float oTop = o.y - o.h / 2;
            float oLeft = o.x - o.w / 2;
            float oRight = o.x + o.w / 2;
            for (PhysObj other : activity.objects) {
                if (other == o || activity.isConstraintBody(other)) continue;
                if (other.type == MainActivity.TYPE_RAMP_STRAIGHT || other.type == MainActivity.TYPE_RAMP_ARC) continue;
                float otherBottom = other.type == MainActivity.TYPE_BALL ? other.y + other.w / 2 : other.y + other.h / 2;
                float otherLeft = other.x - other.w / 2;
                float otherRight = other.x + other.w / 2;
                float overlapX = Math.min(oRight, otherRight) - Math.max(oLeft, otherLeft);
                if (Math.abs(otherBottom - oTop) < 10 && overlapX > 2) {
                    drawForceArrow(c, o.x, o.y, o.x, o.y + ARROW_LEN * 0.7f, "N'", pinkArrow, pinkText);
                    float relVx = other.vx - o.vx;
                    if (other.friction > 0.0001f && Math.abs(relVx) > activity.pixelsPerMeter * 0.05f) {
                        float fricDir = relVx > 0 ? 1f : -1f;
                        drawForceArrow(c, o.x, o.y, o.x + fricDir * ARROW_LEN, o.y, "Ff'", pinkArrow, pinkText);
                    }
                }
            }
        }

        for (PhysObj other : activity.objects) {
            if (other.type == MainActivity.TYPE_SPRING || other.type == MainActivity.TYPE_ROPE) {
                if (other.connectedObjA == o || other.connectedObjB == o) {
                    if (Math.abs(o.constraintForceX) > 0.001f || Math.abs(o.constraintForceY) > 0.001f) break;
                    if (Math.abs(o.rodSupportForceX) > 0.001f || Math.abs(o.rodSupportForceY) > 0.001f) break;
                    if (other.type == MainActivity.TYPE_SPRING && other.springAtRest) continue;
                    if (other.type == MainActivity.TYPE_ROPE && !activity.ropeShowsTension(other)) continue;
                    boolean pressing = other.type == MainActivity.TYPE_SPRING && other.deformation < -0.0005f;
                    float tx, ty;
                    if (other.connectedObjA == o) {
                        if (other.connectedObjB != null) {
                            tx = other.connectedObjB.x - o.x;
                            ty = other.connectedObjB.y - o.y;
                        } else if (other.connectedToCeiling && other.ceilingSegmentId >= 0 && other.ceilingSegmentId < activity.ceilingSegments.size()) {
                            CeilingSegment cs = activity.ceilingSegments.get(other.ceilingSegmentId);
                            float cax = other.ceilingAnchorX != 0 ? other.ceilingAnchorX : (cs.x1 + cs.x2) / 2;
                            tx = cax - o.x;
                            ty = cs.y1 - o.y;
                        } else if (other.connectedToGround) {
                            tx = other.groundAnchorX - o.x;
                            ty = activity.groundY - o.y;
                        } else {
                            tx = other.freeEndX - o.x;
                            ty = other.freeEndY - o.y;
                        }
                    } else {
                        if (other.connectedObjA != null) {
                            tx = other.connectedObjA.x - o.x;
                            ty = other.connectedObjA.y - o.y;
                        } else if (other.connectedToCeiling && other.ceilingSegmentId >= 0 && other.ceilingSegmentId < activity.ceilingSegments.size()) {
                            CeilingSegment cs = activity.ceilingSegments.get(other.ceilingSegmentId);
                            float cax = other.ceilingAnchorX != 0 ? other.ceilingAnchorX : (cs.x1 + cs.x2) / 2;
                            tx = cax - o.x;
                            ty = cs.y1 - o.y;
                        } else if (other.connectedToGround) {
                            tx = other.groundAnchorX - o.x;
                            ty = activity.groundY - o.y;
                        } else {
                            tx = other.freeEndX - o.x;
                            ty = other.freeEndY - o.y;
                        }
                    }
                    float[] wrapPt = activity.ropePullPointFor(other, o);
                    if (wrapPt != null) {
                        tx = wrapPt[0] - o.x;
                        ty = wrapPt[1] - o.y;
                    }
                    float tLen = magnitude(tx, ty);
                    if (tLen > 0.1f) {
                        float dirS = pressing ? -1f : 1f;
                        String lblS = pressing ? activity.symArrowLabel("N", o, 5) : activity.symArrowLabel("Fₜ", o, 4);
                        drawForceArrow(c, o.x, o.y, o.x + dirS * tx / tLen * ARROW_LEN, o.y + dirS * ty / tLen * ARROW_LEN, lblS, pinkArrow, pinkText);
                    }
                    break;
                }
            }
        }

        if (rodAxialOnly) {
            float axLen = magnitude(rodAxialF[0], rodAxialF[1]);
            boolean rodTension = rodAxialF[2] > 0f;
            String lblR = rodTension ? activity.symArrowLabel("Fₜ", o, 4) : activity.symArrowLabel("N", o, 5);
            drawForceArrow(c, o.x, o.y, o.x + rodAxialF[0] / axLen * ARROW_LEN, o.y + rodAxialF[1] / axLen * ARROW_LEN, lblR, pinkArrow, pinkText);
        } else {
            for (PhysObj link : activity.objects) {
                if (link.type != MainActivity.TYPE_ROD) continue;
                if (link.remoteLock) continue;
                if (link.connectedObjA != o && link.connectedObjB != o) continue;
                if (Math.abs(o.constraintForceX) > 0.001f || Math.abs(o.constraintForceY) > 0.001f) break;
                if (Math.abs(o.rodSupportForceX) > 0.001f || Math.abs(o.rodSupportForceY) > 0.001f) break;
                float[] axf = activity.rodAxialOnObject(link, o);
                if (axf == null) break;
                float axLen = magnitude(axf[0], axf[1]);
                if (axLen < 0.001f) break;
                boolean rodTension = axf[2] > 0f;
                String lblR = rodTension ? activity.symArrowLabel("Fₜ", o, 4) : activity.symArrowLabel("N", o, 5);
                drawForceArrow(c, o.x, o.y, o.x + axf[0] / axLen * ARROW_LEN, o.y + axf[1] / axLen * ARROW_LEN, lblR, pinkArrow, pinkText);
                break;
            }
        }
    }

    private boolean hasBodyRestingOnTop(PhysObj o) {
        if (o.type == MainActivity.TYPE_BALL) return false;
        if (o.type == MainActivity.TYPE_RAMP_STRAIGHT || o.type == MainActivity.TYPE_RAMP_ARC) return false;
        float oTop = o.y - o.h / 2;
        float oLeft = o.x - o.w / 2;
        float oRight = o.x + o.w / 2;
        for (PhysObj other : activity.objects) {
            if (other == o || activity.isConstraintBody(other)) continue;
            if (other.type == MainActivity.TYPE_RAMP_STRAIGHT || other.type == MainActivity.TYPE_RAMP_ARC) continue;
            float otherBottom = other.type == MainActivity.TYPE_BALL ? other.y + other.w / 2 : other.y + other.h / 2;
            float otherLeft = other.x - other.w / 2;
            float otherRight = other.x + other.w / 2;
            float overlapX = Math.min(oRight, otherRight) - Math.max(oLeft, otherLeft);
            if (Math.abs(otherBottom - oTop) < 10 && overlapX > 2) return true;
        }
        return false;
    }

    private boolean pressedAgainstWall(PhysObj o, float sign) {
        float ppm = activity.pixelsPerMeter;
        if (sign * o.vx > ppm * 0.01f) return true;
        if (sign * o.appliedForceX > 0.01f) return true;
        if (activity.windSpeed > 0.001f) {
            float windX = activity.windSpeed * (float) Math.cos(activity.windDirection * Math.PI / 180);
            if (sign * windX > 0.01f) return true;
        }
        if (sign * o.constraintForceX > ppm * 0.02f) return true;
        if (sign * o.rodSupportForceX > ppm * 0.02f) return true;
        if (sign * o.supportForceX > 0.01f) return true;
        if (sign * o.displayForceX > ppm * 0.05f) return true;
        float oHalfW = o.type == MainActivity.TYPE_BALL ? o.w / 2f : halfWidth(o);
        float oHalfH = o.type == MainActivity.TYPE_BALL ? o.w / 2f : halfHeight(o);
        float oTop = o.y - oHalfH, oBot = o.y + oHalfH;
        for (PhysObj other : activity.objects) {
            if (other == o || activity.isConstraintBody(other)) continue;
            if (other.type == MainActivity.TYPE_RING) continue;
            float ohw = other.type == MainActivity.TYPE_BALL ? other.w / 2f : halfWidth(other);
            float ohh = other.type == MainActivity.TYPE_BALL ? other.w / 2f : halfHeight(other);
            float dxRel = other.x - o.x;
            if (sign * dxRel >= 0f) continue;
            if (Math.abs(dxRel) - (oHalfW + ohw) > 4f) continue;
            float overlapY = Math.min(oBot, other.y + ohh) - Math.max(oTop, other.y - ohh);
            if (overlapY <= 2f) continue;
            if (sign * other.vx > ppm * 0.01f) return true;
            if (sign * other.displayForceX > ppm * 0.05f) return true;
        }
        return false;
    }

    void drawForceArrow(Canvas c, float x1, float y1, float x2, float y2, String label, Paint arrow, Paint textPaint) {
        c.drawLine(x1, y1, x2, y2, arrow);
        float angle = (float) Math.atan2(y2 - y1, x2 - x1);
        float len = 12f;
        c.drawLine(x2, y2, x2 - len * (float) Math.cos(angle - 0.5), y2 - len * (float) Math.sin(angle - 0.5), arrow);
        c.drawLine(x2, y2, x2 - len * (float) Math.cos(angle + 0.5), y2 - len * (float) Math.sin(angle + 0.5), arrow);
        float labelX = x2 + 6;
        if (x2 < x1 - 0.5f) {
            labelX = x2 - 6 - measureForceLabel(label, textPaint);
        }
        drawForceLabel(c, label, labelX, y2 + 5, textPaint);
    }

    float measureForceLabel(String label, Paint textPaint) {
        if (label == null || label.length() == 0) return 0f;
        float baseSize = textPaint.getTextSize();
        int idx = label.indexOf('浮');
        if (idx > 0) {
            String head = label.substring(0, idx);
            String sub = label.substring(idx);
            float headW = textPaint.measureText(head);
            textPaint.setTextSize(baseSize * 0.62f);
            float subW = textPaint.measureText(sub);
            textPaint.setTextSize(baseSize);
            return headW + 1f + subW;
        }
        if (label.length() >= 2 && label.charAt(0) == 'F' && label.charAt(1) == 'f') {
            float headW = textPaint.measureText("F");
            textPaint.setTextSize(baseSize * 0.62f);
            float subW = textPaint.measureText("f");
            textPaint.setTextSize(baseSize);
            String tail = label.substring(2);
            float tailW = 0f;
            if (tail.length() > 0) {
                tailW = 1f + textPaint.measureText(tail);
            }
            return headW + 1f + subW + tailW;
        }
        return textPaint.measureText(label);
    }

    void drawForceLabel(Canvas c, String label, float x, float y, Paint textPaint) {
        if (label == null || label.length() == 0) return;
        int idx = label.indexOf('浮');
        if (idx > 0) {
            String head = label.substring(0, idx);
            String sub = label.substring(idx);
            float baseSize = textPaint.getTextSize();
            c.drawText(head, x, y, textPaint);
            float headW = textPaint.measureText(head);
            textPaint.setTextSize(baseSize * 0.62f);
            c.drawText(sub, x + headW + 1f, y + baseSize * 0.22f, textPaint);
            textPaint.setTextSize(baseSize);
            return;
        }
        if (label.length() >= 2 && label.charAt(0) == 'F' && label.charAt(1) == 'f') {
            float baseSize = textPaint.getTextSize();
            c.drawText("F", x, y, textPaint);
            float headW = textPaint.measureText("F");
            textPaint.setTextSize(baseSize * 0.62f);
            c.drawText("f", x + headW + 1f, y + baseSize * 0.22f, textPaint);
            float subW = textPaint.measureText("f");
            textPaint.setTextSize(baseSize);
            String tail = label.substring(2);
            if (tail.length() > 0) {
                c.drawText(tail, x + headW + subW + 2f, y, textPaint);
            }
            return;
        }
        c.drawText(label, x, y, textPaint);
    }

    void drawArrowWithLabel(Canvas c, float x1, float y1, float x2, float y2, String label, boolean aboveObj) {
        drawArrow(c, x1, y1, x2, y2, "");
        Paint labelPaint = PhysObj.mp(0xFFFF0000, Paint.Style.FILL, 0f);
        labelPaint.setTextSize(16f);
        float labelY = aboveObj ? (y1 - 30) : (y2 + 20);
        c.drawText(label, x1, labelY, labelPaint);
    }

    void drawArrow(Canvas c, float x1, float y1, float x2, float y2, String label) {
        float dx = x2 - x1, dy = y2 - y1;
        float angle = (float) Math.atan2(dy, dx);
        c.drawLine(x1, y1, x2, y2, arrowP);
        float arrowLength = 15f, arrowAngle = (float) (Math.PI / 6);
        c.drawLine(x2, y2,
                x2 - arrowLength * (float) Math.cos(angle - arrowAngle),
                y2 - arrowLength * (float) Math.sin(angle - arrowAngle), arrowP);
        c.drawLine(x2, y2,
                x2 - arrowLength * (float) Math.cos(angle + arrowAngle),
                y2 - arrowLength * (float) Math.sin(angle + arrowAngle), arrowP);
        if (label != null && !label.isEmpty()) {
            c.drawText(label, x2 + 10, y2 - 10, textP);
        }
    }

    void drawObj(Canvas c, PhysObj o, Paint bP, Paint fP) {
        if (o.type == MainActivity.TYPE_ROPE && o.ropeWrapped && !o.ropeWrapPoints.isEmpty()) {
            Paint wp = PhysObj.mp(Color.BLACK, Paint.Style.STROKE, 2.5f);
            ArrayList<float[]> pts = o.ropeWrapPoints;
            for (int i = 0; i < pts.size() - 1; i++) {
                c.drawLine(pts.get(i)[0], pts.get(i)[1], pts.get(i + 1)[0], pts.get(i + 1)[1], wp);
            }
            float[] first = pts.get(0), last = pts.get(pts.size() - 1);
            c.drawCircle(first[0], first[1], 3, wp);
            c.drawCircle(last[0], last[1], 3, wp);
            if (o == activity.connectHighlightObj) {
                Paint hlP = PhysObj.mp(0xAAB0D9FF, Paint.Style.STROKE, 2f);
                c.drawCircle(first[0], first[1], 8, hlP);
            }
            return;
        }

        int saveCount = c.save();
        c.translate(o.x, o.y);
        if (o.rotation != 0) {
            c.rotate((float) (o.rotation * 180 / Math.PI));
        }

        switch (o.type) {
            case MainActivity.TYPE_BLOCK:
                Block.draw(c, o, bP, fP);
                break;
            case MainActivity.TYPE_BOARD:
                Board.draw(c, o, bP, fP);
                break;
            case MainActivity.TYPE_SPRING:
                Spring.draw(c, o, bP, fP);
                break;
            case MainActivity.TYPE_ROPE:
                Rope.draw(c, o, bP);
                break;
            case MainActivity.TYPE_RAMP_STRAIGHT:
                RampStraight.draw(c, o, bP, fP);
                break;
            case MainActivity.TYPE_RAMP_ARC:
                RampArc.draw(c, o, bP, fP);
                break;
            case MainActivity.TYPE_BALL:
                Ball.draw(c, o, bP, ballFillP);
                break;
            case MainActivity.TYPE_CONTAINER:
                Container.draw(c, o, bP, fP);
                break;
            case MainActivity.TYPE_PULLEY:
                Pulley.draw(c, o, bP, fP);
                break;
            case MainActivity.TYPE_ROD:
                Rod.draw(c, o, bP, fP);
                break;
            case MainActivity.TYPE_RING:
                Ring.draw(c, o, bP, fP);
                break;
        }

        if (o == activity.connectHighlightObj) {
            Paint hlP = PhysObj.mp(0xAAB0D9FF, Paint.Style.STROKE, 2f);
            if (o.type == MainActivity.TYPE_BALL) {
                c.drawCircle(0, 0, o.w / 2 + 4, hlP);
            } else {
                c.drawRect(-o.w / 2 - 4, -o.h / 2 - 4, o.w / 2 + 4, o.h / 2 + 4, hlP);
            }
        }
        if (o == activity.conditionHighlightObj) {
            Paint hlP = PhysObj.mp(0xFFFF8C00, Paint.Style.STROKE, 3f);
            if (o.type == MainActivity.TYPE_BALL) {
                c.drawCircle(0, 0, o.w / 2 + 5, hlP);
            } else {
                c.drawRect(-o.w / 2 - 5, -o.h / 2 - 5, o.w / 2 + 5, o.h / 2 + 5, hlP);
            }
        }

        if (o.showState) {
            c.restoreToCount(saveCount);
            c.save();
            c.translate(o.x, o.y);
            double v = Math.sqrt(o.physVx * o.physVx + o.physVy * o.physVy) / activity.pixelsPerMeter;
            double a = Math.sqrt(o.accelX * o.accelX + o.accelY * o.accelY);
            double dispDx = o.physX - o.initialX;
            double dispDy = o.physY - o.initialY;
            double displacement = Math.sqrt(dispDx * dispDx + dispDy * dispDy) / activity.pixelsPerMeter;

            String[][] stateRows;
            if (activity.symbolMode) {
                stateRows = activity.symStateRows(o);
            } else {
                stateRows = activity.numericStateRows(o, v, displacement, a);
            }

            float totalGraphWidth = 3 * 60 + 2 * 5f;
            float halfSz = (o.type == MainActivity.TYPE_BALL) ? o.w / 2 : Math.max(o.w, o.h) / 2;
            float aboveY = -halfSz - 80;
            float screenTopLocal = -o.y + 5;
            float belowY = halfSz + 10;
            float graphY = (aboveY >= screenTopLocal) ? aboveY : belowY;
            float textY = graphY + 20;

            float graphX = -totalGraphWidth / 2;
            float screenLeftLocal = -o.x + 5;
            if (graphX < screenLeftLocal) graphX = screenLeftLocal;

            drawWaveformGraphs(c, o, graphX, graphY);
            drawFormulaRows(c, stateRows, graphX, graphY + 22, textP);
        }
        c.restoreToCount(saveCount);
    }

    void drawFormulaRows(Canvas c, String[][] rows, float x, float yTop, Paint paint) {
        if (rows == null) return;
        com.lie.acsm.formula.ERenderer r = new com.lie.acsm.formula.ERenderer(null);
        float lineTop = yTop;
        for (int i = 0; i < rows.length; i++) {
            String pre = rows[i][0] == null ? "" : rows[i][0];
            String tex = rows[i][1] == null ? "" : rows[i][1];
            String suf = rows[i][2] == null ? "" : rows[i][2];
            float cx = x;
            if (tex.length() == 0) {
                float base = lineTop - paint.getFontMetrics().top;
                c.drawText(pre + suf, cx, base, paint);
                lineTop = base + paint.getFontMetrics().bottom + 2f;
                continue;
            }
            com.lie.acsm.formula.ETree tree = com.lie.acsm.formula.EBuild.fromTex(tex);
            android.graphics.RectF b = com.lie.acsm.formula.ERenderer.measureSeq(tree.root, paint);
            float base = lineTop - b.top;
            if (pre.length() > 0) {
                c.drawText(pre, cx, base, paint);
                cx += paint.measureText(pre);
            }
            cx += r.drawSeq(c, tree.root, cx, base, paint);
            if (suf.length() > 0) {
                c.drawText(suf, cx, base, paint);
            }
            lineTop = base + b.bottom + 2f;
        }
    }

    void drawTextMultiline(Canvas c, String text, float x, float y, Paint paint) {
        String[] lines = text.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            c.drawText(lines[i], x, y + i * paint.getTextSize(), paint);
        }
    }

    void drawWaveformGraphs(Canvas c, PhysObj o, float x, float y) {
        float graphWidth = 60;
        float graphHeight = 15;
        float spacing = 5;

        Paint graphBg = PhysObj.mp(0xFFEEEEEE, Paint.Style.FILL, 0);
        Paint graphLineV = PhysObj.mp(0xFFFF0000, Paint.Style.STROKE, 1f);
        Paint graphLineX = PhysObj.mp(0xFF00FF00, Paint.Style.STROKE, 1f);
        Paint graphLineA = PhysObj.mp(0xFF0000FF, Paint.Style.STROKE, 1f);

        c.drawRect(x, y, x + graphWidth, y + graphHeight, graphBg);
        if (!o.velocityHistory.isEmpty()) {
            float maxV = 5f;
            for (int i = 0; i < o.velocityHistory.size() - 1; i++) {
                float v1 = Math.max(-1f, Math.min(1f, o.velocityHistory.get(i) / maxV));
                float v2 = Math.max(-1f, Math.min(1f, o.velocityHistory.get(i + 1) / maxV));
                float x1 = x + (float) i / o.velocityHistory.size() * graphWidth;
                float x2 = x + (float) (i + 1) / o.velocityHistory.size() * graphWidth;
                c.drawLine(x1, y + graphHeight / 2 - v1 * graphHeight / 2,
                        x2, y + graphHeight / 2 - v2 * graphHeight / 2, graphLineV);
            }
        }

        c.drawRect(x + graphWidth + spacing, y, x + 2 * graphWidth + spacing, y + graphHeight, graphBg);
        if (!o.positionHistory.isEmpty()) {
            float maxX = 5f;
            for (int i = 0; i < o.positionHistory.size() - 1; i++) {
                float p1 = Math.max(-1f, Math.min(1f, o.positionHistory.get(i) / maxX));
                float p2 = Math.max(-1f, Math.min(1f, o.positionHistory.get(i + 1) / maxX));
                float x1 = x + graphWidth + spacing + (float) i / o.positionHistory.size() * graphWidth;
                float x2 = x + graphWidth + spacing + (float) (i + 1) / o.positionHistory.size() * graphWidth;
                c.drawLine(x1, y + graphHeight / 2 - p1 * graphHeight / 2,
                        x2, y + graphHeight / 2 - p2 * graphHeight / 2, graphLineX);
            }
        }

        c.drawRect(x + 2 * graphWidth + 2 * spacing, y, x + 3 * graphWidth + 2 * spacing, y + graphHeight, graphBg);
        if (!o.accelHistory.isEmpty()) {
            float maxA = 20f;
            for (int i = 0; i < o.accelHistory.size() - 1; i++) {
                float a1 = Math.max(-1f, Math.min(1f, o.accelHistory.get(i) / maxA));
                float a2 = Math.max(-1f, Math.min(1f, o.accelHistory.get(i + 1) / maxA));
                float x1 = x + 2 * graphWidth + 2 * spacing + (float) i / o.accelHistory.size() * graphWidth;
                float x2 = x + 2 * graphWidth + 2 * spacing + (float) (i + 1) / o.accelHistory.size() * graphWidth;
                c.drawLine(x1, y + graphHeight / 2 - a1 * graphHeight / 2,
                        x2, y + graphHeight / 2 - a2 * graphHeight / 2, graphLineA);
            }
        }
    }

    void drawDragDistances(Canvas canvas, PhysObj obj) {
        float ppm = activity.pixelsPerMeter;
        Paint dp = PhysObj.mp(Color.BLACK, Paint.Style.STROKE, 1.5f);
        Paint dtp = PhysObj.mp(Color.BLACK, Paint.Style.FILL, 0);
        dtp.setTextSize(20f); dtp.setTextAlign(Paint.Align.CENTER);

        float ol, or2, ot, ob;
        if (obj.type == MainActivity.TYPE_BALL) {
            float r = obj.w / 2;
            ol = obj.x - r; or2 = obj.x + r; ot = obj.y - r; ob = obj.y + r;
        } else {
            ol = obj.x - obj.w / 2; or2 = obj.x + obj.w / 2;
            ot = obj.y - obj.h / 2; ob = obj.y + obj.h / 2;
        }

        boolean onRamp = obj.contactRamp;
        float surfAngle = onRamp ? obj.rampSurfAngle : 0f;
        float cosA = (float) Math.cos(surfAngle), sinA = (float) Math.sin(surfAngle);

        float nearestVert = Float.MAX_VALUE;
        float vx1 = 0, vy1 = 0, vx2 = 0, vy2 = 0;
        String vLabel = "";

        float nearestHoriz = Float.MAX_VALUE;
        float hx1 = 0, hy1 = 0, hx2 = 0, hy2 = 0;
        String hLabel = "";

        if (!onRamp) {
            float dg = (activity.groundY - ob) / ppm;
            if (dg >= 0 && dg < 5f) {
                nearestVert = dg;
                vx1 = obj.x; vy1 = ob; vx2 = obj.x; vy2 = activity.groundY;
                vLabel = activity.toSurdDisplay(dg) + "m";
            }
            float dlw = (ol - activity.wallLeftX) / ppm;
            if (dlw >= 0 && dlw < 5f) {
                nearestHoriz = dlw;
                hx1 = activity.wallLeftX; hy1 = obj.y; hx2 = ol; hy2 = obj.y;
                hLabel = activity.toSurdDisplay(dlw) + "m";
            }
            float drw = (activity.wallRightX - or2) / ppm;
            if (drw >= 0 && drw < 5f && drw < nearestHoriz) {
                nearestHoriz = drw;
                hx1 = or2; hy1 = obj.y; hx2 = activity.wallRightX; hy2 = obj.y;
                hLabel = activity.toSurdDisplay(drw) + "m";
            }
            for (PhysObj other : activity.objects) {
                if (other == obj || activity.isConstraintBody(other)) continue;
                float obl = other.x - other.w / 2, obr = other.x + other.w / 2;
                float obt = other.type == MainActivity.TYPE_BALL ? other.y - other.w / 2 : other.y - other.h / 2;
                float obb = other.type == MainActivity.TYPE_BALL ? other.y + other.w / 2 : other.y + other.h / 2;

                float midX = other.x, midY = other.y;
                float vdMid = (midY - ob) / ppm;
                if (vdMid > 0 && vdMid < 3f && vdMid < nearestVert) {
                    nearestVert = vdMid;
                    vx1 = obj.x; vy1 = ob; vx2 = obj.x; vy2 = midY;
                    vLabel = activity.toSurdDisplay(vdMid) + "m";
                }
                float hdMidR = (midX - or2) / ppm;
                if (hdMidR > 0 && hdMidR < 3f && hdMidR < nearestHoriz) {
                    nearestHoriz = hdMidR;
                    hx1 = or2; hy1 = obj.y; hx2 = midX; hy2 = obj.y;
                    hLabel = activity.toSurdDisplay(hdMidR) + "m";
                }
                float hdMidL = (ol - midX) / ppm;
                if (hdMidL > 0 && hdMidL < 3f && hdMidL < nearestHoriz) {
                    nearestHoriz = hdMidL;
                    hx1 = midX; hy1 = obj.y; hx2 = ol; hy2 = obj.y;
                    hLabel = activity.toSurdDisplay(hdMidL) + "m";
                }

                float vd = (obt - ob) / ppm;
                if (vd > 0 && vd < 3f && vd < nearestVert) {
                    float overlapX = Math.min(or2, obr) - Math.max(ol, obl);
                    if (overlapX > 0) {
                        nearestVert = vd;
                        float mx = (Math.max(ol, obl) + Math.min(or2, obr)) / 2;
                        vx1 = mx; vy1 = ob; vx2 = mx; vy2 = obt;
                        vLabel = activity.toSurdDisplay(vd) + "m";
                    }
                }
                float hd = (obl - or2) / ppm;
                if (hd > 0 && hd < 3f && hd < nearestHoriz) {
                    float overlapY = Math.min(ob, obb) - Math.max(ot, obt);
                    if (overlapY > 0) {
                        nearestHoriz = hd;
                        float my = (Math.max(ot, obt) + Math.min(ob, obb)) / 2;
                        hx1 = or2; hy1 = my; hx2 = obl; hy2 = my;
                        hLabel = activity.toSurdDisplay(hd) + "m";
                    }
                }
                float hd2 = (ol - obr) / ppm;
                if (hd2 > 0 && hd2 < 3f && hd2 < nearestHoriz) {
                    float overlapY = Math.min(ob, obb) - Math.max(ot, obt);
                    if (overlapY > 0) {
                        nearestHoriz = hd2;
                        float my = (Math.max(ot, obt) + Math.min(ob, obb)) / 2;
                        hx1 = obr; hy1 = my; hx2 = ol; hy2 = my;
                        hLabel = activity.toSurdDisplay(hd2) + "m";
                    }
                }
            }
        } else {
            float objProjT = obj.x * cosA + obj.y * sinA;
            float objProjN = obj.x * (-sinA) + obj.y * cosA;
            for (PhysObj other : activity.objects) {
                if (other == obj || activity.isConstraintBody(other)) continue;
                float otherProjT = other.x * cosA + other.y * sinA;
                float otherProjN = other.x * (-sinA) + other.y * cosA;
                float dT = Math.abs((otherProjT - objProjT) / ppm);
                if (dT < 3f && dT < nearestHoriz) {
                    nearestHoriz = dT;
                    float sign = (otherProjT > objProjT) ? 1 : -1;
                    hx1 = obj.x; hy1 = obj.y;
                    hx2 = obj.x + cosA * sign * dT * ppm;
                    hy2 = obj.y + sinA * sign * dT * ppm;
                    hLabel = activity.toSurdDisplay(dT) + "m";
                }
                float dN = Math.abs((otherProjN - objProjN) / ppm);
                if (dN < 3f && dN < nearestVert) {
                    nearestVert = dN;
                    float signN = (otherProjN > objProjN) ? 1 : -1;
                    vx1 = obj.x; vy1 = obj.y;
                    vx2 = obj.x + (-sinA) * signN * dN * ppm;
                    vy2 = obj.y + cosA * signN * dN * ppm;
                    vLabel = activity.toSurdDisplay(dN) + "m";
                }
            }
            float dg2 = (activity.groundY - ob) / ppm;
            if (dg2 >= 0 && dg2 < 5f && dg2 < nearestVert) {
                nearestVert = dg2;
                vx1 = obj.x; vy1 = ob; vx2 = obj.x; vy2 = activity.groundY;
                vLabel = activity.toSurdDisplay(dg2) + "m";
            }
        }

        if (nearestVert < Float.MAX_VALUE)
            drawMeasureLine(canvas, vx1, vy1, vx2, vy2, vLabel, true, dp, dtp);
        if (nearestHoriz < Float.MAX_VALUE)
            drawMeasureLine(canvas, hx1, hy1, hx2, hy2, hLabel, false, dp, dtp);
    }

    void drawMeasureLine(Canvas c, float x1, float y1, float x2, float y2, String txt, boolean isVert, Paint lp, Paint tp) {
        c.drawLine(x1, y1, x2, y2, lp);
        float perp = 7f;
        if (isVert) {
            c.drawLine(x1-perp, y1, x1+perp, y1, lp);
            c.drawLine(x2-perp, y2, x2+perp, y2, lp);
            float dx2 = x2-x1, dy2 = y2-y1, len = magnitude(dx2, dy2);
            if (len>1) { float f=8/len; c.drawLine(x1,y1,x1+dx2*f+perp*0.4f*(dy2/len),y1+dy2*f,lp); c.drawLine(x1,y1,x1+dx2*f-perp*0.4f*(dy2/len),y1+dy2*f,lp); c.drawLine(x2,y2,x2-dx2*f+perp*0.4f*(dy2/len),y2-dy2*f,lp); c.drawLine(x2,y2,x2-dx2*f-perp*0.4f*(dy2/len),y2-dy2*f,lp); }
            c.drawText(txt, (x1+x2)/2+18, (y1+y2)/2+5, tp);
        } else {
            c.drawLine(x1, y1-perp, x1, y1+perp, lp);
            c.drawLine(x2, y2-perp, x2, y2+perp, lp);
            float dx2=x2-x1,dy2=y2-y1,len=magnitude(dx2, dy2);
            if (len>1) { float f=8/len; c.drawLine(x1,y1,x1+dx2*f,y1+dy2*f+perp*0.4f*(dx2/len),lp); c.drawLine(x1,y1,x1+dx2*f,y1+dy2*f-perp*0.4f*(dx2/len),lp); c.drawLine(x2,y2,x2-dx2*f,y2-dy2*f+perp*0.4f*(dx2/len),lp); c.drawLine(x2,y2,x2-dx2*f,y2-dy2*f-perp*0.4f*(dx2/len),lp); }
            c.drawText(txt, (x1+x2)/2, (y1+y2)/2-10, tp);
        }
    }

    void drawPointDistances(Canvas canvas, float sx, float sy, float cx, float cy) {
        float ppm = activity.pixelsPerMeter;
        Paint dp = PhysObj.mp(Color.BLACK, Paint.Style.STROKE, 1.5f);
        Paint dtp = PhysObj.mp(Color.BLACK, Paint.Style.FILL, 0);
        dtp.setTextSize(20f);
        dtp.setTextAlign(Paint.Align.CENTER);

        float dCur = magnitude(cx - sx, cy - sy) / ppm;
        canvas.drawText(activity.toSurdDisplay(dCur) + "m", cx, cy - 24, dtp);

        float dLeft = (sx - activity.wallLeftX) / ppm;
        float dRight = (activity.wallRightX - sx) / ppm;
        if (dLeft <= dRight) {
            drawMeasureLine(canvas, activity.wallLeftX, sy, sx, sy,
                    activity.toSurdDisplay(dLeft) + "m", false, dp, dtp);
        } else {
            drawMeasureLine(canvas, sx, sy, activity.wallRightX, sy,
                    activity.toSurdDisplay(dRight) + "m", false, dp, dtp);
        }

        float dGround = (activity.groundY - sy) / ppm;
        float dTop = sy / ppm;
        if (dGround <= dTop) {
            drawMeasureLine(canvas, sx, sy, sx, activity.groundY,
                    activity.toSurdDisplay(dGround) + "m", true, dp, dtp);
        } else {
            drawMeasureLine(canvas, sx, 0, sx, sy,
                    activity.toSurdDisplay(dTop) + "m", true, dp, dtp);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        float x = ev.getX(), y = ev.getY();
        int action = ev.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
        if (activity.symbolMode && activity.symNumbering) {
            PhysObj numHit = activity.hitTest(x, y);
            if (numHit != null && activity.symNumberable(numHit) && numHit.symOrdinal <= 0) {
                numHit.symOrdinal = activity.symNextOrdinal++;
                activity.updateSymNumberHint();
            } else if (numHit == null) {
                activity.symAutoNumberRest();
            }
            invalidate();
            return true;
        }
        if (activity.isSelectingPulleyRamp) {
            PhysObj hit = activity.hitTest(x, y);
            if (hit != null && hit.type == MainActivity.TYPE_RAMP_STRAIGHT) {
                PhysObj pulley = activity.selectingPulleyObj;
                pulley.pulleyBoundRamp = hit;
                pulley.fixed = true;
                float topCornerX = hit.isHorizontal ? hit.x + hit.w / 2 : hit.x - hit.w / 2;
                float topCornerY = hit.y - hit.h / 2;
            pulley.x = topCornerX; pulley.y = topCornerY;
                pulley.initialX = pulley.x; pulley.initialY = pulley.y;
            } else if (hit == null) {
                if (activity.selectingPulleyObj != null) {
                     activity.selectingPulleyObj.pulleyBoundRamp = null;
                }
            }
            activity.isSelectingPulleyRamp = false;
            activity.selectingPulleyObj = null;
            invalidate();
            return true;
        }
            if (activity.isSelectingConditionTarget) {
                PhysObj hit = activity.hitTest(x, y);
                MainActivity.ConditionObjCallback cb = activity.conditionObjCallback;
                AlertDialog parentDlg = activity.activeConditionParentDialog;
                if (hit != null && hit == activity.conditionHighlightObj) {
                    activity.conditionHighlightObj = null;
                    activity.isSelectingConditionTarget = false;
                    activity.conditionObjCallback = null;
                    activity.activeConditionParentDialog = null;
                    if (parentDlg != null) parentDlg.show();
                    if (cb != null) cb.onSelected(null);
                } else if (hit != null) {
                    activity.conditionHighlightObj = hit;
                    activity.isSelectingConditionTarget = false;
                    activity.conditionObjCallback = null;
                    activity.activeConditionParentDialog = null;
                    if (parentDlg != null) parentDlg.show();
                    if (cb != null) cb.onSelected(hit);
                } else {
                    activity.conditionHighlightObj = null;
                    activity.isSelectingConditionTarget = false;
                    activity.conditionObjCallback = null;
                    activity.activeConditionParentDialog = null;
                    if (parentDlg != null) parentDlg.show();
                    if (cb != null) cb.onSelected(null);
                }
                activity.physView.invalidate();
                return true;
            }
            if (activity.springRotatePendingObj != null) {
                PhysObj hit = activity.hitTest(x, y);
                if (hit == activity.springRotatePendingObj) {
                    startSpringRotate(hit, x, y);
                }
                activity.springRotatePendingObj = null;
                invalidate();
                return true;
            }
            if (activity.isSelectingConnect) {
                PhysObj hit = activity.hitTest(x, y);
                CeilingSegment hitCeiling = activity.ceilingHitTest(x, y);
                boolean hitIsOwnRing = hit != null && hit.type == MainActivity.TYPE_RING
                        && hit.ringHostRod == activity.connectTargetSpring;
                if (hit != null && hit != activity.connectTargetSpring && !hitIsOwnRing &&
                        hit.type != MainActivity.TYPE_SPRING && hit.type != MainActivity.TYPE_ROPE
                        && hit.type != MainActivity.TYPE_ROD) {
                    if (activity.connectHighlightObj == null) {
                        activity.connectHighlightObj = hit;
                    } else if (hit == activity.connectHighlightObj) {
                        if (activity.connectIsEndA) {
                            activity.connectTargetSpring.connectedObjA = hit;
                        } else {
                            activity.connectTargetSpring.connectedObjB = hit;
                        }
                        activity.connectHighlightObj = null;
                        activity.isSelectingConnect = false;
                    } else {
                        activity.connectHighlightObj = hit;
                    }
                } else if (hitCeiling != null) {
                    float ceilMinX = Math.min(hitCeiling.x1, hitCeiling.x2);
                    float ceilMaxX = Math.max(hitCeiling.x1, hitCeiling.x2);
                    activity.connectTargetSpring.ceilingAnchorX = Math.max(ceilMinX, Math.min(x, ceilMaxX));
                    activity.connectTargetSpring.connectedToCeiling = true;
                    activity.connectTargetSpring.ceilingSegmentId =
                            activity.ceilingSegments.indexOf(hitCeiling);
                    if (activity.connectIsEndA) activity.connectTargetSpring.connectedObjA = null;
                    else activity.connectTargetSpring.connectedObjB = null;
                    activity.connectHighlightObj = null;
                    activity.isSelectingConnect = false;
                } else if (Math.abs(y - activity.groundY) < 20) {
                    if (!activity.connectTargetSpring.isHorizontal) {
                        activity.connectTargetSpring.connectedToGround = true;
                        activity.connectTargetSpring.groundAnchorX = x;
                        if (activity.connectIsEndA) activity.connectTargetSpring.connectedObjA = null;
                        else activity.connectTargetSpring.connectedObjB = null;
                        activity.connectHighlightObj = null;
                        activity.isSelectingConnect = false;
                    }
                } else if (Math.abs(x - activity.wallLeftX) < 20 && y >= 0 && y <= activity.groundY) {
                    if (activity.connectTargetSpring.isHorizontal) {
                        activity.connectTargetSpring.connectedToWallLeft = true;
                        activity.connectTargetSpring.wallAnchorY = y;
                        if (activity.connectIsEndA) {
                            activity.connectTargetSpring.connectedObjA = null;
                            activity.connectTargetSpring.connectedToCeiling = false;
                            activity.connectTargetSpring.connectedToGround = false;
                        } else {
                            activity.connectTargetSpring.connectedObjB = null;
                        }
                        activity.connectHighlightObj = null;
                        activity.isSelectingConnect = false;
                        invalidate();
                        return true;
                    }
                } else if (Math.abs(x - activity.wallRightX) < 20 && y >= 0 && y <= activity.groundY) {
                    if (activity.connectTargetSpring.isHorizontal) {
                        activity.connectTargetSpring.connectedToWallRight = true;
                        activity.connectTargetSpring.wallAnchorY = y;
                        if (activity.connectIsEndA) {
                            activity.connectTargetSpring.connectedObjA = null;
                            activity.connectTargetSpring.connectedToCeiling = false;
                            activity.connectTargetSpring.connectedToGround = false;
                        } else {
                            activity.connectTargetSpring.connectedObjB = null;
                        }
                        activity.connectHighlightObj = null;
                        activity.isSelectingConnect = false;
                        invalidate();
                        return true;
                    }
                } else if (hit == null) {
                    activity.connectHighlightObj = null;
                    activity.isSelectingConnect = false;
                }
                if (!activity.isSelectingConnect && activity.connectTargetSpring != null) {
                    activity.refreshConstraintPose(activity.connectTargetSpring);
                }
                invalidate();
                return true;
            }

            activity.longPressTriggered = false;
            activity.longPressStartX = x;
            activity.longPressStartY = y;

            if (activity.currentMode == MainActivity.MODE_ERASE) {
                eraseStartX = x;
                eraseStartY = y;
                eraseCurrentX = x;
                eraseCurrentY = y;
                handleEraseDown(x, y);
                return true;
            } else if (activity.currentMode == MainActivity.MODE_DRAW) {
                drawStartX = x;
                drawStartY = y;
                drawCurrentX = x;
                drawCurrentY = y;
                return true;
            } else if (activity.currentMode == MainActivity.MODE_CUT) {
                activity.isCutting = true;
                activity.cutStartX = x;
                activity.cutStartY = y;
                activity.cutEndX = x;
                activity.cutEndY = y;
                activity.cuttingObj = null;
                invalidate();
                return true;
            } else if (activity.currentMode == MainActivity.MODE_VELOCITY) {
                PhysObj hit = activity.hitTest(x, y);
                if (hit != null && !activity.isConstraintBody(hit)) {
                    activity.selectedObj = hit;
                    activity.isSettingVelocity = true;
                    activity.dragStartX = x;
                    activity.dragStartY = y;
                    activity.velMagnitude = activity.symbolMode ? activity.symLevelVelocity(hit) : 0;
                    activity.velAngle = 0;
                }
            } else if (activity.currentMode == MainActivity.MODE_ACCEL) {
                PhysObj hit = activity.hitTest(x, y);
                if (hit != null && !activity.isConstraintBody(hit)) {
                    activity.selectedObj = hit;
                    activity.isSettingForce = true;
                    activity.dragStartX = x;
                    activity.dragStartY = y;
                    activity.forceMagnitude = activity.symbolMode ? activity.symLevelForce(hit) : 0;
                    activity.forceAngle = 0;
                }
            } else if (activity.currentMode == MainActivity.MODE_NORMAL) {
                PhysObj hit = activity.hitTest(x, y);
                if (hit != null) {
                    activity.selectedObj = hit;
                    activity.dragging = true;
                    activity.dragOffX = x - hit.x;
                    activity.dragOffY = y - hit.y;
                } else {
                    activity.selectedObj = null;
                    if (activity.currentSettingsSidePanel != null) {
                        activity.dismissSettingsPanel();
                    }
                }
            }
            invalidate();
            return true;

        } else if (action == MotionEvent.ACTION_MOVE) {
            if (activity.isSelectingConnect) return true;

            if (!activity.longPressTriggered && longPressRunnable != null) {
                float dx = x - activity.longPressStartX, dy = y - activity.longPressStartY;
                if (dx * dx + dy * dy > 15 * 15) {
                    longPressHandler.removeCallbacks(longPressRunnable);
                }
            }

            if (activity.currentMode == MainActivity.MODE_ERASE) {
                eraseCurrentX = x;
                eraseCurrentY = y;
                handleEraseMove(x, y);
                invalidate();
                return true;
            }

            if (activity.rotatingSpring && activity.rotatingObj != null) {
                handleSpringRotate(x, y);
                invalidate();
                return true;
            }

            if (activity.currentMode == MainActivity.MODE_DRAW && drawStartX >= 0) {
                drawCurrentX = x;
                drawCurrentY = drawStartY;
                invalidate();
                return true;
            } else if (activity.currentMode == MainActivity.MODE_CUT && activity.isCutting) {
                activity.cutEndX = x;
                activity.cutEndY = y;
                activity.cuttingObj = activity.findCutTarget(
                        activity.cutStartX, activity.cutStartY, activity.cutEndX, activity.cutEndY);
                invalidate();
                return true;
            } else if (activity.currentMode == MainActivity.MODE_VELOCITY &&
                    activity.isSettingVelocity && activity.selectedObj != null) {
                float dx = x - activity.dragStartX, dy = y - activity.dragStartY;
                if (activity.symbolMode) {
                    activity.velMagnitude = activity.symLevelVelocity(activity.selectedObj);
                    if (magnitude(dx, dy) > 3f) {
                        activity.velAngle = (float) (Math.atan2(-dy, dx) * 180 / Math.PI);
                        if (activity.velAngle < 0) activity.velAngle += 360;
                    }
                } else {
                    activity.velMagnitude = magnitude(dx, dy) / activity.pixelsPerMeter * 2;
                    activity.velAngle = (float) (Math.atan2(-dy, dx) * 180 / Math.PI);
                    if (activity.velAngle < 0) activity.velAngle += 360;
                    activity.velMagnitude = Math.min(activity.velMagnitude, 15);
                }
                invalidate();
            } else if (activity.currentMode == MainActivity.MODE_ACCEL &&
                    activity.isSettingForce && activity.selectedObj != null) {
                float dx = x - activity.dragStartX, dy = y - activity.dragStartY;
                if (activity.symbolMode) {
                    activity.forceMagnitude = activity.symLevelForce(activity.selectedObj);
                    if (magnitude(dx, dy) > 3f) {
                        activity.forceAngle = (float) (Math.atan2(-dy, dx) * 180 / Math.PI);
                        if (activity.forceAngle < 0) activity.forceAngle += 360;
                    }
                } else {
                    activity.forceMagnitude = magnitude(dx, dy) * 20f / activity.pixelsPerMeter;
                    activity.forceAngle = (float) (Math.atan2(-dy, dx) * 180 / Math.PI);
                    if (activity.forceAngle < 0) activity.forceAngle += 360;
                    activity.forceMagnitude = Math.min(activity.forceMagnitude, 50);
                }
                invalidate();
            } else if (activity.dragging && activity.selectedObj != null && !activity.longPressTriggered) {
                activity.selectedObj.x = x - activity.dragOffX;
                activity.selectedObj.y = y - activity.dragOffY;
                activity.selectedObj.physX = activity.selectedObj.x;
                activity.selectedObj.physY = activity.selectedObj.y;
                invalidate();
            } else if (activity.isWrapDrawing && activity.wrapDrawingRope != null) {
                ArrayList<float[]> pts = activity.wrapDrawingRope.ropeWrapPoints;
                if (pts.isEmpty()) {
                    pts.add(new float[]{x, y});
                    activity.wrapTotalLength = 0f;
                } else {
                    float[] last = pts.get(pts.size() - 1);
                    float segLen = (float) Math.sqrt((x - last[0]) * (x - last[0]) + (y - last[1]) * (y - last[1]));
                    float maxLen = activity.wrapDrawingRope.ropeLen;
                    if (activity.wrapTotalLength + segLen <= maxLen) {
                        pts.add(new float[]{x, y});
                        activity.wrapTotalLength += segLen;
                    }
                }
                invalidate();
                return true;
            }
            return true;

        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (longPressRunnable != null) longPressHandler.removeCallbacks(longPressRunnable);
            activity.rotatingSpring = false;
            activity.rotatingObj = null;
            if (activity.currentMode == MainActivity.MODE_ERASE) {
                eraseStartX = -1;
                eraseStartY = -1;
                invalidate();
                return true;
            }
            if (activity.isSelectingConnect) {
                return true;
            }
            if (activity.currentMode == MainActivity.MODE_DRAW && drawStartX >= 0) {
                if (Math.abs(drawCurrentX - drawStartX) > 20) {
                    activity.createCeilingSegment(drawStartX, drawStartY, drawCurrentX, drawStartY);
                }
                drawStartX = -1;
                drawStartY = -1;
                invalidate();
                return true;
            } else if (activity.currentMode == MainActivity.MODE_CUT && activity.isCutting) {
                activity.cutEndX = x;
                activity.cutEndY = y;
                if (activity.fineCutCheck != null && activity.fineCutCheck.isChecked()) {
                    PhysObj target = activity.findCutTarget(
                            activity.cutStartX, activity.cutStartY, activity.cutEndX, activity.cutEndY);
                    activity.isCutting = false;
                    activity.cuttingObj = null;
                    if (target != null) {
                        if (target.type == MainActivity.TYPE_ROD) {
                            activity.performCut();
                        } else {
                            activity.showFineCutDialog(target);
                        }
                    }
                    invalidate();
                    return true;
                }
                activity.performCut();
                activity.isCutting = false;
                activity.cuttingObj = null;
                invalidate();
                return true;
            } else if (activity.currentMode == MainActivity.MODE_VELOCITY &&
                    activity.isSettingVelocity && activity.selectedObj != null) {
                final float finalMag = activity.velMagnitude;
                final float finalAng = activity.velAngle;
                final PhysObj fObj = activity.selectedObj;
                activity.isSettingVelocity = false;
                activity.selectedObj = null;
                final boolean symMode = activity.symbolMode;
                final float roundedMag = symMode
                        ? activity.symLevelVelocity(fObj)
                        : Math.round(finalMag * 10) / 10.0f;
                final float roundedAng = (float) Math.round(finalAng);
                LinearLayout layout = new LinearLayout(activity);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(40, 20, 40, 10);
                final EditText etMag = new EditText(activity);
                final String v0Base = "v₀";
                final String v0Sym = symMode
                        ? activity.symEffectiveSymbol(fObj, v0Base) : v0Base;
                if (symMode) {
                    TextView tvSym = new TextView(activity);
                    tvSym.setText("大小(符号): " + v0Sym);
                    tvSym.setTextSize(12);
                    tvSym.setTextColor(Color.BLACK);
                    layout.addView(tvSym);
                    activity.addSymKnownRow(layout, fObj, v0Base, v0Sym);
                    activity.addSliderLevel(layout, v0Sym + " 模糊量", fObj.symV0Level,
                            new MainActivity.LevelSetter() {
                                public void set(int level) {
                                    fObj.symV0Level = level;
                                }
                            });
                } else {
                    TextView tvMag = new TextView(activity);
                    tvMag.setText("大小 (m/s):");
                    etMag.setText(activity.toSurdDisplay(roundedMag));
                    etMag.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                    activity.setupMathKeyboard(etMag);
                    layout.addView(tvMag);
                    layout.addView(etMag);
                }
                TextView tvAng = new TextView(activity);
                tvAng.setText("方向 (°):");
                final EditText etAng = new EditText(activity);
                etAng.setText(activity.toSurdDisplay((int) roundedAng));
                etAng.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                activity.setupMathKeyboard(etAng);
                layout.addView(tvAng);
                layout.addView(etAng);
                AlertDialog.Builder b = new AlertDialog.Builder(activity);
                b.setTitle("设置初速度");
                b.setView(layout);
                b.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        if (symMode && !activity.symPropKnown(fObj, v0Base)) {
                            android.widget.Toast.makeText(activity,
                                    MainActivity.SYM_INSUFFICIENT,
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                        double mag = symMode
                                ? activity.symLevelVelocity(fObj)
                                : activity.parseExprD(etMag.getText().toString(), roundedMag);
                        double ang = activity.parseExprD(etAng.getText().toString(), roundedAng);
                        double rad = ang * Math.PI / 180.0;
                        double vpx = mag * Math.cos(rad) * activity.pixelsPerMeter;
                        double vpy = -mag * Math.sin(rad) * activity.pixelsPerMeter;
                        if (activity.isFreeEndConstraint(fObj)) {
                            fObj.freeEndInitVx = (float) vpx;
                            fObj.freeEndInitVy = (float) vpy;
                            fObj.freeEndHasInitVel = true;
                            fObj.freeEndVx = (float) vpx;
                            fObj.freeEndVy = (float) vpy;
                        } else if (activity.isConstraintBody(fObj)) {
                            fObj.freeEndHasInitVel = false;
                        } else {
                            fObj.vx = (float) vpx;
                            fObj.vy = (float) vpy;
                            fObj.initialVx = vpx;
                            fObj.initialVy = vpy;
                            fObj.physVx = vpx;
                            fObj.physVy = vpy;
                        }
                        fObj.symV0Set = true;
                        fObj.symFrozenExpr.clear();
                        fObj.symFrozenTime.clear();
                    }
                });
                b.setNegativeButton("取消", null);
                b.show();
            } else if (activity.currentMode == MainActivity.MODE_ACCEL &&
                    activity.isSettingForce && activity.selectedObj != null) {
                final float finalMag = activity.forceMagnitude;
                final float finalAng = activity.forceAngle;
                final PhysObj fObj = activity.selectedObj;
                activity.isSettingForce = false;
                activity.selectedObj = null;
                final boolean symMode = activity.symbolMode;
                final float roundedMag = symMode
                        ? activity.symLevelForce(fObj)
                        : Math.round(finalMag * 10) / 10.0f;
                final float roundedAng = (float) Math.round(finalAng);

                LinearLayout layout = new LinearLayout(activity);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(40, 20, 40, 10);

                final EditText etMag = new EditText(activity);
                final String fBase = "F";
                final String fSym = symMode
                        ? activity.symEffectiveSymbol(fObj, fBase) : fBase;
                if (symMode) {
                    TextView tvSym = new TextView(activity);
                    tvSym.setText("大小(符号): " + fSym);
                    tvSym.setTextSize(12);
                    tvSym.setTextColor(Color.BLACK);
                    layout.addView(tvSym);
                    activity.addSymKnownRow(layout, fObj, fBase, fSym);
                    activity.addSliderLevel(layout, fSym + " 模糊量", fObj.symForceLevel,
                            new MainActivity.LevelSetter() {
                                public void set(int level) {
                                    fObj.symForceLevel = level;
                                }
                            });
                } else {
                    TextView tvMag = new TextView(activity);
                    tvMag.setText("大小 (N):");
                    etMag.setText(activity.toSurdDisplay(roundedMag));
                    etMag.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                    activity.setupMathKeyboard(etMag);
                    layout.addView(tvMag);
                    layout.addView(etMag);
                }

                TextView tvAng = new TextView(activity);
                tvAng.setText("方向 (°):");
                final EditText etAng = new EditText(activity);
                etAng.setText(activity.toSurdDisplay((int) roundedAng));
                etAng.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                activity.setupMathKeyboard(etAng);

                final LogicCondition[] forceCond = {null};
                final TextView tvCondDisplay = new TextView(activity);
                tvCondDisplay.setText("受该力直到: 永久");
                tvCondDisplay.setTextSize(11);
                tvCondDisplay.setPadding(0, 4, 0, 0);
                tvCondDisplay.setTextColor(Color.BLACK);
                final AlertDialog[] forceDialogRef = {null};
                Button btnSetForceCond = new Button(activity);
                btnSetForceCond.setText("展开选择逻辑块...");
                btnSetForceCond.setTextSize(10);
                btnSetForceCond.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.activeConditionParentDialog = forceDialogRef[0];
                        activity.showConditionDialog(new MainActivity.LogicConditionCallback() {
                            @Override
                            public void onCondition(LogicCondition cond) {
                                forceCond[0] = cond;
                                tvCondDisplay.setText("受该力直到: " + (cond != null ? cond.describe() : "永久"));
                                if (forceDialogRef[0] != null) forceDialogRef[0].show();
                            }
                        });
                    }
                });

                layout.addView(tvAng);
                layout.addView(etAng);
                layout.addView(tvCondDisplay);
                layout.addView(btnSetForceCond);

                ScrollView scrollView = new ScrollView(activity);
                scrollView.addView(layout);

                AlertDialog.Builder b = new AlertDialog.Builder(activity);
                b.setTitle("施加力");
                b.setView(scrollView);
                b.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        if (symMode && !activity.symPropKnown(fObj, fBase)) {
                            android.widget.Toast.makeText(activity,
                                    MainActivity.SYM_INSUFFICIENT,
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                        double mag = symMode
                                ? activity.symLevelForce(fObj)
                                : activity.parseExprD(etMag.getText().toString(), roundedMag);
                        double ang = activity.parseExprD(etAng.getText().toString(), roundedAng);
                        double rad = ang * Math.PI / 180.0;
                        double fpx = mag * Math.cos(rad);
                        double fpy = -mag * Math.sin(rad);
                        if (activity.isFreeEndConstraint(fObj)) {
                            fObj.freeAppliedForceX = (float) fpx;
                            fObj.freeAppliedForceY = (float) fpy;
                        } else if (activity.isConstraintBody(fObj)) {
                            fObj.freeAppliedForceX = 0;
                            fObj.freeAppliedForceY = 0;
                        } else {
                            fObj.appliedForceX = fpx;
                            fObj.appliedForceY = fpy;
                            fObj.forceCondition = forceCond[0];
                            if (fObj.forceCondition != null) fObj.forceCondition.reset();
                            fObj.forceDuration = -1f;
                            fObj.forceTimer = 0f;
                        }
                    }
                });
                b.setNegativeButton("取消", null);
                forceDialogRef[0] = b.show();
            } else if (activity.isWrapDrawing && activity.wrapDrawingRope != null) {
                activity.isWrapDrawing = false;
                PhysObj rope = activity.wrapDrawingRope;
                if (!rope.ropeWrapPoints.isEmpty()) {
                    rope.ropeWrapped = true;
                    rope.ropeParticlesInit = false;
                    activity.detectWrapPulley(rope);
                }
                activity.wrapDrawingRope = null;
                invalidate();
                return true;
            } else if (activity.dragging && activity.selectedObj != null && !activity.longPressTriggered) {
                float tdx = x - activity.longPressStartX;
                float tdy = y - activity.longPressStartY;
                if (tdx * tdx + tdy * tdy <= 15 * 15) {
                    activity.showSettingsSidePanel(activity.selectedObj);
                }
            }
            activity.dragging = false;
            activity.longPressTriggered = false;
            invalidate();
            return true;
        }
        return super.onTouchEvent(ev);
    }

    void startSpringRotate(PhysObj obj, float touchX, float touchY) {
        if (obj.angleFixed) return;
        float[] eps = activity.getSpringEndpoints(obj);
        float ex1 = eps[0], ey1 = eps[1], ex2 = eps[2], ey2 = eps[3];
        float d1 = (touchX - ex1) * (touchX - ex1) + (touchY - ey1) * (touchY - ey1);
        float d2 = (touchX - ex2) * (touchX - ex2) + (touchY - ey2) * (touchY - ey2);
        boolean pivotIsEp1 = d1 > d2;
        if (pivotIsEp1) {
            activity.rotatingPivotX = ex1; activity.rotatingPivotY = ey1;
        } else {
            activity.rotatingPivotX = ex2; activity.rotatingPivotY = ey2;
        }
        PhysObj objAtEp1 = null, objAtEp2 = null;
        if (obj.connectedObjA != null && obj.connectedObjB != null) {
            objAtEp1 = obj.connectedObjA;
            objAtEp2 = obj.connectedObjB;
        } else if (obj.connectedObjA != null) {
            objAtEp2 = obj.connectedObjA;
        } else if (obj.connectedObjB != null) {
            objAtEp2 = obj.connectedObjB;
        }
        PhysObj followObj = null;
        if (pivotIsEp1) {
            if (objAtEp2 != null && !objAtEp2.fixed) followObj = objAtEp2;
        } else {
            if (objAtEp1 != null && !objAtEp1.fixed) followObj = objAtEp1;
        }
        activity.rotatingFollowObj = followObj;
        activity.rotatingObj = obj;
        activity.rotatingSpring = true;
    }

    void handleSpringRotate(float x, float y) {
        PhysObj obj = activity.rotatingObj;
        if (obj == null || obj.angleFixed) return;
        float px = activity.rotatingPivotX, py = activity.rotatingPivotY;
        float angle = (float) Math.atan2(y - py, x - px);
        float len = obj.type == MainActivity.TYPE_SPRING ? obj.naturalLen
                : (obj.type == MainActivity.TYPE_ROD ? obj.rodLen : obj.ropeLen);
        obj.x = px + (float) Math.cos(angle) * len / 2;
        obj.y = py + (float) Math.sin(angle) * len / 2;
        obj.rotation = angle;
        obj.isHorizontal = true;
        obj.w = len;
        obj.h = obj.type == MainActivity.TYPE_SPRING ? 20 : (obj.type == MainActivity.TYPE_ROD ? 0f : 10);
        if (activity.rotatingFollowObj != null && !activity.rotatingFollowObj.fixed) {
            float freeX = px + (float) Math.cos(angle) * len;
            float freeY = py + (float) Math.sin(angle) * len;
            activity.rotatingFollowObj.x = freeX;
            activity.rotatingFollowObj.y = freeY;
        }
    }

    void handleEraseDown(float x, float y) {
        CeilingSegment hit = activity.ceilingHitTest(x, y);
        if (hit != null) {
            activity.ceilingSegments.remove(hit);
            int segIdx = activity.ceilingSegments.indexOf(hit);
            for (PhysObj o : activity.objects) {
                if (o.ceilingSegmentId == segIdx) {
                    o.connectedToCeiling = false;
                    o.ceilingSegmentId = -1;
                }
            }
            invalidate();
            return;
        }
        if (Math.abs(y - activity.groundY) < 24) {
            activity.groundEraseRanges.add(new float[]{x - 20, x + 20});
        }
        invalidate();
    }

    void handleEraseMove(float x, float y) {
        if (Math.abs(y - activity.groundY) < 24 && !activity.groundEraseRanges.isEmpty()) {
            float[] last = activity.groundEraseRanges.get(activity.groundEraseRanges.size() - 1);
            last[0] = Math.min(last[0], x - 8);
            last[1] = Math.max(last[1], x + 8);
        }
    }
}

class RightPanelView extends View {
    static final int COLS = 3, ROWS = 4;
    float cellW, cellH;
    int[] types = {
            MainActivity.TYPE_BLOCK,
            MainActivity.TYPE_BOARD,
            MainActivity.TYPE_SPRING,
            MainActivity.TYPE_ROPE,
            MainActivity.TYPE_RAMP_STRAIGHT,
            MainActivity.TYPE_RAMP_ARC,
            MainActivity.TYPE_BALL,
            MainActivity.TYPE_CONTAINER,
            MainActivity.TYPE_PULLEY,
            MainActivity.TYPE_ROD
    };
    String[] names = {"木块", "木板", "弹簧", "绳子", "直斜面", "弧形斜面", "小球", "容器", "滑轮", "杆"};
    Paint iconB, iconF, labelP, cellB;
    MainActivity activity;

    public RightPanelView(Context ctx) {
        super(ctx);
        this.activity = (MainActivity) ctx;
        iconB = PhysObj.mp(Color.BLACK, Paint.Style.STROKE, 1.5f);
        iconF = PhysObj.mp(Color.WHITE, Paint.Style.FILL, 0f);
        labelP = PhysObj.mp(Color.BLACK, Paint.Style.FILL, 0f);
        labelP.setTextSize(14f);
        labelP.setTextAlign(Paint.Align.CENTER);
        cellB = PhysObj.mp(0xFF999999, Paint.Style.STROKE, 1f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        cellW = getWidth() / (float) COLS;
        cellH = getHeight() / (float) ROWS;

        for (int i = 0; i < types.length; i++) {
            int col = i % COLS;
            int row = i / COLS;
            float cx = col * cellW + cellW / 2;
            float cy = row * cellH + cellH / 2;

            canvas.drawRect(col * cellW + 3, row * cellH + 3,
                    (col + 1) * cellW - 3, (row + 1) * cellH - 3, iconF);
            canvas.drawRect(col * cellW + 3, row * cellH + 3,
                    (col + 1) * cellW - 3, (row + 1) * cellH - 3, cellB);

            drawMini(canvas, types[i], cx, cy - 10);
            canvas.drawText(names[i], cx, cy + 18, labelP);
        }
    }

    void drawMini(Canvas c, int type, float cx, float cy) {
        switch (type) {
            case MainActivity.TYPE_BLOCK:
                Block.drawMini(c, cx, cy, iconB, iconF);
                break;
            case MainActivity.TYPE_BOARD:
                Board.drawMini(c, cx, cy, iconB, iconF);
                break;
            case MainActivity.TYPE_SPRING:
                Spring.drawMini(c, cx, cy, iconB, iconF);
                break;
            case MainActivity.TYPE_ROPE:
                Rope.drawMini(c, cx, cy, iconB, iconF);
                break;
            case MainActivity.TYPE_RAMP_STRAIGHT:
                RampStraight.drawMini(c, cx, cy, iconB, iconF);
                break;
            case MainActivity.TYPE_RAMP_ARC:
                RampArc.drawMini(c, cx, cy, iconB, iconF);
                break;
            case MainActivity.TYPE_BALL:
                Ball.drawMini(c, cx, cy, iconB, iconF);
                break;
            case MainActivity.TYPE_CONTAINER:
                Container.drawMini(c, cx, cy, iconB, iconF);
                break;
            case MainActivity.TYPE_PULLEY:
                Pulley.drawMini(c, cx, cy, iconB, iconF);
                break;
            case MainActivity.TYPE_ROD:
                Rod.drawMini(c, cx, cy, iconB, iconF);
                break;
        }
    }

    int getTypeAt(float x, float y) {
        if (cellW <= 0) {
            cellW = getWidth() / (float) COLS;
            cellH = getHeight() / (float) ROWS;
        }
        int col = (int) (x / cellW);
        int row = (int) (y / cellH);
        int idx = row * COLS + col;
        return (idx >= 0 && idx < types.length) ? types[idx] : -1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        float x = ev.getX(), y = ev.getY();
        int action = ev.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            int type = getTypeAt(x, y);
            if (type >= 0) {
                if (type == MainActivity.TYPE_SPRING || type == MainActivity.TYPE_ROPE) {
                    activity.connectingType = type;
                    activity.isConnecting = true;
                }
                activity.dragFromPanelType = type;
                activity.draggingFromPanel = true;
                updateDragPos(ev);
                activity.physView.invalidate();
                return true;
            }
            return false;
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (activity.draggingFromPanel) {
                updateDragPos(ev);
                activity.physView.invalidate();
                return true;
            }
            return false;
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (activity.draggingFromPanel && activity.dragFromPanelType >= 0) {
                updateDragPos(ev);
                if (activity.panelDragX >= 0 && activity.panelDragX <= activity.physView.getWidth() &&
                        activity.panelDragY >= 0 && activity.panelDragY <= activity.physView.getHeight()) {

                    boolean isSpring = activity.dragFromPanelType == MainActivity.TYPE_SPRING;
                    boolean isRope = activity.dragFromPanelType == MainActivity.TYPE_ROPE;

                    if ((isSpring || isRope) && activity.isConnecting) {
                        PhysObj newObj;
                        if (isSpring) {
                            newObj = Spring.createObjAt(activity.panelDragX, activity.panelDragY,
                                    activity.pixelsPerMeter, true, activity.groundLength);
                        } else {
                            newObj = Rope.createObjAt(activity.panelDragX, activity.panelDragY,
                                    activity.pixelsPerMeter, true, activity.groundLength);
                        }
                        activity.objects.add(newObj);
                        activity.symOnObjectAdded(newObj);
                    } else {
                        PhysObj newObj = activity.createObjAt(activity.dragFromPanelType, activity.panelDragX, activity.panelDragY);
                        if (newObj != null) {
                            activity.objects.add(newObj);
                            activity.symOnObjectAdded(newObj);
                        }
                    }
                }
                activity.draggingFromPanel = false;
                activity.dragFromPanelType = -1;
                activity.isConnecting = false;
                activity.connectingObj = null;
                activity.physView.invalidate();
                return true;
            }
            return false;
        }
        return super.onTouchEvent(ev);
    }

    void updateDragPos(MotionEvent ev) {
        int[] loc = new int[2];
        getLocationOnScreen(loc);
        int[] pLoc = new int[2];
        activity.physView.getLocationOnScreen(pLoc);
        activity.panelDragX = ev.getX() + loc[0] - pLoc[0];
        activity.panelDragY = ev.getY() + loc[1] - pLoc[1];
    }
}

public class MainActivity extends Activity {
    static final int TYPE_BLOCK = 0, TYPE_BOARD = 1, TYPE_SPRING = 2, TYPE_ROPE = 3,
            TYPE_RAMP_STRAIGHT = 4, TYPE_RAMP_ARC = 5, TYPE_BALL = 6, TYPE_CONTAINER = 7,
            TYPE_PULLEY = 8, TYPE_ROD = 9, TYPE_RING = 10;
    static final int MODE_NORMAL = 0, MODE_VELOCITY = 1, MODE_ACCEL = 2,
            MODE_DRAW = 3, MODE_CUT = 4, MODE_ERASE = 5;
    static final float GRAVITY = 9.8f, GROUND_Y_RATIO = 0.85f;
    static final float RETRACT_SPEED = 280f;

    ArrayList<PhysObj> objects = new ArrayList<PhysObj>();
    ArrayList<CeilingSegment> ceilingSegments = new ArrayList<CeilingSegment>();
    ArrayList<SimulationState> simulationHistory = new ArrayList<>();
    float lastStateSaveTime = -1f;

    float groundY, wallLeftX = 0, wallRightX = 0;
    float groundLength = 10f;
    float pixelsPerMeter = 100f;
    float basePixelsPerMeter = 100f;

    float windSpeed = 0f;
    float windDirection = 0f;
    float airResistance = 0f;
    int decimalPlaces = 2;
    boolean constraintPostPass = false;
    boolean constraintPoseRefresh = false;
    boolean rodChainActive = false;
    ArrayList<PhysObj> rodChainLinks = new ArrayList<PhysObj>();
    ArrayList<PhysObj> rodChainEndA = new ArrayList<PhysObj>();
    ArrayList<PhysObj> rodChainEndB = new ArrayList<PhysObj>();

    PhysObj selectedObj = null;
    boolean dragging = false;
    float dragOffX, dragOffY;
    boolean simRunning = false;
    long lastTime = 0;
    float elapsedTime = 0f;
    float accumulator = 0f;
    static final float FIXED_DT = 1f / 60f;
    boolean leftPanelOpen = false, rightPanelOpen = false;
    int currentMode = MODE_NORMAL;
    float forceAngle = 0, forceMagnitude = 0;
    float velAngle = 0, velMagnitude = 0;
    boolean isCutting = false;
    float cutStartX, cutStartY, cutEndX, cutEndY;
    float cutMassPercent = 0f;
    boolean isSettingForce = false, isSettingVelocity = false;
    float dragStartX, dragStartY;
    long longPressStart = 0;
    boolean longPressTriggered = false;
    int dragFromPanelType = -1;
    boolean draggingFromPanel = false;
    float panelDragX, panelDragY;
    PhysObj connectingObj = null;
    boolean isConnecting = false;
    int connectingType = -1;
    PhysObj cuttingObj = null;

    boolean isSelectingConnect = false;
    PhysObj connectTargetSpring = null;
    boolean connectIsEndA = false;
    PhysObj connectHighlightObj = null;

    PhysicsView physView;
    FrameLayout canvasContainer;
    FrameLayout mainAreaView;
    RightPanelView rightPanelView;
    TextView timeLabel;
    SeekBar timeBar;
    Button playBtn, resetBtn, restartBtn;
    Button btnNormal, btnVelocity, btnAccel, btnDraw, btnCut, btnErase;
    TextView tvCutPercent;
    CheckBox fineCutCheck;
    boolean symbolMode = false;
    int symNextOrdinal = 1;
    boolean symNumbering = false;
    PhysObj symContextObj = null;
    TextView symNumberHint = null;
    ArrayList<SymVar> symVars = new ArrayList<SymVar>();
    HashMap<String, SymVar> symIndex = new HashMap<String, SymVar>();
    View currentSettingsSidePanel = null;
    PhysObj settingsPanelObj = null;
    boolean isWrapDrawing = false;
    PhysObj wrapDrawingRope = null;
    float wrapTotalLength = 0f;
    float longPressStartX, longPressStartY;
    ArrayList<float[]> groundEraseRanges = new ArrayList<>();
    boolean rotatingSpring = false;
    PhysObj rotatingObj = null;
    float rotatingPivotX, rotatingPivotY;
    PhysObj springRotatePendingObj = null;
    PhysObj rotatingFollowObj = null;
    boolean isSelectingPulleyRamp = false;
    PhysObj selectingPulleyObj = null;
    boolean isSelectingConditionTarget = false;
    ConditionObjCallback conditionObjCallback = null;
    PhysObj conditionHighlightObj = null;
    AlertDialog activeConditionParentDialog = null;

    private float magnitude(float dx, float dy) {
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    float distanceToSegment(float px, float py, float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float lenSq = dx * dx + dy * dy;
        float t = lenSq > 0.0001f ? ((px - x1) * dx + (py - y1) * dy) / lenSq : 0f;
        if (t < 0f) t = 0f;
        if (t > 1f) t = 1f;
        float cx = x1 + dx * t;
        float cy = y1 + dy * t;
        return magnitude(px - cx, py - cy);
    }

    private float halfWidth(PhysObj o) {
        return o.w / 2;
    }

    private float halfHeight(PhysObj o) {
        return o.h / 2;
    }

    private float calculateArcRadius(PhysObj arc) {
        float W = arc.w, H = arc.h < 1f ? 1f : arc.h;
        return (W * W + H * H) / (2f * H);
    }

    class ArcContact {
        float cx, cy, radius, theta, startTheta, endTheta;
        float sx, sy, radialX, radialY, normalX, normalY;
        float tangentX, tangentY, angle, radialDist;
        int clampSide = 0;
        boolean clamped;
    }

    private float normAngleNear(float theta, float start, float end) {
        float twoPi = 2f * (float) Math.PI;
        if (start - end > (float) Math.PI) {
            while (theta > start) theta -= twoPi;
            while (theta < end) theta += twoPi;
        } else {
            while (theta > start + (float) Math.PI) theta -= twoPi;
            while (theta < end - (float) Math.PI) theta += twoPi;
        }
        return theta;
    }

    private void fillArcContactAtTheta(PhysObj ramp, float theta, ArcContact out) {
        float W = Math.max(0f, ramp.w);
        float H = ramp.h < 1f ? 1f : ramp.h;
        float R;
        float cx;
        float cy;
        if (W <= 1f) {
            R = Math.max(20f, H / 2f);
            cx = 0f;
            cy = 0f;
        } else {
            R = (W * W + H * H) / (2f * H);
            cx = -W / 2f;
            cy = H / 2f - R;
        }
        float sx = cx + R * (float) Math.cos(theta);
        float sy = cy + R * (float) Math.sin(theta);
        float radialX = (sx - cx) / R;
        float radialY = (sy - cy) / R;
        float sign = ramp.isHorizontal ? 1f : -1f;
        out.cx = ramp.x + sign * cx;
        out.cy = ramp.y + cy;
        out.radius = R;
        out.theta = theta;
        out.sx = ramp.x + sign * sx;
        out.sy = ramp.y + sy;
        out.radialX = sign * radialX;
        out.radialY = radialY;
        out.normalX = -out.radialX;
        out.normalY = -out.radialY;
        out.tangentX = sign * radialY;
        out.tangentY = -radialX;
        out.angle = (float) Math.atan2(out.tangentY, out.tangentX);
    }

    private boolean getArcContact(PhysObj ramp, float wx, float wy, ArcContact out) {
        float W = Math.max(0f, ramp.w);
        float H = ramp.h < 1f ? 1f : ramp.h;
        float sign = ramp.isHorizontal ? 1f : -1f;
        float lx = sign * (wx - ramp.x);
        float ly = wy - ramp.y;
        float R;
        float cx;
        float cy;
        float start = (float) Math.PI / 2f;
        float end;
        if (W <= 1f) {
            R = Math.max(20f, H / 2f);
            cx = 0f;
            cy = 0f;
            end = start - Math.max((float) Math.PI, Math.min((float) (2.0 * Math.PI), ramp.arcAngle));
        } else {
            R = (W * W + H * H) / (2f * H);
            cx = -W / 2f;
            cy = H / 2f - R;
            end = (float) Math.atan2(-H / 2f - cy, W / 2f - cx);
        }
        float dx = lx - cx;
        float dy = ly - cy;
        float dist = magnitude(dx, dy);
        if (dist < 0.001f) return false;
        float theta = normAngleNear((float) Math.atan2(dy, dx), start, end);
        boolean clamped = false;
        int clampSide = 0;
        boolean closedArc = start - end >= 2f * (float) Math.PI - 0.01f;
        if (!closedArc) {
            if (theta > start) {
                theta = start;
                clamped = true;
                clampSide = 1;
            }
            if (theta < end) {
                theta = end;
                clamped = true;
                clampSide = -1;
            }
        }
        out.startTheta = start;
        out.endTheta = end;
        out.radialDist = dist;
        out.clamped = clamped;
        out.clampSide = clampSide;
        fillArcContactAtTheta(ramp, theta, out);
        out.radialDist = dist;
        return true;
    }

    private void separateObjects(PhysObj o1, PhysObj o2, float overlap, float nx, float ny) {
        float totalMass = o1.mass + o2.mass;
        float ratio1 = o2.mass / totalMass;
        float ratio2 = o1.mass / totalMass;

        if (!o1.fixed) {
            o1.x -= overlap * nx * ratio1;
            o1.y -= overlap * ny * ratio1;
        }
        if (!o2.fixed) {
            o2.x += overlap * nx * ratio2;
            o2.y += overlap * ny * ratio2;
        }
    }

    interface FloatSetter {
        void set(float v);
    }

    interface LevelSetter {
        void set(int level);
    }

    interface LogicConditionCallback {
        void onCondition(LogicCondition cond);
    }

    interface ConditionObjCallback {
        void onSelected(PhysObj obj);
    }

    interface ConditionSetter {
        void set(LogicCondition cond);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mainAreaView = (FrameLayout) findViewById(R.id.mainArea);
        canvasContainer = (FrameLayout) findViewById(R.id.canvasContainer);
        physView = new PhysicsView(this);
        canvasContainer.addView(physView);

        fineCutCheck = new CheckBox(this);
        fineCutCheck.setText("精细切割");
        fineCutCheck.setTextSize(8);
        fineCutCheck.setTextColor(Color.BLACK);
        fineCutCheck.setScaleX(0.85f);
        fineCutCheck.setScaleY(0.85f);
        try {
            fineCutCheck.setButtonTintList(android.content.res.ColorStateList.valueOf(0xFF444444));
        } catch (Throwable t) {
        }
        fineCutCheck.setVisibility(View.GONE);
        FrameLayout.LayoutParams fcLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.LEFT);
        fcLp.leftMargin = 8;
        fcLp.topMargin = 8;
        canvasContainer.addView(fineCutCheck, fcLp);

        initSymVars();
        final CheckBox symbolCheck = new CheckBox(this);
        symbolCheck.setText("符号模式");
        symbolCheck.setTextSize(8);
        symbolCheck.setTextColor(Color.BLACK);
        symbolCheck.setScaleX(0.85f);
        symbolCheck.setScaleY(0.85f);
        try {
            symbolCheck.setButtonTintList(android.content.res.ColorStateList.valueOf(0xFF444444));
        } catch (Throwable t) {
        }
        FrameLayout.LayoutParams symLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.LEFT);
        symLp.leftMargin = 8;
        symLp.topMargin = 44;
        canvasContainer.addView(symbolCheck, symLp);

        symNumberHint = new TextView(this);
        symNumberHint.setTextSize(9);
        symNumberHint.setTextColor(0xFFD84315);
        symNumberHint.setVisibility(View.GONE);
        FrameLayout.LayoutParams hintLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.LEFT);
        hintLp.leftMargin = 8;
        hintLp.topMargin = 80;
        canvasContainer.addView(symNumberHint, hintLp);

        symbolCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                symbolMode = symbolCheck.isChecked();
                if (symbolMode) {
                    enterSymbolMode();
                } else {
                    exitSymbolMode();
                }
                if (currentSettingsSidePanel != null && settingsPanelObj != null) {
                    PhysObj reopen = settingsPanelObj;
                    dismissSettingsPanel();
                    showSettingsSidePanel(reopen);
                }
                physView.invalidate();
            }
        });

        FrameLayout rightPanelContainer = (FrameLayout) findViewById(R.id.rightPanelContainer);
        rightPanelView = new RightPanelView(this);
        rightPanelContainer.addView(rightPanelView);

        timeLabel = (TextView) findViewById(R.id.timeLabel);
        timeBar = (SeekBar) findViewById(R.id.timeBar);
        timeBar.setMax(600);
        playBtn = (Button) findViewById(R.id.playBtn);
        resetBtn = (Button) findViewById(R.id.resetBtn);
        restartBtn = (Button) findViewById(R.id.restartBtn);

        btnNormal = (Button) findViewById(R.id.btnNormal);
        btnVelocity = (Button) findViewById(R.id.btnVelocity);
        btnAccel = (Button) findViewById(R.id.btnAccel);
        btnDraw = (Button) findViewById(R.id.btnDraw);
        btnCut = (Button) findViewById(R.id.btnCut);
        btnErase = (Button) findViewById(R.id.btnErase);
        tvCutPercent = (TextView) findViewById(R.id.tvCutPercent);
        Button settingsBtn = (Button) findViewById(R.id.settingsBtn);

        setupEventListeners();
        updateModeButtons();

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGlobalSettingsDialog();
            }
        });
    }

    void showGlobalSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("全局设置");
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(4, 4, 4, 4);

        addSliderSmall(content, "地面长度(m)", groundLength, 0.1f, 50f, new FloatSetter() {
                public void set(float v) {
                    groundLength = v;
                    updatePixelsPerMeter();
                    updateAllObjectSizes();
                    physView.invalidate();
                }
            });
        addSliderSmall(content, "风力(N)", windSpeed, 0f, 20f, new FloatSetter() {
                public void set(float v) {
                    windSpeed = v;
                }
            });
        addSliderSmall(content, "风向(°)", windDirection, 0f, 360f, new FloatSetter() {
                public void set(float v) {
                    windDirection = v;
                }
            });
        addSliderSmall(content, "空气阻力(N)", airResistance, 0f, 20f, new FloatSetter() {
                public void set(float v) {
                    airResistance = v;
                }
            });
        addSliderSmall(content, "小数位数", decimalPlaces, 0f, 15f, new FloatSetter() {
                public void set(float v) {
                    decimalPlaces = (int) v;
                    physView.invalidate();
                }
            });

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.addView(content);
        builder.setView(scrollView);
        builder.setPositiveButton("确定", null);
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    private void setupEventListeners() {
        findViewById(R.id.rightToggleBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    togglePanel(false);
                }
            });

        final int[] modes = {MODE_NORMAL, MODE_VELOCITY, MODE_ACCEL, MODE_DRAW, MODE_CUT, MODE_ERASE};
        final Button[] mbtns = {btnNormal, btnVelocity, btnAccel, btnDraw, btnCut, btnErase};
        for (int i = 0; i < mbtns.length; i++) {
            final int mode = modes[i];
            mbtns[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentMode = mode;
                        if (mode != MODE_DRAW) physView.drawStartX = -1;
                        updateModeButtons();
                        tvCutPercent.setVisibility(mode == MODE_CUT ? View.VISIBLE : View.GONE);
                        fineCutCheck.setVisibility(mode == MODE_CUT ? View.VISIBLE : View.GONE);
                    }
                });
        }

        playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    simRunning = !simRunning;
                    if (simRunning) {
                        lastTime = System.nanoTime();
                        playBtn.setText("⏸ 暂停");
                        saveInitialStates();
                        physView.invalidate();
                    } else {
                        playBtn.setText("▶ 播放");
                    }
                }
            });

        resetBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   dismissSettingsPanel();
                    objects.clear();
                    symNextOrdinal = 1;
                    symNumbering = false;
                    hideSymNumberHint();
                    ceilingSegments.clear();
                    simulationHistory.clear();
                    groundEraseRanges.clear();
                    simRunning = false;
                    elapsedTime = 0f;
                    playBtn.setText("▶ 播放");
                    timeLabel.setText("0.0 s");
                    timeBar.setProgress(0);
                    selectedObj = null;
                    connectingObj = null;
                    isConnecting = false;
                    isSelectingConnect = false;
                    connectTargetSpring = null;
                    connectHighlightObj = null;
                    settingsPanelObj = null;
                    physView.invalidate();
                }
            });

        restartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    elapsedTime = 0f;
                    timeLabel.setText("0.0 s");
                    timeBar.setProgress(0);
                    simRunning = false;
                    playBtn.setText("▶ 播放");
                    physView.invalidate();
                }
            });

        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar s, int p, boolean u) {
                    if (u) {
                        float targetTime = p / 10f;
                        jumpToSimulationTime(targetTime);
                    }
                }
                public void onStartTrackingTouch(SeekBar s) {}
                public void onStopTrackingTouch(SeekBar s) {}
            });

        timeLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimeJumpDialog();
                }
            });
    }

    void showTimeJumpDialog() {
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(String.format("%.1f", elapsedTime));
        input.setSelectAllOnFocus(true);
        setupMathKeyboard(input);
        final AlertDialog dialog = new AlertDialog.Builder(this).setTitle("\u8df3\u8f6c\u5230\u65f6\u523b").setView(input)
                .setPositiveButton("\u786e\u5b9a", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        float targetTime = parseExpr(input.getText().toString(), elapsedTime);
                        jumpToSimulationTime(targetTime);
                    }
                })
                .setNegativeButton("\u53d6\u6d88", null)
                .create();
        dialog.show();
    }

    void jumpToSimulationTime(float targetTime) {
        if (Float.isNaN(targetTime) || Float.isInfinite(targetTime)) targetTime = elapsedTime;
        if (targetTime < 0f) targetTime = 0f;
        if (targetTime > 60f) targetTime = 60f;
        if (simulationHistory.isEmpty()) {
            saveInitialStates();
        }
        restoreStateAtTime(targetTime);
        elapsedTime = targetTime;
        timeLabel.setText(String.format("%.1f s", elapsedTime));
        timeBar.setProgress((int) (elapsedTime * 10f + 0.5f));
        lastTime = System.nanoTime();
        accumulator = 0f;
        physView.invalidate();
    }

    void restoreInitialStates() {
        for (PhysObj o : objects) {
            o.x = o.initialX; o.y = o.initialY; o.vx = (float) o.initialVx; o.vy = (float) o.initialVy;
            o.physX = o.initialX; o.physY = o.initialY; o.physVx = o.initialVx; o.physVy = o.initialVy;
            o.rotation = 0; o.forceX = 0; o.forceY = 0; o.displayForceX = 0; o.displayForceY = 0;
            o.reactionForceX = 0; o.reactionForceY = 0;
            o.springAtRest = false;
            o.accelX = 0; o.accelY = 0; o.forceTimer = 0f;
            o.contactGround = false; o.contactWallLeft = false; o.contactWallRight = false; o.contactRamp = false;
            o.symFrozenExpr.clear(); o.symFrozenTime.clear();
            if (o.forceCondition != null) o.forceCondition.reset();
            if (o.velocityCondition != null) o.velocityCondition.reset();
            if (o.bindACondition != null) o.bindACondition.reset();
            if (o.bindBCondition != null) o.bindBCondition.reset();
            if (o.wrapCondition != null) o.wrapCondition.reset();
        }
    }

    void updateModeButtons() {
        int[] modes = {MODE_NORMAL, MODE_VELOCITY, MODE_ACCEL, MODE_DRAW, MODE_CUT, MODE_ERASE};
        int[] colors = {0xFF4CAF50, 0xFF2196F3, 0xFFFF9800, 0xFF9C27B0, 0xFFF44336, 0xFF795548};
        Button[] btns = {btnNormal, btnVelocity, btnAccel, btnDraw, btnCut, btnErase};
        for (int i = 0; i < btns.length; i++) {
            boolean active = currentMode == modes[i];
            btns[i].setBackgroundColor(active ? colors[i] : 0xFFCCCCCC);
            btns[i].setTextColor(active ? Color.WHITE : Color.BLACK);
        }
    }

    void togglePanel(boolean isLeft) {
        if (!isLeft) {
            rightPanelOpen = !rightPanelOpen;
            View container = findViewById(R.id.rightPanelContainer);
            FrameLayout toggle = (FrameLayout) findViewById(R.id.rightToggleBtn);

            if (rightPanelOpen) {
                container.setVisibility(View.VISIBLE);
                container.setLayoutParams(new LinearLayout.LayoutParams(220,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                ((TextView) toggle.getChildAt(0)).setText("◀");
            } else {
                container.setVisibility(View.GONE);
                container.setLayoutParams(new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                ((TextView) toggle.getChildAt(0)).setText("▶");
            }
        }
    }

    void updatePixelsPerMeter() {
        if (wallRightX > 0 && groundLength > 0) {
            pixelsPerMeter = wallRightX / groundLength;
        }
    }

    void updateAllObjectSizes() {
        for (PhysObj o : objects) {
            switch (o.type) {
                case TYPE_BLOCK:
                case TYPE_BOARD:
                case TYPE_RAMP_STRAIGHT:
                case TYPE_RAMP_ARC:
                    o.w = o.realWidth * pixelsPerMeter;
                    o.h = o.realHeight * pixelsPerMeter;
                    break;
                case TYPE_SPRING:
                    if (o.isHorizontal) { o.w = o.realLength * pixelsPerMeter; o.naturalLen = o.w; }
                    else { o.h = o.realLength * pixelsPerMeter; o.naturalLen = o.h; }
                    break;
                case TYPE_ROPE:
                    if (o.isHorizontal) { o.w = o.realLength * pixelsPerMeter; o.ropeLen = o.w; }
                    else { o.h = o.realLength * pixelsPerMeter; o.ropeLen = o.h; }
                    break;
                case TYPE_ROD:
                    o.realHeight = 0f;
                    if (o.isHorizontal) { o.w = o.realLength * pixelsPerMeter; o.h = 0f; }
                    else { o.w = 0f; o.h = o.realLength * pixelsPerMeter; }
                    o.rodLen = o.realLength * pixelsPerMeter;
                    break;
                case TYPE_BALL:
                case TYPE_PULLEY:
                    o.w = o.realRadius * 2 * pixelsPerMeter;
                    o.h = o.w;
                    break;
                case TYPE_CONTAINER:
                    o.w = o.realWidth * pixelsPerMeter;
                    o.h = o.realHeight * pixelsPerMeter;
                    break;
            }
        }
    }

    PhysObj createObjAt(int type, float x, float y) {
        PhysObj o;
        switch (type) {
            case TYPE_BLOCK: o = Block.createObjAt(x, y, pixelsPerMeter, groundLength); break;
            case TYPE_BOARD: o = Board.createObjAt(x, y, pixelsPerMeter, groundLength); break;
            case TYPE_SPRING: o = Spring.createObjAt(x, y, pixelsPerMeter, true, groundLength); break;
            case TYPE_ROPE: o = Rope.createObjAt(x, y, pixelsPerMeter, true, groundLength); break;
            case TYPE_RAMP_STRAIGHT: o = RampStraight.createObjAt(x, y, pixelsPerMeter, groundLength); break;
            case TYPE_RAMP_ARC: o = RampArc.createObjAt(x, y, pixelsPerMeter, groundLength); break;
            case TYPE_BALL: o = Ball.createObjAt(x, y, pixelsPerMeter, groundLength); break;
            case TYPE_CONTAINER: o = Container.createObjAt(x, y, pixelsPerMeter, groundLength); break;
            case TYPE_PULLEY: o = Pulley.createObjAt(x, y, pixelsPerMeter, groundLength); break;
            case TYPE_ROD: o = Rod.createObjAt(x, y, pixelsPerMeter, true, groundLength); break;
            default:
                o = new PhysObj();
                o.type = type; o.x = x; o.y = y;
                break;
        }
        o.physX = o.x; o.physY = o.y;
        o.physVx = o.vx; o.physVy = o.vy;
        return o;
    }

    void refreshConstraintPose(PhysObj obj) {
        if (obj == null || obj.isCutHalf) return;
        if (!isConstraintBody(obj)) return;
        ArrayList<PhysObj> tmpRemove = new ArrayList<PhysObj>();
        boolean prevPostPass = constraintPostPass;
        boolean prevPoseRefresh = constraintPoseRefresh;
        constraintPostPass = true;
        constraintPoseRefresh = true;
        obj.chainSolved = false;
        obj.chainTension = 0f;
        updateSpringRope(obj, FIXED_DT, tmpRemove);
        constraintPostPass = prevPostPass;
        constraintPoseRefresh = prevPoseRefresh;
        for (PhysObj r : tmpRemove) {
            objects.remove(r);
        }
    }

    CeilingSegment createCeilingSegment(float x1, float y1, float x2, float y2) {
        CeilingSegment cs = new CeilingSegment();
        cs.x1 = x1;
        cs.y1 = y1;
        cs.x2 = x2;
        cs.y2 = y2;
        cs.length = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        ceilingSegments.add(cs);
        physView.invalidate();
        return cs;
    }

    PhysObj hitTest(float x, float y) {
        for (int i = objects.size() - 1; i >= 0; i--) {
            PhysObj o = objects.get(i);
            if (o.type != TYPE_RING || o.isCutHalf) continue;
            float rdx = x - o.x, rdy = y - o.y;
            float rr = o.w / 2 + 8;
            if (rdx * rdx + rdy * rdy <= rr * rr) return o;
        }
        if (!isSelectingConnect && !isSelectingConditionTarget && !isSelectingPulleyRamp) {
            for (int i = objects.size() - 1; i >= 0; i--) {
                PhysObj o = objects.get(i);
                if (o.type != TYPE_ROD || o.isCutHalf) continue;
                float vx = x - o.x, vy = y - o.y;
                float vcos = (float) Math.cos(o.rotation), vsin = (float) Math.sin(o.rotation);
                float localX = vx * vcos + vy * vsin;
                float localY = -vx * vsin + vy * vcos;
                if (Math.abs(localX) <= o.w / 2f + 8f && Math.abs(localY) <= 22f) return o;
                float[] eps = getSpringEndpoints(o);
                float x1 = eps[0], y1 = eps[1], x2 = eps[2], y2 = eps[3];
                float A = x - x1, B = y - y1, C = x2 - x1, D = y2 - y1;
                float dot = A * C + B * D, len_sq = C * C + D * D;
                float param = (len_sq != 0) ? dot / len_sq : -1;
                float xx, yy;
                if (param < 0) { xx = x1; yy = y1; }
                else if (param > 1) { xx = x2; yy = y2; }
                else { xx = x1 + param * C; yy = y1 + param * D; }
                float dxr = x - xx, dyr = y - yy;
                if (magnitude(dxr, dyr) <= 22) return o;
            }
        }
        for (int i = objects.size() - 1; i >= 0; i--) {
            PhysObj o = objects.get(i);
            if (o.isCutHalf) continue;
            if (o.type == TYPE_BALL) {
                if (!o.cutVertices.isEmpty()) {
                    float dx = x - o.x, dy = y - o.y;
                    if (Math.abs(dx) <= o.w / 2 && Math.abs(dy) <= o.h / 2) return o;
                } else {
                    float dx = x - o.x, dy = y - o.y, distSq = dx * dx + dy * dy;
                    float radius = o.w / 2;
                    if (distSq <= radius * radius) return o;
                }
            } else if (o.type == TYPE_SPRING || o.type == TYPE_ROPE) {
                if (o.type == TYPE_ROPE && o.ropeWrapped && !o.ropeWrapPoints.isEmpty()) {
                    ArrayList<float[]> pts = o.ropeWrapPoints;
                    for (int wi = 0; wi < pts.size() - 1; wi++) {
                        float[] p1 = pts.get(wi);
                        float[] p2 = pts.get(wi + 1);
                        if (distanceToSegment(x, y, p1[0], p1[1], p2[0], p2[1]) <= 15f) return o;
                    }
                } else {
                    float[] eps = getSpringEndpoints(o);
                    if (distanceToSegment(x, y, eps[0], eps[1], eps[2], eps[3]) <= (o.type == TYPE_SPRING ? 22f : 15f)) return o;
                }
            } else if (o.type == TYPE_RING) {
                float dx = x - o.x, dy = y - o.y;
                float rr = o.w / 2 + 8;
                if (dx * dx + dy * dy <= rr * rr) return o;
            } else if (o.type == TYPE_ROD) {
                float[] eps = getSpringEndpoints(o);
                float x1 = eps[0], y1 = eps[1], x2 = eps[2], y2 = eps[3];
                float A = x - x1, B = y - y1, C = x2 - x1, D = y2 - y1;
                float dot = A * C + B * D, len_sq = C * C + D * D;
                float param = (len_sq != 0) ? dot / len_sq : -1;
                float xx, yy;
                if (param < 0) { xx = x1; yy = y1; }
                else if (param > 1) { xx = x2; yy = y2; }
                else { xx = x1 + param * C; yy = y1 + param * D; }
                float dxr = x - xx, dyr = y - yy;
                if (magnitude(dxr, dyr) <= 15) return o;
            } else {
                float dx = x - o.x, dy = y - o.y;
                float cos = (float) Math.cos(o.rotation), sin = (float) Math.sin(o.rotation);
                float localX = dx * cos + dy * sin, localY = -dx * sin + dy * cos;
                if (o.type == TYPE_RAMP_STRAIGHT) {
                    float hl = o.w / 2, hh = o.h / 2;
                    float x1t, y1t, x2t, y2t, x3t, y3t;
                    if (o.isHorizontal) {
                        x1t = -hl; y1t = hh; x2t = hl; y2t = hh; x3t = hl; y3t = -hh;
                    } else {
                        x1t = hl; y1t = hh; x2t = -hl; y2t = hh; x3t = -hl; y3t = -hh;
                    }
                    float d1 = (localX - x1t) * (y2t - y1t) - (x2t - x1t) * (localY - y1t);
                    float d2 = (localX - x2t) * (y3t - y2t) - (x3t - x2t) * (localY - y2t);
                    float d3 = (localX - x3t) * (y1t - y3t) - (x1t - x3t) * (localY - y3t);
                    boolean hasNeg = d1 < 0 || d2 < 0 || d3 < 0;
                    boolean hasPos = d1 > 0 || d2 > 0 || d3 > 0;
                    if (!(hasNeg && hasPos)) return o;
                } else if (o.type == TYPE_RAMP_ARC) {
                    ArcContact ac = new ArcContact();
                    if (getArcContact(o, x, y, ac)) {
                        float dOuter = Math.abs(magnitude(x - ac.cx, y - ac.cy) - ac.radius);
                        float ppmHit = o.realWidth > 0.001f ? o.w / o.realWidth
                                : (o.realHeight > 0.001f ? o.h / o.realHeight : pixelsPerMeter);
                        float hitTol = Math.max(14f, o.pipeMode ? o.pipeDiameter * ppmHit * 0.5f : 14f);
                        if (!ac.clamped && dOuter <= hitTol) return o;
                        if (o.pipeMode) {
                            float innerR = Math.max(1f, ac.radius - o.pipeDiameter * ppmHit);
                            float dInner = Math.abs(magnitude(x - ac.cx, y - ac.cy) - innerR);
                            if (!ac.clamped && dInner <= hitTol) return o;
                        }
                        if (o.h > o.w && o.h > 1f && o.w > 1f) {
                            float wallX = o.isHorizontal ? o.x + o.w / 2f : o.x - o.w / 2f;
                            float bottomY = o.y + o.h / 2f;
                            float topY = o.y - o.h / 2f;
                            float W2 = Math.max(0f, o.w);
                            float R2 = (W2 * W2 + o.h * o.h) / (2f * o.h);
                            float cX2 = o.isHorizontal ? o.x - W2 / 2f : o.x + W2 / 2f;
                            float cY2 = o.y + o.h / 2f - R2;
                            float dx2 = wallX - cX2;
                            float crossY2 = topY;
                            float ds2 = R2 * R2 - dx2 * dx2;
                            if (ds2 >= 0f) {
                                float ya = cY2 + (float) Math.sqrt(ds2);
                                float yb = cY2 - (float) Math.sqrt(ds2);
                                crossY2 = Math.max(ya, yb);
                                if (crossY2 < topY) crossY2 = topY;
                                if (crossY2 > bottomY) crossY2 = bottomY;
                            }
                            if (Math.abs(x - wallX) <= 14f && y <= bottomY && y >= crossY2) return o;
                        }
                    }
                } else if (o.type == TYPE_PULLEY) {
                    float r = o.w / 2;
                    float dx2 = x - o.x, dy2 = y - o.y;
                    if (dx2*dx2 + dy2*dy2 <= r*r) return o;
                } else {
                    if (Math.abs(localX) <= o.w / 2 && Math.abs(localY) <= o.h / 2) return o;
                }
            }
        }
        return null;
    }

    CeilingSegment ceilingHitTest(float x, float y) {
        for (int i = ceilingSegments.size() - 1; i >= 0; i--) {
            CeilingSegment cs = ceilingSegments.get(i);
            float A = x - cs.x1, B = y - cs.y1, C = cs.x2 - cs.x1, D = cs.y2 - cs.y1;
            float dot = A * C + B * D, len_sq = C * C + D * D;
            float param = (len_sq != 0) ? dot / len_sq : -1;
            float xx, yy;
            if (param < 0) {
                xx = cs.x1;
                yy = cs.y1;
            } else if (param > 1) {
                xx = cs.x2;
                yy = cs.y2;
            } else {
                xx = cs.x1 + param * C;
                yy = cs.y1 + param * D;
            }
            float dx = x - xx, dy = y - yy, dist = magnitude(dx, dy);
            if (dist <= 15) return cs;
        }
        return null;
    }

    void startConnectMode(final PhysObj spring, final boolean isEndA) {
        isSelectingConnect = true;
        connectTargetSpring = spring;
        connectIsEndA = isEndA;
        connectHighlightObj = null;
        dismissSettingsPanel();
        physView.invalidate();
    }

    void startSelectConditionTarget(ConditionObjCallback callback) {
        isSelectingConditionTarget = true;
        conditionObjCallback = callback;
        conditionHighlightObj = null;
        if (activeConditionParentDialog != null) {
            activeConditionParentDialog.hide();
        }
        physView.invalidate();
    }

    void dismissSettingsPanel() {
        final View panel = currentSettingsSidePanel;
        currentSettingsSidePanel = null;
        settingsPanelObj = null;
        if (panel == null) return;
        panel.setVisibility(View.GONE);
        Runnable detach = new Runnable() {
            @Override
            public void run() {
                ViewGroup parent = (ViewGroup) panel.getParent();
                if (parent != null) parent.removeView(panel);
            }
        };
        if (mainAreaView != null) {
            mainAreaView.post(detach);
        } else {
            detach.run();
        }
    }

    float[] getSurfacePoint(PhysObj obj, float towardX, float towardY) {
        float dx = towardX - obj.x, dy = towardY - obj.y;
        float dist = magnitude(dx, dy);
        if (dist < 0.5f) return new float[]{obj.x, obj.y};
        float nx = dx/dist, ny = dy/dist;
        if (obj.type == TYPE_BALL || obj.type == TYPE_RING) {
            return new float[]{obj.x + nx*(obj.w/2), obj.y + ny*(obj.w/2)};
        }
        float cos = (float)Math.cos(-obj.rotation), sin = (float)Math.sin(-obj.rotation);
        float lx = nx*cos - ny*sin, ly = nx*sin + ny*cos;
        float hw = obj.w/2, hh = obj.h/2;
        float t = Float.MAX_VALUE;
        if (Math.abs(lx) > 0.001f) { float tx = (lx>0?hw:-hw)/lx; if (tx>0&&tx<t) t=tx; }
        if (Math.abs(ly) > 0.001f) { float ty = (ly>0?hh:-hh)/ly; if (ty>0&&ty<t) t=ty; }
        if (t == Float.MAX_VALUE) return new float[]{obj.x, obj.y};
        float sx = lx*t, sy = ly*t;
        float wcos = (float)Math.cos(obj.rotation), wsin = (float)Math.sin(obj.rotation);
        return new float[]{obj.x + sx*wcos - sy*wsin, obj.y + sx*wsin + sy*wcos};
    }

    void showSettingsSidePanel(final PhysObj obj) {
        dismissSettingsPanel();
        settingsPanelObj = obj;

        final float origMass = obj.mass;
        final float origFriction = obj.friction;
        final float origRestitution = obj.restitution;
        final float origRealWidth = obj.realWidth;
        final float origRealHeight = obj.realHeight;
        final float origRealRadius = obj.realRadius;
        final float origRealLength = obj.realLength;
        final float origSpringK = obj.springK;
        final float origRopeElasticLimit = obj.ropeElasticLimit;
        final float origBaseArea = obj.baseArea;
        final float origContainerWallThickness = obj.containerWallThickness;
        final float origContainerWaterLevel = obj.containerWaterLevel;
        final float origW = obj.w;
        final float origH = obj.h;
        final boolean origFixed = obj.fixed;
        final boolean origRemoteLock = obj.remoteLock;
        final boolean origShowState = obj.showState;
        final boolean origShowForces = obj.showForces;
        final boolean origShowTrajectory = obj.showTrajectory;
        final float origNaturalLen = obj.naturalLen;
        final float origRopeLen = obj.ropeLen;
        final boolean origIsHorizontal = obj.isHorizontal;
        final float origDensity = obj.density;
        final float origRodLen = obj.rodLen;
        final boolean origAngleFixed = obj.angleFixed;
        final float origFixedAngle = obj.fixedAngle;
        final LogicCondition origAngleFixedCondition = obj.angleFixedCondition;
        final float origArcAngle = obj.arcAngle;
        final boolean origPipeMode = obj.pipeMode;
        final float origPipeDiameter = obj.pipeDiameter;

        int panelW = (int)(220 * getResources().getDisplayMetrics().density);

        final LinearLayout panelRoot = new LinearLayout(this);
        panelRoot.setOrientation(LinearLayout.VERTICAL);
        panelRoot.setBackgroundColor(0xFFF5F5F5);
        FrameLayout.LayoutParams panelLp = new FrameLayout.LayoutParams(panelW, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.RIGHT);
        panelRoot.setLayoutParams(panelLp);

        TextView tvTitle = new TextView(this);
        String dispName = (obj.type == TYPE_PULLEY) ? getPulleyDisplayName(obj) : getTypeName(obj.type);
        if (symbolMode && obj.symOrdinal > 0) {
            dispName = dispName + " " + symEffectiveSymbol(obj, "m");
        }
        tvTitle.setText(dispName);
        tvTitle.setTextSize(14); tvTitle.setPadding(12, 10, 12, 10);
        tvTitle.setBackgroundColor(0xFF455A64); tvTitle.setTextColor(Color.WHITE);
        panelRoot.addView(tvTitle);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(4, 4, 4, 4);
        content.setMinimumHeight(100);

        final TextView[] tvDensity = {null};
        final TextView[] tvAngle = {null};
        final TextView[] tvMaxDeform = {null};

        addSliderAuto(content, "质量(kg)", "质量 m", obj.mass, 0.1f, 20f, new FloatSetter() {
            public void set(float v) {
                obj.mass = v;
                if (tvDensity[0] != null) {
                    if (obj.type == TYPE_BALL) {
                        float vol = (4f / 3f) * (float) Math.PI * obj.realRadius * obj.realRadius * obj.realRadius;
                        obj.density = vol > 0 ? v / vol : 500f;
                        tvDensity[0].setText(MainActivity.this.densityText(obj, obj.density));
                    } else if (obj.baseArea > 0) {
                        float h = obj.realHeight > 0 ? obj.realHeight : 0.1f;
                        obj.density = v / (obj.baseArea * h);
                        tvDensity[0].setText(MainActivity.this.densityText(obj, obj.density));
                    }
                }
            }
        });

        if (!isConstraintBody(obj)) {
            String frLabel;
            if (obj.type == TYPE_RING) frLabel = "内表面摩擦系数";
            else if (obj.type == TYPE_BALL || obj.type == TYPE_PULLEY) frLabel = "摩擦系数";
            else frLabel = "下表面摩擦系数";
            String rsLabel = "恢复系数";
            addSliderAuto(content, frLabel, frLabel + " μ", obj.friction, 0f, 1f, new FloatSetter() {
                public void set(float v) {
                    obj.friction = v;
                }
            });
            addSliderAuto(content, rsLabel, rsLabel + " e", obj.restitution, 0f, 1f, new FloatSetter() {
                public void set(float v) {
                    obj.restitution = v;
                }
            });
        }

        if (obj.type == TYPE_BLOCK || obj.type == TYPE_BOARD || obj.type == TYPE_RAMP_STRAIGHT) {
            addSliderAuto(content, "长度(m)", "长度 L", obj.realWidth, 0.1f, 5f, new FloatSetter() {
                public void set(float v) {
                    obj.realWidth = v;
                    obj.w = v * pixelsPerMeter;
                    if (tvAngle[0] != null) {
                        float a = (float) Math.toDegrees(Math.atan2(obj.realHeight, obj.realWidth));
                        tvAngle[0].setText(MainActivity.this.angleDisplayText(a));
                    }
                    if (tvDensity[0] != null && obj.baseArea > 0) {
                        float h = obj.realHeight > 0 ? obj.realHeight : 0.1f;
                        tvDensity[0].setText(MainActivity.this.densityText(obj, obj.mass / (obj.baseArea * h)));
                    }
                    physView.invalidate();
                }
            });
            addSliderAuto(content, "高度(m)", "高度 h", obj.realHeight, 0.1f, 5f, new FloatSetter() {
                public void set(float v) {
                    obj.realHeight = v;
                    obj.h = v * pixelsPerMeter;
                    if (tvAngle[0] != null) {
                        float a = (float) Math.toDegrees(Math.atan2(obj.realHeight, obj.realWidth));
                        tvAngle[0].setText(MainActivity.this.angleDisplayText(a));
                    }
                    if (tvDensity[0] != null && obj.baseArea > 0) {
                        float h = v > 0 ? v : 0.1f;
                        tvDensity[0].setText(MainActivity.this.densityText(obj, obj.mass / (obj.baseArea * h)));
                    }
                    physView.invalidate();
                }
            });
        } else if (obj.type == TYPE_RAMP_ARC) {
            final LinearLayout[] arcAngleRowRef = {null};
            addSliderAuto(content, "长度(m)", "长度 L", obj.realWidth, 0f, 5f, new FloatSetter() {
                public void set(float v) {
                    obj.realWidth = v;
                    obj.w = v * pixelsPerMeter;
                    if (arcAngleRowRef[0] != null) {
                        arcAngleRowRef[0].setVisibility(v <= 0.001f ? View.VISIBLE : View.GONE);
                    }
                    physView.invalidate();
                }
            });
            final LinearLayout arcAngleRow = addSliderAuto(content, "弧度(rad)", "弧度 θ", obj.arcAngle, (float) Math.PI, (float) (2.0 * Math.PI), new FloatSetter() {
                public void set(float v) {
                    obj.arcAngle = v;
                    physView.invalidate();
                }
            });
            arcAngleRowRef[0] = arcAngleRow;
            arcAngleRow.setVisibility(obj.realWidth <= 0.001f ? View.VISIBLE : View.GONE);
            addSliderAuto(content, "高度(m)", "高度 h", obj.realHeight, 0.1f, 5f, new FloatSetter() {
                public void set(float v) {
                    obj.realHeight = v;
                    obj.h = v * pixelsPerMeter;
                    physView.invalidate();
                }
            });
            LinearLayout shapeRow = new LinearLayout(this);
            shapeRow.setOrientation(LinearLayout.HORIZONTAL);
            shapeRow.setPadding(0, 2, 0, 2);
            TextView shapeLabel = new TextView(this);
            shapeLabel.setText("形态:");
            shapeLabel.setTextSize(9);
            shapeLabel.setTextColor(Color.BLACK);
            shapeLabel.setPadding(2, 0, 4, 0);
            shapeRow.addView(shapeLabel);
            final Button btnTrack = new Button(this);
            btnTrack.setText("轨道");
            btnTrack.setTextSize(8);
            btnTrack.setPadding(4, 0, 4, 0);
            btnTrack.setLayoutParams(new LinearLayout.LayoutParams(0, 28, 1f));
            btnTrack.setTextColor(Color.BLACK);
            final Button btnPipe = new Button(this);
            btnPipe.setText("管道");
            btnPipe.setTextSize(8);
            btnPipe.setPadding(4, 0, 4, 0);
            btnPipe.setLayoutParams(new LinearLayout.LayoutParams(0, 28, 1f));
            btnPipe.setTextColor(Color.BLACK);
            final LinearLayout pipeDiameterRow = addSliderAuto(content, "管道直径(m)", "管道直径 D", obj.pipeDiameter, 0.05f, 5f, new FloatSetter() {
                public void set(float v) {
                    obj.pipeDiameter = v;
                    physView.invalidate();
                }
            });
            pipeDiameterRow.setVisibility(obj.pipeMode ? View.VISIBLE : View.GONE);
            btnTrack.setBackgroundColor(!obj.pipeMode ? 0xFF2196F3 : 0xFFCCCCCC);
            btnPipe.setBackgroundColor(obj.pipeMode ? 0xFF2196F3 : 0xFFCCCCCC);
            btnTrack.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    obj.pipeMode = false;
                    btnTrack.setBackgroundColor(0xFF2196F3);
                    btnPipe.setBackgroundColor(0xFFCCCCCC);
                    pipeDiameterRow.setVisibility(View.GONE);
                    physView.invalidate();
                }
            });
            btnPipe.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    obj.pipeMode = true;
                    btnPipe.setBackgroundColor(0xFF2196F3);
                    btnTrack.setBackgroundColor(0xFFCCCCCC);
                    pipeDiameterRow.setVisibility(View.VISIBLE);
                    physView.invalidate();
                }
            });
            shapeRow.addView(btnTrack);
            shapeRow.addView(btnPipe);
            content.removeView(pipeDiameterRow);
            content.addView(shapeRow);
            content.addView(pipeDiameterRow);
        }

        if (obj.type == TYPE_SPRING || obj.type == TYPE_ROPE) {
            addSliderAuto(content, "长度(m)", "长度 L", obj.realLength, 0.2f, 5f, new FloatSetter() {
                public void set(float v) {
                    obj.realLength = v;
                    if (obj.isHorizontal) {
                        obj.w = v * pixelsPerMeter;
                        if (obj.type == TYPE_SPRING) obj.naturalLen = obj.w;
                        else obj.ropeLen = obj.w;
                    } else {
                        obj.h = v * pixelsPerMeter;
                        if (obj.type == TYPE_SPRING) obj.naturalLen = obj.h;
                        else obj.ropeLen = obj.h;
                    }
                    physView.invalidate();
                }
            });

            LinearLayout orientRow = new LinearLayout(this);
            orientRow.setOrientation(LinearLayout.HORIZONTAL);
            orientRow.setPadding(0, 2, 0, 2);
            TextView orientLabel = new TextView(this);
            orientLabel.setText("方向:"); orientLabel.setTextSize(9); orientLabel.setTextColor(Color.BLACK);
            orientLabel.setPadding(2, 0, 4, 0);
            orientRow.addView(orientLabel);
            final Button btnOrH = new Button(this);
            btnOrH.setText("水平"); btnOrH.setTextSize(8); btnOrH.setPadding(4, 0, 4, 0);
            btnOrH.setLayoutParams(new LinearLayout.LayoutParams(0, 28, 1f));
            btnOrH.setBackgroundColor(obj.isHorizontal ? 0xFF2196F3 : 0xFFCCCCCC);
            btnOrH.setTextColor(Color.BLACK);
            final Button btnOrV = new Button(this);
            btnOrV.setText("竖直"); btnOrV.setTextSize(8); btnOrV.setPadding(4, 0, 4, 0);
            btnOrV.setLayoutParams(new LinearLayout.LayoutParams(0, 28, 1f));
            btnOrV.setBackgroundColor(!obj.isHorizontal ? 0xFF2196F3 : 0xFFCCCCCC);
            btnOrV.setTextColor(Color.BLACK);
            btnOrH.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (!obj.isHorizontal) {
                        obj.isHorizontal = true;
                        float len = obj.realLength * pixelsPerMeter;
                        obj.w = len; obj.h = obj.type == TYPE_SPRING ? 20 : 10;
                        if (obj.type == TYPE_SPRING) obj.naturalLen = len; else obj.ropeLen = len;
                        obj.freeEndX = obj.x - obj.w/2; obj.freeEndY = obj.y;
                        obj.freeDirX = -1; obj.freeDirY = 0;
                    }
                    if (obj.angleFixed) obj.fixedAngle = 0f;
                    btnOrH.setBackgroundColor(0xFF2196F3); btnOrV.setBackgroundColor(0xFFCCCCCC);
                    physView.invalidate();
                }
            });
            btnOrV.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (obj.isHorizontal) {
                        obj.isHorizontal = false;
                        float len = obj.realLength * pixelsPerMeter;
                        obj.w = 20; obj.h = len;
                        if (obj.type == TYPE_SPRING) obj.naturalLen = len; else obj.ropeLen = len;
                        obj.freeEndX = obj.x; obj.freeEndY = obj.y - obj.h/2;
                        obj.freeDirX = 0; obj.freeDirY = -1;
                    }
                    if (obj.angleFixed) obj.fixedAngle = (float) Math.PI / 2f;
                    btnOrV.setBackgroundColor(0xFF2196F3); btnOrH.setBackgroundColor(0xFFCCCCCC);
                    physView.invalidate();
                }
            });
            orientRow.addView(btnOrH); orientRow.addView(btnOrV);
            content.addView(orientRow);
        }

        if (obj.type == TYPE_ROD) {
            addSliderAuto(content, "长度(m)", "长度 L", obj.realLength, 0.2f, 5f, new FloatSetter() {
                public void set(float v) {
                    obj.realLength = v;
                    obj.rodLen = v * pixelsPerMeter;
                    if (obj.isHorizontal) {
                        obj.w = obj.rodLen;
                        obj.h = 0f;
                    } else {
                        obj.w = 0f;
                        obj.h = obj.rodLen;
                    }
                    physView.invalidate();
                }
            });
            LinearLayout rodOrientRow = new LinearLayout(this);
            rodOrientRow.setOrientation(LinearLayout.HORIZONTAL);
            rodOrientRow.setPadding(0, 2, 0, 2);
            TextView rodOrientLabel = new TextView(this);
            rodOrientLabel.setText("方向:"); rodOrientLabel.setTextSize(9); rodOrientLabel.setTextColor(Color.BLACK);
            rodOrientLabel.setPadding(2, 0, 4, 0);
            rodOrientRow.addView(rodOrientLabel);
            final Button btnRodH = new Button(this);
            btnRodH.setText("水平"); btnRodH.setTextSize(8); btnRodH.setPadding(4, 0, 4, 0);
            btnRodH.setLayoutParams(new LinearLayout.LayoutParams(0, 28, 1f));
            btnRodH.setBackgroundColor(obj.isHorizontal ? 0xFF2196F3 : 0xFFCCCCCC);
            btnRodH.setTextColor(Color.BLACK);
            final Button btnRodV = new Button(this);
            btnRodV.setText("竖直"); btnRodV.setTextSize(8); btnRodV.setPadding(4, 0, 4, 0);
            btnRodV.setLayoutParams(new LinearLayout.LayoutParams(0, 28, 1f));
            btnRodV.setBackgroundColor(!obj.isHorizontal ? 0xFF2196F3 : 0xFFCCCCCC);
            btnRodV.setTextColor(Color.BLACK);
            btnRodH.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (!obj.isHorizontal) {
                        obj.isHorizontal = true;
                        obj.rodLen = obj.realLength * pixelsPerMeter;
                        obj.realHeight = 0f;
                        obj.w = obj.rodLen; obj.h = 0f;
                        obj.rotation = 0;
                    }
                    if (obj.angleFixed) obj.fixedAngle = 0f;
                    btnRodH.setBackgroundColor(0xFF2196F3); btnRodV.setBackgroundColor(0xFFCCCCCC);
                    physView.invalidate();
                }
            });
            btnRodV.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (obj.isHorizontal) {
                        obj.isHorizontal = false;
                        obj.rodLen = obj.realLength * pixelsPerMeter;
                        obj.realHeight = 0f;
                        obj.w = 0f; obj.h = obj.rodLen;
                        obj.rotation = 0;
                    }
                    if (obj.angleFixed) obj.fixedAngle = (float) Math.PI / 2f;
                    btnRodV.setBackgroundColor(0xFF2196F3); btnRodH.setBackgroundColor(0xFFCCCCCC);
                    physView.invalidate();
                }
            });
            rodOrientRow.addView(btnRodH); rodOrientRow.addView(btnRodV);
            content.addView(rodOrientRow);
        }

        if (obj.type == TYPE_SPRING) {
            addSliderAuto(content, "劲度系数(N/m)", "劲度系数 k", obj.springK, 100f, 2000f, new FloatSetter() {
                public void set(float v) {
                    obj.springK = v;
                    if (tvMaxDeform[0] != null && v > 0) {
                        tvMaxDeform[0].setText(MainActivity.this.deformText(obj.ropeElasticLimit / v));
                    }
                }
            });
            addSliderAuto(content, "弹性限度(N)", "弹性限度 Fmax", obj.ropeElasticLimit, 150f, 1200f, new FloatSetter() {
                public void set(float v) {
                    obj.ropeElasticLimit = v;
                    if (tvMaxDeform[0] != null && obj.springK > 0) {
                        tvMaxDeform[0].setText(MainActivity.this.deformText(v / obj.springK));
                    }
                }
            });
            TextView tvMD = new TextView(this);
            tvMD.setTextSize(9);
            tvMD.setPadding(2, 0, 2, 0);
            tvMD.setTextColor(Color.BLACK);
            tvMD.setText(deformText(obj.ropeElasticLimit / obj.springK));
            tvMaxDeform[0] = tvMD;
            content.addView(tvMD);
        }

        if (obj.type == TYPE_ROPE) {
            addSliderAuto(content, "劲度系数(N/m)", "劲度系数 k", obj.springK, 100f, 2000f, new FloatSetter() {
                public void set(float v) {
                    obj.springK = v;
                    if (tvMaxDeform[0] != null && v > 0) {
                        tvMaxDeform[0].setText(MainActivity.this.deformText(obj.ropeElasticLimit / v));
                    }
                }
            });
            addSliderAuto(content, "弹性限度(N)", "弹性限度 Fmax", obj.ropeElasticLimit, 300f, 2400f, new FloatSetter() {
                public void set(float v) {
                    obj.ropeElasticLimit = v;
                    if (tvMaxDeform[0] != null && obj.springK > 0) {
                        tvMaxDeform[0].setText(MainActivity.this.deformText(v / obj.springK));
                    }
                }
            });
            TextView tvMD = new TextView(this);
            tvMD.setTextSize(9);
            tvMD.setPadding(2, 0, 2, 0);
            tvMD.setTextColor(Color.BLACK);
            tvMD.setText(deformText(obj.ropeElasticLimit / obj.springK));
            tvMaxDeform[0] = tvMD;
            content.addView(tvMD);
        }

        if (obj.type == TYPE_BALL) {
            TextView tvD = new TextView(this);
            tvD.setTextSize(9);
            tvD.setPadding(2, 0, 2, 0);
            tvD.setTextColor(Color.BLACK);
            float vol0 = (4f / 3f) * (float) Math.PI * obj.realRadius * obj.realRadius * obj.realRadius;
            obj.density = vol0 > 0 ? obj.mass / vol0 : 500f;
            tvD.setText(densityText(obj, obj.density));
            tvDensity[0] = tvD;
            content.addView(tvD);
        } else if (obj.type != TYPE_SPRING && obj.type != TYPE_ROPE && obj.type != TYPE_PULLEY
                && obj.type != TYPE_ROD && obj.type != TYPE_RING) {
            addSliderAuto(content, "底面积(m²)", "底面积 S", obj.baseArea, 0.01f, 2f, new FloatSetter() {
                public void set(float v) {
                    obj.baseArea = v;
                    float h = obj.realHeight > 0 ? obj.realHeight : 0.1f;
                    obj.density = v > 0 ? obj.mass / (v * h) : 500f;
                    if (tvDensity[0] != null) {
                        tvDensity[0].setText(MainActivity.this.densityText(obj, obj.density));
                    }
                }
            });
            TextView tvD = new TextView(this);
            tvD.setTextSize(9);
            tvD.setPadding(2, 0, 2, 0);
            tvD.setTextColor(Color.BLACK);
            float hh = obj.realHeight > 0 ? obj.realHeight : 0.1f;
            obj.density = obj.baseArea > 0 ? obj.mass / (obj.baseArea * hh) : 500f;
            tvD.setText(densityText(obj, obj.density));
            tvDensity[0] = tvD;
            content.addView(tvD);
        }

        if (obj.type == TYPE_RAMP_STRAIGHT) {
            LinearLayout angleRow = new LinearLayout(this);
            angleRow.setOrientation(LinearLayout.HORIZONTAL);
            angleRow.setPadding(0, 2, 0, 2);
            TextView angleLabel = new TextView(this);
            angleLabel.setText("角度:");
            angleLabel.setTextSize(9);
            angleLabel.setPadding(2, 0, 4, 0);
            angleLabel.setTextColor(Color.BLACK);
            TextView angleValue = new TextView(this);
            angleValue.setTextSize(9);
            angleValue.setPadding(4, 0, 2, 0);
            angleValue.setTextColor(Color.BLACK);
            float angle = (float) Math.toDegrees(Math.atan2(obj.realHeight, obj.realWidth));
            angleValue.setText(angleDisplayText(angle));
            tvAngle[0] = angleValue;
            angleRow.addView(angleLabel);
            angleRow.addView(angleValue);
            content.addView(angleRow);
        }

        if (obj.type == TYPE_RAMP_STRAIGHT || obj.type == TYPE_RAMP_ARC) {
            LinearLayout rampDirRow = new LinearLayout(this);
            rampDirRow.setOrientation(LinearLayout.HORIZONTAL);
            rampDirRow.setPadding(0, 2, 0, 2);

            TextView rampDirLabel = new TextView(this);
            rampDirLabel.setText("朝向:");
            rampDirLabel.setTextSize(9);
            rampDirLabel.setPadding(2, 0, 4, 0);
            rampDirLabel.setTextColor(Color.BLACK);
            rampDirRow.addView(rampDirLabel);

            final Button btnLeft = new Button(this);
            btnLeft.setText("向左");
            btnLeft.setTextSize(8);
            btnLeft.setPadding(4, 0, 4, 0);
            LinearLayout.LayoutParams lpLeft = new LinearLayout.LayoutParams(0, 28, 1f);
            btnLeft.setLayoutParams(lpLeft);
            btnLeft.setBackgroundColor(obj.isHorizontal ? 0xFF2196F3 : 0xFFCCCCCC);
            btnLeft.setTextColor(Color.BLACK);

            final Button btnRight = new Button(this);
            btnRight.setText("向右");
            btnRight.setTextSize(8);
            btnRight.setPadding(4, 0, 4, 0);
            LinearLayout.LayoutParams lpRight = new LinearLayout.LayoutParams(0, 28, 1f);
            btnRight.setLayoutParams(lpRight);
            btnRight.setBackgroundColor(!obj.isHorizontal ? 0xFF2196F3 : 0xFFCCCCCC);
            btnRight.setTextColor(Color.BLACK);

            btnLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!obj.isHorizontal) {
                        obj.isHorizontal = true;
                        physView.invalidate();
                    }
                    btnLeft.setBackgroundColor(0xFF2196F3);
                    btnLeft.setTextColor(Color.WHITE);
                    btnRight.setBackgroundColor(0xFFCCCCCC);
                    btnRight.setTextColor(Color.BLACK);
                }
            });

            btnRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (obj.isHorizontal) {
                        obj.isHorizontal = false;
                        physView.invalidate();
                    }
                    btnRight.setBackgroundColor(0xFF2196F3);
                    btnRight.setTextColor(Color.WHITE);
                    btnLeft.setBackgroundColor(0xFFCCCCCC);
                    btnLeft.setTextColor(Color.BLACK);
                }
            });

            rampDirRow.addView(btnLeft);
            rampDirRow.addView(btnRight);
            content.addView(rampDirRow);
        }

        if (obj.type == TYPE_CONTAINER) {
            addSliderAuto(content, "长度(m)", "长度 L", obj.realWidth, 0.2f, 5f, new FloatSetter() {
                public void set(float v) {
                    obj.realWidth = v;
                    obj.w = v * pixelsPerMeter;
                    physView.invalidate();
                }
            });
            addSliderAuto(content, "高度(m)", "高度 h", obj.realHeight, 0.1f, 3f, new FloatSetter() {
                public void set(float v) {
                    obj.realHeight = v;
                    obj.h = v * pixelsPerMeter;
                    physView.invalidate();
                }
            });
            addSliderAuto(content, "容器壁厚(m)", "容器壁厚 d", obj.containerWallThickness, 0f, 0.1f, new FloatSetter() {
                public void set(float v) {
                    obj.containerWallThickness = v;
                    physView.invalidate();
                }
            });
            float maxWL = Math.max(0f, obj.realHeight - obj.containerWallThickness);
            addSliderAuto(content, "水位(m)", "水位 h水", obj.containerWaterLevel, 0f, maxWL > 0 ? maxWL : obj.realHeight, new FloatSetter() {
                public void set(float v) {
                    obj.containerWaterLevel = v;
                    physView.invalidate();
                }
            });
            TextView tvWaterMass = new TextView(this);
            tvWaterMass.setTextSize(9);
            tvWaterMass.setPadding(2, 0, 2, 0);
            tvWaterMass.setTextColor(Color.BLACK);
            float innerW = Math.max(0f, obj.realWidth - 2 * obj.containerWallThickness);
            float waterVol = innerW * obj.containerWaterLevel * 0.3f;
            tvWaterMass.setText(waterMassText(1000f * waterVol));
            content.addView(tvWaterMass);
        }

        if (obj.type == TYPE_BALL) {
            addSliderAuto(content, "半径(m)", "半径 r", obj.realRadius, 0.05f, 1f, new FloatSetter() {
                public void set(float v) {
                    obj.realRadius = v;
                    obj.w = v * 2 * pixelsPerMeter;
                    obj.h = obj.w;
                    if (tvDensity[0] != null) {
                        float vol = (4f / 3f) * (float) Math.PI * v * v * v;
                        obj.density = vol > 0 ? obj.mass / vol : 500f;
                        tvDensity[0].setText(MainActivity.this.densityText(obj, obj.density));
                    }
                    physView.invalidate();
                }
            });
        }
        if (obj.type == TYPE_PULLEY) {
            addSliderAuto(content, "半径(m)", "半径 r", obj.realRadius, 0.05f, 1f, new FloatSetter() {
                public void set(float v) {
                    obj.realRadius = v;
                    obj.w = v * 2 * pixelsPerMeter;
                    obj.h = obj.w;
                    physView.invalidate();
                }
            });
        }

        final Button[] btnConnectARef = new Button[]{null};
        final Button[] btnConnectBRef = new Button[]{null};
        final CheckBox cbAngleFixed = new CheckBox(this);
        if (isConstraintBody(obj)) {
            cbAngleFixed.setText("固定");
            cbAngleFixed.setChecked(obj.angleFixed);
            cbAngleFixed.setPadding(0, 2, 0, 2);
            cbAngleFixed.setTextSize(10);
            cbAngleFixed.setTextColor(Color.BLACK);
            content.addView(makeUntilRow(cbAngleFixed, obj.angleFixedCondition, new ConditionSetter() {
                @Override
                public void set(LogicCondition cond) {
                    obj.angleFixedCondition = cond;
                    if (cond != null) cond.reset();
                }
            }));
        }
        final CheckBox cbFixed = new CheckBox(this);
        cbFixed.setText(obj.type == TYPE_ROD ? "远程锁定" : ((obj.type == TYPE_SPRING || obj.type == TYPE_ROPE) ? "限制" : "固定"));
        cbFixed.setChecked(obj.type == TYPE_ROD ? obj.remoteLock : obj.fixed);
        cbFixed.setPadding(0, 2, 0, 2);
        cbFixed.setTextSize(10);
        cbFixed.setTextColor(Color.BLACK);
        cbFixed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (obj.type == TYPE_ROD) {
                    if (btnConnectARef[0] != null) btnConnectARef[0].setText(cbFixed.isChecked() ? "绑定端点" : "绑定端点A");
                    if (btnConnectBRef[0] != null) btnConnectBRef[0].setText(cbFixed.isChecked() ? "锁定端点" : "绑定端点B");
                }
            }
        });
        content.addView(makeUntilRow(cbFixed, obj.fixedCondition, new ConditionSetter() {
            @Override
            public void set(LogicCondition cond) {
                obj.fixedCondition = cond;
                if (cond != null) cond.reset();
            }
        }));

        final CheckBox cbShowState = new CheckBox(this);
        final CheckBox cbShowForces = new CheckBox(this);
        final CheckBox cbShowTrajectory = new CheckBox(this);
        if (!isConstraintBody(obj)) {
            cbShowState.setText("显示状态");
            cbShowState.setChecked(obj.showState);
            cbShowState.setPadding(0, 2, 0, 2);
            cbShowState.setTextSize(10);
            cbShowState.setTextColor(Color.BLACK);
            content.addView(cbShowState);

            cbShowForces.setText("受力分析");
            cbShowForces.setChecked(obj.showForces);
            cbShowForces.setPadding(0, 2, 0, 2);
            cbShowForces.setTextSize(10);
            cbShowForces.setTextColor(Color.BLACK);
            content.addView(cbShowForces);

            cbShowTrajectory.setText("轨迹");
            cbShowTrajectory.setChecked(obj.showTrajectory);
            cbShowTrajectory.setPadding(0, 2, 0, 2);
            cbShowTrajectory.setTextSize(10);
            cbShowTrajectory.setTextColor(Color.BLACK);
            content.addView(cbShowTrajectory);
        }

        if (isConstraintBody(obj)) {
            if (obj.connectedObjA != null) {
                final Button btnBoundA = new Button(this);
                btnBoundA.setText("已绑定" + getTypeName(obj.connectedObjA.type));
                btnBoundA.setTextSize(9);
                btnBoundA.setBackgroundColor(0xFFFF8C00);
                btnBoundA.setTextColor(Color.BLACK);
                btnBoundA.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle("取消绑定").setMessage("确定要取消绑定吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    obj.connectedObjA = null;
                                    obj.bindACondition = null;
                                    dismissSettingsPanel();
                                    showSettingsSidePanel(obj);
                                }
                            }).setNegativeButton("取消", null).show();
                    }
                });
                content.addView(makeUntilRow(btnBoundA, obj.bindACondition, new ConditionSetter() {
                    @Override
                    public void set(LogicCondition cond) {
                        obj.connectedObjA = null;
                        obj.bindACondition = cond;
                        dismissSettingsPanel();
                        showSettingsSidePanel(obj);
                    }
                }));
            } else if (obj.connectedToCeiling && obj.connectedObjA == null) {
                final Button btnBoundCeilA = new Button(this);
                btnBoundCeilA.setText("已绑定天花板");
                btnBoundCeilA.setTextSize(9);
                btnBoundCeilA.setBackgroundColor(0xFFFF8C00);
                btnBoundCeilA.setTextColor(Color.BLACK);
                btnBoundCeilA.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle("取消绑定").setMessage("确定要取消绑定吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    obj.connectedToCeiling = false; obj.ceilingSegmentId = -1;
                                    obj.bindACondition = null;
                                    dismissSettingsPanel();
                                    showSettingsSidePanel(obj);
                                }
                            }).setNegativeButton("取消", null).show();
                    }
                });
                content.addView(makeUntilRow(btnBoundCeilA, obj.bindACondition, new ConditionSetter() {
                    @Override
                    public void set(LogicCondition cond) { obj.bindACondition = cond; }
                }));
            } else if (obj.connectedToGround && obj.connectedObjA == null) {
                final Button btnBoundGndA = new Button(this);
                btnBoundGndA.setText("已绑定地面");
                btnBoundGndA.setTextSize(9);
                btnBoundGndA.setBackgroundColor(0xFFFF8C00);
                btnBoundGndA.setTextColor(Color.BLACK);
                btnBoundGndA.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle("取消绑定").setMessage("确定要取消绑定吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    obj.connectedToGround = false; obj.groundAnchorX = 0;
                                    obj.bindACondition = null;
                                    dismissSettingsPanel();
                                    showSettingsSidePanel(obj);
                                }
                            }).setNegativeButton("取消", null).show();
                    }
                });
                content.addView(makeUntilRow(btnBoundGndA, obj.bindACondition, new ConditionSetter() {
                    @Override
                    public void set(LogicCondition cond) { obj.bindACondition = cond; }
                }));
            } else if (obj.connectedToWallLeft && obj.connectedObjA == null) {
                final Button btnBoundWallLA = new Button(this);
                btnBoundWallLA.setText("已绑定左墙");
                btnBoundWallLA.setTextSize(9);
                btnBoundWallLA.setBackgroundColor(0xFFFF8C00);
                btnBoundWallLA.setTextColor(Color.BLACK);
                btnBoundWallLA.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle("取消绑定").setMessage("确定要取消绑定吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    obj.connectedToWallLeft = false;
                                    obj.bindACondition = null;
                                    dismissSettingsPanel();
                                    showSettingsSidePanel(obj);
                                }
                            }).setNegativeButton("取消", null).show();
                    }
                });
                content.addView(makeUntilRow(btnBoundWallLA, obj.bindACondition, new ConditionSetter() {
                    @Override
                    public void set(LogicCondition cond) { obj.bindACondition = cond; }
                }));
            } else if (obj.connectedToWallRight && obj.connectedObjA == null) {
                final Button btnBoundWallRA = new Button(this);
                btnBoundWallRA.setText("已绑定右墙");
                btnBoundWallRA.setTextSize(9);
                btnBoundWallRA.setBackgroundColor(0xFFFF8C00);
                btnBoundWallRA.setTextColor(Color.BLACK);
                btnBoundWallRA.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle("取消绑定").setMessage("确定要取消绑定吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    obj.connectedToWallRight = false;
                                    obj.bindACondition = null;
                                    dismissSettingsPanel();
                                    showSettingsSidePanel(obj);
                                }
                            }).setNegativeButton("取消", null).show();
                    }
                });
                content.addView(makeUntilRow(btnBoundWallRA, obj.bindACondition, new ConditionSetter() {
                    @Override
                    public void set(LogicCondition cond) { obj.bindACondition = cond; }
                }));
            } else {
                Button btnConnectA = new Button(this);
                btnConnectARef[0] = btnConnectA;
                btnConnectA.setText(obj.type == TYPE_ROD && cbFixed.isChecked() ? "绑定端点" : "绑定端点A");
                btnConnectA.setTextSize(9);
                btnConnectA.setTextColor(Color.BLACK);
                btnConnectA.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (obj.type == TYPE_ROD) {
                            setAngleFixed(obj, cbAngleFixed.isChecked());
                            obj.remoteLock = cbFixed.isChecked();
                            obj.fixed = false;
                        } else {
                            if (obj.type == TYPE_SPRING || obj.type == TYPE_ROPE) setAngleFixed(obj, cbAngleFixed.isChecked());
                            obj.fixed = cbFixed.isChecked();
                        }
                        startConnectMode(obj, true);
                    }
                });
                content.addView(makeUntilRow(btnConnectA, obj.bindACondition, new ConditionSetter() {
                    @Override
                    public void set(LogicCondition cond) { obj.bindACondition = cond; }
                }));
            }

            if (obj.connectedObjB != null) {
                final Button btnBoundB = new Button(this);
                btnBoundB.setText("已绑定" + getTypeName(obj.connectedObjB.type));
                btnBoundB.setTextSize(9);
                btnBoundB.setBackgroundColor(0xFFFF8C00);
                btnBoundB.setTextColor(Color.BLACK);
                btnBoundB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle("取消绑定").setMessage("确定要取消绑定吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    obj.connectedObjB = null;
                                    obj.bindBCondition = null;
                                    dismissSettingsPanel();
                                    showSettingsSidePanel(obj);
                                }
                            }).setNegativeButton("取消", null).show();
                    }
                });
                content.addView(makeUntilRow(btnBoundB, obj.bindBCondition, new ConditionSetter() {
                    @Override
                    public void set(LogicCondition cond) { obj.bindBCondition = cond; }
                }));
            } else if (obj.connectedToCeiling && obj.connectedObjA != null && obj.connectedObjB == null) {
                final Button btnBoundCeilB = new Button(this);
                btnBoundCeilB.setText("已绑定天花板");
                btnBoundCeilB.setTextSize(9);
                btnBoundCeilB.setBackgroundColor(0xFFFF8C00);
                btnBoundCeilB.setTextColor(Color.BLACK);
                btnBoundCeilB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle("取消绑定").setMessage("确定要取消绑定吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    obj.connectedToCeiling = false; obj.ceilingSegmentId = -1;
                                    obj.bindBCondition = null;
                                    dismissSettingsPanel();
                                    showSettingsSidePanel(obj);
                                }
                            }).setNegativeButton("取消", null).show();
                    }
                });
                content.addView(makeUntilRow(btnBoundCeilB, obj.bindBCondition, new ConditionSetter() {
                    @Override public void set(LogicCondition cond) { obj.bindBCondition = cond; }
                }));
            } else if (obj.connectedToGround && obj.connectedObjA != null && obj.connectedObjB == null) {
                final Button btnBoundGndB = new Button(this);
                btnBoundGndB.setText("已绑定地面");
                btnBoundGndB.setTextSize(9);
                btnBoundGndB.setBackgroundColor(0xFFFF8C00);
                btnBoundGndB.setTextColor(Color.BLACK);
                btnBoundGndB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle("取消绑定").setMessage("确定要取消绑定吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    obj.connectedToGround = false; obj.groundAnchorX = 0;
                                    obj.bindBCondition = null;
                                    dismissSettingsPanel();
                                    showSettingsSidePanel(obj);
                                }
                            }).setNegativeButton("取消", null).show();
                    }
                });
                content.addView(makeUntilRow(btnBoundGndB, obj.bindBCondition, new ConditionSetter() {
                    @Override public void set(LogicCondition cond) { obj.bindBCondition = cond; }
                }));
            } else if (obj.connectedToWallLeft && obj.connectedObjA != null && obj.connectedObjB == null) {
                final Button btnBoundWallLB = new Button(this);
                btnBoundWallLB.setText("已绑定左墙");
                btnBoundWallLB.setTextSize(9);
                btnBoundWallLB.setBackgroundColor(0xFFFF8C00);
                btnBoundWallLB.setTextColor(Color.BLACK);
                btnBoundWallLB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle("取消绑定").setMessage("确定要取消绑定吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    obj.connectedToWallLeft = false;
                                    obj.bindBCondition = null;
                                    dismissSettingsPanel();
                                    showSettingsSidePanel(obj);
                                }
                            }).setNegativeButton("取消", null).show();
                    }
                });
                content.addView(makeUntilRow(btnBoundWallLB, obj.bindBCondition, new ConditionSetter() {
                    @Override public void set(LogicCondition cond) { obj.bindBCondition = cond; }
                }));
            } else if (obj.connectedToWallRight && obj.connectedObjA != null && obj.connectedObjB == null) {
                final Button btnBoundWallRB = new Button(this);
                btnBoundWallRB.setText("已绑定右墙");
                btnBoundWallRB.setTextSize(9);
                btnBoundWallRB.setBackgroundColor(0xFFFF8C00);
                btnBoundWallRB.setTextColor(Color.BLACK);
                btnBoundWallRB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle("取消绑定").setMessage("确定要取消绑定吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    obj.connectedToWallRight = false;
                                    obj.bindBCondition = null;
                                    dismissSettingsPanel();
                                    showSettingsSidePanel(obj);
                                }
                            }).setNegativeButton("取消", null).show();
                    }
                });
                content.addView(makeUntilRow(btnBoundWallRB, obj.bindBCondition, new ConditionSetter() {
                    @Override public void set(LogicCondition cond) { obj.bindBCondition = cond; }
                }));
            } else {
                Button btnConnectB = new Button(this);
                btnConnectBRef[0] = btnConnectB;
                btnConnectB.setText(obj.type == TYPE_ROD && cbFixed.isChecked() ? "锁定端点" : "绑定端点B");
                btnConnectB.setTextSize(9);
                btnConnectB.setTextColor(Color.BLACK);
                btnConnectB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (obj.type == TYPE_ROD) {
                            setAngleFixed(obj, cbAngleFixed.isChecked());
                            obj.remoteLock = cbFixed.isChecked();
                            obj.fixed = false;
                        } else {
                            if (obj.type == TYPE_SPRING || obj.type == TYPE_ROPE) setAngleFixed(obj, cbAngleFixed.isChecked());
                            obj.fixed = cbFixed.isChecked();
                        }
                        startConnectMode(obj, false);
                    }
                });
                content.addView(makeUntilRow(btnConnectB, obj.bindBCondition, new ConditionSetter() {
                    @Override public void set(LogicCondition cond) { obj.bindBCondition = cond; }
                }));
            }
        }

        if (obj.type == TYPE_ROPE) {
            final Button btnWrap = new Button(this);
            btnWrap.setText(obj.ropeWrapped ? "取消缠绕" : "缠绕");
            btnWrap.setTextSize(9);
            btnWrap.setTextColor(Color.BLACK);
            btnWrap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (obj.ropeWrapped) {
                        clearRopeWrap(obj);
                        wrapTotalLength = 0f;
                        btnWrap.setText("缠绕");
                    } else {
                        clearRopeWrap(obj);
                        wrapTotalLength = 0f;
                        isWrapDrawing = true;
                        wrapDrawingRope = obj;
                        dismissSettingsPanel();
                    }
                    physView.invalidate();
                }
            });
            content.addView(makeUntilRow(btnWrap, obj.wrapCondition, new ConditionSetter() {
                @Override
                public void set(LogicCondition cond) { obj.wrapCondition = cond; }
            }));
        }

        if (isConstraintBody(obj)) {
            Button btnRotate = new Button(this);
            btnRotate.setText("旋转");
            btnRotate.setTextSize(9);
            btnRotate.setTextColor(Color.BLACK);
            btnRotate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    springRotatePendingObj = obj;
                    dismissSettingsPanel();
                    physView.invalidate();
                }
            });
            content.addView(btnRotate);
        }

        if (obj.type == TYPE_ROD) {
            final Button btnRing = new Button(this);
            btnRing.setText(obj.rodRing != null ? "移除环" : "添加环");
            btnRing.setTextSize(9);
            btnRing.setTextColor(Color.BLACK);
            btnRing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (obj.rodRing != null) {
                        objects.remove(obj.rodRing);
                        obj.rodRing = null;
                        btnRing.setText("添加环");
                    } else {
                        float[] eps = getSpringEndpoints(obj);
                        float mx = (eps[0] + eps[2]) / 2f;
                        float my = (eps[1] + eps[3]) / 2f;
                        PhysObj ring = Ring.createObjAt(mx, my, pixelsPerMeter, obj);
                        obj.rodRing = ring;
                        objects.add(ring);
                        symOnObjectAdded(ring);
                        btnRing.setText("移除环");
                    }
                    physView.invalidate();
                }
            });
            content.addView(btnRing);
        }

if (obj.type == TYPE_PULLEY) {
    if (obj.pulleyBoundRamp != null) {
        Button btnUnbindRamp = new Button(this);
        btnUnbindRamp.setText("已绑定直斜面");
        btnUnbindRamp.setTextSize(9);
        btnUnbindRamp.setBackgroundColor(0xFFFF8C00);
        btnUnbindRamp.setTextColor(Color.BLACK);
        btnUnbindRamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("取消绑定").setMessage("确定要取消绑定吗？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int w) {
                            obj.pulleyBoundRamp = null;
                            dismissSettingsPanel();
                            showSettingsSidePanel(obj);
                        }
                    }).setNegativeButton("取消", null).show();
            }
        });
        content.addView(makeUntilRow(btnUnbindRamp, obj.bindACondition, new ConditionSetter() {
            @Override
            public void set(LogicCondition cond) { obj.bindACondition = cond; }
        }));
    } else {
        Button btnBindRamp = new Button(this);
        btnBindRamp.setText("绑定直斜面顶角");
        btnBindRamp.setTextSize(9);
        btnBindRamp.setTextColor(Color.BLACK);
        btnBindRamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSelectingPulleyRamp = true;
                selectingPulleyObj = obj;
                dismissSettingsPanel();
                physView.invalidate();
            }
        });
        content.addView(makeUntilRow(btnBindRamp, obj.bindACondition, new ConditionSetter() {
            @Override
            public void set(LogicCondition cond) { obj.bindACondition = cond; }
        }));
    }
}

        final CheckBox cbViewLogic = new CheckBox(this);
        cbViewLogic.setText("查看逻辑");
        cbViewLogic.setChecked(false);
        cbViewLogic.setPadding(0, 2, 0, 2);
        cbViewLogic.setTextSize(10);
        cbViewLogic.setTextColor(Color.BLACK);
        content.addView(cbViewLogic);

        final LinearLayout logicInfoLayout = new LinearLayout(this);
        logicInfoLayout.setOrientation(LinearLayout.VERTICAL);
        logicInfoLayout.setVisibility(View.GONE);
        content.addView(logicInfoLayout);

        cbViewLogic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbViewLogic.isChecked()) {
                    logicInfoLayout.removeAllViews();
                    collectObjActiveConditions(obj, logicInfoLayout);
                    collectObjPassiveConditions(obj, logicInfoLayout);
                    logicInfoLayout.setVisibility(View.VISIBLE);
                } else {
                    logicInfoLayout.setVisibility(View.GONE);
                }
            }
        });

        Button btnDelete = new Button(this);
        btnDelete.setText("删除");
        btnDelete.setTextColor(Color.WHITE);
        btnDelete.setBackgroundColor(0xFFFF0000);
        btnDelete.setTextSize(12);
        btnDelete.setGravity(Gravity.CENTER);
        btnDelete.setPadding(0, 0, 0, 0);
        btnDelete.setMinHeight(0);
        btnDelete.setMinimumHeight(0);
        btnDelete.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(36)));
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (obj.type == TYPE_ROD && obj.rodRing != null) {
                    objects.remove(obj.rodRing);
                    obj.rodRing = null;
                }
                if (obj.type == TYPE_RING && obj.ringHostRod != null) {
                    obj.ringHostRod.rodRing = null;
                }
                for (PhysObj other : objects) {
                    if (other.connectedObjA == obj) { other.connectedObjA = null; }
                    if (other.connectedObjB == obj) { other.connectedObjB = null; }
                }
                objects.remove(obj);
                dismissSettingsPanel();
                physView.invalidate();
            }
        });
        content.addView(btnDelete);

        scrollView.addView(content);
        panelRoot.addView(scrollView);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        Button applyBtn = new Button(this);
        applyBtn.setText("确定");
        applyBtn.setTextSize(10);
        applyBtn.setTextColor(Color.BLACK);
        applyBtn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (obj.type == TYPE_ROD) {
                    setAngleFixed(obj, cbAngleFixed.isChecked());
                    obj.remoteLock = cbFixed.isChecked();
                    obj.fixed = false;
                } else {
                    if (obj.type == TYPE_SPRING || obj.type == TYPE_ROPE) setAngleFixed(obj, cbAngleFixed.isChecked());
                    obj.fixed = cbFixed.isChecked();
                }
                obj.showState = cbShowState.isChecked();
                obj.showForces = cbShowForces.isChecked();
                obj.showTrajectory = cbShowTrajectory.isChecked();
                if (!obj.showTrajectory) obj.trajectory.clear();
                refreshConstraintPose(obj);
                physView.invalidate();
                dismissSettingsPanel();
            }
        });

        Button cancelBtn = new Button(this);
        cancelBtn.setText("取消");
        cancelBtn.setTextSize(10);
        cancelBtn.setTextColor(Color.BLACK);
        cancelBtn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obj.mass = origMass; obj.friction = origFriction; obj.restitution = origRestitution;
                obj.realWidth = origRealWidth; obj.realHeight = origRealHeight;
                obj.realRadius = origRealRadius; obj.realLength = origRealLength;
                obj.springK = origSpringK; obj.ropeElasticLimit = origRopeElasticLimit;
                obj.baseArea = origBaseArea; obj.containerWallThickness = origContainerWallThickness;
                obj.containerWaterLevel = origContainerWaterLevel;
                obj.w = origW; obj.h = origH; obj.fixed = origFixed; obj.remoteLock = origRemoteLock;
                obj.showState = origShowState; obj.showForces = origShowForces;
                obj.showTrajectory = origShowTrajectory; obj.naturalLen = origNaturalLen;
                obj.ropeLen = origRopeLen; obj.isHorizontal = origIsHorizontal;
                obj.density = origDensity; obj.rodLen = origRodLen;
                obj.angleFixed = origAngleFixed; obj.fixedAngle = origFixedAngle;
                obj.angleFixedCondition = origAngleFixedCondition;
                obj.arcAngle = origArcAngle; obj.pipeMode = origPipeMode;
                obj.pipeDiameter = origPipeDiameter;
                physView.invalidate();
                dismissSettingsPanel();
            }
        });

        btnRow.addView(applyBtn);
        btnRow.addView(cancelBtn);
        panelRoot.addView(btnRow);

        panelRoot.setClickable(true);
        panelRoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        mainAreaView.addView(panelRoot, panelLp);
        currentSettingsSidePanel = panelRoot;
    }

    class FineCutView extends View {
        ArrayList<float[]> poly;
        ArrayList<float[]> cutPts = new ArrayList<>();
        boolean keepFirst = true;
        ArrayList<float[]> pieceA = null, pieceB = null;
        float[] lineP1 = null, lineP2 = null;
        float viewScale = 1f, viewOffX = 0f, viewOffY = 0f;

        public FineCutView(Context ctx, ArrayList<float[]> polygon) {
            super(ctx);
            this.poly = polygon;
        }

        float[] toLocal(float vx, float vy) {
            return new float[]{(vx - viewOffX) / viewScale, (vy - viewOffY) / viewScale};
        }

        float lx(float v) { return v * viewScale + viewOffX; }
        float ly(float v) { return v * viewScale + viewOffY; }

        ArrayList<float[]> buildCutChain() {
            if (cutPts.size() < 2) return null;
            float[] p1 = cutPts.get(0), p2 = cutPts.get(1);
            float dx = p2[0] - p1[0], dy = p2[1] - p1[1];
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len < 0.5f) return null;
            dx /= len;
            dy /= len;
            float mx = (p1[0] + p2[0]) / 2f, my = (p1[1] + p2[1]) / 2f;
            float ext = len;
            for (float[] v : poly) {
                float ddx = v[0] - mx, ddy = v[1] - my;
                float d = (float) Math.sqrt(ddx * ddx + ddy * ddy);
                if (d > ext) ext = d;
            }
            ext = ext * 1.5f + 40f;
            ArrayList<float[]> chain = new ArrayList<>();
            chain.add(new float[]{mx - dx * ext, my - dy * ext});
            chain.add(new float[]{mx + dx * ext, my + dy * ext});
            return chain;
        }

        ArrayList<float[]> trimCutChain(ArrayList<float[]> chain) {
            float[] a = chain.get(0), b = chain.get(1);
            int pn = poly.size();
            float[] ts = new float[pn];
            int cnt = 0;
            for (int ei = 0; ei < pn; ei++) {
                float[] e1 = poly.get(ei), e2 = poly.get((ei + 1) % pn);
                float[] r = segIntersectParams(a[0], a[1], b[0], b[1], e1[0], e1[1], e2[0], e2[1]);
                if (r != null && cnt < pn) {
                    ts[cnt] = r[0];
                    cnt++;
                }
            }
            if (cnt <= 2) return chain;
            float[] sorted = new float[cnt];
            for (int i = 0; i < cnt; i++) sorted[i] = ts[i];
            java.util.Arrays.sort(sorted);
            float[] uniq = new float[cnt];
            int un = 0;
            for (int i = 0; i < cnt; i++) {
                if (un == 0 || sorted[i] - uniq[un - 1] > 0.0005f) {
                    uniq[un] = sorted[i];
                    un++;
                }
            }
            if (un <= 2) return chain;
            float tEnd = (uniq[1] + uniq[2]) / 2f;
            ArrayList<float[]> nc = new ArrayList<>();
            nc.add(new float[]{a[0], a[1]});
            nc.add(new float[]{a[0] + tEnd * (b[0] - a[0]), a[1] + tEnd * (b[1] - a[1])});
            return nc;
        }

        void recompute() {
            pieceA = null;
            pieceB = null;
            lineP1 = null;
            lineP2 = null;
            ArrayList<float[]> chain = buildCutChain();
            if (chain == null) return;
            chain = trimCutChain(chain);
            lineP1 = chain.get(0);
            lineP2 = chain.get(1);
            ArrayList<ArrayList<float[]>> pieces = splitPolygonByChain(poly, chain);
            if (pieces != null) {
                pieceA = pieces.get(0);
                pieceB = pieces.get(1);
            }
        }

        boolean cutReady() {
            return pieceA != null && pieceB != null;
        }

        ArrayList<float[]> keptPiece() {
            if (!cutReady()) return null;
            return keepFirst ? pieceA : pieceB;
        }

        ArrayList<float[]> droppedPiece() {
            if (!cutReady()) return null;
            return keepFirst ? pieceB : pieceA;
        }

        void selectPieceAt(float px, float py) {
            if (!cutReady()) return;
            if (pointInPoly(px, py, pieceA)) {
                keepFirst = true;
            } else if (pointInPoly(px, py, pieceB)) {
                keepFirst = false;
            } else {
                float[] ca = polyCentroid(pieceA), cb = polyCentroid(pieceB);
                float da = (px - ca[0]) * (px - ca[0]) + (py - ca[1]) * (py - ca[1]);
                float db = (px - cb[0]) * (px - cb[0]) + (py - cb[1]) * (py - cb[1]);
                keepFirst = da <= db;
            }
        }

        void drawPolyPath(Canvas c, ArrayList<float[]> vs, Paint fill, Paint stroke) {
            if (vs == null || vs.size() < 3) return;
            Path p = new Path();
            boolean first = true;
            for (float[] v : vs) {
                if (first) {
                    p.moveTo(lx(v[0]), ly(v[1]));
                    first = false;
                } else {
                    p.lineTo(lx(v[0]), ly(v[1]));
                }
            }
            p.close();
            if (fill != null) c.drawPath(p, fill);
            if (stroke != null) c.drawPath(p, stroke);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            c.drawColor(Color.WHITE);
            float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
            float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
            for (float[] v : poly) {
                if (v[0] < minX) minX = v[0];
                if (v[0] > maxX) maxX = v[0];
                if (v[1] < minY) minY = v[1];
                if (v[1] > maxY) maxY = v[1];
            }
            float w = getWidth(), h = getHeight();
            float spanX = Math.max(1f, maxX - minX), spanY = Math.max(1f, maxY - minY);
            viewScale = Math.min(w * 0.45f / spanX, h * 0.45f / spanY);
            viewOffX = w / 2 - (minX + maxX) / 2 * viewScale;
            viewOffY = h * 0.38f - (minY + maxY) / 2 * viewScale;

            Paint axisP = PhysObj.mp(0xFF999999, Paint.Style.STROKE, 1.5f);
            Paint axisT = PhysObj.mp(0xFF666666, Paint.Style.FILL, 0f);
            axisT.setTextSize(16f);
            c.drawLine(0, ly(0), w, ly(0), axisP);
            c.drawLine(lx(0), 0, lx(0), h, axisP);
            c.drawText("x", w - 18, ly(0) - 6, axisT);
            c.drawText("y", lx(0) + 6, 16, axisT);
            float gridStep = pixelsPerMeter * 0.1f * viewScale;
            if (gridStep > 8f) {
                Paint gridP = PhysObj.mp(0x20000000, Paint.Style.STROKE, 1f);
                for (float gx = lx(0) % gridStep; gx < w; gx += gridStep) c.drawLine(gx, 0, gx, h, gridP);
                for (float gy = ly(0) % gridStep; gy < h; gy += gridStep) c.drawLine(0, gy, w, gy, gridP);
            }

            Paint polyF = PhysObj.mp(0x30888888, Paint.Style.FILL, 0f);
            Paint polyB = PhysObj.mp(Color.BLACK, Paint.Style.STROKE, 2f);
            drawPolyPath(c, poly, polyF, polyB);

            ArrayList<float[]> kept = keptPiece();
            ArrayList<float[]> dropped = droppedPiece();
            float totalArea = polyArea(poly);
            if (kept != null && dropped != null) {
                Paint dropF = PhysObj.mp(0x33D32F2F, Paint.Style.FILL, 0f);
                Paint dropB = PhysObj.mp(0xFFD32F2F, Paint.Style.STROKE, 2f);
                dropB.setPathEffect(new android.graphics.DashPathEffect(new float[]{8, 6}, 0));
                drawPolyPath(c, dropped, dropF, dropB);

                Paint keepF = PhysObj.mp(0x5533AA33, Paint.Style.FILL, 0f);
                Paint keepB = PhysObj.mp(0xFF2E7D32, Paint.Style.STROKE, 2.5f);
                drawPolyPath(c, kept, keepF, keepB);

                float[] dCen = polyCentroid(dropped);
                Paint dropT = PhysObj.mp(0xFFD32F2F, Paint.Style.FILL, 0f);
                dropT.setTextSize(17f);
                dropT.setTextAlign(Paint.Align.CENTER);
                float dPct = totalArea > 0.001f ? polyArea(dropped) / totalArea * 100f : 0f;
                c.drawText(String.format("舍弃 %.0f%%", dPct), lx(dCen[0]), ly(dCen[1]) + 5, dropT);

                float[] cen = polyCentroid(kept);
                Paint cenP = PhysObj.mp(0xFFD32F2F, Paint.Style.STROKE, 2.5f);
                float ccx = lx(cen[0]), ccy = ly(cen[1]);
                c.drawCircle(ccx, ccy, 6, cenP);
                c.drawLine(ccx - 12, ccy, ccx + 12, ccy, cenP);
                c.drawLine(ccx, ccy - 12, ccx, ccy + 12, cenP);
                Paint cenT = PhysObj.mp(0xFFD32F2F, Paint.Style.FILL, 0f);
                cenT.setTextSize(18f);
                String cenTxt = "重心(" + toSurdDisplay(cen[0] / pixelsPerMeter) + ", "
                        + toSurdDisplay(-cen[1] / pixelsPerMeter) + ")m";
                c.drawText(cenTxt, ccx + 14, ccy - 10, cenT);

                Paint keepT = PhysObj.mp(0xFF2E7D32, Paint.Style.FILL, 0f);
                keepT.setTextSize(17f);
                keepT.setTextAlign(Paint.Align.CENTER);
                float kPct = totalArea > 0.001f ? polyArea(kept) / totalArea * 100f : 0f;
                c.drawText(String.format("保留 %.0f%%", kPct), ccx, ccy + 28, keepT);
            }

            Paint cutLineP = PhysObj.mp(0xFFFF6600, Paint.Style.STROKE, 2.5f);
            cutLineP.setPathEffect(new android.graphics.DashPathEffect(new float[]{10, 5}, 0));
            if (lineP1 != null && lineP2 != null) {
                c.drawLine(lx(lineP1[0]), ly(lineP1[1]), lx(lineP2[0]), ly(lineP2[1]), cutLineP);
            } else if (cutPts.size() == 2) {
                float[] p1 = cutPts.get(0), p2 = cutPts.get(1);
                c.drawLine(lx(p1[0]), ly(p1[1]), lx(p2[0]), ly(p2[1]), cutLineP);
            }
            Paint ptP = PhysObj.mp(0xFFFF6600, Paint.Style.FILL, 0f);
            Paint ptT = PhysObj.mp(0xFFFF6600, Paint.Style.FILL, 0f);
            ptT.setTextSize(15f);
            for (int i = 0; i < cutPts.size(); i++) {
                float[] p = cutPts.get(i);
                c.drawCircle(lx(p[0]), ly(p[1]), 6, ptP);
                c.drawText(String.valueOf(i + 1), lx(p[0]) + 9, ly(p[1]) - 8, ptT);
            }

            Paint hintP = PhysObj.mp(0xFF1565C0, Paint.Style.FILL, 0f);
            hintP.setTextSize(16f);
            Paint tipP = PhysObj.mp(0xFF888888, Paint.Style.FILL, 0f);
            tipP.setTextSize(14f);
            c.drawText("本窗口仅可切割一刀", 10, 18, tipP);
            if (cutPts.size() == 0) {
                c.drawText("点击取第1个点，确定切割线", 10, h - 12, hintP);
            } else if (cutPts.size() == 1) {
                c.drawText("再点击取第2个点确定切割线", 10, h - 12, hintP);
            } else if (!cutReady()) {
                c.drawText("切割线未穿过图形，请重新取点", 10, h - 12, hintP);
            } else {
                c.drawText("点击任一侧即可选择保留部分", 10, h - 12, hintP);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                float[] p = toLocal(ev.getX(), ev.getY());
                if (cutPts.size() < 2) {
                    cutPts.add(p);
                    recompute();
                    if (cutReady() && polyArea(pieceB) > polyArea(pieceA)) keepFirst = false;
                } else {
                    selectPieceAt(p[0], p[1]);
                }
                invalidate();
                return true;
            }
            return super.onTouchEvent(ev);
        }
    }

    float[] polyCentroid(ArrayList<float[]> verts) {
        float signedArea = 0, cx = 0, cy = 0;
        int n = verts.size();
        for (int i = 0; i < n; i++) {
            float[] v1 = verts.get(i), v2 = verts.get((i + 1) % n);
            float cross = v1[0] * v2[1] - v2[0] * v1[1];
            signedArea += cross;
            cx += (v1[0] + v2[0]) * cross;
            cy += (v1[1] + v2[1]) * cross;
        }
        signedArea /= 2;
        if (Math.abs(signedArea) < 0.001f) {
            float sx = 0, sy = 0;
            for (float[] v : verts) { sx += v[0]; sy += v[1]; }
            return new float[]{sx / n, sy / n};
        }
        return new float[]{cx / (6 * signedArea), cy / (6 * signedArea)};
    }

    float polyArea(ArrayList<float[]> verts) {
        float area = 0;
        int n = verts.size();
        for (int i = 0; i < n; i++) {
            float[] v1 = verts.get(i), v2 = verts.get((i + 1) % n);
            area += v1[0] * v2[1] - v2[0] * v1[1];
        }
        return Math.abs(area) / 2;
    }

    float[] segIntersectParams(float p1x, float p1y, float p2x, float p2y,
                               float p3x, float p3y, float p4x, float p4y) {
        float d1x = p2x - p1x, d1y = p2y - p1y;
        float d2x = p4x - p3x, d2y = p4y - p3y;
        float cross = d1x * d2y - d1y * d2x;
        if (Math.abs(cross) < 0.000001f) return null;
        float t = ((p3x - p1x) * d2y - (p3y - p1y) * d2x) / cross;
        float u = ((p3x - p1x) * d1y - (p3y - p1y) * d1x) / cross;
        if (t < 0 || t > 1 || u < 0 || u > 1) return null;
        return new float[]{t, u};
    }

    ArrayList<ArrayList<float[]>> splitPolygonByChain(ArrayList<float[]> poly, ArrayList<float[]> chain) {
        int pn = poly.size();
        int enterSeg = -1, exitSeg = -1;
        int enterEdge = -1, exitEdge = -1;
        float enterT = 0, exitT = 0, enterU = 0, exitU = 0;
        float[] enterPt = null, exitPt = null;
        for (int ci = 0; ci < chain.size() - 1; ci++) {
            float[] c1 = chain.get(ci), c2 = chain.get(ci + 1);
            float bestT = Float.MAX_VALUE;
            for (int ei = 0; ei < pn; ei++) {
                float[] e1 = poly.get(ei), e2 = poly.get((ei + 1) % pn);
                float[] r = segIntersectParams(c1[0], c1[1], c2[0], c2[1], e1[0], e1[1], e2[0], e2[1]);
                if (r != null && r[0] < bestT) {
                    bestT = r[0];
                    enterSeg = ci; enterEdge = ei; enterT = r[0]; enterU = r[1];
                }
            }
            if (enterSeg >= 0) break;
        }
        if (enterSeg < 0) return null;
        for (int ci = chain.size() - 2; ci >= 0; ci--) {
            float[] c1 = chain.get(ci), c2 = chain.get(ci + 1);
            float bestT = -Float.MAX_VALUE;
            for (int ei = 0; ei < pn; ei++) {
                float[] e1 = poly.get(ei), e2 = poly.get((ei + 1) % pn);
                float[] r = segIntersectParams(c1[0], c1[1], c2[0], c2[1], e1[0], e1[1], e2[0], e2[1]);
                if (r != null && r[0] > bestT) {
                    bestT = r[0];
                    exitSeg = ci; exitEdge = ei; exitT = r[0]; exitU = r[1];
                }
            }
            if (exitSeg >= 0) break;
        }
        if (exitSeg < 0) return null;
        if (enterSeg == exitSeg && Math.abs(enterT - exitT) < 0.0001f) return null;
        if (enterSeg > exitSeg) return null;

        float[] es1 = chain.get(enterSeg), es2 = chain.get(enterSeg + 1);
        enterPt = new float[]{es1[0] + enterT * (es2[0] - es1[0]), es1[1] + enterT * (es2[1] - es1[1])};
        float[] xs1 = chain.get(exitSeg), xs2 = chain.get(exitSeg + 1);
        exitPt = new float[]{xs1[0] + exitT * (xs2[0] - xs1[0]), xs1[1] + exitT * (xs2[1] - xs1[1])};

        ArrayList<float[]> chainInside = new ArrayList<>();
        chainInside.add(enterPt);
        for (int ci = enterSeg + 1; ci <= exitSeg; ci++) {
            chainInside.add(chain.get(ci));
        }
        chainInside.add(exitPt);

        ArrayList<float[]> pieceA = new ArrayList<>(chainInside);
        int vi = (exitEdge + 1) % pn;
        while (true) {
            pieceA.add(poly.get(vi));
            if (vi == enterEdge) break;
            vi = (vi + 1) % pn;
            if (pieceA.size() > pn + chainInside.size() + 2) return null;
        }

        ArrayList<float[]> pieceB = new ArrayList<>();
        for (int ci = chainInside.size() - 1; ci >= 0; ci--) pieceB.add(chainInside.get(ci));
        vi = (enterEdge + 1) % pn;
        while (true) {
            pieceB.add(poly.get(vi));
            if (vi == exitEdge) break;
            vi = (vi + 1) % pn;
            if (pieceB.size() > pn + chainInside.size() + 2) return null;
        }

        if (polyArea(pieceA) < 4f || polyArea(pieceB) < 4f) return null;

        ArrayList<ArrayList<float[]>> result = new ArrayList<>();
        result.add(pieceA);
        result.add(pieceB);
        return result;
    }

    void showFineCutDialog(final PhysObj obj) {
        ArrayList<float[]> polygon = new ArrayList<>();
        if (obj.type == TYPE_BALL && obj.cutVertices.isEmpty()) {
            float r = obj.w / 2;
            for (int i = 0; i < 48; i++) {
                float a = (float) (2 * Math.PI * i / 48);
                polygon.add(new float[]{r * (float) Math.cos(a), r * (float) Math.sin(a)});
            }
        } else if (!obj.cutVertices.isEmpty()) {
            for (float[] v : obj.cutVertices) polygon.add(new float[]{v[0], v[1]});
        } else {
            float hw = obj.w / 2, hh = obj.h / 2;
            polygon.add(new float[]{-hw, -hh});
            polygon.add(new float[]{hw, -hh});
            polygon.add(new float[]{hw, hh});
            polygon.add(new float[]{-hw, hh});
        }

        final float origArea = polyArea(polygon);
        final FineCutView fcv = new FineCutView(this, polygon);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        fcv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(300)));
        root.addView(fcv);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        Button btnUndo = new Button(this);
        btnUndo.setText("撤销点");
        btnUndo.setTextSize(10);
        btnUndo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fcv.cutPts.isEmpty()) {
                    fcv.cutPts.remove(fcv.cutPts.size() - 1);
                    fcv.keepFirst = true;
                    fcv.recompute();
                    fcv.invalidate();
                }
            }
        });
        Button btnSwap = new Button(this);
        btnSwap.setText("切换保留部分");
        btnSwap.setTextSize(10);
        btnSwap.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        btnSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fcv.cutReady()) return;
                fcv.keepFirst = !fcv.keepFirst;
                fcv.invalidate();
            }
        });
        Button btnReset = new Button(this);
        btnReset.setText("重新取点");
        btnReset.setTextSize(10);
        btnReset.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fcv.cutPts.clear();
                fcv.keepFirst = true;
                fcv.recompute();
                fcv.invalidate();
            }
        });
        btnRow.addView(btnUndo);
        btnRow.addView(btnReset);
        btnRow.addView(btnSwap);
        root.addView(btnRow);

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("精细切割");
        b.setView(root);
        b.setPositiveButton("确认切割", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int w) {
                ArrayList<float[]> kept = fcv.keptPiece();
                if (kept == null || kept.size() < 3) return;
                float keptArea = polyArea(kept);
                if (origArea > 0.001f) obj.mass = obj.mass * keptArea / origArea;

                float[] cen = polyCentroid(kept);
                ArrayList<float[]> centered = new ArrayList<>();
                for (float[] v : kept) centered.add(new float[]{v[0] - cen[0], v[1] - cen[1]});
                obj.cutVertices = centered;

                float cosR = (float) Math.cos(obj.rotation), sinR = (float) Math.sin(obj.rotation);
                obj.x += cen[0] * cosR - cen[1] * sinR;
                obj.y += cen[0] * sinR + cen[1] * cosR;
                obj.physX = obj.x;
                obj.physY = obj.y;

                float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
                float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
                for (float[] v : centered) {
                    if (v[0] < minX) minX = v[0];
                    if (v[0] > maxX) maxX = v[0];
                    if (v[1] < minY) minY = v[1];
                    if (v[1] > maxY) maxY = v[1];
                }
                obj.w = maxX - minX;
                obj.h = maxY - minY;
                if (obj.type == TYPE_BALL) {
                    obj.realRadius = Math.max(obj.w, obj.h) / 2f / pixelsPerMeter;
                }
                physView.invalidate();
            }
        });
        b.setNegativeButton("取消", null);
        b.show();
    }

    String getTypeName(int type) {
        switch (type) {
            case TYPE_BLOCK: return "木块";
            case TYPE_BOARD: return "木板";
            case TYPE_BALL: return "小球";
            case TYPE_RAMP_STRAIGHT: return "直斜面";
            case TYPE_RAMP_ARC: return "弧形斜面";
            case TYPE_CONTAINER: return "容器";
            case TYPE_SPRING: return "弹簧";
            case TYPE_ROPE: return "绳子";
            case TYPE_PULLEY: return "滑轮";
            case TYPE_ROD: return "杆";
            case TYPE_RING: return "环";
            default: return "物体";
        }
    }

    String getPulleyDisplayName(PhysObj pulley) {
        boolean connectedToFixed = false;
        boolean hasWrapRope = false;
        for (PhysObj o : objects) {
            if (o.type != TYPE_ROPE) continue;
            if (o.connectedObjA == pulley || o.connectedObjB == pulley) {
                boolean otherFixed = o.connectedToCeiling || o.connectedToWallLeft
                        || o.connectedToWallRight || o.connectedToGround;
                PhysObj other = (o.connectedObjA == pulley) ? o.connectedObjB : o.connectedObjA;
                if (otherFixed || (other != null && other.fixed)) connectedToFixed = true;
            }
            if (o.ropeWrapped) hasWrapRope = true;
        }
        if (connectedToFixed) return "定滑轮";
        if (hasWrapRope) return "动滑轮";
        return "滑轮";
    }

    LinearLayout addSliderSmall(LinearLayout parent, final String label, final float current,
                        final float min, final float max, final FloatSetter setter) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 1, 0, 1);

        final TextView tv = new TextView(this);
        tv.setText(label + ": " + fmtVal(current));
        tv.setTextSize(9);
        tv.setPadding(2, 0, 2, 0);
        tv.setTextColor(Color.BLACK);
        row.addView(tv);

        SeekBar sb = new SeekBar(this);
        sb.setMax(1000);
        sb.setProgress(Math.max(0, Math.min(1000, (int) (((current - min) / (max - min)) * 1000f))));
        sb.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 20));

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean u) {
                float val = min + (max - min) * (p / 1000f);
                setter.set(val);
                tv.setText(label + ": " + fmtVal(val));
            }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });

        row.addView(sb);
        parent.addView(row);
        return row;
    }

    LinearLayout addSliderLevel(LinearLayout parent, final String label, final int current,
                                final LevelSetter setter) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 1, 0, 1);

        final TextView tv = new TextView(this);
        tv.setText(label + ": " + SymVar.levelName(current));
        tv.setTextSize(9);
        tv.setPadding(2, 0, 2, 0);
        tv.setTextColor(Color.BLACK);
        row.addView(tv);

        SeekBar sb = new SeekBar(this);
        sb.setMax(SymVar.LEVEL_NAMES.length - 1);
        sb.setProgress(Math.max(0, Math.min(SymVar.LEVEL_NAMES.length - 1, current)));
        sb.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 20));

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean u) {
                setter.set(p);
                tv.setText(label + ": " + SymVar.levelName(p));
            }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });

        row.addView(sb);

        LinearLayout scaleRow = new LinearLayout(this);
        scaleRow.setOrientation(LinearLayout.HORIZONTAL);
        scaleRow.setPadding(2, 0, 2, 0);
        for (int i = 0; i < SymVar.LEVEL_NAMES.length; i++) {
            TextView mark = new TextView(this);
            mark.setText(SymVar.LEVEL_NAMES[i]);
            mark.setTextSize(7);
            mark.setTextColor(Color.GRAY);
            if (i == 0) mark.setGravity(Gravity.LEFT);
            else if (i == SymVar.LEVEL_NAMES.length - 1) mark.setGravity(Gravity.RIGHT);
            else mark.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            scaleRow.addView(mark, mlp);
        }
        row.addView(scaleRow);

        parent.addView(row);
        return row;
    }

    String fmtVal(float v) {
        if (Math.abs(v - Math.round(v)) < 0.05f) return String.valueOf((int) Math.round(v));
        return String.format("%.2f", v);
    }

    LinearLayout addSliderAuto(LinearLayout parent, final String label, final String symLabel,
                               final float current, final float min, final float max,
                               final FloatSetter setter) {
        if (!symbolMode) {
            return addSliderSmall(parent, label, current, min, max, setter);
        }
        int lv = SymVar.nearestLevel(current, min, max);
        return addSliderSym(parent, symLabel, settingsPanelObj, lv, new LevelSetter() {
            public void set(int level) {
                setter.set(symLevelValue(level, min, max));
            }
        });
    }

    String symRowTitle(PhysObj o, String cname, String base) {
        String eff = symEffectiveSymbol(o, base);
        if (cname == null || cname.length() == 0) return eff;
        if (eff.length() == 0) return cname;
        return cname + " " + eff;
    }

    void symApplyMark(TextView tv, boolean known) {
        if (known) {
            tv.setText("\u2713");
            tv.setTextColor(0xFF2E7D32);
            tv.setBackgroundColor(0xFFDCEDC8);
        } else {
            tv.setText("\u2717");
            tv.setTextColor(0xFFC62828);
            tv.setBackgroundColor(0xFFFFCDD2);
        }
    }

    static final String SYM_INSUFFICIENT = "已知信息不充分";

    SymExpr symInsufficient() {
        return new SymExpr(SYM_INSUFFICIENT, 0, 4);
    }

    boolean symBad(SymExpr e) {
        return e != null && SYM_INSUFFICIENT.equals(e.text);
    }

    boolean symDeclaredUnknown(PhysObj o, String base) {
        if (o == null || base == null || base.length() == 0) return false;
        return o.symKnownMap.containsKey(base) && !symPropKnown(o, base);
    }

    boolean symKeyDeclaredUnknown(String key) {
        return symDeclaredUnknown(symContextObj, symKeyToBaseSym(key));
    }

    LinearLayout addSymKnownRow(LinearLayout parent, final PhysObj owner,
                                final String base, final String title) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 6, 0, 6);

        TextView tvName = new TextView(this);
        tvName.setText(title);
        tvName.setTextSize(12);
        tvName.setTextColor(Color.BLACK);
        row.addView(tvName, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvDecl = new TextView(this);
        tvDecl.setText("是否声明已知:");
        tvDecl.setTextSize(11);
        tvDecl.setTextColor(0xFF666666);
        tvDecl.setPadding(2, 0, 6, 0);
        row.addView(tvDecl);

        final TextView tvMark = new TextView(this);
        tvMark.setTextSize(13);
        tvMark.setPadding(12, 2, 12, 2);
        tvMark.setGravity(Gravity.CENTER);
        symApplyMark(tvMark, symPropKnown(owner, base));
        row.addView(tvMark);
        tvMark.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean nk = !symPropKnown(owner, base);
                symSetPropKnown(owner, base, nk);
                symApplyMark(tvMark, nk);
                if (!nk) {
                    android.widget.Toast.makeText(MainActivity.this,
                            SYM_INSUFFICIENT,
                            android.widget.Toast.LENGTH_SHORT).show();
                }
                if (physView != null) physView.invalidate();
            }
        });

        parent.addView(row);
        return row;
    }

    void showSymRenameDialog(final PhysObj o, final String base, final String cname,
                             final TextView tv, final int[] lvHolder) {
        if (o == null || base == null || base.length() == 0) return;
        final EditText et = new EditText(this);
        et.setSingleLine(true);
        et.setTextSize(14);
        et.setText(symEffectiveSymbol(o, base));
        et.setSelection(et.getText().length());
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("\u4fee\u6539\u7b26\u53f7: " + cname);
        b.setView(et);
        b.setPositiveButton("\u786e\u5b9a", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int w) {
                String s = et.getText().toString().trim();
                if (s.length() == 0 || s.equals(base)) {
                    o.symCustomMap.remove(base);
                } else {
                    o.symCustomMap.put(base, s);
                }
                tv.setText(symRowTitle(o, cname, base) + ": " + SymVar.levelName(lvHolder[0]));
                physView.invalidate();
            }
        });
        b.setNeutralButton("\u6062\u590d\u9ed8\u8ba4", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int w) {
                o.symCustomMap.remove(base);
                tv.setText(symRowTitle(o, cname, base) + ": " + SymVar.levelName(lvHolder[0]));
                physView.invalidate();
            }
        });
        b.setNegativeButton("\u53d6\u6d88", null);
        b.show();
    }

    LinearLayout addSliderSym(LinearLayout parent, final String symLabel, final PhysObj owner,
        final int current, final LevelSetter setter) {
        final String cname = symNameOfLabel(symLabel);
        final String base = symBaseOfLabel(symLabel);
        final int[] lvHolder = {current};

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 1, 0, 1);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(0, 0, 2, 0);

        final TextView tv = new TextView(this);
        tv.setTextSize(9);
        tv.setPadding(2, 0, 2, 0);
        tv.setTextColor(Color.BLACK);
        tv.setText(symRowTitle(owner, cname, base) + ": " + SymVar.levelName(current));
        head.addView(tv, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvDecl = new TextView(this);
        tvDecl.setText("\u662f\u5426\u58f0\u660e\u5df2\u77e5:");
        tvDecl.setTextSize(7);
        tvDecl.setTextColor(0xFF666666);
        tvDecl.setPadding(2, 0, 1, 0);
        head.addView(tvDecl);

        final TextView tvMark = new TextView(this);
        tvMark.setTextSize(9);
        tvMark.setPadding(5, 0, 5, 0);
        tvMark.setGravity(Gravity.CENTER);
        symApplyMark(tvMark, symPropKnown(owner, base));
        head.addView(tvMark);
        tvMark.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean nk = !symPropKnown(owner, base);
                symSetPropKnown(owner, base, nk);
                symApplyMark(tvMark, nk);
                physView.invalidate();
            }
        });

        row.addView(head);

        tv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showSymRenameDialog(owner, base, cname, tv, lvHolder);
            }
        });

        SeekBar sb = new SeekBar(this);
        sb.setMax(SymVar.LEVEL_NAMES.length - 1);
        sb.setProgress(Math.max(0, Math.min(SymVar.LEVEL_NAMES.length - 1, current)));
        sb.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 20));
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean u) {
                lvHolder[0] = p;
                setter.set(p);
                tv.setText(symRowTitle(owner, cname, base) + ": " + SymVar.levelName(p));
            }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });
        row.addView(sb);

        LinearLayout scaleRow = new LinearLayout(this);
        scaleRow.setOrientation(LinearLayout.HORIZONTAL);
        scaleRow.setPadding(2, 0, 2, 0);
        for (int i = 0; i < SymVar.LEVEL_NAMES.length; i++) {
            TextView mark = new TextView(this);
            mark.setText(SymVar.LEVEL_NAMES[i]);
            mark.setTextSize(7);
            mark.setTextColor(Color.GRAY);
            if (i == 0) mark.setGravity(Gravity.LEFT);
            else if (i == SymVar.LEVEL_NAMES.length - 1) mark.setGravity(Gravity.RIGHT);
            else mark.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            scaleRow.addView(mark, mlp);
        }
        row.addView(scaleRow);

        parent.addView(row);
        return row;
    }

    float symLevelValue(int level, float min, float max) {
        int i = level;
        if (i < 0) i = 0;
        if (i >= SymVar.LEVEL_FRAC.length) i = SymVar.LEVEL_FRAC.length - 1;
        return min + (max - min) * SymVar.LEVEL_FRAC[i];
    }

    float symLevelVelocity(PhysObj o) {
        return symLevelValue(o == null ? 2 : o.symV0Level, 0f, 15f);
    }

    float symLevelForce(PhysObj o) {
        return symLevelValue(o == null ? 2 : o.symForceLevel, 0f, 50f);
    }

    String densityText(PhysObj o, float value) {
        if (symbolMode) {
            if (o != null && o.type == TYPE_BALL) return "密度: ρ = 3m/(4πr³)";
            return "密度: ρ = m/(S·h)";
        }
        return "密度: " + fmtVal(value) + " kg/m³";
    }

    String waterMassText(float value) {
        if (symbolMode) return "水质量: m水 = ρ水·V水";
        return "水质量≈" + fmtVal(value) + " kg";
    }

    String deformText(float value) {
        if (symbolMode) return "最大形变量: x = Fmax/k";
        return "最大形变量: " + toSurdDisplay(value) + " m";
    }

    String angleDisplayText(float deg) {
        if (symbolMode) return "θ";
        return toSurdDisplay(deg) + "°";
    }

    float parseExpr(String s, float fallback) {
        return (float) parseExprD(s, fallback);
    }

    double parseExprD(String s, double fallback) {
        if (s == null || s.trim().isEmpty()) return fallback;
        s = s.trim().toLowerCase().replace(" ", "");
        try {
            int[] pos = new int[]{0};
            double v = evalSum(s, pos);
            if (pos[0] < s.length()) return fallback;
            if (Double.isNaN(v) || Double.isInfinite(v)) return fallback;
            return v;
        } catch (Exception e) {
            return fallback;
        }
    }

    private double evalSum(String s, int[] pos) {
        double v = evalTerm(s, pos);
        while (pos[0] < s.length()) {
            char c = s.charAt(pos[0]);
            if (c == '+') { pos[0]++; v += evalTerm(s, pos); }
            else if (c == '-') { pos[0]++; v -= evalTerm(s, pos); }
            else break;
        }
        return v;
    }

    private double evalTerm(String s, int[] pos) {
        double v = evalFactor(s, pos);
        while (pos[0] < s.length()) {
            char c = s.charAt(pos[0]);
            if (c == '*' || c == '\u00d7') { pos[0]++; v *= evalFactor(s, pos); }
            else if (c == '/' || c == '\u00f7') { pos[0]++; v /= evalFactor(s, pos); }
            else break;
        }
        return v;
    }

    private double evalFactor(String s, int[] pos) {
        if (pos[0] >= s.length()) throw new RuntimeException("eof");
        char c = s.charAt(pos[0]);
        if (c == '-') { pos[0]++; return -evalFactor(s, pos); }
        if (c == '+') { pos[0]++; return evalFactor(s, pos); }
        if (c == '(') {
            pos[0]++;
            double v = evalSum(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ')') pos[0]++;
            return evalPostfix(s, pos, v);
        }
        if (c == '\u221a') {
            pos[0]++;
            return Math.sqrt(evalFactor(s, pos));
        }
        if (c == '\u03c0') {
            pos[0]++;
            return evalPostfix(s, pos, Math.PI);
        }
        if (s.startsWith("pi", pos[0])) {
            pos[0] += 2;
            return evalPostfix(s, pos, Math.PI);
        }
        if (s.startsWith("frac(", pos[0])) {
            pos[0] += 5;
            double num = evalSum(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ',') pos[0]++;
            double den = evalSum(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ')') pos[0]++;
            return evalPostfix(s, pos, num / den);
        }
        if (s.startsWith("sqrt(", pos[0])) {
            pos[0] += 5;
            double v = evalSum(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ')') pos[0]++;
            return evalPostfix(s, pos, Math.sqrt(v));
        }
        if (s.startsWith("pow(", pos[0])) {
            pos[0] += 4;
            double base = evalSum(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ',') pos[0]++;
            double exp = evalSum(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ')') pos[0]++;
            return evalPostfix(s, pos, Math.pow(base, exp));
        }
        if (s.startsWith("nroot(", pos[0])) {
            pos[0] += 6;
            double nIdx = evalSum(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ',') pos[0]++;
            double arg = evalSum(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ')') pos[0]++;
            return evalPostfix(s, pos, Math.pow(arg, 1.0 / nIdx));
        }
        if (s.startsWith("sin(", pos[0])) {
            pos[0] += 4;
            double v = evalSum(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ')') pos[0]++;
            return evalPostfix(s, pos, Math.sin(v));
        }
        if (s.startsWith("cos(", pos[0])) {
            pos[0] += 4;
            double v = evalSum(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ')') pos[0]++;
            return evalPostfix(s, pos, Math.cos(v));
        }
        if (s.startsWith("tan(", pos[0])) {
            pos[0] += 4;
            double v = evalSum(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ')') pos[0]++;
            return evalPostfix(s, pos, Math.tan(v));
        }
        if (Character.isDigit(c) || c == '.') {
            int start = pos[0];
            while (pos[0] < s.length() && (Character.isDigit(s.charAt(pos[0])) || s.charAt(pos[0]) == '.')) pos[0]++;
            double v = Double.parseDouble(s.substring(start, pos[0]));
            return evalPostfix(s, pos, v);
        }
        throw new RuntimeException("bad char " + c);
    }

    private double evalPostfix(String s, int[] pos, double v) {
        while (pos[0] < s.length() && s.charAt(pos[0]) == '^') {
            pos[0]++;
            v = Math.pow(v, evalFactor(s, pos));
        }
        if (pos[0] < s.length()) {
            char nc = s.charAt(pos[0]);
            if (nc == '(' || nc == '\u221a' || nc == '\u03c0'
                    || s.startsWith("frac(", pos[0]) || s.startsWith("sqrt(", pos[0])
                    || s.startsWith("pow(", pos[0]) || s.startsWith("nroot(", pos[0])
                    || s.startsWith("pi", pos[0])) {
                v *= evalFactor(s, pos);
            }
        }
        return v;
    }

    void setupMathKeyboard(final EditText editText) {
        try {
            editText.setShowSoftInputOnFocus(false);
            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        com.lie.acsm.keyboard.MathKeyboardDialog.show(
                            MainActivity.this, editText, decimalPlaces);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    void showSimpleKeyboard(final EditText editText) {
        com.lie.acsm.keyboard.MathKeyboardDialog.show(this, editText, decimalPlaces);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    boolean symNumberable(PhysObj o) {
        return o != null && !o.isCutHalf;
    }

    void enterSymbolMode() {
        symNextOrdinal = 1;
        symNumbering = false;
        for (PhysObj o : objects) {
            o.symOrdinal = 0;
            o.symKnownMap.clear();
            o.symCustomMap.clear();
            o.symFrozenExpr.clear();
            o.symFrozenTime.clear();
            o.symV0Set = symV0Given(o);
        }
        int cnt = 0;
        for (PhysObj o : objects) {
            if (symNumberable(o)) cnt++;
        }
        if (cnt > 0) {
            symNumbering = true;
            updateSymNumberHint();
        } else {
            hideSymNumberHint();
        }
    }

    void exitSymbolMode() {
        symNumbering = false;
        symNextOrdinal = 1;
        symContextObj = null;
        for (PhysObj o : objects) {
            o.symOrdinal = 0;
            o.symKnownMap.clear();
            o.symCustomMap.clear();
            o.symFrozenExpr.clear();
            o.symFrozenTime.clear();
        }
        hideSymNumberHint();
    }

    void hideSymNumberHint() {
        if (symNumberHint != null) {
            symNumberHint.setVisibility(View.GONE);
            symNumberHint.setText("");
        }
    }

    void updateSymNumberHint() {
        if (symNumberHint == null) return;
        if (!symbolMode || !symNumbering) {
            hideSymNumberHint();
            return;
        }
        int remain = 0;
        for (PhysObj o : objects) {
            if (symNumberable(o) && o.symOrdinal <= 0) remain++;
        }
        if (remain <= 0) {
            symNumbering = false;
            hideSymNumberHint();
            return;
        }
        symNumberHint.setText("符号模式: 请依次点击物体编号 (剩余 "
                + remain + ", 点空白处自动编号)");
        symNumberHint.setVisibility(View.VISIBLE);
    }

    void symAutoNumberRest() {
        if (!symbolMode) return;
        for (PhysObj o : objects) {
            if (symNumberable(o) && o.symOrdinal <= 0) {
                o.symOrdinal = symNextOrdinal++;
            }
        }
        symNumbering = false;
        hideSymNumberHint();
    }

    void symOnObjectAdded(PhysObj o) {
        if (!symbolMode || o == null) return;
        if (!symNumberable(o)) return;
        if (symNumbering) {
            updateSymNumberHint();
            return;
        }
        o.symOrdinal = symNextOrdinal++;
    }

    String symSubscript(int n) {
        if (n <= 0) return "";
        String digits = "\u2080\u2081\u2082\u2083\u2084\u2085\u2086\u2087\u2088\u2089";
        String s = String.valueOf(n);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            int d = s.charAt(i) - '0';
            if (d >= 0 && d <= 9) sb.append(digits.charAt(d));
        }
        return sb.toString();
    }

    String symBaseOfLabel(String symLabel) {
        if (symLabel == null) return "";
        int sp = symLabel.lastIndexOf(' ');
        if (sp < 0) return symLabel;
        return symLabel.substring(sp + 1);
    }

    String symNameOfLabel(String symLabel) {
        if (symLabel == null) return "";
        int sp = symLabel.lastIndexOf(' ');
        if (sp < 0) return "";
        return symLabel.substring(0, sp);
    }

    boolean symSubscriptable(String base) {
        if (base == null || base.length() == 0) return false;
        return base.equals("m") || base.equals("\u03bc") || base.equals("e")
                || base.equals("L") || base.equals("h") || base.equals("\u03b8")
                || base.equals("k") || base.equals("r") || base.equals("S")
                || base.equals("D") || base.equals("d") || base.equals("Fmax")
                || base.equals("h\u6c34") || base.equals("F") || base.equals("a")
                || base.equals("v\u2080");
    }

    String symEffectiveSymbol(PhysObj o, String base) {
        if (base == null || base.length() == 0) return "";
        if (o != null) {
            String custom = o.symCustomMap.get(base);
            if (custom != null && custom.length() > 0) return custom;
            if (o.symOrdinal > 0 && symSubscriptable(base)) return base + symSubscript(o.symOrdinal);
        }
        return base;
    }

    boolean symPropKnown(PhysObj o, String base) {
        if (o == null || base == null || base.length() == 0) return true;
        Boolean b = o.symKnownMap.get(base);
        return b == null ? true : b.booleanValue();
    }

    void symSetPropKnown(PhysObj o, String base, boolean known) {
        if (o == null || base == null || base.length() == 0) return;
        o.symKnownMap.put(base, Boolean.valueOf(known));
    }

    String symKeyToBaseSym(String key) {
        if (key == null) return "";
        if (key.equals("m")) return "m";
        if (key.equals("mu")) return "\u03bc";
        if (key.equals("L")) return "L";
        if (key.equals("h")) return "h";
        if (key.equals("theta")) return "\u03b8";
        if (key.equals("k")) return "k";
        if (key.equals("F")) return "F";
        if (key.equals("a")) return "a";
        if (key.equals("v0")) return "v\u2080";
        return "";
    }

    String symDisplaySym(String key, String fallback) {
        String base = symKeyToBaseSym(key);
        if (base.length() == 0) return fallback;
        String eff = symEffectiveSymbol(symContextObj, base);
        return eff.length() == 0 ? fallback : eff;
    }

    String symObjTag(PhysObj o) {
        if (o == null || !symbolMode) return "";
        if (o.symOrdinal <= 0) return symNumbering ? "?" : "";
        return symEffectiveSymbol(o, "m");
    }

    void initSymVars() {
        if (!symVars.isEmpty()) return;
        addSymVar("m", "m", "质量", "kg", 1f, 0.1f, 50f, true);
        addSymVar("v0", "v₀", "初速度", "m/s", 4f, 0f, 40f, true);
        addSymVar("t", "t", "时间", "s", 1f, 0f, 60f, true);
        addSymVar("g", "g", "重力加速度", "m/s²", GRAVITY, 1f, 20f, true);
        addSymVar("mu", "μ", "摩擦因数", "", 0.3f, 0f, 1.5f, true);
        addSymVar("a", "a", "加速度", "m/s²", 2f, -30f, 30f, false);
        addSymVar("F", "F", "外力", "N", 5f, 0f, 200f, true);
        addSymVar("theta", "θ", "角度", "°", 30f, 0f, 90f, false);
        addSymVar("L", "L", "长度", "m", 1f, 0.1f, 10f, false);
        addSymVar("h", "h", "高度", "m", 1f, 0f, 10f, false);
        addSymVar("k", "k", "劲度系数", "N/m", 500f, 1f, 5000f, false);
    }

    void addSymVar(String key, String sym, String label, String unit,
                   float value, float minV, float maxV, boolean known) {
        SymVar q = new SymVar(key, sym, label, unit, value, minV, maxV, known);
        symVars.add(q);
        symIndex.put(key, q);
    }

    float symValue(String key) {
        SymVar q = symIndex.get(key);
        return q == null ? 0f : q.value;
    }

    boolean symKnown(String key) {
        SymVar q = symIndex.get(key);
        if (q == null || !q.known) return false;
        String base = symKeyToBaseSym(key);
        if (symContextObj != null && base.length() > 0
                && symContextObj.symKnownMap.containsKey(base)
                && !symPropKnown(symContextObj, base)) {
            return false;
        }
        return true;
    }

    boolean symShort(String t) {
        if (t == null || t.length() == 0 || t.length() > 2) return false;
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c == '+' || c == '−' || c == '·' || c == '/' || c == '(' || c == ')' || c == '²') return false;
        }
        return true;
    }

    String symWrap(SymExpr e, int need) {
        return e.prec < need ? ("(" + e.text + ")") : e.text;
    }

    String symWrapTex(SymExpr e, int need) {
        return e.prec < need ? ("(" + e.tex + ")") : e.tex;
    }

    boolean symIsZero(SymExpr e) {
        return e != null && e.text.equals("0");
    }

    boolean symIsOne(SymExpr e) {
        return e != null && e.text.equals("1");
    }

    SymExpr symNum(double v) {
        String t;
        if (Math.abs(v - Math.round(v)) < 1e-6) t = String.valueOf((long) Math.round(v));
        else t = String.format("%.2f", v);
        return new SymExpr(t, v, 4);
    }

    SymExpr symLeaf(String s, double v) {
        return new SymExpr(s, symTexEscape(s), v, 4);
    }

    String symTexEscape(String s) {
        if (s == null) return "";
        if (s.indexOf('\\') < 0 && s.indexOf('{') < 0 && s.indexOf('}') < 0) return s;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') sb.append("\\\\");
            else if (c != '{' && c != '}') sb.append(c);
        }
        return sb.toString();
    }

    SymExpr symAdd(SymExpr a, SymExpr b) {
        if (symBad(a) || symBad(b)) return symInsufficient();
        if (symIsZero(a)) return b;
        if (symIsZero(b)) return a;
        return new SymExpr(symWrap(a, 1) + "+" + symWrap(b, 1),
                symWrapTex(a, 1) + "+" + symWrapTex(b, 1), a.value + b.value, 1);
    }

    SymExpr symSub(SymExpr a, SymExpr b) {
        if (symBad(a) || symBad(b)) return symInsufficient();
        if (symIsZero(b)) return a;
        if (symIsZero(a)) return new SymExpr("−" + symWrap(b, 2), "−" + symWrapTex(b, 2), -b.value, 1);
        return new SymExpr(symWrap(a, 1) + "−" + symWrap(b, 2),
                symWrapTex(a, 1) + "−" + symWrapTex(b, 2), a.value - b.value, 1);
    }

    SymExpr symMul(SymExpr a, SymExpr b) {
        if (symBad(a) || symBad(b)) return symInsufficient();
        if (symIsZero(a) || symIsZero(b)) return symNum(0);
        if (symIsOne(a)) return b;
        if (symIsOne(b)) return a;
        String sep = (symShort(a.text) && symShort(b.text)) ? "" : "·";
        return new SymExpr(symWrap(a, 2) + sep + symWrap(b, 2),
                symWrapTex(a, 2) + sep + symWrapTex(b, 2), a.value * b.value, 2);
    }

    SymExpr symDiv(SymExpr a, SymExpr b) {
        if (symBad(a) || symBad(b)) return symInsufficient();
        if (symIsZero(a)) return symNum(0);
        if (symIsOne(b)) return a;
        double v = Math.abs(b.value) < 1e-12 ? 0 : a.value / b.value;
        return new SymExpr(symWrap(a, 2) + "/" + symWrap(b, 3),
                "\\f{" + a.tex + "}{" + b.tex + "}", v, 2);
    }

    SymExpr symStripNeg(SymExpr e) {
        if (symBad(e)) return null;
        if (e != null && e.prec == 1 && e.text.length() > 1 && e.text.charAt(0) == '−') {
            String tx = e.tex;
            if (tx != null && tx.length() > 1 && tx.charAt(0) == '−') tx = tx.substring(1);
            else tx = e.text.substring(1);
            return new SymExpr(e.text.substring(1), tx, -e.value, 2);
        }
        return null;
    }

    SymExpr symSq(SymExpr a) {
        if (symBad(a)) return symInsufficient();
        if (symIsZero(a)) return symNum(0);
        if (symIsOne(a)) return symNum(1);
        return new SymExpr(symWrap(a, 4) + "²",
                "\\p{" + symWrapTex(a, 4) + "}{2}", a.value * a.value, 3);
    }

    SymExpr symSqrt(SymExpr a) {
        if (symBad(a)) return symInsufficient();
        double v = a.value <= 0 ? 0 : Math.sqrt(a.value);
        return new SymExpr("√(" + a.text + ")", "\\s{" + a.tex + "}", v, 4);
    }

    SymExpr symTrig(String fn, SymExpr a) {
        if (symBad(a)) return symInsufficient();
        double r = Math.toRadians(a.value);
        double v = fn.equals("sin") ? Math.sin(r) : Math.cos(r);
        return new SymExpr(fn + symWrap(a, 4), fn + symWrapTex(a, 4), v, 3);
    }

    SymExpr symQ(String key) {
        SymVar q = symIndex.get(key);
        if (q == null) return symNum(0);
        String disp = symDisplaySym(key, q.sym);
        if (symKnown(key)) return symLeaf(disp, q.value);
        if (key.equals("a") && symKnown("F")) return symDiv(symQ("F"), symQ("m"));
        if (key.equals("F") && symKnown("a")) return symMul(symQ("m"), symQ("a"));
        if (key.equals("h") && symKnown("L") && symKnown("theta")) {
            return symMul(symQ("L"), symTrig("sin", symQ("theta")));
        }
        if (symKeyDeclaredUnknown(key)) return symInsufficient();
        return symLeaf(disp, q.value);
    }

    boolean symHasInitVelocity() {
        if (symKnown("v0")) return true;
        if (symKnown("a")) return true;
        if (symKnown("F") && symKnown("m")) return true;
        if (symKnown("g") && symKnown("h")) return true;
        return false;
    }

    SymExpr symInitVelocityExpr() {
        if (symKnown("v0")) return symQ("v0");
        if (symKnown("a")) return symMul(symQ("a"), symNowTime());
        if (symKnown("F") && symKnown("m")) {
            return symMul(symDiv(symQ("F"), symQ("m")), symNowTime());
        }
        if (symKnown("g") && symKnown("h")) {
            return symSqrt(symMul(symNum(2), symMul(symQ("g"), symQ("h"))));
        }
        return symQ("v0");
    }

    boolean symHasAppliedForce() {
        if (symKnown("F")) return true;
        if (symKnown("m") && symKnown("a")) return true;
        return false;
    }

    SymExpr symAppliedForceExpr() {
        if (symKnown("F")) return symQ("F");
        if (symKnown("m") && symKnown("a")) return symMul(symQ("m"), symQ("a"));
        if (symKeyDeclaredUnknown("F")) return symInsufficient();
        return symMul(symQ("m"), symQ("a"));
    }

    float symInitVelocityValue() {
        float v = (float) Math.abs(symInitVelocityExpr().value);
        if (Float.isNaN(v) || Float.isInfinite(v)) v = 0f;
        return v;
    }

    float symAppliedForceValue() {
        float v = (float) Math.abs(symAppliedForceExpr().value);
        if (Float.isNaN(v) || Float.isInfinite(v)) v = 0f;
        return v;
    }

    String symInitVelocityText() {
        PhysObj prevCtx = symContextObj;
        if (selectedObj != null) symContextObj = selectedObj;
        String r = symText(symInitVelocityExpr());
        symContextObj = prevCtx;
        return r;
    }

    String symAppliedForceText() {
        PhysObj prevCtx = symContextObj;
        if (selectedObj != null) symContextObj = selectedObj;
        String r = symText(symAppliedForceExpr());
        symContextObj = prevCtx;
        return r;
    }

    String symJoinLabel(String base, String expr) {
        if (expr == null || expr.length() == 0 || expr.equals(base)) return base;
        if (expr.equals(SYM_INSUFFICIENT)) return base + ": " + SYM_INSUFFICIENT;
        return base + "=" + expr;
    }

    SymExpr symWeightOf(PhysObj o) {
        return symMul(symQ("m"), symQ("g"));
    }

    SymExpr symNormalOf(PhysObj o) {
        if (o != null && o.contactRamp) {
            return symMul(symWeightOf(o), symTrig("cos", symQ("theta")));
        }
        return symWeightOf(o);
    }

    SymExpr symFrictionOf(PhysObj o) {
        return symMul(symQ("mu"), symNormalOf(o));
    }

    boolean symWrapSimpleBody(PhysObj o) {
        if (o == null || o.fixed) return false;
        if (isConstraintBody(o) || o.type == TYPE_RING) return false;
        if (o.type == TYPE_PULLEY) return false;
        if (o.type == TYPE_RAMP_STRAIGHT || o.type == TYPE_RAMP_ARC) return false;
        if (o.isCutHalf) return false;
        return true;
    }

    boolean symWrapPushed(PhysObj o) {
        if (o == null) return false;
        return Math.abs(o.appliedForceX) > 0.001f || Math.abs(o.appliedForceY) > 0.001f;
    }

    PhysObj symWrapRopeOf(PhysObj o) {
        if (!symWrapSimpleBody(o)) return null;
        PhysObj found = null;
        int cnt = 0;
        for (PhysObj h : objects) {
            if (!isConstraintBody(h)) continue;
            if (h.isCutHalf || h.cutDecaying) continue;
            if (h.connectedObjA != o && h.connectedObjB != o) continue;
            cnt++;
            found = h;
        }
        if (cnt != 1 || found == null) return null;
        if (found.type != TYPE_ROPE || !found.ropeWrapped) return null;
        PhysObj pulley = found.ropeWrapPulley;
        if (pulley == null || pulley.type != TYPE_PULLEY) return null;
        if (!objects.contains(pulley)) return null;
        return found;
    }

    PhysObj symWrapPeerOf(PhysObj rope, PhysObj o) {
        if (rope == null) return null;
        PhysObj peer = null;
        if (rope.connectedObjA == o) peer = rope.connectedObjB;
        else if (rope.connectedObjB == o) peer = rope.connectedObjA;
        if (peer == o) return null;
        return peer;
    }

    boolean symWrapAnchored(PhysObj rope) {
        if (rope == null) return false;
        return rope.connectedToCeiling || rope.connectedToGround
                || rope.connectedToWallLeft || rope.connectedToWallRight;
    }

    boolean symWrapPulleyFixed(PhysObj rope) {
        PhysObj pulley = rope == null ? null : rope.ropeWrapPulley;
        if (pulley == null) return false;
        if (pulley.fixed || pulley.pulleyBoundRamp != null) return true;
        for (PhysObj h : objects) {
            if (h == rope || !isConstraintBody(h)) continue;
            if (h.isCutHalf || h.cutDecaying || h.ropeWrapped) continue;
            if (h.connectedObjA != pulley && h.connectedObjB != pulley) continue;
            PhysObj other = h.connectedObjA == pulley ? h.connectedObjB : h.connectedObjA;
            boolean anchored = constraintFixedAnchorPoint(h) != null
                    || (other != null && other != pulley && other.fixed);
            if (!anchored) continue;
            if (h.type == TYPE_ROPE && !symRopeTaut(h, pulley)) continue;
            return true;
        }
        return false;
    }

    SymExpr symQFor(PhysObj o, String key) {
        PhysObj prev = symContextObj;
        symContextObj = o;
        SymExpr e = symQ(key);
        symContextObj = prev;
        return e;
    }

    int symWrapModeRank(PhysObj o) {
        if (o == null) return 0;
        if (o.contactRamp) return 1;
        if (o.contactGround) return 0;
        return 2;
    }

    SymExpr symWrapDriveOf(PhysObj o) {
        if (o == null) return symNum(0);
        SymExpr w = symMul(symQFor(o, "m"), symQFor(o, "g"));
        if (o.contactRamp) return symMul(w, symTrig("sin", symQFor(o, "theta")));
        if (o.contactGround) return symNum(0);
        return w;
    }

    SymExpr symWrapFrictionOf(PhysObj o) {
        if (o == null || o.friction <= 0.0001f) return symNum(0);
        SymExpr w = symMul(symQFor(o, "m"), symQFor(o, "g"));
        if (o.contactRamp) {
            return symMul(symQFor(o, "mu"), symMul(w, symTrig("cos", symQFor(o, "theta"))));
        }
        if (o.contactGround) return symMul(symQFor(o, "mu"), w);
        return symNum(0);
    }

    boolean symWrapOrderFirst(PhysObj o, PhysObj peer) {
        if (o == null || peer == null) return true;
        if (o.symOrdinal > 0 && peer.symOrdinal > 0) return o.symOrdinal <= peer.symOrdinal;
        return true;
    }

    boolean symWrapPeerDrives(PhysObj o, PhysObj peer) {
        int rs = symWrapModeRank(o);
        int rp = symWrapModeRank(peer);
        if (rp != rs) return rp > rs;
        return peer.mass >= o.mass;
    }

    SymExpr symWrapAccelOf(PhysObj o) {
        PhysObj rope = symWrapRopeOf(o);
        if (rope == null || !symWrapPulleyFixed(rope)) return null;
        PhysObj peer = symWrapPeerOf(rope, o);
        if (peer == null) return symWrapAnchored(rope) ? symNum(0) : null;
        if (peer.fixed) return symNum(0);
        if (!symWrapSimpleBody(peer)) return null;
        if (symWrapPushed(o) || symWrapPushed(peer)) return null;
        if (symHeldStill(o)) return symNum(0);
        if (symHeldStill(peer)) return null;
        SymExpr mSelf = symQFor(o, "m");
        SymExpr mPeer = symQFor(peer, "m");
        boolean selfFirst = symWrapOrderFirst(o, peer);
        SymExpr den = selfFirst ? symAdd(mSelf, mPeer) : symAdd(mPeer, mSelf);
        boolean peerDrives = symWrapPeerDrives(o, peer);
        if (symWrapModeRank(o) == 2 && symWrapModeRank(peer) == 2) {
            if (mSelf.text.equals(mPeer.text)) return symNum(0);
            SymExpr diff = peerDrives ? symSub(mPeer, mSelf) : symSub(mSelf, mPeer);
            return symDiv(symMul(diff, symQFor(o, "g")), den);
        }
        SymExpr num = peerDrives
                ? symSub(symWrapDriveOf(peer), symWrapDriveOf(o))
                : symSub(symWrapDriveOf(o), symWrapDriveOf(peer));
        num = symSub(num, symWrapFrictionOf(o));
        num = symSub(num, symWrapFrictionOf(peer));
        if (symIsZero(num)) return symNum(0);
        return symDiv(num, den);
    }

    SymExpr symWrapTensionOf(PhysObj o) {
        PhysObj rope = symWrapRopeOf(o);
        if (rope == null || !symWrapPulleyFixed(rope)) return null;
        PhysObj peer = symWrapPeerOf(rope, o);
        if (peer == null || peer.fixed) {
            if (peer == null && !symWrapAnchored(rope)) return null;
            if (symWrapModeRank(o) != 2) return null;
            return symMul(symQFor(o, "m"), symQFor(o, "g"));
        }
        if (!symWrapSimpleBody(peer)) return null;
        if (symWrapPushed(o) || symWrapPushed(peer)) return null;
        boolean restSelf = symHeldStill(o);
        boolean restPeer = symHeldStill(peer);
        SymExpr mSelf = symQFor(o, "m");
        SymExpr mPeer = symQFor(peer, "m");
        boolean selfFirst = symWrapOrderFirst(o, peer);
        SymExpr den = selfFirst ? symAdd(mSelf, mPeer) : symAdd(mPeer, mSelf);
        if (restSelf && restPeer) {
            if (symWrapModeRank(o) == 2) return symMul(mSelf, symQFor(o, "g"));
            if (symWrapModeRank(peer) == 2) return symMul(mPeer, symQFor(peer, "g"));
            return null;
        }
        if (restSelf || restPeer) return null;
        if (symWrapModeRank(o) == 2 && symWrapModeRank(peer) == 2) {
            if (mSelf.text.equals(mPeer.text)) return symMul(mSelf, symQFor(o, "g"));
            SymExpr prod = selfFirst ? symMul(mSelf, mPeer) : symMul(mPeer, mSelf);
            return symDiv(symMul(symNum(2), symMul(prod, symQFor(o, "g"))), den);
        }
        boolean peerDrives = symWrapPeerDrives(o, peer);
        PhysObj drv = peerDrives ? peer : o;
        PhysObj dvn = peerDrives ? o : peer;
        SymExpr num = symAdd(
                symMul(symQFor(drv, "m"), symAdd(symWrapDriveOf(dvn), symWrapFrictionOf(dvn))),
                symMul(symQFor(dvn, "m"), symSub(symWrapDriveOf(drv), symWrapFrictionOf(drv))));
        return symDiv(num, den);
    }

    int symLinkCountOf(PhysObj o) {
        if (o == null) return 0;
        int cnt = 0;
        for (PhysObj h : objects) {
            if (!isConstraintBody(h)) continue;
            if (h.isCutHalf || h.cutDecaying) continue;
            if (h.connectedObjA != o && h.connectedObjB != o) continue;
            cnt++;
        }
        return cnt;
    }

    PhysObj symLinkConstraintOf(PhysObj o) {
        if (!symWrapSimpleBody(o)) return null;
        PhysObj found = null;
        int cnt = 0;
        for (PhysObj h : objects) {
            if (!isConstraintBody(h)) continue;
            if (h.isCutHalf || h.cutDecaying) continue;
            if (h.connectedObjA != o && h.connectedObjB != o) continue;
            cnt++;
            found = h;
        }
        if (cnt != 1 || found == null) return null;
        if (found.ropeWrapped) return null;
        if (found.type != TYPE_ROPE && found.type != TYPE_ROD) return null;
        if (found.type == TYPE_ROD && found.remoteLock) return null;
        if (symWrapAnchored(found)) return null;
        PhysObj peer = symWrapPeerOf(found, o);
        if (peer == null || peer.fixed) return null;
        if (!symWrapSimpleBody(peer)) return null;
        if (symLinkCountOf(peer) != 1) return null;
        if (found.type == TYPE_ROPE && !symRopeTaut(found, o)) return null;
        return found;
    }

    boolean symLinkPairOk(PhysObj o, PhysObj peer) {
        if (o == null || peer == null || o == peer) return false;
        if (symWrapPushed(o) || symWrapPushed(peer)) return false;
        int rs = symWrapModeRank(o);
        int rp = symWrapModeRank(peer);
        if (rs != rp) return false;
        if (rs == 2) return false;
        return true;
    }

    boolean symLinkAtRest(PhysObj o, PhysObj peer) {
        if (symHeldStill(o) || symHeldStill(peer)) return true;
        if (symWrapModeRank(o) == 0 && !symSlidingNow(o) && !symSlidingNow(peer)) return true;
        return false;
    }

    SymExpr symLinkNetOf(PhysObj o) {
        return symSub(symWrapDriveOf(o), symWrapFrictionOf(o));
    }

    SymExpr symLinkAccelOf(PhysObj o) {
        PhysObj link = symLinkConstraintOf(o);
        if (link == null) return null;
        PhysObj peer = symWrapPeerOf(link, o);
        if (!symLinkPairOk(o, peer)) return null;
        if (symLinkAtRest(o, peer)) return symNum(0);
        boolean selfFirst = symWrapOrderFirst(o, peer);
        PhysObj oa = selfFirst ? o : peer;
        PhysObj ob = selfFirst ? peer : o;
        SymExpr den = symAdd(symQFor(oa, "m"), symQFor(ob, "m"));
        SymExpr num = symAdd(symWrapDriveOf(oa), symWrapDriveOf(ob));
        num = symSub(num, symWrapFrictionOf(oa));
        num = symSub(num, symWrapFrictionOf(ob));
        if (symIsZero(num)) return symNum(0);
        return symDiv(num, den);
    }

    SymExpr symLinkTensionOf(PhysObj o) {
        PhysObj link = symLinkConstraintOf(o);
        if (link == null) return null;
        PhysObj peer = symWrapPeerOf(link, o);
        if (!symLinkPairOk(o, peer)) return null;
        if (symLinkAtRest(o, peer)) return symNum(0);
        boolean selfFirst = symWrapOrderFirst(o, peer);
        PhysObj oa = selfFirst ? o : peer;
        PhysObj ob = selfFirst ? peer : o;
        SymExpr mA = symQFor(oa, "m");
        SymExpr mB = symQFor(ob, "m");
        SymExpr den = symAdd(mA, mB);
        SymExpr netA = symLinkNetOf(oa);
        SymExpr netB = symLinkNetOf(ob);
        SymExpr tAB = symDiv(symSub(symMul(mA, netB), symMul(mB, netA)), den);
        if (symBad(tAB)) return tAB;
        if (tAB.value >= -0.000001) return tAB;
        return symDiv(symSub(symMul(mB, netA), symMul(mA, netB)), den);
    }

    SymExpr symAccelOf(PhysObj o) {
        if (o == null || symKnown("a")) return symQ("a");
        SymExpr wrapAcc = symWrapAccelOf(o);
        if (wrapAcc != null) return wrapAcc;
        SymExpr linkAcc = symLinkAccelOf(o);
        if (linkAcc != null) return linkAcc;
        if (o.contactRamp) {
            SymExpr core = symSub(symTrig("sin", symQ("theta")),
                    symMul(symQ("mu"), symTrig("cos", symQ("theta"))));
            return symMul(symQ("g"), core);
        }
        boolean pushed = Math.abs(o.appliedForceX) > 0.001f || Math.abs(o.appliedForceY) > 0.001f;
        if (symHeldStill(o)) return symNum(0);
        if (o.contactGround) {
            if (pushed) return symDiv(symSub(symQ("F"), symFrictionOf(o)), symQ("m"));
            if (!symSlidingNow(o)) return symNum(0);
            return symSub(symNum(0), symMul(symQ("mu"), symQ("g")));
        }
        if (pushed) return symDiv(symQ("F"), symQ("m"));
        PhysObj holder = symAnchoredHolderOf(o);
        if (holder != null) {
            if (symConstraintAtRest(holder, o)) return symNum(0);
            return symMul(symQ("g"), symTrig("sin", symQ("theta")));
        }
        return symQ("g");
    }

    float[] symConstraintAnchorOf(PhysObj h, PhysObj o) {
        if (h == null || o == null) return null;
        float[] fixedPoint = constraintFixedAnchorPoint(h);
        if (fixedPoint != null) return fixedPoint;
        PhysObj other = h.connectedObjA == o ? h.connectedObjB : h.connectedObjA;
        if (other != null && other != o && other.fixed) return new float[]{other.x, other.y};
        return null;
    }

    boolean symRopeTaut(PhysObj h, PhysObj o) {
        if (h == null || o == null) return false;
        float rest = h.ropeLen;
        if (rest <= 0.001f) return false;
        float[] eps = getSpringEndpoints(h);
        if (eps == null || eps.length < 4) return false;
        float len = magnitude(eps[2] - eps[0], eps[3] - eps[1]);
        return len >= rest - Math.max(1.5f, rest * 0.03f);
    }

    boolean ropeSlackNow(PhysObj h) {
        if (h == null || h.type != TYPE_ROPE) return false;
        if (h.ropeWrapped) return false;
        float rest = h.ropeLen;
        if (rest <= 0.001f) return false;
        float[] eps = getSpringEndpoints(h);
        if (eps == null || eps.length < 4) return false;
        float len = magnitude(eps[2] - eps[0], eps[3] - eps[1]);
        return len < rest - Math.max(1.5f, rest * 0.03f);
    }

    boolean ropeBetweenFreeBodies(PhysObj h) {
        if (h == null) return false;
        if (h.fixed || h.ropeWrapped) return false;
        if (constraintFixedAnchorPoint(h) != null) return false;
        PhysObj a = h.connectedObjA;
        PhysObj b = h.connectedObjB;
        if (a == null || b == null) return false;
        if (a.fixed || b.fixed) return false;
        return true;
    }

    boolean ropeShowsTension(PhysObj h) {
        if (h == null) return false;
        if (h.type != TYPE_ROPE) return true;
        if (h.isCutHalf || h.cutDecaying) return false;
        if (ropeSlackNow(h)) return false;
        if (ropeBetweenFreeBodies(h)) return false;
        return true;
    }

    boolean symTensionCarrier(PhysObj o) {
        if (o == null) return false;
        if (Math.abs(o.constraintForceX) > 0.001f || Math.abs(o.constraintForceY) > 0.001f) return true;
        for (PhysObj h : objects) {
            if (!isConstraintBody(h)) continue;
            if (h.connectedObjA != o && h.connectedObjB != o) continue;
            if (h.isCutHalf || h.cutDecaying) continue;
            if (h.type != TYPE_ROPE) return true;
            if (h.ropeWrapped) return true;
            if (!ropeSlackNow(h)) return true;
        }
        return false;
    }

    PhysObj symAnchoredHolderOf(PhysObj o) {
        if (o == null || o.fixed) return null;
        if (isConstraintBody(o) || o.type == TYPE_RING) return null;
        for (PhysObj h : objects) {
            if (!isConstraintBody(h)) continue;
            if (h.connectedObjA != o && h.connectedObjB != o) continue;
            if (h.isCutHalf || h.cutDecaying || h.ropeWrapped) continue;
            if (symConstraintAnchorOf(h, o) == null) continue;
            if (h.type == TYPE_ROPE && !symRopeTaut(h, o)) continue;
            return h;
        }
        return null;
    }

    boolean symConstraintAtRest(PhysObj h, PhysObj o) {
        float[] anchor = symConstraintAnchorOf(h, o);
        if (anchor == null) return false;
        float speed = symSpeedOf(o);
        if (h.fixed || h.angleFixed || h.remoteLock) return speed <= 0.08f;
        if (speed > 0.08f) return false;
        float dx = o.x - anchor[0];
        float dy = o.y - anchor[1];
        float len = magnitude(dx, dy);
        if (len < 0.001f) return true;
        return Math.abs(dx) <= len * 0.05f;
    }

    String symStateSignature(PhysObj o) {
        if (o == null) return "";
        PhysObj prevCtx = symContextObj;
        symContextObj = o;
        StringBuilder sb = new StringBuilder();
        sb.append(o.fixed ? '1' : '0');
        sb.append(o.contactGround ? '1' : '0');
        sb.append(o.contactWallLeft ? '1' : '0');
        sb.append(o.contactWallRight ? '1' : '0');
        sb.append(o.contactRamp ? '1' : '0');
        sb.append((Math.abs(o.appliedForceX) > 0.001f || Math.abs(o.appliedForceY) > 0.001f) ? '1' : '0');
        sb.append('|');
        sb.append(symText(symAccelOf(o)));
        symContextObj = prevCtx;
        return sb.toString();
    }

    void symResetStateClock(PhysObj o, float t) {
        if (o == null) return;
        o.symStateKey = "";
        o.symStateTime = t < 0f ? 0f : t;
    }

    void symSyncStateClock(PhysObj o) {
        if (o == null) return;
        if (o.symStateTime > elapsedTime) o.symStateTime = elapsedTime;
        if (o.symStateTime < 0f) o.symStateTime = 0f;
        String key = symStateSignature(o);
        if (o.symStateKey == null || !o.symStateKey.equals(key)) {
            o.symStateKey = key;
            o.symStateTime = elapsedTime;
        }
    }

    void symUpdateStateClocks() {
        if (!symbolMode) return;
        for (PhysObj o : objects) {
            if (isConstraintBody(o)) continue;
            symSyncStateClock(o);
        }
    }

    float symSpeedOf(PhysObj o) {
        if (o == null) return 0f;
        double vx = o.physVx, vy = o.physVy;
        if (Math.abs(vx) < 1e-9 && Math.abs(vy) < 1e-9) {
            vx = o.vx;
            vy = o.vy;
        }
        double sp = Math.sqrt(vx * vx + vy * vy);
        if (pixelsPerMeter > 0.0001f) sp /= pixelsPerMeter;
        if (Double.isNaN(sp) || Double.isInfinite(sp)) return 0f;
        return (float) sp;
    }

    float symAccelMagOf(PhysObj o) {
        if (o == null) return 0f;
        double a = Math.sqrt(o.accelX * o.accelX + o.accelY * o.accelY);
        if (Double.isNaN(a) || Double.isInfinite(a)) return 0f;
        return (float) a;
    }

    boolean symSlidingNow(PhysObj o) {
        return o != null && !o.fixed && symSpeedOf(o) > 0.02f;
    }

    boolean symHeldStill(PhysObj o) {
        if (o == null) return false;
        if (o.fixed) return true;
        if (symSpeedOf(o) > 0.02f) return false;
        if (elapsedTime > 0.0001f) return symAccelMagOf(o) < 0.05f;
        return false;
    }

    boolean symV0Given(PhysObj o) {
        if (o == null) return false;
        if (o.symV0Set) return true;
        double sp = Math.sqrt(o.initialVx * o.initialVx + o.initialVy * o.initialVy);
        if (pixelsPerMeter > 0.0001f) sp /= pixelsPerMeter;
        return sp > 0.001;
    }

    SymExpr symV0Of(PhysObj o) {
        if (!symV0Given(o)) return symNum(0);
        return symQ("v0");
    }

    float symLocalTime(PhysObj o) {
        float t = elapsedTime;
        if (o != null) {
            if (o.symStateTime > elapsedTime) o.symStateTime = elapsedTime;
            if (o.symStateTime < 0f) o.symStateTime = 0f;
            t = elapsedTime - o.symStateTime;
        }
        if (Float.isNaN(t) || Float.isInfinite(t) || t < 0f) t = 0f;
        return t;
    }

    SymExpr symNowTime() {
        return symNowTime(symContextObj);
    }

    SymExpr symNowTime(PhysObj o) {
        float t = symLocalTime(o);
        double v = Math.round(t * 100.0) / 100.0;
        if (v < 0.005) return symNum(0);
        return new SymExpr(String.format("%.2f", v), v, 4);
    }

    boolean symTimeDriven(PhysObj o, String qty) {
        if (o == null) return true;
        if (o.fixed) return false;
        if ("x".equals(qty)) return symSlidingNow(o);
        return !symIsZero(symAccelOf(o));
    }

    SymExpr symTimeBound(PhysObj o, String qty, SymExpr live) {
        if (o == null) return live;
        Float ft = o.symFrozenTime.get(qty);
        if (ft != null && elapsedTime + 0.0001f < ft.floatValue()) {
            o.symFrozenTime.remove(qty);
            o.symFrozenExpr.remove(qty);
        }
        if (symTimeDriven(o, qty)) {
            o.symFrozenTime.put(qty, Float.valueOf(elapsedTime));
            o.symFrozenExpr.put(qty, live);
            return live;
        }
        SymExpr cached = o.symFrozenExpr.get(qty);
        return cached == null ? live : cached;
    }

    SymExpr symVelocityOf(PhysObj o) {
        SymExpr v0 = symV0Of(o);
        SymExpr acc = symAccelOf(o);
        SymExpr t = symNowTime(o);
        SymExpr neg = symStripNeg(acc);
        SymExpr live;
        if (neg != null) live = symSub(v0, symMul(neg, t));
        else live = symAdd(v0, symMul(acc, t));
        return symTimeBound(o, "v", live);
    }

    SymExpr symDisplacementOf(PhysObj o) {
        SymExpr t = symNowTime(o);
        SymExpr v0 = symV0Of(o);
        SymExpr acc = symAccelOf(o);
        SymExpr first = symMul(v0, t);
        SymExpr neg = symStripNeg(acc);
        SymExpr live;
        if (neg != null) live = symSub(first, symDiv(symMul(neg, symSq(t)), symNum(2)));
        else live = symAdd(first, symDiv(symMul(acc, symSq(t)), symNum(2)));
        return symTimeBound(o, "x", live);
    }

    SymExpr symConstraintOf(PhysObj o, boolean compression) {
        if (!compression) {
            SymExpr wrapT = symWrapTensionOf(o);
            if (wrapT != null) return wrapT;
        }
        SymExpr linkT = symLinkTensionOf(o);
        if (linkT != null) return linkT;
        if (!compression && !symTensionCarrier(o)) return symNum(0);
        SymExpr centripetal = symDiv(symMul(symQ("m"), symSq(symVelocityOf(o))), symQ("L"));
        SymExpr weightPart = symMul(symWeightOf(o), symTrig("cos", symQ("theta")));
        if (compression) return symSub(weightPart, centripetal);
        return symAdd(weightPart, centripetal);
    }

    String symText(SymExpr e) {
        return e == null ? "" : e.text;
    }

    String symArrowLabel(String base, PhysObj o, int kind) {
        if (!symbolMode || o == null) return base;
        PhysObj prevCtx = symContextObj;
        symContextObj = o;
        SymExpr e;
        String head = base;
        switch (kind) {
            case 0: e = symWeightOf(o); break;
            case 1: e = symNormalOf(o); break;
            case 2: e = symFrictionOf(o); break;
            case 3: e = symAppliedForceExpr(); head = symEffectiveSymbol(o, "F"); break;
            case 4: e = symConstraintOf(o, false); break;
            case 5: e = symConstraintOf(o, true); break;
            default:
                symContextObj = prevCtx;
                return base;
        }
        String r = symJoinLabel(head, e.text);
        symContextObj = prevCtx;
        return r;
    }

    String symStateText(PhysObj o) {
        PhysObj prevCtx = symContextObj;
        symContextObj = o;
        StringBuilder sb = new StringBuilder();
        sb.append("v=" + symText(symVelocityOf(o)));
        sb.append("\nx=" + symText(symDisplacementOf(o)));
        sb.append("\na=" + symText(symAccelOf(o)));
        symContextObj = prevCtx;
        return sb.toString();
    }

    String symTex(SymExpr e) {
        return e == null ? "" : e.tex;
    }

    String[][] symStateRows(PhysObj o) {
        PhysObj prevCtx = symContextObj;
        symContextObj = o;
        String[][] rows = new String[3][3];
        rows[0][0] = "v=";
        rows[0][1] = symTex(symVelocityOf(o));
        rows[0][2] = "";
        rows[1][0] = "x=";
        rows[1][1] = symTex(symDisplacementOf(o));
        rows[1][2] = "";
        rows[2][0] = "a=";
        rows[2][1] = symTex(symAccelOf(o));
        rows[2][2] = "";
        symContextObj = prevCtx;
        return rows;
    }

    String[][] numericStateRows(PhysObj o, double v, double displacement, double a) {
        boolean hasP = o.pressure > 0;
        String[][] rows = new String[hasP ? 4 : 3][3];
        rows[0][0] = "v=";
        rows[0][1] = toTexDisplay(v);
        rows[0][2] = "m/s";
        rows[1][0] = "x=";
        rows[1][1] = toTexDisplay(displacement);
        rows[1][2] = "m";
        rows[2][0] = "a=";
        rows[2][1] = toTexDisplay(a);
        rows[2][2] = "m/s²";
        if (hasP) {
            rows[3][0] = "P=";
            rows[3][1] = toTexDisplay(o.pressure);
            rows[3][2] = "Pa";
        }
        return rows;
    }

    String toTexDisplay(double value) {        try {
            return com.lie.acsm.math.SurdCalculator.toTexString(value, decimalPlaces);
        } catch (Exception e) {
            e.printStackTrace();
            return toSurdDisplay(value);
        }
    }

    String toSurdDisplay(double value) {
        try {
            String surdResult = com.lie.acsm.math.SurdCalculator.toSurdText(value, decimalPlaces);
            return surdResult;
        } catch (Exception e) {
            e.printStackTrace();
            if (decimalPlaces == 0) {
                return String.valueOf(Math.round(value));
            }
            String format = "%." + decimalPlaces + "f";
            return String.format(format, value);
        }
    }

    double bestPhysValue(float shown, double stored) {
        return shown == (float) stored ? stored : (double) shown;
    }

    void saveInitialStates() {
        simulationHistory.clear();
        lastStateSaveTime = -1f;
        for (PhysObj o : objects) {
            double bvx = bestPhysValue(o.vx, o.physVx);
            double bvy = bestPhysValue(o.vy, o.physVy);
            double bpx = bestPhysValue(o.x, o.physX);
            double bpy = bestPhysValue(o.y, o.physY);
            o.initialX = o.x; o.initialY = o.y; o.initialVx = bvx; o.initialVy = bvy;
            o.physX = bpx; o.physY = bpy; o.physVx = bvx; o.physVy = bvy;
            symResetStateClock(o, elapsedTime);
        }
        saveSimulationState();
        lastStateSaveTime = 0f;
    }

    void saveSimulationState() {
        SimulationState state = new SimulationState();
        state.time = elapsedTime;
        for (PhysObj o : objects) {
            state.states.put(o, new ObjState(o.x, o.y, o.vx, o.vy, o.rotation));
        }
        simulationHistory.add(state);
    }

    void restoreStateAtTime(float targetTime) {
        if (!simulationHistory.isEmpty()) {
            SimulationState latest = simulationHistory.get(0);
            SimulationState bestBefore = null;
            SimulationState exact = null;
            for (SimulationState s : simulationHistory) {
                if (s.time > latest.time) latest = s;
                if (Math.abs(s.time - targetTime) < 0.0005f) exact = s;
                if (s.time <= targetTime + 0.0005f && (bestBefore == null || s.time > bestBefore.time)) {
                    bestBefore = s;
                }
            }
            if (exact != null) {
                restoreSimulationState(exact);
                elapsedTime = targetTime;
                return;
            }
            if (bestBefore == null) {
                restoreSimulationState(simulationHistory.get(0));
                elapsedTime = simulationHistory.get(0).time;
            } else {
                restoreSimulationState(bestBefore);
                elapsedTime = bestBefore.time;
            }
            if (targetTime > elapsedTime + 0.0005f) {
                simulateUntilTime(targetTime);
            }
            return;
        }
        resetWaterSims();
        for (PhysObj o : objects) {
            o.x = o.initialX; o.y = o.initialY; o.vx = (float) o.initialVx; o.vy = (float) o.initialVy;
            o.physX = o.initialX; o.physY = o.initialY; o.physVx = o.initialVx; o.physVy = o.initialVy;
            o.rotation = 0; o.forceX = 0; o.forceY = 0; o.accelX = 0; o.accelY = 0;
            
            o.symFrozenExpr.clear(); o.symFrozenTime.clear();
            symResetStateClock(o, 0f);
        }
        elapsedTime = 0f;
        simulateUntilTime(targetTime);
    }

    void resetWaterSims() {
        for (int k = 0; k < objects.size(); k++) {
            PhysObj o = objects.get(k);
            if (o.water != null) o.water.init = false;
            o.waterPrevSubA = 0f;
            o.waterSubArea = 0f;
            o.pressure = 0f;
        }
    }

    void restoreSimulationState(SimulationState state) {
        resetWaterSims();
        for (PhysObj o : objects) {
            ObjState os = state.states.get(o);
            if (os != null) {
                o.x = os.x; o.y = os.y; o.vx = os.vx; o.vy = os.vy; o.rotation = os.rotation;
                o.physX = os.x; o.physY = os.y; o.physVx = os.vx; o.physVy = os.vy;
                o.forceX = 0; o.forceY = 0; o.displayForceX = 0; o.displayForceY = 0;
                o.constraintForceX = 0; o.constraintForceY = 0;
                o.supportForceX = 0; o.supportForceY = 0;
                o.rodSupportForceX = 0; o.rodSupportForceY = 0;
                o.frictionForceX = 0; o.frictionForceY = 0;
                o.reactionForceX = 0; o.reactionForceY = 0;
                o.springAtRest = false;
                
                o.accelX = 0; o.accelY = 0; o.forceTimer = 0f;
                o.contactGround = false; o.contactWallLeft = false; o.contactWallRight = false; o.contactRamp = false;
                o.symFrozenExpr.clear(); o.symFrozenTime.clear();
                symResetStateClock(o, state.time);
            }
        }
    }

    void simulateUntilTime(float targetTime) {
        while (elapsedTime < targetTime - 0.0005f) {
            float step = Math.min(FIXED_DT, targetTime - elapsedTime);
            stepSimulation(step);
            elapsedTime += step;
            if (elapsedTime - lastStateSaveTime >= 0.1f || targetTime - elapsedTime <= 0.0005f) {
                saveSimulationState();
                lastStateSaveTime = elapsedTime;
            }
        }
    }

    void updateSpringRope(PhysObj obj, float dt, List<PhysObj> toRemove) {
        obj.springAtRest = false;
        if (obj.type == TYPE_ROPE && !obj.ropeWrapped) {
            autoWrapRopeOnPulley(obj);
        }

        boolean hasA = obj.connectedObjA != null;
        boolean hasB = obj.connectedObjB != null;
        boolean hasCeiling = obj.connectedToCeiling && obj.ceilingSegmentId >= 0 &&
                obj.ceilingSegmentId < ceilingSegments.size();
        boolean hasGround = obj.connectedToGround;
        boolean hasWallLeft = obj.connectedToWallLeft;
        boolean hasWallRight = obj.connectedToWallRight;

        boolean hasFixedAnchor = hasCeiling || hasGround || hasWallLeft || hasWallRight;
        int connectedObjCount = (hasA ? 1 : 0) + (hasB ? 1 : 0);
        if (!hasFixedAnchor && connectedObjCount <= 1) {
            PhysObj solo = hasA ? obj.connectedObjA : (hasB ? obj.connectedObjB : null);
            if (solo == null) return;

            float origLen = obj.type == TYPE_SPRING ? obj.naturalLen
                    : (obj.type == TYPE_ROD ? obj.rodLen : obj.ropeLen);
            float freeMass = 0.05f;

            if (obj.freeDirX == 0 && obj.freeDirY == 0) {
                float fdx = obj.freeEndX - solo.x;
                float fdy = obj.freeEndY - solo.y;
                float flen = magnitude(fdx, fdy);
                if (flen > 0.001f) {
                    obj.freeDirX = fdx / flen;
                    obj.freeDirY = fdy / flen;
                } else {
                    obj.freeDirX = 0f;
                    obj.freeDirY = 1f;
                }
                obj.freeEndX = solo.x + obj.freeDirX * origLen;
                obj.freeEndY = solo.y + obj.freeDirY * origLen;
                if (obj.freeEndHasInitVel) {
                    obj.freeEndVx = obj.freeEndInitVx;
                    obj.freeEndVy = obj.freeEndInitVy;
                } else {
                    obj.freeEndVx = 0;
                    obj.freeEndVy = 0;
                }
            }

            boolean soloBeingDragged = dragging && selectedObj == solo;
            if (soloBeingDragged) {
                float fdx = obj.freeEndX - solo.x;
                float fdy = obj.freeEndY - solo.y;
                float flen = magnitude(fdx, fdy);
                if (flen > 0.001f) {
                    obj.freeEndX = solo.x + fdx / flen * origLen;
                    obj.freeEndY = solo.y + fdy / flen * origLen;
                } else {
                    obj.freeEndX = solo.x + obj.freeDirX * origLen;
                    obj.freeEndY = solo.y + obj.freeDirY * origLen;
                }
                obj.freeEndVx = 0; obj.freeEndVy = 0;
            }

            float dx2 = obj.freeEndX - solo.x;
            float dy2 = obj.freeEndY - solo.y;
            float dist2 = magnitude(dx2, dy2);
            if (dist2 < 0.001f) dist2 = 0.001f;
            float nx2 = dx2 / dist2, ny2 = dy2 / dist2;
            if (obj.angleFixed) {
                nx2 = (float) Math.cos(obj.fixedAngle);
                ny2 = (float) Math.sin(obj.fixedAngle);
                float along = dx2 * nx2 + dy2 * ny2;
                if (along < 0f) {
                    nx2 = -nx2;
                    ny2 = -ny2;
                    along = -along;
                }
                if (along < 0.001f) along = origLen;
                dist2 = along;
                obj.freeEndX = solo.x + nx2 * dist2;
                obj.freeEndY = solo.y + ny2 * dist2;
                dx2 = obj.freeEndX - solo.x;
                dy2 = obj.freeEndY - solo.y;
            }
            float stretch = dist2 - origLen;

            if (obj.type == TYPE_ROD) {
                obj.freeEndX = solo.x + nx2 * origLen;
                obj.freeEndY = solo.y + ny2 * origLen;
                obj.freeEndVx = 0;
                obj.freeEndVy = 0;
                obj.x = (solo.x + obj.freeEndX) / 2;
                obj.y = (solo.y + obj.freeEndY) / 2;
                obj.w = origLen;
                obj.h = obj.type == TYPE_SPRING ? 20 : (obj.type == TYPE_ROD ? 0f : 10);
                obj.rotation = resolveConstraintAngle(obj, nx2, ny2);
                obj.isHorizontal = true;
                obj.deformation = 0f;
                return;
            }

            if (obj.fixed) {
                if (dist2 > origLen) {
                    obj.freeEndX = solo.x + nx2 * origLen;
                    obj.freeEndY = solo.y + ny2 * origLen;
                }
                obj.freeEndVx = 0;
                obj.freeEndVy = 0;
                float fx2 = obj.freeEndX - solo.x;
                float fy2 = obj.freeEndY - solo.y;
                float fixedLen = magnitude(fx2, fy2);
                obj.x = (solo.x + obj.freeEndX) / 2;
                obj.y = (solo.y + obj.freeEndY) / 2;
                obj.w = Math.max(1f, fixedLen);
                obj.h = obj.type == TYPE_SPRING ? 20 : 10;
                obj.rotation = resolveConstraintAngle(obj, fx2, fy2);
                obj.isHorizontal = true;
                obj.deformation = 0f;
                return;
            }

            if (obj.type == TYPE_SPRING) {
                float forceMag = obj.springK * stretch;
                double accelX = -forceMag / freeMass * nx2
                        + obj.freeAppliedForceX * pixelsPerMeter / freeMass;
                double accelY = -forceMag / freeMass * ny2
                        + obj.freeAppliedForceY * pixelsPerMeter / freeMass
                        + GRAVITY * pixelsPerMeter;
                if (!constraintPostPass) {
                    obj.freeEndVx = (float) firstHalfKick(
                            obj.freeEndVx, accelX, dt);
                    obj.freeEndVy = (float) firstHalfKick(
                            obj.freeEndVy, accelY, dt);
                    obj.freeEndX = (float) advancePosition(
                            obj.freeEndX, obj.freeEndVx, dt);
                    obj.freeEndY = (float) advancePosition(
                            obj.freeEndY, obj.freeEndVy, dt);
                } else if (!constraintPoseRefresh) {
                    obj.freeEndVx = (float) secondHalfKick(
                            obj.freeEndVx, accelX, dt);
                    obj.freeEndVy = (float) secondHalfKick(
                            obj.freeEndVy, accelY, dt);
                }
                if (!solo.fixed && !soloBeingDragged) {
                    float fx = forceMag * nx2;
                    float fy = forceMag * ny2;
                    solo.forceX += fx;
                    solo.forceY += fy;
                    addRodDisplayForce(solo, fx, fy, forceMag >= 0f);
                }
            } else {
                if (stretch > 0) {
                    if (!constraintPoseRefresh) {
                        float vRel = (obj.freeEndVx - solo.vx) * nx2 + (obj.freeEndVy - solo.vy) * ny2;
                        if (vRel > 0) {
                            float impulse = vRel / (1f / freeMass + (solo.fixed ? 0 : 1f / solo.mass));
                            obj.freeEndVx -= impulse / freeMass * nx2;
                            obj.freeEndVy -= impulse / freeMass * ny2;
                            if (!solo.fixed && !soloBeingDragged) {
                                solo.vx += impulse / solo.mass * nx2;
                                solo.vy += impulse / solo.mass * ny2;
                            }
                        }
                    }
                    obj.freeEndX = solo.x + nx2 * origLen;
                    obj.freeEndY = solo.y + ny2 * origLen;
                }
            }

            if (obj.type == TYPE_ROPE && !constraintPoseRefresh) {
                double accelX = obj.freeAppliedForceX * pixelsPerMeter / freeMass;
                double accelY = obj.freeAppliedForceY * pixelsPerMeter / freeMass
                        + GRAVITY * pixelsPerMeter;
                if (!constraintPostPass) {
                    obj.freeEndVx = (float) firstHalfKick(
                            obj.freeEndVx, accelX, dt);
                    obj.freeEndVy = (float) firstHalfKick(
                            obj.freeEndVy, accelY, dt);
                    obj.freeEndX = (float) advancePosition(
                            obj.freeEndX, obj.freeEndVx, dt);
                    obj.freeEndY = (float) advancePosition(
                            obj.freeEndY, obj.freeEndVy, dt);
                } else {
                    obj.freeEndVx = (float) secondHalfKick(
                            obj.freeEndVx, accelX, dt);
                    obj.freeEndVy = (float) secondHalfKick(
                            obj.freeEndVy, accelY, dt);
                }
            }

            if (obj.freeEndY > groundY - 3) {
                obj.freeEndY = groundY - 3;
                obj.freeEndVy = 0;
            }
            if (obj.freeEndX < wallLeftX + 2) { obj.freeEndX = wallLeftX + 2; if (obj.freeEndVx < 0) obj.freeEndVx = 0; }
            if (obj.freeEndX > wallRightX - 2) { obj.freeEndX = wallRightX - 2; if (obj.freeEndVx > 0) obj.freeEndVx = 0; }

            if (obj.type == TYPE_SPRING) {
                for (PhysObj other : objects) {
                    if (other == obj || other == solo || isConstraintBody(other)) continue;
                    float onx = 0, ony = 0, open = 0;
                    if (other.type == TYPE_BALL || other.type == TYPE_PULLEY) {
                        float pdx = obj.freeEndX - other.x, pdy = obj.freeEndY - other.y;
                        float dist = magnitude(pdx, pdy);
                        float minD = halfWidth(other) + 5;
                        if (dist < minD && dist > 0.001f) { onx = pdx / dist; ony = pdy / dist; open = minD - dist; }
                    } else if (other.type != TYPE_RAMP_STRAIGHT && other.type != TYPE_RAMP_ARC) {
                        float oL = other.x - halfWidth(other) - 5, oR = other.x + halfWidth(other) + 5;
                        float oT = other.y - halfHeight(other) - 5, oB = other.y + halfHeight(other) + 5;
                        if (obj.freeEndX > oL && obj.freeEndX < oR && obj.freeEndY > oT && obj.freeEndY < oB) {
                            float dL = obj.freeEndX - oL, dR = oR - obj.freeEndX, dT = obj.freeEndY - oT, dB = oB - obj.freeEndY;
                            float minP = Math.min(Math.min(dL, dR), Math.min(dT, dB));
                            if (minP == dL) { onx = -1; ony = 0; }
                            else if (minP == dR) { onx = 1; ony = 0; }
                            else if (minP == dT) { onx = 0; ony = -1; }
                            else { onx = 0; ony = 1; }
                            open = minP;
                        }
                    }
                    if (open > 0) {
                        obj.freeEndX += onx * open; obj.freeEndY += ony * open;
                        float vn = obj.freeEndVx * onx + obj.freeEndVy * ony;
                        if (vn < 0) { obj.freeEndVx -= vn * onx; obj.freeEndVy -= vn * ony; }
                    }
                }
            }

            if (obj.angleFixed) {
                float fx = (float) Math.cos(obj.fixedAngle);
                float fy = (float) Math.sin(obj.fixedAngle);
                float along = (obj.freeEndX - solo.x) * fx + (obj.freeEndY - solo.y) * fy;
                if (along < 0f) {
                    fx = -fx;
                    fy = -fy;
                    along = -along;
                }
                if (along < 0.001f) along = origLen;
                if (obj.type == TYPE_ROPE) along = origLen;
                float vAlong = obj.freeEndVx * fx + obj.freeEndVy * fy;
                obj.freeEndX = solo.x + fx * along;
                obj.freeEndY = solo.y + fy * along;
                obj.freeEndVx = fx * vAlong;
                obj.freeEndVy = fy * vAlong;
            }

            dx2 = obj.freeEndX - solo.x; dy2 = obj.freeEndY - solo.y;
            float len3 = magnitude(dx2, dy2);
            if (len3 > 0.001f) { obj.freeDirX = dx2 / len3; obj.freeDirY = dy2 / len3; }

            obj.x = (solo.x + obj.freeEndX) / 2;
            obj.y = (solo.y + obj.freeEndY) / 2;
            obj.w = Math.max(1f, len3);
            obj.h = obj.type == TYPE_SPRING ? 20 : 10;
            obj.rotation = resolveConstraintAngle(obj, dx2, dy2);
            obj.isHorizontal = true;
            return;
        }

        float x1, y1, x2, y2;
        PhysObj objAtX1 = null, objAtX2 = null;

        if (hasA && hasB) {
            x1 = obj.connectedObjA.x; y1 = obj.connectedObjA.y;
            x2 = obj.connectedObjB.x; y2 = obj.connectedObjB.y;
            objAtX1 = obj.connectedObjA; objAtX2 = obj.connectedObjB;
        } else if (hasA && hasCeiling) {
            CeilingSegment cs = ceilingSegments.get(obj.ceilingSegmentId);
            x1 = obj.ceilingAnchorX != 0 ? obj.ceilingAnchorX : (cs.x1 + cs.x2) / 2; y1 = cs.y1;
            x2 = obj.connectedObjA.x; y2 = obj.connectedObjA.y;
            objAtX2 = obj.connectedObjA;
        } else if (hasB && hasCeiling) {
            CeilingSegment cs = ceilingSegments.get(obj.ceilingSegmentId);
            x1 = obj.ceilingAnchorX != 0 ? obj.ceilingAnchorX : (cs.x1 + cs.x2) / 2; y1 = cs.y1;
            x2 = obj.connectedObjB.x; y2 = obj.connectedObjB.y;
            objAtX2 = obj.connectedObjB;
        } else if (hasA && hasGround) {
            x1 = obj.groundAnchorX; y1 = groundY;
            x2 = obj.connectedObjA.x; y2 = obj.connectedObjA.y;
            objAtX2 = obj.connectedObjA;
        } else if (hasB && hasGround) {
            x1 = obj.groundAnchorX; y1 = groundY;
            x2 = obj.connectedObjB.x; y2 = obj.connectedObjB.y;
            objAtX2 = obj.connectedObjB;
        } else if (hasA && hasWallLeft) {
            x1 = wallLeftX; y1 = obj.wallAnchorY;
            x2 = obj.connectedObjA.x; y2 = obj.connectedObjA.y;
            objAtX2 = obj.connectedObjA;
        } else if (hasA && hasWallRight) {
            x1 = wallRightX; y1 = obj.wallAnchorY;
            x2 = obj.connectedObjA.x; y2 = obj.connectedObjA.y;
            objAtX2 = obj.connectedObjA;
        } else if (hasB && hasWallLeft) {
            x1 = wallLeftX; y1 = obj.wallAnchorY;
            x2 = obj.connectedObjB.x; y2 = obj.connectedObjB.y;
            objAtX2 = obj.connectedObjB;
        } else if (hasB && hasWallRight) {
            x1 = wallRightX; y1 = obj.wallAnchorY;
            x2 = obj.connectedObjB.x; y2 = obj.connectedObjB.y;
            objAtX2 = obj.connectedObjB;
        } else if (hasA) {
            x1 = obj.x; y1 = obj.y;
            x2 = obj.connectedObjA.x; y2 = obj.connectedObjA.y;
            objAtX2 = obj.connectedObjA;
        } else if (hasB) {
            x1 = obj.x; y1 = obj.y;
            x2 = obj.connectedObjB.x; y2 = obj.connectedObjB.y;
            objAtX2 = obj.connectedObjB;
        } else if (hasCeiling) {
            CeilingSegment cs = ceilingSegments.get(obj.ceilingSegmentId);
            x1 = obj.ceilingAnchorX != 0 ? obj.ceilingAnchorX : (cs.x1 + cs.x2) / 2; y1 = cs.y1;
            x2 = obj.x; y2 = obj.y;
        } else if (hasGround) {
            x1 = obj.groundAnchorX; y1 = groundY;
            x2 = obj.x; y2 = obj.y;
        } else if (hasWallLeft) {
            x1 = wallLeftX; y1 = obj.wallAnchorY;
            x2 = obj.x; y2 = obj.y;
        } else if (hasWallRight) {
            x1 = wallRightX; y1 = obj.wallAnchorY;
            x2 = obj.x; y2 = obj.y;
        } else {
            return;
        }

        if (objAtX1 != null) {
            float[] sp = getSurfacePoint(objAtX1, x2, y2);
            x1 = sp[0]; y1 = sp[1];
        }
        if (objAtX2 != null) {
            float[] sp = getSurfacePoint(objAtX2, x1, y1);
            x2 = sp[0]; y2 = sp[1];
        }

        if (usesAnalyticWrap(obj)) {
            solveWrappedRopeConstraint(obj, x1, y1, x2, y2, objAtX1, objAtX2);
            return;
        }

        if (obj.type == TYPE_ROD && obj.remoteLock && hasFixedAnchor && connectedObjCount == 1) {
            PhysObj lockedObj = hasA ? obj.connectedObjA : obj.connectedObjB;
            float dxr = lockedObj.x - x1;
            float dyr = lockedObj.y - y1;
            float dlen = magnitude(dxr, dyr);
            if (obj.angleFixed) {
                dxr = (float) Math.cos(obj.fixedAngle);
                dyr = (float) Math.sin(obj.fixedAngle);
                dlen = 1f;
            } else if (dlen < 0.001f) {
                dxr = (float) Math.cos(obj.rotation);
                dyr = (float) Math.sin(obj.rotation);
                dlen = 1f;
            }
            float ux = dxr / dlen;
            float uy = dyr / dlen;
            x2 = x1 + ux * obj.rodLen;
            y2 = y1 + uy * obj.rodLen;
            obj.x = (x1 + x2) / 2;
            obj.y = (y1 + y2) / 2;
            obj.rotation = resolveConstraintAngle(obj, x2 - x1, y2 - y1);
            obj.isHorizontal = true;
            obj.w = obj.rodLen;
            obj.h = 0f;
            obj.fixed = false;
            obj.deformation = 0f;
            return;
        }

        if (obj.type == TYPE_ROD && obj.remoteLock && hasA && hasB) {
            float dxr = obj.connectedObjB.x - obj.connectedObjA.x;
            float dyr = obj.connectedObjB.y - obj.connectedObjA.y;
            float dlen = magnitude(dxr, dyr);
            if (dlen < 0.001f) {
                dxr = (float) Math.cos(obj.rotation);
                dyr = (float) Math.sin(obj.rotation);
                dlen = 1f;
            }
            if (obj.angleFixed) {
                dxr = (float) Math.cos(obj.fixedAngle);
                dyr = (float) Math.sin(obj.fixedAngle);
                dlen = 1f;
            }
            float ux = dxr / dlen;
            float uy = dyr / dlen;
            float[] sp = getSurfacePoint(obj.connectedObjA, obj.connectedObjB.x, obj.connectedObjB.y);
            x1 = sp[0];
            y1 = sp[1];
            x2 = x1 + ux * obj.rodLen;
            y2 = y1 + uy * obj.rodLen;
            obj.x = (x1 + x2) / 2;
            obj.y = (y1 + y2) / 2;
            obj.rotation = resolveConstraintAngle(obj, x2 - x1, y2 - y1);
            obj.isHorizontal = true;
            obj.w = obj.rodLen;
            obj.h = 0f;
            obj.fixed = false;
            obj.deformation = 0f;
            return;
        }

        if (obj.angleFixed) {
            float ux = (float) Math.cos(obj.fixedAngle);
            float uy = (float) Math.sin(obj.fixedAngle);
            float span = (x2 - x1) * ux + (y2 - y1) * uy;
            if (span < 0f) {
                ux = -ux;
                uy = -uy;
                span = -span;
            }
            float curSpan = magnitude(x2 - x1, y2 - y1);
            if (span < 0.001f) span = curSpan;
            if (obj.type == TYPE_ROPE) span = obj.ropeLen;
            float targetX2 = x1 + ux * span;
            float targetY2 = y1 + uy * span;
            if (objAtX2 != null && !objAtX2.fixed) {
                objAtX2.x += targetX2 - x2;
                objAtX2.y += targetY2 - y2;
                projectVelocityAlong(objAtX2, ux, uy);
                syncPhysState(objAtX2);
                x2 = targetX2;
                y2 = targetY2;
            } else if (objAtX1 != null && !objAtX1.fixed) {
                float targetX1 = x2 - ux * span;
                float targetY1 = y2 - uy * span;
                objAtX1.x += targetX1 - x1;
                objAtX1.y += targetY1 - y1;
                projectVelocityAlong(objAtX1, ux, uy);
                syncPhysState(objAtX1);
                x1 = targetX1;
                y1 = targetY1;
            } else {
                x2 = targetX2;
                y2 = targetY2;
            }
        }

        boolean isSingleAnchor = !hasA && !hasB && (hasCeiling || hasGround || hasWallLeft || hasWallRight);
        if (isSingleAnchor && obj.type == TYPE_ROD) {
            float ux, uy;
            if (hasCeiling) { ux = 0; uy = 1; }
            else if (hasGround) { ux = 0; uy = -1; }
            else if (hasWallLeft) { ux = 1; uy = 0; }
            else { ux = -1; uy = 0; }
            x2 = x1 + ux * obj.rodLen;
            y2 = y1 + uy * obj.rodLen;
            float ddx = x2 - x1, ddy = y2 - y1;
            float cl = magnitude(ddx, ddy);
            obj.x = (x1 + x2) / 2; obj.y = (y1 + y2) / 2;
            obj.rotation = resolveConstraintAngle(obj, ddx, ddy);
            obj.w = cl; obj.h = 0f;
            obj.isHorizontal = true;
            return;
        }
        if (isSingleAnchor) {
            float k = Math.max(1f, obj.springK);
            float elongation = obj.fixed ? 0f : (obj.mass * GRAVITY / (2f * k)) * pixelsPerMeter;
            if (obj.type == TYPE_ROPE) {
                if (hasCeiling) {
                    float dispLen = obj.ropeLen + elongation;
                    x2 = x1; y2 = y1 + dispLen;
                } else if (hasGround) {
                    if (obj.fixed) {
                        obj.cutDecaying = false;
                        obj.cutDecayTimer = obj.ropeLen;
                        x2 = x1; y2 = y1 - obj.ropeLen;
                    } else {
                        if (!obj.cutDecaying) {
                            obj.cutDecaying = true;
                            obj.cutDecayTimer = Math.max(1f, obj.ropeLen);
                        }
                        obj.cutDecayTimer -= RETRACT_SPEED * dt;
                        if (obj.cutDecayTimer < 1f) {
                            toRemove.add(obj);
                            return;
                        }
                        x2 = x1; y2 = y1 - obj.cutDecayTimer;
                    }
                } else {
                    float tinyContract = elongation * 0.3f;
                    float dispLen = Math.max(1f, obj.ropeLen - tinyContract);
                    x2 = hasWallLeft ? x1 + dispLen : x1 - dispLen;
                    y2 = y1;
                }
            } else {
                float ux, uy, defLen;
                if (hasCeiling) {
                    ux = 0; uy = 1; defLen = obj.naturalLen + elongation;
                } else if (hasGround) {
                    ux = 0; uy = -1; defLen = Math.max(1f, obj.naturalLen - elongation);
                } else if (hasWallLeft) {
                    ux = 1; uy = 0; defLen = obj.naturalLen;
                } else {
                    ux = -1; uy = 0; defLen = obj.naturalLen;
                }
                float dispLen = defLen;
                float forceLimit = Math.min(defLen, obj.naturalLen);
                float forceLen = forceLimit;
                PhysObj forceObj = null;
                float halfStep = constraintPostPass ? 0f : dt * 0.5f;
                for (PhysObj other : objects) {
                    if (other == obj || other.fixed) continue;
                    if (isConstraintBody(other)) continue;
                    if (other.type == TYPE_RAMP_STRAIGHT || other.type == TYPE_RAMP_ARC) continue;
                    float ohw = other.w / 2;
                    float ohh = other.type == TYPE_BALL ? other.w / 2 : other.h / 2;
                    float relCx = other.x - x1, relCy = other.y - y1;
                    float along = relCx * ux + relCy * uy;
                    float lateral = Math.abs(relCy * ux - relCx * uy);
                    float halfAlong = (ux != 0) ? ohw : ohh;
                    float halfLat = (ux != 0) ? ohh : ohw;
                    if (lateral > halfLat + 14f) continue;
                    if (along <= 0) continue;
                    float nearDist = Math.max(8f, along - halfAlong);
                    if (nearDist < dispLen) {
                        dispLen = nearDist;
                    }
                    float midAlong = along + (other.vx * ux + other.vy * uy) * halfStep;
                    if (midAlong <= 0) continue;
                    float midNear = Math.max(8f, midAlong - halfAlong);
                    if (midNear < forceLen) {
                        forceLen = midNear;
                        forceObj = other;
                    }
                }
                if (forceObj != null && forceLen < obj.naturalLen) {
                    float compress = obj.naturalLen - forceLen;
                    float mag = Math.min(Math.max(1f, obj.ropeElasticLimit * pixelsPerMeter), obj.springK * compress);
                    forceObj.forceX += ux * mag;
                    forceObj.forceY += uy * mag;
                    forceObj.constraintForceX += ux * mag;
                    forceObj.constraintForceY += uy * mag;
                }
                x2 = x1 + ux * dispLen;
                y2 = y1 + uy * dispLen;
            }
            float ddx = x2 - x1, ddy = y2 - y1;
            float cl = magnitude(ddx, ddy);
            obj.x = (x1 + x2) / 2; obj.y = (y1 + y2) / 2;
            obj.rotation = resolveConstraintAngle(obj, ddx, ddy);
            obj.w = cl; obj.h = obj.type == TYPE_SPRING ? 20 : 10;
            obj.isHorizontal = true;
            return;
        }

        float dx = x2 - x1, dy = y2 - y1;
        float currentLen = magnitude(dx, dy);
        if (currentLen < 0.001f) return;

        if (obj.type == TYPE_ROD) {
            float restLen = obj.rodLen;
            float nxr = dx / currentLen, nyr = dy / currentLen;
            float err = currentLen - restLen;
            float rodTolerance = Math.max(0.01f, pixelsPerMeter * 0.0002f);
            boolean m1 = objAtX1 != null && !objAtX1.fixed;
            boolean m2 = objAtX2 != null && !objAtX2.fixed;
            if (obj.angleFixed && hasFixedAnchor && connectedObjCount == 1 && m2) {
                float bx = baseForceX(objAtX2);
                float by = baseForceY(objAtX2);
                float rx = -bx;
                float ry = -by;
                addRodForce(objAtX2, rx, ry, x1, y1, obj);
                objAtX2.vx = 0f;
                objAtX2.vy = 0f;
                objAtX2.physVx = 0f;
                objAtX2.physVy = 0f;
                syncPhysState(objAtX2);
                float[] sp2 = getSurfacePoint(objAtX2, x1, y1);
                x2 = sp2[0];
                y2 = sp2[1];
                float rdx0 = x2 - x1;
                float rdy0 = y2 - y1;
                float displayLen0 = magnitude(rdx0, rdy0);
                if (displayLen0 < 0.001f) displayLen0 = restLen;
                obj.x = (x1 + x2) / 2;
                obj.y = (y1 + y2) / 2;
                obj.rotation = resolveConstraintAngle(obj, rdx0, rdy0);
                obj.isHorizontal = true;
                obj.w = displayLen0;
                obj.h = 0f;
                obj.fixed = false;
                obj.deformation = 0f;
                return;
            }
            boolean correctRodPosition = Math.abs(err) > rodTolerance;
            float perpX = -nyr, perpY = nxr;
            float spanDx = (objAtX2 != null ? objAtX2.x : x2) - (objAtX1 != null ? objAtX1.x : x1);
            float spanDy = (objAtX2 != null ? objAtX2.y : y2) - (objAtX1 != null ? objAtX1.y : y1);
            float rodSpan = Math.max(1f, magnitude(spanDx, spanDy));
            float rodEps = Math.max(0.001f, pixelsPerMeter * 0.001f);
            if (obj.chainSolved) {
                float chainLam = obj.chainTension;
                if (m1) addRodForce(objAtX1, chainLam * nxr, chainLam * nyr, x2, y2, obj);
                if (m2) addRodForce(objAtX2, -chainLam * nxr, -chainLam * nyr, x1, y1, obj);
            } else if (m2 && !m1) {
                float fAlong = baseForceX(objAtX2) * nxr + baseForceY(objAtX2) * nyr;
                float vt = objAtX2.vx * perpX + objAtX2.vy * perpY;
                float react = -fAlong - objAtX2.mass * vt * vt / rodSpan;
                if (Math.abs(react) > rodEps) {
                    addRodForce(objAtX2, react * nxr, react * nyr, x1, y1, obj);
                }
            } else if (m1 && !m2) {
                float fAlong = -(baseForceX(objAtX1) * nxr + baseForceY(objAtX1) * nyr);
                float vt = objAtX1.vx * perpX + objAtX1.vy * perpY;
                float react = -fAlong - objAtX1.mass * vt * vt / rodSpan;
                if (Math.abs(react) > rodEps) {
                    addRodForce(objAtX1, -react * nxr, -react * nyr, x2, y2, obj);
                }
            } else if (m1 && m2) {
                boolean base1 = rodAnchorLike(objAtX1);
                boolean base2 = rodAnchorLike(objAtX2);
                if (base1 != base2) {
                    PhysObj freeEnd = base1 ? objAtX2 : objAtX1;
                    float sgn = base1 ? 1f : -1f;
                    float fAlong = sgn * (baseForceX(freeEnd) * nxr + baseForceY(freeEnd) * nyr);
                    float vt = freeEnd.vx * perpX + freeEnd.vy * perpY;
                    float react = -fAlong - freeEnd.mass * vt * vt / rodSpan;
                    if (Math.abs(react) > rodEps) {
                        float otherX = freeEnd == objAtX1 ? x2 : x1;
                        float otherY = freeEnd == objAtX1 ? y2 : y1;
                        addRodForce(freeEnd, sgn * react * nxr, sgn * react * nyr, otherX, otherY, obj);
                    }
                } else {
                    float accel1 = (baseForceX(objAtX1) * nxr + baseForceY(objAtX1) * nyr) / objAtX1.mass;
                    float accel2 = (baseForceX(objAtX2) * nxr + baseForceY(objAtX2) * nyr) / objAtX2.mass;
                    float vRelPerp = (objAtX2.vx - objAtX1.vx) * perpX + (objAtX2.vy - objAtX1.vy) * perpY;
                    float forceMag = (accel2 - accel1 + vRelPerp * vRelPerp / rodSpan)
                            / (1f / objAtX1.mass + 1f / objAtX2.mass);
                    if (Math.abs(forceMag) > rodEps) {
                        addRodForce(objAtX1, forceMag * nxr, forceMag * nyr, x2, y2, obj);
                        addRodForce(objAtX2, -forceMag * nxr, -forceMag * nyr, x1, y1, obj);
                    }
                }
            }
            if (m1 && m2) {
                if (correctRodPosition) {
                    float tm = objAtX1.mass + objAtX2.mass;
                    objAtX1.x += nxr * err * (objAtX2.mass / tm);
                    objAtX1.y += nyr * err * (objAtX2.mass / tm);
                    objAtX2.x -= nxr * err * (objAtX1.mass / tm);
                    objAtX2.y -= nyr * err * (objAtX1.mass / tm);
                }
                float relVn = (objAtX2.vx - objAtX1.vx) * nxr + (objAtX2.vy - objAtX1.vy) * nyr;
                float corr = relVn / (1f / objAtX1.mass + 1f / objAtX2.mass);
                objAtX1.vx += corr / objAtX1.mass * nxr;
                objAtX1.vy += corr / objAtX1.mass * nyr;
                objAtX2.vx -= corr / objAtX2.mass * nxr;
                objAtX2.vy -= corr / objAtX2.mass * nyr;
            } else if (m2) {
                if (correctRodPosition) {
                    objAtX2.x -= nxr * err;
                    objAtX2.y -= nyr * err;
                }
                float vn = objAtX2.vx * nxr + objAtX2.vy * nyr;
                objAtX2.vx -= vn * nxr;
                objAtX2.vy -= vn * nyr;
            } else if (m1) {
                if (correctRodPosition) {
                    objAtX1.x += nxr * err;
                    objAtX1.y += nyr * err;
                }
                float vn = objAtX1.vx * nxr + objAtX1.vy * nyr;
                objAtX1.vx -= vn * nxr;
                objAtX1.vy -= vn * nyr;
            }
            if (m1) syncPhysState(objAtX1);
            if (m2) syncPhysState(objAtX2);
            if (objAtX1 != null && objAtX2 != null) {
                float[] sp1 = getSurfacePoint(objAtX1, objAtX2.x, objAtX2.y);
                float[] sp2 = getSurfacePoint(objAtX2, objAtX1.x, objAtX1.y);
                x1 = sp1[0]; y1 = sp1[1];
                x2 = sp2[0]; y2 = sp2[1];
            } else {
                x2 = x1 + nxr * restLen;
                y2 = y1 + nyr * restLen;
            }
            float rdx = x2 - x1, rdy = y2 - y1;
            float displayLen = magnitude(rdx, rdy);
            if (displayLen < 0.001f) displayLen = restLen;
            obj.x = (x1 + x2) / 2;
            obj.y = (y1 + y2) / 2;
            obj.rotation = resolveConstraintAngle(obj, rdx, rdy);
            obj.isHorizontal = true;
            obj.w = displayLen;
            obj.h = 0f;
            obj.fixed = false;
            obj.deformation = 0f;
            return;
        }

        if (obj.fixed) {
            float restLen = obj.type == TYPE_SPRING ? obj.naturalLen
                    : obj.ropeLen;
            float nxr = dx / currentLen, nyr = dy / currentLen;
            float err = currentLen - restLen;
            boolean m1 = objAtX1 != null && !objAtX1.fixed;
            boolean m2 = objAtX2 != null && !objAtX2.fixed;
            if (err > 0f && m1 && m2) {
                float tm = objAtX1.mass + objAtX2.mass;
                objAtX1.x += nxr * err * (objAtX2.mass / tm);
                objAtX1.y += nyr * err * (objAtX2.mass / tm);
                objAtX2.x -= nxr * err * (objAtX1.mass / tm);
                objAtX2.y -= nyr * err * (objAtX1.mass / tm);
                float relVn = (objAtX2.vx - objAtX1.vx) * nxr + (objAtX2.vy - objAtX1.vy) * nyr;
                if (relVn > 0f) {
                    float corr = relVn / (1f / objAtX1.mass + 1f / objAtX2.mass);
                    objAtX1.vx += corr / objAtX1.mass * nxr;
                    objAtX1.vy += corr / objAtX1.mass * nyr;
                    objAtX2.vx -= corr / objAtX2.mass * nxr;
                    objAtX2.vy -= corr / objAtX2.mass * nyr;
                }
            } else if (err > 0f && m2) {
                objAtX2.x -= nxr * err;
                objAtX2.y -= nyr * err;
                float vn = objAtX2.vx * nxr + objAtX2.vy * nyr;
                if (vn > 0f) {
                    objAtX2.vx -= vn * nxr;
                    objAtX2.vy -= vn * nyr;
                }
            } else if (err > 0f && m1) {
                objAtX1.x += nxr * err;
                objAtX1.y += nyr * err;
                float vn = objAtX1.vx * nxr + objAtX1.vy * nyr;
                if (vn < 0f) {
                    objAtX1.vx -= vn * nxr;
                    objAtX1.vy -= vn * nyr;
                }
            }
            if (m1) syncPhysState(objAtX1);
            if (m2) syncPhysState(objAtX2);
            if (objAtX1 != null && objAtX2 != null) {
                float[] sp1 = getSurfacePoint(objAtX1, objAtX2.x, objAtX2.y);
                float[] sp2 = getSurfacePoint(objAtX2, objAtX1.x, objAtX1.y);
                x1 = sp1[0]; y1 = sp1[1];
                x2 = sp2[0]; y2 = sp2[1];
            } else if (err > 0f) {
                x2 = x1 + nxr * restLen;
                y2 = y1 + nyr * restLen;
            }
            obj.x = (x1 + x2) / 2;
            obj.y = (y1 + y2) / 2;
            float ddx = x2 - x1, ddy = y2 - y1;
            float displayLen = magnitude(ddx, ddy);
            if (displayLen < 0.001f) displayLen = restLen;
            obj.rotation = resolveConstraintAngle(obj, ddx, ddy);
            obj.isHorizontal = true;
            obj.w = displayLen;
            obj.h = obj.type == TYPE_SPRING ? 20 : 10;
            obj.deformation = 0f;
            return;
        }

        if (obj.type == TYPE_SPRING) {
            if (obj.cutDecaying) {
                obj.cutDecayTimer = Math.max(0f, obj.cutDecayTimer - RETRACT_SPEED * dt);
                obj.naturalLen = obj.cutDecayTimer;
                if (obj.cutDecayTimer < 1f || currentLen < 8f) {
                    obj.connectedObjA = null; obj.connectedObjB = null;
                    obj.connectedToCeiling = false; obj.connectedToGround = false;
                    obj.connectedToWallLeft = false; obj.connectedToWallRight = false;
                    return;
                }
            }
            float naturalLen = obj.naturalLen;
            float stretch = currentLen - naturalLen;
            float deformDenom = naturalLen > 0.1f ? naturalLen : 0.1f;
            obj.deformation = stretch / deformDenom;

            float maxStretchPx = (obj.springK > 0) ? (obj.ropeElasticLimit / obj.springK) * pixelsPerMeter : Float.MAX_VALUE;
            if (stretch > maxStretchPx) {
                toRemove.add(obj);
                return;
            }

            float forceMagnitude = obj.springK * stretch;
            float nx = dx / currentLen;
            float ny = dy / currentLen;
            obj.springAtRest = forceMagnitude == 0f;
            float fx = forceMagnitude * nx;
            float fy = forceMagnitude * ny;

            if (forceMagnitude != 0f) {
                if (objAtX1 != null && !objAtX1.fixed) {
                    objAtX1.forceX += fx; objAtX1.forceY += fy;
                    addRodDisplayForce(objAtX1, fx, fy, forceMagnitude >= 0f);
                }
                if (objAtX2 != null && !objAtX2.fixed) {
                    objAtX2.forceX -= fx; objAtX2.forceY -= fy;
                    addRodDisplayForce(objAtX2, -fx, -fy, forceMagnitude >= 0f);
                }
            }
} else if (obj.type == TYPE_ROPE) {
    float maxStretchPx = (obj.springK > 0) ? (obj.ropeElasticLimit / obj.springK) * pixelsPerMeter : obj.ropeLen * 4f;
    if (currentLen - obj.ropeLen > maxStretchPx) {
        toRemove.add(obj);
        return;
    }
    float overLen = currentLen - obj.ropeLen;
    float nx = dx / currentLen;
    float ny = dy / currentLen;
    boolean x1Free = objAtX1 != null && !objAtX1.fixed;
    boolean x2Free = objAtX2 != null && !objAtX2.fixed;
    float invA = x1Free ? 1f / objAtX1.mass : 0f;
    float invB = x2Free ? 1f / objAtX2.mass : 0f;
    float invMassSum = invA + invB;
    float v1x = x1Free ? objAtX1.vx : 0f;
    float v1y = x1Free ? objAtX1.vy : 0f;
    float v2x = x2Free ? objAtX2.vx : 0f;
    float v2y = x2Free ? objAtX2.vy : 0f;
    float relVx = v2x - v1x, relVy = v2y - v1y;
    float relVn = relVx * nx + relVy * ny;
    float transverseSpeedSq = Math.max(0f,
            relVx * relVx + relVy * relVy - relVn * relVn);
    float spanDx = (objAtX2 != null ? objAtX2.x : x2)
            - (objAtX1 != null ? objAtX1.x : x1);
    float spanDy = (objAtX2 != null ? objAtX2.y : y2)
            - (objAtX1 != null ? objAtX1.y : y1);
    float constraintSpan = Math.max(1f, magnitude(spanDx, spanDy));
    float tautTolerance = Math.max(0.01f, pixelsPerMeter * 0.0002f);

    boolean wasTaut = obj.ropeTaut;
    obj.ropeTaut = false;
    if ((wasTaut || overLen >= -tautTolerance) && invMassSum > 0f) {
        double tension = 0.0;
        if (x1Free && x2Free) {
            double a1 = (constraintInputForceX(objAtX1) * nx
                    + constraintInputForceY(objAtX1) * ny) * invA;
            double a2 = (constraintInputForceX(objAtX2) * nx
                    + constraintInputForceY(objAtX2) * ny) * invB;
            tension = (a2 - a1 + transverseSpeedSq / constraintSpan) / invMassSum;
        } else if (x2Free) {
            double outwardAccel = (constraintInputForceX(objAtX2) * nx
                    + constraintInputForceY(objAtX2) * ny) * invB;
            tension = objAtX2.mass * (outwardAccel + transverseSpeedSq / constraintSpan);
        } else if (x1Free) {
            double outwardAccel = -(constraintInputForceX(objAtX1) * nx
                    + constraintInputForceY(objAtX1) * ny) * invA;
            tension = objAtX1.mass * (outwardAccel + transverseSpeedSq / constraintSpan);
        }
        if (tension > 0.0 && !Double.isNaN(tension) && !Double.isInfinite(tension)) {
            obj.ropeTaut = true;
            float tx = (float) tension * nx;
            float ty = (float) tension * ny;
            if (x1Free) {
                objAtX1.forceX += tx; objAtX1.forceY += ty;
                objAtX1.constraintForceX += tx; objAtX1.constraintForceY += ty;
            }
            if (x2Free) {
                objAtX2.forceX -= tx; objAtX2.forceY -= ty;
                objAtX2.constraintForceX -= tx; objAtX2.constraintForceY -= ty;
            }
        }
    }
    if ((overLen > 0f || (wasTaut && obj.ropeTaut)) && invMassSum > 0f) {
        if (x1Free && x2Free) {
            float tm = objAtX1.mass + objAtX2.mass;
            objAtX1.x += nx * overLen * (objAtX2.mass / tm);
            objAtX1.y += ny * overLen * (objAtX2.mass / tm);
            objAtX2.x -= nx * overLen * (objAtX1.mass / tm);
            objAtX2.y -= ny * overLen * (objAtX1.mass / tm);
            float relVn2 = (objAtX2.vx - objAtX1.vx) * nx + (objAtX2.vy - objAtX1.vy) * ny;
            if (obj.ropeTaut || relVn2 > 0f) {
                float corr = relVn2 / (1f / objAtX1.mass + 1f / objAtX2.mass);
                objAtX1.vx += corr / objAtX1.mass * nx;
                objAtX1.vy += corr / objAtX1.mass * ny;
                objAtX2.vx -= corr / objAtX2.mass * nx;
                objAtX2.vy -= corr / objAtX2.mass * ny;
            }
        } else if (x2Free) {
            objAtX2.x -= nx * overLen;
            objAtX2.y -= ny * overLen;
            float vn2 = objAtX2.vx * nx + objAtX2.vy * ny;
            if (obj.ropeTaut || vn2 > 0f) {
                objAtX2.vx -= vn2 * nx;
                objAtX2.vy -= vn2 * ny;
            }
        } else if (x1Free) {
            objAtX1.x += nx * overLen;
            objAtX1.y += ny * overLen;
            float vn2 = objAtX1.vx * nx + objAtX1.vy * ny;
            if (obj.ropeTaut || vn2 < 0f) {
                objAtX1.vx -= vn2 * nx;
                objAtX1.vy -= vn2 * ny;
            }
        }
        if (objAtX1 != null) {
            objAtX1.physX = objAtX1.x;
            objAtX1.physY = objAtX1.y;
            objAtX1.physVx = objAtX1.vx;
            objAtX1.physVy = objAtX1.vy;
        }
        if (objAtX2 != null) {
            objAtX2.physX = objAtX2.x;
            objAtX2.physY = objAtX2.y;
            objAtX2.physVx = objAtX2.vx;
            objAtX2.physVy = objAtX2.vy;
        }
        }
    }

        if (objAtX1 != null) {
            float[] sp1 = getSurfacePoint(objAtX1, x2, y2);
            x1 = sp1[0]; y1 = sp1[1];
        }
        if (objAtX2 != null) {
            float[] sp2 = getSurfacePoint(objAtX2, x1, y1);
            x2 = sp2[0]; y2 = sp2[1];
        }
        dx = x2 - x1;
        dy = y2 - y1;
        currentLen = magnitude(dx, dy);

        obj.x = (x1 + x2) / 2;
        obj.y = (y1 + y2) / 2;
        obj.rotation = resolveConstraintAngle(obj, dx, dy);
        obj.isHorizontal = true;
        obj.w = currentLen;
        obj.h = obj.type == TYPE_SPRING ? 20 : 10;
    }

    float[] getSpringEndpoints(PhysObj obj) {
        boolean hasA = obj.connectedObjA != null;
        boolean hasB = obj.connectedObjB != null;
        boolean hasCeiling = obj.connectedToCeiling && obj.ceilingSegmentId >= 0 &&
                obj.ceilingSegmentId < ceilingSegments.size();
        boolean hasGround = obj.connectedToGround;
        boolean hasWallLeft = obj.connectedToWallLeft;
        boolean hasWallRight = obj.connectedToWallRight;
        float x1, y1, x2, y2;
        if (hasA && hasB) {
            if (obj.type == TYPE_ROD && obj.remoteLock) {
                float dx = obj.connectedObjB.x - obj.connectedObjA.x;
                float dy = obj.connectedObjB.y - obj.connectedObjA.y;
                float len = magnitude(dx, dy);
                if (len < 0.001f) {
                    dx = (float) Math.cos(obj.rotation);
                    dy = (float) Math.sin(obj.rotation);
                    len = 1f;
                }
                float[] sp = getSurfacePoint(obj.connectedObjA, obj.connectedObjB.x, obj.connectedObjB.y);
                x1 = sp[0]; y1 = sp[1];
                x2 = x1 + dx / len * obj.rodLen;
                y2 = y1 + dy / len * obj.rodLen;
            } else {
                x1 = obj.connectedObjA.x; y1 = obj.connectedObjA.y;
                x2 = obj.connectedObjB.x; y2 = obj.connectedObjB.y;
            }
        } else if (hasA && hasCeiling) {
            CeilingSegment cs = ceilingSegments.get(obj.ceilingSegmentId);
            x1 = obj.ceilingAnchorX != 0 ? obj.ceilingAnchorX : (cs.x1 + cs.x2) / 2; y1 = cs.y1;
            x2 = obj.connectedObjA.x; y2 = obj.connectedObjA.y;
        } else if (hasB && hasCeiling) {
            CeilingSegment cs = ceilingSegments.get(obj.ceilingSegmentId);
            x1 = obj.ceilingAnchorX != 0 ? obj.ceilingAnchorX : (cs.x1 + cs.x2) / 2; y1 = cs.y1;
            x2 = obj.connectedObjB.x; y2 = obj.connectedObjB.y;
        } else if (hasA && hasGround) {
            x1 = obj.groundAnchorX; y1 = groundY;
            x2 = obj.connectedObjA.x; y2 = obj.connectedObjA.y;
        } else if (hasB && hasGround) {
            x1 = obj.groundAnchorX; y1 = groundY;
            x2 = obj.connectedObjB.x; y2 = obj.connectedObjB.y;
        } else if (hasA && hasWallLeft) {
            x1 = wallLeftX; y1 = obj.wallAnchorY;
            x2 = obj.connectedObjA.x; y2 = obj.connectedObjA.y;
        } else if (hasA && hasWallRight) {
            x1 = wallRightX; y1 = obj.wallAnchorY;
            x2 = obj.connectedObjA.x; y2 = obj.connectedObjA.y;
        } else if (hasB && hasWallLeft) {
            x1 = wallLeftX; y1 = obj.wallAnchorY;
            x2 = obj.connectedObjB.x; y2 = obj.connectedObjB.y;
        } else if (hasB && hasWallRight) {
            x1 = wallRightX; y1 = obj.wallAnchorY;
            x2 = obj.connectedObjB.x; y2 = obj.connectedObjB.y;
        } else if (hasA) {
            x1 = obj.freeEndX; y1 = obj.freeEndY;
            x2 = obj.connectedObjA.x; y2 = obj.connectedObjA.y;
        } else if (hasB) {
            x1 = obj.freeEndX; y1 = obj.freeEndY;
            x2 = obj.connectedObjB.x; y2 = obj.connectedObjB.y;
        } else if (hasCeiling) {
            CeilingSegment cs = ceilingSegments.get(obj.ceilingSegmentId);
            x1 = obj.ceilingAnchorX != 0 ? obj.ceilingAnchorX : (cs.x1 + cs.x2) / 2; y1 = cs.y1;
            x2 = obj.x; y2 = obj.y;
        } else if (hasGround) {
            x1 = obj.groundAnchorX; y1 = groundY;
            x2 = obj.x; y2 = obj.y;
        } else if (hasWallLeft) {
            x1 = wallLeftX; y1 = obj.wallAnchorY;
            x2 = obj.x; y2 = obj.y;
        } else if (hasWallRight) {
            x1 = wallRightX; y1 = obj.wallAnchorY;
            x2 = obj.x; y2 = obj.y;
        } else {
            if (obj.rotation != 0) {
                float cosR = (float) Math.cos(obj.rotation);
                float sinR = (float) Math.sin(obj.rotation);
                float halfLen = obj.isHorizontal ? obj.w / 2 : obj.h / 2;
                x1 = obj.x - cosR * halfLen; y1 = obj.y - sinR * halfLen;
                x2 = obj.x + cosR * halfLen; y2 = obj.y + sinR * halfLen;
            } else if (obj.isHorizontal) {
                x1 = obj.x - obj.w / 2; y1 = obj.y;
                x2 = obj.x + obj.w / 2; y2 = obj.y;
            } else {
                x1 = obj.x; y1 = obj.y - obj.h / 2;
                x2 = obj.x; y2 = obj.y + obj.h / 2;
            }
        }
        boolean hasFixedAnchor = hasCeiling || hasGround || hasWallLeft || hasWallRight;
        int connectedObjCount = (hasA ? 1 : 0) + (hasB ? 1 : 0);
        if (obj.type == TYPE_ROD && obj.remoteLock && hasFixedAnchor && connectedObjCount == 1) {
            PhysObj lockedObj = hasA ? obj.connectedObjA : obj.connectedObjB;
            float dx = lockedObj.x - x1;
            float dy = lockedObj.y - y1;
            float len = magnitude(dx, dy);
            if (obj.angleFixed) {
                dx = (float) Math.cos(obj.fixedAngle);
                dy = (float) Math.sin(obj.fixedAngle);
                len = 1f;
            } else if (len < 0.001f) {
                dx = (float) Math.cos(obj.rotation);
                dy = (float) Math.sin(obj.rotation);
                len = 1f;
            }
            x2 = x1 + dx / len * obj.rodLen;
            y2 = y1 + dy / len * obj.rodLen;
            return new float[]{x1, y1, x2, y2};
        }
        if (!(obj.type == TYPE_ROD && obj.remoteLock && hasA && hasB)) {
            if (hasA && hasB) {
                float[] sp1 = getSurfacePoint(obj.connectedObjA, obj.connectedObjB.x, obj.connectedObjB.y);
                float[] sp2 = getSurfacePoint(obj.connectedObjB, obj.connectedObjA.x, obj.connectedObjA.y);
                x1 = sp1[0]; y1 = sp1[1];
                x2 = sp2[0]; y2 = sp2[1];
            } else if (hasA) {
                float[] sp = getSurfacePoint(obj.connectedObjA, x1, y1);
                x2 = sp[0]; y2 = sp[1];
            } else if (hasB) {
                float[] sp = getSurfacePoint(obj.connectedObjB, x1, y1);
                x2 = sp[0]; y2 = sp[1];
            }
        }
        return new float[]{x1, y1, x2, y2};
    }

    boolean isCeilingRodLinkedObject(PhysObj obj, CeilingSegment cs) {
        if (obj == null || cs == null) return false;
        int idx = ceilingSegments.indexOf(cs);
        if (idx < 0) return false;
        for (PhysObj link : objects) {
            if (link.type != TYPE_ROD) continue;
            if (!link.connectedToCeiling || link.ceilingSegmentId != idx) continue;
            if (link.connectedObjA == obj || link.connectedObjB == obj) return true;
        }
        return false;
    }

    void autoWrapRopeOnPulley(PhysObj rope) {
        float[] ep = getSpringEndpoints(rope);
        float x1 = ep[0], y1 = ep[1], x2 = ep[2], y2 = ep[3];
        float dx = x2 - x1, dy = y2 - y1;
        float lenSq = dx * dx + dy * dy;
        if (lenSq < 1f) return;
        for (PhysObj pulley : objects) {
            if (pulley.type != TYPE_PULLEY) continue;
            float t = ((pulley.x - x1) * dx + (pulley.y - y1) * dy) / lenSq;
            if (t < 0f || t > 1f) continue;
            float cx = x1 + dx * t;
            float cy = y1 + dy * t;
            float pdx = pulley.x - cx;
            float pdy = pulley.y - cy;
            float dist = magnitude(pdx, pdy);
            float radius = pulley.w / 2f + 18f;
            if (dist > radius) continue;
            buildPulleyWrapPath(rope, pulley, x1, y1, x2, y2);
            return;
        }
    }

    void buildPulleyWrapPath(PhysObj rope, PhysObj pulley, float x1, float y1, float x2, float y2) {
        float radius = pulley.w / 2f + 8f;
        float a1 = (float) Math.atan2(y1 - pulley.y, x1 - pulley.x);
        float a2 = (float) Math.atan2(y2 - pulley.y, x2 - pulley.x);
        float cw = a2 - a1;
        while (cw > 0) cw -= 2f * (float) Math.PI;
        while (cw < -2f * (float) Math.PI) cw += 2f * (float) Math.PI;
        float ccw = a2 - a1;
        while (ccw < 0) ccw += 2f * (float) Math.PI;
        while (ccw > 2f * (float) Math.PI) ccw -= 2f * (float) Math.PI;
        float midCw = a1 + cw * 0.5f;
        float midCcw = a1 + ccw * 0.5f;
        float midCwY = pulley.y + (float) Math.sin(midCw) * radius;
        float midCcwY = pulley.y + (float) Math.sin(midCcw) * radius;
        float arc = midCwY < midCcwY ? cw : ccw;
        int steps = Math.max(8, (int) (Math.abs(arc) * radius / 12f));
        rope.ropeWrapPoints.clear();
        rope.ropeParticles.clear();
        rope.ropeWrapPoints.add(new float[]{x1, y1});
        for (int i = 0; i <= steps; i++) {
            float a = a1 + arc * i / steps;
            rope.ropeWrapPoints.add(new float[]{
                    pulley.x + (float) Math.cos(a) * radius,
                    pulley.y + (float) Math.sin(a) * radius
            });
        }
        rope.ropeWrapPoints.add(new float[]{x2, y2});
        rope.ropeWrapped = true;
        rope.ropeParticlesInit = false;
        rope.ropeWrapPulley = pulley;
        rope.ropeWrapDir = arc >= 0f ? 1f : -1f;
        
        
    }

    void clearRopeWrap(PhysObj rope) {
        rope.ropeWrapped = false;
        rope.ropeWrapPoints.clear();
        rope.ropeParticles.clear();
        rope.ropeParticlesInit = false;
        rope.ropeWrapPulley = null;
        
        
    }

    void detectWrapPulley(PhysObj rope) {
        rope.ropeWrapPulley = null;
        
        
        ArrayList<float[]> pts = rope.ropeWrapPoints;
        if (pts.size() < 3) return;
        PhysObj best = null;
        float bestSweep = 0f;
        int candidates = 0;
        for (PhysObj pulley : objects) {
            if (pulley.type != TYPE_PULLEY) continue;
            float r = pulley.w / 2f + 8f;
            float sweep = 0f;
            float prevAng = 0f;
            boolean hasPrev = false;
            boolean near = false;
            for (float[] p : pts) {
                float dx = p[0] - pulley.x;
                float dy = p[1] - pulley.y;
                float d = magnitude(dx, dy);
                if (d < 1f) { hasPrev = false; continue; }
                if (d <= r + 25f) near = true;
                float ang = (float) Math.atan2(dy, dx);
                if (hasPrev) {
                    float diff = ang - prevAng;
                    while (diff > (float) Math.PI) diff -= 2f * (float) Math.PI;
                    while (diff < -(float) Math.PI) diff += 2f * (float) Math.PI;
                    sweep += diff;
                }
                prevAng = ang;
                hasPrev = true;
            }
            if (!near || Math.abs(sweep) < 0.6f) continue;
            candidates++;
            if (Math.abs(sweep) > Math.abs(bestSweep)) { best = pulley; bestSweep = sweep; }
        }
        if (best == null || candidates > 1) return;
        if (Math.abs(bestSweep) > 2f * (float) Math.PI + 0.5f) return;
        rope.ropeWrapPulley = best;
        rope.ropeWrapDir = bestSweep >= 0f ? 1f : -1f;
    }

    boolean pulleyLoadBody(PhysObj o) {
        if (o == null || o.fixed) return false;
        if (isConstraintBody(o) || o.type == TYPE_RING) return false;
        if (o.type == TYPE_RAMP_STRAIGHT || o.type == TYPE_RAMP_ARC) return false;
        if (o.isCutHalf) return false;
        float halfH = (o.type == TYPE_BALL || o.type == TYPE_PULLEY) ? o.w / 2f : o.h / 2f;
        if (o.y + halfH >= groundY - 1.5f) return false;
        return true;
    }

    boolean pulleyLoadLink(PhysObj h) {
        if (h == null || h.isCutHalf) return false;
        if (h.type == TYPE_ROD) return true;
        if (h.type != TYPE_ROPE || h.ropeWrapped) return false;
        float[] ep = getSpringEndpoints(h);
        float len = magnitude(ep[2] - ep[0], ep[3] - ep[1]);
        return len >= h.ropeLen - Math.max(1f, h.ropeLen * 0.05f);
    }

    void collectPulleyRiders(PhysObj rope, PhysObj pulley, ArrayList<PhysObj> out) {
        if (pulley == null || pulley.fixed || pulley.pulleyBoundRamp != null) return;
        ArrayList<PhysObj> frontier = new ArrayList<PhysObj>();
        frontier.add(pulley);
        int guard = 0;
        while (!frontier.isEmpty() && guard < 32) {
            guard++;
            PhysObj cur = frontier.remove(0);
            for (PhysObj h : objects) {
                if (h == rope || !isConstraintBody(h)) continue;
                if (!pulleyLoadLink(h)) continue;
                PhysObj other;
                if (h.connectedObjA == cur) other = h.connectedObjB;
                else if (h.connectedObjB == cur) other = h.connectedObjA;
                else continue;
                if (other == null || other == pulley) continue;
                if (out.contains(other)) continue;
                if (!pulleyLoadBody(other)) continue;
                out.add(other);
                frontier.add(other);
            }
        }
    }

    float[] ropePullPointFor(PhysObj rope, PhysObj o) {
        if (rope == null || o == null || rope.type != TYPE_ROPE || !rope.ropeWrapped) return null;
        ArrayList<float[]> pts = rope.ropeWrapPoints;
        if (pts.size() < 2) return null;
        float[] first = pts.get(0);
        float[] last = pts.get(pts.size() - 1);
        boolean fromStart = magnitude(first[0] - o.x, first[1] - o.y)
                <= magnitude(last[0] - o.x, last[1] - o.y);
        float hw = o.w / 2f;
        float hh = (o.type == TYPE_BALL || o.type == TYPE_PULLEY) ? o.w / 2f : o.h / 2f;
        float minDist = magnitude(hw, hh) + 2f;
        int n = pts.size();
        for (int i = 0; i < n; i++) {
            float[] p = fromStart ? pts.get(i) : pts.get(n - 1 - i);
            if (magnitude(p[0] - o.x, p[1] - o.y) > minDist) {
                return new float[]{p[0], p[1]};
            }
        }
        return null;
    }

    float[] wrappedRopePullDir(PhysObj o) {
        if (o == null) return null;
        PhysObj found = null;
        int cnt = 0;
        for (PhysObj h : objects) {
            if (!isConstraintBody(h)) continue;
            if (h.connectedObjA != o && h.connectedObjB != o) continue;
            cnt++;
            found = h;
        }
        if (cnt != 1) return null;
        float[] p = ropePullPointFor(found, o);
        if (p == null) return null;
        float dx = p[0] - o.x;
        float dy = p[1] - o.y;
        float len = magnitude(dx, dy);
        if (len < 0.001f) return null;
        return new float[]{dx / len, dy / len};
    }

    boolean usesAnalyticWrap(PhysObj o) {
        if (o.type != TYPE_ROPE || !o.ropeWrapped || o.ropeWrapPulley == null) return false;
        if (o.ropeWrapPulley.type != TYPE_PULLEY || !objects.contains(o.ropeWrapPulley)) return false;
        int cnt = (o.connectedObjA != null ? 1 : 0) + (o.connectedObjB != null ? 1 : 0);
        boolean anchor = o.connectedToCeiling || o.connectedToGround
                || o.connectedToWallLeft || o.connectedToWallRight;
        return cnt == 2 || (cnt == 1 && anchor);
    }

    void solveWrappedRopeConstraint(PhysObj rope, float ax0, float ay0, float bx0, float by0,
                                    PhysObj objA, PhysObj objB) {
        PhysObj pulley = rope.ropeWrapPulley;
        boolean freeC = !pulley.fixed && pulley.pulleyBoundRamp == null;
        ArrayList<PhysObj> riders = new ArrayList<PhysObj>();
        if (freeC) collectPulleyRiders(rope, pulley, riders);
        if (objA != null) riders.remove(objA);
        if (objB != null) riders.remove(objB);
        float loadMass = 0f, loadForceX = 0f, loadForceY = 0f;
        for (int i = 0; i < riders.size(); i++) {
            PhysObj rd = riders.get(i);
            loadMass += rd.mass;
            loadForceX += baseForceX(rd);
            loadForceY += baseForceY(rd);
        }
        float r = Math.max(4f, pulley.w / 2f + 8f);
        float cx = pulley.x, cy = pulley.y;
        float ax = objA != null ? objA.x : ax0;
        float ay = objA != null ? objA.y : ay0;
        float bx = objB != null ? objB.x : bx0;
        float by = objB != null ? objB.y : by0;
        float dax = ax - cx, day = ay - cy;
        float dbx = bx - cx, dby = by - cy;
        float sa = magnitude(dax, day), sb = magnitude(dbx, dby);
        if (sa <= r + 2f || sb <= r + 2f) return;
        float ta = (float) Math.sqrt(sa * sa - r * r);
        float tb = (float) Math.sqrt(sb * sb - r * r);
        float alphaA = (float) Math.atan2(day, dax);
        float alphaB = (float) Math.atan2(dby, dbx);
        float betaA = (float) Math.acos(Math.max(-1f, Math.min(1f, r / sa)));
        float betaB = (float) Math.acos(Math.max(-1f, Math.min(1f, r / sb)));
        float sigma = rope.ropeWrapDir >= 0f ? 1f : -1f;
        float span = sigma * (alphaB - alphaA);
        while (span < 0f) span += 2f * (float) Math.PI;
        while (span >= 2f * (float) Math.PI) span -= 2f * (float) Math.PI;
        float arcSpan = span - betaA - betaB;
        if (arcSpan <= 0.03f) return;
        float thetaA = alphaA + sigma * betaA;
        float thetaB = alphaB - sigma * betaB;
        float tax = cx + (float) Math.cos(thetaA) * r;
        float tay = cy + (float) Math.sin(thetaA) * r;
        float tbx = cx + (float) Math.cos(thetaB) * r;
        float tby = cy + (float) Math.sin(thetaB) * r;
        float pathLen = ta + tb + r * arcSpan;
        float gax = ax - tax, gay = ay - tay;
        float gbx = bx - tbx, gby = by - tby;
        float gal = magnitude(gax, gay), gbl = magnitude(gbx, gby);
        if (gal < 0.001f || gbl < 0.001f) return;
        gax /= gal; gay /= gal; gbx /= gbl; gby /= gbl;
        boolean freeA = objA != null && !objA.fixed;
        boolean freeB = objB != null && !objB.fixed;
        float wa = freeA ? 1f / Math.max(0.001f, objA.mass) : 0f;
        float wb = freeB ? 1f / Math.max(0.001f, objB.mass) : 0f;
        float wc = freeC ? 1f / Math.max(0.001f, pulley.mass + loadMass) : 0f;
        float gcx = -(gax + gbx), gcy = -(gay + gby);
        float gcLenSq = gcx * gcx + gcy * gcy;
        float wsum = wa + wb + wc * gcLenSq;
        if (wsum > 0f) {
            float over = pathLen - rope.ropeLen;
            if (over > 0f) {
                float lambda = over / wsum;
                if (freeA) { objA.x -= lambda * wa * gax; objA.y -= lambda * wa * gay; }
                if (freeB) { objB.x -= lambda * wb * gbx; objB.y -= lambda * wb * gby; }
                if (freeC) {
                    float sx = -lambda * wc * gcx;
                    float sy = -lambda * wc * gcy;
                    pulley.x += sx;
                    pulley.y += sy;
                    for (int i = 0; i < riders.size(); i++) {
                        PhysObj rd = riders.get(i);
                        rd.x += sx;
                        rd.y += sy;
                    }
                }
                float rate = (freeA ? objA.vx * gax + objA.vy * gay : 0f)
                        + (freeB ? objB.vx * gbx + objB.vy * gby : 0f)
                        + (freeC ? pulley.vx * gcx + pulley.vy * gcy : 0f);
                if (rate > 0f) {
                    float lv = rate / wsum;
                    if (freeA) { objA.vx -= lv * wa * gax; objA.vy -= lv * wa * gay; }
                    if (freeB) { objB.vx -= lv * wb * gbx; objB.vy -= lv * wb * gby; }
                    if (freeC) {
                        float dvx = -lv * wc * gcx;
                        float dvy = -lv * wc * gcy;
                        pulley.vx += dvx;
                        pulley.vy += dvy;
                        for (int i = 0; i < riders.size(); i++) {
                            PhysObj rd = riders.get(i);
                            rd.vx += dvx;
                            rd.vy += dvy;
                        }
                    }
                }
                if (freeA) syncPhysState(objA);
                if (freeB) syncPhysState(objB);
                if (freeC) syncPhysState(pulley);
                for (int i = 0; i < riders.size(); i++) syncPhysState(riders.get(i));
            }
            if (pathLen > rope.ropeLen - 1.5f) {
                float fa = freeA ? (baseForceX(objA) * gax + baseForceY(objA) * gay) * wa : 0f;
                float fb = freeB ? (baseForceX(objB) * gbx + baseForceY(objB) * gby) * wb : 0f;
                float fc = freeC ? ((baseForceX(pulley) + loadForceX) * gcx
                        + (baseForceY(pulley) + loadForceY) * gcy) * wc : 0f;
                float tension = (fa + fb + fc) / wsum;
                if (tension > 0f) {
                    if (freeA) {
                        objA.constraintForceX -= tension * gax;
                        objA.constraintForceY -= tension * gay;
                        objA.forceX -= tension * gax;
                        objA.forceY -= tension * gay;
                    }
                    if (freeB) {
                        objB.constraintForceX -= tension * gbx;
                        objB.constraintForceY -= tension * gby;
                        objB.forceX -= tension * gbx;
                        objB.forceY -= tension * gby;
                    }
                    if (freeC) {
                        pulley.constraintForceX -= tension * gcx;
                        pulley.constraintForceY -= tension * gcy;
                        pulley.forceX -= tension * gcx;
                        pulley.forceY -= tension * gcy;
                    }
                }
            }
        }
        float endAx = objA != null ? objA.x : ax0;
        float endAy = objA != null ? objA.y : ay0;
        float endBx = objB != null ? objB.x : bx0;
        float endBy = objB != null ? objB.y : by0;
        if (objA != null) {
            float[] sp = getSurfacePoint(objA, tax, tay);
            endAx = sp[0]; endAy = sp[1];
        }
        if (objB != null) {
            float[] sp = getSurfacePoint(objB, tbx, tby);
            endBx = sp[0]; endBy = sp[1];
        }
        int steps = Math.max(4, Math.min(24, (int) (arcSpan * r / 10f)));
        int total = steps + 3;
        ArrayList<float[]> pts = rope.ropeWrapPoints;
        while (pts.size() > total) pts.remove(pts.size() - 1);
        while (pts.size() < total) pts.add(new float[2]);
        float[] first = pts.get(0);
        first[0] = endAx; first[1] = endAy;
        for (int i = 0; i <= steps; i++) {
            float a = thetaA + sigma * arcSpan * i / steps;
            float[] p = pts.get(i + 1);
            p[0] = cx + (float) Math.cos(a) * r;
            p[1] = cy + (float) Math.sin(a) * r;
        }
        float[] last = pts.get(total - 1);
        last[0] = endBx; last[1] = endBy;
        rope.ropeParticles.clear();
        rope.ropeParticlesInit = false;
    }

    boolean isConstraintBody(PhysObj o) {
        return o.type == TYPE_SPRING || o.type == TYPE_ROPE || o.type == TYPE_ROD;
    }

    boolean isFreeEndConstraint(PhysObj o) {
        if (o.type != TYPE_SPRING && o.type != TYPE_ROPE) return false;
        boolean hasCeiling = o.connectedToCeiling;
        boolean hasGround = o.connectedToGround;
        boolean hasWallLeft = o.connectedToWallLeft;
        boolean hasWallRight = o.connectedToWallRight;
        boolean hasFixedAnchor = hasCeiling || hasGround || hasWallLeft || hasWallRight;
        int connectedObjCount = (o.connectedObjA != null ? 1 : 0) + (o.connectedObjB != null ? 1 : 0);
        if (hasFixedAnchor) return false;
        return connectedObjCount == 1;
    }

    void syncPhysState(PhysObj o) {
        if (o == null) return;
        o.physX = o.x;
        o.physY = o.y;
        o.physVx = o.vx;
        o.physVy = o.vy;
    }

    void projectVelocityAlong(PhysObj obj, float ux, float uy) {
        if (obj == null || obj.fixed) return;
        float vAlong = obj.vx * ux + obj.vy * uy;
        obj.vx = ux * vAlong;
        obj.vy = uy * vAlong;
        obj.physVx = obj.vx;
        obj.physVy = obj.vy;
    }

    boolean rodBaseSupported(PhysObj o) {
        if (o == null || o.fixed) return false;
        if (o.type == TYPE_RING || isConstraintBody(o)) return false;
        float halfH = o.type == TYPE_BALL ? o.w / 2f : o.h / 2f;
        return o.y + halfH >= groundY - 2.5f && Math.abs(o.vy) < pixelsPerMeter * 0.05f;
    }

    boolean hasSpringAttached(PhysObj o) {
        for (PhysObj other : objects) {
            if (other.type != TYPE_SPRING) continue;
            if (other.connectedObjA == o || other.connectedObjB == o) return true;
        }
        return false;
    }








float[] constraintFixedAnchorPoint(PhysObj holder) {
    if (holder == null) return null;
    if (holder.connectedToCeiling && holder.ceilingSegmentId >= 0
            && holder.ceilingSegmentId < ceilingSegments.size()) {
        CeilingSegment cs = ceilingSegments.get(holder.ceilingSegmentId);
        float cax = holder.ceilingAnchorX != 0 ? holder.ceilingAnchorX : (cs.x1 + cs.x2) / 2f;
        return new float[]{cax, cs.y1};
    }
    if (holder.connectedToGround) return new float[]{holder.groundAnchorX, groundY};
    if (holder.connectedToWallLeft) return new float[]{wallLeftX, holder.wallAnchorY};
    if (holder.connectedToWallRight) return new float[]{wallRightX, holder.wallAnchorY};
    return null;
}




    float chainExternalForceX(PhysObj o) {
        float f = baseForceX(o);
        if (airResistance > 0f) {
            float v = magnitude(o.vx, o.vy);
            if (v > 0.001f) f -= airResistance * pixelsPerMeter * (o.vx / v);
        }
        return f;
    }

    float chainExternalForceY(PhysObj o) {
        float f = baseForceY(o);
        if (airResistance > 0f) {
            float v = magnitude(o.vx, o.vy);
            if (v > 0.001f) f -= airResistance * pixelsPerMeter * (o.vy / v);
        }
        return f;
    }

    double constraintInputForceX(PhysObj o) {
        return o.forceX + (constraintPostPass ? chainExternalForceX(o) : 0.0);
    }

    double constraintInputForceY(PhysObj o) {
        return o.forceY + (constraintPostPass ? chainExternalForceY(o) : 0.0);
    }

    boolean hasRopeAttached(PhysObj o) {
        for (int i = 0; i < objects.size(); i++) {
            PhysObj other = objects.get(i);
            if (other.type != TYPE_ROPE) continue;
            if (other.connectedObjA == o || other.connectedObjB == o) return true;
        }
        return false;
    }

    boolean rodChainBodyOk(PhysObj b) {
        if (b == null) return false;
        if (isConstraintBody(b)) return false;
        if (b.type == TYPE_RING) return false;
        if (b.type == TYPE_RAMP_STRAIGHT || b.type == TYPE_RAMP_ARC) return false;
        if (b.fixed) return true;
        if (dragging && selectedObj == b) return false;
        if (b.mass <= 0.0001f) return false;
        if (hasSpringAttached(b)) return false;
        if (hasRopeAttached(b)) return false;
        return true;
    }

    boolean rodChainLinkCandidate(PhysObj o) {
        if (o == null || o.type != TYPE_ROD) return false;
        if (o.isCutHalf || o.fixed || o.remoteLock || o.angleFixed) return false;
        if (o.rodRing != null) return false;
        if (o.rodLen < 1f) return false;
        boolean hasA = o.connectedObjA != null;
        boolean hasB = o.connectedObjB != null;
        boolean hasCeiling = o.connectedToCeiling && o.ceilingSegmentId >= 0
                && o.ceilingSegmentId < ceilingSegments.size();
        boolean hasAnchor = hasCeiling || o.connectedToGround
                || o.connectedToWallLeft || o.connectedToWallRight;
        if (hasA && hasB) return !hasAnchor;
        if (hasAnchor && (hasA || hasB)) return true;
        return false;
    }

    float[] rodChainEndpoints(PhysObj rod, PhysObj a, PhysObj b) {
        float x1, y1;
        if (a != null) {
            x1 = a.x;
            y1 = a.y;
        } else {
            float[] anc = constraintFixedAnchorPoint(rod);
            if (anc == null) return null;
            x1 = anc[0];
            y1 = anc[1];
        }
        float x2 = b.x, y2 = b.y;
        if (a != null) {
            float[] sp = getSurfacePoint(a, x2, y2);
            x1 = sp[0];
            y1 = sp[1];
        }
        float[] sp2 = getSurfacePoint(b, x1, y1);
        x2 = sp2[0];
        y2 = sp2[1];
        return new float[]{x1, y1, x2, y2};
    }

    int rodChainFind(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]];
            i = parent[i];
        }
        return i;
    }

    boolean rodChainShareMovable(int i, int j) {
        PhysObj ai = rodChainEndA.get(i);
        PhysObj bi = rodChainEndB.get(i);
        PhysObj aj = rodChainEndA.get(j);
        PhysObj bj = rodChainEndB.get(j);
        if (ai != null && !ai.fixed && (ai == aj || ai == bj)) return true;
        if (bi != null && !bi.fixed && (bi == aj || bi == bj)) return true;
        return false;
    }

    void solveRodChainTensions(float dt) {
        rodChainActive = false;
        rodChainLinks.clear();
        rodChainEndA.clear();
        rodChainEndB.clear();
        for (int i = 0; i < objects.size(); i++) {
            PhysObj o = objects.get(i);
            if (o.type != TYPE_ROD) continue;
            o.chainSolved = false;
            o.chainTension = 0f;
            o.chainGroup = -1;
        }
        for (int i = 0; i < objects.size(); i++) {
            PhysObj rod = objects.get(i);
            if (!rodChainLinkCandidate(rod)) continue;
            PhysObj a;
            PhysObj b;
            if (rod.connectedObjA != null && rod.connectedObjB != null) {
                a = rod.connectedObjA;
                b = rod.connectedObjB;
            } else {
                a = null;
                b = rod.connectedObjA != null ? rod.connectedObjA : rod.connectedObjB;
            }
            if (b == null || a == b) continue;
            if (a != null && !rodChainBodyOk(a)) continue;
            if (!rodChainBodyOk(b)) continue;
            if (b.fixed && (a == null || a.fixed)) continue;
            rodChainLinks.add(rod);
            rodChainEndA.add(a);
            rodChainEndB.add(b);
        }
        int total = rodChainLinks.size();
        if (total < 2 || total > 16) {
            rodChainLinks.clear();
            rodChainEndA.clear();
            rodChainEndB.clear();
            return;
        }
        int[] parent = new int[total];
        for (int i = 0; i < total; i++) parent[i] = i;
        for (int i = 0; i < total; i++) {
            for (int j = i + 1; j < total; j++) {
                if (!rodChainShareMovable(i, j)) continue;
                int ra = rodChainFind(parent, i);
                int rb = rodChainFind(parent, j);
                if (ra != rb) parent[rb] = ra;
            }
        }
        int[] count = new int[total];
        for (int i = 0; i < total; i++) count[rodChainFind(parent, i)]++;
        for (int g = 0; g < total; g++) {
            if (count[g] < 2) continue;
            int[] idx = new int[count[g]];
            int p = 0;
            for (int i = 0; i < total; i++) {
                if (rodChainFind(parent, i) != g) continue;
                idx[p] = i;
                p++;
            }
            if (solveRodChainGroup(idx, idx[0])) rodChainActive = true;
        }
    }

    boolean solveRodChainGroup(int[] idx, int groupId) {
        int n = idx.length;
        float[] dirX = new float[n];
        float[] dirY = new float[n];
        float[] span = new float[n];
        for (int k = 0; k < n; k++) {
            int i = idx[k];
            PhysObj rod = rodChainLinks.get(i);
            PhysObj a = rodChainEndA.get(i);
            PhysObj b = rodChainEndB.get(i);
            float[] ep = rodChainEndpoints(rod, a, b);
            if (ep == null) return false;
            float ddx = ep[2] - ep[0];
            float ddy = ep[3] - ep[1];
            float d = magnitude(ddx, ddy);
            if (d < 0.5f) return false;
            dirX[k] = ddx / d;
            dirY[k] = ddy / d;
            float ax = a != null ? a.x : ep[0];
            float ay = a != null ? a.y : ep[1];
            float sp = magnitude(b.x - ax, b.y - ay);
            span[k] = sp < 1f ? 1f : sp;
        }
        double[][] mat = new double[n][n + 1];
        for (int k = 0; k < n; k++) {
            int i = idx[k];
            PhysObj a = rodChainEndA.get(i);
            PhysObj b = rodChainEndB.get(i);
            boolean aFree = a != null && !a.fixed;
            boolean bFree = !b.fixed;
            double invA = aFree ? 1.0 / Math.max(0.001f, a.mass) : 0.0;
            double invB = bFree ? 1.0 / Math.max(0.001f, b.mass) : 0.0;
            for (int q = 0; q < n; q++) {
                int j = idx[q];
                PhysObj aj = rodChainEndA.get(j);
                PhysObj bj = rodChainEndB.get(j);
                double dot = (double) dirX[k] * dirX[q] + (double) dirY[k] * dirY[q];
                double cB = 0.0;
                double cA = 0.0;
                if (b == aj) cB = 1.0;
                else if (b == bj) cB = -1.0;
                if (a != null) {
                    if (a == aj) cA = 1.0;
                    else if (a == bj) cA = -1.0;
                }
                mat[k][q] = dot * (invB * cB - invA * cA);
            }
            double fax = aFree ? chainExternalForceX(a) : 0.0;
            double fay = aFree ? chainExternalForceY(a) : 0.0;
            double fbx = bFree ? chainExternalForceX(b) : 0.0;
            double fby = bFree ? chainExternalForceY(b) : 0.0;
            double rvx = (bFree ? b.vx : 0f) - (aFree ? a.vx : 0f);
            double rvy = (bFree ? b.vy : 0f) - (aFree ? a.vy : 0f);
            double vn = rvx * dirX[k] + rvy * dirY[k];
            double curve = (rvx * rvx + rvy * rvy - vn * vn) / span[k];
            mat[k][n] = -(dirX[k] * (invB * fbx - invA * fax)
                    + dirY[k] * (invB * fby - invA * fay)) - curve;
        }
        for (int col = 0; col < n; col++) {
            int piv = col;
            double best = Math.abs(mat[col][col]);
            for (int r = col + 1; r < n; r++) {
                double v = Math.abs(mat[r][col]);
                if (v > best) {
                    best = v;
                    piv = r;
                }
            }
            if (best < 1e-9) return false;
            if (piv != col) {
                double[] tmp = mat[piv];
                mat[piv] = mat[col];
                mat[col] = tmp;
            }
            double pv = mat[col][col];
            for (int r = col + 1; r < n; r++) {
                double f = mat[r][col] / pv;
                if (f == 0.0) continue;
                for (int c = col; c <= n; c++) mat[r][c] -= f * mat[col][c];
            }
        }
        double[] lam = new double[n];
        for (int r = n - 1; r >= 0; r--) {
            double s = mat[r][n];
            for (int c = r + 1; c < n; c++) s -= mat[r][c] * lam[c];
            lam[r] = s / mat[r][r];
        }
        for (int k = 0; k < n; k++) {
            double v = lam[k];
            if (Double.isNaN(v) || Double.isInfinite(v) || Math.abs(v) > 1.0e7) return false;
        }
        for (int k = 0; k < n; k++) {
            PhysObj rod = rodChainLinks.get(idx[k]);
            rod.chainTension = (float) lam[k];
            rod.chainSolved = true;
            rod.chainGroup = groupId;
        }
        return true;
    }


    void projectRodChainConstraints() {
        int total = rodChainLinks.size();
        if (total < 2) return;
        for (int pass = 0; pass < 8; pass++) {
            for (int i = 0; i < total; i++) {
                PhysObj rod = rodChainLinks.get(i);
                if (!rod.chainSolved) continue;
                PhysObj a = rodChainEndA.get(i);
                PhysObj b = rodChainEndB.get(i);
                float[] ep = rodChainEndpoints(rod, a, b);
                if (ep == null) continue;
                float ddx = ep[2] - ep[0];
                float ddy = ep[3] - ep[1];
                float d = magnitude(ddx, ddy);
                if (d < 0.5f) continue;
                float nxr = ddx / d;
                float nyr = ddy / d;
                float err = d - rod.rodLen;
                boolean movA = a != null && !a.fixed;
                boolean movB = !b.fixed;
                if (movA && movB) {
                    float tm = a.mass + b.mass;
                    if (tm < 0.001f) continue;
                    a.x += nxr * err * (b.mass / tm);
                    a.y += nyr * err * (b.mass / tm);
                    b.x -= nxr * err * (a.mass / tm);
                    b.y -= nyr * err * (a.mass / tm);
                    float relVn = (b.vx - a.vx) * nxr + (b.vy - a.vy) * nyr;
                    float corr = relVn / (1f / a.mass + 1f / b.mass);
                    a.vx += corr / a.mass * nxr;
                    a.vy += corr / a.mass * nyr;
                    b.vx -= corr / b.mass * nxr;
                    b.vy -= corr / b.mass * nyr;
                    syncPhysState(a);
                    syncPhysState(b);
                } else if (movB) {
                    b.x -= nxr * err;
                    b.y -= nyr * err;
                    float vn = b.vx * nxr + b.vy * nyr;
                    b.vx -= vn * nxr;
                    b.vy -= vn * nyr;
                    syncPhysState(b);
                } else if (movA) {
                    a.x += nxr * err;
                    a.y += nyr * err;
                    float vn = a.vx * nxr + a.vy * nyr;
                    a.vx -= vn * nxr;
                    a.vy -= vn * nyr;
                    syncPhysState(a);
                }
            }
        }
    }


    float baseForceX(PhysObj obj) {
        float windX = windSpeed * (float) Math.cos(windDirection * Math.PI / 180);
        return (float) (obj.appliedForceX * pixelsPerMeter + windX * pixelsPerMeter);
    }

    float baseForceY(PhysObj obj) {
        float windY = -windSpeed * (float) Math.sin(windDirection * Math.PI / 180);
        return (float) ((obj.appliedForceY + obj.mass * GRAVITY) * pixelsPerMeter + windY * pixelsPerMeter);
    }

    void addGroundSupportForce(PhysObj obj) {
        float downwardForce = baseForceY(obj) / pixelsPerMeter;
        if (downwardForce > 0f) {
            obj.supportForceY -= downwardForce;
        }
    }

    void addRodForce(PhysObj obj, float fx, float fy, float otherX, float otherY) {
        addRodForce(obj, fx, fy, otherX, otherY, null);
    }

    void addRodForce(PhysObj obj, float fx, float fy, float otherX, float otherY, PhysObj rodSrc) {
        if (obj == null || obj.fixed) return;
        obj.forceX += fx;
        obj.forceY += fy;
        addRodDisplayForce(obj, fx, fy, otherX, otherY, rodSrc);
    }


    void addRodDisplayForce(PhysObj obj, float fx, float fy, boolean tension) {
        if (obj == null || obj.fixed) return;
        if (tension) {
            obj.constraintForceX += fx;
            obj.constraintForceY += fy;
        } else {
            obj.rodSupportForceX += fx;
            obj.rodSupportForceY += fy;
        }
    }

    void addRodDisplayForce(PhysObj obj, float fx, float fy, float otherX, float otherY) {
        addRodDisplayForce(obj, fx, fy, otherX, otherY, null);
    }

    boolean rodElasticSuppressed(PhysObj rodSrc) {
        return rodSrc != null && rodSrc.type == TYPE_ROD && rodSrc.remoteLock;
    }

    void addRodDisplayForce(PhysObj obj, float fx, float fy, float otherX, float otherY, PhysObj rodSrc) {
        if (obj == null || obj.fixed) return;
        if (rodElasticSuppressed(rodSrc)) return;
        float towardX = otherX - obj.x;
        float towardY = otherY - obj.y;
        float tLen = magnitude(towardX, towardY);
        if (tLen < 0.001f) {
            towardX = -fx;
            towardY = -fy;
            tLen = magnitude(towardX, towardY);
            if (tLen < 0.001f) return;
        }
        towardX /= tLen;
        towardY /= tLen;
        float axial = fx * towardX + fy * towardY;
        if (Math.abs(axial) < 0.0005f) return;
        if (axial >= 0f) {
            obj.constraintForceX += towardX * axial;
            obj.constraintForceY += towardY * axial;
        } else {
            obj.rodSupportForceX += towardX * axial;
            obj.rodSupportForceY += towardY * axial;
        }
    }

    boolean rodAnchorLike(PhysObj o) {
        if (o == null) return false;
        if (o.fixed) return true;
        if (o.type == TYPE_RING || isConstraintBody(o)) return false;
        if (rodBaseSupported(o)) return true;
        if (o.contactGround) return true;
        return false;
    }

    float[] rodAxialOnObject(PhysObj rod, PhysObj target) {
        if (rod == null || target == null || target.fixed) return null;
        if (rodElasticSuppressed(rod)) return null;
        if (rod.connectedObjA != target && rod.connectedObjB != target) return null;
        PhysObj other = null;
        if (rod.connectedObjA != null && rod.connectedObjB != null) {
            other = rod.connectedObjA == target ? rod.connectedObjB : rod.connectedObjA;
            if (other == null) return null;
        }
        float[] ep = getSpringEndpoints(rod);
        float tx, ty, ox, oy;
        if (rod.connectedObjA != null && rod.connectedObjB != null && rod.connectedObjA == target) {
            tx = ep[0]; ty = ep[1]; ox = ep[2]; oy = ep[3];
        } else {
            tx = ep[2]; ty = ep[3]; ox = ep[0]; oy = ep[1];
        }
        float dx = tx - ox, dy = ty - oy;
        float len = magnitude(dx, dy);
        if (len < 0.001f) return null;
        float nx = dx / len, ny = dy / len;
        float perpX = -ny, perpY = nx;
        boolean otherAnchored = other == null || rodAnchorLike(other);
        boolean targetAnchored = other != null && rodAnchorLike(target);
        float react;
        if (otherAnchored) {
            float pivotX = other != null ? other.x : ox;
            float pivotY = other != null ? other.y : oy;
            float span = Math.max(1f, magnitude(target.x - pivotX, target.y - pivotY));
            float fAlong = baseForceX(target) * nx + baseForceY(target) * ny;
            float rvx = target.vx - (other != null ? other.vx : 0f);
            float rvy = target.vy - (other != null ? other.vy : 0f);
            float vt = rvx * perpX + rvy * perpY;
            react = -fAlong - target.mass * vt * vt / span;
        } else if (targetAnchored) {
            float span = Math.max(1f, magnitude(other.x - target.x, other.y - target.y));
            float vt = (other.vx - target.vx) * perpX + (other.vy - target.vy) * perpY;
            react = (baseForceX(other) * nx + baseForceY(other) * ny)
                    - other.mass * vt * vt / span;
        } else {
            float span = Math.max(1f, magnitude(target.x - other.x, target.y - other.y));
            float mt = Math.max(0.001f, target.mass);
            float mo = Math.max(0.001f, other.mass);
            float accelT = (baseForceX(target) * nx + baseForceY(target) * ny) / mt;
            float accelO = (baseForceX(other) * nx + baseForceY(other) * ny) / mo;
            float vRelPerp = (target.vx - other.vx) * perpX + (target.vy - other.vy) * perpY;
            float pull = (accelT - accelO + vRelPerp * vRelPerp / span) / (1f / mt + 1f / mo);
            react = -pull;
        }
        return new float[]{react * nx, react * ny, react < 0f ? 1f : -1f};
    }

    float getConstraintAngle(PhysObj obj) {
        if (obj.rotation != 0f || obj.isHorizontal) return obj.rotation;
        return (float) Math.PI / 2f;
    }

    void setAngleFixed(PhysObj obj, boolean fixed) {
        if (fixed && !obj.angleFixed) {
            obj.fixedAngle = getConstraintAngle(obj);
        }
        obj.angleFixed = fixed;
    }

    float resolveConstraintAngle(PhysObj obj, float dx, float dy) {
        if (obj.angleFixed) return obj.fixedAngle;
        return (float) Math.atan2(dy, dx);
    }

    boolean shouldUpdateConstraintAfterIntegration(PhysObj obj) {
        if (obj == null || obj.isCutHalf) return false;
        return obj.type == TYPE_ROD || obj.type == TYPE_SPRING || obj.type == TYPE_ROPE;
    }

    void alignObjectsOnRotatedSurfaces() {
        for (PhysObj top : objects) {
            if (top.fixed || isConstraintBody(top)) continue;
            if (top.type == TYPE_BALL || top.type == TYPE_PULLEY || top.type == TYPE_RING) continue;
            if (top.type == TYPE_RAMP_STRAIGHT || top.type == TYPE_RAMP_ARC) continue;
            for (PhysObj surface : objects) {
                if (surface == top) continue;
                if (isConstraintBody(surface)) continue;
                if (surface.type == TYPE_BALL || surface.type == TYPE_PULLEY || surface.type == TYPE_RING) continue;
                if (surface.type == TYPE_RAMP_STRAIGHT || surface.type == TYPE_RAMP_ARC) continue;
                if (Math.abs(surface.rotation) < 0.001f) continue;
                float angle = surface.rotation;
                float tx = (float) Math.cos(angle);
                float ty = (float) Math.sin(angle);
                float nx = (float) Math.sin(angle);
                float ny = -(float) Math.cos(angle);
                float dx = top.x - surface.x;
                float dy = top.y - surface.y;
                float relT = dx * tx + dy * ty;
                float relN = dx * nx + dy * ny;
                float topExtentN = Math.abs(nx) * top.w / 2f + Math.abs(ny) * top.h / 2f;
                float topExtentT = Math.abs(tx) * top.w / 2f + Math.abs(ty) * top.h / 2f;
                float targetN = surface.h / 2f + topExtentN;
                float tLimit = Math.max(0f, surface.w / 2f - topExtentT * 0.45f);
                if (Math.abs(relT) <= tLimit && relN > 0f && Math.abs(relN - targetN) <= 28f) {
                    top.rotation = surface.rotation;
                    top.contactRamp = true;
                    top.rampSurfAngle = surface.rotation;
                    top.rampNx = nx;
                    top.rampNy = ny;
                    break;
                }
            }
        }
    }

    void clearPostConstraintForces() {
        for (PhysObj o : objects) {
            if (isConstraintBody(o)) continue;
            if (o.type == TYPE_RING) continue;
            o.constraintForceX = 0f;
            o.constraintForceY = 0f;
            o.supportForceX = 0f;
            o.supportForceY = 0f;
            o.rodSupportForceX = 0f;
            o.rodSupportForceY = 0f;
        }
    }

    void completeVelocityVerlet(float dt) {
        for (PhysObj o : objects) {
            if (o.fixed || isConstraintBody(o)) continue;
            double endForceX = o.stepOtherForceX + o.forceX;
            double endForceY = o.stepOtherForceY + o.forceY;
            double endAccelX = endForceX / o.mass;
            double endAccelY = endForceY / o.mass;
            double halfVx = bestPhysValue(o.vx, o.physVx);
            double halfVy = bestPhysValue(o.vy, o.physVy);
            o.physVx = secondHalfKick(halfVx, endAccelX, dt);
            o.physVy = secondHalfKick(halfVy, endAccelY, dt);
            o.vx = (float) o.physVx;
            o.vy = (float) o.physVy;
            o.displayForceX = (float) endForceX;
            o.displayForceY = (float) endForceY;
            o.forceX = 0;
            o.forceY = 0;
        }
    }

    void projectPostVerletConstraintVelocities() {
        for (PhysObj c : objects) {
            if (c.remoteLock || c.angleFixed) {
                float ux = (float) Math.cos(c.fixedAngle);
                float uy = (float) Math.sin(c.fixedAngle);
                boolean rigid = c.remoteLock || c.type == TYPE_ROPE || c.type == TYPE_ROD;
                if (isFreeEndConstraint(c)) {
                    PhysObj solo = c.connectedObjA != null ? c.connectedObjA : c.connectedObjB;
                    if (rigid) {
                        projectFreeEndRelativeVelocity(c, solo, 1f, 0f);
                        projectFreeEndRelativeVelocity(c, solo, 0f, 1f);
                    } else {
                        projectFreeEndRelativeVelocity(c, solo, -uy, ux);
                    }
                } else {
                    boolean both = c.connectedObjA != null && c.connectedObjB != null;
                    PhysObj a = both ? c.connectedObjA : null;
                    PhysObj b = both ? c.connectedObjB
                            : (c.connectedObjA != null ? c.connectedObjA : c.connectedObjB);
                    if (b == null) continue;
                    if (rigid) {
                        projectRelativeVelocity(a, b, 1f, 0f);
                        projectRelativeVelocity(a, b, 0f, 1f);
                    } else {
                        projectRelativeVelocity(a, b, -uy, ux);
                    }
                }
                continue;
            }
            if (c.type != TYPE_ROPE && c.type != TYPE_ROD) continue;
            if (c.fixed || c.isCutHalf || c.cutDecaying || c.ropeWrapped) continue;
            if (isFreeEndConstraint(c)) {
                PhysObj solo = c.connectedObjA != null ? c.connectedObjA : c.connectedObjB;
                float dx = c.freeEndX - solo.x;
                float dy = c.freeEndY - solo.y;
                float len = magnitude(dx, dy);
                if (len < 0.001f) continue;
                float nx = dx / len, ny = dy / len;
                float relVn = (c.freeEndVx - solo.vx) * nx + (c.freeEndVy - solo.vy) * ny;
                float tolerance = Math.max(0.01f, pixelsPerMeter * 0.0002f);
                if (len < c.ropeLen - tolerance || relVn <= 0f) continue;
                float invSolo = solo.fixed ? 0f : 1f / solo.mass;
                float impulse = relVn / (1f / 0.05f + invSolo);
                c.freeEndVx -= impulse / 0.05f * nx;
                c.freeEndVy -= impulse / 0.05f * ny;
                if (!solo.fixed) {
                    solo.vx += impulse * invSolo * nx;
                    solo.vy += impulse * invSolo * ny;
                    syncPhysState(solo);
                }
                continue;
            }
            boolean both = c.connectedObjA != null && c.connectedObjB != null;
            PhysObj a = both ? c.connectedObjA : null;
            PhysObj b = both ? c.connectedObjB
                    : (c.connectedObjA != null ? c.connectedObjA : c.connectedObjB);
            if (b == null) continue;
            float[] ep = getSpringEndpoints(c);
            float dx = ep[2] - ep[0], dy = ep[3] - ep[1];
            float len = magnitude(dx, dy);
            if (len < 0.001f) continue;
            float nx = dx / len, ny = dy / len;
            boolean freeA = a != null && !a.fixed;
            boolean freeB = !b.fixed;
            float invA = freeA ? 1f / a.mass : 0f;
            float invB = freeB ? 1f / b.mass : 0f;
            float invSum = invA + invB;
            if (invSum <= 0f) continue;
            float avx = freeA ? a.vx : 0f, avy = freeA ? a.vy : 0f;
            float bvx = freeB ? b.vx : 0f, bvy = freeB ? b.vy : 0f;
            float relVn = (bvx - avx) * nx + (bvy - avy) * ny;
            if (c.type == TYPE_ROPE && !c.ropeTaut) {
                float tolerance = Math.max(0.01f, pixelsPerMeter * 0.0002f);
                if (len < c.ropeLen - tolerance || relVn <= 0f) continue;
            }
            float impulse = relVn / invSum;
            if (freeA) {
                a.vx += impulse * invA * nx;
                a.vy += impulse * invA * ny;
                syncPhysState(a);
            }
            if (freeB) {
                b.vx -= impulse * invB * nx;
                b.vy -= impulse * invB * ny;
                syncPhysState(b);
            }
        }
    }

    void projectRelativeVelocity(PhysObj a, PhysObj b, float nx, float ny) {
        boolean freeA = a != null && !a.fixed;
        boolean freeB = b != null && !b.fixed;
        float invA = freeA ? 1f / a.mass : 0f;
        float invB = freeB ? 1f / b.mass : 0f;
        float invSum = invA + invB;
        if (invSum <= 0f) return;
        float avx = freeA ? a.vx : 0f, avy = freeA ? a.vy : 0f;
        float bvx = freeB ? b.vx : 0f, bvy = freeB ? b.vy : 0f;
        float relVn = (bvx - avx) * nx + (bvy - avy) * ny;
        float impulse = relVn / invSum;
        if (freeA) {
            a.vx += impulse * invA * nx;
            a.vy += impulse * invA * ny;
            syncPhysState(a);
        }
        if (freeB) {
            b.vx -= impulse * invB * nx;
            b.vy -= impulse * invB * ny;
            syncPhysState(b);
        }
    }

    void projectFreeEndRelativeVelocity(PhysObj c, PhysObj solo, float nx, float ny) {
        float invSolo = solo.fixed ? 0f : 1f / solo.mass;
        float invEnd = 1f / 0.05f;
        float relVn = (c.freeEndVx - solo.vx) * nx + (c.freeEndVy - solo.vy) * ny;
        float impulse = relVn / (invEnd + invSolo);
        c.freeEndVx -= impulse * invEnd * nx;
        c.freeEndVy -= impulse * invEnd * ny;
        if (!solo.fixed) {
            solo.vx += impulse * invSolo * nx;
            solo.vy += impulse * invSolo * ny;
            syncPhysState(solo);
        }
    }

    void densifyWrappedRope(PhysObj rope) {
        if (rope.ropeWrapPoints.size() < 2) return;
        float maxSeg = Math.max(6f, pixelsPerMeter * 0.06f);
        ArrayList<float[]> dense = new ArrayList<>();
        float totalLen = 0f;
        for (int i = 0; i < rope.ropeWrapPoints.size() - 1; i++) {
            float[] a = rope.ropeWrapPoints.get(i);
            float[] b = rope.ropeWrapPoints.get(i + 1);
            if (i == 0) dense.add(new float[]{a[0], a[1]});
            float dx = b[0] - a[0];
            float dy = b[1] - a[1];
            float len = magnitude(dx, dy);
            totalLen += len;
            int steps = Math.max(1, (int) Math.ceil(len / maxSeg));
            for (int s = 1; s <= steps; s++) {
                float t = s / (float) steps;
                dense.add(new float[]{a[0] + dx * t, a[1] + dy * t});
            }
        }
        rope.ropeWrapPoints.clear();
        rope.ropeParticles.clear();
        rope.ropeSegmentRestLengths.clear();
        float restScale = totalLen > 0.0001f ? rope.ropeLen / totalLen : 1f;
        for (int i = 0; i < dense.size(); i++) {
            float[] p = dense.get(i);
            rope.ropeWrapPoints.add(new float[]{p[0], p[1]});
            rope.ropeParticles.add(new float[]{p[0], p[1], 0f, 0f});
            if (i + 1 < dense.size()) {
                float[] next = dense.get(i + 1);
                rope.ropeSegmentRestLengths.add(
                        magnitude(next[0] - p[0], next[1] - p[1]) * restScale);
            }
        }
        rope.ropeParticlesInit = true;
    }

    void collideRopeParticle(PhysObj rope, float[] p) {
        float ropeRadius = 5f;
        if (p[1] > groundY - ropeRadius) {
            p[1] = groundY - ropeRadius;
            if (p[3] > 0f) p[3] = -p[3] * 0.05f;
            p[2] *= 0.6f;
        }
        if (p[0] < wallLeftX + ropeRadius) {
            p[0] = wallLeftX + ropeRadius;
            if (p[2] < 0f) p[2] = 0f;
        }
        if (p[0] > wallRightX - ropeRadius) {
            p[0] = wallRightX - ropeRadius;
            if (p[2] > 0f) p[2] = 0f;
        }
        for (PhysObj other : objects) {
            if (other == rope || isConstraintBody(other)) continue;
            if (other.type == TYPE_RING) continue;
            float nx = 0f;
            float ny = 0f;
            float pen = 0f;
            if (other.type == TYPE_BALL || other.type == TYPE_PULLEY) {
                float dx = p[0] - other.x;
                float dy = p[1] - other.y;
                float dist = magnitude(dx, dy);
                float minD = halfWidth(other) + ropeRadius + (other.type == TYPE_PULLEY ? 3f : 0f);
                if (dist < minD) {
                    if (dist < 0.001f) {
                        nx = 0f;
                        ny = -1f;
                        pen = minD;
                    } else {
                        nx = dx / dist;
                        ny = dy / dist;
                        pen = minD - dist;
                    }
                }
            } else if (other.type == TYPE_RAMP_STRAIGHT) {
                float rL = other.x - other.w / 2f;
                float rR = other.x + other.w / 2f;
                float rB = other.y + other.h / 2f;
                if (p[0] >= rL - ropeRadius && p[0] <= rR + ropeRadius) {
                    float tt = (p[0] - rL) / Math.max(1f, other.w);
                    if (tt < 0f) tt = 0f;
                    if (tt > 1f) tt = 1f;
                    float effT = other.isHorizontal ? tt : 1f - tt;
                    float surfY = rB - effT * other.h;
                    float slope = other.isHorizontal ? -other.h / Math.max(1f, other.w) : other.h / Math.max(1f, other.w);
                    float angle = (float) Math.atan(slope);
                    nx = (float) Math.sin(angle);
                    ny = -(float) Math.cos(angle);
                    float signed = (p[0] - (rL + tt * other.w)) * nx + (p[1] - surfY) * ny;
                    if (signed < ropeRadius && p[1] > surfY - other.h - ropeRadius) pen = ropeRadius - signed;
                }
            } else if (other.type == TYPE_RAMP_ARC) {
                ArcContact ac = new ArcContact();
                if (getArcContact(other, p[0], p[1], ac) && (!ac.clamped || magnitude(p[0] - ac.sx, p[1] - ac.sy) <= ropeRadius + 12f)) {
                    float radial = magnitude(p[0] - ac.cx, p[1] - ac.cy);
                    float target = ac.radius - ropeRadius;
                    if (radial > target && radial - target < Math.max(40f, ropeRadius * 4f)) {
                        nx = -ac.radialX;
                        ny = -ac.radialY;
                        pen = radial - target;
                    }
                }
            } else {
                float cos = (float) Math.cos(other.rotation);
                float sin = (float) Math.sin(other.rotation);
                float dx = p[0] - other.x;
                float dy = p[1] - other.y;
                float lx = dx * cos + dy * sin;
                float ly = -dx * sin + dy * cos;
                float hw = other.w / 2f + ropeRadius;
                float hh = other.h / 2f + ropeRadius;
                if (lx > -hw && lx < hw && ly > -hh && ly < hh) {
                    float dL = lx + hw;
                    float dR = hw - lx;
                    float dT = ly + hh;
                    float dB = hh - ly;
                    float minP = Math.min(Math.min(dL, dR), Math.min(dT, dB));
                    float lnx;
                    float lny;
                    if (minP == dL) { lnx = -1f; lny = 0f; }
                    else if (minP == dR) { lnx = 1f; lny = 0f; }
                    else if (minP == dT) { lnx = 0f; lny = -1f; }
                    else { lnx = 0f; lny = 1f; }
                    nx = lnx * cos - lny * sin;
                    ny = lnx * sin + lny * cos;
                    pen = minP;
                }
            }
            if (pen > 0f) {
                p[0] += nx * pen;
                p[1] += ny * pen;
                float vn = p[2] * nx + p[3] * ny;
                if (vn < 0f) {
                    p[2] -= vn * nx;
                    p[3] -= vn * ny;
                }
            }
        }
    }

    void collideRopeSegments(PhysObj rope, boolean anchorFirst, boolean anchorLast) {
        int n = rope.ropeParticles.size();
        if (n < 2) return;
        for (int i = 0; i < n - 1; i++) {
            float[] a = rope.ropeParticles.get(i);
            float[] b = rope.ropeParticles.get(i + 1);
            boolean fixA = i == 0 && anchorFirst;
            boolean fixB = i == n - 2 && anchorLast;
            for (PhysObj other : objects) {
                if (other.type != TYPE_PULLEY && other.type != TYPE_BALL) continue;
                float abx = b[0] - a[0];
                float aby = b[1] - a[1];
                float lenSq = abx * abx + aby * aby;
                if (lenSq < 0.001f) continue;
                float t0 = ((other.x - a[0]) * abx + (other.y - a[1]) * aby) / lenSq;
                if (t0 < 0f) t0 = 0f;
                if (t0 > 1f) t0 = 1f;
                float cx = a[0] + abx * t0;
                float cy = a[1] + aby * t0;
                float dx = cx - other.x;
                float dy = cy - other.y;
                float dist = magnitude(dx, dy);
                float minD = halfWidth(other) + 7f;
                if (dist >= minD) continue;
                float nx;
                float ny;
                if (dist < 0.001f) {
                    nx = -aby;
                    ny = abx;
                    float nl = magnitude(nx, ny);
                    if (nl < 0.001f) { nx = 0f; ny = -1f; }
                    else { nx /= nl; ny /= nl; }
                } else {
                    nx = dx / dist;
                    ny = dy / dist;
                }
                float push = minD - dist;
                if (!fixA && !fixB) {
                    a[0] += nx * push * (1f - t0);
                    a[1] += ny * push * (1f - t0);
                    b[0] += nx * push * t0;
                    b[1] += ny * push * t0;
                } else if (!fixA) {
                    a[0] += nx * push;
                    a[1] += ny * push;
                } else if (!fixB) {
                    b[0] += nx * push;
                    b[1] += ny * push;
                }
                float vna = a[2] * nx + a[3] * ny;
                if (!fixA && vna < 0f) {
                    a[2] -= vna * nx;
                    a[3] -= vna * ny;
                }
                float vnb = b[2] * nx + b[3] * ny;
                if (!fixB && vnb < 0f) {
                    b[2] -= vnb * nx;
                    b[3] -= vnb * ny;
                }
            }
            for (int s = 1; s <= 2; s++) {
                float t = s / 3f;
                float sx = a[0] + (b[0] - a[0]) * t;
                float sy = a[1] + (b[1] - a[1]) * t;
                float[] sample = new float[]{sx, sy, (a[2] + b[2]) * 0.5f, (a[3] + b[3]) * 0.5f};
                collideRopeParticle(rope, sample);
                float dx = sample[0] - sx;
                float dy = sample[1] - sy;
                if (dx * dx + dy * dy <= 0.0001f) continue;
                if (!fixA && !fixB) {
                    a[0] += dx * (1f - t);
                    a[1] += dy * (1f - t);
                    b[0] += dx * t;
                    b[1] += dy * t;
                } else if (!fixA) {
                    a[0] += dx;
                    a[1] += dy;
                } else if (!fixB) {
                    b[0] += dx;
                    b[1] += dy;
                }
            }
        }
    }

    void anchorWrappedRopeEndpoints(PhysObj o) {
        int n = o.ropeParticles.size();
        if (n < 2) return;
        if (o.connectedObjA != null) {
            float[] fp = o.ropeParticles.get(0);
            float[] next = o.ropeParticles.get(1);
            float[] sp = getSurfacePoint(o.connectedObjA, next[0], next[1]);
            fp[0] = sp[0]; fp[1] = sp[1];
            fp[2] = o.connectedObjA.vx; fp[3] = o.connectedObjA.vy;
        } else if (o.connectedToCeiling && o.connectedObjA == null && o.ceilingSegmentId >= 0 && o.ceilingSegmentId < ceilingSegments.size()) {
            CeilingSegment cs = ceilingSegments.get(o.ceilingSegmentId);
            float[] fp = o.ropeParticles.get(0);
            fp[0] = o.ceilingAnchorX != 0 ? o.ceilingAnchorX : (cs.x1 + cs.x2) / 2;
            fp[1] = cs.y1; fp[2] = 0; fp[3] = 0;
        } else if (o.connectedToGround && o.connectedObjA == null) {
            float[] fp = o.ropeParticles.get(0);
            fp[0] = o.groundAnchorX; fp[1] = groundY; fp[2] = 0; fp[3] = 0;
        } else if (o.connectedToWallLeft && o.connectedObjA == null) {
            float[] fp = o.ropeParticles.get(0);
            fp[0] = wallLeftX; fp[1] = o.wallAnchorY; fp[2] = 0; fp[3] = 0;
        } else if (o.connectedToWallRight && o.connectedObjA == null) {
            float[] fp = o.ropeParticles.get(0);
            fp[0] = wallRightX; fp[1] = o.wallAnchorY; fp[2] = 0; fp[3] = 0;
        }
        if (o.connectedObjB != null) {
            float[] lp = o.ropeParticles.get(n - 1);
            float[] prev = o.ropeParticles.get(n - 2);
            float[] sp = getSurfacePoint(o.connectedObjB, prev[0], prev[1]);
            lp[0] = sp[0]; lp[1] = sp[1];
            lp[2] = o.connectedObjB.vx; lp[3] = o.connectedObjB.vy;
        } else if (o.connectedToCeiling && o.connectedObjA != null && o.connectedObjB == null && o.ceilingSegmentId >= 0 && o.ceilingSegmentId < ceilingSegments.size()) {
            CeilingSegment cs = ceilingSegments.get(o.ceilingSegmentId);
            float[] lp = o.ropeParticles.get(n - 1);
            lp[0] = o.ceilingAnchorX != 0 ? o.ceilingAnchorX : (cs.x1 + cs.x2) / 2;
            lp[1] = cs.y1; lp[2] = 0; lp[3] = 0;
        } else if (o.connectedToGround && o.connectedObjA != null && o.connectedObjB == null) {
            float[] lp = o.ropeParticles.get(n - 1);
            lp[0] = o.groundAnchorX; lp[1] = groundY; lp[2] = 0; lp[3] = 0;
        } else if (o.connectedToWallLeft && o.connectedObjA != null && o.connectedObjB == null) {
            float[] lp = o.ropeParticles.get(n - 1);
            lp[0] = wallLeftX; lp[1] = o.wallAnchorY; lp[2] = 0; lp[3] = 0;
        } else if (o.connectedToWallRight && o.connectedObjA != null && o.connectedObjB == null) {
            float[] lp = o.ropeParticles.get(n - 1);
            lp[0] = wallRightX; lp[1] = o.wallAnchorY; lp[2] = 0; lp[3] = 0;
        }
    }





    void stepSimulation(float dt) {
        if (dt <= 0f || Float.isNaN(dt) || Float.isInfinite(dt)) return;
        int substeps = calculatePhysicsSubsteps(dt);
        float subDt = dt / substeps;
        for (int si = 0; si < substeps; si++) {
            stepSimulationInternal(subDt);
        }
        symUpdateStateClocks();
    }

    int calculatePhysicsSubsteps(float dt) {
        float maxRatio = 0f;
        for (PhysObj o : objects) {
            if (o.fixed || isConstraintBody(o)) continue;
            float speed = magnitude(o.vx, o.vy);
            float minSize = Math.max(6f, Math.min(Math.max(1f, o.w), Math.max(1f, o.h)));
            float ratio = speed * dt / minSize;
            if (ratio > maxRatio) maxRatio = ratio;
        }
        int steps = (int) Math.ceil(maxRatio / 0.35f);
        float stiffRatio = 0f;
        for (PhysObj o : objects) {
            if (o.type != TYPE_SPRING && o.type != TYPE_ROPE) continue;
            float invSum = 0f;
            if (o.connectedObjA != null && !o.connectedObjA.fixed) invSum += 1f / Math.max(0.001f, o.connectedObjA.mass);
            if (o.connectedObjB != null && !o.connectedObjB.fixed) invSum += 1f / Math.max(0.001f, o.connectedObjB.mass);
            if (isFreeEndConstraint(o)) invSum += 1f / 0.05f;
            if (invSum <= 0f) invSum = anchoredSpringPressInvMass(o, dt);
            if (invSum <= 0f) continue;
            float ratio = (float) Math.sqrt(Math.max(1f, o.springK) * invSum) * dt / 0.4f;
            if (ratio > stiffRatio) stiffRatio = ratio;
        }
        if (stiffRatio > steps) steps = (int) Math.ceil(stiffRatio);
        float curvatureRatio = 0f;
        for (PhysObj o : objects) {
            if (o.type != TYPE_ROD || o.isCutHalf || o.remoteLock || o.angleFixed) continue;
            PhysObj a = o.connectedObjA;
            PhysObj b = o.connectedObjB;
            if (a == null && b == null) continue;
            float avx = a != null && !a.fixed ? a.vx : 0f;
            float avy = a != null && !a.fixed ? a.vy : 0f;
            float bvx = b != null && !b.fixed ? b.vx : 0f;
            float bvy = b != null && !b.fixed ? b.vy : 0f;
            float relSpeed = magnitude(bvx - avx, bvy - avy);
            float ratio = relSpeed * dt / (Math.max(6f, o.rodLen) * 0.01f);
            if (ratio > curvatureRatio) curvatureRatio = ratio;
        }
        for (PhysObj o : objects) {
            if (o.fixed || !o.prevContactRamp || !isFullCirclePipe(o.prevRampContactObj)) continue;
            PhysObj ramp = o.prevRampContactObj;
            ArcContact contact = new ArcContact();
            if (!getArcContact(ramp, o.x, o.y, contact)) continue;
            float halfSize = o.type == TYPE_BALL ? o.w / 2f : o.h / 2f;
            float targetDist = Math.max(6f, contact.radius - halfSize);
            float rampVx = ramp.fixed ? 0f : ramp.vx;
            float rampVy = ramp.fixed ? 0f : ramp.vy;
            float relSpeed = magnitude(o.vx - rampVx, o.vy - rampVy);
            float ratio = relSpeed * dt / (targetDist * 0.01f);
            if (ratio > curvatureRatio) curvatureRatio = ratio;
        }
        if (curvatureRatio > steps) steps = (int) Math.ceil(curvatureRatio);
        if (rodChainActive) {
            int chainSteps = (int) Math.ceil(dt / (FIXED_DT * 0.25f));
            if (chainSteps > steps) steps = chainSteps;
        }
        if (steps < 1) steps = 1;
        if (dt > FIXED_DT) {
            int timeSteps = (int) Math.ceil(dt / FIXED_DT);
            if (timeSteps > steps) steps = timeSteps;
        }
        if (steps > 64) steps = 64;
        return steps;
    }

    float anchoredSpringPressInvMass(PhysObj o, float dt) {
        if (o.type != TYPE_SPRING || o.isCutHalf) return 0f;
        if (o.connectedObjA != null || o.connectedObjB != null) return 0f;
        float ax, ay, ux, uy;
        if (o.connectedToCeiling && o.ceilingSegmentId >= 0 && o.ceilingSegmentId < ceilingSegments.size()) {
            CeilingSegment cs = ceilingSegments.get(o.ceilingSegmentId);
            ax = o.ceilingAnchorX != 0 ? o.ceilingAnchorX : (cs.x1 + cs.x2) / 2;
            ay = cs.y1;
            ux = 0f; uy = 1f;
        } else if (o.connectedToGround) {
            ax = o.groundAnchorX; ay = groundY;
            ux = 0f; uy = -1f;
        } else if (o.connectedToWallLeft) {
            ax = wallLeftX; ay = o.wallAnchorY;
            ux = 1f; uy = 0f;
        } else if (o.connectedToWallRight) {
            ax = wallRightX; ay = o.wallAnchorY;
            ux = -1f; uy = 0f;
        } else {
            return 0f;
        }
        float best = 0f;
        for (PhysObj other : objects) {
            if (other == o || other.fixed) continue;
            if (isConstraintBody(other)) continue;
            if (other.type == TYPE_RING) continue;
            if (other.type == TYPE_RAMP_STRAIGHT || other.type == TYPE_RAMP_ARC) continue;
            float ohw = other.w / 2;
            float ohh = other.type == TYPE_BALL ? other.w / 2 : other.h / 2;
            float relCx = other.x - ax, relCy = other.y - ay;
            float along = relCx * ux + relCy * uy;
            float lateral = Math.abs(relCy * ux - relCx * uy);
            float halfAlong = (ux != 0) ? ohw : ohh;
            float halfLat = (ux != 0) ? ohh : ohw;
            if (lateral > halfLat + 14f) continue;
            if (along <= 0) continue;
            float reach = o.naturalLen + magnitude(other.vx, other.vy) * dt + 4f;
            if (along - halfAlong > reach) continue;
            float inv = 1f / Math.max(0.001f, other.mass);
            if (inv > best) best = inv;
        }
        return best;
    }

    void stepSimulationInternal(float dt) {
        ArrayList<PhysObj> toRemove = new ArrayList<>();
        float windX = windSpeed * (float) Math.cos(windDirection * Math.PI / 180);
        float windY = -windSpeed * (float) Math.sin(windDirection * Math.PI / 180);

        for (PhysObj o : objects) {
            o.reactionForceX = 0f;
            o.reactionForceY = 0f;
            if (o.fixed) {
                o.constraintForceX = 0f;
                o.constraintForceY = 0f;
                o.supportForceX = 0f;
                o.supportForceY = 0f;
                o.rodSupportForceX = 0f;
                o.rodSupportForceY = 0f;
                o.frictionForceX = 0f;
                o.frictionForceY = 0f;
            }
            if (o.fixed || isConstraintBody(o)) continue;
            o.contactGround = false;
            o.contactWallLeft = false;
            o.contactWallRight = false;
            o.prevContactRamp = o.contactRamp;
            o.prevRampContactObj = o.rampContactObj;
            o.prevRampNx = o.rampNx;
            o.prevRampNy = o.rampNy;
            o.contactRamp = false;
            o.rampContactObj = null;
            o.prevVx = o.vx;
            o.prevVy = o.vy;
            o.prevX = o.x;
            o.prevY = o.y;
            o.prevPhysVx = o.physVx;
            o.prevPhysVy = o.physVy;
            if (o.showTrajectory) {
                o.trajectory.add(new float[]{o.x, o.y});
                if (o.trajectory.size() > 500) {
                    o.trajectory.remove(0);
                }
            }
            if (o.showState) {
                float v = magnitude(o.vx, o.vy) / pixelsPerMeter;
                float displacement = magnitude(o.x - o.initialX, o.y - o.initialY) / pixelsPerMeter;
                float a = (float) Math.sqrt(o.accelX * o.accelX + o.accelY * o.accelY);

                o.velocityHistory.add(v);
                o.positionHistory.add(displacement);
                o.accelHistory.add(a);

                if (o.velocityHistory.size() > 100) o.velocityHistory.remove(0);
                if (o.positionHistory.size() > 100) o.positionHistory.remove(0);
                if (o.accelHistory.size() > 100) o.accelHistory.remove(0);
            }
        }

        for (PhysObj o : objects) {
            if (isConstraintBody(o)) continue;
            if (o.fixed) continue;
            if (o.forceCondition != null) {
                if (o.forceCondition.evaluate(dt, o, this)) {
                    o.appliedForceX = 0; o.appliedForceY = 0;
                    o.forceCondition = null;
                }
            } else if (o.forceDuration >= 0) {
                o.forceTimer += dt;
                if (o.forceTimer >= o.forceDuration) {
                    o.appliedForceX = 0; o.appliedForceY = 0;
                    o.forceDuration = -1f; o.forceTimer = 0f;
                }
            }
            if (o.velocityCondition != null) {
                if (o.velocityCondition.evaluate(dt, o, this)) {
                    o.vx = 0; o.vy = 0;
                    o.physVx = 0; o.physVy = 0;
                    o.velocityCondition = null;
                }
            }
            o.forceX = o.appliedForceX * (double) pixelsPerMeter;
            o.forceY = (o.appliedForceY + (double) o.mass * GRAVITY) * (double) pixelsPerMeter;
            o.constraintForceX = 0; o.constraintForceY = 0;
            o.supportForceX = 0; o.supportForceY = 0;
            o.rodSupportForceX = 0; o.rodSupportForceY = 0;
            o.frictionForceX = 0; o.frictionForceY = 0;

            o.forceX += windX * pixelsPerMeter;
            o.forceY += windY * pixelsPerMeter;

            if (airResistance > 0) {
                float v = magnitude(o.vx, o.vy);
                if (v > 0.001f) {
                    float dragForce = airResistance * pixelsPerMeter;
                    o.forceX -= dragForce * (o.vx / v);
                    o.forceY -= dragForce * (o.vy / v);
                }
            }
        }

        solveRodChainTensions(dt);
        constraintPostPass = false;
        for (PhysObj o : objects) {
            if (isConstraintBody(o)) {
                if (o.isCutHalf) {
                    if (o.type == TYPE_SPRING && o.cutHalfFollowObj != null && !o.cutHalfFollowObj.fixed) {
                        float fdx = o.cutHalfFreeX - o.cutHalfFollowObj.x;
                        float fdy = o.cutHalfFreeY - o.cutHalfFollowObj.y;
                        float flen = magnitude(fdx, fdy);
                        if (flen > 0.001f && Math.abs(o.deformation) > 0.001f) {
                            float forceMag = Math.max(-5000f, Math.min(5000f, o.deformation));
                            o.cutHalfFollowObj.forceX += forceMag * fdx / flen;
                            o.cutHalfFollowObj.forceY += forceMag * fdy / flen;
                        }
                    }
                    continue;
                }
                updateSpringRope(o, dt, toRemove);
            }
        }

        for (int rIdx = objects.size() - 1; rIdx >= 0; rIdx--) {
            PhysObj ro = objects.get(rIdx);
            if (ro.type == TYPE_SPRING && ro.cutDecaying
                    && ro.connectedObjA == null && ro.connectedObjB == null
                    && !ro.connectedToCeiling && !ro.connectedToGround && !ro.connectedToWallLeft && !ro.connectedToWallRight) {
                objects.remove(rIdx);
            }
        }

        for (PhysObj o : objects) {
            if (o.fixed || isConstraintBody(o)) continue;
            if (o.type == TYPE_RING) continue;
            float oBottom = (o.type == TYPE_BALL) ? o.y + o.w / 2 : o.y + o.h / 2;
            if (oBottom < groundY - 8) continue;
            float stackedNormal = 0f;
            for (PhysObj other : objects) {
                if (other == o || isConstraintBody(other) || other.fixed) continue;
                if (other.type == TYPE_RING) continue;
                if (o.type == TYPE_RAMP_STRAIGHT || o.type == TYPE_RAMP_ARC) {
                    if (other.contactRamp) {
                        float rL = o.x - o.w / 2, rR = o.x + o.w / 2;
                        if (other.x > rL && other.x < rR)
                            stackedNormal += other.mass * GRAVITY;
                    }
                } else {
                    float otherBot = (other.type == TYPE_BALL) ? other.y + other.w / 2 : other.y + other.h / 2;
                    float oTop = o.y - o.h / 2;
                    if (Math.abs(otherBot - oTop) < 10) {
                        float ow = Math.min(other.x + other.w / 2, o.x + o.w / 2)
                                - Math.max(other.x - other.w / 2, o.x - o.w / 2);
                        if (ow > 2) stackedNormal += other.mass * GRAVITY;
                    }
                }
            }
            if (stackedNormal > 0) {
                float extraFric = o.friction * stackedNormal * pixelsPerMeter;
                if (Math.abs(o.vx) > pixelsPerMeter * 0.005f) {
                    o.forceX += (o.vx > 0 ? -1f : 1f) * extraFric;
                } else {
                    if (Math.abs(o.forceX) <= extraFric) {
                        o.forceX = 0;
                    } else {
                        o.forceX += (o.forceX > 0 ? -1f : 1f) * extraFric;
                    }
                }
            }
        }

        updateWaterPhysics(dt);
        applyFullCirclePipeConstraintForces();

        for (PhysObj o : objects) {
            if (o.fixed || isConstraintBody(o)) continue;
            o.displayForceX = (float) o.forceX;
            o.displayForceY = (float) o.forceY;
            o.stepAccelX = o.forceX / o.mass;
            o.stepAccelY = o.forceY / o.mass;
            o.stepOtherForceX = o.forceX - o.constraintForceX - o.rodSupportForceX;
            o.stepOtherForceY = o.forceY - o.constraintForceY - o.rodSupportForceY;

            double oldVx = bestPhysValue(o.vx, o.physVx);
            double oldVy = bestPhysValue(o.vy, o.physVy);
            o.physVx = firstHalfKick(oldVx, o.stepAccelX, dt);
            o.physVy = firstHalfKick(oldVy, o.stepAccelY, dt);
            o.physX = advancePosition(
                    bestPhysValue(o.x, o.physX), o.physVx, dt);
            o.physY = advancePosition(
                    bestPhysValue(o.y, o.physY), o.physVy, dt);
            o.vx = (float) o.physVx;
            o.vy = (float) o.physVy;
            o.x = (float) o.physX;
            o.y = (float) o.physY;
            o.forceX = 0;
            o.forceY = 0;
        }

        projectRodChainConstraints();
        clearPostConstraintForces();
        solveRodChainTensions(dt);
        constraintPostPass = true;
        for (PhysObj o : objects) {
            if (shouldUpdateConstraintAfterIntegration(o)) {
                updateSpringRope(o, dt, toRemove);
            }
        }
        applyFullCirclePipeConstraintForces();
        constraintPostPass = false;
        completeVelocityVerlet(dt);
        projectPostVerletConstraintVelocities();

        for (PhysObj o : objects) {
            if (o.type != TYPE_RING) continue;
            if (o.ringHostRod == null || !objects.contains(o.ringHostRod)) continue;
            if (o.fixed) { o.vx = 0; o.vy = 0; o.physVx = 0; o.physVy = 0; continue; }
            PhysObj rod = o.ringHostRod;
            float[] eps = getSpringEndpoints(rod);
            float rx1 = eps[0], ry1 = eps[1], rx2 = eps[2], ry2 = eps[3];
            float rdx = rx2 - rx1, rdy = ry2 - ry1;
            float rlenSq = rdx * rdx + rdy * rdy;
            if (rlenSq < 0.0001f) continue;
            float t = ((o.x - rx1) * rdx + (o.y - ry1) * rdy) / rlenSq;
            if (t < 0f) t = 0f;
            if (t > 1f) t = 1f;
            o.ringParam = t;
            float projX = rx1 + t * rdx;
            float projY = ry1 + t * rdy;
            float rlen = (float) Math.sqrt(rlenSq);
            float tx = rdx / rlen, ty = rdy / rlen;
            float nx = -ty, ny = tx;
            PhysObj endA = rod.connectedObjA;
            PhysObj endB = rod.connectedObjB;
            float avx = endA != null && !endA.fixed ? endA.vx : 0f;
            float avy = endA != null && !endA.fixed ? endA.vy : 0f;
            float bvx = endB != null && !endB.fixed ? endB.vx : 0f;
            float bvy = endB != null && !endB.fixed ? endB.vy : 0f;
            float surfaceVx = avx * (1f - t) + bvx * t;
            float surfaceVy = avy * (1f - t) + bvy * t;
            float relativeVx = o.vx - surfaceVx;
            float relativeVy = o.vy - surfaceVy;
            float vAlong = relativeVx * tx + relativeVy * ty;
            float forcePerp = o.displayForceX * nx + o.displayForceY * ny;
            float nfx = -forcePerp * nx;
            float nfy = -forcePerp * ny;
            float oldDx = projX - o.x;
            float oldDy = projY - o.y;
            float oldDist = magnitude(oldDx, oldDy);
            if (magnitude(nfx, nfy) < 0.001f && oldDist > 0.001f) {
                float corrMag = Math.min(8000f, oldDist * o.mass / Math.max(0.001f, dt * dt));
                nfx = oldDx / oldDist * corrMag;
                nfy = oldDy / oldDist * corrMag;
            }
            o.supportForceX += nfx;
            o.supportForceY += nfy;
            float normalMag = magnitude(nfx, nfy);
            float fricLimit = o.friction * normalMag;
            float tangForce = o.displayForceX * tx + o.displayForceY * ty;
            float fricMag = 0f;
            if (fricLimit > 0.001f) {
                if (Math.abs(vAlong) > 0.01f) {
                    fricMag = vAlong > 0f ? -fricLimit : fricLimit;
                } else {
                    fricMag = Math.max(-fricLimit, Math.min(fricLimit, -tangForce));
                }
                float oldAlong = vAlong;
                vAlong += fricMag / o.mass * dt;
                if (oldAlong > 0f && vAlong < 0f) vAlong = 0f;
                if (oldAlong < 0f && vAlong > 0f) vAlong = 0f;
                o.frictionForceX += fricMag * tx;
                o.frictionForceY += fricMag * ty;
                float impulse = fricMag * dt;
                if (endA != null && !endA.fixed) {
                    endA.vx -= impulse * tx * (1f - t) / endA.mass;
                    endA.vy -= impulse * ty * (1f - t) / endA.mass;
                    syncPhysState(endA);
                }
                if (endB != null && !endB.fixed) {
                    endB.vx -= impulse * tx * t / endB.mass;
                    endB.vy -= impulse * ty * t / endB.mass;
                    syncPhysState(endB);
                }
            }
            if (t <= 0f && vAlong < 0f) vAlong = 0f;
            if (t >= 1f && vAlong > 0f) vAlong = 0f;
            o.vx = surfaceVx + vAlong * tx;
            o.vy = surfaceVy + vAlong * ty;
            o.x = projX;
            o.y = projY;
            o.physX = projX;
            o.physY = projY;
            o.physVx = o.vx;
            o.physVy = o.vy;
        }

        float gravityStepV = GRAVITY * pixelsPerMeter * dt * 1.5f;

        for (PhysObj o : objects) {
            if (o.fixed || isConstraintBody(o)) continue;
            if (o.type == TYPE_RING) continue;

            float offL, offR, offT, offB;
            if (!o.cutVertices.isEmpty()) {
                float[] bounds = getBoundsFromVertices(o.cutVertices, o.x, o.y, o.rotation);
                offL = o.x - bounds[0]; offR = bounds[1] - o.x;
                offT = o.y - bounds[2]; offB = bounds[3] - o.y;
            } else if (o.type == TYPE_BALL) {
                offL = offR = offT = offB = o.w / 2;
            } else {
                offL = offR = o.w / 2;
                offT = offB = o.h / 2;
            }
            float halfW = (offL + offR) / 2;
            float halfH = (offT + offB) / 2;

            if (o.type == TYPE_BALL && o.cutVertices.isEmpty()) {
                float radius = o.w / 2;
                if (o.y + radius > groundY) {
                    o.y = groundY - radius;
                    o.contactGround = true;
                    addGroundSupportForce(o);
                    if (o.vy > 0) {
                        if (o.vy <= gravityStepV) o.vy = 0;
                        else {
                            o.vy = -o.vy * o.restitution;
                        }
                    }
                    boolean onErasedGround = false;
                    for (float[] range : groundEraseRanges) {
                        if (o.x >= range[0] && o.x <= range[1]) { onErasedGround = true; break; }
                    }
                    if (!onErasedGround) {
                        float fdv = o.friction * GRAVITY * dt * pixelsPerMeter * 0.5f;
                        if (o.vx > 0) o.vx = Math.max(0, o.vx - fdv);
                        else if (o.vx < 0) o.vx = Math.min(0, o.vx + fdv);
                    }
                }
                if (o.x - radius < wallLeftX) {
                    o.x = wallLeftX + radius;
                    o.contactWallLeft = true;
                    if (o.vx < 0) {
                        float forceStepV = Math.abs(o.displayForceX) / o.mass * dt * 1.5f;
                        if (-o.vx <= forceStepV) o.vx = 0;
                        else o.vx = -o.vx * o.restitution;
                    }
                } else if (o.x + radius > wallRightX) {
                    o.x = wallRightX - radius;
                    o.contactWallRight = true;
                    if (o.vx > 0) {
                        float forceStepV = Math.abs(o.displayForceX) / o.mass * dt * 1.5f;
                        if (o.vx <= forceStepV) o.vx = 0;
                        else o.vx = -o.vx * o.restitution;
                    }
                }
                for (CeilingSegment cs : ceilingSegments) {
                    float ceilMinX = Math.min(cs.x1, cs.x2);
                    float ceilMaxX = Math.max(cs.x1, cs.x2);
                    float topY = o.y - radius;
                    if (isCeilingRodLinkedObject(o, cs)) continue;
                    if (o.x >= ceilMinX && o.x <= ceilMaxX && topY <= cs.y1 + 2) {
                        o.y = cs.y1 + radius + 1;
                        if (o.vy < 0) {
                            o.vy = -o.vy * o.restitution;
                        }
                    }
                }
            } else {
                float bottom = o.y + offB;
                if (bottom > groundY) {
                    o.y = groundY - offB;
                    o.contactGround = true;
                    addGroundSupportForce(o);
                    if (o.vy > 0) {
                        if (o.vy <= gravityStepV) o.vy = 0;
                        else {
                            o.vy = -o.vy * o.restitution;
                        }
                    }
                    boolean onErasedGround = false;
                    for (float[] range : groundEraseRanges) {
                        if (o.x >= range[0] && o.x <= range[1]) { onErasedGround = true; break; }
                    }
                    if (!onErasedGround) {
                        float frictionAccel = o.friction * GRAVITY;
                        if (o.type == TYPE_RAMP_STRAIGHT || o.type == TYPE_RAMP_ARC) {
                            if (o.displayForceY > o.mass * GRAVITY * pixelsPerMeter) {
                                frictionAccel = o.friction * o.displayForceY / (o.mass * pixelsPerMeter);
                            }
                        } else if (o.type == TYPE_BOARD || o.type == TYPE_BLOCK) {
                            float extraMass = 0;
                            float oTop = o.y - halfH;
                            float oLeft = o.x - halfW, oRight = o.x + halfW;
                            for (PhysObj other : objects) {
                                if (other == o || other.fixed || isConstraintBody(other)) continue;
                                float otherBottom = other.type == TYPE_BALL ? other.y + other.w / 2 : other.y + other.h / 2;
                                float overlapX = Math.min(oRight, other.x + other.w / 2) - Math.max(oLeft, other.x - other.w / 2);
                                if (Math.abs(otherBottom - oTop) < 12 && overlapX > 2) {
                                    extraMass += other.mass;
                                }
                            }
                            frictionAccel += o.friction * extraMass / o.mass * GRAVITY;
                        }
                        float dv = frictionAccel * dt * pixelsPerMeter;
                        if (o.vx > 0) {
                            o.vx = Math.max(0, o.vx - dv);
                        } else if (o.vx < 0) {
                            o.vx = Math.min(0, o.vx + dv);
                        }
                    }
                }
                if (o.x - offL < wallLeftX) {
                    o.x = wallLeftX + offL;
                    o.contactWallLeft = true;
                    if (o.vx < 0) {
                        float forceStepV = Math.abs(o.displayForceX) / o.mass * dt * 1.5f;
                        if (-o.vx <= forceStepV) o.vx = 0;
                        else o.vx = -o.vx * o.restitution;
                    }
                } else if (o.x + offR > wallRightX) {
                    o.x = wallRightX - offR;
                    o.contactWallRight = true;
                    if (o.vx > 0) {
                        float forceStepV = Math.abs(o.displayForceX) / o.mass * dt * 1.5f;
                        if (o.vx <= forceStepV) o.vx = 0;
                        else o.vx = -o.vx * o.restitution;
                    }
                }
                for (CeilingSegment cs : ceilingSegments) {
                    float ceilMinX = Math.min(cs.x1, cs.x2);
                    float ceilMaxX = Math.max(cs.x1, cs.x2);
                    float topY = o.y - offT;
                    float oL2 = o.x - offL, oR2 = o.x + offR;
                    if (isCeilingRodLinkedObject(o, cs)) continue;
                    if (oR2 >= ceilMinX && oL2 <= ceilMaxX && topY <= cs.y1 + 2) {
                        o.y = cs.y1 + offT + 1;
                        if (o.vy < 0) {
                            o.vy = -o.vy * o.restitution;
                        }
                    }
                }
            }
        }

        for (PhysObj spring : objects) {
            if (spring.type != TYPE_SPRING || spring.isCutHalf) continue;
            int endCount = (spring.connectedObjA != null ? 1 : 0) + (spring.connectedObjB != null ? 1 : 0)
                    + ((spring.connectedToCeiling || spring.connectedToGround
                        || spring.connectedToWallLeft || spring.connectedToWallRight) ? 1 : 0);
            if (endCount == 1) continue;
            float[] seps = getSpringEndpoints(spring);
            float sx1 = seps[0], sy1 = seps[1], sx2 = seps[2], sy2 = seps[3];
            float sdx = sx2 - sx1, sdy = sy2 - sy1;
            float segLenSq = sdx * sdx + sdy * sdy;
            if (segLenSq < 100f) continue;
            float springHT = spring.h / 2f + 6f;
            for (PhysObj obj : objects) {
                if (obj == spring || isConstraintBody(obj)) continue;
                if (obj.type == TYPE_RING) continue;
                if (obj.fixed || obj.type == TYPE_RAMP_STRAIGHT || obj.type == TYPE_RAMP_ARC) continue;
                if (spring.connectedObjA == obj || spring.connectedObjB == obj) continue;
                float objR = obj.type == TYPE_BALL ? obj.w / 2f : Math.min(obj.w, obj.h) * 0.45f;
                float minD = springHT + objR;
                float t = ((obj.x - sx1) * sdx + (obj.y - sy1) * sdy) / segLenSq;
                t = Math.max(0.05f, Math.min(0.95f, t));
                float cpx = sx1 + t * sdx, cpy = sy1 + t * sdy;
                float pdx = obj.x - cpx, pdy = obj.y - cpy;
                float dSq2 = pdx * pdx + pdy * pdy;
                if (dSq2 >= minD * minD || dSq2 < 0.0001f) continue;
                float dist2 = (float) Math.sqrt(dSq2);
                float nx = pdx / dist2, ny = pdy / dist2;
                float pen = minD - dist2;
                obj.x += nx * pen; obj.y += ny * pen;
                PhysObj endA = spring.connectedObjA;
                PhysObj endB = spring.connectedObjB;
                boolean freeA = endA != null && !endA.fixed;
                boolean freeB = endB != null && !endB.fixed;
                float invA = freeA ? (1f - t) * (1f - t) / endA.mass : 0f;
                float invB = freeB ? t * t / endB.mass : 0f;
                float surfaceVx = (freeA ? endA.vx * (1f - t) : 0f)
                        + (freeB ? endB.vx * t : 0f);
                float surfaceVy = (freeA ? endA.vy * (1f - t) : 0f)
                        + (freeB ? endB.vy * t : 0f);
                float vn = (obj.vx - surfaceVx) * nx + (obj.vy - surfaceVy) * ny;
                float invSum = 1f / obj.mass + invA + invB;
                if (vn < 0 && invSum > 0f) {
                    float impulse = -(1f + obj.restitution) * vn / invSum;
                    obj.vx += impulse / obj.mass * nx;
                    obj.vy += impulse / obj.mass * ny;
                    if (freeA) {
                        endA.vx -= impulse * (1f - t) / endA.mass * nx;
                        endA.vy -= impulse * (1f - t) / endA.mass * ny;
                        syncPhysState(endA);
                    }
                    if (freeB) {
                        endB.vx -= impulse * t / endB.mass * nx;
                        endB.vy -= impulse * t / endB.mass * ny;
                        syncPhysState(endB);
                    }
                }
            }
        }

        for (PhysObj o : objects) {
            if (o.type != TYPE_ROPE || !o.ropeWrapped || o.ropeWrapPoints.isEmpty()) continue;
            if (usesAnalyticWrap(o)) continue;
            if (!o.ropeParticlesInit) {
                densifyWrappedRope(o);
                for (float[] p2 : o.ropeParticles) {
                    collideRopeParticle(o, p2);
                }
            }
            int n = o.ropeParticles.size();
            if (n < 2) continue;

            anchorWrappedRopeEndpoints(o);

            boolean anchorFirst = o.connectedObjA != null || o.connectedToCeiling || o.connectedToGround || o.connectedToWallLeft || o.connectedToWallRight;
            boolean anchorLast = o.connectedObjB != null || (o.connectedObjA != null && (o.connectedToCeiling || o.connectedToGround || o.connectedToWallLeft || o.connectedToWallRight));
            for (int pi = 0; pi < n; pi++) {
                if (pi == 0 && anchorFirst) continue;
                if (pi == n - 1 && anchorLast) continue;
                float[] p2 = o.ropeParticles.get(pi);
                p2[3] = (float) firstHalfKick(
                        p2[3], GRAVITY * pixelsPerMeter, dt);
                p2[0] = (float) advancePosition(p2[0], p2[2], dt);
                p2[1] = (float) advancePosition(p2[1], p2[3], dt);
                p2[3] = (float) secondHalfKick(
                        p2[3], GRAVITY * pixelsPerMeter, dt);
                collideRopeParticle(o, p2);
            }
            collideRopeSegments(o, anchorFirst, anchorLast);

            float totalLen = 0;
            float[] segLens = new float[n - 1];
            for (int pi = 0; pi < n - 1; pi++) {
                float[] a2 = o.ropeParticles.get(pi), b2 = o.ropeParticles.get(pi + 1);
                float sdx = b2[0] - a2[0], sdy = b2[1] - a2[1];
                segLens[pi] = magnitude(sdx, sdy);
                totalLen += segLens[pi];
            }
            if (o.ropeSegmentRestLengths.size() != n - 1) {
                o.ropeSegmentRestLengths.clear();
                float initialScale = totalLen > 0.001f ? o.ropeLen / totalLen : 1f;
                for (int pi = 0; pi < n - 1; pi++) {
                    o.ropeSegmentRestLengths.add(segLens[pi] * initialScale);
                }
            }
            for (int iter = 0; iter < 20; iter++) {
                anchorWrappedRopeEndpoints(o);
                for (int pi = 0; pi < n - 1; pi++) {
                    float[] a2 = o.ropeParticles.get(pi), b2 = o.ropeParticles.get(pi + 1);
                    float targetLen = o.ropeSegmentRestLengths.get(pi);
                    float cdx = b2[0] - a2[0], cdy = b2[1] - a2[1];
                    float curLen2 = magnitude(cdx, cdy);
                    if (curLen2 < 0.001f) continue;
                    float diff = (curLen2 - targetLen) / curLen2 * 0.5f;
                    boolean fixA = (pi == 0 && anchorFirst);
                    boolean fixB = (pi == n - 2 && anchorLast);
                    if (!fixA) { a2[0] += cdx * diff; a2[1] += cdy * diff; }
                    if (!fixB) { b2[0] -= cdx * diff; b2[1] -= cdy * diff; }
                }
                for (int pi = 0; pi < n; pi++) {
                    if (pi == 0 && anchorFirst) continue;
                    if (pi == n - 1 && anchorLast) continue;
                    float[] pp = o.ropeParticles.get(pi);
                    collideRopeParticle(o, pp);
                }
                collideRopeSegments(o, anchorFirst, anchorLast);
            }
            anchorWrappedRopeEndpoints(o);
            collideRopeSegments(o, anchorFirst, anchorLast);

            for (int pi = 0; pi < n; pi++) {
                float[] p2 = o.ropeParticles.get(pi);
                o.ropeWrapPoints.get(pi)[0] = p2[0];
                o.ropeWrapPoints.get(pi)[1] = p2[1];
            }
            float[] fp2 = o.ropeWrapPoints.get(0), lp2 = o.ropeWrapPoints.get(n - 1);
            o.x = (fp2[0] + lp2[0]) / 2;
            o.y = (fp2[1] + lp2[1]) / 2;
        }

        for (int iter = 0; iter < 8; iter++) {
            for (int i = 0; i < objects.size(); i++) {
                PhysObj a = objects.get(i);
                if (isConstraintBody(a)) continue;
                if (a.type == TYPE_RING) continue;

                for (int j = i + 1; j < objects.size(); j++) {
                    PhysObj b = objects.get(j);
                    if (isConstraintBody(b)) continue;
                    if (b.type == TYPE_RING) continue;
                    if (a.fixed && b.fixed) continue;

                    if (a.type == TYPE_CONTAINER || b.type == TYPE_CONTAINER) {
                        PhysObj cont = (a.type == TYPE_CONTAINER) ? a : b;
                        PhysObj other = (a.type == TYPE_CONTAINER) ? b : a;
                        if (!other.fixed) resolveContainerWalls(other, cont);
                        continue;
                    }

                    if (a.type == TYPE_RAMP_STRAIGHT || a.type == TYPE_RAMP_ARC) {
                        if (!b.fixed && b.type != TYPE_RAMP_STRAIGHT && b.type != TYPE_RAMP_ARC) {
                            resolveRampCollision(b, a, dt);
                        }
                        continue;
                    }

                    if (b.type == TYPE_RAMP_STRAIGHT || b.type == TYPE_RAMP_ARC) {
                        if (!a.fixed && a.type != TYPE_RAMP_STRAIGHT && a.type != TYPE_RAMP_ARC) {
                            resolveRampCollision(a, b, dt);
                        }
                        continue;
                    }

                    if (a.type == TYPE_BALL || b.type == TYPE_BALL
                            || a.type == TYPE_PULLEY || b.type == TYPE_PULLEY) {
                        boolean bIsCircle = b.type == TYPE_BALL || b.type == TYPE_PULLEY;
                        boolean aIsCircle = a.type == TYPE_BALL || a.type == TYPE_PULLEY;
                        if (bIsCircle && !aIsCircle) {
                            resolveBallCollision(b, a);
                        } else {
                            resolveBallCollision(a, b);
                        }
                    } else {
                        resolveAABB(a, b);
                    }
                }
            }
        }
        projectFullCirclePipeConstraintVelocities();

        for (PhysObj o : objects) {
            if (o.type == TYPE_PULLEY && o.pulleyBoundRamp != null) {
                PhysObj ramp = o.pulleyBoundRamp;
                float topCornerX = ramp.isHorizontal ? ramp.x + ramp.w/2 : ramp.x - ramp.w/2;
                float topCornerY = ramp.y - ramp.h/2;
                o.x = topCornerX; o.y = topCornerY;
                o.vx = 0; o.vy = 0;
                o.physX = topCornerX; o.physY = topCornerY;
                o.physVx = 0; o.physVy = 0;
            }
        }

        alignObjectsOnRotatedSurfaces();

        for (PhysObj o : objects) {
            if (!o.fixed && !o.contactRamp && o.type != TYPE_SPRING && o.type != TYPE_ROPE && o.type != TYPE_BALL
                    && o.type != TYPE_ROD && o.type != TYPE_RING) {
                o.rotation = 0;
            }
        }

        for (PhysObj o : objects) {
            if (o.fixed || isConstraintBody(o)) continue;
            if (o.contactWallLeft && o.vx < 0) o.vx = 0;
            if (o.contactWallRight && o.vx > 0) o.vx = 0;
        }

        for (PhysObj o : objects) {
            if (o.fixed || isConstraintBody(o)) continue;
            double ax;
            double ay;
            double curVx = bestPhysValue(o.vx, o.physVx);
            double curVy = bestPhysValue(o.vy, o.physVy);
            double basePrevVx = bestPhysValue(o.prevVx, o.prevPhysVx);
            double basePrevVy = bestPhysValue(o.prevVy, o.prevPhysVy);
            double ddtA = dt;
            double ppmA = pixelsPerMeter;
            boolean circularMotion = o.contactRamp
                    && o.rampContactObj != null
                    && o.rampContactObj.type == TYPE_RAMP_ARC
                    && o.rampContactObj.w <= 1f && !o.rampContactObj.pipeMode;
            if (circularMotion) {
                ArcContact accelContact = new ArcContact();
                if (getArcContact(o.rampContactObj, o.x, o.y, accelContact)) {
                    float halfSize = o.type == TYPE_BALL ? o.w / 2f : o.h / 2f;
                    float targetDist = Math.max(1f, accelContact.radius - halfSize);
                    double vTang = curVx * accelContact.tangentX
                            + curVy * accelContact.tangentY;
                    double inwardAccel = vTang * vTang / targetDist / ppmA;
                    double tangentialAccel = (o.displayForceX * (double) accelContact.tangentX
                            + o.displayForceY * (double) accelContact.tangentY)
                            / ((double) o.mass * ppmA);
                    tangentialAccel += (o.frictionForceX * (double) accelContact.tangentX
                            + o.frictionForceY * (double) accelContact.tangentY) / (double) o.mass;
                    ax = -accelContact.radialX * inwardAccel
                            + accelContact.tangentX * tangentialAccel;
                    ay = -accelContact.radialY * inwardAccel
                            + accelContact.tangentY * tangentialAccel;
                } else {
                    ax = (curVx - basePrevVx) / ddtA / ppmA;
                    ay = (curVy - basePrevVy) / ddtA / ppmA;
                }
            } else {
                ax = (curVx - basePrevVx) / ddtA / ppmA;
                ay = (curVy - basePrevVy) / ddtA / ppmA;
            }
            if (o.contactGround && o.vy == 0 && o.prevVy == 0) ay = 0;
            o.accelX = ax;
            o.accelY = ay;
        }

        for (int i = objects.size() - 1; i >= 0; i--) {
            PhysObj o = objects.get(i);
            if (!o.isCutHalf) continue;
            float ax = o.cutHalfFollowObj != null ? o.cutHalfFollowObj.x : o.cutHalfAnchorX;
            float ay = o.cutHalfFollowObj != null ? o.cutHalfFollowObj.y : o.cutHalfAnchorY;
            float dx = o.cutHalfFreeX - ax;
            float dy = o.cutHalfFreeY - ay;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            o.cutHalfLen -= 250f * dt;
            if (o.cutHalfLen <= 0) {
                objects.remove(i);
                continue;
            }
            if (len > 0.001f) {
                dx /= len;
                dy /= len;
            }
            o.cutHalfFreeX = ax + dx * o.cutHalfLen;
            o.cutHalfFreeY = ay + dy * o.cutHalfLen;
            o.x = (ax + o.cutHalfFreeX) / 2;
            o.y = (ay + o.cutHalfFreeY) / 2;
            o.w = o.cutHalfLen;
            o.h = 20;
            o.rotation = (float) Math.atan2(dy, dx);
            o.isHorizontal = true;
        }

        for (PhysObj o : objects) {
            if (isConstraintBody(o)) {
                if (o.bindACondition != null && o.bindACondition.evaluate(dt, o, this)) {
                    o.connectedObjA = null;
                    o.connectedToCeiling = false; o.ceilingSegmentId = -1;
                    o.connectedToGround = false; o.groundAnchorX = 0;
                    o.connectedToWallLeft = false; o.connectedToWallRight = false;
                    o.bindACondition = null;
                }
                if (o.bindBCondition != null && o.bindBCondition.evaluate(dt, o, this)) {
                    o.connectedObjB = null;
                    o.bindBCondition = null;
                }
                if (o.type == TYPE_ROPE && o.wrapCondition != null && o.wrapCondition.evaluate(dt, o, this)) {
                    clearRopeWrap(o);
                    o.wrapCondition = null;
                }
            }
            if (o.type == TYPE_PULLEY && o.bindACondition != null) {
                if (o.bindACondition.evaluate(dt, o, this)) {
                    o.pulleyBoundRamp = null;
                    o.bindACondition = null;
                }
            }
            if (o.fixedCondition != null && (o.fixed || (o.type == TYPE_ROD && o.remoteLock))) {
                if (o.fixedCondition.evaluate(dt, o, this)) {
                    if (o.type == TYPE_ROD) o.remoteLock = false;
                    else o.fixed = false;
                    o.fixedCondition = null;
                }
            }
            if (o.angleFixedCondition != null && o.angleFixed) {
                if (o.angleFixedCondition.evaluate(dt, o, this)) {
                    o.angleFixed = false;
                    o.angleFixedCondition = null;
                }
            }
        }

        for (PhysObj o : objects) {
            if (o.type != TYPE_RING) continue;
            if (o.ringHostRod == null || !objects.contains(o.ringHostRod)) continue;
            PhysObj rod = o.ringHostRod;
            float[] eps = getSpringEndpoints(rod);
            float rx1 = eps[0], ry1 = eps[1], rx2 = eps[2], ry2 = eps[3];
            float rdx = rx2 - rx1, rdy = ry2 - ry1;
            float rlenSq = rdx * rdx + rdy * rdy;
            if (rlenSq < 0.0001f) continue;
            float t = ((o.x - rx1) * rdx + (o.y - ry1) * rdy) / rlenSq;
            if (t < 0f) t = 0f;
            if (t > 1f) t = 1f;
            o.ringParam = t;
            o.x = rx1 + t * rdx;
            o.y = ry1 + t * rdy;
            o.physX = o.x;
            o.physY = o.y;
        }
        
        for (PhysObj o : objects) {
            if (o.fixed || isConstraintBody(o)) continue;
            if (o.vx != (float) o.physVx) o.physVx = o.vx;
            if (o.vy != (float) o.physVy) o.physVy = o.vy;
            if (o.x != (float) o.physX) o.physX = o.x;
            if (o.y != (float) o.physY) o.physY = o.y;
        }

        if (!toRemove.isEmpty()) {
            objects.removeAll(toRemove);
            if (selectedObj != null && toRemove.contains(selectedObj)) {
                selectedObj = null;
            }
            if (connectHighlightObj != null && toRemove.contains(connectHighlightObj)) {
                connectHighlightObj = null;
            }
            toRemove.clear();
        }
    }

    void recordContactImpulse(PhysObj a, PhysObj b, float nx, float ny,
                              float normalImpulse, float tx, float ty, float frictionImpulse) {
        float normal = Math.abs(normalImpulse);
        if (!a.fixed) {
            a.supportForceX += nx * normal;
            a.supportForceY += ny * normal;
            a.frictionForceX += tx * frictionImpulse;
            a.frictionForceY += ty * frictionImpulse;
        }
        if (!b.fixed) {
            b.supportForceX -= nx * normal;
            b.supportForceY -= ny * normal;
            b.frictionForceX -= tx * frictionImpulse;
            b.frictionForceY -= ty * frictionImpulse;
        }
    }

    boolean resolveOrientedAABB(PhysObj a, PhysObj b) {
        if (!a.cutVertices.isEmpty() || !b.cutVertices.isEmpty()) return false;
        if (a.type == TYPE_BALL || b.type == TYPE_BALL || a.type == TYPE_PULLEY || b.type == TYPE_PULLEY) return false;
        if (Math.abs(a.rotation) < 0.001f && Math.abs(b.rotation) < 0.001f) return false;

        float angle;
        if (Math.abs(a.rotation) >= 0.001f && Math.abs(b.rotation) >= 0.001f) {
            if (Math.abs(a.rotation - b.rotation) > 0.35f) return false;
            angle = (a.rotation + b.rotation) / 2f;
        } else {
            angle = Math.abs(a.rotation) >= 0.001f ? a.rotation : b.rotation;
        }

        float tx = (float) Math.cos(angle);
        float ty = (float) Math.sin(angle);
        float nx0 = (float) Math.sin(angle);
        float ny0 = -(float) Math.cos(angle);
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        float relT = dx * tx + dy * ty;
        float relN = dx * nx0 + dy * ny0;
        float overlapT = (a.w + b.w) / 2f - Math.abs(relT);
        float overlapN = (a.h + b.h) / 2f - Math.abs(relN);
        if (overlapT <= 0f || overlapN <= 0f) return false;

        boolean normalAxis = overlapN <= overlapT;
        if (!normalAxis) {
            PhysObj topByNormal = relN >= 0f ? a : b;
            PhysObj surfaceByNormal = relN >= 0f ? b : a;
            if (surfaceByNormal.fixed && surfaceByNormal.type == TYPE_BOARD
                    && !topByNormal.fixed && topByNormal.type != TYPE_BALL
                    && topByNormal.type != TYPE_PULLEY && topByNormal.type != TYPE_RING
                    && Math.abs(relN) > surfaceByNormal.h * 0.45f) {
                return true;
            }
        }

        float ax;
        float ay;
        float pen;
        if (normalAxis) {
            float s = relN >= 0f ? 1f : -1f;
            ax = nx0 * s;
            ay = ny0 * s;
            pen = overlapN;
        } else {
            float s2 = relT >= 0f ? 1f : -1f;
            ax = tx * s2;
            ay = ty * s2;
            pen = overlapT;
        }

        if (a.fixed && b.fixed) return true;
        float invA = a.fixed ? 0f : 1f / a.mass;
        float invB = b.fixed ? 0f : 1f / b.mass;
        float invSum = invA + invB;
        if (invSum <= 0f) return true;

        a.x += ax * pen * (invA / invSum);
        a.y += ay * pen * (invA / invSum);
        b.x -= ax * pen * (invB / invSum);
        b.y -= ay * pen * (invB / invSum);

        float normalImpulse = 0f;
        float relV = (a.vx - b.vx) * ax + (a.vy - b.vy) * ay;
        if (relV < 0f) {
            float e = Math.min(a.restitution, b.restitution);
            if (Math.abs(relV) < pixelsPerMeter * 0.35f) e = 0f;
            float j = -(1f + e) * relV / invSum;
            normalImpulse = j;
            a.vx += j * invA * ax;
            a.vy += j * invA * ay;
            b.vx -= j * invB * ax;
            b.vy -= j * invB * ay;
        }

        float frictionImpulse = 0f;
        if (normalAxis) {
            PhysObj top = relN >= 0f ? a : b;
            PhysObj surface = relN >= 0f ? b : a;
            if (!top.fixed && top.type != TYPE_BALL && top.type != TYPE_PULLEY) {
                top.rotation = surface.rotation;
                top.contactRamp = true;
                top.rampSurfAngle = surface.rotation;
                top.rampNx = (float) Math.sin(surface.rotation);
                top.rampNy = -(float) Math.cos(surface.rotation);
            }
            float vTa = a.vx * tx + a.vy * ty;
            float vTb = b.vx * tx + b.vy * ty;
            float relTv = vTa - vTb;
            float maxFrictionImpulse = top.friction * normalImpulse;
            frictionImpulse = Math.max(-maxFrictionImpulse,
                    Math.min(maxFrictionImpulse, -relTv / invSum));
            a.vx += frictionImpulse * invA * tx;
            a.vy += frictionImpulse * invA * ty;
            b.vx -= frictionImpulse * invB * tx;
            b.vy -= frictionImpulse * invB * ty;
            recordContactImpulse(a, b, ax, ay, normalImpulse, tx, ty, frictionImpulse);
        }

        a.physX = a.x; a.physY = a.y; a.physVx = a.vx; a.physVy = a.vy;
        b.physX = b.x; b.physY = b.y; b.physVx = b.vx; b.physVy = b.vy;
        return true;
    }

    void resolveAABB(PhysObj a, PhysObj b) {
        if (resolveOrientedAABB(a, b)) return;
        float aL, aR, aT, aB;
        if (!a.cutVertices.isEmpty()) {
            float[] bounds = getBoundsFromVertices(a.cutVertices, a.x, a.y, a.rotation);
            aL = bounds[0]; aR = bounds[1]; aT = bounds[2]; aB = bounds[3];
        } else {
            aL = a.x - a.w / 2; aR = a.x + a.w / 2; aT = a.y - a.h / 2; aB = a.y + a.h / 2;
        }
        float bL, bR, bT, bB;
        if (!b.cutVertices.isEmpty()) {
            float[] bounds = getBoundsFromVertices(b.cutVertices, b.x, b.y, b.rotation);
            bL = bounds[0]; bR = bounds[1]; bT = bounds[2]; bB = bounds[3];
        } else {
            bL = b.x - b.w / 2; bR = b.x + b.w / 2; bT = b.y - b.h / 2; bB = b.y + b.h / 2;
        }

        float overlapX = Math.min(aR, bR) - Math.max(aL, bL);
        float overlapY = Math.min(aB, bB) - Math.max(aT, bT);

        if (overlapX <= 0 || overlapY <= 0) return;

        boolean axisX = overlapX < overlapY;
        float apx = a.fixed ? a.x : a.prevX;
        float apy = a.fixed ? a.y : a.prevY;
        float bpx = b.fixed ? b.x : b.prevX;
        float bpy = b.fixed ? b.y : b.prevY;
        float paL = aL + apx - a.x, paR = aR + apx - a.x;
        float paT = aT + apy - a.y, paB = aB + apy - a.y;
        float pbL = bL + bpx - b.x, pbR = bR + bpx - b.x;
        float pbT = bT + bpy - b.y, pbB = bB + bpy - b.y;
        boolean prevOverX = paR > pbL + 0.5f && paL < pbR - 0.5f;
        boolean prevOverY = paB > pbT + 0.5f && paT < pbB - 0.5f;
        if (prevOverX != prevOverY) {
            axisX = prevOverY;
        } else {
            float sepX = (a.vx - b.vx) * ((a.x < b.x) ? -1f : 1f);
            float sepY = (a.vy - b.vy) * ((a.y < b.y) ? -1f : 1f);
            if (axisX && sepX > 0f && sepY < 0f) axisX = false;
            else if (!axisX && sepY > 0f && sepX < 0f) axisX = true;
        }

        float nx, ny, pen;
        if (axisX) {
            nx = (a.x < b.x) ? -1f : 1f;
            ny = 0f;
            pen = overlapX;
        } else {
            nx = 0f;
            ny = (a.y < b.y) ? -1f : 1f;
            pen = overlapY;
        }

        if (a.fixed && b.fixed) return;

        float gravityStepV = GRAVITY * pixelsPerMeter * FIXED_DT * 1.5f;

        if (a.fixed) {
            b.x -= nx * pen;
            b.y -= ny * pen;
            float d = b.vx * nx + b.vy * ny;
            float normalImpulse = 0f;
            if (d > 0) {
                float e = (ny != 0 && d <= gravityStepV) ? 0f : b.restitution;
                normalImpulse = (1 + e) * d * b.mass;
                b.vx -= normalImpulse / b.mass * nx;
                b.vy -= normalImpulse / b.mass * ny;
            }
            if (ny != 0 && normalImpulse > 0f) {
                float tx = -ny, ty = nx;
                float relVt = b.vx * tx + b.vy * ty;
                float maxFriction = b.friction * normalImpulse;
                float frictionImpulse = Math.max(-maxFriction,
                        Math.min(maxFriction, -relVt * b.mass));
                b.vx += frictionImpulse / b.mass * tx;
                b.vy += frictionImpulse / b.mass * ty;
            }
        } else if (b.fixed) {
            a.x += nx * pen;
            a.y += ny * pen;
            float d = -(a.vx * nx + a.vy * ny);
            float normalImpulse = 0f;
            if (d > 0) {
                float e = (ny != 0 && d <= gravityStepV) ? 0f : a.restitution;
                normalImpulse = (1 + e) * d * a.mass;
                a.vx += normalImpulse / a.mass * nx;
                a.vy += normalImpulse / a.mass * ny;
            }
            if (ny != 0 && normalImpulse > 0f) {
                float tx = -ny, ty = nx;
                float relVt = a.vx * tx + a.vy * ty;
                float maxFriction = a.friction * normalImpulse;
                float frictionImpulse = Math.max(-maxFriction,
                        Math.min(maxFriction, -relVt * a.mass));
                a.vx += frictionImpulse / a.mass * tx;
                a.vy += frictionImpulse / a.mass * ty;
            }
        } else {
            float invA = 1f / a.mass;
            float invB = 1f / b.mass;
            float tangentInvA = invA;
            float tangentInvB = invB;
            if (ny != 0) {
                PhysObj lower = (a.y < b.y) ? b : a;
                if (lower.contactGround) {
                    if (lower == a) invA = 0f;
                    else invB = 0f;
                }
            }
            float invSum = invA + invB;
            if (invSum <= 0f) return;

            a.x += nx * pen * (invA / invSum);
            a.y += ny * pen * (invA / invSum);
            b.x -= nx * pen * (invB / invSum);
            b.y -= ny * pen * (invB / invSum);

            float normalImpulse = 0f;
            float relVn = (a.vx - b.vx) * nx + (a.vy - b.vy) * ny;
            if (relVn < 0) {
                float e = Math.min(a.restitution, b.restitution);
                if (ny != 0 && -relVn <= gravityStepV) e = 0f;
                float j = -(1 + e) * relVn / invSum;
                normalImpulse = j;
                a.vx += j * invA * nx;
                a.vy += j * invA * ny;
                b.vx -= j * invB * nx;
                b.vy -= j * invB * ny;
            }

            if (ny != 0) {
                PhysObj top = ny < 0f ? a : b;
                float tx = -ny;
                float ty = nx;
                float relVt = (a.vx - b.vx) * tx + (a.vy - b.vy) * ty;
                float maxFrictionImpulse = top.friction * normalImpulse;
                float tangentInvSum = tangentInvA + tangentInvB;
                float frictionImpulse = Math.max(-maxFrictionImpulse,
                        Math.min(maxFrictionImpulse, -relVt / tangentInvSum));
                a.vx += frictionImpulse * tangentInvA * tx;
                a.vy += frictionImpulse * tangentInvA * ty;
                b.vx -= frictionImpulse * tangentInvB * tx;
                b.vy -= frictionImpulse * tangentInvB * ty;
                recordContactImpulse(a, b, nx, ny, normalImpulse, tx, ty, frictionImpulse);
            }
        }
    }

    void updateWaterPhysics(float dt) {
        if (dt <= 0.000001f) return;
        float ppm = pixelsPerMeter < 0.001f ? 100f : pixelsPerMeter;
        for (int k = 0; k < objects.size(); k++) {
            PhysObj o = objects.get(k);
            if (o.type == TYPE_CONTAINER || isConstraintBody(o)) continue;
            o.pressure = 0f;
            o.waterSubArea = 0f;
        }
        float[] span = new float[2];
        for (int k = 0; k < objects.size(); k++) {
            PhysObj container = objects.get(k);
            if (container.type != TYPE_CONTAINER) continue;
            if (!container.isWaterContainer) {
                container.water = null;
                continue;
            }
            float ppm2 = (container.realWidth > 0.001f) ? container.w / container.realWidth : ppm;
            if (ppm2 < 0.001f) continue;
            WaterSim ws = WaterSim.of(container);
            if (!ws.init) continue;
            float wallT = container.containerWallThickness * ppm2;
            float iL = container.x - container.w * 0.5f + wallT;
            float iR = container.x + container.w * 0.5f - wallT;
            float iB = container.y + container.h * 0.5f - wallT;
            if (iR - iL < 2f) continue;
            boolean heldByHand = dragging && selectedObj == container;
            ws.held = heldByHand;
            float accX = 0f;
            float accY = 0f;
            if (ws.haveCPrev) {
                float nvx = (container.x - ws.prevCx) / dt;
                float nvy = (container.y - ws.prevCy) / dt;
                if (nvx != nvx) nvx = 0f;
                if (nvy != nvy) nvy = 0f;
                float kv = dt * 12f;
                if (kv > 1f) kv = 1f;
                float svx = ws.cvx + (nvx - ws.cvx) * kv;
                float svy = ws.cvy + (nvy - ws.cvy) * kv;
                if (svx != svx) svx = 0f;
                if (svy != svy) svy = 0f;
                accX = (svx - ws.cvx) / (dt * ppm2);
                accY = (svy - ws.cvy) / (dt * ppm2);
                ws.cvx = svx;
                ws.cvy = svy;
            } else {
                ws.cvx = 0f;
                ws.cvy = 0f;
                ws.haveCPrev = true;
            }
            ws.prevCx = container.x;
            ws.prevCy = container.y;
            float aLim = GRAVITY * 1.5f;
            if (accX != accX) accX = 0f;
            if (accY != accY) accY = 0f;
            if (accX > aLim) accX = aLim;
            else if (accX < -aLim) accX = -aLim;
            if (accY > aLim) accY = aLim;
            else if (accY < -aLim) accY = -aLim;
            if (heldByHand) {
                accX = 0f;
                accY = 0f;
            }
            float ka = dt * 8f;
            if (ka > 1f) ka = 1f;
            ws.accXs += (accX - ws.accXs) * ka;
            ws.accYs += (accY - ws.accYs) * ka;
            if (ws.accXs != ws.accXs) ws.accXs = 0f;
            if (ws.accYs != ws.accYs) ws.accYs = 0f;
            accX = ws.accXs;
            accY = ws.accYs;
            float gEff = GRAVITY - accY;
            if (gEff < 0.05f) gEff = 0.05f;
            ws.clearSolids();
            for (int j = 0; j < objects.size(); j++) {
                PhysObj o = objects.get(j);
                if (o == container) continue;
                if (o.fixed || isConstraintBody(o)) continue;
                if (o.type == TYPE_RING || o.type == TYPE_CONTAINER) continue;
                waterBodyPass(ws, o, iL, iR, iB, ppm, ppm2, dt, gEff, span);
            }
            float gWave = gEff;
            if (gWave < GRAVITY * 0.4f) gWave = GRAVITY * 0.4f;
            if (gWave > GRAVITY * 2.5f) gWave = GRAVITY * 2.5f;
            ws.step(dt, gWave, -accX);
            if (heldByHand) ws.relaxFlat(dt, 14f);
        }
    }

    void waterBodyPass(WaterSim ws, PhysObj o, float iL, float iR, float iB,
                       float ppm, float ppm2, float dt, float gEff, float[] span) {
        float dxPx = ws.dx * ppm2;
        if (dxPx < 0.0001f) return;
        float minX;
        float maxX;
        if (o.type == TYPE_BALL || o.type == TYPE_PULLEY) {
            float r = o.w * 0.5f;
            minX = o.x - r;
            maxX = o.x + r;
        } else {
            float hw = o.w * 0.5f;
            float hh = o.h * 0.5f;
            float cs = (float) Math.cos((double) o.rotation);
            float sn = (float) Math.sin((double) o.rotation);
            float ex = Math.abs(hw * cs) + Math.abs(hh * sn);
            minX = o.x - ex;
            maxX = o.x + ex;
        }
        float th = waterThickness(o);
        float ratio = th / WaterSim.TANK_DEPTH;
        if (ratio > 1f) ratio = 1f;
        float sxM = (0.5f * (minX + maxX) - iL) / ppm2;
        if (maxX <= iL || minX >= iR) {
            waterExitSplash(ws, o, sxM, th, dt, ppm);
            return;
        }
        int i0 = ws.colAt((minX - iL) / ppm2);
        int i1 = ws.colAt((maxX - iL) / ppm2);
        float dxM = ws.dx;
        float invPpm2Sq = 1f / (ppm2 * ppm2);
        float aSub = 0f;
        float chord = 0f;
        float widthSub = 0f;
        float hMax = 0f;
        float uSum = 0f;
        float edSum = 0f;
        float depthSum = 0f;
        int cnt = 0;
        int crossCnt = 0;
        for (int i = i0; i <= i1; i++) {
            float xc = iL + (i + 0.5f) * dxPx;
            if (!waterSolidSpan(o, xc, span)) continue;
            float surfPx = iB - ws.eta[i] * ppm2;
            float top = span[0] > surfPx ? span[0] : surfPx;
            float bot = span[1] < iB ? span[1] : iB;
            if (bot <= top) continue;
            float hpx = bot - top;
            float areaM = hpx * dxPx * invPpm2Sq;
            ws.addSolid(i, areaM * ratio, (iB - bot) / ppm2);
            aSub += areaM;
            widthSub += dxM;
            float hM = hpx / ppm2;
            if (hM > hMax) hMax = hM;
            if (span[0] < surfPx && span[1] > surfPx) {
                chord += dxM;
                crossCnt++;
            }
            uSum += 0.5f * (ws.uf[i] + ws.uf[i + 1]);
            edSum += ws.etaDot[i];
            depthSum += areaM * ((0.5f * (top + bot) - surfPx) / ppm2);
            cnt++;
        }
        if (cnt == 0 || aSub <= 0.0000001f) {
            waterExitSplash(ws, o, sxM, th, dt, ppm);
            return;
        }
        float rho = WaterSim.RHO;
        float vol = aSub * th;
        float uAvg = uSum / cnt;
        float edAvg = edSum / cnt;
        float halfW = 0.5f * (maxX - minX) / ppm2;
        if (halfW < dxM * 0.5f) halfW = dxM * 0.5f;
        float slope = (ws.surfaceAt(ws.clampX(sxM + halfW)) - ws.surfaceAt(ws.clampX(sxM - halfW)))
                / (2f * halfW);
        if (slope > 2f) slope = 2f;
        else if (slope < -2f) slope = -2f;
        boolean round = o.type == TYPE_BALL || o.type == TYPE_PULLEY;
        float cd = round ? 0.47f : 1.05f;
        float cm = round ? 0.5f : 1.0f;
        float vx0 = (float) o.physVx / ppm;
        float vy0 = (float) o.physVy / ppm;
        float uwx = uAvg + ws.cvx / ppm2;
        float uwy = -0.35f * edAvg + ws.cvy / ppm2;
        if (uwy > 1.2f) uwy = 1.2f;
        else if (uwy < -1.2f) uwy = -1.2f;
        float relX = vx0 - uwx;
        float relY = vy0 - uwy;
        float buoy = rho * gEff * vol;
        float kSp = rho * gEff * chord * th;
        float mEff = o.mass + cm * rho * vol;
        float cY = 0.5f * rho * cd * widthSub * th * (Math.abs(relY) + 0.15f);
        float cX = 0.5f * rho * cd * hMax * th * (Math.abs(relX) + 0.15f);
        float fOtherX = (float) o.forceX / ppm;
        float fOtherY = (float) o.forceY / ppm;
        float fSlope = -rho * gEff * vol * slope;
        float denY = mEff / dt + kSp * dt + cY;
        float denX = mEff / dt + cX;
        float vyNew = denY > 0.000001f
                ? (mEff * vy0 / dt + fOtherY - buoy + cY * uwy) / denY : vy0;
        float vxNew = denX > 0.000001f
                ? (mEff * vx0 / dt + fOtherX + fSlope + cX * uwx) / denX : vx0;
        if (vyNew != vyNew) vyNew = vy0;
        if (vxNew != vxNew) vxNew = vx0;
        if (vyNew > 60f) vyNew = 60f;
        else if (vyNew < -60f) vyNew = -60f;
        if (vxNew > 60f) vxNew = 60f;
        else if (vxNew < -60f) vxNew = -60f;
        float fLim = o.mass * GRAVITY * 400f + 1f;
        float newFy = (vyNew - vy0) * o.mass / dt;
        float newFx = (vxNew - vx0) * o.mass / dt;
        if (newFy > fLim) newFy = fLim;
        else if (newFy < -fLim) newFy = -fLim;
        if (newFx > fLim) newFx = fLim;
        else if (newFx < -fLim) newFx = -fLim;
        o.forceY = (double) (newFy * ppm);
        o.forceX = (double) (newFx * ppm);
        o.waterSubArea = aSub;
        float pd = depthSum / aSub;
        o.pressure = pd > 0f ? rho * gEff * pd : 0f;
        float perCol = cX * (vxNew - uwx) / cnt;
        float colScale = dt / (rho * dxM * WaterSim.TANK_DEPTH);
        float foamGen = (Math.abs(relX) * 0.12f + Math.abs(relY) * 0.16f) * dt;
        for (int i = i0; i <= i1; i++) {
            float dCol = ws.d[i];
            if (dCol < 0.002f) continue;
            float xc = iL + (i + 0.5f) * dxPx;
            if (!waterSolidSpan(o, xc, span)) continue;
            float surfPx = iB - ws.eta[i] * ppm2;
            float top = span[0] > surfPx ? span[0] : surfPx;
            float bot = span[1] < iB ? span[1] : iB;
            if (bot <= top) continue;
            float du = perCol * colScale / dCol;
            if (du != du) du = 0f;
            if (du > 0.3f) du = 0.3f;
            else if (du < -0.3f) du = -0.3f;
            ws.uf[i] += du * 0.5f;
            ws.uf[i + 1] += du * 0.5f;
            if (crossCnt > 0) ws.addFoam(i, foamGen);
        }
        float dA = (aSub - o.waterPrevSubA) / dt;
        o.waterPrevSubA = aSub;
        if (chord > 0.0001f) {
            float closeV = Math.abs(vyNew + edAvg) + 0.02f;
            float capA = chord * closeV;
            if (dA > capA) dA = capA;
            else if (dA < -capA) dA = -capA;
            float sr = Math.abs(dA) * th * 20f;
            if (sr > 0.3f) ws.splash(ws.clampX(sxM), sr, vxNew, dA > 0f);
        } else {
            float sp = (float) Math.sqrt((double) (relX * relX + relY * relY));
            if (sp > 1.2f && ws.rnd() < dt * 12f) {
                float yM = (iB - o.y) / ppm2;
                if (yM > 0.004f) ws.spawnBubble(ws.clampX(sxM), yM, 0.0022f + 0.004f * ws.rnd());
            }
        }
    }

    void waterExitSplash(WaterSim ws, PhysObj o, float sxM, float th, float dt, float ppm) {
        if (o.waterPrevSubA > 0.00002f) {
            float rate = o.waterPrevSubA / dt;
            float capA = (o.w / ppm) * (Math.abs((float) o.physVy / ppm) + 0.02f);
            if (rate > capA) rate = capA;
            float sr = rate * th * 20f;
            if (sr > 0.3f) ws.splash(ws.clampX(sxM), sr, (float) o.physVx / ppm, false);
        }
        o.waterPrevSubA = 0f;
    }

    boolean objInWater(PhysObj o) {
        if (o == null) return false;
        if (o.fixed) return false;
        if (o.type == TYPE_CONTAINER || o.type == TYPE_RING) return false;
        if (isConstraintBody(o)) return false;
        if (o.waterSubArea > 0.0000001f) return true;
        float ppm = pixelsPerMeter < 0.001f ? 100f : pixelsPerMeter;
        for (int k = 0; k < objects.size(); k++) {
            PhysObj container = objects.get(k);
            if (container == o) continue;
            if (container.type != TYPE_CONTAINER) continue;
            if (!container.isWaterContainer) continue;
            if (container.containerWaterLevel <= 0.0001f) continue;
            float ppm2 = (container.realWidth > 0.001f) ? container.w / container.realWidth : ppm;
            if (ppm2 < 0.001f) continue;
            float wallT = container.containerWallThickness * ppm2;
            float iL = container.x - container.w * 0.5f + wallT;
            float iR = container.x + container.w * 0.5f - wallT;
            float iB = container.y + container.h * 0.5f - wallT;
            if (iR - iL < 2f) continue;
            float hw;
            float hh;
            if (o.type == TYPE_BALL || o.type == TYPE_PULLEY) {
                hw = o.w * 0.5f;
                hh = o.w * 0.5f;
            } else {
                float cs = Math.abs((float) Math.cos((double) o.rotation));
                float sn = Math.abs((float) Math.sin((double) o.rotation));
                hw = o.w * 0.5f * cs + o.h * 0.5f * sn;
                hh = o.w * 0.5f * sn + o.h * 0.5f * cs;
            }
            if (hw < 0.5f) hw = 0.5f;
            if (hh < 0.5f) hh = 0.5f;
            if (o.x + hw <= iL || o.x - hw >= iR) continue;
            float surfY = iB - container.containerWaterLevel * ppm2;
            if (container.water != null && container.water.init) {
                float sxM = (o.x - iL) / ppm2;
                surfY = iB - container.water.surfaceAt(container.water.clampX(sxM)) * ppm2;
            }
            if (surfY != surfY) continue;
            if (o.y + hh <= surfY + 0.5f) continue;
            if (o.y - hh >= iB) continue;
            return true;
        }
        return false;
    }

    float waterCrossSection(PhysObj o) {
        float ppm = pixelsPerMeter < 0.001f ? 100f : pixelsPerMeter;
        if (o.type == TYPE_BALL || o.type == TYPE_PULLEY) {
            float r = o.w * 0.5f / ppm;
            return (float) Math.PI * r * r;
        }
        return (o.w / ppm) * (o.h / ppm);
    }

    float waterVolumeOf(PhysObj o) {
        float ppm = pixelsPerMeter < 0.001f ? 100f : pixelsPerMeter;
        if (o.type == TYPE_BALL || o.type == TYPE_PULLEY) {
            float r = o.w * 0.5f / ppm;
            return (4f / 3f) * (float) Math.PI * r * r * r;
        }
        float hM = o.h / ppm;
        if (hM < 0.001f) hM = 0.001f;
        float ba = o.baseArea;
        if (ba < 0.0001f) ba = (o.w / ppm) * 0.1f;
        if (ba < 0.0001f) ba = 0.0001f;
        return ba * hM;
    }

    float waterThickness(PhysObj o) {
        float cs = waterCrossSection(o);
        if (cs < 0.000001f) return 0.01f;
        float t = waterVolumeOf(o) / cs;
        if (t != t) return 0.01f;
        if (t < 0.002f) t = 0.002f;
        else if (t > 2f) t = 2f;
        return t;
    }

    boolean waterSolidSpan(PhysObj o, float xPx, float[] out) {
        if (o.type == TYPE_BALL || o.type == TYPE_PULLEY) {
            float r = o.w * 0.5f;
            if (r <= 0.0001f) return false;
            float dxx = xPx - o.x;
            float q = r * r - dxx * dxx;
            if (q <= 0f) return false;
            float hy = (float) Math.sqrt((double) q);
            out[0] = o.y - hy;
            out[1] = o.y + hy;
            return true;
        }
        float hw = o.w * 0.5f;
        float hh = o.h * 0.5f;
        if (hw <= 0.0001f || hh <= 0.0001f) return false;
        float cs = (float) Math.cos((double) o.rotation);
        float sn = (float) Math.sin((double) o.rotation);
        float ax = o.x - hw * cs + hh * sn;
        float ay = o.y - hw * sn - hh * cs;
        float bx = o.x + hw * cs + hh * sn;
        float by = o.y + hw * sn - hh * cs;
        float cx2 = o.x + hw * cs - hh * sn;
        float cy2 = o.y + hw * sn + hh * cs;
        float dx2 = o.x - hw * cs - hh * sn;
        float dy2 = o.y - hw * sn + hh * cs;
        out[0] = Float.MAX_VALUE;
        out[1] = -Float.MAX_VALUE;
        waterEdgeCut(ax, ay, bx, by, xPx, out);
        waterEdgeCut(bx, by, cx2, cy2, xPx, out);
        waterEdgeCut(cx2, cy2, dx2, dy2, xPx, out);
        waterEdgeCut(dx2, dy2, ax, ay, xPx, out);
        return out[1] > out[0];
    }

    void waterEdgeCut(float ax, float ay, float bx, float by, float xPx, float[] out) {
        boolean spans = (ax <= xPx && bx >= xPx) || (bx <= xPx && ax >= xPx);
        if (!spans) return;
        float den = bx - ax;
        if (Math.abs(den) < 0.00001f) {
            if (ay < out[0]) out[0] = ay;
            if (ay > out[1]) out[1] = ay;
            if (by < out[0]) out[0] = by;
            if (by > out[1]) out[1] = by;
            return;
        }
        float y = ay + (by - ay) * (xPx - ax) / den;
        if (y < out[0]) out[0] = y;
        if (y > out[1]) out[1] = y;
    }

    void resolveContainerWalls(PhysObj o, PhysObj container) {
        float ppm2 = (container.realWidth > 0.001f) ? container.w / container.realWidth : 100f;
        float wallT = container.containerWallThickness * ppm2;
        float cL = container.x - container.w / 2;
        float cR = container.x + container.w / 2;
        float cT = container.y - container.h / 2;
        float cB = container.y + container.h / 2;
        float iL = cL + wallT;
        float iR = cR - wallT;
        float iB = cB - wallT;

        float hw = o.type == TYPE_BALL ? o.w / 2 : o.w / 2;
        float hh = o.type == TYPE_BALL ? o.w / 2 : o.h / 2;
        float oL = o.x - hw, oR = o.x + hw, oT = o.y - hh, oB = o.y + hh;

        if (oR < cL || oL > cR || oB < cT || oT > cB) return;

        boolean insideX = o.x > iL && o.x < iR;
        boolean insideY = o.y > cT;

        float waterSurfaceY = iB - container.containerWaterLevel * ppm2;
        if (container.isWaterContainer && container.water != null && container.water.init && ppm2 > 0.001f) {
            waterSurfaceY = iB - container.water.surfaceAt((o.x - iL) / ppm2) * ppm2;
        }
        boolean inWater = container.isWaterContainer && o.y > waterSurfaceY && o.y < iB;

        if (insideX && insideY) {
            if (oL < iL) {
                o.x = iL + hw;
                if (o.vx < 0) {
                    if (inWater) o.vx = -o.vx * Math.min(0.05f, o.restitution * 0.15f);
                    else o.vx = -o.vx * o.restitution;
                }
            } else if (oR > iR) {
                o.x = iR - hw;
                if (o.vx > 0) {
                    if (inWater) o.vx = -o.vx * Math.min(0.05f, o.restitution * 0.15f);
                    else o.vx = -o.vx * o.restitution;
                }
            }
            if (oB > iB) {
                o.y = iB - hh;
                o.contactGround = true;
                if (o.vy > 0) {
                    if (inWater) {
                        o.vy = 0f;
                        o.vx *= 0.25f;
                    } else {
                        o.vy = -o.vy * o.restitution;
                    }
                }
            }
        } else {
            if (oB <= cT) return;
            if (oR > cL && oL < cL) {
                o.x = cL - hw;
                if (o.vx > 0) o.vx = -o.vx * o.restitution;
            } else if (oL < cR && oR > cR) {
                o.x = cR + hw;
                if (o.vx < 0) o.vx = -o.vx * o.restitution;
            }
            if (oT < cB && oB > cB) {
                o.y = cB + hh;
                if (o.vy < 0) o.vy = -o.vy * o.restitution;
            }
        }
    }

    float[] getBoundsFromVertices(ArrayList<float[]> verts, float cx, float cy, float rotation) {
        if (verts.isEmpty()) {
            return new float[]{cx - 100, cx + 100, cy - 100, cy + 100};
        }
        float cos = (float) Math.cos(rotation);
        float sin = (float) Math.sin(rotation);
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (float[] v : verts) {
            float wx = v[0] * cos - v[1] * sin + cx;
            float wy = v[0] * sin + v[1] * cos + cy;
            if (wx < minX) minX = wx;
            if (wx > maxX) maxX = wx;
            if (wy < minY) minY = wy;
            if (wy > maxY) maxY = wy;
        }
        return new float[]{minX, maxX, minY, maxY};
    }

    void resolveBallCollision(PhysObj a, PhysObj b) {
        if (a.type == TYPE_BALL && !a.cutVertices.isEmpty()) {
            resolveAABB(a, b);
            return;
        }
        if (b.type == TYPE_BALL && !b.cutVertices.isEmpty()) {
            resolveAABB(a, b);
            return;
        }
        if (b.type == TYPE_BALL || b.type == TYPE_PULLEY) {
            float dx = b.x - a.x, dy = b.y - a.y, dist = magnitude(dx, dy);
            float radiusA = halfWidth(a), radiusB = halfWidth(b);

            if (dist < radiusA + radiusB && dist > 0.001f) {
                float nx = dx / dist, ny = dy / dist, overlap = (radiusA + radiusB - dist) / 2;

                separateObjects(a, b, overlap, nx, ny);

                float v1n = a.vx * nx + a.vy * ny, v2n = b.vx * nx + b.vy * ny;
                if (v1n - v2n <= 0) return;

                float e = Math.min(a.restitution, b.restitution);
                float invA = a.fixed ? 0f : 1f / a.mass;
                float invB = b.fixed ? 0f : 1f / b.mass;
                float invSum = invA + invB;
                if (invSum <= 0f) return;
                float normalImpulse = (1f + e) * (v1n - v2n) / invSum;
                a.vx -= normalImpulse * invA * nx;
                a.vy -= normalImpulse * invA * ny;
                b.vx += normalImpulse * invB * nx;
                b.vy += normalImpulse * invB * ny;
                float tx = -ny;
                float ty = nx;
                float relVt = (a.vx - b.vx) * tx + (a.vy - b.vy) * ty;
                PhysObj top = a.y < b.y ? a : b;
                float maxFrictionImpulse = top.friction * normalImpulse;
                float frictionImpulse = Math.max(-maxFrictionImpulse,
                        Math.min(maxFrictionImpulse, -relVt / invSum));
                a.vx += frictionImpulse * invA * tx;
                a.vy += frictionImpulse * invA * ty;
                b.vx -= frictionImpulse * invB * tx;
                b.vy -= frictionImpulse * invB * ty;
                recordContactImpulse(a, b, -nx, -ny, normalImpulse,
                        tx, ty, frictionImpulse);
            }
        } else {
            float cos = (float) Math.cos(b.rotation), sin = (float) Math.sin(b.rotation);
            float dx = a.x - b.x, dy = a.y - b.y;
            float localX = dx * cos + dy * sin, localY = -dx * sin + dy * cos;

            float closestX = Math.max(-halfWidth(b), Math.min(localX, halfWidth(b)));
            float closestY = Math.max(-halfHeight(b), Math.min(localY, halfHeight(b)));
            float distanceX = localX - closestX, distanceY = localY - closestY;
            float distSq = distanceX * distanceX + distanceY * distanceY;
            float radius = halfWidth(a);

            if (distSq < radius * radius) {
                float dist, nx, ny, worldNx, worldNy, overlap;

                if (distSq > 0.0001f) {
                    dist = (float) Math.sqrt(distSq);
                    nx = distanceX / dist;
                    ny = distanceY / dist;
                    worldNx = nx * cos - ny * sin;
                    worldNy = nx * sin + ny * cos;
                    overlap = radius - dist;
                } else {
                    float pLeft = localX + halfWidth(b);
                    float pRight = halfWidth(b) - localX;
                    float pTop = localY + halfHeight(b);
                    float pBottom = halfHeight(b) - localY;
                    float minP = Math.min(Math.min(pLeft, pRight), Math.min(pTop, pBottom));
                    if (minP == pTop) {
                        nx = 0; ny = -1;
                        overlap = pTop + radius;
                    } else if (minP == pBottom) {
                        nx = 0; ny = 1;
                        overlap = pBottom + radius;
                    } else if (minP == pLeft) {
                        nx = -1; ny = 0;
                        overlap = pLeft + radius;
                    } else {
                        nx = 1; ny = 0;
                        overlap = pRight + radius;
                    }
                    worldNx = nx * cos - ny * sin;
                    worldNy = nx * sin + ny * cos;
                }

                if (!a.fixed) {
                    a.x += worldNx * overlap;
                    a.y += worldNy * overlap;
                }

                float e = Math.min(a.restitution, b.restitution);
                float invA = a.fixed ? 0f : 1f / a.mass;
                float invB = b.fixed ? 0f : 1f / b.mass;
                float invSum = invA + invB;
                if (invSum <= 0f) return;
                float vRel = (a.vx - b.vx) * worldNx + (a.vy - b.vy) * worldNy;

                float normalImpulse = 0f;
                if (vRel < 0) {
                    float j = -(1 + e) * vRel / invSum;
                    normalImpulse = j;
                    a.vx += j * worldNx * invA;
                    a.vy += j * worldNy * invA;
                    b.vx -= j * worldNx * invB;
                    b.vy -= j * worldNy * invB;
                }
                if (Math.abs(worldNy) > 0.5f && normalImpulse > 0f) {
                    float tx = -worldNy;
                    float ty = worldNx;
                    float relVt = (a.vx - b.vx) * tx + (a.vy - b.vy) * ty;
                    PhysObj top = worldNy < 0f ? a : b;
                    float maxFrictionImpulse = top.friction * normalImpulse;
                    float frictionImpulse = Math.max(-maxFrictionImpulse,
                            Math.min(maxFrictionImpulse, -relVt / invSum));
                    a.vx += frictionImpulse * invA * tx;
                    a.vy += frictionImpulse * invA * ty;
                    b.vx -= frictionImpulse * invB * tx;
                    b.vy -= frictionImpulse * invB * ty;
                    recordContactImpulse(a, b, worldNx, worldNy, normalImpulse,
                            tx, ty, frictionImpulse);
                }
            }
        }
    }

    boolean isFullCirclePipe(PhysObj ramp) {
        return ramp != null && ramp.type == TYPE_RAMP_ARC && ramp.pipeMode
                && ramp.w <= 1f && ramp.arcAngle >= 2f * (float) Math.PI - 0.01f;
    }

    boolean hasSustainedOuterPipeGeometry(PhysObj block, PhysObj ramp,
                                          ArcContact contact, float targetDist) {
        if (!isFullCirclePipe(ramp) || !block.prevContactRamp
                || block.prevRampContactObj != ramp) return false;
        if (block.prevRampNx * contact.radialX
                + block.prevRampNy * contact.radialY >= 0f) return false;
        float radialDist = magnitude(block.x - contact.cx, block.y - contact.cy);
        float halfSize = block.type == TYPE_BALL ? block.w / 2f : block.h / 2f;
        return Math.abs(radialDist - targetDist) <= Math.max(20f, halfSize * 1.2f);
    }

    double sustainedPipeNormalForce(PhysObj block, PhysObj ramp, ArcContact contact,
                                    float targetDist, double blockOtherX, double blockOtherY,
                                    double rampOtherX, double rampOtherY) {
        float nx = -contact.radialX, ny = -contact.radialY;
        float rampVx = ramp.fixed ? 0f : ramp.vx;
        float rampVy = ramp.fixed ? 0f : ramp.vy;
        float relVx = block.vx - rampVx, relVy = block.vy - rampVy;
        double vt = relVx * contact.tangentX + relVy * contact.tangentY;
        double invBlock = block.fixed ? 0.0 : 1.0 / block.mass;
        double invRamp = ramp.fixed ? 0.0 : 1.0 / ramp.mass;
        double invSum = invBlock + invRamp;
        if (invSum <= 0.0 || targetDist < 1f) return 0.0;
        double otherRelativeNormal = (blockOtherX * nx + blockOtherY * ny) * invBlock
                - (rampOtherX * nx + rampOtherY * ny) * invRamp;
        return (vt * vt / targetDist - otherRelativeNormal) / invSum;
    }

    PhysObj findInitialFullCirclePipeContact(PhysObj block) {
        if (constraintPostPass || block.prevContactRamp) return null;
        for (PhysObj ramp : objects) {
            if (!isFullCirclePipe(ramp)) continue;
            ArcContact contact = new ArcContact();
            if (!getArcContact(ramp, block.x, block.y, contact)) continue;
            float halfSize = block.type == TYPE_BALL ? block.w / 2f : block.h / 2f;
            float targetDist = contact.radius - halfSize;
            float radialDist = magnitude(block.x - contact.cx, block.y - contact.cy);
            if (Math.abs(radialDist - targetDist)
                    > Math.max(0.05f, pixelsPerMeter * 0.002f)) continue;
            float nx = -contact.radialX, ny = -contact.radialY;
            float rampVx = ramp.fixed ? 0f : ramp.vx;
            float rampVy = ramp.fixed ? 0f : ramp.vy;
            float relVn = (block.vx - rampVx) * nx + (block.vy - rampVy) * ny;
            if (Math.abs(relVn) > pixelsPerMeter * 0.0005f) continue;
            double normalForce = sustainedPipeNormalForce(block, ramp, contact, targetDist,
                    block.forceX, block.forceY, ramp.forceX, ramp.forceY);
            if (normalForce <= 0.0 || Double.isNaN(normalForce)
                    || Double.isInfinite(normalForce)) continue;
            block.prevContactRamp = true;
            block.prevRampContactObj = ramp;
            block.prevRampNx = nx;
            block.prevRampNy = ny;
            return ramp;
        }
        return null;
    }

    void applyFullCirclePipeConstraintForces() {
        for (PhysObj block : objects) {
            if (block.fixed || isConstraintBody(block)) continue;
            PhysObj ramp = block.prevRampContactObj;
            if (!isFullCirclePipe(ramp)) ramp = findInitialFullCirclePipeContact(block);
            if (ramp == null) continue;
            ArcContact contact = new ArcContact();
            if (!getArcContact(ramp, block.x, block.y, contact)) continue;
            float halfSize = block.type == TYPE_BALL ? block.w / 2f : block.h / 2f;
            float targetDist = contact.radius - halfSize;
            if (!hasSustainedOuterPipeGeometry(block, ramp, contact, targetDist)) continue;
            double blockOtherX = constraintPostPass
                    ? block.stepOtherForceX + block.forceX : block.forceX;
            double blockOtherY = constraintPostPass
                    ? block.stepOtherForceY + block.forceY : block.forceY;
            double rampOtherX = ramp.fixed ? 0.0 : (constraintPostPass
                    ? ramp.stepOtherForceX + ramp.forceX : ramp.forceX);
            double rampOtherY = ramp.fixed ? 0.0 : (constraintPostPass
                    ? ramp.stepOtherForceY + ramp.forceY : ramp.forceY);
            double normalForce = sustainedPipeNormalForce(block, ramp, contact, targetDist,
                    blockOtherX, blockOtherY, rampOtherX, rampOtherY);
            if (normalForce <= 0.0 || Double.isNaN(normalForce)
                    || Double.isInfinite(normalForce)) continue;
            float nx = -contact.radialX, ny = -contact.radialY;
            if (constraintPostPass) {
                projectArcContactPosition(block, ramp, contact, targetDist, contact.clamped);
                double invBlock = 1.0 / block.mass;
                double invRamp = ramp.fixed ? 0.0 : 1.0 / ramp.mass;
                double invSum = invBlock + invRamp;
                if (invSum > 0.0) {
                    float rampVx = ramp.fixed ? 0f : ramp.vx;
                    float rampVy = ramp.fixed ? 0f : ramp.vy;
                    double relVn = (block.vx - rampVx) * nx + (block.vy - rampVy) * ny;
                    double impulse = relVn / invSum;
                    block.vx -= impulse * invBlock * nx;
                    block.vy -= impulse * invBlock * ny;
                    if (!ramp.fixed) {
                        ramp.vx += impulse * invRamp * nx;
                        ramp.vy += impulse * invRamp * ny;
                        syncPhysState(ramp);
                    }
                    syncPhysState(block);
                }
            }
            double forceX = normalForce * nx, forceY = normalForce * ny;
            block.forceX += forceX;
            block.forceY += forceY;
            block.constraintForceX += forceX;
            block.constraintForceY += forceY;
            if (!ramp.fixed) {
                ramp.forceX -= forceX;
                ramp.forceY -= forceY;
                ramp.constraintForceX -= forceX;
                ramp.constraintForceY -= forceY;
            }
        }
    }

    void projectFullCirclePipeConstraintVelocities() {
        for (PhysObj block : objects) {
            if (block.fixed || !block.contactRamp || isConstraintBody(block)) continue;
            PhysObj ramp = block.rampContactObj;
            if (!isFullCirclePipe(ramp)) continue;
            ArcContact contact = new ArcContact();
            if (!getArcContact(ramp, block.x, block.y, contact)) continue;
            float halfSize = block.type == TYPE_BALL ? block.w / 2f : block.h / 2f;
            float targetDist = contact.radius - halfSize;
            if (!hasSustainedOuterPipeGeometry(block, ramp, contact, targetDist)) continue;
            double rampOtherX = ramp.fixed ? 0.0 : ramp.stepOtherForceX;
            double rampOtherY = ramp.fixed ? 0.0 : ramp.stepOtherForceY;
            double normalForce = sustainedPipeNormalForce(block, ramp, contact, targetDist,
                    block.stepOtherForceX, block.stepOtherForceY, rampOtherX, rampOtherY);
            if (normalForce <= 0.0 || Double.isNaN(normalForce)
                    || Double.isInfinite(normalForce)) continue;
            float nx = -contact.radialX, ny = -contact.radialY;
            double invBlock = 1.0 / block.mass;
            double invRamp = ramp.fixed ? 0.0 : 1.0 / ramp.mass;
            double invSum = invBlock + invRamp;
            if (invSum <= 0.0) continue;
            float rampVx = ramp.fixed ? 0f : ramp.vx;
            float rampVy = ramp.fixed ? 0f : ramp.vy;
            double relVn = (block.vx - rampVx) * nx + (block.vy - rampVy) * ny;
            double impulse = relVn / invSum;
            block.vx -= impulse * invBlock * nx;
            block.vy -= impulse * invBlock * ny;
            if (!ramp.fixed) {
                ramp.vx += impulse * invRamp * nx;
                ramp.vy += impulse * invRamp * ny;
                syncPhysState(ramp);
            }
            syncPhysState(block);
        }
    }

    float requiredCircularNormalAccel(PhysObj block, PhysObj ramp,
                                      ArcContact contact, float targetDist) {
        float inwardX = -contact.radialX;
        float inwardY = -contact.radialY;
        float rampVx = ramp != null && !ramp.fixed ? ramp.vx : 0f;
        float rampVy = ramp != null && !ramp.fixed ? ramp.vy : 0f;
        float relativeVx = block.vx - rampVx;
        float relativeVy = block.vy - rampVy;
        float vTang = relativeVx * contact.tangentX + relativeVy * contact.tangentY;
        float centripetalAccel = vTang * vTang / Math.max(1f, targetDist);
        float forceInwardAccel = 0f;
        if (block.mass > 0.001f) {
            forceInwardAccel = (block.displayForceX * inwardX
                    + block.displayForceY * inwardY) / block.mass;
        }
        return centripetalAccel - forceInwardAccel;
    }

    boolean shouldDetachCircularArc(PhysObj block, PhysObj ramp,
                                    ArcContact contact, float targetDist) {
        float rampVx = ramp.fixed ? 0f : ramp.vx;
        float rampVy = ramp.fixed ? 0f : ramp.vy;
        float radialVel = (block.vx - rampVx) * contact.radialX
                + (block.vy - rampVy) * contact.radialY;
        if (radialVel < -pixelsPerMeter * 0.03f) return true;
        return requiredCircularNormalAccel(block, ramp, contact, targetDist)
                < -pixelsPerMeter * 0.01f;
    }

    void applyArcReactionImpulse(PhysObj ramp, float impulseX, float impulseY, float dt) {
        if (ramp.fixed) return;
        ramp.vx += impulseX / ramp.mass;
        if (!ramp.contactGround) {
            ramp.vy += impulseY / ramp.mass;
        } else if (impulseY < 0f) {
            float supportedImpulse = ramp.mass * GRAVITY * pixelsPerMeter * dt;
            if (-impulseY > supportedImpulse) {
                ramp.vy += impulseY / ramp.mass;
                ramp.contactGround = false;
            }
        }
        ramp.physVx = ramp.vx;
        ramp.physVy = ramp.vy;
    }

    void projectArcContactPosition(PhysObj block, PhysObj ramp, ArcContact contact,
                                   float targetDist, boolean normalOnly) {
        float targetX = contact.cx + contact.radialX * targetDist;
        float targetY = contact.cy + contact.radialY * targetDist;
        if (normalOnly) {
            float pen = (block.x - contact.cx) * contact.radialX
                    + (block.y - contact.cy) * contact.radialY - targetDist;
            targetX = block.x - pen * contact.radialX;
            targetY = block.y - pen * contact.radialY;
        }
        float correctionX = targetX - block.x;
        float correctionY = targetY - block.y;
        if (ramp.fixed) {
            block.x = targetX;
            block.y = targetY;
            return;
        }
        float totalMass = block.mass + ramp.mass;
        float blockShare = ramp.mass / totalMass;
        float rampShare = block.mass / totalMass;
        block.x += correctionX * blockShare;
        ramp.x -= correctionX * rampShare;
        if (ramp.contactGround) {
            block.y = targetY;
        } else {
            block.y += correctionY * blockShare;
            ramp.y -= correctionY * rampShare;
        }
    }





    void applyRampContactVelocity(PhysObj block, PhysObj ramp, float nx, float ny, float tangX, float tangY, float angle, float dt) {
        boolean firstContactThisStep = block.rampContactObj != ramp;
        block.contactRamp = true;
        block.rampContactObj = ramp;
        block.rampNx = nx;
        block.rampNy = ny;
        block.rampSurfAngle = angle;

        boolean fullCircle = ramp.w <= 1f
                && ramp.arcAngle >= 2f * (float) Math.PI - 0.01f;
        boolean rampGrounded = ramp.contactGround;
        boolean rampGroundLocked = rampGrounded && (!fullCircle || ny < 0f);
        boolean elasticCirclePair = fullCircle && !ramp.pipeMode
                && block.friction < 0.0001f && ramp.friction < 0.0001f
                && block.restitution >= 0.999f && ramp.restitution >= 0.999f
                && windSpeed < 0.0001f && airResistance < 0.0001f;
        boolean sustainedTrackContact = block.prevContactRamp
                && block.prevRampContactObj == ramp
                && block.prevRampNx * nx + block.prevRampNy * ny > 0f;
        float rampVx = ramp.fixed ? 0f : ramp.vx;
        float rampVy = ramp.fixed || rampGroundLocked ? 0f : ramp.vy;
        float relVn = (block.vx - rampVx) * nx + (block.vy - rampVy) * ny;
        float normalImpulse = 0f;
        if (relVn < 0f) {
            float e = fullCircle && (ramp.fixed || rampGroundLocked)
                    ? block.restitution : Math.min(block.restitution, ramp.restitution);
            float restNormalV = Math.max(pixelsPerMeter * 0.05f,
                    GRAVITY * pixelsPerMeter * dt * 1.5f);
            boolean keepElastic = elasticCirclePair && Math.abs(relVn) > restNormalV;
            if ((sustainedTrackContact && !keepElastic)
                    || (!fullCircle && Math.abs(relVn) < pixelsPerMeter * 0.35f)) e = 0f;
            float rampNormalInv = 0f;
            if (!ramp.fixed) {
                rampNormalInv = rampGroundLocked ? nx * nx / ramp.mass : 1f / ramp.mass;
            }
            float invSum = 1f / block.mass + rampNormalInv;
            if (invSum > 0f) {
                float j = -(1f + e) * relVn / invSum;
                normalImpulse = j;
                block.vx += j * nx / block.mass;
                block.vy += j * ny / block.mass;
                applyArcReactionImpulse(ramp, -j * nx, -j * ny, dt);
            }
        }

        float contactRampVx = ramp.fixed ? 0f : ramp.vx;
        float contactRampVy = ramp.fixed || rampGroundLocked ? 0f : ramp.vy;
        float vTang = (block.vx - contactRampVx) * tangX
                + (block.vy - contactRampVy) * tangY;
        float tangDriveAccel = (baseForceX(block) * tangX + baseForceY(block) * tangY)
                / (block.mass * pixelsPerMeter);
        float pressAccel = -(baseForceX(block) * nx + baseForceY(block) * ny)
                / (block.mass * pixelsPerMeter);
        float normalForceMag = block.mass * Math.max(0f, pressAccel);
        if (fullCircle) {
            ArcContact forceContact = new ArcContact();
            if (getArcContact(ramp, block.x, block.y, forceContact)) {
                float targetDist = forceContact.radius
                        - (block.type == TYPE_BALL ? block.w / 2f : block.h / 2f);
                normalForceMag = block.mass * Math.max(0f,
                        requiredCircularNormalAccel(block, ramp, forceContact, targetDist))
                        / pixelsPerMeter;
            }
        }
        if (firstContactThisStep) {
            float impulseForceMag = normalImpulse / Math.max(0.0001f,
                    dt * pixelsPerMeter);
            float contactForceMag = Math.max(normalForceMag, impulseForceMag);
            block.supportForceX += nx * contactForceMag;
            block.supportForceY += ny * contactForceMag;
            ramp.reactionForceX -= nx * contactForceMag;
            ramp.reactionForceY -= ny * contactForceMag;
        }
        float maxFrictionAccel = block.friction * normalForceMag / block.mass;
        float frictionDv = maxFrictionAccel * pixelsPerMeter * dt;
        float appliedFrictionAccel = 0f;
        if (firstContactThisStep && frictionDv > 0f && Math.abs(vTang) > 0f) {
            float oldTang = vTang;
            if (Math.abs(vTang) <= frictionDv
                    && Math.abs(tangDriveAccel) <= maxFrictionAccel) {
                vTang = 0f;
            } else {
                vTang += vTang > 0f ? -frictionDv : frictionDv;
                if (oldTang > 0f && vTang < 0f) vTang = 0f;
                if (oldTang < 0f && vTang > 0f) vTang = 0f;
            }
            float deltaTang = vTang - oldTang;
            block.vx += deltaTang * tangX;
            block.vy += deltaTang * tangY;
            appliedFrictionAccel = deltaTang / (pixelsPerMeter * dt);
            float frictionForceX = appliedFrictionAccel * block.mass * tangX;
            float frictionForceY = appliedFrictionAccel * block.mass * tangY;
            block.frictionForceX += frictionForceX;
            block.frictionForceY += frictionForceY;
            ramp.frictionForceX -= frictionForceX;
            ramp.frictionForceY -= frictionForceY;
            float blockFrictionImpulse = block.mass * deltaTang;
            applyArcReactionImpulse(ramp, -blockFrictionImpulse * tangX,
                    -blockFrictionImpulse * tangY, dt);
        }
    }

    void resolveArcRampCollision(PhysObj block, PhysObj ramp, float dt) {
        float halfSize = block.type == TYPE_BALL ? block.w / 2f : block.h / 2f;
        float halfW = block.w / 2f;
        if (!ramp.pipeMode && ramp.h > 1f && ramp.w > 1f) {
            float wallX = ramp.isHorizontal ? ramp.x + ramp.w / 2f : ramp.x - ramp.w / 2f;
            float bottomY = ramp.y + ramp.h / 2f;
            float topY = ramp.y - ramp.h / 2f;
            float W2 = Math.max(0f, ramp.w);
            float R2 = (W2 * W2 + ramp.h * ramp.h) / (2f * ramp.h);
            float cX2 = ramp.isHorizontal ? ramp.x - W2 / 2f : ramp.x + W2 / 2f;
            float cY2 = ramp.y + ramp.h / 2f - R2;
            float dx2 = wallX - cX2;
            float crossY = topY;
            float ds2 = R2 * R2 - dx2 * dx2;
            if (ds2 >= 0f) {
                float ya = cY2 + (float) Math.sqrt(ds2);
                float yb = cY2 - (float) Math.sqrt(ds2);
                crossY = Math.max(ya, yb);
                if (crossY < topY) crossY = topY;
                if (crossY > bottomY) crossY = bottomY;
            }
            float left = block.x - halfW;
            float right = block.x + halfW;
            float top = block.y - halfSize;
            float bottom = block.y + halfSize;
            if (bottom > crossY && top < bottomY) {
                if (ramp.isHorizontal && left < wallX && right > wallX
                        && (block.prevVx < 0f || block.x >= wallX - halfW * 0.6f)
                        && (block.prevX - halfW >= wallX - 2f || block.x >= wallX)) {
                    float overlapR = wallX - left;
                    float relVnR = block.vx - (ramp.fixed ? 0f : ramp.vx);
                    if (ramp.fixed) {
                        block.x = wallX + halfW;
                    } else {
                        float totalMassR = block.mass + ramp.mass;
                        block.x += overlapR * ramp.mass / totalMassR;
                        ramp.x -= overlapR * block.mass / totalMassR;
                    }
                    if (relVnR < 0f) {
                        float eR = Math.min(block.restitution, ramp.restitution);
                        if (-relVnR < pixelsPerMeter * 0.35f) eR = 0f;
                        float invSumR = 1f / block.mass + (ramp.fixed ? 0f : 1f / ramp.mass);
                        if (invSumR > 0f) {
                            float jR = -(1f + eR) * relVnR / invSumR;
                            block.vx += jR / block.mass;
                            if (!ramp.fixed) ramp.vx -= jR / ramp.mass;
                        }
                    }
                    block.physX = block.x;
                    block.physVx = block.vx;
                    return;
                }
                if (!ramp.isHorizontal && right > wallX && left < wallX
                        && (block.prevVx > 0f || block.x <= wallX + halfW * 0.6f)
                        && (block.prevX + halfW <= wallX + 2f || block.x <= wallX)) {
                    float overlapL = right - wallX;
                    float relVnL = block.vx - (ramp.fixed ? 0f : ramp.vx);
                    if (ramp.fixed) {
                        block.x = wallX - halfW;
                    } else {
                        float totalMassL = block.mass + ramp.mass;
                        block.x -= overlapL * ramp.mass / totalMassL;
                        ramp.x += overlapL * block.mass / totalMassL;
                    }
                    if (relVnL > 0f) {
                        float eL = Math.min(block.restitution, ramp.restitution);
                        if (relVnL < pixelsPerMeter * 0.35f) eL = 0f;
                        float invSumL = 1f / block.mass + (ramp.fixed ? 0f : 1f / ramp.mass);
                        if (invSumL > 0f) {
                            float jL = (1f + eL) * relVnL / invSumL;
                            block.vx -= jL / block.mass;
                            if (!ramp.fixed) ramp.vx += jL / ramp.mass;
                        }
                    }
                    block.physX = block.x;
                    block.physVx = block.vx;
                    return;
                }
            }
        }

        ArcContact contact = new ArcContact();
        if (!getArcContact(ramp, block.x, block.y, contact)) return;

        float centerDx = block.x - contact.cx;
        float centerDy = block.y - contact.cy;
        float radialDist = magnitude(centerDx, centerDy);
        if (radialDist < 0.001f) return;
        float vTangAtEnd = block.vx * contact.tangentX + block.vy * contact.tangentY;
        float endPointDistance = magnitude(block.x - contact.sx, block.y - contact.sy);

        if (!ramp.pipeMode && contact.clamped) {
            if (contact.clampSide == 1 && vTangAtEnd < 0f) return;
            if (contact.clampSide == -1 && vTangAtEnd > 0f) return;
        }
        if (!ramp.pipeMode && ramp.w > 1f && ramp.h > ramp.w && ramp.h > 1f && contact.theta < 0f) {
            float holdDist = Math.max(1f, contact.radius - halfSize);
            float holdAccel = requiredCircularNormalAccel(block, ramp, contact, holdDist);
            float outwardV = block.vx * contact.radialX + block.vy * contact.radialY;
            if (holdAccel < -pixelsPerMeter * 0.01f && outwardV < pixelsPerMeter * 0.35f) {
                return;
            }
        }

        if (ramp.pipeMode) {
            float ppm = (ramp.realWidth > 0.001f) ? ramp.w / ramp.realWidth
                    : (ramp.realHeight > 0.001f ? ramp.h / ramp.realHeight : pixelsPerMeter);
            float pipeD = Math.max(1f, ramp.pipeDiameter * ppm);
            if (contact.clamped && endPointDistance > halfSize + pipeD + 20f) return;
            float innerR = Math.max(1f, contact.radius - pipeD);
            float outerTarget = contact.radius - halfSize;
            float innerTarget = innerR + halfSize;
            float objectPipeSize = block.type == TYPE_BALL ? block.w : block.h;
            boolean fitsPipe = objectPipeSize <= pipeD;
            if (fitsPipe && contact.clamped) {
                if (contact.clampSide == 1 && vTangAtEnd < 0f) return;
                if (contact.clampSide == -1 && vTangAtEnd > 0f) return;
            }
            float prevX = block.x - block.vx * dt;
            float prevY = block.y - block.vy * dt;
            float prevRadial = magnitude(prevX - contact.cx, prevY - contact.cy);
            float pipeCapture = Math.max(24f, halfSize * 0.9f);
            boolean hitOuter = radialDist > outerTarget
                    && (radialDist - outerTarget <= pipeCapture || prevRadial <= outerTarget + 4f);
            boolean hitInner = radialDist < innerTarget
                    && (innerTarget - radialDist <= pipeCapture || prevRadial >= innerTarget - 4f);
            double rampOtherX = ramp.fixed ? 0.0 : ramp.stepOtherForceX;
            double rampOtherY = ramp.fixed ? 0.0 : ramp.stepOtherForceY;
            double sustainedNormal = sustainedPipeNormalForce(block, ramp, contact, outerTarget,
                    block.stepOtherForceX, block.stepOtherForceY, rampOtherX, rampOtherY);
            boolean sustainedOuterContact = hasSustainedOuterPipeGeometry(
                    block, ramp, contact, outerTarget)
                    && sustainedNormal > 0.0 && !Double.isNaN(sustainedNormal)
                    && !Double.isInfinite(sustainedNormal);
            if (!fitsPipe) {
                float blockOuter = contact.radius + halfSize;
                float blockInner = innerR - halfSize;
                if (radialDist < blockOuter && radialDist > blockInner) {
                    boolean pushOutward;
                    if (blockInner <= 0f) {
                        pushOutward = true;
                    } else if (prevRadial >= blockOuter || prevRadial <= blockInner) {
                        pushOutward = prevRadial >= blockOuter;
                    } else {
                        pushOutward = radialDist >= (blockOuter + blockInner) * 0.5f;
                    }
                    float blockTarget = pushOutward ? blockOuter : blockInner;
                    projectArcContactPosition(block, ramp, contact, blockTarget, contact.clamped);
                    if (block.type != TYPE_BALL) block.rotation = contact.angle;
                    float bnx = pushOutward ? contact.radialX : -contact.radialX;
                    float bny = pushOutward ? contact.radialY : -contact.radialY;
                    applyRampContactVelocity(block, ramp, bnx, bny, contact.tangentX, contact.tangentY, contact.angle, dt);
                }
            } else if (hitOuter || sustainedOuterContact) {
                projectArcContactPosition(block, ramp, contact, outerTarget, contact.clamped);
                if (block.type != TYPE_BALL) block.rotation = contact.angle;
                applyRampContactVelocity(block, ramp, -contact.radialX, -contact.radialY, contact.tangentX, contact.tangentY, contact.angle, dt);
            } else if (hitInner) {
                projectArcContactPosition(block, ramp, contact, innerTarget, contact.clamped);
                if (block.type != TYPE_BALL) block.rotation = contact.angle;
                applyRampContactVelocity(block, ramp, contact.radialX, contact.radialY, contact.tangentX, contact.tangentY, contact.angle, dt);
            }
            block.physX = block.x;
            block.physY = block.y;
            block.physVx = block.vx;
            block.physVy = block.vy;
            return;
        }

        float targetDist = contact.radius - halfSize;
        float penetration = radialDist - targetDist;
        float captureLimit = Math.max(12f, halfSize * 0.75f);
        if (contact.clamped && endPointDistance > halfSize + 18f) return;
        boolean circularGuide = ramp.w <= 1f && ramp.arcAngle > (float) Math.PI + 0.01f;
        boolean crossedCircularGuide = false;
        if (circularGuide) {
            float rampShiftX = ramp.fixed ? 0f : ramp.x - ramp.prevX;
            float rampShiftY = ramp.fixed ? 0f : ramp.y - ramp.prevY;
            float relPrevX = block.prevX + rampShiftX;
            float relPrevY = block.prevY + rampShiftY;
            float prevRadial = magnitude(relPrevX - contact.cx, relPrevY - contact.cy);
            if (prevRadial <= targetDist + 2f && radialDist > targetDist) {
                crossedCircularGuide = true;
            }
        }
        boolean maintainsCircularContact = false;
        if (circularGuide && block.prevContactRamp && !crossedCircularGuide) {
            if (shouldDetachCircularArc(block, ramp, contact, targetDist)) return;
            maintainsCircularContact = Math.abs(penetration)
                    <= Math.max(20f, halfSize * 1.2f);
        }
        float radialVelocity = block.vx * contact.radialX + block.vy * contact.radialY;
        boolean approachingCircularSurface = penetration >= -2f
                && penetration <= captureLimit && radialVelocity > 0f;
        boolean onCircularGuide = circularGuide
                && (maintainsCircularContact || crossedCircularGuide
                || approachingCircularSurface);
        boolean resolveTrackContact = circularGuide ? onCircularGuide
                : penetration >= -2f && penetration <= captureLimit;
        if (resolveTrackContact) {
            projectArcContactPosition(block, ramp, contact, targetDist, contact.clamped);
            if (block.type != TYPE_BALL) block.rotation = contact.angle;
            applyRampContactVelocity(block, ramp, -contact.radialX, -contact.radialY, contact.tangentX, contact.tangentY, contact.angle, dt);
            block.physX = block.x;
            block.physY = block.y;
            block.physVx = block.vx;
            block.physVy = block.vy;
        }
    }

    void resolveRampCollision(PhysObj block, PhysObj ramp, float dt) {
        if (block.fixed) return;

        if (ramp.type == TYPE_RAMP_ARC) {
            resolveArcRampCollision(block, ramp, dt);
            return;
        }

        float rLeft = ramp.x - ramp.w / 2, rRight = ramp.x + ramp.w / 2;
        float rBottom = ramp.y + ramp.h / 2;
        float rTop = ramp.y - ramp.h / 2;
        float bCX = block.x;
        float sideHalfH = block.type == TYPE_BALL ? block.w / 2f : block.h / 2f;
        float sideHalfW = block.w / 2f;
        float blockLeftNow = block.x - sideHalfW;
        float blockRightNow = block.x + sideHalfW;
        float blockTopNow = block.y - sideHalfH;
        float blockBottomNow = block.y + sideHalfH;

        if (ramp.isHorizontal) {
            if (blockLeftNow < rRight && blockRightNow > rRight && blockBottomNow > rTop && blockTopNow < rBottom
                    && (block.prevVx < 0f || block.x >= rRight - sideHalfW * 0.6f)) {
                float overlap = rRight - blockLeftNow;
                float relVn = block.vx - (ramp.fixed ? 0f : ramp.vx);
                if (!ramp.fixed) {
                    float totalMass = block.mass + ramp.mass;
                    block.x += overlap * ramp.mass / totalMass;
                    ramp.x -= overlap * block.mass / totalMass;
                } else {
                    block.x = rRight + sideHalfW;
                }
                if (relVn < 0f) {
                    float e = Math.min(block.restitution, ramp.restitution);
                    if (-relVn < pixelsPerMeter * 0.35f) e = 0f;
                    float invSum = 1f / block.mass + (ramp.fixed ? 0f : 1f / ramp.mass);
                    if (invSum > 0f) {
                        float j = -(1f + e) * relVn / invSum;
                        block.vx += j / block.mass;
                        if (!ramp.fixed) ramp.vx -= j / ramp.mass;
                    }
                }
                block.physX = block.x;
                block.physVx = block.vx;
                return;
            }
        } else {
            if (blockRightNow > rLeft && blockLeftNow < rLeft && blockBottomNow > rTop && blockTopNow < rBottom
                    && (block.prevVx > 0f || block.x <= rLeft + sideHalfW * 0.6f)) {
                float overlap = blockRightNow - rLeft;
                float relVn = block.vx - (ramp.fixed ? 0f : ramp.vx);
                if (!ramp.fixed) {
                    float totalMass2 = block.mass + ramp.mass;
                    block.x -= overlap * ramp.mass / totalMass2;
                    ramp.x += overlap * block.mass / totalMass2;
                } else {
                    block.x = rLeft - sideHalfW;
                }
                if (relVn > 0f) {
                    float e2 = Math.min(block.restitution, ramp.restitution);
                    if (relVn < pixelsPerMeter * 0.35f) e2 = 0f;
                    float invSum2 = 1f / block.mass + (ramp.fixed ? 0f : 1f / ramp.mass);
                    if (invSum2 > 0f) {
                        float j2 = (1f + e2) * relVn / invSum2;
                        block.vx -= j2 / block.mass;
                        if (!ramp.fixed) ramp.vx += j2 / ramp.mass;
                    }
                }
                block.physX = block.x;
                block.physVx = block.vx;
                return;
            }
        }

        if (bCX < rLeft || bCX > rRight) {
            if (ramp.isHorizontal && bCX > rRight) {
                float halfSize = block.type == TYPE_BALL ? block.w / 2 : block.h / 2;
                float halfW = block.w / 2;
                float blockLeft = block.x - halfW;
                if (blockLeft < rRight && block.y + halfSize > rTop && block.y - halfSize < rBottom) {
                    float overlap = rRight - blockLeft;
                    float relVn = block.vx - (ramp.fixed ? 0 : ramp.vx);
                    if (!ramp.fixed) {
                        float totalMass = block.mass + ramp.mass;
                        block.x += overlap * ramp.mass / totalMass;
                        ramp.x -= overlap * block.mass / totalMass;
                    } else {
                        block.x = rRight + halfW;
                    }
                    if (relVn < 0) {
                        float e = Math.min(block.restitution, ramp.restitution);
                        float forceStepV = Math.abs(block.displayForceX) / block.mass * dt * 1.5f;
                        if (-relVn <= forceStepV) e = 0f;
                        float invSum = 1.0f / block.mass + (ramp.fixed ? 0 : 1.0f / ramp.mass);
                        float j = -(1 + e) * relVn / invSum;
                        block.vx += j / block.mass;
                        if (!ramp.fixed) ramp.vx -= j / ramp.mass;
                    }
                }
            } else if (!ramp.isHorizontal && bCX < rLeft) {
                float halfSize = block.type == TYPE_BALL ? block.w / 2 : block.h / 2;
                float halfW = block.w / 2;
                float blockRight = block.x + halfW;
                if (blockRight > rLeft && block.y + halfSize > rTop && block.y - halfSize < rBottom) {
                    float overlap = blockRight - rLeft;
                    float relVn = block.vx - (ramp.fixed ? 0 : ramp.vx);
                    if (!ramp.fixed) {
                        float totalMass = block.mass + ramp.mass;
                        block.x -= overlap * ramp.mass / totalMass;
                        ramp.x += overlap * block.mass / totalMass;
                    } else {
                        block.x = rLeft - halfW;
                    }
                    if (relVn > 0) {
                        float e = Math.min(block.restitution, ramp.restitution);
                        float forceStepV = Math.abs(block.displayForceX) / block.mass * dt * 1.5f;
                        if (relVn <= forceStepV) e = 0f;
                        float invSum = 1.0f / block.mass + (ramp.fixed ? 0 : 1.0f / ramp.mass);
                        float j = (1 + e) * relVn / invSum;
                        block.vx -= j / block.mass;
                        if (!ramp.fixed) ramp.vx += j / ramp.mass;
                    }
                }
            }
            float halfSize = block.type == TYPE_BALL ? block.w / 2 : block.h / 2;
            if (block.y + halfSize >= groundY - 8) {
                block.y = groundY - halfSize;
                block.contactGround = true;
                block.rotation = 0;
            }
            return;
        }

        float t = (bCX - rLeft) / ramp.w;
        float surfY, slope;

        if (ramp.type == TYPE_RAMP_STRAIGHT) {
            float effT = ramp.isHorizontal ? t : 1f - t;
            surfY = rBottom - effT * ramp.h;
            slope = ramp.isHorizontal ? -ramp.h / ramp.w : ramp.h / ramp.w;
        } else {
            float xloc = ramp.isHorizontal ? (bCX - ramp.x) : -(bCX - ramp.x);
            float W_r = ramp.w, H_r = ramp.h < 1f ? 1f : ramp.h;
            float Rarc = (W_r * W_r + H_r * H_r) / (2f * H_r);
            float acx = -W_r / 2f, acy = H_r / 2f - Rarc;
            float dSq = Rarc * Rarc - (xloc - acx) * (xloc - acx);
            float yloc = (dSq >= 0) ? acy + (float) Math.sqrt(dSq) : acy + ramp.h * (0.5f - t);
            surfY = ramp.y + yloc;
            float denom = yloc - acy;
            slope = Math.abs(denom) > 0.1f ? -(xloc - acx) / denom : -ramp.h / ramp.w;
            if (!ramp.isHorizontal) slope = -slope;
        }

        float halfSize = block.type == TYPE_BALL ? block.w / 2 : block.h / 2;
        float bottom = block.y + halfSize;

        if (bottom > surfY - 5 && bottom < surfY + 15) {
            float angle = (float) Math.atan(slope);

            if (block.type != TYPE_BALL) {
                block.rotation = angle;
            }

            float surfaceOffsetY = halfSize;
            if (block.type != TYPE_BALL) {
                surfaceOffsetY = halfSize / Math.max(0.15f,
                        Math.abs((float) Math.cos(angle)));
            }
            float targetY = surfY - surfaceOffsetY;
            float correctionY = targetY - block.y;
            if (ramp.fixed) {
                block.y = targetY;
            } else {
                float totalMass = block.mass + ramp.mass;
                block.y += correctionY * ramp.mass / totalMass;
                ramp.y -= correctionY * block.mass / totalMass;
            }

            float nx = (float) Math.sin(angle);
            float ny = -(float) Math.cos(angle);

            block.contactRamp = true;
            block.rampNx = nx;
            block.rampNy = ny;
            block.rampSurfAngle = angle;

            float tangX = (float) Math.cos(angle);
            float tangY = (float) Math.sin(angle);
            applyRampContactVelocity(block, ramp, nx, ny, tangX, tangY, angle, dt);
            block.physX = block.x; block.physY = block.y;
            block.physVx = block.vx; block.physVy = block.vy;
            ramp.physX = ramp.x; ramp.physY = ramp.y;
            ramp.physVx = ramp.vx; ramp.physVy = ramp.vy;
        }
    }


    float calculateCutPercentageRealtime() {
        if (cuttingObj == null) return 0;
        float cos = (float) Math.cos(cuttingObj.rotation), sin = (float) Math.sin(cuttingObj.rotation);
        float relX1 = cutStartX - cuttingObj.x, relY1 = cutStartY - cuttingObj.y;
        float lx1 = relX1 * cos + relY1 * sin, ly1 = -relX1 * sin + relY1 * cos;
        float relX2 = cutEndX - cuttingObj.x, relY2 = cutEndY - cuttingObj.y;
        float lx2 = relX2 * cos + relY2 * sin, ly2 = -relX2 * sin + relY2 * cos;

        if (cuttingObj.type == TYPE_BALL && cuttingObj.cutVertices.isEmpty()) {
            float r = halfWidth(cuttingObj);
            float ddx = lx2 - lx1, ddy = ly2 - ly1;
            float lineLen = magnitude(ddx, ddy);
            if (lineLen < 1) return 0;
            float dist = Math.abs(lx1 * ddy - ly1 * ddx) / lineLen;
            if (dist >= r) return 0;
            float theta = (float) Math.acos(Math.min(1f, dist / r));
            float segArea = r * r * (theta - (float) Math.sin(theta) * (float) Math.cos(theta));
            float totalArea = (float) (Math.PI * r * r);
            return Math.min(segArea, totalArea - segArea) / totalArea * 100f;
        }

        ArrayList<float[]> verts = new ArrayList<>();
        if (cuttingObj.cutVertices.isEmpty()) {
            float hw = cuttingObj.w / 2, hh = cuttingObj.h / 2;
            verts.add(new float[]{-hw, -hh});
            verts.add(new float[]{hw, -hh});
            verts.add(new float[]{hw, hh});
            verts.add(new float[]{-hw, hh});
        } else {
            verts.addAll(cuttingObj.cutVertices);
        }

        float cutNX = ly2 - ly1, cutNY = -(lx2 - lx1);
        float nLen = magnitude(cutNX, cutNY);
        if (nLen < 0.001f) return 0;
        cutNX /= nLen; cutNY /= nLen;

        ArrayList<float[]> clipped = new ArrayList<>();
        int n = verts.size();
        for (int i = 0; i < n; i++) {
            float[] v1 = verts.get(i), v2 = verts.get((i + 1) % n);
            float d1 = (v1[0] - lx1) * cutNX + (v1[1] - ly1) * cutNY;
            float d2 = (v2[0] - lx1) * cutNX + (v2[1] - ly1) * cutNY;
            if (d1 >= 0) clipped.add(v1);
            if ((d1 >= 0 && d2 < 0) || (d1 < 0 && d2 >= 0)) {
                float t = d1 / (d1 - d2);
                clipped.add(new float[]{v1[0] + t * (v2[0] - v1[0]), v1[1] + t * (v2[1] - v1[1])});
            }
        }

        float origArea = 0;
        for (int i = 0; i < n; i++) {
            float[] v1 = verts.get(i), v2 = verts.get((i + 1) % n);
            origArea += v1[0] * v2[1] - v2[0] * v1[1];
        }
        origArea = Math.abs(origArea) / 2;
        if (origArea < 0.001f) return 0;

        float clipArea = 0;
        int m = clipped.size();
        for (int i = 0; i < m; i++) {
            float[] v1 = clipped.get(i), v2 = clipped.get((i + 1) % m);
            clipArea += v1[0] * v2[1] - v2[0] * v1[1];
        }
        clipArea = Math.abs(clipArea) / 2;

        float cutFrac = 1f - clipArea / origArea;
        return Math.min(cutFrac, 1f - cutFrac) * 100f;
    }

    PhysObj findCutTarget(float x1, float y1, float x2, float y2) {
        for (int i = objects.size() - 1; i >= 0; i--) {
            PhysObj o = objects.get(i);
            if (o.type == TYPE_SPRING || o.type == TYPE_ROPE) continue;
            if (o.type == TYPE_RING) continue;
            if (o.type == TYPE_RAMP_STRAIGHT || o.type == TYPE_RAMP_ARC) continue;
            if (o.type == TYPE_CONTAINER) continue;
            if (o.type == TYPE_PULLEY) continue;
            if (lineIntersectsObj(x1, y1, x2, y2, o)) return o;
        }
        return null;
    }

    boolean lineIntersectsObj(float x1, float y1, float x2, float y2, PhysObj o) {
        if (o.type == TYPE_ROD) {
            float[] ep = getSpringEndpoints(o);
            if (segmentsIntersect(x1, y1, x2, y2, ep[0], ep[1], ep[2], ep[3])) return true;
            float dx = ep[2] - ep[0], dy = ep[3] - ep[1];
            float lenSq = dx * dx + dy * dy;
            if (lenSq < 0.001f) return false;
            float t = ((x1 - ep[0]) * dx + (y1 - ep[1]) * dy) / lenSq;
            t = Math.max(0f, Math.min(1f, t));
            float px = ep[0] + dx * t;
            float py = ep[1] + dy * t;
            return magnitude(x1 - px, y1 - py) <= 8f;
        }
        float cos = (float) Math.cos(o.rotation), sin = (float) Math.sin(o.rotation);
        float rx1 = x1 - o.x, ry1 = y1 - o.y;
        float rx2 = x2 - o.x, ry2 = y2 - o.y;
        float lx1 = rx1 * cos + ry1 * sin, ly1 = -rx1 * sin + ry1 * cos;
        float lx2 = rx2 * cos + ry2 * sin, ly2 = -rx2 * sin + ry2 * cos;
        if (o.type == TYPE_BALL && o.cutVertices.isEmpty()) {
            float r = o.w / 2;
            float ddx = lx2 - lx1, ddy = ly2 - ly1;
            float a = ddx * ddx + ddy * ddy;
            if (a < 0.0001f) return lx1 * lx1 + ly1 * ly1 <= r * r;
            float b = 2 * (lx1 * ddx + ly1 * ddy);
            float c = lx1 * lx1 + ly1 * ly1 - r * r;
            float disc = b * b - 4 * a * c;
            if (disc < 0) return false;
            float sqrtD = (float) Math.sqrt(disc);
            float t1 = (-b - sqrtD) / (2 * a), t2 = (-b + sqrtD) / (2 * a);
            return t2 >= 0 && t1 <= 1;
        } else {
            ArrayList<float[]> verts;
            if (o.cutVertices.isEmpty()) {
                float hw = o.w / 2, hh = o.h / 2;
                verts = new ArrayList<>();
                verts.add(new float[]{-hw, -hh});
                verts.add(new float[]{hw, -hh});
                verts.add(new float[]{hw, hh});
                verts.add(new float[]{-hw, hh});
            } else {
                verts = o.cutVertices;
            }
            int n = verts.size();
            for (int i = 0; i < n; i++) {
                float[] v1 = verts.get(i), v2 = verts.get((i + 1) % n);
                if (segmentsIntersect(lx1, ly1, lx2, ly2, v1[0], v1[1], v2[0], v2[1])) return true;
            }
            return pointInPoly(lx1, ly1, verts);
        }
    }

    boolean pointInPoly(float px, float py, ArrayList<float[]> verts) {
        boolean inside = false;
        int n = verts.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            float xi = verts.get(i)[0], yi = verts.get(i)[1];
            float xj = verts.get(j)[0], yj = verts.get(j)[1];
            if (((yi > py) != (yj > py)) && px < (xj - xi) * (py - yi) / (yj - yi) + xi)
                inside = !inside;
        }
        return inside;
    }

    boolean segmentsIntersect(float p1x, float p1y, float p2x, float p2y,
                               float p3x, float p3y, float p4x, float p4y) {
        float d1x = p2x - p1x, d1y = p2y - p1y;
        float d2x = p4x - p3x, d2y = p4y - p3y;
        float cross = d1x * d2y - d1y * d2x;
        if (Math.abs(cross) < 0.001f) return false;
        float t = ((p3x - p1x) * d2y - (p3y - p1y) * d2x) / cross;
        float u = ((p3x - p1x) * d1y - (p3y - p1y) * d1x) / cross;
        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }

    void performCut() {
        for (int i = objects.size() - 1; i >= 0; i--) {
            PhysObj o = objects.get(i);
            if (o.type != TYPE_ROPE && o.type != TYPE_SPRING && o.type != TYPE_ROD) continue;
            if (o.isCutHalf) continue;
            float[] ep = getSpringEndpoints(o);
            if (ep == null) continue;
            if (segmentsIntersect(cutStartX, cutStartY, cutEndX, cutEndY,
                    ep[0], ep[1], ep[2], ep[3])) {
                if (o.type == TYPE_ROD) {
                    if (o.rodRing != null) {
                        objects.remove(o.rodRing);
                        o.rodRing = null;
                    }
                    objects.remove(i);
                } else if (o.type == TYPE_ROPE) {
                    o.connectedObjA = null;
                    o.connectedObjB = null;
                    o.connectedToCeiling = false;
                    o.connectedToGround = false;
                    o.connectedToWallLeft = false;
                    o.connectedToWallRight = false;
                    objects.remove(i);
                } else {
                    float ex1 = ep[0], ey1 = ep[1], ex2 = ep[2], ey2 = ep[3];
                    float sd1x = ex2 - ex1, sd1y = ey2 - ey1;
                    float sd2x = cutEndX - cutStartX, sd2y = cutEndY - cutStartY;
                    float scross = sd1x * sd2y - sd1y * sd2x;
                    float cutX, cutY;
                    if (Math.abs(scross) > 0.001f) {
                        float tc = ((cutStartX - ex1) * sd2y - (cutStartY - ey1) * sd2x) / scross;
                        tc = Math.max(0.1f, Math.min(0.9f, tc));
                        cutX = ex1 + tc * sd1x;
                        cutY = ey1 + tc * sd1y;
                    } else {
                        cutX = (ex1 + ex2) / 2;
                        cutY = (ey1 + ey2) / 2;
                    }
                    float lenA = (float) Math.sqrt((cutX - ex1) * (cutX - ex1) + (cutY - ey1) * (cutY - ey1));
                    float lenB = (float) Math.sqrt((cutX - ex2) * (cutX - ex2) + (cutY - ey2) * (cutY - ey2));

                    boolean hasCeil = o.connectedToCeiling && o.ceilingSegmentId >= 0;
                    boolean hasGnd = o.connectedToGround;
                    boolean hasWallL = o.connectedToWallLeft;
                    boolean hasWallR = o.connectedToWallRight;
                    PhysObj objAtX1 = (o.connectedObjA != null && o.connectedObjB != null) ? o.connectedObjA : null;
                    PhysObj objAtX2 = null;
                    if (o.connectedObjA != null && o.connectedObjB != null) {
                        objAtX2 = o.connectedObjB;
                    } else if (o.connectedObjB != null && (hasCeil || hasGnd || hasWallL || hasWallR)) {
                        objAtX2 = o.connectedObjB;
                    } else if (o.connectedObjA != null) {
                        objAtX2 = o.connectedObjA;
                    } else if (o.connectedObjB != null) {
                        objAtX2 = o.connectedObjB;
                    }

                    float sLen2 = (float) Math.sqrt((ex2 - ex1) * (ex2 - ex1) + (ey2 - ey1) * (ey2 - ey1));
                    float sTension = sLen2 > 0.001f ? o.springK * (sLen2 - o.naturalLen) : 0f;

                    if (lenA > 5) {
                        PhysObj half = new PhysObj();
                        half.type = TYPE_SPRING;
                        half.isCutHalf = true;
                        half.cutHalfAnchorX = ex1;
                        half.cutHalfAnchorY = ey1;
                        half.cutHalfFreeX = cutX;
                        half.cutHalfFreeY = cutY;
                        half.cutHalfLen = lenA;
                        half.cutHalfFollowObj = (objAtX1 != null && !objAtX1.fixed) ? objAtX1 : null;
                        half.w = lenA;
                        half.h = 20;
                        half.x = (ex1 + cutX) / 2;
                        half.y = (ey1 + cutY) / 2;
                        half.rotation = (float) Math.atan2(cutY - ey1, cutX - ex1);
                        half.isHorizontal = true;
                        half.deformation = sTension;
                        objects.add(half);
                    }
                    if (lenB > 5) {
                        PhysObj half = new PhysObj();
                        half.type = TYPE_SPRING;
                        half.isCutHalf = true;
                        half.cutHalfAnchorX = ex2;
                        half.cutHalfAnchorY = ey2;
                        half.cutHalfFreeX = cutX;
                        half.cutHalfFreeY = cutY;
                        half.cutHalfLen = lenB;
                        half.cutHalfFollowObj = (objAtX2 != null && !objAtX2.fixed) ? objAtX2 : null;
                        half.w = lenB;
                        half.h = 20;
                        half.x = (ex2 + cutX) / 2;
                        half.y = (ey2 + cutY) / 2;
                        half.rotation = (float) Math.atan2(cutY - ey2, cutX - ex2);
                        half.isHorizontal = true;
                        half.deformation = sTension;
                        objects.add(half);
                    }
                    o.connectedObjA = null;
                    o.connectedObjB = null;
                    o.connectedToCeiling = false;
                    o.connectedToGround = false;
                    o.connectedToWallLeft = false;
                    o.connectedToWallRight = false;
                    objects.remove(i);
                }
            }
        }
        if (cuttingObj == null) return;
        if (cuttingObj.type == TYPE_ROD) return;
        if (cuttingObj.type == TYPE_RAMP_STRAIGHT || cuttingObj.type == TYPE_RAMP_ARC) return;

        float cos = (float) Math.cos(cuttingObj.rotation), sin = (float) Math.sin(cuttingObj.rotation);
        float relX1 = cutStartX - cuttingObj.x, relY1 = cutStartY - cuttingObj.y;
        float localX1 = relX1 * cos + relY1 * sin, localY1 = -relX1 * sin + relY1 * cos;
        float relX2 = cutEndX - cuttingObj.x, relY2 = cutEndY - cuttingObj.y;
        float localX2 = relX2 * cos + relY2 * sin, localY2 = -relX2 * sin + relY2 * cos;

        if (cuttingObj.type == TYPE_BALL && cuttingObj.cutVertices.isEmpty()) {
            float r = halfWidth(cuttingObj);
            float ddx = localX2 - localX1, ddy = localY2 - localY1;
            float lineLen = magnitude(ddx, ddy);
            if (lineLen < 1) { physView.invalidate(); return; }
            float dist = Math.abs(localX1 * ddy - localY1 * ddx) / lineLen;
            if (dist >= r) { physView.invalidate(); return; }
            float theta = (float) Math.acos(Math.min(1f, dist / r));
            float segArea = r * r * (theta - (float) Math.sin(theta) * (float) Math.cos(theta));
            float totalArea = (float) (Math.PI * r * r);
            float cutFrac = Math.min(segArea, totalArea - segArea) / totalArea;
            if (cutFrac < 0.05f) { physView.invalidate(); return; }

            float cutNX = ddy / lineLen, cutNY = -ddx / lineLen;
            float footT = -(localX1 * ddx / lineLen + localY1 * ddy / lineLen);
            float footX = localX1 + footT * (ddx / lineLen), footY = localY1 + footT * (ddy / lineLen);
            float halfChord = (float) Math.sqrt(Math.max(0f, r * r - dist * dist));
            float perpX = -cutNY, perpY = cutNX;
            float pt1x = footX + perpX * halfChord, pt1y = footY + perpY * halfChord;
            float pt2x = footX - perpX * halfChord, pt2y = footY - perpY * halfChord;
            float a1 = (float) Math.atan2(pt1y, pt1x);
            float a2 = (float) Math.atan2(pt2y, pt2x);

            float midAng = (a1 + a2) / 2;
            float midX = r * (float) Math.cos(midAng), midY = r * (float) Math.sin(midAng);
            float dMid = (midX - localX1) * cutNX + (midY - localY1) * cutNY;
            float arcStart, arcEnd;
            float startPtX, startPtY, endPtX, endPtY;
            if (dMid >= 0) {
                arcStart = a1; arcEnd = a2;
                startPtX = pt1x; startPtY = pt1y; endPtX = pt2x; endPtY = pt2y;
            } else {
                arcStart = a2; arcEnd = a1;
                startPtX = pt2x; startPtY = pt2y; endPtX = pt1x; endPtY = pt1y;
            }
            if (arcEnd < arcStart) arcEnd += (float) (2 * Math.PI);
            float arcLen = arcEnd - arcStart;

            ArrayList<float[]> ballPoly = new ArrayList<>();
            ballPoly.add(new float[]{startPtX, startPtY});
            int arcPts = 40;
            for (int i = 1; i < arcPts; i++) {
                float ang = arcStart + arcLen * i / arcPts;
                ballPoly.add(new float[]{r * (float) Math.cos(ang), r * (float) Math.sin(ang)});
            }
            ballPoly.add(new float[]{endPtX, endPtY});

            cuttingObj.mass *= (1f - cutFrac);
            cuttingObj.cutVertices = ballPoly;
            float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
            float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
            for (float[] v : ballPoly) {
                if (v[0] < minX) minX = v[0]; if (v[0] > maxX) maxX = v[0];
                if (v[1] < minY) minY = v[1]; if (v[1] > maxY) maxY = v[1];
            }
            cuttingObj.w = maxX - minX;
            cuttingObj.h = maxY - minY;
            cuttingObj.realRadius = Math.max(cuttingObj.w, cuttingObj.h) / 2f / pixelsPerMeter;
            physView.invalidate();
            return;
        }
        if (cuttingObj.type == TYPE_BALL && !cuttingObj.cutVertices.isEmpty()) {
        } else if (cuttingObj.type != TYPE_BALL) {
        }

        float cutPercent = calculateCutPercentageRealtime();
        if (cutPercent < 5 || cutPercent > 95) { physView.invalidate(); return; }

        ArrayList<float[]> newVertices = new ArrayList<>();
        if (cuttingObj.cutVertices.isEmpty()) {
            float hw = cuttingObj.w / 2, hh = cuttingObj.h / 2;
            float[][] rect = {{-hw, -hh}, {hw, -hh}, {hw, hh}, {-hw, hh}};
            for (float[] v : rect) newVertices.add(v);
        } else {
            for (float[] v : cuttingObj.cutVertices) newVertices.add(v);
        }

        float cutNormalX = localY2 - localY1;
        float cutNormalY = -(localX2 - localX1);
        float cutNormalLen = magnitude(cutNormalX, cutNormalY);
        if (cutNormalLen > 0.001f) { cutNormalX /= cutNormalLen; cutNormalY /= cutNormalLen; }

        ArrayList<float[]> clippedVertices = new ArrayList<>();
        for (int i = 0; i < newVertices.size(); i++) {
            float[] v1 = newVertices.get(i);
            float[] v2 = newVertices.get((i + 1) % newVertices.size());
            float d1 = (v1[0] - localX1) * cutNormalX + (v1[1] - localY1) * cutNormalY;
            float d2 = (v2[0] - localX1) * cutNormalX + (v2[1] - localY1) * cutNormalY;
            if (d1 >= 0) clippedVertices.add(v1);
            if ((d1 >= 0 && d2 < 0) || (d1 < 0 && d2 >= 0)) {
                float tt = d1 / (d1 - d2);
                float ix = v1[0] + tt * (v2[0] - v1[0]);
                float iy = v1[1] + tt * (v2[1] - v1[1]);
                clippedVertices.add(new float[]{ix, iy});
            }
        }

        if (clippedVertices.size() >= 3) {
            float area = 0;
            for (int i = 0; i < clippedVertices.size(); i++) {
                float[] v1 = clippedVertices.get(i);
                float[] v2 = clippedVertices.get((i + 1) % clippedVertices.size());
                area += v1[0] * v2[1] - v2[0] * v1[1];
            }
            area = Math.abs(area) / 2;

            float originalArea;
            if (cuttingObj.type == TYPE_BALL) {
                originalArea = cuttingObj.w * cuttingObj.h;
            } else {
                originalArea = cuttingObj.realWidth * cuttingObj.realHeight * pixelsPerMeter * pixelsPerMeter;
            }
            if (originalArea > 0.001f) cuttingObj.mass = cuttingObj.mass * area / originalArea;

            float signedArea = 0, cxL = 0, cyL = 0;
            int sz = clippedVertices.size();
            for (int i = 0; i < sz; i++) {
                float[] v1 = clippedVertices.get(i), v2 = clippedVertices.get((i + 1) % sz);
                float cross = v1[0] * v2[1] - v2[0] * v1[1];
                signedArea += cross;
                cxL += (v1[0] + v2[0]) * cross;
                cyL += (v1[1] + v2[1]) * cross;
            }
            signedArea /= 2;
            if (Math.abs(signedArea) > 0.001f) {
                cxL /= (6 * signedArea);
                cyL /= (6 * signedArea);
            }

            ArrayList<float[]> centeredVerts = new ArrayList<>();
            for (float[] v : clippedVertices)
                centeredVerts.add(new float[]{v[0] - cxL, v[1] - cyL});
            cuttingObj.cutVertices = centeredVerts;

            cuttingObj.x += cxL * cos - cyL * sin;
            cuttingObj.y += cxL * sin + cyL * cos;

            float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
            float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
            for (float[] v : centeredVerts) {
                if (v[0] < minX) minX = v[0];
                if (v[0] > maxX) maxX = v[0];
                if (v[1] < minY) minY = v[1];
                if (v[1] > maxY) maxY = v[1];
            }
            cuttingObj.w = maxX - minX;
            cuttingObj.h = maxY - minY;
            if (cuttingObj.type == TYPE_BALL) {
                cuttingObj.realRadius = Math.max(cuttingObj.w, cuttingObj.h) / 2f / pixelsPerMeter;
            }
        }

        physView.invalidate();
    }

    boolean isInContact(PhysObj a, PhysObj b) {
        if (a == null || b == null) return false;
        float threshold = 6f;
        float aL, aR, aT, aBot, bL, bR, bT, bBot;
        if (a.type == TYPE_BALL || a.type == TYPE_PULLEY) {
            float r = a.w / 2 + threshold;
            aL = a.x - r; aR = a.x + r; aT = a.y - r; aBot = a.y + r;
        } else {
            aL = a.x - a.w / 2 - threshold; aR = a.x + a.w / 2 + threshold;
            aT = a.y - a.h / 2 - threshold; aBot = a.y + a.h / 2 + threshold;
        }
        if (b.type == TYPE_BALL || b.type == TYPE_PULLEY) {
            float r = b.w / 2 + threshold;
            bL = b.x - r; bR = b.x + r; bT = b.y - r; bBot = b.y + r;
        } else {
            bL = b.x - b.w / 2 - threshold; bR = b.x + b.w / 2 + threshold;
            bT = b.y - b.h / 2 - threshold; bBot = b.y + b.h / 2 + threshold;
        }
        return aL <= bR && aR >= bL && aT <= bBot && aBot >= bT;
    }

    void addLogicConditionLabel(LinearLayout layout, String prefix, LogicCondition cond, boolean isActive) {
        TextView tv = new TextView(this);
        tv.setTextSize(9);
        tv.setPadding(4, 1, 4, 1);
        tv.setTextColor(isActive ? 0xFF7B1FA2 : 0xFFE65100);
        tv.setText(prefix + cond.describe());
        layout.addView(tv);
    }

    String fixedConditionLabel(PhysObj obj) {
        if (obj.type == TYPE_ROD) return "远程锁定-解除: ";
        if (obj.type == TYPE_SPRING || obj.type == TYPE_ROPE) return "限制-解除: ";
        return "固定-解除: ";
    }

    void collectObjActiveConditions(PhysObj obj, LinearLayout layout) {
        if (obj.forceCondition != null) addLogicConditionLabel(layout, "[主动]力-生效: ", obj.forceCondition, true);
        if (obj.velocityCondition != null) addLogicConditionLabel(layout, "[主动]速度-生效: ", obj.velocityCondition, true);
        if (obj.angleFixedCondition != null) addLogicConditionLabel(layout, "[主动]固定-解除: ", obj.angleFixedCondition, true);
        if (obj.fixedCondition != null) addLogicConditionLabel(layout, "[主动]" + fixedConditionLabel(obj), obj.fixedCondition, true);
        if (obj.bindACondition != null) addLogicConditionLabel(layout, "[主动]绑定A-解除: ", obj.bindACondition, true);
        if (obj.bindBCondition != null) addLogicConditionLabel(layout, "[主动]绑定B-解除: ", obj.bindBCondition, true);
        if (obj.wrapCondition != null) addLogicConditionLabel(layout, "[主动]缠绕-解除: ", obj.wrapCondition, true);
    }

    boolean conditionRefsObj(LogicCondition cond, PhysObj obj) {
        if (cond.targetObj == obj) return true;
        for (LogicCondition s : cond.subConditions) {
            if (conditionRefsObj(s, obj)) return true;
        }
        return false;
    }

    void collectObjPassiveConditions(PhysObj obj, LinearLayout layout) {
        for (PhysObj other : objects) {
            if (other == obj) continue;
            String tname = getTypeName(other.type);
            if (other.forceCondition != null && conditionRefsObj(other.forceCondition, obj))
                addLogicConditionLabel(layout, "[被动]" + tname + "力-生效: ", other.forceCondition, false);
            if (other.velocityCondition != null && conditionRefsObj(other.velocityCondition, obj))
                addLogicConditionLabel(layout, "[被动]" + tname + "速度-生效: ", other.velocityCondition, false);
            if (other.angleFixedCondition != null && conditionRefsObj(other.angleFixedCondition, obj))
                addLogicConditionLabel(layout, "[被动]" + tname + "固定-解除: ", other.angleFixedCondition, false);
            if (other.fixedCondition != null && conditionRefsObj(other.fixedCondition, obj))
                addLogicConditionLabel(layout, "[被动]" + tname + fixedConditionLabel(other), other.fixedCondition, false);
            if (other.bindACondition != null && conditionRefsObj(other.bindACondition, obj))
                addLogicConditionLabel(layout, "[被动]" + tname + "绑定A-解除: ", other.bindACondition, false);
            if (other.bindBCondition != null && conditionRefsObj(other.bindBCondition, obj))
                addLogicConditionLabel(layout, "[被动]" + tname + "绑定B-解除: ", other.bindBCondition, false);
            if (other.wrapCondition != null && conditionRefsObj(other.wrapCondition, obj))
                addLogicConditionLabel(layout, "[被动]" + tname + "缠绕-解除: ", other.wrapCondition, false);
        }
    }

    void showConditionDialog(final LogicConditionCallback callback) {
        final String[] typeNames = {
            "1. 经过...时间",
            "2. 接触...物体",
            "3. 至...时间",
            "4. ...物体不存在",
            "5. 接触...物体...时间（连续）",
            "6. 接触...物体总计...时间",
            "7. 第...次接触...物体...时间后",
            "复合逻辑"
        };
        new AlertDialog.Builder(this)
            .setTitle("选择触发逻辑")
            .setItems(typeNames, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int which) {
                    if (which == 7) {
                        buildConditionOfType(LogicCondition.TYPE_COMPOUND, callback);
                    } else {
                        buildConditionOfType(which + 1, callback);
                    }
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

void buildConditionOfType(final int condType, final LogicConditionCallback callback) {
    final LogicCondition cond = new LogicCondition();
    cond.type = condType;
    if (condType == LogicCondition.TYPE_AFTER_TIME) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(40, 20, 40, 10);
        TextView tv = new TextView(this);
        tv.setText("时间 (秒):");
        tv.setTextColor(Color.BLACK);
        final EditText et = new EditText(this);
        et.setText("1");
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        setupMathKeyboard(et);
        ll.addView(tv);
        ll.addView(et);
        new AlertDialog.Builder(this).setTitle("经过...时间").setView(ll)
            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int w) {
                    cond.timeParam = parseExpr(et.getText().toString(), 1f);
                    callback.onCondition(cond);
                }
            }).setNegativeButton("取消", null).show();
    } else if (condType == LogicCondition.TYPE_CONTACT_OBJ) {
        selectObjForCondition(new ConditionObjCallback() {
            @Override
            public void onSelected(PhysObj obj) {
                if (obj != null) {
                    cond.targetObj = obj;
                    callback.onCondition(cond);
                }
            }
        });
    } else if (condType == LogicCondition.TYPE_TIMER_REACH) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(40, 20, 40, 10);
        TextView tv = new TextView(this);
        tv.setText("时间 (秒):");
        tv.setTextColor(Color.BLACK);
        final EditText et = new EditText(this);
        et.setText("1");
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        setupMathKeyboard(et);
        ll.addView(tv);
        ll.addView(et);
        new AlertDialog.Builder(this).setTitle("至...时间").setView(ll)
            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int w) {
                    cond.timeParam = parseExpr(et.getText().toString(), 1f);
                    callback.onCondition(cond);
                }
            }).setNegativeButton("取消", null).show();
    } else if (condType == LogicCondition.TYPE_OBJ_NOT_EXIST) {
        selectObjForCondition(new ConditionObjCallback() {
            @Override
            public void onSelected(PhysObj obj) {
                if (obj != null) {
                    cond.targetObj = obj;
                    callback.onCondition(cond);
                }
            }
        });
    } else if (condType == LogicCondition.TYPE_CONTACT_OBJ_DURATION
            || condType == LogicCondition.TYPE_CONTACT_OBJ_TOTAL) {
        selectObjForCondition(new ConditionObjCallback() {
            @Override
            public void onSelected(final PhysObj obj) {
                if (obj == null) return;
                cond.targetObj = obj;
                LinearLayout ll = new LinearLayout(MainActivity.this);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.setPadding(40, 20, 40, 10);
                TextView tv = new TextView(MainActivity.this);
                tv.setText("时间 (秒):");
                tv.setTextColor(Color.BLACK);
                final EditText et = new EditText(MainActivity.this);
                et.setText("1");
                et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                setupMathKeyboard(et);
                ll.addView(tv);
                ll.addView(et);
                String title = condType == LogicCondition.TYPE_CONTACT_OBJ_DURATION
                        ? "连续接触...时间" : "接触总计...时间";
                new AlertDialog.Builder(MainActivity.this).setTitle(title).setView(ll)
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int w) {
                            cond.timeParam = parseExpr(et.getText().toString(), 1f);
                            callback.onCondition(cond);
                        }
                    }).setNegativeButton("取消", null).show();
            }
        });
    } else if (condType == LogicCondition.TYPE_CONTACT_OBJ_AFTER) {
        LinearLayout ll1 = new LinearLayout(this);
        ll1.setOrientation(LinearLayout.VERTICAL);
        ll1.setPadding(40, 20, 40, 10);
        TextView tv1 = new TextView(this);
        tv1.setText("接触次数 (填-1为最后一次):");
        tv1.setTextColor(Color.BLACK);
        final EditText etN = new EditText(this);
        etN.setText("1");
        etN.setHint("填-1为最后一次");
        etN.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        ll1.addView(tv1);
        ll1.addView(etN);
        new AlertDialog.Builder(this).setTitle("第...次接触...物体...时间后").setView(ll1)
            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int w) {
                    int nth = 1;
                    try { nth = Integer.parseInt(etN.getText().toString().trim()); } catch (Exception e) {}
                    final int finalNth = nth;
                    selectObjForCondition(new ConditionObjCallback() {
                        @Override
                        public void onSelected(final PhysObj obj) {
                            if (obj == null) return;
                            cond.targetObj = obj;
                            cond.contactNth = finalNth;
                            LinearLayout ll2 = new LinearLayout(MainActivity.this);
                            ll2.setOrientation(LinearLayout.VERTICAL);
                            ll2.setPadding(40, 20, 40, 10);
                            TextView tv2 = new TextView(MainActivity.this);
                            tv2.setText("时间 t (秒):");
                            tv2.setTextColor(Color.BLACK);
                            final EditText etT = new EditText(MainActivity.this);
                            etT.setText("1");
                            etT.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                            setupMathKeyboard(etT);
                            ll2.addView(tv2);
                            ll2.addView(etT);
                            new AlertDialog.Builder(MainActivity.this).setTitle("第...次接触...物体...时间后").setView(ll2)
                                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d2, int w2) {
                                        cond.timeParam = parseExpr(etT.getText().toString(), 1f);
                                        callback.onCondition(cond);
                                    }
                                }).setNegativeButton("取消", null).show();
                        }
                    });
                }
            }).setNegativeButton("取消", null).show();
    } else if (condType == LogicCondition.TYPE_COMPOUND || condType == LogicCondition.TYPE_COMPOUND_OR) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(40, 20, 40, 10);
        TextView tv = new TextView(this);
        tv.setText("总逻辑数 (1-5):");
        tv.setTextColor(Color.BLACK);
        final EditText et = new EditText(this);
        et.setText("2");
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        ll.addView(tv);
        ll.addView(et);
        new AlertDialog.Builder(this).setTitle("复合逻辑").setView(ll)
            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int w) {
                    int count = 2;
                    try { count = Integer.parseInt(et.getText().toString()); } catch (Exception e) {}
                    count = Math.max(1, Math.min(5, count));
                    buildCompoundSubs(cond, count, 0, callback);
                }
            }).setNegativeButton("取消", null).show();
    }
}

void askRequiredCount(final LogicCondition compound, final LogicConditionCallback callback) {
    LinearLayout ll = new LinearLayout(this);
    ll.setOrientation(LinearLayout.VERTICAL);
    ll.setPadding(40, 20, 40, 10);
    TextView tv = new TextView(this);
    tv.setText("所需同时生效逻辑数 (1-" + compound.subConditions.size() + "):");
    tv.setTextColor(Color.BLACK);
    final EditText et = new EditText(this);
    et.setText(String.valueOf(compound.subConditions.size()));
    et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
    ll.addView(tv);
    ll.addView(et);
    new AlertDialog.Builder(this).setTitle("复合逻辑").setView(ll)
        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int w) {
                int n = compound.subConditions.size();
                try { n = Integer.parseInt(et.getText().toString()); } catch (Exception e) {}
                n = Math.max(1, Math.min(compound.subConditions.size(), n));
                compound.requiredCount = n;
                callback.onCondition(compound);
            }
        }).setNegativeButton("取消", null).show();
}

    void buildCompoundSubs(final LogicCondition compound, final int total, final int idx, final LogicConditionCallback callback) {
        if (idx >= total) { askRequiredCount(compound, callback); return; }
        final String[] typeNames = {
            "1. 经过...时间",
            "2. 接触...物体",
            "3. 至...时间",
            "4. ...物体不存在",
            "5. 接触...物体...时间（连续）",
            "6. 接触...物体总计...时间",
            "7. 第...次接触...物体...时间后"
        };
        new AlertDialog.Builder(this)
            .setTitle("选择第" + (idx + 1) + "/" + total + "个条件")
            .setItems(typeNames, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int which) {
                    buildConditionOfType(which + 1, new LogicConditionCallback() {
                        @Override
                        public void onCondition(LogicCondition subCond) {
                            compound.subConditions.add(subCond);
                            buildCompoundSubs(compound, total, idx + 1, callback);
                        }
                    });
                }
            }).setNegativeButton("取消", null).show();
    }

    void selectObjForCondition(final ConditionObjCallback callback) {
        if (objects.isEmpty()) {
            new AlertDialog.Builder(this)
                .setMessage("场景中没有可选择的物体")
                .setPositiveButton("确定", null).show();
            return;
        }
        startSelectConditionTarget(callback);
    }

    LinearLayout makeUntilRow(View mainContent, LogicCondition currentCond, final ConditionSetter setter) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams mainLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        mainContent.setLayoutParams(mainLp);
        row.addView(mainContent);
        final Button btnUntil = new Button(this);
        btnUntil.setPadding(4, 0, 4, 0);
        btnUntil.setTextSize(8);
        btnUntil.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        btnUntil.setBackgroundColor(currentCond != null ? 0xFF4CAF50 : 0xFFCCCCCC);
        btnUntil.setText(currentCond != null ? "✓直到" : "直到");
        btnUntil.setTextColor(Color.BLACK);
        btnUntil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConditionDialog(new LogicConditionCallback() {
                    @Override
                    public void onCondition(LogicCondition cond) {
                        setter.set(cond);
                        if (cond != null) cond.reset();
                        btnUntil.setBackgroundColor(cond != null ? 0xFF4CAF50 : 0xFFCCCCCC);
                        btnUntil.setText(cond != null ? "✓直到" : "直到");
                    }
                });
            }
        });
        row.addView(btnUntil);
        return row;
    }

    static double firstHalfKick(double velocity, double acceleration, double dt) {
        return velocity + 0.5 * acceleration * dt;
    }

    static double advancePosition(double position, double halfVelocity, double dt) {
        return position + halfVelocity * dt;
    }

    static double secondHalfKick(double halfVelocity, double endAcceleration, double dt) {
        return halfVelocity + 0.5 * endAcceleration * dt;
    }

    static double distanceVelocityImpulse(double relativeVx, double relativeVy,
                                          double nx, double ny,
                                          double inverseMassA, double inverseMassB) {
        double inverseMassSum = inverseMassA + inverseMassB;
        if (inverseMassSum <= 0.0) return 0.0;
        double relativeNormalVelocity = relativeVx * nx + relativeVy * ny;
        return relativeNormalVelocity / inverseMassSum;
    }

    static double kineticEnergy(double mass, double vx, double vy) {
        return 0.5 * mass * (vx * vx + vy * vy);
    }

    static double gravitationalPotentialEnergy(double mass, double gravity, double y) {
        return -mass * gravity * y;
    }

    static double springPotentialEnergy(double springK, double stretch) {
        return 0.5 * springK * stretch * stretch;
    }
}
