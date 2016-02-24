package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.TripBucketItemPackages;
import com.expedia.bookings.data.ValidPayment;
import com.expedia.bookings.data.packages.PackageCreateTripResponse;
import com.expedia.bookings.widget.PackagePaymentWidget;

import butterknife.ButterKnife;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricRunner.class)
public class PackagePaymentWidgetTest {
	private PackagePaymentWidget packagePaymentWidget;
	private Activity activity;
	private EditText securityCodeInput;

	@Before
	public void before() {
		activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_Packages);
		packagePaymentWidget = (PackagePaymentWidget) LayoutInflater.from(activity)
			.inflate(R.layout.package_payment_widget, null);
	}

	@Test
	public void testCreditCardSecurityCodeWidget() {
		assertNotNull(packagePaymentWidget);
		ButterKnife.inject(activity);
		securityCodeInput = (EditText) packagePaymentWidget.findViewById(R.id.edit_creditcard_cvv);
		assertNotNull(securityCodeInput);
	}

	@Test
	public void testNoTripValidator() {
		packagePaymentWidget.setLineOfBusiness(LineOfBusiness.PACKAGES);
		packagePaymentWidget.setCreditCardRequired(true);
		packagePaymentWidget.setExpanded(true);

		Db.getTripBucket().clear(LineOfBusiness.PACKAGES);

		BillingInfo info = new BillingInfo();
		info.setNumberAndDetectType("345104799171123");
		info.setNameOnCard("Expedia Chicago");
		info.setExpirationDate(new LocalDate(2017, 1, 1));
		info.setSecurityCode("1234");

		Location location = new Location();
		location.setCity("San Francisco");
		location.setCountryCode("USA");
		location.addStreetAddressLine("500 W Madison st");
		location.setPostalCode("60661");
		location.setStateCode("IL");
		info.setLocation(location);
		packagePaymentWidget.sectionBillingInfo.bind(info);

		assertFalse(packagePaymentWidget.sectionBillingInfo.performValidation());
	}


	@Test
	public void testAmexSecurityCodeValidator() {
		packagePaymentWidget.setLineOfBusiness(LineOfBusiness.PACKAGES);
		packagePaymentWidget.setCreditCardRequired(true);
		packagePaymentWidget.setExpanded(true);

		PackageCreateTripResponse response = new PackageCreateTripResponse();
		ValidPayment amexPayment = new ValidPayment();
		amexPayment.name = "AmericanExpress";
		List<ValidPayment> validFormsOfPayment = new ArrayList<>();
		ValidPayment.addValidPayment(validFormsOfPayment, amexPayment);
		response.setValidFormsOfPayment(validFormsOfPayment);
		TripBucketItemPackages trip = new TripBucketItemPackages(response);
		Db.getTripBucket().clear(LineOfBusiness.PACKAGES);
		Db.getTripBucket().add(trip);

		BillingInfo info = new BillingInfo();
		info.setNumberAndDetectType("345104799171123");
		info.setNameOnCard("Expedia Chicago");
		info.setExpirationDate(new LocalDate(2017, 1, 1));
		info.setSecurityCode("123");

		Location location = new Location();
		location.setCity("San Francisco");
		location.setCountryCode("USA");
		location.addStreetAddressLine("500 W Madison st");
		location.setPostalCode("60661");
		location.setStateCode("IL");
		info.setLocation(location);
		packagePaymentWidget.sectionBillingInfo.bind(info);
		assertFalse(packagePaymentWidget.sectionBillingInfo.performValidation());

		info.setSecurityCode("1234");
		packagePaymentWidget.sectionBillingInfo.bind(info);
		assertTrue(packagePaymentWidget.sectionBillingInfo.performValidation());
	}

	@Test
	public void testVisaSecurityCodeValidator() {
		packagePaymentWidget.setLineOfBusiness(LineOfBusiness.PACKAGES);
		packagePaymentWidget.setCreditCardRequired(true);
		packagePaymentWidget.setExpanded(true);

		PackageCreateTripResponse response = new PackageCreateTripResponse();
		ValidPayment visaPayment = new ValidPayment();
		visaPayment.name = "Visa";
		List<ValidPayment> validFormsOfPayment = new ArrayList<>();
		ValidPayment.addValidPayment(validFormsOfPayment, visaPayment);
		response.setValidFormsOfPayment(validFormsOfPayment);
		TripBucketItemPackages trip = new TripBucketItemPackages(response);
		Db.getTripBucket().clear(LineOfBusiness.PACKAGES);
		Db.getTripBucket().add(trip);

		BillingInfo info = new BillingInfo();
		info.setNumberAndDetectType("4284306858654528");
		info.setNameOnCard("Expedia Chicago");
		info.setExpirationDate(new LocalDate(2017, 1, 1));
		info.setSecurityCode("1234");

		Location location = new Location();
		location.setCity("San Francisco");
		location.setCountryCode("USA");
		location.addStreetAddressLine("114 Sansome St.");
		location.setPostalCode("94109");
		location.setStateCode("CA");
		info.setLocation(location);
		packagePaymentWidget.sectionBillingInfo.bind(info);
		assertFalse(packagePaymentWidget.sectionBillingInfo.performValidation());

		info.setSecurityCode("123");
		packagePaymentWidget.sectionBillingInfo.bind(info);
		assertTrue(packagePaymentWidget.sectionBillingInfo.performValidation());
	}
}
