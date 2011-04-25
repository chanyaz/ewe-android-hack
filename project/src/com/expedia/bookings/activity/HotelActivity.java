package com.expedia.bookings.activity;

import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.widget.ImageAdapter;
import com.mobiata.android.ImageCache;
import com.mobiata.android.ImageCache.OnImageLoaded;
import com.mobiata.android.Log;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Media;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.Property.Amenity;
import com.mobiata.hotellib.data.Rate;
import com.mobiata.hotellib.utils.JSONUtils;

public class HotelActivity extends Activity {

	private Property mProperty;

	private int mImageToLoad;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_hotel);

		// Retrieve data to build this with
		final Intent intent = getIntent();
		Property property = mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY,
				Property.class);

		// TODO: Delete this once done testing
		// This code allows us to test the HotelActivity standalone, for layout purposes.
		// Just point the default launcher activity towards this instead of SearchActivity
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
			try {
				property = mProperty = new Property();
				mProperty.fillWithTestData();
			}
			catch (JSONException e) {
				Log.e("Couldn't create dummy data!", e);
			}
		}

		// Fill in header views
		LayoutUtils.configureHeader(this, property, new OnClickListener() {
			public void onClick(View v) {
				startRoomRatesActivity();
			}
		});

		// Configure the gallery
		Gallery gallery = (Gallery) findViewById(R.id.images_gallery);
		if (property.getMediaCount() > 0) {
			final List<Media> mediaList = property.getMediaList();
			ImageAdapter adapter = new ImageAdapter(this, mediaList);
			gallery.setAdapter(adapter);

			// Start loading images in the background.  Load them one-by-one.
			final ImageCache imageCache = ImageCache.getInstance();
			mImageToLoad = 0;
			OnImageLoaded loader = new OnImageLoaded() {
				@Override
				public void onImageLoaded(String url, Bitmap bitmap) {
					if (mImageToLoad < mediaList.size()) {
						Media media = mediaList.get(mImageToLoad++);
						imageCache.loadImage(media.getUrl(), this);
					}
				}
			};
			loader.onImageLoaded(null, null);
		}
		else {
			gallery.setVisibility(View.GONE);
		}

		// Configure views on top of the gallery
		Rate lowestRate = property.getLowestRate();
		String promoDescription = lowestRate.getPromoDescription();
		if (promoDescription != null && promoDescription.length() > 0) {
			TextView promoView = (TextView) findViewById(R.id.promo_description_text_view);
			promoView.setVisibility(View.VISIBLE);
			promoView.setText(Html.fromHtml(promoDescription));
			promoView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.expand_bottom));
		}

		ViewGroup priceContainer = (ViewGroup) findViewById(R.id.price_layout);
		priceContainer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startRoomRatesActivity();
			}
		});
		priceContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_left));
		TextView fromView = (TextView) findViewById(R.id.from_text_view);
		if (lowestRate.getSavingsPercent() > 0) {
			fromView.setText(Html.fromHtml(
					getString(R.string.from_template, lowestRate.getAverageBaseRate().getFormattedMoney()), null,
					new StrikethroughTagHandler()));
		}
		else {
			fromView.setText(R.string.from);
		}

		TextView priceView = (TextView) findViewById(R.id.price_text_view);
		priceView.setText(lowestRate.getAverageRate().getFormattedMoney());

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
			Log.d("Clearing out images from property.");

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

	public void startRoomRatesActivity() {
		Intent roomsRatesIntent = new Intent(this, RoomsAndRatesListActivity.class);
		roomsRatesIntent.fillIn(getIntent(), 0);
		startActivity(roomsRatesIntent);
	}
}
