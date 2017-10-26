package com.expedia.account.input.rules;

public class ExpediaPasswordSignInInputRule extends ExpediaPasswordInputRule {

	private static final int MINIMUM_PASSWORD_LENGTH_SIGNIN = 6;

	@Override
	protected int getMinimumPasswordLength() {
		return MINIMUM_PASSWORD_LENGTH_SIGNIN;
	}
}
