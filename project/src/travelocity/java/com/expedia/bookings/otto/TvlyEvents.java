package com.expedia.bookings.otto;

import com.expedia.bookings.data.LaunchCollection;

public class TvlyEvents {

	public static class DestinationCollectionDrawableAvailable {
		public LaunchCollection launchCollection;

		public DestinationCollectionDrawableAvailable(LaunchCollection launchCollection) {
			this.launchCollection = launchCollection;
		}
	}
}
