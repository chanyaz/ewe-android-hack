package com.expedia.bookings.widget;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.ImageCache;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.Rate;
import com.mobiata.hotellib.data.SearchResponse;

public class HotelAdapter extends BaseAdapter {

	private static final int TYPE_FIRST = 0;
	private static final int TYPE_NOTFIRST = 1;

	private Context mContext;
	private LayoutInflater mInflater;

	private SearchResponse mSearchResponse;

	private Property[] mCachedProperties;

	public HotelAdapter(Context context, SearchResponse searchResponse) {
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mSearchResponse = searchResponse;

		mCachedProperties = mSearchResponse.getFilteredAndSortedProperties();
	}

	/**
	 * Checks that we have the latest set of sorts/filters on the data.  If not, notify
	 * that the dataset has changed and update the data.  Should be called before
	 * any method that uses mCachedProperties.
	 */
	public void checkCachedProperties() {
		if (mSearchResponse.filterChanged()) {
			mCachedProperties = mSearchResponse.getFilteredAndSortedProperties();
			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		checkCachedProperties();

		if (mCachedProperties != null) {
			return mCachedProperties.length;
		}

		return 0;
	}

	@Override
	public Object getItem(int position) {
		checkCachedProperties();
		return mCachedProperties[position];
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public long getItemId(int position) {
		checkCachedProperties();
		return Integer.valueOf(mCachedProperties[position].getPropertyId());
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return TYPE_FIRST;
		}
		return TYPE_NOTFIRST;
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

			// If this is the first row, then add extra margin to the top to account for the pulldown
			if (getItemViewType(position) == TYPE_FIRST) {
				convertView.setPadding(convertView.getPaddingLeft(),
						(int) mContext.getResources().getDimension(R.dimen.hotel_row_first_padding),
						convertView.getPaddingRight(), convertView.getPaddingBottom());
			}

			holder = new HotelViewHolder();
			holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail_image_view);
			holder.saleImage = (ImageView) convertView.findViewById(R.id.sale_image_view);
			holder.saleLabel = (TextView) convertView.findViewById(R.id.sale_text_view);
			holder.name = (TextView) convertView.findViewById(R.id.name_text_view);
			holder.from = (TextView) convertView.findViewById(R.id.from_text_view);
			holder.price = (TextView) convertView.findViewById(R.id.price_text_view);
			holder.hotelRating = (RatingBar) convertView.findViewById(R.id.hotel_rating_bar);
			holder.distance = (TextView) convertView.findViewById(R.id.distance_text_view);
			convertView.setTag(holder);
		}
		else {
			holder = (HotelViewHolder) convertView.getTag();
		}

		Property property = (Property) getItem(position);
		holder.thumbnail.setImageResource(R.drawable.ic_image_placeholder);
		holder.name.setText(property.getName());

		// We assume we have a lowest rate here; this may not be a safe assumption
		Rate lowestRate = property.getLowestRate();
		// Detect if the property is on sale, if it is do special things
		if (lowestRate.getSavingsPercent() > 0) {
			holder.from.setText(Html.fromHtml(
					mContext.getString(R.string.from_template, lowestRate.getAverageBaseRate().getFormattedMoney()),
					null, new StrikethroughTagHandler()));
			holder.saleImage.setVisibility(View.VISIBLE);
			holder.saleLabel.setVisibility(View.VISIBLE);
		}
		else {
			holder.from.setText(R.string.from);
			holder.saleImage.setVisibility(View.GONE);
			holder.saleLabel.setVisibility(View.GONE);
		}

		holder.price.setText(lowestRate.getAverageRate().getFormattedMoney());

		holder.hotelRating.setRating((float) property.getHotelRating());
		holder.distance.setText(property.getDistanceFromUser().formatDistance(mContext));

		// See if there's a first image; if there is, use that as the thumbnail
		if (property.getThumbnail() != null) {
			ImageCache.getInstance().loadImage(property.getThumbnail().getUrl(), holder.thumbnail);
		}

		return convertView;
	}

	private static class HotelViewHolder {
		public ImageView thumbnail;
		public ImageView saleImage;
		public TextView saleLabel;
		public TextView name;
		public TextView from;
		public TextView price;
		public RatingBar hotelRating;
		public TextView distance;
	}
}
