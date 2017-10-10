package com.mobiata.android.validation;

import android.widget.TextView;

/**
 * A Validator for TextViews.  It assumes that you want to check that the field
 * has any data in it, so it always checks that the field has data.  Once its done
 * that, it allows you to validate the CharSequence in the TextView however you want.
 * 
 * (Remember to use a MultiValidator if you want to do multiple validations on the
 * CharSequence.)
 */
public class TextViewValidator implements Validator<TextView> {

	private Validator<CharSequence> mValidator;

	public TextViewValidator() {
		mValidator = RequiredValidator.getInstance();
	}

	public TextViewValidator(Validator<CharSequence> validator) {
		MultiValidator<CharSequence> multiValidator = new MultiValidator<>();
		multiValidator.addValidator(RequiredValidator.getInstance());
		multiValidator.addValidator(validator);
		mValidator = multiValidator;
	}

	@Override
	public int validate(TextView inputView) {
		int errorCode = mValidator.validate(inputView.getText().toString());
		return (errorCode == ValidationError.NO_ERROR) ? ValidationError.NO_ERROR : errorCode;
	}
}
