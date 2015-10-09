package com.expedia.bookings.test.ui.tablet.tests.hotels;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;
import android.support.test.espresso.DataInteraction;

/**
 * Created by dmadan on 8/5/14.
 */
public class HotelDetailsTest extends TabletTestCase {

	public void testHotelDetails() throws Exception {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestionAtPosition(0);
		LocalDate startDate = LocalDate.now().plusDays(35);
		Search.clickDate(startDate, null);
		Search.clickSearchPopupDone();
		Results.swipeUpHotelList();
		Results.clickHotelAtIndex(1);
		verifyHotelDetails();
	}

	private void verifyHotelDetails() throws Exception {

		int currentHotelCount = EspressoUtils.getListCount(Results.hotelList()) - 1;
		for (int j = 1; j < currentHotelCount - 1; j++) {
			DataInteraction resultRow = Results.hotelAtIndex(j);

			//verify hotel name and price in search results matches info in hotel details
			String resultHotelName = EspressoUtils.getListItemValues(resultRow, R.id.name_text_view);
			String resultHotelPrice = EspressoUtils.getListItemValues(resultRow, R.id.price_text_view);
			Results.clickHotelAtIndex(j);

			//assert hotel details info on the card matches hotel search result info
			EspressoUtils.assertContains(HotelDetails.hotelName(), resultHotelName);
			EspressoUtils.assertContains(HotelDetails.hotelPrice(), resultHotelPrice);

			//Add hotel button and hotel rating are displayed on details
			Common.checkDisplayed(HotelDetails.addHotel());
			Common.checkDisplayed(HotelDetails.hotelRating());

			//verify Reviews action bar contents exists
			verifyHotelReviews();
		}
	}

	private void verifyHotelReviews() {
		HotelDetails.clickReviews();
		HotelDetails.clickCriticalTab();
		HotelDetails.clickFavorableTab();
		HotelDetails.clickRecentTab();

		//Add hotel button displayed on Reviews screen
		Common.checkDisplayed(HotelDetails.reviewsAddHotel());
		Common.pressBack();
	}
}
