package com.xd.adhocroute.route;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
/**
 * 支持判断是否是float类型
 * @author qhyuan1992
 *
 */
public class FloatEditPreference extends EditTextPreference {
	public FloatEditPreference(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public FloatEditPreference(Context context, AttributeSet attrs) { super(context, attrs); }
	public FloatEditPreference(Context context) { super(context); }

	@Override
	protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
		editText.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
		super.onAddEditTextToDialogView(dialogView, editText);
	}
	public boolean floatCheck(String text) {
		if (text.isEmpty()) return true;
        if (text != null && !text.isEmpty()) {
            String regex = "^[-\\+]?[.\\d]*$";
            if (text.matches(regex)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
	public  boolean validate(String addr) {
		return floatCheck(addr);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			String addr = getEditText().getText().toString();
			if (!validate(addr)) {
				Toast.makeText(getContext(), "格式不正确", Toast.LENGTH_LONG).show();
				positiveResult = false;
			}
		}
		super.onDialogClosed(positiveResult);
	}
}
