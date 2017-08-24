package com.expedia.bookings.preference;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.MenuItem;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.BugShakerShim;
import com.expedia.bookings.utils.Constants;
import com.expedia.util.PermissionsHelperKt;
import com.mobiata.android.Log;
import rx.subjects.PublishSubject;

public class ExpediaBookingPreferenceActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

	private SearchView searchView;

	public final PublishSubject<String> searchQueryChangeSubject = PublishSubject.create();

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
		@NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == Constants.PERMISSION_WRITE_EXTERNAL_STORAGE_BUGSHAKER) {
			if (PermissionsHelperKt.hasPermissionToWriteToExternalStorage(getBaseContext())) {
				BugShakerShim.startNewBugShaker(getApplication());
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_preferences);

		ActionBar ab = getSupportActionBar();
		if (ab != null) {
			ab.setDisplayHomeAsUpEnabled(true);
		}

		setResult(Constants.RESULT_NO_CHANGES);

		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.fragment_container, new EBPreferencesFragment())
			.commit();

		searchView = (SearchView) findViewById(R.id.preference_search_bar);
		searchView.setOnQueryTextListener(this);
		searchView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchView.setIconified(false);
			}
		});
	}

	@Override
	public void setTitle(CharSequence title) {
		ActionBar ab = getSupportActionBar();
		if (ab != null) {
			ab.setTitle(title);
		}
		else {
			super.setTitle(title);
		}
	}

	public void changedPrefs() {
		setResult(Constants.RESULT_CHANGED_PREFS);
	}

	@Override
	public void onBackPressed() {
		Log.e("onBackPressed. back stack = " + getFragmentManager().getBackStackEntryCount());
		FragmentManager fragmentManager = getFragmentManager();
		if (fragmentManager.getBackStackEntryCount() > 1) {
			fragmentManager.popBackStack();
		}
		else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String query) {
		searchQueryChangeSubject.onNext(query);
		return true;
	}

	public void clearQuery() {
		searchView.setQuery("", false);
	}
}
