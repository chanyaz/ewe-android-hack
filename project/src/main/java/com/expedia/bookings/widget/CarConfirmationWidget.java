package com.expedia.bookings.widget;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.cars.CarCheckoutParamsBuilder;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Images;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarConfirmationWidget extends LinearLayout {

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

	@InjectView(R.id.background_image_view)
	public ImageView backroundImageView;

	@InjectView(R.id.pickup_location_text)
	TextView pickupLocationText;

	@InjectView(R.id.pickup_date_text)
	TextView pickupDateText;

	@InjectView(R.id.dropoff_date_text)
	TextView dropoffDateText;

	@InjectView(R.id.add_hotel_button)
	Button addHotelButton;

	@InjectView(R.id.add_flight_button)
	Button addFlightButton;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		Drawable navIcon = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
		navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(navIcon);
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Events.post(new Events.CarsGoToSearch());
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
			text = response.printErrors();
		}
		else {

			text = getResources().getString(R.string.successful_checkout_TEMPLATE, response.newTrip.itineraryNumber);
		}
		confirmationText.setText(text);
		emailText.setText(builder.getEmailAddress());

		String url = Images.getCarRental(bucket.category, bucket.getLowestTotalPriceOffer().vehicleInfo.type);
		new PicassoHelper.Builder(backroundImageView)
			.fade()
			.setTag("Car Confirmation")
			.fit()
			.centerCrop()
			.build()
			.load(url);
		CreateTripCarOffer offer = createTrip.carProduct;
		vendorText.setText(offer.vendor.name);
		pickupLocationText.setText(offer.pickUpLocation.locationDescription);

		DateTimeFormatter dtf = DateTimeFormat.forPattern("MMM dd, HH:mm aa");
		pickupDateText.setText(dtf.print(offer.pickupTime) + " to");
		dropoffDateText.setText(dtf.print(offer.dropOffTime));
		addHotelButton.setText(getResources()
			.getString(R.string.successful_checkout_cross_sell_hotel, offer.pickUpLocation.locationDescription));
		addFlightButton.setText(getResources()
			.getString(R.string.successful_checkout_cross_sell_flight, offer.pickUpLocation.locationDescription));
	}
}
