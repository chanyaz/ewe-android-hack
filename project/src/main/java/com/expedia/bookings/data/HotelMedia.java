package com.expedia.bookings.data;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.IMedia;
import com.expedia.bookings.bitmaps.PaletteCallback;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

/**
 * This object represents a resolution-independent Expedia media image,
 * found on http://media.expedia.com. These are basically images of hotels.
 *
 * @author doug
 *
 */
public class HotelMedia implements JSONable, IMedia {

/*
	 * The suffixes for different image sizes is documented here:
	 * https://team.mobiata.com/wiki/EAN_Servers#Expedia_Hotels_Image_Derivatives
	 *
	 * tl;dr version:
	 *
	 * URLs look like:
	 * http://media.expedia.com/hotels/1000000/20000/10600/10575/10575_12_b.jpg
	 *
	 * Last 2 letters of the filename should have the following codes:
	 *
	 * Name      Code   width height    type
	 * thumb       _t      70     70    Fixed
	 * 90X90f      _n      90     90    Fixed
	 * 140X140f    _g     140    140    Fixed
	 * 180x180f    _d     180    180    Fixed
	 * Small       _s     200    200    Variable
	 * Landscape   _l     255    144    Fixed
	 * Big         _b     350    350    Variable
	 * 500X500v    _y     500    500    Variable
	 * 1000X1000v  _z    1000   1000    Variable
	 */

	public enum Size {
		THUMB("t", 70, 70),
		N("n", 90, 90),
		G("g", 140, 140),
		D("d", 180, 180),
		SMALL("s", 200, 200),
		LANDSCAPE("l", 255, 144),
		BIG("b", 350, 350),
		Y("y", 500, 500),
		Z("z", 1000, 1000);

		public String code;
		public int width;
		public int height;

		Size(String c, int w, int h) {
			code = c;
			width = w;
			height = h;
		}

		public static Size parse(String c) {
			for (Size t : sMediaTypes) {
				if (c.equals(t.code)) {
					return t;
				}
			}
			return Size.BIG;
		}

		public static Size getIdealGridSize() {
			return Y;
		}
	}

	// A size-ordered list of the different MediaTypes
	private static final Size[] sMediaTypes = {
		Size.THUMB, Size.N, Size.G, Size.D, Size.SMALL,
		Size.LANDSCAPE, Size.BIG, Size.Y, Size.Z, };

	// ..."z.jpg"
	private static final int SUFFIX_LENGTH = 5;

	private String mBaseUrl;
	public String mDescription = "";
	private Size mOriginalType;
	private boolean mIsPlaceholder = false;

	public HotelMedia() {
		// Default constructor
	}

	public HotelMedia(String url) {
		setUrl(url);
	}

	public HotelMedia(String url, String description) {
		this.mDescription = description;
		setUrl(url);
	}

	@Override
	public void setIsPlaceholder(boolean isPlaceholder) {
		mIsPlaceholder = isPlaceholder;
	}

	@Override
	public boolean getIsPlaceHolder() {
		return mIsPlaceholder;
	}

	public String getOriginalUrl() {
		if (mOriginalType == null) { // null when default constructor HotelMedia() used (e.g. for placeholder image)
			return "";
		}
		return getUrl(mOriginalType);
	}

	/**
	 * Returns the url for a specific resolution of this Media.
	 * @param type Size
	 * @return url
	 */
	public String getUrl(Size type) {
		return mBaseUrl + type.code + ".jpg";
	}

	// Returns the index of the media whose size best matches the given width.
	// Let's try: media width greater than 80% of desired width.
	private int getBestIndex(int width) {
		width = (int) (width * 0.8f);
		for (int i = 0; i < sMediaTypes.length; i++) {
			if (sMediaTypes[i].width >= width) {
				return i;
			}
		}
		return sMediaTypes.length - 1;
	}

	/**
	 * Returns a list of urls for the [count] best sized resolutions of this Media,
	 * using this heuristic:
	 *
	 * #1 Best: same result as getUrl(width)
	 * #2 Best: smallest image that is larger than #1 (if that exists)
	 * #3 Best: largest image that is smaller than #1 (if that exists)
	 * #4 Best: smallest image that is larger than #2 (if that exists)
	 * #5 Best: largest image that is smaller than #3 (if that exists)
	 * etc.
	 *
	 * @param count
	 * @param width
	 * @return
	 */
	private List<String> getBestUrls(int count, int width) {
		int i = getBestIndex(width);
		return getUrls(count, i,
			i + 1, i - 1, i + 2, i - 2, i + 3, i - 3, i + 4, i - 4,
			i + 5, i - 5, i + 6, i - 6, i + 7, i - 7, i + 8, i - 8);
	}

	public List<String> getBestUrls(int width) {
		return getBestUrls(sMediaTypes.length, width);
	}

	private List<String> getUrls(int count, int... indices) {
		ArrayList<String> urls = new ArrayList<>(count);
		for (int i : indices) {
			if (urls.size() >= count) {
				return urls;
			}

			if (i >= sMediaTypes.length || i < 0) {
				continue;
			}

			String url = getUrl(sMediaTypes[i]);
			if (urls.contains(url)) {
				continue;
			}

			urls.add(url);
		}
		return urls;
	}

