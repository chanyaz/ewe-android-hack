package com.expedia.bookings.data;

import com.squareup.otto.Bus;

/**
 * Created by jdrotos on 2/27/14.
 */
public class Sp {

	private static SearchParams sSearchParams;
	private static Bus sBus;

	public static Bus getBus() {
		if (sBus == null) {
			sBus = new Bus();
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
		getBus().post(new SpUpdateEvent());
	}

	public static class SpUpdateEvent {
		public SpUpdateEvent() {
		}
	}

	private Sp() {

	}
}
