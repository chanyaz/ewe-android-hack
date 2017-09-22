package com.expedia.bookings.section;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler.AssistanceType;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.widget.TextViewExtensions;
import com.mobiata.android.util.Ui;

import static com.expedia.bookings.utils.FeatureUtilKt.isMaterialFormsEnabled;

public class AssistanceTypeSpinnerAdapter extends BaseAdapter {

	private final int mTextViewId;
	private final int mDropdownResourceId;
	private int mCurrentPosition;

	public void setCurrentPosition(int position) {
		mCurrentPosition = position;
	}

	public int getCurrentPosition() {
		return mCurrentPosition;
	}

	class AssistanceSpinnerHelper {
		AssistanceType mAssistanceType;
		String mAssistanceTypeStr;

		public AssistanceSpinnerHelper(AssistanceType assistanceType, String assistanceTypeStr) {
			setAssistanceType(assistanceType);
			setAssistanceTypeString(assistanceTypeStr);
		}

		public void setAssistanceTypeString(String assistanceType) {
			mAssistanceTypeStr = assistanceType;
		}

		public String getAssistanceTypeString() {
			return mAssistanceTypeStr;
		}

		public void setAssistanceType(AssistanceType assistanceType) {
			mAssistanceType = assistanceType;
		}

		public AssistanceType getAssistanceType() {
			return mAssistanceType;
		}
	}

	private ArrayList<AssistanceSpinnerHelper> mAssistanceTypes;
	private String mFormatString = "%s";

	public AssistanceTypeSpinnerAdapter(Context context) {
		this(context, R.layout.simple_spinner_traveler_item);
	}

	public AssistanceTypeSpinnerAdapter(Context context, int textViewId) {
		this(context, textViewId, R.layout.simple_dropdown_item_2line_dark);
	}

	public AssistanceTypeSpinnerAdapter(Context context, int textViewId, int dropDownResource) {
		mTextViewId = textViewId;
		mDropdownResourceId = dropDownResource;
		fillAssistanceTypes(context);
	}

	public void setFormatString(String formatString) {
		mFormatString = formatString;
	}

	@Override
	public int getCount() {
		return mAssistanceTypes.size();
	}

	@Override
	public CharSequence getItem(int position) {
		return mAssistanceTypes.get(position).getAssistanceTypeString();
	}

	public AssistanceType getItemAssistanceType(int position) {
		return mAssistanceTypes.get(position).getAssistanceType();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View retView;
		if (convertView == null) {
			retView = Ui.inflate(mTextViewId, parent, false);
		}
		else {
			retView = convertView;
		}

		TextView tv = Ui.findView(retView, android.R.id.text1);
		tv.setText(HtmlCompat.fromHtml(String.format(mFormatString, getItem(position))));
		TextViewExtensions.Companion.setTextColorBasedOnPosition(tv, mCurrentPosition, position);
		if (isMaterialFormsEnabled()) {
			tv.setSingleLine(false);
		}

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

		//Wheel chair types are long, so we use the second line
		TextView tv1 = Ui.findView(retView, android.R.id.text1);
		TextView tv2 = Ui.findView(retView, android.R.id.text2);

		AssistanceType type = getItemAssistanceType(position);
		if (type.equals(AssistanceType.WHEELCHAIR_CAN_CLIMB_STAIRS)
			|| type.equals(AssistanceType.WHEELCHAIR_CANNOT_CLIMB_STAIRS)
			|| type.equals(AssistanceType.WHEELCHAIR_IMMOBILE)) {

			tv2.setVisibility(View.VISIBLE);
			tv1.setText(R.string.wheel_chair_needed);
			if (type.equals(AssistanceType.WHEELCHAIR_IMMOBILE)) {
				tv2.setText(R.string.immobile);
			}
			else if (type.equals(AssistanceType.WHEELCHAIR_CANNOT_CLIMB_STAIRS)) {
				tv2.setText(R.string.cannot_climb_stairs);
			}
			else if (type.equals(AssistanceType.WHEELCHAIR_CAN_CLIMB_STAIRS)) {
				tv2.setText(R.string.can_climb_stairs);
			}
		}
		else {
			tv1.setText(getItem(position));
			tv2.setVisibility(View.GONE);
			tv2.setText("");
		}

		if (position == mCurrentPosition) {
			if (tv1 instanceof CheckedTextView) {
				((CheckedTextView) tv1).setChecked(true);
			}
			if (tv2 instanceof CheckedTextView) {
				((CheckedTextView) tv2).setChecked(true);
			}
		}

		return retView;
	}

	public AssistanceType getAssistanceType(int position) {
		return mAssistanceTypes.get(position).getAssistanceType();
	}

	public int getAssistanceTypePosition(AssistanceType assistanceType) {
		if (assistanceType == null) {
			return -1;
		}

		for (int i = 0; i < mAssistanceTypes.size(); i++) {
			if (mAssistanceTypes.get(i).getAssistanceType() == assistanceType) {
				return i;
			}
		}
		return -1;
	}

	private void fillAssistanceTypes(Context context) {
		final Resources res = context.getResources();
		mAssistanceTypes = new ArrayList<>();
		mAssistanceTypes.add(new AssistanceSpinnerHelper(AssistanceType.NONE, res.getString(R.string.no_assistance)));
		mAssistanceTypes.add(new AssistanceSpinnerHelper(AssistanceType.WHEELCHAIR_IMMOBILE, res
			.getString(R.string.wheelchair_immobile)));
		mAssistanceTypes.add(new AssistanceSpinnerHelper(AssistanceType.WHEELCHAIR_CAN_CLIMB_STAIRS, res
			.getString(R.string.wheelchair_stairs_ok)));
		mAssistanceTypes.add(new AssistanceSpinnerHelper(AssistanceType.WHEELCHAIR_CANNOT_CLIMB_STAIRS, res
			.getString(R.string.wheelchair_no_stairs)));
		mAssistanceTypes.add(new AssistanceSpinnerHelper(AssistanceType.DEAF_WITH_HEARING_DOG, res
			.getString(R.string.deaf_with_hearing_dog)));
		mAssistanceTypes.add(new AssistanceSpinnerHelper(AssistanceType.BLIND_WITH_SEEING_EYE_DOG, res
			.getString(R.string.blind_with_guide_dog)));

	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
}
