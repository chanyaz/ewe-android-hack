package com.expedia.bookings.utils;

import org.joda.time.LocalDate;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.trips.TripBucketItemFlight;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.fragment.FlightAdditionalFeesDialogFragment;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.mobiata.flightlib.utils.FormatUtils;
import com.squareup.phrase.Phrase;

public class FlightUtils {

	/**
	 * Returns the best Terminal/Gate string available, depending on the waypoint.
	 * If the gate and terminal are both known, something like "Terminal X, Gate Y",
	 * otherwise something less specific. If neither is known, "Gate TBD".
	 */
	public static String getTerminalGateString(Context context, Waypoint waypoint) {
		Resources res = context.getResources();
		if (waypoint.hasGate() && waypoint.hasTerminal()) {
			return waypoint.isInternationalTerminal()
				? res.getString(R.string.International_Terminal_Gate_X_TEMPLATE, waypoint.getGate())
				: res.getString(R.string.Terminal_X_Gate_Y_TEMPLATE, waypoint.getTerminal(), waypoint.getGate());
		}
		else if (waypoint.hasGate()) {
			//gate only
			return res.getString(R.string.Gate_X_TEMPLATE, waypoint.getGate());
		}
		else if (waypoint.hasTerminal()) {
			//terminal only
			return waypoint.isInternationalTerminal()
				? res.getString(R.string.International_Terminal)
				: res.getString(R.string.Terminal_X_TEMPLATE, waypoint.getTerminal());
		}
		else {
			//no gate or terminal info
			return res.getString(R.string.Gate_To_Be_Determined_abbrev);
		}
	}

	public static String formatDistance(Context context, FlightLeg leg, boolean longTemplate) {
		int flags = PointOfSale.getPointOfSale().getDistanceUnit() == Distance.DistanceUnit.MILES
			? FormatUtils.F_IMPERIAL
			: FormatUtils.F_METRIC;
		if (longTemplate) {
			flags |= FormatUtils.F_LONG;
		}
		return FormatUtils.formatDistance(context, leg.getDistanceInMiles(), flags);
	}

	public static String formatDuration(Context context, FlightLeg leg) {
		return DateTimeUtils.formatDuration(context.getResources(), (int) (leg.getDuration() / 60000));
	}

	/**
	 * Returns the string meant to be displayed below the slide-to-purchase view; i.e. the final
	 * prompt displayed before the card is actually charged. We want this message to be consistent
	 * between phone and tablet.
	 *
	 * @param context context
	 * @param flightItem the flight item to be purchased
	 */
	public static String getSlideToPurchaseString(Context context, TripBucketItemFlight flightItem) {
		Money totalFare = flightItem.getFlightTrip().getTotalFareWithCardFee(Db.getBillingInfo(), flightItem);
		String template = context.getString(
			PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod()
				? R.string.your_card_will_be_charged_plus_airline_fee_template
				: R.string.your_card_will_be_charged_template);
		return Phrase.from(template).put("dueamount", totalFare.getFormattedMoney()).format().toString();
	}

