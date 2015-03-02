package com.expedia.bookings.widget;

import java.util.List;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.CarActivity;
import com.expedia.bookings.activity.HotelDetailsFragmentActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.data.collections.Collection;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.NearbyHotelParams;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.fragment.HotelDetailsMiniGalleryFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.services.CollectionServices;
import com.expedia.bookings.services.HotelServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.DbUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.Strings;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.RequestInterceptor;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PhoneLaunchWidget extends FrameLayout {

	private static final String TAG = "PhoneLaunchWidget";
	private static final String COLLECTION_TITLE = "global-staff-picks";

	private HotelServices hotelServices;
	private HotelSearchParams searchParams;
	private CollectionServices collectionServices;


	@InjectView(R.id.lob_selector)
	ViewGroup lobSelectorWidget;

	@InjectView(R.id.launch_list_widget)
	ViewGroup launchListWidget;

	// Lifecycle

	public PhoneLaunchWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		ButterKnife.inject(this);
		setUpServices();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	public void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	// Set up
	// TODO not this
	private void setUpServices() {
		RequestInterceptor requestInterceptor = new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				request.addEncodedQueryParam("clientid", ServicesUtil.generateClientId(getContext()));
				request.addEncodedQueryParam("sourceType", ServicesUtil.generateSourceType());

				String langid = ServicesUtil.generateLangId();
				if (Strings.isNotEmpty(langid)) {
					request.addEncodedQueryParam("langid", langid);
				}

				if (EndPoint.requestRequiresSiteId(getContext())) {
					request.addEncodedQueryParam("siteid", ServicesUtil.generateSiteId());
				}
			}
		};
		hotelServices = new HotelServices(EndPoint.getE3EndpointUrl(getContext(), true /*isSecure*/),
				DbUtils.generateOkHttpClient(),
				requestInterceptor,
				AndroidSchedulers.mainThread(),
				Schedulers.io());

		collectionServices = new CollectionServices(EndPoint.getE3EndpointUrl(getContext(), true /*isSecure*/),
			DbUtils.generateOkHttpClient(),
			requestInterceptor,
			AndroidSchedulers.mainThread(),
			Schedulers.io());
	}

	private Subscriber<List<Hotel>> downloadListener = new Subscriber<List<Hotel>>() {
		@Override
		public void onCompleted() {
			Log.d(TAG, "Hotel download completed.");
		}

		@Override
		public void onError(Throwable e) {
			Log.d(TAG, e.getMessage());
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
			Db.getHotelSearch().setSearchResponse(response);
			Events.post(new Events.LaunchHotelSearchResponse(nearbyHotelResponse));
		}
	};

	private Subscriber<Collection> collectionDownloadListener = new Subscriber<Collection>() {
		@Override
		public void onCompleted() {
			Log.d(TAG, "Collection download completed.");
		}

		@Override
		public void onError(Throwable e) {
			String country = PointOfSale.getPointOfSale().getTwoLetterCountryCode().toLowerCase(Locale.US);
			collectionServices.getCollection(COLLECTION_TITLE, country, "default", collectionDownloadListener);
		}

		@Override
		public void onNext(Collection collection) {
			Events.post(new Events.CollectionDownloadComplete(collection));
		}
	};

	// Clicking

	// The onClick for the "SEE ALL" button in the list header is in
	// LaunchListAdapter. See onSeeAllButtonPressed() below
	// for the Otto event sent by that onClick()
	@OnClick({R.id.hotels_button, R.id.flights_button, R.id.cars_button})
	public void enterLob(View view) {
		Bundle animOptions = AnimUtils.createActivityScaleBundle(view);
		switch (view.getId()) {
			case R.id.hotels_button:
				goToHotels(animOptions);
				break;
			case R.id.flights_button:
				NavUtils.goToFlights(getContext(), animOptions);
				OmnitureTracking.trackLinkLaunchScreenToFlights(getContext());
				break;
			case R.id.cars_button:
				Intent carsIntent = new Intent(getContext(), CarActivity.class);
				getContext().startActivity(carsIntent);
				break;
		}
	}

	public void goToHotels(Bundle animOptions) {
		NavUtils.goToHotels(getContext(), animOptions);
		OmnitureTracking.trackLinkLaunchScreenToHotels(getContext());
	}

	/*
	 * Otto events
	 */

	// "SEE ALL" button's onClick
	@Subscribe
	public void onSeeAllButtonPressed(Events.LaunchSeeAllButtonPressed event) {
		goToHotels(event.animOptions);
	}

	@Subscribe
	public void onHotelOfferSelected(Events.LaunchListItemSelected event) throws JSONException {
		Hotel offer = event.offer;
		Property property = new Property();
		property.updateFrom(offer);
		Db.getHotelSearch().setSearchParams(searchParams);
		Db.getHotelSearch().setSelectedProperty(property);

		Intent intent = new Intent(getContext(), HotelDetailsFragmentActivity.class);
		intent.putExtra(HotelDetailsMiniGalleryFragment.ARG_FROM_LAUNCH, true);
		NavUtils.startActivity(getContext(), intent, null);
	}

	// Hotel search in collection location
	@Subscribe
	public void onCollectionLocationSelected(Events.LaunchCollectionItemSelected event) throws JSONException {
		Suggestion location = event.collectionLocation.location;
		HotelSearchParams params = new HotelSearchParams();
		params.setQuery(location.shortName);
		params.setSearchType(HotelSearchParams.SearchType.valueOf(location.type));
		params.setRegionId(location.id);
		params.setSearchLatLon(location.latLong.lat, location.latLong.lng);
		LocalDate now = LocalDate.now();
		params.setCheckInDate(now.plusDays(1));
		params.setCheckOutDate(now.plusDays(2));
		params.setNumAdults(2);
		params.setChildren(null);
		NavUtils.goToHotels(getContext(), params, event.animOptions, 0);
	}

	// Hotel Search
	@Subscribe
	public void onLocationFound(Events.LaunchLocationFetchComplete event) {
		Location loc = event.location;
		Log.i(TAG, "Start hotel search");
		LocalDate currentDate = new LocalDate();
		DateTimeFormatter dtf = ISODateTimeFormat.date();

		String today = dtf.print(currentDate);
		String tomorrow = dtf.print(currentDate.plusDays(1));

		NearbyHotelParams params = new NearbyHotelParams(String.valueOf(loc.getLatitude()),
				String.valueOf(loc.getLongitude()), "1",
				today, tomorrow, "MobileDeals");
		searchParams = new HotelSearchParams();
		searchParams.setCheckInDate(currentDate);
		searchParams.setCheckOutDate(currentDate.plusDays(1));
		searchParams.setSearchLatLon(loc.getLatitude(), loc.getLongitude());

		hotelServices.hotelSearch(params, downloadListener);
	}

	@Subscribe
	public void onLocationNotAvailable(Events.LaunchLocationFetchError event) {
		Log.i(TAG, "Start collection download");
		String country = PointOfSale.getPointOfSale().getTwoLetterCountryCode().toLowerCase(Locale.US);
		String localeCode = getContext().getResources().getConfiguration().locale.toString();
		collectionServices.getCollection(COLLECTION_TITLE, country, localeCode, collectionDownloadListener);
	}
}
