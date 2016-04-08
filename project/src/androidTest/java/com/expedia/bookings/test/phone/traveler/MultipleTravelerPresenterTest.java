package com.expedia.bookings.test.phone.traveler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUser;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.phone.packages.PackageScreen;

import rx.observers.TestSubscriber;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
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
		addTravelerToDb(new Traveler());
		addTravelerToDb(new Traveler());

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
		addTravelerToDb(new Traveler());
		addTravelerToDb(new Traveler());
		mockViewModel = getMockViewModelEmptyTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);
		setPackageParams();

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
		addTravelerToDb(getValidTraveler());
		addTravelerToDb(getValidTraveler());
		mockViewModel = getMockViewModelValidTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);
		setPackageParams();

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
		addTravelerToDb(getIncompleteTraveler());
		addTravelerToDb(getIncompleteTraveler());
		mockViewModel = getMockViewModelIncompleteTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);
		setPackageParams();

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUser.clickOnText(expectedTravelerOneText);

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testTravelerPresenter.back();
			}
		});

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
		addTravelerToDb(new Traveler());
		addTravelerToDb(new Traveler());
		mockViewModel = getMockViewModelEmptyTravelers(2);
		testTravelerPresenter.setViewModel(mockViewModel);
		setPackageParams();

		TestSubscriber testSubscriber = new TestSubscriber(1);
		testTravelerPresenter.getToolbarTitleSubject().subscribe(testSubscriber);

		EspressoUser.clickOnView(R.id.traveler_default_state);
		EspressoUser.clickOnText(expectedTravelerOneText);

		assertEquals(true, testTravelerPresenter.getTravelerEntryWidget().getNameEntryView().getFirstName().hasFocus());
		PackageScreen.enterFirstName(testFirstName);
		PackageScreen.clickTravelerDone();

		assertEquals(true, testTravelerPresenter.getTravelerEntryWidget().getNameEntryView().getMiddleInitial().hasFocus());
		PackageScreen.clickTravelerDone();

		assertEquals(true, testTravelerPresenter.getTravelerEntryWidget().getNameEntryView().getLastName().hasFocus());
		PackageScreen.enterLastName(testLastName);
		PackageScreen.clickTravelerDone();

		assertEquals(true, testTravelerPresenter.getTravelerEntryWidget().getPhoneEntryView().getPhoneNumber().hasFocus());
		PackageScreen.enterPhoneNumber(testPhone);
		PackageScreen.selectBirthDate(1989,6,9);
		PackageScreen.clickTravelerDone();

		assertEquals("Select travelers", testSubscriber.getOnNextEvents().get(0));
		assertEquals("Traveler details", testSubscriber.getOnNextEvents().get(1));
		assertEquals("Select travelers", testSubscriber.getOnNextEvents().get(2));

		EspressoUtils.assertViewIsDisplayed(R.id.traveler_select_state);
		EspressoUtils.assertViewWithTextIsDisplayed(testFirstName + " " + testLastName);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedTravelerTwoText);
	}
}
