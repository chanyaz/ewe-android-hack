package com.expedia.bookings.unit;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarFilter;
import com.expedia.bookings.data.cars.Transmission;

public class CarFilterTest {
	@Test
	public void altered() throws Throwable {
		CarFilter filter = new CarFilter();

		Assertions.assertThat(filter.altered()).isFalse();

		filter.hasUnlimitedMileage = true;
		Assertions.assertThat(filter.altered()).isTrue();

		filter.hasUnlimitedMileage = false;
		Assertions.assertThat(filter.altered()).isFalse();
		filter.hasAirConditioning = true;
		Assertions.assertThat(filter.altered()).isTrue();
		filter.hasAirConditioning = false;
		Assertions.assertThat(filter.altered()).isFalse();

		filter.categoriesIncluded.add(CarCategory.MINI);
		Assertions.assertThat(filter.altered()).isTrue();

		filter.suppliersIncluded.add("Brennan's Hot Deals");
		Assertions.assertThat(filter.altered()).isTrue();

		filter.categoriesIncluded.remove(CarCategory.MINI);
		Assertions.assertThat(filter.altered()).isTrue();

		filter.suppliersIncluded.remove("Brennan's Hot Deals");
		Assertions.assertThat(filter.altered()).isFalse();

		filter.carTransmissionType = Transmission.AUTOMATIC_TRANSMISSION;
		Assertions.assertThat(filter.altered()).isTrue();

		filter.carTransmissionType = Transmission.MANUAL_TRANSMISSION;
		Assertions.assertThat(filter.altered()).isTrue();

		filter.carTransmissionType = null;
		Assertions.assertThat(filter.altered()).isFalse();
	}
}
