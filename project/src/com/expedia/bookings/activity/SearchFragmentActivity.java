package com.expedia.bookings.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.fragment.EventManager;
import com.expedia.bookings.model.Search;
import com.mobiata.android.hockey.helper.HockeyAppUtil;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.DialogUtils;
import com.mobiata.android.util.NetUtils;

public class SearchFragmentActivity extends Activity {

	//////////////////////////////////////////////////////////////////////////
	// Constants

	public static final int EVENT_RESET_PARAMS = 1;
	public static final int EVENT_UPDATE_PARAMS = 2;

	public static final int REQUEST_SEARCH = 1;

	public static final int DIALOG_NO_INTERNET = 1;

	//////////////////////////////////////////////////////////////////////////
	// Member vars

	private InstanceFragment mInstance;

	public EventManager mEventManager = new EventManager();

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

		FragmentManager fm = getFragmentManager();
		mInstance = (InstanceFragment) fm.findFragmentByTag(InstanceFragment.TAG);
		if (mInstance == null) {
			mInstance = InstanceFragment.newInstance();
			mInstance.mSearchParams = new SearchParams();
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(mInstance, InstanceFragment.TAG);
			ft.commit();
		}

		setContentView(R.layout.activity_search_fragment);

		// HockeyApp update
		HockeyAppUtil.checkForUpdatesHockeyApp(this, this, Codes.HOCKEY_APP_ID);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent.hasExtra(Codes.EXTRA_NEW_SEARCH)) {
			mInstance.mSearchParams = new SearchParams();
			mInstance.mHasFocusedSearchField = false;

			mEventManager.notifyEventHandlers(EVENT_RESET_PARAMS, mInstance.mSearchParams);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		//HockeyApp crash
		HockeyAppUtil.checkForCrashesHockeyApp(this, Codes.HOCKEY_APP_ID);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && requestCode == REQUEST_SEARCH && data != null
				&& data.hasExtra(Codes.SEARCH_PARAMS)) {
			// #11468: Not sure if this is the root cause of the problem, but trying to prevent crash here
			SearchParams params = JSONUtils.parseJSONableFromIntent(data, Codes.SEARCH_PARAMS, SearchParams.class);
			if (params != null) {
				mInstance.mSearchParams = params;
			}
			mInstance.mHasFocusedSearchField = true;

			mEventManager.notifyEventHandlers(EVENT_UPDATE_PARAMS, mInstance.mSearchParams);
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

		if (getInstance().mSearchParams.getSearchType() == SearchType.FREEFORM) {
			Search.add(this, getInstance().mSearchParams);
		}

		Intent intent = new Intent(this, SearchResultsFragmentActivity.class);
		intent.putExtra(Codes.SEARCH_PARAMS, getInstance().mSearchParams.toJson().toString());
		startActivityForResult(intent, REQUEST_SEARCH);
	}

	//////////////////////////////////////////////////////////////////////////
	// Data access / InstanceFragment

	public InstanceFragment getInstance() {
		return mInstance;
	}

	public static class InstanceFragment extends Fragment {
		public static final String TAG = "INSTANCE";

		public static InstanceFragment newInstance() {
			InstanceFragment fragment = new InstanceFragment();
			fragment.setRetainInstance(true);
			return fragment;
		}

		public SearchParams mSearchParams;

		public boolean mHasFocusedSearchField = false;
	}
}
