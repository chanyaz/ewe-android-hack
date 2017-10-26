package com.expedia.account.input.rules;

import com.expedia.account.input.InputRule;

public class ExpediaPasswordInputRule extends InputRule {

	public final static int MINIMUM_PASSWORD_LENGTH = 8;
	public final static int MAXIMUM_PASSWORD_LENGTH = 30;

	@Override
	public int evaluateInput(String input) {
		int length = input.length();
		if (length < getMinimumPasswordLength()) {
			return COULD_EVENTUALLY_BE_GOOD;
		}
		else if (length > MAXIMUM_PASSWORD_LENGTH) {
			return IRREPARABLY_BAD;
		}
		else {
			return DEFINITELY_GOOD;
		}
	}

	protected int getMinimumPasswordLength() {
		return MINIMUM_PASSWORD_LENGTH;
	}
}
