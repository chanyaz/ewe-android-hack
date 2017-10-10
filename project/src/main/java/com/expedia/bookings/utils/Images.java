package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.lx.LXImage;
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

	public static String forLxCategory(Context context, String categoryKeyEN, String imageCode,
		float width) {
		boolean categoryAllThingsToDoAndItsCategoryImageAvailable = LXDataUtils
			.isCategoryAllThingsToDoAndItsCategoryImageAvailable(context, categoryKeyEN, imageCode);
		String categoryImageURL = categoryAllThingsToDoAndItsCategoryImageAvailable ? getTabletDestination(imageCode)
			: getMediaHost() + "/mobiata/mobile/apps/ExpediaBooking/ActivityCategories/images/" + LXUtils
				.whitelistAlphanumericFromCategoryKey(
					categoryKeyEN) + ".jpg";
		return new Akeakamai(categoryImageURL)
			.downsize(Akeakamai.pixels((int) width), Akeakamai.preserve())
			.build();
	}

	/**
	 * Returns list of image URLs based on the screen size.
	 * List contains the best match at 0th index followed by higher resolution and then lower resolution image URLs
	 *
	 * @param lxImages List of images in ascending order of size
	 * @param width    of the device
	 * @return sorted imageURLs
	 */
	public static List<String> getLXImageURLBasedOnWidth(List<LXImage> lxImages, int width) {
		List<String> imageURLs = new ArrayList<>();
		if (lxImages.size() > 1) {
			lxImages = sortLXImagesBasedOnWidth(lxImages, width);
		}

		for (LXImage image : lxImages) {
			imageURLs.add("https:" + image.imageURL);
		}
		return imageURLs;
	}

	private static List<LXImage> sortLXImagesBasedOnWidth(List<LXImage> lxImages, int width) {
		List<LXImage> sortedImages = new ArrayList<>();
		int index = 0;
		for (LXImage image : lxImages) {
			index++;
			if (image.imageSize.width >= width) {
				break;
			}
		}

		if (index != 0) {
			sortedImages.add(lxImages.get(index - 1));
			// Add higher res images if available
			sortedImages.addAll(lxImages.subList(index, lxImages.size()));

			// Add lower res images in reverse
			List<LXImage> lowerResImages = lxImages.subList(0, index - 1);
			Collections.reverse(lowerResImages);
			sortedImages.addAll(lowerResImages);
		}
		else {
			Collections.reverse(lxImages);
			return lxImages;
		}
		return sortedImages;
	}

	public static String getNearbyHotelImage(Hotel offer) {
		return getMediaHost() + offer.largeThumbnailUrl;
	}

	public static List<HotelMedia> getHotelImages(HotelOffersResponse offer, int placeholder) {
		List<HotelMedia> urlList = new ArrayList<>();
		if (offer != null && offer.photos != null) {
			for (int index = 0; index < offer.photos.size(); index++) {
				HotelOffersResponse.Photos photo = offer.photos.get(index);
				HotelMedia hotelMedia = new HotelMedia(getMediaHost() + photo.url, photo.displayText);
				urlList.add(hotelMedia);
			}
		}
		return urlList;
	}

	public static HeaderBitmapDrawable makeLaunchListBitmapDrawable(Context context) {
		HeaderBitmapDrawable headerBitmapDrawable = new HeaderBitmapDrawable();
		headerBitmapDrawable.setCornerMode(HeaderBitmapDrawable.CornerMode.ALL);
		headerBitmapDrawable.setCornerRadius(
			context.getResources().getDimensionPixelSize(R.dimen.home_screen_card_view_border_radius));
		headerBitmapDrawable.setScaleType(HeaderBitmapDrawable.ScaleType.CENTER_CROP);

		return headerBitmapDrawable;
	}

	public static HeaderBitmapDrawable makeHotelBitmapDrawable(Context context, HeaderBitmapDrawable.PicassoTargetListener listener, int width, String url, String tag, int fallbackImage) {
		HeaderBitmapDrawable headerBitmapDrawable = makeLaunchListBitmapDrawable(context);
		headerBitmapDrawable.setPicassoTargetListener(listener);
		HotelMedia hotelMedia = new HotelMedia(url);

		new PicassoHelper.Builder(context)
			.setPlaceholder(fallbackImage)
			.setTarget(headerBitmapDrawable.getPicassoTarget())
			.setTag(tag)
			.build()
			.load(hotelMedia.getBestUrls(width));

		return headerBitmapDrawable;
	}

	public static HeaderBitmapDrawable makeCollectionBitmapDrawable(Context context, HeaderBitmapDrawable.PicassoTargetListener listener, String url, String tag) {
		HeaderBitmapDrawable headerBitmapDrawable = makeLaunchListBitmapDrawable(context);
		headerBitmapDrawable.setPicassoTargetListener(listener);

		new PicassoHelper.Builder(context)
			.setPlaceholder(R.drawable.results_list_placeholder)
			.setTarget(headerBitmapDrawable.getPicassoTarget())
			.setTag(tag)
			.build()
			.load(url);

		return headerBitmapDrawable;
	}
}
