package com.expedia.bookings.activity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Property.Amenity;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.AdapterView;
import com.expedia.bookings.widget.AdapterView.OnItemSelectedListener;
import com.expedia.bookings.widget.Gallery;
import com.expedia.bookings.widget.Gallery.OnScrollListener;
import com.mobiata.android.ImageCache;
import com.mobiata.android.ImageCache.OnImageLoaded;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.omniture.AppMeasurement;

public class HotelActivity extends Activity {

	// This is the position in the list that the hotel had when the user clicked on it 
	public static final String EXTRA_POSITION = "EXTRA_POSITION";

	private static final float MAX_AMENITY_TEXT_WIDTH_IN_DP = 60.0f;

	private static final int MAX_IMAGES_LOADED = 10;

	private Context mContext;

	private ScrollView mScrollView;

	private Gallery mGallery;

	private Property mProperty;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupHotelActivity(savedInstanceState);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		setupHotelActivity(null);
	}

	private void setupHotelActivity(Bundle savedInstanceState) {
		mContext = this;
		final Intent intent = getIntent();

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
		OnClickListener onBookNowClick = new OnClickListener() {
			public void onClick(View v) {
				startRoomRatesActivity();
			}
		};

		OnClickListener onReviewsClick = (!property.hasExpediaReviews()) ? null : new OnClickListener() {
			public void onClick(View v) {
				Intent newIntent = new Intent(mContext, UserReviewsListActivity.class);
				newIntent.fillIn(intent, 0);
				startActivity(newIntent);
			}
		};
		LayoutUtils.configureHeader(this, property, onBookNowClick, onReviewsClick);

		// Configure the gallery
		Gallery gallery = mGallery = (Gallery) findViewById(R.id.images_gallery);
		mScrollView = (ScrollView) findViewById(R.id.scroll_view);
		if (property.getMediaCount() > 0) {
			final List<String> urls = new ArrayList<String>(property.getMediaCount());
			Set<String> usedUrls = new HashSet<String>();
			for (Media media : property.getMediaList()) {
				String url = media.getUrl();
				if (!usedUrls.contains(url)) {
					urls.add(url);
					usedUrls.add(url);
				}
			}
			gallery.setUrls(urls);

			gallery.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					// Pre-load images around the currently selected image, until we have MAX_IMAGES_LOADED
					// loading.  Then cancel downloads on all the rest.
					int left = position;
					int right = position;
					int loaded = 1;
					int len = urls.size();
					OnImageLoaded doNothing = new OnImageLoaded() {
						public void onImageLoaded(String url, Bitmap bitmap) {
							// Do nothing.  In the future, ImageCache should have 
							// the ability to simply preload, but this is a fix 
							// for #8401 for the 1.0.2 release and I don't want to
							// have to update/branch Utils.
						}
					};
					boolean hasMore = true;
					while (loaded < MAX_IMAGES_LOADED && hasMore) {
						hasMore = false;
						if (left > 0) {
							left--;
							ImageCache.loadImage(urls.get(left), doNothing);
							loaded++;
							hasMore = true;
						}
						if (loaded == MAX_IMAGES_LOADED) {
							break;
						}
						if (right < len - 1) {
							right++;
							ImageCache.loadImage(urls.get(right), doNothing);
							loaded++;
							hasMore = true;
						}
					}

					// Clear images a few to the right/left of the bounds.
					while (left > 0) {
						left--;
						ImageCache.removeImage(urls.get(left), true);
					}
					while (right < len - 1) {
						right++;
						ImageCache.removeImage(urls.get(right), true);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// Do nothing
				}
			});

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
			promoView.setOnClickListener(onBookNowClick);
			promoView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.expand_promo));
		}

		ViewGroup priceContainer = (ViewGroup) findViewById(R.id.price_layout);
		priceContainer.setOnClickListener(onBookNowClick);
		priceContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_left));
		TextView fromView = (TextView) findViewById(R.id.from_text_view);
		if (lowestRate.getSavingsPercent() > 0) {
			Money baseRate = lowestRate.getDisplayBaseRate();
			fromView.setText(Html.fromHtml(
					getString(R.string.from_template, StrUtils.formatHotelPrice(baseRate)), null,
					new StrikethroughTagHandler()));
		}
		else {
			fromView.setText(R.string.from);
		}

		TextView priceView = (TextView) findViewById(R.id.price_text_view);
		priceView.setText(StrUtils.formatHotelPrice(lowestRate.getDisplayRate()));

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
			ViewGroup descriptionContainer = (ViewGroup) findViewById(R.id.description_container);
			layoutDescription(descriptionContainer, description);
		}

		// Tracking
		if (savedInstanceState == null) {
			onPageLoad();

			// Track here if user opened app from widget.  Currently assumes that all widget searches
			// are "last app search" - if this ever changes, this needs to be updated.
			if (intent.getBooleanExtra(Codes.OPENED_FROM_WIDGET, false)) {
				TrackingUtils.trackSimpleEvent(this, null, null, null, "App.Widget.Deal.AppLastSearch");
			}
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

		if (isFinishing() && mProperty.getMediaCount() > 0) {
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
			onPageLoad();
			mWasStopped = false;
		}
	}

	public void addAmenity(ViewGroup amenitiesTable, Amenity amenity, int iconResourceId) {
		View amenityLayout = getLayoutInflater().inflate(R.layout.snippet_amenity, amenitiesTable, false);

		ImageView amenityIcon = (ImageView) amenityLayout.findViewById(R.id.icon_text_view);
		amenityIcon.setImageResource(iconResourceId);

		TextView amenityName = (TextView) amenityLayout.findViewById(R.id.name_text_view);
		String amenityStr = getString(amenity.getStrId());

		// measure the length of the amenity string and determine whether it is short enough
		// to fit within the acceptable width. If not, reduce the font size in an attempt to 
		// get it to fit.
		float acceptableWidth = getResources().getDisplayMetrics().density * MAX_AMENITY_TEXT_WIDTH_IN_DP;
		float measuredWidthOfStr = amenityName.getPaint().measureText(getString(amenity.getStrId()));

		if (amenityStr.contains(" ") || measuredWidthOfStr > acceptableWidth) {
			amenityName.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					getResources().getDimension(R.dimen.amenity_text_size_small));
		}

		amenityName.setText(amenityStr);
		amenitiesTable.addView(amenityLayout);
	}

	public void startRoomRatesActivity() {
		Intent roomsRatesIntent = new Intent(this, RoomsAndRatesListActivity.class);
		roomsRatesIntent.fillIn(getIntent(), 0);
		startActivity(roomsRatesIntent);
	}

	private void layoutDescription(ViewGroup descriptionContainer, String description) {

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
					title = description.substring(nextTitle + 3, endTitle).trim();
					if (title.endsWith(".")) {
						title = title.substring(0, title.length() - 1);
					}

					String body = Html.fromHtml(description.substring(endTitle + 4, endSection)).toString().trim();

					if (title.length() > 0 && body.length() > 0) {
						addSection(title, body, descriptionContainer);
						title = null;
					}
				}
				else {
					String body = description.substring(nextSection + 3, endSection).trim().replace("\n", "<br />");
					if (title != null && body.length() > 0) {
						addSection(title, body, descriptionContainer);
						title = null;
					}
				}

				// Iterate
				index = endSection + 4;

				// Check if we should add address here or not
				addressSection--;
				if (addressSection == 0) {
					addAddressSection(descriptionContainer);
				}
			}
			else {
				// If there's something mysteriously at the end we can't parse, just append it
				String body = description.substring(index);

				// ensure not to add a string that is blank to the hote desription. This is possible
				// if the end of the description is padded with whitespaces
				if (isBlank(body)) {
					break;
				}

				addSection(null, body, descriptionContainer);
				break;
			}
		}

		// If we didn't have enough sections before this, add the address now
		if (addressSection > 0) {
			addAddressSection(descriptionContainer);
		}

	}

	/*
	 * This method returns true if the string is blank.
	 * Ideally, I'd use the StringUtils.isBlank method but didnt want 
	 * to pull in the commons jar just for this. 
	 */
	private boolean isBlank(String str) {
		for (char a : str.toCharArray()) {
			if (a != ' ' && a != '\n') {
				return false;
			}
		}
		return true;
	}

	private View addSection(String title, String body, ViewGroup detailsContainer) {

		RelativeLayout detailsSection = (RelativeLayout) getLayoutInflater().inflate(
				R.layout.snippet_hotel_description_section, null);

		TextView titleTextView = (TextView) detailsSection.findViewById(R.id.title_description_text_view);
		if (title != null) {
			titleTextView.setText(Html.fromHtml(title.trim()));

			// add the hotel rating to the hotel features section
			if (title.contains("Features")) {
				addHotelRating(detailsSection);
			}

		}
		else {
			titleTextView.setVisibility(View.GONE);
		}

		TextView bodyTextView = (TextView) detailsSection.findViewById(R.id.body_description_text_view);
		bodyTextView.setText(Html.fromHtml(body.trim()));

		detailsContainer.addView(detailsSection);

		return detailsSection;
	}

	private void addHotelRating(RelativeLayout detailsSection) {
		// add the star rating section below the body of this section
		View starRatingContainer = getLayoutInflater().inflate(R.layout.snippet_hotel_star_rating, null);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.BELOW, R.id.body_description_text_view);

		RatingBar hotelStarRating = (RatingBar) starRatingContainer.findViewById(R.id.hotel_rating_bar);
		hotelStarRating.setRating((float) mProperty.getHotelRating());

		detailsSection.addView(starRatingContainer, lp);
	}

	private void addAddressSection(ViewGroup descriptionContainer) {
		Location location = mProperty.getLocation();
		if (location != null) {
			int flags = StrUtils.F_STREET_ADDRESS + StrUtils.F_CITY + StrUtils.F_STATE_CODE + StrUtils.F_POSTAL_CODE;

			String countryCode = location.getCountryCode();
			if (countryCode != null && !countryCode.equals("US")) {
				flags += StrUtils.F_COUNTRY_CODE;
			}
			String address = mProperty.getName() + "\n" + StrUtils.formatAddress(location, flags);
			RelativeLayout addressSection = (RelativeLayout) addSection(getString(R.string.address),
					address.replace("\n", "<br />"), descriptionContainer);

			// add the map button to the right of the address section
			ImageButton mapButton = (ImageButton) getLayoutInflater().inflate(R.layout.snippet_map_button, null);
			mapButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent newIntent = new Intent(mContext, HotelMapActivity.class);
					newIntent.fillIn(getIntent(), 0);
					startActivity(newIntent);
				}
			});

			float dimension = getResources().getDimension(R.dimen.action_bar_button_height);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) dimension, (int) dimension);
			lp.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.body_description_text_view);
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

			View body = addressSection.findViewById(R.id.body_description_text_view);
			((RelativeLayout.LayoutParams) body.getLayoutParams()).addRule(RelativeLayout.ALIGN_LEFT, R.id.view_button);
			addressSection.addView(mapButton, lp);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Omniture tracking

	public void onPageLoad() {
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
		int position = getIntent().getIntExtra(EXTRA_POSITION, -1);
		if (position != -1) {
			s.eVar39 = position + "";
		}

		// Send the tracking data
		s.track();
	}

}
