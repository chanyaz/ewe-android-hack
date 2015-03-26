package com.expedia.bookings.data.cars;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripInfo;
import com.google.gson.annotations.SerializedName;

public class CarCheckoutResponse extends BaseCarResponse {
	public String activityId;
	public CarTrackingData trackingData;
	public TripInfo newTrip;
	public String orderId;
	public Money totalChargesPrice; // TODO: API needs to include currency code in this object.

	//TODO: nuke these when the comment above is addressed.
	public String currencyCode;
	public String totalCharges;

	// NOTE: These fields look shockingly similar to the fields in CarCreateTripResponse. That is
	// because a price change on checkout is communicated like the CreateTripResponse with price
	// change.
	public String tripId;
	public CreateTripCarOffer originalCarProduct;
	@SerializedName("carProduct")
	public CreateTripCarOffer newCarProduct;

	public boolean hasPriceChange() {
		return hasErrors() && getFirstError().errorCode == CarApiError.Code.PRICE_CHANGE;
	}

}
