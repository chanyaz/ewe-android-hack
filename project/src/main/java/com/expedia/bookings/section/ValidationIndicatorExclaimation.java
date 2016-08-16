package com.expedia.bookings.section;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.expedia.bookings.widget.TextViewExtensionsKt;
import com.expedia.bookings.widget.accessibility.AccessibleEditText;
import com.expedia.bookings.widget.accessibility.AccessibleTextViewForSpinner;

/**
 * Validation indicator field for textviews and subclasses that sets the error icon and changes the text color if things aren't valid
 *
 * @param <Data>
 * @author jdrotos
 */
public class ValidationIndicatorExclaimation<Data extends Object> extends
	SectionFieldValidIndicator<TextView, Data> {

	//Was this valid last time - this is to improve performance
	Boolean mWasValid = true;
	Drawable mDrawableRight;

	public ValidationIndicatorExclaimation(int fieldId) {
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
		if (!isValid && (force || mWasValid)) {
			//Not valid, but it was the last time we validated
			TextViewExtensionsKt.addErrorExclamation(field);
			if (field instanceof AccessibleEditText) {
				((AccessibleEditText) field).setValid(false);
			}
			else if (field instanceof AccessibleTextViewForSpinner) {
				((AccessibleTextViewForSpinner) field).setValid(false);
			}
			mWasValid = false;
		}
		else if (isValid && (force || !mWasValid)) {
			//Freshly valid
			TextViewExtensionsKt.removeErrorExclamation(field, mDrawableRight);
			if (field instanceof AccessibleEditText) {
				((AccessibleEditText) field).setValid(true);
			}
			else if (field instanceof AccessibleTextViewForSpinner) {
				((AccessibleTextViewForSpinner) field).setValid(true);
			}
			mWasValid = true;
		}
	}
}
