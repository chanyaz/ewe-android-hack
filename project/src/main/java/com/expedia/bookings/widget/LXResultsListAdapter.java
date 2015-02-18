package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Images;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class LXResultsListAdapter extends RecyclerView.Adapter<LXResultsListAdapter.ViewHolder> {

	List<LXActivity> activities = new ArrayList<>();
	private static final String ROW_PICASSO_TAG = "lx_row";

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_lx_search_row, parent, false);
		return new ViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		LXActivity activity = activities.get(position);
		holder.bind(activity);

		String url = Images.getLXImageURL(activity.imageUrl);
		new PicassoHelper.Builder(holder.activityImage)
			.setTag(ROW_PICASSO_TAG)
			.fit()
			.build()
			.load(url);
	}

	@Override
	public int getItemCount() {
		return activities.size();
	}

	public void setActivities(List<LXActivity> activities) {
		this.activities = activities;
		notifyDataSetChanged();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		public ViewHolder(View itemView) {
			super(itemView);
			ButterKnife.inject(this, itemView);
			itemView.setOnClickListener(this);
		}

		@InjectView(R.id.activity_title)
		TextView activityTitle;

		@InjectView(R.id.activity_image)
		ImageView activityImage;

		@Override
		public void onClick(View v) {
			LXActivity activity = (LXActivity)v.getTag();
			Events.post(new Events.LXActivitySelected(activity));
		}

		public void bind(LXActivity activity) {
			itemView.setTag(activity);
			activityTitle.setText(activity.title);
		}
	}
}
