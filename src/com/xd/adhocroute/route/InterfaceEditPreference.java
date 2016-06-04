package com.xd.adhocroute.route;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.xd.adhocroute.AdhocRouteApp;
import com.xd.adhocroute.R;

/**
 * 支持判断网卡是否存在
 * @author qhyuan1992
 *
 */
public class InterfaceEditPreference extends EditTextPreference {
	public InterfaceEditPreference(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public InterfaceEditPreference(Context context, AttributeSet attrs) { super(context, attrs); }
	public InterfaceEditPreference(Context context) { super(context); }

	@Override
	protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
		super.onAddEditTextToDialogView(dialogView, editText);
	}
	public boolean interfaceCheck(String text) {
        if (text != null && !text.isEmpty()) {
        	return ((AdhocRouteApp)getContext().getApplicationContext()).coretask.networkInterfaceExists(text);
        }
        return false;
    }
	public  boolean validate(String addr) {
		return interfaceCheck(addr);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			String addr = getEditText().getText().toString();
			if (!validate(addr)) {
				Toast.makeText(getContext(), R.string.toast_interface_set_not_exist, Toast.LENGTH_SHORT).show();
				positiveResult = false;
			}
		}
		super.onDialogClosed(positiveResult);
	}
}
