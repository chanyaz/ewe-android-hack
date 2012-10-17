package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.expedia.bookings.R;
import com.expedia.bookings.data.*;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.Params;
import com.mobiata.android.util.Ui;

public class LaunchStreamAdapter extends BaseAdapter implements OnMeasureListener {

	private static final int TYPE_EMPTY = 0;
	private static final int TYPE_LOADED = 1;
	private static final int NUM_ROW_TYPES = 2;

	private static final int NUM_PROPERTIES_DEFAULT = 10;

	private static final String THUMBNAIL_SIZE = Media.IMAGE_BIG_SUFFIX;

	private Context mContext;

	private Property[] mProperties;

	private Distance.DistanceUnit mDistanceUnit;

	private boolean mIsMeasuring = false;
	private boolean mHasRealProperties = false;

	public LaunchStreamAdapter(Context context) {
		mContext = context;

		// init with blank data to ensure that there exist dividers on page load to achieve animation effect
		mProperties = new Property[NUM_PROPERTIES_DEFAULT];
	}

	public void setProperties(SearchResponse response) {
		if (response == null || response.getProperties() == null) {
			mProperties = null;
		}
		else {
			mProperties = response.getFilteredAndSortedProperties(Filter.Sort.DEALS);
		}

		mDistanceUnit = response.getFilter().getDistanceUnit();
		mHasRealProperties = true;

		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (mProperties == null) {
			return 0;
		}
		else {
			return mProperties.length;
		}
	}

	@Override
	public Object getItem(int position) {
		if (mProperties == null) {
			return null;
		}
		else {
			return mProperties[position];
		}
	}

	@Override
	public long getItemId(int position) {
		if (mProperties == null || !mHasRealProperties) {
			return 0;
		}
		else {
			return Integer.valueOf(mProperties[position].getPropertyId());
		}
	}

	@Override
	public int getViewTypeCount() {
		return NUM_ROW_TYPES;
	}

	@Override
	public int getItemViewType(int position) {
		if (mProperties != null && mHasRealProperties) {
			Property property = mProperties[position];
			String url = property.getThumbnail().getUrl(THUMBNAIL_SIZE);

			if (ImageCache.containsImage(url)) {
				return TYPE_LOADED;
			}
			else {
				return TYPE_EMPTY;
			}
		}
		return TYPE_EMPTY;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		TileHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.row_launch_tile, parent, false);

			holder = new TileHolder();

			holder.container = Ui.findView(convertView, R.id.launch_tile_container);
			holder.titleTextView = Ui.findView(convertView, R.id.launch_tile_title_text_view);
			holder.distanceTextView = Ui.findView(convertView, R.id.launch_tile_distance_text_view);
			holder.priceTextView = Ui.findView(convertView, R.id.launch_tile_price_text_view);

			convertView.setTag(holder);
		}
		else {
			holder = (TileHolder) convertView.getTag();
		}

		// If we're just measuring the height/width of the row, just return the view without doing anything to it.
		if (mIsMeasuring || !mHasRealProperties) {
			return convertView;
		}

		Property property = mProperties[position];

		holder.titleTextView.setText(property.getName());
		holder.distanceTextView.setText(property.getDistanceFromUser().formatDistance(mContext, mDistanceUnit,
				true));

		Rate lowestRate = property.getLowestRate();
		final String hotelPrice = StrUtils.formatHotelPrice(lowestRate.getDisplayRate());
		holder.priceTextView.setText(hotelPrice);

		String url = property.getThumbnail().getUrl(THUMBNAIL_SIZE);
		if (ImageCache.containsImage(url)) {
			Log.i("imageContained: " + position + " url: " + url);
			holder.container.setBackgroundDrawable(new BitmapDrawable(ImageCache.getImage(url)));
			holder.container.setVisibility(View.VISIBLE);
		}
		else {
			Log.i("imageNotContained: " + position + " url: " + url);
			holder.container.setVisibility(View.INVISIBLE);
			loadImageForLaunchStream(url, holder.container);
		}

		return convertView;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Private methods and stuff

	private boolean loadImageForLaunchStream(String url, final RelativeLayout layout) {
		String key = layout.toString();
		Log.v("Loading RelativeLayout bg " + key + " with " + url);

		// Begin a load on the ImageView
		ImageCache.OnImageLoaded callback = new ImageCache.OnImageLoaded() {
			public void onImageLoaded(String url, Bitmap bitmap) {
				Log.v("ImageLoaded: " + url);
				layout.setVisibility(View.VISIBLE);
				layout.setBackgroundDrawable(new BitmapDrawable(bitmap));
				AlphaAnimation alpha = new AlphaAnimation(0.0F, 1.0F);
				alpha.setDuration(350);
				alpha.setFillAfter(true);
				layout.startAnimation(alpha);
			}

			public void onImageLoadFailed(String url) {
				Log.v("Image load failed: " + url);
			}
		};

		return ImageCache.loadImage(key, url, callback);
	}

	private class TileHolder {
		public RelativeLayout container;
		public TextView titleTextView;
		public TextView distanceTextView;
		public TextView priceTextView;
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
