package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.HotelFilter.OnFilterChangedListener;
import com.expedia.bookings.data.HotelFilter.PriceRange;
import com.expedia.bookings.data.HotelFilter.SearchRadius;
import com.expedia.bookings.data.HotelFilter.Sort;
import com.expedia.bookings.data.DeprecatedHotelSearchParams.SearchType;
import com.expedia.bookings.data.Rate.UserPriceType;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.maps.MapUtils;

public class HotelSearchResponse extends Response implements OnFilterChangedListener, JSONable {
	// Sponsored hotels are displayed at the index 1, 51, 52
	// The server does not return them in this order so they
	// are currently hardcoded to these indexes.
	// For a search in SF, the ads came back in pos 0, 198, 199
	private static final int[] sponsoredIndexes = { 0, 50, 51 };

	// The original list of properties in this response
	private List<Property> mProperties;

	// If the web service was doing the geocoding, here are where
	// the alternate results will go
	private List<Location> mLocations;

	private boolean hasSponsoredListing;
	private String mBeaconUrl;

	public HotelSearchResponse() {
		mPriceTiers = new HashMap<HotelFilter.PriceRange, PriceTier>();
		mFilteredProperties = null;
	}

	public HotelSearchResponse(JSONObject obj) {
		this();
		fromJson(obj);
	}

	public void setBeaconUrl(String url) {
		this.mBeaconUrl = url;
	}

	public String getBeaconUrl() {
		return this.mBeaconUrl;
	}

	public List<Property> getProperties() {
		return mProperties;
	}

	public void addProperty(Property property) {
		if (mProperties == null) {
			mProperties = new ArrayList<Property>();
		}

		mProperties.add(property);

		clearCache();
	}

	public void removeProperty(Property property) {
		if (mProperties == null) {
			mProperties.remove(property);
		}
		clearCache();
	}

	public int getPropertiesCount() {
		if (mProperties == null) {
			return 0;
		}
		return mProperties.size();
	}

	public void setLocations(List<Location> locations) {
		mLocations = locations;
	}

	public List<Location> getLocations() {
		return mLocations;
	}

	public boolean hasSponsoredListing() {
		return hasSponsoredListing;
	}

	public void setHasSponsoredListing(boolean hasASponsoredListing) {
		hasSponsoredListing = hasASponsoredListing;
	}

	/**
	 * Convenience method; assumes that all rates in a response use the same
	 * price type (which is a safe assumption to make for the time being).
	 *
	 * @return the first UserPriceType in the rates, or null if there are no rates
	 */
	public UserPriceType getUserPriceType() {
		if (mProperties != null) {
			for (Property property : mProperties) {
				Rate rate = property.getLowestRate();
				if (rate != null) {
					return rate.getUserPriceType();
				}
			}
		}

		return null;
	}

	/**
	 * Below are all Expedia-based ways of presenting the information, based on sorting and filtering
	 */

	// Pricing tiers (clustered)
	private Map<PriceRange, PriceTier> mPriceTiers;

	// The filter that defines how properties are presented
	private HotelFilter mFilter;

	/**
	 * Cached subset of mProperties that has been sorted by distance bands.
	 */
	private List<Property> mPresortedProperties;

	/**
	 * Cached subset of mProperties that has already been filtered
	 * according to mFilter.
	 */
	private Collection<Property> mFilteredProperties;

	/**
	 * Cached subset of mProperties that has already been filtered
	 * according to mFilter, ignoring the neighborhood part of mFilter.
	 * We'll use this to determine what neighborhoods are available,
	 * even if they've been unselected in mFilter.
	 */
	private Collection<Property> mFilteredPropertiesIgnoringNeighborhood;

	public void setFilter(HotelFilter filter) {
		mFilter = filter;
		filter.setOnDataListener(this);
	}

	public HotelFilter getFilter() {
		return mFilter;
	}

	public void clearCache() {
		mFilteredProperties = null;
		mFilteredPropertiesIgnoringNeighborhood = null;
	}

	public PriceRange getPriceRange(Property property) {
		if (mPriceTiers.size() == 0) {
			return PriceRange.ALL;
		}

		for (PriceRange range : PriceRange.values()) {
			PriceTier tier = mPriceTiers.get(range);
			if (tier.containsProperty(property)) {
				return range;
			}
		}

		return PriceRange.ALL;
	}

