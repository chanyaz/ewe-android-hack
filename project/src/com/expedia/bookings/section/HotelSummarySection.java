package com.expedia.bookings.section;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.graphics.HeaderBitmapDrawable.CornerMode;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.text.StrikethroughTagHandler;

/**
 * Note: This is somewhat overloaded to be able to represent either an entire
 * leg or just one segment inside of a leg, depending on what data is bound
 * to it.
 */
public class HotelSummarySection extends RelativeLayout {

	private static final int[] CARD_GRADIENT_COLORS = new int[] { 0x00000000, 0x40000000, 0xa4000000 };

	private static final float[] CARD_GRADIENT_POSITIONS = null; // Distribute the gradient colors evenly

	private static final int ROOMS_LEFT_CUTOFF = 5;

	private static final int HOTEL_PRICE_TOO_LONG = 7;

	// "ViewHolder" views
	private ImageView mThumbnailView;
	private ImageView mHotelBackgroundView;
	private View mVipView;
	private TextView mNameText;
	private TextView mStrikethroughPriceText;
	private TextView mPriceText;
	private TextView mSaleText;
	private ImageView mSaleImageView;
	private RatingBar mUserRatingBar;
	private TextView mNotRatedText;
	private TextView mProximityText;
	private TextView mUrgencyText;

	// Properties extracted from the view.xml
	private Drawable mUnselectedBackground;
	private Drawable mSelectedBackground;
	private int mSalePriceTextColor;
	private int mPriceTextColor;

