package com.expedia.bookings.test.phone.hotels;

import java.util.Random;

import org.joda.time.LocalDate;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.test.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonPaymentMethodScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsConfirmationScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelsUserData;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.MockModeShim;
import com.mobiata.android.Log;
import com.mobiata.mocke3.ExpediaDispatcher;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class HotelConfirmationTest extends PhoneTestCase {

	private HotelsUserData mUser;
	private int mNumberOfGuestsFromSearch;
	private String mHotelNameFromCheckout;

	public void testLoggedInBookingConfirmation() throws Exception {
		mUser = new HotelsUserData();

		LaunchScreen.launchHotels();

		// Search
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "New York, NY");
		LocalDate startDate = LocalDate.now().plusDays(58);
		LocalDate endDate = LocalDate.now().plusDays(61);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickOnGuestsButton();
		setGuests();
		HotelsGuestPicker.clickOnSearchButton();
		HotelsSearchScreen.clickListItem(1);
		// Click hotel again to make sure the travel ad fires
		Espresso.pressBack();
		HotelsSearchScreen.clickListItem(1);

		// Details
		HotelsDetailsScreen.clickSelectButton();
		HotelsRoomsRatesScreen.selectRoomItem(0);

		// Checkout
		HotelsCheckoutScreen.clickCheckoutButton();
		HotelsCheckoutScreen.clickLogInButton();
		LogInScreen.typeTextEmailEditText(mUser.email);
		LogInScreen.typeTextPasswordEditText(mUser.password);
		LogInScreen.clickOnLoginButton();

		HotelsCheckoutScreen.clickSelectPaymentButton();
		CommonPaymentMethodScreen.clickOnAddNewCardTextView();
		CardInfoScreen.typeTextCreditCardEditText(mUser.creditCardNumber);
		BillingAddressScreen.typeTextPostalCode(mUser.zipcode);
		CardInfoScreen.typeTextNameOnCardEditText(mUser.firstName + " " + mUser.lastName);
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.clickOnDoneButton();
		CardInfoScreen.clickNoThanksButton();

		mHotelNameFromCheckout = EspressoUtils.getText(R.id.title);
		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.enterCVV(mUser.cvv);
		CVVEntryScreen.clickBookButton();
		verifyConfirmationTexts();
		verifyTravelAdTracking();

		// Hitting done takes you to launch (as does back press)
		HotelsConfirmationScreen.doneButton().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.launch_toolbar);
	}

	private void setGuests() {
		Random rand = new Random();

		// Can have a maximum of six guests
		// Can add at most 4 children
		int children = rand.nextInt(5);

		// Must have a minimum of 1 adult, thus can only add a maximum of 5 minus the number of children already added
		int adults = rand.nextInt(6 - children) + 1;

		mNumberOfGuestsFromSearch = adults + children;

		Log.v("Setting adults to: " + adults + " and children to: " + children);
		for (int i = 6; i >= 1; i--) {
			HotelsGuestPicker.decrementAdultsButton();
		}
		for (int i = 4; i >= 0; i--) {
			HotelsGuestPicker.decrementChildrenButton();
		}

		for (int i = 1; i < adults; i++) {
			HotelsGuestPicker.incrementAdultsButton();
		}

		for (int i = 0; i < children; i++) {
			HotelsGuestPicker.incrementChildrenButton();
		}
	}

	private void verifyConfirmationTexts() {
		HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();

		// Guests / dates string
		int cachedNumberOfGuests = params.getNumAdults() + params.getNumChildren();
		assertEquals(mNumberOfGuestsFromSearch, cachedNumberOfGuests);
		String guestString = getActivity().getResources().getQuantityString(R.plurals.number_of_guests,
			mNumberOfGuestsFromSearch, mNumberOfGuestsFromSearch);
		String dateRangeFromSearchString = DateFormatUtils.formatRangeDateToDate(getActivity(), params,
			DateFormatUtils.FLAGS_DATE_ABBREV_MONTH);
		String expectedSummaryString = getActivity().getResources().getString(R.string.stay_summary_TEMPLATE, guestString,
			dateRangeFromSearchString);
		HotelsConfirmationScreen.summaryTextView().check(matches(withText(expectedSummaryString)));

		// Hotel name
		HotelsConfirmationScreen.hotelNameTextView().check(matches(withText(mHotelNameFromCheckout)));

		// Itinerary number
		String expectedItineraryNumber = Db.getTripBucket().getHotel().getBookingResponse().getItineraryId();
		String expectedItineraryConfirmationText = getActivity().getResources().getString(R.string.itinerary_confirmation_TEMPLATE, expectedItineraryNumber);
		HotelsConfirmationScreen.itineraryTextView().check(matches(withText(expectedItineraryConfirmationText)));

		// Email address
		String expectedEmailAddString = mUser.email;
		HotelsConfirmationScreen.emailTextView().check(matches(withText(expectedEmailAddString)));

		// Actions are displayed (share, add to calendar, call expedia)
		EspressoUtils.assertViewIsDisplayed(R.id.call_action_text_view);
		EspressoUtils.assertViewIsDisplayed(R.id.share_action_text_view);
		EspressoUtils.assertViewIsDisplayed(R.id.calendar_action_text_view);
	}

	private void verifyTravelAdTracking() {
		ExpediaDispatcher dispatcher = MockModeShim.getDispatcher();
		assertEquals(3, dispatcher.numOfTravelAdRequests("/travel"));
		assertEquals(3, dispatcher.numOfTravelAdRequests("/TravelAdsService/v3/Hotels/TravelAdImpression"));
		assertEquals(2, dispatcher.numOfTravelAdRequests("/TravelAdsService/v3/Hotels/TravelAdClick"));
		assertEquals(1, dispatcher.numOfTravelAdRequests("/ads/hooklogic"));
	}
}
