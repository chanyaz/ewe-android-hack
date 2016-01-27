package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;

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
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.TripBucketItemHotelV2;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.hotels.HotelRate;
import com.expedia.bookings.interfaces.ToolbarListener;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.ExpandableCardView;
import com.expedia.bookings.widget.PaymentWidget;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class PaymentWidgetFlowTest {
	BillingInfo info;
	Location location;
	StoredCreditCard card;
	ToolbarListener listener;

	@Before
	public void before() {

		card = new StoredCreditCard();
		card.setCardNumber("4111111111111111");
		card.setType(PaymentType.CARD_AMERICAN_EXPRESS);

		location = new Location();
		location.setCity("San Francisco");
		location.setCountryCode("USA");
		location.setDescription("Cool description");
		location.addStreetAddressLine("114 Sansome St.");
		location.setPostalCode("94109");
		location.setStateCode("CA");
		location.setLatitude(37.7833);
		location.setLongitude(122.4167);
		location.setDestinationId("SF");

		info = new BillingInfo();
		info.setEmail("qa-ehcc@mobiata.com");
		info.setFirstName("JexperCC");
		info.setLastName("MobiataTestaverde");
		info.setNameOnCard(info.getFirstName() + " " + info.getLastName());
		info.setNumberAndDetectType("4111111111111111");
		info.setSecurityCode("111");
		info.setTelephone("4155555555");
		info.setTelephoneCountryCode("1");

		info.setLocation(location);
		info.setStoredCard(card);

		listener = new ToolbarListener() {
			@Override
			public void setActionBarTitle(String title) {

			}

			@Override
			public void onWidgetExpanded(ExpandableCardView cardView) {

			}

			@Override
			public void onWidgetClosed() {

			}

			@Override
			public void onEditingComplete() {

			}

			@Override
			public void setMenuLabel(String label) {

			}

			@Override
			public void showRightActionButton(boolean show) {

			}
		};

		HotelCreateTripResponse response = new HotelCreateTripResponse();
		response.validFormsOfPayment = new ArrayList<>();
		response.newHotelProductResponse = new HotelCreateTripResponse.HotelProductResponse();
		response.newHotelProductResponse.hotelRoomResponse = new HotelOffersResponse.HotelRoomResponse();
		response.newHotelProductResponse.hotelRoomResponse.rateInfo = new HotelOffersResponse.RateInfo();
		response.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo = new HotelRate();
		response.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.total = 100;
		response.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.currencyCode = "USD";
		response.newHotelProductResponse.hotelRoomResponse.supplierType = "MERCHANT";
		TripBucketItemHotelV2 trip = new TripBucketItemHotelV2(response);
		Db.getTripBucket().clear(LineOfBusiness.HOTELSV2);
		Db.getTripBucket().add(trip);
	}

	@Test
	public void testCardOptionsNotVisible() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_LX);
		Ui.getApplication(activity).defaultLXComponents();
		PaymentWidget paymentWidget =  (PaymentWidget) LayoutInflater.from(activity)
			.inflate(R.layout.payment_widget, null);
		paymentWidget.setToolbarListener(listener);
		paymentWidget.setLineOfBusiness(LineOfBusiness.LX);
		paymentWidget.setCreditCardRequired(true);
		paymentWidget.setExpanded(true);

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
		Ui.getApplication(activity).defaultLXComponents();
		PaymentWidget paymentWidget =  (PaymentWidget) LayoutInflater.from(activity)
			.inflate(R.layout.payment_widget_v2, null);
		paymentWidget.setToolbarListener(listener);
		paymentWidget.setLineOfBusiness(LineOfBusiness.HOTELSV2);
		paymentWidget.setCreditCardRequired(true);

		paymentWidget.sectionBillingInfo.bind(info);
		paymentWidget.setExpanded(true);

		paymentWidget.onStoredCardClicked();

		LinearLayout storedCardContainer = (LinearLayout) paymentWidget.findViewById(R.id.stored_card_container);
		SectionBillingInfo sectionBillingInfo = (SectionBillingInfo) paymentWidget.findViewById(R.id.section_billing_info);

		//Stored card(wallet) should be visible
		assertEquals(View.VISIBLE, storedCardContainer.getVisibility());
		assertEquals(View.GONE, sectionBillingInfo.getVisibility());

		paymentWidget.onStoredCardRemoved();
		LinearLayout paymentOptions = (LinearLayout) paymentWidget.findViewById(R.id.section_payment_options_container);

//      Enable the below assert once we implemented android pay
//		//Wallet card removed, should return to payment options.
//		assertEquals(View.VISIBLE, paymentOptions.getVisibility());
	}
}