	public HotelSummarySection(Context context, AttributeSet attrs) {
		super(context, attrs);

		mUnselectedBackground = getBackground();

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.hotel_summary_section);
			mSelectedBackground = a.getDrawable(R.styleable.hotel_summary_section_selectedBackground);
			mSalePriceTextColor = a.getColor(R.styleable.hotel_summary_section_salePriceTextColor,
					R.color.hotel_price_sale_text_color);
			mPriceTextColor = a.getColor(R.styleable.hotel_summary_section_priceTextColor,
					R.color.hotel_price_text_color);
			a.recycle();
		}
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mThumbnailView = Ui.findView(this, R.id.thumbnail_image_view);
		mHotelBackgroundView = Ui.findView(this, R.id.hotel_background_view);
		mVipView = Ui.findView(this, R.id.vip_badge);
		mNameText = Ui.findView(this, R.id.name_text_view);
		mStrikethroughPriceText = Ui.findView(this, R.id.strikethrough_price_text_view);
		mPriceText = Ui.findView(this, R.id.price_text_view);
		mSaleText = Ui.findView(this, R.id.sale_text_view);
		mSaleImageView = Ui.findView(this, R.id.sale_image_view);
		mUserRatingBar = Ui.findView(this, R.id.user_rating_bar);
		mNotRatedText = Ui.findView(this, R.id.not_rated_text_view);
		mProximityText = Ui.findView(this, R.id.proximity_text_view);
		mUrgencyText = Ui.findView(this, R.id.urgency_text_view);
	}

	/**
	 * Convenience method for hotels shown in the trip bucket.
	 * @param property
	 * @param rate
	 */
	public void bindForTripBucket(Property property, Rate rate) {
		bind(property, rate, false, 16, false, DistanceUnit.MILES, false);
	}

	/**
	 * Binds the data from the passed Property to the views in this Section. Uses the lowest
	 * rate available for the property (property.getLowestRate()).
	 *
	 * @param property
	 * @param shouldShowVipIcon
	 * @param priceTextSize interpreted as "scaled pixel" units
	 * @param showDistance
	 * @param distanceUnit
	 * @param isSelected
	 */
	public void bind(final Property property, boolean shouldShowVipIcon, float priceTextSize,
					 boolean showDistance, DistanceUnit distanceUnit, boolean isSelected) {

		Rate lowestRate = property.getLowestRate();
		bind(property, lowestRate, shouldShowVipIcon, priceTextSize, showDistance, distanceUnit, isSelected);
	}

	/**
	 * Binds the data from the passed Property & rate to the views in this Section.
	 *
	 * @param property
	 * @param rate
	 * @param shouldShowVipIcon
	 * @param priceTextSize interpreted as "scaled pixel" units
	 * @param showDistance
	 * @param distanceUnit
	 * @param isSelected
	 */
	public void bind(final Property property, final Rate rate, boolean shouldShowVipIcon, float priceTextSize,
					 boolean showDistance, DistanceUnit distanceUnit, boolean isSelected) {
		final Context context = getContext();
		final Resources res = context.getResources();

		mNameText.setText(property.getName());

		final String hotelPrice = rate == null ? "" : StrUtils.formatHotelPrice(rate.getDisplayPrice());

		if (rate == null) {
			if (mStrikethroughPriceText != null) {
				mStrikethroughPriceText.setVisibility(View.GONE);
			}
			if (mPriceText != null) {
				mPriceText.setTextColor(mPriceTextColor);
			}
			if (mSaleText != null) {
				mSaleText.setVisibility(View.GONE);
			}
			if (mSaleImageView != null) {
				mSaleImageView.setVisibility(View.GONE);
			}
		}

		// mStrikethroughPriceText will always be null for tripbucket hotel item - tablet 4.0, so we just skip.
		else if (mStrikethroughPriceText != null) {
			Money highestPriceFromSurvey = property.getHighestPriceFromSurvey();
			Rate lowestRate = property.getLowestRate();

			// Detect if the property is on sale, if it is do special things
			if (rate.isOnSale() && rate.isSaleTenPercentOrBetter()) {
				if (hotelPrice.length() < HOTEL_PRICE_TOO_LONG) {
					mStrikethroughPriceText.setVisibility(View.VISIBLE);
					mStrikethroughPriceText.setText(Html.fromHtml(
							context.getString(R.string.strike_template,
									StrUtils.formatHotelPrice(rate.getDisplayBasePrice())), null,
							new StrikethroughTagHandler()));
				}
				else {
					mStrikethroughPriceText.setVisibility(View.GONE);
				}

				mPriceText.setTextColor(mSalePriceTextColor);
				mSaleText.setVisibility(View.VISIBLE);
				if (mSaleImageView != null) {
					mSaleImageView.setVisibility(View.VISIBLE);
				}
				mSaleText.setText(context.getString(R.string.percent_minus_template,
						rate.getDiscountPercent()));
			}

			// Story #790. Expedia's way of making it seem like they are offering a discount.
			else if (highestPriceFromSurvey != null
					&& (highestPriceFromSurvey.compareTo(lowestRate.getDisplayPrice()) > 0)) {
				mStrikethroughPriceText.setVisibility(View.VISIBLE);
				mStrikethroughPriceText.setText(Html.fromHtml(
						context.getString(R.string.strike_template,
								StrUtils.formatHotelPrice(highestPriceFromSurvey)), null,
						new StrikethroughTagHandler()));
				mSaleText.setVisibility(View.GONE);
				if (mSaleImageView != null) {
					mSaleImageView.setVisibility(View.GONE);
				}
				mPriceText.setTextColor(mPriceTextColor);
			}

			else {
				mStrikethroughPriceText.setVisibility(View.GONE);
				mPriceText.setTextColor(mPriceTextColor);
				mSaleText.setVisibility(View.GONE);
				if (mSaleImageView != null) {
					mSaleImageView.setVisibility(View.GONE);
				}
			}
		}

		int roomsLeft = property.getRoomsLeftAtThisRate();
		// 1400. VSC - remove urgency messages throughout the app
		if (ExpediaBookingApp.IS_VSC) {
			mUrgencyText.setVisibility(View.GONE);
		}
		// mUrgencyText will always be null for tripbucket hotel item - tablet 4.0, so we just skip.
		else if (mUrgencyText != null) {
			if (property.isLowestRateTonightOnly()) {
				mUrgencyText.setText(context.getString(R.string.tonight_only));
				mUrgencyText.setVisibility(View.VISIBLE);
			}
			else if (property.isLowestRateMobileExclusive()) {
				mUrgencyText.setText(context.getString(R.string.mobile_exclusive));
				mUrgencyText.setVisibility(View.VISIBLE);
			}
			else if (roomsLeft > 0 && roomsLeft <= ROOMS_LEFT_CUTOFF) {
				mUrgencyText.setText(res.getQuantityString(R.plurals.num_rooms_left, roomsLeft, roomsLeft));
				mUrgencyText.setVisibility(View.VISIBLE);
			}
			else {
				mUrgencyText.setVisibility(View.GONE);
			}

			if (mVipView != null && shouldShowVipIcon) {
				int visibility = property.isVipAccess() ? View.VISIBLE : View.INVISIBLE;
				mVipView.setVisibility(visibility);
			}
		}

		mPriceText.setTextSize(priceTextSize);
		mPriceText.setText(hotelPrice);

		mUserRatingBar.setRating((float) property.getAverageExpediaRating());
		if (mUserRatingBar.getRating() == 0f) {
			mUserRatingBar.setVisibility(View.GONE);
			mNotRatedText.setVisibility(View.VISIBLE);
		}
		else {
			mUserRatingBar.setVisibility(View.VISIBLE);
			mNotRatedText.setVisibility(View.GONE);
		}

		if (showDistance && property.getDistanceFromUser() != null) {
			// Send true so as to use the "abbreviated" version, which has now become standard in 1.5
			mProximityText.setText(property.getDistanceFromUser().formatDistance(context, distanceUnit, true));
		}
		else {
			mProximityText.setText(property.getLocation().getDescription());
		}

		// See if there's a first image; if there is, use that as the thumbnail
		if (mThumbnailView != null) {
			int placeholderResId = Ui.obtainThemeResID((Activity) context, R.attr.HotelRowThumbPlaceHolderDrawable);
			if (property.getThumbnail() != null) {
				property.getThumbnail().fillImageView(mThumbnailView, placeholderResId);
			}
			else {
				mThumbnailView.setImageResource(placeholderResId);
			}
		}

		if (mHotelBackgroundView != null && property.getThumbnail() != null) {
			final HeaderBitmapDrawable headerBitmapDrawable = new HeaderBitmapDrawable();
			headerBitmapDrawable.setGradient(CARD_GRADIENT_COLORS, CARD_GRADIENT_POSITIONS);
			headerBitmapDrawable.setCornerMode(CornerMode.ALL);
			headerBitmapDrawable.setCornerRadius(res.getDimensionPixelSize(R.dimen.tablet_result_corner_radius));
			//TODO: headerBitmapDrawable.setOverlayDrawable(res.getDrawable(R.drawable.card_top_lighting));

			int placeholderResId = Ui.obtainThemeResID((Activity) context, R.attr.HotelRowThumbPlaceHolderDrawable);
			property.getThumbnail().fillHeaderBitmapDrawable(mHotelBackgroundView, headerBitmapDrawable, placeholderResId);
		}

		// Set the background based on whether the row is selected or not
		if (isSelected && mSelectedBackground != null) {
			setBackgroundDrawable(mSelectedBackground);
		}
		else {
			setBackgroundDrawable(mUnselectedBackground);
		}
	}
}
