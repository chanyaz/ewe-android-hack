package com.expedia.bookings.model;

import java.util.List;

import android.content.Context;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

@Table(name = "WidgetConfigurations")
public class WidgetConfigurationState extends Model {

	public static final String AppWidgetId = "AppWidgetId";

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

	public void setAppWidgetId(int id) {
		this.appWidgetId = id;
	}

	public String getExactSearchLocation() {
		return mExactSearchLocation;
	}

	public void setExactSearchLocation(String exactSearchLocation) {
		this.mExactSearchLocation = exactSearchLocation;
	}

	public double getExactSearchLocationLat() {
		return mExactSearchLocationLat;
	}

	public void setExactSearchLocationLat(double exactSearchLocationLat) {
		this.mExactSearchLocationLat = exactSearchLocationLat;
	}

	public double getExactSearchLocationLon() {
		return mExactSearchLocationLon;
	}

	public void setExactSearchLocationLon(double exactSearchLocationLon) {
		this.mExactSearchLocationLon = exactSearchLocationLon;
	}

	public static WidgetConfigurationState getWidgetConfiguration(Context context, int appWidgetId) {
		List<WidgetConfigurationState> results = new Select().from(WidgetConfigurationState.class)
				.where(AppWidgetId + "=?", appWidgetId).execute();

		if (results.isEmpty()) {
			return null;
		}

		if (results.size() == 1) {
			return (WidgetConfigurationState) results.get(0);
		}

		if (results.size() > 1) {
			throw new RuntimeException("There should not be more than 1 widget configuration state for the specified id!");
		}

		return null;
	}

	public static List<WidgetConfigurationState> getAll() {
		return new Select().from(WidgetConfigurationState.class).execute();
	}

	public static void deleteWidgetConfigState(Context context, int appWidgetId) {
		new Delete()
				.from(WidgetConfigurationState.class)
				.where(AppWidgetId + "=" + Integer.toString(appWidgetId))
				.execute();
	}

	/**
	 * This class ensures that there are no widget configuration states saved in the database
	 * that are not existing widgets, as determined by the ids returned by the AppWidgetManager
	 *
	 * Any widgetConfigState that exists that is not in the list of existingAppWidgetIds is
	 * deleted.
	 */
	public static void reconcileWidgetConfigurationStates(Context context, int[] existingAppWidgetIds) {
		StringBuilder ids = new StringBuilder("( "); // Keep the trailing space here
		for (int id : existingAppWidgetIds) {
			ids.append(id);
			ids.append(",");
		}
		ids.replace(ids.length() - 1, ids.length(), ")");

		List<WidgetConfigurationState> orphanedConfigStates = new Select().from(WidgetConfigurationState.class)
				.where(AppWidgetId + " not in " + ids).execute();

		// delete all the widget configurations that are orphaned
		for (Object config : orphanedConfigStates) {
			WidgetConfigurationState cs = (WidgetConfigurationState) config;
			WidgetConfigurationState.deleteWidgetConfigState(context, cs.getAppWidgetId());
		}
	}
}