	/**
	 * Returns a collection of properties filtered by this object's filter.
	 *
	 * @return
	 */
	private Collection<Property> getFilteredProperties(DeprecatedHotelSearchParams searchParams) {
		// If we have no properties set, return null
		if (mProperties == null) {
			Log.v("getFilteredProperties() - properties is null, returning null");
			return null;
		}

		performFiltering(searchParams);
		removeTravelAdFromFilter(mFilteredProperties);

		return mFilteredProperties;
	}

	/**
	 * Returns a collection of properties filtered by this object's filter, ignoring
	 * the "neighborhoods" part of the filter.
	 *
	 * @return
	 */
	public Collection<Property> getFilteredPropertiesIgnoringNeighborhood(DeprecatedHotelSearchParams searchParams) {
		// If we have no properties set, return null
		if (mProperties == null) {
			Log.v("getFilteredPropertiesIgnoringNeighborhood() - properties is null, returning null");
			return null;
		}

		performFiltering(searchParams);
		removeTravelAdFromFilter(mFilteredPropertiesIgnoringNeighborhood);

		return mFilteredPropertiesIgnoringNeighborhood;
	}

	/**
	 * This removes a travel ad from the filtered results
	 * if the only other result is the same listing as the ad.
	 */
	public void removeTravelAdFromFilter(Collection<Property> properties) {
		boolean hasSponsoredAndDuplicatedProperty;
		if (properties.size() == 2) {
			Property p1 = (Property) properties.toArray()[0];
			Property p2 = (Property) properties.toArray()[1];

			hasSponsoredAndDuplicatedProperty =
				(p1.isSponsored() || p2.isSponsored()) && p1.getPropertyId().equalsIgnoreCase(p2.getPropertyId());

			if (hasSponsoredAndDuplicatedProperty) {
				properties.remove(p1.isSponsored() ? p1 : p2);
			}

		}
	}


	/**
	 * Get properties of a particular sort.  You should probably set a HotelFilter before
	 * running this, but it'll create one on the fly if you're being lazy.
	 * <p/>
	 * Populates mExpediaSortedProperties and mFilteredProperties as a side effect.
	 */
	public List<Property> getFilteredAndSortedProperties(DeprecatedHotelSearchParams searchParams) {
		Log.v("getFilteredAndSortedProperties() called...");

		// If we have no properties set, return null
		if (mProperties == null) {
			Log.v("getFilteredAndSortedProperties() - properties is null, returning null");
			return null;
		}

		// Create a (shallow) list of the filtered results and sort it.
		ArrayList<Property> sortedProperties = new ArrayList<Property>(getFilteredProperties(searchParams));
		ArrayList<Property> sponsoredProperties = getSponsoredProperties(sortedProperties);

		Sort sort = mFilter.getSort();
		switch (sort) {
		case PRICE:
			Log.v("Sorting based on price");
			Collections.sort(sortedProperties, Property.PRICE_COMPARATOR);
			break;
		case DEALS:
			Log.v("Sorting based on deals");
			if (searchParams.getSearchType() == SearchType.MY_LOCATION) {
				Collections.sort(sortedProperties, Property.DISTANCE_COMPARATOR);
			}

			ArrayList<Property> deals = new ArrayList<Property>();
			ArrayList<Property> others = new ArrayList<Property>();
			for (Property p : sortedProperties) {
				Rate rate = p.getLowestRate();
				if (rate != null && rate.isSaleTenPercentOrBetter()) {
					deals.add(p);
				}
				else {
					others.add(p);
				}
			}

			sortedProperties.clear();
			sortedProperties.addAll(deals);
			sortedProperties.addAll(others);
			break;
		case RATING:
			Log.v("Sorting based on rating");
			Collections.sort(sortedProperties, Property.RATING_COMPARATOR);
			break;
		case DISTANCE:
			Log.v("Sorting based on distance");
			Collections.sort(sortedProperties, Property.DISTANCE_COMPARATOR);
			break;
		case RECOMMENDED:
		default:
			// The default sort is RECOMMENDED, which requires no special sorting
			Log.v("Sorting based on popularity (default)");
			break;
		}

		//Put the sponsored listings into their correct position
		reorderSponsoredProperties(sortedProperties, sponsoredProperties);

		return sortedProperties;
	}

