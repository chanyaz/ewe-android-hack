package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.TextUtils;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.*;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.FontCache;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.ObjectAnimator;

public class LaunchFlightAdapter extends CircularArrayAdapter<Location> implements OnMeasureListener {

	private String PREFIX_IMAGE_INFO_KEY = "PREFIX_IMAGE_INFO_KEY";

	private static final int TYPE_EMPTY = 0;
	private static final int TYPE_LOADED = 1;
	private static final int NUM_ROW_TYPES = 2;

	private static final int DURATION_FADE_MS = 700;

	private int mWidth;
	private int mHeight;

	private Context mContext;

	LayoutInflater mInflater;

	private boolean mIsMeasuring = false;

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

	public void setLocations(List<Location> locations) {
		this.clear();

		for (Location location : locations) {
			add(location);
		}

		notifyDataSetChanged();
	}

	@Override
	public int getViewTypeCount() {
		return NUM_ROW_TYPES;
	}

	@Override
	public int getItemViewType(int position) {
		Location location = getItem(position);

		if (location == null) {
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

		final Location location = getItem(position);

		// If we're just measuring the height/width of the row, just return the view without doing anything to it.
		if (mIsMeasuring || location == null) {
			return convertView;
		}

		holder.titleTextView.setText(Html.fromHtml(mContext.getString(R.string.launch_flight_tile_prompt,
				location.getCity())));

		// Note: This just loads a bitmap from APK. TODO: load dynamically
		boolean dynamic = false;
		if (dynamic) {
			ImageInfoDownload imageInfoDownload = new ImageInfoDownload(location.getDestinationId());
			ImageInfoCallback imageInfoCallback = new ImageInfoCallback(holder.container);

			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			bd.cancelDownload(PREFIX_IMAGE_INFO_KEY);
			bd.startDownload(PREFIX_IMAGE_INFO_KEY, imageInfoDownload, imageInfoCallback);
		}
		else {
			setTileBackgroundBitmap(position, holder.container);
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
			BackgroundDownloader.getInstance().addDownloadListener(PREFIX_IMAGE_INFO_KEY + "_" + mCode, services);
			return services.getFlightsBackgroundImage(mCode, mWidth, mHeight);
		}

	}

	private class ImageInfoCallback implements BackgroundDownloader.OnDownloadComplete<BackgroundImageResponse> {

		private RelativeLayout mContainer;

		public ImageInfoCallback(RelativeLayout container) {
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
					loadImageForLaunchStream(response.getmImageUrl(), mContainer);
				}
			}

		}
	}

	private boolean loadImageForLaunchStream(String url, final RelativeLayout layout) {
		String key = layout.toString();
		Log.v("Loading RelativeLayout bg " + key + " with " + url);

		// Begin a load on the ImageView
		ImageCache.OnImageLoaded callback = new ImageCache.OnImageLoaded() {
			public void onImageLoaded(String url, Bitmap bitmap) {
				Log.v("ImageLoaded: " + url);

				layout.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
				layout.setVisibility(View.VISIBLE);
				ObjectAnimator.ofFloat(layout, "alpha", 0.0f, 1.0f).setDuration(DURATION_FADE_MS).start();
			}

			public void onImageLoadFailed(String url) {
				Log.v("Image load failed: " + url);
			}
		};

		return ImageCache.loadImage(key, url, callback);
	}

	private void setTileBackgroundBitmap(int position, RelativeLayout layout) {
		new DownloadTileTask(position, layout).execute();
	}

	private class DownloadTileTask extends AsyncTask<Void, Void, BitmapDrawable> {
		private int mPosition;
		private RelativeLayout mLayout;

		protected DownloadTileTask(int position, RelativeLayout layout) {
			mPosition = position;
			mLayout = layout;
		}

		protected BitmapDrawable doInBackground(Void... params) {
			Bitmap bg;
			if (mPosition % 2 == 0) {
				bg = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.launch_lhr);
			}
			else {
				bg = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.launch_jfk);
			}
			return new BitmapDrawable(mContext.getResources(), bg);
		}

		protected void onPostExecute(BitmapDrawable result) {
			mLayout.setBackgroundDrawable(result);

			// Alpha animate the tile to fade in
			mLayout.setVisibility(View.VISIBLE);
			ObjectAnimator.ofFloat(mLayout, "alpha", 0.0f, 1.0f).setDuration(DURATION_FADE_MS).start();
		}
	}

	private class TileHolder {
		public RelativeLayout container;
		public TextView titleTextView;
	}

	public static List<Location> getHardcodedDestinations() {
		List<Location> locations = new ArrayList<Location>();

		locations.add(new Location("LHR", "London", "London Heathrow"));
		locations.add(new Location("MIA", "Miami", "Miami, yo"));
		locations.add(new Location("JFK", "New York", "JFK - John F. Kennedy"));

		return locations;
	}

	//////////////////////////////////////////////////////////////////////////
	// OnMeasureListener

	@Override
	public void onStartMeasure() {
		mIsMeasuring = true;
	}

	@Override
	public void onStopMeasure() {
		mIsMeasuring = false;
	}

}
