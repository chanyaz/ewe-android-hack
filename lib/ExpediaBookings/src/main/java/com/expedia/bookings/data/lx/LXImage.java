package com.expedia.bookings.data.lx;

public class LXImage {

	public final String imageURL;
	public final ImageSize imageSize;

	public LXImage(String imageURL, ImageSize imageSize) {
		this.imageURL = imageURL;
		this.imageSize = imageSize;
	}

	public enum ImageSize {
		SMALL(350, 197),
		MEDIUM(500, 281),
		LARGE(1000, 561);

		public final int width;
		public final int height;

		ImageSize(int width, int height) {
			this.width = width;
			this.height = height;
		}
	}
}
