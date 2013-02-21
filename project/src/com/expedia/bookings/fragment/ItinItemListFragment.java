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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ItineraryGuestAddActivity;
import com.expedia.bookings.activity.LoginActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncListener;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.dialog.SocialMessageChooserDialogFragment;
import com.expedia.bookings.fragment.LoginFragment.PathMode;
import com.expedia.bookings.widget.ItinCard.OnItinCardClickListener;
import com.expedia.bookings.widget.ItinListView;
import com.expedia.bookings.widget.ItinListView.OnListModeChangedListener;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

public class ItinItemListFragment extends Fragment implements ConfirmLogoutDialogFragment.DoLogoutListener,
		ItinerarySyncListener {

	public static final String TAG = "TAG_ITIN_ITEM_LIST_FRAGMENT";
	public static final String DIALOG_SHARE = "DIALOG_SHARE";

	private View mItinPathView;
	private ItinListView mItinListView;
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

		mItinManager = ItineraryManager.getInstance();
		mItinManager.addSyncListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_itinerary_list, null);

		mItinPathView = Ui.findView(view, R.id.itin_path_view);
		mItinListView = Ui.findView(view, android.R.id.list);
		mEmptyView = Ui.findView(view, android.R.id.empty);
		mOrEnterNumberTv = Ui.findView(view, R.id.or_enter_itin_number_tv);
		mEmptyListLoadingContainer = Ui.findView(view, R.id.empty_list_loading_container);
		mEmptyListContent = Ui.findView(view, R.id.empty_list_content);
		mLoginButton = Ui.findView(view, R.id.login_button);
		mLoginButton.setText(Html.fromHtml(getString(R.string.log_in_for_your_trips)));

		mItinListView.setEmptyView(mEmptyView);
		mItinListView.setOnScrollListener(mOnScrollListener);
		mItinListView.setOnListModeChangedListener(mOnListModeChangedListener);
		mItinListView.setOnItinCardClickListener(mOnItinCardClickListener);

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
		return mItinListView.getMode() == ItinListView.MODE_LIST;
	}

	public void setListMode() {
		mItinListView.setMode(ItinListView.MODE_LIST);
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
		Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
		loginIntent.putExtra(LoginActivity.ARG_PATH_MODE, PathMode.HOTELS.name());
		startActivity(loginIntent);
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
			if (getActivity() instanceof SherlockFragmentActivity) {
				((SherlockFragmentActivity) getActivity()).supportInvalidateOptionsMenu();
			}
			else if (AndroidUtils.getSdkVersion() >= 11) {
				getActivity().invalidateOptionsMenu();
			}
			else {
				throw new RuntimeException(
						"ItinItemListFragment should be attached to a SherlockFragmentActivity if sdk version < 11");
			}
		}
	}

	public void onLoginCompleted() {
		updateLoginState();

		syncItinManager();

		invalidateOptionsMenu();
	}

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
				ObjectAnimator.ofFloat(mItinPathView, "alpha", 1f).setDuration(200).start();
			}
			else if (mode == ItinListView.MODE_DETAIL) {
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
}