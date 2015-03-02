package com.expedia.bookings.presenter.lx;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.VisibilityTransition;
import com.expedia.bookings.widget.LXSearchParamsWidget;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;

public class LXPresenter extends Presenter {

	public LXPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.search_params_widget)
	LXSearchParamsWidget searchParamsWidget;

	@InjectView(R.id.search_list_presenter)
	LXResultsPresenter resultsPresenter;

	@InjectView(R.id.activity_details_presenter)
	LXDetailsPresenter detailsPresenter;

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		addTransition(searchParamsToResults);
		addTransition(resultsToDetails);
		show(searchParamsWidget);
		searchParamsWidget.setVisibility(View.VISIBLE);
	}

	@Subscribe
	public void onNewSearchParamsAvailable(Events.LXNewSearchParamsAvailable event) {
		show(resultsPresenter);
	}

	private Transition searchParamsToResults = new VisibilityTransition(this, LXSearchParamsWidget.class.getName(),
		LXResultsPresenter.class.getName());

	private Transition resultsToDetails = new VisibilityTransition(this, LXResultsPresenter.class.getName(),
		LXDetailsPresenter.class.getName()) {
		@Override
		public void startTransition(boolean forward) {
		}

		@Override
		public void updateTransition(float f, boolean forward) {
		}

		@Override
		public void endTransition(boolean forward) {
		}

		@Override
		public void finalizeTransition(boolean forward) {
			if (forward) {
				resultsPresenter.setVisibility(View.GONE);
				detailsPresenter.setVisibility(View.VISIBLE);
			}
			else {
				resultsPresenter.setVisibility(View.VISIBLE);
				detailsPresenter.setVisibility(View.GONE);
				detailsPresenter.cleanup();
			}
		}
	};

	@Subscribe
	public void onActivitySelected(Events.LXActivitySelected event) {
		show(detailsPresenter);
	}

	@Subscribe
	public void onShowSearchWidget(Events.LXShowSearchWidget event) {
		show(searchParamsWidget, FLAG_CLEAR_BACKSTACK | FLAG_CLEAR_TOP);
	}

}
