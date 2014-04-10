package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BedType;
import com.expedia.bookings.data.BedType.BedTypeId;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.mobiata.android.util.Ui;

public class AvailabilitySummaryWidget {

	// Controlled by max_summarized_rate_rows (via config.xml)
	private int mMaxRateRows;

	// Controlled by show_from (via config.xml)
	private boolean mShowFrom;

	private Context mContext;

	// Cached views.  These may or may not be present, based on the layout being used.
	private ViewGroup mContainer;
	private ViewGroup mHeaderViewGroup;
	private ViewGroup mHeaderViewGroupTwoLine;
	private ViewGroup mMinPriceContainer;
	private View mRibbonView;
	private TextView mFromTextView;
	private TextView mBaseRateTextView;
	private TextView mSaleRateTextView;
	private TextView mRateQualifierTextView;
	private TextView mFromTextViewTwoLine;
	private TextView mBaseRateTextViewTwoLine;
	private TextView mSaleRateTextViewTwoLine;
	private TextView mRateQualifierTextViewTwoLine;
	private TextView mErrorTextView;
	private ProgressBar mProgressBar;
	private LinearLayout mRatesContainer;
	private ViewGroup mSoldOutContainer;
	private TextView mSoldOutDatesTextView;
	private TextView mMoreButton;

	private List<RateRow> mRateRows;

	public AvailabilitySummaryWidget(Context context) {
		mContext = context;

		mMaxRateRows = context.getResources().getInteger(R.integer.max_summarized_rate_rows);
		mShowFrom = context.getResources().getBoolean(R.bool.show_from);
	}

	public void init(View rootView) {
		mContainer = Ui.findView(rootView, R.id.availability_summary_container);
		mHeaderViewGroup = (ViewGroup) rootView.findViewById(R.id.availability_header);
		mFromTextView = (TextView) rootView.findViewById(R.id.from_text_view);
		mBaseRateTextView = (TextView) rootView.findViewById(R.id.base_rate_text_view);
		mSaleRateTextView = (TextView) rootView.findViewById(R.id.sale_rate_text_view);
		mRateQualifierTextView = (TextView) rootView.findViewById(R.id.rate_qualifier_text_view);
		mRibbonView = Ui.findView(rootView, R.id.ribbon);

		mHeaderViewGroupTwoLine = (ViewGroup) rootView.findViewById(R.id.availability_header_two_line);
		mFromTextViewTwoLine = (TextView) rootView.findViewById(R.id.from_text_view_two_line);
		mBaseRateTextViewTwoLine = (TextView) rootView.findViewById(R.id.base_rate_text_view_two_line);
		mSaleRateTextViewTwoLine = (TextView) rootView.findViewById(R.id.sale_rate_text_view_two_line);
		mRateQualifierTextViewTwoLine = (TextView) rootView.findViewById(R.id.rate_qualifier_text_view_two_line);

		mMinPriceContainer = Ui.findView(rootView, R.id.min_price_container);

		mErrorTextView = (TextView) rootView.findViewById(R.id.error_text_view);

		mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);

		mRatesContainer = (LinearLayout) rootView.findViewById(R.id.rates_container);
		if (mRatesContainer != null) {
			// Pre-generate the rate rows, if we have a place to put them
			mRateRows = new ArrayList<AvailabilitySummaryWidget.RateRow>();
			LayoutInflater inflater = LayoutInflater.from(mContext);
			for (int a = 0; a < mMaxRateRows; a++) {
				View row = inflater.inflate(R.layout.snippet_availability_summary_row, mRatesContainer, false);
				mRateRows.add(new RateRow(row));
				mRatesContainer.addView(row);
			}

			// Disable the divider on the last row
			mRateRows.get(mMaxRateRows - 1).toggleDivider(false);
		}

		mMoreButton = (TextView) rootView.findViewById(R.id.more_button);

