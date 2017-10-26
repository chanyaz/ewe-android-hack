package com.expedia.account;

import org.junit.Test;

import com.expedia.account.util.Utils;

import static org.assertj.core.api.Assertions.*;

public class UtilsTest {
	@Test
	public void whatDoesPassableMean() {
		assertThat(passable(null)).isFalse();
		assertThat(passable("a")).isFalse();
		assertThat(passable("a@")).isFalse();
		assertThat(passable("@com")).isFalse();
		assertThat(passable("@a.com")).isFalse();

		assertThat(passable("a@a.c")).isTrue();
		assertThat(passable("a@a.com")).isTrue();
		assertThat(passable("leela@planextexpress.com")).isTrue();
	}

	private boolean passable(String input) {
		return Utils.isAtLeastBarelyPassableEmailAddress(input);
	}

	@Test
	public void getSomeInitials() {
		assertThat(Utils.generateInitials("Amy", null, null, null)).isEqualTo("A");
		assertThat(Utils.generateInitials(null, "Wong", null, null)).isEqualTo("W");
		assertThat(Utils.generateInitials("Amy", "Wong", null, null)).isEqualTo("AW");
		assertThat(Utils.generateInitials("Amy", "Wong", "Phillip J. Fry", "amy@planetexpress.net")).isEqualTo("AW");
		assertThat(Utils.generateInitials(null, null, "Amy Wong", "amy@planetexpress.net")).isEqualTo("AW");
		assertThat(Utils.generateInitials(null, null, "Phillip J. Fry", "amy@planetexpress.net")).isEqualTo("PF");
		assertThat(Utils.generateInitials(null, null, null, "amy@planetexpress.net")).isEqualTo("A");
	}
}
