package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.RoomsAndRatesAdapter;
import com.mobiata.android.ImageCache;
import com.mobiata.android.app.AsyncLoadListActivity;
import com.mobiata.hotellib.data.AvailabilityResponse;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.server.ExpediaServices;
import com.mobiata.hotellib.utils.JSONUtils;
import com.mobiata.hotellib.utils.StrUtils;

public class RoomsAndRatesListActivity extends AsyncLoadListActivity {

	private Property mProperty;
	private SearchParams mSearchParams;

	private RoomsAndRatesAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_rooms_and_rates);

		// Retrieve data to build this with
		final Intent intent = getIntent();
		Property property = mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY,
				Property.class);
		mSearchParams = (SearchParams) JSONUtils.parseJSONableFromIntent(intent, Codes.SEARCH_PARAMS,
				SearchParams.class);

		// Format the header
		ImageView thumbnailView = (ImageView) findViewById(R.id.thumbnail_image_view);
		if (property.getThumbnail() != null) {
			ImageCache.getInstance().loadImage(property.getThumbnail().getUrl(), thumbnailView);
		}
		else {
			thumbnailView.setVisibility(View.GONE);
		}

		TextView nameView = (TextView) findViewById(R.id.name_text_view);
		nameView.setText(property.getName());

		TextView locationView = (TextView) findViewById(R.id.location_text_view);
		locationView.setText(StrUtils.formatAddressShort(property.getLocation()));

		RatingBar hotelRating = (RatingBar) findViewById(R.id.hotel_rating_bar);
		hotelRating.setRating((float) property.getHotelRating());
	}

	@Override
	public String getUniqueKey() {
		return "com.expedia.bookings.roomsrates." + mProperty.getPropertyId();
	}

	@Override
	public void showProgress() {
		// TODO: Show progress for downloading rooms and rates information
	}

	@Override
	public Object downloadImpl() {
		return ExpediaServices.availability(this, mSearchParams, mProperty);
	}

	@Override
	public void onResults(Object results) {
		if (results == null) {
			// TODO: Add error handling here
			return;
		}

		AvailabilityResponse response = (AvailabilityResponse) results;

		mAdapter = new RoomsAndRatesAdapter(this, response.getRates());

		setListAdapter(mAdapter);
	}
}
