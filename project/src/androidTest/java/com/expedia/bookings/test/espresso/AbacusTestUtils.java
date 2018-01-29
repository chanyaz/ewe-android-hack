package com.expedia.bookings.test.espresso;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.ABTest;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusVariant;

public class AbacusTestUtils {

	public static void updateABTest(ABTest abTest, int value) {
		AbacusResponse abacusResponse = Db.sharedInstance.getAbacusResponse();
		abacusResponse.updateABTestForDebug(abTest.getKey(), value);
		Db.sharedInstance.setAbacusResponse(abacusResponse);
	}

	public static void bucketTests(ABTest... tests) {
		AbacusResponse abacusResponse = new AbacusResponse();
		for (ABTest test : tests) {
			abacusResponse.updateABTestForDebug(test.getKey(), AbacusVariant.BUCKETED.getValue());
		}
		Db.sharedInstance.setAbacusResponse(abacusResponse);
	}
}
