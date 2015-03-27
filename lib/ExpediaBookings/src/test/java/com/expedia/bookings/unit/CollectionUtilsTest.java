package com.expedia.bookings.unit;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.expedia.bookings.utils.CollectionUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CollectionUtilsTest {

	@Test
	public void testIsEmpty() {
		assertTrue(CollectionUtils.isEmpty(null));
		assertTrue(CollectionUtils.isEmpty(new ArrayList<Object>()));

		assertFalse(CollectionUtils.isEmpty(Arrays.asList("a")));
	}

	@Test
	public void testIsNotEmpty() {
		assertTrue(CollectionUtils.isNotEmpty(Arrays.asList("a")));

		assertFalse(CollectionUtils.isNotEmpty(null));
		assertFalse(CollectionUtils.isNotEmpty(new ArrayList<Object>()));

	}
}
