package com.expedia.account.view;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityNodeInfo;

import com.expedia.account.R;

public class AccessibleEditText extends AppCompatEditText {
	public enum Status {
		DEFAULT,
		INVALID,
		VALID;
	}

	private Status status = Status.DEFAULT;
	private String errorContDesc;

	public AccessibleEditText(Context context) {
		super(context);
	}

	public AccessibleEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo nodeInfo) {
		super.onInitializeAccessibilityNodeInfo(nodeInfo);
		if (status == Status.INVALID) {
			nodeInfo.setText(getHint().toString() + " " + getText().toString() + ". " + getErrorContDesc());
		} else if (status == Status.VALID){
			nodeInfo.setText(getHint().toString() + " " + getText().toString() + ". "
				+ getContext().getString(R.string.acct__accessibility_cont_desc_role_complete));
		} else {
			nodeInfo.setText(getHint().toString() + " " + getText().toString());
		}
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setErrorContDesc(String errorContDesc) {
		this.errorContDesc = errorContDesc;
	}

	private String getErrorContDesc() {
		if (!TextUtils.isEmpty(errorContDesc)) {
			return errorContDesc;
		} else {
			return getContext().getString(R.string.acct__accessibility_cont_desc_role_error);
		}
	}
}
