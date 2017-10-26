package com.expedia.account.input.rules;

import com.expedia.account.input.InputRule;

public class ExpediaNameInputRule extends InputRule {
	@Override
	public int evaluateInput(String input) {
		if (input.length() > 0) {
			return DEFINITELY_GOOD;
		}
		else {
			return COULD_EVENTUALLY_BE_GOOD;
		}
	}
}
