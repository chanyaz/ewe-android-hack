package com.mobiata.android.validation;

public class ValidationError {

	public static final int NO_ERROR = 0;
	public static final int ERROR_DATA_MISSING = 1;
	public static final int ERROR_DATA_INVALID = 2;

	private Object mObject;
	private int mErrorCode;
	private ErrorHandler mErrorHandler;

	public ValidationError(Object obj, int errorCode) {
		this(obj, errorCode, null);
	}

	public ValidationError(Object obj, int errorCode, ErrorHandler errorHandler) {
		mObject = obj;
		mErrorCode = errorCode;
		mErrorHandler = errorHandler;
	}

	public Object getObject() {
		return mObject;
	}

	public int getErrorCode() {
		return mErrorCode;
	}

	public boolean hasErrorHandler() {
		return mErrorHandler != null;
	}

	public void handleError() {
		if (mErrorHandler != null) {
			mErrorHandler.handleError(this);
		}
	}

	@Override
	public String toString() {
		return "Object: " + mObject + ", errorCode: " + mErrorCode;
	}
}
