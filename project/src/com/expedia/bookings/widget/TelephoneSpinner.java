package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

public class TelephoneSpinner extends Spinner {
	public TelephoneSpinner(Context context) {
		super(context);
		setAdapter(new TelephoneSpinnerAdapter(context));
	}

	public TelephoneSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		setAdapter(new TelephoneSpinnerAdapter(context));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		View child = getChildAt(0);
		if (child instanceof TextView) {
			final int code = getSelectedTelephoneCountryCode();
			((TextView) child).setText(String.format("+%d", code));
		}
	}

	public int getSelectedTelephoneCountryCode() {
		return ((TelephoneSpinnerAdapter) getAdapter()).getCountryCode(getSelectedItemPosition());
	}
}