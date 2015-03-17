package com.expedia.bookings.test.component.lx;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.lx.LXOfferSelected;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.rules.ExpediaMockWebServerRule;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.utils.DateUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class LXCheckoutPresenterTests {
	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.test_lx_checkout_presenter);

	@Rule
	public final ExpediaMockWebServerRule server = new ExpediaMockWebServerRule();

	@Before
	public void before() {
		//Setup LXState

		//Search Params
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.location = "New York";
		searchParams.startDate = DateUtils.yyyyMMddToLocalDate("2015-02-24");
		searchParams.endDate = DateUtils.yyyyMMddToLocalDate("2015-03-09");
		Events.post(new Events.LXNewSearchParamsAvailable(searchParams));

		Gson gson = new GsonBuilder().create();

		//Select Activity
		LXActivity lxActivity = gson.fromJson("{\"id\": \"183615\", \"title\": \"New York Pass: Visit up to 80 Attractions, Museums & Tours\", \"description\": \"<p>Whether you know exactly where you want to visit or you're improvising, The New York Pass offers something just right for you.</p>\", \"images\": [{\"url\": \"//a.travel-assets.com/mediavault.le/media/c932f66857388ec282910f62d64354eff6760223.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/4da13469be6dffa5ff880b0a3cc59cb58e6690bc.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/efe62c374095946276d943831420ccad01e47396.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/cc66fbb8e40c74c670f32124aac534e29128e01b.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/909d80624ff13a43a950e0149adf49bf23195774.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"} ], \"highlights\": [\"<p>Admission to more than 80 top attractions, museums &amp; tours</p>\", \"<p>VIP Fast-Track entry at several top attractions</p>\", \"<p>200-page guidebook with area maps &amp; attraction details</p>\", \"<p>Guidebook offered in multiple languages</p>\", \"<p>7-Day Pass for the 5-Day Pass price when you buy now</p>\"], \"fromPrice\": \"$130\", \"fromOriginalPrice\": \"\", \"fromPriceTicketType\": \"Adult\", \"startDate\": \"2015-02-24\", \"endDate\": \"2015-02-24\", \"maximumBookingLength\": null, \"lastValidDate\": null, \"firstValidDate\": null, \"duration\": \"2d\", \"inclusions\": [\"<p>Admission to more than 80 top attractions, museums, &amp; tours</p>\", \"<p>200-page guidebook offered in English, Italian, Spanish, French, German, and Brazilian Portuguese</p>\", \"<p>Skip-the-Line access at many attractions</p>\", \"<p>Special discounts at restaurants &amp; retailers</p>\"], \"isMultiDuration\": \"true\", \"exclusions\": [\"<p>Transportation to and from attractions</p>\"], \"cancellationPolicyText\": \"72 hours\", \"knowBeforeYouBook\": [\"<p>Children 3 and younger are complimentary at most attractions.</p>\", \"<p>Pass is valid for the number of consecutive calendar days you purchase, beginning on the first day of use.</p>\", \"<p>Advance reservations may be made for tours included on the pass; mention you are a New York Pass holder.</p>\", \"<p>Hours and dates of operation for individual attractions vary.</p>\", \"<p>Get the 7-Day Pass for the 5-Day Price when you buy now, savings reflected in the price above.</p>\"], \"freeCancellation\": true, \"discountPercentage\": 0, \"address\": \"Gray Line New York Visitors Center, 777 8th Avenue\\r\\nbetween 47th and 48th Street, New York, NY 10036, United States\", \"location\": \"New York, United States\", \"regionId\": \"178293\", \"destination\": \"New York\", \"fullName\": \"New York (and vicinity), New York, United States of America\", \"omnitureJson\": \"{\\\"accountName\\\":\\\"expedia1\\\",\\\"omnitureProperties\\\":{\\\"server\\\":\\\"www.expedia.com\\\",\\\"authChannel\\\":\\\"SIGNIN_FORM\\\",\\\"eVar6\\\":\\\"0\\\",\\\"eVar5\\\":\\\"4\\\",\\\"eVar4\\\":\\\"D\\\\u003dc4\\\",\\\"eVar2\\\":\\\"D\\\\u003dc2\\\",\\\"channel\\\":\\\"local expert\\\",\\\"pageName\\\":\\\"page.LX.Infosite.Information\\\",\\\"products\\\":\\\"LX;Merchant LX:183615\\\",\\\"activityId\\\":\\\"4f04feaa-fdf0-4f40-8de1-6ffa262e28f3\\\",\\\"prop30\\\":\\\"1033\\\",\\\"prop11\\\":\\\"null\\\",\\\"prop13\\\":\\\"0\\\",\\\"prop12\\\":\\\"eba1f647-61a7-406f-8bda-6f0c7124e789\\\",\\\"prop34\\\":\\\"6880.0|6611.0|6727.0|5242.0|6524.1|6620.0|5150.1|6271.1\\\",\\\"events\\\":\\\"event3\\\",\\\"charSet\\\":\\\"UTF-8\\\",\\\"eVar34\\\":\\\"D\\\\u003dc34\\\",\\\"eVar56\\\":\\\"RewardsStatus()\\\",\\\"eVar55\\\":\\\"unknown\\\",\\\"eVar54\\\":\\\"1033\\\",\\\"eVar18\\\":\\\"D\\\\u003dpageName\\\",\\\"eVar17\\\":\\\"D\\\\u003dpageName\\\",\\\"prop6\\\":\\\"2015-02-24\\\",\\\"prop5\\\":\\\"2015-02-24\\\",\\\"prop4\\\":\\\"178293\\\",\\\"list1\\\":\\\"6880.0|6611.0|6727.0|5242.0|6524.1|6620.0|5150.1|6271.1\\\",\\\"prop2\\\":\\\"local expert\\\",\\\"prop1\\\":\\\"4\\\",\\\"userType\\\":\\\"ANONYMOUS\\\"}}\", \"offersDetail\": {\"offers\": [{\"id\": \"183619\", \"title\": \"2-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"2d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183621\", \"title\": \"3-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"3d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90046\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$180\", \"originalPrice\": \"\", \"amount\": \"180\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90047\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$140\", \"originalPrice\": \"\", \"amount\": \"140\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183623\", \"title\": \"5-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"5d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90054\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$210\", \"originalPrice\": \"\", \"amount\": \"210\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90055\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$155\", \"originalPrice\": \"\", \"amount\": \"155\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183625\", \"title\": \"7-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"7d\", \"discountPercentage\": \"8\", \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90062\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$210\", \"originalPrice\": \"$230\", \"amount\": \"210\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90063\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$155\", \"originalPrice\": \"$165\", \"amount\": \"155\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null } ], \"priceFootnote\": \"*Taxes included\", \"sameDateSearch\": true }, \"dateAdjusted\": false, \"typeGT\": false, \"passengers\": null, \"bags\": null, \"metaDescription\": \"Whether you know exactly where you want to visit or you're improvising, The New York Pass offers something just right for you.\", \"metaKeywords\": \"New York Pass: Visit up to 80 Attractions, Museums & Tours, New York, United States, New York Attractions, New York Cruises & Water Tours, New York Sightseeing Passes, New York Tours & Sightseeing, New York Activities, New York Things To Do , Book New York Activities , Book New York Things To Do , Activities, Things To Do\", \"pageTitle\": \"New York Pass: Visit up to 80 Attractions, Museums &amp; Tours\", \"category\": \"Attractions\"}", LXActivity.class);
		Events.post(new Events.LXActivitySelected(lxActivity));

		//Select Date
		Events.post(new Events.LXDetailsDateChanged(new LocalDate(2015, 2, 24)));

		//Select Offer
		Offer lxOffer = gson.fromJson("{\"id\": \"183619\", \"title\": \"2-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"2d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }", Offer.class);
		Map<Ticket, Integer> selectedTickets = new LinkedHashMap<>();
		Ticket adultTicket = gson.fromJson("{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }", Ticket.class);
		Ticket childTicket = gson.fromJson("{\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 }", Ticket.class);
		selectedTickets.put(adultTicket, 3);
		selectedTickets.put(childTicket, 1);
		lxOffer.getAvailabilityInfoOfSelectedDate(DateUtils.yyyyMMddHHmmssToLocalDate("2015-02-24 07:30:00"));

		LXOfferSelected offerSelected = new LXOfferSelected(lxOffer, selectedTickets);
		Events.post(new Events.LXOfferBooked(lxOffer, offerSelected));

		//Make Create Trip Succeed
		LXCreateTripResponse lxCreateTripResponse = gson.fromJson("{tripId:\"c0eb37f7-9553-441b-975a-877eff95e8fa\", itineraryNumber:11528775160, activityId:\"0c0af1cb-f721-4cbf-8b99-e4f753cb6caa\", validFormsOfPayment:[{name:\"AmericanExpress\"}, {name:\"Diner's Club International\"}, {name:\"Discover\"}, {name:\"JCB\"}, {name:\"MasterCard\"}, {name:\"Visa\"} ] }", LXCreateTripResponse.class);
		Events.post(new Events.LXCreateTripSucceeded(lxCreateTripResponse));
		ScreenActions.delay(2);
	}

	@Test
	public void testVisibilitiesAndOfferDetailsOnCheckout() {
		LXViewModel.checkoutWidget().check(matches(isDisplayed()));
		LXViewModel.checkoutOfferTitle().check(matches(withText("2-Day New York Pass")));
		LXViewModel.checkoutGroupText().check(matches(withText("3 Adult, 1 Child")));
		LXViewModel.checkoutOfferLocation().check(matches(withText("New York, United States")));
		LXViewModel.checkoutOfferDate().check(matches(withText("2015-02-24 07:30:00")));
		LXViewModel.checkoutGrandTotalText().check(matches(withText("Total with Tax")));
		LXViewModel.checkoutPriceText().check(matches(withText("$500")));
		LXViewModel.checkoutFreeCancellationText().check(matches(withText("Free Cancellation")));

		LXViewModel.checkoutSignInCard().check(matches(isDisplayed()));
		LXViewModel.checkoutContactInfoCard().check(matches(isDisplayed()));
		LXViewModel.checkoutPaymentInfoCard().check(matches(isDisplayed()));
		LXViewModel.checkoutSlideToPurchase().check(matches(not(isDisplayed())));
	}

}
