package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.Html;
import android.text.TextUtils;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.*;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.FontCache;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class LaunchFlightAdapter extends LaunchBaseAdapter<Destination> {

	private static String PREFIX_IMAGE_INFO_KEY = "IMAGE_INFO_KEY_";

	private static final int TYPE_EMPTY = 0;
	private static final int TYPE_LOADED = 1;
	private static final int NUM_ROW_TYPES = 2;

	private int mWidth;
	private int mHeight;

	private Context mContext;
	private LayoutInflater mInflater;

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

	public void setDestinations(List<Destination> destinations) {
		this.clear();

		for (Destination destination : destinations) {
			add(destination);
		}

		notifyDataSetChanged();
	}

	@Override
	public int getViewTypeCount() {
		return NUM_ROW_TYPES;
	}

	@Override
	public int getItemViewType(int position) {
		Destination destination = getItem(position);

		if (destination == null) {
			return TYPE_EMPTY;
		}
		else {
			return TYPE_LOADED;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		TileHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_launch_tile_flight, parent, false);

			holder = new TileHolder();

			holder.container = Ui.findView(convertView, R.id.launch_tile_container);
			holder.titleTextView = Ui.findView(convertView, R.id.launch_tile_title_text_view);
			FontCache.setTypeface(holder.titleTextView, FontCache.Font.ROBOTO_LIGHT);

			convertView.setTag(holder);
		}
		else {
			holder = (TileHolder) convertView.getTag();
		}

		final Destination destination = getItem(position);

		// If we're just measuring the height/width of the row, just return the view without doing anything to it.
		if (isMeasuring() || destination == null) {
			return convertView;
		}

		holder.titleTextView.setText(Html.fromHtml(mContext.getString(R.string.launch_flight_tile_prompt,
				destination.getCity())));

		// Load the image

		String url = destination.getImageUrl();

		// We don't have an image url, go to network and grab the url
		if (url == null) {
			String code = destination.getDestinationId();
			ImageInfoDownload imageInfoDownload = new ImageInfoDownload(code);
			ImageInfoCallback imageInfoCallback = new ImageInfoCallback(destination, holder.container);

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
				holder.container.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), ImageCache
						.getImage(url)));
				holder.container.setVisibility(View.VISIBLE);
			}
			else {
				loadImageForLaunchStream(url, holder.container);
			}
		}

		return convertView;
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
		private RelativeLayout mContainer;

		public ImageInfoCallback(Destination destination, RelativeLayout container) {
			mDestination = destination;
			mContainer = container;
		}

		@Override
		public void onDownload(BackgroundImageResponse response) {
			Log.i("ImageInfoCallback onDownload");

			// If the response is null, fake an error response (for the sake of cleaner code)
			if (response == null) {
				response = new BackgroundImageResponse();
				ServerError error = new ServerError(ServerError.ApiMethod.BACKGROUND_IMAGE);
				error.setPresentationMessage(mContext.getString(R.string.error_server));
				error.setCode("SIMULATED");
				response.addError(error);
			}

			if (response.hasErrors()) {
				Log.e("Errors downloading launch destination image info");
			}
			else {
				if (!TextUtils.isEmpty(response.getmCacheKey())) {
					String responseKey = response.getmCacheKey();
					String responseUrl = response.getmImageUrl();

					if (ImageCache.containsImage(responseUrl)) {
						if (mDestination.getImageKey() != null && responseKey.equals(mDestination.getImageKey())) {
							Log.i("Image SHAs match, use cached image");
							mContainer.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), ImageCache
									.getImage(responseUrl)));
							mContainer.setVisibility(View.VISIBLE);
						}
						else {
							Log.i("Image SHAs don't match, dl new");
							cacheImageMetaAndLoad(mDestination, responseKey, responseUrl, mContainer);
						}
					}
					else {
						Log.i("Destination image cache miss");
						cacheImageMetaAndLoad(mDestination, responseKey, responseUrl, mContainer);
					}
				}
			}
		}
	}

	private void cacheImageMetaAndLoad(Destination destination, String key, String url, RelativeLayout container) {
		destination.setImageMeta(key, url);
		loadImageForLaunchStream(url, container);
	}

	private class TileHolder {
		public RelativeLayout container;
		public TextView titleTextView;
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
