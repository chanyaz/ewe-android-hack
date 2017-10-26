package com.expedia.account.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.expedia.account.R;
import com.expedia.account.input.ErrorableInputTextPresenter;
import com.expedia.account.input.InputRule;
import com.expedia.account.input.InputValidator;
import com.expedia.account.input.rules.ExpediaPasswordInputRule;
import com.expedia.account.util.CombiningFakeObservable;
import com.expedia.account.util.Events;
import com.expedia.account.util.Utils;

import rx.Observer;

public class PasswordLayout extends KeyboardObservingFrameLayout {

	private CombiningFakeObservable mValidationObservable;

	public void setNextButtonController(Observer<Boolean> nextController) {
		mValidationObservable.subscribe(nextController);
	}

	private View vSpace;

	private ErrorableInputTextPresenter vFirstPassword;
	private ErrorableInputTextPresenter vSecondPassword;

	public boolean passwordsAreValid() {
		return (firstPasswordIsValid() && secondPasswordIsValid());
	}

	public boolean firstPasswordIsValid() {
		return Utils.passwordIsValidForAccountCreation(vFirstPassword.getText());
	}

	public boolean secondPasswordIsValid() {
		return secondPasswordStatus() == MATCHES_LENGTH_AND_CONTENT;
	}

	private final int TOO_SHORT = 0;
	private final int TOO_LONG = 1;
	private final int MATCHES_LENGTH_ONLY = 2;
	private final int MATCHES_LENGTH_AND_CONTENT = 3;

	public int secondPasswordStatus() {
		String password = vFirstPassword.getText();
		String confirmPassword = vSecondPassword.getText();
		int match = TOO_SHORT;
		if (password.length() == confirmPassword.length() && firstPasswordIsValid()) {
			match = MATCHES_LENGTH_ONLY;
			if (password.equals(confirmPassword)) {
				match = MATCHES_LENGTH_AND_CONTENT;
			}
		}
		else if (confirmPassword.length() > password.length()) {
			match = TOO_LONG;
		}
		return match;
	}

	public PasswordLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.acct__widget_password, this);

		vSpace = findViewById(R.id.password_bottom_space);
		vFirstPassword = (ErrorableInputTextPresenter) findViewById(R.id.first_password);
		vSecondPassword = (ErrorableInputTextPresenter) findViewById(R.id.second_password);

		mValidationObservable = new CombiningFakeObservable();
		mValidationObservable.addSource(vFirstPassword.getStatusObservable());
		mValidationObservable.addSource(vSecondPassword.getStatusObservable());

		vFirstPassword.setEditTextContentDescription(context.getString(R.string.acct__Create_Password));
		vSecondPassword.setEditTextContentDescription(context.getString(R.string.acct__Confirm_Password));

		vFirstPassword.setValidator(new InputValidator(new ExpediaPasswordInputRule()) {
			@Override
			public int onNewText(String input) {

				if (secondPasswordStatus() == MATCHES_LENGTH_AND_CONTENT) {
					vSecondPassword.show(ErrorableInputTextPresenter.STATE_GOOD);
				}
				else if (vSecondPassword.getText().length() > 0) {
					vSecondPassword.show(ErrorableInputTextPresenter.STATE_PROGRESS);
				}
				else {
					vSecondPassword.show(ErrorableInputTextPresenter.STATE_WAITING);
				}
				return super.onNewText(input);
			}
		});

		vSecondPassword.setValidator(new InputValidator(new InputRule() {
			@Override
			public int evaluateInput(String input) {
				switch (secondPasswordStatus()) {
				case MATCHES_LENGTH_AND_CONTENT:
					return DEFINITELY_GOOD;
				case MATCHES_LENGTH_ONLY:
				case TOO_LONG:
					return IRREPARABLY_BAD;
				case TOO_SHORT:
				default:
					return COULD_EVENTUALLY_BE_GOOD;
				}
			}
		}) {
			@Override
			public int onNewText(String input) {
				if (firstPasswordIsValid()) {
					return super.onNewText(input);
				}
				else {
					return ErrorableInputTextPresenter.PROGRESS;
				}
			}

			@Override
			public int onFocusChanged(String input, boolean hasFocus) {
				if (firstPasswordIsValid()) {
					return super.onFocusChanged(input, hasFocus);
				}
				else {
					if (!hasFocus && input.length() == 0) {
						return ErrorableInputTextPresenter.WAITING;
					}
					else {
						return ErrorableInputTextPresenter.PROGRESS;
					}
				}
			}
		});

		vSecondPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
					if (passwordsAreValid()) {
						Events.post(new Events.NextFromPasswordFired());
					}
					return true;
				}
				return false;
			}
		});

		vFirstPassword.show(ErrorableInputTextPresenter.STATE_WAITING);
		vSecondPassword.show(ErrorableInputTextPresenter.STATE_WAITING);
	}

	public void styleizeFromAccountView(TypedArray a) {
		vFirstPassword.styleizeFromAccountView(a);
		vSecondPassword.styleizeFromAccountView(a);
	}

	public void brandIt(String brand) {
		vFirstPassword.brandIt(brand);
		vSecondPassword.brandIt(brand);
	}

	public String getPassword() {
		return vFirstPassword.getText();
	}

	@Override
	public void onKeyboardVisibilityChanged(boolean isVisible) {
		// This is a special case where vSpace at the bottom of the screen
		// is a large enough space to accommodate the entire keyboard
		if (isVisible && vSpace.getHeight() > getKeyboardHeight()) {
			vSpace.setVisibility(View.VISIBLE);
			setBottomMargin(-getKeyboardHeight());
		}
		else {
			vSpace.setVisibility(isVisible ? View.GONE : View.VISIBLE);
			setBottomMargin(0);
		}
	}

	public void requestFocus(boolean forward) {
		if (forward) {
			vFirstPassword.requestFocus(forward);
		}
		else {
			vSecondPassword.requestFocus(forward);
		}
	}
}
