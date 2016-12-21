package com.expedia.bookings.launch.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.launch.data.LaunchCollection;
import com.expedia.bookings.launch.data.LaunchDb;
import com.expedia.bookings.launch.data.LaunchLocation;
import com.expedia.bookings.fragment.base.Fragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.otto.TvlyEvents;
import com.expedia.bookings.launch.util.LaunchScreenAnimationUtil;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.DestinationCollection;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.HorizontalGridView;
import com.expedia.bookings.widget.OptimizedImageView;
import com.expedia.bookings.launch.widget.TabletLaunchDestinationListAdapter;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.otto.Subscribe;

public class TabletLaunchDestinationListFragment extends Fragment {

	private static final int L_SHAPE_ANIMATION_TIME = 300;
	private static final int POPIN_ANIMATION_TIME = 200;
	private static final int POPIN_ANIMATION_DELAY = 50;
	private static final int POP_IN_AND_OVERLAY_ANIMATION_DELAY = 1000;
	private View rootC;
	private OptimizedImageView launchLocationsBackgroundImageView;
	private OptimizedImageView launchLocationsBackgroundImageViewReflection;
	private TextView launchDestinationTitle;
	private TabletLaunchDestinationListAdapter destinationListAdapter;
	private HorizontalGridView launchListContainer;
	private AnimatorSet lShapeAnimatorSet;
	private AnimatorSet tilesPopInAnimatorSet;

	public static TabletLaunchDestinationListFragment newInstance() {
		TabletLaunchDestinationListFragment frag = new TabletLaunchDestinationListFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
		@Nullable Bundle savedInstanceState) {
		rootC = Ui.inflate(R.layout.fragment_tablet_launch_destination_list, container, false);

		launchLocationsBackgroundImageView = Ui.findView(rootC, R.id.launch_destination_background_image);
		launchLocationsBackgroundImageViewReflection = Ui.findView(rootC, R.id.image_view_reflection);
		launchDestinationTitle = Ui.findView(rootC, R.id.launch_destination_title);
		launchListContainer = Ui.findView(rootC, R.id.launch_destinations_list_container);

		destinationListAdapter = new TabletLaunchDestinationListAdapter(getParentFragment().getActivity());
		launchListContainer.setAdapter(destinationListAdapter);

		FontCache.setTypeface(launchDestinationTitle, FontCache.Font.ROBOTO_LIGHT);

		LaunchScreenAnimationUtil.applyColorToOverlay(getParentFragment().getActivity(),
			Ui.findView(rootC, R.id.destination_title_bg_overlay));
		updateViewsLayoutParams();
		return rootC;
	}

	private void updateViewsLayoutParams() {
		int screenWidth = AndroidUtils.getDisplaySize(getActivity()).x;
		int destinationTitleWidth = (int) (screenWidth / DestinationCollection.NO_OF_TILES_LANDSCAPE);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			destinationTitleWidth = (int) (screenWidth / DestinationCollection.NO_OF_TILES_PORTRAIT);
		}
		FrameLayout.LayoutParams destinationTitleLayoutParams = (FrameLayout.LayoutParams) launchDestinationTitle
			.getLayoutParams();
		destinationTitleLayoutParams.width = destinationTitleWidth;
		launchDestinationTitle.setLayoutParams(destinationTitleLayoutParams);

