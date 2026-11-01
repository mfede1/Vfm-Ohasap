package com.lie.acsm.formula;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class ERenderer {

    private static final float FRAC_PAD  = 4f;
    private static final float BAR_H     = 1.5f;
    private static final float SUP_SCALE = 0.65f;
    private static final float SUP_RISE  = 0.50f;
    private static final float IDX_SCALE = 0.55f;
    private static final float HOOK_W    = 8f;
    private static final float HOOK_RISE = 6f;
    private static final float PLACEHOLDER_W = 12f;

    private final ECursor cursor;

    public ERenderer(ECursor cursor) {
        this.cursor = cursor;
    }

    public float drawSeq(Canvas canvas, ESeq seq, float x, float baseline, Paint paint) {
        if (seq.nodes.isEmpty()) {
            drawPlaceholder(canvas, seq, 0, x, baseline, paint);
            return PLACEHOLDER_W;
        }
        float cx = x;
        for (int i = 0; i < seq.size(); i++) {
            if (cursor != null && cursor.seq == seq && cursor.pos == i) {
                drawCursor(canvas, cx, baseline, paint);
            }
            float w = drawNode(canvas, seq.get(i), i, seq, cx, baseline, paint);
            cx += w;
        }
        if (cursor != null && cursor.seq == seq && cursor.pos == seq.size()) {
            drawCursor(canvas, cx, baseline, paint);
        }
        return cx - x;
    }

    private float drawNode(Canvas canvas, ENode n, int idx, ESeq seq,
                           float x, float baseline, Paint paint) {
        switch (n.kind) {
            case CHAR:  return drawChar(canvas, n, x, baseline, paint);
            case FRAC:  return drawFrac(canvas, n, x, baseline, paint);
            case SQRT:  return drawSqrt(canvas, n, x, baseline, paint);
            case NROOT: return drawNroot(canvas, n, x, baseline, paint);
            case POWER: return drawPower(canvas, n, x, baseline, paint);
            default:    return 0;
        }
    }

    private float drawChar(Canvas canvas, ENode n, float x, float baseline, Paint paint) {
        String s = String.valueOf(n.ch);
        if (canvas != null) canvas.drawText(s, x, baseline, paint);
        return paint.measureText(s);
    }

    private float drawFrac(Canvas canvas, ENode n, float x, float baseline, Paint paint) {
        RectF numR = measureSeq(n.slots[0], paint);
        RectF denR = measureSeq(n.slots[1], paint);
        float w = Math.max(numR.width(), denR.width()) + FRAC_PAD * 2;

        float xH    = paint.getTextSize() * 0.5f;
        float barY  = baseline - xH * 0.5f;

        float numBaseline = barY - FRAC_PAD - BAR_H / 2f - numR.bottom;
        float numX = x + (w - numR.width()) / 2f;
        drawSeq(canvas, n.slots[0], numX, numBaseline, paint);

        if (canvas != null) {
            Paint barPaint = new Paint(paint);
            barPaint.setStyle(Paint.Style.STROKE);
            barPaint.setStrokeWidth(BAR_H);
            canvas.drawLine(x, barY, x + w, barY, barPaint);
        }

        float denBaseline = barY + BAR_H / 2f + FRAC_PAD - denR.top;
        float denX = x + (w - denR.width()) / 2f;
        drawSeq(canvas, n.slots[1], denX, denBaseline, paint);
        return w;
    }

    private float drawSqrt(Canvas canvas, ENode n, float x, float baseline, Paint paint) {
        RectF argR = measureSeq(n.slots[0], paint);
        float w = HOOK_W + argR.width() + 4f;
        float vinculumY = baseline + argR.top - HOOK_RISE * 0.3f;

        if (canvas != null) {
            Paint lp = new Paint(paint);
            lp.setStyle(Paint.Style.STROKE);
            lp.setStrokeWidth(BAR_H);
            float hStartX = x;
            float hMidX   = x + HOOK_W * 0.35f;
            float hTopX   = x + HOOK_W;
            float hStartY = baseline - paint.getTextSize() * 0.2f;
            float hMidY   = baseline + paint.descent() * 0.5f;
            canvas.drawLine(hStartX, hStartY, hMidX, hMidY,   lp);
            canvas.drawLine(hMidX,   hMidY,   hTopX, vinculumY, lp);
            float vinEnd = x + HOOK_W + argR.width() + 2f;
            canvas.drawLine(hTopX, vinculumY, vinEnd, vinculumY, lp);
        }
        drawSeq(canvas, n.slots[0], x + HOOK_W, baseline, paint);
        return w;
    }

    private float drawNroot(Canvas canvas, ENode n, float x, float baseline, Paint paint) {
        Paint idxPaint = scaledPaint(paint, IDX_SCALE);
        RectF idxR = measureSeq(n.slots[0], idxPaint);
        drawSeq(canvas, n.slots[0], x, baseline - paint.getTextSize() * 0.6f, idxPaint);

        ENode sqrtShell = ENode.ofSqrt();
        sqrtShell.slots[0] = n.slots[1];
        float sw = drawSqrt(canvas, sqrtShell, x + idxR.width(), baseline, paint);
        return idxR.width() + sw;
    }

    private float drawPower(Canvas canvas, ENode n, float x, float baseline, Paint paint) {
        RectF baseR = measureSeq(n.slots[0], paint);
        Paint supPaint = scaledPaint(paint, SUP_SCALE);
        float supRise = paint.getTextSize() * SUP_RISE;
        drawSeq(canvas, n.slots[0], x, baseline, paint);
        drawSeq(canvas, n.slots[1], x + baseR.width(), baseline - supRise, supPaint);
        RectF expR = measureSeq(n.slots[1], supPaint);
        return baseR.width() + expR.width();
    }

    private void drawCursor(Canvas canvas, float cx, float baseline, Paint paint) {
        if (canvas == null) return;
        Paint.FontMetrics fm = paint.getFontMetrics();
        float top = baseline + fm.top * 1.1f;
        float bot = baseline + fm.bottom * 0.8f;
        Paint cp = new Paint();
        cp.setColor(0xFF2196F3);
        cp.setStrokeWidth(2f);
        canvas.drawLine(cx, top, cx, bot, cp);
    }

    private void drawPlaceholder(Canvas canvas, ESeq seq, int pos,
                                  float x, float baseline, Paint paint) {
        if (canvas == null) return;
        if (cursor != null && cursor.seq == seq && cursor.pos == pos) {
            drawCursor(canvas, x, baseline, paint);
        }
        Paint bp = new Paint(paint);
        bp.setStyle(Paint.Style.STROKE);
        bp.setStrokeWidth(1f);
        bp.setColor(0xFF9E9E9E);
        float h = paint.getTextSize() * 0.8f;
        float top = baseline - h;
        canvas.drawRect(x, top, x + PLACEHOLDER_W, baseline + paint.descent(), bp);
    }

    public static RectF measureSeq(ESeq seq, Paint paint) {
        if (seq.nodes.isEmpty()) {
            Paint.FontMetrics fm = paint.getFontMetrics();
            return new RectF(0, fm.top, PLACEHOLDER_W, fm.bottom);
        }
        float totalW = 0;
        float top = 0, bot = 0;
        for (ENode n : seq.nodes) {
            RectF r = measureNode(n, paint);
            totalW += r.width();
            if (r.top < top) top = r.top;
            if (r.bottom > bot) bot = r.bottom;
        }
        return new RectF(0, top, totalW, bot);
    }

    private static RectF measureNode(ENode n, Paint paint) {
        switch (n.kind) {
            case CHAR: {
                float w = paint.measureText(String.valueOf(n.ch));
                Paint.FontMetrics fm = paint.getFontMetrics();
                return new RectF(0, fm.top, w, fm.bottom);
            }
            case FRAC: {
                RectF numR = measureSeq(n.slots[0], paint);
                RectF denR = measureSeq(n.slots[1], paint);
                float w = Math.max(numR.width(), denR.width()) + FRAC_PAD * 2;
                float asc = numR.height() + FRAC_PAD + BAR_H / 2f;
                float des = denR.height() + FRAC_PAD + BAR_H / 2f;
                return new RectF(0, -asc, w, des);
            }
            case SQRT: {
                RectF argR = measureSeq(n.slots[0], paint);
                float w = HOOK_W + argR.width() + 4f;
                return new RectF(0, -(argR.height() + HOOK_RISE), w, paint.getFontMetrics().descent);
            }
            case NROOT: {
                Paint ip = scaledPaint(paint, IDX_SCALE);
                RectF idxR = measureSeq(n.slots[0], ip);
                RectF argR = measureSeq(n.slots[1], paint);
                float sqW = HOOK_W + argR.width() + 4f;
                float sqTop = -(argR.height() + HOOK_RISE);
                return new RectF(0, sqTop - idxR.height() * 0.3f,
                        idxR.width() + sqW, paint.getFontMetrics().descent);
            }
            case POWER: {
                RectF baseR = measureSeq(n.slots[0], paint);
                Paint sp = scaledPaint(paint, SUP_SCALE);
                RectF expR = measureSeq(n.slots[1], sp);
                float supRise = paint.getTextSize() * SUP_RISE;
                float top = Math.min(baseR.top, -(supRise + expR.height()));
                return new RectF(0, top, baseR.width() + expR.width(), baseR.bottom);
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
}
