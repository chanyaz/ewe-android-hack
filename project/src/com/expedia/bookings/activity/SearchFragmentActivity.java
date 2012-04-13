package com.expedia.bookings.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.fragment.SearchParamsFragment;
import com.expedia.bookings.fragment.SearchParamsFragment.SearchParamsFragmentListener;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.hockey.helper.HockeyAppUtil;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.DialogUtils;
import com.mobiata.android.util.NetUtils;

public class SearchFragmentActivity extends FragmentActivity implements SearchParamsFragmentListener {

	//////////////////////////////////////////////////////////////////////////
	// Constants

	public static final int EVENT_RESET_PARAMS = 1;

	public static final int DIALOG_NO_INTERNET = 1;

	//////////////////////////////////////////////////////////////////////////
	// Member vars

	private SearchParamsFragment mSearchParamsFragment;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// #7090: If the user was just sent from the ConfirmationActivity, quit (if desired)
		if (getIntent().getBooleanExtra(Codes.EXTRA_FINISH, false)) {
			finish();
			return;
		}

		setContentView(R.layout.activity_search_fragment);

		mSearchParamsFragment = Ui.findSupportFragment(this, getString(R.string.tag_search_params));

		// HockeyApp update
		if (!AndroidUtils.isRelease(this)) {
			HockeyAppUtil.checkForUpdatesHockeyApp(this, this, Codes.HOCKEY_APP_ID);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent.hasExtra(Codes.EXTRA_NEW_SEARCH)) {
			Db.resetSearchParams();

			mSearchParamsFragment.onResetParams();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		//HockeyApp crash
		if (!AndroidUtils.isRelease(this)) {
			HockeyAppUtil.checkForCrashesHockeyApp(this, Codes.HOCKEY_APP_ID);
		}
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

		SearchParams params = Db.getSearchParams();

		if (params.getSearchType() == SearchType.FREEFORM) {
			Search.add(this, params);
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
