package com.expedia.bookings.unit.rail;

import java.util.ArrayList;

import org.junit.Test;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.rail.responses.RailOffer;
import com.expedia.bookings.data.rail.responses.RailProduct;

import static org.junit.Assert.assertEquals;

public class RailOfferTest {
	private RailOffer offerOne;
	private RailOffer offerTwo;

	private final String standardFareClass = "Standard";
	private final String firstFareClass = "First";

	@Test
	public void testCompareNull() {
		offerOne = new RailOffer();
		assertEquals(0, offerOne.compareTo(null));
	}

	@Test
	public void testCompareSameClassDifferentPrice() {
		offerOne = getRailOffer(standardFareClass, new Money(10, "USD"));
		offerTwo = getRailOffer(standardFareClass, new Money(15, "USD"));

		assertEquals("Expected lower price before higer price for matching fare classes.", -1, offerOne.compareTo(offerTwo));
		assertEquals("Expected lower price before higer price for matching fare classes.", 1, offerTwo.compareTo(offerOne));
	}

	@Test
	public void testCompareSameClassSamePrice() {
		offerOne = getRailOffer(standardFareClass, new Money(10, "USD"));
		offerTwo = getRailOffer(standardFareClass, new Money(10, "USD"));

		assertEquals(0, offerOne.compareTo(offerTwo));
		assertEquals(0, offerTwo.compareTo(offerOne));
	}

	@Test
	public void testCompareDifferentClassSamePrice() {
		offerOne = getRailOffer(standardFareClass, new Money(10, "USD"));
		offerTwo = getRailOffer(firstFareClass, new Money(10, "USD"));

		assertEquals("Expected Standard fare class before First", -1, offerOne.compareTo(offerTwo));
		assertEquals("Expected Standard fare class before First", 1, offerTwo.compareTo(offerOne));
	}

	@Test
	public void testCompareDifferentClassDifferentPrice() {
		offerOne = getRailOffer(standardFareClass, new Money(10, "USD"));
		offerTwo = getRailOffer(firstFareClass, new Money(15, "USD"));

		assertEquals("Expected Standard fare class before First regardless of price", -1, offerOne.compareTo(offerTwo));
		assertEquals("Expected Standard fare class before First regardless of price", 1, offerTwo.compareTo(offerOne));

		offerOne = getRailOffer(standardFareClass, new Money(10, "USD"));
		offerTwo = getRailOffer(firstFareClass, new Money(5, "USD"));

		assertEquals("Expected Standard fare class before First regardless of price", -1, offerOne.compareTo(offerTwo));
		assertEquals("Expected Standard fare class before First regardless of price", 1, offerTwo.compareTo(offerOne));
	}

	private RailOffer getRailOffer(String fareClass, Money farePrice) {
		RailOffer offer = new RailOffer();
		RailProduct railProduct = new RailProduct();
		railProduct.aggregatedCarrierServiceClassDisplayName = fareClass;

		offer.railProductList = new ArrayList<>();
		offer.railProductList.add(railProduct);

		offer.totalPrice = farePrice;

		return offer;
	}
}
