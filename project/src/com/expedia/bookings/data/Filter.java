package com.expedia.bookings.data;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class Filter implements JSONable {

	// This is what is defined as a "highly rated" trip advisor rating.
	public static final double HIGH_USER_RATING = 4.5;

	public enum SearchRadius {
		SMALL(2, 2.5), MEDIUM(5, 7.5), LARGE(10, 15), ALL(-1, -1);

		private final double miles;
		private final double kilometers;

		private SearchRadius(double mi, double km) {
			miles = mi;
			kilometers = km;
		}

		public double getRadius(DistanceUnit distanceUnit) {
			if (distanceUnit == DistanceUnit.MILES) {
				return miles;
			}
			else {
				return kilometers;
			}
		}
	}

	public static enum PriceRange {
		CHEAP, MODERATE, EXPENSIVE, ALL
	}

	public static enum Sort {
		POPULAR(R.string.sort_description_popular),
		PRICE(R.string.sort_description_price),
		RATING(R.string.sort_description_rating),
		DISTANCE(R.string.sort_description_distance);

		private int mResId;

		private Sort(int resId) {
			mResId = resId;
		}

		public int getDescriptionResId() {
			return mResId;
		}
	}

	private Set<OnFilterChangedListener> mListeners;

	// This listener is special because it actually updates the data.  This is to solve
	// a race condition where one listener updates the data whereas the other one consumes it.
	private OnFilterChangedListener mDataListener;

	// Filters for the properties list
	private SearchRadius mSearchRadius;
	private DistanceUnit mDistanceUnit;
	private PriceRange mPriceRange;
	private double mMinStarRating;
	private String mHotelName;
	private Sort mSort;

	public Filter() {
		mListeners = new HashSet<Filter.OnFilterChangedListener>();

		// Setup default filters
		mSearchRadius = SearchRadius.ALL;
		mDistanceUnit = DistanceUnit.getDefaultDistanceUnit();
		mPriceRange = PriceRange.ALL;
		mMinStarRating = 0;
		mHotelName = null;
		mSort = Sort.POPULAR;
	}

	public Filter(JSONObject obj) {
		mListeners = new HashSet<Filter.OnFilterChangedListener>();

		if (obj != null) {
			fromJson(obj);
		}
	}

	/**
	 * IMPORTANT: AFTER CALLING THIS, YOU MUST CALL onFilterChanged()!
	 * 
	 * @param searchRadius the search radius, or null for all results
	 */
	public void setSearchRadius(SearchRadius searchRadius) {
		mSearchRadius = searchRadius;
	}

	public SearchRadius getSearchRadius() {
		return mSearchRadius;
	}

	/**
	 * IMPORTANT: AFTER CALLING THIS, YOU MUST CALL onFilterChanged()!
	 * 
	 * @param distanceUnit the distance unit for the search radius
	 */
	public void setDistanceUnit(DistanceUnit distanceUnit) {
		mDistanceUnit = distanceUnit;
	}

	public DistanceUnit getDistanceUnit() {
		return mDistanceUnit;
	}

	/**
	 * IMPORTANT: AFTER CALLING THIS, YOU MUST CALL onFilterChanged()!
	 * 
	 * @param priceFilter the filter for price, or null for all prices
	 */
	public void setPriceRange(PriceRange priceRange) {
		mPriceRange = priceRange;
	}

	public PriceRange getPriceRange() {
		return mPriceRange;
	}

	/**
	 * IMPORTANT: AFTER CALLING THIS, YOU MUST CALL onFilterChanged()!
	 * 
	 * @param starRating the minimum star rating for the hotel; 0 for all hotels
	 */
	public void setMinimumStarRating(double starRating) {
		mMinStarRating = starRating;
	}

	public double getMinimumStarRating() {
		return mMinStarRating;
	}

	/**
	 * The hotel name filter works on partial string matching.  It can match the middle
	 * of a string.
	 * 
	 * IMPORTANT: AFTER CALLING THIS, YOU MUST CALL onFilterChanged()!
	 * 
	 * @param hotelName the string to match the hotel name to
	 */
	public void setHotelName(String hotelName) {
		mHotelName = hotelName;
	}

	public String getHotelName() {
		return mHotelName;
	}

	/**
	 * IMPORTANT: AFTER CALLING THIS, YOU MUST CALL onFilterChanged()!
	 * 
	 * @param sort the sort order
	 */
	public void setSort(Sort sort) {
		mSort = sort;
	}

	public Sort getSort() {
		return mSort;
	}

	public void setOnDataListener(OnFilterChangedListener listener) {
		Log.v("Set OnFilterChangedListener (data): " + listener);
		mDataListener = listener;
	}

	public void addOnFilterChangedListener(OnFilterChangedListener listener) {
		Log.v("Added OnFilterChangedListener: " + listener);
		mListeners.add(listener);
	}

	public void removeOnFilterChangedListener(OnFilterChangedListener listener) {
		Log.v("Removed OnFilterChangedListener: " + listener);
		mListeners.remove(listener);
	}

	public void clearOnFilterChangedListeners() {
		Log.v("Clearing all OnFilterChangedListeners");

		mListeners.clear();
		mDataListener = null;
	}

	public void notifyFilterChanged() {
		Log.v("notifyFilterChanged()");

		// Fire the data listener first
		if (mDataListener != null) {
			Log.v("Firing onDataListener: " + mDataListener.toString());
			mDataListener.onFilterChanged();
		}

		if (mListeners.size() == 0) {
			Log.v("There are no listeners, not firing anything.");
		}

		// All other listeners follow
		for (OnFilterChangedListener listener : mListeners) {
			Log.i("Firing listener: " + listener.toString());
			listener.onFilterChanged();
		}
	}

	public interface OnFilterChangedListener {
		public void onFilterChanged();
	}

	public Filter copy() {
		Filter filter = new Filter();
		filter.setDistanceUnit(mDistanceUnit);
		filter.setPriceRange(mPriceRange);
		filter.setSearchRadius(mSearchRadius);
		filter.setMinimumStarRating(mMinStarRating);
		filter.setHotelName(mHotelName);
		filter.setSort(mSort);
		return filter;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("distanceUnit", mDistanceUnit.toString());
			obj.put("priceRange", mPriceRange.toString());
			obj.put("searchRadius", mSearchRadius.toString());
			obj.put("minStarRating", mMinStarRating);
			obj.put("hotelName", mHotelName);
			obj.put("sort", mSort.toString());
		}
		catch (JSONException e) {
			Log.w("Could not write filter JSON.", e);
		}
		return obj;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mDistanceUnit = DistanceUnit.valueOf(obj.optString("distanceUnit", DistanceUnit.MILES.toString()));
		mPriceRange = PriceRange.valueOf(obj.optString("priceRange", PriceRange.ALL.toString()));
		mSearchRadius = SearchRadius.valueOf(obj.optString("searchRadius", SearchRadius.ALL.toString()));
		mMinStarRating = obj.optDouble("minStarRating", 0);
		mHotelName = obj.optString("hotelName", null);
		mSort = Sort.valueOf(obj.optString("sort", Sort.POPULAR.toString()));
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Filter)) {
			return false;
		}

		Filter other = (Filter) o;

		// Check that hotel names match
		String otherHotelName = other.getHotelName();
		boolean hasHotelName = mHotelName != null;
		boolean otherHasHotelName = otherHotelName != null;
		if (hasHotelName != otherHasHotelName
				|| (hasHotelName && otherHasHotelName && !mHotelName.equals(otherHotelName))) {
			return false;
		}

		// Check the rest
		return mSearchRadius == other.getSearchRadius() && mDistanceUnit == other.getDistanceUnit()
				&& mPriceRange == other.getPriceRange() && mMinStarRating == other.getMinimumStarRating()
				&& mSort == other.getSort();
	}
}
