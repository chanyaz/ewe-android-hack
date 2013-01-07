package com.expedia.bookings.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Policy;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.pos.PointOfSale;
import com.mobiata.android.util.Ui;
import com.mobiata.android.util.ViewUtils;

public class HotelRulesFragment extends SherlockFragment {
	public static final String TAG = HotelRulesFragment.class.toString();

	public static HotelRulesFragment newInstance() {
		return new HotelRulesFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_rules, container, false);

		populateHeaderRows(view);

		ViewUtils.setAllCaps((TextView) Ui.findView(view, R.id.cancellation_policy_header_text_view));

		Rate rate = Db.getSelectedRate();
		if (rate != null) {
			Policy cancellationPolicy = rate.getRateRules().getPolicy(Policy.TYPE_CANCEL);

			TextView cancellationPolicyTextView = Ui.findView(view, R.id.cancellation_policy_text_view);
			cancellationPolicyTextView.setText(Html.fromHtml(cancellationPolicy.getDescription()));
		}

		return view;
	}

	private void populateHeaderRows(View view) {
		// Terms and Conditions
		TextView terms = Ui.findView(view, R.id.terms_and_conditions);
		terms.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = WebViewActivity.getIntent(getActivity(), PointOfSale.getPointOfSale()
						.getTermsAndConditionsUrl(), R.style.HotelWebViewTheme, 0, true);
				startActivity(intent);
			}
		});

		// Privacy Policy
		TextView privacy = Ui.findView(view, R.id.privacy_policy);
		privacy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = WebViewActivity.getIntent(getActivity(), PointOfSale.getPointOfSale()
						.getPrivacyPolicyUrl(), R.style.HotelWebViewTheme, 0, true);
				startActivity(intent);
			}
		});

		// Best Price Guarantee
		TextView guarantee = Ui.findView(view, R.id.best_price_guarantee);
		if (PointOfSale.getPointOfSale().displayBestPriceGuarantee()) {
			guarantee.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = WebViewActivity.getIntent(getActivity(), PointOfSale.getPointOfSale()
							.getBestPriceGuaranteeUrl(), R.style.HotelWebViewTheme, 0, true);
					startActivity(intent);
				}
			});
		}
		else {
			guarantee.setVisibility(View.GONE);
			Ui.findView(view, R.id.best_price_guarantee_divider).setVisibility(View.GONE);
		}
	}
}
