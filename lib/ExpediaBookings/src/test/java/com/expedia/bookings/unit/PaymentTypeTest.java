package com.expedia.bookings.unit;

import org.junit.Assert;
import org.junit.Test;

import com.expedia.bookings.data.PaymentType;

public class PaymentTypeTest {

	@Test
	public void isPointsWorks() {
		Assert.assertTrue(PaymentType.POINTS_REWARDS.isPoints());
		Assert.assertFalse(PaymentType.CARD_AMERICAN_EXPRESS.isPoints());
		Assert.assertFalse(PaymentType.CARD_CARTA_SI.isPoints());
		Assert.assertFalse(PaymentType.CARD_CARTE_BLANCHE.isPoints());
		Assert.assertFalse(PaymentType.CARD_CARTE_BLEUE.isPoints());
		Assert.assertFalse(PaymentType.CARD_CHINA_UNION_PAY.isPoints());
		Assert.assertFalse(PaymentType.CARD_DINERS_CLUB.isPoints());
		Assert.assertFalse(PaymentType.CARD_DISCOVER.isPoints());
		Assert.assertFalse(PaymentType.CARD_JAPAN_CREDIT_BUREAU.isPoints());
		Assert.assertFalse(PaymentType.CARD_MAESTRO.isPoints());
		Assert.assertFalse(PaymentType.CARD_MASTERCARD.isPoints());
		Assert.assertFalse(PaymentType.CARD_VISA.isPoints());
	}

	@Test
	public void isCardWorks() {
		Assert.assertFalse(PaymentType.POINTS_REWARDS.isCard());
		Assert.assertTrue(PaymentType.CARD_AMERICAN_EXPRESS.isCard());
		Assert.assertTrue(PaymentType.CARD_CARTA_SI.isCard());
		Assert.assertTrue(PaymentType.CARD_CARTE_BLANCHE.isCard());
		Assert.assertTrue(PaymentType.CARD_CARTE_BLEUE.isCard());
		Assert.assertTrue(PaymentType.CARD_CHINA_UNION_PAY.isCard());
		Assert.assertTrue(PaymentType.CARD_DINERS_CLUB.isCard());
		Assert.assertTrue(PaymentType.CARD_DISCOVER.isCard());
		Assert.assertTrue(PaymentType.CARD_JAPAN_CREDIT_BUREAU.isCard());
		Assert.assertTrue(PaymentType.CARD_MAESTRO.isCard());
		Assert.assertTrue(PaymentType.CARD_MASTERCARD.isCard());
		Assert.assertTrue(PaymentType.CARD_VISA.isCard());
	}

	@Test
	public void omniturePaymentCodeIsCorrect() {
		Assert.assertEquals("AmericanExpress", PaymentType.CARD_AMERICAN_EXPRESS.getOmnitureTrackingCode());
		Assert.assertEquals("CartaSi", PaymentType.CARD_CARTA_SI.getOmnitureTrackingCode());
		Assert.assertEquals("CarteBlanche", PaymentType.CARD_CARTE_BLANCHE.getOmnitureTrackingCode());
		Assert.assertEquals("CarteBleue", PaymentType.CARD_CARTE_BLEUE.getOmnitureTrackingCode());
		Assert.assertEquals("ChinaUnionPay", PaymentType.CARD_CHINA_UNION_PAY.getOmnitureTrackingCode());
		Assert.assertEquals("DinersClub", PaymentType.CARD_DINERS_CLUB.getOmnitureTrackingCode());
		Assert.assertEquals("Discover", PaymentType.CARD_DISCOVER.getOmnitureTrackingCode());
		Assert.assertEquals("JapanCreditBureau", PaymentType.CARD_JAPAN_CREDIT_BUREAU.getOmnitureTrackingCode());
		Assert.assertEquals("Maestro", PaymentType.CARD_MAESTRO.getOmnitureTrackingCode());
		Assert.assertEquals("Mastercard", PaymentType.CARD_MASTERCARD.getOmnitureTrackingCode());
		Assert.assertEquals("Visa", PaymentType.CARD_VISA.getOmnitureTrackingCode());
		Assert.assertEquals("Unknown", PaymentType.CARD_UNKNOWN.getOmnitureTrackingCode());
	}
}
