package com.expedia.bookings.data;

public class SatelliteApiSearchModel {
	public String clientid = "expedia.app.ios.phone:17.25";
	public int forceNoRedir = 1;
	public int siteid = 1;

	public SatelliteApiSearchModel(String clientid, int forecNoRedir, int siteid) {
		this.clientid = clientid;
		this.forceNoRedir = forecNoRedir;
		this.siteid = siteid;
	}

}
