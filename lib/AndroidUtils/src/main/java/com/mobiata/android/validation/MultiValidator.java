package com.mobiata.android.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * A meta-validator that allows one to chain multiple validators
 * together, all to be applied to one object.
 * 
 * Validations are made in the order they are added.  Even if
 * multiple errors could be found, only the first is returned.
 */
public class MultiValidator<T> implements Validator<T> {

	public List<Validator<T>> mValidators;

	public MultiValidator() {
		mValidators = new ArrayList<>();
	}

	public void addValidator(Validator<T> validator) {
		mValidators.add(validator);
	}

	@Override
	public int validate(T obj) {
		for (Validator<T> validator : mValidators) {
			int errorCode = validator.validate(obj);
			if (errorCode != ValidationError.NO_ERROR) {
				return errorCode;
			}
		}
		return ValidationError.NO_ERROR;
	}
}
