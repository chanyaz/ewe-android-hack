package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.activity.ExpediaBookingApp;

// Class used to construct urls for the akamai image converter
public class Akeakamai {

	private String url;
	private List<String> commands = new ArrayList<>();

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

	public Akeakamai crop(Dimension w, Dimension h, Location x, Location y) {
		commands.add("crop=" + w.render() + ":" + h.render() + ";" + x.render() + "," + y.render());
		return this;
	}

	public Akeakamai resizeExactly(int w, int h) {
		this.resize(pixels(w), preserve());
		this.crop(pixels(w), pixels(h), pixels(0), pixels(0));
		return this;
	}

	public static Pixels pixels(int pixels) {
		return new Pixels(pixels);
	}

	public static Preserve preserve() {
		return new Preserve();
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

		private int alignment;
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
}
