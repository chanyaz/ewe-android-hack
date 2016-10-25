package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.utils.FeatureToggleUtil;

public class HotelFavoriteHelper {

	private static final String firstTimeKey = "Hotel_Favorites_First_Time";
	private static final String listKey = "Hotel_Favorites_List";
	private static final HashSet<String> localFavorites = new HashSet<>();

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

	public static boolean showHotelFavoriteTest(Context context, boolean isOkayToShowFavorites) {
		return FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppHotelFavoriteTest,
			R.string.preference_enable_hotel_favorite) && isOkayToShowFavorites;
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
		localFavorites.add(hotelId);
		saveHotelFavorites(prefs, favoritesList);
		// mark first time flag false
		Editor editor = prefs.edit();
		editor.putBoolean(firstTimeKey, false);
		editor.apply();
	}

	private static void removeHotelFromFavorites(Context context, String hotelId) {
		Set<String> favoritesList = getHotelFavorites(context);
		favoritesList.remove(hotelId);
		localFavorites.remove(hotelId);
		saveHotelFavorites(getDefaultSharedPreferences(context), favoritesList);
	}

	private static SharedPreferences getDefaultSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static HashSet<String> getLocalFavorites() {
		return localFavorites;
	}

	public static void setLocalFavorites(ArrayList<Hotel> list, Context context) {
		Set<String> currentFavorites = getHotelFavorites(context);
		localFavorites.clear();
		for (Hotel hotel: list) {
			if (currentFavorites.contains(hotel.hotelId)) {
				localFavorites.add(hotel.hotelId);
			}
		}
	}
}
