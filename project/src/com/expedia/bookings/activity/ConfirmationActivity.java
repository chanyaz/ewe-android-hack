package com.expedia.bookings.activity;

import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.LayoutUtils;
import com.google.android.maps.MapActivity;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.hotellib.data.BookingResponse;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Location;
import com.mobiata.hotellib.data.Policy;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.Rate;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.utils.JSONUtils;
import com.mobiata.hotellib.utils.StrUtils;

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

		// TODO: Show on map where hotel is

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
		boolean foundCancellationPolicy = false;
		for (Policy policy : mRate.getRateRules().getPolicies()) {
			if (policy.getType() == Policy.TYPE_CANCEL) {
				foundCancellationPolicy = true;
				cancellationPolicyView.setText(policy.getDescription());
			}
		}
		if (!foundCancellationPolicy) {
			cancellationPolicyView.setVisibility(View.GONE);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
