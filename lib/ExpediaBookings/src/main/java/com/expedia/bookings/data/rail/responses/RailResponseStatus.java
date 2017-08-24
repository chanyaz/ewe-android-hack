package com.expedia.bookings.data.rail.responses;

import java.util.ArrayList;
import java.util.List;

public class RailResponseStatus {
	public final String status;
	public final String statusCategory;
	public final List<Warning> warningList = new ArrayList<>();

	public Warning getWarningByCode(String code) {
		for (Warning w : warningList) {
			if (code.equals(w.warningCode)) {
				return w;
			}
		}
		return null;
	}

	public static class Warning {
		public final String warningCode;
		public String warningDescription;
	}
}
