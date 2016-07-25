package com.expedia.bookings.test.phone.traveler;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.rule.UiThreadTestRule;
import android.view.View;
import android.view.ViewStub;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.TravelerName;
import com.expedia.bookings.data.flights.FlightTripDetails;
import com.expedia.bookings.data.flights.ValidFormOfPayment;
import com.expedia.bookings.data.packages.PackageCreateTripResponse;
import com.expedia.bookings.data.packages.PackageSearchParams;
import com.expedia.bookings.data.trips.TripBucket;
import com.expedia.bookings.data.trips.TripBucketItemPackages;
import com.expedia.bookings.enums.PassengerCategory;
import com.expedia.bookings.presenter.packages.TravelerPresenter;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.phone.packages.PackageScreen;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.validation.TravelerValidator;
import com.expedia.bookings.widget.CheckoutToolbar;
import com.expedia.bookings.widget.traveler.TravelerDefaultState;
import com.expedia.vm.CheckoutToolbarViewModel;
import com.expedia.vm.traveler.CheckoutTravelerViewModel;
import com.expedia.vm.traveler.TravelerSummaryViewModel;
import com.squareup.phrase.Phrase;

import kotlin.Unit;
import rx.Observer;

import static org.mockito.Mockito.mock;

public class BaseTravelerPresenterTestHelper {

	@Rule
	public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

	protected TravelerPresenter testTravelerPresenter;
	protected TravelerDefaultState testTravelerDefault;
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
	protected final String testPassport = "Passport: Vietnam";
	protected final String testGender = "Gender";

	protected final String expectedMainText = "Main Traveler";
	protected final String expectedAdditionalText = "Additional Travelers";
	protected final String expectedTravelerOneText = Phrase.from(InstrumentationRegistry.getTargetContext()
		.getString(R.string.checkout_edit_traveler_TEMPLATE)).put("travelernumber", 1).put("passengerage", "Adult").format().toString();
	protected final String expectedTravelerTwoText = Phrase.from(InstrumentationRegistry.getTargetContext()
		.getString(R.string.checkout_edit_traveler_TEMPLATE)).put("travelernumber", 2).put("passengerage", "Adult").format().toString();
	protected final String expectedTravelerChildText = Phrase.from(InstrumentationRegistry.getTargetContext()
		.getString(R.string.checkout_edit_traveler_TEMPLATE)).put("travelernumber", 3).put("passengerage", "10 year old").format().toString();
	protected final String expectedTravelerInfantText = Phrase.from(InstrumentationRegistry.getTargetContext()
		.getString(R.string.checkout_edit_traveler_TEMPLATE)).put("travelernumber", 3).put("passengerage", "1 year old").format().toString();

	private Context context = InstrumentationRegistry.getTargetContext();

	@Rule
	public PlaygroundRule activityTestRule = new PlaygroundRule(R.layout.test_traveler_presenter, R.style.V2_Theme_Packages);

