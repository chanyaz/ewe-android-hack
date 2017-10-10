package com.mobiata.android.validation;

/**
 * Interface for validating data of a particular type.
 * 
 * There are a few essential Validators that are already implemented; check them out
 * in the package before you start writing your own.
 */
public interface Validator<T> {

	/**
	 * Validates a variable, returning an error code if invalid.
	 * @param obj the object to validate
	 * @return an error code if there is an error, 0 if valid.
	 */
	public int validate(T obj);
}
