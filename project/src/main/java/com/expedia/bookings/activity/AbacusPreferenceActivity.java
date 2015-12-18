package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusUtils;

public class AbacusPreferenceActivity extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.abacus_preferences);
		for (int key : AbacusUtils.getActiveTests()) {
			ListPreference preference = (ListPreference) findPreference(
				String.valueOf(key));
			preference.setOnPreferenceChangeListener(abacusPrefListener);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		ActionBar ab = getActionBar();
		// ab is null on tablet
		if (ab != null) {
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayShowTitleEnabled(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	private Preference.OnPreferenceChangeListener abacusPrefListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			int value = Integer.valueOf(newValue.toString());
			Db.getAbacusResponse().updateABTestForDebug(Integer.valueOf(preference.getKey()), value);
			return true;
		}
	};
}
