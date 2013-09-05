package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.Html;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.section.HotelSummarySection;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.mobiata.android.util.ViewUtils;

public class HotelAdapter extends BaseAdapter implements OnMeasureListener {

	// We implement a selected state for rows here.  The reason for this is that in touch mode, a ListView
	// does not have a "selected" state (unless you are in touch mode).  An alternative solution would have
	// been to use checkboxes/checked states, but I think we may someday want to use checkboxes for multiselect
	// so I don't want to block future functionality.
	private static final int ROW_NORMAL = 0;
	private static final int ROW_SELECTED = 1;

	private Context mContext;
	private LayoutInflater mInflater;

	private HotelSearchResponse mSearchResponse;

	private Property[] mCachedProperties;

	private boolean mIsMeasuring = false;
	private boolean mShowDistance = true;

	private DistanceUnit mDistanceUnit;

	private float mPriceTextSize;

	private int mSelectedPosition = -1;
	private boolean mHighlightSelectedPosition = true;

	private int mSaleTextColor;
	private int mStandardTextColor;

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

		mStandardTextColor = Ui.obtainThemeColor(activity, R.attr.hotelPriceStandardColor);
		mSaleTextColor = Ui.obtainThemeColor(activity, R.attr.hotelPriceSaleColor);
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

	private void rebuildCache() {
		Log.d("Rebuilding hotel list adapter cache...");

		if (mSearchResponse == null || mSearchResponse.getProperties() == null) {
			mCachedProperties = null;
		}
		else {
			mCachedProperties = mSearchResponse.getFilteredAndSortedProperties();
			final int size = mCachedProperties == null ? 0 : mCachedProperties.length;
			if (size == 0) {
				OmnitureTracking.trackErrorPage(mContext, "FilteredToZeroResults");
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

	public void setShowVipIcon(boolean showIcon) {
		mShouldShowVipIcon = showIcon;
		notifyDataSetChanged();
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

		try {
			return Integer.valueOf(mCachedProperties[position].getPropertyId());
		}
		catch (java.lang.NumberFormatException e) {
			return position;
		}
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
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.section_hotel_summary, parent, false);
		}

		// If we're just measuring the height/width of the row, just return the view without doing anything to it.
		if (mIsMeasuring) {
			return convertView;
		}

		HotelSummarySection section = (HotelSummarySection) convertView;
		Property property = (Property) getItem(position);
		boolean isSelected = getItemViewType(position) == ROW_SELECTED;
		section.bind(property, mSaleTextColor, mStandardTextColor, mShouldShowVipIcon,
				mPriceTextSize, mShowDistance, mDistanceUnit, isSelected);

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
