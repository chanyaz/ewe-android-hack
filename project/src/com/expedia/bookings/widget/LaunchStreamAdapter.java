package com.expedia.bookings.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.expedia.bookings.R;
import com.expedia.bookings.data.*;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.util.Ui;

public class LaunchStreamAdapter extends BaseAdapter {

	private final int NUM_PROPERTIES_DEFAULT = 10;

	private Context mContext;

	private Property[] mProperties;

	private Distance.DistanceUnit mDistanceUnit;

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
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		TileHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.row_launch_tile, parent, false);

			holder = new TileHolder();

			holder.mContainer = Ui.findView(convertView, R.id.launch_tile_container);
			holder.mTitleTextView = Ui.findView(convertView, R.id.launch_tile_title_text_view);
			holder.mDistanceTextView = Ui.findView(convertView, R.id.launch_tile_distance_text_view);
			holder.mPriceTextView = Ui.findView(convertView, R.id.launch_tile_price_text_view);

			convertView.setTag(holder);
		}
		else {
			holder = (TileHolder) convertView.getTag();
		}

		if (mHasRealProperties) {
			Property property = mProperties[position];

			holder.mTitleTextView.setText(property.getName());

			holder.mDistanceTextView.setText(property.getDistanceFromUser().formatDistance(mContext, mDistanceUnit,
					true));

			// We assume we have a lowest rate here; this may not be a safe assumption
			Rate lowestRate = property.getLowestRate();
			final String hotelPrice = StrUtils.formatHotelPrice(lowestRate.getDisplayRate());
			holder.mPriceTextView.setText(hotelPrice);

			holder.mContainer.setVisibility(View.VISIBLE);
		}
		else {
			holder.mContainer.setVisibility(View.INVISIBLE);
		}

		return convertView;
	}

	private class TileHolder {

		public RelativeLayout mContainer;
		public TextView mTitleTextView;
		public TextView mDistanceTextView;
		public TextView mPriceTextView;

	}
}
