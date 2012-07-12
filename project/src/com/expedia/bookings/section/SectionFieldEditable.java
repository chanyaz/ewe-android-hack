package com.expedia.bookings.section;

import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.Validator;

import android.view.View;

public abstract class SectionFieldEditable<FieldType extends View, Data extends Object> extends
		SectionField<FieldType, Data> {

	public SectionFieldEditable(int fieldId) {
		super(fieldId);
	}

	@Override
	protected void onFieldBind() {
		super.onFieldBind();
		if (this.hasBoundField()) {
			setChangeListener(this.getField());
		}
	}

	public boolean isValid() {
		FieldType field = getField();
		if(field == null){
			//If we don't have the field we return true.
			return true;
		}else{
			Validator<FieldType> validator = getValidator();
			return (validator == null) ? false : validator.validate(getField()) == ValidationError.NO_ERROR;
		}
	}

	/***
	 * The validator to call for the isValid method
	 * @return
	 */
	protected abstract Validator<FieldType> getValidator();

	/***
	 * This gets called when the field gets changed
	 */
	public abstract void setChangeListener(FieldType field);

}
