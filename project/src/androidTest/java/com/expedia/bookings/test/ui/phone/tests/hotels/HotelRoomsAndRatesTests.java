package com.expedia.bookings.test.ui.phone.tests.hotels;

import java.util.concurrent.atomic.AtomicReference;

import org.joda.time.LocalDate;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewAssertion;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.assertion.ViewAssertions.selectedDescendantsMatch;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.ui.espresso.CustomMatchers.isEmpty;
import static com.expedia.bookings.test.ui.espresso.CustomMatchers.withRating;
import static com.expedia.bookings.test.ui.espresso.ViewActions.getString;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

public class HotelRoomsAndRatesTests extends PhoneTestCase {
	private static final String TAG = HotelRoomsAndRatesTests.class.getSimpleName();

	public void testRoomsAndRatesHeaderInfo() throws Exception {
		initiateSearch();

		final int totalHotels = EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView());
		for (int hotelPosition = 1; hotelPosition < totalHotels; hotelPosition++) {
			int hotelSearchPageDiscount = getSearchScreenHotelDiscount(hotelPosition);
			HotelsSearchScreen.clickListItem(hotelPosition);
			if (hotelSearchPageDiscount != -1) {
				int detailsPageDiscount = checkDetailsPageDiscount();
				if (!(hotelSearchPageDiscount == detailsPageDiscount)) {
					throw new Exception("Discount at Hotel-Search-Screen and Hotel-Details-Screen do not match. ");
				}
			}
			String hotelName = EspressoUtils.getText(R.id.title);
			final float detailsHotelRating = EspressoUtils.getRatingValue(HotelsDetailsScreen.ratingBar());

			HotelsDetailsScreen.clickSelectButton();
			HotelsRoomsRatesScreen.hotelNameTextView().check(matches(withText(hotelName)));
			HotelsRoomsRatesScreen.hotelRatingBar().check(matches(withRating(detailsHotelRating)));
			checkAdditionalFees();
			checkRenovationNotice();
			checkRoomsAndRatesPage(hotelSearchPageDiscount);
			HotelsRoomsRatesScreen.clickBackButton();
			HotelsDetailsScreen.clickBackButton();
			ScreenActions.enterLog(TAG,
				"_________[" + hotelPosition + "] hotels Processed , remaining [" + (totalHotels - hotelPosition)
					+ "]");
		}
	}

	private void checkPriceExplanation(int ratePosition) throws Exception {
		boolean flag = false;
		final AtomicReference<String> priceValue = new AtomicReference<String>();
		try {
			HotelsRoomsRatesScreen.listItem().atPosition(ratePosition).onChildView(withId(R.id.total_price_text_view))
				.perform(getString(priceValue));
			String myPriceValue = priceValue.get();
			if (!(myPriceValue.equalsIgnoreCase(mRes.getString(R.string.per_night)) || myPriceValue
				.equalsIgnoreCase(mRes.getString(R.string.rate_avg_per_night)) || myPriceValue
				.equalsIgnoreCase(mRes.getString(R.string.rate_per_night)))) {
				flag = true;
			}
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "Price explanation is not present ");
		}
		finally {
			if (flag) {
				throw new Exception(
					"Price explanation for multi-night stay should be either of the 3 - per night, avg/night, /night");
			}
		}
	}

	private void checkAdditionalFees() {
		try {
			HotelsRoomsRatesScreen.additionalFeesInfoButton().check(matches(isDisplayed()));
			HotelsRoomsRatesScreen.clickAdditionalFeesInfoButton();
			Espresso.pressBack();
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "Additional Info button is not present: ");
		}
	}

	private void checkRenovationNotice() {
		try {
			HotelsRoomsRatesScreen.renovationInfoButton().check(matches(isDisplayed()));
			HotelsRoomsRatesScreen.clickRenovationInfoButton();
			Espresso.pressBack();
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "Renovation Info button is not present: ");
		}
	}

	private int getSearchScreenHotelDiscount(int discountedHotelPosition) {
		try {
			final AtomicReference<String> discountStringAtSearchScreen = new AtomicReference<String>();
			HotelsSearchScreen.hotelListItem().atPosition(discountedHotelPosition)
				.onChildView(allOf(withId(R.id.sale_text_view), isDisplayed()))
				.perform(getString(discountStringAtSearchScreen));
			String discountAtHotelSearchPage = discountStringAtSearchScreen.get().replaceAll("\\D+", "");
			int hotelSearchPageDiscount = Integer.parseInt(discountAtHotelSearchPage);
			return hotelSearchPageDiscount;
		}
		catch (Exception e) {
			ScreenActions
				.enterLog(TAG, "Discount is not present for hotel at list location " + discountedHotelPosition);

		}
		return -1;
	}

	private int checkDetailsPageDiscount() {
		int discountValueAtDetailsPage;
		final AtomicReference<String> discountStringAtDetailsPage = new AtomicReference<String>();
		HotelsDetailsScreen.saleBannerInfo().perform(getString(discountStringAtDetailsPage));
		String discountAtDetailsPage = discountStringAtDetailsPage.get().replaceAll("\\D+", "");
		discountValueAtDetailsPage = Integer.parseInt(discountAtDetailsPage);
		return discountValueAtDetailsPage;
	}

	private int getRoomsAndRatesPageDiscount() {
		int discountValueAtRoomsAndRates = 0, maximumDiscountAtRoomsAndRatesPage = 0;
		final AtomicReference<String> discountStringAtRoomsAndRates = new AtomicReference<String>();
		int typesOfRoomsAvailable = EspressoUtils.getListCount(HotelsRoomsRatesScreen.roomList()) - 1;
		for (int ratePosition = 1; ratePosition < typesOfRoomsAvailable; ratePosition++) {
			try {
				HotelsSearchScreen.hotelListItem().atPosition(ratePosition)
					.onChildView(allOf(withId(R.id.sale_text_view), isDisplayed()))
					.perform(getString(discountStringAtRoomsAndRates));
				String discountAtRoomsAndRates = discountStringAtRoomsAndRates.get().replaceAll("\\D+", "");
				discountValueAtRoomsAndRates = Integer.parseInt(discountAtRoomsAndRates);
				if (maximumDiscountAtRoomsAndRatesPage < discountValueAtRoomsAndRates) {
					maximumDiscountAtRoomsAndRatesPage = discountValueAtRoomsAndRates;
				}
			}
			catch (Exception e) {
				ScreenActions.enterLog(TAG, "Discount is not present for this room ");
			}
		}
		return maximumDiscountAtRoomsAndRatesPage;
	}

	private void checkRoomsAndRatesPage(int hotelSearchPageDiscount) throws Exception {
		int typesOfRoomsAvailable = EspressoUtils.getListCount(HotelsRoomsRatesScreen.roomList()) - 1;
		for (int ratePosition = 1; ratePosition < typesOfRoomsAvailable; ratePosition++) {
			ViewAssertion priceNotEmpty = selectedDescendantsMatch(withId(R.id.price_text_view), not(isEmpty()));
			ViewAssertion roomNameNotEmpty = selectedDescendantsMatch(withId(R.id.room_description_text_view),
				not(isEmpty()));
			HotelsRoomsRatesScreen.listItem().atPosition(ratePosition).check(priceNotEmpty);
			HotelsRoomsRatesScreen.listItem().atPosition(ratePosition).check(roomNameNotEmpty);
			checkPriceExplanation(ratePosition);
		}
		if (hotelSearchPageDiscount != -1) {
			int maxDiscountAtRoomsAndRates = getRoomsAndRatesPageDiscount();
			if (!(hotelSearchPageDiscount == maxDiscountAtRoomsAndRates)) {
				throw new Exception("Discount at Hotel-Search-Screen and Hotel-Rooms-And-Rates-Screen do not match. ");
			}
		}
	}

	private void initiateSearch() throws Exception {
		LocalDate startDate = LocalDate.now().plusDays(30);
		LocalDate endDate = LocalDate.now().plusDays(32);
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("SFO");
		HotelsSearchScreen.clickSuggestionAtIndex(getActivity(), 1);
	}
}
