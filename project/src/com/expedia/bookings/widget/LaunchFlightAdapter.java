package com.expedia.bookings.widget;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Destination;
import com.expedia.bookings.data.LaunchFlightData;
import com.expedia.bookings.utils.FontCache;
import com.mobiata.android.util.Ui;

public class LaunchFlightAdapter extends LaunchBaseAdapter<Destination> {

	private Context mContext;
	private LayoutInflater mInflater;
	private View[] mViewCache;

	public LaunchFlightAdapter(Context context) {
		super(context, R.layout.row_launch_tile_flight);
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Add enough blank items so that we can show blank tiles before loading
		int numTiles = getNumTiles();
		for (int a = 0; a < numTiles; a++) {
			add(null);
		}
		mViewCache = new View[numTiles];
	}

	public void setDestinations(LaunchFlightData launchFlightData) {
		this.clear();

		if (launchFlightData != null) {
			for (Destination destination : launchFlightData.getDestinations()) {
				add(destination);
			}

			mViewCache = new View[getViewCacheSize(launchFlightData.getDestinations().size())];
		}

		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		return AdapterView.ITEM_VIEW_TYPE_IGNORE;
	}

	@Override
	public View getView(int position, View unused, ViewGroup parent) {
		int cacheIndex = position % mViewCache.length;
		View view = mViewCache[cacheIndex];

		// Use the Tag as a flag to indicate this view has been populated
		if (view != null && view.getTag() != null) {
			return view;
		}

		// Inflate the view if possible
		if (view == null) {
			view = mInflater.inflate(R.layout.row_launch_tile_flight, parent, false);
			mViewCache[cacheIndex] = view;
		}

		final Destination destination = getItem(position);

		// If we're just measuring the height/width of the row, just return the view without doing anything to it.
		if (isMeasuring() || destination == null) {
			return view;
		}

		// Cache all ViewHolder views
		ViewHolder vh = new ViewHolder();
		vh.mContainer = Ui.findView(view, R.id.launch_tile_container);
		vh.mTitleTextView = Ui.findView(view, R.id.launch_tile_title_text_view);

		FontCache.setTypeface(vh.mTitleTextView, FontCache.Font.ROBOTO_LIGHT);

		vh.mTitleTextView.setText(Html.fromHtml(mContext.getString(R.string.launch_flight_tile_prompt,
				destination.getCityFormatted())));

		// Load the image
		loadImageForLaunchStream(destination.getImageUrl(), vh.mContainer);

		// We're just using the Tag as a flag to indicate this view has been populated
		view.setTag(vh);

		return view;
	}

	@Override
	public int getTileHeight() {
		return mContext.getResources().getDimensionPixelSize(R.dimen.launch_tile_height_flight);
	}

	private static class ViewHolder {
		public ViewGroup mContainer;
		public TextView mTitleTextView;
	}
}
