package com.expedia.bookings.utils;

import java.util.Locale;

import android.content.Context;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.HotelMedia;
import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarType;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;

public class Images {

	private Images() {
		// ignore
	}

	private static String sCustomHost = null;

	public static void setCustomHost(String customHost) {
		sCustomHost = customHost;
	}

	public static String getMediaHost() {
		if (Strings.isNotEmpty(sCustomHost)) {
			return sCustomHost;
		}

		return BuildConfig.MEDIA_URL;
	}

	public static String getCollectionImageUrl(CollectionLocation location, int widthPx) {
		String url = getTabletLaunch(location.imageCode);
		return new Akeakamai(url) //
			.downsize(Akeakamai.pixels(widthPx), Akeakamai.preserve()) //
			.build();
	}

	public static String getTabletLaunch(String destination) {
		return getMediaHost() + "/mobiata/mobile/apps/ExpediaBooking/LaunchDestinations/images/" + destination + ".jpg";
	}

	public static String getFlightDestination(String destination) {
		return getMediaHost() + "/mobiata/mobile/apps/ExpediaBooking/FlightDestinations/images/" + destination + ".jpg";
	}

	public static String getTabletDestination(String destination) {
		return getMediaHost() + "/mobiata/mobile/apps/ExpediaBooking/TabletDestinations/images/" + destination + ".jpg";
	}

	public static String getCarRental(Car car, float width) {
		return getCarRental(car.getCategory(), car.getType(), width);
	}

	public static String getCarRental(CarCategory category, CarType type, float width) {
		final String categoryString = category.toString().replace("_", "").toLowerCase(Locale.ENGLISH);
		final String typeString = type.toString().replace("_", "").toLowerCase(Locale.ENGLISH);
		final String code = categoryString + "_" + typeString;
		return new Akeakamai(getMediaHost() + "/mobiata/mobile/apps/ExpediaBooking/CarRentals/images/" + code + ".jpg")
			.downsize(Akeakamai.pixels((int) width), Akeakamai.preserve())
			.build();
	}

	public static String getLXImageURL(String url) {
		return "https:" + url;
	}

	public static String getNearbyHotelImage(Hotel offer) {
		return getMediaHost() + offer.largeThumbnailUrl;
	}

	public static HeaderBitmapDrawable makeLaunchListBitmapDrawable(Context context) {
		HeaderBitmapDrawable headerBitmapDrawable = new HeaderBitmapDrawable();
		headerBitmapDrawable.setCornerMode(HeaderBitmapDrawable.CornerMode.ALL);
		headerBitmapDrawable.setCornerRadius(
			context.getResources().getDimensionPixelSize(R.dimen.launch_list_corner_radius));
		headerBitmapDrawable.setScaleType(HeaderBitmapDrawable.ScaleType.CENTER_CROP);

		return headerBitmapDrawable;
	}

	public static HeaderBitmapDrawable makeHotelBitmapDrawable(Context context, HeaderBitmapDrawable.CallbackListener listener, int width, String url, String tag) {
		HeaderBitmapDrawable headerBitmapDrawable = makeLaunchListBitmapDrawable(context);
		headerBitmapDrawable.setCallbackListener(listener);
		HotelMedia hotelMedia = new HotelMedia(url);

		new PicassoHelper.Builder(context)
			.setPlaceholder(R.drawable.results_list_placeholder)
			.setTarget(headerBitmapDrawable.getCallBack())
			.setTag(tag)
			.build()
			.load(hotelMedia.getBestUrls(width));

		return headerBitmapDrawable;
	}

	public static HeaderBitmapDrawable makeCollectionBitmapDrawable(Context context, HeaderBitmapDrawable.CallbackListener listener, String url, String tag) {
		HeaderBitmapDrawable headerBitmapDrawable = makeLaunchListBitmapDrawable(context);
		headerBitmapDrawable.setCallbackListener(listener);

		new PicassoHelper.Builder(context)
			.setPlaceholder(R.drawable.results_list_placeholder)
			.setTarget(headerBitmapDrawable.getCallBack())
			.setTag(tag)
			.build()
			.load(url);

		return headerBitmapDrawable;
	}
}
