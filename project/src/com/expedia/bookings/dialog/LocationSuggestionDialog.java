package com.expedia.bookings.dialog;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;

import com.mobiata.hotellib.R;

public class LocationSuggestionDialog extends AlertDialog {
	private List<String> mLocations;
	
	public LocationSuggestionDialog(Context context, List<String> locations) {
		super(context);
		mLocations = locations;
		

		Builder builder = new Builder(context);
		builder.setTitle(R.string.ChooseLocation);
	}
}
