package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.ImageAdapter;
import com.mobiata.android.ImageCache;
import com.mobiata.hotellib.Params;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Media;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.Property.Amenity;
import com.mobiata.hotellib.utils.JSONUtils;

public class HotelActivity extends Activity {

	private Property mProperty;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_hotel);

		// Retrieve data to build this with
		Intent intent = getIntent();
		Property property = mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY,
				Property.class);

		// Fill in header views
		TextView name = (TextView) findViewById(R.id.name_text_view);
		name.setText(property.getName());
		RatingBar hotelRating = (RatingBar) findViewById(R.id.hotel_rating_bar);
		hotelRating.setRating((float) property.getHotelRating());
		RatingBar tripAdvisorRating = (RatingBar) findViewById(R.id.trip_advisor_rating_bar);
		tripAdvisorRating.setRating((float) property.getTripAdvisorRating());

		// Configure the gallery
		Gallery gallery = (Gallery) findViewById(R.id.images_gallery);
		if (property.getMediaCount() > 0) {
			ImageAdapter adapter = new ImageAdapter(this, property.getMediaList());
			gallery.setAdapter(adapter);
		}
		else {
			gallery.setVisibility(View.GONE);
		}

		// Amenities
		ViewGroup amenitiesContainer = (ViewGroup) findViewById(R.id.amenities_table_row);
		// We have to do these manually as multiple amenities can lead to the same icon.
		if (property.hasAmenity(Amenity.POOL) || property.hasAmenity(Amenity.POOL_INDOOR)
				|| property.hasAmenity(Amenity.POOL_OUTDOOR)) {
			addAmenity(amenitiesContainer, R.drawable.ic_amenity_pool, R.string.AmenityPool);
		}
		if (property.hasAmenity(Amenity.INTERNET)) {
			addAmenity(amenitiesContainer, R.drawable.ic_amenity_internet, R.string.amenity_internet);
		}
		if (property.hasAmenity(Amenity.BREAKFAST)) {
			addAmenity(amenitiesContainer, R.drawable.ic_amenity_breakfast, R.string.AmenityBreakfast);
		}
		if (property.hasAmenity(Amenity.PARKING) || property.hasAmenity(Amenity.EXTENDED_PARKING)
				|| property.hasAmenity(Amenity.FREE_PARKING)) {
			addAmenity(amenitiesContainer, R.drawable.ic_amenity_parking, R.string.AmenityParking);
		}
		if (property.hasAmenity(Amenity.PETS_ALLOWED)) {
			addAmenity(amenitiesContainer, R.drawable.ic_amenity_pets, R.string.amenity_pets);
		}

		// Description
		String description = property.getDescriptionText();
		if (description != null && description.length() > 0) {
			TextView descriptionView = (TextView) findViewById(R.id.description_text_view);
			descriptionView.setText(Html.fromHtml(description.replace("&gt;", ">").replace("&lt;", "<")));
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isFinishing()) {
			// In order to avoid memory issues, clear the cache of images we might've loaded in this activity
			if (Params.isLoggingEnabled()) {
				Log.d(Params.getLoggingTag(), "Clearing out images from property.");
			}

			ImageCache cache = ImageCache.getInstance();
			for (Media image : mProperty.getMediaList()) {
				String imageUrl = image.getUrl();
				cache.removeImage(imageUrl, true);
			}
		}
	}

	public void addAmenity(ViewGroup amenitiesTable, int iconResourceId, int strResourceId) {
		View amenityLayout = getLayoutInflater().inflate(R.layout.snippet_amenity, amenitiesTable, false);

		ImageView amenityIcon = (ImageView) amenityLayout.findViewById(R.id.icon_text_view);
		amenityIcon.setImageResource(iconResourceId);

		TextView amenityName = (TextView) amenityLayout.findViewById(R.id.name_text_view);
		amenityName.setText(strResourceId);

		amenitiesTable.addView(amenityLayout);
	}
}
