package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.Filter;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class LaunchHotelAdapter extends CircularArrayAdapter<Property> implements OnMeasureListener {

	private static final int TYPE_EMPTY = 0;
	private static final int TYPE_LOADED = 1;
	private static final int NUM_ROW_TYPES = 2;

	private static final String THUMBNAIL_SIZE = Media.IMAGE_BIG_SUFFIX;

	private Context mContext;

	LayoutInflater mInflater;

	private Distance.DistanceUnit mDistanceUnit;

	private boolean mIsMeasuring = false;

	public LaunchHotelAdapter(Context context) {
		super(context, R.layout.row_launch_tile_hotel);
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setProperties(SearchResponse response) {
		this.clear();
		if (response != null && response.getProperties() != null) {
			Property[] properties = response.getFilteredAndSortedProperties(Filter.Sort.DISTANCE);
			for (Property property : properties) {
				add(property);
			}
			mDistanceUnit = response.getFilter().getDistanceUnit();
		}

		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {
		Property property = getItem(position);
		if (property == null) {
			return 0;
		}

		return Integer.valueOf(property.getPropertyId());
	}

	@Override
	public int getViewTypeCount() {
		return NUM_ROW_TYPES;
	}

	@Override
	public int getItemViewType(int position) {
		Property property = getItem(position);

		if (property == null) {
			return TYPE_EMPTY;
		}

		String url = property.getThumbnail().getUrl(THUMBNAIL_SIZE);

		if (ImageCache.containsImage(url)) {
			return TYPE_LOADED;
		}

		return TYPE_EMPTY;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		TileHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_launch_tile_hotel, parent, false);

			holder = new TileHolder();

			holder.container = Ui.findView(convertView, R.id.launch_tile_container);
			holder.titleTextView = Ui.findView(convertView, R.id.launch_tile_title_text_view);
			FontCache.setTypeface(holder.titleTextView, FontCache.Font.ROBOTO_LIGHT);

			holder.distanceTextView = Ui.findView(convertView, R.id.launch_tile_distance_text_view);
			FontCache.setTypeface(holder.distanceTextView, FontCache.Font.ROBOTO_LIGHT);

			holder.priceTextView = Ui.findView(convertView, R.id.launch_tile_price_text_view);
			FontCache.setTypeface(holder.priceTextView, FontCache.Font.ROBOTO_BOLD);

			convertView.setTag(holder);
		}
		else {
			holder = (TileHolder) convertView.getTag();
		}

		Property property = getItem(position);

		// If we're just measuring the height/width of the row, just return the view without doing anything to it.
		if (mIsMeasuring || property == null) {
			return convertView;
		}

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
