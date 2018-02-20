package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.deeplink.ActivityDeepLink;
import com.expedia.bookings.deeplink.DeepLink;
import com.expedia.bookings.deeplink.DeepLinkParser;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.utils.ApiDateUtils;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class LXDataUtilsTest {
	private Context getContext() {
		return RuntimeEnvironment.application;
	}

	private Ticket getAdultTicket(int count) {
		Ticket adultTicket = new Ticket();
		adultTicket.code = LXTicketType.Adult;
		adultTicket.count = count;
		return adultTicket;
	}

	private Ticket getChildTicket(int count) {
		Ticket childTicket = new Ticket();
		childTicket.code = LXTicketType.Child;
		childTicket.count = count;
		return childTicket;
	}

	@Test
	public void testTicketsCountSummary() {
		List<Ticket> tickets = new ArrayList<>();

		tickets.add(getAdultTicket(1));
		tickets.add(getChildTicket(1));
		assertEquals(LXDataUtils.ticketsCountSummary(getContext(), tickets), "1 Adult, 1 Child");

		tickets.add(getAdultTicket(1));
		assertEquals(LXDataUtils.ticketsCountSummary(getContext(), tickets), "2 Adults, 1 Child");

		tickets.add(getChildTicket(1));
		assertEquals(LXDataUtils.ticketsCountSummary(getContext(), tickets), "2 Adults, 2 Children");
	}

	@Test
	public void testTicketCountSummary() {
		assertEquals(LXDataUtils.ticketCountSummary(getContext(), LXTicketType.Adult, 0), "");
		assertEquals(LXDataUtils.ticketCountSummary(getContext(), LXTicketType.Adult, 1), "1 Adult");
		assertEquals(LXDataUtils.ticketCountSummary(getContext(), LXTicketType.Adult, 2), "2 Adults");
		assertEquals(LXDataUtils.ticketCountSummary(getContext(), LXTicketType.Adult, 5), "5 Adults");

		assertEquals(LXDataUtils.ticketCountSummary(getContext(), LXTicketType.Child, 0), "");
		assertEquals(LXDataUtils.ticketCountSummary(getContext(), LXTicketType.Child, 1), "1 Child");
		assertEquals(LXDataUtils.ticketCountSummary(getContext(), LXTicketType.Child, 2), "2 Children");
		assertEquals(LXDataUtils.ticketCountSummary(getContext(), LXTicketType.Child, 5), "5 Children");
	}

	@Test
	public void testTicketDisplayName() {
		assertEquals(LXDataUtils.ticketDisplayName(getContext(), LXTicketType.Adult), "Adult");
		assertEquals(LXDataUtils.ticketDisplayName(getContext(), LXTicketType.Child), "Child");
	}

	@Test
	public void testPerTicketTypeDisplayLabel() {
		assertEquals(LXDataUtils.perTicketTypeDisplayLabel(getContext(), LXTicketType.Adult), "per adult");
		assertEquals(LXDataUtils.perTicketTypeDisplayLabel(getContext(), LXTicketType.Child), "per child");
	}

	@Test
	public void testBuildLXSearchParamsFromDeeplinkSearch() {
		final String expectedURL = "expda://activitySearch?startDate=2050-08-08&location=San+Francisco";
		final String location = "San Francisco";
		final String startDate = ApiDateUtils
			.localDateToyyyyMMdd(ApiDateUtils.ensureDateIsTodayOrInFuture(ApiDateUtils.yyyyMMddToLocalDate("2050-08-08")));

		LxSearchParams obtainedLxSearchParams = getLxSearchParamsFromDeeplink(expectedURL);

		LxSearchParams expectedLxSearchParams = (LxSearchParams) new LxSearchParams.Builder().location(location)
			.startDate(ApiDateUtils.yyyyMMddToLocalDate(startDate)).endDate(ApiDateUtils.yyyyMMddToLocalDate(startDate).plusDays(14)).build();

		assertEquals(expectedLxSearchParams.getLocation(), obtainedLxSearchParams.getLocation());
		assertEquals(expectedLxSearchParams.getActivityStartDate(), obtainedLxSearchParams.getActivityStartDate());
		assertEquals(expectedLxSearchParams.getActivityEndDate(), obtainedLxSearchParams.getActivityEndDate());
	}

	@Test
	public void testBuildLXSearchParamsFromDeeplinkSearchWithFilters() {
		final String expectedURL = "expda://activitySearch?startDate=2050-08-08&location=San+Francisco&filters=Private+Transfers|Shared+Transfers";
		final String location = "San Francisco";
		final String startDate = ApiDateUtils
			.localDateToyyyyMMdd(ApiDateUtils.ensureDateIsTodayOrInFuture(ApiDateUtils.yyyyMMddToLocalDate("2050-08-08")));
		final String filters = "Private Transfers|Shared Transfers";

		LxSearchParams obtainedLxSearchParams = getLxSearchParamsFromDeeplink(expectedURL);

		LxSearchParams expectedLxSearchParams = (LxSearchParams) new LxSearchParams.Builder().filters(filters).location(location)
			.startDate(ApiDateUtils.yyyyMMddToLocalDate(startDate)).endDate(ApiDateUtils.yyyyMMddToLocalDate(startDate).plusDays(14)).build();

		assertEquals(expectedLxSearchParams.getLocation(), obtainedLxSearchParams.getLocation());
		assertEquals(expectedLxSearchParams.getFilters(), obtainedLxSearchParams.getFilters());
		assertEquals(expectedLxSearchParams.getActivityStartDate(), obtainedLxSearchParams.getActivityStartDate());
		assertEquals(expectedLxSearchParams.getActivityEndDate(), obtainedLxSearchParams.getActivityEndDate());
	}

	@Test
	public void testBuildLXSearchParamsEmptyURL() {
		// URL with no params
		final String emptyParamsURL = "expda://activitySearch";

		// URL with no date.
		final String missingDateURL = "expda://activitySearch?location=San Francisco";

		LxSearchParams searchParamsFromEmptyParamsURL = getLxSearchParamsFromDeeplink(emptyParamsURL);
		LxSearchParams searchParamsFromMissingDateURL = getLxSearchParamsFromDeeplink(missingDateURL);


		// Default to today's date, in case of an incorrect URL.
		assertEquals(searchParamsFromEmptyParamsURL.getActivityStartDate(), LocalDate.now());
		assertEquals(searchParamsFromMissingDateURL.getActivityStartDate(), LocalDate.now());
	}

	@Test
	public void lxSearchParamsFromHotelParams() {
		LocalDate checkinDate = new LocalDate();
		Location location = new Location();
		location.setCity("San francisco");
		location.setStateCode("SFO");
		LocalDate checkoutDate = new LocalDate().plusDays(10);
		String locationText = getContext().getResources().getString(
			R.string.lx_destination_TEMPLATE, location.getCity(),
			Strings.isEmpty(location.getStateCode()) ? location.getCountryCode() : location.getStateCode());
		// Expected params.
		LxSearchParams expectedSearchParams = (LxSearchParams) new LxSearchParams.Builder()
			.searchType(SearchType.EXPLICIT_SEARCH).location(locationText).startDate(checkinDate).endDate(checkoutDate)
			.build();

		LxSearchParams obtainedSearchParams = LXDataUtils.fromHotelParams(getContext(), checkinDate, checkoutDate, location);

		assertEquals(expectedSearchParams.getLocation(), obtainedSearchParams.getLocation());
		assertEquals(expectedSearchParams.getSearchType(), obtainedSearchParams.getSearchType());
		assertEquals(expectedSearchParams.getActivityStartDate(), obtainedSearchParams.getActivityStartDate());
		assertEquals(expectedSearchParams.getActivityEndDate(), obtainedSearchParams.getActivityEndDate());

	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
		MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS})
	public void getToolbarSearchDateTextTest() {
		LocalDate startDate = new LocalDate(2016, 4, 23);
		LocalDate endDate = new LocalDate(2016, 4, 23).plusDays(2);
		LxSearchParams searchParams = (LxSearchParams) new LxSearchParams.Builder().location("NewYork").startDate(startDate)
			.endDate(endDate).build();

		String toolbarSearchDateText = LXDataUtils.getToolbarSearchDateText(getContext(), searchParams, false);
		assertEquals(toolbarSearchDateText, "Apr 23 - Apr 25");

		String toolbarSearchDateTextContDesc = LXDataUtils.getToolbarSearchDateText(getContext(), searchParams, true);
		assertEquals(toolbarSearchDateTextContDesc, "Apr 23 to Apr 25");
	}

	private LxSearchParams getLxSearchParamsFromDeeplink(String expectedURL) {
		DeepLinkParser deepLinkParser = new DeepLinkParser(RuntimeEnvironment.application.getAssets());
		DeepLink deepLink = deepLinkParser.parseDeepLink(Uri.parse(expectedURL));
		return LXDataUtils.buildLXSearchParamsFromDeeplink((ActivityDeepLink) deepLink, getContext());
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY })
	public void bindPriceAndTicketTypeTest() {
		TextView activityPrice = new TextView(getContext());
		TextView fromPriceTicketType = new TextView(getContext());
		LXDataUtils.bindPriceAndTicketType(getContext(), LXTicketType.Traveler, new Money("180", "USD"),
			new Money("0", "USD"), activityPrice, fromPriceTicketType);

		assertEquals("$180", activityPrice.getText());
		assertEquals("Price is $180 per traveler", activityPrice.getContentDescription().toString());
		assertEquals("per traveler", fromPriceTicketType.getText());

		LXDataUtils.bindPriceAndTicketType(getContext(), LXTicketType.Traveler, new Money("180", "USD"),
			new Money("220", "USD"), activityPrice, fromPriceTicketType);

		assertEquals("$180", activityPrice.getText());
		assertEquals("Price is $180 per traveler. Price before discount was $220 per traveler", activityPrice.getContentDescription().toString());
		assertEquals("per traveler", fromPriceTicketType.getText());
	}

	@Test
	public void bindRecommendationTest() {
		TextView recommendationText = new TextView(getContext());
		TextView recommendationScore = new TextView(getContext());

		LXDataUtils.bindRecommendation(getContext(), 63, recommendationScore, recommendationText);
		assertEquals("3.2", recommendationScore.getText());
		assertEquals("of 5", recommendationText.getText());
		assertEquals("Activity Rated, 3.2 of 5", recommendationText.getContentDescription());

		LXDataUtils.bindRecommendation(getContext(), 89, recommendationScore, recommendationText);
		assertEquals("4.5", recommendationScore.getText());
		assertEquals("of 5 - Great!", recommendationText.getText());

		LXDataUtils.bindRecommendation(getContext(), 43, recommendationScore, recommendationText);
		assertEquals(View.GONE, recommendationScore.getVisibility());
		assertEquals(View.GONE, recommendationText.getVisibility());
	}

	@Test
	public void bindDiscountPercentageTest() {
		TextView discountPercentage = new TextView(getContext());

		LXActivity activity1 = new LXActivity();
		activity1.discountPercentage = 10;

		LXDataUtils.bindDiscountPercentage(getContext(), activity1, discountPercentage);
		assertEquals("-10%", discountPercentage.getText());
		assertEquals("10% discount on original pricing", discountPercentage.getContentDescription());

		LXActivity activity2 = new LXActivity();

		LXDataUtils.bindDiscountPercentage(getContext(), activity2, discountPercentage);
		assertEquals(View.GONE, discountPercentage.getVisibility());
	}

	@Test
	public void incrementTicketCountForVolumePricingTest() {
		Ticket.LxTicketPrices priceObject1 = new Ticket.LxTicketPrices();
		priceObject1.originalPrice = "￥0";
		priceObject1.travellerNum = 1;
		priceObject1.amount = "4305.0";
		priceObject1.price = "￥4,305";

		Ticket.LxTicketPrices priceObject2 = new Ticket.LxTicketPrices();
		priceObject2.originalPrice = "￥0";
		priceObject2.travellerNum = 3;
		priceObject2.amount = "4305.0";
		priceObject2.price = "￥4,305";

		List<Ticket.LxTicketPrices> prices = new ArrayList<>();
		prices.add(priceObject1);
		prices.add(priceObject2);

		assertEquals(3, LXDataUtils.incrementTicketCountForVolumePricing(1, prices));
		assertEquals(1, LXDataUtils.incrementTicketCountForVolumePricing(0, prices));
		assertEquals(3, LXDataUtils.incrementTicketCountForVolumePricing(3, prices));
		assertEquals(4, LXDataUtils.incrementTicketCountForVolumePricing(4, prices));

		assertEquals(1, LXDataUtils.decrementTicketCountForVolumePricing(3, prices));
		assertEquals(0, LXDataUtils.decrementTicketCountForVolumePricing(1, prices));
		assertEquals(0, LXDataUtils.decrementTicketCountForVolumePricing(0, prices));
		assertEquals(4, LXDataUtils.decrementTicketCountForVolumePricing(4, prices));
	}

	private List<Ticket> getSelectedTickets(Gson gson) {
		List<Ticket> selectedTickets = new ArrayList<>();

		Ticket adultTicket = gson.fromJson(
				"{\"code\": \"Adult\",\"count\": \"3 \", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"$145\", \"amount\": \"130\", \"originalAmount\": \"145\", \"displayName\": null, \"defaultTicketCount\": 2 }",
				Ticket.class);
		adultTicket.money = new Money(adultTicket.amount, "USD");
		adultTicket.originalPriceMoney = new Money(adultTicket.originalAmount, "USD");

		Ticket childTicket = gson.fromJson(
				"{\"code\": \"Child\",\"count\": \"1\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"$0\", \"amount\": \"110\", \"originalAmount\": \"0\", \"displayName\": null, \"defaultTicketCount\": 0 }",
				Ticket.class);
		childTicket.money = new Money(childTicket.amount, "USD");
		childTicket.originalPriceMoney = new Money(childTicket.originalAmount, "USD");

		Ticket adultTicket1 = gson.fromJson(
				"{\"code\": \"Adult\",\"count\": \"3\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"$145\", \"amount\": \"130\", \"originalAmount\": \"145\", \"displayName\": null, \"defaultTicketCount\": 2, \"prices\": [{\"originalPrice\": \"$145\", \"travellerNum\": 2, \"amount\": \"125\", \"originalAmount\": \"145\", \"price\": \"$125\"}, {\n" +
						"\"originalPrice\": \"$0\",\n" +
						"\"cost\": null,\n" +
						"\"travellerNum\": 3,\n" +
						"\"amount\": \"39.65\",\n" +
						"\"originalAmount\": \"0\",\n" +
						"\"price\": \"$39.65\"\n" +
						"}] }",
				Ticket.class);
		adultTicket1.money = new Money(adultTicket1.amount, "USD");
		adultTicket1.originalPriceMoney = new Money(adultTicket1.originalAmount, "USD");

		List<Ticket.LxTicketPrices> adultPrices = new ArrayList<>();
		Ticket.LxTicketPrices adultPrice = adultTicket1.prices.get(0);
		adultPrice.money = new Money(adultPrice.amount, "USD");
		adultPrice.originalPriceMoney = new Money(adultPrice.originalAmount, "USD");

		Ticket.LxTicketPrices adultPrice1 = adultTicket1.prices.get(1);
		adultPrice1.money = new Money(adultPrice1.amount, "USD");
		adultPrice1.originalPriceMoney = new Money(adultPrice1.originalAmount, "USD");

		adultPrices.add(adultPrice);
		adultPrices.add(adultPrice1);
		adultTicket1.prices = adultPrices;

		selectedTickets.add(adultTicket);
		selectedTickets.add(childTicket);
		selectedTickets.add(adultTicket1);
		return selectedTickets;
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.VOYAGES})
	public void testAddPriceSummaryRow() {
		LinearLayout viewGroup = new LinearLayout(getContext());
		Gson gson = new GsonBuilder().create();
		List<Ticket> selectedTickets = getSelectedTickets(gson);

		LXDataUtils.addPriceSummaryRow(getContext(), viewGroup, selectedTickets.get(0));
		TextView stpView = viewGroup.getChildAt(0).findViewById(R.id.strike_through_price);
		TextView travelerPriceView = viewGroup.getChildAt(0).findViewById(R.id.traveler_price);
		Assert.assertEquals("$145", stpView.getText().toString());
		Assert.assertEquals("$130/Adult", travelerPriceView.getText());
		Assert.assertEquals("Regular Price per adult $145. Discounted price per adult $130", travelerPriceView.getContentDescription());

		LXDataUtils.addPriceSummaryRow(getContext(), viewGroup, selectedTickets.get(1));
		stpView = viewGroup.getChildAt(1).findViewById(R.id.strike_through_price);
		travelerPriceView = viewGroup.getChildAt(1).findViewById(R.id.traveler_price);
		Assert.assertEquals("", stpView.getText());
		Assert.assertEquals(View.GONE, stpView.getVisibility());
		Assert.assertEquals("$110/Child", travelerPriceView.getText());
		Assert.assertEquals("Price is $110 per child", travelerPriceView.getContentDescription());
	}

	@Test
	public void testGetPriceMoneyMap() {
		Gson gson = new GsonBuilder().create();
		List<Ticket> selectedTickets = getSelectedTickets(gson);

		HashMap<String, Money> moneyMap1 = LXDataUtils.getPriceMoneyMap(selectedTickets.get(0), 0);
		HashMap<String, Money> moneyMap2 = LXDataUtils.getPriceMoneyMap(selectedTickets.get(2), 0);
		HashMap<String, Money> moneyMap3 = LXDataUtils.getPriceMoneyMap(selectedTickets.get(2), 3);

		Assert.assertEquals(new Money("130", "USD"), moneyMap1.get("perTicketPrice"));
		Assert.assertEquals(new Money("125", "USD"), moneyMap2.get("perTicketPrice"));
		Assert.assertEquals(new Money("39.65", "USD"), moneyMap3.get("perTicketPrice"));
	}
}
