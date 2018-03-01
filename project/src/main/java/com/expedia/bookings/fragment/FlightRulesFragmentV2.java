package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.flights.FlightCreateTripResponse;
import com.expedia.bookings.data.flights.FlightTripDetails;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Strings;
import com.squareup.phrase.Phrase;

public class FlightRulesFragmentV2 extends BaseRulesFragment {

	private FlightCreateTripResponse flightCreateTripResponse;
	private FlightTripDetails.FlightEvolable flightEvolableUrls;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		flightCreateTripResponse = Db.getTripBucket().getFlightV2().flightCreateTripResponse;
		if (AbacusFeatureConfigManager.isBucketedForTest(getContext(), AbacusUtils.EBAndroidAppFlightsEvolable)) {
			fetchEvolableDetails();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View fragmentView = super.onCreateView(inflater, container, savedInstanceState);
		cancellationPolicyContainer.setVisibility(View.GONE);
		if (flightCreateTripResponse != null) {
			String completeRuleUrl = flightCreateTripResponse.flightRules.rulesToUrl
				.get(RulesKeys.COMPLETE_PENALTY_RULES.getKey());
			if (flightEvolableUrls != null) {
				String evolablePenaltyRuleUrl = flightEvolableUrls.evolablePenaltyRulesUrl;
				if (Strings.isNotEmpty(evolablePenaltyRuleUrl)) {
					completeRuleUrl = evolablePenaltyRuleUrl;
				}
			}
			setRulesAndRestrictionHeader(fragmentView, completeRuleUrl);
			populateHeaderRows(fragmentView);
			if (flightEvolableUrls == null) {
				populateBody(fragmentView);
			}
			else {
				populateBody(fragmentView, constructHtmlBodyEvolableSectionOne());
			}
			populateLccInfo();
			String completePenaltyRuleText = flightCreateTripResponse.flightRules.rulesToText
				.get(RulesKeys.COMPLETE_PENALTY_RULES.getKey());
			String completePenaltyRuleUrl = flightCreateTripResponse.flightRules.rulesToUrl
				.get(RulesKeys.COMPLETE_PENALTY_RULES.getKey());
			if (flightEvolableUrls != null) {
				completePenaltyRuleText = getResources().getString(R.string.evolable_legal_refund_info);
				completePenaltyRuleUrl = "";
			}
			if (Strings.isNotEmpty(completePenaltyRuleUrl)) {
				populateTextViewWithBreakThatLooksLikeAUrlThatOpensAWebViewActivity(
					completePenaltyRuleText, completePenaltyRuleUrl, mCompletePenaltyRulesTextView);
			}
			else {
				populateTextViewWithBreak(completePenaltyRuleText, mCompletePenaltyRulesTextView);
			}

			String liabilityRuleText = flightCreateTripResponse.flightRules.rulesToText
				.get(RulesKeys.AIRLINE_LIABILITY_LIMITATIONS.getKey());
			String liabilityRuleUrl = flightCreateTripResponse.flightRules.rulesToUrl
				.get(RulesKeys.AIRLINE_LIABILITY_LIMITATIONS.getKey());

			populateTextViewWithBreakThatLooksLikeAUrlThatOpensAWebViewActivity(
				liabilityRuleText, liabilityRuleUrl, mLiabilitiesLinkTextView);

			String airlineFeeRuleText = flightCreateTripResponse.flightRules.rulesToText
				.get(RulesKeys.ADDITIONAL_AIRLINE_FEES.getKey());
			String airlineFeeRuleUrl = flightCreateTripResponse.flightRules.rulesToUrl
				.get(RulesKeys.ADDITIONAL_AIRLINE_FEES.getKey());
			if (flightEvolableUrls != null) {
				String evolableCancellationChargeUrl = flightEvolableUrls.evolableCancellationChargeUrl;
				if (Strings.isEmpty(evolableCancellationChargeUrl)) {
					airlineFeeRuleUrl = "";
				}
				else {
					airlineFeeRuleUrl = evolableCancellationChargeUrl;
				}
			}
			if (Strings.isNotEmpty(airlineFeeRuleUrl)) {
				populateTextViewWithBreakThatLooksLikeAUrlThatOpensAWebViewActivity(
					airlineFeeRuleText, airlineFeeRuleUrl, mAdditionalFeesTextView);
			}
			else {
				populateTextViewWithBreak(airlineFeeRuleText, mAdditionalFeesTextView);
			}

			addGeneralRule();
			mFareInformation.setText(R.string.fare_information);
		}

		return fragmentView;
	}

	private void addGeneralRule() {
		String generalConditionText = flightCreateTripResponse.flightRules.rulesToText.get(RulesKeys.GENERAL_CONDITIONS.getKey());
		String generalConditionUrl = flightCreateTripResponse.flightRules.rulesToUrl.get(RulesKeys.GENERAL_CONDITIONS.getKey());
		populateTextViewThatLooksLikeAUrlThatOpensAWebViewActivity(generalConditionText, generalConditionUrl, mGeneralConditionTextView);
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

	private SpannableStringBuilder constructHtmlBodyEvolableSectionOne() {
		String cancellationText = Phrase.from(getContext(), R.string.evolable_legal_cancellation_info_TEMPLATE)
				.put("cancellation_charge_link", flightEvolableUrls.evolablePenaltyRulesUrl)
				.format().toString();

		SpannableStringBuilder builder = StrUtils.getSpannableTextByColor(cancellationText,
				ContextCompat.getColor(getContext(), R.color.flight_primary_color), true);
		return builder;
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

	private void fetchEvolableDetails() {
		if (flightCreateTripResponse != null) {
			FlightTripDetails.FlightOffer flightOffer = flightCreateTripResponse.getOffer();
			if (flightOffer != null && flightOffer.isEvolable && flightOffer.evolableUrls != null) {
					this.flightEvolableUrls = flightOffer.evolableUrls;
			}
		}
	}
}

