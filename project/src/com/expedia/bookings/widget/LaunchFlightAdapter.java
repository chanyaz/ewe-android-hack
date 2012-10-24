package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.expedia.bookings.R;
import com.expedia.bookings.data.*;
import com.expedia.bookings.utils.FontCache;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.List;

public class LaunchFlightAdapter extends CircularArrayAdapter<Location> implements OnMeasureListener {

	private static final int TYPE_EMPTY = 0;
	private static final int TYPE_LOADED = 1;
	private static final int NUM_ROW_TYPES = 2;

	private static final int DURATION_FADE_MS = 700;

	private Context mContext;

	LayoutInflater mInflater;

	private boolean mIsMeasuring = false;

	public LaunchFlightAdapter(Context context) {
		super(context, R.layout.row_launch_tile_flight);
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setLocations(List<Location> locations) {
		this.clear();

		for (Location location : locations) {
			add(location);
		}

		notifyDataSetChanged();
	}

	@Override
	public int getViewTypeCount() {
		return NUM_ROW_TYPES;
	}

	@Override
	public int getItemViewType(int position) {
		Location location = getItem(position);

		if (location == null) {
			return TYPE_EMPTY;
		}
		else {
			return TYPE_LOADED;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		TileHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_launch_tile_flight, parent, false);

			holder = new TileHolder();

			holder.container = Ui.findView(convertView, R.id.launch_tile_container);
			holder.titleTextView = Ui.findView(convertView, R.id.launch_tile_title_text_view);
			FontCache.setTypeface(holder.titleTextView, FontCache.Font.ROBOTO_LIGHT);

			convertView.setTag(holder);
		}
		else {
			holder = (TileHolder) convertView.getTag();
		}

		Location location = getItem(position);

		// If we're just measuring the height/width of the row, just return the view without doing anything to it.
		if (mIsMeasuring || location == null) {
			return convertView;
		}

		holder.titleTextView.setText(Html.fromHtml(mContext.getString(R.string.launch_flight_tile_prompt,
				location.getCity())));

		// Note: This just loads a bitmap from APK. TODO: load dynamically
		setTileBackgroundBitmap(position, holder.container);

		return convertView;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Private methods and stuff

	private void setTileBackgroundBitmap(int position, RelativeLayout layout) {
		Bitmap bg;
		if (position % 2 == 0) {
			bg = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.launch_lhr);
		}
		else {
			bg = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.launch_jfk);
		}
		layout.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), bg));

		// Alpha animate the tile to fade in
		layout.setVisibility(View.VISIBLE);
		ObjectAnimator.ofFloat(layout, "alpha", 0.0f, 1.0f).setDuration(DURATION_FADE_MS).start();
	}

	private class TileHolder {
		public RelativeLayout container;
		public TextView titleTextView;
	}

	public static List<Location> getHardcodedDestinations() {
		List<Location> locations = new ArrayList<Location>();

		locations.add(new Location("SFO", "San Francisco", "SFO - San Francisco International Airport"));
		locations.add(new Location("JFK", "New York", "JFK - John F. Kennedy"));
		locations.add(new Location("PDX", "Las Vegas", "LAS - McCarran Airport"));

		return locations;
	}

	//////////////////////////////////////////////////////////////////////////
	// OnMeasureListener

	@Override
	public void onStartMeasure() {
		mIsMeasuring = true;
	}

	@Override
	public void onStopMeasure() {
		mIsMeasuring = false;
	}

}
