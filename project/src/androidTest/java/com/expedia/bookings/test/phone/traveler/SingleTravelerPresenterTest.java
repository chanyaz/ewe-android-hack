package com.expedia.bookings.test.phone.traveler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUser;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.phone.packages.PackageScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
public class SingleTravelerPresenterTest extends BaseTravelerPresenterTestHelper {
	@Rule
	public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

	@Test
	public void testTransitions() {
		addTravelerToDb(new Traveler());

		mockViewModel = getMockViewModelEmptyTravelers(1);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_entry_widget);
	}

	@Test
	public void testTravelerEntryPersists() {
		mockViewModel = getMockViewModelEmptyTravelers(1);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_entry_widget);

		enterValidTraveler();
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_default_state);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_entry_widget);
		assertValidTravelerFields();
	}

	@Test
	public void testTravelerValidEntry() {
		mockViewModel = getMockViewModelValidTravelers(1);
		testTravelerPresenter.setViewModel(mockViewModel);
		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUser.clickOnView(R.id.edit_phone_number);
		PackageScreen.clickTravelerDone();
		EspressoUtils.assertContainsImageDrawable(R.id.traveler_status_icon, R.drawable.validated);
	}

	@Test
	public void testIncompleteTraveler() throws Throwable {
		mockViewModel = getMockViewModelIncompleteTravelers(1);
		testTravelerPresenter.setViewModel(mockViewModel);
		EspressoUser.clickOnView(R.id.traveler_default_state);

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelerPresenter.back();
			}
		});

		Common.delay(1);
		EspressoUtils.assertContainsImageDrawable(R.id.traveler_status_icon, R.drawable.invalid);
	}

	@Test
	public void testEntryDirtyState() throws Throwable {
		mockViewModel = getMockViewModelEmptyTravelers(1);
		testTravelerPresenter.setViewModel(mockViewModel);
		EspressoUser.clickOnView(R.id.traveler_default_state);

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelerPresenter.back();
			}
		});

		Common.delay(1);
		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewHasCompoundDrawable(R.id.first_name_input, R.drawable.ic_error_blue);
	}

	@Test
	public void testStoredTravelerWorks() throws Throwable {
		mockViewModel = getMockViewModelEmptyTravelers(1);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelerPresenter.getTravelerEntryWidget().getViewModel().getShowPassportCountryObservable().onNext(true);
				testTravelerPresenter.getTravelerEntryWidget().onTravelerChosen(makeStoredTraveler());
			}
		});

		EspressoUtils.assertViewWithTextIsDisplayed(R.id.first_name_input, testFirstName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.middle_name_input, testMiddleName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.last_name_input, testLastName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_phone_number, testPhone);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_birth_date_text_btn, testBirthDay);
 		onView(allOf(withSpinnerText(testPassport)));
	}

	@Test
	public void testBoardingWarningVisible() {
		mockViewModel = getMockViewModelValidTravelers(1);
		testTravelerPresenter.setViewModel(mockViewModel);
		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.boarding_warning, R.string.name_must_match_warning);
	}

	@Test
	public void testBoardingWarningCleared() {
		mockViewModel = getMockViewModelValidTravelers(1);
		testTravelerPresenter.setViewModel(mockViewModel);
		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.boarding_warning, R.string.name_must_match_warning);

		PackageScreen.clickTravelerDone();
		EspressoUtils.assertViewIsNotDisplayed(R.id.boarding_warning);
	}
}
