package com.expedia.bookings.section;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.widget.ArrayAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightPassenger.SeatPreference;

public class SeatPreferenceSpinnerAdapter extends ArrayAdapter<String> {
	private static Map<String, SeatPreference> SEAT_PREF_VALUES = new HashMap<String, SeatPreference>();

	private String[] mSeatPrefNames;

	public SeatPreferenceSpinnerAdapter(Context context) {
		super(context, R.layout.simple_spinner_item);

		final Resources res = context.getResources();
		mSeatPrefNames = res.getStringArray(R.array.seat_preferences);
		setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

		fillSeatPreferences(context);
	}

	@Override
	public int getCount() {
		return mSeatPrefNames.length;
	}

	@Override
	public String getItem(int position) {
		return mSeatPrefNames[position];
	}

	public SeatPreference getSeatPreferenceValue(int position) {
		SeatPreference retVal = SeatPreference.ANY;
		if (SEAT_PREF_VALUES.containsKey(mSeatPrefNames[position])) {
			retVal = SEAT_PREF_VALUES.get(mSeatPrefNames[position]);
		}
		return retVal;
	}

	private void fillSeatPreferences(Context context) {
		final Resources res = context.getResources();

		SEAT_PREF_VALUES.put(res.getString(R.string.any), SeatPreference.ANY);
		SEAT_PREF_VALUES.put(res.getString(R.string.aisle), SeatPreference.AISLE);
		SEAT_PREF_VALUES.put(res.getString(R.string.window), SeatPreference.WINDOW);
	}
}