package com.expedia.bookings.launch.widget;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.collections.Collection;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.NearbyHotelParams;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.services.CollectionServices;
import com.expedia.bookings.services.HotelServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FrameLayout;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscription;

public class PhoneLaunchWidget extends FrameLayout {

	private static final String TAG = "PhoneLaunchWidget";
	private static final String HOTEL_SORT = "ExpertPicks";
	private static final long MINIMUM_TIME_AGO = 15 * DateUtils.MINUTE_IN_MILLIS; // 15 minutes ago

	@Inject
	public HotelServices hotelServices;

	@Inject
	public CollectionServices collectionServices;

	private HotelSearchParams searchParams;
	private Subscription downloadSubscription;

	private DateTime launchDataTimeStamp;

	private float squashedHeaderHeight;
	private boolean isAirAttachDismissed;
	private boolean wasHotelsDownloadEmpty;

	private float airAttachTranslation;

	@InjectView(R.id.lob_selector)
	LaunchLobWidget lobSelectorWidget;

	@InjectView(R.id.double_row_lob_selector)
	LaunchLobDoubleRowWidget doubleRowLobSelectorWidget;

	@InjectView(R.id.double_row_five_lob_selector)
	LaunchFiveLobDoubleRowWidget doubleRowFiveLobSelectorWidget;

	@InjectView(R.id.launch_list_widget)
	LaunchListWidget launchListWidget;

	@InjectView(R.id.air_attach_banner)
	ViewGroup airAttachBanner;

	@InjectView(R.id.launch_error)
	ViewGroup launchError;

	@InjectView(R.id.launch_progress_bar)
	ViewGroup progressBar;

	// Lifecycle

	public PhoneLaunchWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	float lobHeight;
	boolean doubleRowLob;
	boolean doubleRowFiveLob;

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Ui.getApplication(getContext()).launchComponent().inject(this);
		launchListWidget.setOnScrollListener(scrollListener);
		isAirAttachDismissed = false;
		setListState();
		progressBar.setOnTouchListener(absorbTouchesListener());
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	public void onDetachedFromWindow() {
		Events.unregister(this);
		cleanup();
		super.onDetachedFromWindow();
	}

	private void cleanup() {
		if (downloadSubscription != null) {
			downloadSubscription.unsubscribe();
			downloadSubscription = null;
		}
	}

	private Observer<List<Hotel>> downloadListener = new Observer<List<Hotel>>() {
		@Override
		public void onCompleted() {
			if (!wasHotelsDownloadEmpty) {
				cleanup();
			}
			Log.d(TAG, "Hotel download completed.");
		}

		@Override
		public void onError(Throwable e) {
			Log.d(TAG, e.getMessage());
			Events.post(new Events.LaunchLocationFetchError());
		}

		@Override
		public void onNext(List<Hotel> nearbyHotelResponse) {
			// Pump our results into a HotelSearchResponse to appease some
			// legacy code.
			HotelSearchResponse response = new HotelSearchResponse();
			for (Hotel offer : nearbyHotelResponse) {
				Property p = new Property();
				p.updateFrom(offer);
				response.addProperty(p);
			}
			if (nearbyHotelResponse.size() > 0) {
				wasHotelsDownloadEmpty = false;
				Events.post(new Events.LaunchHotelSearchResponse(nearbyHotelResponse));
			}
			else {
				wasHotelsDownloadEmpty = true;
				Events.post(new Events.LaunchLocationFetchError());
			}
		}
	};

	private Observer<Collection> collectionDownloadListener = new Observer<Collection>() {
		@Override
		public void onCompleted() {
			cleanup();
			Log.d(TAG, "Collection download completed.");
		}

		@Override
		public void onError(Throwable e) {
			Log.d(TAG, "Error downloading locale/POS specific Collections. Kicking off default download.");
			String country = PointOfSale.getPointOfSale().getTwoLetterCountryCode().toLowerCase(Locale.US);
			downloadSubscription = collectionServices
				.getPhoneCollection(ProductFlavorFeatureConfiguration.getInstance().getPhoneCollectionId(), country,
					"default", defaultCollectionListener);
		}

		@Override
		public void onNext(Collection collection) {
			Events.post(new Events.CollectionDownloadComplete(collection));
		}
	};

