package com.expedia.bookings.test.component.lx;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.ViewInteraction;
import android.support.test.runner.AndroidJUnit4;
import android.widget.RadioGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.rules.ExpediaMockWebServerRule;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.widget.LXOffersListWidget;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class LXDetailsPresenterTests {
	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.lx_details_presenter);

	@Rule
	public final ExpediaMockWebServerRule server = new ExpediaMockWebServerRule();

	@Before
	public void before() {
		// To setup LXState
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.location = "New York";
		searchParams.startDate = DateUtils.yyyyMMddToLocalDate("2015-02-24");
		searchParams.endDate = DateUtils.yyyyMMddToLocalDate("2015-03-09");
		Events.post(new Events.LXNewSearchParamsAvailable(searchParams));
	}

	@Test
	public void testActivityDetails() {
		Events.post(new Events.LXActivitySelected(new LXActivity()));
		LXViewModel.progressDetails().check(matches(isDisplayed()));
		ScreenActions.delay(2);

		LXViewModel.detailsWidget().check(matches(isDisplayed()));
		LXViewModel.detailsWidget().check(matches(hasDescendant(withId(R.id.activity_gallery))));
		LXViewModel.detailsWidget().check(matches(hasDescendant(withId(R.id.activity_info_container))));
		LXViewModel.detailsWidget().check(matches(hasDescendant(withId(R.id.description))));
		LXViewModel.detailsWidget().check(matches(hasDescendant(withId(R.id.location))));
		LXViewModel.detailsWidget().check(matches(hasDescendant(withId(R.id.highlights))));
		LXViewModel.detailsWidget().check(matches(hasDescendant(withId(R.id.offers))));
		LXViewModel.detailsWidget().check(matches(hasDescendant(withId(R.id.offer_dates_container))));
	}

	@Test
	public void testToolbar() {
		Events.post(new Events.LXActivitySelected(new LXActivity()));
		ScreenActions.delay(2);
		LXViewModel.toolbar().check(matches(isDisplayed()));

		String expectedToolbarDateRange = "Feb 24 - Mar 09";
		ViewInteraction toolbar = LXViewModel.toolbar();
		toolbar.check(matches(isDisplayed()));
		toolbar.check(matches(hasDescendant(withText(expectedToolbarDateRange))));
		toolbar.check(matches(hasDescendant(withText("New York"))));
	}

	@Test
	public void testActivityInfo() {
		Events.post(new Events.LXActivitySelected(new LXActivity()));
		ScreenActions.delay(2);

		ViewInteraction info = LXViewModel.infoContainer();
		info.check(matches(hasDescendant(allOf(withId(R.id.title), withText(startsWith("New York Pass"))))));
		info.check(matches(hasDescendant(allOf(withId(R.id.price), withText("$130")))));
		info.check(matches(hasDescendant(allOf(withId(R.id.category), withText("Attractions")))));
	}

	@Test
	public void testActivityOffers() {
		Events.post(new Events.LXActivitySelected(new LXActivity()));
		ScreenActions.delay(2);

		//Ensure that we have 4 offers!
		LXOffersListWidget offersListWidget = (LXOffersListWidget) playground.getRoot().findViewById(R.id.offers);
		int offersCount = offersListWidget.getChildCount();
		assertTrue(offersCount == 4);

		//Check 1st offer
		ViewInteraction firstOffer = LXViewModel.withOfferText("2-Day New York Pass");
		firstOffer.check(matches(hasDescendant(allOf(withId(R.id.offer_title), withText(startsWith("2-Day New York Pass"))))));
		firstOffer.check(matches(hasDescendant(allOf(withId(R.id.select_tickets), withText(startsWith("Select Tickets"))))));
		firstOffer.check(matches(hasDescendant(allOf(withId(R.id.price_summary), withText(startsWith("$130 Adult, $110 Child"))))));

		//Check 2nd offer
		ViewInteraction secondOffer = LXViewModel.withOfferText("3-Day New York Pass");
		secondOffer.check(matches(hasDescendant(allOf(withId(R.id.offer_title), withText(startsWith("3-Day New York Pass"))))));
		secondOffer.check(matches(hasDescendant(allOf(withId(R.id.select_tickets), withText(startsWith("Select Tickets"))))));
		secondOffer.check(matches(hasDescendant(allOf(withId(R.id.price_summary), withText(startsWith("$180 Adult, $140 Child"))))));

		//Check 3rd offer
		ViewInteraction thirdOffer = LXViewModel.withOfferText("5-Day New York Pass");
		thirdOffer.check(matches(hasDescendant(allOf(withId(R.id.offer_title), withText(startsWith("5-Day New York Pass"))))));
		thirdOffer.check(matches(hasDescendant(allOf(withId(R.id.select_tickets), withText(startsWith("Select Tickets"))))));
		thirdOffer.check(matches(hasDescendant(allOf(withId(R.id.price_summary), withText(startsWith("$210 Adult, $155 Child"))))));

		//Check 4th offer
		ViewInteraction fourthOffer = LXViewModel.withOfferText("7-Day New York Pass");
		fourthOffer.check(matches(hasDescendant(allOf(withId(R.id.offer_title), withText(startsWith("7-Day New York Pass"))))));
		fourthOffer.check(matches(hasDescendant(allOf(withId(R.id.select_tickets), withText(startsWith("Select Tickets"))))));
		fourthOffer.check(matches(hasDescendant(allOf(withId(R.id.price_summary), withText(startsWith("$210 Adult, $155 Child"))))));
	}

	@Test
	public void testOffersExpandCollapse() {
		Events.post(new Events.LXActivitySelected(new LXActivity()));
		ScreenActions.delay(2);

		ViewInteraction firstOfferTicketPicker = LXViewModel.ticketPicker("2-Day New York Pass");
		ViewInteraction secondOfferTicketPicker = LXViewModel.ticketPicker("3-Day New York Pass");
		ViewInteraction thirdOfferTicketPicker = LXViewModel.ticketPicker("5-Day New York Pass");
		ViewInteraction fourthOfferTicketPicker = LXViewModel.ticketPicker("7-Day New York Pass");

		firstOfferTicketPicker.check(matches(not(isDisplayed())));
		secondOfferTicketPicker.check(matches(not(isDisplayed())));
		thirdOfferTicketPicker.check(matches(not(isDisplayed())));
		fourthOfferTicketPicker.check(matches(not(isDisplayed())));

		ViewInteraction firstOfferSelectTicket = LXViewModel.selectTicketsButton("2-Day New York Pass");
		firstOfferSelectTicket.perform(scrollTo(), click());

		firstOfferTicketPicker.check(matches(isDisplayed()));
		secondOfferTicketPicker.check(matches(not(isDisplayed())));
		thirdOfferTicketPicker.check(matches(not(isDisplayed())));
		fourthOfferTicketPicker.check(matches(not(isDisplayed())));

		ViewInteraction secondOfferSelectTicket = LXViewModel.selectTicketsButton("3-Day New York Pass");
		secondOfferSelectTicket.perform(scrollTo(), click());

		firstOfferTicketPicker.check(matches(not(isDisplayed())));
		secondOfferTicketPicker.check(matches(isDisplayed()));
		thirdOfferTicketPicker.check(matches(not(isDisplayed())));
		fourthOfferTicketPicker.check(matches(not(isDisplayed())));

		ViewInteraction thirdOfferSelectTicket = LXViewModel.selectTicketsButton("5-Day New York Pass");
		thirdOfferSelectTicket.perform(scrollTo(), click());

		firstOfferTicketPicker.check(matches(not(isDisplayed())));
		secondOfferTicketPicker.check(matches(not(isDisplayed())));
		thirdOfferTicketPicker.check(matches(isDisplayed()));
		fourthOfferTicketPicker.check(matches(not(isDisplayed())));
	}

	@Test
	public void testDatesContainer() {
		Events.post(new Events.LXActivitySelected(new LXActivity()));
		ScreenActions.delay(2);

		RadioGroup container = (RadioGroup) playground.getRoot().findViewById(R.id.offer_dates_container);
		int count = container.getChildCount();
		Assert.assertEquals(playground.get().getResources().getInteger(R.integer.lx_default_search_range), count);
	}

	@Test
	public void testDatesContainerSelection() {
		Events.post(new Events.LXActivitySelected(new LXActivity()));
		ScreenActions.delay(2);

		LXViewModel.detailsDate("25").perform(click());

		ViewInteraction offer = LXViewModel.withOfferText("2-Day New York Pass");
		offer.check(matches(hasDescendant(allOf(withId(R.id.select_tickets),
			withText(startsWith("Select Tickets")), not(isEnabled())))));
		offer.check(matches(hasDescendant(allOf(withId(R.id.price_summary), withText(startsWith(""))))));

	}
}
