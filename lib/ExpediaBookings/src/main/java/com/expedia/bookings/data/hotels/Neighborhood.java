package com.expedia.bookings.data.hotels;

import java.util.ArrayList;
import java.util.List;

public class Neighborhood {
	public String name;
	public String id;

	public transient List<Hotel> hotels = new ArrayList<>();
	public transient int score;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Neighborhood that = (Neighborhood) o;

		if (name != null ? !name.equals(that.name) : that.name != null) {
			return false;
		}
		return id != null ? id.equals(that.id) : that.id == null;

	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (id != null ? id.hashCode() : 0);
		return result;
	}
}
