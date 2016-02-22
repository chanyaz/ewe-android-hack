package com.expedia.bookings.widget;

import java.util.Locale;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.mobiata.android.widget.Spinner;

public class TelephoneSpinner extends Spinner {

	public TelephoneSpinner(Context context) {
		super(context);
		setAdapter(new TelephoneSpinnerAdapter(context));
		selectPOSCountry();
	}

	public TelephoneSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TelephoneSpinner);
		int textLayout = a.getResourceId(R.styleable.TelephoneSpinner_text_view_layout,
			R.layout.simple_spinner_item);
		int dropDownView = a.getResourceId(R.styleable.TelephoneSpinner_drop_down_view,
			R.layout.simple_spinner_dropdown_item);

		setAdapter(new TelephoneSpinnerAdapter(context, textLayout, dropDownView));

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
		if (child instanceof TextView) {
			((TextView) child).setText(String.format(Locale.getDefault(), "+%d", getSelectedTelephoneCountryCode()));
		}
	}

	public void update(String countryCode, String countryName) {
		TelephoneSpinnerAdapter adapter = (TelephoneSpinnerAdapter) getAdapter();
		for (int i = 0; i < adapter.getCount() - 1; i++) {
			if (countryCode.equalsIgnoreCase("" + adapter.getCountryCode(i)) && (TextUtils.isEmpty(countryName)
				|| countryName.equalsIgnoreCase(adapter.getCountryName(i)))) {
				setSelection(i);
			}
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

	public void selectPOSCountry() {
		TelephoneSpinnerAdapter adapter = (TelephoneSpinnerAdapter) getAdapter();
		String targetCountry = getContext().getString(PointOfSale.getPointOfSale()
			.getCountryNameResId());
		for (int i = 0; i < adapter.getCount(); i++) {
			if (targetCountry.equalsIgnoreCase(adapter.getCountryName(i))) {
				setSelection(i);
				break;
			}
		}
	}
}
