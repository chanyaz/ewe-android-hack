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
import com.expedia.bookings.data.pos.PointOfSaleInfo;
import com.expedia.bookings.utils.RulesRestrictionsUtils;
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
		// terms and conditions
		TextView terms = Ui.findView(view, R.id.terms_and_conditions);
		terms.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), WebViewActivity.class);
				intent.putExtra(WebViewActivity.ARG_URL, RulesRestrictionsUtils.getTermsAndConditionsUrl(getActivity()));
				intent.putExtra(WebViewActivity.ARG_STYLE_RES_ID, R.style.HotelWebViewTheme);
				intent.putExtra(WebViewActivity.ARG_DISABLE_SIGN_IN, true);
				startActivity(intent);
			}
		});

		// privacy policy
		TextView privacy = Ui.findView(view, R.id.privacy_policy);
		privacy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), WebViewActivity.class);
				intent.putExtra(WebViewActivity.ARG_URL, RulesRestrictionsUtils.getPrivacyPolicyUrl(getActivity()));
				intent.putExtra(WebViewActivity.ARG_STYLE_RES_ID, R.style.HotelWebViewTheme);
				intent.putExtra(WebViewActivity.ARG_DISABLE_SIGN_IN, true);
				startActivity(intent);
			}
		});

		// privacy policy
		TextView guarantee = Ui.findView(view, R.id.best_price_guarantee);
		if (PointOfSaleInfo.getPointOfSaleInfo().displayBestPriceGuarantee()) {
			guarantee.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), WebViewActivity.class);
					intent.putExtra(WebViewActivity.ARG_URL, PointOfSaleInfo.getPointOfSaleInfo()
							.getBestPriceGuaranteeUrl());
					intent.putExtra(WebViewActivity.ARG_STYLE_RES_ID, R.style.HotelWebViewTheme);
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
