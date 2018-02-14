package com.expedia.bookings.data.foursquare;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by nbirla on 16/02/18.
 */

public class Items {

	@SerializedName("prefix")
	@Expose
	private String prefix;
	@SerializedName("suffix")
	@Expose
	private String suffix;

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

}
