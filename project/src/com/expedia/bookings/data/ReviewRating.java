package com.expedia.bookings.data;

public class ReviewRating {

	private int mConvenienceOfLocation;
	private int mHotelCondition;
	private int mQualityOfService;
	private int mRoomCleanliness;
	private int mRoomComfort;
	private int mOverallSatisfaction;
	private int mNeighborhoodSatisfaction;

	public int getConvenienceOfLocation() {
		return mConvenienceOfLocation;
	}

	public void setConvenienceOfLocation(int convenienceOfLocation) {
		this.mConvenienceOfLocation = convenienceOfLocation;
	}

	public int getHotelCondition() {
		return mHotelCondition;
	}

	public void setHotelCondition(int hotelCondition) {
		this.mHotelCondition = hotelCondition;
	}

	public int getQualityOfService() {
		return mQualityOfService;
	}

	public void setQualityOfService(int qualityOfService) {
		this.mQualityOfService = qualityOfService;
	}

	public int getRoomCleanliness() {
		return mRoomCleanliness;
	}

	public void setRoomCleanliness(int roomCleanliness) {
		this.mRoomCleanliness = roomCleanliness;
	}

	public int getRoomComfort() {
		return mRoomComfort;
	}

	public void setRoomComfort(int roomComfort) {
		this.mRoomComfort = roomComfort;
	}

	public int getOverallSatisfaction() {
		return mOverallSatisfaction;
	}

	public void setOverallSatisfaction(int overallSatisfaction) {
		this.mOverallSatisfaction = overallSatisfaction;
	}

	public int getNeighborhoodSatisfaction() {
		return mNeighborhoodSatisfaction;
	}

	public void setNeighborhoodSatisfaction(int neighborhoodSatisfaction) {
		this.mNeighborhoodSatisfaction = neighborhoodSatisfaction;
	}
}
