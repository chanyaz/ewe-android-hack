package com.expedia.bookings.data.lx;

public class LXImage {

	public String imageURL;
	public ImageSize imageSize;

	public LXImage(String imageURL, ImageSize imageSize) {
		this.imageURL = imageURL;
		this.imageSize = imageSize;
	}

	public enum ImageSize {
		SMALL(350, 197),
		MEDIUM(500, 281),
		LARGE(1000, 561);

		public int width;
		public int height;

		ImageSize(int width, int height) {
			this.width = width;
			this.height = height;
		}
	}
}
