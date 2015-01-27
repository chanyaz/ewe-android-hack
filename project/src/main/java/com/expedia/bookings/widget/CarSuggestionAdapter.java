package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.Suggestion;

public class CarSuggestionAdapter extends ArrayAdapter<Suggestion> {

	private List<Suggestion> mList;
	private LayoutInflater mInflater;


	public CarSuggestionAdapter(Context context, int resource) {
		super(context, resource);
		mInflater = LayoutInflater.from(context);
		mList = new ArrayList<>();
	}

	public void setSuggestionList(List<Suggestion> suggestionList) {
		mList = suggestionList;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Suggestion getItem(int position) {
		return mList.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Suggestion suggestion = getItem(position);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_suggestion_dropdown, parent, false);
		}
		convertView.setBackgroundColor(Color.BLACK);
		((TextView) convertView).setText(suggestion.airportCode);
		return convertView;
	}
}
