package com.expedia.bookings.fragment;

import java.util.Collection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.LaunchActivity;
import com.expedia.bookings.activity.LoginActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncListener;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.fragment.ItineraryGuestAddDialogFragment.AddGuestItineraryDialogListener;
import com.expedia.bookings.fragment.LoginFragment.PathMode;
import com.expedia.bookings.widget.ItinScrollView;
import com.expedia.bookings.widget.ScrollView;
import com.expedia.bookings.widget.ScrollView.OnScrollListener;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

public class ItinItemListFragment extends Fragment implements ConfirmLogoutDialogFragment.DoLogoutListener,
		ItinerarySyncListener {

	public static final String TAG = "TAG_ITIN_ITEM_LIST_FRAGMENT";
	private static final String NET_TRIPS = "NET_TRIPS";

	private LaunchActivity mActivity;

	private ItinScrollView mListView;
	private View mEmptyView;
	private View mOrEnterNumberTv;
	private ItineraryManager mItinManager;
	private ViewGroup mEmptyListLoadingContainer;
	private ViewGroup mEmptyListContent;
	private Button mLoginButton;

	private boolean mAllowLoadItins = false;

	public static ItinItemListFragment newInstance() {
		return new ItinItemListFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mActivity = (LaunchActivity) activity;
		mItinManager = ItineraryManager.getInstance();
		mItinManager.addSyncListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_itinerary_list, null);

		mListView = Ui.findView(view, android.R.id.list);
		mEmptyView = Ui.findView(view, android.R.id.empty);
		mOrEnterNumberTv = Ui.findView(view, R.id.or_enter_itin_number_tv);
		mEmptyListLoadingContainer = Ui.findView(view, R.id.empty_list_loading_container);
		mEmptyListContent = Ui.findView(view, R.id.empty_list_content);
		mLoginButton = Ui.findView(view, R.id.login_button);
		mLoginButton.setText(Html.fromHtml(getString(R.string.log_in_for_your_trips)));

		mListView.setEmptyView(mEmptyView);
		mListView.setOnScrollListener(mOnScrollListener);

		mLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
				loginIntent.putExtra(LoginActivity.ARG_PATH_MODE, PathMode.HOTELS.name());
				startActivity(loginIntent);
			}
		});

		mOrEnterNumberTv.setOnClickListener(new OnClickListener() {
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

		updateLoginState();

		syncItinManager();
	}

	public void syncItinManager() {
		if (mAllowLoadItins) {
			mItinManager.startSync();
			setIsLoading(true);
		}
	}

	public void setIsLoading(boolean isLoading) {
		mEmptyListLoadingContainer.setVisibility(isLoading ? View.VISIBLE : View.GONE);
		mEmptyListContent.setVisibility(isLoading ? View.GONE : View.VISIBLE);
	}

	public boolean inListMode() {
		return mListView.getMode() == ItinScrollView.MODE_LIST;
	}

	public void setListMode() {
		mListView.setMode(ItinScrollView.MODE_LIST);
	}

	public void enableLoadItins() {
		mAllowLoadItins = true;
		syncItinManager();
	}

	public synchronized void showAddItinDialog() {
		ItineraryGuestAddDialogFragment addNewItinFrag = (ItineraryGuestAddDialogFragment) getFragmentManager()
				.findFragmentByTag(ItineraryGuestAddDialogFragment.TAG);
		if (addNewItinFrag == null) {
			addNewItinFrag = ItineraryGuestAddDialogFragment.newInstance();
		}
		if (!addNewItinFrag.isAdded() && !addNewItinFrag.isVisible()) {
			addNewItinFrag.setListener(new AddGuestItineraryDialogListener() {

				@Override
				public void onFindItinClicked(String email, String itinNumber) {
					mItinManager.addGuestTrip(email, itinNumber, true);
					setIsLoading(true);
				}

				@Override
				public void onCancel() {
					// We dont care...

				}

			});
			addNewItinFrag.show(getFragmentManager(), ItineraryGuestAddDialogFragment.TAG);
		}
	}

	private void updateLoginState() {
		if (User.isLoggedIn(getActivity()) && Db.getUser() != null) {
			mLoginButton.setVisibility(View.GONE);
		}
		else {
			mLoginButton.setVisibility(View.VISIBLE);
		}
	}

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
		updateLoginState();

		invalidateOptionsMenu();

		syncItinManager();
	}

	@SuppressLint("NewApi")
	public void invalidateOptionsMenu() {
		if (this.getActivity() != null) {
			if (getActivity() instanceof SherlockActivity) {
				((SherlockActivity) getActivity()).supportInvalidateOptionsMenu();
			}
			else if (AndroidUtils.getSdkVersion() >= 11) {
				getActivity().invalidateOptionsMenu();
			}
			else {
				throw new RuntimeException(
						"ItinItemListFragment should be attached to a SherlockActivity if sdk version < 11");
			}
		}
	}

	public void onLoginCompleted() {
		updateLoginState();

		syncItinManager();

		invalidateOptionsMenu();
	}

	private OnScrollListener mOnScrollListener = new OnScrollListener() {
		@Override
		public void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy) {
			mActivity.setHeaderOffset(-y);
		}
	};

	@Override
	public void onTripAdded(Trip trip) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTripUpdated(Trip trip) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTripUpateFailed(Trip trip) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTripRemoved(Trip trip) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSyncFinished(Collection<Trip> trips) {
		setIsLoading(false);
	}

}
