package com.expedia.bookings.test.phone.traveler;

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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseTravelerPresenterTestHelper {
	protected TravelerPresenter testTravelerPresenter;
	private CheckoutToolbar testToolbar;
	protected CheckoutTravelerViewModel mockViewModel;

	protected TravelerName testName = new TravelerName();
	protected final String testFirstName = "Oscar";
	protected final String testMiddleName = "T";
	protected final String testLastName = "Grouch";
	protected final String testPhone = "7732025862";
	protected final String testBirthDay = "Jan 27, 1991";

	protected final String expectedTravelerOneText = Phrase.from(InstrumentationRegistry.getTargetContext()
		.getString(R.string.checkout_edit_traveler_TEMPLATE)).put("travelernumber", 1).format().toString();
	protected final String expectedTravelerTwoText = Phrase.from(InstrumentationRegistry.getTargetContext()
		.getString(R.string.checkout_edit_traveler_TEMPLATE)).put("travelernumber", 2).format().toString();

	@Rule
	public PlaygroundRule activityTestRule = new PlaygroundRule(R.layout.test_traveler_presenter, R.style.V2_Theme_Packages);

	@Before
	public void setUp() {
		testTravelerPresenter = (TravelerPresenter) activityTestRule.getRoot().findViewById(R.id.traveler_presenter);
		testToolbar = (CheckoutToolbar) activityTestRule.getRoot().findViewById(R.id.checkout_toolbar);
		testToolbar.setViewModel(new CheckoutToolbarViewModel(activityTestRule.getActivity()));
		testTravelerPresenter.getToolbarTitleSubject().subscribe(testToolbar.getViewModel().getToolbarTitle());
		testTravelerPresenter.getTravelerEntryWidget().getFocusedView().subscribe(testToolbar.getViewModel().getEditText());
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

	protected void setPackageParams() {
		PackageSearchParams packageParams = (PackageSearchParams) new PackageSearchParams.Builder(12)
			.startDate(LocalDate.now().plusDays(1))
			.endDate(LocalDate.now().plusDays(2))
			.departure(new SuggestionV4())
			.arrival(new SuggestionV4())
			.build();
		Db.setPackageParams(packageParams);
	}

	protected void addTravelerToDb(Traveler traveler) {
		Db.getTravelers().add(traveler);
	}


	protected void assertValidTravelerFields() {
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.first_name_input, testFirstName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.last_name_input, testLastName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_phone_number, testPhone);
	}

	protected CheckoutTravelerViewModel getMockViewModelEmptyTravelers(int travelerCount) {
		CheckoutTravelerViewModel mockViewModel = mock(CheckoutTravelerViewModel.class);
		List<Traveler> mockTravelerList = mock(List.class);
		when(mockTravelerList.size()).thenReturn(travelerCount);
		when(mockViewModel.getTravelers()).thenReturn(mockTravelerList);

		when(mockTravelerList.get(anyInt())).thenReturn(new Traveler());
		when(mockViewModel.getTraveler(anyInt())).thenReturn(new Traveler());
		return mockViewModel;
	}

	protected CheckoutTravelerViewModel getMockViewModelIncompleteTravelers(int travelerCount) {
		CheckoutTravelerViewModel mockViewModel = mock(CheckoutTravelerViewModel.class);
		List<Traveler> mockTravelerList = mock(List.class);
		when(mockTravelerList.size()).thenReturn(travelerCount);
		when(mockViewModel.getTravelers()).thenReturn(mockTravelerList);

		when(mockTravelerList.get(anyInt())).thenReturn(getIncompleteTraveler());
		when(mockViewModel.getTraveler(anyInt())).thenReturn(getIncompleteTraveler());
		return mockViewModel;
	}

	protected CheckoutTravelerViewModel getMockViewModelValidTravelers(int travelerCount) {
		CheckoutTravelerViewModel mockViewModel = mock(CheckoutTravelerViewModel.class);
		List<Traveler> mockTravelerList = mock(List.class);
		when(mockTravelerList.size()).thenReturn(travelerCount);
		when(mockViewModel.getTravelers()).thenReturn(mockTravelerList);

		when(mockTravelerList.get(anyInt())).thenReturn(getValidTraveler());
		when(mockViewModel.getTraveler(anyInt())).thenReturn(getValidTraveler());
		when(mockViewModel.validateTravelersComplete()).thenReturn(true);
		return mockViewModel;
	}

	protected Traveler getIncompleteTraveler() {
		Traveler validTraveler = new Traveler();
		validTraveler.setFirstName(testFirstName);
		return validTraveler;
	}

	protected Traveler getValidTraveler() {
		Traveler validTraveler = new Traveler();
		validTraveler.setFirstName(testFirstName);
		validTraveler.setLastName(testLastName);
		validTraveler.setGender(Traveler.Gender.MALE);
		validTraveler.setPhoneNumber(testPhone);
		validTraveler.setBirthDate(LocalDate.now().minusYears(18));

		return validTraveler;
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
