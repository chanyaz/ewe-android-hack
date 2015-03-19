package com.expedia.bookings.widget;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.cars.CarCheckoutParamsBuilder;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.AddToCalendarUtils;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CarConfirmationWidget extends FrameLayout {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("MMM dd, hh:mm aa");

	private CarCheckoutParamsBuilder builder;
	private CategorizedCarOffers bucket;
	private CarCreateTripResponse createTrip;

	public CarConfirmationWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.toolbar)
	Toolbar toolbar;

	@InjectView(R.id.confirmation_text)
	TextView confirmationText;

	@InjectView(R.id.email_text)
	TextView emailText;

	@InjectView(R.id.vendor_text)
	TextView vendorText;

	@InjectView(R.id.pickup_label)
	TextView pickUpLabel;

	@InjectView(R.id.background_image_view)
	ImageView backgroundImageView;

	@InjectView(R.id.pickup_location_text)
	TextView pickupLocationText;

	@InjectView(R.id.pickup_date_text)
	TextView pickupDateText;

	@InjectView(R.id.dropoff_date_text)
	TextView dropofDateText;

	@InjectView(R.id.itinerary_text_view)
	TextView itinText;

	@InjectView(R.id.add_hotel_textView)
	TextView addHotelTextView;

	@InjectView(R.id.add_flight_textView)
	TextView addFlightTextView;

	@InjectView(R.id.direction_action_textView)
	TextView directionsTextView;

	@InjectView(R.id.calendar_action_textView)
	TextView calendarTextView;

	@InjectView(R.id.text_container)
	ViewGroup textContainer;

	private CreateTripCarOffer offer;
	private String itineraryNumber;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		Drawable navIcon = getResources().getDrawable(R.drawable.ic_close_white_24dp).mutate();
		navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(navIcon);
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				NavUtils.goToItin(getContext());
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
	public void onShowCheckout(Events.CarsShowCheckout event) {
		createTrip = event.createTripResponse;
	}

	public void bind(CarCheckoutResponse response) {
		final Resources res = getResources();
		itineraryNumber = response.newTrip.itineraryNumber;
		confirmationText.setText(res.getString(R.string.successful_checkout_email_label));
		emailText.setText(builder.getEmailAddress());
		itinText.setText(res.getString(R.string.successful_checkout_TEMPLATE, itineraryNumber));

		String url = Images.getCarRental(bucket.category, bucket.getLowestTotalPriceOffer().vehicleInfo.type);
		new PicassoHelper.Builder(backgroundImageView)
			.fade()
			.setTag("Car Confirmation")
			.fit()
			.centerCrop()
			.build()
			.load(url);
		offer = createTrip.carProduct;
		vendorText.setText(offer.vendor.name);
		pickupLocationText.setText(offer.pickUpLocation.locationDescription);
		directionsTextView.setText(res.getString(R.string.car_confirmation_directions, offer.vendor.name));
		pickupDateText.setText(res.getString(R.string.car_confirmation_pickup_time_TEMPLATE,
			DateFormatUtils.formatDateTimeRange(getContext(), offer.pickupTime, offer.pickupTime,
				DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT)));
		dropofDateText.setText(DateFormatUtils.formatDateTimeRange(getContext(), offer.dropOffTime, offer.dropOffTime,
			DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT));
		addHotelTextView.setText(res.getString(R.string.successful_checkout_cross_sell_hotel,
			offer.pickUpLocation.locationDescription));
		addFlightTextView.setText(res.getString(R.string.successful_checkout_cross_sell_flight,
			offer.pickUpLocation.locationDescription));

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		toolbar.setPadding(0, statusBarHeight, 0, 0);
		textContainer.setPadding(0, statusBarHeight, 0, 0);

		dressAction(res, directionsTextView, R.drawable.car_directions);
		dressAction(res, calendarTextView, R.drawable.add_to_calendar);
		dressAction(res, addHotelTextView, R.drawable.car_hotel);
		dressAction(res, addFlightTextView, R.drawable.car_flights);

		FontCache.setTypeface(confirmationText, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(emailText, FontCache.Font.ROBOTO_LIGHT);
	}

	@OnClick(R.id.add_hotel_textView)
	public void searchHotels() {
		HotelSearchParams sp = HotelSearchParams.fromCarParams(offer);
		NavUtils.goToHotels(getContext(), sp);
	}

	@OnClick(R.id.add_flight_textView)
	public void searchFlight() {
		searchForFlights();
	}

	@OnClick(R.id.direction_action_textView)
	public void getDirections() {
		Uri uri = Uri.parse("http://maps.google.com/maps?daddr=" + offer.pickUpLocation.toAddress());
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		getContext().startActivity(intent);
	}

	@OnClick(R.id.calendar_action_textView)
	public void generateCalendarInsertIntent() {
		PointOfSale pointOfSale = PointOfSale.getPointOfSale();
		Intent intent = AddToCalendarUtils
			.generateCarAddToCalendarIntent(getContext(), pointOfSale, itineraryNumber, offer);
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

		flightSearchParams.setDepartureDate(offer.pickupTime.toLocalDate());
		flightSearchParams.setReturnDate(offer.dropOffTime.toLocalDate());

		// Go to flights
		NavUtils.goToFlightsUsingSearchParams(getContext());
	}
}
