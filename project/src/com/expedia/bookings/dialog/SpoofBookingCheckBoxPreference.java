package com.expedia.bookings.dialog;

import java.util.ArrayList;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.expedia.bookings.R;

public class SpoofBookingCheckBoxPreference extends CheckBoxPreference {
	public SpoofBookingCheckBoxPreference(Context context) {
		super(context);
	}

	public SpoofBookingCheckBoxPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SpoofBookingCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public View getView(View convertView, ViewGroup parent) {
		View v = super.getView(convertView, parent);

		if (v instanceof ViewGroup) {
			ArrayList<ViewGroup> worklist = new ArrayList<ViewGroup>();
			worklist.add((ViewGroup) v);

			for (int i = 0; i < worklist.size(); i ++) {
				ViewGroup vg = worklist.get(i);
				for (int j = 0; j < vg.getChildCount(); j ++) {
					View child = vg.getChildAt(j);
					if (child instanceof CheckBox) {
						CheckBox check = (CheckBox) child;
						check.setId(R.id.preference_spoof_booking_checkbox);
						// fastpath out
						return v;
					}
					else if (child instanceof ViewGroup) {
						ViewGroup group = (ViewGroup) child;
						worklist.add(group);
					}
				}
			}

		}

		return v;
	}
}

