package com.expedia.bookings.test.unit.traveler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.test.espresso.Espresso;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUser;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.pagemodels.common.TravelerModel.TravelerDetails;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;
import com.expedia.bookings.widget.TextView;
import com.expedia.vm.traveler.TravelersViewModel;

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
	public void testMainTravelerMinAgeMessagingShows() throws Throwable {
		setViewModelForMiAge(true);
		TextView mainTravelerMinAgeTextView =
			testTravelersPresenter.getTravelerPickerWidget().getMainTravelerMinAgeTextView();
		assertEquals(View.VISIBLE, mainTravelerMinAgeTextView.getVisibility());
	}

	@Test
	public void testTravelerPickerIOB() throws Throwable {
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TravelersViewModel mockViewModel = getMockViewModelEmptyTravelers(3);
				testTravelersPresenter.setViewModel(mockViewModel);
				testTravelersPresenter.getTravelerPickerWidget().show();
				List<Traveler> travelers = new ArrayList<>();
				travelers.add(new Traveler());
				Db.sharedInstance.setTravelers(travelers);
				testTravelersPresenter.showSelectOrEntryState();
				try {
					testTravelersPresenter.getTravelerPickerWidget().show();
				}
				catch (Exception e) {
					Assert.fail("Oops. We shouldn't fail when customer adjusts number of travelers on the search form");
				}
			}
		});
		//No assertion, just dont crash
	}

	@Test
	public void testMainTravelerMinAgeMessagingHidden() throws Throwable {
		setViewModelForMiAge(false);
		TextView mainTravelerMinAgeTextView =
			testTravelersPresenter.getTravelerPickerWidget().getMainTravelerMinAgeTextView();
		assertEquals(View.GONE, mainTravelerMinAgeTextView.getVisibility());
	}

	@Test
	public void testMultipleTravelerFlow() throws Throwable {
		setTravelerViewModelForEmptyTravelers(2);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedTravelerOneText);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedTravelerTwoText);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedMainText);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedAdditionalText);
	}

	@Test
	public void testAllTravelersValidEntryToDefault() throws Throwable {
		setTravelerViewModelForEmptyTravelers(2);
		EspressoUser.clickOnView(R.id.traveler_default_state);

		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUser.clickOnText(expectedTravelerOneText);
		enterValidTraveler(true);
		EspressoUser.clickOnText(expectedTravelerTwoText);
		enterValidTraveler(false, false);

		EspressoUtils.assertViewIsDisplayed(R.id.traveler_default_state);
		EspressoUtils.assertContainsImageDrawable(R.id.traveler_status_icon, R.id.traveler_default_state, R.drawable.validated);
	}

	@Test
	public void testIncompleteTravelerState() throws Throwable {
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mockViewModel = getMockViewModelIncompleteTravelers(2);
				testTravelersPresenter.setViewModel(mockViewModel);
			}
		});
		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUser.clickOnText(expectedIncompleteTravelerOneText);

		travelerPresenterBack();

		Common.delay(2);
		checkOscarInvalid(R.id.traveler_status_icon, R.drawable.invalid, testFirstName);
	}

	private void checkOscarInvalid(@IdRes int viewId, @DrawableRes int drawableId, String siblingText) {
		onView(allOf(withId(viewId),
			hasSibling(
				withChild(allOf(withId(R.id.primary_details_text), isDescendantOfA(withId(R.id.traveler_picker_widget)), withText(siblingText)))
			))).check(matches(withImageDrawable(drawableId)));
	}

	@Test
	public void testNumberOfErrorsCorrect() throws Throwable {
		setTravelerViewModelForEmptyTravelers(2);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUser.clickOnText(expectedTravelerOneText);

		TravelerDetails.enterFirstName(testFirstName);

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelersPresenter.onDoneClicked();
				assertEquals(4, testTravelersPresenter.getTravelerEntryWidget().getNumberOfInvalidFields());
			}
		});

		TravelerDetails.enterLastName(testLastName);
		EspressoUser.scrollToView(R.id.edit_email_address);
		TravelerDetails.enterEmail(testEmail);
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelersPresenter.onDoneClicked();
			}
		});
		assertEquals(2, testTravelersPresenter.getTravelerEntryWidget().getNumberOfInvalidFields());
	}

	@Test
	public void testEmptyChildInfantTraveler() throws Throwable {
		final List<Integer> children = Arrays.asList(1);
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mockViewModel = getMockViewModelEmptyTravelersWithInfant(2, children, true);
				testTravelersPresenter.setViewModel(mockViewModel);
				testTravelersPresenter.getViewModel().getDoneClickedMethod().subscribe(testToolbar.getViewModel().getDoneClickedMethod());
			}
		});

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedTravelerOneText);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedTravelerTwoText);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedTravelerInfantText);
	}

	@Test
	public void testInvalidChildInfantTraveler() throws Throwable {
		final List<Integer> children = Arrays.asList(1);
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mockViewModel = getMockViewModelEmptyTravelersWithInfant(2, children, true);
				Traveler child1 = Db.sharedInstance.getTravelers().get(2);
				setChildTraveler(child1, 10);
				testTravelersPresenter.setViewModel(mockViewModel);
				testTravelersPresenter.getViewModel().getDoneClickedMethod().subscribe(testToolbar.getViewModel().getDoneClickedMethod());
			}
		});

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUser.clickOnText(expectedFilledTravelerChildText);
		PackageScreen.closeDateErrorDialog();
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
				Traveler child1 = Db.sharedInstance.getTravelers().get(2);
				setChildTraveler(child1, 1);
				testTravelersPresenter.setViewModel(mockViewModel);
				testTravelersPresenter.getViewModel().getDoneClickedMethod().subscribe(testToolbar.getViewModel().getDoneClickedMethod());
			}
		});

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUser.clickOnText(expectedFilledTravelerChildText);
		TravelerDetails.clickDone();
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		checkOscarInvalid(R.id.traveler_status_icon, R.drawable.validated, testChildFullName);
	}


	@Test
	public void testValidChildTravelers() throws Throwable {
		final List<Integer> children = Arrays.asList(10);
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mockViewModel = getMockViewModelEmptyTravelersWithInfant(2, children, false);
				Traveler child1 = Db.sharedInstance.getTravelers().get(2);
				setChildTraveler(child1, 10);
				testTravelersPresenter.setViewModel(mockViewModel);
				testTravelersPresenter.getViewModel().getDoneClickedMethod().subscribe(testToolbar.getViewModel().getDoneClickedMethod());
			}
		});

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUser.clickOnText(expectedFilledTravelerChildText);
		TravelerDetails.clickDone();
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		checkOscarInvalid(R.id.traveler_status_icon, R.drawable.validated, testChildFullName);
	}

	@Test
	public void testInvalidChildTravelers() throws Throwable {
		final List<Integer> children = Arrays.asList(10);
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mockViewModel = getMockViewModelEmptyTravelersWithInfant(2, children, false);
				Traveler child1 = Db.sharedInstance.getTravelers().get(2);
				setChildTraveler(child1, 1);
				testTravelersPresenter.setViewModel(mockViewModel);
				testTravelersPresenter.getViewModel().getDoneClickedMethod().subscribe(testToolbar.getViewModel().getDoneClickedMethod());
			}
		});

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUser.clickOnText(expectedFilledTravelerChildText);
		PackageScreen.closeDateErrorDialog();
		travelerPresenterBack();

		Common.delay(2);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		checkOscarInvalid(R.id.traveler_status_icon, R.drawable.invalid, testChildFullName);
	}

	@Test
	public void testBoardingWarning() throws Throwable {
		setTravelerViewModelForEmptyTravelers(2);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUtils.assertViewIsNotDisplayed(R.id.boarding_warning);

		EspressoUser.clickOnText(expectedTravelerOneText);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.boarding_warning, R.string.name_must_match_warning_new);
	}

	@Test
	public void testBoardingWarningCleared() throws Throwable {
		setTravelerViewModelForValidTravelers(2);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_picker_widget);
		EspressoUtils.assertViewIsNotDisplayed(R.id.boarding_warning);

		onView(allOf(isDescendantOfA(withId(R.id.main_traveler_container)), withText(expectedFilledTravelerOneText))).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.boarding_warning, R.string.name_must_match_warning_new);
		TravelerDetails.clickDone();
		EspressoUtils.assertViewIsNotDisplayed(R.id.boarding_warning);
	}

	@Test
	public void testPassportIndependent() throws Throwable {
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mockViewModel = getMockViewModelValidTravelers(2);
				mockViewModel.getPassportRequired().onNext(true);
				testTravelersPresenter.setViewModel(mockViewModel);
			}
		});

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUser.clickOnText(expectedFilledTravelerOneText);
		Espresso.closeSoftKeyboard();
		EspressoUser.scrollToView(R.id.passport_country_btn);
		EspressoUser.clickOnView(R.id.passport_country_btn);
		onData(allOf(is(instanceOf(String.class)), is(pointOfSaleCountry))).atPosition(1).check(matches(isDisplayed()));
		onData(allOf(is(instanceOf(String.class)), is("Afghanistan"))).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed("Afghanistan");
		travelerPresenterBack();

		EspressoUser.clickOnText(expectedFilledTravelerTwoText);
		onView(withText("Please use the Roman alphabet")).check(matches(isDisplayed()));
		onView(withText("OK")).perform(click());
		Espresso.closeSoftKeyboard();
		EspressoUser.scrollToView(R.id.passport_country_btn);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.passport_country_btn, "");
		onView(withText("Passport: Afghanistan")).check(doesNotExist());
		travelerPresenterBack();
		EspressoUtils.assertContainsImageDrawable(R.id.traveler_status_icon, R.id.traveler_default_state, R.drawable.invalid);
	}

	private void travelerPresenterBack() throws Throwable {
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelersPresenter.back();
			}
		});
	}

	public void testStoredTravelerButtonActions() throws Throwable {
		setTravelerViewModelForEmptyTravelers(1);

		EspressoUser.clickOnView(R.id.traveler_default_state);

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelersPresenter.getTravelerEntryWidget().onAddNewTravelerSelected();
			}
		});

		EspressoUtils.assertViewWithTextIsDisplayed(R.id.first_name_input, "");
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.middle_name_input, "");
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.last_name_input, "");
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_phone_number, "");
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_birth_date_text_btn, "");
	}

	private void setViewModelForMiAge(final boolean mainTravelerMinAge) throws Throwable {
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TravelersViewModel mockViewModel = getMockviewModel(mainTravelerMinAge);
				testTravelersPresenter.setViewModel(mockViewModel);
			}
		});
	}
}
