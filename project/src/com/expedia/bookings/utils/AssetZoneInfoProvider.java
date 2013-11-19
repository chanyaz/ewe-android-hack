package com.expedia.bookings.utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTimeZone;
import org.joda.time.tz.DateTimeZoneBuilder;
import org.joda.time.tz.Provider;

import android.content.Context;

/**
 * This is a version of Joda's ZoneInfoProvider that uses
 * Android assets for TZ data storage.
 *
 * NOTE: Please make sure this is up-to-date with the latest
 * Joda that you're using!
 *
 * It seems that ClassLoader.getResourceAsStream() causes the
 * amount of memory to balloon when accessing resources in a JAR.
 * As a result, we want to load TZ data from assets instead.
 *
 */
public class AssetZoneInfoProvider implements Provider {

	private static Context sAppContext;
	private static String sAssetPath;

	/** Maps ids to strings or SoftReferences to DateTimeZones. */
	private final Map<String, Object> iZoneInfoMap;

	public static void init(Context context, String assetPath) {
		sAppContext = context.getApplicationContext();
		sAssetPath = assetPath;

		// We assume that if you init it, you want to use it
		System.setProperty("org.joda.time.DateTimeZone.Provider",
				AssetZoneInfoProvider.class.getCanonicalName());
	}

	public AssetZoneInfoProvider() throws IOException {
		iZoneInfoMap = loadZoneInfoMap(openResource("ZoneInfoMap"));
	}

	/**
	 * Opens a resource from assets
	 */
	private InputStream openResource(String name) throws IOException {
		String path = sAssetPath.concat(name);
		return sAppContext.getAssets().open(path);
	}

	//////////////////////////////////////////////////////////////////////////
	// BELOW THIS LINE IS ALL ZONEINFOPROVIDER ORIGINAL CODE
	//////////////////////////////////////////////////////////////////////////

	//-----------------------------------------------------------------------
	/**
	 * If an error is thrown while loading zone data, uncaughtException is
	 * called to log the error and null is returned for this and all future
	 * requests.
	 * 
	 * @param id  the id to load
	 * @return the loaded zone
	 */
	public DateTimeZone getZone(String id) {
		if (id == null) {
			return null;
		}

		Object obj = iZoneInfoMap.get(id);
		if (obj == null) {
			return null;
		}

		if (id.equals(obj)) {
			// Load zone data for the first time.
			return loadZoneData(id);
		}

		if (obj instanceof SoftReference<?>) {
			@SuppressWarnings("unchecked")
			SoftReference<DateTimeZone> ref = (SoftReference<DateTimeZone>) obj;
			DateTimeZone tz = ref.get();
			if (tz != null) {
				return tz;
			}
			// Reference cleared; load data again.
			return loadZoneData(id);
		}

		// If this point is reached, mapping must link to another.
		return getZone((String) obj);
	}

	/**
	 * Gets a list of all the available zone ids.
	 * 
	 * @return the zone ids
	 */
	public Set<String> getAvailableIDs() {
		// Return a copy of the keys rather than an umodifiable collection.
		// This prevents ConcurrentModificationExceptions from being thrown by
		// some JVMs if zones are opened while this set is iterated over.
		return new TreeSet<String>(iZoneInfoMap.keySet());
	}

	/**
	 * Called if an exception is thrown from getZone while loading zone data.
	 * 
	 * @param ex  the exception
	 */
	protected void uncaughtException(Exception ex) {
		Thread t = Thread.currentThread();
		t.getThreadGroup().uncaughtException(t, ex);
	}

	/**
	 * Loads the time zone data for one id.
	 * 
	 * @param id  the id to load
	 * @return the zone
	 */
	private DateTimeZone loadZoneData(String id) {
		InputStream in = null;
		try {
			in = openResource(id);
			DateTimeZone tz = DateTimeZoneBuilder.readFrom(in, id);
			iZoneInfoMap.put(id, new SoftReference<DateTimeZone>(tz));
			return tz;
		}
		catch (IOException ex) {
			uncaughtException(ex);
			iZoneInfoMap.remove(id);
			return null;
		}
		finally {
			try {
				if (in != null) {
					in.close();
				}
			}
			catch (IOException ex) {
				// ignore
			}
		}
	}

	//-----------------------------------------------------------------------
	/**
	 * Loads the zone info map.
	 * 
	 * @param in  the input stream
	 * @return the map
	 */
	private static Map<String, Object> loadZoneInfoMap(InputStream in) throws IOException {
		Map<String, Object> map = new ConcurrentHashMap<String, Object>();
		DataInputStream din = new DataInputStream(in);
		try {
			readZoneInfoMap(din, map);
		}
		finally {
			try {
				din.close();
			}
			catch (IOException ex) {
				// ignore
			}
		}
		map.put("UTC", new SoftReference<DateTimeZone>(DateTimeZone.UTC));
		return map;
	}

	/**
	 * Reads the zone info map from file.
	 * 
	 * @param din  the input stream
	 * @param zimap  gets filled with string id to string id mappings
	 */
	private static void readZoneInfoMap(DataInputStream din, Map<String, Object> zimap) throws IOException {
		// Read the string pool.
		int size = din.readUnsignedShort();
		String[] pool = new String[size];
		for (int i = 0; i < size; i++) {
			pool[i] = din.readUTF().intern();
		}

		// Read the mappings.
		size = din.readUnsignedShort();
		for (int i = 0; i < size; i++) {
			try {
				zimap.put(pool[din.readUnsignedShort()], pool[din.readUnsignedShort()]);
			}
			catch (ArrayIndexOutOfBoundsException ex) {
				throw new IOException("Corrupt zone info map");
			}
		}
	}

}
