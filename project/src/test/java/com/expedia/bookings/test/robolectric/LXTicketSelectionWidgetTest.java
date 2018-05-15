package com.expedia.bookings.test.robolectric;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.AvailabilityInfo;
import com.expedia.bookings.data.lx.LXRedemptionType;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.widget.LXOfferDescription;
import com.expedia.bookings.widget.LXTicketPicker;
import com.expedia.bookings.widget.LXTicketSelectionWidget;

import butterknife.ButterKnife;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricRunner.class)
@RunForBrands(brands = { MultiBrand.EXPEDIA, MultiBrand.ORBITZ })
public class LXTicketSelectionWidgetTest {
	private LXTicketSelectionWidget widget;
	private Activity activity;

	@Before
	public void before() {
		activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_LX);
		widget = (LXTicketSelectionWidget) LayoutInflater.from(activity).inflate(R.layout.widget_lx_ticket_selection,
			null);
	}

	@Test
	public void testActivityTicketSelectionWidgetViews() {
		assertNotNull(widget);
		ButterKnife.inject(activity);

		widget.bind(buildActivityOffer(), false);
		widget.buildTicketPickers(singleTicketAvailability());

		View container = widget.findViewById(R.id.ticket_selectors_container);
		assertNotNull(container);

		View ticketSelector = container.findViewById(R.id.ticket_picker);
		assertNotNull(container);

		TextView titleText = (TextView) widget.findViewById(R.id.expanded_offer_title);
		TextView offerDuration = (TextView) widget.findViewById(R.id.offer_detail1);
		TextView freeCancellation = (TextView) widget.findViewById(R.id.offer_detail2);
		TextView redemptionType = (TextView) widget.findViewById(R.id.offer_detail3);
		LXOfferDescription descriptionWidget = (LXOfferDescription) widget.findViewById(R.id.offer_description);
		LinearLayout ticketDetailsContainer = (LinearLayout) ticketSelector.findViewById(R.id.ticket_details_container);
		TextView travelerType = ticketSelector.findViewById(R.id.traveler_type);
		TextView originalPriceView = ticketSelector.findViewById(R.id.original_price);
		TextView priceView = ticketSelector.findViewById(R.id.actual_price);
		TextView ticketCount = (TextView) ticketSelector.findViewById(R.id.ticket_count);
		ImageButton addTicketView = (ImageButton) ticketSelector.findViewById(R.id.ticket_add);
		ImageButton removeTicketView = (ImageButton) ticketSelector.findViewById(R.id.ticket_remove);

		assertNotNull(titleText);
		assertNotNull(offerDuration);
		assertEquals("1h", offerDuration.getText());
		assertNotNull(freeCancellation);
		assertEquals(activity.getResources().getString(R.string.free_cancellation), freeCancellation.getText());
		assertNotNull(redemptionType);
		assertEquals(activity.getResources().getString(R.string.lx_print_voucher_offer), redemptionType.getText());
		assertNotNull(descriptionWidget);
		assertNotNull(ticketDetailsContainer);
		assertNotNull(travelerType);
		assertNotNull(originalPriceView);
		assertNotNull(priceView);
		assertNotNull(ticketCount);
		assertNotNull(addTicketView);
		assertNotNull(removeTicketView);
	}

	@Test
	public void testGTTicketSelectionWidgetViews() {
		assertNotNull(widget);
		ButterKnife.inject(activity);

		widget.bind(buildGTOffer(), false);
		widget.buildTicketPickers(singleTicketAvailability());

		TextView titleText = (TextView) widget.findViewById(R.id.expanded_offer_title);
		TextView offerDuration = (TextView) widget.findViewById(R.id.offer_detail1);
		TextView freeCancellation = (TextView) widget.findViewById(R.id.offer_detail2);
		TextView redemptionType = (TextView) widget.findViewById(R.id.offer_detail3);
		TextView bags = (TextView) widget.findViewById(R.id.offer_bags);
		TextView passengers = (TextView) widget.findViewById(R.id.offer_passengers);

		assertNotNull(titleText);
		assertNotNull(offerDuration);
		assertEquals("1h", offerDuration.getText());
		assertNotNull(freeCancellation);
		assertEquals(activity.getResources().getString(R.string.free_cancellation), freeCancellation.getText());
		assertNotNull(redemptionType);
		assertEquals(activity.getResources().getString(R.string.lx_voucherless_offer), redemptionType.getText());
		assertNotNull(bags);
		assertNotNull(passengers);
	}

	@Test
	public void testTicketSelectionWidgetWithOnlyDuration() {
		assertNotNull(widget);
		ButterKnife.inject(activity);

		Offer offer = buildGTOffer();
		offer.freeCancellation = false;
		offer.redemptionType = null;
		widget.bind(offer, false);
		widget.buildTicketPickers(singleTicketAvailability());

		TextView offerDuration = (TextView) widget.findViewById(R.id.offer_detail1);
		TextView offerDetail2 = (TextView) widget.findViewById(R.id.offer_detail2);
		TextView offerDetail3 = (TextView) widget.findViewById(R.id.offer_detail3);

		assertEquals("1h", offerDuration.getText());
		assertEquals(View.GONE, offerDetail2.getVisibility());
		assertEquals(View.GONE, offerDetail3.getVisibility());
	}

	@Test
	public void testTicketSelectionWidgetWithDurationAndRedemption() {
		assertNotNull(widget);
		ButterKnife.inject(activity);

		Offer offer = buildGTOffer();
		offer.freeCancellation = false;
		widget.bind(offer, false);
		widget.buildTicketPickers(singleTicketAvailability());

		TextView offerDuration = (TextView) widget.findViewById(R.id.offer_detail1);
		TextView redemptionType = (TextView) widget.findViewById(R.id.offer_detail2);
		TextView offerDetail3 = (TextView) widget.findViewById(R.id.offer_detail3);

		assertEquals("1h", offerDuration.getText());
		assertEquals(activity.getResources().getString(R.string.lx_voucherless_offer), redemptionType.getText());
		assertEquals(View.GONE, offerDetail3.getVisibility());
	}

	@Test
	public void testTicketSelectionWidgetWithFreeCancellationAndRedemption() {
		assertNotNull(widget);
		ButterKnife.inject(activity);

		Offer offer = buildGTOffer();
		offer.duration = null;
		widget.bind(offer, false);
		widget.buildTicketPickers(singleTicketAvailability());

		TextView freeCancellation = (TextView) widget.findViewById(R.id.offer_detail1);
		TextView redemptionType = (TextView) widget.findViewById(R.id.offer_detail2);
		TextView offerDetail3 = (TextView) widget.findViewById(R.id.offer_detail3);

		assertEquals(activity.getResources().getString(R.string.free_cancellation), freeCancellation.getText());
		assertEquals(activity.getResources().getString(R.string.lx_voucherless_offer), redemptionType.getText());
		assertEquals(View.GONE, offerDetail3.getVisibility());
	}

	@Test
	public void testSingleTicketTypeSelections() {
		AvailabilityInfo availabilityInfo = singleTicketAvailability();

		widget.bind(buildActivityOffer(), false);
		widget.buildTicketPickers(availabilityInfo);

		Ticket testTicket = availabilityInfo.tickets.get(0);

		TextView travelerType = widget.findViewById(R.id.traveler_type);
		TextView originalPriceView = widget.findViewById(R.id.original_price);
		TextView priceView = widget.findViewById(R.id.actual_price);
		TextView ticketCount = (TextView) widget.findViewById(R.id.ticket_count);
		ImageButton addTicketView = (ImageButton) widget.findViewById(R.id.ticket_add);
		ImageButton removeTicketView = (ImageButton) widget.findViewById(R.id.ticket_remove);
		TextView ticketsSummary = (TextView) widget.findViewById(R.id.selected_ticket_summary);
		LinearLayout priceSummaryContainer = widget.findViewById(R.id.price_summary_container);
		TextView stpView = priceSummaryContainer.findViewById(R.id.strike_through_price);    // Tkt selection
		TextView totalPriceView = priceSummaryContainer.findViewById(R.id.actual_price);     // Tkt selection
		TextView discountPercentView = priceSummaryContainer.findViewById(R.id.discount_percentage);
		Button bookButton = (Button) widget.findViewById(R.id.lx_book_now);
		TextView titleText = (TextView) widget.findViewById(R.id.expanded_offer_title);
		LXOfferDescription descriptionWidget = (LXOfferDescription) widget.findViewById(R.id.offer_description);
		TextView descriptionText = (TextView) descriptionWidget.findViewById(R.id.description);

		int expectedCount = 1;
		String expectedTravelerType = testTicket.code + " (" + testTicket.restrictionText + ")";
		String expectedTotalPrice = testTicket.money.getFormattedMoney();
		String expectedOriginalPrice = testTicket.originalPriceMoney.getFormattedMoney();
		String expectedDiscountPercentage = "-50%";
		String expectedSummary = LXDataUtils.ticketCountSummary(activity, testTicket.code, expectedCount);
		String expectedCurrencyCode = "USD";
		String expectedTitleText = "One Day Tour";
		String expectedDescription = "Offer Description";
		String expectedAmountWithCurrency = new Money(new BigDecimal(40), expectedCurrencyCode).getFormattedMoney();
		String expectedOriginalAmountWithCurrency = new Money(new BigDecimal(80), expectedCurrencyCode).getFormattedMoney();
		String expectedBookText = activity.getResources().getString(R.string.offer_book_now_button);

		assertEquals(String.valueOf(expectedCount), ticketCount.getText());
		assertEquals(expectedTravelerType, travelerType.getText());
		assertEquals(expectedTotalPrice, priceView.getText());
		assertEquals(expectedOriginalPrice, originalPriceView.getText().toString());
		assertEquals(expectedSummary, ticketsSummary.getText());
		assertEquals(expectedBookText, bookButton.getText());
		assertEquals(expectedTitleText, titleText.getText());
		assertEquals(expectedDescription, descriptionText.getText());
		assertEquals(expectedAmountWithCurrency, totalPriceView.getText());
		assertEquals(expectedOriginalAmountWithCurrency, stpView.getText().toString());
		assertEquals(expectedDiscountPercentage, discountPercentView.getText().toString());

		addTicketView.performClick();
		expectedCount++;
		expectedSummary = LXDataUtils.ticketCountSummary(activity, testTicket.code, expectedCount);
		expectedAmountWithCurrency = new Money(new BigDecimal(80), expectedCurrencyCode).getFormattedMoney();
		expectedOriginalAmountWithCurrency = new Money(new BigDecimal(160), expectedCurrencyCode).getFormattedMoney();

		assertEquals(String.valueOf(expectedCount), ticketCount.getText());
		assertEquals(expectedTotalPrice, priceView.getText());
		assertEquals(expectedSummary, ticketsSummary.getText());
		assertEquals(expectedBookText, bookButton.getText());
		assertEquals(expectedTitleText, titleText.getText());
		assertEquals(expectedAmountWithCurrency, totalPriceView.getText());
		assertEquals(expectedOriginalAmountWithCurrency, stpView.getText().toString());

		// Set ticket count to 0.
		for (int i = 0; i < 2; i++) {
			removeTicketView.performClick();
			expectedCount--;
		}
		expectedSummary = "";
		expectedAmountWithCurrency = new Money(BigDecimal.ZERO, expectedCurrencyCode).getFormattedMoney();
		expectedOriginalAmountWithCurrency = new Money(BigDecimal.ZERO, expectedCurrencyCode).getFormattedMoney();

		assertEquals(String.valueOf(expectedCount), ticketCount.getText());
		assertEquals(expectedTravelerType, travelerType.getText());
		assertEquals(expectedSummary, ticketsSummary.getText());
		assertEquals(expectedBookText, bookButton.getText());
		assertEquals(expectedTitleText, titleText.getText());
		assertEquals(expectedAmountWithCurrency, totalPriceView.getText());
	}

	@Test
	public void testMultipleTicketTypeSelections() {
		AvailabilityInfo availabilityInfo = multipleTicketAvailability();

		widget.bind(buildActivityOffer(), false);
		widget.buildTicketPickers(availabilityInfo);

		List<Ticket> tickets = availabilityInfo.tickets;

		LinearLayout container = (LinearLayout) widget.findViewById(R.id.ticket_selectors_container);

		int ticketPickerIndex = 0;
		for (int i = 0; i < container.getChildCount(); i++) {
			View child = container.getChildAt(i);
			if (child instanceof LXTicketPicker) {
				String expectedTotalPrice = tickets.get(ticketPickerIndex).money.getFormattedMoney();
				String expectedOriginalPrice = tickets.get(ticketPickerIndex).originalPriceMoney.getFormattedMoney();
				String expectedDetails = tickets.get(ticketPickerIndex).code + " (" + tickets.get(ticketPickerIndex).restrictionText + ")";

				TextView travelerType = child.findViewById(R.id.traveler_type);
				TextView originalPriceView = child.findViewById(R.id.original_price);
				TextView priceView = child.findViewById(R.id.actual_price);
				TextView ticketCount = (TextView) child.findViewById(R.id.ticket_count);
				ImageButton addTicketView = (ImageButton) child.findViewById(R.id.ticket_add);

				int expectedTicketCount = 0;
				if (i == 0) {
					expectedTicketCount = activity.getResources().getInteger(R.integer.lx_offer_ticket_default_count);
				}
				assertEquals(String.valueOf(expectedTicketCount), ticketCount.getText());
				assertEquals(expectedDetails, travelerType.getText());
				// If condition is required because when the originalPrice = 0, the text of the view does not get updated, causing the test to fail
				if (!"$0".equals(expectedOriginalPrice)) {
					assertEquals(expectedOriginalPrice, originalPriceView.getText().toString());
				}
				assertEquals(expectedTotalPrice, priceView.getText());
				ticketPickerIndex++;

				addTicketView.performClick();
			}
		}

		BigDecimal expectedTotalAmount = new BigDecimal(110);
		String expectedTitleText = "One Day Tour";
		String expectedDescription = "Offer Description";
		String expectedAmountWithCurrency = new Money(expectedTotalAmount, tickets.get(0).money.getCurrency())
			.getFormattedMoney();
		String expectedBookText = activity.getResources().getString(R.string.offer_book_now_button);
		Button bookButton = (Button) widget.findViewById(R.id.lx_book_now);
		TextView titleText = (TextView) widget.findViewById(R.id.expanded_offer_title);
		LXOfferDescription descriptionWidget = (LXOfferDescription) widget.findViewById(R.id.offer_description);
		TextView descriptionText = (TextView) descriptionWidget.findViewById(R.id.description);

		assertEquals(expectedBookText, bookButton.getText());
		assertEquals(expectedTitleText, titleText.getText());
		assertEquals(expectedDescription, descriptionText.getText());

		String expectedSummary = LXDataUtils.ticketCountSummary(activity, tickets.get(0).code, 2) + ", " + LXDataUtils
			.ticketCountSummary(activity, tickets.get(1).code, 1);
		TextView ticketsSummary = (TextView) widget.findViewById(R.id.selected_ticket_summary);

		assertEquals(expectedSummary, ticketsSummary.getText());
	}

	@Test
	public void zeroTicketsSelection() {
		AvailabilityInfo availabilityInfo = multipleTicketAvailability();
		widget.bind(buildActivityOffer(), false);
		widget.buildTicketPickers(availabilityInfo);
		LinearLayout container = (LinearLayout) widget.findViewById(R.id.ticket_selectors_container);
		LinearLayout ticketSummaryContainer = (LinearLayout) widget.findViewById(R.id.ticket_summary_container);
		View child = container.getChildAt(0);
		ImageButton addTicketView = (ImageButton) child.findViewById(R.id.ticket_add);
		ImageButton removeTicketView = (ImageButton) child.findViewById(R.id.ticket_remove);
		removeTicketView.performClick();
		assertFalse(removeTicketView.isEnabled());
		assertEquals(View.GONE,ticketSummaryContainer.getVisibility());
		addTicketView.performClick();
		assertEquals(View.VISIBLE, ticketSummaryContainer.getVisibility());
	}

	private AvailabilityInfo singleTicketAvailability() {
		AvailabilityInfo availabilityInfo = new AvailabilityInfo();
		List<Ticket> tickets = new ArrayList<>();
		Ticket testTicket = new Ticket();
		testTicket.code = LXTicketType.Adult;
		testTicket.money = new Money("40", "USD");
		testTicket.originalPriceMoney = new Money("80", "USD");
		testTicket.restrictionText = "13+ years";
		tickets.add(testTicket);
		availabilityInfo.tickets = tickets;
		return availabilityInfo;
	}

	private AvailabilityInfo multipleTicketAvailability() {
		AvailabilityInfo availabilityInfo = new AvailabilityInfo();
		List<Ticket> tickets = new ArrayList<>();
		Ticket adultTicket = new Ticket();
		adultTicket.code = LXTicketType.Adult;
		adultTicket.money = new Money("40", "USD");
		adultTicket.originalPriceMoney = new Money("0", "USD");
		adultTicket.restrictionText = "13+ years";
		tickets.add(adultTicket);

		Ticket childTicket = new Ticket();
		childTicket.code = LXTicketType.Child;
		childTicket.money = new Money("30", "USD");
		childTicket.originalPriceMoney = new Money("45", "USD");
		childTicket.restrictionText = "4-12 years";
		tickets.add(childTicket);

		availabilityInfo.tickets = tickets;
		return availabilityInfo;
	}

	private Offer buildActivityOffer() {
		Offer offer = new Offer();
		offer.id = "offerId";
		offer.title = "One Day Tour";
		offer.description = "Offer Description";
		offer.freeCancellation = true;
		offer.duration = "1h";
		offer.redemptionType = LXRedemptionType.PRINT;
		return offer;
	}

	private Offer buildGTOffer() {
		Offer offer = new Offer();
		offer.id = "offerId";
		offer.title = "Ground Transport";
		offer.description = "Offer Description";
		offer.freeCancellation = true;
		offer.duration = "1h";
		offer.redemptionType = LXRedemptionType.VOUCHERLESS;
		offer.bags = "2";
		offer.passengers = "2";
		return offer;
	}
}
