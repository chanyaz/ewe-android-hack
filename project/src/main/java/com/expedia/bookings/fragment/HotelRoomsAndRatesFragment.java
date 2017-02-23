package com.expedia.bookings.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.dialog.HotelErrorDialog;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.RoomsAndRatesAdapter;
import com.expedia.bookings.widget.SlidingRadioGroup;
import com.mobiata.android.util.HtmlUtils;
import com.squareup.phrase.Phrase;

public class HotelRoomsAndRatesFragment extends ListFragment implements AbsListView.OnScrollListener {

	private static final String INSTANCE_PAY_GROUP_CHECKED_POS = "INSTANCE_PAY_GROUP_CHECKED_POS";

	public static HotelRoomsAndRatesFragment newInstance() {
		HotelRoomsAndRatesFragment fragment = new HotelRoomsAndRatesFragment();
		return fragment;
	}

	private RoomsAndRatesFragmentListener mListener;
	private RoomsAndRatesAdapter mAdapter;

	private SlidingRadioGroup mPayGroup;
	private ProgressBar mProgressBar;
	private TextView mEmptyTextView;
	private TextView mFooterTextView;
	private ViewGroup mNoticeContainer;

	private View mHeader;
	private View mStickyHeader;
	private View mHeaderPlaceholder;

	private int mLastCheckedItem = R.id.radius_pay_now;
	private boolean isStickyHeader = false;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

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

		ListView lv = getListView();
		mHeader = getActivity().getLayoutInflater().inflate(R.layout.header_rooms_and_rates_list, lv, false);
		mHeaderPlaceholder = Ui.findView(mHeader, R.id.header_placeholder);
		lv.addHeaderView(mHeader, null, false);
		lv.setOnScrollListener(this);

		if (savedInstanceState != null) {
			mLastCheckedItem = savedInstanceState
				.getInt(INSTANCE_PAY_GROUP_CHECKED_POS, R.id.radius_pay_now);
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
		mStickyHeader = Ui.findView(view, R.id.sticky_header);
		mPayGroup = Ui.findView(view, R.id.radius_pay_options);
		mPayGroup.setOnCheckedChangeListener(mPayOptionsListener);
		mPayGroup.setVisibility(View.GONE);

		// Setup the ListView
		View footer = inflater.inflate(R.layout.footer_rooms_and_rates, null);
		mFooterTextView = (TextView) footer.findViewById(R.id.footer_text_view);
		((ListView) view.findViewById(android.R.id.list)).addFooterView(footer, null, false);
		mFooterTextView.setVisibility(View.GONE);

		// Hide the header if this is not the tablet
		if (!ExpediaBookingApp.useTabletInterface()) {
			Ui.findView(view, R.id.header_layout).setVisibility(View.GONE);
		}

		return view;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		mListener.onRateSelected(getItem(position));

		// Track room clicked with the type of payment i.e. pay now/later. and fire only if user have the option
		if (mPayGroup.getVisibility() == View.VISIBLE) {
			Rate rate = getItem(position);
			OmnitureTracking.trackHotelETPRoomSelected(rate.isPayLater());
		}

		mAdapter.setSelectedPosition(position);
		mAdapter.notifyDataSetChanged();
	}

