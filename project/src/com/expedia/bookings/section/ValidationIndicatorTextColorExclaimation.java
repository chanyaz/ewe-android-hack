package com.expedia.bookings.section;

import android.content.res.ColorStateList;
import android.graphics.Color;
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
public class ValidationIndicatorTextColorExclaimation<Data extends Object> extends
		SectionFieldValidIndicator<TextView, Data> {
	
	//This gets pulled from the textview
	ColorStateList mValidColor;
	
	//Error color
	int mInvalidTextColor = Color.RED;
	
	//Was this valid last time - this is to improve performance
	Boolean mWasValid = true;
	
	public ValidationIndicatorTextColorExclaimation(int fieldId) {
		super(fieldId);
	}

	public void setInvalidTextColor(int color){
		mInvalidTextColor = color;
	}
	
	@Override
	protected void onPostValidate(TextView field, boolean isValid) {
		if (!isValid && mWasValid) {
			//Not valid, but it was the last time we validated
			mValidColor = field.getTextColors();
			field.setTextColor(mInvalidTextColor);
			Drawable errorIcon = field.getContext().getResources().getDrawable(R.drawable.ic_error);
			errorIcon.setBounds(new Rect(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight()));
			field.setError(null, errorIcon);
			mWasValid = false;
		}
		else if (isValid && !mWasValid) {
			//Freshly valid
			field.setTextColor(mValidColor);
			field.setError(null, null);
			mWasValid = true;
		}
	}
}
