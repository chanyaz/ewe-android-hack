package com.expedia.bookings.data;

public class SignInResponse extends Response {

	private boolean mSuccess;

	// Information on the user
	private User mUser;

	public void setSuccess(boolean success) {
		mSuccess = success;
	}

	@Override
	public boolean isSuccess() {
		return !hasErrors() && mSuccess;
	}

	public void setUser(User user) {
		mUser = user;
	}

	public User getUser() {
		return mUser;
	}

	// Shortcut if all we care about is the traveler
	//
	// This is the case when we're retrieving profile details
	// about an associated traveler
	public Traveler getTraveler() {
		if (mUser != null) {
			return mUser.getPrimaryTraveler();
		}
		return null;
	}

}
