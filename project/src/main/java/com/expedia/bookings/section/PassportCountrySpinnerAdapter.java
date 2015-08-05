package com.expedia.bookings.section;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
		View viewImpl = getViewImpl(span.build(), convertView, parent, RES_ID);

		boolean noCountrySelected = (position == 0);
		if (noCountrySelected) { // draw error warning icon
			android.widget.TextView textView = ((CountrySpinnerAdapter.ViewHolder) viewImpl.getTag()).text;
			Drawable errorIcon = textView.getContext().getResources().getDrawable(R.drawable.ic_error_blue);
			errorIcon.setBounds(new Rect(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight()));
			Drawable[] compounds = textView.getCompoundDrawables();
			textView.setCompoundDrawablesWithIntrinsicBounds(compounds[0], compounds[1], errorIcon, compounds[3]);
		}

		return viewImpl;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getViewImpl(getItem(position), convertView, parent, RES_ID);
	}

	@Override
	protected CountrySpinnerAdapter.CountryNameData[] buildCountriesDataSet(String[] countryNames,
		String[] twoLetterCountryCodes,
		String[] threeLetterCountryCodes) {
		int countriesLength = countryNames.length + 1;
		CountryNameData[] mCountries = new CountryNameData[countriesLength];
		// create empty country. This will be the unselected (index:0) item
		mCountries[0] = new CountryNameData("", "", "");
		for (int i = 1; i < countriesLength; i++) {
			mCountries[i] = new CountryNameData(countryNames[i - 1], twoLetterCountryCodes[i - 1], threeLetterCountryCodes[i - 1]);
		}

		return mCountries;
	}
}
