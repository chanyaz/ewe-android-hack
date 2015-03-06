package com.expedia.bookings.presenter.lx;

import javax.inject.Inject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.lx.ActivityDetailsParams;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.VisibilityTransition;
import com.expedia.bookings.services.LXServices;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.LXActivityDetailsWidget;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class LXDetailsPresenter extends Presenter {

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

	@Inject
	LXState lxState;

	private Subscription detailsSubscription;

	@Inject
	LXServices lxServices;

	// Transitions
	private Transition loadingToDetails = new VisibilityTransition(this, ProgressBar.class.getName(), LXActivityDetailsWidget.class.getName());
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
		setupToolbar();
	}

	@Override
	protected void onDetachedFromWindow() {
		cleanup();
		super.onDetachedFromWindow();
	}

	public void cleanup() {
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
		setToolbarTitles();
		LXSearchParams searchParams = lxState.searchParams;
		ActivityDetailsParams activityDetailsParams = new ActivityDetailsParams();
		activityDetailsParams.activityId = event.lxActivity.id;
		activityDetailsParams.startDate = searchParams.startDate;
		activityDetailsParams.endDate = searchParams.endDate;
		detailsSubscription = lxServices.lxDetails(activityDetailsParams, detailsObserver);
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
		if (statusBarHeight > 0) {
			int toolbarColor = getContext().getResources().getColor(R.color.lx_primary_color);
			addView(Ui.setUpStatusBar(getContext(), toolbar, details, toolbarColor));
		}
	}

	private void setToolbarTitles() {
		LXSearchParams searchParams = lxState.searchParams;
		toolbar.setTitle(searchParams.location);
		String dateRange = String.format(getResources().getString(R.string.lx_toolbar_date_range_template),
			DateUtils.localDateToMMMdd(searchParams.startDate), DateUtils.localDateToMMMdd(searchParams.endDate));
		toolbar.setSubtitle(dateRange);
	}

}
