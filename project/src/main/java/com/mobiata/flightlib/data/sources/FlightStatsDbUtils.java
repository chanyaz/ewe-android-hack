package com.mobiata.flightlib.data.sources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTimeZone;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;
import com.mobiata.flightlib.data.Airline;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.AirportMap;

public class FlightStatsDbUtils {

	/**
	 * This is a path constructed based on the package of the app. This should
	 * be set before you try to manipulate the db; it'll be set automatically
	 * when you attempt to create the database.
	 */
	private static String sDbPath = null;

	private static final String DB_NAME = "FS.db";
	private static final String DB_ASSET_NAME = "FlightStatsDb/FS.db";

	/**
	 * The file that contains the version # of the currently installed static
	 * database
	 */
	private static final String DB_VERSION_FILE = "staticdb.ver";

	private static final String[] AIRLINE_COLS = new String[] { "name", "code", "url", "phone" };

	private static final int AIRLINE_NAME_INDEX = 0;
	private static final int AIRLINE_CODE_INDEX = 1;
	private static final int AIRLINE_PHONE_INDEX = 3;

	private static final int AIRPORT_CODE_INDEX = 0;
	private static final int AIRPORT_NAME_INDEX = 1;
	private static final int AIRPORT_CITY_INDEX = 2;
	private static final int AIRPORT_STATE_CODE_INDEX = 3;
	private static final int AIRPORT_COUNTRY_ID_INDEX = 4;
	private static final int AIRPORT_LATITUDE_INDEX = 5;
	private static final int AIRPORT_LONGITUDE_INDEX = 6;
	private static final int AIRPORT_TIMEZONE_ID_INDEX = 7;
	private static final int AIRPORT_CLASSIFICATION_INDEX = 8;
	private static final int AIRPORT_HAS_INTERNATIONAL_TERMINAL_I_INDEX = 9;

	// Cached data for fast retrieval of airline/airport information
	private static final ConcurrentHashMap<String, Airline> mAirlines = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, Airport> mAirports = new ConcurrentHashMap<>();

	// Debug ways to avoid having to update FS.db all the time
	private static long sUpgradeCutoff = 0;

	public static void setUpgradeCutoff(long cutoff) {
		sUpgradeCutoff = cutoff;
	}

	/**
	 * This creates the FS database if it does not already exist.
	 *
	 * @param context
	 * @param isRelease
	 * @throws IOException
	 */
	public static void createDatabaseIfNotExists(Context context, boolean isRelease) throws IOException {
		Log.d("Checking if static database exists...");

		sDbPath = context.getFilesDir().getAbsolutePath() + "/databases/";

		boolean createDb = false;

		File dbDir = new File(sDbPath);
		File dbFile = new File(sDbPath + DB_NAME);
		if (!dbDir.exists()) {
			Log.i("Static database does not exist, creating it.");
			dbDir.mkdir();
			createDb = true;
		}
		else if (!dbFile.exists()) {
			Log.i("Static database does not exist, creating it.");
			createDb = true;
		}
		else {
			// Check that we have the latest version of the db
			boolean doUpgrade = false;
			File versionFile = context.getFileStreamPath(DB_VERSION_FILE);
			if (!versionFile.exists() || !versionFile.canRead()) {
				doUpgrade = true;
			}
			else {
				try {
					String versionStr = IoUtils.readStringFromFile(DB_VERSION_FILE, context);
					long oldMillis = Long.parseLong(versionStr);
					long addition = isRelease ? 0 : sUpgradeCutoff;
					if (oldMillis + addition < AndroidUtils.getAppBuildDate(context).getTimeInMillis()) {
						doUpgrade = true;
					}
				}
				catch (IOException e) {
					Log.w("Tried to read static database version but failed..", e);
					doUpgrade = true;
				}
			}

			// If we are doing an upgrade, basically we just delete the db then
			// flip the switch to create a new one
			if (doUpgrade) {
				Log.i("Static database is out of date, creating new version.");
				dbFile.delete();
				createDb = true;
			}
		}

		if (createDb) {
			InputStream myInput = null;
			OutputStream myOutput = null;
			try {
				// Open your local db as the input stream
				myInput = context.getAssets().open(DB_ASSET_NAME);

				// Open the empty db as the output stream
				myOutput = new FileOutputStream(dbFile);

				// transfer bytes from the inputfile to the outputfile
				byte[] buffer = new byte[1024];
				int length;
				while ((length = myInput.read(buffer)) > 0) {
					myOutput.write(buffer, 0, length);
				}
			}
			catch (Exception ex) {
				Log.e("Error handling the streams.");
			}
			finally {
				// Close the streams
				if (myOutput != null) {
					myOutput.flush();
					myOutput.close();
				}
				if (myInput != null) {
					myInput.close();
				}
			}

			// Write out the current version # of the db
			Calendar appBuildTime = AndroidUtils.getAppBuildDate(context);
			String versionFileContents;
			if (appBuildTime == null) {
				versionFileContents = "0";
			}
			else {
				versionFileContents = appBuildTime.getTimeInMillis() + "";
			}
			IoUtils.writeStringToFile(DB_VERSION_FILE, versionFileContents, context);
		}
		else {
			Log.d("Static database already exists and is up to date, not doing anything.");
		}
	}

