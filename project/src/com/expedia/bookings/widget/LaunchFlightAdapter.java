package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Destination;
import com.expedia.bookings.data.LaunchFlightData;
import com.expedia.bookings.utils.FontCache;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.ObjectAnimator;

public class LaunchFlightAdapter extends LaunchBaseAdapter<Destination> {

	private Context mContext;
	private LayoutInflater mInflater;
	private View[] mViewCache;

	public LaunchFlightAdapter(Context context) {
		super(context, R.layout.row_launch_tile_flight);
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Add enough blank items so that we can show blank tiles before loading
		int numTiles = getNumTiles();
		for (int a = 0; a < numTiles; a++) {
			add(null);
		}
		mViewCache = new View[numTiles];
	}

	public void setDestinations(LaunchFlightData launchFlightData) {
		this.clear();

		if (launchFlightData != null) {
			for (Destination destination : launchFlightData.getDestinations()) {
				add(destination);
			}

			mViewCache = new View[getViewCacheSize(launchFlightData.getDestinations().size())];
		}

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
				destination.getCityFormatted())));

		// Load the image
		//
		// NOTE: It may be in poor form to use the cached image like this blindly (without checking against a newly
		// grabbed SHA fresh off the network) as it could be outdated. I don't anticipate these images being so volatile
		// that we will have to constantly request the meta info, as we only cache the destination images in memory.
		// TODO: Figure out if it is a big deal to be doing this
		String url = destination.getImageUrl();
		if (ImageCache.containsImage(url)) {
			container.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), ImageCache.getImage(url)));
			titleTextView.setVisibility(View.VISIBLE);
		}
		else {
			loadImageForLaunchStream(url, container, titleTextView);
			titleTextView.setVisibility(View.GONE);
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

	@Override
	public int getTileHeight() {
		return mContext.getResources().getDimensionPixelSize(R.dimen.launch_tile_height_flight);
	}
}
