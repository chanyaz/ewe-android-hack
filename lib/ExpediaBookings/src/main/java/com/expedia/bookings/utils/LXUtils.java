package com.expedia.bookings.utils;

import java.math.BigDecimal;
import java.util.List;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.Ticket;

public class LXUtils {
	public static Money getTotalAmount(List<Ticket> selectedTickets) {
		Money totalMoney = new Money();

		if (CollectionUtils.isEmpty(selectedTickets)) {
			//Should be invoked with at least 1 selected ticket!
			return totalMoney;
		}

		for (Ticket ticket : selectedTickets) {
			BigDecimal amountDueForTickets = BigDecimal.ZERO;
			if (ticket.prices == null || ticket.count == 0) {
				amountDueForTickets = ticket.money.getAmount().multiply(BigDecimal.valueOf(ticket.count));
			}
			else {
				for (Ticket.LxTicketPrices price : ticket.prices) {
					if (ticket.count == price.travellerNum) {
						if (price.groupPrice != null) {
							amountDueForTickets = new BigDecimal(price.groupPrice);
						}
						else  {
							amountDueForTickets = price.money.getAmount().multiply(BigDecimal.valueOf(price.travellerNum));
						}
					}
				}
			}
			totalMoney.setAmount(totalMoney.getAmount().add(amountDueForTickets));
		}

		//Currency code for all tickets is the same!
		String currencyCode = selectedTickets.get(0).money.getCurrency();
		totalMoney.setCurrency(currencyCode);

		return totalMoney;
	}

	public static int getTotalTicketCount(List<Ticket> selectedTickets) {
		int ticketCount = 0;

		if (CollectionUtils.isEmpty(selectedTickets)) {
			//Should be invoked with at least 1 selected ticket!
			return ticketCount;
		}

		for (Ticket ticket : selectedTickets) {
			ticketCount += ticket.count;
		}

		return ticketCount;
	}

	public static int getTicketTypeCount(List<Ticket> selectedTickets, LXTicketType ticketType) {
		int ticketTypeCount = 0;

		if (CollectionUtils.isEmpty(selectedTickets)) {
			//Should be invoked with at least 1 selected ticket!
			return ticketTypeCount;
		}

		for (Ticket ticket : selectedTickets) {
			if (ticket.code == ticketType) {
				ticketTypeCount += ticket.count;
			}
		}

		return ticketTypeCount;
	}

	public static String whitelistAlphanumericFromCategoryKey(String categoryKey) {
		return categoryKey.replaceAll("[^a-zA-Z0-9]", "");
	}

	public static Money getTotalOriginalAmount(List<Ticket> selectedTickets) {
		Money totalMoney = new Money();

		if (CollectionUtils.isEmpty(selectedTickets)) {
			//Should be invoked with at least 1 selected ticket!
			return totalMoney;
		}

		for (Ticket ticket : selectedTickets) {
			BigDecimal amountDueForTickets = BigDecimal.ZERO;
			if (ticket.prices == null || ticket.count == 0) {
				amountDueForTickets = ticket.originalPriceMoney.getAmount().multiply(BigDecimal.valueOf(ticket.count));
			}
			else {
				for (Ticket.LxTicketPrices price : ticket.prices) {
					if (ticket.count == price.travellerNum) {
						amountDueForTickets = price.originalPriceMoney.getAmount().multiply(BigDecimal.valueOf(price.travellerNum));
					}
				}
			}
			totalMoney.setAmount(totalMoney.getAmount().add(amountDueForTickets));
		}

		//Currency code for all tickets is the same!
		String currencyCode = selectedTickets.get(0).originalPriceMoney.getCurrency();
		totalMoney.setCurrency(currencyCode);

		return totalMoney;
	}

	public static int getDiscountPercentValue(BigDecimal discountedAmount, BigDecimal originalAmount) {
		if (originalAmount.equals(BigDecimal.ZERO) || originalAmount.intValue() < discountedAmount.intValue()) {
			return 0;
		}

		float discountPercentage = ((originalAmount.floatValue() - discountedAmount.floatValue()) / originalAmount.floatValue()) * 100;
		return Math.round(discountPercentage);
	}

	public static int getMaxPromoDiscount(List<LXActivity> activities) {

		int maxPromoPricingDiscountForResult = 0;
		if (activities != null) {
			for (LXActivity activity : activities) {
				int activityMIPDP = 0;
				if (activity.mipDiscountPercentage > 0) {
					activityMIPDP = activity.mipDiscountPercentage;
				}
				if (activity.discountType != null && activity.discountType.equals(Constants.LX_AIR_MIP)) {
					maxPromoPricingDiscountForResult = Math.max(activityMIPDP, maxPromoPricingDiscountForResult);
				}
			}
		}
		return maxPromoPricingDiscountForResult;
	}
}
