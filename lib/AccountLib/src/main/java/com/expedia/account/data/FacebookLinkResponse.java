package com.expedia.account.data;

public class FacebookLinkResponse {

	public enum FacebookLinkResponseCode {
		none, // This is not a valid code, but a default value
		success,
		notLinked,
		existing,
		loginFailed,
		error
	}

	public String tlLoginSuccess;
	public String tlError;
	public String tlAcctState;
	public String tlAccountSource;
	public String tlAcctType;
	public FacebookLinkResponseCode status = FacebookLinkResponseCode.none;
	public String rewardsState;

	//////////////////////////////////////////////////////////////////////////

	public boolean isSuccess() {
		return (status == FacebookLinkResponseCode.success);
	}

	public boolean isExisting() {
		return (status == FacebookLinkResponseCode.existing);
	}

	public boolean isNotLinked() {
		return (status == FacebookLinkResponseCode.notLinked);
	}

	public boolean isLoginFailed() {
		return (status == FacebookLinkResponseCode.loginFailed);
	}

}