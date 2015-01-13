package com.expedia.bookings.unit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.data.cars.CarInfo;
import com.expedia.bookings.data.cars.CarOffer;
import com.expedia.bookings.services.CarServices;

import rx.Observable;

import static org.junit.Assert.assertEquals;

public class CarServicesTest {

	@Before
	public void clearOutHashmap() {
		CarDb.carSearch.reset();
	}

	@Test
	public void carMapEntryUniqueness() throws Throwable {
		CarCategory econCat = CarCategory.ECONOMY;

		final CarCategory[] categoryList2 = {econCat, econCat};

		List<CarOffer> carOffersList2 = generateOfferList(categoryList2);
		Observable.from(carOffersList2)
			.flatMap(CarServices.processOffer)
			.subscribe();
		int expectedMapSize = 1;
		assertEquals(expectedMapSize, CarDb.carSearch.carCategoryOfferMap.size());
		int expectedListSize = 2;
		assertEquals(expectedListSize, CarDb.carSearch.carCategoryOfferMap.get(econCat).size());
	}

	@Test
	public void carMapEntryCountTest() {
		CarCategory compactCat = CarCategory.COMPACT;
		CarCategory fullsizeCat = CarCategory.FULLSIZE;

		final CarCategory[] categoryList = {compactCat, fullsizeCat};

		List<CarOffer> carOffersList = generateOfferList(categoryList);

		Observable.from(carOffersList)
			.flatMap(CarServices.processOffer)
			.subscribe();

		assertEquals(CarDb.carSearch.carCategoryOfferMap.size(), 2);
		assertEquals(CarDb.carSearch.carCategoryOfferMap.get(compactCat).size(), 1);
		assertEquals(CarDb.carSearch.carCategoryOfferMap.get(fullsizeCat).size(), 1);
	}

	private List<CarOffer> generateOfferList(CarCategory[] categoryList) {
		List<CarOffer> carOffersList = new ArrayList<>();
		assertEquals(2, categoryList.length);
		for (CarCategory c : categoryList) {
			CarOffer offer = new CarOffer();
			offer.vehicleInfo = new CarInfo();
			offer.vehicleInfo.category = c;
			carOffersList.add(offer);
		}

		return carOffersList;
	}
}
