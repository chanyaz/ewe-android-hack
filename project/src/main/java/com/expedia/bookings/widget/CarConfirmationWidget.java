package com.expedia.bookings.widget;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.SocialUtils;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CarConfirmationWidget extends RelativeLayout {

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

	@InjectView(R.id.add_hotel_button)
	Button addHotelButton;

	@InjectView(R.id.add_flight_button)
	Button addFlightButton;

	@InjectView(R.id.call_parent)
	ViewGroup callParent;

	@InjectView(R.id.call_container)
	ViewGroup callContainer;

	@InjectView(R.id.local_phone_number_text_view)
	TextView localPhoneNumber;

	@InjectView(R.id.toll_free_phone_number_text_view)
	TextView tollFreePhoneNumber;

	@InjectView(R.id.direction_action_button)
	Button directionsButton;

	@InjectView(R.id.calendar_action_button)
	Button calendarButton;

	@InjectView(R.id.call_action_button)
	Button callButton;

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

		callContainer.setVisibility(INVISIBLE);
		toolbar.setPadding(0, Ui.getStatusBarHeight(getContext()), 0, 0);
		callParent.setVisibility(INVISIBLE);
		callParent.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				showPhoneNumbers(false);
				return true;
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
		String text;
		if (response.hasErrors()) {
			text = response.errorsToString();
		}
		else {
			itineraryNumber = response.newTrip.itineraryNumber;
			text = getResources().getString(R.string.successful_checkout_TEMPLATE, itineraryNumber);
		}
		confirmationText.setText(text);
		emailText.setText(builder.getEmailAddress());

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

		DateTimeFormatter dtf = DateTimeFormat.forPattern("MMM dd, hh:mm aa");
		pickupDateText.setText(dtf.print(offer.pickupTime) + " to");
		dropofDateText.setText(dtf.print(offer.dropOffTime));
		addHotelButton.setText(getResources()
			.getString(R.string.successful_checkout_cross_sell_hotel, offer.pickUpLocation.locationDescription));
		addFlightButton.setText(getResources()
			.getString(R.string.successful_checkout_cross_sell_flight, offer.pickUpLocation.locationDescription));
		localPhoneNumber.setText(
			getResources().getString(R.string.car_confirmation_local_support_TEMPLATE, offer.vendor.localPhoneNumber));
		tollFreePhoneNumber.setText(
			getResources().getString(R.string.car_confirmation_toll_free_support_TEMPLATE, offer.vendor.phoneNumber));

		vendorText.setPadding(0, Ui.getStatusBarHeight(getContext()), 0, 0);

		Drawable drawableDirection = getResources().getDrawable(R.drawable.car_directions);
		drawableDirection
			.setColorFilter(getResources().getColor(R.color.cars_confirmation_icon_color), PorterDuff.Mode.SRC_IN);
		directionsButton.setCompoundDrawablesWithIntrinsicBounds(drawableDirection, null, null, null);
		FontCache.setTypeface(directionsButton, FontCache.Font.ROBOTO_REGULAR);

		Drawable drawableCalendar = getResources().getDrawable(R.drawable.add_to_calendar);
		drawableCalendar
			.setColorFilter(getResources().getColor(R.color.cars_confirmation_icon_color), PorterDuff.Mode.SRC_IN);
		calendarButton.setCompoundDrawablesWithIntrinsicBounds(drawableCalendar, null, null, null);
		FontCache.setTypeface(calendarButton, FontCache.Font.ROBOTO_REGULAR);

		Drawable drawableCustomerSupport = getResources().getDrawable(R.drawable.car_call);
		drawableCustomerSupport
			.setColorFilter(getResources().getColor(R.color.cars_confirmation_icon_color), PorterDuff.Mode.SRC_IN);
		callButton.setCompoundDrawablesWithIntrinsicBounds(drawableCustomerSupport, null, null, null);
		FontCache.setTypeface(callButton, FontCache.Font.ROBOTO_REGULAR);

		Drawable drawableHotel = getResources().getDrawable(R.drawable.car_hotel);
		drawableHotel
			.setColorFilter(getResources().getColor(R.color.cars_confirmation_icon_color), PorterDuff.Mode.SRC_IN);
		addHotelButton.setCompoundDrawablesWithIntrinsicBounds(drawableHotel, null, null, null);
		FontCache.setTypeface(addHotelButton, FontCache.Font.ROBOTO_REGULAR);

		Drawable drawableFlight = getResources().getDrawable(R.drawable.car_flights);
		drawableFlight
			.setColorFilter(getResources().getColor(R.color.cars_confirmation_icon_color), PorterDuff.Mode.SRC_IN);
		addFlightButton.setCompoundDrawablesWithIntrinsicBounds(drawableFlight, null, null, null);
		FontCache.setTypeface(addFlightButton, FontCache.Font.ROBOTO_REGULAR);

		FontCache.setTypeface(confirmationText, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(emailText, FontCache.Font.ROBOTO_LIGHT);
	}

	@OnClick(R.id.add_hotel_button)
	public void searchHotels() {
		HotelSearchParams sp = HotelSearchParams.fromCarParams(offer);
		NavUtils.goToHotels(getContext(), sp);
	}

	@OnClick(R.id.add_flight_button)
	public void searchFlight() {
		searchForFlights();
	}

	@OnClick(R.id.direction_action_button)
	public void getDirections() {
		Uri uri = Uri.parse("http://maps.google.com/maps?daddr=" + offer.pickUpLocation.toAddress());
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		getContext().startActivity(intent);
	}

	@OnClick(R.id.calendar_action_button)
	public void generateCalendarInsertIntent() {
		PointOfSale pointOfSale = PointOfSale.getPointOfSale();
		Intent intent = AddToCalendarUtils
			.generateCarAddToCalendarIntent(getContext(), pointOfSale, itineraryNumber, offer);
		getContext().startActivity(intent);
	}

	@OnClick(R.id.call_action_button)
	public void callSupport() {
		if (offer.vendor.phoneNumber != null && offer.vendor.localPhoneNumber
			.equalsIgnoreCase(offer.vendor.phoneNumber)) {
			SocialUtils.call(getContext(), offer.vendor.localPhoneNumber);
		}
		else {
			showPhoneNumbers(true);
		}
	}

	@OnClick(R.id.local_phone_number_text_view)
	public void callLocalNumber() {
		SocialUtils.call(getContext(),
			localPhoneNumber.getText().toString().substring(localPhoneNumber.getText().toString().indexOf(":")));
	}

	@OnClick(R.id.toll_free_phone_number_text_view)
	public void callTollFreeNumber() {
		SocialUtils.call(getContext(), tollFreePhoneNumber.getText().toString()
			.substring(tollFreePhoneNumber.getText().toString().indexOf(":")));
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

	public void showPhoneNumbers(boolean show) {
		callParent.setVisibility(show ? VISIBLE : GONE);
		callContainer.setTranslationY(show ? callContainer.getHeight() : 0);
		callContainer.setVisibility(VISIBLE);

		ObjectAnimator animator = ObjectAnimator
			.ofFloat(callContainer, "translationY", show ? 0 : callContainer.getHeight());
		animator.setDuration(200);
		animator.start();
	}
}
