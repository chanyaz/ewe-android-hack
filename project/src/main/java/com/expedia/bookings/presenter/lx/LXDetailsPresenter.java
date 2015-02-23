package com.expedia.bookings.presenter.lx;

import org.joda.time.LocalDate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.ActivityDetailsParams;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXDb;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.VisibilityTransition;
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

	private Subscription detailsSubscription;

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
		addTransition(loadingToDetails);
		addDefaultTransition(setUpLoading);
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
		ActivityDetailsParams activityDetailsParams = new ActivityDetailsParams();
		activityDetailsParams.activityId = event.lxActivity.id;
		activityDetailsParams.startDate = LocalDate.now();
		activityDetailsParams.endDate = LocalDate.now().plusDays(4);
		detailsSubscription = LXDb.getLxServices().lxDetails(activityDetailsParams, detailsObserver);
	}

}
