package com.expedia.bookings.unit;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.AvailabilityInfo;
import com.expedia.bookings.data.lx.Ticket;

/**
 * Created by udigupta on 8/19/15.
 */
public class AvailabilityInfoTest {

	@Test
	public void testLowestTicket() {
		AvailabilityInfo availabilityInfo = new AvailabilityInfo();

		Ticket firstTicket = new Ticket();
		firstTicket.money = new Money("500","USD");
		availabilityInfo.tickets.add(firstTicket);

		Ticket secondTicket = new Ticket();
		secondTicket.money = new Money("600","USD");
		availabilityInfo.tickets.add(secondTicket);

		Ticket thirdTicket = new Ticket();
		thirdTicket.money = new Money("700","USD");
		availabilityInfo.tickets.add(thirdTicket);

		BigDecimal lowestAmount = new BigDecimal(500);

		Assert.assertEquals(availabilityInfo.getLowestTicket().money.getAmount(), lowestAmount);
		Collections.shuffle(availabilityInfo.tickets);
		Assert.assertEquals(availabilityInfo.getLowestTicket().money.getAmount(), lowestAmount);
		Collections.shuffle(availabilityInfo.tickets);
		Assert.assertEquals(availabilityInfo.getLowestTicket().money.getAmount(), lowestAmount);
	}

}
