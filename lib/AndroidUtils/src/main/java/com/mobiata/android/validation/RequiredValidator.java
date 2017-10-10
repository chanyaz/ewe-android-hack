package com.mobiata.android.validation;

/**
 * A validator for the common case of ensuring that a CharSequence exists.
 */
public class RequiredValidator implements Validator<CharSequence> {

	private static final RequiredValidator INSTANCE = new RequiredValidator();

	private RequiredValidator() {
	}

	public static RequiredValidator getInstance() {
		return INSTANCE;
	}

	@Override
	public int validate(CharSequence text) {
		return (text == null || text.length() == 0) ? ValidationError.ERROR_DATA_MISSING : ValidationError.NO_ERROR;
	}
}
