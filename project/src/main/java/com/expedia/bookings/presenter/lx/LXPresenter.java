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
	Presenter resultsPresenter;

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		addTransition(searchParamsToResults);
		show(searchParamsWidget);
		searchParamsWidget.setVisibility(View.VISIBLE);
	}

	@Subscribe
	public void onNewSearchParamsAvailable(Events.LXNewSearchParamsAvailable event) {
		show(resultsPresenter);
	}

	private Transition searchParamsToResults = new VisibilityTransition(this, LXSearchParamsWidget.class.getName(),
		LXResultsPresenter.class.getName());

}
