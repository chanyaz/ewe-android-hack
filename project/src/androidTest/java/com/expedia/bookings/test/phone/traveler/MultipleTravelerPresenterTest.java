package com.expedia.bookings.test.phone.traveler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.PickerActions;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.enums.TravelerCheckoutStatus;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUser;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.packages.PackageScreen;
import com.expedia.bookings.widget.TextView;
import com.expedia.vm.traveler.CheckoutTravelerViewModel;

import rx.observers.TestSubscriber;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withImageDrawable;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class MultipleTravelerPresenterTest extends BaseTravelerPresenterTestHelper {

	@Test
	public void testMainTravelerMinAgeMessagingShows() {
		boolean mainTravelerMinAge = true;
		CheckoutTravelerViewModel mockViewModel = getMockviewModel(mainTravelerMinAge);
		testTravelerPresenter.setViewModel(mockViewModel);

		TextView mainTravelerMinAgeTextView =
			testTravelerPresenter.getTravelerPickerWidget().getMainTravelerMinAgeTextView();
		assertEquals(View.VISIBLE, mainTravelerMinAgeTextView.getVisibility());
	}
	
	@Test
	public void testTravelerPickerIOB() throws Throwable {
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				CheckoutTravelerViewModel mockViewModel = getMockViewModelEmptyTravelers(3);
				testTravelerPresenter.setViewModel(mockViewModel);
				testTravelerPresenter.showSelectOrEntryState(TravelerCheckoutStatus.CLEAN);
				testTravelerPresenter.getTravelerPickerWidget().show();
				List<Traveler> travelers = new ArrayList<>();
				travelers.add(new Traveler());
				Db.setTravelers(travelers);
				testTravelerPresenter.showSelectOrEntryState(TravelerCheckoutStatus.CLEAN);
				try {
					testTravelerPresenter.getTravelerPickerWidget().show();
				}
				catch (Exception e) {
					Assert.fail("Oops. We shouldn't fail when customer adjusts number of travelers on the search form");
				}
			}
		});
		//No assertion, just dont crash
	}

	@Test
	public void testMainTravelerMinAgeMessagingHidden() {
		boolean mainTravelerMinAge = false;
		CheckoutTravelerViewModel mockViewModel = getMockviewModel(mainTravelerMinAge);
		testTravelerPresenter.setViewModel(mockViewModel);

		TextView mainTravelerMinAgeTextView =
			testTravelerPresenter.getTravelerPickerWidget().getMainTravelerMinAgeTextView();
		assertEquals(View.GONE, mainTravelerMinAgeTextView.getVisibility());
	}

	@Test
	public void testMultipleTravelerFlow() {
		mockViewModel = getMockViewModelEmptyTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
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
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUser.clickOnText(expectedTravelerOneText);
		Espresso.closeSoftKeyboard();
		enterValidTraveler(true);
		onView(allOf(withText(testName.getFullName()), isDescendantOfA(withId(R.id.traveler_picker_widget)))).check(matches(isDisplayed()));
		onView(allOf(withText(testName.getFullName()), isDescendantOfA(withId(R.id.traveler_picker_widget)))).perform(click());

		assertValidTravelerFields();
	}

	@Test
	public void testAllTravelersValidEntryToDefault() {
		mockViewModel = getMockViewModelEmptyTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);

		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUser.clickOnText(expectedTravelerOneText);
		enterValidTraveler(true);
		EspressoUser.clickOnText(expectedTravelerTwoText);
		enterValidTraveler(false, false);

		EspressoUtils.assertViewIsDisplayed(R.id.traveler_default_state);
		EspressoUtils.assertContainsImageDrawable(R.id.traveler_status_icon,  R.id.traveler_default_state,  R.drawable.validated);
	}

	@Test
	public void testIncompleteTravelerState() throws Throwable {
		mockViewModel = getMockViewModelIncompleteTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUser.clickOnText(expectedTravelerOneText);

		travelerPresenterBack();

		Common.delay(2);
		checkOscarInvalid(R.id.traveler_status_icon, R.drawable.invalid, testFirstName);
	}

	private void checkOscarInvalid(@IdRes int viewId, @DrawableRes int drawableId, String siblingText) {
		onView(allOf(withId(viewId),
			hasSibling(
				withChild(allOf(withId(R.id.primary_details_text), isDescendantOfA(withId(R.id.traveler_picker_widget)),  withText(siblingText)))
			))).check(matches(withImageDrawable(drawableId)));
	}

	@Test
	public void testToolbarNextFlow() throws Throwable {
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mockViewModel = getMockViewModelEmptyTravelers(2);
				testTravelerPresenter.setViewModel(mockViewModel);
			}
		});

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

		assertEquals(true, testTravelerPresenter.getTravelerEntryWidget().getEmailEntryView().getEmailAddress().hasFocus());
		PackageScreen.enterEmail(testEmail);
		PackageScreen.clickTravelerDone();

		assertEquals(true, testTravelerPresenter.getTravelerEntryWidget().getPhoneEntryView().getPhoneNumber().hasFocus());
		PackageScreen.clickTravelerDone();

		//skip phone number, assert that focus went back to first name
		onView(withId(R.id.datePicker)).perform(ViewActions.waitForViewToDisplay());
		onView(withId(R.id.datePicker)).perform(PickerActions.setDate(1989, 9, 6));
		onView(withId(R.id.datePickerDoneButton)).perform(click());
		PackageScreen.clickTravelerDone();

		onData(allOf(is(instanceOf(String.class)),is("Male"))).perform(click());
		PackageScreen.clickTravelerDone();

		assertEquals(true, testTravelerPresenter.getTravelerEntryWidget().getNameEntryView().getFirstName().hasFocus());
		PackageScreen.clickTravelerDone();
		assertEquals(true, testTravelerPresenter.getTravelerEntryWidget().getNameEntryView().getLastName().hasFocus());
		PackageScreen.clickTravelerDone();
		assertEquals(true, testTravelerPresenter.getTravelerEntryWidget().getEmailEntryView().getEmailAddress().hasFocus());
		PackageScreen.clickTravelerDone();
		assertEquals(true, testTravelerPresenter.getTravelerEntryWidget().getPhoneEntryView().getPhoneNumber().hasFocus());
		PackageScreen.enterPhoneNumber(testPhone);
		PackageScreen.clickTravelerDone();

		assertEquals("Traveler details", testSubscriber.getOnNextEvents().get(0));
		assertEquals("Edit Traveler 1 (Adult)", testSubscriber.getOnNextEvents().get(1));
		assertEquals("Traveler details", testSubscriber.getOnNextEvents().get(2));

		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		onView(allOf(withText(testFirstName + " " + testLastName), isDescendantOfA(withId(R.id.traveler_picker_widget)))).check(matches(isDisplayed()));
		EspressoUtils.assertViewWithTextIsDisplayed(expectedTravelerTwoText);
	}


	@Test
	public void testEmptyChildInfantTraveler() {
		List<Integer> children = Arrays.asList(1);
		mockViewModel = getMockViewModelEmptyTravelersWithInfant(2, children, true);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
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
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUser.clickOnText(expectedTravelerInfantText);
		travelerPresenterBack();

		Common.delay(2);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		checkOscarInvalid(R.id.traveler_status_icon, R.drawable.invalid, testChildFullName);
	}

	@Test
	public void testValidChildInfantTraveler() throws Throwable {
		final List<Integer> children = Arrays.asList(1);
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mockViewModel = getMockViewModelEmptyTravelersWithInfant(2, children, true);
				Traveler child1 = Db.getTravelers().get(2);
				setChildTraveler(child1, 1);
				testTravelerPresenter.setViewModel(mockViewModel);
			}
		});

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUser.clickOnText(expectedTravelerInfantText);
		PackageScreen.clickTravelerDone();
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
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
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUser.clickOnText(expectedTravelerChildText);
		PackageScreen.clickTravelerDone();
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
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
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUser.clickOnText(expectedTravelerChildText);
		travelerPresenterBack();

		Common.delay(2);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		checkOscarInvalid(R.id.traveler_status_icon, R.drawable.invalid, testChildFullName);
	}

	@Test
	public void testBoardingWarning() {
		mockViewModel = getMockViewModelEmptyTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUtils.assertViewIsNotDisplayed(R.id.boarding_warning);

		EspressoUser.clickOnText(expectedTravelerOneText);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.boarding_warning, R.string.name_must_match_warning_new);
	}

	@Test
	public void testBoardingWarningCleared() {
		mockViewModel = getMockViewModelValidTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUtils.assertViewIsNotDisplayed(R.id.boarding_warning);

		EspressoUser.clickOnText(expectedTravelerOneText);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.boarding_warning, R.string.name_must_match_warning_new);
		PackageScreen.clickTravelerDone();
		EspressoUtils.assertViewIsNotDisplayed(R.id.boarding_warning);
	}

	@Test
	public void testPassportIndependent() throws Throwable {
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mockViewModel = getMockViewModelValidTravelers(2);
				mockViewModel.getPassportRequired().onNext(true);
				testTravelerPresenter.setViewModel(mockViewModel);
			}
		});

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUser.clickOnText(expectedTravelerOneText);
		Espresso.closeSoftKeyboard();
		EspressoUser.scrollToView(R.id.passport_country_spinner);
		EspressoUtils.assertViewWithTextIsDisplayed("Passport: ");
		EspressoUser.clickOnView(R.id.passport_country_spinner);
		onData(allOf(is(instanceOf(String.class)), is("Afghanistan"))).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed("Passport: Afghanistan");
		travelerPresenterBack();

		EspressoUser.clickOnText(expectedTravelerTwoText);
		EspressoUser.scrollToView(R.id.passport_country_spinner);
		EspressoUtils.assertViewWithTextIsDisplayed("Passport: ");
		onView(withText("Passport: Afghanistan")).check(doesNotExist());
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
