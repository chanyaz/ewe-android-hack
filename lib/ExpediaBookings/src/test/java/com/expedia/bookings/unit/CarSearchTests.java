package com.expedia.bookings.unit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarInfo;
import com.expedia.bookings.data.cars.CarOffer;
import com.expedia.bookings.data.cars.CarSearch;

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
		assertEquals(expectedListSize, carSearch.carCategoryOfferMap.get(econCat).size());
	}

	@Test
	public void offerMapEntryCountTest() {
		CarCategory compactCat = CarCategory.COMPACT;
		CarCategory fullsizeCat = CarCategory.FULLSIZE;

		final CarCategory[] categoryList = {compactCat, fullsizeCat};
		CarSearch carSearch = generateOfferList(categoryList);

		assertEquals(carSearch.carCategoryOfferMap.size(), 2);
		assertEquals(carSearch.carCategoryOfferMap.get(compactCat).size(), 1);
		assertEquals(carSearch.carCategoryOfferMap.get(fullsizeCat).size(), 1);
	}

	private CarSearch generateOfferList(CarCategory[] categoryList) {
		List<CarOffer> carOffersList = new ArrayList<>();
		assertEquals(2, categoryList.length);
		for (CarCategory c : categoryList) {
			CarOffer offer = new CarOffer();
			offer.vehicleInfo = new CarInfo();
			offer.vehicleInfo.category = c;
			carOffersList.add(offer);
		}

		CarSearch carSearch = new CarSearch();
		for (CarOffer offer : carOffersList) {
			carSearch.updateOfferMap(offer);
		}

		return carSearch;
	}
}
