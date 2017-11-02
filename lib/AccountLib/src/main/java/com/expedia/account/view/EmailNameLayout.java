package com.expedia.account.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.expedia.account.R;
import com.expedia.account.data.Db;
import com.expedia.account.data.PartialUser;
import com.expedia.account.input.ErrorableInputTextPresenter;
import com.expedia.account.input.InputValidator;
import com.expedia.account.input.LookupNameValidator;
import com.expedia.account.input.rules.ExpediaEmailInputRule;
import com.expedia.account.input.rules.ExpediaNameInputRule;
import com.expedia.account.presenter.Presenter;
import com.expedia.account.util.CombiningFakeObservable;
import com.expedia.account.util.Events;
import com.expedia.account.util.InvalidCharacterTextWatcher;
import com.expedia.account.util.Utils;
import com.squareup.otto.Subscribe;

import io.reactivex.Observer;

public class EmailNameLayout extends KeyboardObservingFrameLayout {

	private CombiningFakeObservable mValidationObservable;

	public void setNextButtonController(Observer<Boolean> nextController) {
		mValidationObservable.subscribe(nextController);
	}

	private View vSpace;
	private ErrorableInputTextPresenter vEmailAddress;
	private ErrorableInputTextPresenter vFirstNameInput;
	private ErrorableInputTextPresenter vLastNameInput;
	private LookupNameValidator mFirstNameValidator = new LookupNameValidator(new ExpediaNameInputRule());
	private LookupNameValidator mLastNameValidator = new LookupNameValidator(new ExpediaNameInputRule());

	public EmailNameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.acct__widget_email_name, this);

		vSpace = findViewById(R.id.email_name_bottom_space);
		vEmailAddress = (ErrorableInputTextPresenter) findViewById(R.id.email_address_create_account);
		vFirstNameInput = (ErrorableInputTextPresenter) findViewById(R.id.first_name);
		vLastNameInput = (ErrorableInputTextPresenter) findViewById(R.id.last_name);

		mValidationObservable = new CombiningFakeObservable();
		mValidationObservable.addSource(vEmailAddress.getStatusObservable());
		mValidationObservable.addSource(vFirstNameInput.getStatusObservable());
		mValidationObservable.addSource(vLastNameInput.getStatusObservable());
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		vEmailAddress.setValidator(new InputValidator(new ExpediaEmailInputRule()));

		vFirstNameInput.setValidator(mFirstNameValidator);
		vLastNameInput.setValidator(mLastNameValidator);

		vFirstNameInput.addTextChangedListener(new InvalidCharacterTextWatcher(null));
		vLastNameInput.addTextChangedListener(new InvalidCharacterTextWatcher(null));

		vLastNameInput.setOnEditorActionListener(
			new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE
						|| actionId == EditorInfo.IME_ACTION_NEXT) {
						if (everythingChecksOut()) {
							Events.post(new Events.NextFromLastNameFired());
						}
						return true;
					}
					return false;
				}
			});
	}

	public void styleizeFromAccountView(TypedArray a) {
		vEmailAddress.styleizeFromAccountView(a);
		vFirstNameInput.styleizeFromAccountView(a);
		vLastNameInput.styleizeFromAccountView(a);
	}

	public void brandIt(String brand) {
		vEmailAddress.brandIt(brand);
		vFirstNameInput.brandIt(brand);
		vLastNameInput.brandIt(brand);
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

	public void focusEmailAddress() {
		// Make sure this runs lazily, after any pending transitions are done
		this.post(new Runnable() {
			@Override
			public void run() {
				focusView(vEmailAddress);
				Utils.showKeyboard(vEmailAddress, null);
			}
		});
	}

	public void focusFirstName() {
		// Make sure this runs lazily, after any pending transitions are done
		this.post(new Runnable() {
			@Override
			public void run() {
				focusView(vFirstNameInput);
			}
		});
	}

	public void focusLastName() {
		// Make sure this runs lazily, after any pending transitions are done
		this.post(new Runnable() {
			@Override
			public void run() {
				focusView(vLastNameInput);
			}
		});
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	public boolean everythingChecksOut() {
		return vFirstNameInput.getText().length() > 0 &&
			vLastNameInput.getText().length() > 0 &&
			Utils.isAtLeastBarelyPassableEmailAddress(vEmailAddress.getText());
	}

	///////////////////////////////////////////////////////////////////////////
	// Transitions
	///////////////////////////////////////////////////////////////////////////

	public Presenter.Transition getEmailToLookupTransition() {
		return mEmailToLookupTransition;
	}

	Presenter.Transition mEmailToLookupTransition = new Presenter.Transition(null, null) {
		@Override
		public void startTransition(boolean forward) {
			super.startTransition(forward);
			if (forward) {
				storeDataInNewUser();
				vEmailAddress.setVisibility(View.INVISIBLE);
			}
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			super.updateTransition(f, forward);
			setAlpha(forward ? 1f - f : f);
		}

		@Override
		public void finalizeTransition(boolean forward) {
			super.finalizeTransition(forward);
			if (!forward) {
				vEmailAddress.setVisibility(View.VISIBLE);
			}
		}
	};

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

	///////////////////////////////////////////////////////////////////////////
	// Otto
	///////////////////////////////////////////////////////////////////////////

	@Subscribe
	public void otto(Events.PartialUserDataChanged e) {
		PartialUser user = Db.getNewUser();

		if (!vEmailAddress.getText().equals(user.email)) {
			vEmailAddress.setText(user.email);
		}
		if (!vFirstNameInput.getText().equals(user.firstName)) {
			vFirstNameInput.setText(user.firstName);
			vFirstNameInput.moveCursorToEnd();

		}
		if (!vLastNameInput.getText().equals(user.lastName)) {
			vLastNameInput.setText(user.lastName);
			vLastNameInput.moveCursorToEnd();
		}
	}

	/////
	// Data Storage
	/////
	public void storeDataInNewUser() {
		Db.getNewUser().email = vEmailAddress.getText();
		Db.getNewUser().firstName = vFirstNameInput.getText();
		Db.getNewUser().lastName = vLastNameInput.getText();
		Events.post(new Events.PartialUserDataChanged());
	}

	public void requestFocus(boolean forward) {
		if (forward) {
			vEmailAddress.requestFocus();
		}
		else {
			vLastNameInput.requestFocus(forward);
		}
	}

}
