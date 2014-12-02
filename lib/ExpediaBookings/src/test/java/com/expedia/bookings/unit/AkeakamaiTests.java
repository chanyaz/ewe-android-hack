package com.expedia.bookings.unit;

import java.util.ArrayList;

import org.junit.Test;

import com.expedia.bookings.utils.Akeakamai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AkeakamaiTests {
	@Test
	public void testResize() {
		String result = new Akeakamai("") //
			.resize(new Akeakamai.Width(), new Akeakamai.Height()) //
			.build();

		assertEquals("?resize=w:h&", result);
	}

	@Test
	public void testDownsize() {
		String result = new Akeakamai("") //
			.downsize(new Akeakamai.Width(), new Akeakamai.Height()) //
			.build();

		assertEquals("?downsize=w:h&", result);
	}

	@Test
	public void testQuality() {
		String result = new Akeakamai("") //
			.quality(1) //
			.build();

		assertEquals("?output-quality=1&output-format=jpeg&", result);
	}

	@Test
	public void testCrop() {
		String result = new Akeakamai("") //
			.crop(
				Akeakamai.pixels(1),
				Akeakamai.pixels(2),
				Akeakamai.pixels(3),
				Akeakamai.pixels(4)
			) //
			.build();

		assertEquals("?crop=1px:2px;3px,4px&", result);
	}
}