	private ArrayList<Property> getSponsoredProperties(ArrayList<Property> properties) {
		ArrayList<Property> sponsored = new ArrayList<Property>();
		ListIterator<Property> it = properties.listIterator();
		while (it.hasNext()) {
			Property prop = it.next();
			if (prop.isSponsored()) {
				it.remove();
				sponsored.add(prop);
			}
		}

		return sponsored;
	}

	private void reorderSponsoredProperties(ArrayList<Property> properties, ArrayList<Property> sponsored) {
		for (int i = 0; i < sponsored.size() && i < sponsoredIndexes.length; i++) {
			if (sponsoredIndexes[i] <= properties.size()) {
				properties.add(sponsoredIndexes[i], sponsored.get(i));
			}
		}
	}

	public List<Property> getFilteredAndSortedProperties(Sort sort, int count, DeprecatedHotelSearchParams searchParams) {
		mFilter = new HotelFilter();
		mFilter.setSort(sort);

		List<Property> sorted = getFilteredAndSortedProperties(searchParams);

		if (mFilteredProperties.size() > count) {
			return sorted.subList(0, count);
		}

		return sorted;
	}

	public int getFilteredPropertiesCount(DeprecatedHotelSearchParams searchParams) {
		getFilteredProperties(searchParams);
		return mFilteredProperties == null ? 0 : mFilteredProperties.size();
	}

	//////////////////////////////////////////////////////////////////////////////////
	// OnFilterChangedListener

