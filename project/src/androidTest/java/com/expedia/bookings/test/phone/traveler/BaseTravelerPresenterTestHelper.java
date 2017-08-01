package com.expedia.bookings.test.phone.traveler;

import com.expedia.bookings.test.pagemodels.common.TravelerModel.TravelerDetails;
import com.expedia.vm.traveler.FlightTravelersViewModel;
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
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.TravelerName;
import com.expedia.bookings.data.packages.PackageSearchParams;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.PassengerCategory;
import com.expedia.bookings.presenter.packages.AbstractTravelersPresenter;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.validation.TravelerValidator;
import com.expedia.bookings.widget.CheckoutToolbar;
import com.expedia.bookings.widget.traveler.TravelerSummaryCard;
import com.expedia.vm.CheckoutToolbarViewModel;
import com.expedia.vm.traveler.TravelersViewModel;
import com.expedia.vm.traveler.TravelerSummaryViewModel;
import com.squareup.phrase.Phrase;
import kotlin.Unit;
import io.reactivex.Observer;

public class BaseTravelerPresenterTestHelper {

	@Rule
	public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

	protected AbstractTravelersPresenter testTravelersPresenter;
	protected TravelerSummaryCard testTravelerDefault;
	private CheckoutToolbar testToolbar;
	protected TravelersViewModel mockViewModel;

	protected TravelerName testName = new TravelerName();
	protected final String testChildFullName = "Oscar Grouch Jr.";
	protected final String testFirstName = "Oscar";
	protected final String testUpdatedFirstName = "UpdatedOscar";
	protected final String testMiddleName = "T";
	protected final String testLastName = "Grouch";
	protected final String testEmail = "Grouch@gmail.com";
	protected final String testChildLastName = "Grouch Jr.";
	protected final String testPhone = "7732025862";
	protected final String testBirthDay = "Jan 27, 1991";
	protected final String testPassport = "Passport: Vietnam";
	protected final String testEmptyPassport = "Passport: Country";
	protected final String testGender = "Gender";

	protected final String expectedMainText = "Main Traveler";
	protected final String expectedAdditionalText = "Additional Travelers";
	protected final String expectedTravelerOneText = Phrase.from(InstrumentationRegistry.getTargetContext()
		.getString(R.string.checkout_edit_traveler_TEMPLATE)).put("travelernumber", 1).put("passengerage", "Adult")
		.format().toString();
	protected final String expectedTravelerTwoText = Phrase.from(InstrumentationRegistry.getTargetContext()
		.getString(R.string.checkout_edit_traveler_TEMPLATE)).put("travelernumber", 2).put("passengerage", "Adult")
		.format().toString();

	protected final String expectedFilledTravelerOneText = testFirstName + " " + testLastName;
	protected final String expectedFilledTravelerTwoText = expectedFilledTravelerOneText + 1;
	protected final String expectedIncompleteTravelerOneText = testFirstName;

	protected final String expectedFilledTravelerChildText = testChildFullName;

	protected final String expectedTravelerInfantText = Phrase.from(InstrumentationRegistry.getTargetContext()
		.getString(R.string.checkout_edit_traveler_TEMPLATE)).put("travelernumber", 3).put("passengerage", "1 year old")
		.format().toString();

	private Context context = InstrumentationRegistry.getTargetContext();
	protected String pointOfSaleCountry = context.getString(PointOfSale.getPointOfSale().getCountryNameResId());

	@Rule
	public PlaygroundRule activityTestRule = new PlaygroundRule(R.layout.test_traveler_presenter,
		R.style.V2_Theme_Packages);

