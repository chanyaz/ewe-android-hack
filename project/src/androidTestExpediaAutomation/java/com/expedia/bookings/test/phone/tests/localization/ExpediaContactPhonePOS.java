package com.expedia.bookings.test.phone.tests.localization;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.utils.PhoneTestCase;

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

	public void testARGENTINA() throws Throwable {
		setPOS(PointOfSaleId.ARGENTINA);
		runTestCase(("0-800-266-1693"));
	}

	public void testAUSTRALIA() throws Throwable {
		setPOS(PointOfSaleId.AUSTRALIA);
		runTestCase("61 2 8066 2745");
	}

	public void testAUSTRIA() throws Throwable {
		setPOS(PointOfSaleId.AUSTRIA);
		runTestCase("01 206 09 1626");
	}

	public void testBELGIUM() throws Throwable {
		setPOS(PointOfSaleId.BELGIUM);
		runTestCase("022 00 83 65");
	}

	public void testBRAZIL() throws Throwable {
		setPOS(PointOfSaleId.BRAZIL);
		runTestCase("0800-762-5309");
	}

	public void testCANADA() throws Throwable {
		setPOS(PointOfSaleId.CANADA);
		runTestCase("855-247-9310");
	}

	public void testDENMARK() throws Throwable {
		setPOS(PointOfSaleId.DENMARK);
		runTestCase("43 68 21 91");
	}

	public void testFRANCE() throws Throwable {
		setPOS(PointOfSaleId.FRANCE);
		runTestCase("01 57 32 47 22");
	}

	public void testGERMANY() throws Throwable {
		setPOS(PointOfSaleId.GERMANY);
		runTestCase("069-999 915 582");
	}

	public void testHONG_KONG() throws Throwable {
		setPOS(PointOfSaleId.HONG_KONG);
		runTestCase("3077 4857");
	}

	public void testINDIA() throws Throwable {
		setPOS(PointOfSaleId.INDIA);
		runTestCase("0124 487 3888");
	}

	public void testINDONESIA() throws Throwable {
		setPOS(PointOfSaleId.INDONESIA);
		runTestCase("007 803 011 0463");
	}

	public void testIRELAND() throws Throwable {
		setPOS(PointOfSaleId.IRELAND);
		runTestCase("01 517 1524");
	}

	public void testITALY() throws Throwable {
		setPOS(PointOfSaleId.ITALY);
		runTestCase("02-23331404");
	}

	public void testJAPAN() throws Throwable {
		setPOS(PointOfSaleId.JAPAN);
		runTestCase("03 6743 6572");
	}

	public void testSOUTH_KOREA() throws Throwable {
		setPOS(PointOfSaleId.SOUTH_KOREA);
		runTestCase("02 2076 8343");
	}

	public void testMALAYSIA() throws Throwable {
		setPOS(PointOfSaleId.MALAYSIA);
		runTestCase("1 800 815676");
	}

	public void testMEXICO() throws Throwable {
		setPOS(PointOfSaleId.MEXICO);
		runTestCase("001-855-395-8976");
	}

	public void testNETHERLANDS() throws Throwable {
		setPOS(PointOfSaleId.NETHERLANDS);
		runTestCase("020 700 60 42");
	}

	public void testNORWAY() throws Throwable {
		setPOS(PointOfSaleId.NORWAY);
		runTestCase("24 15 97 43");
	}

	public void testPHILIPPINES() throws Throwable {
		setPOS(PointOfSaleId.PHILIPPINES);
		runTestCase("1 800 1 114 2468");
	}

	public void testSINGAPORE() throws Throwable {
		setPOS(PointOfSaleId.SINGAPORE);
		runTestCase("6226 3973");
	}

	public void testSPAIN() throws Throwable {
		setPOS(PointOfSaleId.SPAIN);
		runTestCase("912 754 962");
	}

	public void testSWEDEN() throws Throwable {
		setPOS(PointOfSaleId.SWEDEN);
		runTestCase("08 502 52029");
	}

	public void testTAIWAN() throws Throwable {
		setPOS(PointOfSaleId.TAIWAN);
		runTestCase("00801 136291");
	}

	public void testTHAILAND() throws Throwable {
		setPOS(PointOfSaleId.THAILAND);
		runTestCase("02 1055728");
	}

	public void testUNITED_KINGDOM() throws Throwable {
		setPOS(PointOfSaleId.UNITED_KINGDOM);
		runTestCase("020 3564 5468");
	}

	public void testUNITED_STATES() throws Throwable {
		setPOS(PointOfSaleId.UNITED_STATES);
		runTestCase("1-877-222-6503");
	}

	public void testVIETNAM() throws Throwable {
		setPOS(PointOfSaleId.VIETNAM);
		runTestCase("120 65 125");
	}
}