		if (mMoreButton != null) {
			mMoreButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					onButtonClicked();
				}
			});
		}

		mSoldOutContainer = Ui.findView(rootView, R.id.sold_out_container);
		mSoldOutDatesTextView = Ui.findView(rootView, R.id.sold_out_dates_text_view);
	}

	//////////////////////////////////////////////////////////////////////////
	// Data updates

	public void updateProperty(Property property) {
		Rate lowestRate = property.getLowestRate();

		if (lowestRate != null) {
			setShowHeader(true);

			if (mNeedsTwoLines && lowestRate.isOnSale()) {
				configureTwoLine();
			}
			else {
				configureSingleLine();
			}

			configureFromTextView(lowestRate, mFromTextView);
			configureFromTextView(lowestRate, mFromTextViewTwoLine);

			configureBaseRateTextView(lowestRate, mBaseRateTextView);
			configureBaseRateTextView(lowestRate, mBaseRateTextViewTwoLine);

			configureSaleRateTextView(lowestRate, mSaleRateTextView);
			configureSaleRateTextView(lowestRate, mSaleRateTextViewTwoLine);

			setRateQualifierTextView(lowestRate, mRateQualifierTextView);
			setRateQualifierTextView(lowestRate, mRateQualifierTextViewTwoLine);
		}
		else {
			showSoldOut();
		}
	}

	private void configureFromTextView(Rate lowestRate, TextView fromTextView) {
		// We use the "show from" value to conditionally determine whether to show "From" - it's a suggestion here.
		if (fromTextView != null && !mShowFrom) {
			fromTextView.setVisibility(lowestRate.isOnSale() ? View.GONE : View.VISIBLE);
		}
	}

	private void configureBaseRateTextView(Rate lowestRate, TextView baseRateTextView) {
		if (baseRateTextView != null) {
			if (lowestRate.isOnSale()) {
				baseRateTextView.setVisibility(View.VISIBLE);
				baseRateTextView.setText(Html.fromHtml(
						mContext.getString(R.string.strike_template,
								StrUtils.formatHotelPrice(lowestRate.getDisplayBasePrice())), null,
						new StrikethroughTagHandler()));
			}
			else {
				baseRateTextView.setVisibility(View.GONE);
			}
		}
	}

	private void configureSaleRateTextView(Rate lowestRate, TextView saleRateTextView) {
		if (saleRateTextView != null) {
			String displayRate = StrUtils.formatHotelPrice(lowestRate.getDisplayPrice());
			saleRateTextView.setText(displayRate);
		}
	}

	public void showProgressBar() {
		setSingleViewVisible(mProgressBar, mRatesContainer, mErrorTextView, mSoldOutContainer);
	}

	public void showRates(HotelOffersResponse response) {
		if (response == null) {
			showError(mContext.getString(R.string.ean_error_connect_unavailable));
		}
		else if (response.hasErrors()) {
			showError(response.getErrors().get(0).getPresentableMessage(mContext));
		}
		else if (response.getRateCount() == 0) {
			showSoldOut();
		}
		else if (mRatesContainer != null) {
			setSingleViewVisible(mRatesContainer, mProgressBar, mErrorTextView, mSoldOutContainer);

			SummarizedRoomRates summarizedRates = response.getSummarizedRoomRates();
			int numSummarizedRates = summarizedRates.numSummarizedRates();
			for (int a = 0; a < mMaxRateRows; a++) {
				if (a < numSummarizedRates) {
					mRateRows.get(a).updateRow(summarizedRates.getBedTypeId(a), summarizedRates.getRate(a));
				}
				else {
					mRateRows.get(a).toggleRowVisibility(false);
				}
			}
		}
	}

	public void showError(CharSequence errorText) {
		setSingleViewVisible(mErrorTextView, mProgressBar, mRatesContainer, mSoldOutContainer);

		if (mErrorTextView != null) {
			mErrorTextView.setText(errorText);
		}
	}

	public void showSoldOut() {
		setShowHeader(false);

		setSingleViewVisible(mSoldOutContainer, mErrorTextView, mProgressBar, mRatesContainer);

		if (mSoldOutDatesTextView != null) {
			HotelSearchParams params = Db.getHotelSearch().getSearchParams();
			mSoldOutDatesTextView.setText(JodaUtils.formatDateRange(mContext, params.getCheckInDate(),
					params.getCheckOutDate(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL));
		}
	}

	public void setButtonText(int resId) {
		mMoreButton.setText(resId);
	}

	public void setButtonEnabled(boolean enabled) {
		mMoreButton.setEnabled(enabled);
	}

	private void setSingleViewVisible(View visibleView, View... hideViews) {
		if (visibleView != null) {
			visibleView.setVisibility(View.VISIBLE);
		}

		for (View hide : hideViews) {
			if (hide != null) {
				hide.setVisibility(View.GONE);
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Rate row

	private class RateRow {
		private View mRow;

		private TextView mDescriptionTextView;
		private TextView mRateTextView;
		private TextView mRateQualifierTextView;
		private View mDivider;

		private Rate mRate;

		public RateRow(View v) {
			mRow = v;
			mDescriptionTextView = (TextView) v.findViewById(R.id.description_text_view);
			mRateTextView = (TextView) v.findViewById(R.id.rate_text_view);
			mRateQualifierTextView = (TextView) v.findViewById(R.id.rate_qualifier_text_view);
			mDivider = v.findViewById(R.id.divider);

			v.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					onRowClick();
				}
			});
		}

		public void updateRow(BedTypeId bedTypeId, Rate rate) {
			toggleRowVisibility(true);

			mRate = rate;

			String description = rate.getRoomDescription();
			for (BedType bedType : rate.getBedTypes()) {
				if (bedType.getBedTypeId() == bedTypeId) {
					description = bedType.getBedTypeDescription();
					break;
				}
			}

			int template = (mShowFrom) ? R.string.bed_type_start_value_template : R.string.bold_template;
			mDescriptionTextView.setText(Html.fromHtml(mContext.getString(template, description)));

			mRateTextView.setText(StrUtils.formatHotelPrice(rate.getDisplayPrice()));

			setRateQualifierTextView(rate, mRateQualifierTextView);
		}

		public void toggleRowVisibility(boolean visible) {
			if (visible) {
				mRow.setVisibility(View.VISIBLE);
			}
			else {
				mRow.setVisibility(View.INVISIBLE);
				mRate = null;
			}
		}

		public void toggleDivider(boolean enabled) {
			mDivider.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
		}

		private void onRowClick() {
			if (mRate != null) {
				onRateClicked(mRate);
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener code

	public interface AvailabilitySummaryListener {
		public void onRateClicked(Rate rate);

		public void onButtonClicked();
	}

	private AvailabilitySummaryListener mListener;

	public void setListener(AvailabilitySummaryListener listener) {
		mListener = listener;
	}

	public void onButtonClicked() {
		if (mListener != null) {
			mListener.onButtonClicked();
		}
	}

	public void onRateClicked(Rate rate) {
		if (mListener != null) {
			mListener.onRateClicked(rate);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Utility code

	private void setRateQualifierTextView(Rate rate, TextView textView) {
		int qualifierId = rate.getQualifier(true);
		if (textView != null) {
			if (qualifierId != 0) {
				textView.setVisibility(View.VISIBLE);
				textView.setText(mContext.getString(qualifierId));
			}
			else {
				textView.setVisibility(View.GONE);
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Single line vs. two line layouts, depending on space

	private boolean mNeedsTwoLines;

	private void configureSingleLine() {
		if (mHeaderViewGroup != null && mHeaderViewGroupTwoLine != null) {
			mNeedsTwoLines = false;
			mHeaderViewGroup.setVisibility(View.VISIBLE);
			mHeaderViewGroupTwoLine.setVisibility(View.GONE);

			// Add a VTO to check if single line is alright;
			mHeaderViewGroup.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
						mHeaderViewGroup.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
					else {
						mHeaderViewGroup.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					}

					boolean fits = true;
					int len = mHeaderViewGroup.getChildCount();
					for (int a = 0; a < len; a++) {
						TextView child = (TextView) mHeaderViewGroup.getChildAt(a);
						if (child.getVisibility() != View.GONE && (child.getWidth() == 0 || child.getLineCount() > 1)
								&& !TextUtils.isEmpty(child.getText())) {
							fits = false;
							break;
						}
					}

					if (!fits) {
						mNeedsTwoLines = true;

						configureTwoLine();
					}
				}
			});
		}
	}

	private void configureTwoLine() {
		if (mHeaderViewGroup != null && mHeaderViewGroupTwoLine != null) {
			mHeaderViewGroup.setVisibility(View.GONE);
			mHeaderViewGroupTwoLine.setVisibility(View.VISIBLE);
		}
	}

	// This enables/disables all "header" like functionality (which may not look like a header in some configs)
	private void setShowHeader(boolean showHeader) {
		int visibility = showHeader ? View.VISIBLE : View.GONE;

		if (mContainer != null) {
			if (showHeader) {
				Drawable drawable = mContext.getResources().getDrawable(R.drawable.bg_availability_summary);
				mContainer.setBackgroundDrawable(drawable);
			}
			else {
				mContainer.setBackgroundDrawable(null);
			}
		}

		if (mRibbonView != null) {
			mRibbonView.setVisibility(showHeader ? View.VISIBLE : View.GONE);
		}

		if (mMinPriceContainer != null) {
			mMinPriceContainer.setVisibility(visibility);
		}

		if (mHeaderViewGroup != null && mHeaderViewGroupTwoLine != null) {
			mHeaderViewGroup.setVisibility(visibility);
			mHeaderViewGroupTwoLine.setVisibility(visibility);
		}

		mMoreButton.setVisibility(showHeader || mMoreButton.isEnabled() ? View.VISIBLE : View.GONE);
	}
}
