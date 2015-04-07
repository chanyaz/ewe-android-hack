package com.expedia.bookings.activity;

import org.joda.time.LocalDate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBarActivity;
import android.util.TimeFormatException;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.location.CurrentLocationSuggestionProvider;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.lx.LXPresenter;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.RetrofitUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class LXBaseActivity extends ActionBarActivity {

	private static final String TAG = "LXBaseActivity";

	@InjectView(R.id.lx_base_presenter)
	LXPresenter lxPresenter;

	private LXSearchParams currentLocationSearchParams = null;

	private Subscription currentLocationSuggestionSubscription;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Ui.getApplication(this).defaultLXComponents();
		setContentView(R.layout.lx_base_layout);
		ButterKnife.inject(this);
		Ui.showTransparentStatusBar(this);
		handleNavigationViaDeepLink();

		initializeDefaultSearchParams();
		CurrentLocationSuggestionProvider currentLocationSuggestionProvider = Ui.getApplication(this).lxComponent().currentLocationSuggestionProvider();
		currentLocationSuggestionSubscription = currentLocationSuggestionProvider.currentLocationSuggestion().subscribe(currentLocationSuggestionObserver);
	}

	private Observer<Suggestion> currentLocationSuggestionObserver = new Observer<Suggestion>() {

		@Override
		public void onCompleted() {
		}

		@Override
		public void onError(Throwable e) {
			if (RetrofitUtils.isNetworkError(e)) {
				showNoInternetErrorDialog(R.string.error_no_internet);
			}

			//Default
			ApiError apiError = new ApiError();
			apiError.errorCode = ApiError.Code.LX_SEARCH_NO_RESULTS;
			Events.post(new Events.LXShowSearchError(apiError, SearchType.DEFAULT_SEARCH));
		}

		@Override
		public void onNext(Suggestion suggestion) {
			if (currentLocationSearchParams != null) {
				postNewSearchParamsAvailableEvent(suggestion.fullName, currentLocationSearchParams.startDate,
					currentLocationSearchParams.searchType);
				currentLocationSearchParams = null;
			}
		}
	};

	private void showNoInternetErrorDialog(@StringRes int message) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setCancelable(false)
			.setMessage(getResources().getString(message))
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.LXShowSearchWidget());
				}
			})
			.show();
	}

	private void initializeDefaultSearchParams() {
		currentLocationSearchParams = new LXSearchParams();
		currentLocationSearchParams.startDate = LocalDate.now();
		currentLocationSearchParams.searchType = SearchType.DEFAULT_SEARCH;
	}

	private void handleNavigationViaDeepLink() {
		Intent intent = getIntent();
		if (intent != null && intent.getBooleanExtra(Codes.FROM_DEEPLINK, false)) {
			postNewSearchParamsAvailableEvent(intent.getStringExtra("location"),
				getStartDate(intent.getStringExtra("startDateStr")));
		}
	}

	private LocalDate getStartDate(String startDateStr) {
		LocalDate startDate = LocalDate.now();
		try {
			startDate = DateUtils.yyyyMMddToLocalDate(startDateStr);
			Log.d(TAG, "Set activity search date: " + startDate);
		}
		catch (TimeFormatException | IllegalArgumentException e) {
			Log.w(TAG, "Could not parse check in date: " + startDateStr, e);
		}
		return startDate;
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

	@Subscribe
	public void onFinishActivity(Events.FinishActivity event) {
		finish();
	}

	private void postNewSearchParamsAvailableEvent(String locationName, LocalDate startDate) {
		postNewSearchParamsAvailableEvent(locationName, startDate, SearchType.EXPLICIT_SEARCH);
	}

	private void postNewSearchParamsAvailableEvent(String locationName, LocalDate startDate, SearchType searchType) {
		LXSearchParams params = new LXSearchParams();
		params.location = locationName;
		params.startDate = startDate;
		params.endDate = params.startDate.plusDays(getResources().getInteger(R.integer.lx_default_search_range));
		params.searchType = searchType;
		Events.post(new Events.LXNewSearchParamsAvailable(params));
	}
}
