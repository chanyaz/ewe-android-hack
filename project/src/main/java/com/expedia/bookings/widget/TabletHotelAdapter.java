package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.section.HotelSummarySection;
import com.mobiata.android.util.ViewUtils;

/**
 * Most of the functionality extends from HotelAdapter. This class
 * just contains handling that is specific to tablet.
 */
public class TabletHotelAdapter extends HotelAdapter {

	LayoutInflater mInflater;
	Context mContext;

	float mCollapseNewViewsPercent = 0f;

	private int mCardHeight;
	private int mMedContainerHeight;

	public TabletHotelAdapter(Activity activity) {
		super(activity);
		mInflater = LayoutInflater.from(activity);
		mContext = activity;

		Resources res = mContext.getResources();
		mCardHeight = res.getDimensionPixelSize(R.dimen.hotel_flight_card_height);
		mMedContainerHeight = res.getDimensionPixelSize(R.dimen.hotel_row_med_container_height);
	}

	/**
	 * Will return true if the row at position [position] is expandable. In this case, it means
	 * if there are Mobile Exclusive Deals or other Urgency Messages for this hotel.
	 *
	 * @param position
	 * @return
	 */
	@Override
	public boolean isRowExpandable(int position) {
		// The logic here should basically mirror what's in HotelSummarySection
		Property property = (Property) getItem(position);
		if (property.isSponsored()) {
			return true;
		}
		if (property.isLowestRateTonightOnly()) {
			return true;
		}

		if (property.isLowestRateMobileExclusive()) {
			return true;
		}

		int roomsLeft = property.getRoomsLeftAtThisRate();
		if (roomsLeft > 0 && roomsLeft <= HotelSummarySection.ROOMS_LEFT_CUTOFF) {
			return true;
		}

		return false;
	}

	/**
	 * Returns the estimated height for the view at position [position].
	 *
	 * @param position
	 * @return
	 */
	public int estimateViewHeight(int position) {
		int height = mCardHeight;
		if (isRowExpandable(position)) {
			height += mMedContainerHeight;
		}
		return height;
	}

	/**
	 * Returns the number of pixels by which the row at position [position] expands
	 * (if it's expandable at all).
	 *
	 * @param position
	 * @return
	 */
	public int estimateExpandableHeight(int position) {
		return isRowExpandable(position)
			? mMedContainerHeight
			: 0;
	}

	/**
	 * Returns the number of pixels by which the entire row at position [position] should
	 * be translated upwards due to the percentage by which the rows are collapsed. This algorithm
	 * is O(n); not the fastest but reasonbly fast for our situation (we should only be performing
	 * a calculation on the first few rows).
	 *
	 * @param position
	 * @return
	 */
	public int estimateExpandableOffset(int position) {
		int offset = 0;

		if (mCollapseNewViewsPercent != 0f) {
			for (int i = 0; i < position; i++) {
				offset += estimateExpandableHeight(i) * mCollapseNewViewsPercent;
			}
		}

		return offset;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.section_hotel_summary_tablet, parent, false);
		}

		HotelSummarySection section = (HotelSummarySection) super.getView(position, convertView, parent);

		section.collapseBy(mCollapseNewViewsPercent * estimateExpandableHeight(position));
		section.setTranslationY(-estimateExpandableOffset(position));

		return section;
	}

	@Override
	public float determinePriceTextSize(String longestPrice) {
		Resources r = mContext.getResources();
		DisplayMetrics dm = r.getDisplayMetrics();
		float defaultTextSize = r.getDimension(R.dimen.tablet_result_row_price_size) / dm.scaledDensity;
		float maxViewWidthdp = r.getDimension(R.dimen.tablet_result_row_price_max_width) / dm.density;
		TextPaint textPaint = new TextPaint();
		textPaint.setTypeface(Typeface.DEFAULT);
		return ViewUtils.getTextSizeForMaxLines(mContext, longestPrice, textPaint,
			maxViewWidthdp, 1, defaultTextSize, 5);
	}

	public void collapseNewViewsBy(float percentage) {
		mCollapseNewViewsPercent = percentage;
	}
}
