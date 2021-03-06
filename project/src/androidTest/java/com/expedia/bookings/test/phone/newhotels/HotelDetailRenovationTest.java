package com.expedia.bookings.test.phone.newhotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

public class HotelDetailRenovationTest extends HotelTestCase {
	public void testETPHotelWithoutFreeCancellationHavingRenovation() throws Throwable {
		HotelScreen.doGenericSearch();
		HotelScreen.selectHotel("hotel_etp_renovation_resort");
		HotelScreen.waitForDetailsLoaded();

		onView(withText("View Room")).perform(scrollTo());

		// checking we are not showing rating and amenities container
		HotelScreen.ratingContainer().check(matches(not(isDisplayed())));
		HotelScreen.amenityContainer().check(matches(not(isDisplayed())));

		//if current allotment < 5, we show number of rooms left on "collapsed room container"
		//otherwise we just show free cancellation message

		HotelScreen.clickPayNow();
		onView(allOf(withId(R.id.room_header_image), isDisplayed())).perform(swipeUp());
		Common.delay(1);
		onView(withText("View Room")).perform(scrollTo());
		onView(allOf(withId(R.id.collapsed_urgency_text_view), withParent(
			withId(R.id.collapsed_container)), isDisplayed(), hasSibling(withText("2 double"))))
			.check(matches(withText("1 Room Left!")));

		onView(withText("View Room")).perform(click());

		Common.delay(1);
		onView(allOf(withId(R.id.collapsed_urgency_text_view), withParent(
			withId(R.id.collapsed_container)), isDisplayed(), hasSibling(withText("One King Bed"))))
			.check(matches(withText("Non-refundable")));
	}
}
