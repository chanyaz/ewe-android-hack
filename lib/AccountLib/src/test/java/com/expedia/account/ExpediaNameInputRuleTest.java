package com.expedia.account;

import org.junit.Before;
import org.junit.Test;

import com.expedia.account.input.InputRule;
import com.expedia.account.input.rules.ExpediaNameInputRule;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpediaNameInputRuleTest {

	private ExpediaNameInputRule rule;

	@Before
	public void reset() {
		rule = new ExpediaNameInputRule();
	}

	@Test
	public void testNameRules() {
		//Names are simple
		assertThat(rule.evaluateInput("")).isEqualTo(InputRule.COULD_EVENTUALLY_BE_GOOD);
		assertThat(rule.evaluateInput("a")).isEqualTo(InputRule.DEFINITELY_GOOD);
		assertThat(rule.evaluateInput("asdinaidwhwandoawndoasdonawo a odawoi dawdawd ")).isEqualTo(InputRule.DEFINITELY_GOOD);
	}
}
