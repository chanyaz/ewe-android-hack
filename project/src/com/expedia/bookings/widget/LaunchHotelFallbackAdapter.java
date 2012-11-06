package com.expedia.bookings.widget;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelDestination;
import com.expedia.bookings.data.LaunchHotelFallbackData;
import com.expedia.bookings.utils.FontCache;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.ObjectAnimator;

public class LaunchHotelFallbackAdapter extends LaunchBaseAdapter<HotelDestination> {

	private Context mContext;
	private LayoutInflater mInflater;

	private View[] mViewCache;

	public LaunchHotelFallbackAdapter(Context context) {
		super(context, R.layout.row_launch_tile_hotel_destination);
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Add enough blank items so that we can show blank tiles before loading
		int numTiles = getNumTiles();
		for (int a = 0; a < numTiles; a++) {
			add(null);
		}
		mViewCache = new View[numTiles];
	}

	public void setHotelDestinations(LaunchHotelFallbackData launchHotelFallbackData) {
		this.clear();

		if (launchHotelFallbackData != null && launchHotelFallbackData.getDestinations() != null) {
			for (HotelDestination hotel : launchHotelFallbackData.getDestinations()) {
				add(hotel);
			}

			mViewCache = new View[getViewCacheSize(launchHotelFallbackData.getDestinations().size())];
		}

		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		return AdapterView.ITEM_VIEW_TYPE_IGNORE;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int cacheIndex = position % mViewCache.length;
		View view = mViewCache[cacheIndex];

		// Use the Tag as a flag to indicate this view has been populated
		if (view != null && view.getTag() != null) {
			return view;
		}

		// Inflate the view if possible
		if (view == null) {
			view = mInflater.inflate(R.layout.row_launch_tile_hotel_destination, parent, false);
			mViewCache[cacheIndex] = view;
		}

		HotelDestination hotel = getItem(position);

		// If we're just measuring the height/width of the row, just return the view without doing anything to it.
		if (isMeasuring() || hotel == null) {
			return view;
		}

		View container = Ui.findView(view, R.id.launch_tile_container);
		TextView titleTextView = Ui.findView(view, R.id.launch_tile_title_text_view);
		FontCache.setTypeface(titleTextView, FontCache.Font.ROBOTO_LIGHT);

		titleTextView.setText(Html.fromHtml(mContext.getString(R.string.launch_hotel_fallback_tile_prompt,
				hotel.getLaunchTileText())));

		// Background image
		String url = hotel.getImgUrl();
		if (ImageCache.containsImage(url)) {
			Log.v("imageContained: " + position + " url: " + url);
			container.setBackgroundDrawable(new BitmapDrawable(ImageCache.getImage(url)));
			toggleTile(titleTextView, true);
		}
		else {
			Log.v("imageNotContained: " + position + " url: " + url);
			loadImageForLaunchStream(url, container, titleTextView);
			toggleTile(titleTextView, false);
		}

		// We're just using the Tag as a flag to indicate this view has been populated
		view.setTag(new Object());

		return view;
	}

	private boolean loadImageForLaunchStream(String url, final View layout, final TextView banner) {
		String key = layout.toString();
		Log.v("Loading RelativeLayout bg " + key + " with " + url);

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

	private void toggleTile(TextView label, boolean loaded) {
		int visibility = loaded ? View.VISIBLE : View.GONE;
		label.setVisibility(visibility);
	}

	@Override
	public int getTileHeight() {
		return mContext.getResources().getDimensionPixelSize(R.dimen.launch_tile_height_hotel);
	}
}
