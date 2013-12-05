package com.expedia.bookings.section;

import android.app.Activity;
import android.content.Context;
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
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
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

	public void bind(Property property, boolean mShouldShowVipIcon, float mPriceTextSize,
			boolean mShowDistance, DistanceUnit mDistanceUnit, boolean isSelected) {
		Context context = getContext();

		mNameText.setText(property.getName());

		// We assume we have a lowest rate here; this may not be a safe assumption
		Rate lowestRate = property.getLowestRate();
		Money highestPriceFromSurvey = property.getHighestPriceFromSurvey();
		final String hotelPrice = lowestRate == null ? "" : StrUtils.formatHotelPrice(lowestRate.getDisplayPrice());

		if (lowestRate == null) {
			mStrikethroughPriceText.setVisibility(View.GONE);
			mPriceText.setTextColor(mPriceTextColor);
			mSaleText.setVisibility(View.GONE);
			if (mSaleImageView != null) {
				mSaleImageView.setVisibility(View.GONE);
			}
		}
		else {
			// Detect if the property is on sale, if it is do special things
			if (lowestRate.isOnSale() && lowestRate.isSaleTenPercentOrBetter()) {
				if (hotelPrice.length() < HOTEL_PRICE_TOO_LONG) {
					mStrikethroughPriceText.setVisibility(View.VISIBLE);
					mStrikethroughPriceText.setText(Html.fromHtml(
							context.getString(R.string.strike_template,
									StrUtils.formatHotelPrice(lowestRate.getDisplayBasePrice())), null,
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
				mSaleText
						.setText(context.getString(R.string.percent_minus_template, lowestRate.getDiscountPercent()));
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
		else {
			if (property.isLowestRateTonightOnly()) {
				mUrgencyText.setText(context.getString(R.string.tonight_only));
				mUrgencyText.setVisibility(View.VISIBLE);
			}
			else if (property.isLowestRateMobileExclusive()) {
				mUrgencyText.setText(context.getString(R.string.mobile_exclusive));
				mUrgencyText.setVisibility(View.VISIBLE);
			}
			else if (roomsLeft > 0 && roomsLeft <= ROOMS_LEFT_CUTOFF) {
				mUrgencyText.setText(context.getResources().getQuantityString(R.plurals.num_rooms_left, roomsLeft,
						roomsLeft));
				mUrgencyText.setVisibility(View.VISIBLE);
			}
			else {
				mUrgencyText.setVisibility(View.GONE);
			}

			if (mVipView != null && mShouldShowVipIcon) {
				int visibility = property.isVipAccess() ? View.VISIBLE : View.INVISIBLE;
				mVipView.setVisibility(visibility);
			}
		}

		mPriceText.setTextSize(mPriceTextSize);
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

		if (mShowDistance && property.getDistanceFromUser() != null) {
			// Send true so as to use the "abbreviated" version, which has now become standard in 1.5
			mProximityText.setText(property.getDistanceFromUser().formatDistance(context, mDistanceUnit, true));
		}
		else {
			mProximityText.setText(property.getLocation().getDescription());
		}

		// See if there's a first image; if there is, use that as the thumbnail
		if (mThumbnailView != null) {
			if (property.getThumbnail() != null) {
				String url = property.getThumbnail().getUrl();
				UrlBitmapDrawable.loadImageView(url, mThumbnailView,
						Ui.obtainThemeResID((Activity) context, R.attr.HotelRowThumbPlaceHolderDrawable));
			}
			else {
				mThumbnailView.setImageResource(Ui
						.obtainThemeResID((Activity) context, R.attr.HotelRowThumbPlaceHolderDrawable));
			}
		}

		if (mHotelBackgroundView != null && property.getThumbnail() != null) {
			HeaderBitmapDrawable headerBitmapDrawable = new HeaderBitmapDrawable();
			headerBitmapDrawable.setGradient(CARD_GRADIENT_COLORS, CARD_GRADIENT_POSITIONS);
			headerBitmapDrawable.setCornerMode(CornerMode.ALL);
			headerBitmapDrawable.setCornerRadius(context.getResources().getDimensionPixelSize(
					R.dimen.tablet_result_corner_radius));
			//TODO: headerBitmapDrawable.setOverlayDrawable(context.getResources().getDrawable(R.drawable.card_top_lighting));

			mHotelBackgroundView.setImageDrawable(headerBitmapDrawable);

			String url = property.getThumbnail().getUrl();
			headerBitmapDrawable.setUrlBitmapDrawable(new UrlBitmapDrawable(context.getResources(), url,
					Ui.obtainThemeResID((Activity) context, R.attr.HotelRowThumbPlaceHolderDrawable)));

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
