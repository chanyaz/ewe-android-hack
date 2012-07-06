package com.expedia.bookings.utils;

import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;

/**
 * Properties can be returned from E3 (and from Db.java) in a few ways: through 
 * ExpediaServices.search(), ExpediaServices.information(), ExpediaServices.availability().
 * Each one of these has minor differences (lacking some fields, longer/shorter description, etc.)
 * This class helps us not have to deal with those differences.
 * @author doug
 *
 */
public class DbPropertyHelper {
	
	/**
	 * Returns the Property best suited to get the lowest rate.
	 * @return
	 */
	public static Property getBestRateProperty() {
		return Db.getSelectedProperty();
	}

	/**
	 * Returns the Property best suited for user reviews.
	 * @return
	 */
	public static Property getBestReviewsProperty() {
		return Db.getSelectedProperty();
	}

	/**
	 * Returns the Property best suited for the hotel description.
	 * @return
	 */
	public static Property getBestDescriptionProperty() {
		AvailabilityResponse infoResponse = Db.getSelectedInfoResponse();
		if (infoResponse != null && infoResponse.getProperty() != null) {
			return infoResponse.getProperty();
		}
		return Db.getSelectedProperty();
	}
	
	/**
	 * Returns the Property best suited to get the hotel's amenities.
	 * @return
	 */
	public static Property getBestAmenityProperty() {
		AvailabilityResponse infoResponse = Db.getSelectedInfoResponse();
		if (infoResponse != null && infoResponse.getProperty() != null) {
			return infoResponse.getProperty();
		}
		return Db.getSelectedProperty();
	}
	
	/**
	 * Returns the Property best suited to get the hotel's media (gallery pictures).
	 * @return
	 */
	public static Property getBestMediaProperty() {
		AvailabilityResponse infoResponse = Db.getSelectedInfoResponse();
		if (infoResponse != null && infoResponse.getProperty() != null) {
			return infoResponse.getProperty();
		}
		return Db.getSelectedProperty();
	}

}
