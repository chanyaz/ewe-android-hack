package com.expedia.bookings.test.phone.traveler;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.TravelerName;
import com.expedia.bookings.data.packages.PackageSearchParams;
import com.expedia.bookings.presenter.packages.TravelerPresenter;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUser;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.phone.packages.PackageScreen;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.vm.traveler.CheckoutTravelerViewModel;
import com.squareup.phrase.Phrase;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class TravelerPresenterTest {
	private TravelerPresenter testTravelerPresenter;

	TravelerName testName = new TravelerName();
	private final String testFirstName = "Oscar";
	private final String testLastName = "Grouch";
	private final String testPhone = "7732025862";

	private final String expectedTravelerOneText = Phrase.from(InstrumentationRegistry.getTargetContext()
		.getString(R.string.checkout_edit_traveler_TEMPLATE)).put("travelernumber", 1).format().toString();
	private final String expectedTravelerTwoText = Phrase.from(InstrumentationRegistry.getTargetContext()
		.getString(R.string.checkout_edit_traveler_TEMPLATE)).put("travelernumber", 2).format().toString();

	private CheckoutTravelerViewModel mockViewModel;

	@Rule
	public PlaygroundRule activityTestRule = new PlaygroundRule(R.layout.test_traveler_presenter, R.style.V2_Theme_Packages);

	@Before
	public void setUp() {
		testTravelerPresenter = (TravelerPresenter) activityTestRule.getRoot();
		testName.setFirstName(testFirstName);
		testName.setLastName(testLastName);
	}

	@Test
	public void testTransitionsOneTraveler() {
		mockViewModel = getMockViewModelEmptyTravelers(1);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_entry_widget);
	}

	@Test
	public void testTravelerEntryPersists() {
		mockViewModel = getMockViewModelEmptyTravelers(1);
		when(mockViewModel.validateTravelersComplete()).thenReturn(true);
		testTravelerPresenter.setViewModel(mockViewModel);
		setPackageParams();

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_entry_widget);

		enterValidTraveler();
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_default_state);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_entry_widget);
		assertValidTravelerFields();
	}

	@Test
	public void testMultipleTravelerFlow() {
		mockViewModel = getMockViewModelEmptyTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);
		setPackageParams();

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedTravelerOneText);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedTravelerTwoText);
	}

	@Test
	public void testMultipleTravelerEntryPersists() {
		mockViewModel = getMockViewModelEmptyTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);
		setPackageParams();

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUser.clickOnText(expectedTravelerOneText);
		enterValidTraveler();
		EspressoUtils.assertViewWithTextIsDisplayed(testName.getFullName());
		EspressoUser.clickOnText(testName.getFullName());

		assertValidTravelerFields();
	}

	@Test
	public void testAllTravelersValidEntryToDefault() {
		mockViewModel = getMockViewModelValidTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);
		setPackageParams();

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUser.clickOnText(expectedTravelerOneText);
		PackageScreen.clickTravelerDone();

		EspressoUtils.assertViewIsDisplayed(R.id.traveler_default_state);
	}

	private void enterValidTraveler() {
		PackageScreen.enterFirstName(testFirstName);
		PackageScreen.enterLastName(testLastName);
		Common.delay(1);
		PackageScreen.enterPhoneNumber(testPhone);
		PackageScreen.selectBirthDate(1989,6,9);
		PackageScreen.clickTravelerDone();
	}

	private void assertValidTravelerFields() {
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.first_name_input, testFirstName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.last_name_input, testLastName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_phone_number, testPhone);
	}

	private CheckoutTravelerViewModel getMockViewModelValidTravelers(int travelerCount) {
		CheckoutTravelerViewModel mockViewModel = mock(CheckoutTravelerViewModel.class);
		List<Traveler> mockTravelerList = mock(List.class);
		when(mockTravelerList.size()).thenReturn(travelerCount);
		when(mockViewModel.getTravelers()).thenReturn(mockTravelerList);
		when(mockViewModel.getTraveler(anyInt())).thenReturn(getValidTraveler());
		when(mockViewModel.validateTravelersComplete()).thenReturn(true);
		return mockViewModel;
	}

	private CheckoutTravelerViewModel getMockViewModelEmptyTravelers(int travelerCount) {
		CheckoutTravelerViewModel mockViewModel = mock(CheckoutTravelerViewModel.class);
		List<Traveler> mockTravelerList = mock(List.class);
		when(mockTravelerList.size()).thenReturn(travelerCount);
		when(mockViewModel.getTravelers()).thenReturn(mockTravelerList);
		when(mockViewModel.getTraveler(anyInt())).thenReturn(new Traveler());
		return mockViewModel;
	}

	private Traveler getValidTraveler() {
		Traveler validTraveler = new Traveler();
		validTraveler.setFirstName(testFirstName);
		validTraveler.setLastName(testLastName);
		validTraveler.setGender(Traveler.Gender.MALE);
		validTraveler.setPhoneNumber(testPhone);
		validTraveler.setBirthDate(LocalDate.now().minusYears(18));

		return validTraveler;
	}

	private void setPackageParams() {
		PackageSearchParams packageParams = new PackageSearchParams.Builder(12)
			.checkIn(LocalDate.now().plusDays(1))
			.checkOut(LocalDate.now().plusDays(2))
			.origin(new SuggestionV4())
			.destination(new SuggestionV4())
			.build();
		Db.setPackageParams(packageParams);
	}
}
