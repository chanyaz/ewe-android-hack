package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.section.CreditCardInputSection;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Ui;

/**
 * Created by dmelton on 3/4/14.
 */
public class CVVTextView extends TextView implements CreditCardInputSection.CreditCardInputListener {

	public CVVTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		setTypeface(FontCache.getTypeface(FontCache.Font.OCRA_STD));
	}

	public void setCvvErrorMode(boolean enabled) {
		int colorResId = (enabled) ? R.color.cvv_error : R.color.cvv_normal;
		setTextColor(getContext().getResources().getColor(colorResId));

		// For some reason, the padding gets lost when you set a background to a 9-patch.  Preserve
		// the padding and reset it after we update the bg.
		int left = getPaddingLeft();
		int top = getPaddingTop();
		int right = getPaddingRight();
		int bottom = getPaddingBottom();
		int bgResId = (enabled) ? Ui.obtainThemeResID(getContext(), R.attr.skin_cvvDivErrorDrawable)
			: Ui.obtainThemeResID(getContext(), R.attr.skin_cvvDivDrawable);
		setBackgroundResource(bgResId);
		setPadding(left, top, right, bottom);
	}

	public String getCvv() {
		return getText().toString();
	}

	//////////////////////////////////////////////////////////////////////////
	// CreditCardInputListener

	@Override
	public void onKeyPress(int code) {
		String currText = getText().toString();
		int len = currText.length();
		if (code == CreditCardInputSection.CODE_DELETE) {
			if (len != 0) {
				currText = currText.substring(0, len - 1);
			}
		}
		else {
			if (len != 4) {
				currText += code;
			}
		}
		setText(currText);
	}
}
