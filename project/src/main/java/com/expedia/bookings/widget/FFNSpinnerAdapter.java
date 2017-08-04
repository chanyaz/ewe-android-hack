package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.expedia.bookings.R;

public class FFNSpinnerAdapter extends ArrayAdapter<String> {
	private ArrayList<String> airlines;

	private String[] airlinePrograms;

	public FFNSpinnerAdapter(Context context) {
		this(context, R.layout.simple_spinner_item);
	}

	public FFNSpinnerAdapter(Context context, int textViewResourceId) {
		this(context, textViewResourceId, R.layout.simple_spinner_dropdown_item);
	}

	public FFNSpinnerAdapter(Context context, int textViewResId, int dropDownViewResId) {
		super(context, textViewResId);
		setDropDownViewResource(dropDownViewResId);
		init(context);
	}

	private void init(Context context) {
		final Resources res = context.getResources();
		airlinePrograms = res.getStringArray(R.array.ffn_programs);
		fillAirlines(context);
	}

	private void fillAirlines(Context context) {
		final Resources res = context.getResources();
		airlines.add("Airline A");
		airlines.add("Airline B");
		airlines.add("Airline C");
		airlines.add("Airline D");
		airlines.add("Airline E");
	}

	public String getAirlineName(int position) {
		return airlines.get(position);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View retView = super.getDropDownView(position, convertView, parent);
		//TODO: we should really probably set the formatting here
		return retView;
	}
}
