package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.NumberMaskFormatter;
import com.expedia.bookings.utils.NumberMaskTextWatcher;

/**
 * This EditText view gracefully applies formatting to a credit card or phone number input.
 *
 * Note: the formatting characters will not show up when calling toString(). To get the full string
 * with embellishments as it's displayed, call toFormattedString().
 *
 * Created by dmelton on 3/20/14.
 */
public class NumberMaskEditText extends EditText {

	private TextWatcher mTextWatcher;
	private String mCustomNumberFormat;
	private NumberMaskFormatter mFormatter;

	public NumberMaskEditText(Context context) {
		super(context);
		init(context, null, 0);
	}

	public NumberMaskEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public NumberMaskEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberMaskEditText, defStyle, 0);
		final String customNumberFormat = a.getString(R.styleable.NumberMaskEditText_customNumberFormat);
		final int numberFormatEnum = a.getInt(R.styleable.NumberMaskEditText_numberFormat, 0);
		a.recycle();

		if (!TextUtils.isEmpty(customNumberFormat)) {
			setCustomNumberFormat(customNumberFormat);
		}
		else {
			if (numberFormatEnum == 0) {
				setInputType(InputType.TYPE_CLASS_PHONE);
				mFormatter = new NumberMaskFormatter(NumberMaskFormatter.NORTH_AMERICAN_PHONE_NUMBER);
				mTextWatcher = new NumberMaskTextWatcher(mFormatter);
			}
			else if (numberFormatEnum == 1) {
				setInputType(InputType.TYPE_CLASS_NUMBER);

				// set maxLength = 19
				InputFilter[] filters = new InputFilter[1];
				filters[0] = new InputFilter.LengthFilter(getResources().getInteger(R.integer.max_credit_card_length));
				setFilters(filters);

				mFormatter = new NumberMaskFormatter(NumberMaskFormatter.CREDIT_CARD);
				mTextWatcher = new NumberMaskTextWatcher(mFormatter);
			}
			addTextChangedListener(mTextWatcher);
		}
	}

	public void setCustomNumberFormat(String customNumberFormat) {
		mCustomNumberFormat = customNumberFormat;
		replaceTextChangedListener(new NumberMaskTextWatcher(customNumberFormat));
	}

	public String getCustomNumberFormat() {
		return mCustomNumberFormat;
	}

	private void replaceTextChangedListener(TextWatcher listener) {
		if (mTextWatcher != null) {
			removeTextChangedListener(mTextWatcher);
		}
		mTextWatcher = listener;
		addTextChangedListener(mTextWatcher);
	}

	/**
	 * Returns the full, formatted text value of this view.
	 */
	public void toFormattedString() {
		mFormatter.applyTo(getEditableText().toString());
	}

	@Override
	public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		String text = this.getText().toString();
		String hint = this.getHint().toString();
		if (text.isEmpty()) {
			info.setText(" " + hint);
		}
		else {
			info.setText(" " + hint + ", " + text);
		}
	}
}

