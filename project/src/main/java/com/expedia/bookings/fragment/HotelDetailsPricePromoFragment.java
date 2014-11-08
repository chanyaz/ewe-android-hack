package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Rate.UserPriceType;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.VipBadgeClickListener;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.text.StrikethroughTagHandler;


public class HotelDetailsPricePromoFragment extends Fragment {

	private View mVipIcon;

	private View mRateInfoContainer;
	private View mUnavailableContainer;
	private TextView mSoldOutTextView;

	public static HotelDetailsPricePromoFragment newInstance() {
		return new HotelDetailsPricePromoFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_details_price_promo, container, false);

		mRateInfoContainer = Ui.findView(view, R.id.rate_info_container);
		mUnavailableContainer = Ui.findView(view, R.id.unavailable_container);
		mSoldOutTextView = Ui.findView(view, R.id.sold_out_text_view);

		populateViews(view);
		return view;
	}

	public void populateViews() {
		populateViews(getView());
	}

	private void populateViews(View view) {
		populatePricePromoBar(view, Db.getHotelSearch().getSelectedProperty());
		populateVipIcon(view, Db.getHotelSearch().getSelectedProperty());
	}

	private void populatePricePromoBar(View view, Property property) {
		if (property == null) {
			return;
		}

		Rate rate = property.getLowestRate();

		if (rate == null) {
			mRateInfoContainer.setVisibility(View.GONE);
			mUnavailableContainer.setVisibility(View.VISIBLE);

			HotelSearchParams params = Db.getHotelSearch().getSearchParams();
			if (params.isDefaultStay()) {
				mSoldOutTextView.setText((Ui.obtainThemeResID(getActivity(), R.attr.skin_hotelSearchResultNotAvailale)));
			}
			else {
				String dates = DateFormatUtils.formatDateRange(getActivity(), params, DateFormatUtils.FLAGS_DATE_ABBREV_ALL);
				mSoldOutTextView.setText(getString(Ui.obtainThemeResID(getActivity(), R.attr.skin_hotelSearchResultTEMPLATE), dates));
			}
		}
		else {
			mRateInfoContainer.setVisibility(View.VISIBLE);
			mUnavailableContainer.setVisibility(View.GONE);

			// Sale banner
			TextView saleBannerTextView = Ui.findView(view, R.id.sale_banner_text_view);
			TextView promoTextView = Ui.findView(view, R.id.promo_text_view);
			if (rate.isSaleTenPercentOrBetter()) {
				saleBannerTextView.setVisibility(View.VISIBLE);
				saleBannerTextView.setText(getString(R.string.minus_x_percent, rate.getDiscountPercent()));
			}
			else {
				saleBannerTextView.setVisibility(View.GONE);
			}

			// Promo text, i.e. "Mobile Exclusive!" or "Tonight Only!"
			if (property.isLowestRateTonightOnly()) {
				promoTextView.setText(getString(R.string.tonight_only));
				promoTextView.setVisibility(View.VISIBLE);
			}
			else if (property.isLowestRateMobileExclusive()) {
				promoTextView.setText(getString(R.string.mobile_exclusive));
				promoTextView.setVisibility(View.VISIBLE);
			}
			else {
				promoTextView.setVisibility(View.GONE);
			}

			// "<strike>$400</strike>" (if it's on sale)
			TextView strikethroughTextView = Ui.findView(view, R.id.strikethrough_price_text_view);
			if (rate.isOnSale()) {
				strikethroughTextView.setText(Html.fromHtml(
						getString(R.string.strike_template, StrUtils.formatHotelPrice(rate.getDisplayBasePrice())),
						null,
						new StrikethroughTagHandler()));
				strikethroughTextView.setVisibility(View.VISIBLE);
			}
			else {
				strikethroughTextView.setVisibility(View.GONE);
			}

			// Rate
			TextView rateTextView = Ui.findView(view, R.id.rate_text_view);
			rateTextView.setText(StrUtils.formatHotelPrice(rate.getDisplayPrice()));
			view.findViewById(R.id.per_nt_text_view).setVisibility(
					rate.getUserPriceType() != UserPriceType.PER_NIGHT_RATE_NO_TAXES ? View.GONE : View.VISIBLE);
		}
	}

	public void populateVipIcon(View root, Property property) {
		if (property == null) {
			return;
		}

		if (PointOfSale.getPointOfSale().supportsVipAccess() && property.isVipAccess()) {
			mVipIcon = Ui.findView(root, R.id.vip_badge);
			mVipIcon.setVisibility(View.VISIBLE);
			mVipIcon.setOnClickListener(new VipBadgeClickListener(getResources(), getFragmentManager()));
		}
	}

	public void setVipIconEnabled(boolean enabled) {
		if (mVipIcon != null) {
			mVipIcon.setEnabled(enabled);
		}
	}
}
