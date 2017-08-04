package com.expedia.bookings.widget;

import java.util.Locale;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.*;

import com.expedia.bookings.R;

public class FFNSpinner extends Spinner {

	public FFNSpinner(Context context) {
		super(context);
		setAdapter(new TelephoneSpinnerAdapter(context));
	}

	public FFNSpinner(Context context, AttributeSet attrs) {
		super(context, attrs, 0);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FFNSpinner);
		int textLayout = a.getResourceId(R.styleable.FFNSpinner_ffn_text_view_layout,
			R.layout.simple_spinner_item);
		int dropDownView = a.getResourceId(R.styleable.FFNSpinner_ffn_drop_down_view,
			R.layout.simple_spinner_dropdown_item);

		setAdapter(new FFNSpinnerAdapter(context, textLayout, dropDownView));

		a.recycle();
	}
	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		updateText();
	}

	public void updateText() {
		View child = getChildAt(0);
		if (child instanceof android.widget.TextView) {
			((android.widget.TextView) child).setText(String.format(Locale.getDefault(), "+%d", getSelectedAirline()));
		}
	}

	public void update(String airline) {
		FFNSpinnerAdapter adapter = (FFNSpinnerAdapter) getAdapter();
		for (int i = 0; i < adapter.getCount() - 1; i++) {
			if (airline.equalsIgnoreCase(adapter.getAirlineName(i))) {
				setSelection(i);
			}
		}
	}

	public String getSelectedAirline() {
		int position = getSelectedItemPosition();
		if (position == AdapterView.INVALID_POSITION) {
			return null;
		}
		return ((FFNSpinnerAdapter) getAdapter()).getAirlineName(position);
	}
}
