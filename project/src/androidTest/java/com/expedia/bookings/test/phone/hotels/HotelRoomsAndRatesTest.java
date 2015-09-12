package com.expedia.bookings.test.phone.hotels;

import org.joda.time.LocalDate;

import android.support.test.espresso.ViewAssertion;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.espresso.Common;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.assertion.ViewAssertions.selectedDescendantsMatch;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.isEmpty;
import static com.expedia.bookings.test.espresso.CustomMatchers.withRating;
import static org.hamcrest.CoreMatchers.not;

public class HotelRoomsAndRatesTest extends PhoneTestCase {

	public void testRoomsAndRatesInfo() throws Exception {
		initiateSearch();
		HotelsSearchScreen.clickListItem(1);

		/*The hotel thumbnail/name/city,state,country/star rating appear at
		  the top of the list
		 */

		String hotelName = EspressoUtils.getText(R.id.title);
		final float detailsHotelRating = EspressoUtils.getRatingValue(HotelsDetailsScreen.ratingBar());
		HotelsDetailsScreen.clickSelectButton();

		HotelsRoomsRatesScreen.hotelNameTextView().check(matches(withText(hotelName)));
		HotelsRoomsRatesScreen.hotelRatingBar().check(matches(withRating(detailsHotelRating)));

		/*Cells should include: Room Type, Bed Type, Price (if it’s a multi-night stay in an avg/night
		* POS on hotel which has variable rates, the text “avg/night” will appear next to the price)
		*/

		ViewAssertion priceNotEmpty = selectedDescendantsMatch(withId(R.id.price_text_view), not(isEmpty()));
		ViewAssertion roomNameNotEmpty = selectedDescendantsMatch(withId(R.id.room_description_text_view),
			not(isEmpty()));

		HotelsRoomsRatesScreen.listItem().atPosition(2).check(priceNotEmpty);
		HotelsRoomsRatesScreen.listItem().atPosition(2).check(roomNameNotEmpty);

		HotelsRoomsRatesScreen.listItem().atPosition(2).onChildView(withId(R.id.total_price_text_view))
			.check(matches(withText(mRes.getString(R.string.rate_per_night))));

		/* Merchant hotels reliably display “Free Cancellation” or “Non Refundable”
		 */
		ViewAssertion nonRefundableInfo = selectedDescendantsMatch(withId(R.id.value_adds_beds_text_view),
			not(isEmpty()));
		HotelsRoomsRatesScreen.listItem().atPosition(2).check(nonRefundableInfo);

		/*Display a resort fee if the hotel has a resort fee
		 */
		onView(withId(R.id.resort_fees_top_text)).check(matches(isDisplayed()));

	}

	public void testRoomsAndRatesDiscountLabel() throws Throwable {
		/*Discounted rooms display a green banner/triangle indicating the % off
		*/
		initiateSearch();
		HotelsSearchScreen.clickHotelWithName("air_attached_hotel");
		HotelsDetailsScreen.clickSelectButton();
		HotelsRoomsRatesScreen.listItem().atPosition(1).check(selectedDescendantsMatch(withId(R.id.sale_text_view),
			isDisplayed()));
		screenshot("discounted_room");
		Common.pressBack();
	}

	public void testARenovationNotice() throws Throwable {
		/*Display renovation notice if the hotel has one
		*/
		initiateSearch();
		HotelsSearchScreen.clickHotelWithName("hotel_etp_renovation_resort");
		HotelsDetailsScreen.clickSelectButton();
		screenshot("renovation_notice");
		onView(withId(R.id.construction_top_text)).check(matches(withText("Renovation Notice")));
		onView(withId(R.id.construction_bottom_text)).check(matches(withText("This property is undergoing renovations")));
		onView(withId(R.id.construction_icon)).check(matches(isDisplayed()));
		Common.pressBack();

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
