package com.expedia.bookings.unit;

import java.util.ArrayList;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CategorizedCarOffers;

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
}
