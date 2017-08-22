package com.expedia.bookings.section;

import android.view.View;

/**
 * Validation indicator field for Spinner that displays the error icon
 *
 * @param <Data>
 */
public class ValidationIndicatorExclamationSpinner<Data> extends
	SectionFieldValidIndicator<RailDeliverySpinnerWithValidationIndicator, Data> {

	Boolean mWasValid = true;

	public ValidationIndicatorExclamationSpinner(int fieldId) {
		super(fieldId);
	}

	@Override
	protected void onPostValidate(RailDeliverySpinnerWithValidationIndicator field, boolean isValid, boolean force) {
		if (!isValid && (force || mWasValid)) {
			//Not valid, but it was the last time we validated
			field.getValidationIndicator().setVisibility(View.VISIBLE);
			mWasValid = false;
		}
		else if (isValid && (force || !mWasValid)) {
			//Freshly valid
			field.getValidationIndicator().setVisibility(View.GONE);
			mWasValid = true;
		}
	}
}
