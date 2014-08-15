package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Policy;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.WalletUtils;
import com.mobiata.android.util.HtmlUtils;
import com.mobiata.android.util.ViewUtils;

public class HotelRulesFragment extends Fragment {
	public static final String TAG = HotelRulesFragment.class.toString();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_rules, container, false);

		populateHeaderRows(view);

		ViewUtils.setAllCaps((TextView) Ui.findView(view, R.id.cancellation_policy_header_text_view));

		Rate rate = Db.getTripBucket().getHotel().getRate();
		if (rate != null) {
			Policy cancellationPolicy = rate.getRateRules().getPolicy(Policy.TYPE_CANCEL);

			if (cancellationPolicy != null) {
				TextView cancellationPolicyTextView = Ui.findView(view, R.id.cancellation_policy_text_view);
				cancellationPolicyTextView.setText(Html.fromHtml(cancellationPolicy.getDescription()));
			}

			// Show Google Wallet promo terms & condition if it's being offered
			if (PointOfSale.getPointOfSale().supportsGoogleWallet()
					&& WalletUtils.offerGoogleWallet(rate.getTotalAmountAfterTax())
					&& WalletUtils.offerGoogleWalletCoupon(getActivity())) {
				TextView header = Ui.findView(view, R.id.wallet_promo_header);
				ViewUtils.setAllCaps(header);

				header.setVisibility(View.VISIBLE);
				Ui.findView(view, R.id.wallet_promo_divider).setVisibility(View.VISIBLE);
				Ui.findView(view, R.id.wallet_promo_text).setVisibility(View.VISIBLE);
			}
		}

		return view;
	}

	private void populateHeaderRows(View view) {

		final PointOfSale pos = PointOfSale.getPointOfSale();

		// Terms and Conditions
		TextView terms = Ui.findView(view, R.id.terms_and_conditions);
		terms.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
				builder.setUrl(pos.getTermsAndConditionsUrl());
				builder.setTheme(R.style.HotelWebViewTheme);
				builder.setTitle(R.string.terms_and_conditions);
				startActivity(builder.getIntent());
			}
		});

		// Terms of Booking
		if (pos.getTermsOfBookingUrl() != null) {
			TextView booking = Ui.findView(view, R.id.terms_of_booking);
			booking.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
					builder.setUrl(pos.getTermsOfBookingUrl());
					builder.setTheme(R.style.HotelWebViewTheme);
					builder.setTitle(R.string.Terms_of_Booking);
					startActivity(builder.getIntent());
				}
			});
		}
		else {
			Ui.findView(view, R.id.terms_of_booking).setVisibility(View.GONE);
			Ui.findView(view, R.id.terms_of_booking_divider).setVisibility(View.GONE);
		}

		// Privacy Policy
		TextView privacy = Ui.findView(view, R.id.privacy_policy);
		privacy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
				builder.setUrl(PointOfSale.getPointOfSale().getPrivacyPolicyUrl());
				builder.setTheme(R.style.HotelWebViewTheme);
				builder.setTitle(R.string.privacy_policy);
				startActivity(builder.getIntent());
			}
		});

		// Best Price Guarantee
		TextView guarantee = Ui.findView(view, R.id.best_price_guarantee);
		if (PointOfSale.getPointOfSale().displayBestPriceGuarantee()) {
			guarantee.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
					builder.setUrl(PointOfSale.getPointOfSale().getBestPriceGuaranteeUrl());
					builder.setTheme(R.style.HotelWebViewTheme);
					builder.setTitle(Ui.obtainThemeResID(getActivity(), R.attr.bestPriceGuaranteeString));
					startActivity(builder.getIntent());
				}
			});
		}
		else {
			guarantee.setVisibility(View.GONE);
			Ui.findView(view, R.id.best_price_guarantee_divider).setVisibility(View.GONE);
		}

		// ATOL Information for UK pos
		TextView atolInformation = Ui.findView(view, R.id.atol_information);
		if (PointOfSale.getPointOfSale().showAtolInfo()) {
			atolInformation.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());

					String message = getString(R.string.lawyer_label_atol_long_message);
					String html;
					if (ExpediaBookingApp.useTabletInterface(getActivity())) {
						html = HtmlUtils.wrapInHeadAndBodyWithStandardTabletMargins(message);
					}
					else {
						html = HtmlUtils.wrapInHeadAndBody(message);
					}
					builder.setHtmlData(html);

					builder.setTitle(R.string.lawyer_label_atol_information);
					builder.setTheme(R.style.Theme_Phone);
					startActivity(builder.getIntent());
				}
			});
		}
		else {
			atolInformation.setVisibility(View.GONE);
			Ui.findView(view, R.id.atol_information_divider).setVisibility(View.GONE);
		}
	}
}
