package com.expedia.bookings.data.cars;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripInfo;
import com.google.gson.annotations.SerializedName;

public class CarCheckoutResponse extends com.expedia.bookings.data.BaseApiResponse {
	public final CarTrackingData trackingData;
	public final TripInfo newTrip;
	public final String orderId;
	public final Money totalChargesPrice;

	// NOTE: These fields look shockingly similar to the fields in CarCreateTripResponse. That is
	// because a price change on checkout is communicated like the CreateTripResponse with price
	// change.
	public final String tripId;
	public final CreateTripCarOffer originalCarProduct;
	@SerializedName("carProduct")
	public final CreateTripCarOffer newCarProduct;

}
