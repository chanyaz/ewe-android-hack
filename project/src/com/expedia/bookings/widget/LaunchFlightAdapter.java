package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.Html;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BackgroundImageResponse;
import com.expedia.bookings.data.Destination;
import com.expedia.bookings.data.LaunchFlightData;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.FontCache;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.ObjectAnimator;

public class LaunchFlightAdapter extends LaunchBaseAdapter<Destination> {

	private static String PREFIX_IMAGE_INFO_KEY = "IMAGE_INFO_KEY_";

	private int mWidth;
	private int mHeight;

	private Context mContext;
	private LayoutInflater mInflater;
	private View[] mViewCache = new View[1];

	public LaunchFlightAdapter(Context context) {
		super(context, R.layout.row_launch_tile_flight);
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// grab the width and height of the tile here for the image api
		mHeight = Math.round(mContext.getResources().getDimension(R.dimen.launch_tile_height_flight));

		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		mWidth = Math.round(display.getWidth() / 2);
		Log.i(String.format("LaunchFlightAdapter tile size %s x %s", mWidth, mHeight));
	}

	public void setDestinations(LaunchFlightData launchFlightData) {
		this.clear();

		for (Destination destination : launchFlightData.getDestinations()) {
			add(destination);
		}

		mViewCache = new View[launchFlightData.getDestinations().size()];

		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		return AdapterView.ITEM_VIEW_TYPE_IGNORE;
	}

	@Override
	public View getView(int position, View unused, ViewGroup parent) {
		int cacheIndex = position % mViewCache.length;
		View view = mViewCache[cacheIndex];

		// Use the Tag as a flag to indicate this view has been populated
		if (view != null && view.getTag() != null) {
			return view;
		}

		// Inflate the view if possible
		if (view == null) {
			view = mInflater.inflate(R.layout.row_launch_tile_flight, parent, false);
			mViewCache[cacheIndex] = view;
		}

		final Destination destination = getItem(position);

		// If we're just measuring the height/width of the row, just return the view without doing anything to it.
		if (isMeasuring() || destination == null) {
			return view;
		}

		View container = Ui.findView(view, R.id.launch_tile_container);
		TextView titleTextView = Ui.findView(view, R.id.launch_tile_title_text_view);
		FontCache.setTypeface(titleTextView, FontCache.Font.ROBOTO_LIGHT);

		titleTextView.setText(Html.fromHtml(mContext.getString(R.string.launch_flight_tile_prompt,
				destination.getCity())));

		// Load the image

		String url = destination.getImageUrl();
		View banner = Ui.findView(view, R.id.launch_tile_banner_container);

		// We don't have an image url, go to network and grab the url
		if (url == null) {
			String code = destination.getDestinationId();
			ImageInfoDownload imageInfoDownload = new ImageInfoDownload(code);
			ImageInfoCallback imageInfoCallback = new ImageInfoCallback(destination, container, banner);

			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			bd.cancelDownload(getBGDKey(code));
			bd.startDownload(getBGDKey(code), imageInfoDownload, imageInfoCallback);
		}

		// NOTE: It may be in poor form to use the cached image like this blindly (without checking against a newly
		// grabbed SHA fresh off the network) as it could be outdated. I don't anticipate these images being so volatile
		// that we will have to constantly request the meta info, as we only cache the destination images in memory.
		// TODO: Figure out if it is a big deal to be doing this 
		else {
			if (ImageCache.containsImage(url)) {
				container.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), ImageCache.getImage(url)));
				toggleTile(banner, true);
			}
			else {
				loadImageForLaunchStream(url, container, banner);
				toggleTile(banner, false);
			}
		}

		// We're just using the Tag as a flag to indicate this view has been populated
		view.setTag(new Object());

		return view;
	}

	protected boolean loadImageForLaunchStream(String url, final View layout, final View banner) {
		String key = layout.toString();
		Log.v("Loading View bg " + key + " with " + url);

		// Begin a load on the ImageView
		ImageCache.OnImageLoaded callback = new ImageCache.OnImageLoaded() {
			public void onImageLoaded(String url, Bitmap bitmap) {
				Log.v("ImageLoaded: " + url);

				layout.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
				banner.setVisibility(View.VISIBLE);
				ObjectAnimator.ofFloat(layout, "alpha", 0.0f, 1.0f).setDuration(DURATION_FADE_MS).start();
			}

			public void onImageLoadFailed(String url) {
				Log.v("Image load failed: " + url);
			}
		};

		return ImageCache.loadImage(key, url, callback);
	}

	private void toggleTile(View banner, boolean loaded) {
		int visibility = loaded ? View.VISIBLE : View.GONE;
		banner.setVisibility(visibility);
	}

	private class ImageInfoDownload implements BackgroundDownloader.Download<BackgroundImageResponse> {

		private String mCode;

		public ImageInfoDownload(String code) {
			mCode = code;
		}

		@Override
		public BackgroundImageResponse doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(getBGDKey(mCode), services);
			return services.getFlightsBackgroundImage(mCode, mWidth, mHeight);
		}

	}

	private class ImageInfoCallback implements BackgroundDownloader.OnDownloadComplete<BackgroundImageResponse> {

		private Destination mDestination;
		private View mContainer;
		private View mBanner;

		public ImageInfoCallback(Destination destination, View container, View banner) {
			mDestination = destination;
			mContainer = container;
			mBanner = banner;
		}

		@Override
		public void onDownload(BackgroundImageResponse response) {
			Log.i("ImageInfoCallback onDownload");

			if (response == null || response.hasErrors()) {
				Log.e("Errors downloading launch destination image info");
			}
			else {
				String responseKey = response.getCacheKey();
				String responseUrl = response.getImageUrl();
				mDestination.setImageMeta(responseKey, responseUrl);

				if (ImageCache.containsImage(responseUrl)) {
					Log.i("Destination image cache hit");
					mContainer.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), ImageCache
							.getImage(responseUrl)));
					mBanner.setVisibility(View.VISIBLE);
				}
				else {
					Log.i("Destination image cache miss");
					mBanner.setVisibility(View.GONE);
					loadImageForLaunchStream(responseUrl, mContainer, mBanner);
				}
			}
		}
	}

	private static String getBGDKey(String code) {
		return PREFIX_IMAGE_INFO_KEY + code;
	}

	public static List<Destination> getHardcodedDestinations() {
		List<Destination> destinations = new ArrayList<Destination>();

		destinations.add(new Destination("LHR", "London", "London Heathrow"));
		destinations.add(new Destination("MIA", "Miami", "Miami, yo"));
		destinations.add(new Destination("JFK", "New York", "JFK - John F. Kennedy"));

		return destinations;
	}

}
