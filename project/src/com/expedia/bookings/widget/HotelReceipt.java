package com.expedia.bookings.widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.Ui;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;

public class HotelReceipt extends LinearLayout {
	public interface OnSizeChangedListener {
		public void onReceiptSizeChanged(int w, int h, int oldw, int oldh);

		public void onMiniReceiptSizeChanged(int w, int h, int oldw, int oldh);
	}

	public HotelReceipt(Context context) {
		this(context, null);
	}

	public HotelReceipt(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public HotelReceipt(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.widget_hotel_receipt, this);
	}

	private OnSizeChangedListener mOnSizeChangedListener;
	private OnClickListener mRateBreakdownClickListener;

	private ImageView mHeaderImageView;
	private TextView mRoomTypeDesciptionTextView;
	private TextView mBedTypeNameTextView;
	private View mRoomLongDescriptionDivider;
	private TextView mRoomLongDescriptionTextView;
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

	private TextView mWalletPromoAppliedTextView;

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		mHeaderImageView = Ui.findView(this, R.id.header_image_view);
		mRoomTypeDesciptionTextView = Ui.findView(this, R.id.room_type_description_text_view);
		mBedTypeNameTextView = Ui.findView(this, R.id.bed_type_name_text_view);
		mRoomLongDescriptionDivider = Ui.findView(this, R.id.room_long_description_divider);
		mRoomLongDescriptionTextView = Ui.findView(this, R.id.room_long_description_text_view);

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

		mWalletPromoAppliedTextView = Ui.findView(this, R.id.wallet_promo_applied_text_view);
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

	public void bind(boolean showMiniReceipt, Property property, HotelSearchParams params, Rate rate,
			boolean usingGoogleWalletCoupon) {
		Media media = HotelUtils.getRoomMedia(property, rate);
		if (media != null) {
			media.loadHighResImage(mHeaderImageView, null);
		}

		mRoomTypeDesciptionTextView.setText(rate.getRoomDescription());
		mBedTypeNameTextView.setText(rate.getFormattedBedNames());

		if (TextUtils.isEmpty(rate.getRoomLongDescription())) {
			mRoomLongDescriptionDivider.setVisibility(View.GONE);
			mRoomLongDescriptionTextView.setVisibility(View.GONE);
		}
		else {
			mRoomLongDescriptionDivider.setVisibility(View.VISIBLE);
			mRoomLongDescriptionTextView.setVisibility(View.VISIBLE);
			mRoomLongDescriptionTextView.setText(rate.getRoomLongDescription());

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
		if (PointOfSale.getPointOfSale().displayBestPriceGuarantee()) {
			addExtraRow(R.string.best_price_guarantee);
		}

		if (rate.shouldShowFreeCancellation()) {
			Date window = rate.getFreeCancellationWindowDate();
			if (window != null) {
				Context context = getContext();
				String format = context.getString(Ui.obtainThemeResID((Activity) context, R.attr.hotelCancellationPolicyDateFormat));

				CharSequence formattedDate = DateFormat.format(format, window);
				String formattedString = getContext()
						.getString(R.string.free_cancellation_date_TEMPLATE, formattedDate);
				addExtraRow(Html.fromHtml(formattedString));
			}
			else {
				addExtraRow(R.string.free_cancellation);
			}
		}

		final Resources res = getContext().getResources();

		int numNights = params.getStayDuration();
		String numNightsString = res.getQuantityString(R.plurals.number_of_nights, numNights, numNights);
		mNightsTextView.setText(numNightsString);

		mDateRangeTextView.setText(getFormattedDateRange(params));

		int numberOfGuests = params.getNumAdults() + params.getNumChildren();
		mGuestsTextView.setText(res.getQuantityString(R.plurals.number_of_guests, numberOfGuests, numberOfGuests));

		mPriceTextView.setText(rate.getDisplayTotalPrice().getFormattedMoney());

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
			crossfade.start();

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

		bind(usingGoogleWalletCoupon);
	}

	public void bind(boolean usingGoogleWalletCoupon) {
		mWalletPromoAppliedTextView.setVisibility(usingGoogleWalletCoupon ? View.VISIBLE : View.GONE);
	}

	private String getFormattedDateRange(HotelSearchParams params) {
		//A little hacky: use the DateFormat to set the date order and set dividers, then just remove the year.
		String yearlessShortPattern = ((SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT))
				.toPattern().replaceAll("\\W?[Yy]+\\W?", "");

		CharSequence from = DateFormat.format(yearlessShortPattern, params.getCheckInDate());
		CharSequence to = DateFormat.format(yearlessShortPattern, params.getCheckOutDate());
		String rangeString = getContext().getString(R.string.date_range_TEMPLATE, from, to);
		return "(" + rangeString + ")";

	}

	private void addExtraRow(int stringId) {
		addExtraRow(getContext().getString(stringId));
	}

	private void addExtraRow(CharSequence label) {
		mExtrasLayout.setVisibility(View.VISIBLE);
		mExtrasDivider.setVisibility(View.VISIBLE);

		LayoutInflater inflater = LayoutInflater.from(getContext());
		View extraRow = inflater.inflate(R.layout.snippet_hotel_receipt_extra, mExtrasLayout, false);
		TextView labelView = (TextView) extraRow.findViewById(R.id.extra_label);
		labelView.setText(label);
		mExtrasLayout.addView(extraRow);
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
}
