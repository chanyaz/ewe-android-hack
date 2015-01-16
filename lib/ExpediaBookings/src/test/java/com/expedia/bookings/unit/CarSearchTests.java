package com.expedia.bookings.unit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarFare;
import com.expedia.bookings.data.cars.CarInfo;
import com.expedia.bookings.data.cars.CarOffer;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.Money;

import static org.junit.Assert.assertEquals;

public class CarSearchTests {

	@Test
	public void offerMapEntryUniqueness() throws Throwable {
		CarCategory econCat = CarCategory.ECONOMY;

		final CarCategory[] categoryList2 = {econCat, econCat};
		CarSearch carSearch = generateOfferList(categoryList2);

		int expectedMapSize = 1;
		assertEquals(expectedMapSize, carSearch.carCategoryOfferMap.size());
		int expectedListSize = 2;
		assertEquals(expectedListSize, carSearch.carCategoryOfferMap.get(econCat).getOffers().size());
	}

	@Test
	public void offerMapEntryCountTest() {
		CarCategory compactCat = CarCategory.COMPACT;
		CarCategory fullsizeCat = CarCategory.FULLSIZE;

		final CarCategory[] categoryList = {compactCat, fullsizeCat};
		CarSearch carSearch = generateOfferList(categoryList);

		assertEquals(carSearch.carCategoryOfferMap.size(), 2);
		assertEquals(carSearch.carCategoryOfferMap.get(compactCat).getOffers().size(), 1);
		assertEquals(carSearch.carCategoryOfferMap.get(fullsizeCat).getOffers().size(), 1);
	}

	@Test
	public void offerMapLowestTotalFareTest() throws Throwable {
		CarCategory econCat = CarCategory.ECONOMY;

		final CarCategory[] categoryList2 = {econCat, econCat};
		CarSearch carSearch = generateOfferList(categoryList2);
		List<BigDecimal> prices = new ArrayList<>();
		for(CarOffer offer : carSearch.carCategoryOfferMap.get(econCat).getOffers()) {
			prices.add(offer.fare.total.amount);
		}
		Collections.sort(prices);

		assertEquals(prices.get(0), carSearch.carCategoryOfferMap.get(econCat).getLowestTotalPriceOffer().fare.total.amount);
	}

	private CarSearch generateOfferList(CarCategory[] categoryList) {
		List<CarOffer> carOffersList = new ArrayList<>();
		assertEquals(2, categoryList.length);
		for (CarCategory c : categoryList) {
			CarOffer offer = new CarOffer();
			offer.vehicleInfo = new CarInfo();
			offer.vehicleInfo.category = c;
			offer.fare = generateRandomFare();
			carOffersList.add(offer);
		}

		CarSearch carSearch = new CarSearch();
		for (CarOffer offer : carOffersList) {
			carSearch.updateOfferMap(offer);
		}

		return carSearch;
	}

	private CarFare generateRandomFare() {
		Money total = new Money();
		total.setAmount(Math.ceil(Math.random() * 100));
		CarFare fare = new CarFare();
		fare.total = total;
		return fare;
	}
}
