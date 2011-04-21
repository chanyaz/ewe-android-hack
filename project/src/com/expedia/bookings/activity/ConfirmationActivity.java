package com.expedia.bookings.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.LayoutUtils;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.hotellib.data.BookingResponse;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Location;
import com.mobiata.hotellib.data.Policy;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.Rate;
import com.mobiata.hotellib.data.RateBreakdown;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.utils.JSONUtils;
import com.mobiata.hotellib.utils.StrUtils;
import com.mobiata.hotellib.widget.HotelItemizedOverlay;

public class ConfirmationActivity extends MapActivity {

	private SearchParams mSearchParams;
	private Property mProperty;
	private Rate mRate;
	private BookingResponse mBookingResponse;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_confirmation);

		// Retrieve data to build this with
		final Intent intent = getIntent();
		mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY, Property.class);
		mSearchParams = (SearchParams) JSONUtils.parseJSONableFromIntent(intent, Codes.SEARCH_PARAMS,
				SearchParams.class);
		mRate = (Rate) JSONUtils.parseJSONableFromIntent(intent, Codes.RATE, Rate.class);
		mBookingResponse = (BookingResponse) JSONUtils.parseJSONableFromIntent(intent, Codes.BOOKING_RESPONSE,
				BookingResponse.class);

		// TODO: Delete this once done testing
		// This code allows us to test the ConfirmationActivity standalone, for layout purposes.
		// Just point the default launcher activity towards this instead of SearchActivity
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
			try {
				mSearchParams = new SearchParams();
				mSearchParams.fillWithTestData();
				mProperty = new Property();
				mProperty.fillWithTestData();
				mRate = new Rate();
				mRate.fillWithTestData();
				mBookingResponse = new BookingResponse();
				mBookingResponse.fillWithTestData();
			}
			catch (JSONException e) {
				Log.e("Couldn't create dummy data!", e);
			}
		}

		//////////////////////////////////////////////////
		// Screen configuration

		// Thumbnail in the map
		ImageView thumbnail = (ImageView) findViewById(R.id.thumbnail_image_view);
		if (mProperty.getThumbnail() != null) {
			ImageCache.getInstance().loadImage(mProperty.getThumbnail().getUrl(), thumbnail);
		}
		else {
			thumbnail.setVisibility(View.GONE);
		}

		// Show on the map where the hotel is
		MapView mapView = (MapView) findViewById(R.id.map_view);
		List<Property> properties = new ArrayList<Property>(1);
		properties.add(mProperty);
		List<Overlay> overlays = mapView.getOverlays();
		HotelItemizedOverlay overlay = new HotelItemizedOverlay(this, properties, false, mapView, null);
		overlays.add(overlay);
		MapController mc = mapView.getController();
		GeoPoint center = overlay.getCenter();
		GeoPoint offsetCenter = new GeoPoint(center.getLatitudeE6() + 1000, center.getLongitudeE6() - 8000);
		mc.setCenter(offsetCenter);
		mc.setZoom(15);

		// Overview of hotel
		TextView nameView = (TextView) findViewById(R.id.name_text_view);
		nameView.setText(mProperty.getName());
		Location location = mProperty.getLocation();
		TextView address1View = (TextView) findViewById(R.id.address1_text_view);
		address1View.setText(StrUtils.formatAddressStreet(location));
		TextView address2View = (TextView) findViewById(R.id.address2_text_view);
		address2View.setText(StrUtils.formatAddressCity(location));
		RatingBar hotelRating = (RatingBar) findViewById(R.id.hotel_rating_bar);
		hotelRating.setRating((float) mProperty.getHotelRating());
		RatingBar tripAdvisorRating = (RatingBar) findViewById(R.id.trip_advisor_rating_bar);
		tripAdvisorRating.setRating((float) mProperty.getTripAdvisorRating());

		// Reservation summary
		ViewGroup detailsLayout = (ViewGroup) findViewById(R.id.details_layout);
		LayoutUtils.addDetail(this, detailsLayout, R.string.confirmation_number, mBookingResponse.getConfNumber());
		LayoutUtils.addDetail(this, detailsLayout, R.string.itinerary_number, mBookingResponse.getItineraryId());
		LayoutUtils.addRateDetails(this, detailsLayout, mSearchParams, mProperty, mRate);

		// Total cost / cancellation policy at the bottom
		TextView totalCostView = (TextView) findViewById(R.id.total_cost_text_view);
		totalCostView.setText(mRate.getTotalAmountAfterTax().getFormattedMoney());
		TextView cancellationPolicyView = (TextView) findViewById(R.id.cancellation_policy_text_view);
		Policy cancellationPolicy = mRate.getRateRules().getPolicy(Policy.TYPE_CANCEL);
		if (cancellationPolicy != null) {
			cancellationPolicyView.setText(Html.fromHtml(cancellationPolicy.getDescription()));
		}
		else {
			cancellationPolicyView.setVisibility(View.GONE);
		}

		//////////////////////////////////////////////////
		// Button bar configuration
		ImageButton shareButton = (ImageButton) findViewById(R.id.share_button);
		shareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				share();
			}
		});

		ImageButton mapButton = (ImageButton) findViewById(R.id.map_button);
		mapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent newIntent = new Intent(Intent.ACTION_VIEW);
				String queryAddress = com.mobiata.hotellib.utils.StrUtils.formatAddress(mProperty.getLocation())
						.replace("\n", " ");
				newIntent.setData(Uri.parse("geo:0,0?q=" + queryAddress));
				startActivity(newIntent);
			}
		});
	}

	public void share() {
		Resources res = getResources();

		DateFormat dateFormatter = new SimpleDateFormat("MM/dd");
		DateFormat fullDateFormatter = android.text.format.DateFormat.getMediumDateFormat(this);
		DateFormat dayFormatter = new SimpleDateFormat("EEE");

		Date checkIn = mSearchParams.getCheckInDate().getTime();
		Date checkOut = mSearchParams.getCheckOutDate().getTime();

		// Create the subject
		String dateStart = dateFormatter.format(checkIn);
		String dateEnd = dateFormatter.format(checkOut);
		String subject = getString(R.string.share_subject_template, mProperty.getName(), dateStart, dateEnd);

		// Create the body 
		StringBuilder body = new StringBuilder();
		body.append(getString(R.string.share_body_start));
		body.append("\n\n");
		body.append(mProperty.getName());
		body.append("\n");
		body.append(StrUtils.formatAddress(mProperty.getLocation()));
		body.append("\n\n");
		appendLabelValue(body, R.string.confirmation_number, mBookingResponse.getConfNumber());
		body.append("\n\n");
		appendLabelValue(body, R.string.itinerary_number, mBookingResponse.getItineraryId());
		body.append("\n\n");
		appendLabelValue(body, R.string.CheckIn,
				dayFormatter.format(checkIn) + ", " + fullDateFormatter.format(checkIn));
		body.append("\n");
		appendLabelValue(body, R.string.CheckOut,
				dayFormatter.format(checkOut) + ", " + fullDateFormatter.format(checkOut));
		body.append("\n");
		body.append(res.getQuantityString(R.plurals.number_of_adults, mSearchParams.getNumAdults(),
				mSearchParams.getNumAdults()));
		body.append("\n");
		body.append(res.getQuantityString(R.plurals.number_of_children, mSearchParams.getNumChildren(),
				mSearchParams.getNumChildren()));
		body.append("\n\n");

		if (mRate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : mRate.getRateBreakdownList()) {
				Date date = breakdown.getDate().getCalendar().getTime();
				String dateStr = dayFormatter.format(date) + ", " + fullDateFormatter.format(date);
				appendLabelValue(body, getString(R.string.room_rate_template, dateStr), breakdown.getAmount()
						.getFormattedMoney());
			}
			body.append("\n\n");
		}

		appendLabelValue(body, R.string.subtotal, mRate.getTotalAmountBeforeTax().getFormattedMoney());
		body.append("\n");
		appendLabelValue(body, R.string.TaxesAndFees, mRate.getTaxesAndFeesPerRoom().getFormattedMoney());
		body.append("\n\n");
		appendLabelValue(body, R.string.Total, mRate.getTotalAmountAfterTax().getFormattedMoney());

		Policy cancellationPolicy = mRate.getRateRules().getPolicy(Policy.TYPE_CANCEL);
		if (cancellationPolicy != null) {
			body.append("\n\n");
			body.append(getString(R.string.cancellation_policy));
			body.append("\n");
			body.append(Html.fromHtml(cancellationPolicy.getDescription()));
		}

		SocialUtils.email(this, subject, body.toString());
	}

	private void appendLabelValue(StringBuilder sb, int labelStrId, String value) {
		appendLabelValue(sb, getString(labelStrId), value);
	}

	private void appendLabelValue(StringBuilder sb, String label, String value) {
		sb.append(label);
		sb.append(": ");
		sb.append(value);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
