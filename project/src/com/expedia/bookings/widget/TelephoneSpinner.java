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
			((TextView) child).setText(String.format("+%d", getSelectedTelephoneCountryCode()));
		}
	}

	public int getSelectedTelephoneCountryCode() {
		int position = getSelectedItemPosition();
		if (position == AdapterView.INVALID_POSITION) {
			return AdapterView.INVALID_POSITION;
		}
		return ((TelephoneSpinnerAdapter) getAdapter()).getCountryCode(position);
	}

	public String getSelectedTelephoneCountry() {
		int position = getSelectedItemPosition();
		if (position == AdapterView.INVALID_POSITION) {
			return null;
		}
		return ((TelephoneSpinnerAdapter) getAdapter()).getCountryName(position);
	}
}