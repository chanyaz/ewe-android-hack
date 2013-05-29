package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Rule;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.HtmlUtils;
import com.mobiata.android.util.Ui;

public class FlightRulesFragment extends SherlockFragment {

	public static final String TAG = FlightRulesFragment.class.toString();

	private enum RulesKeys {
		COMPLETE_PENALTY_RULES("CompletePenaltyRules"),
		REFUNDABILITY_TEXT("RefundabilityText"),
		CANCEL_CHANGE_INTRODUCTION_TEXT("CancelChangeIntroductionText"),
		AIRLINE_LIABILITY_LIMITATIONS("AirlineLiabilityLimitations"),
		ADDITIONAL_AIRLINE_FEES("AdditionalAirlineFees"),
		CHANGE_PENALTY_TEXT("ChangePenaltyText");

		private String mKey;

		private RulesKeys(String key) {
			mKey = key;
		}

		public String getKey() {
			return mKey;
		}

	}

	private FlightTrip mFlightTrip;

	private TextView mCompletePenaltyRulesTextView;
	private TextView mLiabilitiesLinkTextView;
	private TextView mAdditionalFeesTextView;

	public static FlightRulesFragment newInstance() {
		return new FlightRulesFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String tripKey = Db.getFlightSearch().getSelectedFlightTrip().getProductKey();
		mFlightTrip = Db.getFlightSearch().getFlightTrip(tripKey);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_rules, container, false);

		mCompletePenaltyRulesTextView = Ui.findView(v, R.id.complete_penalty_rules_link_text_view);
		mLiabilitiesLinkTextView = Ui.findView(v, R.id.liabilities_link_text_view);
		mAdditionalFeesTextView = Ui.findView(v, R.id.additional_fee_text_view);

		if (mFlightTrip != null) {
			populateHeaderRows(v);

			populateBody(v);

			populateCompletePenaltyRulesTextView();

			populateLiabilitiesTextView();

			populateAdditionalFeesTextView();
		}

