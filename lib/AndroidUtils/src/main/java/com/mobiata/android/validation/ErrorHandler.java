package com.mobiata.android.validation;

/**
 * Error handler interface for validation errors.
 */
public interface ErrorHandler {
	/**
	 * Handle a ValidationError.  How you handle it is up to you!
	 * @param error the error to handle
	 */
	public void handleError(ValidationError error);
}
