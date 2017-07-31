package com.expedia.bookings.lob.lx.ui.activity;

import org.joda.time.LocalDate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.ViewTreeObserver.OnPreDrawListener;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.location.LXCurrentLocationSuggestionObserver;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.lx.LXPresenter;
import com.expedia.bookings.utils.AlertDialogUtils;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.FeatureToggleUtil;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.ui.AbstractAppCompatActivity;
import com.google.android.gms.maps.MapView;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscription;

public class LXBaseActivity extends AbstractAppCompatActivity {

	@InjectView(R.id.lx_base_presenter)
	LXPresenter lxPresenter;

	@InjectView(R.id.details_map_view)
	MapView detailsMapView;

	private LXCurrentLocationSuggestionObserver currentLocationSuggestionObserver;
	private Subscription currentLocationSuggestionSubscription;

	public static final String EXTRA_IS_GROUND_TRANSPORT = "IS_GROUND_TRANSPORT";
	private boolean isGroundTransport;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Ui.getApplication(this).defaultLXComponents();
		Ui.getApplication(this).defaultTravelerComponent();

		Intent intent = getIntent();
		isGroundTransport = intent.getBooleanExtra(EXTRA_IS_GROUND_TRANSPORT, false);
		boolean isUserBucketedForTest = Db.getAbacusResponse()
			.isUserBucketedForTest(AbacusUtils.EBAndroidAppLXCategoryABTest);
		boolean isUserBucketedForRTRTest = Db.getAbacusResponse()
			.isUserBucketedForTest(AbacusUtils.EBAndroidAppLXRTROnSearchAndDetails);

		if (isGroundTransport) {
			this.setTheme(R.style.V2_Theme_LX_Transport);
		}

