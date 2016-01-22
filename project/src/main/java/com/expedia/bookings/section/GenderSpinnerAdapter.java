package com.expedia.bookings.section;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler.Gender;
import com.mobiata.android.util.Ui;

public class GenderSpinnerAdapter extends ArrayAdapter<CharSequence> {

	class GenderSpinnerHelper {
		Gender mGender;
		String mGenderStr;

		public GenderSpinnerHelper(Gender gender, String genderStr) {
			setGender(gender);
			setGenderString(genderStr);
		}

		public void setGenderString(String gender) {
			mGenderStr = gender;
		}
		public String getGenderString() {
			return mGenderStr;
		}

		public void setGender(Gender gender) {
			mGender = gender;
		}
		public Gender getGender() {
			return mGender;
		}
	}

	private ArrayList<GenderSpinnerHelper> mGenders;
	private static final String DEFAULT_FORMAT_STRING = "%s";
	private String mFormatString = DEFAULT_FORMAT_STRING;

	public GenderSpinnerAdapter(Context context) {
		this(context, R.layout.simple_spinner_traveler_item);
	}

	public GenderSpinnerAdapter(Context context, int textViewId) {
		super(context, textViewId);
		setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		populateGenders(context);
	}

	private void populateGenders(Context context) {
		Resources res = context.getResources();
		mGenders = new ArrayList<GenderSpinnerHelper>();
		mGenders.add(new GenderSpinnerHelper(Gender.MALE, res.getString(R.string.male)));
		mGenders.add(new GenderSpinnerHelper(Gender.FEMALE, res.getString(R.string.female)));
	}

	public void setFormatString(String formatString) {
		mFormatString = formatString;
	}

	public void resetFormatString() {
		mFormatString = DEFAULT_FORMAT_STRING;
	}

	@Override
	public int getCount() {
		return mGenders.size();
	}

	@Override
	public CharSequence getItem(int position) {
		return mGenders.get(position).getGenderString();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View retView = super.getView(position, convertView, parent);
		TextView tv = Ui.findView(retView, android.R.id.text1);
		tv.setEllipsize(TruncateAt.START); //If we have a long name, we want to make sure atleast the Gender is displayed
		tv.setText(Html.fromHtml(String.format(mFormatString, getItem(position))));
		return retView;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View retView = super.getDropDownView(position, convertView, parent);
		//TODO: we should really probably set the formatting here
		return retView;
	}

	public Gender getGender(int position) {
		return mGenders.get(position).getGender();
	}

	public int getGenderPosition(Gender gender) {
		if (gender == null) {
			return -1;
		}

		for (int i = 0; i < mGenders.size(); i++) {
			if (mGenders.get(i).getGender() == gender) {
				return i;
			}
		}
		return -1;
	}

}
