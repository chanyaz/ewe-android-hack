package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.Html;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.Filter.Sort;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.mobiata.android.util.ViewUtils;

public class HotelAdapter extends BaseAdapter implements OnMeasureListener {

	private Context mContext;
	private LayoutInflater mInflater;

	private SearchResponse mSearchResponse;

	private Property[] mCachedProperties;

	private boolean mIsMeasuring = false;
	private boolean mShowDistance = true;
	private boolean mIsSortedByUserRating = false;

	private float mSaleTextSize;

	private DistanceUnit mDistanceUnit;

	public HotelAdapter(Context context, SearchResponse searchResponse) {
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mSearchResponse = searchResponse;
		rebuildCache();

		// Calculate the size of the sale text size
		mSaleTextSize = ViewUtils.getTextSizeForMaxLines(context.getString(R.string.sale_caps), 1, 11,
				new TextPaint(), 25);
	}

	public void rebuildCache() {
		Log.d("Rebuilding hotel list adapter cache...");

		mCachedProperties = mSearchResponse.getFilteredAndSortedProperties();
		if (mCachedProperties.length == 0) {
			TrackingUtils.trackErrorPage(mContext, "FilteredToZeroResults");
		}

		mIsSortedByUserRating = (mSearchResponse.getFilter().getSort() == Sort.RATING);

		mDistanceUnit = mSearchResponse.getFilter().getDistanceUnit();

		final List<Property> properties = new ArrayList<Property>();
		properties.addAll(mSearchResponse.getProperties());

		final int size = mCachedProperties.length;
		for (int i = 0; i < size; i++) {
			properties.remove(mCachedProperties[i]);
		}

		for (Property property : properties) {
			Media thumbnail = property.getThumbnail();
			if (thumbnail != null && thumbnail.getUrl() != null) {
				ImageCache.removeImage(thumbnail.getUrl(), true);
			}
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
	public View getView(int position, View convertView, ViewGroup parent) {
		HotelViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_hotel, parent, false);

			holder = new HotelViewHolder();
			holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail_image_view);
			holder.saleImage = (ImageView) convertView.findViewById(R.id.sale_image_view);
			holder.saleLabel = (TextView) convertView.findViewById(R.id.sale_text_view);
			holder.name = (TextView) convertView.findViewById(R.id.name_text_view);
			holder.from = (TextView) convertView.findViewById(R.id.from_text_view);
			holder.price = (TextView) convertView.findViewById(R.id.price_text_view);
			holder.perNight = (TextView) convertView.findViewById(R.id.per_night_text_view);
			holder.highlyRatedImage = (ImageView) convertView.findViewById(R.id.highly_rated_image_view);
			holder.hotelRating = (RatingBar) convertView.findViewById(R.id.hotel_rating_bar);
			holder.userRating = (RatingBar) convertView.findViewById(R.id.user_rating_bar);
			holder.distance = (TextView) convertView.findViewById(R.id.distance_text_view);

			holder.saleLabel.setTextSize(mSaleTextSize);

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
		if (lowestRate.getSavingsPercent() > 0) {
			holder.from.setText(Html.fromHtml(
					mContext.getString(R.string.from_template,
							StrUtils.formatHotelPrice(lowestRate.getDisplayBaseRate())), null,
					new StrikethroughTagHandler()));
			holder.saleImage.setVisibility(View.VISIBLE);
			holder.saleLabel.setVisibility(View.VISIBLE);
		}
		else {
			holder.from.setText(R.string.from);
			holder.saleImage.setVisibility(View.GONE);
			holder.saleLabel.setVisibility(View.GONE);
		}

		holder.price.setText(StrUtils.formatHotelPrice(lowestRate.getDisplayRate()));

		if (Rate.showInclusivePrices()) {
			holder.perNight.setVisibility(View.GONE);
		}
		else {
			holder.perNight.setVisibility(View.VISIBLE);
		}

		holder.hotelRating.setRating((float) property.getHotelRating());
		holder.userRating.setRating((float) property.getAverageExpediaRating());
		if (mIsSortedByUserRating) {
			holder.hotelRating.setVisibility(View.INVISIBLE);
			holder.userRating.setVisibility(View.VISIBLE);
		}
		else {
			holder.hotelRating.setVisibility(View.VISIBLE);
			holder.userRating.setVisibility(View.INVISIBLE);
		}

		holder.distance.setText(property.getDistanceFromUser().formatDistance(mContext, mDistanceUnit));
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

		// See if this property is highly rated via TripAdvisor
		if (property.isHighlyRated()) {
			holder.highlyRatedImage.setVisibility(View.VISIBLE);
		}
		else {
			holder.highlyRatedImage.setVisibility(View.GONE);
		}

		return convertView;
	}

	public void trimDrawables(int start, int end) {
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
		public ImageView saleImage;
		public TextView saleLabel;
		public TextView name;
		public TextView from;
		public TextView price;
		public TextView perNight;
		public ImageView highlyRatedImage;
		public RatingBar hotelRating;
		public RatingBar userRating;
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
