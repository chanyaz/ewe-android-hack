package com.expedia.bookings.section;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.widget.ArrayAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightPassenger.AssistanceType;

public class AssistanceTypeSpinnerAdapter extends ArrayAdapter<String> {
	private static Map<String, AssistanceType> ASSISTANCE_VALUES = new HashMap<String, AssistanceType>();

	private String[] mAssistanceTypeNames;

	public AssistanceTypeSpinnerAdapter(Context context) {
		super(context, R.layout.simple_spinner_item);

		final Resources res = context.getResources();
		mAssistanceTypeNames = res.getStringArray(R.array.assistance_preferences);
		setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

		fillAssistanceTypes(context);
	}

	@Override
	public int getCount() {
		return mAssistanceTypeNames.length;
	}

	@Override
	public String getItem(int position) {
		return mAssistanceTypeNames[position];
	}

	public AssistanceType getAssistanceTypeValue(int position) {
		AssistanceType retVal = AssistanceType.NONE;
		if (ASSISTANCE_VALUES.containsKey(mAssistanceTypeNames[position])) {
			retVal = ASSISTANCE_VALUES.get(mAssistanceTypeNames[position]);
		}
		return retVal;
	}

	private void fillAssistanceTypes(Context context) {
		final Resources res = context.getResources();

		ASSISTANCE_VALUES.put(res.getString(R.string.none), AssistanceType.NONE);
		ASSISTANCE_VALUES.put(res.getString(R.string.wheelchair), AssistanceType.WHEELCHAIR);
		ASSISTANCE_VALUES.put(res.getString(R.string.defibrillator), AssistanceType.DEFIBRILLATOR);
		ASSISTANCE_VALUES.put(res.getString(R.string.super_long_assistance), AssistanceType.SUPER_LONG_ASSISTANCE_TYPE);
		
	}
}