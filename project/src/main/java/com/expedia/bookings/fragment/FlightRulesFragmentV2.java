package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.flights.FlightCreateTripResponse;
import com.expedia.bookings.text.HtmlCompat;

public class FlightRulesFragmentV2 extends BaseRulesFragment {

	private FlightCreateTripResponse flightCreateTripResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		flightCreateTripResponse = Db.getTripBucket().getFlightV2().flightCreateTripResponse;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		cancellationPolicyContainer.setVisibility(View.GONE);
		if (flightCreateTripResponse != null) {
			String completeRuleUrl = flightCreateTripResponse.flightRules.rulesToUrl
				.get(RulesKeys.COMPLETE_PENALTY_RULES.getKey());
			setRulesAndRestrictionHeader(v, completeRuleUrl);
			populateHeaderRows(v);
			populateBody(v);
			populateLccInfo();
			String completePenaltyRuleText = flightCreateTripResponse.flightRules.rulesToText
				.get(RulesKeys.COMPLETE_PENALTY_RULES.getKey());
			String completePenaltyRuleUrl = flightCreateTripResponse.flightRules.rulesToUrl
				.get(RulesKeys.COMPLETE_PENALTY_RULES.getKey());

			populateTextViewThatLooksLikeAUrlThatOpensAWebViewActivity(
				completePenaltyRuleText, completePenaltyRuleUrl, mCompletePenaltyRulesTextView);

			String liabilityRuleText = flightCreateTripResponse.flightRules.rulesToText
				.get(RulesKeys.AIRLINE_LIABILITY_LIMITATIONS.getKey());
			String liabilityRuleUrl = flightCreateTripResponse.flightRules.rulesToUrl
				.get(RulesKeys.AIRLINE_LIABILITY_LIMITATIONS.getKey());

			populateTextViewThatLooksLikeAUrlThatOpensAWebViewActivity(
				liabilityRuleText, liabilityRuleUrl, mLiabilitiesLinkTextView);

			String airlineFeeRuleText = flightCreateTripResponse.flightRules.rulesToText
				.get(RulesKeys.ADDITIONAL_AIRLINE_FEES.getKey());
			String airlineFeeRuleUrl = flightCreateTripResponse.flightRules.rulesToUrl
				.get(RulesKeys.ADDITIONAL_AIRLINE_FEES.getKey());

			populateTextViewThatLooksLikeAUrlThatOpensAWebViewActivity(
				airlineFeeRuleText, airlineFeeRuleUrl, mAdditionalFeesTextView);

			addGeneralRule();
			mFareInformation.setText(R.string.fare_information);
		}

		return v;
	}

	private void addGeneralRule() {
		String generalConditionText = flightCreateTripResponse.flightRules.rulesToText
				.get(RulesKeys.GENERAL_CONDITIONS.getKey());
		String generalConditionUrl = flightCreateTripResponse.flightRules.rulesToUrl
				.get(RulesKeys.GENERAL_CONDITIONS.getKey());
		if (generalConditionText != null && generalConditionUrl != null) {
			populateTextViewThatLooksLikeAUrlThatOpensAWebViewActivity(
				generalConditionText, generalConditionUrl, mGeneralConditionTextView);
		}
	}

    @Override
	String constructHtmlBodySectionOne() {
		StringBuilder rulesBodyBuilder = new StringBuilder();
		if (flightCreateTripResponse != null) {
			// intro rule
			String cancelChangeText = flightCreateTripResponse.flightRules.rulesToText
				.get(RulesKeys.CANCEL_CHANGE_INTRODUCTION_TEXT.getKey());
			appendStringWithBreak(rulesBodyBuilder, cancelChangeText);

			// refundability
			String refundText = flightCreateTripResponse.flightRules.rulesToText
				.get(RulesKeys.REFUNDABILITY_TEXT.getKey());
			appendBodyWithBoldedRule(rulesBodyBuilder, refundText);

			// change penalty
			String penaltyText = flightCreateTripResponse.flightRules.rulesToText
				.get(RulesKeys.CHANGE_PENALTY_TEXT.getKey());
			appendBodyWithRuleWithoutBreaks(rulesBodyBuilder, penaltyText);
		}

		return rulesBodyBuilder.toString();
	}

	@Override
	void populateLccInfo() {
		StringBuilder builder = new StringBuilder();
		if (flightCreateTripResponse != null && flightCreateTripResponse.flightRules.rulesToText
			.containsKey(RulesKeys.LCC_IMPORTANT_TEXT.getKey())) {
			appendStringWithBreak(builder,
				flightCreateTripResponse.flightRules.rulesToText.get(RulesKeys.LCC_IMPORTANT_TEXT.getKey()));
			appendStringWithBreak(builder,
				flightCreateTripResponse.flightRules.rulesToText.get(RulesKeys.LCC_CHECKIN_TEXT.getKey()));
			appendStringWithBreak(builder,
				flightCreateTripResponse.flightRules.rulesToText.get(RulesKeys.LCC_LITE_TEXT.getKey()));
			mLccTextView.setText(HtmlCompat.fromHtml(builder.toString()));
			mLccTextView.setVisibility(View.VISIBLE);
		}
	}

}

