package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Rate.BedType;
import com.expedia.bookings.data.Rate.BedTypeId;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.text.StrikethroughTagHandler;

public class AvailabilitySummaryWidget {

	// Controlled by max_summarized_rate_rows (via config.xml)
	private int mMaxRateRows;

	// Controlled by show_from (via config.xml)
	private boolean mShowFrom;

	private Context mContext;

	// Cached views.  These may or may not be present, based on the layout being used.
	private TextView mFromTextView;
	private TextView mBaseRateTextView;
	private TextView mSaleRateTextView;
	private TextView mRateQualifierTextView;
	private TextView mErrorTextView;
	private ProgressBar mProgressBar;
	private LinearLayout mRatesContainer;
	private TextView mMoreButton;

	private List<RateRow> mRateRows;

	public AvailabilitySummaryWidget(Context context) {
		mContext = context;

		mMaxRateRows = context.getResources().getInteger(R.integer.max_summarized_rate_rows);
		mShowFrom = context.getResources().getBoolean(R.bool.show_from);
	}

	public void init(View rootView) {
		mFromTextView = (TextView) rootView.findViewById(R.id.from_text_view);
		mBaseRateTextView = (TextView) rootView.findViewById(R.id.base_rate_text_view);
		mSaleRateTextView = (TextView) rootView.findViewById(R.id.sale_rate_text_view);
		mRateQualifierTextView = (TextView) rootView.findViewById(R.id.rate_qualifier_text_view);

		mErrorTextView = (TextView) rootView.findViewById(R.id.error_text_view);

		mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);

		mRatesContainer = (LinearLayout) rootView.findViewById(R.id.rates_container);
		if (mRatesContainer != null) {
			// Pre-generate the rate rows, if we have a place to put them
			mRateRows = new ArrayList<AvailabilitySummaryWidget.RateRow>();
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
	}

	//////////////////////////////////////////////////////////////////////////
	// Data updates

	public void updateProperty(Property property) {
		Rate lowestRate = property.getLowestRate();

		// We use the "show from" value to conditionally determine whether to show "From" - it's a suggestion here.
		if (mFromTextView != null && !mShowFrom) {
			mFromTextView.setVisibility(lowestRate.isOnSale() ? View.GONE : View.VISIBLE);
		}

		if (mBaseRateTextView != null) {
			if (lowestRate.isOnSale()) {
				mBaseRateTextView.setVisibility(View.VISIBLE);
				mBaseRateTextView.setText(Html.fromHtml(
						mContext.getString(R.string.strike_template,
								StrUtils.formatHotelPrice(lowestRate.getDisplayBaseRate())), null,
						new StrikethroughTagHandler()));
			}
			else {
				mBaseRateTextView.setVisibility(View.GONE);
			}
		}

		if (mSaleRateTextView != null) {
			String displayRate = StrUtils.formatHotelPrice(lowestRate.getDisplayRate());
			mSaleRateTextView.setText(displayRate);
		}

		setRateQualifierTextView(lowestRate, mRateQualifierTextView);
	}

	public void showProgressBar() {
		setSingleViewVisible(mProgressBar, mRatesContainer, mErrorTextView);
	}

	public void showRates(AvailabilityResponse response) {
		setSingleViewVisible(mRatesContainer, mProgressBar, mErrorTextView);

		if (response == null) {
			showError(mContext.getString(R.string.ean_error_connect_unavailable));
		}
		else if (response.hasErrors()) {
			showError(response.getErrors().get(0).getPresentableMessage(mContext));
		}
		else {
			if (mRatesContainer != null) {
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
	}

	public void showError(CharSequence errorText) {
		setSingleViewVisible(mErrorTextView, mProgressBar, mRatesContainer);

		if (mErrorTextView != null) {
			mErrorTextView.setText(errorText);
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
				if (bedType.bedTypeId == bedTypeId) {
					description = bedType.bedTypeDescription;
					break;
				}
			}

			int template = (mShowFrom) ? R.string.bed_type_start_value_template : R.string.bold_template;
			mDescriptionTextView.setText(Html.fromHtml(mContext.getString(template, description)));

			mRateTextView.setText(StrUtils.formatHotelPrice(rate.getDisplayRate()));

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
}
