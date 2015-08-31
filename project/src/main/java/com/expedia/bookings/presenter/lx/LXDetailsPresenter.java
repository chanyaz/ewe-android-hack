package com.expedia.bookings.presenter.lx;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.services.LXServices;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.RetrofitUtils;
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

	@InjectView(R.id.activity_details)
	LXActivityDetailsWidget details;

	@InjectView(R.id.toolbar)
	Toolbar toolbar;

	@InjectView(R.id.toolbar_background)
	View toolbarBackground;

	@InjectView(R.id.toolbar_dropshadow)
	View toolbarDropshadow;

	@Inject
	LXState lxState;

	@InjectView(R.id.toolbar_search_text)
	android.widget.TextView toolBarSearchText;

	@InjectView(R.id.toolbar_detail_text)
	android.widget.TextView toolBarDetailText;

	@InjectView(R.id.toolbar_subtitle_text)
	android.widget.TextView toolBarSubtitleText;

	@InjectView(R.id.toolbar_two)
	LinearLayout toolbarTwo;

	private Subscription detailsSubscription;

	private int searchTop;

	@Inject
	LXServices lxServices;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Ui.getApplication(getContext()).lxComponent().inject(this);
		setupToolbar();
		details.addOnScrollListener(parallaxScrollListener);
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

	private void showActivityFetchErrorDialog(@StringRes int message) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(message))
			.setPositiveButton(getResources().getString(R.string.retry), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.LXActivitySelectedRetry());
				}
			})
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					((AppCompatActivity) getContext()).onBackPressed();
				}
			})
			.show();
	}

	private Observer<ActivityDetailsResponse> detailsObserver = new Observer<ActivityDetailsResponse>() {
		@Override
		public void onCompleted() {
			// ignore
		}

		@Override
		public void onError(Throwable e) {
			if (RetrofitUtils.isNetworkError(e)) {
				showActivityFetchErrorDialog(R.string.error_no_internet);
			}
			else {
				//Bucket all other errors as Activity Details Fetch Error
				showActivityFetchErrorDialog(R.string.lx_error_details);
			}
		}

		@Override
		public void onNext(ActivityDetailsResponse activityDetails) {
			Events.post(new Events.LXShowDetails(activityDetails));
			show(details, FLAG_CLEAR_BACKSTACK);
		}
	};

	@Subscribe
	public void onActivitySelected(Events.LXActivitySelected event) {
		showActivityDetails(event.lxActivity.id, event.lxActivity.title, lxState.searchParams.location,
			lxState.searchParams.startDate, lxState.searchParams.endDate);
	}

	@Subscribe
	public void onActivitySelectedRetry(Events.LXActivitySelectedRetry event) {
		showActivityDetails(lxState.activity.id, lxState.activity.title, lxState.searchParams.location,
			lxState.searchParams.startDate, lxState.searchParams.endDate);
	}

	private void showActivityDetails(String activityId, String title, String location, LocalDate startDate,
		LocalDate endDate) {
		setToolbarTitles(title);
		detailsSubscription = lxServices.lxDetails(activityId, location, startDate, endDate, detailsObserver);
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

	private void setToolbarTitles(String title) {
		LXSearchParams searchParams = lxState.searchParams;
		toolBarDetailText.setText(title);
		String dateRange = String.format(getResources().getString(R.string.lx_toolbar_date_range_template),
			DateUtils.localDateToMMMd(searchParams.startDate), DateUtils.localDateToMMMd(searchParams.endDate));
		toolBarSubtitleText.setText(dateRange);
		toolbarBackground.setAlpha(0);
		toolbarDropshadow.setAlpha(0);
	}

	com.expedia.bookings.widget.ScrollView.OnScrollListener parallaxScrollListener = new com.expedia.bookings.widget.ScrollView.OnScrollListener() {
		@Override
		public void onScrollChanged(com.expedia.bookings.widget.ScrollView scrollView, int x, int y, int oldx, int oldy) {
			float ratio = details.parallaxScrollHeader(y);
			toolbarBackground.setAlpha(ratio);
			toolbarDropshadow.setAlpha(ratio);
		}
	};

	public float animationStart(boolean forward) {
		searchTop = toolBarSearchText.getTop() - toolbarTwo.getTop();
		toolbar.setVisibility(VISIBLE);
		toolBarDetailText.setTranslationY(searchTop);
		toolBarSubtitleText.setTranslationY(searchTop);
		return toolbarBackground.getAlpha();
	}

	public void animationUpdate(float f, boolean forward) {
		float yTrans = forward ?  - (searchTop * -f) : (searchTop * (1 - f));
		toolBarDetailText.setTranslationY(yTrans);
		toolBarSubtitleText.setTranslationY(yTrans);
	}

	public void animationFinalize(boolean forward) {
		toolbar.setVisibility(VISIBLE);
		toolbarBackground.setVisibility(VISIBLE);
		toolBarDetailText.setTranslationY(0);
		toolBarSubtitleText.setTranslationY(0);
	}

}

