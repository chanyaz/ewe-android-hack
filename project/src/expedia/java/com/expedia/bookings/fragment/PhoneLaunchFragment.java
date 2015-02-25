package com.expedia.bookings.fragment;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.CarsActivity;
import com.expedia.bookings.activity.HotelDetailsFragmentActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.NearbyHotelParams;
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.services.HotelServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.DbUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.NetUtils;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.RequestInterceptor;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PhoneLaunchFragment extends Fragment implements IPhoneLaunchActivityLaunchFragment {

	private static final String TAG = "LAUNCH_SCREEN";

	// Used to prevent launching of both flight and hotel activities at once
	// (as it is otherwise possible to quickly click on both sides).
	private boolean launchingActivity;

	@InjectView(R.id.lob_selector)
	ViewGroup lobSelectorWidget;

	@InjectView(R.id.nearby_deals_widget)
	ViewGroup nearbyDealsWidget;

	private int actionBarSpace;

	private HotelServices hotelServices;
	private HotelSearchParams searchParams;

	// Invisible Fragment that handles FusedLocationProvider
	private FusedLocationProviderFragment locationFragment;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		locationFragment = FusedLocationProviderFragment.getInstance(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_new_phone_launch, container, false);
		ButterKnife.inject(this, v);
		lobSelectorWidget.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				actionBarSpace = ButterKnife.findById(v, R.id.action_bar_space).getHeight();
				nearbyDealsWidget.setTranslationY(lobSelectorWidget.getBottom() - actionBarSpace);
				Ui.removeOnGlobalLayoutListener(lobSelectorWidget, this);
			}
		});

		RequestInterceptor requestInterceptor = new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				request.addEncodedQueryParam("clientid", ServicesUtil.generateClientId(getActivity()));
				request.addEncodedQueryParam("sourceType", ServicesUtil.generateSourceType());

				String langid = ServicesUtil.generateLangId();
				if (Strings.isNotEmpty(langid)) {
					request.addEncodedQueryParam("langid", langid);
				}

				if (EndPoint.requestRequiresSiteId(getActivity())) {
					request.addEncodedQueryParam("siteid", ServicesUtil.generateSiteId());
				}
			}
		};
		hotelServices = new HotelServices(EndPoint.getE3EndpointUrl(getActivity(), true /*isSecure*/),
			DbUtils.generateOkHttpClient(),
			requestInterceptor,
			AndroidSchedulers.mainThread(),
			Schedulers.io());

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		findLocation();
		Events.register(this);
		launchingActivity = false;
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ButterKnife.reset(this);
	}

	// Nearby hotel search

	private void startNearbyHotelSearch(Location loc) {
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

	private Subscriber<List<Hotel>> downloadListener = new Subscriber<List<Hotel>>() {
		@Override
		public void onCompleted() {
			Log.d("nearbyhotelstatus", "completed");
		}

		@Override
		public void onError(Throwable e) {
			Log.d("nearbyhotelstatus", e.getMessage());
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

	// Listeners

	// The onClick for the "SEE ALL" button in the list header is in
	// NearbyHotelsListAdapter. See onSeeAllButtonPressed() below
	// for the Otto event sent by that onClick()
	@OnClick({R.id.hotels_button, R.id.flights_button, R.id.cars_button})
	public void enterLob(View view) {
		Bundle animOptions = AnimUtils.createActivityScaleBundle(view);
		switch (view.getId()) {
		case R.id.hotels_button:
			goToHotels(animOptions);
			break;
		case R.id.flights_button:
			if (!launchingActivity) {
				launchingActivity = true;
				NavUtils.goToFlights(getActivity(), animOptions);
				OmnitureTracking.trackLinkLaunchScreenToFlights(getActivity());
			}
			break;
		case R.id.cars_button:
			if (!launchingActivity) {
				launchingActivity = true;
				Intent carsIntent = new Intent(getActivity(), CarsActivity.class);
				getActivity().startActivity(carsIntent);
				break;
			}
		}
		cleanUp();
	}

	public void goToHotels(Bundle animOptions) {
		if (!launchingActivity) {
			launchingActivity = true;
			NavUtils.goToHotels(getActivity(), animOptions);
			OmnitureTracking.trackLinkLaunchScreenToHotels(getActivity());
		}
	}

	@Override
	public void startMarquee() {

	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void reset() {

	}

	// Location finder

	private void findLocation() {

		if (!NetUtils.isOnline(getActivity())) {
			// TODO: Use fallback data
			return;
		}

		locationFragment.find(new FusedLocationProviderFragment.FusedLocationProviderListener() {

			@Override
			public void onFound(Location currentLocation) {
				startNearbyHotelSearch(currentLocation);
			}

			@Override
			public void onError() {
				// TODO: Use fallback data
			}
		});
	}

	// Otto events

	@Subscribe
	public void onHotelOfferSelected(Events.LaunchListItemSelected event) throws JSONException {
		Hotel offer = event.offer;
		Property property = new Property();
		property.updateFrom(offer);
		Db.getHotelSearch().setSearchParams(searchParams);
		Db.getHotelSearch().setSelectedProperty(property);

		Intent intent = new Intent(getActivity(), HotelDetailsFragmentActivity.class);
		intent.putExtra(HotelDetailsMiniGalleryFragment.ARG_FROM_LAUNCH, true);
		NavUtils.startActivity(getActivity(), intent, null);
	}

	// "SEE ALL" button's onClick
	@Subscribe
	public void onSeeAllButtonPressed(Events.LaunchSeeAllButtonPressed event) {
		goToHotels(event.animOptions);
	}
}
