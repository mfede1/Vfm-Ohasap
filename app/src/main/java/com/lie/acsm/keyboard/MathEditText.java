package com.lie.acsm.keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.lie.acsm.formula.EBuild;
import com.lie.acsm.formula.ECursor;
import com.lie.acsm.formula.ELayout;
import com.lie.acsm.formula.ENode;
import com.lie.acsm.formula.ERenderer;
import com.lie.acsm.formula.ESeq;
import com.lie.acsm.formula.ETree;

public class MathEditText extends View {

    private ETree tree;
    private ECursor cursor;
    private ERenderer renderer;
    private ERenderer flatRenderer;
    private Paint paint;
    private boolean focused = false;
    private final ELayout layout = new ELayout();

    private final Handler blinkHandler = new Handler(Looper.getMainLooper());
    private boolean cursorVisible = true;
    private final Runnable blinkRunnable = new Runnable() {
        @Override
        public void run() {
            cursorVisible = !cursorVisible;
            invalidate();
            blinkHandler.postDelayed(this, 530);
        }
    };

    public MathEditText(Context context) {
        super(context);
        init();
    }

    public MathEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MathEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#212121"));
        paint.setTextSize(54f);
        paint.setTextAlign(Paint.Align.LEFT);

        tree = new ETree();
        cursor = new ECursor(tree.root, 0);
        renderer = new ERenderer(cursor);
        flatRenderer = new ERenderer(null);

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void setTextSize(float sp) {
        paint.setTextSize(sp * getResources().getDisplayMetrics().scaledDensity);
        requestLayout();
        invalidate();
    }

    public void setTextColor(int color) {
        paint.setColor(color);
        invalidate();
    }

    public String getText2() {
        return tree.serialize();
    }

    public void setText2(String s) {
        setPlainText(s);
    }

    public String getPlainText() {
        return EBuild.toPlain(tree.root);
    }

    public void setPlainText(String s) {
        tree = EBuild.fromPlain(s);
        cursor = new ECursor(tree.root, tree.root.size());
        renderer = new ERenderer(cursor);
        requestLayout();
        invalidate();
    }

    public void requestFocus2() {
        focused = true;
        cursorVisible = true;
        blinkHandler.removeCallbacks(blinkRunnable);
        blinkHandler.postDelayed(blinkRunnable, 530);
        invalidate();
    }

    public void clearFocus2() {
        focused = false;
        blinkHandler.removeCallbacks(blinkRunnable);
        invalidate();
    }

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        RectF bounds = ERenderer.measureSeq(tree.root, paint);
        int minH = (int) (paint.getTextSize() * 2.2f);
        int w = (int) Math.ceil(bounds.width()) + getPaddingLeft() + getPaddingRight() + 24;
        int h = Math.max(minH, (int) Math.ceil(bounds.height())) + getPaddingTop() + getPaddingBottom() + 8;
        setMeasuredDimension(resolveSize(Math.max(w, 60), wSpec),
                             resolveSize(Math.max(h, minH), hSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint bg = new Paint();
        bg.setColor(Color.parseColor("#F5F5F5"));
        canvas.drawRect(0, 0, getWidth(), getHeight(), bg);

        RectF bounds = ERenderer.measureSeq(tree.root, paint);
        float x = getPaddingLeft() + 6f;
        float baseline = getPaddingTop() + 4f + (-bounds.top);

        layout.layout(tree.root, x, baseline, paint);

        if (focused && cursorVisible) {
            renderer.drawSeq(canvas, tree.root, x, baseline, paint);
        } else {
            flatRenderer.drawSeq(canvas, tree.root, x, baseline, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN
                || action == MotionEvent.ACTION_MOVE
                || action == MotionEvent.ACTION_UP) {
            placeCursorAt(event.getX(), event.getY());
            requestFocus2();
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void placeCursorAt(float px, float py) {
        ELayout.Box b = layout.hitTestBest(px, py);
        if (b == null) {
            cursor.set(tree.root, tree.root.size());
        } else {
            int p = b.index;
            if (b.seq.size() > 0 && px > b.x + b.w / 2f) p = b.index + 1;
            cursor.set(b.seq, p);
            cursor.clamp();
        }
        cursorVisible = true;
        blinkHandler.removeCallbacks(blinkRunnable);
        blinkHandler.postDelayed(blinkRunnable, 530);
        invalidate();
    }

    public void handleKey(String value) {
        if (value.equals("__DEL__")) {
            cursor.delete(tree.root);
        } else if (value.equals("__LEFT__")) {
            cursor.moveLeft(tree.root);
        } else if (value.equals("__RIGHT__")) {
            cursor.moveRight(tree.root);
        } else if (value.equals("__UP__")) {
            cursor.moveUp(tree.root);
        } else if (value.equals("__DOWN__")) {
            cursor.moveDown(tree.root);
        } else if (value.equals("__HOME__")) {
            cursor.moveHome();
        } else if (value.equals("__END__")) {
            cursor.moveEnd();
        } else if (value.equals("__FRAC__")) {
            cursor.insertFrac();
        } else if (value.equals("__CLEAR__")) {
            setPlainText("");
            focused = true;
        } else if (value.length() > 1 && value.endsWith("(")) {
            insertString(value);
            cursor.insert(ENode.ofChar(')'));
            cursor.moveLeft(tree.root);
        } else {
            insertString(value);
        }
        cursorVisible = true;
        blinkHandler.removeCallbacks(blinkRunnable);
        blinkHandler.postDelayed(blinkRunnable, 530);
        requestLayout();
        invalidate();
    }

    private void insertString(String text) {
        for (int i = 0; i < text.length(); i++) {
            cursor.insert(ENode.ofChar(text.charAt(i)));
        }
    }

    public void insertStructured(ENode n) {
        cursor.insert(n);
        cursorVisible = true;
        requestLayout();
        invalidate();
    }

    public static String toFormulaString(String plain) {
        return plain == null ? "" : plain;
    }

    public boolean isFormulaMode() {
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        blinkHandler.removeCallbacks(blinkRunnable);
    }
}
