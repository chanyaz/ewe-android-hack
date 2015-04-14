package com.expedia.bookings.test.ui.phone.tests.lx;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.LXBaseActivity;
import com.expedia.bookings.test.component.lx.LXViewModel;
import com.expedia.bookings.test.component.lx.models.TicketDataModel;
import com.expedia.bookings.test.component.lx.models.TicketSummaryDataModel;
import com.expedia.bookings.test.component.lx.models.TileDataModel;
import com.expedia.bookings.test.component.lx.pagemodels.LXInfositePageModel;
import com.expedia.bookings.test.component.lx.pagemodels.LXSearchResultsPageModel;
import com.expedia.bookings.test.component.lx.utils.ExpectedDataSupplierForTicketWidget;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.ui.espresso.CustomMatchers.isEmpty;
import static com.expedia.bookings.test.ui.espresso.CustomMatchers.withAtleastChildCount;
import static com.expedia.bookings.test.ui.espresso.CustomMatchers.withChildCount;
import static com.expedia.bookings.test.ui.espresso.CustomMatchers.withDateCaptionAtIndex;
import static com.expedia.bookings.test.ui.espresso.CustomMatchers.withOneEnabled;
import static com.expedia.bookings.test.ui.espresso.CustomMatchers.withTotalPrice;
import static com.expedia.bookings.test.ui.espresso.ViewActions.clickOnFirstEnabled;
import static com.expedia.bookings.test.ui.espresso.ViewActions.waitFor;
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
public class LXInfositeTestCases extends PhoneTestCase {

	private TileDataModel mTileData;

	private ExpectedDataSupplierForTicketWidget mExpectedDataTktWdgt;

	public LXInfositeTestCases() {
		super(LXBaseActivity.class);
	}

