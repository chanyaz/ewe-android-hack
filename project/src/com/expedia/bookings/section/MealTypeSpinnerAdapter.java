package com.expedia.bookings.section;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.widget.ArrayAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightPassenger.MealType;

public class MealTypeSpinnerAdapter extends ArrayAdapter<String> {
	private static Map<String, MealType> MEAL_VALUES = new HashMap<String, MealType>();

	private String[] mMealTypeNames;

	public MealTypeSpinnerAdapter(Context context) {
		super(context, R.layout.simple_spinner_item);

		final Resources res = context.getResources();
		mMealTypeNames = res.getStringArray(R.array.meal_preferences);
		setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

		fillMealTypes(context);
	}

	@Override
	public int getCount() {
		return mMealTypeNames.length;
	}

	@Override
	public String getItem(int position) {
		return mMealTypeNames[position];
	}

	public MealType getMealTypeValue(int position) {
		MealType retVal = MealType.NONE;
		if (MEAL_VALUES.containsKey(mMealTypeNames[position])) {
			retVal = MEAL_VALUES.get(mMealTypeNames[position]);
		}
		return retVal;
	}

	private void fillMealTypes(Context context) {
		final Resources res = context.getResources();

		MEAL_VALUES.put(res.getString(R.string.none), MealType.NONE);
		MEAL_VALUES.put(res.getString(R.string.vegetarian), MealType.VEGITARIAN);
		MEAL_VALUES.put(res.getString(R.string.booze), MealType.BOOZE);
	}
}