package com.expedia.bookings.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.cars.CarCheckoutParamsBuilder;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.AddToCalendarUtils;
import com.expedia.bookings.utils.CollectionUtils;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CarConfirmationWidget extends FrameLayout {

	private CarCheckoutParamsBuilder builder;
	private CategorizedCarOffers bucket;
	private CarCreateTripResponse createTrip;

	public CarConfirmationWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	//@InjectView(R.id.toolbar)
	Toolbar toolbar;

	//@InjectView(R.id.confirmation_text)
	TextView confirmationText;

	//@InjectView(R.id.email_text)
	TextView emailText;

	//@InjectView(R.id.vendor_text)
	TextView vendorText;

	//@InjectView(R.id.pickup_label)
	TextView pickUpLabel;

	//@InjectView(R.id.background_image_view)
	ImageView backgroundImageView;

	//@InjectView(R.id.pickup_location_text)
	TextView pickupLocationText;

	//@InjectView(R.id.pickup_date_text)
	TextView pickupDateText;

	//@InjectView(R.id.dropoff_date_text)
	TextView dropofDateText;

	//@InjectView(R.id.itinerary_text_view)
	TextView itinText;

	//@InjectView(R.id.add_hotel_textView)
	TextView addHotelTextView;

	//@InjectView(R.id.add_flight_textView)
	TextView addFlightTextView;

	//@InjectView(R.id.direction_action_textView)
	TextView directionsTextView;

	//@InjectView(R.id.calendar_action_textView)
	TextView calendarTextView;

	//@InjectView(R.id.text_container)
	ViewGroup textContainer;

	private CreateTripCarOffer offer;
	private Events.CarsShowCheckout tripParams;
	private String itineraryNumber;
	private UserStateManager userStateManager;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		userStateManager = Ui.getApplication(getContext()).appComponent().userStateManager();

		Drawable navIcon = getResources().getDrawable(R.drawable.ic_close_white_24dp).mutate();
		navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(navIcon);
		toolbar.setNavigationContentDescription(getResources().getString(R.string.toolbar_nav_icon_close_cont_desc));
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				NavUtils.goToItin(getContext());
				Events.post(new Events.FinishActivity());
			}
		});
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	@Subscribe
	public void onShowConfirmation(Events.CarsShowConfirmation event) {
		bind(event.checkoutResponse);
	}

	@Subscribe
	public void onDoCheckoutCall(Events.CarsKickOffCheckoutCall event) {
		builder = event.checkoutParamsBuilder;
	}

	@Subscribe
	public void onCarsShowDetails(Events.CarsShowDetails event) {
		bucket = event.categorizedCarOffers;
	}

	@Subscribe
	public void onCarsShowProductKeyDetails(Events.CarsShowProductKeyDetails event) {
		if (CollectionUtils.isNotEmpty(event.productKeyCarSearch.categories)) {
			bucket = event.productKeyCarSearch.categories.get(0);
		}
	}

	@Subscribe
	public void onOfferSelected(Events.CarsShowCheckout event) {
		tripParams = event;
	}

	@Subscribe
	public void onCheckoutCreateTripSuccess(Events.CarsCheckoutCreateTripSuccess event) {
		createTrip = event.response;
	}

	public void bind(CarCheckoutResponse response) {
		final Resources res = getResources();
		itineraryNumber = response.newTrip.itineraryNumber;
		confirmationText.setText(res.getString(R.string.successful_checkout_email_label));
		emailText.setText(builder.getEmailAddress());
		itinText.setText(res.getString(R.string.successful_checkout_TEMPLATE, itineraryNumber));

		String url = Images.getCarRental(bucket.category, bucket.getLowestTotalPriceOffer().vehicleInfo.type,
			getResources().getDimension(R.dimen.car_image_width));
		new PicassoHelper.Builder(backgroundImageView)
			.setError(R.drawable.cars_fallback)
			.fade()
			.fit()
			.centerCrop()
			.build()
			.load(url);
		offer = createTrip.carProduct;
		vendorText.setText(offer.vendor.name);
		pickupLocationText.setText(offer.pickUpLocation.locationDescription);
		directionsTextView.setText(res.getString(R.string.car_confirmation_directions, offer.vendor.name));
		directionsTextView.setContentDescription(Phrase.from(getContext(), R.string.car_confirmation_directions_cont_desc_TEMPLATE)
			.put("vendor", offer.vendor.name).format().toString());
		pickupDateText.setText(res.getString(R.string.car_confirmation_pickup_time_TEMPLATE,
			DateUtils.formatDateTime(getContext(), offer.getPickupTime().getMillis(),
				DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT)));
		dropofDateText.setText(DateUtils.formatDateTime(getContext(), offer.getDropOffTime().getMillis(),
			DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT));
		addHotelTextView.setText(res.getString(R.string.successful_checkout_cross_sell_hotel,
			offer.pickUpLocation.cityName));
		addHotelTextView.setContentDescription(Phrase.from(getContext(), R.string.successful_checkout_cross_sell_hotel_cont_desc_TEMPLATE)
			.put("location", offer.pickUpLocation.cityName).format().toString());
		addFlightTextView.setText(res.getString(R.string.successful_checkout_cross_sell_flight,
			offer.pickUpLocation.cityName));
		addFlightTextView.setContentDescription(Phrase.from(getContext(), R.string.successful_checkout_cross_sell_flight_cont_desc_TEMPLATE)
			.put("location", offer.pickUpLocation.cityName).format().toString());

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		toolbar.setPadding(0, statusBarHeight, 0, 0);
		textContainer.setPadding(0, statusBarHeight, 0, 0);

		dressAction(res, directionsTextView, R.drawable.car_directions);
		dressAction(res, calendarTextView, R.drawable.add_to_calendar);
		dressAction(res, addHotelTextView, R.drawable.car_hotel);
		dressAction(res, addFlightTextView, R.drawable.car_flights);

		FontCache.setTypeface(confirmationText, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(emailText, FontCache.Font.ROBOTO_LIGHT);

		// Fallback lat long in case we don't get it from create trip call.
		if (offer.pickUpLocation.latitude == null || offer.dropOffLocation.longitude == null) {
			offer.pickUpLocation.latitude = tripParams.location.latitude;
			offer.pickUpLocation.longitude = tripParams.location.longitude;
		}

		// Add guest itin to itin manager
		if (!userStateManager.isUserAuthenticated()) {
			String email = builder.getEmailAddress();
			String tripId = response.newTrip.itineraryNumber;
			ItineraryManager.getInstance().addGuestTrip(email, tripId);
		}

		// Show Add to Calendar only if sharing is supported.
		if (!ProductFlavorFeatureConfiguration.getInstance().shouldShowItinShare()) {
			calendarTextView.setVisibility(View.GONE);
		}

		OmnitureTracking.trackAppCarCheckoutConfirmation(response);
		AdTracker.trackCarBooked(response);
	}

	//@OnClick(R.id.add_hotel_textView)
	public void searchHotels() {
		HotelSearchParams sp = HotelSearchParams.fromCarParams(offer);
		NavUtils.goToHotels(getContext(), sp);
		OmnitureTracking.trackAppCarCheckoutConfirmationCrossSell(LineOfBusiness.HOTELS);
		Events.post(new Events.FinishActivity());
	}

	//@OnClick(R.id.add_flight_textView)
	public void searchFlight() {
		searchForFlights();
		OmnitureTracking.trackAppCarCheckoutConfirmationCrossSell(LineOfBusiness.FLIGHTS);
		Events.post(new Events.FinishActivity());
	}

	//@OnClick(R.id.direction_action_textView)
	public void getDirections() {
		Uri uri = Uri.parse("http://maps.google.com/maps?daddr=" + offer.pickUpLocation.toAddress());
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		getContext().startActivity(intent);
	}

	//@OnClick(R.id.calendar_action_textView)
	public void generateCalendarInsertIntent() {
		Intent intent = AddToCalendarUtils
			.generateCarAddToCalendarIntent(getContext(), itineraryNumber, offer);
		getContext().startActivity(intent);
	}

	private static void dressAction(Resources res, TextView textView, int drawableResId) {
		Drawable drawable = res.getDrawable(drawableResId);
		drawable.setColorFilter(res.getColor(R.color.cars_confirmation_icon_color), PorterDuff.Mode.SRC_IN);
		textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
		FontCache.setTypeface(textView, FontCache.Font.ROBOTO_REGULAR);
	}

	private void searchForFlights() {
		// Load the search params
		FlightSearchParams flightSearchParams = Db.getFlightSearch().getSearchParams();
		flightSearchParams.reset();

		Location loc = new Location();
		loc.setLatitude(offer.pickUpLocation.latitude);
		loc.setLongitude(offer.pickUpLocation.longitude);
		loc.setDestinationId(offer.pickUpLocation.locationCode);
		flightSearchParams.setArrivalLocation(loc);

		flightSearchParams.setDepartureDate(offer.getPickupTime().toLocalDate());
		flightSearchParams.setReturnDate(offer.getDropOffTime().toLocalDate());

		// Go to flights
		NavUtils.goToFlights(getContext(), flightSearchParams);
	}

	public void setFocusOnToolbarForAccessibility() {
		AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar);
	}

}
