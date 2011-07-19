package com.expedia.bookings.activity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.widget.Gallery;
import com.expedia.bookings.widget.Gallery.OnScrollListener;
import com.mobiata.android.ImageCache;
import com.mobiata.android.ImageCache.OnImageLoaded;
import com.mobiata.android.Log;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Location;
import com.mobiata.hotellib.data.Media;
import com.mobiata.hotellib.data.Money;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.Property.Amenity;
import com.mobiata.hotellib.data.Rate;
import com.mobiata.hotellib.utils.JSONUtils;
import com.mobiata.hotellib.utils.StrUtils;
import com.omniture.AppMeasurement;

public class HotelActivity extends Activity {

	// This is the position in the list that the hotel had when the user clicked on it 
	public static final String EXTRA_POSITION = "EXTRA_POSITION";

	private Context mContext;

	private ScrollView mScrollView;

	private Gallery mGallery;

	private Property mProperty;

	private int mImageToLoad;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupHotelActivity(savedInstanceState, getIntent());
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setupHotelActivity(null, intent);
	}

	private void setupHotelActivity(Bundle savedInstanceState, final Intent intent) {
		mContext = this;

		setContentView(R.layout.activity_hotel);

		// Retrieve data to build this with
		Property property = mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY,
				Property.class);

		// Retrieve the last instance
		boolean startFlipping = true;
		Instance instance = (Instance) getLastNonConfigurationInstance();
		if (instance != null) {
			startFlipping = instance.mGalleryFlipping;
		}

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
				startRoomRatesActivity(intent);
			}
		});

		// Configure the gallery
		Gallery gallery = mGallery = (Gallery) findViewById(R.id.images_gallery);
		mScrollView = (ScrollView) findViewById(R.id.scroll_view);
		if (property.getMediaCount() > 0) {
			final List<String> urls = new ArrayList<String>(property.getMediaCount());
			for (Media media : property.getMediaList()) {
				urls.add(media.getUrl());
			}
			gallery.setUrls(urls);

			// Start loading images in the background.  Load them one-by-one.
			mImageToLoad = 0;
			OnImageLoaded loader = new OnImageLoaded() {
				@Override
				public void onImageLoaded(String url, Bitmap bitmap) {
					if (mImageToLoad < urls.size()) {
						String nextUrl = urls.get(mImageToLoad++);
						ImageCache.loadImage(toString() + nextUrl, nextUrl, this);
					}
				}
			};
			loader.onImageLoaded(null, null);

			if (startFlipping) {
				gallery.startFlipping();
			}

			// Set it up so that we scroll to the top whenever user scrolls the gallery
			// ONLY do this is not landscape
			if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
				gallery.setOnScrollListener(new OnScrollListener() {
					public void onScroll() {
						mScrollView.smoothScrollTo(0, 0);
					}
				});
			}
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
				startRoomRatesActivity(intent);
			}
		});
		priceContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_left));
		TextView fromView = (TextView) findViewById(R.id.from_text_view);
		if (lowestRate.getSavingsPercent() > 0) {
			Money baseRate = lowestRate.getDisplayBaseRate();
			fromView.setText(Html.fromHtml(
					getString(R.string.from_template,
							baseRate.getFormattedMoney(Money.F_NO_DECIMAL + Money.F_ROUND_DOWN)), null,
					new StrikethroughTagHandler()));
		}
		else {
			fromView.setText(R.string.from);
		}

		TextView priceView = (TextView) findViewById(R.id.price_text_view);
		priceView.setText(lowestRate.getDisplayRate().getFormattedMoney(Money.F_NO_DECIMAL + Money.F_ROUND_DOWN));

		Button mapButton = (Button) findViewById(R.id.map_button);
		mapButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(mContext, HotelMapActivity.class);
				intent.fillIn(intent, 0);
				startActivity(intent);
			}
		});
		
		Button userReviewsButton = (Button) findViewById(R.id.user_reviews_button);
		if(mProperty.hasExpediaReviews()) {
			userReviewsButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(mContext, UserReviewsListActivity.class);
					intent.fillIn(intent, 0);
					startActivity(intent);
				}	
			});
		}
		else {
			userReviewsButton.setEnabled(false);
			userReviewsButton.setTextColor(R.color.btn_text_disabled);
//			userReviewsButton.setShadowLayer((float) 1.0, (float) 1.0, (float) 1.0, R.color.hotel_activity_user_review_disabled_button_shadow);
		}

		// Amenities
		// Disable some aspects of the horizontal scrollview so it looks pretty
		HorizontalScrollView amenitiesScrollView = (HorizontalScrollView) findViewById(R.id.amenities_scroll_view);
		amenitiesScrollView.setHorizontalScrollBarEnabled(false);

		// Have to disable overscroll mode via reflection, since it's only in API 9+
		try {
			Field f = HorizontalScrollView.class.getField("OVER_SCROLL_NEVER");
			Method m = HorizontalScrollView.class.getMethod("setOverScrollMode", int.class);
			m.invoke(amenitiesScrollView, f.getInt(null));
		}
		catch (NoSuchFieldError e) {
			// Ignore; this will just happen pre-9
		}
		catch (NoSuchMethodException e) {
			// Ignore; this will just happen pre-9
		}
		catch (Exception e) {
			Log.w("Something went wrong trying to disable overscroll mode.", e);
		}

		ViewGroup amenitiesContainer = (ViewGroup) findViewById(R.id.amenities_table_row);

		// We have to do these manually as multiple amenities can lead to the same icon, also for proper ordering
		if (property.hasAmenity(Amenity.POOL) || property.hasAmenity(Amenity.POOL_INDOOR)
				|| property.hasAmenity(Amenity.POOL_OUTDOOR)) {
			addAmenity(amenitiesContainer, Amenity.POOL, R.drawable.ic_amenity_pool);
		}
		if (property.hasAmenity(Amenity.INTERNET)) {
			addAmenity(amenitiesContainer, Amenity.INTERNET, R.drawable.ic_amenity_internet);
		}
		if (property.hasAmenity(Amenity.BREAKFAST)) {
			addAmenity(amenitiesContainer, Amenity.BREAKFAST, R.drawable.ic_amenity_breakfast);
		}
		if (property.hasAmenity(Amenity.PARKING) || property.hasAmenity(Amenity.EXTENDED_PARKING)
				|| property.hasAmenity(Amenity.FREE_PARKING)) {
			addAmenity(amenitiesContainer, Amenity.PARKING, R.drawable.ic_amenity_parking);
		}
		if (property.hasAmenity(Amenity.PETS_ALLOWED)) {
			addAmenity(amenitiesContainer, Amenity.PETS_ALLOWED, R.drawable.ic_amenity_pets);
		}
		if (property.hasAmenity(Amenity.RESTAURANT)) {
			addAmenity(amenitiesContainer, Amenity.RESTAURANT, R.drawable.ic_amenity_restaurant);
		}
		if (property.hasAmenity(Amenity.FITNESS_CENTER)) {
			addAmenity(amenitiesContainer, Amenity.FITNESS_CENTER, R.drawable.ic_amenity_fitness_center);
		}
		if (property.hasAmenity(Amenity.ROOM_SERVICE)) {
			addAmenity(amenitiesContainer, Amenity.ROOM_SERVICE, R.drawable.ic_amenity_room_service);
		}
		if (property.hasAmenity(Amenity.SPA)) {
			addAmenity(amenitiesContainer, Amenity.SPA, R.drawable.ic_amenity_spa);
		}
		if (property.hasAmenity(Amenity.BUSINESS_CENTER)) {
			addAmenity(amenitiesContainer, Amenity.BUSINESS_CENTER, R.drawable.ic_amenity_business);
		}
		if (property.hasAmenity(Amenity.FREE_AIRPORT_SHUTTLE)) {
			addAmenity(amenitiesContainer, Amenity.FREE_AIRPORT_SHUTTLE, R.drawable.ic_amenity_airport_shuttle);
		}
		if (property.hasAmenity(Amenity.ACCESSIBLE_BATHROOM)) {
			addAmenity(amenitiesContainer, Amenity.ACCESSIBLE_BATHROOM, R.drawable.ic_amenity_accessible_bathroom);
		}
		if (property.hasAmenity(Amenity.HOT_TUB)) {
			addAmenity(amenitiesContainer, Amenity.HOT_TUB, R.drawable.ic_amenity_hot_tub);
		}
		if (property.hasAmenity(Amenity.JACUZZI)) {
			addAmenity(amenitiesContainer, Amenity.JACUZZI, R.drawable.ic_amenity_jacuzzi);
		}
		if (property.hasAmenity(Amenity.WHIRLPOOL_BATH)) {
			addAmenity(amenitiesContainer, Amenity.WHIRLPOOL_BATH, R.drawable.ic_amenity_whirl_pool);
		}
		if (property.hasAmenity(Amenity.KITCHEN)) {
			addAmenity(amenitiesContainer, Amenity.KITCHEN, R.drawable.ic_amenity_kitchen);
		}
		if (property.hasAmenity(Amenity.KIDS_ACTIVITIES)) {
			addAmenity(amenitiesContainer, Amenity.KIDS_ACTIVITIES, R.drawable.ic_amenity_children_activities);
		}
		if (property.hasAmenity(Amenity.BABYSITTING)) {
			addAmenity(amenitiesContainer, Amenity.BABYSITTING, R.drawable.ic_amenity_baby_sitting);
		}
		if (property.hasAmenity(Amenity.ACCESSIBLE_PATHS)) {
			addAmenity(amenitiesContainer, Amenity.ACCESSIBLE_PATHS, R.drawable.ic_amenity_accessible_ramp);
		}
		if (property.hasAmenity(Amenity.ROLL_IN_SHOWER)) {
			addAmenity(amenitiesContainer, Amenity.ROLL_IN_SHOWER, R.drawable.ic_amenity_accessible_shower);
		}
		if (property.hasAmenity(Amenity.HANDICAPPED_PARKING)) {
			addAmenity(amenitiesContainer, Amenity.HANDICAPPED_PARKING, R.drawable.ic_amenity_handicap_parking);
		}
		if (property.hasAmenity(Amenity.IN_ROOM_ACCESSIBILITY)) {
			addAmenity(amenitiesContainer, Amenity.IN_ROOM_ACCESSIBILITY, R.drawable.ic_amenity_accessible_room);
		}
		if (property.hasAmenity(Amenity.DEAF_ACCESSIBILITY_EQUIPMENT)) {
			addAmenity(amenitiesContainer, Amenity.DEAF_ACCESSIBILITY_EQUIPMENT, R.drawable.ic_amenity_deaf_access);
		}
		if (property.hasAmenity(Amenity.BRAILLE_SIGNAGE)) {
			addAmenity(amenitiesContainer, Amenity.BRAILLE_SIGNAGE, R.drawable.ic_amenity_braille_signs);
		}

		// Description
		String description = property.getDescriptionText();
		if (description != null && description.length() > 0) {
			TextView descriptionView = (TextView) findViewById(R.id.description_text_view);
			descriptionView.setText(formatDescription(description));
		}

		// Tracking
		if (savedInstanceState == null) {
			onPageLoad(intent);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Instance instance = new Instance();
		instance.mGalleryFlipping = mGallery.isFlipping();
		return instance;
	}

	private static class Instance {
		private boolean mGalleryFlipping;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isFinishing()) {
			// In order to avoid memory issues, clear the cache of images we might've loaded in this activity
			Log.d("Clearing out images from property.");

			for (Media image : mProperty.getMediaList()) {
				String imageUrl = image.getUrl();
				ImageCache.removeImage(imageUrl, true);
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		mWasStopped = true;
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (mWasStopped) {
			onPageLoad(getIntent());
			mWasStopped = false;
		}
	}

	public void addAmenity(ViewGroup amenitiesTable, Amenity amenity, int iconResourceId) {
		View amenityLayout = getLayoutInflater().inflate(R.layout.snippet_amenity, amenitiesTable, false);

		ImageView amenityIcon = (ImageView) amenityLayout.findViewById(R.id.icon_text_view);
		amenityIcon.setImageResource(iconResourceId);

		TextView amenityName = (TextView) amenityLayout.findViewById(R.id.name_text_view);
		String amenityStr = getString(amenity.getStrId());
		if (amenityStr.contains(" ")) {
			amenityName.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					getResources().getDimension(R.dimen.amenity_text_size_small));
		}
		amenityName.setText(amenity.getStrId());

		amenitiesTable.addView(amenityLayout);
	}

	public void startRoomRatesActivity(Intent intent) {
		Intent roomsRatesIntent = new Intent(this, RoomsAndRatesListActivity.class);
		roomsRatesIntent.fillIn(intent, 0);
		startActivity(roomsRatesIntent);
	}

	private CharSequence formatDescription(String description) {
		StringBuilder html = new StringBuilder();

		// List support
		description = description.replace("<ul>", "\n\n");
		description = description.replace("</ul>", "\n");
		description = description.replace("<li>", getString(R.string.bullet_point) + " ");
		description = description.replace("</li>", "\n");

		// Try to add the address as the third section
		int addressSection = 2;

		int len = description.length();
		int index = 0;
		String title = null;
		while (index < len && index >= 0) {
			int nextSection = description.indexOf("<p>", index);
			int endSection = description.indexOf("</p>", nextSection);

			if (nextSection != -1 && endSection > nextSection) {
				int nextTitle = description.indexOf("<b>", index);
				int endTitle = description.indexOf("</b>", nextTitle);

				if (nextTitle != -1 && endTitle > nextTitle && endTitle < endSection) {
					title = description.substring(nextTitle + 3, endTitle);

					String body = Html.fromHtml(description.substring(endTitle + 4, endSection)).toString().trim();

					if (title.length() > 0 && body.length() > 0) {
						addSection(html, title, body);
						title = null;
					}
				}
				else {
					String body = description.substring(nextSection + 3, endSection).trim().replace("\n", "<br />");
					if (title != null && body.length() > 0) {
						addSection(html, title, body);
						title = null;
					}
					else {
						html.append("<p>" + body + "</p>");
					}
				}

				// Iterate
				index = endSection + 4;

				// Check if we should add address here or not
				addressSection--;
				if (addressSection == 0) {
					addAddressSection(html);
				}
			}
			else {
				// If there's something mysteriously at the end we can't parse, just append it
				html.append(description.substring(index));
				break;
			}
		}

		// If we didn't have enough sections before this, add the address now
		if (addressSection > 0) {
			addAddressSection(html);
		}

		return Html.fromHtml(html.toString().trim());
	}

	private void addSection(StringBuilder html, String title, String body) {
		html.append("<p><b>");
		html.append(title);
		html.append("</b><br />");
		html.append(body);
		html.append("</p>");
	}

	private void addAddressSection(StringBuilder html) {
		Location location = mProperty.getLocation();
		if (location != null) {
			int flags = StrUtils.F_STREET_ADDRESS + StrUtils.F_CITY + StrUtils.F_STATE_CODE + StrUtils.F_POSTAL_CODE;

			String countryCode = location.getCountryCode();
			if (countryCode != null && !countryCode.equals("US")) {
				flags += StrUtils.F_COUNTRY_CODE;
			}
			String address = mProperty.getName() + "\n" + StrUtils.formatAddress(location, flags);

			addSection(html, getString(R.string.address), address.replace("\n", "<br />"));
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Omniture tracking

	public void onPageLoad(Intent intent) {
		Log.d("Tracking \"App.Hotels.Infosite\" pageLoad");

		AppMeasurement s = new AppMeasurement(getApplication());

		TrackingUtils.addStandardFields(this, s);

		s.pageName = "App.Hotels.Infosite";

		s.events = "event32";

		// Shopper/Confirmer
		s.eVar25 = s.prop25 = "Shopper";

		// Rating or highly rated
		TrackingUtils.addHotelRating(s, mProperty);

		// Products
		TrackingUtils.addProducts(s, mProperty);

		// Position, if opened from list
		int position = intent.getIntExtra(EXTRA_POSITION, -1);
		if (position != -1) {
			s.eVar39 = position + "";
		}

		// Send the tracking data
		s.track();
	}
}
