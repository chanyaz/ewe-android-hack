package com.expedia.bookings.test.tests.hotelsEspresso.ui.regression;

import java.util.Calendar;

import org.joda.time.LocalDate;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CVVEntryScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.mobiata.android.util.SettingUtils;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;

/**
 * Created by dmadan on 5/14/14.
 */
public class HotelDetailsTests extends ActivityInstrumentationTestCase2<PhoneSearchActivity> {
	public HotelDetailsTests() {
		super(PhoneSearchActivity.class);
	}

	private static final String TAG = HotelDetailsTests.class.getName();

	Context mContext;
	SharedPreferences mPrefs;
	Resources mRes;

	protected void setUp() throws Exception {
		super.setUp();
		mContext = getInstrumentation().getTargetContext();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mRes = mContext.getResources();
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Integration");
		getActivity();
	}

	// Verify that the correct dialog appears after clicking the VIP Access image in
	// on the image gallery
	public void testVIPAccessDialog() throws Exception {
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickOnFilterButton();
		HotelsSearchScreen.filterMenu().clickVIPAccessFilterButton();
		Espresso.pressBack();
		EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView(), "totalHotels", 0);
		int totalHotels = mPrefs.getInt("totalHotels", 0);
		for (int i = 1; i < totalHotels - 1; i++) {
			HotelsSearchScreen.clickListItem(i);
			EspressoUtils.getValues("hotelName", R.id.title);
			String hotelName = mPrefs.getString("hotelName", "");
			ScreenActions.enterLog(TAG, "Verifying VIP Dialog for hotel: " + hotelName);
			HotelsDetailsScreen.clickVIPImageView();
			EspressoUtils.assertTrue("At VIP Access hotels, Expedia Elite Plus members receive free room upgrades and other perks upon availability at check-in.");
			CVVEntryScreen.clickOkButton();
			Espresso.pressBack();
		}
	}

	// Verify that some UI Elements are present on the hotel details screen
	public void testDetailsUIElements() throws Exception {
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		Calendar cal = Calendar.getInstance();
		int year = cal.get(cal.YEAR);
		int month = cal.get(cal.MONTH) + 1;
		LocalDate mStartDate = new LocalDate(year, month, 5);
		LocalDate mEndDate = new LocalDate(year, month, 10);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(mStartDate, mEndDate);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView(), "totalHotels", 0);
		int totalHotels = mPrefs.getInt("totalHotels", 0);
		for (int i = 1; i < totalHotels; i++) {
			String value = "value";
			DataInteraction searchResultRow = HotelsSearchScreen.hotelListItem().atPosition(i);
			EspressoUtils.getListItemValues(searchResultRow, R.id.name_text_view, value);
			String rowHotelName = mPrefs.getString(value, "");
			searchResultRow.perform(click());
			try{
				ScreenActions.enterLog(TAG, "Verifying UI elements for details of: " + rowHotelName);
				if (!rowHotelName.isEmpty() && !rowHotelName.contains("...")) {
					EspressoUtils.getValues(value, R.id.title);
					String detailHotelsName = mPrefs.getString(value, "");
					ScreenActions.enterLog(TAG, "Testing that the hotel name: " + rowHotelName + " matches " + detailHotelsName);
					assertEquals(rowHotelName, detailHotelsName);
				}
				HotelsDetailsScreen.ratingBar().check(matches(isDisplayed()));
				HotelsDetailsScreen.hotelGallery().check(matches(isDisplayed()));
				HotelsDetailsScreen.bookNowButton().perform(scrollTo()).check(matches(isDisplayed()));
			}
			catch (Exception e) {
				CVVEntryScreen.clickOkButton();
			}
			Espresso.pressBack();
		}
	}
}
