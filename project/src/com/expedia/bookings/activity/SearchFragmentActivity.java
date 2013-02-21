package com.expedia.bookings.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.dialog.GooglePlayServicesDialog;
import com.expedia.bookings.fragment.SearchParamsFragment;
import com.expedia.bookings.fragment.SearchParamsFragment.SearchParamsFragmentListener;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.DialogUtils;
import com.mobiata.android.util.NetUtils;

// This is the TABLET search fragment activity

public class SearchFragmentActivity extends FragmentActivity implements SearchParamsFragmentListener {

	//////////////////////////////////////////////////////////////////////////
	// Constants

	public static final int EVENT_RESET_PARAMS = 1;

	public static final int DIALOG_NO_INTERNET = 1;

	// Used in onNewIntent(), if the calling Activity wants the SearchActivity to start fresh
	private static final String EXTRA_NEW_SEARCH = "EXTRA_NEW_SEARCH";

	//////////////////////////////////////////////////////////////////////////
	// Member vars

	private SearchParamsFragment mSearchParamsFragment;

	private HockeyPuck mHockeyPuck;

	private ActivityKillReceiver mKillReciever;

	//////////////////////////////////////////////////////////////////////////////////////////
	// Static Methods
	//////////////////////////////////////////////////////////////////////////////////////////

	public static Intent createIntent(Context context, boolean startNewSearch) {
		Intent intent = new Intent(context, SearchFragmentActivity.class);
		if (startNewSearch) {
			intent.putExtra(EXTRA_NEW_SEARCH, true);
		}
		return intent;
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_search_fragment);

		mSearchParamsFragment = Ui.findSupportFragment(this, getString(R.string.tag_search_params));

		// HockeyApp update
		mHockeyPuck = new HockeyPuck(this, Codes.HOCKEY_APP_ID, !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);

		mKillReciever = new ActivityKillReceiver(this);
		mKillReciever.onCreate();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent.hasExtra(EXTRA_NEW_SEARCH)) {
			Db.resetSearchParams();

			mSearchParamsFragment.onResetParams();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mHockeyPuck.onResume();

		GooglePlayServicesDialog gpsd = new GooglePlayServicesDialog(this);
		gpsd.startChecking();

		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		OmnitureTracking.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mKillReciever != null) {
			mKillReciever.onDestroy();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		mHockeyPuck.onSaveInstanceState(outState);
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		case DIALOG_NO_INTERNET:
			return DialogUtils.createSimpleDialog(this, DIALOG_NO_INTERNET, 0, R.string.error_no_internet);
		}

		return super.onCreateDialog(id, args);

	}

	public void startSearch() {
		if (!NetUtils.isOnline(this)) {
			showDialog(DIALOG_NO_INTERNET);
			return;
		}

		Intent intent = new Intent(this, SearchResultsFragmentActivity.class);
		startActivity(intent);
	}

	//////////////////////////////////////////////////////////////////////////
	// SearchParamsFragmentListener

	@Override
	public void onSearch() {
		startSearch();
	}
}
