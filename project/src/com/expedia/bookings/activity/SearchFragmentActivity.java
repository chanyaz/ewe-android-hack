package com.expedia.bookings.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.fragment.EventManager;

public class SearchFragmentActivity extends Activity {

	//////////////////////////////////////////////////////////////////////////
	// Constants

	public static final int EVENT_RESET_PARAMS = 1;

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
