package com.expedia.account.sample;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.account.AccountView;
import com.expedia.account.util.Events;
import com.expedia.account.util.FacebookViewHelper;
import com.expedia.account.util.MockFacebookViewHelper;
import com.squareup.otto.Subscribe;

public class MockAccountView extends AccountView {

	private boolean mCreateCalled = false;
	private boolean mIsMockModeEnabled = false;

	public MockAccountView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public FacebookViewHelper createFacebookViewHelper() {
		mCreateCalled = true;
		return mIsMockModeEnabled
			? new MockFacebookViewHelper(this)
			: new FacebookViewHelper(this);
	}

	public void setMockMode(boolean enabled) {
		if (mCreateCalled) {
			throw new RuntimeException("setFacebookMockMode() should be called before setupConfig().");
		}
		mIsMockModeEnabled = enabled;
	}

	///////////////////////////////////////////////////////////////////////////
	// Otto workaround
	///////////////////////////////////////////////////////////////////////////

	@Subscribe
	@Override
	public void otto(Events.SignInButtonClicked e) {
		super.otto(e);
	}

	@Subscribe
	@Override
	public void otto(Events.SignInWithFacebookButtonClicked e) {
		super.otto(e);
	}

	@Subscribe
	@Override
	public void otto(Events.ForgotPasswordButtonClicked e) {
		super.otto(e);
	}

	@Subscribe
	@Override
	public void otto(Events.CreateAccountButtonClicked e) {
		super.otto(e);
	}

	@Subscribe
	@Override
	public void otto(Events.NextFromPasswordFired e) {
		super.otto(e);
	}

	@Subscribe
	@Override
	public void otto(Events.NextFromLastNameFired e) {
		super.otto(e);
	}

	@Subscribe
	public void otto(Events.LinkFromFacebookFired e) {
		super.otto(e);
	}

	@Subscribe
	public void otto(Events.KeyBoardVisibilityChanged e) {
		super.otto(e);
	}

	@Subscribe
	@Override
	public void otto(Events.TOSContinueButtonClicked e) {
		super.otto(e);
	}

	@Subscribe
	@Override
	public void otto(Events.ObscureBackgroundDesired e) {
		super.otto(e);
	}

	@Subscribe
	@Override
	public void otto(Events.OverallProgress e) {
		super.otto(e);
	}

	@Subscribe
	@Override
	public void otto(Events.UserChangedSpamOptin e) {
		super.otto(e);
	}
}
