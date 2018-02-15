package com.expedia.bookings.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncListener;
import com.expedia.bookings.data.trips.ItineraryManager.SyncError;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.itin.ItinPageUsableTracking;
import com.expedia.bookings.itin.activity.FlightItinDetailsActivity;
import com.expedia.bookings.itin.activity.HotelItinDetailsActivity;
import com.expedia.bookings.itin.activity.NewAddGuestItinActivity;
import com.expedia.bookings.itin.data.ItinCardDataHotel;
import com.expedia.bookings.presenter.trips.ItinSignInPresenter;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FragmentModificationSafeLock;
import com.expedia.bookings.utils.ProWizardBucketCache;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.ItineraryLoaderLoginExtender;
import com.expedia.bookings.widget.itin.ItinListView;
import com.expedia.vm.UserReviewDialogViewModel;
import com.mobiata.android.app.SimpleDialogFragment;

import java.util.Collection;
import java.util.List;

import io.reactivex.functions.Consumer;
import kotlin.Unit;

public class ItinItemListFragment extends Fragment implements LoginConfirmLogoutDialogFragment.DoLogoutListener,
	ItinerarySyncListener {

	public static final String TAG = "TAG_ITIN_ITEM_LIST_FRAGMENT";

	public static final String ARG_JUMP_TO_UNIQUE_ID = "JUMP_TO_UNIQUE_ID";

	private static final String STATE_CURRENT_STATE = "STATE_CURRENT_STATE";
	private static final String STATE_ALLOW_LOAD_ITINS = "STATE_ALLOW_LOAD_ITINS";
	private static final String STATE_ITIN_LIST_TRACKED = "STATE_ITIN_LIST_TRACKED";
	private static final String STATE_JUMP_TO_UNIQUE_ID = "STATE_JUMP_TO_UNIQUE_ID";
	private static final String STATE_NUMBER_ITIN_CARD_GUEST_USER = "STATE_NUMBER_ITIN_CARD_GUEST_USER";

	private ItinItemListFragmentListener mListener;

	private Toolbar tripToolbar;
	private View mRoot;
	private ImageView mShadowImageView;
	private ItinListView mItinListView;
	private View mOrEnterNumberTv;
	private UserReviewRatingDialog ratingDialog;
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
	private boolean isAttached = false;
	private int mNumberOfItinCardsOfGuestUser = 0;

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

	private FragmentModificationSafeLock mFragmentModLock = new FragmentModificationSafeLock();

	/**
	 * Creates a new fragment that will open right away to the passed uniqueId.
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
		userStateManager = Ui.getApplication(context).appComponent().userStateManager();

		isAttached = true;
		getItineraryManager().addSyncListener(this);

		// Not a strict requirement
		if (context instanceof ItinItemListFragmentListener) {
			mListener = (ItinItemListFragmentListener) context;

			mListener.onItinItemListFragmentAttached(this);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_itinerary_list, null);

		tripToolbar = Ui.findView(view, R.id.trip_launch_toolbar);
		mRoot = Ui.findView(view, R.id.outer_container);
		mShadowImageView = Ui.findView(view, R.id.shadow_image_view);
		mItinListView = Ui.findView(view, android.R.id.list);
		mDeepRefreshLoadingView = Ui.findView(view, R.id.deep_refresh_loading_layout);

		mItinListView.getItinCardDataAdapter().registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				mRoot.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.exp_itin_bg_crystal_theme));
			}
		});

		mOrEnterNumberTv = Ui.findView(view, R.id.or_enter_itin_number_tv);
		mEmptyListLoadingContainer = Ui.findView(view, R.id.empty_list_loading_container);
		mEmptyListContent = Ui.findView(view, R.id.empty_list_content);
		mStatusRefreshButton = Ui.findView(view, R.id.status_refresh_button);
		mStatusText = Ui.findView(view, R.id.no_upcoming_trips);
		mStatusImage = Ui.findView(view, R.id.no_trips_image);
		mFindItineraryButton = Ui.findView(view, R.id.find_itinerary_button);

		mItinListView.setOnItemClickListener(mOnItemClickListener);
		mOldEmptyView =  Ui.findView(view, R.id.old_sign_in_view);

		View guestItinView = inflater.inflate(R.layout.add_guest_itin, null);
		mItinListView.addFooterView(guestItinView);
		com.expedia.bookings.widget.TextView guestItinTextView = Ui.findView(view, R.id.add_guest_itin_text_view);

		guestItinTextView.setCompoundDrawablesTint(ContextCompat.getColor(getContext(), R.color.itin_add_guest_text_button_color));
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
			mNumberOfItinCardsOfGuestUser = savedInstanceState.getInt(STATE_NUMBER_ITIN_CARD_GUEST_USER);
		}
		else if (getArguments() != null) {
			mJumpToItinId = getArguments().getString(ARG_JUMP_TO_UNIQUE_ID);
		}

		mOrEnterNumberTv.setVisibility(View.VISIBLE);
		mFindItineraryButton.setVisibility(View.GONE);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (ProWizardBucketCache.isBucketed(getContext())) {
			tripToolbar.setVisibility(View.VISIBLE);
		}
		else {
			tripToolbar.setVisibility(View.GONE);
		}
	}

	public void showAddGuestItinScreen() {
		Intent intent = new Intent(getActivity(), NewAddGuestItinActivity.class);
		startActivity(intent);
	}

	private void setSignInView(View rootView) {
		View mEmptyView;
		if (mSignInPresenter == null) {
			ViewStub viewStub = Ui.findView(rootView, R.id.sign_in_presenter_stub);
			mSignInPresenter = (ItinSignInPresenter) viewStub.inflate();
			getItineraryManager().addSyncListener(mSignInPresenter.getSyncListenerAdapter());
			mSignInPresenter.getSignInWidget().getViewModel().getSyncItinManagerSubject().subscribe(
				new Consumer<Unit>() {
					@Override
					public void accept(Unit unit) {
						syncItinManager(true, true);
					}
				});
		}
		Collection<Trip> trips = ItineraryManager.getInstance().getTrips();
		mSignInPresenter.getSignInWidget().getViewModel().newTripsUpdateState(trips);

		mEmptyView = mSignInPresenter;

		if (mSignInPresenter != null) {
			mSignInPresenter.setVisibility(View.GONE);
		}
		mOldEmptyView.setVisibility(View.GONE);
		mItinListView.setEmptyView(mEmptyView);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!userStateManager.isUserAuthenticated()) {
			mNumberOfItinCardsOfGuestUser = getItinCardCount();
		}

		setSignInView(getView());
		syncItinManager(true, false);

		if (mJumpToItinId != null) {
			showItinCard(mJumpToItinId);
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
		outState.putInt(STATE_NUMBER_ITIN_CARD_GUEST_USER, mNumberOfItinCardsOfGuestUser);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		getItineraryManager().removeSyncListener(this);
		isAttached = false;
	}

	public int getItinCardCount() {
		int retVal = -1;
		if (mItinListView != null) {
			retVal = mItinListView.getCount() - mItinListView.getFooterViewsCount()
				- mItinListView.getHeaderViewsCount();
		}
		return retVal;
	}

	public void goToItin(String itinId) {
		Boolean isFlightDetailOn = AbacusFeatureConfigManager
			.isBucketedForTest(getContext(), AbacusUtils.TripsFlightsNewDesign);
		ItinCardData data = ItineraryManager.getInstance().getItinCardDataFromItinId(itinId);
		if (data instanceof ItinCardDataHotel) {
			startActivity(HotelItinDetailsActivity.createIntent(getContext(), data.getId()));
		}
		else if (data instanceof ItinCardDataFlight && isFlightDetailOn) {
			startActivity(FlightItinDetailsActivity.createIntent(getContext(), data.getId()));
		}
		else {
			showItinCard(itinId);
		}
	}

	// Can only be called after onCreateView(); not an issue right now, if it becomes
	// one we can update the code.
	public void setBackgroundColor(int color) {
		mRoot.setBackgroundColor(color);
	}

	public void showItinCard(final String id) {
		if (mIsLoading || mItinListView == null) {
			mJumpToItinId = id;
			return;
		}

		// ItinListView will take care of executing these in order.
		String itinId = getItineraryManager().getItinIdByTripNumber(id);
		if (itinId == null) {
			itinId = id;
		}
		mItinListView.showDetails(itinId);

		mJumpToItinId = null;
	}

	public void syncItinManager(boolean forceRefresh, boolean showLoading) {
		if (mAllowLoadItins && mItinListView != null && isAttached) {
			boolean syncing = getItineraryManager().startSync(forceRefresh);
			setIsLoading(syncing);
			if (syncing && (showLoading || getItinCardCount() <= 0 || mNumberOfItinCardsOfGuestUser > 0)) {
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

		if (isLoading && mSignInPresenter != null) {
			if (mNumberOfItinCardsOfGuestUser > 0) {
				mItinListView.getItinCardDataAdapter().clearAdapter();
			}
			mSignInPresenter.showItinFetchProgress();
		}
	}

	public boolean isLoading() {
		return mIsLoading;
	}

	public void enableLoadItins() {
		mAllowLoadItins = true;
		if (mNumberOfItinCardsOfGuestUser == 0) {
			syncItinManager(false, false);
		}
	}

	public synchronized void startLoginActivity() {
		Bundle args = AccountLibActivity.createArgumentsBundle(LineOfBusiness.ITIN, new ItineraryLoaderLoginExtender());
		userStateManager.signIn(getActivity(), args);
	}

	@NonNull
	protected ItineraryManager getItineraryManager() {
		return ItineraryManager.getInstance();
	}

	private void updateLoginState() {
		User user = userStateManager.getUserSource().getUser();

		if (userStateManager.isUserAuthenticated() && user != null) {
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

		User user = userStateManager.getUserSource().getUser();

		String email = null;

		if (user != null) {
			email = user.getPrimaryTraveler().getEmail();
		}

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

			//Make it invisible so nobody clicks anything
			mItinListView.getItinCardDataAdapter().clearAdapter();
		}

		// Sign out user
		userStateManager.signOut();

		syncItinManager(true, false);

		updateLoginState();

		invalidateOptionsMenu();
	}

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (mListener != null) {
				mListener.onItinCardClicked(mItinListView.getItinCardData(position));
			}
		}
	};

	// Animations

	//////////////////////////////////////////////////////////////////////////
	// Access into FragmentActivity

	private void invalidateOptionsMenu() {
		if (getActivity() != null) {
			getActivity().invalidateOptionsMenu();
		}
	}

	private int getToolbarHeight() {
		if (ProWizardBucketCache.isBucketed(getContext())) {
			return tripToolbar.getHeight();
		}
		else {
			ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
			int ret = ab == null ? 0 : ab.getHeight();
			return ret;
		}
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
	}

	@Override
	public void onTripFailedFetchingRegisteredUserItinerary() {
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
		if (userStateManager.isUserAuthenticated()) {
			mNumberOfItinCardsOfGuestUser = 0;
		}
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
				showItinCard(mJumpToItinId);
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
			if (isResumed()) {
				trackItinPageUsable();
			}
		}
	}

	public void showUserReview() {
		if (getActivity() != null && UserReviewDialogViewModel.shouldShowReviewDialog(getActivity())) {
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

	private void trackItinPageUsable() {
		ItinPageUsableTracking pageUsableTracking = getItinPageUsableTracking();
		if (pageUsableTracking != null) {
			List<ItinCardData> dataList = getItineraryManager().getItinCardData();
			if (dataList != null && dataList.size() > 0) {
				pageUsableTracking.markTripResultsUsable(System.currentTimeMillis());
				pageUsableTracking.trackIfReady(getItineraryManager().getItinCardData());
			}
		}
	}

	@Nullable
	protected ItinPageUsableTracking getItinPageUsableTracking() {
		com.expedia.bookings.dagger.TripComponent tripComponent = Ui.getApplication(getContext()).tripComponent();
		if (tripComponent != null) {
			return tripComponent.itinPageUsableTracking();
		}
		else {
			return null;
		}
	}
}
