package com.expedia.bookings.test.robolectric;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.section.CountrySpinnerAdapter;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.widget.TextView;
import com.expedia.bookings.widget.shared.BillingDetailsPaymentWidget;
import com.expedia.vm.PaymentViewModel;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricRunner.class)
public class RailPaymentWidgetTest {

	private BillingDetailsPaymentWidget paymentWidget;
	private Activity activity;

	@Before
	public void before() {
		activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_Packages);
		paymentWidget = (BillingDetailsPaymentWidget) LayoutInflater.from(activity)
			.inflate(R.layout.billing_details_payment_widget, null);
		paymentWidget.setViewmodel(new PaymentViewModel(activity));
	}

	@Test
	public void testShowBillingStateFieldWhenRequired() {
		paymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.RAILS);
		paymentWidget.getCardInfoContainer().performClick();

		SectionLocation locationWidget = (SectionLocation) paymentWidget.findViewById(R.id.section_location_address);
		Spinner countrySpinner = (Spinner) locationWidget.findViewById(R.id.edit_country_spinner);

		CountrySpinnerAdapter countryAdapter = (CountrySpinnerAdapter) countrySpinner.getAdapter();
		int position = countryAdapter.getPositionByCountryThreeLetterCode("FRA");

		countrySpinner.setSelection(position);
		assertFalse(paymentWidget.isStateRequired());

		position = countryAdapter.getPositionByCountryThreeLetterCode("USA");
		countrySpinner.setSelection(position);
		assertTrue(paymentWidget.isStateRequired());

		position = countryAdapter.getPositionByCountryThreeLetterCode("PRY");
		countrySpinner.setSelection(position);
		assertFalse(paymentWidget.isStateRequired());

		position = countryAdapter.getPositionByCountryThreeLetterCode("CAN");
		countrySpinner.setSelection(position);
		assertTrue(paymentWidget.isStateRequired());
	}

	@Test
	public void testCardFeeDisclaimerIsDisplayed() {
		paymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.RAILS);
		paymentWidget.getCardInfoContainer().performClick();

		TextView creditCardFeeDisclaimer = (TextView) paymentWidget.findViewById(R.id.card_fee_disclaimer);
		assertTrue(creditCardFeeDisclaimer.getVisibility() == View.VISIBLE);
	}

	@Test
	public void testCardFeeDisclaimerIsNotDisplayedForPackages() {
		paymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.PACKAGES);
		paymentWidget.getCardInfoContainer().performClick();

		TextView creditCardFeeDisclaimer = (TextView) paymentWidget.findViewById(R.id.card_fee_disclaimer);
		assertTrue(creditCardFeeDisclaimer.getVisibility() == View.GONE);
	}

}
