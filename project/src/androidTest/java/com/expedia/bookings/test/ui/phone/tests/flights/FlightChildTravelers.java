package com.expedia.bookings.test.ui.phone.tests.flights;

import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.test.rules.ExpediaMockWebServerRule;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsTravelerInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsTravelerPicker;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class FlightChildTravelers {
	/*
	*  #511. Child travelers - Flights
	*/
	private static final String TAG = "FlightChildTravelersTests";

	//Launch directly into the About/Info Screen
	@Rule
	public ActivityTestRule<FlightSearchActivity> mRule = new ActivityTestRule<>(FlightSearchActivity.class);

	@Rule
	public final ExpediaMockWebServerRule server = new ExpediaMockWebServerRule();

	private int adultCount = 1;
	private int childCount = 0;

	@Test
	public void testTravelerPicker() throws Throwable {
		FlightsSearchScreen.clickPassengerSelectionButton();
		incrementAdultCountBy(1);
		incrementChildCountBy(1);
		incrementAdultCountBy(1);
		incrementAdultCountBy(1);
		incrementChildCountBy(1);
	}

	private void checkRefinementInfoText() {
		FlightsTravelerPicker.refinementInfoTextMatches(adultCount, childCount, mRule.getActivity());
	}

	private void incrementAdultCountBy(int count) {
		for (int i = 0; i < count; i++) {
			FlightsTravelerPicker.incrementAdultCount();
			adultCount++;
			checkRefinementInfoText();
		}
	}

	private void decrementAdultCountBy(int count) {
		for (int i = 0; i < count; i++) {
			FlightsTravelerPicker.decrementAdultCount();
			adultCount--;
			checkRefinementInfoText();
		}
	}

	private void incrementChildCountBy(int count) {
		for (int i = 0; i < count; i++) {
			FlightsTravelerPicker.incrementChildCount();
			childCount++;
			checkRefinementInfoText();
			FlightsTravelerPicker.checkChildAgeOptions(childCount, mRule.getActivity());
			Espresso.pressBack();
		}
	}

	private void decrementChildCountBy(int count) {
		for (int i = 0; i < count; i++) {
			FlightsTravelerPicker.decrementChildCount();
			childCount--;
			checkRefinementInfoText();
		}
	}

	@Test
	public void testChildTravelerMax() throws Throwable {
		FlightsSearchScreen.clickPassengerSelectionButton();
		incrementChildCountBy(4);
		ScreenActions.enterLog(TAG, "Flights supports max of 4 children");
		//TODO check for +/- disabled status when max children/max travelers is reached. Tracked by #4733
	}

	@Test
	public void testInfantDefaultLapSelection() throws Throwable {
		FlightsSearchScreen.clickPassengerSelectionButton();
		incrementChildCountBy(1);
		// Select less than one year old
		FlightsTravelerPicker.selectChildAge(mRule.getActivity(), childCount, 0);
		FlightsTravelerPicker.isInfantInLapChecked(true);
		// Select any child > 2 years old
		FlightsTravelerPicker.selectChildAge(mRule.getActivity(), childCount, 2);
		FlightsTravelerPicker.isInfantSeatingLayoutVisible(false);
		// Select child 1 year
		FlightsTravelerPicker.selectChildAge(mRule.getActivity(), childCount, 1);
		FlightsTravelerPicker.isInfantInLapChecked(true);
	}

	@Test
	public void testChildNotEnoughLaps() throws Throwable {
		FlightsSearchScreen.clickPassengerSelectionButton();
		incrementChildCountBy(1);
		// Select less than one year old (infant)
		FlightsTravelerPicker.selectChildAge(mRule.getActivity(), childCount, 0);
		FlightsTravelerPicker.isInfantInLapChecked(true);

		// Add another infant and check alert shown
		incrementChildCountBy(1);
		FlightsTravelerPicker.selectChildAge(mRule.getActivity(), childCount, 0);
		FlightsTravelerPicker.isInfantAlertShown(true);

		// remove one infant and check alert is not shown
		decrementChildCountBy(1);
		FlightsTravelerPicker.isInfantAlertShown(false);
		FlightsTravelerPicker.isInfantInSeatChecked(true);

		// 1 Adult, 1 10yr, 2 infant = show alert
		// add 10yr old (childCount#2)
		incrementChildCountBy(1);
		// add infant
		incrementChildCountBy(1);
		FlightsTravelerPicker.selectChildAge(mRule.getActivity(), childCount, 0);
		FlightsTravelerPicker.isInfantAlertShown(true);

		// change child#2 to >= 12yr and alert is not shown
		FlightsTravelerPicker.selectChildAge(mRule.getActivity(), 2, 12);
		FlightsTravelerPicker.isInfantAlertShown(false);
		FlightsTravelerPicker.isInfantInSeatChecked(true);

		// change child#2 to < 12yr and alert is shown
		FlightsTravelerPicker.selectChildAge(mRule.getActivity(), 2, 11);
		FlightsTravelerPicker.isInfantAlertShown(true);

		// change child#2 to >= 12yr and alert is not shown
		FlightsTravelerPicker.selectChildAge(mRule.getActivity(), 2, 13);
		FlightsTravelerPicker.isInfantAlertShown(false);
		FlightsTravelerPicker.isInfantInSeatChecked(true);

		// remove all children and check visibilities
		decrementChildCountBy(3);
		FlightsTravelerPicker.isInfantSeatingLayoutVisible(false);
		FlightsTravelerPicker.isInfantAlertShown(false);

		Espresso.pressBack();
	}

	@Test
	public void testInfantLapPriceBreakdownCheckoutOverview() throws Throwable {
		FlightsSearchScreen.clickPassengerSelectionButton();
		incrementChildCountBy(1);
		// Select less than one year old (infant)
		FlightsTravelerPicker.selectChildAge(mRule.getActivity(), childCount, 0);
		FlightsTravelerPicker.isInfantInLapChecked(true);

		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.clickSearchButton();
		FlightsSearchResultsScreen.clickListItem(4);
		FlightLegScreen.clickSelectFlightButton();

		FlightLegScreen.clickCostBreakdownButtonView();
		onView(withText("Infant in lap 1 details")).check(matches(hasSibling(withText("$0.00"))));
		FlightLegScreen.clickCostBreakdownDoneButton();

		CommonCheckoutScreen.clickCheckoutButton();
		FlightsTravelerInfoScreen.assertEmptyTravelerDetailsLabel(0, "Adult details");
		FlightsTravelerInfoScreen.assertEmptyTravelerDetailsLabel(2, "Infant in lap details");
	}

	@Test
	public void testInfantSeatPriceBreakdownCheckoutOverview() throws Throwable {
		FlightsSearchScreen.clickPassengerSelectionButton();
		incrementChildCountBy(1);
		// Select less than one year old (infant)
		FlightsTravelerPicker.selectChildAge(mRule.getActivity(), childCount, 0);
		FlightsTravelerPicker.isInfantInLapChecked(true);

		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.clickSearchButton();
		FlightsSearchResultsScreen.clickListItem(6);
		FlightLegScreen.clickSelectFlightButton();

		FlightLegScreen.clickCostBreakdownButtonView();
		onView(withText("Infant in seat 1 details")).check(matches(hasSibling(withText("$589.00"))));
		FlightLegScreen.clickCostBreakdownDoneButton();

		CommonCheckoutScreen.clickCheckoutButton();
		FlightsTravelerInfoScreen.assertEmptyTravelerDetailsLabel(0, "Adult details");
		FlightsTravelerInfoScreen.assertEmptyTravelerDetailsLabel(2, "Infant in seat details");
	}

	@Test
	public void testChildInfantLapPriceBreakdownOverview() throws Throwable {
		FlightsSearchScreen.clickPassengerSelectionButton();
		incrementChildCountBy(1);
		incrementChildCountBy(1);
		// Select less than one year old (infant)
		FlightsTravelerPicker.selectChildAge(mRule.getActivity(), childCount, 0);
		FlightsTravelerPicker.isInfantInLapChecked(true);

		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.clickSearchButton();
		FlightsSearchResultsScreen.clickListItem(5);
		FlightLegScreen.clickSelectFlightButton();

		FlightLegScreen.clickCostBreakdownButtonView();
		onView(withText("Infant in lap 1 details")).check(matches(hasSibling(withText("$0.00"))));
		onView(withText("Child 1 details")).check(matches(hasSibling(withText("$589.00"))));
		FlightLegScreen.clickCostBreakdownDoneButton();

		CommonCheckoutScreen.clickCheckoutButton();
		FlightsTravelerInfoScreen.assertEmptyTravelerDetailsLabel(0, "Adult details");
		FlightsTravelerInfoScreen.assertEmptyTravelerDetailsLabel(2, "10 yr old details");
		FlightsTravelerInfoScreen.assertEmptyTravelerDetailsLabel(4, "Infant in lap details");
	}
}