	private Rate getItem(int position) {
		Rate nowRate = (Rate) getListView().getItemAtPosition(position);
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
			OmnitureTracking.trackErrorPage("RatesListRequestFailed");
			mEmptyTextView.setText(R.string.error_no_response_room_rates);
			return;
		}
		else if (response.getRateCount() == 0) {
			Db.getHotelSearch().removeProperty(selectedId);

			HotelErrorDialog dialog = HotelErrorDialog.newInstance();
			dialog.setMessage(Phrase.from(getActivity(), R.string.error_hotel_is_now_sold_out_TEMPLATE).put("brand", BuildConfig.brand).format().toString());
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
			OmnitureTracking.trackErrorPage("RatesListRequestFailed");
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
		boolean resortFeesNoticeDisplayed = displayResortFeesNotice(response, selectedRate);
		boolean renovationNoticeDisplayed = displayRenovationNotice(response);
		mNoticeContainer.setVisibility(renovationNoticeDisplayed || resortFeesNoticeDisplayed ? View.VISIBLE
			: View.GONE);

		// Disable highlighting if we're on phone UI
		mAdapter.highlightSelectedPosition(ExpediaBookingApp.useTabletInterface());

		CharSequence commonValueAdds = response.getCommonValueAddsString(getActivity());
		if (commonValueAdds != null) {
			mFooterTextView.setText(commonValueAdds);
			mFooterTextView.setVisibility(View.VISIBLE);
		}

		if (mAdapter.getCount() == 0) {
			OmnitureTracking.trackErrorPage("HotelHasNoRoomsAvailable");
		}

		Property property = Db.getHotelSearch().getSelectedProperty();
		boolean isETPAvailable = ProductFlavorFeatureConfiguration.getInstance().isETPEnabled() && property.hasEtpOffer();
		mPayGroup.setVisibility(isETPAvailable ? View.VISIBLE : View.GONE);
		mPayGroup.check(mLastCheckedItem = isETPAvailable ? mLastCheckedItem : R.id.radius_pay_now);

		//Set up sticky header
		isStickyHeader = renovationNoticeDisplayed && resortFeesNoticeDisplayed;

		mNoticeContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mNoticeContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mHeaderPlaceholder.getLayoutParams();
				params.height = mNoticeContainer.getHeight() + mStickyHeader.getHeight();
				mHeaderPlaceholder.setLayoutParams(params);
			}
		});
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

		if (resortFeeRate.showResortFeesMessaging()) {
			LayoutInflater inflater = this.getLayoutInflater(null);
			View mandatoryFeeView = inflater.inflate(R.layout.include_rooms_and_rates_resort_fees_notice,
				mNoticeContainer);
			ViewGroup feesContainer = Ui.findView(mandatoryFeeView, R.id.resort_fees_container);

			TextView feeAmountTv = Ui.findView(mandatoryFeeView, R.id.resort_fees_price);

			if (resortFeeRate.getTotalMandatoryFees().hasCents()) {
				feeAmountTv.setText(resortFeeRate.getTotalMandatoryFees().getFormattedMoney());
			}
			else {
				feeAmountTv.setText(resortFeeRate.getTotalMandatoryFees().getFormattedMoney(Money.F_NO_DECIMAL));
			}

			TextView bottomTv = Ui.findView(mandatoryFeeView, R.id.resort_fees_bottom_text);
			bottomTv.setText(HotelUtils.getPhoneResortFeeBannerText(getActivity(), resortFeeRate));

			final String resortFeesText = response.getProperty().getMandatoryFeesText() == null ? null
				: response.getProperty().getMandatoryFeesText().getContent();
			if (resortFeesText != null) {
				feesContainer.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						openWebViewWithText(getString(R.string.additional_fees), resortFeesText, false);
					}
				});
			}
			return true;
		}
		return false;
	}

	private void openWebViewWithText(String title, String text, boolean useLeftMargin) {
		String html;
		if (ExpediaBookingApp.useTabletInterface()) {
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
		void onRateSelected(Rate rate);

		void noRatesAvailable();
	}

	private RadioGroup.OnCheckedChangeListener mPayOptionsListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// Track ETP payment toggle events.
			// Let's not trigger omniture event when we 1st get to the rooms&rate screen. Only on user toggle.
			if (checkedId != mLastCheckedItem) {
				OmnitureTracking.trackHotelETPPayToggle(isInPayLaterMode(checkedId));
			}
			mLastCheckedItem = checkedId;
			if (mAdapter != null) {
				mAdapter.setPayOption(isInPayLaterMode(checkedId));
			}
		}
	};

	private boolean isInPayLaterMode(int checkedId) {
		return checkedId == R.id.radius_pay_later;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

		if (visibleItemCount == 0) {
			return;
		}

		int top = mHeader.getTop();
		int stickyHeight = mStickyHeader.getMeasuredHeight();
		int noticeHeight = mNoticeContainer.getMeasuredHeight();
		int headerViewHeight = mHeader.getMeasuredHeight();
		int delta = headerViewHeight - stickyHeight;

		if (isStickyHeader) {
			//Slide the notice container up along with the header
			mNoticeContainer.setTranslationY(top);
			//Stick the sticky header to the top of the list
			mStickyHeader.setTranslationY(Math.max(0, delta + top));
		}
		else {
			//fix the sticky header under the notice container
			mStickyHeader.setTranslationY(noticeHeight);
		}
	}
}
