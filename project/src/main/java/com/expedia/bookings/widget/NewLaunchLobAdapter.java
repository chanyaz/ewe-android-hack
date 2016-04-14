package com.expedia.bookings.widget;

import java.util.List;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NewLaunchLobAdapter extends RecyclerView.Adapter<NewLaunchLobAdapter.LobViewHolder> {

	public interface OnLobClickListener {
		void onHotelsLobClick();
		void onFlightsLobClick();
		void onCarsLobClick();
		void onActivitiesLobClick();
		void onTransportLobClick();
	}

	private List<LobInfo> lobInfos;
	private OnLobClickListener listener;

	public NewLaunchLobAdapter(OnLobClickListener listener) {
		this.listener = listener;
	}

	@Override
	public LobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_lob_button, parent, false);
		return new LobViewHolder(view, listener);
	}

	@Override
	public void onBindViewHolder(LobViewHolder holder, int position) {
		holder.bind(lobInfos.get(position), getSpanSize(position) != 1);
	}

	@Override
	public int getItemCount() {
		return (lobInfos != null) ? lobInfos.size() : 0;
	}

	public void setLobs(List<LobInfo> lobs) {
		lobInfos = lobs;
		notifyDataSetChanged();
	}

	public int getSpanSize(int position) {
		int length = getItemCount();
		return (length > 0 && (position == length - 1) && (position % 2) == 0) ? 2 : 1;
	}

	public static class LobViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		@InjectView(R.id.lob_cell_text)
		TextView lobText;

		LobInfo lobInfo;
		OnLobClickListener listener;

		public LobViewHolder(View itemView, OnLobClickListener listener) {
			super(itemView);
			ButterKnife.inject(this, itemView);

			itemView.setOnClickListener(this);
			this.listener = listener;
		}

		public void bind(LobInfo info, boolean spansMultipleColumns) {
			lobInfo = info;

			lobText.setText(info.labelRes);
			lobText.setCompoundDrawablesWithIntrinsicBounds(info.iconRes, 0, 0, 0);

			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) lobText.getLayoutParams();
			if (lp != null) {
				if (spansMultipleColumns) {
					lp.gravity = Gravity.CENTER;
				}
				else {
					lp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
				}
			}
		}

		@Override
		public void onClick(View v) {
			if (lobInfo != null && listener != null) {
				switch (lobInfo.labelRes) {
				case R.string.nav_hotels:
					listener.onHotelsLobClick();
					break;
				case R.string.flights_title:
					listener.onFlightsLobClick();
					break;
				case R.string.nav_cars:
					listener.onCarsLobClick();
					break;
				case R.string.nav_lx:
					listener.onActivitiesLobClick();
					break;
				case R.string.nav_transport:
					listener.onTransportLobClick();
					break;
				}
			}
		}
	}

	public static class LobInfo {
		public @StringRes int labelRes;
		public @DrawableRes int iconRes;

		public LobInfo(@StringRes int label, @DrawableRes int icon) {
			labelRes = label;
			iconRes = icon;
		}
	}
}
