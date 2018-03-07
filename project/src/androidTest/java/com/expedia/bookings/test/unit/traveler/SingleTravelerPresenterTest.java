package com.expedia.bookings.test.unit.traveler;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.Espresso;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUser;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.pagemodels.common.TravelerModel.TravelerDetails;
import com.expedia.vm.traveler.FlightTravelerEntryWidgetViewModel;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class SingleTravelerPresenterTest extends BaseTravelerPresenterTestHelper {

	@Test
	public void testTransitions() throws Throwable {
		addTravelerToDb(new Traveler());
		setTravelerViewModelForEmptyTravelers(1);
		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_entry_widget);
		EspressoUtils.assertViewIsNotDisplayed(R.id.traveler_picker_widget);

		activityTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelersPresenter.back();
			}
		});

		EspressoUtils.assertViewIsDisplayed(R.id.traveler_default_state);
		EspressoUtils.assertViewIsNotDisplayed(R.id.traveler_picker_widget);
	}

	@Test
	public void testTravelerEntryPersists() throws Throwable {
		setTravelerViewModelForEmptyTravelers(1);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_entry_widget);

		enterValidTraveler(true);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_default_state);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_entry_widget);
		assertValidTravelerFields();
	}

	@Test
	public void testTravelerValidEntry() throws Throwable {
		setTravelerViewModelForValidTravelers(1);
		EspressoUser.clickOnView(R.id.traveler_default_state);
		Espresso.closeSoftKeyboard();
		EspressoUser.clickOnView(R.id.edit_phone_number);
		TravelerDetails.clickDone();
		String today = new LocalDate().minusYears(18).toString("MM/dd/yyyy");
		assertEquals("Oscar Grouch, " + today + ", traveler details complete. Button.", testTravelerDefault.getContentDescription());
		EspressoUtils.assertContainsImageDrawable(R.id.traveler_status_icon, R.drawable.validated);
	}

	//ToDO: commented out this flaky test
//	@Test
//	public void testTravelerCardContentDescription () throws Throwable {
//		uiThreadTestRule.runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				mockViewModel = getMockViewModelEmptyTravelers(1);
//				testTravelersPresenter.setViewModel(mockViewModel);
//				mockViewModel.getTravelersCompletenessStatus().onNext(TravelerCheckoutStatus.CLEAN);
//				Common.delay(1);
//
//				assertEquals("Enter traveler details Button", testTravelerDefault.getContentDescription().toString());
//
//				mockViewModel = getMockViewModelIncompleteTravelers(1);
//				mockViewModel.getTravelersCompletenessStatus().onNext(TravelerCheckoutStatus.DIRTY);
//				Common.delay(1);
//
//				assertEquals("Oscar Error: Enter missing traveler details. Button.", testTravelerDefault.getContentDescription());
//
//				mockViewModel = getMockViewModelValidTravelers(1);
//				mockViewModel.getTravelersCompletenessStatus().onNext(TravelerCheckoutStatus.COMPLETE);
//				Common.delay(1);
//				String today = new LocalDate().withYear(1999).toString("MM/dd/yyyy");
//
//				assertEquals("Oscar Grouch, " + today + ", traveler details complete. Button.", testTravelerDefault.getContentDescription());
//			}
//		});
//	}

	@Test
	public void testIncompleteTraveler() throws Throwable {

		activityTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mockViewModel = getMockViewModelIncompleteTravelers(1);
				testTravelersPresenter.setViewModel(mockViewModel);
				testTravelersPresenter.getViewModel().updateCompletionStatus();
			}
		});
		Common.delay(1);
		EspressoUser.clickOnView(R.id.traveler_default_state);
		assertEquals(testTravelerDefault.getContentDescription(), "Oscar Error: Enter missing traveler details. Button.");
		EspressoUtils.assertContainsImageDrawable(R.id.traveler_status_icon, R.id.traveler_default_state, R.drawable.invalid);
	}

	@Test
	public void testEntryDirtyState() throws Throwable {
		setTravelerViewModelForEmptyTravelers(1);
		EspressoUser.clickOnView(R.id.traveler_default_state);
		TravelerDetails.enterLastName(testLastName);
		activityTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelersPresenter.getViewModel().updateCompletionStatus();
			}
		});
		EspressoUser.clickOnView(R.id.traveler_default_state);

		EspressoUtils.assertViewWithTextIsDisplayed(R.id.textinput_error, R.string.first_name_validation_error_message);
	}

	@Test
	public void testStoredTravelerWorks() throws Throwable {
		setTravelerViewModelForEmptyTravelers(1);

		EspressoUser.clickOnView(R.id.traveler_default_state);

		activityTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				((FlightTravelerEntryWidgetViewModel) testTravelersPresenter.getTravelerEntryWidget().getViewModel()).getShowPassportCountryObservable().onNext(true);
				testTravelersPresenter.getTravelerEntryWidget().onTravelerChosen(makeStoredTraveler("VNM"));
			}
		});

		Espresso.closeSoftKeyboard();
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.first_name_input, testFirstName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.middle_name_input, testMiddleName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.last_name_input, testLastName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_phone_number, testPhone);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_birth_date_text_btn, testBirthDay);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.passport_country_btn, testPassport);
	}

	@Test
	public void testStoredTravelerNoPassport() throws Throwable {
		setTravelerViewModelForEmptyTravelers(1);

		EspressoUser.clickOnView(R.id.traveler_default_state);

		activityTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				((FlightTravelerEntryWidgetViewModel) testTravelersPresenter.getTravelerEntryWidget().getViewModel()).getShowPassportCountryObservable().onNext(true);
				testTravelersPresenter.getTravelerEntryWidget().onTravelerChosen(makeStoredTraveler(null));
			}
		});

		Espresso.closeSoftKeyboard();

		EspressoUtils.assertViewWithTextIsDisplayed(R.id.passport_country_btn, "");
	}

	@Test
	public void testBoardingWarningVisible() throws Throwable {
		setTravelerViewModelForValidTravelers(1);
		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.boarding_warning, R.string.name_must_match_warning_new);
	}

	@Test
	public void testBoardingWarningCleared() throws Throwable {
		setTravelerViewModelForValidTravelers(1);
		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.boarding_warning, R.string.name_must_match_warning_new);

		TravelerDetails.clickDone();
		EspressoUtils.assertViewIsNotDisplayed(R.id.boarding_warning);
	}
}
