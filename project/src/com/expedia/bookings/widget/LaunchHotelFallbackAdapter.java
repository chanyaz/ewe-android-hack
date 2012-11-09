package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Bitmap;
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
import com.mobiata.android.graphics.ResilientBitmapDrawable;
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

		// Cache all views in a ViewHolder
		ViewHolder vh = new ViewHolder();
		vh.mContainer = Ui.findView(view, R.id.launch_tile_container);
		vh.mTitleTextView = Ui.findView(view, R.id.launch_tile_title_text_view);

		FontCache.setTypeface(vh.mTitleTextView, FontCache.Font.ROBOTO_LIGHT);

		vh.mTitleTextView.setText(Html.fromHtml(mContext.getString(R.string.launch_hotel_fallback_tile_prompt,
				hotel.getLaunchTileText())));

		// Background image
		String url = hotel.getImgUrl();
		if (ImageCache.containsImage(url)) {
			vh.mContainer.setVisibility(View.VISIBLE);
			vh.mContainer.setBackgroundDrawable(new ResilientBitmapDrawable(mContext.getResources(), ImageCache
					.getImage(url)));
		}
		else {
			loadImageForLaunchStream(url, vh);
		}

		// We're just using the Tag as a flag to indicate this view has been populated
		view.setTag(vh);

		return view;
	}

	private boolean loadImageForLaunchStream(String url, final ViewHolder vh) {
		String key = vh.toString();
		Log.v("Loading RelativeLayout bg " + key + " with " + url);

		// Begin a load on the ImageView
		ImageCache.OnImageLoaded callback = new ImageCache.OnImageLoaded() {
			public void onImageLoaded(String url, Bitmap bitmap) {
				Log.v("ImageLoaded: " + url);

				vh.mContainer.setVisibility(View.VISIBLE);
				vh.mContainer.setBackgroundDrawable(new ResilientBitmapDrawable(mContext.getResources(), bitmap));
				ObjectAnimator.ofFloat(vh.mContainer, "alpha", 0.0f, 1.0f).setDuration(DURATION_FADE_MS).start();
			}

			public void onImageLoadFailed(String url) {
				Log.v("Image load failed: " + url);
			}
		};

		return ImageCache.loadImage(key, url, callback);
	}

	@Override
	public int getTileHeight() {
		return mContext.getResources().getDimensionPixelSize(R.dimen.launch_tile_height_hotel);
	}

	private static class ViewHolder {
		public ViewGroup mContainer;
		public TextView mTitleTextView;
	}
}
