package com.expedia.bookings.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class HotelFilter implements JSONable {

	// This is what is defined as a "highly rated" trip advisor rating.
	public static final double HIGH_USER_RATING = 4.5;

	public enum SearchRadius {
		SMALL(2, 2.5), MEDIUM(5, 7.5), LARGE(10, 15), ALL(-1, -1);

		private final double miles;
		private final double kilometers;

		SearchRadius(double mi, double km) {
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

	enum PriceRange {
		CHEAP, MODERATE, EXPENSIVE, ALL
	}

	public enum Sort {
		RECOMMENDED(R.string.sort_description_recommended),
		PRICE(R.string.sort_description_price),
		DEALS(R.string.sort_description_deals),
		RATING(R.string.sort_description_rating),
		DISTANCE(R.string.sort_description_distance);

		private int mResId;

		Sort(int resId) {
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
	private boolean mVipAccessOnly;
	private Set<Integer> mNeighborhoods;

	public HotelFilter() {
		mListeners = new HashSet<HotelFilter.OnFilterChangedListener>();

		// Setup default filters
		reset();
	}

	public HotelFilter(JSONObject obj) {
		mListeners = new HashSet<HotelFilter.OnFilterChangedListener>();

		if (obj != null) {
			fromJson(obj);
		}
	}

	/**
	 * Resets the filter to its default settings.  (Does not clear listeners.)
	 */
	public void reset() {
		mSearchRadius = SearchRadius.ALL;
		mDistanceUnit = DistanceUnit.getDefaultDistanceUnit();
		mPriceRange = PriceRange.ALL;
		mMinStarRating = 0;
		mHotelName = null;
		mSort = Sort.RECOMMENDED;
		mVipAccessOnly = false;
		mNeighborhoods = null;
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

	public void setVipAccessOnly(boolean vipAccessOnly) {
		mVipAccessOnly = vipAccessOnly;
	}

	public boolean isVipAccessOnly() {
		return mVipAccessOnly;
	}

	public void setNeighborhoods(Set<Integer> neighborhoods) {
		mNeighborhoods = neighborhoods;
	}

	/**
	 * There is a difference between "null" and an empty set. A "null" value
	 * means that we are not filtering by neighborhood at all. An empty set
	 * means that we are filtering by neighborhood, and the user has unchecked
	 * all neighborhoods.
	 * @return
	 */
	public Set<Integer> getNeighborhoods() {
		return mNeighborhoods;
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
		void onFilterChanged();
	}

	public HotelFilter copy() {
		HotelFilter filter = new HotelFilter();
		filter.setDistanceUnit(mDistanceUnit);
		filter.setPriceRange(mPriceRange);
		filter.setSearchRadius(mSearchRadius);
		filter.setMinimumStarRating(mMinStarRating);
		filter.setHotelName(mHotelName == null ? null : new String(mHotelName));
		filter.setSort(mSort);
		filter.setVipAccessOnly(mVipAccessOnly);
		Set<Integer> neighborhoodsCopy = mNeighborhoods == null ? null : new HashSet<Integer>(mNeighborhoods);
		filter.setNeighborhoods(neighborhoodsCopy);
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
			obj.put("vipAccess", mVipAccessOnly);
			if (mNeighborhoods != null) {
				obj.put("neighborhoods", new JSONArray(mNeighborhoods));
			}
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
		mSort = Sort.valueOf(obj.optString("sort", Sort.RECOMMENDED.toString()));
		mVipAccessOnly = obj.optBoolean("vipAccess", false);
		if (obj.has("neighborhoods")) {
			JSONArray neighborhoods = obj.optJSONArray("neighborhoods");
			mNeighborhoods = new HashSet<Integer>();
			int len = neighborhoods.length();
			for (int i = 0; i < len; i++) {
				mNeighborhoods.add(neighborhoods.optInt(i));
			}
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof HotelFilter)) {
			return false;
		}

		HotelFilter other = (HotelFilter) o;

		// Check the rest
		boolean ret = true;
		ret &= mSearchRadius == other.getSearchRadius();
		ret &= mDistanceUnit == other.getDistanceUnit();
		ret &= mPriceRange == other.getPriceRange();
		ret &= mMinStarRating == other.getMinimumStarRating();
		ret &= mSort == other.getSort();
		ret &= mVipAccessOnly == other.isVipAccessOnly();

		// Compare the complicated ones at the bottom... if any of the others
		// are false, these won't even bother executing.
		ret &= TextUtils.equals(mHotelName, other.getHotelName());
		ret &= compareNeighborhoods(mNeighborhoods, other.mNeighborhoods);

		return ret;
	}

	private static boolean compareNeighborhoods(Set<Integer> n1, Set<Integer> n2) {
		boolean ret = true;
		ret &= n1 == null || n2 != null && n1.containsAll(n2);
		ret &= n2 == null || n1 != null && n2.containsAll(n1);
		return ret;
	}

	public void diff(HotelFilter other) {
		if (other == null) {
			Log.d("HotelFilter diff: other == null");
			return;
		}

		if (!TextUtils.equals(mHotelName, other.getHotelName())) {
			Log.d("HotelFilter diff: Hotel Names: " + mHotelName + ", " + other.getHotelName());
		}

		if (mSearchRadius != other.getSearchRadius()) {
			Log.d("HotelFilter diff: Search Radius: " + mSearchRadius + ", " + other.getSearchRadius());
		}

		if (mDistanceUnit != other.getDistanceUnit()) {
			Log.d("HotelFilter diff: Distance Units: " + mDistanceUnit + ", " + other.getDistanceUnit());
		}

		if (mPriceRange != other.getPriceRange()) {
			Log.d("HotelFilter diff: Price Range: " + mPriceRange + ", " + other.getPriceRange());
		}

		if (mMinStarRating != other.getMinimumStarRating()) {
			Log.d("HotelFilter diff: Star Rating: " + mMinStarRating + ", " + other.getMinimumStarRating());
		}

		if (mSort != other.getSort()) {
			Log.d("HotelFilter diff: Sort: " + mSort + ", " + other.getSort());
		}

		if (mVipAccessOnly != other.isVipAccessOnly()) {
			Log.d("HotelFilter diff: Vip Access: " + mVipAccessOnly + ", " + other.isVipAccessOnly());
		}

		if (!compareNeighborhoods(mNeighborhoods, other.mNeighborhoods)) {
			String n1 = mNeighborhoods == null ? "null" : Arrays.toString(mNeighborhoods.toArray());
			String n2 = other.mNeighborhoods == null ? "null" : Arrays.toString(other.mNeighborhoods.toArray());
			Log.d("HotelFilter diff: Neighborhoods: " + n1 + ", " + n2);
		}
	}
}
