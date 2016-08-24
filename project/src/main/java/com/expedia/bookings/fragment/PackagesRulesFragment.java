package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.packages.PackageCreateTripResponse;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;

public class PackagesRulesFragment extends BaseRulesFragment {

	private PackageCreateTripResponse packageCreateTripResponse;
	private PackageCreateTripResponse.FlightProduct flightCreateTripResponse;
	private HotelOffersResponse.HotelRoomResponse hotelRoomResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		packageCreateTripResponse = Db.getTripBucket().getPackage().mPackageTripResponse;
		flightCreateTripResponse = packageCreateTripResponse.packageDetails.flight;
		hotelRoomResponse = packageCreateTripResponse.packageDetails.hotel.hotelRoomResponse;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		if (flightCreateTripResponse != null) {
			String completeRuleUrl = packageCreateTripResponse.packageRulesAndRestrictions;
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

			mFareInformation.setText(R.string.packages_fare_information);
		}

		String cancellationPolicy = "";
		if (hotelRoomResponse != null) {
			cancellationPolicy = hotelRoomResponse.cancellationPolicy;
		}

		if (Strings.isNotEmpty(cancellationPolicy)) {
			cancellationPolicyContainer.setVisibility(View.VISIBLE);
			TextView cancellationPolicyTextView = Ui.findView(v, R.id.cancellation_policy_text_view);
			cancellationPolicyTextView.setText(Html.fromHtml(cancellationPolicy));
		}

		String insuranceStatement = PointOfSale.getPointOfSale().getInsuranceStatement();
		if (Strings.isNotEmpty(insuranceStatement)) {
			LinearLayout mInsuranceInformationContainer = Ui.findView(v, R.id.insurance_information_container);
			TextView mInsuranceInformationTextView = Ui.findView(v, R.id.insurance_information_text_view);
			mInsuranceInformationContainer.setVisibility(View.VISIBLE);
			mInsuranceInformationTextView.setText(insuranceStatement);
		}
		return v;
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
			mLccTextView.setText(Html.fromHtml(builder.toString()));
			mLccTextView.setVisibility(View.VISIBLE);
		}
	}

}
