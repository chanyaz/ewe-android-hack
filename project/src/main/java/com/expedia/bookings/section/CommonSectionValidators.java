package com.expedia.bookings.section;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.expedia.bookings.section.InvalidCharacterHelper.Mode;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.validation.MultiValidator;
import com.mobiata.android.validation.PatternValidator;
import com.mobiata.android.validation.PatternValidator.TelephoneValidator;
import com.mobiata.android.validation.TextViewValidator;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.Validator;

public class CommonSectionValidators {

	public static final Validator<EditText> REQUIRED_FIELD_VALIDATOR_ET = new Validator<EditText>() {
		final TextViewValidator mValidator = new TextViewValidator();

		@Override
		public int validate(EditText obj) {
			return (obj == null) ? ValidationError.ERROR_DATA_MISSING : mValidator.validate(obj);
		}
	};

	public static final Validator<EditText> ALWAYS_VALID_VALIDATOR_ET = new Validator<EditText>() {
		@Override
		public int validate(EditText obj) {
			return ValidationError.NO_ERROR;
		}
	};

	public static final Validator<EditText> EXPIRATION_DATE_VALIDATOR_ET = new Validator<EditText>() {
		final PatternValidator mValidator = new PatternValidator(Pattern.compile("^\\d{1,2}/\\d{2}$"));

		@Override
		public int validate(EditText obj) {
			return (obj == null) ? ValidationError.ERROR_DATA_MISSING : mValidator.validate(obj.getText());
		}
	};

	public static final Validator<EditText> TELEPHONE_NUMBER_VALIDATOR_ET = new Validator<EditText>() {

		TextViewValidator mValidator;

		@Override
		public int validate(EditText obj) {

			if (mValidator == null) {
				MultiValidator<CharSequence> mMultiValidator = new MultiValidator<>();
				mMultiValidator.addValidator(new TelephoneValidator());
				mMultiValidator.addValidator(new PhoneNumberLengthValidator());
				mValidator = new TextViewValidator(mMultiValidator);
			}

			return (obj == null) ? ValidationError.ERROR_DATA_MISSING : mValidator.validate(obj);
		}
	};

	public static final Validator<String> TELEPHONE_NUMBER_VALIDATOR_STRING = new Validator<String>() {

		MultiValidator mValidator;

		@Override
		public int validate(String obj) {

			if (mValidator == null) {
				mValidator = new MultiValidator<>();
				mValidator.addValidator(new TelephoneValidator());
				mValidator.addValidator(new PhoneNumberLengthValidator());
			}

			return (obj == null) ? ValidationError.ERROR_DATA_MISSING : mValidator.validate(obj);
		}
	};

	public static class PhoneNumberLengthValidator extends TelephoneValidator {

		@Override
		public int validate(CharSequence text) {
			if (Strings.isEmpty(text)) {
				return ValidationError.ERROR_DATA_MISSING;
			}
			else {
				String userInput = text.toString();
				String filteredNumbers = userInput.replaceAll("[^0-9,]","");
				return filteredNumbers.length() > 15 || filteredNumbers.length() < 4 ?
					ValidationError.ERROR_DATA_INVALID : ValidationError.NO_ERROR;
			}
		}
	}

	public static final Validator<EditText> EMAIL_VALIDATOR_STRICT = new Validator<EditText>() {
		@Override
		public int validate(EditText obj) {
			if (obj == null) {
				return ValidationError.ERROR_DATA_MISSING;
			}
			return EMAIL_STRING_VALIDATIOR_STRICT.validate(obj.getText().toString());
		}
	};

	public static final Validator<String> EMAIL_STRING_VALIDATIOR_STRICT = new Validator<String>() {
		//This pattern is borrowed from iOS
		private static final String STRICT_EMAIL_VALIDATION_REGEX = "(?:[a-zA-Z0-9!#$%\\&'*+/=?\\^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%\\&'*+/=?\\^_`{|}~-]+)*|\\\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\\\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
		final PatternValidator mValidator = new PatternValidator(Pattern.compile(STRICT_EMAIL_VALIDATION_REGEX));

		@Override
		public int validate(String text) {
			if (TextUtils.isEmpty(text)) {
				return ValidationError.ERROR_DATA_MISSING;
			}
			else {
				if (text.length() >= 132) {
					return ValidationError.ERROR_DATA_INVALID;
				}
				else {
					return mValidator.validate(text.toLowerCase(Locale.ENGLISH));
				}
			}
		}
	};

