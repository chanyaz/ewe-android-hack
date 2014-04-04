package com.expedia.bookings.data;

import com.mobiata.android.Log;
import com.squareup.otto.Bus;

/**
 * Created by jdrotos on 2/27/14.
 */
public class Sp {

	public static final String BUS_NAME = "SP_BUS";

	private static SearchParams sSearchParams;
	private static Bus sBus;

	public static Bus getBus() {
		if (sBus == null) {
			sBus = new Bus(BUS_NAME);
		}
		return sBus;
	}

	public static SearchParams getParams() {
		if (sSearchParams == null) {
			sSearchParams = new SearchParams();
		}
		return sSearchParams;
	}

	public static void setParams(SearchParams params, boolean report) {
		sSearchParams = params;
		if (report) {
			reportSpUpdate();
		}
	}

	public static void reportSpUpdate() {
		try {
			getBus().post(new SpUpdateEvent());
		}
		catch (Exception ex) {
			Log.e("Exception posting the Sp event bus.", ex);
		}
	}

	public static class SpUpdateEvent {
		public SpUpdateEvent() {
		}
	}

	private Sp() {

	}
}
