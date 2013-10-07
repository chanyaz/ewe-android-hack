package com.expedia.bookings.test.tests.hotels.ui.regression;

import java.util.HashMap;

import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.data.Property.Amenity;
import com.expedia.bookings.test.tests.pageModels.hotels.HotelSearchResultRow;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;

public class HotelDetailsTests extends CustomActivityInstrumentationTestCase<PhoneSearchActivity> {

	private static final String TAG = HotelDetailsTests.class.getSimpleName();

	private static HashMap<String, Integer> mAmenityNameToDrawableID;

	public HotelDetailsTests() {
		super(PhoneSearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	// Hopefully, someday, the drawable resource ids will be directly tied
	// to the Amenity types. For now, we do this ourselves.
	private void setUpAmenityHashMap() {
		mAmenityNameToDrawableID = new HashMap<String, Integer>();
		mAmenityNameToDrawableID.put(getString(Amenity.ACCESSIBLE_BATHROOM.getStrId()),
				R.drawable.ic_amenity_accessible_bathroom);
		mAmenityNameToDrawableID.put(getString(Amenity.ACCESSIBLE_PATHS.getStrId()),
				R.drawable.ic_amenity_accessible_ramp);
		mAmenityNameToDrawableID.put(getString(Amenity.BABYSITTING.getStrId()), R.drawable.ic_amenity_baby_sitting);
		mAmenityNameToDrawableID
				.put(getString(Amenity.BRAILLE_SIGNAGE.getStrId()), R.drawable.ic_amenity_braille_signs);
		mAmenityNameToDrawableID.put(getString(Amenity.BREAKFAST.getStrId()), R.drawable.ic_amenity_breakfast);
		mAmenityNameToDrawableID.put(getString(Amenity.BUSINESS_CENTER.getStrId()), R.drawable.ic_amenity_business);
		mAmenityNameToDrawableID.put(getString(Amenity.DEAF_ACCESSIBILITY_EQUIPMENT.getStrId()),
				R.drawable.ic_amenity_deaf_access);
		mAmenityNameToDrawableID.put(getString(Amenity.EXTENDED_PARKING.getStrId()), R.drawable.ic_amenity_parking);
		mAmenityNameToDrawableID
				.put(getString(Amenity.FITNESS_CENTER.getStrId()), R.drawable.ic_amenity_fitness_center);
		mAmenityNameToDrawableID.put(getString(Amenity.FREE_AIRPORT_SHUTTLE.getStrId()),
				R.drawable.ic_amenity_airport_shuttle);
		mAmenityNameToDrawableID.put(getString(Amenity.FREE_PARKING.getStrId()), R.drawable.ic_amenity_parking);
		mAmenityNameToDrawableID.put(getString(Amenity.HANDICAPPED_PARKING.getStrId()),
				R.drawable.ic_amenity_handicap_parking);
		mAmenityNameToDrawableID.put(getString(Amenity.HOT_TUB.getStrId()), R.drawable.ic_amenity_hot_tub);
		mAmenityNameToDrawableID.put(getString(Amenity.IN_ROOM_ACCESSIBILITY.getStrId()),
				R.drawable.ic_amenity_accessible_room);
		mAmenityNameToDrawableID.put(getString(Amenity.INTERNET.getStrId()), R.drawable.ic_amenity_internet);
		mAmenityNameToDrawableID.put(getString(Amenity.JACUZZI.getStrId()), R.drawable.ic_amenity_jacuzzi);
		mAmenityNameToDrawableID.put(getString(Amenity.KIDS_ACTIVITIES.getStrId()),
				R.drawable.ic_amenity_children_activities);
		mAmenityNameToDrawableID.put(getString(Amenity.KITCHEN.getStrId()), R.drawable.ic_amenity_kitchen);
		mAmenityNameToDrawableID.put(getString(Amenity.PARKING.getStrId()), R.drawable.ic_amenity_parking);
		mAmenityNameToDrawableID.put(getString(Amenity.PETS_ALLOWED.getStrId()), R.drawable.ic_amenity_pets);
		mAmenityNameToDrawableID.put(getString(Amenity.POOL.getStrId()), R.drawable.ic_amenity_pool);
		mAmenityNameToDrawableID.put(getString(Amenity.POOL_INDOOR.getStrId()), R.drawable.ic_amenity_pool);
		mAmenityNameToDrawableID.put(getString(Amenity.POOL_OUTDOOR.getStrId()), R.drawable.ic_amenity_pool);
		mAmenityNameToDrawableID.put(getString(Amenity.RESTAURANT.getStrId()), R.drawable.ic_amenity_restaurant);
		mAmenityNameToDrawableID.put(getString(Amenity.ROLL_IN_SHOWER.getStrId()),
				R.drawable.ic_amenity_accessible_shower);
		mAmenityNameToDrawableID.put(getString(Amenity.ROOM_SERVICE.getStrId()), R.drawable.ic_amenity_room_service);
		mAmenityNameToDrawableID.put(getString(Amenity.SPA.getStrId()), R.drawable.ic_amenity_spa);
		mAmenityNameToDrawableID.put(getString(Amenity.WHIRLPOOL_BATH.getStrId()), R.drawable.ic_amenity_whirl_pool);
	}

	// Test that amenity drawables match their text labels
	public void testAmenities() throws Exception {
		setUpAmenityHashMap();
		mUser.setHotelCityToRandomUSCity();
		mDriver.enterLog(TAG, "Testing amenities in city: " + mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		int hotelsDisplayed = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount() - 1;
		for (int i = 0; i < hotelsDisplayed; i++) {
			mDriver.hotelsSearchScreen().selectHotelFromList(i);
			mDriver.delay();
			String hotelName = mDriver.hotelsDetailsScreen().titleView().getText().toString();
			mDriver.enterLog(TAG, "Verifying VIP Dialog for hotel: " + hotelName);
			mDriver.scrollDown();
			mDriver.delay();
			ViewGroup amenitiesContainer = mDriver.hotelsDetailsScreen().amenitiesContainer();
			if (amenitiesContainer.isShown()) {
				int childCount = amenitiesContainer.getChildCount();
				for (int j = 0; j < childCount; j++) {
					TextView amenityView = (TextView) amenitiesContainer.getChildAt(j);
					String amenityString = amenityView.getText().toString();
					if (!amenityView.getText().toString().isEmpty()) {
						BitmapDrawable amenityDrawable = (BitmapDrawable) amenityView.getCompoundDrawables()[1];
						BitmapDrawable expectedBitmapDrawable = (BitmapDrawable) mRes
								.getDrawable(mAmenityNameToDrawableID
										.get(amenityString));
						expectedBitmapDrawable.getBitmap();
						amenityDrawable.getBitmap();
						assertEquals(expectedBitmapDrawable.getBitmap(), amenityDrawable.getBitmap());
					}
				}
			}
			mDriver.goBack();
		}
	}

	// Verify that the correct dialog appears after clicking the VIP Access image in 
	// on the image gallery
	public void testVIPAccessDialog() throws Exception {
		mUser.setHotelCityToRandomUSCity();
		mDriver.enterLog(TAG, "Testing VIP Access Dialog for hotels in city: " + mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());

		mDriver.hotelsSearchScreen().clickOnFilterButton();
		mDriver.hotelsSearchScreen().filterMenu().clickVIPAccessFilterButton();
		mDriver.goBack();
		int hotelsDisplayed = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount() - 1;
		for (int i = 0; i < hotelsDisplayed; i++) {
			mDriver.hotelsSearchScreen().selectHotelFromList(i);
			mDriver.delay();
			String hotelName = mDriver.hotelsDetailsScreen().titleView().getText().toString();
			mDriver.enterLog(TAG, "Verifying VIP Dialog for hotel: " + hotelName);
			mDriver.hotelsDetailsScreen().clickVIPImageView();
			mDriver.delay(1);
			assertTrue(mDriver.searchText(mDriver.hotelsDetailsScreen().vipAccessMessage()));
			mDriver.hotelsDetailsScreen().clickOnButton(0);
			mDriver.goBack();
		}
	}

	// Verify that some UI Elements are present on the hotel details screen
	public void testDetailsUIElements() throws Exception {
		mUser.setHotelCityToRandomUSCity();
		mDriver.enterLog(TAG, "Search city is: " + mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());

		int hotelsDisplayed = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount() - 1;
		for (int i = 0; i < hotelsDisplayed; i++) {
			HotelSearchResultRow row = mDriver.hotelsSearchScreen().getSearchResultRowModelFromIndex(i);
			String rowHotelName = row.getNameTextView().getText().toString();
			mDriver.hotelsSearchScreen().selectHotelFromList(i);
			mDriver.enterLog(TAG, "Verifying UI elements for details of: " + rowHotelName);
			mDriver.delay();
			if (!rowHotelName.isEmpty() && !rowHotelName.contains("...")) {
				String detailHotelsName = mDriver.hotelsDetailsScreen().titleView().getText().toString();
				mDriver.enterLog(TAG, "Testing that the hotel name: " + rowHotelName + " matches " + detailHotelsName);
				assertEquals(rowHotelName, detailHotelsName);
			}
			assertTrue(mDriver.hotelsDetailsScreen().ratingBar().isShown());
			assertTrue(mDriver.hotelsDetailsScreen().hotelGallery().isShown());
			mDriver.scrollToBottom();
			mDriver.delay();
			mDriver.scrollDown();
			assertTrue(mDriver.hotelsDetailsScreen().bookNowButton().isShown());
			mDriver.goBack();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}

}
