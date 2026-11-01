package com.lie.acsm.formula;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class FormulaView extends View {

    private final FormulaParser parser = new FormulaParser();
    private Paint paint;
    private FormulaNode formulaNode;
    private String formulaText = "";


    public FormulaView(Context context) {
        super(context);
        init();
    }

    public FormulaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FormulaView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTextSize(48f);
        paint.setTextAlign(Paint.Align.LEFT);
    }


    public void setFormula(String formula) {
        this.formulaText = (formula == null) ? "" : formula;
        this.formulaNode = parser.parse(formulaText);
        requestLayout();
        invalidate();
    }

    public String getFormula() {
        return formulaText;
    }

    public void setTextSize(float sizeSp) {
        paint.setTextSize(sizeSp * getResources().getDisplayMetrics().scaledDensity);
        requestLayout();
        invalidate();
    }

    public void setTextSizePx(float sizePx) {
        paint.setTextSize(sizePx);
        requestLayout();
        invalidate();
    }

    public void setTextColor(int color) {
        paint.setColor(color);
        invalidate();
    }


    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        FormulaNode node = ensureNode();
        RectF bounds = FormulaRenderer.measure(node, paint);
        int w = (int) Math.ceil(bounds.width()) + getPaddingLeft() + getPaddingRight();
        int h = (int) Math.ceil(FormulaRenderer.totalHeight(bounds)) + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(
                resolveSize(Math.max(w, getSuggestedMinimumWidth()), widthSpec),
                resolveSize(Math.max(h, getSuggestedMinimumHeight()), heightSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        FormulaNode node = ensureNode();
        RectF bounds = FormulaRenderer.measure(node, paint);
        float baseline = getPaddingTop() + (-bounds.top);
        float x = getPaddingLeft();
        FormulaRenderer.draw(canvas, node, paint, x, baseline);
    }


    private FormulaNode ensureNode() {
        if (formulaNode == null) {
            formulaNode = parser.parse(formulaText);
        }
        return formulaNode;
    }
}
