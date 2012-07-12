package com.expedia.bookings.section;

import java.util.regex.Pattern;

import android.widget.EditText;

import com.mobiata.android.validation.PatternValidator;
import com.mobiata.android.validation.TextViewValidator;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.Validator;
import com.mobiata.android.validation.PatternValidator.TelephoneValidator;
import com.mobiata.android.validation.PatternValidator.EmailValidator;

public class CommonSectionValidators {
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
		TextViewValidator mValidator = new TextViewValidator(new TelephoneValidator());

		@Override
		public int validate(EditText obj) {
			return (obj == null) ? ValidationError.ERROR_DATA_MISSING : mValidator.validate(obj);
		}
	};

	public static final Validator<EditText> EMAIL_VALIDATOR_ET = new Validator<EditText>() {
		TextViewValidator mValidator = new TextViewValidator(new EmailValidator());

		@Override
		public int validate(EditText obj) {
			return (obj == null) ? ValidationError.ERROR_DATA_MISSING : mValidator.validate(obj);
		}
	};
}
