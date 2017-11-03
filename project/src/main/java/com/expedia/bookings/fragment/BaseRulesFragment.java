package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Rule;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.util.HtmlUtils;
import com.mobiata.android.util.Ui;

public abstract class BaseRulesFragment extends Fragment {

	protected enum RulesKeys {
		COMPLETE_PENALTY_RULES("CompletePenaltyRules"),
		REFUNDABILITY_TEXT("RefundabilityText"),
		CANCEL_CHANGE_INTRODUCTION_TEXT("CancelChangeIntroductionText"),
		AIRLINE_LIABILITY_LIMITATIONS("AirlineLiabilityLimitations"),
		ADDITIONAL_AIRLINE_FEES("AdditionalAirlineFees"),
		CHANGE_PENALTY_TEXT("ChangePenaltyText"),
		LCC_IMPORTANT_TEXT("flightRulesLCCImportantMessage"),
		LCC_LITE_TEXT("flightRulesLCCServiceLiteMessage"),
		LCC_EMAIL_TEXT("LccPartnerConfEmailsText"),
		LCC_CHECKIN_TEXT("flightRulesLCCPrecheckinAdvice"),
		GENERAL_CONDITIONS("GeneralConditions");

		private String mKey;

		RulesKeys(String key) {
			mKey = key;
		}

		public String getKey() {
			return mKey;
		}
	}


	protected TextView mCompletePenaltyRulesTextView;
	protected TextView mLiabilitiesLinkTextView;
	protected TextView mAdditionalFeesTextView;
	protected TextView mGeneralConditionTextView;

	protected TextView mLccTextView;
	protected LinearLayout cancellationPolicyContainer;
	protected TextView mCancellationTextView;
	protected TextView mFareInformation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_legal_rules, container, false);

		mCompletePenaltyRulesTextView = Ui.findView(v, R.id.complete_penalty_rules_link_text_view);
		mLiabilitiesLinkTextView = Ui.findView(v, R.id.liabilities_link_text_view);
		mAdditionalFeesTextView = Ui.findView(v, R.id.additional_fee_text_view);
		mLccTextView = Ui.findView(v, R.id.lcc_text_view);
		cancellationPolicyContainer = Ui.findView(v, R.id.cancellation_policy_container);
		mCancellationTextView = Ui.findView(v, R.id.cancellation_policy_text_view);
		mFareInformation = Ui.findView(v, R.id.fare_information);
		mGeneralConditionTextView = Ui.findView(v, R.id.general_condition_view);
		return v;
	}

	public void setRulesAndRestrictionHeader(View v, final String url) {
		// Rules and Restrictions
		TextView rules = Ui.findView(v, R.id.rules_and_restrictions);
		rules.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
				builder.setUrl(url);
				builder.setTitle(R.string.rules_and_restrictions);
				builder.setInjectExpediaCookies(true);
				builder.setLoginEnabled(true);
				startActivity(builder.getIntent());
			}

		});


	}

	protected void populateHeaderRows(View v) {

		final PointOfSale pos = PointOfSale.getPointOfSale();

		// Terms and Conditions
		TextView terms = Ui.findView(v, R.id.terms_and_conditions);
		String termsUrl = pos.getTermsAndConditionsUrl();
		if (termsUrl != null && !termsUrl.isEmpty()) {
			terms.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
					builder.setUrl(pos.getTermsAndConditionsUrl());
					builder.setTitle(R.string.terms_and_conditions);
					startActivity(builder.getIntent());
				}
			});
		}
		else {
			terms.setVisibility(View.GONE);
		}


		// Terms of Booking
		if (pos.getTermsOfBookingUrl() != null) {
			TextView booking = Ui.findView(v, R.id.terms_of_booking);
			booking.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
					builder.setUrl(pos.getTermsOfBookingUrl());
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
					startActivity(builder.getIntent());
				}
			});
		}
		else {
			atolInformation.setVisibility(View.GONE);
			Ui.findView(v, R.id.atol_information_divider).setVisibility(View.GONE);
		}
	}

	protected void populateBody(View v) {
		TextView tv = Ui.findView(v, R.id.flight_rules_text_view);
		String body = constructHtmlBodySectionOne();

		tv.setText(HtmlCompat.fromHtml(body));
		tv.setMovementMethod(LinkMovementMethod.getInstance());
	}

	abstract String constructHtmlBodySectionOne();
	abstract void populateLccInfo();

	protected void appendStringWithBreak(StringBuilder builder, Rule rule) {
		if (rule != null) {
			appendStringWithBreak(builder, rule.getText());
		}
	}

	protected void appendStringWithBreak(StringBuilder builder, String text) {
		if (Strings.isNotEmpty(text)) {
			builder.append(text);
			builder.append("<br><br>");
		}
	}

	protected void appendBodyWithRuleWithoutBreaks(StringBuilder builder, Rule rule) {
		if (rule != null) {
			String text = rule.getText();
			appendBodyWithRuleWithoutBreaks(builder, text);
		}
	}

	protected void appendBodyWithRuleWithoutBreaks(StringBuilder builder, String text) {
		if (Strings.isNotEmpty(text)) {
			builder.append(text);
		}
	}

	protected void appendBodyWithBoldedRule(StringBuilder builder, Rule rule) {
		if (rule != null) {
			String text = rule.getText();
			appendBodyWithBoldedRule(builder, text);
		}
	}

	protected void appendBodyWithBoldedRule(StringBuilder builder, String text) {
		if (Strings.isNotEmpty(text)) {
			builder.append("<b>");
			builder.append(text);
			builder.append("</b>");
			builder.append("&nbsp;&nbsp;");
		}
	}

	protected void populateTextViewThatLooksLikeAUrlThatOpensAWebViewActivity(String text, final String url,
		TextView textView) {
		if (text != null) {
			textView.setVisibility(View.VISIBLE);
			textView.setText(getDummyHtmlLink(text));
			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
					builder.setUrl(url);
					builder.setTitle(R.string.legal_information);
					startActivity(builder.getIntent());
				}
			});
		}
	}

	// This method just makes the TextView look like a link, doesn't contain actual link
	protected Spanned getDummyHtmlLink(String text) {
		StringBuilder builder = new StringBuilder();

		builder.append("<a href=\"\">");
		builder.append(text);
		builder.append("</a>");

		return HtmlCompat.fromHtml(builder.toString());
	}

}
