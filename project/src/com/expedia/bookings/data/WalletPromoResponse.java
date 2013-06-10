package com.expedia.bookings.data;

public class WalletPromoResponse extends Response {

	private static boolean mEnabled;

	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	public boolean isEnabled() {
		return mEnabled;
	}
}
