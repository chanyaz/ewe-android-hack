package com.expedia.bookings.test.phone.sweep;

import org.joda.time.DateTime;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

/**
 * Created by dmadan on 6/30/14.
 */
public class PhoneSweep extends PhoneTestCase {


	public void testBookNewHotel() throws Throwable {
		Common.setLocale(getLocale());
		Common.setPOS(PointOfSaleId.valueOf(getPOS(getLocale())));
		final DateTime startDateTime = DateTime.now().plusDays(60);
		final DateTime endDateTime = startDateTime.plusDays(5);
		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelsABTest,
			AbacusUtils.DefaultVariate.BUCKETED.ordinal());
		Common.delay(1);

		LaunchScreen.launchHotels();
		HotelScreen.location().perform(typeText("Las Vegas, NV"));
		Common.delay(2);
		HotelScreen.hotelSuggestionList().perform(
			RecyclerViewActions.actionOnItemAtPosition(1, click()));
		HotelScreen.selectDateButton().perform(click());
		HotelScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		screenshot("Hotels_Search");
		HotelScreen.guestPicker().perform((click()));
		screenshot("Hotel_search_guest_picker");
		onView(withId(R.id.children_plus)).perform(click());
		screenshot("Hotel_search_child_picker");
		onView(withId(R.id.child_spinner_1)).perform(click());
		screenshot("Hotel_search_child_age");
		Common.pressBack();
		HotelScreen.searchButton().perform(click());
		Common.delay(5);
		screenshot("Hotels_Search_Results");
		HotelScreen.clickSortFilter();
		Common.delay(1);
		screenshot("Hotel_sort_filter");
		Common.pressBack();
		HotelScreen.selectHotel("Stratosphere Hotel - Casino & Resort Hotel");
		Common.delay(5);
		try {
			HotelScreen.clickRatingContainer();
			Common.delay(2);
			screenshot("Hotel_Reviews");
			Common.pressBack();
		}
		catch (Exception e) {
			//no reviews
		}
		screenshot("Hotels_Details");
		onView(allOf(withText(R.string.select_a_room_instruction), isDisplayed())).perform(click());
		screenshot("Hotel_rooms_pay_now");
		onView(withId(R.id.radius_pay_later)).perform(click());
		Common.delay(1);
		screenshot("Hotel_rooms_pay_later");
		onView(allOf(withId(R.id.deposit_terms_buttons), isDisplayed())).perform(click());

		Common.delay(1);
		screenshot("Hotel_deposits_terms_text");
		Common.pressBack();
		Common.delay(1);
		onView(allOf(withText(R.string.book_room_button_text), isDisplayed())).perform(click());
		Common.delay(5);

		screenshot("Hotel_checkout");
		onView(withId(R.id.amount_due_today_label)).perform(click());
		screenshot("Hotel_checkout_cost_summary");
		Common.pressBack();
		onView(withId(R.id.login_widget)).perform(click());
		Common.delay(1);
		screenshot("Hotel_Login");

		Common.pressBack();
		Common.delay(1);
		CheckoutViewModel.clickDriverInfo();
		screenshot("Hotel_traveler_info_empty");
		Common.delay(1);
		CheckoutViewModel.enterFirstName("Girija");
		CheckoutViewModel.enterLastName("Balachandran");
		Common.closeSoftKeyboard(CheckoutViewModel.lastName());
		Common.delay(1);
		CheckoutViewModel.enterEmail("girija@mobiata.com");
		Common.closeSoftKeyboard(CheckoutViewModel.email());
		Common.delay(1);
		CheckoutViewModel.enterPhoneNumber("4152984012");
		screenshot("Hotel_traveler_info_filled");
		CheckoutViewModel.clickDone();
		Common.delay(2);

		Common.delay(2);
		CheckoutViewModel.clickPaymentInfo();
		screenshot("Hotel_payment_options");
		Common.delay(1);
		try {
			CheckoutViewModel.clickAddCreditCard();
			Common.delay(1);
		}
		catch (Exception e) {
			//No credit or debit card
		}

		screenshot("Hotel_payment_info_empty");
		CardInfoScreen.typeTextCreditCardEditText("4444444444444448");
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextPostalCode("94104");
		CardInfoScreen.typeTextNameOnCardEditText("Girija Balachandran");
		Common.delay(2);
		screenshot("Hotel_payment_info_filled");
		CheckoutViewModel.pressClose();
		screenshot("Hotel_Checkout_Ready_To_Purchase");
		try {
			onView(withId(R.id.i_accept_terms_button)).perform(click());
		}
		catch (Exception e) {
			//No I accept button
		}
		CheckoutViewModel.performSlideToPurchase();
		Common.delay(1);
		screenshot("Slide_to_checkout");
		CVVEntryScreen.enterCVV("123");
		screenshot("CVV_Entry");
		CVVEntryScreen.clickBookButton();
		Common.delay(5);
		screenshot("Confirmation");
	}


}
