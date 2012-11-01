package com.expedia.bookings.widget;

import com.expedia.bookings.R;

import android.content.Context;
import android.content.res.TypedArray;
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

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TelephoneSpinner);
		int id = a.getResourceId(R.styleable.TelephoneSpinner_text_view_layout, -1);
		if (id > 0) {
			setAdapter(new TelephoneSpinnerAdapter(context, id));
		}
		else {
			setAdapter(new TelephoneSpinnerAdapter(context));
		}
		a.recycle();
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