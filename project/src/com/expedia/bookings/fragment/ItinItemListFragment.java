package com.expedia.bookings.fragment;

import java.util.Collection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ItineraryGuestAddActivity;
import com.expedia.bookings.activity.LoginActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncListener;
import com.expedia.bookings.data.trips.ItineraryManager.SyncError;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.dialog.SocialMessageChooserDialogFragment;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.ItinCard.OnItinCardClickListener;
import com.expedia.bookings.widget.ItinListView;
import com.expedia.bookings.widget.ItinListView.OnListModeChangedListener;
import com.expedia.bookings.widget.ItineraryLoaderLoginExtender;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class ItinItemListFragment extends Fragment implements ConfirmLogoutDialogFragment.DoLogoutListener,
		ItinerarySyncListener {

	public static final String TAG = "TAG_ITIN_ITEM_LIST_FRAGMENT";
	public static final String DIALOG_SHARE = "DIALOG_SHARE";

	private static final String STATE_ERROR_MESSAGE = "STATE_ERROR_MESSAGE";
	private static final String STATE_ALLOW_LOAD_ITINS = "STATE_ALLOW_LOAD_ITINS";

	private ItinItemListFragmentListener mListener;

	private View mRoot;
	private ItinListView mItinListView;
	private View mEmptyView;
	private View mOrEnterNumberTv;
	private ItineraryManager mItinManager;
	private ViewGroup mEmptyListLoadingContainer;
	private ViewGroup mEmptyListContent;
	private Button mLoginButton;
	private Button mNoTripsRefreshButton;
	private Button mNoTripsTryAgainButton;
	private ViewGroup mErrorContainer;
	private TextView mErrorTv;
	private View mErrorMask;

	private String mErrorMessage;
	private boolean mShowError = false;
	private boolean mAllowLoadItins = false;

	private boolean mIsLoading = false;
	private String mJumpToItinId = null;

	/**
	 * Creates a new fragment that will open right away to the passed uniqueId.
	 * @param uniqueId
	 * @return
	 */
	public static ItinItemListFragment newInstance(String uniqueId) {
		ItinItemListFragment frag = new ItinItemListFragment();
		frag.mJumpToItinId = uniqueId;
		return frag;
	}

	public static ItinItemListFragment newInstance() {
		return new ItinItemListFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mItinManager = ItineraryManager.getInstance();
		mItinManager.addSyncListener(this);

		// Not a strict requirement
		if (activity instanceof ItinItemListFragmentListener) {
			mListener = (ItinItemListFragmentListener) activity;

			mListener.onItinItemListFragmentAttached(this);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_itinerary_list, null);

		mRoot = Ui.findView(view, R.id.outer_container);
		mItinListView = Ui.findView(view, android.R.id.list);
		mEmptyView = Ui.findView(view, android.R.id.empty);
		mOrEnterNumberTv = Ui.findView(view, R.id.or_enter_itin_number_tv);
		mEmptyListLoadingContainer = Ui.findView(view, R.id.empty_list_loading_container);
		mEmptyListContent = Ui.findView(view, R.id.empty_list_content);
		mLoginButton = Ui.findView(view, R.id.login_button);
		mLoginButton.setText(Html.fromHtml(getString(R.string.log_in_for_your_trips)));
		mNoTripsRefreshButton = Ui.findView(view, R.id.no_trips_refresh_button);
		mNoTripsTryAgainButton = Ui.findView(view, R.id.no_trips_try_again_button);
		mErrorTv = Ui.findView(view, R.id.no_trips_error_message);
		mErrorMask = Ui.findView(view, R.id.empty_list_error_mask);
		mErrorContainer = Ui.findView(view, R.id.error_container);

		mItinListView.setEmptyView(mEmptyView);
		mItinListView.setOnListModeChangedListener(mOnListModeChangedListener);
		mItinListView.setOnItinCardClickListener(mOnItinCardClickListener);
		mItinListView.setOnItemClickListener(mOnItemClickListener);
		mItinListView.post(new Runnable() {
			@Override
			public void run() {
				if (getActivity() != null) {
					mItinListView.setExpandedCardHeight(view.getHeight() + getSupportActionBar().getHeight());
				}
			}
		});

		mLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startLoginActivity();
			}
		});

		mOrEnterNumberTv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startAddGuestItinActivity();
			}
		});

		OnClickListener syncManagerClickListener = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				syncItinManager(true, true);
			}
		};

		mNoTripsRefreshButton.setOnClickListener(syncManagerClickListener);
		mNoTripsTryAgainButton.setOnClickListener(syncManagerClickListener);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_ERROR_MESSAGE)) {
				setErrorMessage(savedInstanceState.getString(STATE_ERROR_MESSAGE), true);
			}
			mAllowLoadItins = savedInstanceState.getBoolean(STATE_ALLOW_LOAD_ITINS);
		}

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		updateLoginState();
		syncItinManager(false, false);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mShowError && mErrorMessage != null) {
			outState.putString(STATE_ERROR_MESSAGE, mErrorMessage);
		}
		outState.putBoolean(STATE_ALLOW_LOAD_ITINS, mAllowLoadItins);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mItinManager.removeSyncListener(this);
		mItinManager = null;
	}

	public int getItinCardCount() {
		int retVal = -1;
		if (mItinListView != null) {
			retVal = mItinListView.getCount() - mItinListView.getFooterViewsCount()
					- mItinListView.getHeaderViewsCount();
		}
		return retVal;
	}

	// Can only be called after onCreateView(); not an issue right now, if it becomes
	// one we can update the code.
	public void setBackgroundColor(int color) {
		mRoot.setBackgroundColor(color);
	}

	public void setSimpleMode(boolean enabled) {
		mItinListView.setSimpleMode(true);
	}

	public void showItinCard(String id) {
		if (mIsLoading || mItinListView == null) {
			mJumpToItinId = id;
			return;
		}
		mItinListView.showDetails(id);
		mJumpToItinId = null;
	}

	public ItinCardData getSelectedItinCardData() {
		return mItinListView.getSelectedItinCard();
	}

	public void syncItinManager(boolean forceRefresh, boolean showLoading) {
		if (mAllowLoadItins && mItinListView != null && mItinManager != null) {
			boolean syncing = mItinManager.startSync(forceRefresh);
			if (syncing && (showLoading || getItinCardCount() <= 0)) {
				setIsLoading(true);
				mItinListView.enableScrollToRevelentWhenDataSetChanged();
			}
			else {
				invalidateOptionsMenu();
				trackWithOmniture(true);
			}
		}
	}

	public void setIsLoading(boolean isLoading) {
		mIsLoading = isLoading;
		mEmptyListLoadingContainer.setVisibility(isLoading ? View.VISIBLE : View.GONE);
		mEmptyListContent.setVisibility(isLoading ? View.GONE : View.VISIBLE);
		invalidateOptionsMenu();
	}

	public boolean isLoading() {
		return mIsLoading;
	}

	public boolean inListMode() {
		if (mItinListView != null) {
			return mItinListView.getMode() == ItinListView.MODE_LIST;
		}
		return true;//We start out in list mode
	}

	public void setListMode() {
		if (mItinListView != null) {
			mItinListView.setMode(ItinListView.MODE_LIST);
		}
	}

	public void enableLoadItins() {
		mAllowLoadItins = true;
		syncItinManager(false, false);
	}

	public synchronized void startAddGuestItinActivity() {
		Intent intent = new Intent(getActivity(), ItineraryGuestAddActivity.class);
		startActivity(intent);
	}

	public synchronized void startLoginActivity() {
		Intent intent = LoginActivity.createIntent(getActivity(), LineOfBusiness.ITIN,
				new ItineraryLoaderLoginExtender());
		startActivity(intent);
	}

	private void updateLoginState() {
		if (User.isLoggedIn(getActivity()) && Db.getUser() != null) {
			mLoginButton.setVisibility(View.GONE);
			mNoTripsRefreshButton.setVisibility(mShowError ? View.GONE : View.VISIBLE);
		}
		else {
			mLoginButton.setVisibility(mShowError ? View.GONE : View.VISIBLE);
			mNoTripsRefreshButton.setVisibility(View.GONE);
		}
	}

	public void accountLogoutClicked() {
		if (!User.isLoggedIn(getActivity())) {
			doLogout();
			return;
		}
		if (Db.getUser() == null) {
			Db.loadUser(getActivity());
		}
		String email = Db.getUser().getPrimaryTraveler().getEmail();
		String logoutMessage = getResources().getString(R.string.itin_logout_confirmation_message_TEMPLATE, email);
		ConfirmLogoutDialogFragment df = ConfirmLogoutDialogFragment.getInstance(logoutMessage);
		df.show(getFragmentManager(), ConfirmLogoutDialogFragment.TAG);
	}

	public void setErrorMessage(int messageId, boolean showError) {
		setErrorMessage(getString(messageId), showError);
	}

	public void setErrorMessage(String message, boolean showError) {
		mShowError = showError;
		mErrorMessage = message;

		mErrorTv.setText(mErrorMessage != null ? mErrorMessage : "");
		mErrorContainer.setVisibility(mShowError ? View.VISIBLE : View.GONE);
		mErrorMask.setVisibility(mShowError ? View.VISIBLE : View.GONE);

		updateLoginState();
	}

	@Override
	public void doLogout() {
		// Note: On 2.x, the user can logout from the expanded details view, be sure to collapse the view so when we
		// re-populate the ListView with data, it does not think there is something expanded.
		if (mItinListView != null) {
			mItinListView.hideDetails();
		}

		// Sign out user
		User.signOut(getActivity());

		// Update UI
		updateLoginState();

		setErrorMessage(null, false);

		invalidateOptionsMenu();

		syncItinManager(true, false);
	}

	public void onLoginCompleted() {
		updateLoginState();

		syncItinManager(true, false);

		invalidateOptionsMenu();
	}

	private OnListModeChangedListener mOnListModeChangedListener = new OnListModeChangedListener() {
		@Override
		public void onListModeChanged(int mode) {
			// In some bad timing situations, it's possible for the listener to fire
			// far after this Fragment is dead in the eyes of its Activity.  In that
			// case, don't do the list mode change (as it requires being attached).
			Activity activity = getActivity();
			if (getActivity() == null) {
				return;
			}

			if (mode == ItinListView.MODE_LIST) {
				getSupportActionBar().show();
				if (activity instanceof OnListModeChangedListener) {
					((OnListModeChangedListener) activity).onListModeChanged(mode);
				}
			}
			else if (mode == ItinListView.MODE_DETAIL) {
				if (activity instanceof OnListModeChangedListener) {
					((OnListModeChangedListener) activity).onListModeChanged(mode);
				}
				getSupportActionBar().hide();
			}
		}
	};

	private OnItinCardClickListener mOnItinCardClickListener = new OnItinCardClickListener() {
		@Override
		public void onCloseButtonClicked() {
		}

		@Override
		public void onShareButtonClicked(ItinContentGenerator<?> generator) {
			SocialMessageChooserDialogFragment.newInstance(generator).show(getFragmentManager(), DIALOG_SHARE);
		}
	};

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (mListener != null) {
				mListener.onItinCardClicked(mItinListView.getItinCardData(position));
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Access into SherlockFragmentActivity

	private void invalidateOptionsMenu() {
		if (getActivity() != null) {
			((SherlockFragmentActivity) getActivity()).supportInvalidateOptionsMenu();
		}
	}

	private ActionBar getSupportActionBar() {
		return ((SherlockFragmentActivity) getActivity()).getSupportActionBar();
	}

	//////////////////////////////////////////////////////////////////////////
	// ItinerarySyncListener

	@Override
	public void onTripAdded(Trip trip) {
		// Do nothing
	}

	@Override
	public void onTripUpdated(Trip trip) {
		Log.d("ItinItemListFragment - onTripUpdated");
		OmnitureTracking.trackItinAdd(getActivity(), trip);
	}

	@Override
	public void onTripUpdateFailed(Trip trip) {
		// Do nothing
	}

	@Override
	public void onTripRemoved(Trip removedTrip) {
		// If there is an expanded card and the removedTrip matches the expanded card, make sure to collapse the card
		// otherwise weirdness ensues.
		Trip selectedTrip = null;
		if (mItinListView != null) {
			ItinCardData itinCardData = mItinListView.getSelectedItinCard();
			if (itinCardData != null && itinCardData.getTripComponent() != null) {
				selectedTrip = itinCardData.getTripComponent().getParentTrip();
			}
		}

		if (selectedTrip != null) {
			if (removedTrip.isGuest()) {
				if (removedTrip.isSameGuest(selectedTrip)) {
					mItinListView.hideDetails();
				}
			}
			else {
				if (removedTrip.getTripId() != null && removedTrip.getTripId().equals(selectedTrip.getTripId())) {
					mItinListView.hideDetails();
				}
			}
		}
	}

	@Override
	public void onSyncFailure(SyncError error) {
		setIsLoading(false);
		setErrorMessage(R.string.itinerary_fetch_error, User.isLoggedIn(getActivity()));
	}

	@Override
	public void onSyncFinished(Collection<Trip> trips) {
		setIsLoading(false);
		setErrorMessage(null, false);

		trackWithOmniture(false);

		if (mJumpToItinId != null) {
			showItinCard(mJumpToItinId);
		}
	}

	private void trackWithOmniture(boolean trackEmpty) {
		if (mAllowLoadItins) {
			ItineraryManager im = ItineraryManager.getInstance();
			Collection<Trip> trips = im.getTrips();
			Context context = getActivity();
			if (context != null) {
				if (trips.size() > 0) {
					OmnitureTracking.trackItin(getActivity());
					AdTracker.trackViewItinList();
				}
				else {
					if (trackEmpty) {
						OmnitureTracking.trackItinEmpty(getActivity());
					}
				}
			}
		}
	}

	//////////////////////////////////////////
	// INTERFACES

	/**
	 * If we attach to an activity that implements this we will notify that activity we are attached.
	 * This is useful for getting references to fragments that are in viewpagers
	 */
	public interface ItinItemListFragmentListener {
		public void onItinItemListFragmentAttached(ItinItemListFragment frag);

		public void onItinCardClicked(ItinCardData data);
	}
}
