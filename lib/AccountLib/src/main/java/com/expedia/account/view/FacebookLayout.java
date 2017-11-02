package com.expedia.account.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Html;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.expedia.account.R;
import com.expedia.account.data.Db;
import com.expedia.account.data.PartialUser;
import com.expedia.account.input.ErrorableInputTextPresenter;
import com.expedia.account.input.rules.ExpediaEmailInputRule;
import com.expedia.account.input.rules.ExpediaPasswordInputRule;
import com.expedia.account.util.CombiningFakeObservable;
import com.expedia.account.util.Events;
import com.expedia.account.util.Utils;

import io.reactivex.Observer;

public class FacebookLayout extends KeyboardObservingFrameLayout {

	private TextView vMessage;
	private ErrorableInputTextPresenter vEmailAddress;
	private ErrorableInputTextPresenter vPassword;
	private View vSpace;

	private String mBrand;

	private CombiningFakeObservable mValidationObservable;

	public void setLinkButtonController(Observer<Boolean> nextController) {
		mValidationObservable.subscribe(nextController);
	}

	public FacebookLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.acct__widget_facebook, this);

		vMessage = (TextView) findViewById(R.id.facebook_result_message);
		vEmailAddress = (ErrorableInputTextPresenter) findViewById(R.id.email_address_facebook);
		vPassword = (ErrorableInputTextPresenter) findViewById(R.id.password_facebook);
		vSpace = findViewById(R.id.facebook_bottom_space);

		mValidationObservable = new CombiningFakeObservable();
		mValidationObservable.addSource(vEmailAddress.getStatusObservable());
		mValidationObservable.addSource(vPassword.getStatusObservable());
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		vEmailAddress.setValidator(new ExpediaEmailInputRule());
		vPassword.setValidator(new ExpediaPasswordInputRule());

		vPassword.setOnEditorActionListener(
			new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE
						|| actionId == EditorInfo.IME_ACTION_NEXT) {
						if (everythingChecksOut()) {
							Events.post(new Events.LinkFromFacebookFired());
						}
						return true;
					}
					return false;
				}
			});
	}

	public void styleizeFromAccountView(TypedArray a) {
		vEmailAddress.styleizeFromAccountView(a);
		vPassword.styleizeFromAccountView(a);
	}

	public void brandIt(String brand) {
		vEmailAddress.brandIt(brand);
		vPassword.brandIt(brand);
		mBrand = brand;
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

	public void setupNotLinked() {
		CharSequence formatted = Utils.obtainBrandedPhrase(getContext(),
			R.string.acct__fb_enter_your_brand_credentials, mBrand)
			.format();
		vMessage.setText(formatted);
		PartialUser user = Db.getNewUser();
		user.email = "";
		vEmailAddress.setText(user.email);
		vEmailAddress.setEnabled(true);
		vEmailAddress.setVisibility(View.VISIBLE);
		vEmailAddress.requestFocus(true);
		vPassword.setVisibility(View.VISIBLE);
	}

	public void setupExisting() {
		PartialUser user = Db.getNewUser();
		String formatted = Utils.obtainBrandedPhrase(getContext(),
			R.string.acct__fb_weve_found_your_account, mBrand)
			.put("email_address", user.email)
			.format()
			.toString();
		vMessage.setText(Html.fromHtml(formatted));
		vEmailAddress.setText(user.email);
		vEmailAddress.setEnabled(false);
		vEmailAddress.setVisibility(View.VISIBLE);
		vPassword.setVisibility(View.VISIBLE);
		vPassword.requestFocus(true);
	}

	public boolean everythingChecksOut() {
		return Utils.isAtLeastBarelyPassableEmailAddress(vEmailAddress.getText())
			&& Utils.passwordIsValidForAccountCreation(vPassword.getText());
	}

	/////
	// Data Storage
	/////

	public void storeDataInNewUser() {
		Db.getNewUser().email = vEmailAddress.getText();
		Db.getNewUser().password = vPassword.getText();
		Events.post(new Events.PartialUserDataChanged());
	}


}
