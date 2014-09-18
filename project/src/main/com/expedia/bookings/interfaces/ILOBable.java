package com.expedia.bookings.interfaces;

import com.expedia.bookings.data.LineOfBusiness;

public interface ILOBable {
	public void setLob(LineOfBusiness lob);
	public LineOfBusiness getLob();
}
