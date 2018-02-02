package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.expedia.bookings.data.AirAttach;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelAvailability;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.TripBucketItemFlightV2;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.JodaUtils;
import com.google.android.gms.maps.model.LatLng;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.maps.MapUtils;

/**
 * This class is the single source of truth for storing product checkout related information, e.g. anything
 * with a price that the user has expressed some interest in booking.
 */
public class TripBucket implements JSONable {

	private int mRefreshCount;
	private LineOfBusiness mLastLOBAdded;
	private List<TripBucketItem> mItems;

	public TripBucket() {
		mItems = new LinkedList<TripBucketItem>();
	}

	public AirAttach mAirAttach;

	/**
	 * Removes all items from this TripBucket.
	 */
	public void clear() {
		mRefreshCount = 0;
		mItems.clear();
	}

	/**
	 * Removes all items with lineOfBusiness from this TripBucket.
	 *
	 * @param lineOfBusiness
	 */
	public void clear(LineOfBusiness lineOfBusiness) {
		for (int i = 0; i < mItems.size(); i++) {
			while (i < mItems.size() && mItems.get(i).getLineOfBusiness() == lineOfBusiness) {
				remove(i);
			}
		}
	}

	public void clearHotel() {
		clear(LineOfBusiness.HOTELS);
	}

	public void clearFlight() {
		clear(LineOfBusiness.FLIGHTS);
		clear(LineOfBusiness.FLIGHTS_V2);
	}

	public void clearLX() {
		clear(LineOfBusiness.LX);
	}

	public void clearTransport() {
		clear(LineOfBusiness.TRANSPORT);
	}

	public void clearHotelV2() {
		clear(LineOfBusiness.HOTELS);
	}

	public void clearPackages() {
		clear(LineOfBusiness.PACKAGES);
	}

	public void clearRails() {
		clear(LineOfBusiness.RAILS);
	}

	/**
	 * Convenience method to determine when we really need to refresh this TripBucket.
	 *
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
	 *
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

	public void add(TripBucketItemHotel hotel) {
		mLastLOBAdded = LineOfBusiness.HOTELS;
		mRefreshCount++;
		mItems.add(hotel);

		checkForTabletMismatchedItems();
	}

	/**
	 * Adds a Hotel to the trip bucket. Must specify the property and room rate.
	 *
	 * @param hotelSearch
	 * @param rate
	 * @param availability
	 */
	public void add(HotelSearch hotelSearch, Rate rate, HotelAvailability availability) {
		add(new TripBucketItemHotel(hotelSearch, rate, availability));
	}

	public void add(TripBucketItemFlight flight) {
		addBucket(flight);
	}

	public void add(TripBucketItemFlightV2 flight) {
		addBucket(flight);
	}

	public void add(TripBucketItemLX lx) {
		addBucket(lx);
	}

	public void add(TripBucketItemTransport transport) {
		addBucket(transport);
	}

	public void add(TripBucketItemHotelV2 hotelV2) {
		addBucket(hotelV2);
	}

	public void add(TripBucketItemPackages packages) {
		addBucket(packages);
	}

	public void add(TripBucketItemRails rails) {
		addBucket(rails);
	}

	private void addBucket(TripBucketItem tripBucketItem) {
		mLastLOBAdded = tripBucketItem.getLineOfBusiness();
		mRefreshCount++;
		mItems.add(tripBucketItem);
	}

	public void add(FlightSearch flightSearch) {
		add(new TripBucketItemFlight(flightSearch));
	}

	public int size() {
		return mItems.size();
	}

	/**
	 * Are there any items in the trip bucket?
	 *
	 * @return
	 */
	public boolean isEmpty() {
		return mItems.size() < 1;
	}

	/**
	 * Returns the first LX found in the bucket, or null if not found.
	 *
	 * @return
	 */
	public TripBucketItemLX getLX() {
		int index = getIndexOf(LineOfBusiness.LX);
		return index == -1 ? null : (TripBucketItemLX) mItems.get(index);
	}

