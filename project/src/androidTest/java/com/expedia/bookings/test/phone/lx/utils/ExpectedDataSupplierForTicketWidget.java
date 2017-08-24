package com.expedia.bookings.test.phone.lx.utils;

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

import com.expedia.bookings.test.phone.lx.models.TicketDataModel;
import com.expedia.bookings.test.phone.lx.models.TicketSummaryDataModel;

public class ExpectedDataSupplierForTicketWidget {
	private BigDecimal total = BigDecimal.ZERO;
	private final List<TicketDataModel> mTicketsToOffer = new ArrayList<>();
	private final HashMap<TicketDataModel, Integer> mTotalTickets = new HashMap<>();
	private final String ticketName;
	final HashMap<String,String> expectedPlural = new HashMap<String, String>();

	public ExpectedDataSupplierForTicketWidget(TicketSummaryDataModel summary) {
		/*
			priceSummary comes as $99 Adult,$80 Child
			when I perform split on the priceSummary I get a array of Price and Traveller Type
		*/
		String[] options = summary.priceSummary.split(",");
		buildTicketsFromPriceSummary(options);
		this.ticketName = summary.ticketTitle;

		//create the expected Plural Types of the traveller so that we can use it in validating the Price Summary
		// as upon click of add ticket button the Price summary changes from Child to Children and so on.
		// Other way of getting this would be store it in Strings.xml of test apk but for now keeping it here
		expectedPlural.put("Adult", "Adults");
		expectedPlural.put("Child","Children");
		expectedPlural.put("Infant","Infants");
		expectedPlural.put("Youth","Youths");
		expectedPlural.put("Senior","Seniors");
		expectedPlural.put("Group","Groups");
		expectedPlural.put("Couple","Couples");
		expectedPlural.put("Student","Students");
		expectedPlural.put("Military","Military");
		expectedPlural.put("Sedan","Sedans");
		expectedPlural.put("Minivan","Minivans");
		expectedPlural.put("Water taxi","Water taxis");
		expectedPlural.put("SUV"," SUVs");
		expectedPlural.put("Executive car","Executive cars");
		expectedPlural.put("Luxury car","Luxury cars");
		expectedPlural.put("Limousine","Limousines");
		expectedPlural.put("TownCar","TownCars");
		expectedPlural.put("Vehicle Parking Spot","Vehicle Parking Spots");
		expectedPlural.put("Book","Books");
		expectedPlural.put("Guide","Guides");
		expectedPlural.put("Travel Card","Travel Cards");
		expectedPlural.put("Boat","Boats");
		expectedPlural.put("Motorcycle","Motorcycles");
		expectedPlural.put("Ceremony","Ceremonies");
		expectedPlural.put("Calling Card","Calling Cards");
		expectedPlural.put("Pass","Passes");
		expectedPlural.put("Minibus","Minibuses");
		expectedPlural.put("Helicopter","Helicopters");
		expectedPlural.put("Device","Devices");
		expectedPlural.put("Room","Rooms");
		expectedPlural.put("Carriage","Carriages");
		expectedPlural.put("Buggy","Buggies");
		expectedPlural.put("Jet Ski","Jet Skis");
		expectedPlural.put("Scooter","Scooters");
		expectedPlural.put("Scooter Car","Scooter Cars");
		expectedPlural.put("Snowmobile","Snowmobiles");
		expectedPlural.put("Day","Days");
		expectedPlural.put("Bike","Bikes");
		expectedPlural.put("Week","Weeks");
		expectedPlural.put("Subscription","Subscriptions");
		expectedPlural.put("Electric Bike","Electric Bikes");
		expectedPlural.put("Segway","Segways");
		expectedPlural.put("Vehicle","Vehicles");
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
			String expectedTravellerType =
				mTotalTickets.get(ticket) > 1 ? ticket.travellerTypePlural : ticket.travellerType;
			summaryStrip = summaryStrip + mTotalTickets.get(ticket) + " " + expectedTravellerType + ", ";
		}
		return summaryStrip.trim().substring(0, summaryStrip.length() - 2);
	}

	public void updateTravellers(int numberOfTravellers, String travellerType) {
		TicketDataModel ticket = getTicketFromTravellerType(travellerType);
		if (numberOfTravellers > 1) {
			ticket.travellerTypePlural = getPluralTravellerType(travellerType);
		}
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

	private String getPluralTravellerType(String travellerType) {
		return expectedPlural.get(travellerType);
	}
}
