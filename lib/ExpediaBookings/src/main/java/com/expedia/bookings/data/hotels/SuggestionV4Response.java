package com.expedia.bookings.data.hotels;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class SuggestionV4Response {

	@SerializedName("q")
	public String query;
	@SerializedName("rid")
	public String requestId;
	@SerializedName("sr")
	public List<SuggestionV4> suggestions;

}
