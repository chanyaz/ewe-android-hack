package com.expedia.bookings.activity;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.fragment.EventManager;
import com.expedia.bookings.model.Search;
import com.mobiata.android.hockey.helper.HockeyAppUtil;
import com.mobiata.android.util.AndroidUtils;
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
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(mInstance, InstanceFragment.TAG);
			ft.commit();
		}

		setContentView(R.layout.activity_search_fragment);

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

			mEventManager.notifyEventHandlers(EVENT_RESET_PARAMS, Db.getSearchParams());
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && requestCode == REQUEST_SEARCH) {
			// TODO: Currently this is how the Fragment knows to update itself because it could
			// have new information - perhaps we should do this in a better manner?
			mEventManager.notifyEventHandlers(EVENT_UPDATE_PARAMS, Db.getSearchParams());
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

		public List<Search> mAutosuggestions;
		public String mAutosuggestQuery;
	}
}
