package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LaunchLocation;
import com.expedia.bookings.fragment.base.Fragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.util.LaunchScreenAnimationUtil;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.HorizontalGridView;
import com.expedia.bookings.widget.OptimizedImageView;
import com.expedia.bookings.widget.TabletLaunchDestinationListAdapter;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.otto.Subscribe;

public class TabletLaunchDestinationListFragment extends Fragment {

	private HashMap<LaunchLocation, LaunchCard> locations = new HashMap<>();

	private View rootC;
	private OptimizedImageView launchLocationsBackgroundImageView;
	private TextView launchDestinationTitle;
	private TabletLaunchDestinationListAdapter destinationListAdapter;

	public static TabletLaunchDestinationListFragment newInstance() {
		TabletLaunchDestinationListFragment frag = new TabletLaunchDestinationListFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
		@Nullable Bundle savedInstanceState) {
		rootC = Ui.inflate(R.layout.fragment_tablet_launch_destination_list, container, false);

		launchLocationsBackgroundImageView = Ui.findView(rootC, R.id.launch_destination_background_image);
		launchDestinationTitle = Ui.findView(rootC, R.id.launch_destination_title);
		HorizontalGridView launchListContainer = Ui.findView(rootC, R.id.launch_destinations_list_container);

		destinationListAdapter = new TabletLaunchDestinationListAdapter(getParentFragment().getActivity());
		launchListContainer.setAdapter(destinationListAdapter);

		FontCache.setTypeface(launchDestinationTitle, FontCache.Font.ROBOTO_LIGHT);

		LaunchScreenAnimationUtil.applyColorToOverlay(getParentFragment().getActivity(), Ui.findView(rootC, R.id.bg_overlay));
		centerAlignGridView(launchListContainer);

		return rootC;

	}

	private void centerAlignGridView(HorizontalGridView launchListContainer) {
		Point screenSize = AndroidUtils.getDisplaySize(getParentFragment().getActivity());

		ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) launchListContainer
			.getLayoutParams();
		final int marginTop = LaunchScreenAnimationUtil.getActionBarNavBarSize(getParentFragment().getActivity());
		final int marginBottom = LaunchScreenAnimationUtil.getMarginBottom(getParentFragment().getActivity());

		lp.topMargin = ((screenSize.y - marginTop - marginBottom) % getResources()
			.getDimensionPixelSize(R.dimen.tablet_destination_tile_size)) / 2;
		launchListContainer.setLayoutParams(lp);
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	private ArrayList<LaunchScreenAnimationUtil.PicassoTargetCallback> picassoTargetCallbacks = new ArrayList<LaunchScreenAnimationUtil.PicassoTargetCallback>();

	@Subscribe
	public void onLaunchCollectionClicked(final Events.LaunchCollectionClicked event) {
		if (event.launchCollection != null) {
			replaceAllPins(event.launchCollection.locations);
			launchDestinationTitle.setText(event.launchCollection.title);
			launchLocationsBackgroundImageView.setImageDrawable(
				LaunchScreenAnimationUtil
					.makeHeaderBitmapDrawable(getParentFragment().getActivity(), picassoTargetCallbacks, event.launchCollection.getImageUrl(),
						false));
		}
	}

	private void replaceAllPins(List<LaunchLocation> locations) {
		this.locations.clear();
		destinationListAdapter.updateLocations(locations);
	}
}
