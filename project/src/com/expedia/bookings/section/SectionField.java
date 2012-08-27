package com.expedia.bookings.section;

import com.mobiata.android.util.Ui;

import android.app.Activity;
import android.view.View;

public abstract class SectionField<FieldType extends View, Data extends Object> {

	private int mFieldId;
	protected FieldType mField;
	protected Data mData;

	public SectionField(int fieldId) {
		mFieldId = fieldId;
	}

	public void bindField(View parent) {
		mField = Ui.findView(parent, mFieldId);
		onFieldBind();
	}

	public void bindField(Activity parent) {
		mField = Ui.findView(parent, mFieldId);
		onFieldBind();
	}

	protected void onFieldBind() {
		fieldOrDataUpdated();
	}

	public boolean hasBoundData() {
		return (mData == null) ? false : true;
	}

	public boolean hasBoundField() {
		return (mField == null) ? false : true;
	}

	public FieldType getField() {
		return mField;
	}

	public Data getData() {
		return mData;
	}

	public void bindData(Data data) {
		mData = data;
		fieldOrDataUpdated();
	}

	private void fieldOrDataUpdated() {
		if (hasBoundData() && hasBoundField()) {
			onHasFieldAndData(mField, mData);
		}
	}

	/***
	 * This gets called after we update the field or data, and both are present
	 */
	protected abstract void onHasFieldAndData(FieldType field, Data data);

}
