package com.mobiata.android.validation;

import java.util.regex.Pattern;

/**
 * A validator for the common use-case of validating a CharSequence
 * against a particular pattern.
 * 
 * It also comes with built-in email and phone # validators.
 */
public class PatternValidator implements Validator<CharSequence> {

	private Pattern mPattern;

	public PatternValidator(Pattern pattern) {
		mPattern = pattern;
	}

	@Override
	public int validate(CharSequence charSeq) {
		int errorCode = RequiredValidator.getInstance().validate(charSeq);
		if (errorCode != ValidationError.NO_ERROR) {
			return errorCode;
		}
		else if (!mPattern.matcher(charSeq).matches()) {
			return ValidationError.ERROR_DATA_INVALID;
		}
		else {
			return ValidationError.NO_ERROR;
		}
	}

	public static class EmailValidator extends PatternValidator {
		private static final Pattern EMAIL_PATTERN = Pattern.compile(".+@.+\\..+");

		public EmailValidator() {
			super(EMAIL_PATTERN);
		}
	}

	public static class TelephoneValidator extends PatternValidator {
		private static final Pattern PHONE_PATTERN = Pattern.compile(".*\\d+.*");

		public TelephoneValidator() {
			super(PHONE_PATTERN);
		}
	}
}