	public TripBucketItemTransport getTransport() {
		int index = getIndexOf(LineOfBusiness.TRANSPORT);
		return index == -1 ? null : (TripBucketItemTransport) mItems.get(index);
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
	 * Returns the first hotel found in the bucket, or null if not found.
	 *
	 * @return
	 */
	public TripBucketItemHotelV2 getHotelV2() {
		int index = getIndexOf(LineOfBusiness.HOTELS);
		return index == -1 ? null : (TripBucketItemHotelV2) mItems.get(index);
	}

	public TripBucketItemPackages getPackage() {
		int index = getIndexOf(LineOfBusiness.PACKAGES);
		return index == -1 ? null : (TripBucketItemPackages) mItems.get(index);
	}

	/**
	 * Returns the trip bucket item based on LOB, or null if not found.
	 *
	 * @return
	 */
	public TripBucketItem getItem(LineOfBusiness lineOfBusiness) {
		int index = getIndexOf(lineOfBusiness);
		return index == -1 ? null : mItems.get(index);
	}

	/**
	 * Returns the index of the first item of LineOfBusiness found in this TripBucket.
	 *
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
	 * Returns the first flight found in the bucket, or null if not found.
	 *
	 * @return
	 */
	public TripBucketItemFlightV2 getFlightV2() {
		int index = getIndexOf(LineOfBusiness.FLIGHTS_V2);
		return index == -1 ? null : (TripBucketItemFlightV2) mItems.get(index);
	}

	/**
	 * Removes the trip bucket item at position [index]. Silently ignores an out of range index.
	 *
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

	private void checkForTabletMismatchedItems() {
		if (getFlight() == null || getHotel() == null) {
			// Don't bother checking if we don't have both items
			return;
		}

		if (hasRedeyeDates()) {
			Events.post(new Events.TripBucketHasRedeyeItems());
		}
		else if (hasMismatchedDates()) {
			Events.post(new Events.TripBucketHasMismatchedItems());
		}
	}

	private boolean hasRedeyeDates() {
		if (!getFlight().getFlightSearchParams().isRoundTrip()) {
			// We don't trigger the redeye case for round trips
			return false;
		}

		if (!itemsAreForSameDestination()) {
			return false;
		}

		return hotelCheckinIsDayBeforeFlightLands() && !hotelCheckoutIsBeforeOrAfterFlightLeaves();

	}

	private boolean hasMismatchedDates() {
		if (!itemsAreForSameDestination()) {
			return false;
		}

		if (hotelCheckinIsDayBeforeFlightLands()) {
			return true;
		}

		return daysBetweenFlights() && hotelCheckoutIsBeforeOrAfterFlightLeaves();

	}

	private boolean hotelCheckinIsDayBeforeFlightLands() {
		HotelSearchParams hotelParams = getHotel().getHotelSearchParams();
		FlightLeg departingFlight = getFlight().getFlightTrip().getLeg(0);
		LocalDate departingFlightLandingTime = new LocalDate(
			departingFlight.getLastWaypoint().getMostRelevantDateTime());
		if (hotelParams.getCheckInDate().isBefore(departingFlightLandingTime)) {
			Log.d("TripBucket", "Hotel Checkin is the day before Departing Flight Lands");
			return true;
		}

		return false;
	}

	private boolean hotelCheckoutIsBeforeOrAfterFlightLeaves() {
		HotelSearchParams hotelParams = getHotel().getHotelSearchParams();
		if (getFlight().getFlightSearchParams().isRoundTrip()) {
			FlightLeg returnFlight = getFlight().getFlightTrip().getLeg(1);
			LocalDate returnFlightTakeoffTime = new LocalDate(
				returnFlight.getFirstWaypoint().getMostRelevantDateTime());
			if (hotelParams.getCheckOutDate().isAfter(returnFlightTakeoffTime) ||
				hotelParams.getCheckOutDate().isBefore(returnFlightTakeoffTime)) {
				Log.d("TripBucket", "Hotel Checkout is the day before or after Returning Flight Takes off");
				return true;
			}
		}

		return false;
	}

	private boolean daysBetweenFlights() {
		if (!getFlight().getFlightSearchParams().isRoundTrip()) {
			return true;
		}

		FlightLeg departingFlight = getFlight().getFlightTrip().getLeg(0);
		LocalDate departingFlightLandingTime = new LocalDate(
			departingFlight.getLastWaypoint().getMostRelevantDateTime());
		FlightLeg returnFlight = getFlight().getFlightTrip().getLeg(1);
		LocalDate returnFlightTakeoffTime = new LocalDate(returnFlight.getFirstWaypoint().getMostRelevantDateTime());

		return JodaUtils.daysBetween(departingFlightLandingTime, returnFlightTakeoffTime) > 0;
	}

	private boolean itemsAreForSameDestination() {
		HotelSearchParams hotelParams = getHotel().getHotelSearchParams();
		FlightSearchParams flightParams = getFlight().getFlightSearchParams();

		if (TextUtils.equals("" + flightParams.getDestinationId(), hotelParams.getRegionId())) {
			Log.d("TripBucket", "Items are for same region id");
			return true;
		}

		// check latlng within 50 miles
		Location flightLocation = flightParams.getArrivalLocation();
		LatLng flightLL = new LatLng(flightLocation.getLatitude(), flightLocation.getLongitude());
		LatLng hotelLL = new LatLng(hotelParams.getSearchLatitude(), hotelParams.getSearchLongitude());

		double distance = MapUtils.getDistance(flightLL, hotelLL);
		if (distance < 50.0f) {
			Log.d("TripBucket", "Items are within 50miles of each other");
			return true;
		}

		return false;
	}

	public AirAttach getAirAttach() {
		return mAirAttach;
	}

	/**
	 * Set a new global air attach state if
	 * 1) we don't have one or
	 * 2) the existing air attach qualification is outdated
	 *
	 * @param airAttach
	 * @return whether or not air attach was updated
	 */
	public boolean setAirAttach(AirAttach airAttach) {
		if (mAirAttach == null || !mAirAttach.isAirAttachQualified() || mAirAttach.getExpirationDate()
			.isBefore(airAttach.getExpirationDate())) {
			mAirAttach = airAttach;
			return true;
		}
		return false;
	}

	public void clearAirAttach() {
		mAirAttach = null;
	}

	public boolean isUserAirAttachQualified() {
		return mAirAttach != null && mAirAttach.isAirAttachQualified();
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putJSONableList(obj, "items", mItems);
			JSONUtils.putJSONable(obj, "airAttach", mAirAttach);
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
		mAirAttach = JSONUtils.getJSONable(obj, "airAttach", AirAttach.class);
		return true;
	}
}
