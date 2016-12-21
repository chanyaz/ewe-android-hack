package com.expedia.bookings.launch.widget;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.launch.data.LaunchLocation;
import com.expedia.bookings.launch.fragment.LaunchCard;

public class TabletLaunchDestinationListAdapter extends BaseAdapter {

	private List<LaunchLocation> launchLocation;
	private final LayoutInflater inflater;

	public TabletLaunchDestinationListAdapter(Context context) {
		inflater = LayoutInflater.from(context);
	}

	public void updateLocations(List<LaunchLocation> launchLocation) {
		this.launchLocation = launchLocation;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return launchLocation.size();
	}

	@Override
	public LaunchLocation getItem(int position) {
		return launchLocation.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = inflater.inflate(R.layout.snippet_tablet_launch_tile, parent, false);

		LaunchCard tile = (LaunchCard) convertView;
		tile.bind(getItem(position));

		return convertView;
	}
}
