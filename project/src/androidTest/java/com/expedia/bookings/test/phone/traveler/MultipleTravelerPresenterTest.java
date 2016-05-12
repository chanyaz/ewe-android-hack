package com.expedia.bookings.test.phone.traveler;

import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUser;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.phone.packages.PackageScreen;

import rx.observers.TestSubscriber;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withImageDrawable;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class MultipleTravelerPresenterTest extends BaseTravelerPresenterTestHelper {
	@Rule
	public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

	@Test
	public void testMultipleTravelerFlow() {
		mockViewModel = getMockViewModelEmptyTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedTravelerOneText);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedTravelerTwoText);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedMainText);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedAdditionalText);
	}

	@Test
	public void testMultipleTravelerEntryPersists() {
		mockViewModel = getMockViewModelEmptyTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUser.clickOnText(expectedTravelerOneText);
		EspressoUser.clickOnView(R.id.edit_phone_number);
		enterValidTraveler();
		EspressoUtils.assertViewWithTextIsDisplayed(testName.getFullName());
		EspressoUser.clickOnText(testName.getFullName());

		assertValidTravelerFields();
	}

	@Test
	public void testAllTravelersValidEntryToDefault() {
		mockViewModel = getMockViewModelValidTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUser.clickOnText(expectedTravelerOneText);
		EspressoUser.clickOnView(R.id.edit_phone_number);
		PackageScreen.clickTravelerDone();

		EspressoUtils.assertViewIsDisplayed(R.id.traveler_default_state);
		EspressoUtils.assertContainsImageDrawable(R.id.traveler_status_icon, R.drawable.validated);
	}

	@Test
	public void testIncompleteTravelerState() throws Throwable {
		mockViewModel = getMockViewModelIncompleteTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUser.clickOnText(expectedTravelerOneText);

		travelerPresenterBack();

		Common.delay(2);
		checkOscarInvalid(R.id.traveler_status_icon, R.drawable.invalid, testFirstName);
	}

	private void checkOscarInvalid(@IdRes int viewId, @DrawableRes int drawableId, String siblingText) {
		onView(allOf(withId(viewId),
			hasSibling(
				withChild(allOf(withId(R.id.primary_details_text), withText(siblingText)))
			))).check(matches(withImageDrawable(drawableId)));
	}

	@Test
	public void testToolbar() throws Throwable {
		mockViewModel = getMockViewModelEmptyTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);

		TestSubscriber testSubscriber = new TestSubscriber(1);
		testTravelerPresenter.getToolbarTitleSubject().subscribe(testSubscriber);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUser.clickOnText(expectedTravelerOneText);

		assertEquals(true, testTravelerPresenter.getTravelerEntryWidget().getNameEntryView().getFirstName().hasFocus());
		PackageScreen.enterFirstName(testFirstName);
		PackageScreen.clickTravelerDone();

		assertEquals(false, testTravelerPresenter.getTravelerEntryWidget().getNameEntryView().getMiddleName().hasFocus());

		assertEquals(true, testTravelerPresenter.getTravelerEntryWidget().getNameEntryView().getLastName().hasFocus());
		PackageScreen.enterLastName(testLastName);
		PackageScreen.clickTravelerDone();

		assertEquals(true, testTravelerPresenter.getTravelerEntryWidget().getPhoneEntryView().getPhoneNumber().hasFocus());
		//skip phone number, assert that focus went back to first name
		PackageScreen.selectBirthDate(1989,6,9);
		PackageScreen.clickTravelerDone();

		assertEquals(true, testTravelerPresenter.getTravelerEntryWidget().getNameEntryView().getFirstName().hasFocus());
		PackageScreen.clickTravelerDone();
		assertEquals(true, testTravelerPresenter.getTravelerEntryWidget().getNameEntryView().getLastName().hasFocus());
		PackageScreen.clickTravelerDone();
		assertEquals(true, testTravelerPresenter.getTravelerEntryWidget().getPhoneEntryView().getPhoneNumber().hasFocus());
		PackageScreen.enterPhoneNumber(testPhone);
		PackageScreen.clickTravelerDone();

		assertEquals("Traveler details", testSubscriber.getOnNextEvents().get(0));
		assertEquals("Edit Traveler 1 (Adult)", testSubscriber.getOnNextEvents().get(1));
		assertEquals("Traveler details", testSubscriber.getOnNextEvents().get(2));

		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUtils.assertViewWithTextIsDisplayed(testFirstName + " " + testLastName);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedTravelerTwoText);
	}


	@Test
	public void testEmptyChildInfantTraveler() {
		List<Integer> children = Arrays.asList(1);
		mockViewModel = getMockViewModelEmptyTravelersWithInfant(2, children, true);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedTravelerOneText);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedTravelerTwoText);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedTravelerInfantText);
	}

	@Test
	public void testInvalidChildInfantTraveler() throws Throwable {
		List<Integer> children = Arrays.asList(1);
		mockViewModel = getMockViewModelEmptyTravelersWithInfant(2, children, true);
		Traveler child1 = Db.getTravelers().get(2);
		setChildTraveler(child1, 10);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUser.clickOnText(expectedTravelerInfantText);
		travelerPresenterBack();

		Common.delay(2);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		checkOscarInvalid(R.id.traveler_status_icon, R.drawable.invalid, testChildFullName);

	}

	@Test
	public void testValidChildInfantTraveler() {
		List<Integer> children = Arrays.asList(1);
		mockViewModel = getMockViewModelEmptyTravelersWithInfant(2, children, true);
		Traveler child1 = Db.getTravelers().get(2);
		setChildTraveler(child1, 1);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUser.clickOnText(expectedTravelerInfantText);
		PackageScreen.clickTravelerDone();
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		checkOscarInvalid(R.id.traveler_status_icon, R.drawable.validated, testChildFullName);
	}


	@Test
	public void testValidChildTravelers() {
		List<Integer> children = Arrays.asList(10);
		mockViewModel = getMockViewModelEmptyTravelersWithInfant(2, children, false);
		Traveler child1 = Db.getTravelers().get(2);
		setChildTraveler(child1, 10);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUser.clickOnText(expectedTravelerChildText);
		PackageScreen.clickTravelerDone();
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		checkOscarInvalid(R.id.traveler_status_icon, R.drawable.validated, testChildFullName);
	}

	@Test
	public void testInvalidChildTravelers() throws Throwable {
		List<Integer> children = Arrays.asList(10);
		mockViewModel = getMockViewModelEmptyTravelersWithInfant(2, children, false);
		Traveler child1 = Db.getTravelers().get(2);
		setChildTraveler(child1, 1);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUser.clickOnText(expectedTravelerChildText);
		travelerPresenterBack();

		Common.delay(2);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		checkOscarInvalid(R.id.traveler_status_icon, R.drawable.invalid, testChildFullName);
	}

	@Test
	public void testBoardingWarning() {
		mockViewModel = getMockViewModelEmptyTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUtils.assertViewIsNotDisplayed(R.id.boarding_warning);

		EspressoUser.clickOnText(expectedTravelerOneText);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.boarding_warning, R.string.name_must_match_warning_new);
	}

	@Test
	public void testBoardingWarningCleared() {
		mockViewModel = getMockViewModelValidTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUtils.assertViewIsNotDisplayed(R.id.boarding_warning);

		EspressoUser.clickOnText(expectedTravelerOneText);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.boarding_warning, R.string.name_must_match_warning_new);
		PackageScreen.clickTravelerDone();
		EspressoUtils.assertViewIsNotDisplayed(R.id.boarding_warning);
	}

	@Test
	public void testPassportIndependent() throws Throwable {
		mockViewModel = getMockViewModelValidTravelers(2);
		generateMockTripWithPassport();
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUser.clickOnText(expectedTravelerOneText);

		EspressoUser.scrollToView(R.id.passport_country_spinner);
		EspressoUser.clickOnView(R.id.passport_country_spinner);
		EspressoUtils.assertViewWithTextIsDisplayed("United States");
		onView(withText("United States")).check(matches(isChecked()));
		EspressoUser.clickOnText("Uruguay");

		EspressoUser.scrollToView(R.id.passport_country_spinner);
		EspressoUtils.assertViewWithTextIsDisplayed("Passport: Uruguay");

		travelerPresenterBack();
		Common.delay(1);

		EspressoUser.clickOnText(expectedTravelerTwoText);
		EspressoUser.scrollToView(R.id.passport_country_spinner);
		EspressoUtils.assertViewWithTextIsDisplayed("Passport: United States");
		onView(withText("Passport: Uruguay")).check(doesNotExist());
	}

	private void travelerPresenterBack() throws Throwable {
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelerPresenter.back();
			}
		});
	}
	
	public void testStoredTravelerButtonActions() throws Throwable {
		mockViewModel = getMockViewModelEmptyTravelers(1);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelerPresenter.getTravelerEntryWidget().onAddNewTravelerSelected();
			}
		});

		EspressoUtils.assertViewWithTextIsDisplayed(R.id.first_name_input, "");
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.middle_name_input, "");
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.last_name_input, "");
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_phone_number, "");
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_birth_date_text_btn, "");
	}
}
