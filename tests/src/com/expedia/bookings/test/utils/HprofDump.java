package com.expedia.bookings.test.utils;

import android.test.InstrumentationTestCase;
import com.mobiata.android.debug.MemoryUtils;

public class HprofDump extends InstrumentationTestCase {

	public void testHprofDump() {
		MemoryUtils.dumpHprofDataToSdcard("dump.hprof", getInstrumentation().getTargetContext());
	}

}
