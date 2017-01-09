package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.TripBucketItemFlightV2;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.ValidPayment;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.flights.FlightCreateTripResponse;
import com.expedia.bookings.data.flights.ValidFormOfPayment;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.hotels.HotelRate;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.packages.PackageCreateTripResponse;
import com.expedia.bookings.data.trips.TripBucketItem;
import com.expedia.bookings.data.trips.TripBucketItemCar;
import com.expedia.bookings.data.trips.TripBucketItemHotelV2;
import com.expedia.bookings.data.trips.TripBucketItemLX;
import com.expedia.bookings.data.trips.TripBucketItemPackages;
import com.expedia.bookings.data.utils.ValidFormOfPaymentUtils;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB;
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM;
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.PaymentWidget;
import com.expedia.bookings.widget.PaymentWidgetV2;
import com.expedia.bookings.widget.StoredCreditCardList;
import com.expedia.bookings.widget.TextView;
import com.expedia.vm.PaymentViewModel;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
@Config(shadows = { ShadowGCM.class, ShadowUserManager.class, ShadowAccountManagerEB.class })
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

	@Test
	public void testInvalidCardOptionsMessageLx() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_LX);
		Ui.getApplication(activity).defaultLXComponents();
		PaymentWidget paymentWidget =  (PaymentWidget) LayoutInflater.from(activity)
			.inflate(R.layout.payment_widget, null);
		paymentWidget.setViewmodel(new PaymentViewModel(activity));
		paymentWidget.show(new PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK);
		paymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.LX);

		UserLoginTestUtil.Companion.setupUserAndMockLogin(UserLoginTestUtil.Companion.mockUser());

		LXCreateTripResponse response = new LXCreateTripResponse();
		response.validFormsOfPayment = setupValidPayments();
		TripBucketItem tripItem = new TripBucketItemLX(response);
		Db.getTripBucket().add((TripBucketItemLX) tripItem);

		assertCorrectErrorMessage("Activity does not accept Maestro", paymentWidget);
	}

	@Test
	public void testInvalidCardOptionsMessagePackages() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_Packages);
		Ui.getApplication(activity).defaultPackageComponents();
		PaymentWidget paymentWidget =  (PaymentWidget) LayoutInflater.from(activity)
			.inflate(R.layout.payment_widget, null);
		paymentWidget.setViewmodel(new PaymentViewModel(activity));
		paymentWidget.show(new PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK);
		paymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.PACKAGES);

		UserLoginTestUtil.Companion.setupUserAndMockLogin(UserLoginTestUtil.Companion.mockUser());

		PackageCreateTripResponse response = new PackageCreateTripResponse();
		response.setValidFormsOfPayment(setupValidFormsOfPayment());
		TripBucketItem tripItem = new TripBucketItemPackages(response);
		Db.getTripBucket().add((TripBucketItemPackages) tripItem);

		assertCorrectErrorMessage("Trip does not accept Maestro", paymentWidget);
	}

	@Test
	public void testInvalidCardOptionsMessageCars() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_Cars);
		Ui.getApplication(activity).defaultCarComponents();
		PaymentWidget paymentWidget =  (PaymentWidget) LayoutInflater.from(activity)
			.inflate(R.layout.payment_widget, null);
		paymentWidget.setViewmodel(new PaymentViewModel(activity));
		paymentWidget.show(new PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK);
		paymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.CARS);

		UserLoginTestUtil.Companion.setupUserAndMockLogin(UserLoginTestUtil.Companion.mockUser());

		CarCreateTripResponse response = new CarCreateTripResponse();
		response.validFormsOfPayment = setupValidPayments();
		TripBucketItem tripItem = new TripBucketItemCar(response);
		Db.getTripBucket().add((TripBucketItemCar) tripItem);

		assertCorrectErrorMessage("Rental company does not accept Maestro", paymentWidget);
	}


	@Test
	public void testInvalidCardOptionsMessageFlights() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_Packages);
		Ui.getApplication(activity).defaultPackageComponents();
		PaymentWidget paymentWidget =  (PaymentWidget) LayoutInflater.from(activity)
			.inflate(R.layout.payment_widget, null);
		paymentWidget.setViewmodel(new PaymentViewModel(activity));
		paymentWidget.show(new PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK);
		paymentWidget.getViewmodel().getLineOfBusiness().onNext(LineOfBusiness.FLIGHTS_V2);

		UserLoginTestUtil.Companion.setupUserAndMockLogin(UserLoginTestUtil.Companion.mockUser());

		FlightCreateTripResponse response = new FlightCreateTripResponse();
		response.setValidFormsOfPayment(setupValidFormsOfPayment());
		TripBucketItemFlightV2 tripItem = new TripBucketItemFlightV2(response);
		Db.getTripBucket().add(tripItem);

		assertCorrectErrorMessage("Airline does not accept Maestro", paymentWidget);
	}


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

	private void setUserWithStoredCard(PaymentWidget paymentWidget) {
		User user = new User();
		user.addStoredCreditCard(getNewCard());
		Db.setUser(user);

		paymentWidget.getViewmodel().isCreditCardRequired().onNext(true);
		paymentWidget.getSectionBillingInfo().bind(new BillingInfo());
		paymentWidget.selectFirstAvailableCard();
	}

	private StoredCreditCard getNewCard() {
		StoredCreditCard card = new StoredCreditCard();

		card.setCardNumber("1234567812345678");
		card.setId("stored-card-id");
		card.setType(PaymentType.CARD_MAESTRO);
		card.setDescription("shouldBeInvalid");
		card.setIsGoogleWallet(false);
		return card;
	}

	private ArrayList setupValidPayments() {
		ArrayList<ValidPayment> validPayments = new ArrayList<>();
		ValidPayment validPayment = new ValidPayment();
		validPayment.name = "Visa";
		validPayments.add(validPayment);
		return validPayments;
	}

	private ArrayList setupValidFormsOfPayment() {
		ArrayList<ValidFormOfPayment> validFormsOfPayment = new ArrayList<>();
		ValidFormOfPayment validPayment = new ValidFormOfPayment();
		validPayment.name = "Visa";
		ValidFormOfPaymentUtils.addValidPayment(validFormsOfPayment, validPayment);
		return validFormsOfPayment;
	}

	private void assertCorrectErrorMessage(String message, PaymentWidget paymentWidget) {
		paymentWidget.getSectionBillingInfo().bind(storedCardBillingInfo);
		setUserWithStoredCard(paymentWidget);
		paymentWidget.getStoredCreditCardList().bind();

		ListView storedList = (ListView) paymentWidget.getStoredCreditCardList().findViewById(R.id.stored_card_list);

		TextView tv = (TextView) storedList.getAdapter().getView(0, null, paymentWidget).findViewById(R.id.text1) ;
		assertEquals(message, tv.getText());
		assertEquals(message + ", disabled Button" , tv.getContentDescription().toString());
	}

}
