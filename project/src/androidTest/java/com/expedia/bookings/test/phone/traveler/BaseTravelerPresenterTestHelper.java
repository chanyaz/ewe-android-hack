package com.expedia.bookings.test.phone.traveler;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;

import android.support.test.InstrumentationRegistry;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.TravelerName;
import com.expedia.bookings.data.packages.PackageSearchParams;
import com.expedia.bookings.presenter.packages.TravelerPresenter;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.phone.packages.PackageScreen;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.widget.CheckoutToolbar;
import com.expedia.vm.CheckoutToolbarViewModel;
import com.expedia.vm.traveler.CheckoutTravelerViewModel;
import com.squareup.phrase.Phrase;

public class BaseTravelerPresenterTestHelper {
	protected TravelerPresenter testTravelerPresenter;
	private CheckoutToolbar testToolbar;
	protected CheckoutTravelerViewModel mockViewModel;

	protected TravelerName testName = new TravelerName();
	protected final String testChildFullName = "Oscar Grouch Jr.";
	protected final String testFirstName = "Oscar";
	protected final String testMiddleName = "T";
	protected final String testLastName = "Grouch";
	protected final String testChildLastName = "Grouch Jr.";
	protected final String testPhone = "7732025862";
	protected final String testBirthDay = "Jan 27, 1991";

	protected final String expectedMainText = "Main Traveler";
	protected final String expectedAdditionalText = "Additional Travelers";
	protected final String expectedTravelerOneText = Phrase.from(InstrumentationRegistry.getTargetContext()
		.getString(R.string.checkout_edit_traveler_TEMPLATE)).put("travelernumber", 1).put("passengercategory", "Adult").format().toString();
	protected final String expectedTravelerTwoText = Phrase.from(InstrumentationRegistry.getTargetContext()
		.getString(R.string.checkout_edit_traveler_TEMPLATE)).put("travelernumber", 2).put("passengercategory", "Adult").format().toString();
	protected final String expectedTravelerChildText = Phrase.from(InstrumentationRegistry.getTargetContext()
		.getString(R.string.checkout_edit_traveler_TEMPLATE)).put("travelernumber", 3).put("passengercategory", "Child").format().toString();
	protected final String expectedTravelerInfantText = Phrase.from(InstrumentationRegistry.getTargetContext()
		.getString(R.string.checkout_edit_traveler_TEMPLATE)).put("travelernumber", 3).put("passengercategory", "Infant").format().toString();

	@Rule
	public PlaygroundRule activityTestRule = new PlaygroundRule(R.layout.test_traveler_presenter, R.style.V2_Theme_Packages);

	@Before
	public void setUp() {
		testTravelerPresenter = (TravelerPresenter) activityTestRule.getRoot().findViewById(R.id.traveler_presenter);
		testToolbar = (CheckoutToolbar) activityTestRule.getRoot().findViewById(R.id.checkout_toolbar);
		testToolbar.setViewModel(new CheckoutToolbarViewModel(activityTestRule.getActivity()));
		testTravelerPresenter.getToolbarTitleSubject().subscribe(testToolbar.getViewModel().getToolbarTitle());
		testTravelerPresenter.getTravelerEntryWidget().getFocusedView().subscribe(testToolbar.getViewModel().getCurrentFocus());
		testTravelerPresenter.getTravelerEntryWidget().getFilledIn().subscribe(testToolbar.getViewModel().getFormFilledIn());
		testTravelerPresenter.getMenuVisibility().subscribe(testToolbar.getViewModel().getMenuVisibility());
		testToolbar.getViewModel().getDoneClicked().subscribe(testTravelerPresenter.getTravelerEntryWidget().getDoneClicked());
		testName.setFirstName(testFirstName);
		testName.setLastName(testLastName);
	}

	protected void enterValidTraveler() {
		PackageScreen.enterFirstName(testFirstName);
		PackageScreen.enterLastName(testLastName);
		Common.delay(1);
		PackageScreen.enterPhoneNumber(testPhone);
		PackageScreen.selectBirthDate(1991, 1, 27);
		PackageScreen.clickTravelerDone();
	}

