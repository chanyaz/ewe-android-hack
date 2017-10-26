package com.expedia.account.input;

import android.support.annotation.NonNull;

public class LookupNameValidator extends InputValidator {

	public LookupNameValidator(@NonNull InputRule rule) {
		super(rule);
	}

	private boolean mHasNeverBeenEdited = true;
	private boolean mNonhumanUpdate = false;

	public void warnAboutNonhumanUpdate() {
		mNonhumanUpdate = true;
	}

	public boolean overridable() {
		return mHasNeverBeenEdited;
	}

	@Override
	public int onNewText(String input) {
		if (mNonhumanUpdate) {
			mNonhumanUpdate = false;
		} else {
			mHasNeverBeenEdited = false;
		}
		return super.onNewText(input);
	}
}
