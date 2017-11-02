package com.expedia.account.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.expedia.account.R;
import com.expedia.account.input.ErrorableInputTextPresenter;
import com.expedia.account.input.rules.ExpediaEmailInputRule;
import com.expedia.account.input.rules.ExpediaPasswordSignInInputRule;
import com.expedia.account.util.CombiningFakeObservable;
import com.expedia.account.util.Events;
import com.expedia.account.util.Utils;
import com.squareup.phrase.Phrase;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class SignInLayout extends KeyboardObservingFrameLayout {

	public ErrorableInputTextPresenter vEmailAddressPresenter;
	public ErrorableInputTextPresenter vPasswordPresenter;
	public View vForgotPassword;
	public Button vSignInButton;
	public Button vSignInWithFacebookButton;
	public View vSpaceAboveButtons;
	public View vKeyboardVisibleSpaceAboveButtons;
	public View vSpaceBelowButtons;
	public View vKeyboardVisibleSpaceBelowButtons;
	public View vKeyboardHiddenFields;
	public View vOrRow;
	public Button vCreateAccountButton;
	public View vSpaceBottom;
	public TextView vGoogleAccountChange;
	public String brand;

	private boolean mEnableFacebookButton = false;

	public SignInLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.acct__widget_sign_in, this);

		vEmailAddressPresenter = (ErrorableInputTextPresenter) findViewById(R.id.email_address_sign_in);
		vPasswordPresenter = (ErrorableInputTextPresenter) findViewById(R.id.password);
		vForgotPassword = findViewById(R.id.forgot_password);
		vSignInButton = (Button) findViewById(R.id.sign_in_button);
		vSignInWithFacebookButton = (Button) findViewById(R.id.sign_in_with_facebook_button);
		vSpaceAboveButtons = findViewById(R.id.space_above_buttons);
		vKeyboardVisibleSpaceAboveButtons = findViewById(R.id.keyboard_visible_space_above_buttons);
		vSpaceBelowButtons = findViewById(R.id.space_below_buttons);
		vKeyboardVisibleSpaceBelowButtons = findViewById(R.id.keyboard_visible_space_below_buttons);
		vKeyboardHiddenFields = findViewById(R.id.keyboard_hidden_fields);
		vOrRow = findViewById(R.id.or_row);
		vCreateAccountButton = (Button) findViewById(R.id.create_account);
		vSpaceBottom = findViewById(R.id.sign_in_bottom_space);
		vGoogleAccountChange = (TextView) findViewById(R.id.google_account_change);

		setGoogleAccountVisibilityAndClickListener(context);

		vSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				vSignInWithFacebookButton.setEnabled(false);
				vCreateAccountButton.setEnabled(false);
				String email = vEmailAddressPresenter.getText();
				String pass = vPasswordPresenter.getText();
				Events.post(new Events.SignInButtonClicked(email, pass));
			}
		});
		vPasswordPresenter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if ((actionId == EditorInfo.IME_ACTION_DONE
						|| actionId == EditorInfo.IME_ACTION_NEXT) && vSignInButton.isEnabled()) {
					vSignInButton.callOnClick();
					return true;
				}
				return false;
			}
		});

		vForgotPassword.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Events.post(new Events.ForgotPasswordButtonClicked());
			}
		});

		vSignInWithFacebookButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				vSignInButton.setEnabled(false);
				vCreateAccountButton.setEnabled(false);
				Events.post(new Events.SignInWithFacebookButtonClicked());
			}
		});

		vCreateAccountButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				vSignInButton.setEnabled(false);
				vSignInWithFacebookButton.setEnabled(false);
				Events.post(new Events.CreateAccountButtonClicked());
			}
		});

		vEmailAddressPresenter.setValidator(new ExpediaEmailInputRule());
		vPasswordPresenter.setValidator(new ExpediaPasswordSignInInputRule());

		CombiningFakeObservable validationObservable = new CombiningFakeObservable();
		validationObservable.addSource(vEmailAddressPresenter.getStatusObservable());
		validationObservable.addSource(vPasswordPresenter.getStatusObservable());
		validationObservable.subscribe(new Observer<Boolean>() {
			@Override
			public void onComplete() {

			}

			@Override
			public void onError(Throwable e) {

			}

			@Override
			public void onSubscribe(Disposable d) {

			}

			@Override
			public void onNext(Boolean aBoolean) {
				vSignInButton.setEnabled(aBoolean);
			}
		});

	}

	private void setGoogleAccountVisibilityAndClickListener(Context context) {
		boolean isGoogleAccountChangeEnabled = getResources().getBoolean(R.bool.acct__isGoogleAccountChangeEnabled);
		vGoogleAccountChange.setVisibility(isGoogleAccountChangeEnabled ? View.VISIBLE : View.GONE);
		if (isGoogleAccountChangeEnabled) {
			vGoogleAccountChange.setOnClickListener(new GoogleAccountChangeListener());
		}
	}

	public void styleizeFromAccountView(TypedArray a) {
		if (a.hasValue(R.styleable.acct__AccountView_acct__sign_in_button_background_drawable)) {
			vSignInButton.setBackground(a.getDrawable(
				R.styleable.acct__AccountView_acct__sign_in_button_background_drawable));
		}
		if (a.hasValue(R.styleable.acct__AccountView_acct__sign_in_button_text_color)) {
			vSignInButton.setTextColor(a.getColor(R.styleable.acct__AccountView_acct__sign_in_button_text_color, 0));
		}
		if (a.hasValue(R.styleable.acct__AccountView_acct__flavor_text_color)) {
			int color = a.getColor(R.styleable.acct__AccountView_acct__flavor_text_color, 0);
			findViewById(R.id.dash_left).setBackgroundColor(color);
			findViewById(R.id.dash_right).setBackgroundColor(color);
			((TextView) findViewById(R.id.or)).setTextColor(color);
			vCreateAccountButton.setTextColor(color);
		}

		vEmailAddressPresenter.styleizeFromAccountView(a);
		vPasswordPresenter.styleizeFromAccountView(a);
	}

	public void brandIt(String brand) {
		this.brand = brand;
		Utils.brandText(vCreateAccountButton, brand);
		vEmailAddressPresenter.brandIt(brand);
		vPasswordPresenter.brandIt(brand);
	}

	public void configureAccountCreationString(String string) {
		vCreateAccountButton.setText(string);
	}

	public void configurePOS(boolean enableFacebookButton) {
		mEnableFacebookButton = enableFacebookButton;

		vSignInWithFacebookButton.setVisibility(
			mEnableFacebookButton
				? View.VISIBLE
				: View.INVISIBLE);
	}

	public void populate(String email, String password) {
		vEmailAddressPresenter.setText(email);
		vPasswordPresenter.setText(password);
		setVisibilities();
	}

	private boolean focusView(View view) {
		if (this.getVisibility() == VISIBLE) {
			view.requestFocus();
			return true;
		}
		else {
			return false;
		}
	}

	public void focusPassword() {
		// Make sure this runs lazily, after any pending transitions are done
		this.post(new Runnable() {
			@Override
			public void run() {
				focusView(vPasswordPresenter);
				Utils.showKeyboard(vPasswordPresenter, null);
			}
		});
	}

	@Override
	public void onKeyboardVisibilityChanged(boolean isKeyboardVisible) {
		setVisibilities();
	}

	public void setVisibilities() {
		Events.post(new Events.KeyBoardVisibilityChanged(isKeyboardVisible()));
		boolean isVisible = isKeyboardVisible();
		int spaceAvailableForKeyboard = vKeyboardHiddenFields.getHeight()
			- vForgotPassword.getHeight()
			+ vSpaceBottom.getHeight();

		vSpaceAboveButtons.setVisibility(isVisible ? View.GONE : View.VISIBLE);
		vKeyboardVisibleSpaceAboveButtons.setVisibility(isVisible ? View.VISIBLE : View.GONE);

		// This is a special case where there's enough room
		// at the bottom of the screen to accommodate the entire keyboard
		if (isVisible && spaceAvailableForKeyboard > getKeyboardHeight()) {
			vKeyboardHiddenFields.setVisibility(View.INVISIBLE);
			vKeyboardHiddenFields.setAlpha(0.2f);
			vForgotPassword.setVisibility(View.VISIBLE);
			setBottomMargin(-getKeyboardHeight());
		}
		else {
			vKeyboardHiddenFields.setAlpha(1f);
			vKeyboardHiddenFields.setVisibility(isVisible ? View.GONE : View.VISIBLE);
			vForgotPassword.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
			vSpaceBottom.setVisibility(isVisible ? View.GONE : View.VISIBLE);
			setBottomMargin(0);
		}
	}

	public void suppressCurrentErrors() {
		vEmailAddressPresenter.suppressIfEmpty();
		vPasswordPresenter.suppressIfEmpty();
	}

	private class GoogleAccountChangeListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			new AlertDialog.Builder(getContext())
				.setCancelable(false)
				.setNeutralButton("ok", null)
				.setMessage(Phrase.from(getContent(), R.string.acct__google_account_change_message_TEMPLATE)
					.put("brand", brand)
					.format())
				.show();
		}
	}

	public void enableButtons() {
		vSignInWithFacebookButton.setEnabled(true);
		vCreateAccountButton.setEnabled(true);
		if (vPasswordPresenter.isTextValid() && vEmailAddressPresenter.isTextValid()) {
			vSignInButton.setEnabled(true);
		}
		else {
			vSignInButton.setEnabled(false);
		}
	}
}
