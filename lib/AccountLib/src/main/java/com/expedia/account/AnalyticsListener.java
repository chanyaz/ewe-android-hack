package com.expedia.account;

public interface AnalyticsListener {
	void signInSucceeded();

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
}
