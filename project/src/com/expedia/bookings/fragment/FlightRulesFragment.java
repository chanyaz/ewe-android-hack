package com.expedia.bookings.fragment;

import android.content.Context;
import android.content.Intent;
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
import com.expedia.bookings.activity.FlightPenaltyRulesActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Rule;
import com.expedia.bookings.utils.RulesRestrictionsUtils;
import com.mobiata.android.SocialUtils;
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

	private Context mContext;
	private FlightTrip mFlightTrip;

	private TextView mCompletePenaltyRulesTextView;
	private TextView mLiabilitiesLinkTextView;

	public static FlightRulesFragment newInstance() {
		return new FlightRulesFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getSherlockActivity();

		String tripKey = Db.getFlightSearch().getSelectedFlightTrip().getProductKey();
		mFlightTrip = Db.getFlightSearch().getFlightTrip(tripKey);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_rules, container, false);

		mCompletePenaltyRulesTextView = Ui.findView(v, R.id.complete_penalty_rules_link_text_view);
		mLiabilitiesLinkTextView = Ui.findView(v, R.id.liabilities_link_text_view);

		if (mFlightTrip != null) {
			populateHeaderRows(v);
			populateBody(v);

			populateCompletePenaltyRulesTextView();
			populateLiabilitiesTextView();
		}

		return v;
	}

	private void populateHeaderRows(View v) {
		// rules and restrictions
		TextView rules = Ui.findView(v, R.id.rules_and_restrictions);
		rules.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Rule completeRule = mFlightTrip.getRule(RulesKeys.COMPLETE_PENALTY_RULES.getKey());
				Intent intent = new Intent(mContext, FlightPenaltyRulesActivity.class);
				intent.putExtra(FlightPenaltyRulesActivity.ARG_URL, completeRule.getUrl());
				mContext.startActivity(intent);
			}

		});

		// terms and conditions
		TextView terms = Ui.findView(v, R.id.terms_and_conditions);
		terms.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.openSite(mContext, RulesRestrictionsUtils.getTermsAndConditionsUrl(mContext));
			}
		});

		// privacy policy
		TextView privacy = Ui.findView(v, R.id.privacy_policy);
		privacy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.openSite(mContext, RulesRestrictionsUtils.getPrivacyPolicyUrl(mContext));
			}
		});
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
		Rule completeRule = mFlightTrip.getRule(RulesKeys.COMPLETE_PENALTY_RULES.getKey());

		mCompletePenaltyRulesTextView.setText(getDummyHtmlLink(completeRule));
		mLiabilitiesLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());

		final String url = completeRule.getUrl();

		mCompletePenaltyRulesTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, FlightPenaltyRulesActivity.class);
				intent.putExtra(FlightPenaltyRulesActivity.ARG_URL, url);
				mContext.startActivity(intent);
			}
		});
	}

	private void populateLiabilitiesTextView() {
		StringBuilder rulesBodyBuilder = new StringBuilder();

		// airline liability
		Rule airlineRule = mFlightTrip.getRule(RulesKeys.AIRLINE_LIABILITY_LIMITATIONS.getKey());
		appendBodyWithRuleContainingUrl(airlineRule, rulesBodyBuilder);

		// additional rules
		Rule additionalRules = mFlightTrip.getRule(RulesKeys.ADDITIONAL_AIRLINE_FEES.getKey());
		// Sometimes additional rules are not included in the API, null check here
		if (additionalRules != null) {
			rulesBodyBuilder.append(additionalRules.getText());
		}

		mLiabilitiesLinkTextView.setText(Html.fromHtml(rulesBodyBuilder.toString()));
		mLiabilitiesLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());
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
