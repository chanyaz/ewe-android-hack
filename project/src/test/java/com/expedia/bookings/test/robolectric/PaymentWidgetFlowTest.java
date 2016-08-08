package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.flights.ValidFormOfPayment;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.hotels.HotelRate;
import com.expedia.bookings.data.trips.TripBucketItemHotelV2;
import com.expedia.bookings.data.utils.ValidFormOfPaymentUtils;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.PaymentWidget;
import com.expedia.bookings.widget.PaymentWidgetV2;
import com.expedia.bookings.widget.StoredCreditCardList;
import com.expedia.vm.PaymentViewModel;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class PaymentWidgetFlowTest {
	private BillingInfo storedCardBillingInfo;
	private BillingInfo tempSavedCardBillingInfo;
	private BillingInfo tempNotSavedCardBillingInfo;

	@Before
	public void before() {

		StoredCreditCard storedCreditCard = new StoredCreditCard();
		storedCreditCard.setCardNumber("4111111111111111");
		storedCreditCard.setType(PaymentType.CARD_AMERICAN_EXPRESS);

		Location location = new Location();
		location.setCity("San Francisco");
		location.setCountryCode("USA");
		location.setDescription("Cool description");
		location.addStreetAddressLine("114 Sansome St.");
		location.setPostalCode("94109");
		location.setStateCode("CA");
		location.setLatitude(37.7833);
		location.setLongitude(122.4167);
		location.setDestinationId("SF");

		BillingInfo billingInfo = new BillingInfo();
		billingInfo = new BillingInfo();
		billingInfo.setEmail("qa-ehcc@mobiata.com");
		billingInfo.setFirstName("JexperCC");
		billingInfo.setLastName("MobiataTestaverde");
		billingInfo.setNameOnCard("JexperCC MobiataTestaverde");
		billingInfo.setNumberAndDetectType("4111111111111111");
		billingInfo.setExpirationDate(LocalDate.now().plusYears(1));
		billingInfo.setSecurityCode("111");
		billingInfo.setTelephone("4155555555");
		billingInfo.setTelephoneCountryCode("1");
		billingInfo.setLocation(location);

		storedCardBillingInfo = new BillingInfo(billingInfo);
		storedCardBillingInfo.setStoredCard(storedCreditCard);

		tempSavedCardBillingInfo = new BillingInfo(billingInfo);

		tempNotSavedCardBillingInfo = new BillingInfo(billingInfo);
		tempNotSavedCardBillingInfo.setNumberAndDetectType("6011111111111111");

		ArrayList<ValidFormOfPayment> validFormsOfPayment = new ArrayList<>();
		ValidFormOfPayment validPayment = new ValidFormOfPayment();
		validPayment.name = "AmericanExpress";
		ValidFormOfPaymentUtils.addValidPayment(validFormsOfPayment, validPayment);
		validPayment = new ValidFormOfPayment();
		validPayment.name = "Visa";
		ValidFormOfPaymentUtils.addValidPayment(validFormsOfPayment, validPayment);
		validPayment = new ValidFormOfPayment();
		validPayment.name = "Discover";
		ValidFormOfPaymentUtils.addValidPayment(validFormsOfPayment, validPayment);

		HotelCreateTripResponse response = new HotelCreateTripResponse();
		response.setValidFormsOfPayment(validFormsOfPayment);
		response.newHotelProductResponse = new HotelCreateTripResponse.HotelProductResponse();
		response.newHotelProductResponse.hotelRoomResponse = new HotelOffersResponse.HotelRoomResponse();
		response.newHotelProductResponse.hotelRoomResponse.rateInfo = new HotelOffersResponse.RateInfo();
		response.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo = new HotelRate();
		response.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.total = 100;
		response.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.currencyCode = "USD";
		response.newHotelProductResponse.hotelRoomResponse.supplierType = "MERCHANT";
		TripBucketItemHotelV2 trip = new TripBucketItemHotelV2(response);
		Db.getTripBucket().clear(LineOfBusiness.HOTELS);
		Db.getTripBucket().add(trip);
	}

	@Test
	public void testCardOptionsNotVisible() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_LX);
		Ui.getApplication(activity).defaultLXComponents();
		PaymentWidget paymentWidget =  (PaymentWidget) LayoutInflater.from(activity)
			.inflate(R.layout.payment_widget, null);
		paymentWidget.setViewmodel(new PaymentViewModel(activity));
		paymentWidget.show(new PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK);
		paymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.LX);
		paymentWidget.getViewmodel().isCreditCardRequired().onNext(true);
		paymentWidget.getCardInfoContainer().performClick();

		LinearLayout paymentOptions = (LinearLayout) paymentWidget.findViewById(R.id.section_payment_options_container);

		//Payment options don't appear on Cars/Lx
		assertEquals(View.GONE, paymentOptions.getVisibility());
	}

	// Enable the below test when we implemented android pay
