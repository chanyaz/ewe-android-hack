package com.expedia.bookings.data.urgency;

public class RegionTicker {
	final String displayName;
	final RegionLevelData regionLevelData;

	private class RegionLevelData {
		final CompressionSummary compressionSummary;
	}

	private class CompressionSummary {
		final int score;
		String status;
	}

	public int getScore() {
		return regionLevelData.compressionSummary.score;
	}

	public String getDisplayName() {
		return displayName;
	}
}
