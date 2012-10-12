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
		init(context, displayType, R.layout.simple_spinner_dropdown_item);
	}

	public CountrySpinnerAdapter(Context context, CountryDisplayType displayType, int resource) {
		super(context, resource);
		init(context, displayType, resource);
	}

	private void init(Context context, CountryDisplayType displayType, int dropDownResId) {
		mContext = context;
		setDropDownViewResource(dropDownResId);
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

	public int getPositionByCountryName(String countryName) {
		for (int i = 0; i < mCountries.length; i++) {
			if (mCountries[i].mName.compareToIgnoreCase(countryName) == 0) {
				return i;
			}
		}
		return -1;
	}

	public int getPositionByCountryThreeLetterCode(String countryCode) {
		for (int i = 0; i < mCountries.length; i++) {
			if (mCountries[i].mThreeLetter.compareToIgnoreCase(countryCode) == 0) {
				return i;
			}
		}
		return -1;
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