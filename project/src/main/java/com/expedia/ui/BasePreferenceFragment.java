package com.expedia.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.expedia.bookings.R;

/**
 * Created by doug on 2/3/15.
 */
public abstract class BasePreferenceFragment extends PreferenceFragment {
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		if (v != null) {
			ListView lv = (ListView) v.findViewById(android.R.id.list);
			lv.setPadding(0, 0, 0, 0);
			lv.setDividerHeight(0);
			lv.setSelector(R.drawable.list_choice_background_preferences);
		}
		return v;
	}
}
