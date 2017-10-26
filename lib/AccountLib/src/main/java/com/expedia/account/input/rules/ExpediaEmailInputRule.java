package com.expedia.account.input.rules;

import com.expedia.account.input.InputRule;
import com.expedia.account.util.Utils;

//TODO: This could be a singleton, since it doesn't store any state whatsoever.
public class ExpediaEmailInputRule extends InputRule {
	@Override
	public int evaluateInput(String input) {
		if (Utils.isAtLeastBarelyPassableEmailAddress(input)) {
			//Bit of a funny combination of names here
			return DEFINITELY_GOOD;
		}
		else {
			return COULD_EVENTUALLY_BE_GOOD;
		}
	}
}