package com.expedia.bookings.widget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.utils.StrUtils;

public class HotelReceiptMini extends FrameLayout {
	public interface OnSizeChangedListener {
		public void onMiniReceiptSizeChanged(int w, int h, int oldw, int oldh);
	}

	private enum ViewType {
		TOTAL_COST, MINI_DETAILS;
	}

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("M/d", Locale.getDefault());

	private OnSizeChangedListener mOnSizeChangedListener;

	private LayoutInflater mInflater;
	private ViewType mViewType = ViewType.TOTAL_COST;

	private View mTotalCostLayout;
	private View mMiniDetailsLayout;

	private TextView mRoomTypeTextView;
	private TextView mGuestsTextView;
	private TextView mDatesTextView;
	private TextView mMiniTotalCostTextView;

	private TextView mTotalCostTextView;
	private ViewGroup mBelowTotalCostLayout;

	public HotelReceiptMini(Context context) {
		this(context, null, 0);
	}

	public HotelReceiptMini(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HotelReceiptMini(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		DATE_FORMAT.setTimeZone(CalendarUtils.getFormatTimeZone());

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mInflater.inflate(R.layout.widget_hotel_receipt_mini, this);

		mTotalCostLayout = (View) findViewById(R.id.total_cost_layout);
		mMiniDetailsLayout = (View) findViewById(R.id.mini_details_layout);

		mRoomTypeTextView = (TextView) findViewById(R.id.room_type_text_view);
		mGuestsTextView = (TextView) findViewById(R.id.guests_text_view);
		mDatesTextView = (TextView) findViewById(R.id.dates_text_view);
		mMiniTotalCostTextView = (TextView) findViewById(R.id.mini_total_cost_text_view);

		mTotalCostTextView = (TextView) findViewById(R.id.total_cost_text_view);
		mBelowTotalCostLayout = (ViewGroup) findViewById(R.id.below_total_details_layout);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (mOnSizeChangedListener != null) {
			mOnSizeChangedListener.onMiniReceiptSizeChanged(w, h, oldw, oldh);
		}
	}

	public void setOnSizeChangedListener(OnSizeChangedListener onSizeChangedListener) {
		mOnSizeChangedListener = onSizeChangedListener;
	}

	public void showTotalCostLayout() {
		if (mViewType == ViewType.TOTAL_COST) {
			return;
		}

		mViewType = ViewType.TOTAL_COST;

		if (getVisibility() != View.VISIBLE) {
			mTotalCostLayout.setVisibility(View.VISIBLE);
			mMiniDetailsLayout.setVisibility(View.INVISIBLE);

			return;
		}

		Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
		fadeOut.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mMiniDetailsLayout.setVisibility(View.INVISIBLE);
			}
		});
		mMiniDetailsLayout.startAnimation(fadeOut);

		mTotalCostLayout.setVisibility(View.VISIBLE);
		mTotalCostLayout.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
	}

	public void showMiniDetailsLayout() {
		if (mViewType == ViewType.MINI_DETAILS) {
			return;
		}

		mViewType = ViewType.MINI_DETAILS;

		if (getVisibility() != View.VISIBLE) {
			mTotalCostLayout.setVisibility(View.INVISIBLE);
			mMiniDetailsLayout.setVisibility(View.VISIBLE);

			return;
		}

		Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
		fadeOut.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mTotalCostLayout.setVisibility(View.INVISIBLE);
			}
		});
		mTotalCostLayout.startAnimation(fadeOut);

		mMiniDetailsLayout.setVisibility(View.VISIBLE);
		mMiniDetailsLayout.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
	}

	public void updateData(Property selectedProperty, SearchParams searchParams, Rate selectedRate) {
		mRoomTypeTextView.setText(Html.fromHtml(selectedRate.getRoomDescription()));
		mGuestsTextView.setText(StrUtils.formatGuests(getContext(), searchParams));

		Money displayedTotal;
		if (LocaleUtils.shouldDisplayMandatoryFees(getContext())) {
			mBelowTotalCostLayout.setVisibility(View.VISIBLE);
			addTextRow(mBelowTotalCostLayout, R.string.PayToExpedia, selectedRate.getTotalAmountAfterTax()
					.getFormattedMoney());
			displayedTotal = selectedRate.getTotalPriceWithMandatoryFees();
		}
		else {
			mBelowTotalCostLayout.setVisibility(View.GONE);
			displayedTotal = selectedRate.getTotalAmountAfterTax();
		}

		mMiniTotalCostTextView.setText(displayedTotal.getFormattedMoney());
		mTotalCostTextView.setText(displayedTotal.getFormattedMoney());

		mDatesTextView.setText(DATE_FORMAT.format(searchParams.getCheckInDate().getTime()) + " - "
				+ DATE_FORMAT.format(searchParams.getCheckOutDate().getTime()));
	}

	public void reset() {
		mBelowTotalCostLayout.removeAllViews();
	}

	/**
	 * This adds a row, using snippet_booking_detail_text, where the LEFT column has a
	 * width of wrap_content and the RIGHT column is wrapped if too long.
	 */
	private View addTextRow(ViewGroup parent, int labelStrId, CharSequence value) {
		return addRow(parent, getContext().getString(labelStrId), value, R.layout.snippet_booking_detail_text);
	}

	private View addRow(ViewGroup parent, CharSequence label, CharSequence value, int layoutResId) {
		if (value == null || value.length() == 0) {
			return null;
		}

		View detailRow = mInflater.inflate(layoutResId, parent, false);
		TextView labelView = (TextView) detailRow.findViewById(R.id.label_text_view);
		labelView.setText(label);
		TextView valueView = (TextView) detailRow.findViewById(R.id.value_text_view);
		valueView.setText(value);
		parent.addView(detailRow);
		return detailRow;
	}
}