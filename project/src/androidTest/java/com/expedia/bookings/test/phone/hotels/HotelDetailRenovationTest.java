package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

public class HotelDetailRenovationTest extends HotelTestCase {
	@Test
	public void testETPHotelWithoutFreeCancellationHavingRenovation() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("hotel_etp_renovation_resort");
		HotelInfoSiteScreen.waitForDetailsLoaded();

		HotelInfoSiteScreen.clickStickySelectRoom();
		HotelScreen.clickPayNow();

		HotelScreen.ratingContainer().check(matches(not(isDisplayed())));
		HotelScreen.amenityContainer().check(matches(not(isDisplayed())));

		HotelInfoSiteScreen.roomCardViewForRoomType("hotel_etp_renovation_resort_0").perform(scrollTo());

		onView(allOf(
			withId(R.id.cancellation_text_view),
			withParent(allOf(
				withId(R.id.value_adds_point_fee_container),
				HotelInfoSiteScreen.descendantOfSameGroupRoomWithBed("hotel_etp_renovation_resort_0", "One King Bed")
			))
		)).check(matches(withText("Non-refundable")));

		HotelInfoSiteScreen.roomCardViewForRoomType("pay_later_room_0").perform(scrollTo());
		onView(allOf(
			withId(R.id.cancellation_text_view),
			withParent(allOf(
				withId(R.id.value_adds_point_fee_container),
				HotelInfoSiteScreen.descendantOfSameGroupRoomWithBed("pay_later_room_0", "2 double")
			))
		)).check(matches(withText("Non-refundable")));

		onView(allOf(
			withId(R.id.room_left_text_view),
			withParent(allOf(
				withId(R.id.room_left_container),
				withParent(allOf(
					withId(R.id.price_button_container),
					HotelInfoSiteScreen.descendantOfSameGroupRoomWithBed("pay_later_room_0", "2 double")
				))
			))
		)).check(matches(withText("1 Room Left!")));
	}
}
