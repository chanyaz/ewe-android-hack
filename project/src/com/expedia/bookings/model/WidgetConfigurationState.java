package com.expedia.bookings.model;

import java.util.ArrayList;

import junit.framework.Assert;
import android.content.Context;

import com.activeandroid.ActiveRecordBase;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "WidgetConfigurations")
public class WidgetConfigurationState extends ActiveRecordBase<WidgetConfigurationState> {

	public static final String AppWidgetId = "AppWidgetId";

	public WidgetConfigurationState(Context context) {
		super(context);
	}

	@Column(name = "AppWidgetId")
	private int appWidgetId;

	@Column(name = "ExactSearchLocation")
	private String mExactSearchLocation;

	@Column(name = "ExactSearchLocationLatitude")
	private double mExactSearchLocationLat;

	@Column(name = "ExactSearchLocationLongitude")
	private double mExactSearchLocationLon;

	public int getAppWidgetId() {
		return appWidgetId;
	}

	public void setAppWidgetId(int appWidgetId) {
		this.appWidgetId = appWidgetId;
	}

	public String getExactSearchLocation() {
		return mExactSearchLocation;
	}

	public void setExactSearchLocation(String mExactSearchLocation) {
		this.mExactSearchLocation = mExactSearchLocation;
	}

	public double getExactSearchLocationLat() {
		return mExactSearchLocationLat;
	}

	public void setExactSearchLocationLat(double mExactSearchLocationLat) {
		this.mExactSearchLocationLat = mExactSearchLocationLat;
	}

	public double getExactSearchLocationLon() {
		return mExactSearchLocationLon;
	}

	public void setExactSearchLocationLon(double mExactSearchLocationLon) {
		this.mExactSearchLocationLon = mExactSearchLocationLon;
	}

	public static WidgetConfigurationState getWidgetConfiguration(Context context, int appWidgetId) {
		ArrayList<Object> results = WidgetConfigurationState.query(context, WidgetConfigurationState.class, null,
				AppWidgetId + "=" + Integer.toString(appWidgetId));
		if (results.isEmpty()) {
			return null;
		}

		if (results.size() == 1) {
			return (WidgetConfigurationState) results.get(0);
		}

		Assert.assertTrue("There should not be more than 1 widget configuration state for the specified id!",
				results.size() == 1);
		return null;
	}

	public static ArrayList<Object> getAll(Context context) {
		return WidgetConfigurationState.query(context, WidgetConfigurationState.class);
	}

	public static void deleteWidgetConfigState(Context context, int appWidgetId) {
		WidgetConfigurationState.delete(context, WidgetConfigurationState.class,
				AppWidgetId + "=" + Integer.toString(appWidgetId));
	}

	/**
	 * This class ensures that there are no widget configuration states saved in the database
	 * that are not existing widgets, as determined by the ids returned by the AppWidgetManager
	 * 
	 * Any widgetConfigState that exists that is not in the list of existingAppWidgetIds is
	 * deleted.
	 */
	public static void reconcileWidgetConfigurationStates(Context context, int[] existingAppWidgetIds) {
		String appWidgetIds = " (";
		for (int i = 0; i < (existingAppWidgetIds.length - 1); i++) {
			appWidgetIds += existingAppWidgetIds[i] + ", ";
		}
		if (existingAppWidgetIds.length > 0) {
			appWidgetIds += existingAppWidgetIds[existingAppWidgetIds.length - 1] + " )";
		}

		ArrayList<Object> orphanedConfigStates = WidgetConfigurationState.query(context,
				WidgetConfigurationState.class, null, AppWidgetId + " not in " + appWidgetIds);

		// delete all the widget configurations that are orphaned
		for (Object config : orphanedConfigStates) {
			WidgetConfigurationState cs = (WidgetConfigurationState) config;
			WidgetConfigurationState.deleteWidgetConfigState(context, cs.getAppWidgetId());
		}
	}
}
