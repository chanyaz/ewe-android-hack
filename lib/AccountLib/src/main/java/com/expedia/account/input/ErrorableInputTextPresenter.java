package com.expedia.account.input;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.expedia.account.R;
import com.expedia.account.util.StatusObservableWrapper;
import com.expedia.account.util.Utils;
import com.expedia.account.view.AccessibleEditText;
import com.expedia.account.view.CheckmarkPresenter;

public class ErrorableInputTextPresenter extends BaseInputTextPresenter {

	private int mWaitingColor = -1;
	private int mGoodColor = -1;
	private int mBadColor = -1;
	private int mHintColor = -1;

	private static final
	@ColorRes
	int DEFAULT_WAITING_COLOR_RES = R.color.acct__default_bg_input_text_default;
	private static final
	@ColorRes
	int DEFAULT_GOOD_COLOR_RES = R.color.acct__default_bg_input_text_good;
	private static final
	@ColorRes
	int DEFAULT_BAD_COLOR_RES = R.color.acct__default_bg_input_text_error;
	private static final
	@ColorRes
	int DEFAULT_HINT_COLOR_RES = R.color.acct__default_edit_text_hint_color;


	// While we don't directly use this in the class, it exists for external code
	// to know that it can say "I don't care, don't change anything" in its validation code.

	private CheckmarkPresenter vCheckmark;
	private TextView vDescriptionErrorView;

	public ErrorableInputTextPresenter(final Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.acct__widget_errorable_text_input, this);

		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.acct__ErrorableInputText, 0, 0);
		try {
			mWaitingColor = ta.getColor(R.styleable.acct__ErrorableInputText_acct__eit_waitingColor,
				getResources().getColor(DEFAULT_WAITING_COLOR_RES));
			mBadColor = ta.getColor(R.styleable.acct__ErrorableInputText_acct__eit_badColor,
				getResources().getColor(DEFAULT_BAD_COLOR_RES));
			mGoodColor = ta.getColor(R.styleable.acct__ErrorableInputText_acct__eit_goodColor,
				getResources().getColor(DEFAULT_GOOD_COLOR_RES));
		}
		finally {
			ta.recycle();
		}

		editText = (AccessibleEditText) findViewById(R.id.input_text);
		vDescriptionErrorView = (TextView) findViewById(R.id.suggestion_error_text);
		vCheckmark = (CheckmarkPresenter) findViewById(R.id.checkmark_presenter);

		editText.setHint(hintText);
		editText.setInputType(inputType);
		editText.setImeOptions(imeOptions);
		editText.setErrorContDesc(errorContDesc);
		fixPasswordFont();

		vDescriptionErrorView.setText(errorText);
		vDescriptionErrorView.setTextColor(mBadColor);

		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (mValidator != null) {
					showInternal(mValidator.onNewText(charSequence.toString()));
				}

			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});

		editText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (mValidator != null) {
					showInternal(mValidator.onFocusChanged(editText.getText().toString(), hasFocus));
				}
			}
		});

		createAndAddTransitions();

		mStatusObservable = new StatusObservableWrapper(new StatusObservableWrapper.StatusEmitter() {
			@Override
			public boolean isGood() {
				return ErrorableInputTextPresenter.this.isGood();
			}
		});

		show(STATE_WAITING);
	}

	public void styleizeFromAccountView(TypedArray a) {
		mWaitingColor = a.getColor(R.styleable.acct__AccountView_acct__input_text_default_color,
			getResources().getColor(DEFAULT_WAITING_COLOR_RES));

		mGoodColor = a.getColor(R.styleable.acct__AccountView_acct__input_text_good_color,
			getResources().getColor(DEFAULT_GOOD_COLOR_RES));

		mBadColor = a.getColor(R.styleable.acct__AccountView_acct__input_text_error_color,
			getResources().getColor(DEFAULT_BAD_COLOR_RES));

		mHintColor = a.getColor(R.styleable.acct__AccountView_acct__input_text_hint_color,
			getResources().getColor(DEFAULT_HINT_COLOR_RES));

		editText.setHintTextColor(mHintColor);
		vDescriptionErrorView.setTextColor(mBadColor);
		vCheckmark.styleizeFromAccountView(a);
	}

	public void brandIt(String brand) {
		Utils.brandHint(editText, brand);
		Utils.brandText(vDescriptionErrorView, brand);
	}

	@Override
	protected void showError() {
		editText.setStatus(AccessibleEditText.Status.INVALID);
		mStatusObservable.emit(false);
		vCheckmark.show(CheckmarkPresenter.STATE_BAD);
		vDescriptionErrorView.setVisibility(View.VISIBLE);
		changeEditText(mBadColor, R.drawable.acct__bg_input_text);
	}

	@Override
	protected void showGood() {
		editText.setStatus(AccessibleEditText.Status.VALID);
		mStatusObservable.emit(true);
		vCheckmark.show(CheckmarkPresenter.STATE_GOOD);
		vDescriptionErrorView.setVisibility(View.GONE);
		changeEditText(mGoodColor, R.drawable.acct__bg_input_text);
	}

	@Override
	protected void showWaiting() {
		editText.setStatus(AccessibleEditText.Status.DEFAULT);
		mStatusObservable.emit(false);
		vCheckmark.show(CheckmarkPresenter.STATE_HIDDEN);
		vDescriptionErrorView.setVisibility(View.GONE);
		changeEditText(mWaitingColor, R.drawable.acct__bg_input_text_thin);
	}

	@Override
	protected void showProgress() {
		editText.setStatus(AccessibleEditText.Status.DEFAULT);
		mStatusObservable.emit(false);
		vCheckmark.show(CheckmarkPresenter.STATE_HIDDEN);
		vDescriptionErrorView.setVisibility(View.GONE);
		changeEditText(mGoodColor, R.drawable.acct__bg_input_text);
	}
}
