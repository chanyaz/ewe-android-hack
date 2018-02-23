package com.expedia.bookings.test.phone.lx;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.LxTestCase;
import com.expedia.bookings.test.pagemodels.lx.LXInfositeScreen;
import com.expedia.bookings.test.pagemodels.lx.LXScreen;
import com.expedia.bookings.test.phone.lx.models.TicketDataModel;
import com.expedia.bookings.test.phone.lx.models.TicketSummaryDataModel;
import com.expedia.bookings.test.phone.lx.utils.ExpectedDataSupplierForTicketWidget;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.isEmpty;
import static com.expedia.bookings.test.espresso.CustomMatchers.withAtLeastChildCount;
import static com.expedia.bookings.test.espresso.CustomMatchers.withChildCount;
import static com.expedia.bookings.test.espresso.CustomMatchers.withDateCaptionAtIndex;
import static com.expedia.bookings.test.espresso.CustomMatchers.withOneEnabled;
import static com.expedia.bookings.test.espresso.CustomMatchers.withTotalPrice;
import static com.expedia.bookings.test.espresso.ViewActions.clickWhenEnabled;
import static com.expedia.bookings.test.espresso.ViewActions.customScroll;
import static com.expedia.bookings.test.espresso.ViewActions.waitFor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/*
	This test case will test the following things.

	1> Validate that the Hero Image has correct Data ( data that was been passed from the SRP Page)
	2> Validate that the Info Site page has three sections i > Highlights section ii > Description Section and iii > Location section all the sections must have some data.
	3> Offer Selection widget. This has the following checks
		a> There must be exactly 15 offers available
		b> The stamping on the buttons must match the next 15 days ( weekday and month of the day)
		c> Atleast one offer must be available( clickable)
		d> Upon click of the button we must get select ticket widget
		e> The Select ticket widget must have atleast one ticket to offer
		f> Upon click of the Select ticket we must be able to get the following
			i> The number of rows must match exactly the number of traveller types
			ii> Initially no tickets should be selected
			iii> On each traveller type we must have the same per ticker cost as was displayed in the summary section when the Select ticket was not clicked
			iv> We should not be allowed to select more than 8 traveller on every row
			v> We must not be allowed to select anything lesser than 0 traveller
			v> The addition of travellers must match the number of travellers on each row.
			vi> The total cost of travel must be correctly reflected on the book now button.
*/
public class LXInfositeTest extends LxTestCase {

	private ExpectedDataSupplierForTicketWidget mExpectedDataTktWdgt;

