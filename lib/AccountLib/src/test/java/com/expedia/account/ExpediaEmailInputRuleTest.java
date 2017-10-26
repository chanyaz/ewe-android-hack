package com.expedia.account;

import org.junit.Before;
import org.junit.Test;

import com.expedia.account.input.InputRule;
import com.expedia.account.input.rules.ExpediaEmailInputRule;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpediaEmailInputRuleTest {

	private ExpediaEmailInputRule rule;

	@Before
	public void reset() {
		rule = new ExpediaEmailInputRule();
	}

	@Test
	public void testEmailRules() {
		//Make sure things that aren't emails all return the possibility of goodness
		assertThat(rule.evaluateInput("")).isEqualTo(InputRule.COULD_EVENTUALLY_BE_GOOD);
		assertThat(rule.evaluateInput("a")).isEqualTo(InputRule.COULD_EVENTUALLY_BE_GOOD);
		assertThat(rule.evaluateInput("thisisSuperLongButNotTEchincallygoodyet@emailwithoutadot"))
			.isEqualTo(InputRule.COULD_EVENTUALLY_BE_GOOD);

		//Make sure that actual emails return good
		assertThat(rule.evaluateInput("hello@example.com")).isEqualTo(InputRule.DEFINITELY_GOOD);
	}
}