	public void setUrl(String url) {
		if (url == null) {
			throw new InvalidParameterException("url cannot be null");
		}
		int split = url.length() - SUFFIX_LENGTH;
		mBaseUrl = url.substring(0, split);
		mOriginalType = Size.parse(url.substring(split, split + 1));
	}

	@Override
	public void loadImage(ImageView imageView, PicassoTarget target, int defaultResId) {
		fillImageView(imageView, defaultResId, target);
	}

	@Override
	public void loadErrorImage(final ImageView imageView, final PicassoTarget target, final int fallbackId) {
		imageView.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				imageView.getViewTreeObserver().removeOnPreDrawListener(this);
				new PicassoHelper.Builder(imageView.getContext())
					.setTarget(target)
					.build()
					.load(fallbackId);
				return true;
			}
		});
	}

	public List<String> getHighResUrls() {
		return Arrays.asList(getUrl(Size.Y), getUrl(Size.BIG), getOriginalUrl());
	}

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("url", getOriginalUrl());
			obj.put("isPlaceholder", getIsPlaceHolder());
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Media object to JSON.", e);
			return null;
		}
	}

	public boolean fromJson(JSONObject obj) {
		String url = obj.optString("url");
		if (Strings.isNotEmpty(url)) {
			setUrl(url);
		}
		setIsPlaceholder(obj.optBoolean("isPlaceholder"));
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		HotelMedia that = (HotelMedia) o;

		if (mIsPlaceholder != that.mIsPlaceholder) {
			return false;
		}
		if (mBaseUrl != null ? !mBaseUrl.equals(that.mBaseUrl) : that.mBaseUrl != null) {
			return false;
		}
		if (mDescription != null ? !mDescription.equals(that.mDescription) : that.mDescription != null) {
			return false;
		}
		return mOriginalType == that.mOriginalType;

	}

	@Override
	public int hashCode() {
		int result = mBaseUrl != null ? mBaseUrl.hashCode() : 0;
		result = 31 * result + (mDescription != null ? mDescription.hashCode() : 0);
		result = 31 * result + (mOriginalType != null ? mOriginalType.hashCode() : 0);
		result = 31 * result + (mIsPlaceholder ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		JSONObject obj = toJson();
		try {
			return obj.toString(2);
		}
		catch (JSONException e) {
			return obj.toString();
		}
	}

	/**
	 * Determines the best-sized Media to fit in the passed ImageView, creates a
	 * UrlBitmapDrawable with that sized Media (falling back to lower resolutions
	 * if necessary), and stuffs it into that ImageView. The Media will be
	 * downloaded in the background. This variation allows the caller to hook a callback.
	 */
	private void fillImageView(final ImageView view, final int placeholderResId,
		final PicassoTarget target) {

		// Do this OnPreDraw so that we are sure we have the imageView's width
		view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				view.getViewTreeObserver().removeOnPreDrawListener(this);
				fillImageView(view, view.getWidth()/2, placeholderResId, target);
				return true;
			}
		});
	}

	public void fillImageView(final ImageView view, final int placeholderResId,
		final PaletteCallback callback, final String tag) {

		// Do this OnPreDraw so that we are sure we have the imageView's width
		view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				view.getViewTreeObserver().removeOnPreDrawListener(this);
				fillImageView(view, view.getWidth()/2, placeholderResId, callback, tag);
				return true;
			}
		});
	}

	/**
	 * Creates a UrlBitmapDrawable with appropriately sized Media (falling back to lower resolutions
	 * if necessary), and stuffs it into the passed ImageView. The Media will be
	 * downloaded in the background.
	 */
	public void fillImageView(final ImageView view, final int width, final int placeholderResId) {
		new PicassoHelper.Builder(view).setPlaceholder(placeholderResId).build()
			.load(getBestUrls(width));
	}

	private void fillImageView(final ImageView view, final int width, final int placeholderResId,
		final PicassoTarget target) {
		new PicassoHelper.Builder(view.getContext()).setCacheEnabled(false).setPlaceholder(placeholderResId).setTarget(target).build()
			.load(getBestUrls(width));
	}

	private void fillImageView(final ImageView view, final int width, final int placeholderResId,
		final PaletteCallback callback, final String tag) {
		new PicassoHelper.Builder(view).setPlaceholder(placeholderResId).applyPaletteTransformation(
			callback).setTag(tag).build().load(getBestUrls(width));
	}
	/**
	 * This is a specialized variant on fillImageView, where the ImageView wants to
	 * hold a HeaderBitmapDrawable.
	 *
	 * @see{fillImageView()}
	 */
	public void fillHeaderBitmapDrawable(final ImageView view, final HeaderBitmapDrawable drawable,
		final int placeholderResId) {
		view.setImageDrawable(drawable);

		// Do this OnPreDraw so that we are sure we have the imageView's width
		view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				view.getViewTreeObserver().removeOnPreDrawListener(this);
				List<String> urls = getBestUrls(view.getWidth());
				new PicassoHelper.Builder(view.getContext()).setPlaceholder(placeholderResId).setTarget(
					drawable.getPicassoTarget()).build().load(urls);
				return true;
			}
		});
	}

	@Override
	public int getPlaceHolderId() {
		return R.drawable.room_fallback;
	}

	@Override
	public int getFallbackImage() {
		return R.drawable.room_fallback;
	}

	@Override
	public String getDescription() {
		return mDescription;
	}
}
