package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.ViewUtils;

public class HotelAdapter extends BaseAdapter implements OnMeasureListener {

	// We implement a selected state for rows here.  The reason for this is that in touch mode, a ListView
	// does not have a "selected" state (unless you are in touch mode).  An alternative solution would have
	// been to use checkboxes/checked states, but I think we may someday want to use checkboxes for multiselect
	// so I don't want to block future functionality.
	private static final int ROW_NORMAL = 0;
	private static final int ROW_SELECTED = 1;

	private static final int ROOMS_LEFT_CUTOFF = 5;

	private Context mContext;
	private LayoutInflater mInflater;

	private SearchResponse mSearchResponse;

	private Property[] mCachedProperties;

	private boolean mIsMeasuring = false;
	private boolean mShowDistance = true;

	private DistanceUnit mDistanceUnit;

	private float mPriceTextSize;

	private int mSelectedPosition = -1;
	private boolean mHighlightSelectedPosition = true;

	private boolean mUseCondensedRows;

	public HotelAdapter(Context context) {
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mUseCondensedRows = LayoutUtils.isScreenNarrow(context);
	}

	public HotelAdapter(Context context, SearchResponse searchResponse) {
		this(context);

		setSearchResponse(searchResponse);
	}

	public void setSearchResponse(SearchResponse searchResponse) {
		mSearchResponse = searchResponse;
		rebuildCache();
	}

	public void highlightSelectedPosition(boolean highlight) {
		mHighlightSelectedPosition = highlight;
	}

	public void setSelectedPosition(int selectedPosition) {
		mSelectedPosition = selectedPosition;
	}

	public int getSelectedPosition() {
		return mSelectedPosition;
	}

	public void rebuildCache() {
		Log.d("Rebuilding hotel list adapter cache...");

		if (mSearchResponse == null || mSearchResponse.getProperties() == null) {
			mCachedProperties = null;
		}
		else {
			mCachedProperties = mSearchResponse.getFilteredAndSortedProperties();
			final int size = mCachedProperties == null ? 0 : mCachedProperties.length;
			if (size == 0) {
				TrackingUtils.trackErrorPage(mContext, "FilteredToZeroResults");
			}

			mDistanceUnit = mSearchResponse.getFilter().getDistanceUnit();

			// Clear all the images that are no longer going to be displayed (since we're only showing cached props)
			final List<Property> properties = new ArrayList<Property>();
			properties.addAll(mSearchResponse.getProperties());

			for (int i = 0; i < size; i++) {
				properties.remove(mCachedProperties[i]);
			}

			String longestPrice = "";
			for (Property property : properties) {
				Media thumbnail = property.getThumbnail();
				if (thumbnail != null && thumbnail.getUrl() != null) {
					ImageCache.removeImage(thumbnail.getUrl(), true);
				}

				String displayPrice = StrUtils.formatHotelPrice(property.getLowestRate().getDisplayRate());
				if (longestPrice.length() < displayPrice.length()) {
					longestPrice = displayPrice;
				}
			}

			// Determine the price text size based on longest price
			Resources r = mContext.getResources();
			DisplayMetrics dm = r.getDisplayMetrics();
			float maxTextSize = r.getDimension(R.dimen.hotel_row_price_text_size) / dm.scaledDensity;
			float maxViewWidthdp = r.getDimension(R.dimen.hotel_row_price_text_view_max_width) / dm.density;
			TextPaint textPaint = new TextPaint();
			textPaint.setTypeface(Typeface.DEFAULT_BOLD);
			mPriceTextSize = ViewUtils.getTextSizeForMaxLines(mContext, longestPrice, textPaint, maxViewWidthdp, 1,
					maxTextSize, 5);
		}

		notifyDataSetChanged();
	}

	public void setShowDistance(boolean showDistance) {
		mShowDistance = showDistance;
	}

	@Override
	public int getCount() {
		if (mCachedProperties != null) {
			return mCachedProperties.length;
		}

		return 0;
	}