	public static Spanned getCardFeeLegalText(Context context) {
		Spanned cardFeeHtml = Html.fromHtml(context.getResources().getString(R.string.airline_notice_fee_added_tablet));
		SpannableStringBuilder cardFeeSb = new SpannableStringBuilder(cardFeeHtml);

		UnderlineSpan underlineSpan = cardFeeSb.getSpans(0, cardFeeHtml.length(), UnderlineSpan.class)[0];
		int spanStart = cardFeeSb.getSpanStart(underlineSpan);
		int spanEnd = cardFeeSb.getSpanEnd(underlineSpan);
		int color = R.color.tablet_legal_blurb_text_color;
		cardFeeSb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, color)), spanStart, spanEnd,
			Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		return cardFeeSb;
	}

	////////////////////////////////////////////
	// Flight Details Baggage Fees

	public interface OnBaggageFeeViewClicked {
		void onBaggageFeeViewClicked(String title, String url);
	}

	public static void configureBaggageFeeViews(Context context, final FlightTrip trip, final FlightLeg leg,
		TextView feesTv, ViewGroup mFeesContainer, TextView secondaryFeesTv, boolean isPhone,
		final FragmentManager fragmentManager, final OnBaggageFeeViewClicked baggageFeeViewClickedListener) {

		// Configure the first TextView, "Baggage Fee Information"
		int textViewResId;
		int drawableResId;
		if (leg.isSpirit()) {
			textViewResId = R.string.carry_on_baggage_fees_apply;
			drawableResId = isPhone ? R.drawable.ic_suitcase_baggage_fee : R.drawable.ic_tablet_baggage_check_fees;
		}
		else if (leg.hasBagFee()) {
			textViewResId = R.string.checked_baggage_not_included;
			drawableResId = isPhone ? R.drawable.ic_suitcase_baggage_fee : R.drawable.ic_tablet_baggage_check_fees;
		}
		else {
			textViewResId = R.string.baggage_fee_info;
			drawableResId =  isPhone ? R.drawable.ic_suitcase_small : R.drawable.ic_tablet_baggage_fees;
		}

		feesTv.setText(textViewResId);
		feesTv.setAllCaps(true);
		feesTv.setCompoundDrawablesWithIntrinsicBounds(drawableResId, 0, 0, 0);

		boolean airlinesChargePaymentFees = PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod();
		if (airlinesChargePaymentFees || trip.getMayChargeObFees()) {
			final String feeString;
			final String feeUrl;

			if (airlinesChargePaymentFees) {
				drawableResId = isPhone ? R.drawable.ic_payment_fee : R.drawable.ic_tablet_payment_fees;
				textViewResId = R.string.airline_fee_notice_payment;
				feeString = context.getString(R.string.Airline_fee);
				feeUrl = PointOfSale.getPointOfSale().getAirlineFeeBasedOnPaymentMethodTermsAndConditionsURL();
			}
			else {
				// trip mayChargeObFees
				drawableResId = isPhone ? R.drawable.ic_payment_fee : R.drawable.ic_tablet_payment_fees;
				textViewResId = R.string.payment_and_baggage_fees_may_apply;
				feeString = context.getString(R.string.payment_processing_fees);
				feeUrl = Db.getFlightSearch().getSearchResponse().getObFeesDetails();
			}

			secondaryFeesTv.setCompoundDrawablesWithIntrinsicBounds(drawableResId, 0, 0 ,0);
			secondaryFeesTv.setVisibility(View.VISIBLE);
			secondaryFeesTv.setText(textViewResId);
			secondaryFeesTv.setAllCaps(true);

			mFeesContainer.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					FlightAdditionalFeesDialogFragment dialogFragment = FlightAdditionalFeesDialogFragment.newInstance(
						leg.getBaggageFeesUrl(), feeUrl, feeString);
					dialogFragment.show(fragmentManager, "additionalFeesDialog");
				}
			});
		}
		else {
			secondaryFeesTv.setVisibility(View.GONE);
			final String baggageFeesString = context.getString(R.string.baggage_fees);
			mFeesContainer.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					baggageFeeViewClickedListener.onBaggageFeeViewClicked(baggageFeesString, leg.getBaggageFeesUrl());
				}
			});
		}
	}

	 /*
	  * Helper method to check if it's valid to start the flight search.
	  */
	public static boolean dateRangeSupportsFlightSearch(Context context) {
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();
		LocalDate searchDate = params.getDepartureDate();
		LocalDate arrivalDate = params.getReturnDate();
		LocalDate maxSearchDate = LocalDate.now()
			.plusDays(context.getResources().getInteger(R.integer.calendar_max_days_flight_search) + 1);
		return arrivalDate != null ? arrivalDate.isBefore(maxSearchDate) : searchDate.isBefore(maxSearchDate);
	}
}
