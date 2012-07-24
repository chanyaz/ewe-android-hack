package com.expedia.bookings.section;

import android.content.Context;
import android.content.res.Resources;
import android.widget.ArrayAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.LocaleUtils;

public class CountrySpinnerAdapter extends ArrayAdapter<String> {
	private CountryNameData[] mCountries;
	private CountryDisplayType mDisplayType;

	private Context mContext;

	public enum CountryDisplayType {
		FULL_NAME,
		TWO_LETTER,
		THREE_LETTER
	}

	public CountrySpinnerAdapter(Context context, CountryDisplayType displayType) {
		super(context, R.layout.simple_spinner_item);

		mContext = context;

		setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		setDisplayType(displayType, context);
	}

	private void setDisplayType(CountryDisplayType displayType, Context context) {
		mDisplayType = displayType;

		final Resources res = context.getResources();
		String[] countryNames = res.getStringArray(R.array.country_names);
		String[] twoLetterCountryCodes = res.getStringArray(R.array.country_codes);
		String[] threeLetterCountryCodes = new String[twoLetterCountryCodes.length];
		for (int i = 0; i < twoLetterCountryCodes.length; i++) {
			threeLetterCountryCodes[i] = LocaleUtils.convertCountryCode(twoLetterCountryCodes[i]);
		}

		mCountries = new CountryNameData[countryNames.length];
		for (int i = 0; i < countryNames.length; i++) {
			mCountries[i] = new CountryNameData(countryNames[i], twoLetterCountryCodes[i], threeLetterCountryCodes[i]);
		}

	}

	@Override
	public int getCount() {
		return mCountries.length;
	}

	@Override
	public String getItem(int position) {
		return mCountries[position].getValue(mDisplayType);
	}

	public String getItemValue(int position, CountryDisplayType displayType) {
		return mCountries[position].getValue(displayType);
	}

	private class CountryNameData {
		String mName;
		String mTwoLetter;
		String mThreeLetter;

		public CountryNameData(String name, String twoLetter, String threeLetter) {
			mName = name;
			mTwoLetter = twoLetter;
			mThreeLetter = threeLetter;
		}

		public String getValue(CountryDisplayType type) {

			switch (type) {
			case THREE_LETTER:
				return mThreeLetter;
			case TWO_LETTER:
				return mTwoLetter;
			case FULL_NAME:
				return mName;
			default:
				return mName;

			}
		}
	}

	public int getDefaultLocalePosition() {
		String defaultCountry = mContext.getString(LocaleUtils.getDefaultCountryResId(mContext));
		for (int i = 0; i < this.getCount(); i++) {
			if (this.getItemValue(i, CountryDisplayType.FULL_NAME).equalsIgnoreCase(defaultCountry)) {
				return i;
			}
		}
		return 0;
	}
}