	private Observer<Collection> defaultCollectionListener = new Observer<Collection>() {
		@Override
		public void onCompleted() {
			cleanup();
			Log.d(TAG, "Default collection download completed.");
		}

		@Override
		public void onError(Throwable e) {
			Log.d(TAG, e.getMessage());
		}

		@Override
		public void onNext(Collection collection) {
			Events.post(new Events.CollectionDownloadComplete(collection));
		}
	};

	// Clicking

	@OnClick(R.id.air_attach_banner_close)
	public void closeAirAttachBanner() {
		isAirAttachDismissed = true;
		airAttachBanner.animate()
			.translationY(airAttachBanner.getHeight())
			.setDuration(300)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					airAttachBanner.setVisibility(View.GONE);
				}
			});
	}

	private boolean isExpired() {
		return launchDataTimeStamp == null || JodaUtils.isExpired(launchDataTimeStamp, MINIMUM_TIME_AGO)
			|| Db.getLaunchListHotelData() == null;
	}

	/*
	 * Scrolling
	 */

	RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			float currentPos = recyclerView.computeVerticalScrollOffset();
			if (airAttachTranslation >= 0 && airAttachTranslation <= airAttachBanner.getHeight()) {
				airAttachTranslation += dy;
				airAttachTranslation = Math.min(airAttachTranslation, airAttachBanner.getHeight());
				airAttachTranslation = Math.max(0, airAttachTranslation);
				airAttachBanner.setTranslationY(airAttachTranslation);
			}
			// between header starting point and the squashed height
			if (currentPos < squashedHeaderHeight) {
				float squashInput = 1 - (currentPos / lobHeight);
				if (doubleRowLob) {
					doubleRowLobSelectorWidget.transformButtons(squashInput);
				}
				else if (doubleRowFiveLob) {
					doubleRowFiveLobSelectorWidget.transformButtons(squashInput);
				}
				else {
					lobSelectorWidget.transformButtons(squashInput);
				}
			}
			// Make sure that the header is squashed.
			else if (currentPos > squashedHeaderHeight) {
				if (doubleRowLob) {
					doubleRowLobSelectorWidget.transformButtons(1 - (Math.min(currentPos, squashedHeaderHeight) / lobHeight));
				}
				else if (doubleRowFiveLob) {
					doubleRowFiveLobSelectorWidget.transformButtons(1 - (Math.min(currentPos, squashedHeaderHeight) / lobHeight));
				}
				else {
					lobSelectorWidget.transformButtons(1 - (Math.min(currentPos, squashedHeaderHeight) / lobHeight));
				}
			}
		}
	};

	private void goToHotels(Bundle animOptions) {
		NavUtils.goToHotels(getContext(), animOptions);
		OmnitureTracking.trackLinkLaunchScreenToHotels();
	}

	/*
	 * Otto events
	 */

	// The onClick for the "SEE ALL" button in the list header is in
	// LaunchListAdapter. See onSeeAllButtonPressed() below
	// for the Otto event sent by that onClick()

	@Subscribe
	public void onSeeAllButtonPressed(Events.LaunchSeeAllButtonPressed event) {
		NavUtils.goToHotels(getContext(), searchParams, event.animOptions, 0);
	}

	@Subscribe
	public void onHotelOfferSelected(Events.LaunchListItemSelected event) throws JSONException {
		Hotel offer = event.offer;
		HotelSearchParams params = new HotelSearchParams();
		params.hotelId = offer.hotelId;
		params.setQuery(offer.localizedName);
		params.setSearchType(HotelSearchParams.SearchType.HOTEL);
		LocalDate now = LocalDate.now();
		params.setCheckInDate(now);
		params.setCheckOutDate(now.plusDays(1));
		params.setNumAdults(2);
		params.setChildren(null);
		NavUtils.goToHotels(getContext(), params);
	}

	// Hotel Search
	@Subscribe
	public void onLocationFound(Events.LaunchLocationFetchComplete event) {
		Location loc = event.location;
		Log.i(TAG, "Start hotel search");
		launchListWidget.setVisibility(VISIBLE);
		launchError.setVisibility(View.GONE);

		if (isExpired()) {
			// In case the POS changed, ensure that components (particularly hotelServices)
			// update with regard to new POS.
			Ui.getApplication(getContext()).defaultLaunchComponents();
			Ui.getApplication(getContext()).launchComponent().inject(this);

			LocalDate currentDate = new LocalDate();
			DateTimeFormatter dtf = ISODateTimeFormat.date();

			String today = dtf.print(currentDate);
			String tomorrow = dtf.print(currentDate.plusDays(1));

			NearbyHotelParams params = new NearbyHotelParams(String.valueOf(loc.getLatitude()),
				String.valueOf(loc.getLongitude()), "1",
				today, tomorrow, HOTEL_SORT, "true");
			searchParams = new HotelSearchParams();
			searchParams.setCheckInDate(currentDate);
			searchParams.setCheckOutDate(currentDate.plusDays(1));
			searchParams.setSearchLatLon(loc.getLatitude(), loc.getLongitude());
			searchParams.setFromLaunchScreen(true);

			downloadSubscription = hotelServices.nearbyHotels(params, downloadListener);
			launchDataTimeStamp = DateTime.now();
		}
	}

	@Subscribe
	public void onLocationNotAvailable(Events.LaunchLocationFetchError event) {
		Log.i(TAG, "Start collection download");
		launchListWidget.setVisibility(VISIBLE);
		launchError.setVisibility(View.GONE);
		launchDataTimeStamp = null;
		String country = PointOfSale.getPointOfSale().getTwoLetterCountryCode().toLowerCase(Locale.US);
		String localeCode = PointOfSale.getPointOfSale().getLocaleIdentifier();
		downloadSubscription = collectionServices.getPhoneCollection(
			ProductFlavorFeatureConfiguration.getInstance().getPhoneCollectionId(), country, localeCode,
			collectionDownloadListener);
	}

	@Subscribe
	public void onNetworkUnavailable(Events.LaunchOfflineState event) {
		Log.i(TAG, "Launch page is offline");
		launchListWidget.scrollToPosition(0);
		launchListWidget.setVisibility(GONE);
		launchError.setVisibility(View.VISIBLE);
	}

	// Air attach

	@Subscribe
	public void onShowAirAttach(Events.LaunchAirAttachBannerShow event) {
		if (isAirAttachDismissed) {
			airAttachBanner.setVisibility(View.GONE);
			return;
		}
		final HotelSearchParams params = event.params;
		if (airAttachBanner.getVisibility() == View.GONE) {
			airAttachBanner.setVisibility(View.VISIBLE);
			airAttachBanner.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					if (airAttachBanner.getHeight() == 0 || airAttachBanner.getVisibility() == View.GONE) {
						return true;
					}
					airAttachBanner.getViewTreeObserver().removeOnPreDrawListener(this);
					animateAirAttachBanner(params, true);
					return false;
				}
			});
		}
		else {
			animateAirAttachBanner(params, false);
		}

		airAttachBanner.setVisibility(View.VISIBLE);
	}

	private void animateAirAttachBanner(final HotelSearchParams hotelSearchParams, boolean animate) {
		airAttachBanner.setTranslationY(airAttachBanner.getHeight());
		airAttachBanner.animate()
			.translationY(0f)
			.setDuration(animate ? 300 : 0);
		// In the absence of search params from user's itin info,
		// launch into hotels mode.
		if (hotelSearchParams == null) {
			airAttachBanner.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Bundle animOptions = AnimUtils.createActivityScaleBundle(airAttachBanner);
					goToHotels(animOptions);
					OmnitureTracking.trackPhoneAirAttachBannerClick();
				}
			});
		}
		else {
			airAttachBanner.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					NavUtils.goToHotels(getContext(), hotelSearchParams);
					OmnitureTracking.trackPhoneAirAttachBannerClick();
				}
			});
			OmnitureTracking.trackPhoneAirAttachBanner();
		}
	}

	@Subscribe
	public void onHideAirAttach(Events.LaunchAirAttachBannerHide event) {
		airAttachBanner.setVisibility(View.GONE);
	}

	// onResume()-esque behavior

	private void setListState() {
		if (isExpired()) {
			launchListWidget.showListLoadingAnimation();
		}
	}

	public void initLaunchListScroll() {
		airAttachTranslation = 0;
		launchListWidget.scrollToPosition(0);
	}

	public void bindLobWidget() {
		int listHeaderPaddingTop;
		progressBar.setVisibility(View.GONE);
		initLaunchListScroll();
		PointOfSale currentPointOfSale = PointOfSale.getPointOfSale();

		boolean isCarsEnabled = currentPointOfSale.supports(LineOfBusiness.CARS);
		boolean isLXEnabled = currentPointOfSale.supports(LineOfBusiness.LX);
		boolean isTransportEnabled = currentPointOfSale.supports(LineOfBusiness.TRANSPORT);

		if (isCarsEnabled && isLXEnabled) {
			lobHeight = getResources().getDimension(R.dimen.launch_lob_double_row_container_height);
			lobSelectorWidget.setVisibility(View.GONE);
			if (isTransportEnabled) {
				doubleRowFiveLob = true;
				doubleRowLob = false;
				doubleRowLobSelectorWidget.setVisibility(View.GONE);
				doubleRowFiveLobSelectorWidget.transformButtons(1.0f);
				doubleRowFiveLobSelectorWidget.setVisibility(View.VISIBLE);
				listHeaderPaddingTop = R.dimen.launch_header_five_lob_top_space;
			}
			else {
				doubleRowLob = true;
				doubleRowFiveLob = false;
				doubleRowFiveLobSelectorWidget.setVisibility(View.GONE);
				doubleRowLobSelectorWidget.transformButtons(1.0f);
				doubleRowLobSelectorWidget.setVisibility(View.VISIBLE);
				listHeaderPaddingTop = R.dimen.launch_header_double_row_top_space;
			}
		}
		else {
			doubleRowLob = false;
			doubleRowFiveLob = false;
			lobHeight = getResources().getDimension(R.dimen.launch_lob_container_height);
			lobSelectorWidget.transformButtons(1.0f);
			lobSelectorWidget.setVisibility(View.VISIBLE);
			doubleRowLobSelectorWidget.setVisibility(View.GONE);
			doubleRowFiveLobSelectorWidget.setVisibility(View.GONE);
			listHeaderPaddingTop = R.dimen.launch_header_top_space;
			lobSelectorWidget.updateView();
		}
		launchListWidget.setHeaderPaddingTop(getResources().getDimension(listHeaderPaddingTop));
		squashedHeaderHeight = getResources().getDimension(R.dimen.launch_lob_squashed_height);
	}

	@Subscribe
	public void onLaunchResume(Events.PhoneLaunchOnResume event) {
		setListState();
	}

	@Subscribe
	public void onPOSChange(Events.PhoneLaunchOnPOSChange event) {
		bindLobWidget();
		setListState();
	}

	/**
	 * @return touchListener that consumes all touch events
	 */
	private OnTouchListener absorbTouchesListener() {
		return new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		};
	}
}
