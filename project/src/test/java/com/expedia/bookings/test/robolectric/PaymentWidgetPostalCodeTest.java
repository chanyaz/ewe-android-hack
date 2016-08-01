package com.expedia.bookings.test.robolectric;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.PaymentWidget;
import com.expedia.vm.PaymentViewModel;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class PaymentWidgetPostalCodeTest {

	@Test
	public void testPostalCodeVisible() {
		PointOfSale.getPointOfSale().setRequiresHotelPostalCode(true);
		PaymentWidget paymentWidget = getPaymentWidgetForHotels();
		SectionLocation postalCodeWidget = (SectionLocation) paymentWidget.findViewById(R.id.section_location_address);
		assertEquals(postalCodeWidget.getVisibility(), View.VISIBLE);
	}

	@Test
	public void testPostalCodeNotVisible() {
		PointOfSale.getPointOfSale().setRequiresHotelPostalCode(false);
		PaymentWidget paymentWidget = getPaymentWidgetForHotels();
		SectionLocation postalCodeWidget = (SectionLocation) paymentWidget.findViewById(R.id.section_location_address);
		assertEquals(postalCodeWidget.getVisibility(), View.GONE);
	}

	private PaymentWidget getPaymentWidgetForHotels() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_Hotels);
		Ui.getApplication(activity).defaultHotelComponents();

		PaymentWidget paymentWidget = (PaymentWidget) LayoutInflater.from(activity)
			.inflate(R.layout.payment_widget, null);
		paymentWidget.setViewmodel(new PaymentViewModel(activity));
		paymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.HOTELS);
		return paymentWidget;
	}


}
