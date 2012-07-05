package com.expedia.bookings.section;

import android.view.View;

public abstract class SectionFieldEditable<FieldType extends View, Data extends Object> extends
		SectionField<FieldType, Data> {

	public SectionFieldEditable(int fieldId) {
		super(fieldId);
	}
	
	@Override
	protected void onFieldBind(){
		super.onFieldBind();
		if(this.hasBoundField()){
			setChangeListener(this.getField());
		}
	}
	
	public boolean isValid(){
		return hasValidInput(getField());
	}
	
	/***
	 * This validates the data in the field
	 * @return
	 */
	protected abstract boolean hasValidInput(FieldType field);

	/***
	 * This gets called when the field gets changed
	 */
	public abstract void setChangeListener(FieldType field);
	
}
