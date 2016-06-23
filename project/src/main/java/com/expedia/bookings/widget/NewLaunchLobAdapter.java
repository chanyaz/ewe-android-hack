package com.expedia.bookings.widget;

import java.util.List;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NewLaunchLobAdapter extends RecyclerView.Adapter<NewLaunchLobAdapter.LobViewHolder> {


	public NewLaunchLobAdapter(OnLobClickListener listener) {
		this.listener = listener;
	}

	public interface OnLobClickListener {
		void onHotelsLobClick(View view);

		void onFlightsLobClick();

		void onCarsLobClick();

		void onActivitiesLobClick();

		void onTransportLobClick();

		void onPackagesLobClick();

		void onRailLobClick();
	}

	private Boolean enableLobs = true;
	private List<LobInfo> lobInfos;
	private OnLobClickListener listener;

	@Override
	public LobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_lob_button, parent, false);
		return new LobViewHolder(view, listener);
	}

	@Override
	public void onBindViewHolder(LobViewHolder holder, int position) {
		holder.bind(lobInfos.get(position), getSpanSize(position) != 1, holder.lobText.getContext(), enableLobs);
	}

	@Override
	public int getItemCount() {
		return (lobInfos != null) ? lobInfos.size() : 0;
	}

	public void enableLobs(boolean enable) {
		enableLobs = enable;
		notifyDataSetChanged();
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
		boolean isLobEnabled;
		OnLobClickListener listener;

		public LobViewHolder(View itemView, OnLobClickListener listener) {
			super(itemView);
			ButterKnife.inject(this, itemView);

			itemView.setOnClickListener(this);
			this.listener = listener;
		}

		public void bind(LobInfo info, boolean spansMultipleColumns, Context context, boolean lobEnabled) {
			lobInfo = info;
			isLobEnabled = lobEnabled;
			lobText.setEnabled(lobEnabled);
			lobText.setText(info.labelRes);
			Drawable lobDrawable = ContextCompat.getDrawable(context, lobInfo.iconRes).mutate();
			if (isLobEnabled) {
				lobDrawable.setColorFilter(ContextCompat.getColor(context, lobInfo.colorRes), PorterDuff.Mode.SRC_IN);
				lobText.setAlpha(1f);
			}
			else {
				lobDrawable
					.setColorFilter(ContextCompat.getColor(context, lobInfo.disabledColorRes), PorterDuff.Mode.SRC_IN);
				lobText.setAlpha(0.25f);
			}
			lobText.setCompoundDrawablesWithIntrinsicBounds(lobDrawable, null, null, null);


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
		public void onClick(View view) {
			if (lobInfo != null && listener != null && isLobEnabled) {
				switch (lobInfo) {
				case HOTELS:
					listener.onHotelsLobClick(view);
					break;
				case FLIGHTS:
					listener.onFlightsLobClick();
					break;
				case CARS:
					listener.onCarsLobClick();
					break;
				case ACTIVITIES:
					listener.onActivitiesLobClick();
					break;
				case TRANSPORT:
					listener.onTransportLobClick();
					break;
				case PACKAGES:
					listener.onPackagesLobClick();
					break;
				case RAIL:
					listener.onRailLobClick();
					break;
				}
			}
		}
	}

	public enum LobInfo {
		HOTELS(R.string.nav_hotels, R.drawable.ic_lob_hotels, R.color.new_launch_hotels_lob_color),
		FLIGHTS(R.string.flights_title, R.drawable.ic_lob_flights, R.color.new_launch_flights_lob_color),
		CARS(R.string.nav_car_rentals, R.drawable.ic_lob_cars, R.color.new_launch_cars_lob_color),
		ACTIVITIES(R.string.nav_things_to_do, R.drawable.ic_lob_lx, R.color.new_launch_lx_lob_color),
		TRANSPORT(R.string.nav_transport, R.drawable.ic_lob_gt, R.color.new_launch_gt_lob_color),
		PACKAGES(R.string.nav_packages, R.drawable.ic_lob_packages, R.color.new_launch_packages_lob_color),
		RAIL(R.string.nav_rail, R.drawable.ic_lob_rail, R.color.new_launch_rail_lob_color);


		public
		@StringRes
		int labelRes;
		public
		@DrawableRes
		int iconRes;
		@ColorRes
		int colorRes;
		@ColorRes
		public static int disabledColorRes = R.color.new_launch_lob_disabled_color;

		LobInfo(@StringRes int label, @DrawableRes int icon, @ColorRes int color) {
			labelRes = label;
			iconRes = icon;
			colorRes = color;
		}
	}
}
