package com.expedia.bookings.data.rail.responses;

import java.util.List;

import com.expedia.bookings.data.rail.Passengers;

public class RailSearchResponse {

	public RailSearchResult railSearchResult;

	public static class RailSearchResult {
		public List<Passengers> passengers;
	}
}

