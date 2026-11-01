package com.lie.acsm.formula;

import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

public class ELayout {

    private static final float FRAC_PAD   = 4f;
    private static final float BAR_H      = 1.5f;
    private static final float SUP_SCALE  = 0.65f;
    private static final float SUP_RISE   = 0.50f;
    private static final float IDX_SCALE  = 0.55f;
    private static final float HOOK_W     = 8f;
    private static final float HOOK_RISE  = 6f;
    private static final float PLACEHOLDER_W = 12f;

    public static class Box {
        public final ESeq seq;
        public final int  index;
        public final float x, y, w, h;
        public final float baseline;
        public final float ascent;

        Box(ESeq seq, int index, float x, float y, float w, float h, float baseline) {
            this.seq      = seq;
            this.index    = index;
            this.x        = x;
            this.y        = y;
            this.w        = w;
            this.h        = h;
            this.baseline = baseline;
            this.ascent   = baseline - y;
        }
    }

    public final List<Box> boxes = new ArrayList<>();

    public void layout(ESeq seq, float x, float baseline, Paint paint) {
        boxes.clear();
        layoutSeq(seq, x, baseline, paint);
    }

    private float layoutSeq(ESeq seq, float x, float baseline, Paint paint) {
        if (seq.size() == 0) {
            Paint.FontMetrics fm = paint.getFontMetrics();
            float top = baseline + fm.top;
            float bot = baseline + fm.bottom;
            boxes.add(new Box(seq, 0, x, top, PLACEHOLDER_W, bot - top, baseline));
            return PLACEHOLDER_W;
        }
        float cx = x;
        for (int i = 0; i < seq.size(); i++) {
            float w = layoutNode(seq.get(i), i, seq, cx, baseline, paint);
            cx += w;
        }
        return cx - x;
    }

    private float layoutNode(ENode n, int idx, ESeq seq, float x, float baseline, Paint paint) {
        switch (n.kind) {
            case CHAR:   return layoutChar(n, idx, seq, x, baseline, paint);
            case FRAC:   return layoutFrac(n, idx, seq, x, baseline, paint);
            case SQRT:   return layoutSqrt(n, idx, seq, x, baseline, paint);
            case NROOT:  return layoutNroot(n, idx, seq, x, baseline, paint);
            case POWER:  return layoutPower(n, idx, seq, x, baseline, paint);
            default:     return 0;
        }
    }

    private float layoutChar(ENode n, int idx, ESeq seq, float x, float baseline, Paint paint) {
        String s = String.valueOf(n.ch);
        float w = paint.measureText(s);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float top = baseline + fm.top;
        float bot = baseline + fm.bottom;
        boxes.add(new Box(seq, idx, x, top, w, bot - top, baseline));
        return w;
    }

    private float layoutFrac(ENode n, int idx, ESeq seq, float x, float baseline, Paint paint) {
        RectF numR = measureSeq(n.slots[0], paint);
        RectF denR = measureSeq(n.slots[1], paint);
        float w = Math.max(numR.width(), denR.width()) + FRAC_PAD * 2;

        float xH = paint.getTextSize() * 0.5f;
        float barY = baseline - xH * 0.5f;

        float numBaseline = barY - FRAC_PAD - BAR_H / 2f - numR.bottom;
        float numX = x + (w - numR.width()) / 2f;
        layoutSeq(n.slots[0], numX, numBaseline, paint);

        float denBaseline = barY + BAR_H / 2f + FRAC_PAD - denR.top;
        float denX = x + (w - denR.width()) / 2f;
        layoutSeq(n.slots[1], denX, denBaseline, paint);

        float ascent = numR.height() + FRAC_PAD + BAR_H / 2f;
        float descent = denR.height() + FRAC_PAD + BAR_H / 2f;
        float top = baseline - ascent;
        float h   = ascent + descent;
        boxes.add(new Box(seq, idx, x, top, w, h, baseline));
        return w;
    }

    private float layoutSqrt(ENode n, int idx, ESeq seq, float x, float baseline, Paint paint) {
        RectF argR = measureSeq(n.slots[0], paint);
        float w = HOOK_W + argR.width() + 4f;
        float ascent = argR.height() + HOOK_RISE;
        Paint.FontMetrics fm = paint.getFontMetrics();
        float descent = fm.descent;
        float top = baseline - ascent;
        float h   = ascent + descent;
        boxes.add(new Box(seq, idx, x, top, w, h, baseline));

        float argX = x + HOOK_W;
        layoutSeq(n.slots[0], argX, baseline, paint);
        return w;
    }

    private float layoutNroot(ENode n, int idx, ESeq seq, float x, float baseline, Paint paint) {
        Paint idxPaint = scaledPaint(paint, IDX_SCALE);
        RectF idxR  = measureSeq(n.slots[0], idxPaint);
        RectF sqrtR = measureSqrtNode(n.slots[1], paint);
        float w = idxR.width() + sqrtR.width();

        float top = Math.min(sqrtR.top - idxR.height() * 0.3f, sqrtR.top);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float bot = baseline + fm.descent;
        boxes.add(new Box(seq, idx, x, top, w, bot - top, baseline));

        layoutSeq(n.slots[0], x, baseline - paint.getTextSize() * 0.6f, idxPaint);
        layoutSeq(n.slots[1], x + idxR.width(), baseline, paint);
        return w;
    }

