package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXOfferSelected;
import com.expedia.bookings.data.lx.LXTicketSelected;

public class LXFormatter {
	public static String selectedTicketsSummaryText(Context context, LXOfferSelected offerSelected) {
		String ticketSummaryTemplate = context.getResources().getString(R.string.ticket_summary_type_count_TEMPLATE);
		List<String> tickets = new ArrayList<String>();

		if (offerSelected != null) {
			for (LXTicketSelected ticketSelected : offerSelected.tickets) {
				tickets.add(String.format(ticketSummaryTemplate, ticketSelected.count,
					context.getResources().getString(LXDataUtils.LX_TICKET_TYPE_NAME_MAP.get(ticketSelected.code))));
			}
			return Strings.joinWithoutEmpties(", ", tickets);
		}

		return "";
	}
}
