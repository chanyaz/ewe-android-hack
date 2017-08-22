package com.expedia.bookings.section;

import android.view.View;

public abstract class SectionFieldValidIndicator<FieldType extends View, Data> extends
		SectionField<FieldType, Data> {

	public SectionFieldValidIndicator(int fieldId) {
		super(fieldId);
	}

	@Override
	public void bindData(Data data) {
	}

	@Override
	protected void onHasFieldAndData(FieldType field, Data data) {
	}

	public void postValidate(boolean isValid) {
		if (this.hasBoundField()) {
			onPostValidate(getField(), isValid, false);
		}
	}

	protected abstract void onPostValidate(FieldType field, boolean isValid, boolean force);
}
