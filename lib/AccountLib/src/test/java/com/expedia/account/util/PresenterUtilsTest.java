package com.expedia.account.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class PresenterUtilsTest {

	@Test
	public void testCalculateStep() throws Exception {
		assertThat(PresenterUtils.calculateStep(0, 10, .5f)).isEqualTo(5);
		assertThat(PresenterUtils.calculateStep(3, 3, .75f)).isEqualTo(3);
		assertThat(PresenterUtils.calculateStep(-30, 20, 0f)).isEqualTo(-30);
		assertThat(PresenterUtils.calculateStep(56, -78, 1f)).isEqualTo(-78);
		assertThat(PresenterUtils.calculateStep(10, -10, .75f)).isEqualTo(-5);
	}
}