	@Test
	public void testInfoSiteTestSuite() throws Throwable {
		LXScreen.goToSearchResults(getLxIdlingResource());
		LXScreen.waitForSearchListDisplayed();
		screenshot("LX captured data");
		LXScreen.clickOnResultAtIndex(1);
		onView(withId(R.id.overlay_title_container)).perform(waitFor(not(isDisplayed()), 20L, TimeUnit.SECONDS));

		screenshot("LX validated hero image");

		onView(allOf(withId(R.id.section_title), withText(
			R.string.highlights_activity_details))).perform(scrollTo()).check(matches(
			isDisplayed()));
		onView(allOf(withId(R.id.section_title), withText(
			R.string.highlights_activity_details))).check(matches(
			not(isEmpty())));
		onView(allOf(withId(R.id.section_content), hasSibling(allOf(withId(R.id.section_title),
			withText(R.string.description_activity_details)))))
			.check(matches(not(isEmpty())));
		onView(allOf(withId(R.id.section_content), hasSibling(allOf(withId(R.id.section_title),
			withText(R.string.location_activity_details)))))
			.check(matches(not(isEmpty())));
		onView(allOf(withId(R.id.section_content), hasSibling(allOf(withId(R.id.section_title),
			withText(R.string.cancellation_policy)))))
			.check(matches(not(isEmpty())));
		screenshot("LX validated 4 sections");

		LXInfositeScreen.detailsDateContainer().perform(scrollTo());
		LXInfositeScreen.detailsDateContainer().check(matches(withChildCount(15)));

		for (int dayOffset = 0; dayOffset < 15; dayOffset++) {
			LocalDate dayToValidate = LocalDate.now().plusDays(dayOffset);
			String currentDayOfTheWeek = dayToValidate.dayOfWeek().getAsShortText();
			String currentDayOfTheMonth = dayToValidate.dayOfMonth().getAsText();
			LXInfositeScreen.detailsDateContainer()
				.check(matches(withDateCaptionAtIndex(dayOffset, currentDayOfTheWeek, currentDayOfTheMonth)));
		}

		LXInfositeScreen.detailsDateContainer().check(matches(withOneEnabled()));
		screenshot("LX validated offers strip");

		LXInfositeScreen.offersWidgetContainer().check(matches(withAtLeastChildCount(1)));

		TicketSummaryDataModel ticketSummary = new TicketSummaryDataModel();
		LXInfositeScreen.offersWidgetContainer().perform(LXInfositeScreen.loadTicketSummary(0, ticketSummary));
		mExpectedDataTktWdgt = new ExpectedDataSupplierForTicketWidget(ticketSummary);

		LXInfositeScreen.selectOffer(mExpectedDataTktWdgt.getTicketName()).perform(click());

		LXInfositeScreen.ticketContainer(mExpectedDataTktWdgt.getTicketName())
			.check(matches(withChildCount(mExpectedDataTktWdgt.numberOfTicketRows())));
		//Below is the default state verification
		boolean isFirstRow = true;
		for (TicketDataModel ticket : mExpectedDataTktWdgt.getTickets()) {
			LXInfositeScreen.ticketRow(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType).check(
				matches(withText(
					containsString(ticket.perTicketCost.toString()))));
			LXInfositeScreen.ticketRow(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType).check(
				matches(LXInfositeScreen.withRestrictionText()));
			if (isFirstRow) {
				isFirstRow = false;
				// the very first row must contain 1 travellers by default
				LXInfositeScreen.ticketCount(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.check(matches(withText(containsString("1"))));
				LXInfositeScreen.bookNowButton(mExpectedDataTktWdgt.getTicketName()).perform(customScroll(100));
				LXInfositeScreen.bookNowButton(mExpectedDataTktWdgt.getTicketName()).check(matches(isEnabled()));
				//now bring back the counter to zero so that all the rows have zero tickets
				LXInfositeScreen
					.ticketRemoveButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.perform(clickWhenEnabled());
				//check in this situation where we dont have any ticket we dont have the book now button nor the ticket summary
				LXInfositeScreen.priceSummary(mExpectedDataTktWdgt.getTicketName())
						.check(matches(not(isDisplayed())));
				LXInfositeScreen.bookNowButton(mExpectedDataTktWdgt.getTicketName())
						.check(matches(not(isDisplayed())));
				LXInfositeScreen
						.ticketRemoveButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
						.check(matches(not(isEnabled())));
				LXInfositeScreen
						.ticketAddButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
						.check(matches(isEnabled()));
			}
			else {
				LXInfositeScreen.ticketCount(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.check(matches(withText(containsString("0"))));
				//remove button must be disabled and add button must be enabled
				LXInfositeScreen
					.ticketRemoveButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.check(matches(not(
						isEnabled())));
				LXInfositeScreen
					.ticketAddButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.check(matches(
						isEnabled()));
			}
		}

		for (int currentClickCounter = 1; currentClickCounter <= 8; currentClickCounter++) {
			for (TicketDataModel ticket : mExpectedDataTktWdgt.getTickets()) {
				int count = currentClickCounter;
				LXInfositeScreen
					.ticketAddButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.perform(clickWhenEnabled());
				if (currentClickCounter == 8) {
					count = 8;
					LXInfositeScreen.ticketAddButton(mExpectedDataTktWdgt.getTicketName(),
						ticket.travellerType)
						.check(matches(not(
							isEnabled())));

				}
				LXInfositeScreen.ticketCount(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.check(matches(withText(String.valueOf(
						count))));
			}
		}
		for (int currentClickCounter = 1; currentClickCounter <= 8; currentClickCounter++) {
			for (TicketDataModel ticket : mExpectedDataTktWdgt.getTickets()) {
				LXInfositeScreen.offersWidgetContainer().perform(customScroll(50));
				LXInfositeScreen
					.ticketRemoveButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.perform(clickWhenEnabled());
				if (currentClickCounter == 8) {
					LXInfositeScreen.ticketRemoveButton(mExpectedDataTktWdgt.getTicketName(),
						ticket.travellerType)
						.check(matches(not(
							isEnabled())));
				}
			}
		}
		for (TicketDataModel ticket : mExpectedDataTktWdgt.getTickets()) {
			Random rand = new Random();
			int numberOfClicks = rand.nextInt(7) + 1;
			for (int currentClickCounter = 1; currentClickCounter <= numberOfClicks; currentClickCounter++) {
				LXInfositeScreen.offersWidgetContainer().perform(customScroll(50));
				LXInfositeScreen
					.ticketAddButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.perform(clickWhenEnabled());
				mExpectedDataTktWdgt.updateTravellers(currentClickCounter, ticket.travellerType);
			}
		}
		LXInfositeScreen.priceSummary(mExpectedDataTktWdgt.getTicketName())
				.check(matches(withText(mExpectedDataTktWdgt.expectedSummary())));
		LXInfositeScreen.bookNowButton(mExpectedDataTktWdgt.getTicketName())
				.check(matches(withTotalPrice(mExpectedDataTktWdgt.getTotalPrice())));
		LXInfositeScreen.offersWidgetContainer().perform(customScroll(50));
		LXInfositeScreen.bookNowButton(mExpectedDataTktWdgt.getTicketName()).perform(clickWhenEnabled());
		screenshot("LX validated the offers widget");
	}
}
