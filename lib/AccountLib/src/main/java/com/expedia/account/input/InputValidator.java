package com.expedia.account.input;

import android.support.annotation.NonNull;

public class InputValidator {
	private InputRule mRule = null;

	public InputValidator(@NonNull InputRule rule) {
		mRule = rule;
	}

	private int basicCheck(String input) {
		switch (mRule.evaluateInput(input)) {
		case InputRule.COULD_EVENTUALLY_BE_GOOD:
			return BaseInputTextPresenter.PROGRESS;
		case InputRule.DEFINITELY_GOOD:
			return BaseInputTextPresenter.GOOD;
		case InputRule.IRREPARABLY_BAD:
			return BaseInputTextPresenter.BAD;
		default:
			return BaseInputTextPresenter.NO_CHANGE;
		}
	}

	public int onNewText(String input) {
		return basicCheck(input);
	}

	public int onFocusChanged(String currentText, boolean hasFocus) {
		int basicResult = basicCheck(currentText);
		if (!hasFocus && basicResult == BaseInputTextPresenter.PROGRESS) {
			return BaseInputTextPresenter.BAD;
		}
		else {
			return basicResult;
		}
	}
}