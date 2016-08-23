package com.expedia.bookings.test.phone.traveler;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.Espresso;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.CustomMatchers;
import com.expedia.bookings.test.espresso.EspressoUser;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.phone.packages.PackageScreen;

import kotlin.Unit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class SingleTravelerPresenterTest extends BaseTravelerPresenterTestHelper {

	@Test
	public void testTransitions() {
		addTravelerToDb(new Traveler());

		mockViewModel = getMockViewModelEmptyTravelers(1);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_entry_widget);
	}

	@Test
	public void testGenderErrorShowsWhenUserEntersIncorrectGender() throws Throwable {
		addTravelerToDb(new Traveler());

		mockViewModel = getMockViewModelEmptyTravelers(1);
		testTravelerPresenter.setViewModel(mockViewModel);
		EspressoUser.clickOnView(R.id.traveler_default_state);
		PackageScreen.enterFirstName(testFirstName);
		PackageScreen.enterLastName(testLastName);
		PackageScreen.enterEmail(testEmail);
		Espresso.closeSoftKeyboard();
		PackageScreen.enterPhoneNumber(testPhone);
		Espresso.closeSoftKeyboard();
		PackageScreen.selectBirthDate(06,20,1990);
		PackageScreen.selectGender(testGender);
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelerPresenter.getDoneClicked().onNext(Unit.INSTANCE);
			}
		});

		onView(CustomMatchers.withCompoundDrawable(R.drawable.invalid)).check(matches(isDisplayed()));
	}

	@Test
	public void testTravelerEntryPersists() {
		mockViewModel = getMockViewModelEmptyTravelers(1);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_entry_widget);

		enterValidTraveler(true);
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
		Espresso.closeSoftKeyboard();
		EspressoUser.clickOnView(R.id.edit_phone_number);
		PackageScreen.clickTravelerDone();
		assertEquals(testTravelerDefault.getContentDescription(),"Traveler Information Complete");
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
				testTravelerPresenter.getViewModel().updateCompletionStatus();
			}
		});
		Common.delay(1);
		assertEquals(testTravelerDefault.getContentDescription(),"Traveler Information Incomplete");
		EspressoUtils.assertContainsImageDrawable(R.id.traveler_status_icon, R.drawable.invalid);
	}

	@Test
	public void testEntryDirtyState() throws Throwable {
		mockViewModel = getMockViewModelEmptyTravelers(1);
		testTravelerPresenter.setViewModel(mockViewModel);
		EspressoUser.clickOnView(R.id.traveler_default_state);
		PackageScreen.enterLastName(testLastName);

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelerPresenter.getViewModel().updateCompletionStatus();
			}
		});

		Common.delay(1);
		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewHasCompoundDrawable(R.id.first_name_input, R.drawable.invalid);
	}

	@Test
	public void testStoredTravelerWorks() throws Throwable {
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mockViewModel = getMockViewModelEmptyTravelers(1);
				testTravelerPresenter.setViewModel(mockViewModel);
			}
		});

		EspressoUser.clickOnView(R.id.traveler_default_state);

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelerPresenter.getTravelerEntryWidget().getViewModel().getShowPassportCountryObservable().onNext(true);
				testTravelerPresenter.getTravelerEntryWidget().onTravelerChosen(makeStoredTraveler("VNM"));
			}
		});

		Espresso.closeSoftKeyboard();
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.first_name_input, testFirstName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.middle_name_input, testMiddleName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.last_name_input, testLastName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_phone_number, testPhone);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_birth_date_text_btn, testBirthDay);
		onView(withId(R.id.passport_country_spinner)).check(matches(hasDescendant(withText(testPassport))));
	}

	@Test
	public void testStoredTravelerNoPassport() throws Throwable {
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mockViewModel = getMockViewModelEmptyTravelers(1);
				testTravelerPresenter.setViewModel(mockViewModel);
			}
		});


		EspressoUser.clickOnView(R.id.traveler_default_state);

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelerPresenter.getTravelerEntryWidget().getViewModel().getShowPassportCountryObservable().onNext(true);
				testTravelerPresenter.getTravelerEntryWidget().onTravelerChosen(makeStoredTraveler(null));
			}
		});

		Espresso.closeSoftKeyboard();
		onView(withId(R.id.passport_country_spinner)).check(matches(hasDescendant(withText(testEmptyPassport))));

	}

	@Test
	public void testBoardingWarningVisible() {
		mockViewModel = getMockViewModelValidTravelers(1);
		testTravelerPresenter.setViewModel(mockViewModel);
		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.boarding_warning, R.string.name_must_match_warning_new);
	}

	@Test
	public void testBoardingWarningCleared() {
		mockViewModel = getMockViewModelValidTravelers(1);
		testTravelerPresenter.setViewModel(mockViewModel);
		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.boarding_warning, R.string.name_must_match_warning_new);

		PackageScreen.clickTravelerDone();
		EspressoUtils.assertViewIsNotDisplayed(R.id.boarding_warning);
	}
}
