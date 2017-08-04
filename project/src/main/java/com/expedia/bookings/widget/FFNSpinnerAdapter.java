package com.expedia.bookings.widget;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.util.Ui;

public class FFNSpinnerAdapter extends ArrayAdapter<String> {
	private static final Map<String, Integer> FFNAirlines = new HashMap<String, Integer>();

	private int[] FFNnumber;
	private String[] airlinePrograms;
	private int CurrentPosition;

	public FFNSpinnerAdapter(Context context) {
		this(context, R.layout.simple_spinner_item);
	}

	public FFNSpinnerAdapter(Context context, int textViewResourceId) {
		this(context, textViewResourceId, R.layout.simple_spinner_dropdown_item);
	}

	public FFNSpinnerAdapter(Context context, int textViewResId, int dropDownViewResId) {
		super(context, textViewResId);
		setDropDownViewResource(dropDownViewResId);
		init(context);
	}

	private void init(Context context) {
		final Resources res = context.getResources();
		FFNnumber = res.getIntArray(R.array.ffn_number);
		airlinePrograms = res.getStringArray(R.array.ffn_programs);
		fillAirlines(context);
	}

	@Override
	public int getCount() {
		return FFNnumber.length;
	}

	@Override
	public String getItem(int position) {
		return String.format(Locale.getDefault(), "%s (%d)", getAirlineName(position), getFFNNumber(position));
	}

	public String getAirlineName(int position) {
		return airlinePrograms[position];
	}

	public int getFFNNumber(int position) {
		if (FFNAirlines.containsKey(getAirlineName(position))) {
			return FFNAirlines.get(getAirlineName(position));
		}

		return FFNnumber[position];
	}

	public int getCountryCodeFromCountryName(String countryName) {
		return FFNAirlines.get(countryName);
	}

	public int getPositionFromName(String countryName) {
		if (Strings.isEmpty(countryName)) {
			return CurrentPosition;
		}
		for (int i = 0; i < FFNAirlines.size(); i++) {
			if (getAirlineName(i).equals(countryName)) {
				return i;
			}
		}
		return CurrentPosition;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View retView = super.getView(position, convertView, parent);
		android.widget.TextView tv = Ui.findView(retView, android.R.id.text1);
		CharSequence item = getItem(position);
		Spannable stringToSpan = new SpannableString(String.format(getItem(position), item));
		tv.setText(stringToSpan);
		TextViewExtensions.Companion.setTextColorBasedOnPosition(tv, CurrentPosition, position);

		return retView;
	}

	public void setCurrentPosition(int position) {
		CurrentPosition = position;
	}

	public int getCurrentPosition() {
		return CurrentPosition;
	}

	private void fillAirlines(Context context) {
		final Resources res = context.getResources();
		FFNAirlines.put("Airline A", 0001);
		FFNAirlines.put("Airline B", 0002);
		FFNAirlines.put("Airline C", 0003);
		FFNAirlines.put("Airline D", 0004);
		FFNAirlines.put("Airline E", 0005);
	}

}
