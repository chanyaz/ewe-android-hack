package com.expedia.bookings.test.phone.rail;


import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.RailTestCase;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class RailTicketDeliveryTest extends RailTestCase {

	public void testDefaultPickupAtStation() throws Throwable {
		RailScreen.navigateToCheckout();

		checkDefaultPickupAtStation();
		checkMailDeliverySelected();
	}

	private void checkDefaultPickupAtStation() {
		EspressoUtils.assertViewIsDisplayed(R.id.ticket_delivery_overview_widget);
		EspressoUtils.assertViewIsNotDisplayed(R.id.ticket_delivery_entry_widget);
		assertPickupAtStationSelected();

		RailScreen.ticketDeliveryOverview().perform(click());
		assertEntryWidget();
		Common.pressBack();
	}

	private void assertEntryWidget() {
		EspressoUtils.assertViewIsDisplayed(R.id.station_container);
		EspressoUtils.assertViewIsDisplayed(R.id.mail_delivery_container);

		RailScreen.stationContainer().check(matches(hasDescendant(withText("Pick-up at station"))));
		RailScreen.stationContainer().check(matches(hasDescendant(withText(R.string.pickup_station_details))));

		RailScreen.mailDeliveryContainer().check(matches(hasDescendant(withText("Delivery by mail"))));
		RailScreen.mailDeliveryContainer().check(matches(hasDescendant(withText(R.string.mail_delivery_details))));
		RailScreen.mailDeliveryContainer().check(matches(hasDescendant(withText(R.string.tdo_fees_non_refundable))));
	}

	public void checkMailDeliverySelected() throws Throwable {
		RailScreen.ticketDeliveryOverview().perform(click());

		RailScreen.mailDeliveryContainer().perform(click());
		Common.pressBack();

		// no selection applied on back
		assertPickupAtStationSelected();

		RailScreen.ticketDeliveryOverview().perform(click());
		RailScreen.mailDeliveryContainer().perform(click());
		RailScreen.clickDone();

		assertMailDeliverySelected();
	}

	private void assertPickupAtStationSelected() {
		EspressoUtils.assertViewIsDisplayed(R.id.ticket_delivery_overview_widget);
		EspressoUtils.assertViewIsNotDisplayed(R.id.ticket_delivery_entry_widget);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.delivery_option_label_text, "Pick-up at station");
		EspressoUtils.assertContainsImageDrawable(R.id.delivery_option_icon, R.drawable.ticket_delivery_cko_station);
	}

	private void assertMailDeliverySelected() {
		EspressoUtils.assertViewIsDisplayed(R.id.ticket_delivery_overview_widget);
		EspressoUtils.assertViewIsNotDisplayed(R.id.ticket_delivery_entry_widget);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.delivery_option_label_text, "Delivery by mail");
		EspressoUtils.assertContainsImageDrawable(R.id.delivery_option_icon, R.drawable.ticket_delivery_cko_mail);
	}
}
