package com.expedia.bookings.section;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler.AssistanceType;
import com.mobiata.android.util.Ui;

public class AssistanceTypeSpinnerAdapter extends BaseAdapter {

	private Context mContext;

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
		mContext = context;
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
			LayoutInflater inflater = LayoutInflater.from(mContext);
			retView = inflater.inflate(R.layout.simple_spinner_traveler_item, null);
		}
		else {
			retView = convertView;
		}
		TextView tv = Ui.findView(retView, android.R.id.text1);
		tv.setText(Html.fromHtml(String.format(mFormatString, getItem(position))));
		return retView;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View retView;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			retView = inflater.inflate(R.layout.simple_dropdown_item_2line_dark, null);
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

		return retView;
	}

	public AssistanceType getAssistanceType(int position) {
		return mAssistanceTypes.get(position).getAssistanceType();
	}

	public int getAssistanceTypePosition(AssistanceType gender) {
		if (gender == null) {
			return -1;
		}

		for (int i = 0; i < mAssistanceTypes.size(); i++) {
			if (mAssistanceTypes.get(i).getAssistanceType() == gender) {
				return i;
			}
		}
		return -1;
	}

	private void fillAssistanceTypes(Context context) {
		final Resources res = context.getResources();
		mAssistanceTypes = new ArrayList<AssistanceSpinnerHelper>();
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
				.getString(R.string.blind_with_seeing_eye_dog)));

	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
}