	@Before
	public void setUp() throws Throwable {
		Context context = InstrumentationRegistry.getTargetContext();
		Ui.getApplication(context).defaultTravelerComponent();
		TravelerValidator travelerValidator = Ui.getApplication(context).travelerComponent().travelerValidator();
		travelerValidator.updateForNewSearch(setPackageParams(1));

		final ViewStub viewStub = (ViewStub) activityTestRule.getRoot().findViewById(R.id.traveler_presenter_stub);
		testTravelerDefault = (TravelerDefaultState) activityTestRule.getRoot()
			.findViewById(R.id.traveler_default_state);
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelerPresenter = (TravelerPresenter) viewStub.inflate();
				testTravelerDefault.setViewModel(new TravelerSummaryViewModel(activityTestRule.getActivity()));
			}
		});

		testToolbar = (CheckoutToolbar) activityTestRule.getRoot().findViewById(R.id.checkout_toolbar);
		testToolbar.setViewModel(new CheckoutToolbarViewModel(activityTestRule.getActivity()));
		testTravelerPresenter.getToolbarTitleSubject().subscribe(testToolbar.getViewModel().getToolbarTitle());
		testTravelerPresenter.getTravelerEntryWidget().getFocusedView().subscribe(testToolbar.getViewModel().getCurrentFocus());
		testTravelerPresenter.getTravelerEntryWidget().getFilledIn().subscribe(testToolbar.getViewModel().getFormFilledIn());
		testTravelerPresenter.getMenuVisibility().subscribe(testToolbar.getViewModel().getMenuVisibility());
		testToolbar.getViewModel().getDoneClicked().subscribe(testTravelerPresenter.getDoneClicked());
		testName.setFirstName(testFirstName);
		testName.setLastName(testLastName);

		testTravelerDefault.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				testTravelerPresenter.setVisibility(View.VISIBLE);
				testTravelerPresenter.showSelectOrEntryState(testTravelerDefault.getStatus());
			}
		});

		testTravelerPresenter.getCloseSubject().subscribe(new Observer<Unit>() {
			@Override
			public void onCompleted() {

			}

			@Override
			public void onError(Throwable e) {

			}

			@Override
			public void onNext(Unit unit) {
				testTravelerPresenter.setVisibility(View.GONE);
			}
		});
	}

	protected void enterValidTraveler() {
		PackageScreen.enterFirstName(testFirstName);
		PackageScreen.enterLastName(testLastName);
		PackageScreen.enterPhoneNumber(testPhone);
		Espresso.closeSoftKeyboard();
		Common.delay(1);
		PackageScreen.selectBirthDate(1991, 1, 27);
		PackageScreen.clickTravelerDone();
	}

	protected PackageSearchParams setPackageParams(int adults) {
		return setPackageParams(adults, new ArrayList<Integer>(), false);
	}

	protected PackageSearchParams setPackageParams(int adults, List<Integer> children, boolean infantsInLap) {
		PackageSearchParams packageParams = (PackageSearchParams) new PackageSearchParams.Builder(12, 329)
			.infantSeatingInLap(infantsInLap)
			.startDate(LocalDate.now().plusDays(1))
			.endDate(LocalDate.now().plusDays(2))
			.origin(new SuggestionV4())
			.destination(new SuggestionV4())
			.adults(adults)
			.children(children)
			.build();
		Db.setPackageParams(packageParams);
		return packageParams;
	}

	private void setDbTravelers(int adults, List<Integer> children, boolean infantsInLap) {
		List<Traveler> travelers = new ArrayList<>();
		for (int i = 0; i < adults; i++) {
			Traveler adultTraveler = new Traveler();
			adultTraveler.setPassengerCategory(PassengerCategory.ADULT);
			adultTraveler.setGender(Traveler.Gender.MALE);
			adultTraveler.setSearchedAge(-1);
			travelers.add(adultTraveler);
		}
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				Traveler childTraveler = new Traveler();
				childTraveler.setGender(Traveler.Gender.GENDER);
				int age = children.get(i);
				if (age < 2) {
					if (infantsInLap) {
						childTraveler.setPassengerCategory(PassengerCategory.INFANT_IN_LAP);
					}
					else {
						childTraveler.setPassengerCategory(PassengerCategory.INFANT_IN_SEAT);
					}
				}
				else if (age < 12) {
					childTraveler.setPassengerCategory(PassengerCategory.CHILD);
				}
				else if (age < 18) {
					childTraveler.setPassengerCategory(PassengerCategory.ADULT_CHILD);
				}
				childTraveler.setSearchedAge(age);
				travelers.add(childTraveler);
			}
		}
		Db.setTravelers(travelers);
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
		CheckoutTravelerViewModel mockViewModel = getMockviewModel();
		setPackageParams(adultCount, children, infantsInLap);
		setDbTravelers(adultCount, children, infantsInLap);
		return mockViewModel;
	}

	protected CheckoutTravelerViewModel getMockViewModelEmptyTravelers(int travelerCount) {
		CheckoutTravelerViewModel mockViewModel = getMockviewModel();
		setPackageParams(travelerCount);
		setDbTravelers(travelerCount, null, false);
		return mockViewModel;
	}

	protected CheckoutTravelerViewModel getMockViewModelIncompleteTravelers(int travelerCount) {
		CheckoutTravelerViewModel mockViewModel = getMockviewModel();
		setPackageParams(travelerCount);
		setDbTravelers(travelerCount, null, false);
		for (int i = 0; i < travelerCount; i++) {
			Traveler traveler = Db.getTravelers().get(i);
			setIncompleteTraveler(traveler);
		}
		return mockViewModel;
	}

	protected CheckoutTravelerViewModel getMockViewModelValidTravelers(int travelerCount) {
		CheckoutTravelerViewModel mockViewModel = getMockviewModel();
		setPackageParams(travelerCount);
		setDbTravelers(travelerCount, null, false);
		for (int i = 0; i < travelerCount; i++) {
			Traveler traveler = Db.getTravelers().get(i);
			setValidTraveler(traveler);
		}
		return mockViewModel;
	}

	protected CheckoutTravelerViewModel getMockviewModel() {
		CheckoutTravelerViewModel mockViewModel = new CheckoutTravelerViewModel(context);
		mockViewModel.getTravelerCompletenessStatus().subscribe(testTravelerDefault.getViewModel().getTravelerStatusObserver());
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
		storedTraveler.addPassportCountry("VNM");
		storedTraveler.setBirthDate(LocalDate.now().withYear(1991).withMonthOfYear(1).withDayOfMonth(27));
		return storedTraveler;
	}

	protected void generateMockTripWithPassport() {
		PackageCreateTripResponse mockCreateTrip = mock(PackageCreateTripResponse.class);
		mockCreateTrip.setValidFormsOfPayment(new ArrayList<ValidFormOfPayment>());

		TripBucketItemPackages mockPackagesItem = new TripBucketItemPackages(mockCreateTrip);

		PackageCreateTripResponse.PackageDetails mockPackageDetails = new PackageCreateTripResponse.PackageDetails();
		mockCreateTrip.packageDetails = mockPackageDetails;

		PackageCreateTripResponse.FlightProduct mockFlightProduct = new PackageCreateTripResponse.FlightProduct();
		mockPackageDetails.flight = mockFlightProduct;

		FlightTripDetails mockFlightDetails = new FlightTripDetails();
		mockFlightProduct.details = mockFlightDetails;

		FlightTripDetails.FlightOffer mockFlightOffer = new FlightTripDetails.FlightOffer();
		mockFlightDetails.offer = mockFlightOffer;

		mockFlightOffer.isInternational = true;

		TripBucket dbTripBucket = Db.getTripBucket();
		dbTripBucket.add(mockPackagesItem);
		Db.saveTripBucket(InstrumentationRegistry.getTargetContext());
	}
}
