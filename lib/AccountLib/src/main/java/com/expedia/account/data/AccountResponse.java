package com.expedia.account.data;

import java.util.List;

public class AccountResponse {

	public static class ErrorInfo {
		public String summary;
		public ErrorField field;
		public String cause;
	}

	public static class AccountError {
		public ErrorCode errorCode;
		public ErrorInfo errorInfo;
		public String diagnosticId;
		public String getDiagnosticFullText;
		public String activityId;
	}

	public enum ErrorCode {
		USER_SERVICE_DUPLICATE_EMAIL,
		INVALID_INPUT,
		EMAIL_PASSWORD_IDENTICAL_ERROR,
		COMMON_PASSWORD_ERROR
	}

	// These have to (case sensitively) match the server response
	public enum ErrorField {
		email,
		password,
		firstName,
		middleName,
		lastName
	}

	public enum SignInError {
		INVALID_CREDENTIALS,
		ACCOUNT_LOCKED
	}

	public String tuid;
	public boolean success;
	public List<AccountError> errors;
	String detailedStatus;
	String detailedStatusMsg;

	/**
	 * tells us which error msg to show based on detailedStatusMsg
	 *
	 * @return
	 */
	public SignInError SignInFailureError() {
		if (detailedStatusMsg != null && detailedStatusMsg.equalsIgnoreCase("Login limit exceeded")) {
			return SignInError.ACCOUNT_LOCKED;
		}
		return SignInError.INVALID_CREDENTIALS;
	}

	/**
	 * Searches through this object's errors and returns the first one
	 * that matches the specified ErrorCode. Returns null if the error
	 * isn't found.
	 * @param code
	 * @return
	 */
	public AccountError findError(ErrorCode code) {
		for (AccountError error : errors) {
			if (error.errorCode == code) {
				return error;
			}
		}
		return null;
	}

	/**
	 * Returns true if the error with the specified code exists in the response.
	 * @param code
	 * @return
	 */
	public boolean hasError(ErrorCode code) {
		return findError(code) != null;
	}
}
