package com.expedia.bookings.test.robolectric;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.AvailabilityInfo;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.widget.LXTicketPicker;
import com.expedia.bookings.widget.LXTicketSelectionWidget;

import butterknife.ButterKnife;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricSubmoduleTestRunner.class)
public class LXTicketSelectionWidgetTest {
	@Test
	public void testTicketSelectionWidgetViews() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		LXTicketSelectionWidget widget = (LXTicketSelectionWidget) LayoutInflater.from(activity)
			.inflate(R.layout.widget_lx_ticket_selection, null);
		assertNotNull(widget);
		ButterKnife.inject(activity);

		widget.buildTicketPickers(singleTicketAvailability());

		View container = widget.findViewById(R.id.ticket_selectors_container);
		assertNotNull(container);

		View ticketSelector = container.findViewById(R.id.ticket_picker);
		assertNotNull(container);

		TextView ticketDetails = (TextView) ticketSelector.findViewById(R.id.ticket_details);
		TextView ticketCount = (TextView) ticketSelector.findViewById(R.id.ticket_count);
		TextView addTicketView = (TextView) ticketSelector.findViewById(R.id.ticket_add);
		TextView removeTicketView = (TextView) ticketSelector.findViewById(R.id.ticket_remove);

		assertNotNull(ticketDetails);
		assertNotNull(ticketCount);
		assertNotNull(addTicketView);
		assertNotNull(removeTicketView);
	}

	@Test
	public void testSingleTicketTypeSelections() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		LXTicketSelectionWidget widget = (LXTicketSelectionWidget) LayoutInflater.from(activity)
			.inflate(R.layout.widget_lx_ticket_selection, null);

		AvailabilityInfo availabilityInfo = singleTicketAvailability();

		widget.setOfferId("offerId");
		widget.buildTicketPickers(availabilityInfo);

		Ticket testTicket = availabilityInfo.tickets.get(0);

		TextView ticketDetails = (TextView) widget.findViewById(R.id.ticket_details);
		TextView ticketCount = (TextView) widget.findViewById(R.id.ticket_count);
		TextView addTicketView = (TextView) widget.findViewById(R.id.ticket_add);
		TextView removeTicketView = (TextView) widget.findViewById(R.id.ticket_remove);
		TextView ticketsSummary = (TextView) widget.findViewById(R.id.selected_ticket_summary);
		Button bookButton = (Button) widget.findViewById(R.id.lx_book_now);

		int expectedCount = 0;
		String expectedDetails = String
			.format(activity.getResources().getString(R.string.ticket_details_template), testTicket.code,
				testTicket.price, testTicket.restrictionText);
		String expectedSummary = testTicket.code + " " + expectedCount;
		String bookButtonTemplate = activity.getResources().getString(R.string.offer_book_now);
		String expectedBookText = String.format(bookButtonTemplate, BigDecimal.ZERO);

		assertEquals(String.valueOf(expectedCount), ticketCount.getText());
		assertEquals(expectedDetails, ticketDetails.getText());
		assertEquals(expectedSummary, ticketsSummary.getText());
		assertEquals(expectedBookText, bookButton.getText());

		addTicketView.performClick();
		expectedCount++;
		expectedSummary = testTicket.code + " " + expectedCount;
		expectedBookText = String.format(bookButtonTemplate, testTicket.amount);

		assertEquals(String.valueOf(expectedCount), ticketCount.getText());
		assertEquals(expectedDetails, ticketDetails.getText());
		assertEquals(expectedSummary, ticketsSummary.getText());
		assertEquals(expectedBookText, bookButton.getText());

		removeTicketView.performClick();
		expectedCount--;
		expectedSummary = testTicket.code + " " + expectedCount;
		expectedBookText = String.format(bookButtonTemplate, BigDecimal.ZERO);

		assertEquals(String.valueOf(expectedCount), ticketCount.getText());
		assertEquals(expectedDetails, ticketDetails.getText());
		assertEquals(expectedSummary, ticketsSummary.getText());
		assertEquals(expectedBookText, bookButton.getText());
	}

	@Test
	public void testMultipleTicketTypeSelections() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		LXTicketSelectionWidget widget = (LXTicketSelectionWidget) LayoutInflater.from(activity)
			.inflate(R.layout.widget_lx_ticket_selection, null);
		AvailabilityInfo availabilityInfo = multipleTicketAvailability();

		widget.setOfferId("offerId");
		widget.buildTicketPickers(availabilityInfo);

		List<Ticket> tickets = availabilityInfo.tickets;

		LinearLayout container = (LinearLayout) widget.findViewById(R.id.ticket_selectors_container);

		int ticketPickerIndex = 0;
		for (int i = 0; i < container.getChildCount(); i++) {
			View child = container.getChildAt(i);
			if (child instanceof LXTicketPicker) {
				String expectedDetails = String
					.format(activity.getResources().getString(R.string.ticket_details_template),
						tickets.get(ticketPickerIndex).code,
						tickets.get(ticketPickerIndex).price, tickets.get(ticketPickerIndex).restrictionText);

				TextView ticketDetails = (TextView) child.findViewById(R.id.ticket_details);
				TextView ticketCount = (TextView) child.findViewById(R.id.ticket_count);
				TextView addTicketView = (TextView) child.findViewById(R.id.ticket_add);

				assertEquals(String.valueOf(0), ticketCount.getText());
				assertEquals(expectedDetails, ticketDetails.getText());
				ticketPickerIndex++;

				addTicketView.performClick();
			}
		}

		BigDecimal expectedTotalAmount = tickets.get(0).amount.add(tickets.get(1).amount);
		String expectedBookText = String
			.format(activity.getResources().getString(R.string.offer_book_now), expectedTotalAmount);
		Button bookButton = (Button) widget.findViewById(R.id.lx_book_now);
		assertEquals(expectedBookText, bookButton.getText());

		String expectedSummary = tickets.get(0).code + " 1," + tickets.get(1).code + " 1";
		TextView ticketsSummary = (TextView) widget.findViewById(R.id.selected_ticket_summary);

		assertEquals(expectedSummary, ticketsSummary.getText());
	}

	private AvailabilityInfo singleTicketAvailability() {
		AvailabilityInfo availabilityInfo = new AvailabilityInfo();
		List<Ticket> tickets = new ArrayList<>();
		Ticket testTicket = new Ticket();
		testTicket.amount = new BigDecimal(40);
		testTicket.code = "Adult";
		testTicket.price = "$40";
		testTicket.restrictionText = "13+ years";
		tickets.add(testTicket);
		availabilityInfo.tickets = tickets;
		return availabilityInfo;
	}

	private AvailabilityInfo multipleTicketAvailability() {
		AvailabilityInfo availabilityInfo = new AvailabilityInfo();
		List<Ticket> tickets = new ArrayList<>();
		Ticket adultTicket = new Ticket();
		adultTicket.amount = new BigDecimal(40);
		adultTicket.code = "Adult";
		adultTicket.price = "$40";
		adultTicket.restrictionText = "13+ years";
		tickets.add(adultTicket);

		Ticket childTicket = new Ticket();
		childTicket.amount = new BigDecimal(30);
		childTicket.code = "Child";
		childTicket.price = "$30";
		childTicket.restrictionText = "4-12 years";
		tickets.add(childTicket);

		availabilityInfo.tickets = tickets;
		return availabilityInfo;
	}

}
