package com.expedia.bookings.test.ui.phone.tests.localization;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.PhoneTestCase;

/**
 * Created by dmadan on 11/17/14.
 */
public class ExpediaContactPhonePOS extends PhoneTestCase {

	/*
	 *  Test Info screen phone numbers by POS
	 *  #264 eb_tp [a] Info screen phone numbers by POS in phone
	 */

	public void runTestCase(String phoneNumber) {
		final String phone = PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser());
		assertEquals(phoneNumber, phone);
	}

	public void testArgentina() throws Throwable {
		setPOS(PointOfSaleId.ARGENTINA);
		runTestCase(("0-800-266-1693"));
	}

	public void testAustralia() throws Throwable {
		setPOS(PointOfSaleId.AUSTRALIA);
		runTestCase("61 2 8066 2745");
	}

	public void testAustria() throws Throwable {
		setPOS(PointOfSaleId.AUSTRIA);
		runTestCase("01 206 09 1626");
	}

	public void testBelgium() throws Throwable {
		setPOS(PointOfSaleId.BELGIUM);
		runTestCase("022 00 83 65");
	}

	public void testBrazil() throws Throwable {
		setPOS(PointOfSaleId.BRAZIL);
		runTestCase("0800-762-5309");
	}

	public void testCanada() throws Throwable {
		setPOS(PointOfSaleId.CANADA);
		runTestCase("855-247-9310");
	}

	public void testDenmark() throws Throwable {
		setPOS(PointOfSaleId.DENMARK);
		runTestCase("43 68 21 91");
	}

	public void testFrance() throws Throwable {
		setPOS(PointOfSaleId.FRANCE);
		runTestCase("01 57 32 47 22");
	}

	public void testGermany() throws Throwable {
		setPOS(PointOfSaleId.GERMANY);
		runTestCase("069-999 915 582");
	}

	public void testHongKong() throws Throwable {
		setPOS(PointOfSaleId.HONG_KONG);
		runTestCase("3077 4857");
	}

	public void testIndia() throws Throwable {
		setPOS(PointOfSaleId.INDIA);
		runTestCase("0124 487 3888");
	}

	public void testIndonesia() throws Throwable {
		setPOS(PointOfSaleId.INDONESIA);
		runTestCase("007 803 011 0463");
	}

	public void testIreland() throws Throwable {
		setPOS(PointOfSaleId.IRELAND);
		runTestCase("01 517 1524");
	}

	public void testItaly() throws Throwable {
		setPOS(PointOfSaleId.ITALY);
		runTestCase("02-23331404");
	}

	public void testJapan() throws Throwable {
		setPOS(PointOfSaleId.JAPAN);
		runTestCase("03 6743 6572");
	}

	public void testSouthKorea() throws Throwable {
		setPOS(PointOfSaleId.SOUTH_KOREA);
		runTestCase("02 3480 0166");
	}

	public void testMalaysia() throws Throwable {
		setPOS(PointOfSaleId.MALAYSIA);
		runTestCase("1 800 815676");
	}

	public void testMexico() throws Throwable {
		setPOS(PointOfSaleId.MEXICO);
		runTestCase("001-855-395-8976");
	}

	public void testNetherlands() throws Throwable {
		setPOS(PointOfSaleId.NETHERLANDS);
		runTestCase("020 700 60 42");
	}

	public void testNorway() throws Throwable {
		setPOS(PointOfSaleId.NORWAY);
		runTestCase("24 15 97 43");
	}

	public void testPhilippines() throws Throwable {
		setPOS(PointOfSaleId.PHILIPPINES);
		runTestCase("1 800 1 114 2468");
	}

	public void testSingapore() throws Throwable {
		setPOS(PointOfSaleId.SINGAPORE);
		runTestCase("6226 3973");
	}

	public void testSpain() throws Throwable {
		setPOS(PointOfSaleId.SPAIN);
		runTestCase("912 754 962");
	}

	public void testSweden() throws Throwable {
		setPOS(PointOfSaleId.SWEDEN);
		runTestCase("08 502 52029");
	}

	public void testTaiwan() throws Throwable {
		setPOS(PointOfSaleId.TAIWAN);
		runTestCase("00801 136291");
	}

	public void testThailand() throws Throwable {
		setPOS(PointOfSaleId.THAILAND);
		runTestCase("02 1055728");
	}

	public void testUnitedKingdom() throws Throwable {
		setPOS(PointOfSaleId.UNITED_KINGDOM);
		runTestCase("020 3564 5468");
	}

	public void testUnitedStates() throws Throwable {
		setPOS(PointOfSaleId.UNITED_STATES);
		runTestCase("1-877-222-6503");
	}

	public void testVietnam() throws Throwable {
		setPOS(PointOfSaleId.VIETNAM);
		runTestCase("120 65 125");
	}
}
