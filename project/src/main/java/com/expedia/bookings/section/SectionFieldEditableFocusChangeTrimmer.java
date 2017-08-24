package com.expedia.bookings.section;

import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

/**
 * This class is a superclass of SectionFieldEditable, and its purpose for existing is to
 *
 * @param <FieldType>
 * @param <Data>
 */
public abstract class SectionFieldEditableFocusChangeTrimmer<FieldType extends EditText, Data> extends
		SectionFieldEditable<FieldType, Data> {

	public SectionFieldEditableFocusChangeTrimmer(int fieldId) {
		super(fieldId);
	}

	@Override
	protected void onFieldBind() {
		super.onFieldBind();
		if (hasBoundField()) {
			getField().setOnFocusChangeListener(mFocusChangeTrimmer);
		}
	}

	private final OnFocusChangeListener mFocusChangeTrimmer = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				//We lost focus, so lets trim.
				EditText et = (EditText) v;
				if (!hasFocus && !et.getText().toString().trim().equals(et.getText().toString())) {
					et.setText(et.getText().toString().trim());
				}
			}
		}
	};
}
