package com.expedia.bookings.data;

public class CardFeeResponse extends BaseApiResponse {

	public String tripId;
	public Money feePrice;
	public Money tripTotalPrice;
	public Money bundleTotalPrice;
	public String flexStatus;
}
