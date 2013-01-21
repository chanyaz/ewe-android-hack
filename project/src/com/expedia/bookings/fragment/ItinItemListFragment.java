package com.expedia.bookings.fragment;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.LaunchActivity;
import com.expedia.bookings.activity.LoginActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripResponse;
import com.expedia.bookings.fragment.LoginFragment.PathMode;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.expedia.bookings.widget.ItinItemAdapter;
import com.expedia.bookings.widget.ItinListView;
import com.expedia.bookings.widget.ScrollView;
import com.expedia.bookings.widget.ScrollView.OnScrollListener;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

public class ItinItemListFragment extends Fragment implements AccountButtonClickListener,
		ConfirmLogoutDialogFragment.DoLogoutListener {

	public static final String TAG = "TAG_ITIN_ITEM_LIST_FRAGMENT";
	private static final String NET_TRIPS = "NET_TRIPS";

	private LaunchActivity mActivity;

	private ItinListView mListView;
	private View mEmptyView;
	private View mOrEnterNumberTv;

	private AccountButton mAccountButton;

	private ItinItemAdapter mAdapter;
	private boolean mAllowLoadItins = false;

	public static ItinItemListFragment newInstance() {
		return new ItinItemListFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mActivity = (LaunchActivity) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_itinerary_list, null);

		mListView = Ui.findView(view, android.R.id.list);
		mEmptyView = Ui.findView(view, android.R.id.empty);
		mAccountButton = Ui.findView(view, R.id.account_button_root);
		mOrEnterNumberTv = Ui.findView(view, R.id.or_enter_itin_number_tv);

		mAdapter = new ItinItemAdapter(getActivity());
		mAdapter.registerDataSetObserver(mDataSetObserver);

		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(mOnScrollListener);
		mAccountButton.setListener(this);

		setListVisibility();
		
		
		mOrEnterNumberTv.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				showAddItinDialog();
			}
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		refreshAccountButtonState();
	}

	public boolean inListMode() {
		return mListView.getMode() == ItinListView.MODE_LIST;
	}

	public void showList() {
		mListView.setMode(ItinListView.MODE_LIST);
	}

	public void enableLoadItins() {
		mAllowLoadItins = true;
		refreshAccountButtonState();
	}
	
	public synchronized void showAddItinDialog() {
		ItineraryGuestAddDialogFragment addNewItinFrag = (ItineraryGuestAddDialogFragment) getFragmentManager()
				.findFragmentByTag(ItineraryGuestAddDialogFragment.TAG);
		if (addNewItinFrag == null) {
			addNewItinFrag = ItineraryGuestAddDialogFragment.newInstance();
		}
		if (!addNewItinFrag.isAdded() && !addNewItinFrag.isVisible()) {
			addNewItinFrag.show(getFragmentManager(), ItineraryGuestAddDialogFragment.TAG);
		}
	}

	private void refreshAccountButtonState() {
		if (User.isLoggedIn(getActivity())) {
			if (Db.getUser() == null) {
				Db.loadUser(getActivity());
			}

			if (Db.getUser() != null && Db.getUser().getPrimaryTraveler() != null
					&& !TextUtils.isEmpty(Db.getUser().getPrimaryTraveler().getEmail())) {

				mAccountButton.bind(false, true, Db.getUser(), true);
				onLoginCompleted();
			}
			else {
				//We thought the user was logged in, but the user appears to not contain the data we need, get rid of the user
				User.signOut(getActivity());
				mAccountButton.bind(false, false, null, true);
			}
		}
		else {
			mAccountButton.bind(false, false, null, true);
		}
	}

	@Override
	public void accountLoginClicked() {
		Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
		loginIntent.putExtra(LoginActivity.ARG_PATH_MODE, PathMode.FLIGHTS.name());
		startActivity(loginIntent);
	}

	@Override
	public void accountLogoutClicked() {
		ConfirmLogoutDialogFragment df = new ConfirmLogoutDialogFragment();
		df.setDoLogoutListener(this);
		df.show(getFragmentManager(), ConfirmLogoutDialogFragment.TAG);
	}

	@Override
	public void doLogout() {
		// Sign out user
		User.signOut(getActivity());

		// Update UI
		mAccountButton.bind(false, false, null, true);
		
		//TODO: We should keep around the guest itins...
		if(mAdapter != null){
			mAdapter.clearItinItems();
		}
		
		invalidateOptionsMenu();
	}
	
	@SuppressLint("NewApi")
	public void invalidateOptionsMenu(){
		if(this.getActivity() != null){
			if(getActivity() instanceof SherlockActivity){
				((SherlockActivity) getActivity()).supportInvalidateOptionsMenu();
			}else if(AndroidUtils.getSdkVersion() >= 11){
				getActivity().invalidateOptionsMenu();
			}else{
				throw new RuntimeException("ItinItemListFragment should be attached to a SherlockActivity if sdk version < 11");
			}
		}
	}
	

	public void onLoginCompleted() {
		mAccountButton.bind(false, true, Db.getUser(), true);

		if (mAllowLoadItins) {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			if (!bd.isDownloading(NET_TRIPS)) {
				bd.startDownload(NET_TRIPS, mTripDownload, mTripHandler);
			}
		}
		
		invalidateOptionsMenu();
	}

	private void setListVisibility() {
		final boolean listVisible = mAdapter != null && mAdapter.getCount() > 0;

		mListView.setVisibility(listVisible ? View.VISIBLE : View.GONE);
		mEmptyView.setVisibility(listVisible ? View.GONE : View.VISIBLE);
	}

	private final DataSetObserver mDataSetObserver = new DataSetObserver() {
		public void onChanged() {
			setListVisibility();
		};
	};

	private OnScrollListener mOnScrollListener = new OnScrollListener() {
		@Override
		public void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy) {
			mActivity.setHeaderOffset(-scrollView.getScrollY());
		}
	};

	//////////////
	//TODO: REMOVE THIS STUFF WHEN WE GET A BETTER ITIN MANAGER IN PLACE
	/////////////

	private final Download<TripResponse> mTripDownload = new Download<TripResponse>() {
		@Override
		public TripResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			return services.getTrips(ExpediaServices.F_FLIGHTS | ExpediaServices.F_HOTELS);
		}
	};

	private final OnDownloadComplete<TripResponse> mTripHandler = new OnDownloadComplete<TripResponse>() {
		@Override
		public void onDownload(TripResponse response) {
			List<Trip> trips = response.getTrips();
			for (int i = 0; i < trips.size(); i++) {
				mAdapter.addAllItinItems(trips.get(0).getTripComponents());
			}

			mAdapter.notifyDataSetChanged();
		}
	};
}