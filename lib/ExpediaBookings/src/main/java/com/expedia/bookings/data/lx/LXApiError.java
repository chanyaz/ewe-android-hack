package com.expedia.bookings.data.lx;

public class LXApiError {
	public enum Code {
		INVALID_INPUT,
		PAYMENT_FAILED
	}

	public static class ErrorInfo {
		public String summary;
		public String field;
		public String cause;
	}

	public Code errorCode;
	public int diagnosticId;
	public String diagnosticFullText;
	public ErrorInfo errorInfo;
}
