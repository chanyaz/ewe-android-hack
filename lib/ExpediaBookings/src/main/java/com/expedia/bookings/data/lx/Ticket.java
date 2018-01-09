package com.expedia.bookings.data.lx;

import java.util.List;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.cars.PriceBreakdownItemType;

public class Ticket implements Comparable<Ticket> {
	public String ticketId;
	public LXTicketType code;
	public String restrictionText;
	public String amount;
	public Money money;
	// Count is manipulated from the ticket picker. But this is send back in create trip/ checkout api response.
	public int count;
	public List<LXPriceBreakdownItem> priceBreakdownItemList;
	public List<LxTicketPrices> prices;

	public LXPriceBreakdownItem getBreakdownForType(PriceBreakdownItemType type) {
		for (LXPriceBreakdownItem lxPriceBreakdownItem : priceBreakdownItemList) {
			if (lxPriceBreakdownItem.type.equals(type)) {
				return lxPriceBreakdownItem;
			}
		}
		return null;
	}

	public static class LxTicketPrices {
		public String originalPrice;
		public String cost;
		public int travellerNum;
		public String amount;
		public String price;
		public Money money;
	}

	@Override
	public int compareTo(Ticket o) {
		return code.compareTo(o.code);
	}
}
