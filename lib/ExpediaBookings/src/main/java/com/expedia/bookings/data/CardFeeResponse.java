package com.expedia.bookings.data;

public class CardFeeResponse extends BaseApiResponse {

	public String tripId;
	public final Money feePrice;
	public Money tripTotalPrice;
	public Money bundleTotalPrice;
	public String flexStatus;
}