		return v;
	}

	private void populateHeaderRows(View v) {

		final PointOfSale pos = PointOfSale.getPointOfSale();

		// Rules and Restrictions
		TextView rules = Ui.findView(v, R.id.rules_and_restrictions);
		rules.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Rule completeRule = mFlightTrip.getRule(RulesKeys.COMPLETE_PENALTY_RULES.getKey());
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
				builder.setUrl(completeRule.getUrl());
				builder.setTheme(R.style.FlightTheme);
				builder.setTitle(R.string.rules_and_restrictions);
				builder.setInjectExpediaCookies(true);
				builder.setLoginEnabled(true);
				startActivity(builder.getIntent());
			}

		});

		// Terms and Conditions
		TextView terms = Ui.findView(v, R.id.terms_and_conditions);
		terms.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
				builder.setUrl(pos.getTermsAndConditionsUrl());
				builder.setTheme(R.style.FlightTheme);
				builder.setTitle(R.string.terms_and_conditions);
				startActivity(builder.getIntent());
			}
		});

		// Terms of Booking
		if (pos.getTermsOfBookingUrl() != null) {
			TextView booking = Ui.findView(v, R.id.terms_of_booking);
			booking.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
					builder.setUrl(pos.getTermsOfBookingUrl());
					builder.setTheme(R.style.FlightTheme);
					builder.setTitle(R.string.Terms_of_Booking);
					startActivity(builder.getIntent());
				}
			});
		}
		else {
			Ui.findView(v, R.id.terms_of_booking).setVisibility(View.GONE);
			Ui.findView(v, R.id.terms_of_booking_divider).setVisibility(View.GONE);
		}

		// Privacy Policy
		TextView privacy = Ui.findView(v, R.id.privacy_policy);
		privacy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
				builder.setUrl(pos.getPrivacyPolicyUrl());
				builder.setTheme(R.style.FlightTheme);
				builder.setTitle(R.string.privacy_policy);
				startActivity(builder.getIntent());
			}
		});

		// ATOL Information for UK pos
		TextView atolInformation = Ui.findView(v, R.id.atol_information);
		if (PointOfSale.getPointOfSale().showAtolInfo()) {
			atolInformation.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());

					String message = getString(R.string.lawyer_label_atol_long_message);
					String html = HtmlUtils.wrapInHeadAndBody(message);
					builder.setHtmlData(html);

					builder.setTitle(R.string.lawyer_label_atol_information);
					builder.setTheme(R.style.FlightTheme);
					startActivity(builder.getIntent());
				}
			});
		}
		else {
			atolInformation.setVisibility(View.GONE);
			Ui.findView(v, R.id.atol_information_divider).setVisibility(View.GONE);
		}
	}

	private void populateBody(View v) {
		TextView tv = Ui.findView(v, R.id.flight_rules_text_view);
		String body = constructHtmlBodySectionOne();

		tv.setText(Html.fromHtml(body));
		tv.setMovementMethod(LinkMovementMethod.getInstance());
	}

	private String constructHtmlBodySectionOne() {
		StringBuilder rulesBodyBuilder = new StringBuilder();

		// intro rule
		Rule introRule = mFlightTrip.getRule(RulesKeys.CANCEL_CHANGE_INTRODUCTION_TEXT.getKey());
		appendBodyWithRule(introRule, rulesBodyBuilder);

		// refundability
		Rule refundRule = mFlightTrip.getRule(RulesKeys.REFUNDABILITY_TEXT.getKey());
		appendBodyWithBoldedRule(refundRule, rulesBodyBuilder);

		// change penalty
		Rule penaltyRule = mFlightTrip.getRule(RulesKeys.CHANGE_PENALTY_TEXT.getKey());
		appendBodyWithRuleWithoutBreaks(penaltyRule, rulesBodyBuilder);

		return rulesBodyBuilder.toString();
	}

	private void populateCompletePenaltyRulesTextView() {
		populateTextViewThatLooksLikeAUrlThatOpensAWebViewActivity(
				mFlightTrip.getRule(RulesKeys.COMPLETE_PENALTY_RULES.getKey()), mCompletePenaltyRulesTextView);

	}

	private void populateLiabilitiesTextView() {
		populateTextViewThatLooksLikeAUrlThatOpensAWebViewActivity(
				mFlightTrip.getRule(RulesKeys.AIRLINE_LIABILITY_LIMITATIONS.getKey()), mLiabilitiesLinkTextView);
	}

	private void populateTextViewThatLooksLikeAUrlThatOpensAWebViewActivity(final Rule rule, TextView textView) {
		if (rule != null) {
			textView.setText(getDummyHtmlLink(rule));
			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
					builder.setUrl(rule.getUrl());
					builder.setTheme(R.style.FlightTheme);
					builder.setTitle(R.string.legal_information);
					startActivity(builder.getIntent());
				}
			});
		}
	}

	private void populateAdditionalFeesTextView() {
		// additional rules
		final Rule additionalRules = mFlightTrip.getRule(RulesKeys.ADDITIONAL_AIRLINE_FEES.getKey());
		// Sometimes additional rules are not included in the API, null check here
		if (additionalRules != null) {
			mAdditionalFeesTextView.setText(additionalRules.getText());
		}
	}

	private void appendBodyWithRule(Rule rule, StringBuilder builder) {
		if (rule != null) {
			builder.append(rule.getText());
			builder.append("<br><br>");
		}
	}

	private void appendBodyWithRuleWithoutBreaks(Rule rule, StringBuilder builder) {
		if (rule != null) {
			builder.append(rule.getText());
		}
	}

	private void appendBodyWithBoldedRule(Rule rule, StringBuilder builder) {
		if (rule != null) {
			builder.append("<b>");
			builder.append(rule.getText());
			builder.append("</b>");
			builder.append("&nbsp;&nbsp;");
		}
	}

	private void appendBodyWithRuleContainingUrl(Rule rule, StringBuilder builder) {
		if (rule != null) {
			builder.append("<a href=\"");
			builder.append(rule.getUrl());
			builder.append("\">");
			builder.append(rule.getText());
			builder.append("</a>");
			builder.append("<br><br>");
		}
	}

	// This method just makes the TextView look like a link, doesn't contain actual link
	private Spanned getDummyHtmlLink(Rule rule) {
		StringBuilder builder = new StringBuilder();

		builder.append("<a href=\"\">");
		builder.append(rule.getText());
		builder.append("</a>");

		return Html.fromHtml(builder.toString());
	}

}
