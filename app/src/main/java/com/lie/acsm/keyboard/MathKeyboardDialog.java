package com.lie.acsm.keyboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MathKeyboardDialog {

    public interface ResultCallback {
        void onResult(String plainText, String formulaText);
    }

    public static void show(final Context context, final EditText target, final int decimalPlaces) {
        show(context, target, decimalPlaces, null);
    }

    public static void show(final Context context, final EditText target, final int decimalPlaces,
                            final ResultCallback callback) {

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#FAFAFA"));

        LinearLayout displayArea = new LinearLayout(context);
        displayArea.setOrientation(LinearLayout.VERTICAL);
        displayArea.setBackgroundColor(Color.WHITE);
        displayArea.setPadding(16, 12, 16, 12);

        TextView inputLabel = new TextView(context);
        inputLabel.setText("\u8868\u8FBE\u5F0F");
        inputLabel.setTextSize(11f);
        inputLabel.setTextColor(Color.parseColor("#9E9E9E"));
        displayArea.addView(inputLabel);

        final MathEditText mathEdit = new MathEditText(context);
        if (target != null && target.getText() != null) {
            mathEdit.setPlainText(target.getText().toString());
        }
        mathEdit.setTextSize(18f);
        mathEdit.setTextColor(Color.parseColor("#212121"));
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        editParams.setMargins(0, 4, 0, 8);
        mathEdit.setLayoutParams(editParams);
        mathEdit.requestFocus2();
        displayArea.addView(mathEdit);

        root.addView(displayArea, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        ScrollView keyboardScroll = new ScrollView(context);
        MathKeyboardView keyboard = new MathKeyboardView(context);
        keyboard.setTargetEditText(mathEdit);

        keyboardScroll.addView(keyboard);
        root.addView(keyboardScroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("\u6570\u5B66\u952E\u76D8")
                .setView(root)
                .setPositiveButton("\u5B8C\u6210", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        String plain = mathEdit.getPlainText();
                        if (target != null) target.setText(plain);
                        if (callback != null) callback.onResult(plain, mathEdit.getText2());
                    }
                })
                .setNegativeButton("\u6E05\u7A7A", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        if (target != null) target.setText("");
                        if (callback != null) callback.onResult("", "");
                    }
                })
                .setNeutralButton("\u53D6\u6D88", null)
                .create();

        dialog.show();
    }
}