	private static SQLiteDatabase getStaticDb() {
		try {
			return SQLiteDatabase.openDatabase(sDbPath + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
		}
		catch (SQLiteException sqle) {
			// make one more attempt in read-only mode
			return SQLiteDatabase.openDatabase(sDbPath + DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
		}
	}

	public static Airline getAirline(String airlineCode) {
		if (airlineCode == null) {
			return null;
		}

		Airline airline = mAirlines.get(airlineCode);
		if (airline == null) {
			airline = new Airline();
			airline.mAirlineCode = airlineCode;
			fillAirlineDataFromDb(airline);
			mAirlines.put(airlineCode, airline);
		}
		return airline;
	}

	public static Airport getAirport(String airportCode) {
		if (airportCode == null) {
			return null;
		}

		Airport airport = mAirports.get(airportCode);
		if (airport == null) {
			airport = new Airport();
			airport.mAirportCode = airportCode;
			fillAirportDataFromDb(airport);
			mAirports.put(airportCode, airport);
		}
		return airport;
	}

	private static void fillAirlineDataFromDb(Airline airline) {
		SQLiteDatabase db = getStaticDb();
		Cursor c = db.query("airlines", AIRLINE_COLS, "code = ?", new String[] { airline.mAirlineCode }, null, null,
				null);
		if (c.getCount() == 0) {
			Log.w("Tried to retrieve an airline code for which we had no data: " + airline.mAirlineCode);
		}
		else {
			c.moveToFirst();
			fillAirlineFromCursor(airline, c);
		}
		c.close();
		db.close();
	}

	private static void fillAirlineFromCursor(Airline airline, Cursor c) {
		airline.mAirlineName = c.getString(AIRLINE_NAME_INDEX);
		airline.mAirlineCode = c.getString(AIRLINE_CODE_INDEX);
		airline.mAirlinePhone = c.getString(AIRLINE_PHONE_INDEX);
	}

	private static void fillAirportDataFromDb(Airport airport) {
		// Fill in airport data
		SQLiteDatabase db = getStaticDb();

		String queryString =
				  "SELECT airports.code, airports.name, airports.city, airports.stateCode, countries.countryCode, "
				+        "airports.latitude, airports.longitude, timezones.timeZoneName, airports.classification, "
				+        "airports.hasInternationalTerminalI "
				+ "FROM airports "
				+     "INNER JOIN countries ON countries._id=airports.countryId "
				+     "INNER JOIN timezones ON timezones._id=airports.timeZoneId "
				+ "WHERE airports.code = ?";

		Cursor c = db.rawQuery(queryString, new String[] { airport.mAirportCode });

		if (c.getCount() == 0) {
			Log.w("Tried to retrieve an airport code for which we had no data: " + airport.mAirportCode);
		}
		else {
			c.moveToFirst();
			fillAirportFromCursor(airport, c);
			fillAirportMapsDataFromDb(db, airport);
		}
		c.close();
		db.close();
	}

	private static void fillAirportFromCursor(Airport airport, Cursor c) {
		airport.mAirportCode = c.getString(AIRPORT_CODE_INDEX);
		airport.mName = c.getString(AIRPORT_NAME_INDEX);
		airport.mCity = c.getString(AIRPORT_CITY_INDEX);
		airport.mStateCode = c.getString(AIRPORT_STATE_CODE_INDEX);
		airport.mCountryCode = c.getString(AIRPORT_COUNTRY_ID_INDEX);
		airport.mLat = c.getFloat(AIRPORT_LATITUDE_INDEX);
		airport.mLon = c.getFloat(AIRPORT_LONGITUDE_INDEX);
		String timeZoneId = c.getString(AIRPORT_TIMEZONE_ID_INDEX);

		if (timeZoneId != null && timeZoneId.length() > 0) {
			airport.mTimeZone = DateTimeZone.forID(timeZoneId);
		}
		airport.mClassification = c.getInt(AIRPORT_CLASSIFICATION_INDEX);
		airport.mHasInternationalTerminalI = c.getInt(AIRPORT_HAS_INTERNATIONAL_TERMINAL_I_INDEX) == 1;

	}

	private static void fillAirportMapsDataFromDb(SQLiteDatabase db, Airport airport) {
		String queryString = "SELECT type, name, url "
				+ "FROM airport_maps "
				+ "WHERE airport = ?";

		Cursor c = db.rawQuery(queryString, new String[] { airport.mAirportCode });

		if (c.getCount() != 0) {
			if (airport.mAirportMaps == null) {
				airport.mAirportMaps = new ArrayList<>();
			}
			else {
				airport.mAirportMaps.clear();
			}

			c.moveToFirst();
			int i = 0;
			do {
				AirportMap map = new AirportMap();
				map.mId = i++;
				map.mType = c.getInt(0);
				map.mName = c.getString(1);
				map.mUrl = c.getString(2);
				airport.mAirportMaps.add(map);
			}
			while (c.moveToNext());

			Collections.sort(airport.mAirportMaps);
		}
		c.close();
	}

}
