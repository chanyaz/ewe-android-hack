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

public class FrequentFlyerSpinnerAdapter extends ArrayAdapter<String> {
	private static final Map<String, Integer> FrequentFlyerAirlines = new HashMap<>();

	private int[] frequentFlyerNumber;
	private String[] frequentFlyerProgram;
	private int CurrentPosition;

//	public FrequentFlyerSpinnerAdapter(Context context) {
//		this(context, R.layout.simple_spinner_item);
//	}

	public FrequentFlyerSpinnerAdapter(Context context, int textViewResourceId) {
		this(context, textViewResourceId, R.layout.simple_spinner_dropdown_item);
	}

	public FrequentFlyerSpinnerAdapter(Context context, int textViewResId, int dropDownViewResId) {
		super(context, textViewResId);
		setDropDownViewResource(dropDownViewResId);
		init(context);
	}

	private void init(Context context) {
		final Resources res = context.getResources();
		frequentFlyerNumber = res.getIntArray(R.array.ffn_number);
		frequentFlyerProgram = res.getStringArray(R.array.ffn_programs);
		fillAirlines();
	}

	@Override
	public int getCount() {
		return frequentFlyerNumber.length;
	}

	@Override
	public String getItem(int position) {
		return String.format(Locale.getDefault(), "%s Number: (%d)", getFrequentFlyerProgram(position), getFrequentFlyerNumber(position));
	}

	public String getFrequentFlyerProgram(int position) {
		return frequentFlyerProgram[position];
	}

	public int getFrequentFlyerNumber(int position) {
		if (FrequentFlyerAirlines.containsKey(getFrequentFlyerProgram(position))) {
			return FrequentFlyerAirlines.get(getFrequentFlyerProgram(position));
		}

		return frequentFlyerNumber[position];
	}
//
//	public int getFFNnumberFromAirlineName(String airlineName) {
//		return FrequentFlyerAirlines.get(airlineName);
//	}

	public int getPositionFromName(String airlineName) {
		if (Strings.isEmpty(airlineName)) {
			return CurrentPosition;
		}
		for (int i = 0; i < FrequentFlyerAirlines.size(); i++) {
			if (getFrequentFlyerProgram(i).equals(airlineName)) {
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

	private void fillAirlines() {
		FrequentFlyerAirlines.put("Airline A", 1234);
		FrequentFlyerAirlines.put("Airline B", 2345);
		FrequentFlyerAirlines.put("Airline C", 3456);
		FrequentFlyerAirlines.put("Airline D", 4567);
		FrequentFlyerAirlines.put("Airline E", 5678);
	}

}
