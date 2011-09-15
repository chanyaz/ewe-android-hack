package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

public class AvailabilityResponse extends Response {
	private List<Rate> mRates;

	public void addRate(Rate rate) {
		if (mRates == null) {
			mRates = new ArrayList<Rate>();
		}
		mRates.add(rate);
	}

	public int getRateCount() {
		if (mRates == null) {
			return 0;
		}
		return mRates.size();
	}

	public Rate getRate(int index) {
		if (mRates == null) {
			return null;
		}
		return mRates.get(index);
	}

	public List<Rate> getRates() {
		return mRates;
	}

	/**
	 * Gathers the list of common value adds between all Rates contained within.
	 * 
	 * @return the list of common value adds between all Rates.  Null if there are none.
	 */
	public List<String> getCommonValueAdds() {
		int size = getRateCount();
		if (size > 0) {
			List<String> commonValueAdds = new ArrayList<String>(mRates.get(0).getValueAdds());

			for (int a = 1; a < size; a++) {
				commonValueAdds.retainAll(mRates.get(a).getValueAdds());
			}

			if (commonValueAdds.size() > 0) {
				return commonValueAdds;
			}
		}

		return null;
	}
}
