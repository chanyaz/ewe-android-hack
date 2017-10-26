package com.expedia.account;

import org.junit.Before;
import org.junit.Test;

import com.expedia.account.input.ErrorableInputTextPresenter;
import com.expedia.account.input.InputRule;
import com.expedia.account.input.InputValidator;

import static org.assertj.core.api.Assertions.assertThat;

public class InputValidatorTest {

	private InputValidator validator;

	@Before
	public void reset() {
		validator = new InputValidator(new InputRule() {
			@Override
			public int evaluateInput(String input) {
				if ("good".equals(input)) {
					return DEFINITELY_GOOD;
				}
				else if ("terrible".equals(input)) {
					return IRREPARABLY_BAD;
				}
				else {
					return COULD_EVENTUALLY_BE_GOOD;
				}
			}
		});
	}

	public void setHasBeenLeftBefore(InputValidator validator) {
		validator.onFocusChanged("", false);
	}

	@Test
	public void testFirstManualEntry() {
		//If I am just typing things the first time...
		//And I've only just started, show progress
		assertThat(validator.onNewText("g")).isEqualTo(ErrorableInputTextPresenter.PROGRESS);
		//And the input is good, reward my success
		assertThat(validator.onNewText("good")).isEqualTo(ErrorableInputTextPresenter.GOOD);
		//Unless we know for sure that the input is bad
		assertThat(validator.onNewText("terrible")).isEqualTo(ErrorableInputTextPresenter.BAD);
	}

	@Test
	public void testLeaving() {
		//If I've typed things and wandered off...
		//And the input isn't good, then warn them
		assertThat(validator.onFocusChanged("g", false)).isEqualTo(ErrorableInputTextPresenter.BAD);
		//And the input is good, reward them
		assertThat(validator.onFocusChanged("good", false)).isEqualTo(ErrorableInputTextPresenter.GOOD);
		//And the input is definitely wrong, warn them
		assertThat(validator.onFocusChanged("terrible", false)).isEqualTo(ErrorableInputTextPresenter.BAD);
	}

	@Test
	public void testReturningAfterLeaving() {
		setHasBeenLeftBefore(validator);
		//If I'm coming back to the editor
		//And I'm not a good input, then give them a chance to fix it
		assertThat(validator.onFocusChanged("g", true)).isEqualTo(ErrorableInputTextPresenter.PROGRESS);
		assertThat(validator.onFocusChanged("terrible", true)).isEqualTo(ErrorableInputTextPresenter.BAD);
		//And it's good, then keep it good
		assertThat(validator.onFocusChanged("good", true)).isEqualTo(ErrorableInputTextPresenter.GOOD);
	}

	@Test
	public void testEditingAfterReturning() {
		setHasBeenLeftBefore(validator);
		//Even if I've been here before
		//Then check it just like I've never been here before
		testFirstManualEntry();
	}
}