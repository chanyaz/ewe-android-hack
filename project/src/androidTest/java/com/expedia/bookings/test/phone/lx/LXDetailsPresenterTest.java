package com.expedia.bookings.test.phone.lx;

import org.hamcrest.CoreMatchers;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.ViewInteraction;
import android.support.test.runner.AndroidJUnit4;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.rules.ExpediaMockWebServerRule;
import com.expedia.bookings.test.rules.PlaygroundRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.customScroll;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class LXDetailsPresenterTest {
	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.lx_details_presenter, R.style.V2_Theme_LX);

	@Rule
	public final ExpediaMockWebServerRule server = new ExpediaMockWebServerRule();

	@Before
	public void before() {
		// To setup LXState
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.location = "New York";
		searchParams.startDate = LocalDate.now();
		searchParams.endDate = LocalDate.now().plusDays(13);
		Events.post(new Events.LXNewSearchParamsAvailable(searchParams));
	}

	@Test
	public void testActivityDetails() {
		Events.post(new Events.LXActivitySelected(new LXActivity()));
		LXScreen.waitForDetailsDisplayed();

		LXScreen.detailsWidget().check(matches(isDisplayed()));
		LXScreen.detailsWidget().check(matches(hasDescendant(withId(R.id.activity_gallery))));
		LXScreen.detailsWidget().check(matches(hasDescendant(withId(R.id.description))));
		LXScreen.detailsWidget().check(matches(hasDescendant(withId(R.id.location))));
		LXScreen.detailsWidget().check(matches(hasDescendant(withId(R.id.highlights))));
		LXScreen.detailsWidget().check(matches(hasDescendant(withId(R.id.offers))));
		LXScreen.detailsWidget().check(matches(hasDescendant(withId(R.id.offer_dates_container))));
		LXScreen.detailsWidget().check(matches(hasDescendant(withId(R.id.inclusions))));
		LXScreen.detailsWidget().check(matches(hasDescendant(withId(R.id.exclusions))));
		LXScreen.detailsWidget().check(matches(hasDescendant(withId(R.id.know_before_you_book))));
		LXScreen.detailsWidget().check(matches(hasDescendant(withId(R.id.cancellation))));

	}

	@Test
	public void testToolbar() {
		String title = "Activity title";
		LXActivity lxActivity = new LXActivity();
		lxActivity.title = title;
		Events.post(new Events.LXActivitySelected(lxActivity));
		LXScreen.waitForDetailsDisplayed();
		LXScreen.toolbar().check(matches(isDisplayed()));

		String expectedToolbarDateRange = String
			.format("%1$s - %2$s", LocalDate.now().toString("MMM d"), LocalDate.now().plusDays(13).toString("MMM d"));
		ViewInteraction toolbar = LXScreen.toolbar();
		toolbar.check(matches(isDisplayed()));
		toolbar.check(matches(hasDescendant(withText(expectedToolbarDateRange))));
		toolbar.check(matches(hasDescendant(withText(title))));
	}

	@Test
	public void testActivityOffers() {
		Events.post(new Events.LXActivitySelected(new LXActivity()));
		LXScreen.waitForDetailsDisplayed();


		//Ensure that we have 4 offers!
		LinearLayout offersContainer = (LinearLayout) playground.getRoot().findViewById(R.id.offers_container);
		int offersCount = offersContainer.getChildCount();
		int offersToShow = playground.getActivity().getResources().getInteger(R.integer.lx_offers_list_initial_size);
		assertTrue(offersCount == offersToShow);

		//Check 1st offer
		ViewInteraction firstOffer = LXScreen.withOfferText("2-Day New York Pass");
		firstOffer.check(matches(hasDescendant(allOf(withId(R.id.offer_title), withText(startsWith("2-Day New York Pass"))))));
		firstOffer.check(matches(hasDescendant(allOf(withId(R.id.select_tickets), withText(startsWith("Select Tickets"))))));
		firstOffer.check(matches(hasDescendant(allOf(withId(R.id.price_summary), withText(startsWith("$130 Adult, $110 Child"))))));

		//Check 2nd offer
		ViewInteraction secondOffer = LXScreen.withOfferText("3-Day New York Pass");
		secondOffer.check(matches(hasDescendant(allOf(withId(R.id.offer_title), withText(startsWith("3-Day New York Pass"))))));
		secondOffer.check(matches(hasDescendant(allOf(withId(R.id.select_tickets), withText(startsWith("Select Tickets"))))));
		secondOffer.check(matches(hasDescendant(allOf(withId(R.id.price_summary), withText(startsWith("$180 Adult, $140 Child"))))));

		//Check 3rd offer
		ViewInteraction thirdOffer = LXScreen.withOfferText("5-Day New York Pass");
		thirdOffer.check(
			matches(hasDescendant(allOf(withId(R.id.offer_title), withText(startsWith("5-Day New York Pass"))))));
		thirdOffer.check(
			matches(hasDescendant(allOf(withId(R.id.select_tickets), withText(startsWith("Select Tickets"))))));
		thirdOffer.check(
			matches(hasDescendant(allOf(withId(R.id.price_summary), withText(startsWith("$210 Adult, $155 Child"))))));

		//Scroll to show more
		thirdOffer.perform(customScroll());
		onView(
			CoreMatchers.allOf(withId(R.id.section_content), hasSibling(CoreMatchers.allOf(withId(R.id.section_title),
				withText(R.string.description_activity_details))))).perform(customScroll());
		LXScreen.showMore().perform(customScroll(),click());

		//Check 4th offer
		ViewInteraction fourthOffer = LXScreen.withOfferText("7-Day New York Pass");
		fourthOffer.check(matches(hasDescendant(allOf(withId(R.id.offer_title), withText(startsWith("7-Day New York Pass"))))));
		fourthOffer.check(matches(hasDescendant(allOf(withId(R.id.select_tickets), withText(startsWith("Select Tickets"))))));
		fourthOffer.check(matches(hasDescendant(allOf(withId(R.id.price_summary), withText(startsWith("$210 Adult, $155 Child"))))));
	}

	@Test
	public void testOffersExpandCollapse() {
		Events.post(new Events.LXActivitySelected(new LXActivity()));
		LXScreen.waitForDetailsDisplayed();

		ViewInteraction firstOfferTicketPicker = LXScreen.ticketPicker("2-Day New York Pass");
		ViewInteraction secondOfferTicketPicker = LXScreen.ticketPicker("3-Day New York Pass");
		ViewInteraction thirdOfferTicketPicker = LXScreen.ticketPicker("5-Day New York Pass");

		firstOfferTicketPicker.check(matches(not(isDisplayed())));
		secondOfferTicketPicker.check(matches(not(isDisplayed())));
		thirdOfferTicketPicker.check(matches(not(isDisplayed())));

		ViewInteraction firstOfferSelectTicket = LXScreen.selectTicketsButton("2-Day New York Pass");
		LXScreen.withOfferText("2-Day New York Pass").perform(customScroll());
		firstOfferSelectTicket.perform(customScroll(), click());

		firstOfferTicketPicker.check(matches(isDisplayed()));
		secondOfferTicketPicker.check(matches(not(isDisplayed())));
		thirdOfferTicketPicker.check(matches(not(isDisplayed())));

		ViewInteraction secondOfferSelectTicket = LXScreen.selectTicketsButton("3-Day New York Pass");
		LXScreen.withOfferText("3-Day New York Pass").perform(customScroll());
		secondOfferSelectTicket.perform(customScroll(), click());

		firstOfferTicketPicker.check(matches(not(isDisplayed())));
		secondOfferTicketPicker.check(matches(isDisplayed()));
		thirdOfferTicketPicker.check(matches(not(isDisplayed())));

		ViewInteraction thirdOfferSelectTicket = LXScreen.selectTicketsButton("5-Day New York Pass");
		LXScreen.withOfferText("5-Day New York Pass").perform(customScroll());
		thirdOfferSelectTicket.perform(customScroll(), click());

		firstOfferTicketPicker.check(matches(not(isDisplayed())));
		secondOfferTicketPicker.check(matches(not(isDisplayed())));
		thirdOfferSelectTicket.perform(customScroll(50));
		thirdOfferTicketPicker.check(matches(isDisplayed()));

		//Scroll to show more and display all the offers.
		LXScreen.showMore().perform(customScroll(), click());
		ViewInteraction fourthOfferTicketPicker = LXScreen.ticketPicker("7-Day New York Pass");

		fourthOfferTicketPicker.check(matches(not(isDisplayed())));

		ViewInteraction fourthOfferSelectTicket = LXScreen.selectTicketsButton("7-Day New York Pass");
		fourthOfferSelectTicket.perform(customScroll(), click());

		firstOfferTicketPicker.check(matches(not(isDisplayed())));
		secondOfferTicketPicker.check(matches(not(isDisplayed())));
		thirdOfferTicketPicker.check(matches(not(isDisplayed())));
		fourthOfferTicketPicker.perform(customScroll(50));
		fourthOfferTicketPicker.check(matches(isDisplayed()));
	}

	@Test
	public void testDatesContainer() {
		Events.post(new Events.LXActivitySelected(new LXActivity()));
		LXScreen.waitForDetailsDisplayed();

		RadioGroup container = (RadioGroup) playground.getRoot().findViewById(R.id.offer_dates_container);
		int count = container.getChildCount();
		int range = playground.getActivity().getResources().getInteger(R.integer.lx_default_search_range) + 1;
		Assert.assertEquals(range, count);
	}

	@Test
	public void testDatesContainerSelection() {
		Events.post(new Events.LXActivitySelected(new LXActivity()));
		LXScreen.waitForDetailsDisplayed();

		LocalDate now = LocalDate.now();
		LocalDate withoutOfferDate = LocalDate.now().plusDays(14);

		LXScreen.detailsDate(now.dayOfWeek().getAsShortText() + "\n" + now.dayOfMonth().getAsText() + "\n" + now.monthOfYear().getAsShortText()).check(
			matches(isEnabled()));

		LXScreen.detailsDate(withoutOfferDate.dayOfWeek().getAsShortText() + "\n" + withoutOfferDate.dayOfMonth().getAsShortText() + "\n").check(
			matches(not(isEnabled())));
	}
}
