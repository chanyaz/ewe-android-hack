package com.expedia.bookings.test.robolectric;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.HotelUtils;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class HotelUtilsTest {

	final static String MERCHANT_SUPPLIER_TYPE = "MERCHANT";
	final static String NON_MERCHANT_SUPPLIER_TYPE = "NOT_MERCHANT_I_PROMISE";

	@Test
	public void testHotelSlideToWidgetText() throws Throwable {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();

		// Init vars
		String expectedText = "";
		String resultText = "";
		boolean isTablet = false;
		final Property property = new Property();
		final Rate rate = new Rate();
		final Money depositAmount = new Money("0", "USD");
		final Money totalAmount = new Money("123.45", "USD");
		rate.setDepositAmount(depositAmount);
		rate.setTotalAmountAfterTax(totalAmount);

		// Pay later, merchant AND non-merchant, phone
		rate.setIsPayLater(true);
		property.setSupplierType(MERCHANT_SUPPLIER_TYPE);
		isTablet = false;
		resultText = HotelUtils.getSlideToPurchaseString(activity, property, rate, isTablet);
		expectedText = activity.getString(R.string.Amount_to_be_paid_now_TEMPLATE, depositAmount.getFormattedMoney());
		assertEquals(expectedText, resultText);

		rate.setIsPayLater(true);
		property.setSupplierType(NON_MERCHANT_SUPPLIER_TYPE);
		isTablet = false;
		resultText = HotelUtils.getSlideToPurchaseString(activity, property, rate, isTablet);
		expectedText = activity.getString(R.string.collected_by_the_hotel_TEMPLATE, totalAmount.getFormattedMoney());
		assertEquals(expectedText, resultText);

		// Total with mandatory fees rate type
		rate.setIsPayLater(false);
		rate.setCheckoutPriceType(Rate.CheckoutPriceType.TOTAL_WITH_MANDATORY_FEES);
		property.setSupplierType(MERCHANT_SUPPLIER_TYPE);
		isTablet = true;
		resultText = HotelUtils.getSlideToPurchaseString(activity, property, rate, isTablet);
		expectedText = activity.getString(R.string.Amount_to_be_paid_now_TEMPLATE, totalAmount.getFormattedMoney());
		assertEquals(expectedText, resultText);

		// Pay later, merchant AND non-merchant, tablet
		// Note: The tablet ui does not support the selection of ETP rates. Unfortunately, in the case of non-merchant
		// hotels, we do not receive this information until the create trip call. This means the user is able to effectively
		// select an ETP rate on the tablet app. The product decision was to show the full amount even though their card
		// would not be charged. This behavior may change when we implement ETP for tablet.
		rate.setIsPayLater(true);
		property.setSupplierType(MERCHANT_SUPPLIER_TYPE);
		isTablet = true;
		resultText = HotelUtils.getSlideToPurchaseString(activity, property, rate, isTablet);
		expectedText = activity.getString(R.string.Amount_to_be_paid_now_TEMPLATE, totalAmount.getFormattedMoney());
		assertEquals(expectedText, resultText);

		rate.setIsPayLater(true);
		property.setSupplierType(NON_MERCHANT_SUPPLIER_TYPE);
		isTablet = true;
		resultText = HotelUtils.getSlideToPurchaseString(activity, property, rate, isTablet);
		expectedText = activity.getString(R.string.collected_by_the_hotel_TEMPLATE, totalAmount.getFormattedMoney());
		assertEquals(expectedText, resultText);

		// Pay now, merchant
		rate.setIsPayLater(false);
		rate.setCheckoutPriceType(Rate.CheckoutPriceType.TOTAL);
		property.setSupplierType(MERCHANT_SUPPLIER_TYPE);
		isTablet = true;
		resultText = HotelUtils.getSlideToPurchaseString(activity, property, rate, isTablet);
		expectedText = activity.getString(R.string.your_card_will_be_charged_TEMPLATE, totalAmount.getFormattedMoney());
		assertEquals(expectedText, resultText);

		// Pay now, non-merchant
		rate.setIsPayLater(false);
		rate.setCheckoutPriceType(Rate.CheckoutPriceType.TOTAL);
		property.setSupplierType(NON_MERCHANT_SUPPLIER_TYPE);
		isTablet = true;
		resultText = HotelUtils.getSlideToPurchaseString(activity, property, rate, isTablet);
		expectedText = activity.getString(R.string.collected_by_the_hotel_TEMPLATE, totalAmount.getFormattedMoney());
		assertEquals(expectedText, resultText);
	}

}