	public void onFilterChanged() {
		clearCache();
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Grunt work for sorting/filtering

	private void preSortByLocation(double latitude, double longitude) {
		// Here we do banding, as described by #6261
		List<Property> propsShort = new ArrayList<Property>();
		List<Property> propsMedium = new ArrayList<Property>();
		List<Property> propsFar = new ArrayList<Property>();
		List<Property> propsRest = new ArrayList<Property>();

		DistanceUnit distanceUnit = mFilter.getDistanceUnit();
		double smallRadius = HotelFilter.SearchRadius.SMALL.getRadius(distanceUnit);
		double mediumRadius = HotelFilter.SearchRadius.MEDIUM.getRadius(distanceUnit);
		double farRadius = HotelFilter.SearchRadius.LARGE.getRadius(distanceUnit);

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
					distance = MapUtils.getDistance(
						latitude, longitude,
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

		mPresortedProperties = new ArrayList<Property>(mProperties.size());
		mPresortedProperties.addAll(propsShort);
		mPresortedProperties.addAll(propsMedium);
		mPresortedProperties.addAll(propsFar);
		mPresortedProperties.addAll(propsRest);
	}

	/**
	 * Filters the list of properties based on the passed filter.
	 * This populates mFilter, mPresortedProperties, mPriceTiers,
	 * mFilteredProperties, and mFilteredPropertiesIgnoringNeighborhood
	 */
	private void performFiltering(DeprecatedHotelSearchParams searchParams) {
		// Check that we have a filter, if not create a new one
		if (mFilter == null) {
			Log.v("performFiltering() - no filter set, setting default one");
			HotelFilter filter = new HotelFilter();
			if (searchParams.getSearchType().shouldShowDistance()) {
				filter.setSort(Sort.DISTANCE);
			}
			setFilter(filter);
		}

		// Check if we've done the custom RECOMMENDED sort for MY_LOCATION
		if (mPresortedProperties == null) {
			Log.v("performFiltering() - No Expedia sorted items, sorting now...");
			Log.v("performFiltering() - Current search type: " + searchParams.getSearchType());

			if (searchParams.getSearchType() == SearchType.MY_LOCATION) {
				Log.v("My location search, doing special sorting...");
				preSortByLocation(searchParams.getSearchLatitude(), searchParams.getSearchLongitude());
			}
			else {
				Log.v("NOT a my location search, skipping special sorting...");
				mPresortedProperties = mProperties;
			}
		}

		// Check if we've clustered properties yet
		if (mPriceTiers.size() == 0) {
			Log.v("performFiltering() - No price tiers, clustering now...");
			clusterProperties();
		}

		if (mFilteredProperties != null) {
			return;
		}

		Log.v("performFiltering() - No cached properties, filtering now...");

		mFilteredProperties = new ArrayList<Property>();
		mFilteredPropertiesIgnoringNeighborhood = new ArrayList<Property>();

		// Get all the current HotelFilter options
		SearchRadius searchRadius = mFilter.getSearchRadius();
		DistanceUnit distanceUnit = mFilter.getDistanceUnit();
		PriceRange priceRange = mFilter.getPriceRange();
		double minStarRating = mFilter.getMinimumStarRating();
		String hotelName = mFilter.getHotelName();
		Set<Integer> neighborhoods = mFilter.getNeighborhoods();

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
			namePattern = Pattern.compile(".*" + hotelName + ".*", Pattern.CASE_INSENSITIVE);
		}

		for (Property property : mPresortedProperties) {
			// HotelFilter search radius
			if (searchDistance != null) {
				if (property.getDistanceFromUser() != null) {
					Distance distanceFromUser = roundDistanceToTenths(property.getDistanceFromUser());
					if (distanceFromUser.compareTo(searchDistance) > 0) {
						continue;
					}
				}
			}

			// HotelFilter price range
			if (priceTier != null && !priceTier.containsProperty(property)) {
				continue;
			}

			// HotelFilter star rating
			if (minStarRating > property.getHotelRating()) {
				continue;
			}

			// HotelFilter VIP Access
			if (mFilter.isVipAccessOnly() && !property.isVipAccess()) {
				continue;
			}

			if (namePattern != null && !namePattern.matcher(property.getName()).find()) {
				continue;
			}

			mFilteredPropertiesIgnoringNeighborhood.add(property);

			if (neighborhoods != null && !neighborhoods.contains(property.getLocation().getLocationId())) {
				continue;
			}

			// Property passed the tests, add it to results
			mFilteredProperties.add(property);
		}
	}

	private Distance roundDistanceToTenths(Distance distance) {
		Distance roundedDistance = new Distance(distance.getDistance(), distance.getUnit());
		roundedDistance.setDistance(Math.rint(roundedDistance.getDistance() * 10.0d) / 10.0d);
		return roundedDistance;
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
		List<Property> propertiesWithPrices = new ArrayList<Property>();
		for (Property property : mProperties) {
			if (property.getLowestRate() != null) {
				propertiesWithPrices.add(property);
			}
		}

		len = propertiesWithPrices.size();

		// Don't bother clustering if we have no properties with prices
		if (len == 0) {
			return;
		}

		double[] prices = new double[len];
		for (int index = 0; index < len; index++) {
			prices[index] = propertiesWithPrices.get(index).getLowestRate().getDisplayPrice().getAmount().doubleValue();
		}

		// Cluster
		double[] medoids = cluster(prices);

		// Separate out based on medoid values
		@SuppressWarnings("unchecked")
		ArrayList<Property>[] tiers = new ArrayList[] {
			new ArrayList<Property>(),
			new ArrayList<Property>(),
			new ArrayList<Property>(),
		};
		double closest;
		double tmp;
		double amount;
		int closestIndex;
		for (Property property : propertiesWithPrices) {
			closestIndex = 0;
			amount = property.getLowestRate().getDisplayPrice().getAmount().doubleValue();
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
			return new double[] {
				prices[0],
				prices[0],
				prices[1],
			};
		}
		else if (len == 1) {
			return new double[] {
				prices[0],
				prices[0],
				prices[0],
			};
		}

		// Pick three reasonable starting medoids
		int[] medoidIndexes = new int[] {
			len / 6,
			len / 2,
			(len * 5) / 6,
		};

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
				return new double[] {
					prices[medoidIndexes[0]],
					prices[medoidIndexes[1]],
					prices[medoidIndexes[2]],
				};
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
			JSONUtils.putJSONableList(obj, "properties", mProperties);
			JSONUtils.putJSONableList(obj, "locations", mLocations);
			obj.putOpt("hasSponsoredListing", hasSponsoredListing);
			obj.putOpt("pageViewBeaconPixelUrl", mBeaconUrl);

			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert HotelSearchResponse to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mProperties = JSONUtils.getJSONableList(obj, "properties", Property.class);
		mLocations = JSONUtils.getJSONableList(obj, "locations", Location.class);
		hasSponsoredListing = obj.optBoolean("hasSponsoredListing", false);
		mBeaconUrl = obj.optString("pageViewBeaconPixelUrl", null);

		return true;
	}
}
