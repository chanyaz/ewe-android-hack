package com.expedia.bookings.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowTextView;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.PointOfSaleTestConfiguration;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.widget.FrameLayout;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class FlightUtilsTest {

	Context context;
	TextView feesTextView;
	ViewGroup feesViewGroup;
	TextView secondaryFeesTextView;

	@Mock
	FlightUtils.OnBaggageFeeViewClicked mockBaggageFeeClickedListener;

	@Before
	public void setup() {
		context = RuntimeEnvironment.application;
		feesTextView = new TextView(context);
		feesViewGroup = new FrameLayout(context);
		secondaryFeesTextView = new TextView(context);

		mockBaggageFeeClickedListener = Mockito.mock(FlightUtils.OnBaggageFeeViewClicked.class);
		Mockito.doNothing().when(mockBaggageFeeClickedListener)
			.onBaggageFeeViewClicked(Mockito.anyString(), Mockito.anyString());
	}


	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA, MultiBrand.ORBITZ})
	public void testConfigureBaggageFeeViewsNoPaymentFees() {
		FlightTrip trip = new FlightTrip();
		trip.setMayChargeObFees(false);

		FlightLeg leg = new FlightLeg();
		leg.setBaggageFeesUrl("baggageFeesUrl");
		leg.setHasBagFee(false);

		// phone
		FlightUtils.configureBaggageFeeViews(context, trip, leg, feesTextView, feesViewGroup, secondaryFeesTextView,
			true, null, mockBaggageFeeClickedListener);
		verifyConfigureBaggageFeeViewsNoPaymentFees(R.string.baggage_fee_info, R.drawable.ic_suitcase_small, "baggageFeesUrl");
		// tablet
		leg.setBaggageFeesUrl("baggageFeesUrl2");
		FlightUtils.configureBaggageFeeViews(context, trip, leg, feesTextView, feesViewGroup, secondaryFeesTextView,
			false, null, mockBaggageFeeClickedListener);
		verifyConfigureBaggageFeeViewsNoPaymentFees(R.string.baggage_fee_info, R.drawable.ic_tablet_baggage_fees, "baggageFeesUrl2");

		leg.setHasBagFee(true);
		// phone
		leg.setBaggageFeesUrl("baggageFeesUrl3");
		FlightUtils.configureBaggageFeeViews(context, trip, leg, feesTextView, feesViewGroup, secondaryFeesTextView,
			true, null, mockBaggageFeeClickedListener);
		verifyConfigureBaggageFeeViewsNoPaymentFees(R.string.checked_baggage_not_included, R.drawable.ic_suitcase_baggage_fee, "baggageFeesUrl3");
		// tablet
		leg.setBaggageFeesUrl("baggageFeesUrl4");
		FlightUtils.configureBaggageFeeViews(context, trip, leg, feesTextView, feesViewGroup, secondaryFeesTextView,
			false, null, mockBaggageFeeClickedListener);
		verifyConfigureBaggageFeeViewsNoPaymentFees(R.string.checked_baggage_not_included, R.drawable.ic_tablet_baggage_check_fees, "baggageFeesUrl4");

		leg.addSegment(createSpiritFlight());
		// phone
		leg.setBaggageFeesUrl("baggageFeesUrl5");
		FlightUtils.configureBaggageFeeViews(context, trip, leg, feesTextView, feesViewGroup, secondaryFeesTextView,
			true, null, mockBaggageFeeClickedListener);
		verifyConfigureBaggageFeeViewsNoPaymentFees(R.string.carry_on_baggage_fees_apply, R.drawable.ic_suitcase_baggage_fee, "baggageFeesUrl5");
		// tablet
		leg.setBaggageFeesUrl("baggageFeesUrl6");
		FlightUtils.configureBaggageFeeViews(context, trip, leg, feesTextView, feesViewGroup, secondaryFeesTextView,
			false, null, mockBaggageFeeClickedListener);
		verifyConfigureBaggageFeeViewsNoPaymentFees(R.string.carry_on_baggage_fees_apply, R.drawable.ic_tablet_baggage_check_fees, "baggageFeesUrl6");
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA, MultiBrand.ORBITZ})
	public void testConfigureBaggageFeeViewsWithPaymentFees() {
		FlightTrip trip = new FlightTrip();
		trip.setMayChargeObFees(true);

		FlightLeg leg = new FlightLeg();
		leg.setBaggageFeesUrl("baggageFeesUrl");
		leg.setHasBagFee(false);

		FlightSearchResponse flightSearchResponse = new FlightSearchResponse();
		flightSearchResponse.addTrip(trip);
		Db.getFlightSearch().setSearchResponse(flightSearchResponse);

		// phone
		FlightUtils.configureBaggageFeeViews(context, trip, leg, feesTextView, feesViewGroup, secondaryFeesTextView,
			true, null, mockBaggageFeeClickedListener);
		verifyConfigureBaggageFeeViewsWithPaymentFees(R.string.payment_and_baggage_fees_may_apply, R.drawable.ic_payment_fee);
		// tablet
		FlightUtils.configureBaggageFeeViews(context, trip, leg, feesTextView, feesViewGroup, secondaryFeesTextView,
			false, null, mockBaggageFeeClickedListener);
		verifyConfigureBaggageFeeViewsWithPaymentFees(R.string.payment_and_baggage_fees_may_apply, R.drawable.ic_tablet_payment_fees);

		PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_with_airline_payment_fees.json");
		// phone
		SettingUtils.save(context, R.string.preference_payment_legal_message, true);
		FlightUtils.configureBaggageFeeViews(context, trip, leg, feesTextView, feesViewGroup, secondaryFeesTextView,
			true, null, mockBaggageFeeClickedListener);
		if (FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_payment_legal_message)) {
			verifyConfigureBaggageFeeViewsWithPaymentFees(R.string.airline_fee_apply, R.drawable.ic_payment_fee);
		}

		SettingUtils.save(context, R.string.preference_payment_legal_message, false);
		FlightUtils.configureBaggageFeeViews(context, trip, leg, feesTextView, feesViewGroup, secondaryFeesTextView,
			true, null, mockBaggageFeeClickedListener);
		if (!FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_payment_legal_message)) {
			verifyConfigureBaggageFeeViewsWithPaymentFees(R.string.airline_fee_notice_payment, R.drawable.ic_payment_fee);
		}

		// tablet
		SettingUtils.save(context, R.string.preference_payment_legal_message, true);
		FlightUtils.configureBaggageFeeViews(context, trip, leg, feesTextView, feesViewGroup, secondaryFeesTextView,
			false, null, mockBaggageFeeClickedListener);
		if (FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_payment_legal_message)) {
			verifyConfigureBaggageFeeViewsWithPaymentFees(R.string.airline_fee_apply, R.drawable.ic_tablet_payment_fees);
		}

		SettingUtils.save(context, R.string.preference_payment_legal_message, false);
		FlightUtils.configureBaggageFeeViews(context, trip, leg, feesTextView, feesViewGroup, secondaryFeesTextView,
			false, null, mockBaggageFeeClickedListener);
		if (!FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_payment_legal_message)) {
			verifyConfigureBaggageFeeViewsWithPaymentFees(R.string.airline_fee_notice_payment, R.drawable.ic_tablet_payment_fees);
		}
	}

	private void verifyConfigureBaggageFeeViewsNoPaymentFees(@StringRes int expectedFeeStringId,
			@DrawableRes int expectedFeeDrawableId, String baggagesFeesUrl) {
		assertEquals(context.getString(expectedFeeStringId), feesTextView.getText());
		ShadowTextView shadow = Shadows.shadowOf(feesTextView);
		assertEquals(expectedFeeDrawableId, shadow.getCompoundDrawablesWithIntrinsicBoundsLeft());
		assertEquals(View.GONE, secondaryFeesTextView.getVisibility());
		feesViewGroup.performClick();
		Mockito.verify(mockBaggageFeeClickedListener)
			.onBaggageFeeViewClicked(context.getString(R.string.baggage_fees), baggagesFeesUrl);
	}

	private void verifyConfigureBaggageFeeViewsWithPaymentFees(@StringRes int expectedFeeStringId,
			@DrawableRes int expectedFeeDrawableId) {
		assertEquals(context.getString(expectedFeeStringId), secondaryFeesTextView.getText());
		assertEquals(View.VISIBLE, secondaryFeesTextView.getVisibility());
		ShadowTextView shadow = Shadows.shadowOf(secondaryFeesTextView);
		assertEquals(expectedFeeDrawableId, shadow.getCompoundDrawablesWithIntrinsicBoundsLeft());
	}

	private Flight createSpiritFlight() {
		FlightCode fc = new FlightCode();
		fc.mAirlineCode = "NK";
		fc.mNumber = "123";

		Flight f = new Flight();
		f.addFlightCode(fc, Flight.F_PRIMARY_AIRLINE_CODE);

		return f;
	}
}