//	@Test
//	public void testGoogleWallet() {
//		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
//		Ui.getApplication(activity).defaultLXComponents();
//		PaymentWidget paymentWidget =  (PaymentWidget) LayoutInflater.from(activity)
//			.inflate(R.layout.payment_widget_test, null);
//		paymentWidget.setToolbarListener(listener);
//		paymentWidget.setLineOfBusiness(LineOfBusiness.HOTELSV2);
//		paymentWidget.setCreditCardRequired(true);
//		paymentWidget.setExpanded(true);
//
//		LinearLayout paymentOptions = (LinearLayout) paymentWidget.findViewById(R.id.section_payment_options_container);
//		TextView walletOption = (TextView) paymentWidget.findViewById(R.id.payment_option_google_wallet);
//
//		//Payment options appear on new hotels
//		assertEquals(View.VISIBLE, paymentOptions.getVisibility());
//
//		HotelCreateTripResponse response = new HotelCreateTripResponse();
//		response.newHotelProductResponse = new HotelCreateTripResponse.HotelProductResponse();
//		response.newHotelProductResponse.hotelRoomResponse = new HotelOffersResponse.HotelRoomResponse();
//		response.newHotelProductResponse.hotelRoomResponse.rateInfo = new HotelOffersResponse.RateInfo();
//		response.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo = new HotelRate();
//		response.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.total = 1801;
//		response.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.currencyCode = "USD";
//		response.newHotelProductResponse.hotelRoomResponse.supplierType = "MERCHANT";
//		TripBucketItemHotelV2 trip = new TripBucketItemHotelV2(response);
//		Db.getTripBucket().clear(LineOfBusiness.HOTELSV2);
//		Db.getTripBucket().add(trip);
//
//		paymentWidget.setExpanded(true);
//		//Payment options dont appear if google wallet is not supported
//		assertEquals(View.GONE, paymentOptions.getVisibility());
//	}

	@Test
	public void testStoredCardFlow() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_Hotels);
		Ui.getApplication(activity).defaultHotelComponents();
		PaymentWidgetV2 paymentWidget =  (PaymentWidgetV2) LayoutInflater.from(activity)
			.inflate(R.layout.payment_widget_v2, null);
		paymentWidget.setViewmodel(new PaymentViewModel(activity));
		paymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.HOTELS);
		paymentWidget.getSectionBillingInfo().bind(storedCardBillingInfo);

		SectionBillingInfo sectionBillingInfo = (SectionBillingInfo) paymentWidget.findViewById(R.id.section_billing_info);
		LinearLayout paymentOptions = (LinearLayout) paymentWidget.findViewById(R.id.section_payment_options_container);
		StoredCreditCardList storedCreditCardList = (StoredCreditCardList) paymentWidget.findViewById(R.id.stored_creditcard_list);

		assertEquals(View.VISIBLE, sectionBillingInfo.getVisibility());
		assertEquals(View.VISIBLE, paymentOptions.getVisibility());
		assertEquals(View.VISIBLE, storedCreditCardList.getVisibility());

		assertEquals(PaymentType.CARD_AMERICAN_EXPRESS, paymentWidget.getCardType());
	}

	@Test
	public void testTempSavedCardType() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_Hotels);
		Ui.getApplication(activity).defaultHotelComponents();
		PaymentWidgetV2 paymentWidget =  (PaymentWidgetV2) LayoutInflater.from(activity)
				.inflate(R.layout.payment_widget_v2, null);
		paymentWidget.setViewmodel(new PaymentViewModel(activity));
		paymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.HOTELS);
		paymentWidget.getSectionBillingInfo().bind(tempSavedCardBillingInfo);
		paymentWidget.userChoosesToSaveCard();

		assertEquals(PaymentType.CARD_VISA, paymentWidget.getCardType());
	}

	@Test
	public void testTempNotSavedCardType() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_Hotels);
		Ui.getApplication(activity).defaultHotelComponents();
		PaymentWidgetV2 paymentWidget =  (PaymentWidgetV2) LayoutInflater.from(activity)
				.inflate(R.layout.payment_widget_v2, null);
		paymentWidget.setViewmodel(new PaymentViewModel(activity));
		paymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.HOTELS);
		paymentWidget.getSectionBillingInfo().bind(tempNotSavedCardBillingInfo);
		paymentWidget.userChoosesNotToSaveCard();

		assertEquals(PaymentType.CARD_DISCOVER, paymentWidget.getCardType());
	}
}
