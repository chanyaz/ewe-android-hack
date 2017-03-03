package com.expedia.bookings.data.urgency;

public class RegionTicker {
	String displayName;
	RegionLevelData regionLevelData;

	private class RegionLevelData {
		CompressionSummary compressionSummary;
	}

	private class CompressionSummary {
		int score;
		String status;
	}

	public int getScore() {
		return regionLevelData.compressionSummary.score;
	}

	public String getDisplayName() {
		return displayName;
	}
}
