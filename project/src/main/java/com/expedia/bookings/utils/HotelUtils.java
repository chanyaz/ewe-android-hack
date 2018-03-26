package com.expedia.bookings.utils;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;

import com.expedia.bookings.R;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelRate;
import com.squareup.phrase.Phrase;

public class HotelUtils {

	private static boolean isDistanceUnitInMiles() {
		return Locale.getDefault().getCountry().toLowerCase(Locale.ENGLISH).equals("us");
	}

	public static String formatDistanceForNearby(Resources resources, Hotel offer) {
		boolean isMiles = isDistanceUnitInMiles();
		double distance = isMiles ? offer.proximityDistanceInMiles : offer.proximityDistanceInKiloMeters;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		nf.setMinimumFractionDigits(0);

		int unitStrId = isMiles ? R.string.unit_miles : R.string.unit_kilometers;
		return resources.getString(R.string.distance_template_short,
			nf.format(distance), resources.getString(unitStrId));
	}

	public static float getDiscountPercent(HotelRate rate) {
		if (rate == null) {
			return 0;
		}
		return Math.abs(rate.discountPercent);
	}

	public static boolean isDiscountTenPercentOrBetter(HotelRate rate) {
		float discountPercent = getDiscountPercent(rate);
		if (discountPercent <= 100 && discountPercent >= 0) {
			return discountPercent > 9.5;
		}
		return false;
	}

	public static String formattedReviewCount(int numberOfReviews) {
		NumberFormat nf = NumberFormat.getInstance();
		return nf.format(numberOfReviews);
	}

	public static int getFirstUncommonHotelIndex(List<Hotel> firstListOfHotels, List<Hotel> secondListOfHotels) {
		int initialRefreshIndex = Integer.MAX_VALUE;
		int firstListSize = firstListOfHotels.size();
		int secondListSize = secondListOfHotels.size();
		int size = (firstListSize > secondListSize) ? secondListSize : firstListSize;
		for (int i = 0; i < size; i++) {
			if (!firstListOfHotels.get(i).hotelId.equals(secondListOfHotels.get(i).hotelId)) {
				initialRefreshIndex = i;
				break;
			}
		}
		return initialRefreshIndex;
	}

	public static String getFreeCancellationText(Context context, String cancellationWindow) {
		if (cancellationWindow != null) {
			String cancellationDate = LocaleBasedDateFormatUtils
				.localDateToEEEMMMd(
					ApiDateUtils.yyyyMMddHHmmToDateTime(cancellationWindow).toLocalDate());
			return Phrase.from(context, R.string.hotel_free_cancellation_before_TEMPLATE)
				.put("date", cancellationDate)
				.format()
				.toString();
		}
		else {
			return context.getString(R.string.free_cancellation);
		}
	}
}