		updateReflectionImageLayoutParams();
	}

	private void updateReflectionImageLayoutParams() {
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			Point screenSize = AndroidUtils.getDisplaySize(getParentFragment().getActivity());
			int imageMarginBottom = getResources().getDimensionPixelSize(R.dimen.destination_image_margin_bottom);
			int actionBarNavBarHeight = LaunchScreenAnimationUtil
				.getActionBarNavBarSize(getParentFragment().getActivity());
			int imageHeight = screenSize.y - imageMarginBottom - actionBarNavBarHeight;
			FrameLayout.LayoutParams imageViewReflectionLayoutParams = (FrameLayout.LayoutParams) launchLocationsBackgroundImageViewReflection
				.getLayoutParams();
			imageViewReflectionLayoutParams.height = imageHeight;
			imageViewReflectionLayoutParams.topMargin = imageHeight;
			launchLocationsBackgroundImageViewReflection.setLayoutParams(imageViewReflectionLayoutParams);
			launchLocationsBackgroundImageViewReflection.setVisibility(View.VISIBLE);
		}
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

	@Subscribe
	public void onDestinationCollectionDrawableAvailable(
		TvlyEvents.DestinationCollectionDrawableAvailable drawableAvailable) {
		if (drawableAvailable.launchCollection == LaunchDb.getSelectedCollection()) {
			updateImageDrawable(drawableAvailable.launchCollection.imageDrawable);
		}
	}

	private void updateDestinationListView(LaunchCollection launchCollection) {
		if (launchCollection != null) {
			replaceAllPins(launchCollection.locations);
			LaunchScreenAnimationUtil
				.applyColorToOverlay(getParentFragment().getActivity(), Ui.findView(rootC, R.id.bg_overlay));
			updateImageDrawable(launchCollection.imageDrawable);
			launchDestinationTitle.setText(launchCollection.title);
		}
	}

	private void updateImageDrawable(Drawable drawable) {
		launchLocationsBackgroundImageView.setImageDrawable(drawable);
		launchLocationsBackgroundImageViewReflection.setImageDrawable(drawable);
	}

	private void replaceAllPins(List<LaunchLocation> locations) {
		destinationListAdapter.updateLocations(locations);
	}

	public void startTilesPopinAnimation() {
		setupInitialValues();

		final List<Animator> tilesPopInAnimatorList = new ArrayList<>();
		tilesPopInAnimatorSet = new AnimatorSet();
		Random rand = new Random();

		for (View destination : launchListContainer.getChildViews()) {
			destination.setAlpha(0);
			ObjectAnimator tileAnimator = ObjectAnimator.ofFloat(destination, "alpha", 1f);
			tileAnimator.setStartDelay(POPIN_ANIMATION_DELAY * rand.nextInt(7));
			tilesPopInAnimatorList.add(tileAnimator);
		}

		ObjectAnimator titleAnimator = ObjectAnimator.ofFloat(launchDestinationTitle, "translationY", 0);

		ObjectAnimator bgOverlayAnimator = ObjectAnimator.ofFloat(Ui.findView(rootC, R.id.destination_title_bg_overlay),
			"alpha", 0f, 1f);
		bgOverlayAnimator.setStartDelay(POP_IN_AND_OVERLAY_ANIMATION_DELAY);

		ObjectAnimator destinationTitleBgOverlayAnimator = ObjectAnimator.ofFloat(Ui.findView(rootC, R.id.bg_overlay),
			"alpha", 0f, 1f);
		destinationTitleBgOverlayAnimator.setStartDelay(POP_IN_AND_OVERLAY_ANIMATION_DELAY);

		lShapeAnimatorSet = new AnimatorSet();
		lShapeAnimatorSet.setDuration(L_SHAPE_ANIMATION_TIME);
		lShapeAnimatorSet.playTogether(titleAnimator, bgOverlayAnimator, destinationTitleBgOverlayAnimator);
		lShapeAnimatorSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				lShapeAnimatorSet = null;
			}
		});
		lShapeAnimatorSet.start();

		tilesPopInAnimatorSet.setDuration(POPIN_ANIMATION_TIME);
		tilesPopInAnimatorSet.setStartDelay(POP_IN_AND_OVERLAY_ANIMATION_DELAY);
		tilesPopInAnimatorSet.playTogether(tilesPopInAnimatorList);
		tilesPopInAnimatorSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				tilesPopInAnimatorSet = null;
			}
		});
		tilesPopInAnimatorSet.start();
	}

	private void setupInitialValues() {
		if (lShapeAnimatorSet != null && lShapeAnimatorSet.isRunning()) {
			lShapeAnimatorSet.end();
		}
		if (tilesPopInAnimatorSet != null && tilesPopInAnimatorSet.isRunning()) {
			tilesPopInAnimatorSet.end();
		}
		launchListContainer.setScrollY(0);
		launchListContainer.getChildAt(0).setScrollX(0);
		Ui.findView(rootC, R.id.destination_title_bg_overlay).setAlpha(0f);
		Ui.findView(rootC, R.id.bg_overlay).setAlpha(0f);

		int screenHeight = AndroidUtils.getDisplaySize(getActivity()).y;
		int actionBarNavBarSize = LaunchScreenAnimationUtil.getActionBarNavBarSize(getActivity());
		int destinationListTitleTextMarginTop = getResources()
			.getDimensionPixelSize(R.dimen.tablet_launch_destination_title_margin_top);
		int collectionTitleTextContainerHeight = getResources().getDimensionPixelSize(
			R.dimen.destination_text_container_height);
		int extraMarginBottom = LaunchScreenAnimationUtil.getMarginBottom(getActivity());
		int navigationBarHeight = LaunchScreenAnimationUtil.getNavigationBarHeight(getActivity());

		launchDestinationTitle.setTranslationY(
			screenHeight - actionBarNavBarSize - destinationListTitleTextMarginTop - collectionTitleTextContainerHeight
				- extraMarginBottom - navigationBarHeight);
	}
}
