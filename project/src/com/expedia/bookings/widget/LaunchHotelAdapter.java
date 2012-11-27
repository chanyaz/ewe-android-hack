package com.expedia.bookings.widget;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.HotelDestination;
import com.expedia.bookings.data.LaunchHotelData;
import com.expedia.bookings.data.LaunchHotelFallbackData;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.util.Ui;

/**
 * Handles both regular and fallback data.  The reason for this is to
 * avoid "jumping" when we fallback, and so that we can avoid issues of
 * infinite scrolling.
 */
public class LaunchHotelAdapter extends LaunchBaseAdapter<Object> {

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

	public void setHotelDestinations(LaunchHotelFallbackData launchHotelFallbackData) {
		this.clear();

		if (launchHotelFallbackData != null && launchHotelFallbackData.getDestinations() != null) {
			for (HotelDestination destination : launchHotelFallbackData.getDestinations()) {
				add(destination);
			}

			mViewCache = new View[getViewCacheSize(launchHotelFallbackData.getDestinations().size())];
		}

		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {
		Object item = getItem(position);
		if (item == null) {
			return 0;
		}

		if (item instanceof Property) {
			return Integer.valueOf(((Property) item).getPropertyId());
		}
		else {
			return position;
		}
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

		Object item = getItem(position);

		// If we're just measuring the height/width of the row, just return the view without doing anything to it.
		if (isMeasuring() || item == null) {
			return view;
		}

		// Cache all views in a ViewHolder
		ViewHolder vh = new ViewHolder();
		vh.mContainer = Ui.findView(view, R.id.launch_tile_container);
		vh.mBackgroundView = Ui.findView(view, R.id.background_view);
		vh.mSaleTextView = Ui.findView(view, R.id.launch_tile_sale_text_view);
		vh.mTitleTextView = Ui.findView(view, R.id.launch_tile_title_text_view);
		vh.mDetailsContainer = Ui.findView(view, R.id.launch_tile_details_container);
		vh.mDistanceTextView = Ui.findView(view, R.id.launch_tile_distance_text_view);
		vh.mPriceTextView = Ui.findView(view, R.id.launch_tile_price_text_view);

		vh.mBackgroundView.setFixedSize(true);

		// Set custom fonts
		FontCache.setTypeface(vh.mTitleTextView, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(vh.mDistanceTextView, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(vh.mPriceTextView, FontCache.Font.ROBOTO_BOLD);
		FontCache.setTypeface(vh.mSaleTextView, FontCache.Font.ROBOTO_BOLD);

		if (item instanceof Property) {
			Property property = (Property) item;

			// Bottom banner/label
			vh.mTitleTextView.setText(property.getName());
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
		}
		else if (item instanceof HotelDestination) {
			HotelDestination destination = (HotelDestination) item;

			// Make gone a number of Views we don't need in fallback
			vh.mDetailsContainer.setVisibility(View.GONE);
			vh.mSaleTextView.setVisibility(View.GONE);

			// Set the title
			vh.mTitleTextView.setText(Html.fromHtml(mContext.getString(R.string.launch_hotel_fallback_tile_prompt,
					destination.getLaunchTileText())));

			// Background image
			loadImageForLaunchStream(destination.getImgUrl(), vh.mContainer, vh.mBackgroundView);
		}

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
		public OptimizedImageView mBackgroundView;
		public TextView mSaleTextView;
		public TextView mTitleTextView;
		private ViewGroup mDetailsContainer;
		public TextView mDistanceTextView;
		public TextView mPriceTextView;
	}
}
