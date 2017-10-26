package com.expedia.account.data;

public class PartialUser {
	public String email;
	public String firstName;
	public String lastName;
	public String password;

	public boolean expediaEmailOptin = false;
	public boolean enrollInLoyalty = false;

	// Facebook fields
	public boolean isFacebookUser;
	public String facebookUserId;
	public String facebookToken;
}
