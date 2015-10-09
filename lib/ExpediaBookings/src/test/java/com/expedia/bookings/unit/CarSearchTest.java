package com.expedia.bookings.unit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.cars.CarInfo;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.SearchCarFare;
import com.expedia.bookings.data.cars.SearchCarOffer;

public class CarSearchTest {
	private CarSearch search;

	@Before
	public void before() throws Throwable {
		search = new CarSearch();
		CategorizedCarOffers bucket;

		search.categories = new ArrayList<CategorizedCarOffers>();

		bucket = new CategorizedCarOffers();
		bucket.carCategoryDisplayLabel = "label1";
		search.categories.add(bucket);

		bucket = new CategorizedCarOffers();
		bucket.carCategoryDisplayLabel = "label2";
		search.categories.add(bucket);

		bucket = new CategorizedCarOffers();
		bucket.carCategoryDisplayLabel = "label3";
		search.categories.add(bucket);
	}

	@Test
	public void displayLabel() throws Throwable {
		Assertions.assertThat(search.getFromDisplayLabel("label2").carCategoryDisplayLabel).isEqualTo("label2");
	}

	@Test(expected = RuntimeException.class)
	public void displayLabelNotFound() throws Throwable {
		search.getFromDisplayLabel("not here");
	}

	@Test
	public void testLowestTotalPriceOfferTest() {
		search.categories.clear();
		CategorizedCarOffers carOffers = new CategorizedCarOffers();

		CarInfo carInfo = new CarInfo();
		carInfo.adultCapacity = 1;
		carInfo.largeLuggageCapacity = 2;
		carInfo.maxDoors = 3;
		carInfo.minDoors = 2;

		SearchCarOffer firstCarOffer = new SearchCarOffer();
		SearchCarFare carFare = new SearchCarFare();
		carFare.total = new Money("500", "USD");
		firstCarOffer.vehicleInfo = carInfo;
		firstCarOffer.fare = carFare;
		carOffers.add(firstCarOffer);

		SearchCarOffer secondCarOffer = new SearchCarOffer();
		carFare.total = new Money("500", "USD");
		secondCarOffer.fare = carFare;
		secondCarOffer.vehicleInfo = carInfo;
		carOffers.add(secondCarOffer);

		SearchCarOffer thirdCarOffer = new SearchCarOffer();
		carFare.total = new Money("500", "USD");
		thirdCarOffer.fare = carFare;
		thirdCarOffer.vehicleInfo = carInfo;
		carOffers.add(thirdCarOffer);

		search.categories.add(carOffers);
		BigDecimal lowestAmount = new BigDecimal(500);
		Assert.assertEquals(search.getLowestTotalPriceOffer().fare.total.getAmount(), lowestAmount);
		Collections.shuffle(search.categories.get(0).offers);
		Assert.assertEquals(search.getLowestTotalPriceOffer().fare.total.getAmount(), lowestAmount);
		Collections.shuffle(search.categories.get(0).offers);
		Assert.assertEquals(search.getLowestTotalPriceOffer().fare.total.getAmount(), lowestAmount);
	}
}
