package com.expedia.bookings.test.robolectric;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowTelephonyManager;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.AvailabilityInfo;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.widget.LXOfferDescription;
import com.expedia.bookings.widget.LXTicketPicker;
import com.expedia.bookings.widget.LXTicketSelectionWidget;

import butterknife.ButterKnife;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricRunner.class)
public class LXTicketSelectionWidgetTest {
	private LXTicketSelectionWidget widget;
	private Activity activity;

	// This test hits Omniture which in turn throws NPE because of unavailability of operator information.
	@Before
	public void before() {
		TelephonyManager telephonyManager = (TelephonyManager) RuntimeEnvironment.application
			.getSystemService(Context.TELEPHONY_SERVICE);
		ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
		shadowTelephonyManager.setNetworkOperatorName("Test Operator");

		activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_LX);
		widget = (LXTicketSelectionWidget) LayoutInflater.from(activity).inflate(R.layout.widget_lx_ticket_selection,
			null);
	}

	@Test
	public void testActivityTicketSelectionWidgetViews() {
		assertNotNull(widget);
		ButterKnife.inject(activity);

		widget.bind(buildActivityOffer());
		widget.buildTicketPickers(singleTicketAvailability());

		View container = widget.findViewById(R.id.ticket_selectors_container);
		assertNotNull(container);

		View ticketSelector = container.findViewById(R.id.ticket_picker);
		assertNotNull(container);

		TextView titleText = (TextView) widget.findViewById(R.id.offer_title);
		TextView offerDuration = (TextView) widget.findViewById(R.id.offer_duration);
		TextView freeCancellation = (TextView) widget.findViewById(R.id.free_cancellation);
		LXOfferDescription descriptionWidget = (LXOfferDescription) widget.findViewById(R.id.offer_description);
		TextView ticketDetails = (TextView) ticketSelector.findViewById(R.id.ticket_details);
		TextView ticketCount = (TextView) ticketSelector.findViewById(R.id.ticket_count);
		ImageButton addTicketView = (ImageButton) ticketSelector.findViewById(R.id.ticket_add);
		ImageButton removeTicketView = (ImageButton) ticketSelector.findViewById(R.id.ticket_remove);

		assertNotNull(titleText);
		assertNotNull(offerDuration);
		assertNotNull(freeCancellation);
		assertNotNull(descriptionWidget);
		assertNotNull(ticketDetails);
		assertNotNull(ticketCount);
		assertNotNull(addTicketView);
		assertNotNull(removeTicketView);
	}

	@Test
	public void testGTTicketSelectionWidgetViews() {
		assertNotNull(widget);
		ButterKnife.inject(activity);

		widget.bind(buildGTOffer());
		widget.buildTicketPickers(singleTicketAvailability());

		TextView titleText = (TextView) widget.findViewById(R.id.offer_title);
		TextView offerDuration = (TextView) widget.findViewById(R.id.offer_duration);
		TextView freeCancellation = (TextView) widget.findViewById(R.id.free_cancellation);
		TextView bags = (TextView) widget.findViewById(R.id.offer_bags);
		TextView passengers = (TextView) widget.findViewById(R.id.offer_passengers);

		assertNotNull(titleText);
		assertNotNull(offerDuration);
		assertNotNull(freeCancellation);
		assertNotNull(bags);
		assertNotNull(passengers);
	}

	@Test
	public void testSingleTicketTypeSelections() {
		AvailabilityInfo availabilityInfo = singleTicketAvailability();

		widget.bind(buildActivityOffer());
		widget.buildTicketPickers(availabilityInfo);

		Ticket testTicket = availabilityInfo.tickets.get(0);

		TextView ticketDetails = (TextView) widget.findViewById(R.id.ticket_details);
		TextView ticketCount = (TextView) widget.findViewById(R.id.ticket_count);
		ImageButton addTicketView = (ImageButton) widget.findViewById(R.id.ticket_add);
		ImageButton removeTicketView = (ImageButton) widget.findViewById(R.id.ticket_remove);
		TextView ticketsSummary = (TextView) widget.findViewById(R.id.selected_ticket_summary);
		Button bookButton = (Button) widget.findViewById(R.id.lx_book_now);
		TextView titleText = (TextView) widget.findViewById(R.id.offer_title);
		LXOfferDescription descriptionWidget = (LXOfferDescription) widget.findViewById(R.id.offer_description);
		TextView descriptionText = (TextView) descriptionWidget.findViewById(R.id.description);

		int expectedCount = 1;
		String expectedDetails = String
			.format(activity.getResources().getString(R.string.ticket_details_template),
				testTicket.money.getFormattedMoney(),
				testTicket.code, testTicket.restrictionText);

		String expectedSummary = LXDataUtils.ticketCountSummary(activity, testTicket.code, expectedCount);
		String expectedCurrencyCode = "USD";
		String expectedTitleText = "One Day Tour";
		String expectedDescription = "Offer Description";
		String bookButtonTemplate = activity.getResources().getString(R.string.offer_book_now_TEMPLATE);
		String expectedAmountWithCurrency = new Money(new BigDecimal(40), expectedCurrencyCode).getFormattedMoney();
		String expectedBookText = String.format(bookButtonTemplate, expectedAmountWithCurrency);

		assertEquals(String.valueOf(expectedCount), ticketCount.getText());
		assertEquals(expectedDetails, ticketDetails.getText());
		assertEquals(expectedSummary, ticketsSummary.getText());
		assertEquals(expectedBookText, bookButton.getText());
		assertEquals(expectedTitleText, titleText.getText());
		assertEquals(expectedDescription, descriptionText.getText());

		addTicketView.performClick();
		expectedCount++;
		expectedSummary = LXDataUtils.ticketCountSummary(activity, testTicket.code, expectedCount);
		expectedAmountWithCurrency = new Money(new BigDecimal(80), expectedCurrencyCode).getFormattedMoney();
		expectedBookText = String.format(bookButtonTemplate, expectedAmountWithCurrency);

		assertEquals(String.valueOf(expectedCount), ticketCount.getText());
		assertEquals(expectedDetails, ticketDetails.getText());
		assertEquals(expectedSummary, ticketsSummary.getText());
		assertEquals(expectedBookText, bookButton.getText());
		assertEquals(expectedTitleText, titleText.getText());

		// Set ticket count to 0.
		for (int i = 0; i < 2; i++) {
			removeTicketView.performClick();
			expectedCount--;
		}
		expectedSummary = "";
		expectedAmountWithCurrency = new Money(BigDecimal.ZERO, expectedCurrencyCode).getFormattedMoney();
		expectedBookText = String.format(bookButtonTemplate, expectedAmountWithCurrency);

		assertEquals(String.valueOf(expectedCount), ticketCount.getText());
		assertEquals(expectedDetails, ticketDetails.getText());
		assertEquals(expectedSummary, ticketsSummary.getText());
		assertEquals(expectedBookText, bookButton.getText());
		assertEquals(expectedTitleText, titleText.getText());
	}

	@Test
	public void testMultipleTicketTypeSelections() {
		AvailabilityInfo availabilityInfo = multipleTicketAvailability();

		widget.bind(buildActivityOffer());
		widget.buildTicketPickers(availabilityInfo);

		List<Ticket> tickets = availabilityInfo.tickets;

		LinearLayout container = (LinearLayout) widget.findViewById(R.id.ticket_selectors_container);

		int ticketPickerIndex = 0;
		for (int i = 0; i < container.getChildCount(); i++) {
			View child = container.getChildAt(i);
			if (child instanceof LXTicketPicker) {
				String expectedDetails = String
					.format(activity.getResources().getString(R.string.ticket_details_template),
						tickets.get(ticketPickerIndex).money.getFormattedMoney(),
						tickets.get(ticketPickerIndex).code, tickets.get(ticketPickerIndex).restrictionText);

				TextView ticketDetails = (TextView) child.findViewById(R.id.ticket_details);
				TextView ticketCount = (TextView) child.findViewById(R.id.ticket_count);
				ImageButton addTicketView = (ImageButton) child.findViewById(R.id.ticket_add);

				int expectedTicketCount = 0;
				if (i == 0) {
					expectedTicketCount = activity.getResources().getInteger(R.integer.lx_offer_ticket_default_count);
				}
				assertEquals(String.valueOf(expectedTicketCount), ticketCount.getText());
				assertEquals(expectedDetails, ticketDetails.getText());
				ticketPickerIndex++;

				addTicketView.performClick();
			}
		}

		BigDecimal expectedTotalAmount = new BigDecimal(110);
		String expectedTitleText = "One Day Tour";
		String expectedDescription = "Offer Description";
		String expectedAmountWithCurrency = new Money(expectedTotalAmount, tickets.get(0).money.getCurrency())
			.getFormattedMoney();
		String expectedBookText = String
			.format(activity.getResources().getString(R.string.offer_book_now_TEMPLATE), expectedAmountWithCurrency);
		Button bookButton = (Button) widget.findViewById(R.id.lx_book_now);
		TextView titleText = (TextView) widget.findViewById(R.id.offer_title);
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

	private AvailabilityInfo singleTicketAvailability() {
		AvailabilityInfo availabilityInfo = new AvailabilityInfo();
		List<Ticket> tickets = new ArrayList<>();
		Ticket testTicket = new Ticket();
		testTicket.code = LXTicketType.Adult;
		testTicket.money = new Money("40", "USD");
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
		adultTicket.restrictionText = "13+ years";
		tickets.add(adultTicket);

		Ticket childTicket = new Ticket();
		childTicket.code = LXTicketType.Child;
		childTicket.money = new Money("30", "USD");
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
		return offer;
	}

	private Offer buildGTOffer() {
		Offer offer = new Offer();
		offer.id = "offerId";
		offer.title = "Ground Transport";
		offer.description = "Offer Description";
		offer.freeCancellation = true;
		offer.duration = "1h";
		offer.bags = "2";
		offer.passengers = "2";
		return offer;
	}
}
