package com.expedia.bookings.activity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.HotelDescription;
import com.expedia.bookings.data.HotelDescription.DescriptionSection;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.AdapterView;
import com.expedia.bookings.widget.AdapterView.OnItemSelectedListener;
import com.expedia.bookings.widget.Gallery;
import com.expedia.bookings.widget.Gallery.OnScrollListener;
import com.mobiata.android.ImageCache.OnImageLoaded;
import com.mobiata.android.Log;
import com.mobiata.android.app.AsyncLoadActivity;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.omniture.AppMeasurement;

public class HotelActivity extends AsyncLoadActivity {

	// This is the position in the list that the hotel had when the user clicked on it 
	public static final String EXTRA_POSITION = "EXTRA_POSITION";

	private static final String INSTANCE_GALLERY_FLIPPING = "INSTANCE_GALLERY_FLIPPING";
	private static final String INSTANCE_GALLERY_POSITION = "INSTANCE_GALLERY_POSITION";
	private static final String INSTANCE_IS_PROPERTY_AMENITIES_EXPANDED = "INSTANCE_IS_PROPERTY_AMENITIES_EXPANDED";

	private static final float MAX_AMENITY_TEXT_WIDTH_IN_DP = 60.0f;

	private static final int MAX_IMAGES_LOADED = 10;

	private static final int BODY_LENGTH_CUTOFF = 400;
	private static final int BODY_LENGTH_TRUNCATE = 300;

	private Context mContext;
	private ExpediaBookingApp mApp;

	private ScrollView mScrollView;

	private Gallery mGallery;
	private ViewGroup mDescriptionContainer;
	private ProgressBar mProgressBar;

	private SearchParams mSearchParams;
	private Property mProperty;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;

	private boolean mGalleryFlipping = true;
	private int mGalleryPosition = 0;
	private boolean mIsAmenitiesExpanded;

