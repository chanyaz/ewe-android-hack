package com.expedia.bookings.section;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.utils.FeatureToggleUtil;
import com.expedia.bookings.widget.TextViewExtensions;
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
	Drawable mDrawableRight;
	private int mErrorString = -1;

	public void setErrorString(int fieldId) {
		mErrorString = fieldId;
	}

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
		boolean materialFormTestEnabled = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(field.getContext(),
			AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, R.string.preference_universal_checkout_material_forms);

		if (materialFormTestEnabled && mErrorString != -1) {
			String errorMessage = field.getContext().getResources().getString(mErrorString);
			TextViewExtensions.Companion.setMaterialFormsError(field, isValid, errorMessage);
		} else {
			if (!isValid) {
				TextViewExtensionsKt.addErrorExclamation(field);
			}
			else {
				TextViewExtensionsKt.removeErrorExclamation(field, null);
			}
			if (field instanceof AccessibleEditText) {
				((AccessibleEditText) field).setValid(isValid);
			}
			else if (field instanceof AccessibleTextViewForSpinner) {
				((AccessibleTextViewForSpinner) field).setValid(isValid);
			}
		}
	}

}
