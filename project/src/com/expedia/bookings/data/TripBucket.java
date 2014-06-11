package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
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

	private int mRefreshCount;
	private LineOfBusiness mLastLOBAdded;
	private List<TripBucketItem> mItems;

	public TripBucket() {
		mItems = new LinkedList<TripBucketItem>();
	}

	/**
	 * Removes all items from this TripBucket.
	 */
	public void clear() {
		mRefreshCount = 0;
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
	 * Convenience method to determine when we really need to refresh this TripBucket.
	 * @return
	 */
	public boolean doRefresh() {
		if (mRefreshCount <= 0) {
			return false;
		}
		else {
			mRefreshCount--;
			return true;
		}
	}

	/**
	 * Convenience method to determine which LOB to refresh.
	 * @return {LineOfBusiness} If returns null then don't refresh the TripBucket
	 */
	public LineOfBusiness getLOBToRefresh() {
		if (doRefresh()) {
			return mLastLOBAdded;
		}
		else {
			return null;
		}
	}

	/**
	 * Adds a Hotel to the trip bucket. Must specify the property and room rate.
	 * @param rate
	 * @param property
	 */
	public void add(HotelSearchParams params, Rate rate, Property property) {
		mLastLOBAdded = LineOfBusiness.HOTELS;
		mRefreshCount++;
		mItems.add(new TripBucketItemHotel(property, rate, params));
	}

	/**
	 * Adds a Flight to the trip bucket.
	 */
	public void add(FlightSearchParams params, FlightTrip flightTrip) {
		mLastLOBAdded = LineOfBusiness.FLIGHTS;
		mRefreshCount++;
		mItems.add(new TripBucketItemFlight(params, flightTrip));
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
	 * Are there any items in the trip bucket?
	 * @return
	 */
	public boolean isEmpty() {
		return mItems.size() < 1;
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
	 * Populate Db.getHotelSearch()/getFlightSearch() with the Hotel/Flight from the TripBucket.
	 */
	public void selectHotelAndFlight() {
		selectHotel();
		selectFlight();
	}

	/**
	 * Populates Db so that Db.getHotelSearch() and friends will reflect
	 * the hotel that's stored in the bucket here at getHotel()
	 */
	private void selectHotel() {
		TripBucketItemHotel item = getHotel();
		if (item != null) {
			Db.getHotelSearch().setSearchParams(item.getHotelSearchParams());
			Db.getHotelSearch().setSelectedProperty(item.getProperty());
			Db.getHotelSearch().setSelectedHotelAvailability(item.getHotelAvailability());
			Db.getHotelSearch().setSelectedRate(item.getRate(), item.getHotelAvailability());
		}
	}

	/**
	 * Populates Db so that Db.getFlightSearch() and friends will reflect
	 * the flight that's stored in the bucket here at getFlight()
	 */
	private void selectFlight() {
		if (getFlight() != null) {
			Db.getFlightSearch().clearSelectedLegs();
			Db.getFlightSearch().setSearchParams(getFlight().getFlightSearchParams());
			Db.getFlightSearch().setSelectedFlightTrip(getFlight().getFlightTrip());
		}
	}

	/**
	 * Removes the trip bucket item at position [index]. Silently ignores an out of range index.
	 * @param index
	 */
	public void remove(int index) {
		if (index >= 0 && index < mItems.size()) {
			mItems.remove(index);
			if (mRefreshCount > 0) {
				mRefreshCount--;
			}
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
		// We have a custom fromJson because of the way that this class is structured. Because
		// mItems is a Collection of an abstract class, we cannot use the JSONUtils methods and
		// provide TripBucketItem.class and have instantiations occur. Therefore, we store a tag
		// in the toJson of the subclasses and refer to that here in order to build the TripBucket
		// back up again.
		final String key = "items";
		if (obj != null && obj.has(key)) {
			JSONArray arr = obj.optJSONArray(key);
			if (arr != null) {
				int len = arr.length();
				mItems = new ArrayList<TripBucketItem>(len);

				for (int a = 0; a < len; a++) {
					JSONObject jsonObj = arr.optJSONObject(a);

					String type = jsonObj.optString("type");
					if ("hotel".equals(type)) {
						TripBucketItemHotel hotel = new TripBucketItemHotel();
						hotel.fromJson(jsonObj);
						mItems.add(hotel);
					}
					else if ("flight".equals(type)) {
						TripBucketItemFlight flight = new TripBucketItemFlight();
						flight.fromJson(jsonObj);
						mItems.add(flight);
					}

				}
			}
		}
		return true;
	}
}
