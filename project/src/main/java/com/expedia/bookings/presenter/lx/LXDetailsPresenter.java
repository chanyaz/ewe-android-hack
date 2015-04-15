package com.expedia.bookings.presenter.lx;

import javax.inject.Inject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.TripBucketItemLX;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.VisibilityTransition;
import com.expedia.bookings.services.LXServices;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.RetrofitUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.bookings.widget.LXActivityDetailsWidget;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class LXDetailsPresenter extends Presenter implements UserAccountRefresher.IUserAccountRefreshListener {
	public LXDetailsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * TODO: Will need to refactor this based on the designs. If same loading is shown as on SRP, then reuse the progress bar
	 */

	@InjectView(R.id.loading_details)
	ProgressBar loadingProgress;

	@InjectView(R.id.activity_details)
	LXActivityDetailsWidget details;

	@InjectView(R.id.toolbar)
	Toolbar toolbar;

	@InjectView(R.id.toolbar_background)
	View toolbarBackground;

	@Inject
	LXState lxState;

	private ProgressDialog createTripDialog;

	private Subscription detailsSubscription;
	private Subscription createTripSubscription;

	@Inject
	LXServices lxServices;

	UserAccountRefresher userAccountRefresher;

	// Transitions
	private Transition loadingToDetails = new VisibilityTransition(this, ProgressBar.class.getName(), LXActivityDetailsWidget.class.getName()) {
		@Override
		public void finalizeTransition(boolean forward) {
			super.finalizeTransition(forward);
			toolbarBackground.setAlpha(0f);
		}
	};
	DefaultTransition setUpLoading = new DefaultTransition(ProgressBar.class.getName()) {
		@Override
		public void finalizeTransition(boolean forward) {
			loadingProgress.setVisibility(View.VISIBLE);
			details.setVisibility(View.GONE);
		}
	};

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Ui.getApplication(getContext()).lxComponent().inject(this);

		addTransition(loadingToDetails);
		addDefaultTransition(setUpLoading);

		createTripDialog = new ProgressDialog(getContext());
		createTripDialog.setMessage(getResources().getString(R.string.preparing_checkout_message));
		createTripDialog.setIndeterminate(true);
		setupToolbar();
		details.addOnScrollListener(parallaxScrollListener);

		userAccountRefresher = new UserAccountRefresher(getContext(), LineOfBusiness.LX, this);
	}

	@Override
	protected void onDetachedFromWindow() {
		cleanup();
		super.onDetachedFromWindow();
	}

	public void cleanup() {
		if (createTripSubscription != null) {
			createTripSubscription.unsubscribe();
			createTripSubscription = null;
		}
		if (detailsSubscription != null) {
			detailsSubscription.unsubscribe();
			detailsSubscription = null;
		}
	}

	private Observer<ActivityDetailsResponse> detailsObserver = new Observer<ActivityDetailsResponse>() {
		@Override
		public void onCompleted() {
			// ignore
		}

		@Override
		public void onError(Throwable e) {
			// ignore
		}

		@Override
		public void onNext(ActivityDetailsResponse activityDetails) {
			Events.post(new Events.LXShowDetails(activityDetails));
			show(details, FLAG_CLEAR_BACKSTACK);
		}
	};

	@Subscribe
	public void onActivitySelected(Events.LXActivitySelected event) {
		show(loadingProgress);
		setToolbarTitles(event.lxActivity);
		detailsSubscription = lxServices.lxDetails(event.lxActivity, lxState.searchParams.startDate, lxState.searchParams.endDate, detailsObserver);
	}

	private void setupToolbar() {
		Drawable navIcon = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
		toolbar.setNavigationIcon(navIcon);
		toolbar.inflateMenu(R.menu.lx_results_details_menu);

		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) getContext()).onBackPressed();
			}
		});
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
				case R.id.menu_open_search:
					Events.post(new Events.LXSearchParamsOverlay());
					return true;
				}
				return false;
			}
		});

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		toolbarBackground.getLayoutParams().height += statusBarHeight;
		toolbar.setPadding(0, statusBarHeight, 0, 0);
	}

	private void setToolbarTitles(LXActivity lxActivity) {
		LXSearchParams searchParams = lxState.searchParams;
		toolbar.setTitle(lxActivity.title);
		String dateRange = String.format(getResources().getString(R.string.lx_toolbar_date_range_template),
			DateUtils.localDateToMMMdd(searchParams.startDate), DateUtils.localDateToMMMdd(searchParams.endDate));
		toolbar.setSubtitle(dateRange);
	}

	@Subscribe
	public void onOfferBooked(Events.LXOfferBooked event) {
		createTripDialog.show();
		cleanup();
		userAccountRefresher.ensureAccountIsRefreshed();
	}

	private Observer<LXCreateTripResponse> createTripObserver = new Observer<LXCreateTripResponse>() {
		@Override
		public void onCompleted() {
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
			Log.e("LXCreateTrip - onError", e);
			createTripDialog.dismiss();
			if (RetrofitUtils.isNetworkError(e)) {
				showOnCreateNoInternetErrorDialog(R.string.error_no_internet);
			}
			else {
				showCreateTripErrorDialog();
			}
		}

		@Override
		public void onNext(LXCreateTripResponse response) {
			createTripDialog.dismiss();
			Db.getTripBucket().clearLX();
			Db.getTripBucket().add(new TripBucketItemLX(response));
			Events.post(new Events.LXCreateTripSucceeded(response));
		}
	};

	private void showOnCreateNoInternetErrorDialog(@StringRes int message) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(message))
			.setPositiveButton(getResources().getString(R.string.retry), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.LXOfferBooked(lxState.offer, lxState.selectedTickets));
				}
			})
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.show();
	}

	private void showCreateTripErrorDialog() {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(R.string.error_server))
			.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.LXShowSearchWidget());
				}
			})
			.show();
	}

	com.expedia.bookings.widget.ScrollView.OnScrollListener parallaxScrollListener = new com.expedia.bookings.widget.ScrollView.OnScrollListener() {
		@Override
		public void onScrollChanged(com.expedia.bookings.widget.ScrollView scrollView, int x, int y, int oldx, int oldy) {
			float ratio = details.parallaxScrollHeader(y);
			toolbarBackground.setAlpha(ratio);
		}
	};

	public void animationStart(boolean forward) {
		toolbarBackground.setTranslationY(forward ? 0 : -toolbarBackground.getHeight());
		toolbar.setTranslationY(forward ? 0 : 50);
		toolbar.setVisibility(VISIBLE);
	}

	public void animationUpdate(float f, boolean forward) {
		toolbarBackground
			.setTranslationY(forward ? -toolbarBackground.getHeight() * f : -toolbarBackground.getHeight() * (1 - f));
		toolbar.setTranslationY(forward ? 50 * f : 50 * (1 - f));
	}

	public void animationFinalize(boolean forward) {
		toolbarBackground.setTranslationY(forward ? -toolbarBackground.getHeight() : 0);
		toolbar.setTranslationY(forward ? 50 : 0);
		toolbar.setVisibility(forward ? GONE : VISIBLE);
		toolbarBackground.setAlpha(
			Strings.equals(getCurrentState(), LXActivityDetailsWidget.class.getName()) ? toolbarBackground.getAlpha()
				: 1f);
	}

	@Override
	public void onUserAccountRefreshed() {
		createTripSubscription = lxServices.createTrip(lxState.createTripParams(), createTripObserver);
	}
}

