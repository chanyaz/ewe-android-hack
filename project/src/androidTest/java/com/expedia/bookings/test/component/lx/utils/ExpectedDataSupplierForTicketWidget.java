package com.expedia.bookings.test.component.lx.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.expedia.bookings.test.component.lx.models.TicketDataModel;
import com.expedia.bookings.test.component.lx.models.TicketSummaryDataModel;

public class ExpectedDataSupplierForTicketWidget {
	private BigDecimal total = BigDecimal.ZERO;
	private List<TicketDataModel> mTicketsToOffer = new ArrayList<>();
	private HashMap<TicketDataModel, Integer> mTotalTickets = new HashMap<>();
	private String ticketName;

	public ExpectedDataSupplierForTicketWidget(TicketSummaryDataModel summary) {
		/*
			priceSummary comes as $99 Adult,$80 Child
			when I perform split on the priceSummary I get a array of Price and Traveller Type
		*/
		String[] options = summary.priceSummary.split(",");
		buildTicketsFromPriceSummary(options);
		this.ticketName = summary.ticketTitle;
	}

	public String getTicketName() {
		return ticketName;
	}

	public void buildTicketsFromPriceSummary(String[] options) {
		int order = 1;
		for (String option : options) { // here options is ["$99 Adult","$80 Child"];
			String travellerType = option.trim().split(" ")[1];//travellerType would be Adult/Child in first and second pass
			Pattern p = Pattern.compile("([\\d.]+)");// this will extract the price value
			java.util.regex.Matcher m = p.matcher(option);
			m.find();
			TicketDataModel ticket = new TicketDataModel();
			ticket.perTicketCost = new BigDecimal(m.group());
			ticket.travellerType = travellerType;
			ticket.order = order;
			order++;
			mTicketsToOffer.add(ticket);
			mTotalTickets.put(ticket, 0);
		}
	}

	public List<TicketDataModel> getTickets() {
		return mTicketsToOffer;
	}

	public int numberOfTicketRows() {
		return mTicketsToOffer.size();
	}

	public BigDecimal getTotalPrice() {
		Iterator it = mTotalTickets.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			TicketDataModel ticket = (TicketDataModel) pair.getKey();
			int numberOfTravellers = (int) pair.getValue();
			total = total.add(ticket.perTicketCost.multiply(BigDecimal.valueOf(numberOfTravellers)));
			it.remove();
		}
		return total;
	}
	/*
		This method will get the expected Summary Strip
		of the current state of the UX model when we have randomly
		clicked on the + button of the tickets

		return would be something like 2 Adult,1 Child
	 */
	public String expectedSummary() {
		Set<TicketDataModel> tickets = mTotalTickets.keySet();
		List<TicketDataModel> sortedTickets = new ArrayList<>(tickets);
		// We have to sort the tickets as we have to show the summary strip in the same
		// order in which we have broken down in initial phase
		Collections.sort(sortedTickets,
			new Comparator() {
				public int compare(Object left, Object right) {
					if (((TicketDataModel) left).order
						< ((TicketDataModel) right).order) {
						return -1;
					}
					else {
						return 1;
					}
				}
			});
		String summaryStrip = "";
		for (TicketDataModel ticket : sortedTickets) {
			//							  get the total tickets              get the traveller type
			summaryStrip = summaryStrip + mTotalTickets.get(ticket) + " " + ticket.travellerType + ", ";
		}
		return summaryStrip.trim().substring(0, summaryStrip.length() - 2);
	}

	public void updateTravellers(int numberOfTravellers, String travellerType) {
		TicketDataModel ticket = getTicketFromTravellerType(travellerType);
		mTotalTickets.put(ticket, numberOfTravellers);
	}

	private TicketDataModel getTicketFromTravellerType(String travellerType) {
		for (TicketDataModel ticket : mTicketsToOffer) {
			if (ticket.travellerType.equals(travellerType)) {
				return ticket;
			}
		}
		return null;
	}
}
