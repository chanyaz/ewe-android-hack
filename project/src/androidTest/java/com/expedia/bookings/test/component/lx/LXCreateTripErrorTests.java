package com.expedia.bookings.test.component.lx;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.rules.ExpediaMockWebServerRule;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.utils.DateUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static com.expedia.bookings.test.ui.espresso.ViewActions.waitFor;

@RunWith(AndroidJUnit4.class)
public class LXCreateTripErrorTests {

	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.lx_details_presenter);

	@Rule
	public final ExpediaMockWebServerRule server = new ExpediaMockWebServerRule();

	Gson gson = new GsonBuilder().create();

	@Before
	public void before() {
		//Search Params
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.location = "New York";
		searchParams.startDate = DateUtils.yyyyMMddToLocalDate("2015-03-25");
		searchParams.endDate = DateUtils.yyyyMMddToLocalDate("2015-04-08");
		Events.post(new Events.LXNewSearchParamsAvailable(searchParams));

		//Select Activity
		Events.post(new Events.LXActivitySelected(new LXActivity()));
	}

	@Test
	public void testCreateTripError() {

		LXViewModel.progressDetails().perform(waitFor(10L, TimeUnit.SECONDS));
		//Select Offer which will return mocked data for create trip failure.
		Offer lxOffer = gson.fromJson("{\"id\": \"error_activity_id\", \"title\": \"2-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"2d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }", Offer.class);
		Map<Ticket, Integer> selectedTickets = new LinkedHashMap<>();
		Ticket adultTicket = gson.fromJson("{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }", Ticket.class);

		adultTicket.money = new Money(adultTicket.amount, "USD");

		selectedTickets.put(adultTicket, 3);
		lxOffer.updateAvailabilityInfoOfSelectedDate(DateUtils.yyyyMMddHHmmssToLocalDate("2015-02-24 07:30:00"));

		Events.post(new Events.LXOfferBooked(lxOffer, selectedTickets));

		LXViewModel.alertDialogPositiveButton().check(matches(isDisplayed()));
	}
}
