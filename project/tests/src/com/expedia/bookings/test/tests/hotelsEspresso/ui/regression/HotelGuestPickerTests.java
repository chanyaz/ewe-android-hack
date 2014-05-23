package com.expedia.bookings.test.tests.hotelsEspresso.ui.regression;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsSearchScreen;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.mobiata.android.util.SettingUtils;

import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 5/22/14.
 */
public class HotelGuestPickerTests extends ActivityInstrumentationTestCase2<SearchActivity> {
	public HotelGuestPickerTests() {
		super(SearchActivity.class);
	}

	private static final String TAG = HotelGuestPickerTests.class.getSimpleName();

	Context mContext;
	SharedPreferences mPrefs;
	Resources mRes;
	String mValue;

	protected void setUp() throws Exception {
		super.setUp();
		mContext = getInstrumentation().getTargetContext();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mRes = mContext.getResources();
		mValue = "value";
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Integration");
		getActivity();
	}

	// verify that the guest number picker's text views
	// show the expected text when children and adults
	// are incremented and decremented
	public void testPickerTextViews() {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickOnGuestsButton();

		HotelsGuestPicker.getGuestTextViewValue(R.id.text_lower, R.id.children_number_picker, mValue);
		String lowerTextChildView = mPrefs.getString(mValue, "");
		HotelsGuestPicker.getGuestTextViewValue(R.id.text_lower, R.id.adults_number_picker, mValue);
		String lowerTextAdultView = mPrefs.getString(mValue, "");
		assertEquals(lowerTextChildView, "");
		assertEquals(lowerTextAdultView, "");

		int adultCount = 1;
		int childCount = 0;
		final int adultMax = 6;
		final int childMax = 4;

		String adultCurrentTextViewValue;
		String adultHigherTextViewValue;
		String adultLowerTextViewValue;

		for (int i = 1; i < adultMax; i++) {
			HotelsGuestPicker.getGuestTextViewValue(R.id.text_current, R.id.adults_number_picker, mValue);
			adultCurrentTextViewValue = mPrefs.getString(mValue, "");
			assertEquals(HotelsGuestPicker.adultPickerStringPlural(adultCount, mRes), adultCurrentTextViewValue);

			HotelsGuestPicker.getGuestTextViewValue(R.id.text_higher, R.id.adults_number_picker, mValue);
			adultHigherTextViewValue = mPrefs.getString(mValue, "");
			assertEquals(HotelsGuestPicker.adultPickerStringPlural(adultCount + 1, mRes), adultHigherTextViewValue);

			HotelsGuestPicker.incrementAdultsButton();
			adultCount++;

			HotelsGuestPicker.getGuestTextViewValue(R.id.text_lower, R.id.adults_number_picker, mValue);
			adultLowerTextViewValue = mPrefs.getString(mValue, "");
			assertEquals(HotelsGuestPicker.adultPickerStringPlural(adultCount - 1, mRes), adultLowerTextViewValue);
		}

		for (int i = 6; i > 0; i--) {
			HotelsGuestPicker.decrementAdultsButton();
		}

		String childCurrentTextViewValue;
		String childHigherTextViewValue;
		String childLowerTextViewValue;

		for (int i = 0; i < childMax; i++) {
			HotelsGuestPicker.getGuestTextViewValue(R.id.text_higher, R.id.children_number_picker, mValue);
			childHigherTextViewValue = mPrefs.getString(mValue, "");
			assertEquals(HotelsGuestPicker.childPickerStringPlural(childCount + 1, mRes), childHigherTextViewValue);

			HotelsGuestPicker.getGuestTextViewValue(R.id.text_current, R.id.children_number_picker, mValue);
			childCurrentTextViewValue = mPrefs.getString(mValue, "");
			assertEquals(HotelsGuestPicker.childPickerStringPlural(childCount, mRes), childCurrentTextViewValue);

			HotelsGuestPicker.incrementChildrenButton();
			childCount++;

			HotelsGuestPicker.getGuestTextViewValue(R.id.text_lower, R.id.children_number_picker, mValue);
			childLowerTextViewValue = mPrefs.getString(mValue, "");
			assertEquals(HotelsGuestPicker.childPickerStringPlural(childCount - 1, mRes), childLowerTextViewValue);
		}
	}

	public void testChildSelectorAppearing() {
		final int childMax = 4;
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickOnGuestsButton();
		assertFalse(withText(HotelsGuestPicker.selectChildAgePlural(1, mRes)).matches(isDisplayed()));

		for (int i = 1; i <= childMax; i++) {
			HotelsGuestPicker.incrementChildrenButton();
			assertFalse(withText(HotelsGuestPicker.selectChildAgePlural(i, mRes)).matches(isDisplayed()));
		}
		Espresso.pressBack();
		Espresso.pressBack();
	}
}
