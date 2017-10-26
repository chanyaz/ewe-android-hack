package com.expedia.account.input;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class LookupNameValidatorTest {

	LookupNameValidator mValidator;

	@Before
	public void setUp() throws Exception {
		mValidator = new LookupNameValidator(new InputRule() {
			@Override
			public int evaluateInput(String input) {
				//The input is irrelevant for testing this, so just always say good.
				return InputRule.DEFINITELY_GOOD;
			}
		});
	}

	@Test
	public void testMultipleInhumanUpdates() {
		//We should be able to override it at the start
		assertThat(mValidator.overridable()).isTrue();
		mValidator.warnAboutNonhumanUpdate();
		mValidator.onNewText("anything");
		//Or after the lookup renames it
		assertThat(mValidator.overridable()).isTrue();
		mValidator.warnAboutNonhumanUpdate();
		mValidator.onNewText("something");
		//Or after it does again!
		assertThat(mValidator.overridable()).isTrue();
		mValidator.onNewText("something");
		//But not once a human touches it, even if it's the same text
		assertThat(mValidator.overridable()).isFalse();
	}

	@Test
	public void testFocusNotMessingWithEditing() {
		// Focus shouldn't mess with whether or not it has been edited
		assertThat(mValidator.overridable()).isTrue();
		mValidator.onFocusChanged("anything", true);
		assertThat(mValidator.overridable()).isTrue();
		mValidator.onFocusChanged("something", false);
		assertThat(mValidator.overridable()).isTrue();
	}
}