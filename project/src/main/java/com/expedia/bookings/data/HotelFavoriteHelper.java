package com.expedia.bookings.data;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.tracking.HotelTracking;
import com.expedia.bookings.utils.FeatureToggleUtil;
import com.expedia.util.ToggleFeatureConfiguration;

public class HotelFavoriteHelper {

	private static final String firstTimeKey = "Hotel_Favorites_First_Time";
	private static final String listKey = "Hotel_Favorites_List";

	public static Boolean isFirstTimeFavoriting(Context context) {
		return getDefaultSharedPreferences(context).getBoolean(firstTimeKey, true);
	}

	public static Set<String> getHotelFavorites(Context context) {
		Set<String> favoritesList = getDefaultSharedPreferences(context).getStringSet(listKey, new HashSet<String>());
		return favoritesList;
	}

	public static boolean isHotelFavorite(Context context, String hotelId) {
		Set<String> favs = getHotelFavorites(context);
		return favs.contains(hotelId);
	}

	public static void toggleHotelFavoriteState(Context context, String hotelId) {
		if (isHotelFavorite(context, hotelId)) {
			removeHotelFromFavorites(context, hotelId);
		}
		else {
			saveHotelToFavorites(context, hotelId);
		}
	}

	public static void trackToggleHotelFavoriteState(Context context, String hotelId, int parent) {
		new HotelTracking().trackHotelV2FavoriteClick(hotelId, parent, isHotelFavorite(context, hotelId));
	}


	public static Boolean showHotelFavoriteTest(Context context) {
		return FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppHotelFavoriteTest,
			R.string.preference_enable_hotel_favorite, ToggleFeatureConfiguration.HOTEL_FAVORITE_FEATURE);
	}

	private static void saveHotelFavorites(SharedPreferences prefs, Set<String> set) {
		HashSet<String> copy = new HashSet(set);
		prefs.edit().remove(listKey).apply();
		prefs.edit().putStringSet(listKey, copy).apply();
	}

	private static void saveHotelToFavorites(Context context, String hotelId) {
		SharedPreferences prefs = getDefaultSharedPreferences(context);
		Set<String> favoritesList = getHotelFavorites(context);
		favoritesList.add(hotelId);
		saveHotelFavorites(prefs, favoritesList);
		// mark first time flag false
		Editor editor = prefs.edit();
		editor.putBoolean(firstTimeKey, false);
		editor.apply();
	}

	private static void removeHotelFromFavorites(Context context, String hotelId) {
		Set<String> favoritesList = getHotelFavorites(context);
		favoritesList.remove(hotelId);
		saveHotelFavorites(getDefaultSharedPreferences(context), favoritesList);
	}

	private static SharedPreferences getDefaultSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

}
