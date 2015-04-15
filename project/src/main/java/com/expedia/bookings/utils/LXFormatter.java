package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.expedia.bookings.data.lx.Ticket;

public class LXFormatter {
	public static String selectedTicketsSummaryText(Context context, List<Ticket> tickets) {
		List<String> ticketSummaries = new ArrayList<String>();

		if (CollectionUtils.isNotEmpty(tickets)) {
			for (Ticket ticket : tickets) {
				ticketSummaries.add(LXDataUtils.ticketCountSummary(context, ticket.code, ticket.count));
			}
			return Strings.joinWithoutEmpties(", ", ticketSummaries);
		}

		return "";
	}
}
