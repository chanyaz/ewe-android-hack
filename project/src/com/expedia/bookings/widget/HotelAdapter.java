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
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
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

	private static final int ROOMS_LEFT_CUTOFF = 5;

	private static final int HOTEL_PRICE_TOO_LONG = 7;

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

	private boolean mUseCondensedRows;

	private int mSaleTextColor;
	private int mStandardTextColor;
	private int mDefaultTextColor;

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
		mUseCondensedRows = LayoutUtils.isScreenNarrow(mContext);

		mDefaultTextColor = mContext.getResources().getColor(R.color.hotel_price_text_color);
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
				Rate rate = property.getLowestRate();
				if (rate != null) {
					String displayPrice = StrUtils.formatHotelPrice(rate.getDisplayPrice());
					if (longestPrice.length() < displayPrice.length()) {
						longestPrice = displayPrice;
					}
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
		HotelViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_hotel, parent, false);

			holder = new HotelViewHolder();
			holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail_image_view);
			holder.vip = (ImageView) convertView.findViewById(R.id.vip_image_view);
			holder.name = (TextView) convertView.findViewById(R.id.name_text_view);
			holder.strikethroughPrice = (TextView) convertView.findViewById(R.id.strikethrough_price_text_view);
			holder.price = (TextView) convertView.findViewById(R.id.price_text_view);
			holder.saleText = (TextView) convertView.findViewById(R.id.sale_text_view);
			holder.saleImage = (ImageView) convertView.findViewById(R.id.sale_image_view);
			holder.userRating = (RatingBar) convertView.findViewById(R.id.user_rating_bar);
			holder.notRatedText = (TextView) convertView.findViewById(R.id.not_rated_text_view);
			holder.proximity = (TextView) convertView.findViewById(R.id.proximity_text_view);
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
		if (lowestRate == null) {
			holder.strikethroughPrice.setVisibility(View.GONE);
			holder.price.setVisibility(View.GONE);
			holder.saleText.setVisibility(View.GONE);
			holder.saleImage.setVisibility(View.GONE);
		}
		else {
			Money highestPriceFromSurvey = property.getHighestPriceFromSurvey();
			final String hotelPrice = StrUtils.formatHotelPrice(lowestRate.getDisplayPrice());

			holder.price.setVisibility(View.VISIBLE);
			holder.price.setTextSize(mPriceTextSize);
			holder.price.setText(hotelPrice);

			// Detect if the property is on sale, if it is do special things
			if (lowestRate.isOnSale() && lowestRate.isSaleTenPercentOrBetter()) {
				if (hotelPrice.length() < HOTEL_PRICE_TOO_LONG) {
					holder.strikethroughPrice.setVisibility(View.VISIBLE);
					holder.strikethroughPrice.setText(Html.fromHtml(
							mContext.getString(R.string.strike_template,
									StrUtils.formatHotelPrice(lowestRate.getDisplayBasePrice())), null,
							new StrikethroughTagHandler()));
				}
				else {
					holder.strikethroughPrice.setVisibility(View.GONE);
				}

				holder.price.setTextColor(mSaleTextColor);
				holder.saleText.setVisibility(View.VISIBLE);
				holder.saleImage.setVisibility(View.VISIBLE);
				holder.saleText
						.setText(mContext.getString(R.string.percent_minus_template, lowestRate.getDiscountPercent()));
			}
			// Story #790. Expedia's way of making it seem like they are offering a discount.
			else if (highestPriceFromSurvey != null
					&& (highestPriceFromSurvey.compareTo(lowestRate.getDisplayPrice()) > 0)) {
				holder.strikethroughPrice.setVisibility(View.VISIBLE);
				holder.strikethroughPrice.setText(Html.fromHtml(
						mContext.getString(R.string.strike_template,
								StrUtils.formatHotelPrice(highestPriceFromSurvey)), null,
						new StrikethroughTagHandler()));
				holder.saleText.setVisibility(View.GONE);
				holder.saleImage.setVisibility(View.GONE);
				holder.price.setTextColor(mStandardTextColor);
			}
			else {
				holder.strikethroughPrice.setVisibility(View.GONE);
				holder.price.setTextColor(mStandardTextColor);
				holder.saleText.setVisibility(View.GONE);
				holder.saleImage.setVisibility(View.GONE);
			}
		}

		int roomsLeft = property.getRoomsLeftAtThisRate();
		// 1400. VSC - remove urgency messages throughout the app
		if (ExpediaBookingApp.IS_VSC) {
			holder.urgency.setVisibility(View.GONE);
		}
		else {
			if (property.isLowestRateTonightOnly()) {
				holder.urgency.setText(mContext.getString(R.string.tonight_only));
				holder.urgency.setVisibility(View.VISIBLE);
			}
			else if (property.isLowestRateMobileExclusive()) {
				holder.urgency.setText(mContext.getString(R.string.mobile_exclusive));
				holder.urgency.setVisibility(View.VISIBLE);
			}
			else if (roomsLeft > 0 && roomsLeft <= ROOMS_LEFT_CUTOFF) {
				holder.urgency.setText(mContext.getResources().getQuantityString(R.plurals.num_rooms_left, roomsLeft,
						roomsLeft));
				holder.urgency.setVisibility(View.VISIBLE);
			}
			else {
				holder.urgency.setVisibility(View.GONE);
			}

			if (holder.vip != null && mShouldShowVipIcon) {
				int visibility = property.isVipAccess() ? View.VISIBLE : View.INVISIBLE;
				holder.vip.setVisibility(visibility);
			}
		}

		holder.userRating.setRating((float) property.getAverageExpediaRating());
		if (holder.userRating.getRating() == 0) {
			holder.userRating.setVisibility(View.INVISIBLE);
			holder.notRatedText.setVisibility(View.VISIBLE);
		}
		else {
			holder.userRating.setVisibility(View.VISIBLE);
			holder.notRatedText.setVisibility(View.GONE);
		}

		if (mShowDistance && property.getDistanceFromUser() != null) {
			// Send true so as to use the "abbreviated" version, which has now become standard in 1.5
			holder.proximity.setText(property.getDistanceFromUser().formatDistance(mContext, mDistanceUnit, true));
		}
		else {
			holder.proximity.setText(property.getLocation().getDescription());
		}

		// See if there's a first image; if there is, use that as the thumbnail
		// Don't try to load the thumbnail if we're just measuring the height of the ListView
		boolean imageSet = false;
		if (holder.thumbnail != null && !mIsMeasuring && property.getThumbnail() != null) {
			String url = property.getThumbnail().getUrl();
			UrlBitmapDrawable.loadImageView(url, holder.thumbnail, Ui.obtainThemeResID((Activity)mContext, R.attr.HotelRowThumbPlaceHolderDrawable));
			imageSet = true;
		}
		if (holder.thumbnail != null && !imageSet) {
			holder.thumbnail.setImageResource(Ui.obtainThemeResID((Activity)mContext, R.attr.HotelRowThumbPlaceHolderDrawable));
		}

		// Set the background based on whether the row is selected or not
		if (getItemViewType(position) == ROW_SELECTED) {
			convertView.setBackgroundResource(Ui.obtainThemeResID(mContext, R.attr.tabletSelectedRowBackground));
		}
		else {
			convertView.setBackgroundResource(R.drawable.bg_row);
		}

		return convertView;
	}

	private static class HotelViewHolder {
		public ImageView thumbnail;
		public ImageView vip;
		public TextView name;
		public TextView strikethroughPrice;
		public TextView price;
		public TextView saleText;
		public ImageView saleImage;
		public RatingBar userRating;
		public TextView notRatedText;
		public TextView proximity;
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
