package com.expedia.bookings.data.cars;

public final class CarDb {

	public static CarSearchParams searchParams = new CarSearchParams();

	public static void setSearchParams(CarSearchParams carSearchParams) {
		searchParams = carSearchParams.clone();
	}

}
