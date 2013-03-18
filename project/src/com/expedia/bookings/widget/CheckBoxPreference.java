package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.expedia.bookings.R;

public class CheckBoxPreference extends android.preference.CheckBoxPreference {

	private int mId;

	public CheckBoxPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		if (attrs != null) {
			TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CheckBoxPreference, 0, 0);
			mId = ta.getResourceId(R.styleable.CheckBoxPreference_id, 0);
			ta.recycle();
		}
	}

	@Override
	public View getView(View convertView, ViewGroup parent) {
		View v = super.getView(convertView, parent);

		if (mId != 0) {
			if (v instanceof ViewGroup) {
				ArrayList<ViewGroup> worklist = new ArrayList<ViewGroup>();
				worklist.add((ViewGroup) v);

				for (int i = 0; i < worklist.size(); i++) {
					ViewGroup vg = worklist.get(i);
					for (int j = 0; j < vg.getChildCount(); j++) {
						View child = vg.getChildAt(j);
						if (child instanceof CheckBox) {
							CheckBox check = (CheckBox) child;
							check.setId(mId);
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
		}

		return v;
	}
}