	@Override
	public Object getItem(int position) {
		return mCachedProperties[position];
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public long getItemId(int position) {
		if (position >= mCachedProperties.length) {
			Log.w("Adapter may be trying to store instance state of hotels in list that have been filtered out while map is visible (See #7118).");
			Log.w("If you didn't just click a hotel after filtering on the Map tab in Android 2.2 or lower, this means there's a more serious problem.");
			return -1;
		}

		return Integer.valueOf(mCachedProperties[position].getPropertyId());
	}

	@Override
	public int getItemViewType(int position) {
		if (position == mSelectedPosition && mHighlightSelectedPosition) {
			return ROW_SELECTED;
		}
		else {
			return ROW_NORMAL;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HotelViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_hotel, parent, false);

			holder = new HotelViewHolder();
			holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail_image_view);
			holder.name = (TextView) convertView.findViewById(R.id.name_text_view);
			holder.price = (TextView) convertView.findViewById(R.id.price_text_view);
			holder.saleContainer = (FrameLayout) convertView.findViewById(R.id.sale_container);
			holder.saleText = (TextView) convertView.findViewById(R.id.sale_text_view);
			holder.userRating = (RatingBar) convertView.findViewById(R.id.user_rating_bar);
			holder.notRatedText = (TextView) convertView.findViewById(R.id.not_rated_text_view);
			holder.distance = (TextView) convertView.findViewById(R.id.distance_text_view);
			holder.urgency = (TextView) convertView.findViewById(R.id.urgency_text_view);

			convertView.setTag(holder);
		}
		else {
			holder = (HotelViewHolder) convertView.getTag();
		}

		// If we're just measuring the height/width of the row, just return the view without doing anything to it.
		if (mIsMeasuring) {
			return convertView;
		}

		Property property = (Property) getItem(position);
		holder.name.setText(property.getName());

		// We assume we have a lowest rate here; this may not be a safe assumption
		Rate lowestRate = property.getLowestRate();

		// Detect if the property is on sale, if it is do special things
		if (lowestRate.isOnSale()) {
			holder.price.setTextColor(mContext.getResources().getColor(R.color.hotel_price_sale_text_color));
			holder.saleContainer.setVisibility(View.VISIBLE);
			if (!mUseCondensedRows || !ExpediaBookingApp.useTabletInterface(mContext)) {
				holder.saleText.setText(mContext.getString(R.string.percent_minus_template,
						lowestRate.getSavingsPercent() * 100));
			}
		}
		else {
			holder.price.setTextColor(mContext.getResources().getColor(R.color.hotel_price_text_color));
			holder.saleContainer.setVisibility(View.GONE);
		}

		int roomsLeft = property.getRoomsLeftAtThisRate();
		if (roomsLeft != -1 && roomsLeft <= ROOMS_LEFT_CUTOFF) {
			holder.urgency.setText(mContext.getResources().getQuantityString(R.plurals.num_rooms_left, roomsLeft,
					roomsLeft));
			holder.urgency.setVisibility(View.VISIBLE);
		}
		else {
			holder.urgency.setVisibility(View.GONE);

		}

		holder.price.setTextSize(mPriceTextSize);
		holder.price.setText(StrUtils.formatHotelPrice(lowestRate.getDisplayRate()));

		holder.userRating.setRating((float) property.getAverageExpediaRating());
		if (holder.userRating.getRating() == 0) {
			holder.userRating.setVisibility(View.INVISIBLE);
			holder.notRatedText.setVisibility(View.VISIBLE);
		}
		else {
			holder.userRating.setVisibility(View.VISIBLE);
			holder.notRatedText.setVisibility(View.GONE);
		}

		// send true so as to use the "abbreviated" version, which has now become standard in 1.5
		holder.distance.setText(property.getDistanceFromUser().formatDistance(mContext, mDistanceUnit, true));
		holder.distance.setVisibility(mShowDistance ? View.VISIBLE : View.GONE);

		// See if there's a first image; if there is, use that as the thumbnail
		// Don't try to load the thumbnail if we're just measuring the height of the ListView
		boolean imageSet = false;
		if (!mIsMeasuring && property.getThumbnail() != null) {
			String url = property.getThumbnail().getUrl();
			imageSet = ImageCache.loadImage(url, holder.thumbnail);
		}
		if (!imageSet) {
			holder.thumbnail.setImageResource(R.drawable.ic_row_thumb_placeholder);
		}

		// Set the background based on whether the row is selected or not
		if (getItemViewType(position) == ROW_SELECTED) {
			convertView.setBackgroundResource(R.drawable.bg_row_selected);
		}
		else {
			convertView.setBackgroundResource(R.drawable.bg_row);
		}

		return convertView;
	}

	public void trimDrawables(int start, int end) {
		Log.v("Trimming HotelAdapter images from (" + start + ", " + end + ")");

		final int size = mCachedProperties.length;
		for (int i = 0; i < size; i++) {
			if (i < start || i > end) {
				Media thumbnail = mCachedProperties[i].getThumbnail();
				if (thumbnail != null) {
					String url = thumbnail.getUrl();
					ImageCache.removeImage(url, true);
				}
			}
			else {
				i = end;
			}
		}
	}

	private static class HotelViewHolder {
		public ImageView thumbnail;
		public TextView name;
		public TextView price;
		public FrameLayout saleContainer;
		public TextView saleText;
		public RatingBar userRating;
		public TextView notRatedText;
		public TextView distance;
		public TextView urgency;
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
