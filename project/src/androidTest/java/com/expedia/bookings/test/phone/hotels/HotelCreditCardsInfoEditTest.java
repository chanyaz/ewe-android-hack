package com.expedia.bookings.test.phone.hotels;

import java.util.Random;

import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelSearchActivity;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonPaymentMethodScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.tablet.pagemodels.Checkout;

import static com.expedia.bookings.R.drawable.ic_amex_white;
import static com.expedia.bookings.R.drawable.ic_diners_club_white;
import static com.expedia.bookings.R.drawable.ic_discover_white;
import static com.expedia.bookings.R.drawable.ic_maestro_white;
import static com.expedia.bookings.R.drawable.ic_master_card_white;
import static com.expedia.bookings.R.drawable.ic_union_pay_white;
import static com.expedia.bookings.R.drawable.ic_visa_white;

@RunWith(AndroidJUnit4.class)
public class HotelCreditCardsInfoEditTest {

	@Rule
	public ActivityTestRule<HotelSearchActivity> activity = new ActivityTestRule<>(HotelSearchActivity.class);

	private static class TestData {
		public String name;
		public String[] prefixes;
		public int length;
		public int drawableId;

		public TestData name(String name) {
			this.name = name;
			return this;
		}

		public TestData prefixes(String... prefixes) {
			this.prefixes = prefixes;
			return this;
		}

		public TestData length(int length) {
			this.length = length;
			return this;
		}

		public TestData drawableId(int drawableId) {
			this.drawableId = drawableId;
			return this;
		}
	}

	@Test
	public void amexIconTest() throws Throwable {
		TestData data = new TestData()
			.name("Amex")
			.prefixes("34", "37")
			.length(15)
			.drawableId(ic_amex_white);

		runTest(data);
	}

	@Test
	public void carteBlancheIconTest() throws Throwable {
		TestData data = new TestData()
			.name("CarteBlanche")
			.prefixes("94", "95")
			.length(14)
			.drawableId(R.drawable.ic_carte_blanche_white);

		runTest(data);
	}

	@Test
	public void chinaUnionNineteenIconTest() throws Throwable {
		TestData data = new TestData()
			.name("ChinaUnion19")
			.prefixes("62")
			.length(19)
			.drawableId(ic_union_pay_white);

		runTest(data);
	}

	@Test
	public void chinaUnionEighteenIconTest() throws Throwable {
		TestData data = new TestData()
			.name("ChinaUnion18")
			.prefixes("62")
			.length(18)
			.drawableId(ic_union_pay_white);

		runTest(data);
	}

	@Test
	public void chinaUnionSeventeenIconTest() throws Throwable {
		TestData data = new TestData()
			.name("ChinaUnion17")
			.prefixes("62")
			.length(17)
			.drawableId(ic_union_pay_white);

		runTest(data);
	}

	@Test
	public void dinersClubIconTest() throws Throwable {
		TestData data = new TestData()
			.name("DinersClub")
			.prefixes("30", "36", "38", "60")
			.length(14)
			.drawableId(ic_diners_club_white);

		runTest(data);
	}

	@Test
	public void discoverIconTest() throws Throwable {
		TestData data = new TestData()
			.name("Discover")
			.prefixes("60")
			.length(16)
			.drawableId(ic_discover_white);

		runTest(data);
	}


	@Test
	public void maestroNineteenIconTest() throws Throwable {
		TestData data = new TestData()
			.name("Maestro19")
			.prefixes("50", "63", "67")
			.length(19)
			.drawableId(ic_maestro_white);

		runTest(data);
	}

	@Test
	public void maestroEighteenIconTest() throws Throwable {
		TestData data = new TestData()
			.name("Maestro18")
			.prefixes("50", "63", "67")
			.length(18)
			.drawableId(ic_maestro_white);

		runTest(data);
	}

	@Test
	public void maestroSixteenIconTest() throws Throwable {
		TestData data = new TestData()
			.name("Maestro16")
			.prefixes("50", "63", "67")
			.length(16)
			.drawableId(ic_maestro_white);

		runTest(data);
	}


	@Test
	public void mastercardIconTest() throws Throwable {
		TestData data = new TestData()
			.name("MasterCard")
			.prefixes("51", "52", "53", "54", "55")
			.length(16)
			.drawableId(ic_master_card_white);

		runTest(data);
	}

	@Test
	public void visaThirteenIconTest() throws Throwable {
		TestData data = new TestData()
			.name("Visa13")
			.prefixes("4")
			.length(13)
			.drawableId(ic_visa_white);

		runTest(data);
	}

	@Test
	public void visaSixteenIconTest() throws Throwable {
		TestData data = new TestData()
			.name("Visa16")
			.prefixes("4")
			.length(16)
			.drawableId(ic_visa_white);

		runTest(data);
	}

	public void runTest(TestData data) throws Throwable {
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickSuggestionWithName(activity.getActivity(), "New York, NY");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickHotelWithName("happypath");
		HotelsDetailsScreen.clickSelectButton();
		HotelsRoomsRatesScreen.selectRoomItem(0);
		HotelsCheckoutScreen.clickCheckoutButton();

		HotelsCheckoutScreen.clickGuestDetails();
		CommonTravelerInformationScreen.enterFirstName("Mobiata");
		CommonTravelerInformationScreen.enterLastName("Auto");
		CommonTravelerInformationScreen.enterPhoneNumber("1112223333");
		CommonTravelerInformationScreen.enterEmailAddress("mobiataauto@gmail.com");
		CommonTravelerInformationScreen.clickDoneButton();

		HotelsCheckoutScreen.clickSelectPaymentButton();

		Random rand = new Random();
		int randomNumber;

		String creditCardNumber;

		for (int i = 0; i < data.prefixes.length; i++) {
			//random credit card numbers per card type
			creditCardNumber = data.prefixes[i];
			for (int k = creditCardNumber.length(); k < data.length; k++) {
				randomNumber = rand.nextInt(10);
				creditCardNumber += randomNumber;
			}

			CardInfoScreen.typeTextCreditCardEditText(creditCardNumber);
			Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
			CardInfoScreen.clickOnExpirationDateButton();
			CardInfoScreen.clickMonthUpButton();
			CardInfoScreen.clickYearUpButton();
			CardInfoScreen.clickSetButton();
			CardInfoScreen.typeTextPostalCode("94015");
			CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");


			/*
			* Case 1: verify cards working, test credit card logo displayed
			*/
			EspressoUtils.assertContainsImageDrawable(R.id.display_credit_card_brand_icon_white,
				data.drawableId);
			CardInfoScreen.clickOnDoneButton();
			HotelsCheckoutScreen.slideToCheckout();

			/*
			* Case 2: check cvv sub prompt text view
			* For Amex cards:"See front of the card" and for other cards: "See back of card"
			*/

			if (data.name.equals("Amex")) {
				EspressoUtils.assertViewWithTextIsDisplayed(
					activity.getActivity().getString(R.string.See_front_of_card));
			}
			else {
				EspressoUtils
					.assertViewWithTextIsDisplayed(activity.getActivity().getString(R.string.See_back_of_card));
			}

			/*
			* Case 3: Security Code will show the cardholders name Firstname Lastname as: F. Lastname
			*/

			EspressoUtils.assertContains(CVVEntryScreen.cvvSignatureText(), "M. Auto");

			//go back for next test data
			Common.pressBack();
			Checkout.clickCreditCardSection();
			Common.delay(3);
			CommonPaymentMethodScreen.clickOnAddNewCardTextView();
		}
	}
}
