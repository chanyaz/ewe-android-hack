package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCheckoutParams;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.DateUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LXStateTestUtil {

	private static final Gson gson = new GsonBuilder().create();

	public static void selectActivityState() {
		LXActivity lxActivity = gson.fromJson(
			"{\"id\": \"183615\", \"title\": \"New York Pass: Visit up to 80 Attractions, Museums & Tours\", \"description\": \"<p>Whether you know exactly where you want to visit or you're improvising, The New York Pass offers something just right for you.</p>\", \"images\": [{\"url\": \"//a.travel-assets.com/mediavault.le/media/c932f66857388ec282910f62d64354eff6760223.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/4da13469be6dffa5ff880b0a3cc59cb58e6690bc.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/efe62c374095946276d943831420ccad01e47396.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/cc66fbb8e40c74c670f32124aac534e29128e01b.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/909d80624ff13a43a950e0149adf49bf23195774.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"} ], \"highlights\": [\"<p>Admission to more than 80 top attractions, museums &amp; tours</p>\", \"<p>VIP Fast-Track entry at several top attractions</p>\", \"<p>200-page guidebook with area maps &amp; attraction details</p>\", \"<p>Guidebook offered in multiple languages</p>\", \"<p>7-Day Pass for the 5-Day Pass price when you buy now</p>\"], \"fromPrice\": \"$130\", \"fromOriginalPrice\": \"\", \"fromPriceTicketType\": \"Adult\", \"startDate\": \"2015-02-24\", \"endDate\": \"2015-02-24\", \"maximumBookingLength\": null, \"lastValidDate\": null, \"firstValidDate\": null, \"duration\": \"2d\", \"inclusions\": [\"<p>Admission to more than 80 top attractions, museums, &amp; tours</p>\", \"<p>200-page guidebook offered in English, Italian, Spanish, French, German, and Brazilian Portuguese</p>\", \"<p>Skip-the-Line access at many attractions</p>\", \"<p>Special discounts at restaurants &amp; retailers</p>\"], \"isMultiDuration\": \"true\", \"exclusions\": [\"<p>Transportation to and from attractions</p>\"], \"freeCancellationMinHours\": 72, \"knowBeforeYouBook\": [\"<p>Children 3 and younger are complimentary at most attractions.</p>\", \"<p>Pass is valid for the number of consecutive calendar days you purchase, beginning on the first day of use.</p>\", \"<p>Advance reservations may be made for tours included on the pass; mention you are a New York Pass holder.</p>\", \"<p>Hours and dates of operation for individual attractions vary.</p>\", \"<p>Get the 7-Day Pass for the 5-Day Price when you buy now, savings reflected in the price above.</p>\"], \"freeCancellation\": true, \"discountPercentage\": 0, \"address\": \"Gray Line New York Visitors Center, 777 8th Avenue\\r\\nbetween 47th and 48th Street, New York, NY 10036, United States\", \"location\": \"New York, United States\", \"regionId\": \"178293\", \"destination\": \"New York\", \"fullName\": \"New York (and vicinity), New York, United States of America\", \"omnitureJson\": \"{\\\"accountName\\\":\\\"expedia1\\\",\\\"omnitureProperties\\\":{\\\"server\\\":\\\"www.expedia.com\\\",\\\"authChannel\\\":\\\"SIGNIN_FORM\\\",\\\"eVar6\\\":\\\"0\\\",\\\"eVar5\\\":\\\"4\\\",\\\"eVar4\\\":\\\"D\\\\u003dc4\\\",\\\"eVar2\\\":\\\"D\\\\u003dc2\\\",\\\"channel\\\":\\\"local expert\\\",\\\"pageName\\\":\\\"page.LX.Infosite.Information\\\",\\\"products\\\":\\\"LX;Merchant LX:183615\\\",\\\"activityId\\\":\\\"4f04feaa-fdf0-4f40-8de1-6ffa262e28f3\\\",\\\"prop30\\\":\\\"1033\\\",\\\"prop11\\\":\\\"null\\\",\\\"prop13\\\":\\\"0\\\",\\\"prop12\\\":\\\"eba1f647-61a7-406f-8bda-6f0c7124e789\\\",\\\"prop34\\\":\\\"6880.0|6611.0|6727.0|5242.0|6524.1|6620.0|5150.1|6271.1\\\",\\\"events\\\":\\\"event3\\\",\\\"charSet\\\":\\\"UTF-8\\\",\\\"eVar34\\\":\\\"D\\\\u003dc34\\\",\\\"eVar56\\\":\\\"RewardsStatus()\\\",\\\"eVar55\\\":\\\"unknown\\\",\\\"eVar54\\\":\\\"1033\\\",\\\"eVar18\\\":\\\"D\\\\u003dpageName\\\",\\\"eVar17\\\":\\\"D\\\\u003dpageName\\\",\\\"prop6\\\":\\\"2015-02-24\\\",\\\"prop5\\\":\\\"2015-02-24\\\",\\\"prop4\\\":\\\"178293\\\",\\\"list1\\\":\\\"6880.0|6611.0|6727.0|5242.0|6524.1|6620.0|5150.1|6271.1\\\",\\\"prop2\\\":\\\"local expert\\\",\\\"prop1\\\":\\\"4\\\",\\\"userType\\\":\\\"ANONYMOUS\\\"}}\", \"offersDetail\": {\"offers\": [{\"id\": \"183619\", \"title\": \"2-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"2d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183621\", \"title\": \"3-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"3d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90046\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$180\", \"originalPrice\": \"\", \"amount\": \"180\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90047\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$140\", \"originalPrice\": \"\", \"amount\": \"140\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183623\", \"title\": \"5-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"5d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90054\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$210\", \"originalPrice\": \"\", \"amount\": \"210\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90055\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$155\", \"originalPrice\": \"\", \"amount\": \"155\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183625\", \"title\": \"7-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"7d\", \"discountPercentage\": \"8\", \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90062\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$210\", \"originalPrice\": \"$230\", \"amount\": \"210\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90063\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$155\", \"originalPrice\": \"$165\", \"amount\": \"155\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null } ], \"priceFootnote\": \"*Taxes included\", \"sameDateSearch\": true }, \"dateAdjusted\": false, \"typeGT\": false, \"passengers\": null, \"bags\": null, \"metaDescription\": \"Whether you know exactly where you want to visit or you're improvising, The New York Pass offers something just right for you.\", \"metaKeywords\": \"New York Pass: Visit up to 80 Attractions, Museums & Tours, New York, United States, New York Attractions, New York Cruises & Water Tours, New York Sightseeing Passes, New York Tours & Sightseeing, New York Activities, New York Things To Do , Book New York Activities , Book New York Things To Do , Activities, Things To Do\", \"pageTitle\": \"New York Pass: Visit up to 80 Attractions, Museums &amp; Tours\", \"category\": \"Attractions\"}",
			LXActivity.class);
		Events.post(new Events.LXActivitySelected(lxActivity));
	}

	public static void offerSelected() {
		Offer lxOffer = gson.fromJson(
			"{\"id\": \"183619\", \"title\": \"2-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"2d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }",
			Offer.class);
		List<Ticket> selectedTickets = new ArrayList<>();
		Ticket adultTicket = gson.fromJson(
			"{\"code\": \"Adult\", \"count\": \"3\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }",
			Ticket.class);
		Ticket childTicket = gson.fromJson(
			"{\"code\": \"Child\", \"count\": \"1\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 }",
			Ticket.class);
		selectedTickets.add(adultTicket);
		selectedTickets.add(childTicket);
		for (Ticket ticket : selectedTickets) {
			ticket.money = new Money(ticket.amount, "USD");
		}
		lxOffer.updateAvailabilityInfoOfSelectedDate(DateUtils.yyyyMMddHHmmssToLocalDate("2015-02-24 07:30:00"));
		Events.post(new Events.LXOfferBooked(lxOffer, selectedTickets));
	}

	public static void checkoutSuccessState() {

		LXCheckoutParams checkoutParams = new LXCheckoutParams()
			.email("coolguy@expedia.com");
		Events.post(new Events.LXKickOffCheckoutCall(checkoutParams));

		LXCheckoutResponse checkoutResponse = gson.fromJson(
			"{\"activityId\": \"9afe3fd5-13d7-4d57-924f-b377683af928\", \"currencyCode\": \"USD\", \"newTrip\": { \"itineraryNumber\": \"7666328719\", \"travelRecordLocator\": \"15251504\", \"tripId\": \"f9a4aea0-8756-47b7-aa7d-586d5b454184\" }, \"orderId\": \"000000\", \"totalCharges\": \"64.07\", \"totalChargesPrice\": { \"amount\": \"64.07\", \"formattedPrice\": \"$64.07\", \"formattedWholePrice\": \"$64\" }}",
			LXCheckoutResponse.class);
		Events.post(new Events.LXCheckoutSucceeded(checkoutResponse));
	}

	public static void searchParamsState() {
		String location = "New York";
		LocalDate startDate = DateUtils.yyyyMMddToLocalDate("2015-03-25");
		LocalDate endDate = DateUtils.yyyyMMddToLocalDate("2015-04-08");
		LxSearchParams searchParams = (LxSearchParams) new LxSearchParams.Builder().location(location)
			.startDate(startDate).endDate(endDate).build();

		Events.post(new Events.LXNewSearchParamsAvailable(searchParams));
	}

}
