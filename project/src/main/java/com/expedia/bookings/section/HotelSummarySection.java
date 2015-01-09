package com.expedia.bookings.section;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.text.Html;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.bitmaps.PaletteCallback;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.mobiata.android.util.AndroidUtils;

/**
 * Note: This is somewhat overloaded to be able to represent either an entire
 * leg or just one segment inside of a leg, depending on what data is bound
 * to it.
 */
public class HotelSummarySection extends RelativeLayout {

	private static final int[] DEFAULT_GRADIENT_COLORS = new int[] {
		0x00000000,
		0x40000000,
		0xa4000000,
	};
	private static final float[] DEFAULT_GRADIENT_POSITIONS = null; // Distribute the gradient colors evenly

	private static final int[] SELECTED_GRADIENT_COLORS = new int[] {
		0xb34180d9,
		0xb34180d9,
		0xba3d72bc,
		0xb33867a9,
	};
	private static final float[] SELECTED_GRADIENT_POSITIONS = new float[] {
		0f,
		0.28f,
		0.85f,
		1f,
	};

	public static final int ROOMS_LEFT_CUTOFF = 5;

	private static final int HOTEL_PRICE_TOO_LONG = 7;

	private static final String PICASSO_TAG = "hotel_list";

	// "ViewHolder" views
	private ImageView mThumbnailView;
	private ImageView mHotelBackgroundView;
	private View mVipView;
	private TextView mNameText;
	private TextView mStrikethroughPriceText;
	private TextView mPriceText;
	private TextView mSaleText;
	private ImageView mSaleImageView;
	private ViewGroup mAirAttachC;
	private TextView mAirAttachTv;
	private RatingBar mUserRatingBar;
	private TextView mNotRatedText;
	private TextView mProximityText;
	private TextView mSoldOutText;
	private TextView mUrgencyText;
	private boolean mDoUrgencyTextColorMatching = false;
	private View mCardCornersBottom;
	private View mBgImgOverlay;
	private View mSelectedOverlay;

	// Properties extracted from the view.xml
	private Drawable mUnselectedBackground;
	private Drawable mSelectedBackground;
	private int mSalePriceTextColor;
	private int mAirAttachPriceTextColor;
	private int mPriceTextColor;
	private boolean mIsSelected;

	public HotelSummarySection(Context context, AttributeSet attrs) {
		super(context, attrs);

		mUnselectedBackground = getBackground();

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.hotel_summary_section);
			mSelectedBackground = a.getDrawable(R.styleable.hotel_summary_section_selectedBackground);
			mSalePriceTextColor = a.getColor(R.styleable.hotel_summary_section_salePriceTextColor,
				R.color.hotel_price_sale_text_color);
			mAirAttachPriceTextColor = a.getColor(R.styleable.hotel_summary_section_airAttachPriceTextColor,
				R.color.hotel_price_air_attach_text_color);
			mPriceTextColor = a
				.getColor(R.styleable.hotel_summary_section_priceTextColor, R.color.hotel_price_text_color);
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
		mAirAttachC = Ui.findView(this, R.id.air_attach_sale_container);
		mAirAttachTv = Ui.findView(this, R.id.air_attach_sale_text_view);
		mUserRatingBar = Ui.findView(this, R.id.user_rating_bar);
		mNotRatedText = Ui.findView(this, R.id.not_rated_text_view);
		mProximityText = Ui.findView(this, R.id.proximity_text_view);
		mSoldOutText = Ui.findView(this, R.id.sold_out_text_view);
		mCardCornersBottom = Ui.findView(this, R.id.card_corners_bottom);
		mBgImgOverlay = Ui.findView(this, R.id.gradient_header_mask);

		// We'll fill mUrgencyText either from urgency_text_view or urgency_text_view_color_matched
		// and if it's from color_matched, then we know we'll need to do color matching later on.
		mUrgencyText = Ui.findView(this, R.id.urgency_text_view);
		if (mUrgencyText == null) {
			mUrgencyText = Ui.findView(this, R.id.urgency_text_view_color_matched);
			mDoUrgencyTextColorMatching = true;
		}

