package com.expedia.bookings.appwidget;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.data.Session;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.IoUtils;

/**
 * This class is responsible for holding 
 * properties that are classified as deals for the widget.
 * It also has functions to act on the data to save it, restore it,
 * and determine relevant deals from a list of properties passed in.
 *
 */
public class WidgetDeals implements JSONable {

	private Context mContext;
	private List<Property> mDeals;
	private Session mSession;
	private SearchParams mSearchParams;

	private static WidgetDeals singleton;

	public static final String WIDGET_DEALS_FILE = "widgetDeals.dat";
	private static final int MAX_DEALS = 5;
	public static final long WIDGET_DEALS_EXPIRATION = 1000 * 60 * 60; // 1 hour

	public static final int WIDGET_DEALS_RESTORED = 0;
	public static final int NO_WIDGET_FILE_EXISTS = -1;
	public static final int NO_DEALS_EXIST = -2;

	public static WidgetDeals getInstance(Context context) {
		if (singleton == null) {
			singleton = new WidgetDeals(context);
		}
		return singleton;
	}

	private WidgetDeals(Context context) {
		mContext = context;
	}

	public List<Property> getDeals() {
		return mDeals;
	}

	public Session getSession() {
		return mSession;
	}

	public SearchParams getSearchParams() {
		return mSearchParams;
	}

	public void setSearchParams(SearchParams searchParams) {
		mSearchParams = new SearchParams();
		mSearchParams.fromJson(searchParams.toJson());
	}

