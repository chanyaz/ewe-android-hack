package com.expedia.bookings.section;

import android.content.Context;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.SpannableBuilder;

public class PassportCountrySpinnerAdapter extends CountrySpinnerAdapter {
	private static final int RES_ID = android.R.layout.simple_list_item_1;

	public PassportCountrySpinnerAdapter(Context context) {
		super(context, CountrySpinnerAdapter.CountryDisplayType.FULL_NAME, RES_ID);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SpannableBuilder span = new SpannableBuilder();
		span.append(parent.getContext().getResources().getString(R.string.passport_country), new ForegroundColorSpan(0xFF808080));
		span.append(" ");
		span.append(getItem(position));
		return getViewImpl(span.build(), convertView, parent, RES_ID);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getViewImpl(getItem(position), convertView, parent, RES_ID);
	}
}
