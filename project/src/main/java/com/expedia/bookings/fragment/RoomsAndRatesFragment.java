package com.expedia.bookings.fragment;

import java.math.BigDecimal;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Rate.CheckoutPriceType;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.dialog.HotelErrorDialog;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.RoomsAndRatesAdapter;
import com.expedia.bookings.widget.SlidingRadioGroup;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.HtmlUtils;

public class RoomsAndRatesFragment extends ListFragment {

	private static final String INSTANCE_PAY_GROUP_CHECKED_POS = "INSTANCE_PAY_GROUP_CHECKED_POS";

	public static RoomsAndRatesFragment newInstance() {
		RoomsAndRatesFragment fragment = new RoomsAndRatesFragment();
		return fragment;
	}

	private RoomsAndRatesFragmentListener mListener;

	private RoomsAndRatesAdapter mAdapter;

	private SlidingRadioGroup mPayGroup;
	private ProgressBar mProgressBar;
	private TextView mEmptyTextView;
	private TextView mFooterTextView;
	private ViewGroup mNoticeContainer;

	private int mLastCheckedItem = R.id.radius_pay_later;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, RoomsAndRatesFragmentListener.class);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(INSTANCE_PAY_GROUP_CHECKED_POS, mLastCheckedItem);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {
			mLastCheckedItem = savedInstanceState
				.getInt(INSTANCE_PAY_GROUP_CHECKED_POS, R.id.radius_pay_later);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mPayGroup.onCheckedChanged(mPayGroup, mLastCheckedItem);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_availability_list, container, false);
		mProgressBar = Ui.findView(view, R.id.progress_bar);
		mEmptyTextView = Ui.findView(view, R.id.empty_text_view);
		mNoticeContainer = Ui.findView(view, R.id.hotel_notice_container);
		mPayGroup = Ui.findView(view, R.id.radius_pay_options);
		mPayGroup.setOnCheckedChangeListener(mPayOptionsListener);
		mPayGroup.setVisibility(View.GONE);

		// Setup the ListView
		View footer = inflater.inflate(R.layout.footer_rooms_and_rates, null);
		mFooterTextView = (TextView) footer.findViewById(R.id.footer_text_view);
		((ListView) view.findViewById(android.R.id.list)).addFooterView(footer, null, false);
		mFooterTextView.setVisibility(View.GONE);

		// Hide the header if this is not the tablet
		if (!AndroidUtils.isHoneycombTablet(getActivity())) {
			Ui.findView(view, R.id.header_layout).setVisibility(View.GONE);
		}

		return view;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		mListener.onRateSelected(getItem(position));

		mAdapter.setSelectedPosition(position);
		mAdapter.notifyDataSetChanged();
	}

	private Rate getItem(int position) {
		Rate nowRate = (Rate) mAdapter.getItem(position);
		Rate laterRate = nowRate.getEtpRate();
		if (laterRate != null) {
			if (isInPayLaterMode(mLastCheckedItem)) {
				laterRate.setIsPayLater(true);
			}
			else {
				laterRate.setIsPayLater(false);
			}
		}
		return laterRate != null && isInPayLaterMode(mLastCheckedItem) ? laterRate : nowRate;
	}

	private int getPositionOfRate(Rate rate) {
		if (rate != null) {
			int count = mAdapter.getCount();
			for (int position = 0; position < count; position++) {
				Object item = mAdapter.getItem(position);
				if (item != null && rate.equals(item)) {
					return position;
				}
			}
		}
		return -1;
	}

	//////////////////////////////////////////////////////////////////////////
	// Control

	public void showProgress() {
		mProgressBar.setVisibility(View.VISIBLE);
		mEmptyTextView.setText(R.string.room_rates_loading);
	}

	public void notifyAvailabilityLoaded() {
		String selectedId = Db.getHotelSearch().getSelectedPropertyId();
		HotelOffersResponse response = Db.getHotelSearch().getHotelOffersResponse(selectedId);

		mProgressBar.setVisibility(View.GONE);

		if (response == null) {
			OmnitureTracking.trackErrorPage(getActivity(), "RatesListRequestFailed");
			mEmptyTextView.setText(R.string.error_no_response_room_rates);
			return;
		}
		else if (response.getRateCount() == 0) {
			Db.getHotelSearch().removeProperty(selectedId);

			HotelErrorDialog dialog = HotelErrorDialog.newInstance();
			dialog.setMessage(Ui.obtainThemeResID(getActivity(), R.attr.skin_sorryRoomsSoldOutErrorMessage));
			dialog.show(getFragmentManager(), "soldOutDialog");

			mEmptyTextView.setText(R.string.error_no_hotel_rooms_available);
			mAdapter = null;
			setListAdapter(null);
			mListener.noRatesAvailable();
			return;
		}

		loadResponse(response);
	}

	private void loadResponse(HotelOffersResponse response) {
		if (response.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			for (ServerError error : response.getErrors()) {
				sb.append(error.getPresentableMessage(getActivity()));
				sb.append("\n");
			}
			mEmptyTextView.setText(sb.toString().trim());
			OmnitureTracking.trackErrorPage(getActivity(), "RatesListRequestFailed");
			return;
		}

		mAdapter = new RoomsAndRatesAdapter(getActivity(), response);

		final Rate selectedRate = Db.getHotelSearch().getSelectedRate();
		if (selectedRate == null || getPositionOfRate(selectedRate) == -1) {
			mAdapter.setSelectedPosition(0);
		}
		else {
			mAdapter.setSelectedPosition(getPositionOfRate(selectedRate));
		}

		setListAdapter(mAdapter);

		//Display notices if applicable
		mNoticeContainer.removeAllViews();
		boolean renovationNoticeDisplayed = displayRenovationNotice(response);
		boolean resortFeesNoticeDisplayed = displayResortFeesNotice(response, selectedRate);
		mNoticeContainer.setVisibility(renovationNoticeDisplayed || resortFeesNoticeDisplayed ? View.VISIBLE
				: View.GONE);

		// Disable highlighting if we're on phone UI
		mAdapter.highlightSelectedPosition(AndroidUtils.isHoneycombTablet(getActivity()));

		CharSequence commonValueAdds = response.getCommonValueAddsString(getActivity());
		if (commonValueAdds != null) {
			mFooterTextView.setText(commonValueAdds);
			mFooterTextView.setVisibility(View.VISIBLE);
		}

		if (mAdapter.getCount() == 0) {
			OmnitureTracking.trackErrorPage(getActivity(), "HotelHasNoRoomsAvailable");
		}

		Property property = Db.getHotelSearch().getSelectedProperty();
		boolean isETPAvailable = property.hasEtpOffer();
		mPayGroup.setVisibility(isETPAvailable ? View.VISIBLE : View.GONE);
		mPayGroup.check(!isETPAvailable ? R.id.radius_pay_now : mLastCheckedItem);
	}

	/**
	 * If the response contains a renovation notice, we should display it to the user
	 * 
	 * @param response
	 * @return
	 */
	private boolean displayRenovationNotice(HotelOffersResponse response) {
		final String constructionText;
		if (response != null && response.getProperty() != null
				&& response.getProperty().getRenovationText() != null
				&& !TextUtils.isEmpty(response.getProperty().getRenovationText().getContent())) {
			constructionText = response.getProperty().getRenovationText().getContent();

			View constructionView = Ui.inflate(this, R.layout.include_rooms_and_rates_construction_notice,
					mNoticeContainer);
			ViewGroup constructionContainer = Ui.findView(constructionView, R.id.construction_container);

			constructionContainer.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					openWebViewWithText(getString(R.string.renovation_notice), constructionText, true);
				}
			});
			return true;
		}
		return false;
	}

	/**
	 * If the response or selectedRate contian mandatory fees, we should display a notice to the user.
	 * 
	 * @param response
	 * @param selectedRate
	 * @return
	 */
	private boolean displayResortFeesNotice(HotelOffersResponse response, Rate selectedRate) {
		Rate resortFeeRate = selectedRate;
		if (resortFeeRate == null) {
			resortFeeRate = (Rate) mAdapter.getItem(0);
		}

		if (HotelUtils.showResortFeeInfo(response.getProperty(), resortFeeRate)) {
			LayoutInflater inflater = this.getLayoutInflater(null);
			View mandatoryFeeView = inflater.inflate(R.layout.include_rooms_and_rates_resort_fees_notice,
					mNoticeContainer);
			ViewGroup feesContainer = Ui.findView(mandatoryFeeView, R.id.resort_fees_container);

			TextView feeAmountTv = Ui.findView(mandatoryFeeView, R.id.resort_fees_price);
			feeAmountTv.setText(resortFeeRate.getTotalMandatoryFees().getFormattedMoney());

			final String resortFeesText = response.getProperty().getMandatoryFeesText().getContent();
			feesContainer.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					openWebViewWithText(getString(R.string.additional_fees), resortFeesText, false);
				}
			});
			return true;
		}
		return false;
	}

	private void openWebViewWithText(String title, String text, boolean useLeftMargin) {
		String html;
		if (ExpediaBookingApp.useTabletInterface(getActivity())) {
			html = HtmlUtils.wrapInHeadAndBodyWithStandardTabletMargins(text);
		}
		else {
			html = HtmlUtils.wrapInHeadAndBodyWithMargins(text, "5%", "5%", "0px", useLeftMargin ? "5%" : "0px");
		}

		WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
		builder.setHtmlData(html);
		builder.setTitle(title);
		builder.setTheme(R.style.Theme_Phone_WebView_WithTitle);
		startActivity(builder.getIntent());
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface RoomsAndRatesFragmentListener {
		public void onRateSelected(Rate rate);

		public void noRatesAvailable();
	}

	private RadioGroup.OnCheckedChangeListener mPayOptionsListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			mLastCheckedItem = checkedId;
			if (mAdapter != null) {
				mAdapter.setPayOption(isInPayLaterMode(checkedId));
			}
		}
	};

	private boolean isInPayLaterMode(int checkedId) {
		return checkedId == R.id.radius_pay_later;
	}

}
