package com.expedia.bookings.test.utils;

import android.test.InstrumentationTestCase;
import com.mobiata.android.util.IoUtils;

public class HprofDump extends InstrumentationTestCase {

	public void testHprofDump() {
		IoUtils.dumpHprofDataToSdcard("dump.hprof", getInstrumentation().getTargetContext());
	}

}
