package com.expedia.bookings.section;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler.SeatPreference;
import com.expedia.bookings.extensions.TextViewExtensions;
import com.mobiata.android.util.Ui;

public class SeatPreferenceSpinnerAdapter extends ArrayAdapter<CharSequence> {
	Integer color;
	private int mCurrentPosition;
	private int mDropdownResourceId;

	class SeatPreferenceSpinnerHelper {
		SeatPreference mSeatPreference;
		String mSeatPreferenceStr;


		public SeatPreferenceSpinnerHelper(SeatPreference seatPreference, String seatPreferenceStr) {
			setSeatPreference(seatPreference);
			setSeatPreferenceString(seatPreferenceStr);
		}

		public void setSeatPreferenceString(String seatPreference) {
			mSeatPreferenceStr = seatPreference;
		}

		public String getSeatPreferenceString() {
			return mSeatPreferenceStr;
		}

		public void setSeatPreference(SeatPreference seatPreference) {
			mSeatPreference = seatPreference;
		}

		public SeatPreference getSeatPreference() {
			return mSeatPreference;
		}
	}

	private ArrayList<SeatPreferenceSpinnerHelper> mSeatPreferences;
	private String mFormatString = "%s";

	public SeatPreferenceSpinnerAdapter(Context context) {
		this(context, R.layout.simple_spinner_traveler_item);
	}

	public SeatPreferenceSpinnerAdapter(Context context, int textViewId) {
		this(context, textViewId, R.layout.simple_spinner_dropdown_item);
	}

	public SeatPreferenceSpinnerAdapter(Context context, int textViewId, int dropDownResource) {
		super(context, textViewId);
		mDropdownResourceId = dropDownResource;
		setDropDownViewResource(dropDownResource);
		fillSeatPreferences(context);
	}

	public void setFormatString(String formatString) {
		mFormatString = formatString;
	}

	public void setSpanColor(int colorId) {
		this.color = ContextCompat.getColor(getContext(), colorId);
	}

	@Override
	public int getCount() {
		return mSeatPreferences.size();
	}

	@Override
	public CharSequence getItem(int position) {
		return mSeatPreferences.get(position).getSeatPreferenceString();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View retView = super.getView(position, convertView, parent);
		TextView tv = Ui.findView(retView, android.R.id.text1);
		CharSequence item = getItem(position);
		Spannable stringToSpan = new SpannableString(String.format(mFormatString, item));
		if (color != null) {
			com.expedia.bookings.utils.Ui.setTextStyleNormalText(stringToSpan, color, 0,
				stringToSpan.toString().indexOf(item.toString()));
		}
		tv.setText(stringToSpan);
		TextViewExtensions.Companion.setTextColorBasedOnPosition(tv, mCurrentPosition, position);

		return retView;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View retView;
		if (convertView == null) {
			retView = Ui.inflate(mDropdownResourceId, parent, false);
		}
		else {
			retView = convertView;
		}

		TextView tv = Ui.findView(retView, android.R.id.text1);
		Context context = tv.getContext();
		tv.setText(getItem(position));
		int textColor = ContextCompat.getColor(context, R.color.default_text_color);
		if (position == mCurrentPosition) {
			textColor = ContextCompat.getColor(context, com.expedia.bookings.utils.Ui.obtainThemeResID(context, R.attr.primary_color));
		}
		tv.setTextColor(textColor);

		return retView;
	}

	public SeatPreference getSeatPreference(int position) {
		return mSeatPreferences.get(position).getSeatPreference();
	}

	public int getSeatPreferencePosition(SeatPreference seatPreference) {
		if (seatPreference == null) {
			return -1;
		}

		for (int i = 0; i < mSeatPreferences.size(); i++) {
			if (mSeatPreferences.get(i).getSeatPreference() == seatPreference) {
				return i;
			}
		}
		return -1;
	}

	private void fillSeatPreferences(Context context) {
		final Resources res = context.getResources();
		mSeatPreferences = new ArrayList<SeatPreferenceSpinnerHelper>();
		mSeatPreferences.add(new SeatPreferenceSpinnerHelper(SeatPreference.AISLE, res.getString(R.string.aisle)));
		mSeatPreferences.add(new SeatPreferenceSpinnerHelper(SeatPreference.WINDOW, res.getString(R.string.window)));
	}

	public void setCurrentPosition(int position) {
		mCurrentPosition = position;
	}

	public int getCurrentPosition() {
		return mCurrentPosition;
	}
}
