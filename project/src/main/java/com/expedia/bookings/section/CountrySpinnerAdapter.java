package com.expedia.bookings.section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.utils.SpannableBuilder;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;

public class CountrySpinnerAdapter extends BaseAdapter {
	private List<CountryNameData> mCountries;
	private CountryDisplayType mDisplayType;

	private Context mContext;
	private int mItemResId = View.NO_ID;
	private int mDropDownResId = View.NO_ID;
	private boolean mShowEmptyRow = false;
	private String prefix;
	private boolean isColoredPrefix = true;
	private boolean hasError = false;

	public enum CountryDisplayType {
		FULL_NAME,
		TWO_LETTER,
		THREE_LETTER
	}

	public CountrySpinnerAdapter(Context context, CountryDisplayType displayType) {
		this(context, displayType, R.layout.simple_spinner_item);
	}

	public CountrySpinnerAdapter(Context context, CountryDisplayType displayType, int itemResId) {
		this(context, displayType, itemResId, View.NO_ID, false);
	}

	public CountrySpinnerAdapter(Context context, CountryDisplayType displayType, int itemResId, int dropdownresource,
			boolean showEmptyRow) {
		super();
		init(context, displayType, itemResId, dropdownresource, showEmptyRow);
	}

	private void init(Context context, CountryDisplayType displayType, int itemResId, int dropDownResId,
		boolean showEmptyRow) {
		mContext = context;
		mItemResId = itemResId;
		mDropDownResId = dropDownResId;
		mShowEmptyRow = showEmptyRow;
		setDisplayType(displayType);
	}

	protected void setDisplayType(CountryDisplayType displayType) {
		mDisplayType = displayType;

		final Resources res = mContext.getResources();
		String[] countryNames = res.getStringArray(R.array.country_names);
		String[] twoLetterCountryCodes = res.getStringArray(R.array.country_codes);
		String[] threeLetterCountryCodes = new String[twoLetterCountryCodes.length];
		for (int i = 0; i < twoLetterCountryCodes.length; i++) {
			threeLetterCountryCodes[i] = LocaleUtils.convertCountryCode(twoLetterCountryCodes[i]);
		}
		mCountries = buildCountriesDataSet(countryNames, twoLetterCountryCodes, threeLetterCountryCodes);

		CountryNameDataComparator comparator = new CountryNameDataComparator(displayType);
		Collections.sort(mCountries, comparator);
		if (mShowEmptyRow) {
			mCountries.add(0, new CountryNameData(mContext.getResources().getString(R.string.country), "", ""));
			mCountries.add(1, new CountryNameData(mContext.getResources()
				.getString(PointOfSale.getPointOfSale().getCountryNameResId()), PointOfSale.getPointOfSale().getTwoLetterCountryCode(), PointOfSale.getPointOfSale().getThreeLetterCountryCode()));
		}
	}

	public void showPosAsFirstCountry() {
		mCountries.add(0, new CountryNameData(mContext.getResources()
			.getString(PointOfSale.getPointOfSale().getCountryNameResId()), PointOfSale.getPointOfSale().getTwoLetterCountryCode(), PointOfSale.getPointOfSale().getThreeLetterCountryCode()));
	}

	protected List<CountryNameData> buildCountriesDataSet(String[] countryNames, String[] twoLetterCountryCodes,
		String[] threeLetterCountryCodes) {
		List<CountryNameData> mCountries = new ArrayList<CountryNameData>();
		for (int i = 0; i < countryNames.length; i++) {
			mCountries.add(new CountryNameData(countryNames[i], twoLetterCountryCodes[i],
				threeLetterCountryCodes[i]));
		}

		return mCountries;
	}

	private void setCountryList(List<String> countryCodes) {
		List<CountryNameData> countryList = new ArrayList();
		for (CountryNameData countryNameData: mCountries) {
			if (countryCodes.contains(countryNameData.mTwoLetter)) {
				countryList.add(countryNameData);
			}
		}
		mCountries = countryList;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setColoredPrefix(boolean coloredPrefix) {
		isColoredPrefix = coloredPrefix;
	}

	@Override
	public int getCount() {
		return mCountries.size();
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
		View viewImpl = getViewImpl(
			mShowEmptyRow || usePrefix() ? getFormattedText(getItem(position)) : getItem(position),
			convertView, parent, mItemResId);
		TextView tv = Ui.findView(viewImpl, android.R.id.text1);

		boolean noCountrySelected = (position == 0);
		if (mShowEmptyRow && hasError) {
			drawErrorIcon(viewImpl, noCountrySelected);
		}
		if (!noCountrySelected) {
			tv.setTextColor(ContextCompat.getColor(tv.getContext(), R.color.black));
		}

		return viewImpl;
	}

	private CharSequence getFormattedText(String text) {
		SpannableBuilder span = new SpannableBuilder();
		String formatString = prefix;
		if (Strings.isEmpty(formatString)) {
			formatString = mContext.getResources().getString(R.string.passport_country);
		}
		if (isColoredPrefix) {
			span.append(formatString, new ForegroundColorSpan(0xFF808080));
		}
		else {
			span.append(formatString);
		}
		span.append(" ");
		span.append(text);
		return span.build();
	}

	private void drawErrorIcon(View view, boolean noCountrySelected) {
		android.widget.TextView textView = ((CountrySpinnerAdapter.ViewHolder) view.getTag()).text;
		if (noCountrySelected) {
			Drawable errorIcon = ContextCompat.getDrawable(mContext,
				Ui.obtainThemeResID(mContext, R.attr.skin_errorIndicationExclaimationDrawable));
			errorIcon.setBounds(new Rect(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight()));
			Drawable[] compounds = textView.getCompoundDrawables();
			textView.setCompoundDrawablesWithIntrinsicBounds(compounds[0], compounds[1], errorIcon, compounds[3]);
		}
		else {
			Drawable[] compounds = textView.getCompoundDrawables();
			textView.setCompoundDrawablesWithIntrinsicBounds(compounds[0], compounds[1], null, compounds[3]);
		}
	}

	private boolean usePrefix() {
		return Strings.isNotEmpty(prefix);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		if (mDropDownResId == View.NO_ID && !mShowEmptyRow) {
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
		public final TextView text;

		public ViewHolder(View view) {
			text = Ui.findView(view, android.R.id.text1);
		}
	}

	public String getItemValue(int position, CountryDisplayType displayType) {
		return mCountries.get(position).getValue(displayType);
	}

	public int getPositionByCountryThreeLetterCode(String countryCode) {
		for (int i = 0; i < mCountries.size(); i++) {
			if (mCountries.get(i).mThreeLetter.compareToIgnoreCase(countryCode) == 0) {
				return i;
			}
		}
		return -1;
	}

	public static class CountryNameData {
		final String mName;
		final String mTwoLetter;
		final String mThreeLetter;

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

		private final CountryDisplayType mDisplayType;

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

	public void setErrorVisible(boolean hasError) {
		this.hasError = hasError;
		notifyDataSetChanged();
	}

	public void dataSetChanged(List<String> countryCodes) {
		setCountryList(countryCodes);
		notifyDataSetChanged();
	}
}