	private HotelDescription mDescription;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupHotelActivity(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_IS_PROPERTY_AMENITIES_EXPANDED)) {
			mIsAmenitiesExpanded = savedInstanceState.getBoolean(INSTANCE_IS_PROPERTY_AMENITIES_EXPANDED);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		setupHotelActivity(null);
	}

	private void setupHotelActivity(Bundle savedInstanceState) {
		mContext = this;
		mApp = (ExpediaBookingApp) getApplicationContext();
		final Intent intent = getIntent();

		if (savedInstanceState != null) {
			mGalleryFlipping = savedInstanceState.getBoolean(INSTANCE_GALLERY_FLIPPING, true);
			mGalleryPosition = savedInstanceState.getInt(INSTANCE_GALLERY_POSITION, 0);
		}

		setContentView(R.layout.activity_hotel);

		// Retrieve data to build this with
		mSearchParams = (SearchParams) JSONUtils.parseJSONableFromIntent(intent, Codes.SEARCH_PARAMS,
				SearchParams.class);
		Property property = mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY,
				Property.class);

		HotelDescription.SectionStrings.initSectionStrings(this);
		mDescription = new HotelDescription(this);

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
		mGallery = (Gallery) findViewById(R.id.images_gallery);
		mGallery.setVisibility(View.GONE);
		mScrollView = (ScrollView) findViewById(R.id.scroll_view);

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
		if (lowestRate.isOnSale()) {
			Money baseRate = lowestRate.getDisplayBaseRate();
			fromView.setText(Html.fromHtml(getString(R.string.from_template, StrUtils.formatHotelPrice(baseRate)),
					null, new StrikethroughTagHandler()));
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

		// Description
		mDescriptionContainer = (ViewGroup) findViewById(R.id.description_container);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

		// Tracking
		if (savedInstanceState == null) {
			onPageLoad();

			// Track here if user opened app from widget.  Currently assumes that all widget searches
			// are "nearby" - if this ever changes, this needs to be updated.
			if (intent.getBooleanExtra(Codes.OPENED_FROM_WIDGET, false)) {
				TrackingUtils.trackSimpleEvent(this, null, null, null, "App.Widget.Deal.Nearby");
				mApp.broadcastSearchParamsChangedInWidget((SearchParams) JSONUtils.parseJSONableFromIntent(intent,
						Codes.SEARCH_PARAMS, SearchParams.class));
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(INSTANCE_GALLERY_FLIPPING, mGallery.isFlipping());
		outState.putInt(INSTANCE_GALLERY_POSITION, mGallery.getSelectedItemPosition());
		outState.putBoolean(INSTANCE_IS_PROPERTY_AMENITIES_EXPANDED, mIsAmenitiesExpanded);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isFinishing() && mProperty.getMediaCount() > 0) {
			// In order to avoid memory issues, clear the cache of images we might've loaded in this activity
			Log.d("Clearing out images from property.");

			for (Media image : mProperty.getMediaList()) {
				image.removeFromImageCache();
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

	public void startRoomRatesActivity() {
		Intent roomsRatesIntent = new Intent(this, RoomsAndRatesListActivity.class);
		roomsRatesIntent.fillIn(getIntent(), 0);
		startActivity(roomsRatesIntent);
	}

	private void layoutDescription(ViewGroup descriptionContainer, String description) {
		mDescription.parseDescription(description);

		descriptionContainer.removeAllViews();

		// Try to add the address as the third section
		int addressSection = 2;

		if (mDescription.getSections().size() == 0) {
			// Just display the description we were given
			addSection("", description, descriptionContainer);
		}
		else {
			for (DescriptionSection section : mDescription.getSections()) {
				addSection(section.title, section.description, descriptionContainer);
				// Check if we should add address here or not
				addressSection--;
				if (addressSection == 0) {
					addAddressSection(descriptionContainer);
				}
			}
		}

		// If we didn't have enough sections before this, add the address now
		if (addressSection > 0) {
			addAddressSection(descriptionContainer);
		}
	}

	private View addSection(String title, final String body, ViewGroup detailsContainer) {
		RelativeLayout detailsSection = (RelativeLayout) getLayoutInflater().inflate(
				R.layout.snippet_hotel_description_section, null);

		TextView titleTextView = (TextView) detailsSection.findViewById(R.id.title_description_text_view);

		if (title != null && title.length() > 0) {
			titleTextView.setText(Html.fromHtml(title));
		}
		else if (title.length() > 0) {
			title = "";
			titleTextView.setVisibility(View.GONE);
		}

		TextView bodyTextView = (TextView) detailsSection.findViewById(R.id.body_description_text_view);

		// TODO: Is this ever called? No description seems to have "Features"
		if (title.contains("Features")) {
			addHotelRating(detailsSection);
		}
		//else if (title.contains(getString(R.string.section_property_amenities))) {
		else if (HotelDescription.SectionStrings.isValidPropertyAmenitiesString(title)) {
			if (mIsAmenitiesExpanded == false && body.length() > BODY_LENGTH_CUTOFF) {
				bodyTextView.setText(Html.fromHtml(body.substring(0, BODY_LENGTH_TRUNCATE)) + "...");

				TextView expanderTextView = (TextView) getLayoutInflater().inflate(R.layout.include_read_more_button,
						null);
				expanderTextView.setVisibility(View.VISIBLE);
				expanderTextView.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						v.setVisibility(View.GONE);
						RelativeLayout p = (RelativeLayout) v.getParent();
						TextView bodyTextView = (TextView) p.findViewById(R.id.body_description_text_view);
						bodyTextView.setText(Html.fromHtml(body));
						mIsAmenitiesExpanded = true;
					}
				});

				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);
				lp.addRule(RelativeLayout.BELOW, R.id.body_description_text_view);
				lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
				detailsSection.addView(expanderTextView, lp);
			}
			else {
				bodyTextView.setText(Html.fromHtml(body));
			}
		}
		else {
			bodyTextView.setText(Html.fromHtml(body));
		}

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
	// Async loading of hoteloffers

	@Override
	public String getUniqueKey() {
		return "com.expedia.booking.details.offer";
	}

	@Override
	public void showProgress() {
		mProgressBar.setVisibility(View.VISIBLE);
	}

	@Override
	public Object downloadImpl() {
		ExpediaServices services = new ExpediaServices(this);
		return services.availability(mSearchParams, mProperty, 0);
	}

	@Override
	public void onResults(Object results) {
		mProgressBar.setVisibility(View.GONE);

		AvailabilityResponse response = (AvailabilityResponse) results;
		String description;
		if (response == null) {
			// Use short description (if available)
			description = mProperty.getDescriptionText();
		}
		else if (response.hasErrors()) {
			// TODO: At a later junction, remove the error display and
			// just show the short description.
			description = response.getErrors().get(0).getPresentableMessage(this);
		}
		else {
			Property property = response.getProperty();

			mProperty.setMediaList(property.getMediaList());

			description = property.getDescriptionText();

			setupGallery(property);
			setupAmenities(property);
		}

		if (description != null && description.length() > 0) {
			layoutDescription(mDescriptionContainer, description);
		}
	}

	private void setupGallery(Property property) {
		if (property.getMediaCount() > 0) {
			mGallery.setVisibility(View.VISIBLE);
			final List<Media> media = property.getMediaList();

			if (media != null) {
				mGallery.setMedia(media);

				mGallery.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						// Pre-load images around the currently selected image, until we have MAX_IMAGES_LOADED
						// loading.  Then cancel downloads on all the rest.
						int left = position;
						int right = position;
						int loaded = 1;
						int len = media.size();
						OnImageLoaded doNothing = new OnImageLoaded() {
							public void onImageLoaded(String url, Bitmap bitmap) {
								// Do nothing.  In the future, ImageCache should have 
								// the ability to simply preload, but this is a fix 
								// for #8401 for the 1.0.2 release and I don't want to
								// have to update/branch Utils.
							}

							public void onImageLoadFailed(String url) {
								// Do nothing.
							}
						};
						boolean hasMore = true;
						while (loaded < MAX_IMAGES_LOADED && hasMore) {
							hasMore = false;
							if (left > 0) {
								left--;
								media.get(left).loadHighResImage(null, doNothing);
								loaded++;
								hasMore = true;
							}
							if (loaded == MAX_IMAGES_LOADED) {
								break;
							}
							if (right < len - 1) {
								right++;
								media.get(right).loadHighResImage(null, doNothing);
								loaded++;
								hasMore = true;
							}
						}

						// Clear images a few to the right/left of the bounds.
						while (left > 0) {
							left--;
							media.get(left).removeFromImageCache();
						}
						while (right < len - 1) {
							right++;
							media.get(right).removeFromImageCache();
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// Do nothing
					}
				});

				if (mGalleryPosition > 0 && media.size() > mGalleryPosition) {
					mGallery.setSelection(mGalleryPosition);
				}

				if (mGalleryFlipping) {
					mGallery.startFlipping();
				}

				// Set it up so that we scroll to the top whenever user scrolls the gallery
				// ONLY do this is not landscape
				if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
					mGallery.setOnScrollListener(new OnScrollListener() {
						public void onScroll() {
							mScrollView.smoothScrollTo(0, 0);
						}
					});
				}
			}
		}
		else {
			mGallery.setVisibility(View.GONE);
		}
	}

	private void setupAmenities(Property property) {
		ViewGroup amenitiesContainer = (ViewGroup) findViewById(R.id.amenities_table_row);
		amenitiesContainer.removeAllViews();
		LayoutUtils.addAmenities(this, property, amenitiesContainer);

		// Hide the text that indicated no amenities because there are amenities
		if (property.hasAmenities()) {
			findViewById(R.id.amenities_none_text).setVisibility(View.GONE);
		}
		else {
			findViewById(R.id.amenities_none_text).setVisibility(View.VISIBLE);
		}

		findViewById(R.id.amenities_scroll_view).setVisibility(View.VISIBLE);
		findViewById(R.id.amenities_divider).setVisibility(View.VISIBLE);
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

	//////////////////////////////////////////////////////////////////////////////////////////
	// Options menu (just for debug)

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		DebugMenu.onCreateOptionsMenu(this, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		DebugMenu.onPrepareOptionsMenu(this, menu);
		return super.onPrepareOptionsMenu(menu);
	}

}
