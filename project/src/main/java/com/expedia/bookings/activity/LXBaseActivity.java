package com.expedia.bookings.activity;

import org.joda.time.LocalDate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.location.LXCurrentLocationSuggestionObserver;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.lx.LXPresenter;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.Ui;
import com.facebook.Session;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscription;

public class LXBaseActivity extends ActionBarActivity {

	@InjectView(R.id.lx_base_presenter)
	LXPresenter lxPresenter;

	private LXCurrentLocationSuggestionObserver currentLocationSuggestionObserver;
	private Subscription currentLocationSuggestionSubscription;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Ui.getApplication(this).defaultLXComponents();
		setContentView(R.layout.lx_base_layout);
		ButterKnife.inject(this);
		Ui.showTransparentStatusBar(this);

		if (!handleNavigationViaDeepLink()) {
			triggerCurrentLocationSuggestions();
		}
	}

	private void triggerCurrentLocationSuggestions() {
		if (!ExpediaBookingApp.sIsAutomation) {
			Events.post(new Events.LXShowLoadingAnimation());
		}
		LXSearchParams currentLocationSearchParams = new LXSearchParams()
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(getResources().getInteger(R.integer.lx_default_search_range)))
			.searchType(SearchType.DEFAULT_SEARCH);

		Observable<Suggestion> currentLocationSuggestionObservable = Ui.getApplication(this).lxComponent().currentLocationSuggestionObservable();
		currentLocationSuggestionObserver = new LXCurrentLocationSuggestionObserver(this, currentLocationSearchParams);
		currentLocationSuggestionSubscription = currentLocationSuggestionObservable.subscribe(currentLocationSuggestionObserver);
	}

	private boolean handleNavigationViaDeepLink() {
		Intent intent = getIntent();
		boolean hasDeepLinkIntent = intent != null && intent.getBooleanExtra(Codes.FROM_DEEPLINK, false);
		if (!hasDeepLinkIntent) {
			return false;
		}

		LocalDate startDate = DateUtils.yyyyMMddToLocalDateSafe(intent.getStringExtra("startDateStr"), LocalDate.now());
		Events.post(new Events.LXNewSearchParamsAvailable(intent.getStringExtra("location"),
			startDate,
			startDate.plusDays(getResources().getInteger(R.integer.lx_default_search_range))));

		return true;
	}

	@Override
	public void onBackPressed() {
		if (!lxPresenter.back()) {
			super.onBackPressed();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (currentLocationSuggestionSubscription != null) {
			currentLocationSuggestionSubscription.unsubscribe();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(LXBaseActivity.this, requestCode, resultCode, data);
	}

	@Subscribe
	public void onFinishActivity(Events.FinishActivity event) {
		finish();
	}
}
