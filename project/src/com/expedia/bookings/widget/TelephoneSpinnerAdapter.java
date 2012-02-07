package com.expedia.bookings.widget;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.widget.ArrayAdapter;

import com.expedia.bookings.R;

public class TelephoneSpinnerAdapter extends ArrayAdapter<String> {
	private static Map<String, Integer> COUNTRY_CODES = new HashMap<String, Integer>();

	private int[] mCountryPhoneCodes;
	private String[] mCountryNames;

	public TelephoneSpinnerAdapter(Context context) {
		super(context, android.R.layout.simple_spinner_item);

		final Resources res = context.getResources();
		mCountryPhoneCodes = res.getIntArray(R.array.country_phone_codes);
		mCountryNames = res.getStringArray(R.array.country_names);

		setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		fillCountryCodes(context);
	}

	@Override
	public int getCount() {
		return mCountryPhoneCodes.length;
	}

	@Override
	public String getItem(int position) {
		return mCountryNames[position];
	}

	public int getCountryCode(int position) {
		if (COUNTRY_CODES.containsKey(getItem(position))) {
			return COUNTRY_CODES.get(getItem(position));
		}

		return mCountryPhoneCodes[position];
	}

	private void fillCountryCodes(Context context) {
		final Resources res = context.getResources();

		COUNTRY_CODES.put(context.getString(R.string.country_af), res.getInteger(R.integer.country_phone_code_af));
	}
}