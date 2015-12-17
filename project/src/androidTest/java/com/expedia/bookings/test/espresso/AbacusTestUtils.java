package com.expedia.bookings.test.espresso;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusResponse;

public class AbacusTestUtils {

	public static void updateABTest(int key, int value) {
		AbacusResponse abacusResponse = new AbacusResponse();
		abacusResponse.updateABTestForDebug(key, value);
		Db.setAbacusResponse(abacusResponse);
	}
}
