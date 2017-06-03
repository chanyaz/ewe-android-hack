package com.expedia.bookings.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
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
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncListener;
import com.expedia.bookings.data.trips.ItineraryManager.SyncError;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.itin.activity.NewAddGuestItinActivity;
import com.expedia.bookings.presenter.trips.ItinSignInPresenter;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FragmentModificationSafeLock;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.ItineraryLoaderLoginExtender;
import com.expedia.bookings.widget.itin.ItinListView;
import com.expedia.bookings.widget.itin.ItinListView.OnListModeChangedListener;
import com.expedia.vm.UserReviewDialogViewModel;
import com.mobiata.android.app.SimpleDialogFragment;
import java.util.Collection;
import kotlin.Unit;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

public class ItinItemListFragment extends Fragment implements LoginConfirmLogoutDialogFragment.DoLogoutListener,
	ItinerarySyncListener {

	public static final String TAG = "TAG_ITIN_ITEM_LIST_FRAGMENT";

	public static final String ARG_JUMP_TO_UNIQUE_ID = "JUMP_TO_UNIQUE_ID";

	private static final String STATE_CURRENT_STATE = "STATE_CURRENT_STATE";
	private static final String STATE_ALLOW_LOAD_ITINS = "STATE_ALLOW_LOAD_ITINS";
	private static final String STATE_ITIN_LIST_TRACKED = "STATE_ITIN_LIST_TRACKED";
	private static final String STATE_JUMP_TO_UNIQUE_ID = "STATE_JUMP_TO_UNIQUE_ID";

	private ItinItemListFragmentListener mListener;

	private View mRoot;
	private ImageView mShadowImageView;
	private ItinListView mItinListView;
	private View mOrEnterNumberTv;
	private UserReviewRatingDialog ratingDialog;
	private ItineraryManager mItinManager;
	private ViewGroup mEmptyListLoadingContainer;
	private ViewGroup mEmptyListContent;
	private Button mStatusRefreshButton;
	private TextView mStatusText;
	private ImageView mStatusImage;
	private Button mFindItineraryButton;
	public ItinSignInPresenter mSignInPresenter;
	public View mOldEmptyView;
	private FrameLayout mDeepRefreshLoadingView;

	private boolean mAllowLoadItins = false;

	private boolean mCurrentSyncHasErrors = false;
	private boolean mIsLoading = false;
	private String mJumpToItinId = null;
	private UserStateManager userStateManager;

	//Have we tracked this itin list view yet?
	private boolean mItinListTracked = false;
	// should be removed when we remove launch screen AB test
	private static boolean fromNewLaunchScreen = false;

	private MessageState mCurrentState = MessageState.NONE;

	private enum MessageState {
		NOT_LOGGED_IN,
		NO_UPCOMING_TRIPS,
		TRIPS_ERROR,
		FAILURE,
		NONE
	}

	public BehaviorSubject<Boolean> toolBarVisibilitySubject = BehaviorSubject.create();

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

	@VisibleForTesting
	protected ItinListView getItinListView() {
		return mItinListView;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		userStateManager = Ui.getApplication(context).appComponent().userStateManager();

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
		mShadowImageView = Ui.findView(view, R.id.shadow_image_view);
		mItinListView = Ui.findView(view, android.R.id.list);
		mDeepRefreshLoadingView = Ui.findView(view, R.id.deep_refresh_loading_layout);

		mItinListView.getItinCardDataAdapter().registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				super.onChanged();

				/**
				 In the case when the user is Bucketed for Crystal Trips but not for New SignIn Widget then
				 we have to make sure that the Background color is set appropriately. i.e. dark (older itin bg color) when list is empty.
				 But make sure to update it to show the Crystal Trips background color if there happens to be a show (list is populated)
				 */
				if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppItinCrystalSkin)) {
					if (mItinListView.getItinCardDataAdapter().getCount() == 0) {
						mRoot.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.exp_itin_bg));
					}
					else {
						mRoot.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.exp_itin_bg_crystal_theme));
					}
				}
			}
		});

		mOrEnterNumberTv = Ui.findView(view, R.id.or_enter_itin_number_tv);
		mEmptyListLoadingContainer = Ui.findView(view, R.id.empty_list_loading_container);
		mEmptyListContent = Ui.findView(view, R.id.empty_list_content);
		mStatusRefreshButton = Ui.findView(view, R.id.status_refresh_button);
		mStatusText = Ui.findView(view, R.id.no_upcoming_trips);
		mStatusImage = Ui.findView(view, R.id.no_trips_image);
		mFindItineraryButton = Ui.findView(view, R.id.find_itinerary_button);

		mItinListView.setOnListModeChangedListener(mOnListModeChangedListener);
		mItinListView.setOnItemClickListener(mOnItemClickListener);
		mOldEmptyView =  Ui.findView(view, R.id.old_sign_in_view);

		View guestItinView = inflater.inflate(R.layout.add_guest_itin, null);
		mItinListView.addFooterView(guestItinView);
		View guestItinTextView = Ui.findView(view, R.id.add_guest_itin_text_view);

		if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppItinCrystalSkin)) {
			View itinListDivider = Ui.findView(guestItinView, R.id.itin_list_divider);
			itinListDivider.setVisibility(View.GONE);
		}

		guestItinTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddGuestItinScreen();
			}
		});

		if (fromNewLaunchScreen) {
			mStatusRefreshButton.setBackgroundResource(R.drawable.new_launch_screen_itin_login_ripple);
		}

		mOrEnterNumberTv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showAddGuestItinScreen();
			}
		});
		mFindItineraryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showAddGuestItinScreen();
			}
		});

		OnClickListener syncManagerClickListener = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mCurrentState == MessageState.NOT_LOGGED_IN) {
					startLoginActivity();
				}
				else {
					syncItinManager(true, true);
				}
			}
		};

		mStatusRefreshButton.setOnClickListener(syncManagerClickListener);

		if (savedInstanceState != null) {
			setState(MessageState.valueOf(savedInstanceState.getString(STATE_CURRENT_STATE)));
			mAllowLoadItins = savedInstanceState.getBoolean(STATE_ALLOW_LOAD_ITINS);
			mItinListTracked = savedInstanceState.getBoolean(STATE_ITIN_LIST_TRACKED, false);
			mJumpToItinId = savedInstanceState.getString(STATE_JUMP_TO_UNIQUE_ID);
		}
		else if (getArguments() != null) {
			mJumpToItinId = getArguments().getString(ARG_JUMP_TO_UNIQUE_ID);
		}

		boolean isSignInEnabled = ProductFlavorFeatureConfiguration.getInstance().isSigninEnabled();
		mOrEnterNumberTv.setVisibility(isSignInEnabled ? View.VISIBLE : View.GONE);
		mFindItineraryButton.setVisibility(isSignInEnabled ? View.GONE : View.VISIBLE);

		return view;
	}

	public void showAddGuestItinScreen() {
		if (isNewSignInScreen()) {
			Intent intent = new Intent(getActivity(), NewAddGuestItinActivity.class);
			startActivity(intent);
		}
		else {
			startAddGuestItinActivity(false);
		}
	}

	private void setSignInView(View rootView) {
		View mEmptyView;
		if (isNewSignInScreen()) {
			if (mSignInPresenter == null) {
				ViewStub viewStub = Ui.findView(rootView, R.id.sign_in_presenter_stub);
				mSignInPresenter = (ItinSignInPresenter) viewStub.inflate();
				mItinManager.addSyncListener(mSignInPresenter.getSyncListenerAdapter());
				mSignInPresenter.getAddGuestItinWidget().getViewModel().getToolBarVisibilityObservable().subscribe(
					new Action1<Boolean>() {
						@Override
						public void call(Boolean show) {
							toolBarVisibilitySubject.onNext(show);
							Ui.hideKeyboard(getActivity());
						}
					});
				mSignInPresenter.getSignInWidget().getViewModel().getSyncItinManagerSubject().subscribe(
					new Action1<Unit>() {
						@Override
						public void call(Unit unit) {
							syncItinManager(true, true);
						}
					});
			}
			Collection<Trip> trips = ItineraryManager.getInstance().getTrips();
			mSignInPresenter.getSignInWidget().getViewModel().newTripsUpdateState(trips);

			mEmptyView = mSignInPresenter;
		}
		else {
			mEmptyView = mOldEmptyView;
		}

		if (mSignInPresenter != null) {
			mSignInPresenter.setVisibility(View.GONE);
		}
		mOldEmptyView.setVisibility(View.GONE);
		mItinListView.setEmptyView(mEmptyView);
	}

	@Override
	public void onResume() {
		super.onResume();

		setSignInView(getView());
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
		outState.putString(STATE_CURRENT_STATE, mCurrentState.name());
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
			mShadowImageView.setVisibility(View.GONE);
		}
		else if (!isInDetailMode()) {
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
		String itinId = mItinManager.getItinIdByTripNumber(id);
		if (itinId == null) {
			itinId = id;
		}
		mItinListView.showDetails(itinId, animate);

		mJumpToItinId = null;
	}

	public ItinCardData getSelectedItinCardData() {
		return mItinListView.getSelectedItinCard();
	}

	public void syncItinManager(boolean forceRefresh, boolean showLoading) {
		if (mAllowLoadItins && mItinListView != null && mItinManager != null) {
			boolean syncing = mItinManager.startSync(forceRefresh);
			setIsLoading(syncing);
			if (syncing && (showLoading || getItinCardCount() <= 0)) {
				setIsLoading(true);
				mItinListView.enableScrollToRevelentWhenDataSetChanged();
			}
			else {
				invalidateOptionsMenu();
				trackItins(true);
				setIsLoading(false);
				updateLoginState();
			}
		}
	}

	public void setIsLoading(boolean isLoading) {
		mIsLoading = isLoading;
		mEmptyListLoadingContainer.setVisibility(isLoading ? View.VISIBLE : View.GONE);
		mEmptyListContent.setVisibility(isLoading ? View.GONE : View.VISIBLE);
		invalidateOptionsMenu();
		if (isNewSignInScreen() && isLoading && mSignInPresenter != null) {
			mSignInPresenter.getAddGuestItinWidget().getViewModel().getShowItinFetchProgressObservable().onNext(Unit.INSTANCE);
		}
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

	private boolean isNewSignInScreen() {
		return
			Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppTripsNewSignInPage);
	}

	private synchronized void startAddGuestItinActivity(boolean isFetchGuestItinFailure) {
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
		if (userStateManager.isUserAuthenticated() && Db.getUser() != null) {
			setState(MessageState.NO_UPCOMING_TRIPS);
		}
		else {
			setState(MessageState.NOT_LOGGED_IN);
		}
	}

	public void accountLogoutClicked() {
		if (!userStateManager.isUserAuthenticated()) {
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

	private void setState(MessageState state) {
		mCurrentState = state;
		switch (state) {
		case NOT_LOGGED_IN:
			updateMessageAndButton(R.string.no_upcoming_trips, R.string.sign_in_for_your_trips, R.drawable.ic_empty_itin_suitcase);
			break;
		case NO_UPCOMING_TRIPS:
			updateMessageAndButton(R.string.no_upcoming_trips, R.string.refresh_trips, R.drawable.ic_empty_itin_suitcase);
			break;
		case FAILURE:
			updateMessageAndButton(R.string.fetching_trips_error_connection, R.string.refresh_trips, R.drawable.ic_itin_connection_error);
			break;
		case TRIPS_ERROR:
			updateMessageAndButton(R.string.fetching_trips_error, R.string.refresh_trips, R.drawable.ic_itin_connection_error);
			break;
		case NONE:
			mStatusRefreshButton.setVisibility(View.GONE);
			mStatusText.setVisibility(View.GONE);
			break;
		}
	}

	private void updateMessageAndButton(int messageId, int buttonTextId, int imageResId) {
		updateMessageAndButton(getString(messageId), getString(buttonTextId), imageResId);
	}

	private void updateMessageAndButton(String messageText, String buttonText, int imageResId) {
		mStatusRefreshButton.setVisibility(View.VISIBLE);
		mStatusRefreshButton.setText(buttonText);
		mStatusText.setVisibility(View.VISIBLE);
		mStatusText.setText(messageText);
		mStatusImage.setImageResource(imageResId);
	}

	@Override
	public void doLogout() {
		setState(MessageState.NOT_LOGGED_IN);

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
				// do nothing
			}
			else if (isInDetailMode) {
				getView().post(new Runnable() {
					@Override
					public void run() {
						getExpandAnimatorSet().start();
					}
				});
			}
			else {
				getView().post(new Runnable() {
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

		ObjectAnimator pagerSlideDown = ObjectAnimator.ofFloat(mItinListView, "translationY", -actionBarHeight, 0);
		ObjectAnimator shadowSlideDown = ObjectAnimator.ofFloat(mShadowImageView, "translationY", -actionBarHeight, 0);

		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(pagerSlideDown, shadowSlideDown);
		animatorSet.setDuration(400);

		return animatorSet;
	}

	private AnimatorSet getExpandAnimatorSet() {
		final int actionBarHeight = getSupportActionBarHeight();

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
		ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
		int ret = ab == null ? 0 : ab.getHeight();
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
		showDeepRefreshLoadingView(false);
	}

	@Override
	public void onTripFailedFetchingGuestItinerary() {
		boolean isFetchGuestItinFailure = true;
		if (!isNewSignInScreen()) {
			startAddGuestItinActivity(isFetchGuestItinFailure);
		}
	}

	@Override
	public void onTripFailedFetchingRegisteredUserItinerary() {
		if (!isNewSignInScreen()) {
			startAddRegisteredUserItinActivity();
		}
	}

	@Override
	public void onTripUpdateFailed(Trip trip) {
		showDeepRefreshLoadingView(false);
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
		if (mCurrentSyncHasErrors) {
			if (trips == null) {
				setState(MessageState.TRIPS_ERROR);
			}
			else {
				setState(MessageState.FAILURE);
			}
		}
		else {
			if (trips.size() == 0) {
				setState(MessageState.NO_UPCOMING_TRIPS);
			}
			trackItins(false);
			updateLoginState();

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
				if (trips.size() < 0 && trackEmpty) {
					OmnitureTracking.trackItinEmpty();
				}

				//we just want to track when the user goes to the page.
				if (!mItinListTracked) {
					mItinListTracked = true;
					OmnitureTracking.trackItin(null);
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

	@Override
	public void setUserVisibleHint(boolean visible) {
		super.setUserVisibleHint(visible);
		if (visible) {
			showUserReview();
		}
	}

	public void showUserReview() {
		if (UserReviewDialogViewModel.shouldShowReviewDialog(getActivity())) {
			if (ratingDialog == null) {
				ratingDialog = new UserReviewRatingDialog(getActivity());
				ratingDialog.setViewModel(new UserReviewDialogViewModel(getActivity()));
			}
			ratingDialog.show();
		}
	}

	public void showDeepRefreshLoadingView(boolean show) {
		mDeepRefreshLoadingView.setVisibility(show ? View.VISIBLE : View.GONE);
	}

}
