package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

public class SatelliteSearchResponse extends BaseApiResponse {
//	public List<String> testList = new ArrayList<>();
	public List<String> testList;

	public void setSource(String source) {
		this.testList.add(source);
	}
}
