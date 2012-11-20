package com.expedia.bookings.section;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.expedia.bookings.R;

/**
 * Validation indicator field for textviews and subclasses that sets the error icon and changes the text color if things aren't valid
 * @author jdrotos
 *
 * @param <Data>
 */
public class ValidationIndicatorExclaimation<Data extends Object> extends
		SectionFieldValidIndicator<TextView, Data> {

	//Was this valid last time - this is to improve performance
	Boolean mWasValid = true;

	public ValidationIndicatorExclaimation(int fieldId) {
		super(fieldId);
	}

	@Override
	protected void onPostValidate(TextView field, boolean isValid) {
		if (!isValid && mWasValid) {
			//Not valid, but it was the last time we validated
			Drawable errorIcon = field.getContext().getResources().getDrawable(R.drawable.ic_error_blue);
			errorIcon.setBounds(new Rect(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight()));
			Drawable[] compounds = field.getCompoundDrawables();
			field.setCompoundDrawablesWithIntrinsicBounds(compounds[0], compounds[1], errorIcon, compounds[3]);
			mWasValid = false;
		}
		else if (isValid && !mWasValid) {
			//Freshly valid
			Drawable[] compounds = field.getCompoundDrawables();
			field.setCompoundDrawablesWithIntrinsicBounds(compounds[0], compounds[1], null, compounds[3]);
			mWasValid = true;
		}
	}
}
