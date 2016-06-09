package com.expedia.bookings.widget;

import java.text.SimpleDateFormat;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelMedia;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.trips.TripBucketItemHotel;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.graphics.HeaderBitmapDrawable.CornerMode;
import com.expedia.bookings.section.HotelReceiptExtraSection;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.phrase.Phrase;

public class HotelReceipt extends LinearLayout {
	public interface OnSizeChangedListener {
		void onReceiptSizeChanged(int w, int h, int oldw, int oldh);

		void onMiniReceiptSizeChanged(int w, int h, int oldw, int oldh);
	}

	public interface OnViewMapClickListener {
		void onViewMapClicked();
	}

	public HotelReceipt(Context context) {
		this(context, null);
	}

	public HotelReceipt(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HotelReceipt(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.widget_hotel_receipt, this);
	}

	private OnSizeChangedListener mOnSizeChangedListener;
	private OnClickListener mRateBreakdownClickListener;
	private OnViewMapClickListener mMapClickListener;

	private ImageView mHeaderImageView;
	private TextView mRoomTypeDesciptionTextView;
	private TextView mBedTypeNameTextView;
	private ViewGroup mRoomAddressLayout;
	private View mRoomLongDescriptionDivider;
	private TextView mRoomLongDescriptionTextView;
	private TextView mRoomAddressTextView;
	private ViewGroup mExtrasLayout;
	private View mExtrasDivider;

	private FrameLayout mMiniReceipt;
	private ViewGroup mMiniReceiptLoading;
	private ViewGroup mMiniReceiptDetails;
	private TextView mNightsTextView;
	private TextView mDateRangeTextView;
	private TextView mGuestsTextView;
	private TextView mPriceTextView;
	private TextView mGrandTotalTextView;

	private boolean mIsABTestViewMapClicked;

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		mHeaderImageView = Ui.findView(this, R.id.header_image_view);
		mRoomTypeDesciptionTextView = Ui.findView(this, R.id.room_type_description_text_view);
		mBedTypeNameTextView = Ui.findView(this, R.id.bed_type_name_text_view);
		mRoomLongDescriptionDivider = Ui.findView(this, R.id.room_long_description_divider);
		mRoomLongDescriptionTextView = Ui.findView(this, R.id.room_long_description_text_view);
		mRoomAddressTextView = Ui.findView(this, R.id.room_address_text_view);
		mRoomAddressLayout = Ui.findView(this, R.id.room_address_layout);
		TextView viewMapTextButton = Ui.findView(this, R.id.hotel_receipt_view_map_textbutton);

		mExtrasLayout = Ui.findView(this, R.id.extras_layout);
		mExtrasDivider = Ui.findView(this, R.id.extras_divider);

		mMiniReceipt = Ui.findView(this, R.id.mini_receipt_layout);
		mMiniReceipt.setOnSizeChangedListener(mMiniReceiptOnSizeChangedListener);

		mMiniReceiptLoading = Ui.findView(this, R.id.mini_receipt_loading);
		mMiniReceiptDetails = Ui.findView(this, R.id.mini_receipt_details);

		mNightsTextView = Ui.findView(this, R.id.nights_text);
		mDateRangeTextView = Ui.findView(this, R.id.date_range_text);
		mGuestsTextView = Ui.findView(this, R.id.guests_text);
		mPriceTextView = Ui.findView(this, R.id.price_text);
		mGrandTotalTextView = Ui.findView(this, R.id.grand_total_text);

		viewMapTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mMapClickListener != null) {
					mMapClickListener.onViewMapClicked();
					mIsABTestViewMapClicked = true;
				}
			}
		});
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (mOnSizeChangedListener != null) {
			mOnSizeChangedListener.onReceiptSizeChanged(w, h, oldw, oldh);
		}
	}

	private final FrameLayout.OnSizeChangedListener mMiniReceiptOnSizeChangedListener = new FrameLayout.OnSizeChangedListener() {
		@Override
		public void onSizeChanged(int w, int h, int oldw, int oldh) {
			if (mOnSizeChangedListener != null) {
				mOnSizeChangedListener.onMiniReceiptSizeChanged(w, h, oldw, oldh);
			}
		}
	};

	private static String getAvailableLongDesc(TripBucketItemHotel hotel) {
		String ret = null;
		if (!TextUtils.isEmpty(hotel.getRate().getRoomLongDescription())) {
			ret = hotel.getRate().getRoomLongDescription();
		}
		else if (hotel.getOldRate() != null && !TextUtils.isEmpty(hotel.getOldRate().getRoomLongDescription())) {
			ret = hotel.getOldRate().getRoomLongDescription();
		}
		return ret;
	}

	public void bind(boolean showMiniReceipt, TripBucketItemHotel hotel) {
		HotelSearchParams params = hotel.getHotelSearchParams();
		Rate rate = hotel.getRate();

		HeaderBitmapDrawable headerBitmapDrawable = new HeaderBitmapDrawable();
		headerBitmapDrawable.setCornerMode(CornerMode.TOP);
		headerBitmapDrawable.setCornerRadius(getResources().getDimensionPixelSize(R.dimen.itin_card_corner_radius));
		mHeaderImageView.setImageDrawable(headerBitmapDrawable);

		HotelMedia hotelMedia = HotelUtils.getRoomMedia(hotel);
		int placeholderResId = Ui.obtainThemeResID((Activity) getContext(), R.attr.skin_hotelImagePlaceHolderDrawable);
		if (hotelMedia != null) {
			new PicassoHelper.Builder(getContext()).setPlaceholder(placeholderResId)
				.setTarget(headerBitmapDrawable.getCallBack()).build().load(hotelMedia.getHighResUrls());
		}
		else {
			headerBitmapDrawable.setBitmap(BitmapFactory.decodeResource(getResources(), placeholderResId));
		}

		mRoomTypeDesciptionTextView.setText(rate.getRoomDescription());
		mBedTypeNameTextView.setText(rate.getFormattedBedNames());
		String roomLongDesc = getAvailableLongDesc(hotel);

		// 4764 - AB Test: Add address/link to map overlay on Hotel CKO
		boolean isUserBucketedInTest = Db.getAbacusResponse()
			.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelShowAddressMapInReceipt);

		if (isUserBucketedInTest) {
			mRoomAddressLayout.setVisibility(VISIBLE);
			Location hotelLocation = hotel.getProperty().getLocation();
			mRoomAddressTextView
				.setText(hotelLocation.getStreetAddressString() + "\n" + hotelLocation.toShortFormattedString());
		}

		if (TextUtils.isEmpty(roomLongDesc)) {
			mRoomLongDescriptionDivider.setVisibility(View.GONE);
			mRoomLongDescriptionTextView.setVisibility(View.GONE);
		}
		else {
			mRoomLongDescriptionDivider.setVisibility(View.VISIBLE);
			mRoomLongDescriptionTextView.setVisibility(View.VISIBLE);
			mRoomLongDescriptionTextView.setText(roomLongDesc);

			// #817. Let user tap to expand or contract the room description text.
			mRoomLongDescriptionTextView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mRoomLongDescriptionTextView.getEllipsize() != null) {
						mRoomLongDescriptionTextView.setEllipsize(null);
						mRoomLongDescriptionTextView.setMaxLines(Integer.MAX_VALUE);
					}
					else {
						mRoomLongDescriptionTextView.setEllipsize(TruncateAt.END);
						mRoomLongDescriptionTextView.setMaxLines(5);
					}
				}
			});
		}

		mExtrasLayout.removeAllViews();
		if (PointOfSale.getPointOfSale().showFTCResortRegulations() &&
			hotel.getRate().showResortFeesMessaging()) {
			addResortFeeRows(hotel.getRate());
			mGrandTotalTextView.setText(getResources().getString(R.string.trip_total));
			mPriceTextView.setText(rate.getTotalPriceWithMandatoryFees().getFormattedMoney());
		}
		else if (rate.isPayLater()) {
			addPayLaterRow(rate);
			mGrandTotalTextView.setText(getResources().getString(R.string.total_with_tax));
			mPriceTextView.setText(rate.getDisplayTotalPrice().getFormattedMoney());
		}
		else {
			mGrandTotalTextView.setText(getResources().getString(R.string.total_with_tax));
			mPriceTextView.setText(rate.getDisplayTotalPrice().getFormattedMoney());
		}
		addPrioritizedAmenityRows(rate);

		final Resources res = getContext().getResources();

		int numNights = params.getStayDuration();
		String numNightsString = res.getQuantityString(R.plurals.number_of_nights, numNights, numNights);
		mNightsTextView.setText(numNightsString);

		mDateRangeTextView.setText(getFormattedDateRange(params));

		int numberOfGuests = params.getNumAdults() + params.getNumChildren();
		mGuestsTextView.setText(res.getQuantityString(R.plurals.number_of_guests, numberOfGuests, numberOfGuests));

		if (showMiniReceipt) {
			mMiniReceiptLoading.setVisibility(View.VISIBLE);
			mMiniReceiptDetails.setVisibility(View.VISIBLE);

			Animator fadeout = AnimUtils.createFadeAnimator(mMiniReceiptLoading, false);
			Animator fadein = AnimUtils.createFadeAnimator(mMiniReceiptDetails, true);
			Animator crossfade = AnimUtils.playTogether(fadeout, fadein);
			crossfade.addListener(new AnimatorListener() {
				@Override
				public void onAnimationCancel(Animator anim) {
					this.onAnimationEnd(anim);
				}

				@Override
				public void onAnimationEnd(Animator anim) {
					mMiniReceiptLoading.setVisibility(View.INVISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animator anim) {
					// ignore
				}

				@Override
				public void onAnimationStart(Animator anim) {
					// ignore
				}
			});

			// Let's not show the crossfade animation when they are coming back from map view AB test.
			if (!mIsABTestViewMapClicked) {
				crossfade.start();
			}

			mMiniReceipt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mRateBreakdownClickListener != null) {
						mRateBreakdownClickListener.onClick(v);
					}
				}
			});
		}
		else {
			// We never go backwards so don't bother with animation here
			mMiniReceiptLoading.setVisibility(View.VISIBLE);
			mMiniReceiptDetails.setVisibility(View.INVISIBLE);

			mMiniReceipt.setOnClickListener(null);
		}
	}

	private String getFormattedDateRange(HotelSearchParams params) {
		//A little hacky: use the DateFormat to set the date order and set dividers, then just remove the year.
		String yearlessShortPattern = ((SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT))
			.toPattern().replaceAll("\\W?[Yy]+\\W?", "");
		DateTimeFormatter dtf = DateTimeFormat.forPattern(yearlessShortPattern);
		CharSequence from = dtf.print(params.getCheckInDate());
		CharSequence to = dtf.print(params.getCheckOutDate());
		String rangeString = getContext().getString(R.string.date_range_TEMPLATE, from, to);
		return "(" + rangeString + ")";
	}

	private void addExtraRow(int stringId, boolean addToTop) {
		addExtraRow(getContext().getString(stringId), addToTop);
	}

	private void addExtraRow(CharSequence label, boolean addToTop) {
		mExtrasLayout.setVisibility(View.VISIBLE);
		mExtrasDivider.setVisibility(View.VISIBLE);
		HotelReceiptExtraSection extraRow = Ui
			.inflate(R.layout.snippet_hotel_receipt_price_extra, mExtrasLayout, false);
		extraRow.bind(label, null);
		if (addToTop) {
			mExtrasLayout.addView(extraRow, 0);
		}
		else {
			mExtrasLayout.addView(extraRow);
		}
	}

	private void addResortFeeRows(Rate rate) {
		mExtrasLayout.setVisibility(View.VISIBLE);
		mExtrasDivider.setVisibility(View.VISIBLE);

		HotelReceiptExtraSection resortFeesRow = Ui
			.inflate(R.layout.snippet_hotel_receipt_price_extra, mExtrasLayout, false);
		String feesPaidAtHotel = getResources().getString(R.string.fees_paid_at_hotel);
		resortFeesRow.bind(feesPaidAtHotel, rate.getTotalMandatoryFees().getFormattedMoney());
		mExtrasLayout.addView(resortFeesRow);

		if (rate.isPayLater()) {
			addPayLaterRow(rate);
		}
		else {
			HotelReceiptExtraSection dueToExpediaRow = Ui
				.inflate(R.layout.snippet_hotel_receipt_price_extra, mExtrasLayout, false);

			String totalDueToOurBrandToday = Phrase.from(this, R.string.due_to_brand_today_TEMPLATE)
				.put("brand", ProductFlavorFeatureConfiguration.getInstance().getPOSSpecificBrandName(getContext()))
				.format()
				.toString();
			dueToExpediaRow.bind(totalDueToOurBrandToday, rate.getTotalAmountAfterTax().getFormattedMoney());
			mExtrasLayout.addView(dueToExpediaRow);
		}
	}

	private void addPayLaterRow(Rate rate) {
		mExtrasLayout.setVisibility(View.VISIBLE);
		mExtrasDivider.setVisibility(View.VISIBLE);

		HotelReceiptExtraSection dueToOurBrandRow = Ui
			.inflate(R.layout.snippet_hotel_receipt_price_extra, mExtrasLayout, false);
		String totalDueToOurBrandToday = Phrase.from(this, R.string.due_to_brand_today_TEMPLATE)
			.put("brand", ProductFlavorFeatureConfiguration.getInstance().getPOSSpecificBrandName(getContext()))
			.format()
			.toString();
		dueToOurBrandRow.bind(totalDueToOurBrandToday, rate.getDepositAmount().getFormattedMoney());

		if (rate.getDepositAmount().isZero()) {
			TextView labelView = (TextView) dueToOurBrandRow.findViewById(R.id.price_title);
			TextView rateView = (TextView) dueToOurBrandRow.findViewById(R.id.price_text_view);
			labelView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_etp_overview_checkmark, 0, 0, 0);
			labelView.setTextColor(getResources().getColor(R.color.etp_text_color));
			rateView.setTextColor(getResources().getColor(R.color.etp_text_color));
		}
		mExtrasLayout.addView(dueToOurBrandRow);
	}

	private static final int MAX_AMENITY_ROWS = 3;

	private void addPrioritizedAmenityRows(Rate rate) {
		if (rate.shouldShowFreeCancellation() && mExtrasLayout.getChildCount() < MAX_AMENITY_ROWS) {
			addExtraRow(HotelUtils.getRoomCancellationText(getContext(), rate), true);
		}
		// Amenity rows
		if (PointOfSale.getPointOfSale().displayBestPriceGuarantee()
			&& mExtrasLayout.getChildCount() < MAX_AMENITY_ROWS) {
			addExtraRow(Ui.obtainThemeResID(getContext(), R.attr.skin_bestPriceGuaranteeString), true);
		}
	}

	public void saveInstanceState(Bundle outState) {
		// TODO
	}

	public void restoreInstanceState(Bundle inState) {
		// TODO
	}

	public void setOnSizeChangedListener(OnSizeChangedListener onSizeChangedListener) {
		mOnSizeChangedListener = onSizeChangedListener;
	}

	public void setRateBreakdownClickListener(OnClickListener listener) {
		mRateBreakdownClickListener = listener;
	}

	public void setOnViewMapClickListener(OnViewMapClickListener listener) {
		mMapClickListener = listener;
	}
}