		setContentView(R.layout.lx_base_layout);
		ButterKnife.inject(this);
		lxPresenter.setIsGroundTransport(isGroundTransport);
		lxPresenter.setUserBucketedForCategoriesTest(isUserBucketedForTest);
		lxPresenter.setUserBucketedForRTRTest(isUserBucketedForRTRTest && !isGroundTransport);
		detailsMapView.onCreate(savedInstanceState);
		Ui.showTransparentStatusBar(this);
		handleNavigationViaDeepLink();
	}

	private void triggerCurrentLocationSuggestions(boolean isGroundTransport) {
		int permissionCheck = ContextCompat.checkSelfPermission(this,
			Manifest.permission.ACCESS_FINE_LOCATION);

		if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
			// If no permission on LX, we should go to the search screen instead of results
			Events.post(new Events.LXNewSearch(null, LocalDate.now(),
				LocalDate.now().plusDays(getResources().getInteger(R.integer.lx_default_search_range))));
			return;
		}

		Events.post(new Events.LXShowLoadingAnimation());
		LxSearchParams currentLocationSearchParams = new LxSearchParams("",LocalDate.now(),
			LocalDate.now().plusDays(getResources().getInteger(R.integer.lx_default_search_range)),
			SearchType.DEFAULT_SEARCH,"","","");

		Observable<SuggestionV4> currentLocationSuggestionObservable =
			Ui.getApplication(this).lxComponent().currentLocationSuggestionObservable();
		currentLocationSuggestionObserver = new LXCurrentLocationSuggestionObserver(this, currentLocationSearchParams,
			isGroundTransport);
		currentLocationSuggestionSubscription =
			currentLocationSuggestionObservable.subscribe(currentLocationSuggestionObserver);
	}

	private void handleNavigationViaDeepLink() {
		Intent intent = getIntent();
		final boolean navigateToResults = (intent != null) && intent.getBooleanExtra(Codes.EXTRA_OPEN_RESULTS, false);
		final boolean navigateToSearch = (intent != null) && intent.getBooleanExtra(Codes.EXTRA_OPEN_SEARCH, false);
		final boolean navigateToResultsFromDeepLink =
			(intent != null) && intent.getBooleanExtra(Codes.FROM_DEEPLINK, false);
		final boolean navigateToDetailsFromDeepLink =
			(intent != null) && intent.getBooleanExtra(Codes.FROM_DEEPLINK_TO_DETAILS, false);

		final String location = intent.getStringExtra("location");
		final LocalDate startDate = DateUtils.yyyyMMddToLocalDateSafe(intent.getStringExtra("startDateStr"),
			LocalDate.now());

		String hotelCheckout = intent.getStringExtra("endDateStr");
		Integer noHotelUseSearchDefaultOf14Days = R.integer.lx_default_search_range;
		final LocalDate endDate = DateUtils.yyyyMMddToLocalDateSafe(hotelCheckout,
			startDate.plusDays(getResources().getInteger(noHotelUseSearchDefaultOf14Days)));

		final String filters = intent.getStringExtra("filters");
		final String activityId = intent.getStringExtra("activityId");

		lxPresenter.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				lxPresenter.getViewTreeObserver().removeOnPreDrawListener(this);
				if (startDate.isBefore(LocalDate.now())) {
					AlertDialogUtils
						.showDialog(LXBaseActivity.this, getResources().getString(R.string.lx_start_date_error),
							new Events.LXShowSearchWidget(), getResources().getString(R.string.ok), null, null);
					return true;
				}
				if (navigateToSearch) {
					Events.LXNewSearch event = new Events.LXNewSearch(location, startDate, endDate);
					updateSearchViewModel(event);
					Events.post(event);
					return true;
				}
				else if (navigateToResults) {
					Events.post(new Events.LXNewSearchParamsAvailable(location, startDate, endDate));
					return true;
				}
				else if (navigateToResultsFromDeepLink) {
					Events.post(new Events.LXNewSearchParamsAvailable(location, startDate, endDate, filters));
					return true;
				}
				else if (navigateToDetailsFromDeepLink) {
					Events.post(new Events.LXNewSearchParamsAvailable(activityId, location, startDate, endDate));
					return true;
				}
				if (!FeatureToggleUtil.isFeatureEnabled(LXBaseActivity.this, R.string.preference_new_launchscreen_nav)) {
					triggerCurrentLocationSuggestions(isGroundTransport);
				}
				return true;
			}
		});
	}

	public void updateSearchViewModel(Events.LXNewSearch event) {
		if (Strings.isNotEmpty(event.locationName)) {
			lxPresenter.searchParamsWidget.getSearchViewModel().getDestinationLocationObserver()
				.onNext(getSuggestionFromLocation(event.locationName));
		}
		lxPresenter.searchParamsWidget.getSearchViewModel().datesUpdated(event.startDate, event.endDate);
		lxPresenter.searchParamsWidget.selectDates(event.startDate, event.endDate);
		lxPresenter.searchParamsWidget.getSearchViewModel().getSearchButtonObservable().onNext(true);
	}

	public SuggestionV4 getSuggestionFromLocation(String locationName) {
		SuggestionV4 suggestionV4 = new SuggestionV4();
		SuggestionV4.RegionNames regionNames = new SuggestionV4.RegionNames();
		regionNames.fullName = locationName;
		regionNames.displayName = locationName;
		regionNames.shortName = locationName;
		suggestionV4.regionNames = regionNames;
		return suggestionV4;
	}

	@Override
	public void onBackPressed() {
		if (!lxPresenter.back()) {
			super.onBackPressed();
		}
	}

	@Override
	protected void onResume() {
		detailsMapView.onResume();
		super.onResume();
		Events.register(this);
	}

	@Override
	protected void onDestroy() {
		detailsMapView.onDestroy();
		Ui.getApplication(this).setLXTestComponent(null);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		detailsMapView.onPause();
		super.onPause();
		Events.unregister(this);

		if (isFinishing()) {
			clearCCNumber();
			clearStoredCard();
		}
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
}
