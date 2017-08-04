package com.expedia.bookings.widget;

import java.util.Locale;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.*;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;

public class FFNSpinner extends Spinner {

	public FFNSpinner(Context context) {
		super(context);
		setAdapter(new FFNSpinnerAdapter(context));
		selectPOSCountry();
	}

	public FFNSpinner(Context context, AttributeSet attrs) {
		super(context, attrs, 0);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FFNSpinner);
		int textLayout = a.getResourceId(R.styleable.FFNSpinner_ffn_text_view_layout,
			R.layout.simple_spinner_item);
		int dropDownView = a.getResourceId(R.styleable.FFNSpinner_ffn_drop_down_view,
			R.layout.simple_spinner_dropdown_item);

		setAdapter(new FFNSpinnerAdapter(context, textLayout, dropDownView));
		setPrompt("My Programs");
		a.recycle();
		selectPOSCountry();
	}
	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		updateText();
	}

	public void updateText() {
		View child = getChildAt(0);
		if (child instanceof android.widget.TextView) {
			((TextView) child).setText(String.format(Locale.getDefault(), "+%d", getSelectedFFNnumber()));
		}
	}

	public void update(String FFNnumber, String airlineName) {
		FFNSpinnerAdapter adapter = (FFNSpinnerAdapter) getAdapter();
		for (int i = 0; i < adapter.getCount() - 1; i++) {
			if (FFNnumber.equalsIgnoreCase("" + adapter.getFFNNumber(i)) && (TextUtils.isEmpty(airlineName)
				|| airlineName.equalsIgnoreCase(adapter.getAirlineName(i)))) {
				setSelection(i);
			}
		}
	}

	public int getSelectedFFNnumber() {
		int position = getSelectedItemPosition();
		if (position == AdapterView.INVALID_POSITION) {
			return AdapterView.INVALID_POSITION;
		}
		return ((FFNSpinnerAdapter) getAdapter()).getFFNNumber(position);
	}

	public String getSelectedAirlineName() {
		int position = getSelectedItemPosition();
		if (position == AdapterView.INVALID_POSITION) {
			return null;
		}
		return ((FFNSpinnerAdapter) getAdapter()).getAirlineName(position);
	}

	public void selectPOSCountry() {
		FFNSpinnerAdapter adapter = (FFNSpinnerAdapter) getAdapter();
		String targetCountry = getContext().getString(PointOfSale.getPointOfSale()
			.getCountryNameResId());
		for (int i = 0; i < adapter.getCount(); i++) {
			if (targetCountry.equalsIgnoreCase(adapter.getAirlineName(i))) {
				setSelection(i);
				break;
			}
		}
	}

	@Override
	public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setText(getContext().getString(R.string.spinner_title));
	}
}
