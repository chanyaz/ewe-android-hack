package com.expedia.bookings.interfaces;

import com.expedia.bookings.data.LineOfBusiness;

public interface ILOBable {
	void setLob(LineOfBusiness lob);

	LineOfBusiness getLob();
}