	protected PackageSearchParams setPackageParams(int adults) {
		return setPackageParams(adults, new ArrayList<Integer>(), false);
	}

	protected PackageSearchParams setPackageParams(int adults, List<Integer> children, boolean infantsInLap) {
		PackageSearchParams packageParams = (PackageSearchParams) new PackageSearchParams.Builder(12)
			.startDate(LocalDate.now().plusDays(1))
			.endDate(LocalDate.now().plusDays(2))
			.origin(new SuggestionV4())
			.destination(new SuggestionV4())
			.adults(adults)
			.children(children)
			.infantSeatingInLap(infantsInLap)
			.build();
		Db.setPackageParams(packageParams);
		return packageParams;
	}

	protected void addTravelerToDb(Traveler traveler) {
		Db.getTravelers().add(traveler);
	}


	protected void assertValidTravelerFields() {
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.first_name_input, testFirstName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.last_name_input, testLastName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_phone_number, testPhone);
	}

	protected CheckoutTravelerViewModel getMockViewModelEmptyTravelersWithInfant(int adultCount, List<Integer> children, boolean infantsInLap) {
		CheckoutTravelerViewModel mockViewModel = new CheckoutTravelerViewModel();
		mockViewModel.refreshTravelerList(setPackageParams(adultCount, children, infantsInLap));
		return mockViewModel;
	}

	protected CheckoutTravelerViewModel getMockViewModelEmptyTravelers(int travelerCount) {
		CheckoutTravelerViewModel mockViewModel = new CheckoutTravelerViewModel();
		mockViewModel.refreshTravelerList(setPackageParams(travelerCount));
		return mockViewModel;
	}

	protected CheckoutTravelerViewModel getMockViewModelIncompleteTravelers(int travelerCount) {
		CheckoutTravelerViewModel mockViewModel = new CheckoutTravelerViewModel();
		mockViewModel.refreshTravelerList(setPackageParams(travelerCount));
		for (int i = 0; i < travelerCount; i++) {
			Traveler traveler = Db.getTravelers().get(i);
			setIncompleteTraveler(traveler);
		}
		return mockViewModel;
	}

	protected CheckoutTravelerViewModel getMockViewModelValidTravelers(int travelerCount) {
		CheckoutTravelerViewModel mockViewModel = new CheckoutTravelerViewModel();
		mockViewModel.refreshTravelerList(setPackageParams(travelerCount));
		for (int i = 0; i < travelerCount; i++) {
			Traveler traveler = Db.getTravelers().get(i);
			setValidTraveler(traveler);
		}
		return mockViewModel;
	}

	protected void setIncompleteTraveler(Traveler validTraveler) {
		validTraveler.setFirstName(testFirstName);
	}

	protected void setValidTraveler(Traveler validTraveler) {
		validTraveler.setFirstName(testFirstName);
		validTraveler.setLastName(testLastName);
		validTraveler.setGender(Traveler.Gender.MALE);
		validTraveler.setPhoneNumber(testPhone);
		validTraveler.setBirthDate(LocalDate.now().minusYears(18));
	}

	protected void setChildTraveler(Traveler childTraveler, int yearsOld) {
		childTraveler.setFirstName(testFirstName);
		childTraveler.setLastName(testChildLastName);
		childTraveler.setGender(Traveler.Gender.MALE);
		childTraveler.setPhoneNumber(testPhone);
		childTraveler.setBirthDate(LocalDate.now().minusYears(yearsOld));
	}

	protected Traveler makeStoredTraveler() {
		Traveler storedTraveler = new Traveler();
		storedTraveler.setFirstName(testFirstName);
		storedTraveler.setMiddleName(testMiddleName);
		storedTraveler.setLastName(testLastName);
		storedTraveler.setGender(Traveler.Gender.MALE);
		storedTraveler.setPhoneNumber(testPhone);
		storedTraveler.setBirthDate(LocalDate.now().withYear(1991).withMonthOfYear(1).withDayOfMonth(27));
		return storedTraveler;
	}
}
