package com.expedia.bookings.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.LaunchHotelData;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.util.Ui;

public class LaunchHotelAdapter extends LaunchBaseAdapter<Property> {

	private static final String THUMBNAIL_SIZE = Media.IMAGE_BIG_SUFFIX;

	private Context mContext;
	private LayoutInflater mInflater;

	private Distance.DistanceUnit mDistanceUnit;

	private View[] mViewCache;

	public LaunchHotelAdapter(Context context) {
		super(context, R.layout.row_launch_tile_hotel);
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Add enough blank items so that we can show blank tiles before loading
		int numTiles = getNumTiles();
		for (int a = 0; a < numTiles; a++) {
			add(null);
		}
		mViewCache = new View[numTiles];
	}

	public void setProperties(LaunchHotelData launchHotelData) {
		this.clear();

		if (launchHotelData != null && launchHotelData.getProperties() != null) {
			mDistanceUnit = launchHotelData.getDistanceUnit();

			for (Property property : launchHotelData.getProperties()) {
				add(property);
			}

			mViewCache = new View[getViewCacheSize(launchHotelData.getProperties().size())];
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
			view = mInflater.inflate(R.layout.row_launch_tile_hotel, parent, false);
			mViewCache[cacheIndex] = view;
		}

		Property property = getItem(position);

		// If we're just measuring the height/width of the row, just return the view without doing anything to it.
		if (isMeasuring() || property == null) {
			return view;
		}

		// Cache all views in a ViewHolder
		ViewHolder vh = new ViewHolder();
		vh.mContainer = Ui.findView(view, R.id.launch_tile_container);
		vh.mBackgroundView = Ui.findView(view, R.id.background_view);
		vh.mSaleTextView = Ui.findView(view, R.id.launch_tile_sale_text_view);
		vh.mHotelTextView = Ui.findView(view, R.id.launch_tile_title_text_view);
		vh.mDistanceTextView = Ui.findView(view, R.id.launch_tile_distance_text_view);
		vh.mPriceTextView = Ui.findView(view, R.id.launch_tile_price_text_view);

		// Set custom fonts
		FontCache.setTypeface(vh.mHotelTextView, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(vh.mDistanceTextView, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(vh.mPriceTextView, FontCache.Font.ROBOTO_BOLD);
		FontCache.setTypeface(vh.mSaleTextView, FontCache.Font.ROBOTO_BOLD);

		// Bottom banner/label
		vh.mHotelTextView.setText(property.getName());
		vh.mDistanceTextView.setText(property.getDistanceFromUser().formatDistance(mContext, mDistanceUnit,
				true) + " - ");

		Rate lowestRate = property.getLowestRate();
		vh.mPriceTextView.setText(StrUtils.formatHotelPrice(lowestRate.getDisplayRate()));

		// Sale
		if (lowestRate.isSaleTenPercentOrBetter()) {
			vh.mSaleTextView.setText(mContext.getString(R.string.percent_minus_template,
					lowestRate.getDiscountPercent()));
			vh.mSaleTextView.setVisibility(View.VISIBLE);
		}
		else {
			vh.mSaleTextView.setVisibility(View.GONE);
		}

		// Background image
		loadImageForLaunchStream(property.getThumbnail().getUrl(THUMBNAIL_SIZE), vh.mContainer, vh.mBackgroundView);

		// We're just using the Tag as a flag to indicate this view has been populated
		view.setTag(vh);

		return view;
	}

	@Override
	public int getTileHeight() {
		return mContext.getResources().getDimensionPixelSize(R.dimen.launch_tile_height_hotel);
	}

	private static class ViewHolder {
		public ViewGroup mContainer;
		public ImageView mBackgroundView;
		public TextView mSaleTextView;
		public TextView mHotelTextView;
		public TextView mDistanceTextView;
		public TextView mPriceTextView;
	}
}