    private float layoutPower(ENode n, int idx, ESeq seq, float x, float baseline, Paint paint) {
        RectF baseR = measureSeq(n.slots[0], paint);
        Paint supPaint = scaledPaint(paint, SUP_SCALE);
        RectF expR  = measureSeq(n.slots[1], supPaint);
        float supRise = paint.getTextSize() * SUP_RISE;
        float w = baseR.width() + expR.width();

        float top = Math.min(baseline + paint.getFontMetrics().top, -(supRise + expR.height()) + baseline);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float bot = baseline + fm.bottom;
        boxes.add(new Box(seq, idx, x, top, w, bot - top, baseline));

        layoutSeq(n.slots[0], x, baseline, paint);
        layoutSeq(n.slots[1], x + baseR.width(), baseline - supRise, supPaint);
        return w;
    }

    private static RectF measureSeq(ESeq seq, Paint paint) {
        float totalW = 0;
        float top = 0, bot = 0;
        for (ENode n : seq.nodes) {
            RectF r = measureNode(n, paint);
            totalW += r.width();
            if (r.top < top) top = r.top;
            if (r.bottom > bot) bot = r.bottom;
        }
        if (seq.nodes.isEmpty()) {
            Paint.FontMetrics fm = paint.getFontMetrics();
            top = fm.top;
            bot = fm.bottom;
            totalW = PLACEHOLDER_W;
        }
        return new RectF(0, top, totalW, bot);
    }

    private static RectF measureSqrtNode(ESeq inner, Paint paint) {
        RectF argR = measureSeq(inner, paint);
        float w = HOOK_W + argR.width() + 4f;
        float ascent = argR.height() + HOOK_RISE;
        float descent = paint.getFontMetrics().descent;
        return new RectF(0, -ascent, w, descent);
    }

    private static RectF measureNode(ENode n, Paint paint) {
        switch (n.kind) {
            case CHAR: {
                String s = String.valueOf(n.ch);
                float w = paint.measureText(s);
                Paint.FontMetrics fm = paint.getFontMetrics();
                return new RectF(0, fm.top, w, fm.bottom);
            }
            case FRAC: {
                RectF numR = measureSeq(n.slots[0], paint);
                RectF denR = measureSeq(n.slots[1], paint);
                float w = Math.max(numR.width(), denR.width()) + FRAC_PAD * 2;
                float ascent  = numR.height() + FRAC_PAD + BAR_H / 2f;
                float descent = denR.height() + FRAC_PAD + BAR_H / 2f;
                return new RectF(0, -ascent, w, descent);
            }
            case SQRT: {
                RectF argR = measureSeq(n.slots[0], paint);
                float w = HOOK_W + argR.width() + 4f;
                float ascent  = argR.height() + HOOK_RISE;
                float descent = paint.getFontMetrics().descent;
                return new RectF(0, -ascent, w, descent);
            }
            case NROOT: {
                Paint ip = scaledPaint(paint, IDX_SCALE);
                RectF idxR  = measureSeq(n.slots[0], ip);
                RectF sqrtR = measureSqrtNode(n.slots[1], paint);
                float w = idxR.width() + sqrtR.width();
                return new RectF(0, sqrtR.top - idxR.height() * 0.3f, w, sqrtR.bottom);
            }
            case POWER: {
                RectF baseR = measureSeq(n.slots[0], paint);
                Paint sp = scaledPaint(paint, SUP_SCALE);
                RectF expR  = measureSeq(n.slots[1], sp);
                float supRise = paint.getTextSize() * SUP_RISE;
                float w = baseR.width() + expR.width();
                float top = Math.min(baseR.top, -(supRise + expR.height()));
                return new RectF(0, top, w, baseR.bottom);
            }
            default:
                return new RectF(0, 0, 0, 0);
        }
    }

    private static Paint scaledPaint(Paint src, float scale) {
        Paint p = new Paint(src);
        p.setTextSize(src.getTextSize() * scale);
        return p;
    }

    public static RectF measureTree(ESeq root, Paint paint) {
        return measureSeq(root, paint);
    }

    public Box hitTest(float tx, float ty) {
        Box best = null;
        float bestDist = Float.MAX_VALUE;
        for (Box b : boxes) {
            if (tx >= b.x && tx <= b.x + b.w && ty >= b.y && ty <= b.y + b.h) {
                float dist = Math.abs(tx - (b.x + b.w / 2f)) + Math.abs(ty - b.baseline);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = b;
                }
            }
        }
        return best;
    }

    public Box findBox(ESeq seq, int index) {
        for (Box b : boxes) {
            if (b.seq == seq && b.index == index) return b;
        }
        return null;
    }

    public Box hitTestBest(float tx, float ty) {
        Box best = null;
        float bestArea = Float.MAX_VALUE;
        for (Box b : boxes) {
            if (tx >= b.x && tx <= b.x + b.w && ty >= b.y && ty <= b.y + b.h) {
                float area = b.w * b.h;
                if (area < bestArea) {
                    bestArea = area;
                    best = b;
                }
            }
        }
        if (best != null) return best;
        float bestDist = Float.MAX_VALUE;
        for (Box b : boxes) {
            float dx = tx < b.x ? b.x - tx : (tx > b.x + b.w ? tx - (b.x + b.w) : 0f);
            float dy = ty < b.y ? b.y - ty : (ty > b.y + b.h ? ty - (b.y + b.h) : 0f);
            float d = dx * 2f + dy;
            if (d < bestDist) {
                bestDist = d;
                best = b;
            }
        }
        return best;
    }
}
