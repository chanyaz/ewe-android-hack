package com.expedia.bookings.section;

import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.utils.Ui;

public class CountrySpinnerAdapter extends BaseAdapter {
	private CountryNameData[] mCountries;
	private CountryDisplayType mDisplayType;

	private Context mContext;
	private int mItemResId = View.NO_ID;
	private int mDropDownResId = View.NO_ID;

	public enum CountryDisplayType {
		FULL_NAME,
		TWO_LETTER,
		THREE_LETTER
	}

	public CountrySpinnerAdapter(Context context, CountryDisplayType displayType) {
		this(context, displayType, R.layout.simple_spinner_item);
	}

	public CountrySpinnerAdapter(Context context, CountryDisplayType displayType, int itemResId) {
		this(context, displayType, itemResId, View.NO_ID);
	}

	public CountrySpinnerAdapter(Context context, CountryDisplayType displayType, int itemResId, int dropdownresource) {
		super();
		init(context, displayType, itemResId, dropdownresource);
	}

	private void init(Context context, CountryDisplayType displayType, int itemResId, int dropDownResId) {
		mContext = context;
		mItemResId = itemResId;
		mDropDownResId = dropDownResId;
		setDisplayType(displayType);
	}

	private void setDisplayType(CountryDisplayType displayType) {
		mDisplayType = displayType;

		final Resources res = mContext.getResources();
		String[] countryNames = res.getStringArray(R.array.country_names);
		String[] twoLetterCountryCodes = res.getStringArray(R.array.country_codes);
		String[] threeLetterCountryCodes = new String[twoLetterCountryCodes.length];
		for (int i = 0; i < twoLetterCountryCodes.length; i++) {
			threeLetterCountryCodes[i] = LocaleUtils.convertCountryCode(twoLetterCountryCodes[i]);
		}

		int countriesLength = countryNames.length + 1;
		mCountries = new CountryNameData[countriesLength];
		mCountries[0] = new CountryNameData("", "", "");
		for (int i = 1; i < countriesLength; i++) {
			mCountries[i] = new CountryNameData(countryNames[i - 1], twoLetterCountryCodes[i - 1], threeLetterCountryCodes[i - 1]);
		}

		CountryNameDataComparator comparator = new CountryNameDataComparator(displayType);
		Arrays.sort(mCountries, comparator);
	}

	@Override
	public int getCount() {
		return mCountries.length;
	}

	@Override
	public String getItem(int position) {
		return getItemValue(position, mDisplayType);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getViewImpl(getItem(position), convertView, parent, mItemResId);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		if (mDropDownResId == View.NO_ID) {
			return getView(position, convertView, parent);
		}

		return getViewImpl(getItem(position), convertView, parent, mDropDownResId);
	}

	protected View getViewImpl(CharSequence text, View convertView, ViewGroup parent, int layoutId) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = Ui.inflate(layoutId, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.text.setText(text);
		return convertView;
	}

	public static class ViewHolder {
		public TextView text;

		public ViewHolder(View view) {
			text = Ui.findView(view, android.R.id.text1);
		}
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

	private static class CountryNameData {
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
			default:
				return mName;

			}
		}
	}

	private static class CountryNameDataComparator implements Comparator<CountryNameData> {

		private CountryDisplayType mDisplayType;

		public CountryNameDataComparator(CountryDisplayType displayType) {
			mDisplayType = displayType;
		}

		@Override
		public int compare(CountryNameData one, CountryNameData two) {
			return one.getValue(mDisplayType).compareTo(two.getValue(mDisplayType));
		}

	}

	public int getDefaultLocalePosition() {
		String defaultCountry = mContext.getString(PointOfSale.getPointOfSale().getCountryNameResId());
		for (int i = 0; i < this.getCount(); i++) {
			if (this.getItemValue(i, CountryDisplayType.FULL_NAME).equalsIgnoreCase(defaultCountry)) {
				return i;
			}
		}
		return 0;
	}
}
