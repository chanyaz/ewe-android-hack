package com.expedia.bookings.preference;

import java.util.Locale;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import rx.Observer;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat {

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		RecyclerView recyclerView = getListView();
		recyclerView.setPadding(0,0,0,0);
		setDividerHeight(2);

		if (getActivity() instanceof ExpediaBookingPreferenceActivity) {
			((ExpediaBookingPreferenceActivity) getActivity()).searchQueryChangeSubject.subscribe(new Observer<String>() {
				@Override
				public void onCompleted() {
				}
				@Override
				public void onError(Throwable e) {
				}
				@Override
				public void onNext(String query) {
					setAllPreferencesVisible(getPreferenceScreen());
					filter(query.toLowerCase(Locale.US), getPreferenceScreen());
				}
			});
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (getActivity() instanceof ExpediaBookingPreferenceActivity) {
			((ExpediaBookingPreferenceActivity) getActivity()).clearQuery();
		}
	}

	public void filter(String query, PreferenceGroup preferenceGroup) {
		if (query.length() == 0) {
			getListView().scrollToPosition(0);
			return;
		}

		for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
			Preference preference = preferenceGroup.getPreference(i);
			if (preference instanceof PreferenceGroup) {
				preference.setVisible(false);
				filter(query, (PreferenceGroup) preference);
			}
			else {
				if (preference.getTitle() != null && preference.getTitle().toString().toLowerCase(Locale.US).contains(query)) {
					preference.setVisible(true);
				}
				else if (preference.getSummary() != null && preference.getSummary().toString().toLowerCase(Locale.US).contains(query)) {
					preference.setVisible(true);
				}
				else {
					preference.setVisible(false);
				}
			}
		}
	}

	private void setAllPreferencesVisible(PreferenceGroup preferenceGroup) {
		for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
			Preference preference = preferenceGroup.getPreference(i);
			preference.setVisible(true);
			if (preference instanceof PreferenceGroup) {
				setAllPreferencesVisible((PreferenceGroup) preference);
			}
		}
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
	}
}
