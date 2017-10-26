package com.expedia.account;

import org.junit.Test;

import com.expedia.account.input.InputRule;
import com.expedia.account.input.rules.ExpediaPasswordInputRule;
import com.expedia.account.input.rules.ExpediaPasswordSignInInputRule;

import static org.assertj.core.api.Assertions.*;

public class ExpediaPasswordInputRuleTest {
	@Test
	public void testPasswords() {
		ExpediaPasswordInputRule rule = new ExpediaPasswordInputRule();
		//"Good"
		assertThat(rule.isGood("asdasdas")).isTrue();
		assertThat(rule.isGood("LongerWithChars#$%_")).isTrue();
		assertThat(rule.isGood("ThisPassIsThirtyCharactersLong")).isTrue();
		assertThat(rule.isGood("1230$%^][abcABC")).isTrue();

		//"Bad"
		assertThat(rule.isGood("hi")).isFalse();
		assertThat(rule.isGood("shortpw")).isFalse();
		assertThat(rule.isGood("holycrapthispasswordissolongIcantevenreallytypeitallinareasonableamountoftime")).isFalse();
		assertThat(rule.isGood(("ThisPassIsThirty1CharactersLong"))).isFalse();

		//Potentially good
		assertThat(rule.evaluateInput("")).isEqualTo(InputRule.COULD_EVENTUALLY_BE_GOOD);
		assertThat(rule.evaluateInput("short")).isEqualTo(InputRule.COULD_EVENTUALLY_BE_GOOD);
	}

	@Test
	public void testPasswordsSignIn() {
		ExpediaPasswordSignInInputRule rule = new ExpediaPasswordSignInInputRule();
		//"Good"
		assertThat(rule.isGood("asdasd")).isTrue();
		assertThat(rule.isGood("LongerWithChars#$%_")).isTrue();
		assertThat(rule.isGood("ThisPassIsThirtyCharactersLong")).isTrue();
		assertThat(rule.isGood("1230$%^][abcABC")).isTrue();

		//"Bad"
		assertThat(rule.isGood("hi")).isFalse();
		assertThat(rule.isGood("short")).isFalse();
		assertThat(rule.isGood("holycrapthispasswordissolongIcantevenreallytypeitallinareasonableamountoftime")).isFalse();
		assertThat(rule.isGood(("ThisPassIsThirty1CharactersLong"))).isFalse();

		//Potentially good
		assertThat(rule.evaluateInput("")).isEqualTo(InputRule.COULD_EVENTUALLY_BE_GOOD);
		assertThat(rule.evaluateInput("short")).isEqualTo(InputRule.COULD_EVENTUALLY_BE_GOOD);
	}
}
