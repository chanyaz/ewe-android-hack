package com.expedia.bookings.presenter.lx;

import java.util.List;

import javax.inject.Inject;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.VisibilityTransition;
import com.expedia.bookings.services.LXServices;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.LXSearchResultsWidget;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class LXResultsPresenter extends Presenter {

	@Inject
	LXServices lxServices;

	@InjectView(R.id.lx_search_results_widget)
	LXSearchResultsWidget searchResultsWidget;

	@InjectView(R.id.loading_results)
	ProgressBar loadingProgress;

	Subscription searchSubscription;

	public LXResultsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	// Transitions
	private Transition loadingToSearchResults = new VisibilityTransition(this, ProgressBar.class.getName(), LXSearchResultsWidget.class.getName());

	private DefaultTransition setUpLoading = new DefaultTransition(ProgressBar.class.getName()) {
		@Override
		public void finalizeTransition(boolean forward) {
			loadingProgress.setVisibility(View.VISIBLE);
			searchResultsWidget.setVisibility(View.GONE);
		}
	};

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Ui.getApplication(getContext()).lxComponent().inject(this);

		addTransition(loadingToSearchResults);
		addDefaultTransition(setUpLoading);
	}

	@Override
	protected void onDetachedFromWindow() {
		cleanup();
		super.onDetachedFromWindow();
	}

	private void cleanup() {
		if (searchSubscription != null) {
			searchSubscription.unsubscribe();
		}
	}

	private Observer<List<LXActivity>> searchResultObserver = new Observer<List<LXActivity>>() {
		@Override
		public void onCompleted() {
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
			// ignore
		}

		@Override
		public void onNext(List<LXActivity> lxActivities) {
			Events.post(new Events.LXShowSearchResults(lxActivities));
			show(searchResultsWidget, FLAG_CLEAR_BACKSTACK);
		}
	};

	@Subscribe
	public void onLXNewSearchParamsAvailable(Events.LXNewSearchParamsAvailable event) {
		cleanup();
		show(loadingProgress);
		searchSubscription = lxServices.lxSearch(event.lxSearchParams, searchResultObserver);
	}

}
