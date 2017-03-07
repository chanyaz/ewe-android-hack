package com.expedia.bookings.test.espresso;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;

public class AbacusTestUtils {

	public static void updateABTest(int key, int value) {
		AbacusResponse abacusResponse = new AbacusResponse();
		abacusResponse.updateABTestForDebug(key, value);
		Db.setAbacusResponse(abacusResponse);
	}

	public static void bucketTests(int... tests) {
		AbacusResponse abacusResponse = new AbacusResponse();
		for (int test : tests) {
			abacusResponse.updateABTestForDebug(test, AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}
		Db.setAbacusResponse(abacusResponse);
	}
}
