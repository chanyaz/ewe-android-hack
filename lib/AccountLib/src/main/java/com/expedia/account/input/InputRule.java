package com.expedia.account.input;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import android.support.annotation.IntDef;

public abstract class InputRule {

	@IntDef({ COULD_EVENTUALLY_BE_GOOD, DEFINITELY_GOOD, IRREPARABLY_BAD })
	@Retention(RetentionPolicy.SOURCE)
	@interface PasswordRuleResult {
	}

	public static final int COULD_EVENTUALLY_BE_GOOD = 0;
	public static final int DEFINITELY_GOOD = 1;
	public static final int IRREPARABLY_BAD = 2;

	@PasswordRuleResult
	public abstract int evaluateInput(String input);

	public boolean isGood(String input) {
		return evaluateInput(input) == DEFINITELY_GOOD;
	}
}
