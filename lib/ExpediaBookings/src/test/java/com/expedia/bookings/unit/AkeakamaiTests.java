package com.expedia.bookings.unit;

import org.junit.Test;

import com.expedia.bookings.utils.Akeakamai;

import static org.junit.Assert.assertEquals;

public class AkeakamaiTests {
	@Test
	public void testBuildBaseCase() {
		String result;
		result = new Akeakamai("").build();
		assertEquals("", result);
	}

	@Test
	public void testResize() {
		String result;

		result = new Akeakamai("") //
			.resize(new Akeakamai.Width(), new Akeakamai.Height()) //
			.build();

		assertEquals("?resize=w:h&", result);

		result = new Akeakamai("") //
			.resize(new Akeakamai.Preserve(), new Akeakamai.Preserve()) //
			.build();

		assertEquals("?resize=*:*&", result);

		result = new Akeakamai("") //
			.resize(Akeakamai.preserve(), Akeakamai.preserve()) //
			.build();

		assertEquals("?resize=*:*&", result);

		result = new Akeakamai("") //
			.resize(new Akeakamai.Fractional(2, 9, new Akeakamai.Width()), new Akeakamai.Preserve()) //
			.build();

		assertEquals("?resize=2/9xw:*&", result);
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

	@Test
	public void testResizeExactly() {
		String result = new Akeakamai("") //
			.resizeExactly(800, 600) //
			.build();

		assertEquals("?downsize=800px:*&crop=w:600/800xw;center,top&output-quality=60&output-format=jpeg&", result);
	}

	@Test
	public void testAlignment() {
		String result;
		result = new Akeakamai("") //
			.crop(
				Akeakamai.pixels(1),
				Akeakamai.pixels(2),
				new Akeakamai.Alignment(Akeakamai.Alignment.TOP),
				new Akeakamai.Alignment(Akeakamai.Alignment.BOTTOM)
			) //
			.build();

		assertEquals("?crop=1px:2px;top,bottom&", result);

		result = new Akeakamai("") //
			.crop(
				Akeakamai.pixels(1),
				Akeakamai.pixels(2),
				new Akeakamai.Alignment(Akeakamai.Alignment.LEFT),
				new Akeakamai.Alignment(Akeakamai.Alignment.RIGHT)
			) //
			.build();

		assertEquals("?crop=1px:2px;left,right&", result);

		result = new Akeakamai("") //
			.crop(
				Akeakamai.pixels(1),
				Akeakamai.pixels(2),
				new Akeakamai.Alignment(0),
				new Akeakamai.Alignment(0)
			) //
			.build();

		assertEquals("?crop=1px:2px;*,*&", result);

		result = new Akeakamai("") //
			.crop(
				Akeakamai.pixels(1),
				Akeakamai.pixels(2),
				new Akeakamai.Alignment(0, Akeakamai.pixels(3)),
				new Akeakamai.Alignment(0, null)
			) //
			.build();

		assertEquals("?crop=1px:2px;*[3px],*&", result);
	}
}
