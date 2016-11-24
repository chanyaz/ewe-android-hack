package com.expedia.bookings.fragment;

import java.util.Collection;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.AccountLibActivity;
import com.expedia.bookings.activity.ItineraryGuestAddActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncListener;
import com.expedia.bookings.data.trips.ItineraryManager.SyncError;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FragmentModificationSafeLock;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.itin.ItinListView;
import com.expedia.bookings.widget.itin.ItinListView.OnListModeChangedListener;
import com.expedia.bookings.widget.ItineraryLoaderLoginExtender;
import com.mobiata.android.app.SimpleDialogFragment;
import com.mobiata.android.util.AndroidUtils;

public class ItinItemListFragment extends Fragment implements LoginConfirmLogoutDialogFragment.DoLogoutListener,
	ItinerarySyncListener {

	public static final String TAG = "TAG_ITIN_ITEM_LIST_FRAGMENT";

	public static final String ARG_JUMP_TO_UNIQUE_ID = "JUMP_TO_UNIQUE_ID";

	private static final String STATE_ERROR_MESSAGE = "STATE_ERROR_MESSAGE";
	private static final String STATE_ALLOW_LOAD_ITINS = "STATE_ALLOW_LOAD_ITINS";
	private static final String STATE_ITIN_LIST_TRACKED = "STATE_ITIN_LIST_TRACKED";
	private static final String STATE_JUMP_TO_UNIQUE_ID = "STATE_JUMP_TO_UNIQUE_ID";

	private ItinItemListFragmentListener mListener;

	private View mRoot;
	private View mSpacerView;
	private ImageView mShadowImageView;
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
	private Button mFindItineraryButton;

	private String mErrorMessage;
	private boolean mShowError = false;
	private boolean mAllowLoadItins = false;

	private boolean mCurrentSyncHasErrors = false;
	private boolean mIsLoading = false;
	private String mJumpToItinId = null;

	//Have we tracked this itin list view yet?
	private boolean mItinListTracked = false;
	// should be removed when we remove launch screen AB test
	private static boolean fromNewLaunchScreen = false;

	private FragmentModificationSafeLock mFragmentModLock = new FragmentModificationSafeLock();

	/**
	 * Creates a new fragment that will open right away to the passed uniqueId.
	 *
	 * @param uniqueId
	 * @return
	 */
	public static ItinItemListFragment newInstance(String uniqueId, boolean newLaunchScreen) {
		ItinItemListFragment frag = new ItinItemListFragment();
		Bundle args = new Bundle();
		args.putString(ARG_JUMP_TO_UNIQUE_ID, uniqueId);
		frag.setArguments(args);
		fromNewLaunchScreen = newLaunchScreen;
		return frag;
	}

	public static ItinItemListFragment newInstance() {
		return new ItinItemListFragment();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		mItinManager = ItineraryManager.getInstance();
		mItinManager.addSyncListener(this);

		// Not a strict requirement
		if (context instanceof ItinItemListFragmentListener) {
			mListener = (ItinItemListFragmentListener) context;

			mListener.onItinItemListFragmentAttached(this);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_itinerary_list, null);

		mRoot = Ui.findView(view, R.id.outer_container);
		mSpacerView = Ui.findView(view, R.id.spacer_view);
		mShadowImageView = Ui.findView(view, R.id.shadow_image_view);
		mItinListView = Ui.findView(view, android.R.id.list);
		mEmptyView = Ui.findView(view, android.R.id.empty);
		mOrEnterNumberTv = Ui.findView(view, R.id.or_enter_itin_number_tv);
		mEmptyListLoadingContainer = Ui.findView(view, R.id.empty_list_loading_container);
		mEmptyListContent = Ui.findView(view, R.id.empty_list_content);
		mLoginButton = Ui.findView(view, R.id.login_button);
		mLoginButton.setText(HtmlCompat.fromHtml(getString(R.string.sign_in_for_your_trips)));
		mNoTripsRefreshButton = Ui.findView(view, R.id.no_trips_refresh_button);
		mNoTripsTryAgainButton = Ui.findView(view, R.id.no_trips_try_again_button);
		mErrorTv = Ui.findView(view, R.id.no_trips_error_message);
		mErrorMask = Ui.findView(view, R.id.empty_list_error_mask);
		mErrorContainer = Ui.findView(view, R.id.error_container);
		mFindItineraryButton = Ui.findView(view, R.id.find_itinerary_button);

		mItinListView.setEmptyView(mEmptyView);
		mItinListView.setOnListModeChangedListener(mOnListModeChangedListener);
		mItinListView.setOnItemClickListener(mOnItemClickListener);

		View guestItinView = inflater.inflate(R.layout.add_guest_itin, null);
		mItinListView.addFooterView(guestItinView);
		View guestItinTextView = Ui.findView(view, R.id.add_guest_itin_text_view);
		guestItinTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startAddGuestItinActivity(false);
			}
		});

		mLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startLoginActivity();
			}
		});

		if (fromNewLaunchScreen) {
			mLoginButton.setBackgroundResource(R.drawable.new_launch_screen_itin_login_ripple);
			mNoTripsRefreshButton.setBackgroundResource(R.drawable.new_launch_screen_itin_login_ripple);
		}

		mOrEnterNumberTv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startAddGuestItinActivity(false);
			}
		});
		mFindItineraryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startAddGuestItinActivity(false);
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
			mItinListTracked = savedInstanceState.getBoolean(STATE_ITIN_LIST_TRACKED, false);
			mJumpToItinId = savedInstanceState.getString(STATE_JUMP_TO_UNIQUE_ID);
		}
		else if (getArguments() != null) {
			mJumpToItinId = getArguments().getString(ARG_JUMP_TO_UNIQUE_ID);
		}

		boolean isSignInEnabled = ProductFlavorFeatureConfiguration.getInstance().isSigninEnabled();
		mLoginButton.setVisibility(isSignInEnabled ? View.VISIBLE : View.GONE);
		mOrEnterNumberTv.setVisibility(isSignInEnabled ? View.VISIBLE : View.GONE);
		mFindItineraryButton.setVisibility(isSignInEnabled ? View.GONE : View.VISIBLE);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		updateLoginState();

		syncItinManager(true, false);

		if (mJumpToItinId != null) {
			showItinCard(mJumpToItinId, true);
		}

		mFragmentModLock.setSafe(true);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		mFragmentModLock.setSafe(false);
		super.onSaveInstanceState(outState);

		if (mShowError && mErrorMessage != null) {
			outState.putString(STATE_ERROR_MESSAGE, mErrorMessage);
		}
		outState.putBoolean(STATE_ALLOW_LOAD_ITINS, mAllowLoadItins);
		outState.putBoolean(STATE_ITIN_LIST_TRACKED, mItinListTracked);
		outState.putString(STATE_JUMP_TO_UNIQUE_ID, mJumpToItinId);
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

		if (enabled) {
			mSpacerView.setVisibility(View.GONE);
			mShadowImageView.setVisibility(View.GONE);
		}
		else if (!isInDetailMode()) {
			mSpacerView.setVisibility(View.VISIBLE);
			mShadowImageView.setVisibility(View.VISIBLE);
		}
	}

	public void showItinCard(final String id, final boolean animate) {
		if (mIsLoading || mItinListView == null) {
			mJumpToItinId = id;
			return;
		}

		// ItinListView will take care of executing these in order.
		mItinListView.hideDetails(false);
		mItinListView.showDetails(id, animate);

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
				trackItins(true);
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

	public boolean isInDetailMode() {
		// false when mItinListView == null because we start out in list mode
		return mItinListView != null && mItinListView.isInDetailMode();
	}

	public void hideDetails() {
		if (mItinListView != null) {
			mItinListView.hideDetails(true);
		}
	}

	public void enableLoadItins() {
		mAllowLoadItins = true;
		syncItinManager(false, false);
	}

	public void disableLoadItins() {
		mAllowLoadItins = false;
	}

	public synchronized void startAddGuestItinActivity(boolean isFetchGuestItinFailure) {
		Intent intent = new Intent(getActivity(), ItineraryGuestAddActivity.class);
		if (isFetchGuestItinFailure) {
			intent.setAction(ItineraryGuestAddActivity.ERROR_FETCHING_GUEST_ITINERARY);
		}
		OmnitureTracking.trackFindItin();
		startActivity(intent);
	}

	public synchronized void startAddRegisteredUserItinActivity() {
		Intent intent = new Intent(getActivity(), ItineraryGuestAddActivity.class);
		intent.setAction(ItineraryGuestAddActivity.ERROR_FETCHING_REGISTERED_USER_ITINERARY);
		OmnitureTracking.trackFindItin();
		startActivity(intent);
	}

	public synchronized void startLoginActivity() {
		Bundle args = AccountLibActivity.createArgumentsBundle(LineOfBusiness.ITIN, new ItineraryLoaderLoginExtender());
		User.signIn(getActivity(), args);
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
		String logoutMessage = getResources().getString(R.string.itin_sign_out_confirmation_message_TEMPLATE, email);
		LoginConfirmLogoutDialogFragment df = LoginConfirmLogoutDialogFragment.getInstance(logoutMessage);
		df.show(getFragmentManager(), LoginConfirmLogoutDialogFragment.TAG);
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
		setErrorMessage(null, false);

		// Note: On 2.x, the user can logout from the expanded details view, be sure to collapse the view so when we
		// re-populate the ListView with data, it does not think there is something expanded.
		if (mItinListView != null) {
			mItinListView.hideDetails(false);

			//Make it invisible so nobody clicks anything
			mItinListView.getItinCardDataAdapter().clearAdapter();
		}

		// Sign out user
		User.signOut(getActivity());

		syncItinManager(true, false);
		AdTracker.trackLogout();

		updateLoginState();

		invalidateOptionsMenu();
	}

	private OnListModeChangedListener mOnListModeChangedListener = new OnListModeChangedListener() {
		@Override
		public void onListModeChanged(boolean isInDetailMode, final boolean animate) {
			// In some bad timing situations, it's possible for the listener to fire
			// far after this Fragment is dead in the eyes of its Activity.  In that
			// case, don't do the list mode change (as it requires being attached).
			Activity activity = getActivity();
			if (getActivity() == null) {
				return;
			}

			if (!animate) {
				mSpacerView.setVisibility(isInDetailMode ? View.GONE : View.VISIBLE);
			}
			else if (isInDetailMode) {
				mSpacerView.post(new Runnable() {
					@Override
					public void run() {
						getExpandAnimatorSet().start();
					}
				});
			}
			else {
				mSpacerView.post(new Runnable() {
					@Override
					public void run() {
						getCollapseAnimatorSet().start();
					}
				});
			}

			if (activity instanceof OnListModeChangedListener) {
				((OnListModeChangedListener) activity).onListModeChanged(isInDetailMode, animate);
			}
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

	// Animations

	private AnimatorSet getCollapseAnimatorSet() {
		final int actionBarHeight = getSupportActionBarHeight();

		mSpacerView.setVisibility(View.VISIBLE);

		ObjectAnimator pagerSlideDown = ObjectAnimator.ofFloat(mItinListView, "translationY", -actionBarHeight, 0);
		ObjectAnimator shadowSlideDown = ObjectAnimator.ofFloat(mShadowImageView, "translationY", -actionBarHeight, 0);

		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(pagerSlideDown, shadowSlideDown);
		animatorSet.setDuration(400);

		return animatorSet;
	}

	private AnimatorSet getExpandAnimatorSet() {
		final int actionBarHeight = getSupportActionBarHeight();

		mSpacerView.setVisibility(View.GONE);

		ObjectAnimator pagerSlideUp = ObjectAnimator.ofFloat(mItinListView, "translationY", actionBarHeight, 0);
		ObjectAnimator shadowSlideUp = ObjectAnimator.ofFloat(mShadowImageView, "translationY", actionBarHeight, 0);

		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(pagerSlideUp, shadowSlideUp);
		animatorSet.setDuration(400);

		return animatorSet;
	}

	//////////////////////////////////////////////////////////////////////////
	// Access into FragmentActivity

	private void invalidateOptionsMenu() {
		if (getActivity() != null) {
			((FragmentActivity) getActivity()).invalidateOptionsMenu();
		}
	}

	private int getSupportActionBarHeight() {
		int ret;
		// Tablet's ItineraryActivity is not an ActionBarActivity yet.
		if (AndroidUtils.isTablet(getActivity())) {
			ret = getActivity().getActionBar() == null ? 0 : getActivity().getActionBar().getHeight();
		}
		else {
			ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
			ret = ab == null ? 0 : ab.getHeight();
		}
		return ret;
	}

	//////////////////////////////////////////////////////////////////////////
	// ItinerarySyncListener

	@Override
	public void onTripAdded(Trip trip) {
		// Do nothing
	}

	@Override
	public void onTripUpdated(Trip trip) {
		OmnitureTracking.trackItinAdd(trip);
	}

	@Override
	public void onTripFailedFetchingGuestItinerary() {
		boolean isFetchGuestItinFailure = true;
		startAddGuestItinActivity(isFetchGuestItinFailure);
	}

	@Override
	public void onTripFailedFetchingRegisteredUserItinerary() {
		startAddRegisteredUserItinActivity();
	}

	@Override
	public void onTripUpdateFailed(Trip trip) {
		// Do nothing
	}

	@Override
	public void onTripRemoved(Trip removedTrip) {
		// Do nothing
	}

	@Override
	public void onSyncFailure(SyncError error) {
		mCurrentSyncHasErrors = true;
	}

	private static final String COMPLETED_TRIP_DIALOG_TAG = "USER_ADDED_COMPLETED_TRIP_DIALOG";
	private static final String CANCELLED_TRIP_DIALOG_TAG = "USER_ADDED_CANCELLED_TRIP_DIALOG";

	@Override
	public void onCompletedTripAdded(Trip trip) {
		mFragmentModLock.runWhenSafe(new Runnable() {
			@Override
			public void run() {
				SimpleDialogFragment df = SimpleDialogFragment.newInstance(null,
					getString(R.string.viewing_completed_itineraries_not_yet_supported));
				df.show(getFragmentManager(), COMPLETED_TRIP_DIALOG_TAG);
			}
		});
	}

	@Override
	public void onCancelledTripAdded(Trip trip) {
		mFragmentModLock.runWhenSafe(new Runnable() {
			@Override
			public void run() {
				SimpleDialogFragment df = SimpleDialogFragment.newInstance(null,
					getString(R.string.viewing_cancelled_itineraries_not_yet_supported));
				df.show(getFragmentManager(), CANCELLED_TRIP_DIALOG_TAG);
			}
		});
	}

	@Override
	public void onSyncFinished(Collection<Trip> trips) {
		mItinListView.syncWithManager();
		setIsLoading(false);
		if (mCurrentSyncHasErrors && (trips == null || trips.size() == 0)) {
			setErrorMessage(R.string.itinerary_fetch_error, true);
		}
		else {
			setErrorMessage(null, false);
			trackItins(false);

			if (mJumpToItinId != null) {
				showItinCard(mJumpToItinId, true);
			}
		}
		mCurrentSyncHasErrors = false;
	}

	public void resetTrackingState() {
		mItinListTracked = false;
	}

	private void trackItins(boolean trackEmpty) {
		if (mAllowLoadItins) {
			ItineraryManager im = ItineraryManager.getInstance();
			Collection<Trip> trips = im.getTrips();
			Context context = getActivity();
			if (context != null) {
				if (trips.size() > 0) {
					OmnitureTracking.trackItin(getActivity());
				}
				else {
					if (trackEmpty) {
						OmnitureTracking.trackItinEmpty();
					}
				}

				//AdX we just want to track when the user goes to the page.
				if (!mItinListTracked) {
					mItinListTracked = true;
					AdTracker.trackViewItinList();
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
		void onItinItemListFragmentAttached(ItinItemListFragment frag);

		void onItinCardClicked(ItinCardData data);
	}
}
