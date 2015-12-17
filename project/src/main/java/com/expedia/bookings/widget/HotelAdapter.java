package com.expedia.bookings.widget;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.section.HotelSummarySection;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.ViewUtils;

public class HotelAdapter extends BaseAdapter implements OnMeasureListener {

	// We'll use the itemViewType's to indicate flags on a per-row basis.

	// We implement a selected state for rows here. The reason for this is that in touch mode,
	// a ListView does not have a "selected" state (unless you are in touch mode).
	static final int BIT_SELECTED = 0x1;
	// The expandable bit indicates (on tablet only) if the row can be "expanded"; i.e. depending
	// on some set of circumstances, it can change its height from one height to another.
	static final int BIT_EXPANDABLE = 0x2;

	static final int VIEW_TYPE_COUNT = 4;

	private Context mContext;
	private LayoutInflater mInflater;

	private HotelSearchResponse mSearchResponse;

	private List<Property> mCachedProperties;

	private boolean mIsMeasuring = false;
	private boolean mShowDistance = false;

	private DistanceUnit mDistanceUnit;

	private float mPriceTextSize;

	private int mSelectedPosition = -1;
	private boolean mHighlightSelectedPosition = true;

	private boolean mShouldShowVipIcon;

	public HotelAdapter(Activity activity) {
		init(activity);
	}

	public HotelAdapter(Activity activity, HotelSearchResponse searchResponse) {
		this(activity);
		setSearchResponse(searchResponse);
	}

	private void init(final Activity activity) {
		mContext = activity;
		mInflater = LayoutInflater.from(mContext);
	}

	public void setSearchResponse(HotelSearchResponse searchResponse) {
		mSearchResponse = searchResponse;
		rebuildCache();
	}

	public void highlightSelectedPosition(boolean highlight) {
		mHighlightSelectedPosition = highlight;
	}

	public void setSelectedPosition(int selectedPosition) {
		mSelectedPosition = selectedPosition;
	}

	public void setSelectedProperty(Property property) {
		setSelectedPosition(getPositionOfProperty(property));
	}

	public int getSelectedPosition() {
		return mSelectedPosition;
	}

	public int getPositionOfProperty(Property property) {
		if (property != null) {
			int count = getCount();
			for (int position = 0; position < count; position++) {
				if (getItem(position) == property) {
					return position;
				}
			}
		}
		return -1;
	}

	//TODO: rebuildCacheBlocking is expensive. Consider trying this on a background thread.
	private void rebuildCache() {
		mCachedProperties = rebuildCacheBlocking();
		if (mSearchResponse != null && mSearchResponse.getFilter() != null) {
			mDistanceUnit = mSearchResponse.getFilter().getDistanceUnit();
		}
		notifyDataSetChanged();
	}

	private List<Property> rebuildCacheBlocking() {
		Log.d("Rebuilding hotel list adapter cache...");

		if (mSearchResponse == null || mSearchResponse.getProperties() == null) {
			return null;
		}

		List<Property> properties = mSearchResponse.getFilteredAndSortedProperties(Db.getHotelSearch().getSearchParams());

		if (properties == null || properties.size() == 0) {
			OmnitureTracking.trackErrorPage("FilteredToZeroResults");
		}
		else {
			mPriceTextSize = determinePriceTextSize(properties);
		}

		return properties;
	}

	private float determinePriceTextSize(List<Property> properties) {
		if (properties == null || properties.size() == 0) {
			Resources r = mContext.getResources();
			DisplayMetrics dm = r.getDisplayMetrics();
			float defaultTextSize = r.getDimension(R.dimen.hotel_row_price_text_size) / dm.scaledDensity;
			return defaultTextSize;
		}

		String longestPrice = "";
		for (Property property : properties) {
			Rate rate = property.getLowestRate();
			if (rate != null) {
				String displayPrice = StrUtils.formatHotelPrice(rate.getDisplayPrice());
				if (longestPrice.length() < displayPrice.length()) {
					longestPrice = displayPrice;
				}
			}
		}

		return determinePriceTextSize(longestPrice);
	}

	public float determinePriceTextSize(String longestPrice) {
		Resources r = mContext.getResources();
		DisplayMetrics dm = r.getDisplayMetrics();
		float defaultTextSize = r.getDimension(R.dimen.hotel_row_price_text_size) / dm.scaledDensity;
		float maxViewWidthdp = r.getDimension(R.dimen.hotel_row_price_text_view_max_width) / dm.density;
		TextPaint textPaint = new TextPaint();
		textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		return ViewUtils.getTextSizeForMaxLines(mContext, longestPrice, textPaint,
			maxViewWidthdp, 1, defaultTextSize, 5);
	}

	public void setShowDistance(boolean showDistance) {
		mShowDistance = showDistance;
	}

	public void setShowVipIcon(boolean showIcon) {
		mShouldShowVipIcon = showIcon;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (mCachedProperties != null) {
			return mCachedProperties.size();
		}

		return 0;
	}

	@Override
	public Object getItem(int position) {
		return mCachedProperties.get(position);
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public long getItemId(int position) {
		if (position >= mCachedProperties.size()) {
			Log.w("Adapter may be trying to store instance state of hotels in list that have been filtered out while map is visible (See #7118).");
			Log.w("If you didn't just click a hotel after filtering on the Map tab in Android 2.2 or lower, this means there's a more serious problem.");
			return -1;
		}

		try {
			return Integer.valueOf(mCachedProperties.get(position).getPropertyId());
		}
		catch (java.lang.NumberFormatException e) {
			return position;
		}
	}

	public boolean isRowSelected(int position) {
		return position == mSelectedPosition && mHighlightSelectedPosition;
	}

	public boolean isRowExpandable(int position) {
		return false;
	}

	@Override
	public int getItemViewType(int position) {
		int ret = 0;

		if (isRowSelected(position)) {
			ret |= BIT_SELECTED;
		}

		if (isRowExpandable(position)) {
			ret |= BIT_EXPANDABLE;
		}

		return ret;
	}

	@Override
	public int getViewTypeCount() {
		return VIEW_TYPE_COUNT;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.section_hotel_summary, parent, false);
		}

		// If we're just measuring the height/width of the row,
		// just return the view without doing anything to it.
		if (mIsMeasuring) {
			return convertView;
		}

		HotelSummarySection section = (HotelSummarySection) convertView;
		Property property = (Property) getItem(position);

		boolean isSelected = isRowSelected(position);
		section.bind(property, mShouldShowVipIcon, mPriceTextSize,
				mShowDistance, mDistanceUnit, isSelected);

		return convertView;
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
