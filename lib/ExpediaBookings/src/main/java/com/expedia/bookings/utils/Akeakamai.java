package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;

// Class used to construct urls for the akamai image converter
public class Akeakamai {

	private final String url;
	private final List<String> commands = new ArrayList<>();

	public Akeakamai(String url) {
		this.url = url;
	}

	public String build() {
		StringBuilder sb = new StringBuilder();
		sb.append(url);
		if (commands.size() > 0) {
			sb.append("?");
		}
		for (String command : commands) {
			sb.append(command);
			sb.append("&");
		}

		return sb.toString();
	}

	public Akeakamai resize(Dimension w, Dimension h) {
		commands.add("resize=" + w.render() + ":" + h.render());
		return this;
	}

	public Akeakamai downsize(Dimension w, Dimension h) {
		commands.add("downsize=" + w.render() + ":" + h.render());
		return this;
	}

	public Akeakamai quality(int amount) {
		commands.add("output-quality=" + amount);
		// the quality command is only valid for jpegs
		commands.add("output-format=jpeg");
		return this;
	}

	public Akeakamai crop(Dimension w, Dimension h, Location x, Location y) {
		commands.add("crop=" + w.render() + ":" + h.render() + ";" + x.render() + "," + y.render());
		return this;
	}

	public Akeakamai resizeExactly(int w, int h) {
		this.downsize(pixels(w), preserve());
		this.crop(new Width(), new Fractional(h, w, new Width()), new Alignment(Alignment.CENTER), new Alignment(Alignment.TOP));
		this.quality(60);
		return this;
	}

	public static Pixels pixels(int pixels) {
		return new Pixels(pixels);
	}

	private static final Preserve mPreserve = new Preserve();
	public static Preserve preserve() {
		return mPreserve;
	}

	public static abstract class Location {
		public abstract String render();
	}

	public static class Alignment extends Location {
		public static final int CENTER = 1;
		public static final int LEFT = 2;
		public static final int RIGHT = 3;
		public static final int TOP = 4;
		public static final int BOTTOM = 5;

		private final int alignment;
		private Dimension dimension;

		public Alignment(int alignment) {
			this.alignment = alignment;
		}

		public Alignment(int alignment, Dimension dimension) {
			this.alignment = alignment;
			this.dimension = dimension;
		}

		@Override
		public String render() {
			String ret;
			switch (alignment) {
			case CENTER:
				ret = "center";
				break;
			case LEFT:
				ret = "left";
				break;
			case RIGHT:
				ret = "right";
				break;
			case TOP:
				ret = "top";
				break;
			case BOTTOM:
				ret = "bottom";
				break;
			default:
				ret = "*";
				break;
			}

			if (dimension != null) {
				ret += "[" + dimension.render() + "]";
			}

			return ret;
		}
	}

	public static abstract class Dimension extends Location {
	}

	public static class Pixels extends Dimension {
		private int amount;

		public Pixels(int amount) {
			this.amount = amount;
		}

		@Override
		public String render() {
			return "" + amount + "px";
		}
	}

	public static class Preserve extends Dimension {
		@Override
		public String render() {
			return "*";
		}
	}

	public static class Width extends Dimension {
		@Override
		public String render() {
			return "w";
		}
	}

	public static class Height extends Dimension {
		@Override
		public String render() {
			return "h";
		}
	}

	public static class Fractional extends Dimension {
		private int numerator;
		private int denominator;
		private Dimension dimension;

		public Fractional(int numerator, int denominator, Dimension dimension) {
			this.numerator = numerator;
			this.denominator = denominator;
			this.dimension = dimension;
		}

		@Override
		public String render() {
			return "" + numerator + "/" + denominator + "x" + dimension.render();
		}
	}
}
