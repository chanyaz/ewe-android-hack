package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.Filter.OnFilterChangedListener;
import com.expedia.bookings.data.Filter.PriceRange;
import com.expedia.bookings.data.Filter.SearchRadius;
import com.expedia.bookings.data.Filter.Sort;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.mobiata.android.Log;
import com.mobiata.android.MapUtils;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class SearchResponse extends Response implements OnFilterChangedListener, JSONable {
	private SearchType mSearchType;

	// We keep both a list of properties (for ordering) and a map (for easy id lookup)
	private List<Property> mProperties;
	private Map<String, Property> mPropertyMap;

	// For paging through results
	private boolean mLastPage;
	private int mEndingMatchIndex;

	// For paging through results, for Expedia
	private String mCacheKey;
	private String mCacheLocation;

	// If the web service was doing the geocoding, here are where
	// the alternate results will go
	private List<Location> mLocations;

	public SearchResponse() {
		mPriceTiers = new HashMap<Filter.PriceRange, PriceTier>();
		mFilteredProperties = null;
	}

	public SearchResponse(JSONObject obj) {
		this();
		fromJson(obj);
	}

	public List<Property> getProperties() {
		return mProperties;
	}

	public Property getProperty(int index) {
		return mProperties.get(index);
	}

	public Property getProperty(String id) {
		if (id == null || id.length() == 0) {
			return null;
		}

		// We don't bother initializing this until it's used, since it's
		// just a different data representation of the property list.
		if (mPropertyMap == null) {
			mPropertyMap = new HashMap<String, Property>();

			if (mProperties != null) {
				for (Property property : mProperties) {
					mPropertyMap.put(property.getPropertyId(), property);
				}
			}
		}

		return mPropertyMap.get(id);
	}

	public void addProperty(Property property) {
		if (mProperties == null) {
			mProperties = new ArrayList<Property>();
		}

		mProperties.add(property);

		// If we've already loaded the property map, add there as well
		if (mPropertyMap != null) {
			mPropertyMap.put(property.getPropertyId(), property);
		}

		clearCache();
	}

	public int getPropertiesCount() {
		if (mProperties == null) {
			return 0;
		}
		return mProperties.size();
	}

	public boolean isLastPage() {
		return mLastPage;
	}

	public void setLastPage(boolean lastPage) {
		this.mLastPage = lastPage;
	}

	public int getEndingMatchIndex() {
		return mEndingMatchIndex;
	}

	public void setEndingMatchIndex(int endingMatchIndex) {
		this.mEndingMatchIndex = endingMatchIndex;
	}

	public void setCacheKey(String cacheKey) {
		mCacheKey = cacheKey;
	}

	public String getCachekey() {
		return mCacheKey;
	}

	public void setCacheLocation(String cacheLocation) {
		mCacheLocation = cacheLocation;
	}

	public String getCacheLocation() {
		return mCacheLocation;
	}

	public void setSearchType(SearchType searchType) {
		mSearchType = searchType;
	}

	public SearchType getSearchType() {
		return mSearchType;
	}

	public void setLocations(List<Location> locations) {
		mLocations = locations;
	}

	public List<Location> getLocations() {
		return mLocations;
	}

	/**
	 * Below are all Expedia-based ways of presenting the information, based on sorting and filtering
	 */

	// Pricing tiers (clustered)
	private Map<PriceRange, PriceTier> mPriceTiers;

	// The filter that defines how properties are presented
	private Filter mFilter;

	// Search latitude/longitude.  Used for banding results.
	private double mSearchLatitude;
	private double mSearchLongitude;

	// Cached list of filtered properties
	private List<Property> mExpediaSortedProperties;
	private Property[] mFilteredProperties;

	public void setSearchLatLon(double searchLat, double searchLon) {
		mSearchLatitude = searchLat;
		mSearchLongitude = searchLon;
	}

	public void setFilter(Filter filter) {
		mFilter = filter;
		filter.setOnDataListener(this);
	}

	public Filter getFilter() {
		return mFilter;
	}

	public void clearCache() {
		mFilteredProperties = null;
	}

	public boolean filterChanged() {
		return mFilteredProperties == null;
	}

	public PriceTier getPriceTier(PriceRange priceRange) {
		if (mPriceTiers.size() == 0) {
			clusterProperties();
		}

		return mPriceTiers.get(priceRange);
	}

	/**
	 * Get properties of a particular sort.  You should probably set a Filter before running this,
	 * but it'll create one on the fly if you're being lazy.
	 */
	public Property[] getFilteredAndSortedProperties() {
		Log.v("getFilteredAndSortedProperties() called...");

		// If we have no properties set, return null
		if (mProperties == null) {
			Log.v("getFilteredAndSortedProperties() - properties is null, returning null");
			return null;
		}

		// Check that we have a filter, if not create a new one
		if (mFilter == null) {
			Log.v("getFilteredAndSortedProperties() - no filter set, setting default one");
			setFilter(new Filter());
		}

		// Check if we've done the custom POPULAR sort for MY_LOCATION
		if (mExpediaSortedProperties == null) {
			Log.v("getFilteredAndSortedProperties() - No Expedia sorted items, sorting now...");
			Log.v("getFilteredAndSortedProperties() - Current search type: " + mSearchType);

			if (mSearchType == SearchType.MY_LOCATION) {
				Log.v("My location search, doing special sorting...");

				// Here we do banding, as described by #6261
				List<Property> propsShort = new ArrayList<Property>();
				List<Property> propsMedium = new ArrayList<Property>();
				List<Property> propsFar = new ArrayList<Property>();
				List<Property> propsRest = new ArrayList<Property>();

				DistanceUnit distanceUnit = mFilter.getDistanceUnit();
				double smallRadius = Filter.SearchRadius.SMALL.getRadius(distanceUnit);
				double mediumRadius = Filter.SearchRadius.MEDIUM.getRadius(distanceUnit);
				double farRadius = Filter.SearchRadius.LARGE.getRadius(distanceUnit);

				// Our math is based on miles to somewhere; if we're using km, then convert to miles
				// before we do our calculations
				if (distanceUnit == DistanceUnit.KILOMETERS) {
					smallRadius = MapUtils.kilometersToMiles(smallRadius);
					mediumRadius = MapUtils.kilometersToMiles(mediumRadius);
					farRadius = MapUtils.kilometersToMiles(farRadius);
				}

				for (Property property : mProperties) {
					Distance distanceFromUser = property.getDistanceFromUser();
					Location loc = property.getLocation();
					if (distanceFromUser != null || loc != null) {
						double distance;
						if (distanceFromUser != null) {
							distance = distanceFromUser.getDistance(DistanceUnit.MILES);
						}
						else {
							distance = MapUtils.getDistance(mSearchLatitude, mSearchLongitude,
									loc.getLatitude(), loc.getLongitude());
						}

						if (distance <= smallRadius) {
							propsShort.add(property);
						}
						else if (distance <= mediumRadius) {
							propsMedium.add(property);
						}
						else if (distance <= farRadius) {
							propsFar.add(property);
						}
						else {
							propsRest.add(property);
						}
					}
					else {
						propsRest.add(property);
					}
				}

				mExpediaSortedProperties = new ArrayList<Property>(mProperties.size());
				mExpediaSortedProperties.addAll(propsShort);
				mExpediaSortedProperties.addAll(propsMedium);
				mExpediaSortedProperties.addAll(propsFar);
				mExpediaSortedProperties.addAll(propsRest);
			}
			else {
				Log.v("NOT a my location search, skipping special sorting...");

				mExpediaSortedProperties = mProperties;
			}
		}

		// Create a list of filtered properties, if we don't have one yet
		if (mFilteredProperties == null) {
			Log.v("getFilteredAndSortedProperties() - No cached properties, filtering now...");

			// Check if we've clustered properties yet
			if (mPriceTiers.size() == 0) {
				Log.v("getFilteredAndSortedProperties() - No price tiers, clustering now...");
				clusterProperties();
			}

			List<Property> results = new ArrayList<Property>();

			// Get all the current Filter options
			SearchRadius searchRadius = mFilter.getSearchRadius();
			DistanceUnit distanceUnit = mFilter.getDistanceUnit();
			PriceRange priceRange = mFilter.getPriceRange();
			double minStarRating = mFilter.getMinimumStarRating();
			String hotelName = mFilter.getHotelName();

			Distance searchDistance = null;
			if (searchRadius != null && searchRadius != SearchRadius.ALL) {
				searchDistance = new Distance(searchRadius.getRadius(distanceUnit), distanceUnit);
			}

			PriceTier priceTier = null;
			if (priceRange != null && priceRange != PriceRange.ALL) {
				priceTier = mPriceTiers.get(priceRange);
			}

			Pattern namePattern = null;
			if (hotelName != null) {
				namePattern = Pattern.compile("(\\A| )" + hotelName, Pattern.CASE_INSENSITIVE);
			}

			for (Property property : mExpediaSortedProperties) {
				// Filter search radius
				if (searchDistance != null) {
					if (property.getDistanceFromUser() != null) {
						Distance distanceFromUser = new Distance(property.getDistanceFromUser().getDistance(), property
								.getDistanceFromUser().getUnit());
						distanceFromUser.setDistance(Math.rint(distanceFromUser.getDistance() * 10.0d) / 10.0d);
						if (distanceFromUser == null || distanceFromUser.compareTo(searchDistance) > 0) {
							continue;
						}
					}
				}

				// Filter price range
				if (priceTier != null && !priceTier.containsProperty(property)) {
					continue;
				}

				// Filter star rating
				if (minStarRating > property.getHotelRating()) {
					continue;
				}

				if (namePattern != null && !namePattern.matcher(property.getName()).find()) {
					continue;
				}

				// Property passed the tests, add it to results
				results.add(property);
			}

			mFilteredProperties = results.toArray(new Property[0]);
		}

		// Create a copy of the filtered results and sort them
		Property[] filteredProperties = mFilteredProperties.clone();
		Sort sort = mFilter.getSort();
		switch (sort) {
		case PRICE:
			Arrays.sort(filteredProperties, Property.PRICE_COMPARATOR);
			break;
		case DEALS:
			ArrayList<Property> deals = new ArrayList<Property>();
			ArrayList<Property> others = new ArrayList<Property>();
			for (Property p : filteredProperties) {
				if (p.getLowestRate().isSaleTenPercentOrBetter()) {
					deals.add(p);
				}
				else {
					others.add(p);
				}
			}
			Collections.sort(deals, Property.DEALS_COMPARATOR);
			int i = 0;
			for (Property p : deals) {
				filteredProperties[i] = p;
				i++;
			}
			for (Property p : others) {
				filteredProperties[i] = p;
				i++;
			}
			break;
		case RATING:
			Arrays.sort(filteredProperties, Property.RATING_COMPARATOR);
			break;
		case DISTANCE:
			Arrays.sort(filteredProperties, Property.DISTANCE_COMPARATOR);
			break;
		case POPULAR:
		default:
			// The default sort is POPULAR, which requires no special sorting
			break;
		}

		return filteredProperties;
	}

	public Property[] getFilteredAndSortedProperties(Sort sort) {
		mFilter = new Filter();
		mFilter.setSort(sort);

		return getFilteredAndSortedProperties();
	}

	public void onFilterChanged() {
		clearCache();
	}

	//////////////////////////////////////////////////////////////////////////////////
	/////// Clustering section

	/**
	 * Clusters the properties into price tiers.
	 */
	public void clusterProperties() {
		mPriceTiers.clear();

		// Extract the prices from the properties
		int len = mProperties.size();
		double[] prices = new double[len];
		for (int index = 0; index < len; index++) {
			prices[index] = mProperties.get(index).getLowestRate().getDisplayRate().getAmount().doubleValue();
		}

		// Cluster
		double[] medoids = cluster(prices);

		// Separate out based on medoid values
		@SuppressWarnings("unchecked")
		ArrayList<Property>[] tiers = new ArrayList[] { new ArrayList<Property>(), new ArrayList<Property>(),
				new ArrayList<Property>() };
		double closest;
		double tmp;
		double amount;
		int closestIndex;
		for (Property property : mProperties) {
			closestIndex = 0;
			amount = property.getLowestRate().getDisplayRate().getAmount().doubleValue();
			closest = Math.abs(amount - medoids[0]);
			for (int a = 1; a < 3; a++) {
				tmp = Math.abs(amount - medoids[a]);
				if (tmp < closest) {
					closest = tmp;
					closestIndex = a;
				}
			}
			tiers[closestIndex].add(property);
		}

		// #9396: Check that each tier has at least on property in it.  This only really fails when
		// there's < 3 prices available, so it's okay to just duplicate properties from a tier above/below
		if (tiers[1].size() == 0) {
			if (tiers[2].size() > 0) {
				tiers[1].add(tiers[2].get(0));
			}
			else {
				tiers[1].add(tiers[0].get(0));
			}
		}
		if (tiers[0].size() == 0) {
			tiers[0].add(tiers[1].get(0));
		}
		if (tiers[2].size() == 0) {
			tiers[2].add(tiers[1].get(0));
		}

		mPriceTiers.put(PriceRange.CHEAP, new PriceTier(tiers[0]));
		mPriceTiers.put(PriceRange.MODERATE, new PriceTier(tiers[1]));
		mPriceTiers.put(PriceRange.EXPENSIVE, new PriceTier(tiers[2]));
		mPriceTiers.put(PriceRange.ALL, new PriceTier(mProperties));
	}

	private static final double TOLERANCE = .01;

	// Clusters a list of prices into cheap, moderate and expensive.  Returns the three centers of
	// each cluster (leaving it as an exercise to split the Properties into clusters later)
	public double[] cluster(double[] prices) {
		if (prices == null || prices.length == 0) {
			return null;
		}

		int len = prices.length;

		// Sort the prices
		Arrays.sort(prices);

		// SPECIAL CASES: For fewer than 3 items
		if (len == 2) {
			return new double[] { prices[0], prices[0], prices[1] };
		}
		else if (len == 1) {
			return new double[] { prices[0], prices[0], prices[0] };
		}

		// Pick three reasonable starting medoids
		int[] medoidIndexes = new int[] { len / 6, len / 2, (len * 5) / 6 };

		// Determine initial clustering
		double totalError = getTotalError(prices, medoidIndexes);

		// The way this works is to move the medoids left and right, attempting to minimize error.
		// We optimize things by making assumptions about the data, since it's linear and sorted.  Basically what
		// we are doing is calculating the difference in error by moving a medoid left and right; it may not
		// change between iterations if a related medoid was not moved.
		Double[] changes = new Double[6];
		int[] newMedoidIndex = new int[6];
		Arrays.fill(changes, null);
		int[] tmpMedoidIndexes = new int[3];
		while (true) {
			// Medoid0 index--
			if (changes[0] == null) {
				System.arraycopy(medoidIndexes, 0, tmpMedoidIndexes, 0, 3);
				tmpMedoidIndexes[0] = newMedoidIndex[0] = nextValidIndexLeft(prices, medoidIndexes[0], 0);
				changes[0] = getTotalError(prices, tmpMedoidIndexes) - totalError;
			}

			// Medoid0 index++
			if (changes[1] == null) {
				System.arraycopy(medoidIndexes, 0, tmpMedoidIndexes, 0, 3);
				tmpMedoidIndexes[0] = newMedoidIndex[1] = nextValidIndexRight(prices, medoidIndexes[0],
						medoidIndexes[1] - 1);
				changes[1] = getTotalError(prices, tmpMedoidIndexes) - totalError;
			}

			// Medoid1 index--
			if (changes[2] == null) {
				System.arraycopy(medoidIndexes, 0, tmpMedoidIndexes, 0, 3);
				tmpMedoidIndexes[1] = newMedoidIndex[2] = nextValidIndexLeft(prices, medoidIndexes[1],
						medoidIndexes[0] + 1);
				changes[2] = getTotalError(prices, tmpMedoidIndexes) - totalError;
			}

			// Medoid1 index++
			if (changes[3] == null) {
				System.arraycopy(medoidIndexes, 0, tmpMedoidIndexes, 0, 3);
				tmpMedoidIndexes[1] = newMedoidIndex[3] = nextValidIndexRight(prices, medoidIndexes[1],
						medoidIndexes[2] - 1);
				changes[3] = getTotalError(prices, tmpMedoidIndexes) - totalError;
			}

			// Medoid2 index--
			if (changes[4] == null) {
				System.arraycopy(medoidIndexes, 0, tmpMedoidIndexes, 0, 3);
				tmpMedoidIndexes[2] = newMedoidIndex[4] = nextValidIndexLeft(prices, medoidIndexes[2],
						medoidIndexes[1] + 1);
				changes[4] = getTotalError(prices, tmpMedoidIndexes) - totalError;
			}

			// Medoid2 index++
			if (changes[5] == null) {
				System.arraycopy(medoidIndexes, 0, tmpMedoidIndexes, 0, 3);
				tmpMedoidIndexes[2] = newMedoidIndex[5] = nextValidIndexRight(prices, medoidIndexes[2], len - 1);
				changes[5] = getTotalError(prices, tmpMedoidIndexes) - totalError;
			}

			int bestIndex = 0;
			double mostSavings = changes[0];
			for (int index = 1; index < 6; index++) {
				if (changes[index] < mostSavings) {
					mostSavings = changes[index];
					bestIndex = index;
				}
			}

			if (mostSavings >= 0) {
				return new double[] { prices[medoidIndexes[0]], prices[medoidIndexes[1]], prices[medoidIndexes[2]] };
			}

			switch (bestIndex) {
			case 0:
				// Setup data for index that we just moved from
				newMedoidIndex[1] = medoidIndexes[0];
				changes[1] = -mostSavings;

				// Move to new index
				medoidIndexes[0] = newMedoidIndex[0];

				// Clear out results that are no longer valid
				changes[0] = null;
				changes[2] = null;
				changes[3] = null;
				break;
			case 1:
				// Setup data for index that we just moved from
				newMedoidIndex[0] = medoidIndexes[0];
				changes[0] = -mostSavings;

				// Move to new index
				medoidIndexes[0] = newMedoidIndex[1];

				// Clear out results that are no longer valid
				changes[1] = null;
				changes[2] = null;
				changes[3] = null;
				break;
			case 2:
				// Setup data for index that we just moved from
				newMedoidIndex[3] = medoidIndexes[1];
				changes[3] = -mostSavings;

				// Move to new index
				medoidIndexes[1] = newMedoidIndex[2];

				// Clear out results that are no longer valid
				changes[0] = null;
				changes[1] = null;
				changes[2] = null;
				changes[4] = null;
				changes[5] = null;
				break;
			case 3:
				// Setup data for index that we just moved from
				newMedoidIndex[2] = medoidIndexes[1];
				changes[2] = -mostSavings;

				// Move to new index
				medoidIndexes[1] = newMedoidIndex[3];

				// Clear out results that are no longer valid
				changes[0] = null;
				changes[1] = null;
				changes[3] = null;
				changes[4] = null;
				changes[5] = null;
				break;
			case 4:
				// Setup data for index that we just moved from
				newMedoidIndex[5] = medoidIndexes[0];
				changes[5] = -mostSavings;

				// Move to new index
				medoidIndexes[2] = newMedoidIndex[4];

				// Clear out results that are no longer valid
				changes[2] = null;
				changes[3] = null;
				changes[4] = null;
				break;
			case 5:
				// Setup data for index that we just moved from
				newMedoidIndex[4] = medoidIndexes[0];
				changes[4] = -mostSavings;

				// Move to new index
				medoidIndexes[2] = newMedoidIndex[5];

				// Clear out results that are no longer valid
				changes[2] = null;
				changes[3] = null;
				changes[5] = null;
				break;
			}

			totalError += mostSavings;
		}
	}

	// Keeps shifting the index left until a different price is found (outside TOLERANCE),
	// or until the index has hit the minimum index
	public int nextValidIndexLeft(double[] prices, int startingIndex, int minIndex) {
		double val = prices[startingIndex];
		for (int index = startingIndex - 1; index > minIndex; index--) {
			if (val - prices[index] > TOLERANCE) {
				return index;
			}
		}
		return minIndex;
	}

	// Keeps shifting the index right until a different price is found (outside TOLERANCE),
	// or until the index has hit the minimum index
	public int nextValidIndexRight(double[] prices, int startingIndex, int maxIndex) {
		double val = prices[startingIndex];
		for (int index = startingIndex + 1; index < maxIndex; index++) {
			if (prices[index] - val > TOLERANCE) {
				return index;
			}
		}
		return maxIndex;
	}

	// Makes three assumptions:
	// 1. There are at least three values in prices.
	// 2. prices is sorted
	// 3. medoidIndexes is sorted
	public double getTotalError(double[] prices, int[] medoidIndexes) {
		double total = 0;

		double medoid0 = prices[medoidIndexes[0]];
		double medoid1 = prices[medoidIndexes[1]];
		double medoid2 = prices[medoidIndexes[2]];

		int len = prices.length;
		int n = 1;
		double curr = prices[0];
		double diff;

		// Stage 1 - prices are lower than medoid0
		while (curr <= medoid0 && n < len) {
			total += medoid0 - curr;
			curr = prices[n++];
		}

		// Stage 2 - prices are between medoid0 and medoid1, but closer to medoid0
		while ((diff = curr - medoid0) < medoid1 - curr && n < len) {
			total += diff;
			curr = prices[n++];
		}

		// Stage 3 - prices are between medoid0 and medoid1, but closer to medoid1
		while (curr <= medoid1 && n < len) {
			total += medoid1 - curr;
			curr = prices[n++];
		}

		// Stage 4 - prices are between medoid1 and medoid2, but closer to medoid1
		while ((diff = curr - medoid1) < medoid2 - curr && n < len) {
			total += diff;
			curr = prices[n++];
		}

		// Stage 5 - prices are between medoid1 and medoid2, but closer to medoid2
		while (curr < medoid2 && n < len) {
			total += medoid2 - curr;
			curr = prices[n++];
		}

		// Stage 6 - prices are above medoid2
		while (n < len) {
			total += curr - medoid2;
			curr = prices[n++];
		}

		// Above loop exits one too early, account for that here
		total += curr - medoid2;

		return total;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable interface

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			if (mSearchType != null) {
				obj.putOpt("searchType", mSearchType.toString());
			}

			JSONUtils.putJSONableList(obj, "properties", mProperties);

			obj.putOpt("lastPage", mLastPage);
			obj.putOpt("endingMatchIndex", mEndingMatchIndex);

			obj.putOpt("cacheKey", mCacheKey);
			obj.putOpt("cacheLocation", mCacheLocation);

			JSONUtils.putJSONableList(obj, "locations", mLocations);

			obj.putOpt("searchLatitude", mSearchLatitude);
			obj.putOpt("searchLongitude", mSearchLongitude);

			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert SearchResponse to JSON", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		try {
			mSearchType = SearchType.valueOf(obj.getString("searchType"));
			mProperties = (List<Property>) JSONUtils.getJSONableList(obj, "properties", Property.class);

			mLastPage = obj.optBoolean("lastPage");
			mEndingMatchIndex = obj.optInt("endingMatchIndex");

			mCacheKey = obj.optString("cacheKey", null);
			mCacheLocation = obj.optString("cacheLocation", null);

			mLocations = (List<Location>) JSONUtils.getJSONableList(obj, "locations", Location.class);

			mSearchLatitude = obj.optDouble("searchLatitude");
			mSearchLongitude = obj.optDouble("searchLongitude");

			return true;
		}
		catch (JSONException e) {
			Log.e("Could not create SearchResponse from JSON.", e);
			return false;
		}
	}
}