	public static final Validator<RadioGroup> RADIO_GROUP_HAS_SELECTION = new Validator<RadioGroup>() {
		@Override
		public int validate(RadioGroup obj) {
			if (obj.getCheckedRadioButtonId() < 0) {
				return ValidationError.ERROR_DATA_MISSING;
			}
			return ValidationError.NO_ERROR;
		}
	};

	public static final Validator<EditText> SUPPORTED_CHARACTER_VALIDATOR_ASCII = new Validator<EditText>() {

		@Override
		public int validate(EditText obj) {
			if (obj == null) {
				return ValidationError.ERROR_DATA_MISSING;
			}
			Matcher matcher = InvalidCharacterHelper.getSupportedCharacterPattern(Mode.ASCII).matcher(obj.getText().toString());
			return matcher.matches() ? ValidationError.NO_ERROR : ValidationError.ERROR_DATA_INVALID;
		}
	};

	public static final Validator<EditText> SUPPORTED_CHARACTER_VALIDATOR_NAMES = new Validator<EditText>() {

		@Override
		public int validate(EditText obj) {
			if (obj == null) {
				return ValidationError.ERROR_DATA_MISSING;
			}
			Matcher matcher = InvalidCharacterHelper.getSupportedCharacterPattern(Mode.NAME).matcher(obj.getText());
			return matcher.matches() ? ValidationError.NO_ERROR : ValidationError.ERROR_DATA_INVALID;
		}
	};

	public static final Validator<EditText> NAME_PATTERN_VALIDATOR = new Validator<EditText>() {
		//This pattern is borrowed from iOS
		private static final String NAME_PATTERN_VALIDATOR_REGEX = "^.+\\s+.+$";
		final PatternValidator mValidator = new PatternValidator(Pattern.compile(NAME_PATTERN_VALIDATOR_REGEX));

		@Override
		public int validate(EditText obj) {
			if (TextUtils.isEmpty(obj.getText().toString())) {
				return ValidationError.ERROR_DATA_MISSING;
			}
			else {
				return mValidator.validate(obj.getText().toString());
			}
		}
	};

	public static final Validator<String> NON_EMPTY_VALIDATOR = new Validator<String>() {
		@Override
		public int validate(String string) {
			if (TextUtils.isEmpty(string)) {
				return ValidationError.ERROR_DATA_MISSING;
			}
			return ValidationError.NO_ERROR;
		}
	};

	public static final Validator<String> SUPPORTED_CHARACTER_VALIDATOR_NAMES_STRING = new Validator<String>() {

		@Override
		public int validate(String string) {
			if (string == null) {
				return ValidationError.ERROR_DATA_MISSING;
			}
			Matcher matcher = InvalidCharacterHelper.getSupportedCharacterPattern(Mode.NAME).matcher(string);
			return matcher.matches() ? ValidationError.NO_ERROR : ValidationError.ERROR_DATA_INVALID;
		}
	};

	public static final Validator<EditText> ADDRESS_STATE_VALIDATOR = new Validator<EditText>() {
		@Override
		public int validate(EditText obj) {
			if (obj == null) {
				return ValidationError.ERROR_DATA_MISSING;
			}
			return STATE_VALIDATOR_LENGTH.validate(obj.getText().toString());
		}
	};

	public static final Validator<String> STATE_VALIDATOR_LENGTH = new Validator<String>() {
		@Override
		public int validate(String text) {
			if (TextUtils.isEmpty(text)) {
				return ValidationError.ERROR_DATA_MISSING;
			}
			else if (text.length() < 2 || text.length() > 100) {
				return ValidationError.ERROR_DATA_INVALID;
			}
			return ValidationError.NO_ERROR;
		}
	};

}
