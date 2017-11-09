package com.expedia.bookings.section;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.widget.TextViewExtensionsKt;
import com.expedia.bookings.widget.accessibility.AccessibleEditText;

/**
 * Validation indicator field for textviews and subclasses that sets the error icon and changes the text color if things aren't valid
 *
 * @param <Data>
 * @author jdrotos
 */
public class ValidationIndicatorExclamation<Data> extends
	SectionFieldValidIndicator<TextView, Data> {

	//Was this valid last time - this is to improve performance
	Drawable mDrawableRight;
	private String mErrorString = null;
	private int mDropDownInt = 0;

	public void setErrorString(String fieldId) {
		mErrorString = fieldId;
	}

	public void setMaterialDropdownResource(int dropdownId) {
		mDropDownInt = dropdownId;
	}

	public ValidationIndicatorExclamation(int fieldId) {
		super(fieldId);
	}

	@Override
	protected void onFieldBind() {
		super.onFieldBind();
		if (hasBoundField()) {
			mDrawableRight = getField().getCompoundDrawables()[2];
		}
	}

	@Override
	protected void onPostValidate(TextView field, boolean isValid, boolean force) {
		if (Strings.isNotEmpty(mErrorString) && (TextViewExtensionsKt.getParentTextInputLayout(field) != null)) {
			TextViewExtensionsKt.setMaterialFormsError(field, isValid, mErrorString, mDropDownInt);
		}
		else {
			if (!isValid) {
				TextViewExtensionsKt.addErrorExclamation(field);
			}
			else {
				TextViewExtensionsKt.removeErrorExclamation(field, null);
			}
		}
		if (field instanceof AccessibleEditText) {
			((AccessibleEditText) field).setValid(isValid);
			if (Strings.isNotEmpty(mErrorString)) {
				((AccessibleEditText) field).setErrorMessage(mErrorString);
			}
		}
	}
}