	@Before
	public void setUp() throws Throwable {
		Context context = InstrumentationRegistry.getTargetContext();
		Ui.getApplication(context).defaultTravelerComponent();
		TravelerValidator travelerValidator = Ui.getApplication(context).travelerComponent().travelerValidator();
		travelerValidator.updateForNewSearch(setPackageParams(1));

		final ViewStub viewStub = (ViewStub) activityTestRule.getRoot().findViewById(R.id.traveler_presenter_stub);
		testTravelerDefault = (TravelerSummaryCard) activityTestRule.getRoot()
			.findViewById(R.id.traveler_default_state);
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelersPresenter = (AbstractTravelersPresenter) viewStub.inflate();
				testTravelerDefault.setViewModel(new TravelerSummaryViewModel(activityTestRule.getActivity()));
			}
		});

		testToolbar = (CheckoutToolbar) activityTestRule.getRoot().findViewById(R.id.checkout_toolbar);
		testToolbar.setViewModel(new CheckoutToolbarViewModel(activityTestRule.getActivity()));
		testTravelersPresenter.getToolbarTitleSubject().subscribe(testToolbar.getViewModel().getToolbarTitle());
		testTravelersPresenter.getTravelerEntryWidget().getFocusedView()
			.subscribe(testToolbar.getViewModel().getCurrentFocus());
		testTravelersPresenter.getMenuVisibility().subscribe(testToolbar.getViewModel().getMenuVisibility());
		testToolbar.getViewModel().getDoneClicked().subscribe(testTravelersPresenter.getDoneClicked());
		testName.setFirstName(testFirstName);
		testName.setLastName(testLastName);

		testTravelerDefault.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				testTravelersPresenter.setVisibility(View.VISIBLE);
				testTravelersPresenter.resetTravelers();
				testTravelersPresenter.showSelectOrEntryState();
			}
		});

		testTravelersPresenter.getCloseSubject().subscribe(new Observer<Unit>() {
			@Override
			public void onCompleted() {

			}

			@Override
			public void onError(Throwable e) {

			}

			@Override
			public void onNext(Unit unit) {
				testTravelersPresenter.setVisibility(View.GONE);
			}
		});
	}

	protected void enterValidTraveler(boolean withPhoneNumber) {
		enterValidTraveler(withPhoneNumber, true);
	}

	protected void enterValidTraveler(boolean withPhoneNumber, boolean withEmail) {
		TravelerDetails.enterFirstName(testFirstName);
		TravelerDetails.enterLastName(testLastName);
		if (withEmail) {
			TravelerDetails.enterEmail(testEmail);
		}
		Espresso.closeSoftKeyboard();
		if (withPhoneNumber) {
			TravelerDetails.enterPhoneNumber(testPhone);
		}
		Espresso.closeSoftKeyboard();
		Common.delay(1);
		TravelerDetails.selectBirthDate(1991, 1, 27);
		TravelerDetails.clickDone();
		Espresso.closeSoftKeyboard();
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
		Espresso.closeSoftKeyboard();
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.first_name_input, testFirstName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.last_name_input, testLastName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_phone_number, testPhone);
	}

	protected TravelersViewModel getMockViewModelEmptyTravelersWithInfant(int adultCount, List<Integer> children,
		boolean infantsInLap) {
		TravelersViewModel mockViewModel = getMockviewModel();
		setPackageParams(adultCount, children, infantsInLap);
		setDbTravelers(adultCount, children, infantsInLap);
		return mockViewModel;
	}

	protected TravelersViewModel getMockViewModelEmptyTravelers(int travelerCount) {
		TravelersViewModel mockViewModel = getMockviewModel();
		setPackageParams(travelerCount);
		setDbTravelers(travelerCount, null, false);
		return mockViewModel;
	}

	protected TravelersViewModel getMockViewModelIncompleteTravelers(int travelerCount) {
		TravelersViewModel mockViewModel = getMockviewModel();
		setPackageParams(travelerCount);
		setDbTravelers(travelerCount, null, false);
		for (int i = 0; i < travelerCount; i++) {
			Traveler traveler = Db.getTravelers().get(i);
			setIncompleteTraveler(traveler, i);
		}
		return mockViewModel;
	}

	protected TravelersViewModel getMockViewModelValidTravelers(int travelerCount) {
		TravelersViewModel mockViewModel = getMockviewModel();
		setPackageParams(travelerCount);
		setDbTravelers(travelerCount, null, false);
		for (int i = 0; i < travelerCount; i++) {
			Traveler traveler = Db.getTravelers().get(i);
			setValidTraveler(traveler, i);
		}
		return mockViewModel;
	}

	protected TravelersViewModel getMockviewModel(boolean showMainTravelerMinAge) {
		TravelersViewModel mockViewModel = new FlightTravelersViewModel(context, LineOfBusiness.PACKAGES,
			showMainTravelerMinAge);
		mockViewModel.getTravelersCompletenessStatus()
			.subscribe(testTravelerDefault.getViewModel().getTravelerStatusObserver());
		return mockViewModel;
	}

	protected TravelersViewModel getMockviewModel() {
		return getMockviewModel(false);
	}

	protected void setIncompleteTraveler(Traveler validTraveler, int index) {
		if (index == 0) {
			validTraveler.setFirstName(testFirstName);
		}
		else {
			validTraveler.setFirstName(testFirstName + index);
		}
	}

	protected void setValidTraveler(Traveler validTraveler, int index) {
		validTraveler.setFirstName(testFirstName);
		if (index == 0) {
			validTraveler.setLastName(testLastName);
		}
		else {
			validTraveler.setLastName(testLastName + index);
		}
		validTraveler.setEmail(testEmail);
		validTraveler.setGender(Traveler.Gender.MALE);
		validTraveler.setPhoneNumber(testPhone);
		validTraveler.setBirthDate(LocalDate.now().minusYears(18));
	}

	protected void setChildTraveler(Traveler childTraveler, int yearsOld) {
		childTraveler.setFirstName(testFirstName);
		childTraveler.setLastName(testChildLastName);
		childTraveler.setEmail(testEmail);
		childTraveler.setGender(Traveler.Gender.MALE);
		childTraveler.setPhoneNumber(testPhone);
		childTraveler.setBirthDate(LocalDate.now().minusYears(yearsOld));
	}

	protected Traveler makeStoredTraveler(String passport) {
		Traveler storedTraveler = new Traveler();
		storedTraveler.setFirstName(testFirstName);
		storedTraveler.setMiddleName(testMiddleName);
		storedTraveler.setLastName(testLastName);
		storedTraveler.setEmail(testEmail);
		storedTraveler.setGender(Traveler.Gender.MALE);
		storedTraveler.setPhoneNumber(testPhone);
		storedTraveler.addPassportCountry(passport);
		storedTraveler.setBirthDate(LocalDate.now().withYear(1991).withMonthOfYear(1).withDayOfMonth(27));
		return storedTraveler;
	}

	protected void setTravelerViewModelForEmptyTravelers(final int travelerCount) throws Throwable {
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mockViewModel = getMockViewModelEmptyTravelers(travelerCount);
				testTravelersPresenter.setViewModel(mockViewModel);
			}
		});
	}

	protected void setTravelerViewModelForValidTravelers(final int travelerCount) throws Throwable {
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mockViewModel = getMockViewModelValidTravelers(travelerCount);
				testTravelersPresenter.setViewModel(mockViewModel);
			}
		});
	}
}
