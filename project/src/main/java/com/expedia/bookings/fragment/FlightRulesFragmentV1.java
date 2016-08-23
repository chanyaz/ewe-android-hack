package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Rule;

public class FlightRulesFragmentV1 extends BaseRulesFragment {

	private FlightTrip mFlightTrip;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFlightTrip = Db.getTripBucket().getFlight().getFlightTrip();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		cancellationPolicyContainer.setVisibility(View.GONE);
		if (mFlightTrip != null) {
			Rule completeRule = mFlightTrip.getRule(RulesKeys.COMPLETE_PENALTY_RULES.getKey());
			setRulesAndRestrictionHeader(v, completeRule.getUrl());
			populateHeaderRows(v);
			populateBody(v);
			Rule penaltyRule = mFlightTrip.getRule(RulesKeys.COMPLETE_PENALTY_RULES.getKey());
			populateTextViewThatLooksLikeAUrlThatOpensAWebViewActivity(penaltyRule.getText(), penaltyRule.getUrl()
				, mCompletePenaltyRulesTextView);

			Rule liabilityRule = mFlightTrip.getRule(RulesKeys.AIRLINE_LIABILITY_LIMITATIONS.getKey());
			populateTextViewThatLooksLikeAUrlThatOpensAWebViewActivity(liabilityRule.getText(), liabilityRule.getUrl()
				, mLiabilitiesLinkTextView);

			Rule airlineFeeRule = mFlightTrip.getRule(RulesKeys.ADDITIONAL_AIRLINE_FEES.getKey());
			populateTextViewThatLooksLikeAUrlThatOpensAWebViewActivity(airlineFeeRule.getText(), airlineFeeRule.getUrl()
				, mAdditionalFeesTextView);

			populateLccInfo();
			mFareInformation.setText(R.string.fare_information);
		}

		return v;
	}


	@Override
	String constructHtmlBodySectionOne() {
		StringBuilder rulesBodyBuilder = new StringBuilder();

		if (mFlightTrip != null) {
			// intro rule
			Rule introRule = mFlightTrip.getRule(RulesKeys.CANCEL_CHANGE_INTRODUCTION_TEXT.getKey());
			appendStringWithBreak(rulesBodyBuilder, introRule);

			// refundability
			Rule refundRule = mFlightTrip.getRule(RulesKeys.REFUNDABILITY_TEXT.getKey());
			appendBodyWithBoldedRule(rulesBodyBuilder, refundRule);

			// change penalty
			Rule penaltyRule = mFlightTrip.getRule(RulesKeys.CHANGE_PENALTY_TEXT.getKey());
			appendBodyWithRuleWithoutBreaks(rulesBodyBuilder, penaltyRule);


		}
		return rulesBodyBuilder.toString();
	}

	@Override
	void populateLccInfo() {
		StringBuilder builder = new StringBuilder();
		if (mFlightTrip != null && mFlightTrip.getRule(RulesKeys.LCC_IMPORTANT_TEXT.getKey()) != null) {
			appendStringWithBreak(builder, mFlightTrip.getRule(RulesKeys.LCC_IMPORTANT_TEXT.getKey()));
			appendStringWithBreak(builder, mFlightTrip.getRule(RulesKeys.LCC_CHECKIN_TEXT.getKey()));
			appendStringWithBreak(builder, mFlightTrip.getRule(RulesKeys.LCC_LITE_TEXT.getKey()));
			mLccTextView.setText(Html.fromHtml(builder.toString()));
			mLccTextView.setVisibility(View.VISIBLE);
		}
	}

}
