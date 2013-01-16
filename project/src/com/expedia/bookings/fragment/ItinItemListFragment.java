package com.expedia.bookings.fragment;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

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
import com.expedia.bookings.widget.ItinCard;
import com.expedia.bookings.widget.ItinItemAdapter;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

public class ItinItemListFragment extends ListFragment implements AccountButtonClickListener,
		ConfirmLogoutDialogFragment.DoLogoutListener {

	public static final String TAG = "TAG_ITIN_ITEM_LIST_FRAGMENT";

	private AccountButton mAccountButton;

	private ListView mListView;
	private ViewGroup mDetailLayout;
	private View mContentShadowView;

	private boolean mInListMode = true;

	public static ItinItemListFragment newInstance() {
		return new ItinItemListFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_itinerary_list, null);

		mAccountButton = Ui.findView(view, R.id.account_button_root);
		mListView = Ui.findView(view, android.R.id.list);
		mDetailLayout = Ui.findView(view, R.id.detail_layout);
		mContentShadowView = Ui.findView(view, R.id.content_shadow);

		mAccountButton.setListener(this);

		setListAdapter(new ItinItemAdapter(getActivity()));

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		refreshAccountButtonState();

		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				Log.d("Scroll Range: firstItem:" + firstVisibleItem + " visibleItemCount:" + visibleItemCount);
				for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
					if (arg0.getChildAt(i) != null) {
						arg0.getChildAt(i).invalidate();
					}
					else {
						Log.d("Fail getting child at: " + i);
					}
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
			}
		});
	}

	public boolean inListMode() {
		return mInListMode;
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
	}

	public void onLoginCompleted() {
		mAccountButton.bind(false, true, Db.getUser(), true);

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (!bd.isDownloading(NET_TRIPS)) {
			bd.startDownload(NET_TRIPS, mTripDownload, mTripHandler);
		}

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		showDetails(id, ViewHelper.getY(v));
	}

	public void showDetails(long id, float y) {
		mInListMode = false;

		ItinCard card = new ItinCard(getActivity());

		ViewHelper.setAlpha(mDetailLayout, 1);
		mDetailLayout.removeAllViews();
		mDetailLayout.addView(card);

		ObjectAnimator.ofFloat(mListView, "alpha", 1, 0).start();
		ObjectAnimator.ofFloat(mContentShadowView, "alpha", 1, 0).start();
		ObjectAnimator.ofFloat(card, "translationY", y, 0).start();

		((LaunchActivity) getActivity()).hideHeader();
	}

	public void showList() {
		mInListMode = true;

		ObjectAnimator.ofFloat(mListView, "alpha", 0, 1).start();
		ObjectAnimator.ofFloat(mContentShadowView, "alpha", 0, 1).start();
		ObjectAnimator.ofFloat(mDetailLayout, "alpha", 1, 0).start();

		((LaunchActivity) getActivity()).showHeader();
	}

	//////////////
	//TODO: REMOVE THIS STUFF WHEN WE GET A BETTER ITIN MANAGER IN PLACE
	/////////////

	private static final String NET_TRIPS = "NET_TRIPS";

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
				((ItinItemAdapter) ItinItemListFragment.this.getListAdapter()).addAllItinItems(trips.get(0)
						.getTripComponents());
			}
			((ItinItemAdapter) ItinItemListFragment.this.getListAdapter()).notifyDataSetChanged();

		}
	};
}