	public void determineRelevantProperties(SearchResponse response) {
		long start = System.currentTimeMillis();
		List<Property> relevantProperties = new ArrayList<Property>();

		if (response == null || response.hasErrors()) {
			mDeals = null;
			return;
		}

		mSession = response.getSession();

		// first populate the list with hotels that have rooms on sale
		// from the list of user-filtered properties
		Property[] filteredProperties = response.getFilteredAndSortedProperties();
		Arrays.sort(filteredProperties, Property.PRICE_COMPARATOR);

		trackDeals(relevantProperties, filteredProperties);

		// if that isn't enough, look through the global list of properties
		// to fill in the slots
		if (relevantProperties.size() < MAX_DEALS) {
			Property[] properties = response.getProperties().toArray(new Property[1]);
			Arrays.sort(properties, Property.PRICE_COMPARATOR);

			trackDeals(relevantProperties, properties);

			trackHighlyRatedHotels(relevantProperties, properties);

			fillMaxSlotsIfAnyLeft(relevantProperties, properties);
		}

		mDeals = relevantProperties;
		Log.d("Deals determined: " + relevantProperties.size() + ". Time taken : "
				+ (System.currentTimeMillis() - start) + " ms");
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		try {
			if (mDeals != null) {
				JSONUtils.putJSONableList(obj, "deals", mDeals);
				obj.put("session", mSession.toJson());
				obj.put("searchParams", mSearchParams.toJson());
			}
		}
		catch (JSONException e) {
			Log.w("Could not write deals JSON", e);
		}
		return obj;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean fromJson(JSONObject obj) {

		mDeals = (List<Property>) JSONUtils.getJSONableList(obj, "deals", Property.class);
		if (mDeals == null) {
			return false;
		}

		mSession = new Session();
		mSession.fromJson(obj.optJSONObject("session"));
		mSearchParams = new SearchParams(obj.optJSONObject("searchParams"));
		return true;
	}

	public boolean persistToDisk() {
		File widgetDealsFile = mContext.getFileStreamPath(WIDGET_DEALS_FILE);

		boolean results = false;

		if (!widgetDealsFile.exists()) {
			try {
				long start = System.currentTimeMillis();
				IoUtils.writeStringToFile(WIDGET_DEALS_FILE, toJson().toString(0), mContext);
				Log.i("Saved widget deals, time taken: " + (System.currentTimeMillis() - start) + " ms");
				results = true;
			}
			catch (IOException e) {
				Log.w("Couldn't save widget deals.", e);
			}
			catch (JSONException e) {
				Log.w("Couldn't save widget deals.", e);
			}
			catch (OutOfMemoryError e) {
				Log.w("Ran out of memory while trying to save widget deals file", e);
			}
		}
		return results;
	}

	public int restoreFromDisk() {
		int results = WIDGET_DEALS_RESTORED;
		File widgetDealsFile = mContext.getFileStreamPath(WIDGET_DEALS_FILE);
		if (widgetDealsFile.exists()) {
			if (widgetDealsFile.lastModified() < (System.currentTimeMillis() - WIDGET_DEALS_EXPIRATION)) {
				Log.d("There are widget deals but they are expired.");
				widgetDealsFile.delete();
				results = NO_DEALS_EXIST;
			}
			else {
				try {
					long start = System.currentTimeMillis();
					JSONObject obj = new JSONObject(IoUtils.readStringFromFile(WIDGET_DEALS_FILE, mContext));
					if (!fromJson(obj)) {
						results = NO_DEALS_EXIST;
					}
					Log.i("Loaded widget deals, time taken: " + (System.currentTimeMillis() - start) + " ms");
				}
				catch (IOException e) {
					Log.w("Couldn't load widget deals.", e);
				}
				catch (JSONException e) {
					Log.w("Couldn't load widget deals.", e);
				}
			}
		}
		else {
			results = NO_WIDGET_FILE_EXISTS;
		}
		return results;
	}

	public boolean deleteFromDisk() {
		boolean results = false;
		File widgetDealsFile = mContext.getFileStreamPath(WIDGET_DEALS_FILE);
		if (widgetDealsFile.exists()) {
			results = widgetDealsFile.delete();
		}
		return results;
	}

	public void clearOutData() {
		mDeals = null;
		mSearchParams = null;
	}

	/**
	 * This method looks through the list of properties 
	 * to find those that are neither highly rated
	 * nor are on sale. The goal here is to find hotels to
	 * fill in the remaining slots in the widget since there
	 * aren't enough hotels that are highly rated or on sale.
	 */
	private void fillMaxSlotsIfAnyLeft(List<Property> relevantProperties, Property[] properties) {
		for (Property property : properties) {
			if (relevantProperties.size() == MAX_DEALS) {
				break;
			}

			if (!relevantProperties.contains(property) && property.getLowestRate().getSavingsPercent() == 0
					&& !property.isHighlyRated()) {
				relevantProperties.add(property);
			}
		}
	}

	/**
	 * This method looks through the list of properties to find those
	 * that are highly rated and not on sale. The goal here is to 
	 * fill in the slots with the second-optimal solution of highly 
	 * rated hotels if there aren't enough deals defined as hotels
	 * on sale.
	 */
	private void trackHighlyRatedHotels(List<Property> relevantProperties, Property[] properties) {
		// then populate with highly rated rooms if there aren't enough
		// hotels with rooms on sale
		for (Property property : properties) {
			if (relevantProperties.size() == MAX_DEALS) {
				break;
			}

			if (!relevantProperties.contains(property) && property.isHighlyRated()
					&& (property.getLowestRate().getSavingsPercent() == 0)) {
				relevantProperties.add(property);
			}
		}
	}

	/**
	 * This method looks through the list of properties to find those that
	 * are on sale. It also calculates the hotel with the maximum savings
	 * from the list of chosen hotels (the maximum from the first 5 hotels on sale, 
	 * sorted by price).
	 */
	private void trackDeals(List<Property> relevantProperties, Property[] properties) {
		for (Property property : properties) {

			if (relevantProperties.size() == MAX_DEALS) {
				break;
			}

			if (!relevantProperties.contains(property)) {
				if (property.getLowestRate().getSavingsPercent() > 0) {
					relevantProperties.add(property);
				}
			}
		}
	}

}
