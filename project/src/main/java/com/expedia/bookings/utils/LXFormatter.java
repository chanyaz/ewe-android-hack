package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.Ticket;

public class LXFormatter {
	public static String selectedTicketsSummaryText(Context context, Map<Ticket, Integer> tickets) {
		String ticketSummaryTemplate = context.getResources().getString(R.string.ticket_summary_type_count_TEMPLATE);
		List<String> ticketSummaries = new ArrayList<String>();

		if (tickets != null) {
			for (Map.Entry<Ticket, Integer> ticketAndCount : tickets.entrySet()) {
				Ticket ticket = ticketAndCount.getKey();
				int ticketCount = ticketAndCount.getValue();

				ticketSummaries.add(String.format(ticketSummaryTemplate, ticketCount,
					context.getResources().getString(LXDataUtils.LX_TICKET_TYPE_NAME_MAP.get(ticket.code))));
			}
			return Strings.joinWithoutEmpties(", ", ticketSummaries);
		}

		return "";
	}
}
