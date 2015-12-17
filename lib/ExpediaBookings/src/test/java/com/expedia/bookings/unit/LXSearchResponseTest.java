package com.expedia.bookings.unit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXSearchResponse;

public class LXSearchResponseTest {

	@Test
	public void testLowestTotalPriceOfferTest() {

		LXSearchResponse lxSearchResponse = new LXSearchResponse();
		List<LXActivity> activities = new ArrayList<>();

		LXActivity firstActivity = new LXActivity();
		firstActivity.price = new Money("19", "USD");
		activities.add(firstActivity);

		LXActivity secondActivity = new LXActivity();
		secondActivity.price = new Money("32", "USD");
		activities.add(secondActivity);

		LXActivity thirdActivity = new LXActivity();
		thirdActivity.price = new Money("31", "USD");
		activities.add(thirdActivity);

		lxSearchResponse.activities = activities;

		Assert.assertEquals(lxSearchResponse.getLowestPriceActivity().price.getAmount(), new BigDecimal(19));
	}
}
