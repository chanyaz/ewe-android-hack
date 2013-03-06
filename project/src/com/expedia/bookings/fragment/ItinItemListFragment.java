package com.expedia.bookings.fragment;

import java.util.Collection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ItineraryGuestAddActivity;
import com.expedia.bookings.activity.LoginActivity;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncListener;
import com.expedia.bookings.data.trips.ItineraryManager.SyncError;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.dialog.SocialMessageChooserDialogFragment;
import com.expedia.bookings.fragment.LoginFragment.PathMode;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.ItinCard.OnItinCardClickListener;
import com.expedia.bookings.widget.ItinListView;
import com.expedia.bookings.widget.ItinListView.OnListModeChangedListener;
import com.expedia.bookings.widget.ItineraryLoaderLoginExtender;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

public class ItinItemListFragment extends Fragment implements ConfirmLogoutDialogFragment.DoLogoutListener,
		ItinerarySyncListener {

	public static final String TAG = "TAG_ITIN_ITEM_LIST_FRAGMENT";
	public static final String DIALOG_SHARE = "DIALOG_SHARE";

	private static final String STATE_ERROR_MESSAGE = "STATE_ERROR_MESSAGE";

	private View mItinPathView;
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

	public static ItinItemListFragment newInstance() {
		return new ItinItemListFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mItinManager = ItineraryManager.getInstance();
		mItinManager.addSyncListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_itinerary_list, null);

		mItinPathView = Ui.findView(view, R.id.itin_path_view);
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
		mItinListView.setOnScrollListener(mOnScrollListener);
		mItinListView.setOnListModeChangedListener(mOnListModeChangedListener);
		mItinListView.setOnItinCardClickListener(mOnItinCardClickListener);
		mItinListView.setOnItemClickListener(mOnItemClickListener);
		mItinListView.post(new Runnable() {
			@Override
			public void run() {
				mItinListView.setExpandedCardHeight(view.getHeight() + getSupportActionBar().getHeight());
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
				syncItinManager();
			}
		};

		mNoTripsRefreshButton.setOnClickListener(syncManagerClickListener);
		mNoTripsTryAgainButton.setOnClickListener(syncManagerClickListener);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_ERROR_MESSAGE)) {
				setErrorMessage(savedInstanceState.getString(STATE_ERROR_MESSAGE), true);
			}
		}

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		updateLoginState();
		syncItinManager();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mShowError && mErrorMessage != null) {
			outState.putString(STATE_ERROR_MESSAGE, mErrorMessage);
		}

	}

	@Override
	public void onDetach() {
		super.onDetach();

		mItinManager.removeSyncListener(this);
		mItinManager = null;
	}

	public void syncItinManager() {
		if (mAllowLoadItins && mItinManager != null) {
			mItinManager.startSync();
			setIsLoading(true);
			mItinListView.enableScrollToRevelentWhenDataSetChanged();
		}
	}

	public void setIsLoading(boolean isLoading) {
		mEmptyListLoadingContainer.setVisibility(isLoading ? View.VISIBLE : View.GONE);
		mEmptyListContent.setVisibility(isLoading ? View.GONE : View.VISIBLE);
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
		syncItinManager();
	}

	public synchronized void startAddGuestItinActivity() {
		Intent intent = new Intent(getActivity(), ItineraryGuestAddActivity.class);
		startActivity(intent);
	}

	public synchronized void startLoginActivity() {
		Intent intent = LoginActivity.createIntent(getActivity(), PathMode.ITIN, new ItineraryLoaderLoginExtender());
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
		ConfirmLogoutDialogFragment df = ConfirmLogoutDialogFragment.getInstance(this, logoutMessage);
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
		// Sign out user
		User.signOut(getActivity());

		// Update UI
		updateLoginState();

		setErrorMessage(null, false);

		invalidateOptionsMenu();

		syncItinManager();
	}

	public void onLoginCompleted() {
		updateLoginState();

		syncItinManager();

		invalidateOptionsMenu();
	}

	private OnScrollListener mOnScrollListener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			int translationY = 0;
			if (firstVisibleItem == 0) {
				View child = view.getChildAt(firstVisibleItem);
				if (child != null) {
					translationY = child.getTop() + (child.getHeight() / 2);
					translationY = Math.max(0, translationY);
				}
			}

			float scaleY = 1f;
			if (firstVisibleItem + visibleItemCount >= totalItemCount) {
				View child = view.getChildAt(visibleItemCount - 1);
				if (child != null) {
					final int height = mItinPathView.getHeight();
					scaleY = (child.getTop() + (child.getHeight() / 2) - translationY) / (float) height;
				}
			}

			ViewHelper.setTranslationY(mItinPathView, translationY);
			ViewHelper.setScaleY(mItinPathView, scaleY);
		}
	};

	private OnListModeChangedListener mOnListModeChangedListener = new OnListModeChangedListener() {
		@Override
		public void onListModeChanged(int mode) {
			if (mode == ItinListView.MODE_LIST) {
				getSupportActionBar().show();
				ObjectAnimator.ofFloat(mItinPathView, "alpha", 1f).setDuration(200).start();
				Activity activity = getActivity();
				if (activity != null && activity instanceof OnListModeChangedListener) {
					((OnListModeChangedListener) activity).onListModeChanged(mode);
				}
			}
			else if (mode == ItinListView.MODE_DETAIL) {
				Activity activity = getActivity();
				if (activity != null && activity instanceof OnListModeChangedListener) {
					((OnListModeChangedListener) activity).onListModeChanged(mode);
				}
				getSupportActionBar().hide();
				ObjectAnimator.ofFloat(mItinPathView, "alpha", 0f).setDuration(200).start();
			}
		}
	};

	private OnItinCardClickListener mOnItinCardClickListener = new OnItinCardClickListener() {
		@Override
		public void onCloseButtonClicked() {
		}

		@Override
		public void onShareButtonClicked(String subject, String shortMessage, String longMessage) {
			SocialMessageChooserDialogFragment.newInstance(subject, shortMessage, longMessage).show(
					getFragmentManager(), DIALOG_SHARE);
		}
	};

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (!(parent.getAdapter().getItem(position) instanceof ItinCardData)) {
				return;
			}

			// If there's no detail data (if this is a fallback card or for whatever other reason)
			// then at least we can open the details url for this trip.
			ItinCardData data = (ItinCardData) parent.getAdapter().getItem(position);
			if (!data.hasDetailData()) {
				String url = data.getDetailsUrl();
				startActivity(new WebViewActivity.IntentBuilder(getActivity()).setUrl(url).getIntent());
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
		// Do nothing
	}

	@Override
	public void onTripUpateFailed(Trip trip) {
		// Do nothing
	}

	@Override
	public void onTripRemoved(Trip trip) {
		// Do nothing
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

		// TODO: make sure these calls are fired the correct number of times, will probably need extra bookkeeping
		Context context = getActivity();
		if (context != null) {
			if (trips != null && trips.size() > 1) {
				OmnitureTracking.trackItin(context);
			}
			else {
				OmnitureTracking.trackItinEmpty(context);
			}
		}
	}
}
