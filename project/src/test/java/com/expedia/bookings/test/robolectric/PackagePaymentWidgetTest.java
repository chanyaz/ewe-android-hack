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
import com.expedia.bookings.data.flights.ValidFormOfPayment;
import com.expedia.bookings.data.packages.PackageCreateTripResponse;
import com.expedia.bookings.data.trips.TripBucketItemPackages;
import com.expedia.bookings.data.utils.ValidFormOfPaymentUtils;
import com.expedia.bookings.widget.packages.BillingDetailsPaymentWidget;
import com.expedia.vm.PaymentViewModel;

import butterknife.ButterKnife;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricRunner.class)
public class PackagePaymentWidgetTest {
	private BillingDetailsPaymentWidget packagePaymentWidget;
	private Activity activity;
	private EditText securityCodeInput;

	@Before
	public void before() {
		activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_Packages);
		packagePaymentWidget = (BillingDetailsPaymentWidget) LayoutInflater.from(activity)
			.inflate(R.layout.billing_details_payment_widget, null);
		packagePaymentWidget.setViewmodel(new PaymentViewModel(activity));
	}

	@Test
	public void testCreditCardSecurityCodeWidget() {
		assertNotNull(packagePaymentWidget);
		ButterKnife.inject(activity);
		securityCodeInput = (EditText) packagePaymentWidget.findViewById(R.id.edit_creditcard_cvv);
		//test for accessibility content description
		assertEquals(securityCodeInput.getContentDescription(),"CVV");
		assertNotNull(securityCodeInput);
	}

	@Test
	public void testNoTripValidator() {
		packagePaymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.PACKAGES);
		packagePaymentWidget.getCardInfoContainer().performClick();

		Db.getTripBucket().clear(LineOfBusiness.PACKAGES);

		BillingInfo info = new BillingInfo();
		info.setNumberAndDetectType("345104799171123");
		info.setNameOnCard("Expedia Chicago");
		info.setExpirationDate(new LocalDate(2017, 1, 1));
		info.setSecurityCode("1234");
		info.setEmail("test@email.com");

		Location location = givenLocation();
		info.setLocation(location);
		packagePaymentWidget.getSectionBillingInfo().bind(info);

		assertFalse(packagePaymentWidget.getSectionBillingInfo().performValidation());
	}


	@Test
	public void testAmexSecurityCodeValidator() {
		packagePaymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.PACKAGES);
		packagePaymentWidget.getCardInfoContainer().performClick();

		givenTripResponse("AmericanExpress");

		BillingInfo info = new BillingInfo();
		info.setNumberAndDetectType("345104799171123");
		info.setNameOnCard("Expedia Chicago");
		info.setExpirationDate(new LocalDate(2017, 1, 1));
		info.setSecurityCode("123");
		info.setEmail("test@email.com");

		Location location = givenLocation();
		info.setLocation(location);
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.getSectionBillingInfo().performValidation());

		info.setSecurityCode("1234");
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertTrue(packagePaymentWidget.getSectionBillingInfo().performValidation());
	}

	@Test
	public void testVisaSecurityCodeValidator() {
		packagePaymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.PACKAGES);
		packagePaymentWidget.getCardInfoContainer().performClick();

		givenTripResponse("Visa");

		BillingInfo info = new BillingInfo();
		info.setNumberAndDetectType("4284306858654528");
		info.setNameOnCard("Expedia Chicago");
		info.setExpirationDate(new LocalDate(2017, 1, 1));
		info.setSecurityCode("1234");
		info.setEmail("test@email.com");

		Location location = givenLocation();
		info.setLocation(location);
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.getSectionBillingInfo().performValidation());

		info.setSecurityCode("123");
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertTrue(packagePaymentWidget.getSectionBillingInfo().performValidation());
	}

	@Test
	public void testEmailValidator() {
		packagePaymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.PACKAGES);
		packagePaymentWidget.getCardInfoContainer().performClick();

		givenTripResponse("AmericanExpress");

		BillingInfo info = new BillingInfo();
		info.setNumberAndDetectType("345104799171123");
		info.setNameOnCard("Expedia Chicago");
		info.setExpirationDate(new LocalDate(2017, 1, 1));
		info.setSecurityCode("1234");

		Location location = givenLocation();
		info.setLocation(location);
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.getSectionBillingInfo().performValidation());

		info.setEmail("qa-ehcc");
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.getSectionBillingInfo().performValidation());

		info.setEmail("qa-ehcc@");
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.getSectionBillingInfo().performValidation());

		info.setEmail("qa-ehcc@mobiata");
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.getSectionBillingInfo().performValidation());

		info.setEmail("TEST@email.com");
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertTrue(packagePaymentWidget.getSectionBillingInfo().performValidation());

		info.setEmail("test@email.com");
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertTrue(packagePaymentWidget.getSectionBillingInfo().performValidation());
	}

	@Test
	public void testSecureCheckoutDisabled() {
		assertFalse("All Hotel A/B tests must be disabled for packages",
			packagePaymentWidget.isSecureToolbarBucketed());
	}

	@Test
	public void testCreditCardHint() {
		assertEquals("All Hotel A/B tests must be disabled for packages",
			packagePaymentWidget.getCreditCardNumberHintResId(), R.string.credit_card_hint);
	}

	@Test
	public void testIsAtLeastPartiallyFilled() {
		packagePaymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.PACKAGES);
		packagePaymentWidget.getCardInfoContainer().performClick();

		BillingInfo info = new BillingInfo();
		Location location = new Location();

		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.isAtLeastPartiallyFilled());
		info.setNumberAndDetectType("345104799171123");
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertTrue(packagePaymentWidget.isAtLeastPartiallyFilled());

		info = new BillingInfo();
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.isAtLeastPartiallyFilled());
		info.setNameOnCard("Expedia Chicago");
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertTrue(packagePaymentWidget.isAtLeastPartiallyFilled());

		info = new BillingInfo();
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.isAtLeastPartiallyFilled());
		info.setSecurityCode("1234");
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertTrue(packagePaymentWidget.isAtLeastPartiallyFilled());

		info = new BillingInfo();
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.isAtLeastPartiallyFilled());
		info.setEmail("test@email.com");
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertTrue(packagePaymentWidget.isAtLeastPartiallyFilled());

		info = new BillingInfo();
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.isAtLeastPartiallyFilled());
		location.setCity("San Francisco");
		info.setLocation(location);
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertTrue(packagePaymentWidget.isAtLeastPartiallyFilled());

		info = new BillingInfo();
		location = new Location();
		info.setLocation(location);
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.isAtLeastPartiallyFilled());
		location.setPostalCode("60661");
		info.setLocation(location);
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertTrue(packagePaymentWidget.isAtLeastPartiallyFilled());

		info = new BillingInfo();
		location = new Location();
		info.setLocation(location);
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.isAtLeastPartiallyFilled());
		location.setStateCode("IL");
		info.setLocation(location);
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertTrue(packagePaymentWidget.isAtLeastPartiallyFilled());

		info = new BillingInfo();
		location = new Location();
		info.setLocation(location);
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.isAtLeastPartiallyFilled());
		location.setCountryCode("USA");
		info.setLocation(location);
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.isAtLeastPartiallyFilled());
	}

	@Test
	public void testIsCompletelyFilled() {
		packagePaymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.PACKAGES);
		packagePaymentWidget.getCardInfoContainer().performClick();

		BillingInfo info = new BillingInfo();
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.isCompletelyFilled());
		info.setNumberAndDetectType("345104799171123");
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.isCompletelyFilled());

		info.setNameOnCard("Expedia Chicago");
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.isCompletelyFilled());

		info.setSecurityCode("1234");
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.isCompletelyFilled());

		info.setEmail("test@email.com");
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertFalse(packagePaymentWidget.isCompletelyFilled());

		Location location = givenLocation();
		info.setLocation(location);
		packagePaymentWidget.getSectionBillingInfo().bind(info);
		assertTrue(packagePaymentWidget.isCompletelyFilled());
	}

	private Location givenLocation() {
		Location location = new Location();
		location.setCity("San Francisco");
		location.setCountryCode("USA");
		location.addStreetAddressLine("500 W Madison st");
		location.setPostalCode("60661");
		location.setStateCode("IL");
		return location;
	}

	private void givenTripResponse(String paymentName) {
		PackageCreateTripResponse response = new PackageCreateTripResponse();
		ValidFormOfPayment amexPayment = new ValidFormOfPayment();
		amexPayment.name = paymentName;
		List<ValidFormOfPayment> validFormsOfPayment = new ArrayList<>();
		ValidFormOfPaymentUtils.addValidPayment(validFormsOfPayment, amexPayment);
		response.setValidFormsOfPayment(validFormsOfPayment);
		TripBucketItemPackages trip = new TripBucketItemPackages(response);
		Db.getTripBucket().clear(LineOfBusiness.PACKAGES);
		Db.getTripBucket().add(trip);
	}
}
