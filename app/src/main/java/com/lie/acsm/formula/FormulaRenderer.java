package com.lie.acsm.formula;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class FormulaRenderer {

    private static final float FRAC_PADDING   = 4f;
    private static final float BAR_THICKNESS  = 1.5f;
    private static final float SUP_SCALE      = 0.65f;
    private static final float SUP_RISE       = 0.50f;
    private static final float IDX_SCALE      = 0.55f;
    private static final float SQRT_HOOK_W    = 8f;
    private static final float SQRT_HOOK_RISE = 6f;


    public static RectF measure(FormulaNode node, Paint paint) {
        switch (node.type) {
            case TEXT:     return measureText(node.text, paint);
            case FRACTION: return measureFraction(node, paint);
            case SQRT:     return measureSqrt(node, paint);
            case NROOT:    return measureNroot(node, paint);
            case POWER:    return measurePower(node, paint);
            case SEQUENCE: return measureSequence(node, paint);
            default:       return new RectF(0, 0, 0, 0);
        }
    }


    public static void draw(Canvas canvas, FormulaNode node, Paint paint, float x, float baseline) {
        switch (node.type) {
            case TEXT:     drawText(canvas, node.text, paint, x, baseline); break;
            case FRACTION: drawFraction(canvas, node, paint, x, baseline); break;
            case SQRT:     drawSqrt(canvas, node, paint, x, baseline); break;
            case NROOT:    drawNroot(canvas, node, paint, x, baseline); break;
            case POWER:    drawPower(canvas, node, paint, x, baseline); break;
            case SEQUENCE: drawSequence(canvas, node, paint, x, baseline); break;
        }
    }


    private static RectF measureText(String text, Paint paint) {
        if (text == null || text.isEmpty()) return new RectF(0, -paint.getTextSize() * 0.8f, 0, paint.descent());
        float w = paint.measureText(text);
        Paint.FontMetrics fm = paint.getFontMetrics();
        return new RectF(0, fm.top, w, fm.bottom);
    }

    private static RectF measureFraction(FormulaNode node, Paint paint) {
        RectF numR = measure(node.children.get(0), paint);
        RectF denR = measure(node.children.get(1), paint);
        float w = Math.max(numR.width(), denR.width()) + FRAC_PADDING * 2;
        float totalH = numR.height() + FRAC_PADDING * 2 + BAR_THICKNESS + denR.height();
        float ascent  = numR.height() + FRAC_PADDING + BAR_THICKNESS / 2f;
        float descent = denR.height() + FRAC_PADDING + BAR_THICKNESS / 2f;
        return new RectF(0, -ascent, w, descent);
    }

    private static RectF measureSqrt(FormulaNode node, Paint paint) {
        RectF argR = measure(node.children.get(0), paint);
        float w = SQRT_HOOK_W + argR.width() + 4f;
        float ascent  = argR.height() + SQRT_HOOK_RISE;
        float descent = paint.descent();
        return new RectF(0, -ascent, w, descent);
    }

    private static RectF measureNroot(FormulaNode node, Paint paint) {
        Paint idxPaint = scaledPaint(paint, IDX_SCALE);
        RectF idxR  = measure(node.children.get(0), idxPaint);
        RectF sqrtR = measureSqrt(node, paint);
        float w = idxR.width() + sqrtR.width();
        return new RectF(0, sqrtR.top - idxR.height() * 0.3f, w, sqrtR.bottom);
    }

    private static RectF measurePower(FormulaNode node, Paint paint) {
        RectF baseR = measure(node.children.get(0), paint);
        Paint supPaint = scaledPaint(paint, SUP_SCALE);
        RectF expR = measure(node.children.get(1), supPaint);
        float supRise = paint.getTextSize() * SUP_RISE;
        float w = baseR.width() + expR.width();
        float top = Math.min(baseR.top, -(supRise + expR.height()));
        return new RectF(0, top, w, baseR.bottom);
    }

    private static RectF measureSequence(FormulaNode node, Paint paint) {
        float totalW = 0;
        float top = 0, bottom = 0;
        for (FormulaNode child : node.children) {
            RectF r = measure(child, paint);
            totalW += r.width();
            if (r.top < top) top = r.top;
            if (r.bottom > bottom) bottom = r.bottom;
        }
        return new RectF(0, top, totalW, bottom);
    }


    private static void drawText(Canvas canvas, String text, Paint paint, float x, float baseline) {
        if (text != null && !text.isEmpty()) {
            canvas.drawText(text, x, baseline, paint);
        }
    }

    private static void drawFraction(Canvas canvas, FormulaNode node, Paint paint, float x, float baseline) {
        FormulaNode num = node.children.get(0);
        FormulaNode den = node.children.get(1);
        RectF numR = measure(num, paint);
        RectF denR = measure(den, paint);
        float boxW = Math.max(numR.width(), denR.width()) + FRAC_PADDING * 2;

        float barY = baseline;

        float numBaseline = barY - FRAC_PADDING - BAR_THICKNESS / 2f - numR.bottom;
        float numX = x + (boxW - numR.width()) / 2f;
        draw(canvas, num, paint, numX, numBaseline);

        Paint barPaint = new Paint(paint);
        barPaint.setStyle(Paint.Style.STROKE);
        barPaint.setStrokeWidth(BAR_THICKNESS);
        canvas.drawLine(x, barY, x + boxW, barY, barPaint);

        float denBaseline = barY + BAR_THICKNESS / 2f + FRAC_PADDING - denR.top;
        float denX = x + (boxW - denR.width()) / 2f;
        draw(canvas, den, paint, denX, denBaseline);
    }

    private static void drawSqrt(Canvas canvas, FormulaNode node, Paint paint, float x, float baseline) {
        FormulaNode arg = node.children.get(0);
        RectF argR = measure(arg, paint);

        float argX = x + SQRT_HOOK_W;
        float vinculumY = baseline + argR.top - SQRT_HOOK_RISE * 0.3f;

        Paint linePaint = new Paint(paint);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(BAR_THICKNESS);

        float hookStartX = x;
        float hookMidX   = x + SQRT_HOOK_W * 0.35f;
        float hookTopX   = x + SQRT_HOOK_W;

        float hookStartY = baseline - paint.getTextSize() * 0.2f;
        float hookMidY   = baseline + paint.descent() * 0.5f;
        float hookTopY   = vinculumY;

        canvas.drawLine(hookStartX, hookStartY, hookMidX,  hookMidY,  linePaint);
        canvas.drawLine(hookMidX,  hookMidY,   hookTopX,  hookTopY,  linePaint);

        float vinculumEndX = argX + argR.width() + 2f;
        canvas.drawLine(hookTopX, vinculumY, vinculumEndX, vinculumY, linePaint);

        draw(canvas, arg, paint, argX, baseline);
    }

    private static void drawNroot(Canvas canvas, FormulaNode node, Paint paint, float x, float baseline) {
        Paint idxPaint = scaledPaint(paint, IDX_SCALE);
        RectF idxR = measure(node.children.get(0), idxPaint);

        float idxX = x;
        float idxBaseline = baseline - paint.getTextSize() * 0.6f;
        draw(canvas, node.children.get(0), idxPaint, idxX, idxBaseline);

        float sqrtX = x + idxR.width();
        FormulaNode sqrtNode = FormulaNode.sqrt(node.children.get(1));
        drawSqrt(canvas, sqrtNode, paint, sqrtX, baseline);
    }

    private static void drawPower(Canvas canvas, FormulaNode node, Paint paint, float x, float baseline) {
        FormulaNode base = node.children.get(0);
        FormulaNode exp  = node.children.get(1);
        RectF baseR = measure(base, paint);
        Paint supPaint = scaledPaint(paint, SUP_SCALE);
        float supRise = paint.getTextSize() * SUP_RISE;

        draw(canvas, base, paint, x, baseline);
        draw(canvas, exp, supPaint, x + baseR.width(), baseline - supRise);
    }

    private static void drawSequence(Canvas canvas, FormulaNode node, Paint paint, float x, float baseline) {
        float cx = x;
        for (FormulaNode child : node.children) {
            draw(canvas, child, paint, cx, baseline);
            cx += measure(child, paint).width();
        }
    }


    public static Paint scaledPaint(Paint src, float scale) {
        Paint p = new Paint(src);
        p.setTextSize(src.getTextSize() * scale);
        return p;
    }

    public static float totalHeight(RectF r) {
        return r.bottom - r.top;
    }
}