		mSelectedOverlay = Ui.findView(this, R.id.selected_hotel_overlay);
	}

	/**
	 * Convenience method for hotels shown in the trip bucket.
	 *
	 * @param property
	 * @param rate
	 */
	public void bindForTripBucket(Property property, Rate rate) {
		bind(property, rate, false, 16, false, DistanceUnit.MILES, false, true);
	}

	/**
	 * Binds the data from the passed Property to the views in this Section. Uses the lowest
	 * rate available for the property (property.getLowestRate()).
	 *
	 * @param property
	 * @param shouldShowVipIcon
	 * @param priceTextSize     interpreted as "scaled pixel" units
	 * @param showDistance
	 * @param distanceUnit
	 * @param isSelected
	 */
	public void bind(final Property property, boolean shouldShowVipIcon, float priceTextSize,
					 boolean showDistance, DistanceUnit distanceUnit, boolean isSelected) {

		Rate lowestRate = property.getLowestRate();
		bind(property, lowestRate, shouldShowVipIcon, priceTextSize, showDistance, distanceUnit, isSelected, false);
	}

	/**
	 * Binds the data from the passed Property & rate to the views in this Section.
	 *
	 * @param property
	 * @param rate
	 * @param shouldShowVipIcon
	 * @param priceTextSize     interpreted as "scaled pixel" units
	 * @param showDistance
	 * @param distanceUnit
	 * @param isSelected
	 */
	public void bind(final Property property, final Rate rate, boolean shouldShowVipIcon, float priceTextSize,
					 boolean showDistance, DistanceUnit distanceUnit, boolean isSelected, boolean showTotal) {
		final Context context = getContext();
		final Resources res = context.getResources();
		mIsSelected = isSelected;

		mNameText.setText(property.getName());

		Money thePrice = null;
		if (rate != null) {
			thePrice = showTotal ? rate.getDisplayTotalPrice() : rate.getDisplayPrice();
		}
		final String hotelPrice = thePrice == null ? "" : thePrice.getFormattedMoney(Money.F_NO_DECIMAL);

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
						new StrikethroughTagHandler()
					));
				}
				else {
					mStrikethroughPriceText.setVisibility(View.GONE);
				}

				mPriceText.setTextColor(mSalePriceTextColor);
				if (rate.isAirAttached()) {
					// Story #3586. Air Attach Phone - Search Results.
					if (!ExpediaBookingApp.useTabletInterface(getContext())) {
						if (mSaleImageView != null) {
							mSaleImageView.setVisibility(View.VISIBLE);
							mSaleImageView.setImageResource(R.drawable.bg_hotel_cell_sale_air_attach);
						}
						mPriceText.setTextColor(getResources().getColor(mAirAttachPriceTextColor));
						mSaleText.setVisibility(View.VISIBLE);
						mSaleText.setText(context.getString(R.string.percent_minus_template,
							(float) rate.getDiscountPercent()));
					}
					// Story #3512. Air Attach Tablet - Search Results.
					else if (mAirAttachC != null) {
						mSaleText.setVisibility(View.GONE);
						mAirAttachC.setVisibility(View.VISIBLE);
						mAirAttachTv.setText(context.getString(R.string.percent_minus_template,
							(float) rate.getDiscountPercent()));
					}
				}
				else {
					if (mAirAttachC != null) {
						mAirAttachC.setVisibility(View.GONE);
					}
					if (mSaleImageView != null) {
						mSaleImageView.setVisibility(View.VISIBLE);
						mSaleImageView.setImageResource(R.drawable.bg_hotel_cell_sale);
					}
					mSaleText.setVisibility(View.VISIBLE);
					mSaleText.setText(context.getString(R.string.percent_minus_template,
						(float) rate.getDiscountPercent()));
				}
			}

			// Story #790. Expedia's way of making it seem like they are offering a discount.
			else if (highestPriceFromSurvey != null
				&& (highestPriceFromSurvey.compareTo(lowestRate.getDisplayPrice()) > 0)) {
				mStrikethroughPriceText.setVisibility(View.VISIBLE);
				mStrikethroughPriceText.setText(Html.fromHtml(
					context.getString(R.string.strike_template,
						StrUtils.formatHotelPrice(highestPriceFromSurvey)), null,
					new StrikethroughTagHandler()
				));
				mSaleText.setVisibility(View.GONE);
				if (mSaleImageView != null) {
					mSaleImageView.setVisibility(View.GONE);
				}
				mPriceText.setTextColor(mPriceTextColor);
				if (mAirAttachC != null) {
					mAirAttachC.setVisibility(View.GONE);
				}
			}

			else {
				mStrikethroughPriceText.setVisibility(View.GONE);
				mPriceText.setTextColor(mPriceTextColor);
				mSaleText.setVisibility(View.GONE);
				if (mSaleImageView != null) {
					mSaleImageView.setVisibility(View.GONE);
				}
				if (mAirAttachC != null) {
					mAirAttachC.setVisibility(View.GONE);
				}
			}
		}

		int roomsLeft = property.getRoomsLeftAtThisRate();
		if (mUrgencyText != null) {
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

		if (mDoUrgencyTextColorMatching && mUrgencyText.getVisibility() == View.VISIBLE) {
			if (mIsSelected) {
				mUrgencyText.setSelected(true);
				setDominantColor(getResources().getColor(R.color.tablet_hotel_urgency_msg_selected_unpressed_overlay));
			}
			else {
				setDominantColor(getResources().getColor(R.color.transparent_dark));
			}
		}

		if (mSelectedOverlay != null && mIsSelected) {
			mSelectedOverlay.setVisibility(VISIBLE);
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

		// Sold out stuff
		HotelOffersResponse offersResponse = Db.getHotelSearch().getHotelOffersResponse(property.getPropertyId());
		boolean isSoldOut = offersResponse != null && offersResponse.getRateCount() == 0;
		if (isSoldOut) {
			setSoldOut();
		}
		else {
			if (mSoldOutText != null) {
				mSoldOutText.setVisibility(View.GONE);
			}
		}

		// See if there's a first image; if there is, use that as the thumbnail
		if (mThumbnailView != null) {
			int placeholderResId = Ui
				.obtainThemeResID(context, R.attr.skin_HotelRowThumbPlaceHolderDrawable);
			if (property.getThumbnail() != null) {
				//Picasso.with(context).load(property.getThumbnail().getBestUrls(250).get(0)).placeholder(placeholderResId).fit().centerCrop().into(mThumbnailView);
				property.getThumbnail().fillImageView(mThumbnailView, placeholderResId, null, PICASSO_TAG);
			}
			else {
				new PicassoHelper.Builder(mThumbnailView).setTag(PICASSO_TAG).build().load(placeholderResId);
			}
		}

		// There are 2 ways that we indicate that this hotel is selected:
		// 1. Changing this View's background drawable. This is what's used by the Phone UI.
		//    We'll detect this by testing whether mSelectedBackground exists.
		// 2. Setting/changing the gradient on the header bitmap. This is what we're using on
		//    the 2013 tablet UI.
		//    We'll assume this is what we want if we're not using #1.
		boolean useSelectedBackground = mSelectedBackground != null;
		boolean useHeaderGradient = !useSelectedBackground;

		if (mHotelBackgroundView != null) {

			if (isSoldOut) {
				ColorMatrix cm = new ColorMatrix();
				cm.setSaturation(0.0f);
				mHotelBackgroundView.setColorFilter(new ColorMatrixColorFilter(cm));
				mBgImgOverlay.setBackgroundResource(R.drawable.bg_hotel_row_tablet_sold_out_overlay);
			}
			else {
				mHotelBackgroundView.setColorFilter(null);
				mBgImgOverlay.setBackgroundResource(R.drawable.bg_hotel_details_header_gradient);
			}

			int placeholderResId = Ui
				.obtainThemeResID((Activity) context, R.attr.skin_HotelRowThumbPlaceHolderDrawable);
			if (property.getThumbnail() != null) {
				PaletteCallback callback = new PaletteCallback(mHotelBackgroundView) {
					@Override
					public void onSuccess(int vibrantColor) {
						setDominantColor(vibrantColor);
					}

					@Override
					public void onFailed() {
						if (mDoUrgencyTextColorMatching && !mIsSelected) {
							setDominantColor(getResources().getColor(R.color.transparent_dark));
						}
					}
				};
				property.getThumbnail().fillImageView(mHotelBackgroundView, placeholderResId, callback, PICASSO_TAG);
			}
			else {
				new PicassoHelper.Builder(mHotelBackgroundView).build().load(placeholderResId);
			}
		}

		// Set the background based on whether the row is selected or not
		setBackgroundDrawable(useSelectedBackground && isSelected ? mSelectedBackground : mUnselectedBackground);
	}

	private void setSoldOut() {
		if (ExpediaBookingApp.useTabletInterface(getContext())) {
			if (mSoldOutText != null) {
				mSoldOutText.setVisibility(View.VISIBLE);
			}
			if (mNotRatedText != null) {
				mNotRatedText.setVisibility(View.GONE);
			}
			if (mUserRatingBar != null) {
				mUserRatingBar.setVisibility(View.GONE);
			}
			if (mProximityText != null) {
				mProximityText.setVisibility(View.GONE);
			}
		}
		else {
			mProximityText.setVisibility(View.VISIBLE);
			mProximityText.setText(Ui.obtainThemeResID(getContext(), R.attr.skin_hotelSearchResultSoldOut));
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Async handling of Mobile Exclusive Deals / ColorScheme




	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setDominantColor(int color) {
		StateListDrawable stateListDrawable = new StateListDrawable();
		stateListDrawable.addState(new int[] {
			android.R.attr.state_pressed, android.R.attr.state_selected
		}, new ColorDrawable(getResources().getColor(R.color.tablet_hotel_urgency_msg_pressed_selected_overlay)));
		stateListDrawable.addState(new int[] {
			android.R.attr.state_pressed
		}, new ColorDrawable(getResources().getColor(R.color.tablet_hotel_urgency_msg_pressed_unselected_overlay)));
		stateListDrawable.addState(new int[] {
			android.R.attr.state_selected
		}, new ColorDrawable(getResources().getColor(R.color.tablet_hotel_urgency_msg_selected_unpressed_overlay)));

		stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(color));
		if (AndroidUtils.getSdkVersion() < Build.VERSION_CODES.JELLY_BEAN) {
			mUrgencyText.setBackgroundDrawable(stateListDrawable);
		}
		else {
			mUrgencyText.setBackground(stateListDrawable);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Show/hide MED callout
	@TargetApi(11)
	public void collapseBy(float pixels) {
		mUrgencyText.setTranslationY(-pixels);
		mCardCornersBottom.setTranslationY(-pixels);
	}


}
