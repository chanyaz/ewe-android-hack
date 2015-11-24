package com.expedia.bookings.interfaces;

import com.expedia.bookings.data.Response;

public interface IExpediaServicesListener {

	enum ServiceType {
		SUGGEST_NEARBY,
		HOTEL_SEARCH,
		HOTEL_SEARCH_HOTEL,
		FLIGHT_SEARCH,
		HOTEL_AFFINITY_SEARCH,
	}

	void onExpediaServicesDownload(ServiceType type, Response response);
}
