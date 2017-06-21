package com.expedia.bookings.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.expedia.bookings.R;

public class TravelerTagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return LayoutInflater.from(parent.getContext()).inflate(R.layout.traveler_tag_list, null);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

	}

	@Override
	public int getItemCount() {
		return 0;
	}
}
