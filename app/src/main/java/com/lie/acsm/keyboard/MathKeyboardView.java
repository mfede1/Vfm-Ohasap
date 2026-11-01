package com.lie.acsm.keyboard;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.lie.acsm.formula.ENode;
import com.lie.acsm.formula.FormulaView;

public class MathKeyboardView extends LinearLayout {

    private MathEditText targetEditText;
    private FormulaView  targetFormulaView;

    private static final String[][] ROW1 = {
        {"sin","sin("}, {"asin","asin("},
        {"|","|"}, {"7","7"}, {"8","8"}, {"9","9"}, {MathChars.MULTIPLICATION,MathChars.MULTIPLICATION}, {"a/b","__FRAC__"}
    };
    private static final String[][] ROW2 = {
        {"cos","cos("}, {"acos","acos("},
        {"|","|"}, {"4","4"}, {"5","5"}, {"6","6"}, {"+","+"}, {"\u2212","-"}
    };
    private static final String[][] ROW3 = {
        {"tan","tan("}, {"atan","atan("},
        {"|","|"}, {"1","1"}, {"2","2"}, {"3","3"}, {"\u232B","__DEL__"}, {MathChars.PI,MathChars.PI}
    };
    private static final String[][] ROW4 = {
        {"ln","ln("}, {"lg","log("},
        {"|","|"}, {"0","0"}, {".","."}, {"\u2190","__LEFT__"}, {"\u2192","__RIGHT__"}, {MathChars.EULER,MathChars.EULER}
    };
    private static final String[][] ROW5 = {
        {MathChars.ROOT,"__SQRT__"}, {"x\u00B2","__POW2__"}, {"x^a","__POWA__"}, {"(","("}, {")",")"},
        {"|","|"}, {"\u2191","__UP__"}, {"\u2193","__DOWN__"}
    };

    public MathKeyboardView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setBackgroundColor(Color.parseColor("#ECEFF1"));
        setPadding(6, 8, 6, 8);

        addRow(context, ROW1);
        addRow(context, ROW2);
        addRow(context, ROW3);
        addRow(context, ROW4);
        addRow(context, ROW5);
    }

    private void addRow(Context context, String[][] keys) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 3, 0, 3);
        row.setLayoutParams(rowParams);

        for (String[] key : keys) {
            final String label = key[0];
            final String value = key[1];

            if (label.equals("|")) {
                View sep = new View(context);
                LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                        dpToPx(context, 4), LayoutParams.MATCH_PARENT);
                sep.setLayoutParams(sp);
                sep.setBackgroundColor(Color.parseColor("#B0BEC5"));
                row.addView(sep);
                continue;
            }

            Button btn = buildButton(context, label, value);
            row.addView(btn);
        }

        addView(row);
    }

    private Button buildButton(Context context, String label, final String value) {
        Button btn = new Button(context);
        btn.setAllCaps(false);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(2, 2, 2, 2);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,
                dpToPx(context, 48), 1f);
        p.setMargins(3, 1, 3, 1);
        btn.setLayoutParams(p);

        if (value.equals("__DEL__")) {
            btn.setBackgroundColor(Color.parseColor("#EF9A9A"));
            btn.setTextColor(Color.parseColor("#B71C1C"));
        } else if (value.equals("__FRAC__")) {
            btn.setBackgroundColor(Color.parseColor("#CE93D8"));
            btn.setTextColor(Color.WHITE);
        } else if (value.equals("__SQRT__") || value.equals("__POW2__") || value.equals("__POWA__")) {
            btn.setBackgroundColor(Color.parseColor("#B3E5FC"));
            btn.setTextColor(Color.parseColor("#01579B"));
        } else if (isFunction(value)) {
            btn.setBackgroundColor(Color.parseColor("#B3E5FC"));
            btn.setTextColor(Color.parseColor("#01579B"));
        } else if (isNumber(value)) {
            btn.setBackgroundColor(Color.WHITE);
            btn.setTextColor(Color.parseColor("#212121"));
        } else {
            btn.setBackgroundColor(Color.parseColor("#E3F2FD"));
            btn.setTextColor(Color.parseColor("#0D47A1"));
        }

        if (label.equals("x\u00B2") || label.equals("x^a")) {
            btn.setText(buildSuperscriptLabel(label));
        } else {
            btn.setText(label);
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleKey(value);
            }
        });
        return btn;
    }

    private static SpannableString buildSuperscriptLabel(String raw) {
        SpannableStringBuilder sb = new SpannableStringBuilder(raw);
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '\u00B2' || c == 'a') {
                if (c == 'a' && i > 0 && raw.charAt(i - 1) == '^') {
                    sb.setSpan(new SuperscriptSpan(), i, raw.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.setSpan(new RelativeSizeSpan(0.65f), i, raw.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (c != 'a') {
                    sb.setSpan(new SuperscriptSpan(), i, raw.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.setSpan(new RelativeSizeSpan(0.65f), i, raw.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                break;
            }
        }
        return new SpannableString(sb);
    }

    private void handleKey(String value) {
        if (targetEditText != null) {
            switch (value) {
                case "__SQRT__": {
                    ENode sq = ENode.ofSqrt();
                    targetEditText.insertStructured(sq);
                    break;
                }
                case "__POW2__": {
                    ENode pw = ENode.ofPower();
                    pw.slots[1].insert(0, ENode.ofChar('2'));
                    targetEditText.insertStructured(pw);
                    targetEditText.handleKey("__RIGHT__");
                    break;
                }
                case "__POWA__": {
                    ENode pw = ENode.ofPower();
                    targetEditText.insertStructured(pw);
                    break;
                }
                default:
                    targetEditText.handleKey(value);
                    break;
            }
        } else if (targetFormulaView != null) {
            switch (value) {
                case "__DEL__": case "__LEFT__": case "__RIGHT__": break;
                case "__FRAC__":
                    targetFormulaView.setFormula(targetFormulaView.getFormula() + "frac(,)");
                    break;
                case "__SQRT__":
                    targetFormulaView.setFormula(targetFormulaView.getFormula() + "sqrt()");
                    break;
                case "__POW2__":
                    targetFormulaView.setFormula(targetFormulaView.getFormula() + "pow(,2)");
                    break;
                case "__POWA__":
                    targetFormulaView.setFormula(targetFormulaView.getFormula() + "pow(,)");
                    break;
                default:
                    targetFormulaView.setFormula(targetFormulaView.getFormula() + value);
                    break;
            }
        }
    }

    public void setTargetEditText(MathEditText et) {
        this.targetEditText = et;
    }

    public void setTargetFormulaView(FormulaView fv) {
        this.targetFormulaView = fv;
    }

    private static boolean isFunction(String v) {
        return v.endsWith("(");
    }

    private static boolean isNumber(String v) {
        if (v.length() == 1) {
            char c = v.charAt(0);
            return c >= '0' && c <= '9';
        }
        return false;
    }

    private int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
