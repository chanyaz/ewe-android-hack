package com.expedia.bookings.data.cars;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class SuggestionResponse {
	@SerializedName("q")
	public String query;
	@SerializedName("rid")
	public String requestId;
	@SerializedName("sr")
	public List<Suggestion> suggestions;
}
