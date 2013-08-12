package com.expedia.bookings.section;

import java.util.regex.Pattern;

import android.widget.EditText;
import android.widget.RadioGroup;

import com.expedia.bookings.section.InvalidCharacterHelper.Mode;
import com.mobiata.android.validation.MultiValidator;
import com.mobiata.android.validation.PatternValidator;
import com.mobiata.android.validation.PatternValidator.EmailValidator;
import com.mobiata.android.validation.PatternValidator.TelephoneValidator;
import com.mobiata.android.validation.TextViewValidator;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.Validator;

public class CommonSectionValidators {
	
	public static final String STRICT_EMAIL_VALIDATION_REGEX = "(?:[a-zA-Z0-9!#$%\\&'*+/=?\\^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%\\&'*+/=?\\^_`{|}~-]+)*|\\\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\\\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
	
	public static final Validator<EditText> REQUIRED_FIELD_VALIDATOR_ET = new Validator<EditText>() {
		TextViewValidator mValidator = new TextViewValidator();

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
		PatternValidator mValidator = new PatternValidator(Pattern.compile("^\\d{1,2}/\\d{2}$"));

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
				MultiValidator<CharSequence> mMultiValidator = new MultiValidator<CharSequence>();
				mMultiValidator.addValidator(new TelephoneValidator());
				mMultiValidator.addValidator(new PhoneNumberLengthValidator());
				mValidator = new TextViewValidator(mMultiValidator);
			}

			return (obj == null) ? ValidationError.ERROR_DATA_MISSING : mValidator.validate(obj);
		}
	};

	public static class PhoneNumberLengthValidator extends PatternValidator {
		private static final Pattern THREE_NUMBERS_PATTERN = Pattern.compile(".*\\d+.*\\d+.*\\d+.*");//atleast three digits

		public PhoneNumberLengthValidator() {
			super(THREE_NUMBERS_PATTERN);
		}
	}

	public static final Validator<EditText> EMAIL_VALIDATOR_ET = new Validator<EditText>() {
		TextViewValidator mValidator = new TextViewValidator(new EmailValidator());

		@Override
		public int validate(EditText obj) {
			return (obj == null) ? ValidationError.ERROR_DATA_MISSING : mValidator.validate(obj);
		}
	};

	public static final Validator<EditText> EMAIL_VALIDATOR_STRICT = new Validator<EditText>() {

		//This pattern is borrowed from iOS
		PatternValidator mValidator = new PatternValidator(
				Pattern.compile(STRICT_EMAIL_VALIDATION_REGEX));

		@Override
		public int validate(EditText obj) {
			if (obj == null) {
				return ValidationError.ERROR_DATA_MISSING;
			}
			else {
				if (obj.getText().length() >= 132) {
					return ValidationError.ERROR_DATA_INVALID;
				}
				else {
					return mValidator.validate(obj.getText());
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
			else {
				return InvalidCharacterHelper.getSupportedCharacterPattern(Mode.ASCII).matcher(obj.getText()).matches() ? ValidationError.NO_ERROR
						: ValidationError.ERROR_DATA_INVALID;
			}
		}
	};

	public static final Validator<EditText> SUPPORTED_CHARACTER_VALIDATOR_NAMES = new Validator<EditText>() {

		@Override
		public int validate(EditText obj) {
			if (obj == null) {
				return ValidationError.ERROR_DATA_MISSING;
			}
			else {
				return InvalidCharacterHelper.getSupportedCharacterPattern(Mode.NAME).matcher(obj.getText()).matches() ? ValidationError.NO_ERROR
						: ValidationError.ERROR_DATA_INVALID;
			}
		}
	};

}
