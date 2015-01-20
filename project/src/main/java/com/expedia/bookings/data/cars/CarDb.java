package com.expedia.bookings.data.cars;

import com.expedia.bookings.services.CarServices;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class CarDb {

	public static CarSearchParams searchParams = new CarSearchParams();

	public static CarSearch carSearch = new CarSearch();

	public static void setSearchParams(CarSearchParams carSearchParams) {
		searchParams = carSearchParams.clone();
	}

	private static final CarServices mCarServices = new CarServices(CarServices.TRUNK, AndroidSchedulers.mainThread(), Schedulers.io());
	public static CarServices getCarServices() {
		return mCarServices;
	}
}
