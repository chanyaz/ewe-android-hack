package com.expedia.bookings.section;

import java.util.ArrayList;

import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.widget.TextViewExtensionsKt;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.Validator;

public abstract class SectionFieldEditable<FieldType extends View, Data> extends
		SectionField<FieldType, Data> {

	final ArrayList<SectionFieldValidIndicator<?, Data>> mPostValidators = new ArrayList<SectionFieldValidIndicator<?, Data>>();

	public SectionFieldEditable(int fieldId) {
		super(fieldId);
	}

	@Override
	protected void onFieldBind() {
		super.onFieldBind();
		if (this.hasBoundField()) {
			setChangeListener(this.getField());
			setPostValidators(this.getPostValidators());
		}
	}

	public boolean isValid() {
		return doValidation();
	}

	private boolean doValidation() {
		boolean valid = true;
		if (hasBoundField()) {
			Validator<FieldType> validator = getValidator();
			valid = validator != null && validator.validate(getField()) == ValidationError.NO_ERROR;
		}
		firePostValidators(valid);
		return valid;
	}

	private void firePostValidators(boolean isValid) {
		for (SectionFieldValidIndicator<?, Data> postVal : mPostValidators) {
			postVal.postValidate(isValid);
		}
	}

	private void setPostValidators(ArrayList<SectionFieldValidIndicator<?, Data>> postValidators) {
		mPostValidators.clear();
		if (postValidators != null) {
			mPostValidators.addAll(postValidators);
		}
	}


	/***
	 * This should be called everytime the field's value changes.
	 * @param parent - SectionFieldEditable instances should always be part of ISectionEditable classes, which also wish to be notified of changes. We call parent.onChange from this onChange method
	 */
	public void onChange(ISectionEditable parent) {
		FieldType field = getField();
		if (field instanceof TextView) {
			TextViewExtensionsKt.removeErrorExclamation((TextView) field, null);
		}
		if (field != null && field.getParent() != null && field.getParent() instanceof TextInputLayout) {
			((TextInputLayout) field.getParent()).setError(null);
			((TextInputLayout) field.getParent()).setErrorEnabled(false);
		}
		if (parent != null) {
			parent.onChange();
		}
	}

	/***
	 * The validator to call for the isValid method
	 * @return
	 */
	protected abstract Validator<FieldType> getValidator();

	/***
	 * Returns a list of PostValidators to be run after validation. These are usually used to update visual feedback about the state of a field
	 * e.g. If a user's email address isn't formatted correctly we may want to change the text color to red.
	 * @return ArrayList of SectionFeildValidIndicators or null
	 *
	 */
	protected abstract ArrayList<SectionFieldValidIndicator<?, Data>> getPostValidators();

	/***
	 * IMPORTANT - This method has a contract to add a change listener to the field that calls SectionFieldEditable.onChange()
	 * our hook system will get messed up, and because FieldType is generic we depend on fieldType specific change listener to call
	 * onChange(parent);
	 *
	 */
	public abstract void setChangeListener(FieldType field);

}
