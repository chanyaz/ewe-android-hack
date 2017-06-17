package com.expedia.bookings.presenter.lx;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.expedia.account.graphics.ArrowXDrawable;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.ScaleTransition;
import com.expedia.bookings.services.LxServices;
import com.expedia.bookings.utils.ArrowXDrawableUtil;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.RetrofitUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.LXActivityDetailsWidget;
import com.expedia.bookings.widget.LXMapView;
import com.google.android.gms.maps.GoogleMap;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import kotlin.Unit;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;

public class LXDetailsPresenter extends Presenter {
	private LXActivity lxActivity;

	public LXDetailsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private ArrowXDrawable navIcon;

	//@Injectview(R.id.activity_details)
	LXActivityDetailsWidget details;

	//@Injectview(R.id.lx_maps_view)
	LXMapView fullscreenMapView;

	//@Injectview(R.id.toolbar)
	Toolbar toolbar;

	//@Injectview(R.id.toolbar_background)
	View toolbarBackground;

	//@Injectview(R.id.toolbar_dropshadow)
	View toolbarDropshadow;

	@Inject
	LXState lxState;

	//@Injectview(R.id.toolbar_search_text)
	android.widget.TextView toolBarSearchText;

	//@Injectview(R.id.toolbar_detail_text)
	android.widget.TextView toolBarDetailText;

	//@Injectview(R.id.toolbar_subtitle_text)
	android.widget.TextView toolBarSubtitleText;

	//@Injectview(R.id.toolbar_two)
	LinearLayout toolbarTwo;

	//@Injectview(R.id.lx_details_gradient_top)
	View lxDetailsGradientTop;

	private Subscription detailsSubscription;

	private int searchTop;

	@Inject
	LxServices lxServices;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Ui.getApplication(getContext()).lxComponent().inject(this);
		addTransition(detailToMap);
		setupToolbar();
		details.addOnScrollListener(parallaxScrollListener);
		details.mapClickSubject.subscribe(new Action1<Unit>() {
			@Override
			public void call(Unit unit) {
				show(fullscreenMapView);
			}
		});
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
			fullscreenMapView.getViewmodel().getOffersObserver().onNext(activityDetails);
		}
	};

	public Transition detailToMap = new ScaleTransition(this, LXActivityDetailsWidget.class, LXMapView.class) {
		@Override
		public void endTransition(boolean forward) {
			super.endTransition(forward);
			if (forward) {
				details.setVisibility(GONE);
				fullscreenMapView.setVisibility(VISIBLE);
				toolbarBackground.setVisibility(GONE);
				toolbar.setVisibility(GONE);
				fullscreenMapView.getGoogleMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
			}
			else {
				details.setVisibility(VISIBLE);
				fullscreenMapView.setVisibility(GONE);
				toolbarBackground.setVisibility(VISIBLE);
				toolbar.setVisibility(VISIBLE);
				fullscreenMapView.getGoogleMap().setMapType(GoogleMap.MAP_TYPE_NONE);
			}
		}
	};

	public void onActivitySelected(LXActivity lxActivity) {
		this.lxActivity = lxActivity;
		showActivityDetails(lxActivity.id, lxActivity.title, lxState.searchParams.getLocation(),
			lxState.searchParams.getActivityStartDate(), lxState.searchParams.getActivityEndDate());
	}

	@Subscribe
	public void onActivitySelectedRetry(Events.LXActivitySelectedRetry event) {
		showActivityDetails(lxState.activity.id, lxState.activity.title, lxState.searchParams.getLocation(),
			lxState.searchParams.getActivityStartDate(), lxState.searchParams.getActivityEndDate());
	}

	private void showActivityDetails(String activityId, String title, String location, LocalDate startDate,
		LocalDate endDate) {
		setToolbarTitles(title);
		detailsSubscription = lxServices.lxDetails(activityId, location, startDate, endDate, detailsObserver);
		details.defaultScroll();
	}

	private void setupToolbar() {
		navIcon = ArrowXDrawableUtil
			.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK);
		navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(navIcon);
		toolbar.setNavigationContentDescription(R.string.toolbar_nav_icon_cont_desc);
		toolbar.inflateMenu(R.menu.lx_results_details_menu);

		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (navIcon.getParameter() != ArrowXDrawableUtil.ArrowDrawableType.BACK.getType()) {
					details.toggleFullScreenGallery();
				}
				else {
					((Activity) getContext()).onBackPressed();
				}
			}
		});
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				/*switch (menuItem.getItemId()) {
				case R.id.menu_open_search:
					Events.post(new Events.LXSearchParamsOverlay());
					return true;
				}*/
				return false;
			}
		});

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		toolbarBackground.getLayoutParams().height += statusBarHeight;
		toolbar.setPadding(0, statusBarHeight, 0, 0);
	}

	void setToolbarTitles(String title) {
		toolBarDetailText.setText(title);
		toolBarSubtitleText.setText(LXDataUtils.getToolbarSearchDateText(getContext(), lxState.searchParams, false));
		toolBarSubtitleText
			.setContentDescription(LXDataUtils.getToolbarSearchDateText(getContext(), lxState.searchParams, true));
		toolbarBackground.setAlpha(0);
		toolbarDropshadow.setAlpha(0);
	}

	com.expedia.bookings.widget.ScrollView.OnScrollListener parallaxScrollListener = new com.expedia.bookings.widget.ScrollView.OnScrollListener() {
		@Override
		public void onScrollChanged(com.expedia.bookings.widget.ScrollView scrollView, int x, int y, int oldx,
			int oldy) {
			float ratio = details.parallaxScrollHeader(y);
			float arrowRatio = details.getArrowRotationRatio(y);
			toolbarBackground.setAlpha(ratio);
			toolbarDropshadow.setAlpha(ratio);
			lxDetailsGradientTop.setAlpha(1.0f - ratio);
			if (arrowRatio >= 0 && arrowRatio <= 1) {
				navIcon.setParameter(1 - arrowRatio);
			}
			if (!ExpediaBookingApp.isAutomation()) {
				details.updateGalleryPosition();
			}
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
		float yTrans = forward ? -(searchTop * -f) : (searchTop * (1 - f));
		toolBarDetailText.setTranslationY(yTrans);
		toolBarSubtitleText.setTranslationY(yTrans);
	}

	public void animationFinalize(boolean forward) {
		toolbar.setVisibility(VISIBLE);
		toolbarBackground.setVisibility(VISIBLE);
		toolBarDetailText.setTranslationY(0);
		toolBarSubtitleText.setTranslationY(0);
	}

	@Override
	public boolean back() {
		if (navIcon.getParameter() != ArrowXDrawableUtil.ArrowDrawableType.BACK.getType() && !LXMapView.class.getName()
			.equals(getCurrentState())) {
			details.toggleFullScreenGallery();
			return true;
		}
		return super.back();
	}

	public LXActivity getLxActivity() {
		return lxActivity;
	}
}
