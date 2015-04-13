package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.Ticket;

public class LXFormatter {
	public static String selectedTicketsSummaryText(Context context, List<Ticket> tickets) {
		String ticketSummaryTemplate = context.getResources().getString(R.string.ticket_summary_type_count_TEMPLATE);
		List<String> ticketSummaries = new ArrayList<String>();

		if (tickets != null) {
			for (Ticket ticket : tickets) {
				ticketSummaries.add(String.format(ticketSummaryTemplate, ticket.count,
					LXDataUtils.ticketDisplayName(context, ticket.code)));
			}
			return Strings.joinWithoutEmpties(", ", ticketSummaries);
		}

		return "";
	}
}
