package com.expedia.bookings.unit.rail;

import java.util.ArrayList;

import org.junit.Test;

import com.expedia.bookings.data.rail.responses.RailLegOption;
import com.expedia.bookings.data.rail.responses.RailSegment;
import com.expedia.bookings.data.rail.responses.RailTripOffer;
import com.expedia.bookings.data.rail.responses.RailTripProduct;

import static org.junit.Assert.assertEquals;

public class RailTripOfferTest {

	@Test
	public void testOneWayCarrierString() {
		RailTripOffer offer = createOffer(false);
		assertEquals("carrier1:carrier2", offer.colonSeparatedSegmentOperatingCarriers());
	}

	@Test
	public void testRoundTripCarrierString() {
		RailTripOffer offer = createOffer(true);
		assertEquals("carrier1:carrier2:carrier1:carrier2", offer.colonSeparatedSegmentOperatingCarriers());
	}

	private RailTripOffer createOffer(boolean roundTrip) {
		RailTripOffer offer = new RailTripOffer();
		offer.railProductList = new ArrayList<>();
		offer.railProductList.add(createRailProduct());

		if (roundTrip) {
			offer.railProductList.add(createRailProduct());
		}

		return offer;
	}

	private RailTripProduct createRailProduct() {
		RailTripProduct railProduct = new RailTripProduct();
		railProduct.legOptionList = new ArrayList<>();
		railProduct.legOptionList.add(createLegOption());
		return railProduct;
	}

	private RailLegOption createLegOption() {
		RailLegOption legOption = new RailLegOption();
		legOption.travelSegmentList = new ArrayList<>();

		for (int i = 1; i <= 2; i++) {
			RailSegment segment = new RailSegment();
			segment.operatingCarrier = "carrier" + i;
			legOption.travelSegmentList.add(segment);
		}
		return legOption;
	}


}
