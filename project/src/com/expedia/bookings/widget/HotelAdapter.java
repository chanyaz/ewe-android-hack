package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

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
import com.expedia.bookings.tracking.TrackingUtils;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.mobiata.hotellib.data.Media;
import com.mobiata.hotellib.data.Money;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.Rate;
import com.mobiata.hotellib.data.SearchResponse;

public class HotelAdapter extends BaseAdapter implements OnMeasureListener {

	private static final int TYPE_FIRST = 0;
	private static final int TYPE_NOTFIRST = 1;

	private Context mContext;
	private LayoutInflater mInflater;
	private ImageCache mImageCache;

	private SearchResponse mSearchResponse;

	private Property[] mCachedProperties;

	private boolean mIsMeasuring = false;

	public HotelAdapter(Context context, SearchResponse searchResponse) {
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mImageCache = ImageCache.getInstance();

		mSearchResponse = searchResponse;
		rebuildCache();
	}

	public void rebuildCache() {
		Log.d("Rebuilding hotel list adapter cache...");

		mCachedProperties = mSearchResponse.getFilteredAndSortedProperties();
		if (mCachedProperties.length == 0) {
			TrackingUtils.trackErrorPage(mContext, "FilteredToZeroResults");
		}

		final List<Property> properties = new ArrayList<Property>();
		properties.addAll(mSearchResponse.getProperties());

		final int size = mCachedProperties.length;
		for (int i = 0; i < size; i++) {
			properties.remove(mCachedProperties[i]);
		}

		for (Property property : properties) {
			Media thumbnail = property.getThumbnail();
			if (thumbnail != null && thumbnail.getUrl() != null) {
				mImageCache.removeImage(thumbnail.getUrl(), true);
			}
		}

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
			holder.highlyRated = (TextView) convertView.findViewById(R.id.highly_rated_text_view);
			holder.price = (TextView) convertView.findViewById(R.id.price_text_view);
			holder.highlyRatedImage = (ImageView) convertView.findViewById(R.id.highly_rated_image_view);
			holder.hotelRating = (RatingBar) convertView.findViewById(R.id.hotel_rating_bar);
			holder.distance = (TextView) convertView.findViewById(R.id.distance_text_view);
			convertView.setTag(holder);
		}
		else {
			holder = (HotelViewHolder) convertView.getTag();
		}

		Property property = (Property) getItem(position);
		holder.name.setText(property.getName());

		// We assume we have a lowest rate here; this may not be a safe assumption
		Rate lowestRate = property.getLowestRate();
		// Detect if the property is on sale, if it is do special things
		if (lowestRate.getSavingsPercent() > 0) {
			holder.from
					.setText(Html.fromHtml(mContext.getString(R.string.from_template, lowestRate.getAverageBaseRate()
							.getFormattedMoney(Money.F_NO_DECIMAL + Money.F_ROUND_DOWN)), null,
							new StrikethroughTagHandler()));
			holder.saleImage.setVisibility(View.VISIBLE);
			holder.saleLabel.setVisibility(View.VISIBLE);
		}
		else {
			holder.from.setText(R.string.from);
			holder.saleImage.setVisibility(View.GONE);
			holder.saleLabel.setVisibility(View.GONE);
		}

		holder.price.setText(lowestRate.getAverageRate().getFormattedMoney(Money.F_NO_DECIMAL + Money.F_ROUND_DOWN));

		holder.hotelRating.setRating((float) property.getHotelRating());
		holder.distance.setText(property.getDistanceFromUser().formatDistance(mContext));

		// See if there's a first image; if there is, use that as the thumbnail
		// Don't try to load the thumbnail if we're just measuring the height of the ListView
		boolean imageSet = false;
		if (!mIsMeasuring && property.getThumbnail() != null) {
			String url = property.getThumbnail().getUrl();
			if (mImageCache.containsImage(url)) {
				holder.thumbnail.setImageBitmap(mImageCache.getImage(url));
				imageSet = true;
			}
			else {
				mImageCache.loadImage(url, holder.thumbnail);
			}
		}
		if (!imageSet) {
			holder.thumbnail.setImageResource(R.drawable.ic_row_thumb_placeholder);
		}

		// See if this property is highly rated via TripAdvisor
		if (property.isHighlyRated()) {
			holder.highlyRated.setVisibility(View.VISIBLE);
			holder.highlyRatedImage.setVisibility(View.VISIBLE);
		}
		else {
			holder.highlyRated.setVisibility(View.GONE);
			holder.highlyRatedImage.setVisibility(View.GONE);
		}

		return convertView;
	}

	public void trimDrawables(int start, int end) {
		final int size = mCachedProperties.length;
		for (int i = 0; i < size; i++) {
			if (i < start || i > end) {
				Property property = mCachedProperties[i];
				Media thumbnail = property.getThumbnail();
				if (thumbnail != null && thumbnail.getUrl() != null) {
					mImageCache.removeImage(thumbnail.getUrl(), true);
				}
			}
		}
	}

	private static class HotelViewHolder {
		public ImageView thumbnail;
		public ImageView saleImage;
		public TextView saleLabel;
		public TextView name;
		public TextView from;
		public TextView highlyRated;
		public TextView price;
		public ImageView highlyRatedImage;
		public RatingBar hotelRating;
		public TextView distance;
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
