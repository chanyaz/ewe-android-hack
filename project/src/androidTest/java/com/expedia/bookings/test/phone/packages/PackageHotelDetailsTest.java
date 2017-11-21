package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.swipeUp;
import static org.hamcrest.CoreMatchers.allOf;

public class PackageHotelDetailsTest extends PackageTestCase {

	@Test
	public void testHideStandaloneHotelPricing() throws Throwable {
		PackageScreen.searchPackage();
		Common.delay(2);

		HotelScreen.selectHotel("Package Happy Path");
		Common.delay(3);

		EspressoUtils.assertViewWithSiblingIsNotDisplayed(R.id.discount_percentage, R.id.air_attach_swp_image_details);
		EspressoUtils.assertViewIsDisplayed(R.id.hotel_price_container);

		HotelInfoSiteScreen.clickStickySelectRoom();

		HotelInfoSiteScreen.roomCardViewForRoomType("change_hotel_room").perform(scrollTo(), swipeUp());
		HotelInfoSiteScreen.pricePerDescriptorViewWithTextAndPrice("/night", "+$0.00").check(matches(isDisplayed()));

		HotelInfoSiteScreen.roomCardViewForRoomType("happy_outbound_flight").perform(scrollTo(), swipeUp());
		HotelInfoSiteScreen.pricePerDescriptorViewWithTextAndPrice("/night", "-$3.21").check(matches(isDisplayed()));
	}

	@Test
	public void testHotelDetailsToolbarText() throws Throwable {
		PackageScreen.searchPackageFor(2, 1);
		Common.delay(1);

		HotelScreen.selectHotel("Package Happy Path");
		Common.delay(1);

		PackageScreen.hotelDetailsToolbar().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Package Happy Path")))));
		onView(allOf(withId(R.id.price_per_descriptor), withText(" per person")));
		onView(withId(R.id.hotel_search_info)).check(matches(withText("Feb 3 - Feb 4")));
		onView(withId(R.id.hotel_search_info_guests)).check(matches(withText("3 guests")));
	}

	@Test
	public void testVIPHotel() throws Throwable {
		PackageScreen.searchPackage();
		Common.delay(1);
		HotelScreen.selectHotel("Package Happy Path");
		Common.delay(3);
		HotelScreen.clickVIPAccess();
		Common.delay(2);
		EspressoUtils.assertViewWithTextIsDisplayed(getActivity().getString(R.string.vip_access_message));
		Common.pressBack();
		HotelInfoSiteScreen.waitForDetailsLoaded();
		EspressoUtils.assertViewIsDisplayed(R.id.vip_access_message_container);
	}

	@Test
	public void testRenoHotel() throws Throwable {
		PackageScreen.searchPackage();
		HotelScreen.selectHotel("Package Happy Path");
		HotelScreen.clickRenoInfo();
		onView(allOf(withId(R.id.content_description),
			withText("<ul><li>Elevator</li><li>Front desk</li><li>Lobby</li></ul>")));
	}
}
