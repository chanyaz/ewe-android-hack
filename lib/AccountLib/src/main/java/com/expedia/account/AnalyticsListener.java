package com.expedia.account;

public interface AnalyticsListener {
	void signInSucceeded();

	//Not relevant until M, but leaving in so that it isn't left out of the spec.
	void contactsAccessRequested();

	void contactsAccessResponse(boolean granted);

	void emailsQueried();

	void accountCreationAttemptWithPreexistingEmail(boolean useExisting, boolean createNew);

	void userViewedNameEntering();

	void userViewedPasswordEntering();

	void userViewedTosPage();

	void userViewedSinglePage();

	/**
	 * @param optIn true if the user chose to opt in, false if they chose to opt out
	 */
	void userExplicitlyModifiedMarketingOptIn(boolean optIn);

	void userSucceededInCreatingAccount();

	void userReceivedErrorOnSignInAttempt(String failureReason);

	void userReceivedErrorOnAccountCreationAttempt(String failureReason);

	void userAutoLoggedInBySmartPassword();

	void userSignedInUsingSmartPassword();
}