	public void testInfoSiteTestSuite() throws Throwable {

		if (getLxIdlingResource().isInSearchEditMode()) {
			onView(Matchers
				.allOf(withId(R.id.error_action_button), withText(
					R.string.edit_search))).perform(click());

			String expectedLocationDisplayName = "San Francisco, CA";
			LXViewModel.location().perform(typeText("San"));
			LXViewModel.selectLocation(getInstrumentation(), expectedLocationDisplayName);
			LXViewModel.selectDateButton().perform(click());
			LXViewModel.selectDates(LocalDate.now(), null);
			LXViewModel.searchButton().perform(click());
		}

		mTileData = LXSearchResultsPageModel.getTileDataAtIndex(1);
		screenshot("LX captured data");
		LXSearchResultsPageModel.clickOnTileAtIndex(1);
		onView(withId(R.id.loading_details)).perform(waitFor(20L, TimeUnit.SECONDS));

		screenshot("LX validated hero image");

		onView(allOf(withId(R.id.section_title), withText(
			R.string.highlights_activity_details))).check(matches(
			isDisplayed()));
		onView(allOf(withId(R.id.section_title), withText(
			R.string.highlights_activity_details))).check(matches(
			not(isEmpty())));
		onView(allOf(withId(R.id.section_content), hasSibling(allOf(withId(R.id.section_title),
			withText(R.string.description_activity_details)))))
			.check(matches(not(isEmpty())));
		onView(allOf(withId(R.id.section_content), hasSibling(allOf(withId(R.id.section_title),
			withText(R.string.location_activity_details)))))
			.perform(scrollTo()).check(matches(not(isEmpty())));
		screenshot("LX validated 3 sections");

		LXInfositePageModel.detailsDateContainer().perform(scrollTo());
		LXInfositePageModel.detailsDateContainer().check(matches(withChildCount(15)));

		for (int dayOffset = 0; dayOffset < 15; dayOffset++) {
			LocalDate dayToValidate = LocalDate.now().plusDays(dayOffset);
			String currentDayOfTheWeek = dayToValidate.dayOfWeek().getAsShortText();
			String currentDayOfTheMonth = dayToValidate.dayOfMonth().getAsText();
			LXInfositePageModel.detailsDateContainer()
				.check(matches(withDateCaptionAtIndex(dayOffset, currentDayOfTheWeek, currentDayOfTheMonth)));
		}

		LXInfositePageModel.detailsDateContainer().check(matches(withOneEnabled()));
		screenshot("LX validated offers strip");


		LXViewModel.detailsDateContainer().perform(clickOnFirstEnabled());
		onView(withId(R.id.offers)).check(matches(withAtleastChildCount(1)));


		TicketSummaryDataModel ticketSummary = new TicketSummaryDataModel();
		LXInfositePageModel.offersWidgetContainer().perform(scrollTo());
		LXInfositePageModel.offersWidgetContainer().perform(LXInfositePageModel.loadTicketSummary(0, ticketSummary));
		mExpectedDataTktWdgt = new ExpectedDataSupplierForTicketWidget(ticketSummary);
		LXViewModel.selectTicketsButton(ticketSummary.ticketTitle).perform(scrollTo(), click());

		LXInfositePageModel.ticketContainer(mExpectedDataTktWdgt.getTicketName())
			.check(matches(withChildCount(mExpectedDataTktWdgt.numberOfTicketRows())));
		//Below is the default state verification
		boolean isFirstRow = true;
		for (TicketDataModel ticket : mExpectedDataTktWdgt.getTickets()) {
			LXInfositePageModel.ticketRow(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType).check(
				matches(withText(
					containsString(ticket.perTicketCost.toString()))));
			LXInfositePageModel.ticketRow(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType).check(
				matches(LXInfositePageModel.withRestrictionText()));
			if (isFirstRow) {
				isFirstRow = false;
				// the very first row must contain 2 travellers by default
				LXInfositePageModel.ticketCount(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.check(matches(withText(containsString("2"))));
				//now bring back the counter to zero so that all the rows have zero tickets
				LXInfositePageModel.ticketRemoveButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.perform(click());
				LXInfositePageModel.ticketRemoveButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.perform(click());
				//check in this situation where we dont have any ticket we dont have the book now button nor the ticket summary
				LXInfositePageModel.priceSummary(mExpectedDataTktWdgt.getTicketName()).check(
					matches(not(isDisplayed())));
				LXInfositePageModel.bookNowButton(mExpectedDataTktWdgt.getTicketName()).check(matches(not(
					isDisplayed())));
				LXInfositePageModel.ticketRemoveButton(mExpectedDataTktWdgt.getTicketName(),
					ticket.travellerType).check(matches(not(
					isEnabled())));
				LXInfositePageModel.ticketAddButton(mExpectedDataTktWdgt.getTicketName(),
					ticket.travellerType).check(matches(
					isEnabled()));
			}
			else {
				LXInfositePageModel.ticketCount(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.check(matches(withText(containsString("0"))));
				//remove button must be disabled and add button must be enabled
				LXInfositePageModel.ticketRemoveButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.check(matches(not(
						isEnabled())));
				LXInfositePageModel.ticketAddButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.check(matches(
						isEnabled()));
			}
		}

		for (int currentClickCounter = 1; currentClickCounter <= 9; currentClickCounter++) {
			for (TicketDataModel ticket : mExpectedDataTktWdgt.getTickets()) {
				int count = currentClickCounter;
				LXInfositePageModel.ticketAddButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.perform(click());
				if (currentClickCounter == 9) {
					count = 8;
					LXInfositePageModel.ticketAddButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
						.check(matches(not(
							isEnabled())));

				}
				LXInfositePageModel.ticketCount(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.check(matches(withText(String.valueOf(
						count))));
			}
		}

		for (int currentClickCounter = 1; currentClickCounter <= 9; currentClickCounter++) {
			for (TicketDataModel ticket : mExpectedDataTktWdgt.getTickets()) {
				LXInfositePageModel.ticketRemoveButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.perform(click());
				if (currentClickCounter == 9) {
					LXInfositePageModel.ticketRemoveButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
						.check(matches(not(
							isEnabled())));
				}
			}
		}

		for (TicketDataModel ticket : mExpectedDataTktWdgt.getTickets()) {
			Random rand = new Random();
			int numberOfClicks = rand.nextInt(7) + 1;
			for (int currentClickCounter = 1; currentClickCounter <= numberOfClicks; currentClickCounter++) {
				LXInfositePageModel.ticketAddButton(mExpectedDataTktWdgt.getTicketName(), ticket.travellerType)
					.perform(click());
				mExpectedDataTktWdgt.updateTravellers(currentClickCounter, ticket.travellerType);
			}
		}
		LXInfositePageModel.priceSummary(mExpectedDataTktWdgt.getTicketName()).check(
			matches(withText(mExpectedDataTktWdgt.expectedSummary())));
		LXInfositePageModel.bookNowButton(mExpectedDataTktWdgt.getTicketName()).check(matches(
			withTotalPrice(mExpectedDataTktWdgt.getTotalPrice())));
		LXInfositePageModel.bookNowButton(mExpectedDataTktWdgt.getTicketName()).perform(click());
		screenshot("LX validated the offers widget");
	}
}
