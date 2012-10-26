package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.LaunchHotelData;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class LaunchHotelAdapter extends LaunchBaseAdapter<Property> {

	private static final String THUMBNAIL_SIZE = Media.IMAGE_BIG_SUFFIX;

	private Context mContext;
	private LayoutInflater mInflater;

	private Distance.DistanceUnit mDistanceUnit;

	private View[] mViewCache = new View[1];

	public LaunchHotelAdapter(Context context) {
		super(context, R.layout.row_launch_tile_hotel);
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setProperties(LaunchHotelData launchHotelData) {
		this.clear();

		mDistanceUnit = launchHotelData.getDistanceUnit();

		if (launchHotelData != null && launchHotelData.getProperties() != null) {
			for (Property property : launchHotelData.getProperties()) {
				add(property);
			}
		}

		mViewCache = new View[launchHotelData.getProperties().size()];

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
		return -1;
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

		View container = Ui.findView(view, R.id.launch_tile_container);
		TextView titleTextView = Ui.findView(view, R.id.launch_tile_title_text_view);
		FontCache.setTypeface(titleTextView, FontCache.Font.ROBOTO_LIGHT);

		TextView distanceTextView = Ui.findView(view, R.id.launch_tile_distance_text_view);
		FontCache.setTypeface(distanceTextView, FontCache.Font.ROBOTO_LIGHT);

		TextView priceTextView = Ui.findView(view, R.id.launch_tile_price_text_view);
		FontCache.setTypeface(priceTextView, FontCache.Font.ROBOTO_BOLD);

		// Bottom banner/label

		titleTextView.setText(property.getName());
		distanceTextView.setText(property.getDistanceFromUser().formatDistance(mContext, mDistanceUnit,
				true));

		Rate lowestRate = property.getLowestRate();
		final String hotelPrice = StrUtils.formatHotelPrice(lowestRate.getDisplayRate());
		priceTextView.setText(hotelPrice);

		TextView sale = Ui.findView(view, R.id.launch_tile_sale_text_view);

		// Sale
		if (property.isLowestRateTonightOnly()) {
			sale.setText(mContext.getString(R.string.percent_minus_template, lowestRate.getDiscountPercent()));
			sale.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tonight_only, 0, 0, 0);
			sale.setVisibility(View.VISIBLE);
		}
		else if (property.isLowestRateMobileExclusive()) {
			sale.setText(mContext.getString(R.string.percent_minus_template, lowestRate.getDiscountPercent()));
			sale.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mobile_only, 0, 0, 0);
			sale.setVisibility(View.VISIBLE);
		}
		else if (property.getLowestRate().isSaleTenPercentOrBetter()) {
			sale.setText(mContext.getString(R.string.percent_minus_template, lowestRate.getDiscountPercent()));
			sale.setVisibility(View.VISIBLE);
		}
		else {
			sale.setVisibility(View.GONE);
		}

		// Background image

		String url = property.getThumbnail().getUrl(THUMBNAIL_SIZE);
		if (ImageCache.containsImage(url)) {
			Log.i("imageContained: " + position + " url: " + url);
			container.setBackgroundDrawable(new BitmapDrawable(ImageCache.getImage(url)));
			container.setVisibility(View.VISIBLE);
		}
		else {
			Log.i("imageNotContained: " + position + " url: " + url);
			container.setVisibility(View.INVISIBLE);
			loadImageForLaunchStream(url, container);
		}

		// We're just using the Tag as a flag to indicate this view has been populated
		view.setTag(new Object());

		return view;
	}
}
