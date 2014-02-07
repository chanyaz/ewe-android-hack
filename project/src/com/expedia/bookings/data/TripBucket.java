package com.expedia.bookings.data;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

/**
 * Stores hotel and flight (and other Lines of Business?) selected items
 * for use in the tablet trip bucket.
 *
 * @author doug
 */
public class TripBucket implements JSONable {

	private List<TripBucketItem> mItems;

	public TripBucket() {
		mItems = new LinkedList<TripBucketItem>();
	}

	/**
	 * Removes all items from this TripBucket.
	 */
	public void clear() {
		mItems.clear();
	}

	/**
	 * Removes all items with lineOfBusiness from this TripBucket.
	 * @param lineOfBusiness
	 */
	public void clear(LineOfBusiness lineOfBusiness) {
		for (int i = 0; i < mItems.size(); i++) {
			while (i < mItems.size() && mItems.get(i).getLineOfBusiness() == lineOfBusiness) {
				remove(i);
			}
		}
	}

	/**
	 * Convenience method to remove all Hotels from this TripBucket.
	 */
	public void clearHotel() {
		clear(LineOfBusiness.HOTELS);
	}

	/**
	 * Convenience method to remove all Flights from this TripBucket.
	 */
	public void clearFlight() {
		clear(LineOfBusiness.FLIGHTS);
	}

	/**
	 * Adds a Hotel to the trip bucket. Must specify the property and room rate.
	 * @param hotel
	 * @param property
	 * @param rate
	 */
	public void add(HotelSearch hotel, Property property, Rate rate) {
		mItems.add(new TripBucketItemHotel(hotel, property, rate));
	}

	/**
	 * Adds a Flight to the trip bucket.
	 * @param flight
	 * @param trip
	 */
	public void add(FlightSearch flight, FlightTrip trip) {
		mItems.add(new TripBucketItemFlight(flight, trip));
	}

	/**
	 * Returns the number of items in the trip bucket.
	 *
	 * @return
	 */
	public int size() {
		return mItems.size();
	}

	/**
	 * Returns the first hotel found in the bucket, or null if not found.
	 *
	 * @return
	 */
	public TripBucketItemHotel getHotel() {
		int index = getIndexOf(LineOfBusiness.HOTELS);
		return index == -1 ? null : (TripBucketItemHotel) mItems.get(index);
	}

	/**
	 * Populates Db so that Db.getHotelSearch() and friends will reflect
	 * the hotel that's stored in the bucket here at getHotel()
	 */
	public void selectHotel() {
		TripBucketItemHotel item = getHotel();
		Db.setHotelSearch(item.getHotelSearch());
		Db.getHotelSearch().setSelectedProperty(item.getProperty());
		Db.getHotelSearch().setSelectedRate(item.getRate());
	}

	/**
	 * Returns the index of the first item of LineOfBusiness found in this TripBucket.
	 * @param lineOfBusiness
	 * @return -1 if not found
	 */
	private int getIndexOf(LineOfBusiness lineOfBusiness) {
		for (int i = 0; i < mItems.size(); i++) {
			TripBucketItem item = mItems.get(i);
			if (item.getLineOfBusiness() == lineOfBusiness) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the first flight found in the bucket, or null if not found.
	 *
	 * @return
	 */
	public TripBucketItemFlight getFlight() {
		int index = getIndexOf(LineOfBusiness.FLIGHTS);
		return index == -1 ? null : (TripBucketItemFlight) mItems.get(index);
	}

	/**
	 * Populates Db so that Db.getFlightSearch() and friends will reflect
	 * the flight that's stored in the bucket here at getFlight()
	 */
	public void selectFlight() {
		TripBucketItemFlight item = getFlight();
		FlightTrip trip = item.getFlightTrip();
		FlightSearch search = item.getFlightSearch();
		Db.setFlightSearch(search);
		Db.getFlightSearch().clearSelectedLegs();
		int i = 0;
		for (FlightLeg leg : item.getFlightTrip().getLegs()) {
			search.setSelectedLeg(i++, new FlightTripLeg(trip, leg));
		}
	}

	/**
	 * Removes the trip bucket item at position [index]. Silently ignores an out of range index.
	 * @param index
	 */
	public void remove(int index) {
		if (index >= 0 && index < mItems.size()) {
			mItems.remove(index);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putJSONableList(obj, "items", mItems);

			return obj;
		}
		catch (JSONException e) {
			Log.e("TripBucket toJson() failed", e);
		}
		return null;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mItems = JSONUtils.getJSONableList(obj, "items", TripBucketItem.class);
		return true;
	}
}
