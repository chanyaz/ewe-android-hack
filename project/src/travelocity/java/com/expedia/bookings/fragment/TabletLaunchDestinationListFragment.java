package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LaunchCollection;
import com.expedia.bookings.data.LaunchLocation;
import com.expedia.bookings.fragment.base.Fragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.util.LaunchScreenAnimationUtil;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.DestinationCollection;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.HorizontalGridView;
import com.expedia.bookings.widget.OptimizedImageView;
import com.expedia.bookings.widget.TabletLaunchDestinationListAdapter;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.otto.Subscribe;

public class TabletLaunchDestinationListFragment extends Fragment {

	private static final int TILE_ANIMATION_TIME = 300;
	private static final int POPIN_ANIMATION_TIME = 200;
	private static final int POPIN_ANIMATION_DELAY = 50;
	private View rootC;
	private OptimizedImageView launchLocationsBackgroundImageView;
	private TextView launchDestinationTitle;
	private TabletLaunchDestinationListAdapter destinationListAdapter;
	private HorizontalGridView launchListContainer;

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
		launchListContainer = Ui.findView(rootC, R.id.launch_destinations_list_container);

		destinationListAdapter = new TabletLaunchDestinationListAdapter(getParentFragment().getActivity());
		launchListContainer.setAdapter(destinationListAdapter);

		FontCache.setTypeface(launchDestinationTitle, FontCache.Font.ROBOTO_LIGHT);

		LaunchScreenAnimationUtil
			.applyColorToOverlay(getParentFragment().getActivity(), Ui.findView(rootC, R.id.bg_overlay),
				Ui.findView(rootC, R.id.destination_title_bg_overlay));
		updateTitleLayoutParams();
		return rootC;
	}

	private void updateTitleLayoutParams() {
		int screenWidth = AndroidUtils.getDisplaySize(getActivity()).x;
		int destinationTitleWidth = (int) (screenWidth / DestinationCollection.NO_OF_TILES_LANDSCAPE);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			destinationTitleWidth = (int) (screenWidth / DestinationCollection.NO_OF_TILES_PORTRAIT);
		}
		FrameLayout.LayoutParams destinationTitleLayoutParams = (android.widget.FrameLayout.LayoutParams) launchDestinationTitle
			.getLayoutParams();
		destinationTitleLayoutParams.width = destinationTitleWidth;
		launchDestinationTitle.setLayoutParams(destinationTitleLayoutParams);
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

	@Subscribe
	public void onLaunchCollectionsAvailable(Events.LaunchCollectionsAvailable event) {
		updateDestinationListView(event.selectedCollection);
	}

	@Subscribe
	public void onLaunchCollectionClicked(final Events.LaunchCollectionClicked event) {
		updateDestinationListView(event.launchCollection);
		startTilesPopinAnimation();
	}

	private void updateDestinationListView(LaunchCollection launchCollection) {
		if (launchCollection != null) {
			replaceAllPins(launchCollection.locations);
			launchLocationsBackgroundImageView.setImageDrawable(launchCollection.imageDrawable);
			launchDestinationTitle.setText(launchCollection.title);
		}
	}

	private void replaceAllPins(List<LaunchLocation> locations) {
		destinationListAdapter.updateLocations(locations);
	}

	public void startTilesPopinAnimation() {
		launchListContainer.setAlpha(0);

		int screenHeight = AndroidUtils.getDisplaySize(getActivity()).y;
		int actionBarNavBarSize = LaunchScreenAnimationUtil.getActionBarNavBarSize(getActivity());
		int destinationListTitleTextMarginTop = getResources()
			.getDimensionPixelSize(R.dimen.tablet_launch_destination_title_margin_top);
		int collectionTitleTextContainerHeight = getResources().getDimensionPixelSize(
			R.dimen.destination_text_container_height);
		int extraMarginBottom = LaunchScreenAnimationUtil.getMarginBottom(getActivity());

		launchDestinationTitle
			.setTranslationY(screenHeight - actionBarNavBarSize - destinationListTitleTextMarginTop
				- collectionTitleTextContainerHeight
				- extraMarginBottom);

		final List<Animator> tilesPopInAnimatorList = new ArrayList<>();
		final AnimatorSet animatorSet = new AnimatorSet();
		Random rand = new Random();

		for (View destination : launchListContainer.getChildViews()) {
			destination.setAlpha(0);
			ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(destination, "alpha", 1f)
				.setDuration(POPIN_ANIMATION_TIME);
			objectAnimator.setStartDelay(POPIN_ANIMATION_DELAY * rand.nextInt(7));
			tilesPopInAnimatorList.add(objectAnimator);
		}

		animatorSet.playTogether(tilesPopInAnimatorList);
		launchListContainer.setAlpha(1f);
		launchListContainer.setScrollX(0);
		animatorSet.start();
		ObjectAnimator.ofFloat(launchDestinationTitle, "translationY", 0)
			.setDuration(TILE_ANIMATION_TIME).start();
	}
}
