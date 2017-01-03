package com.expedia.bookings.launch.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.launch.data.LaunchDb;
import com.expedia.bookings.launch.data.LaunchLocation;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.launch.util.LaunchScreenAnimationUtil;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LaunchCard extends FrameLayout {

	private static final int FLIP_ANIMATION_TIME = 150;
	private static final int ROW_COLUMN_COUNT = 2;
	private static final float ASPECT_WIDTH = 550;
	private static final float ASPECT_HEIGHT = 685;

	private static LaunchCard currentToggledCard = null;
	private static AnimatorSet animatorSet;
	@InjectView(R.id.launch_title_front_text_view)
	TextView frontTextView;
	@InjectView(R.id.launch_title_country_front_text_view)
	TextView countryFrontTextView;
	@InjectView(R.id.launch_title_front_container)
	FrameLayout frontContainer;
	@InjectView(R.id.launch_title_back_container)
	FrameLayout backContainer;
	@InjectView(R.id.launch_title_front_image_view)
	ImageView frontImageView;
	@InjectView(R.id.launch_title_back_text_view)
	TextView backTextView;
	@InjectView(R.id.launch_title_country_back_text_view)
	TextView countryBackTextView;
	@InjectView(R.id.launch_card_back_text_description)
	TextView backTextDescription;
	private LaunchLocation launchLocation;
	private Point launchCardSize;
	private PicassoTargetCallback picassoTargetCallback;

	public LaunchCard(Context context) {
		super(context);
	}

	public LaunchCard(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	public static void clearHistory() {
		currentToggledCard = null;
	}

	public LaunchLocation getLaunchLocation() {
		return launchLocation;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		updateLayoutParams();

		FontCache.setTypeface(frontTextView, FontCache.Font.ROBOTO_REGULAR);
		FontCache.setTypeface(backTextView, FontCache.Font.ROBOTO_REGULAR);
		FontCache.setTypeface(backTextDescription, FontCache.Font.ROBOTO_REGULAR);
	}

	private void updateLayoutParams() {
		int launchCardWidth = -2;
		int launchCardHeight = -2;

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			int screenWidth = AndroidUtils.getDisplaySize(getContext()).x;
			int marginLeft = getResources().getDimensionPixelSize(R.dimen.destination_list_horizontal_grid_margin_left);
			float aspectRatio = ASPECT_HEIGHT / ASPECT_WIDTH;

			launchCardWidth = (screenWidth - marginLeft) / ROW_COLUMN_COUNT;
			launchCardHeight = (int) (launchCardWidth * aspectRatio);
		}
		else {
			int screenHeight = AndroidUtils.getDisplaySize(getContext()).y;
			int verticalMargin =
				getResources().getDimensionPixelSize(R.dimen.destination_list_horizontal_grid_margin_vertical) * 2;
			int marginTop = LaunchScreenAnimationUtil.getActionBarNavBarSize(getContext());
			int navigationBarHeight = LaunchScreenAnimationUtil.getNavigationBarHeight(getContext());
			float aspectRatio = ASPECT_WIDTH / ASPECT_HEIGHT;
			launchCardHeight = (screenHeight - verticalMargin - marginTop - navigationBarHeight) / ROW_COLUMN_COUNT;
			launchCardWidth = (int) (launchCardHeight * aspectRatio);
		}
		launchCardSize = new Point();
		launchCardSize.x = launchCardWidth;
		launchCardSize.y = launchCardHeight;
		ViewGroup.LayoutParams cardLayoutParams = getLayoutParams();
		cardLayoutParams.width = launchCardWidth;
		cardLayoutParams.height = launchCardHeight;
		setLayoutParams(cardLayoutParams);
	}

	public void bind(final LaunchLocation launchLocation) {
		if (launchLocation != null) {
			this.launchLocation = launchLocation;

			if (currentToggledCard != null
				&& currentToggledCard.getLaunchLocation().location == launchLocation.location) {
				frontContainer.setVisibility(GONE);
				backContainer.setVisibility(VISIBLE);
				currentToggledCard = this;
			}

			frontTextView.setText(launchLocation.title);
			backTextView.setText(launchLocation.title);

			if (!Strings.isEmpty(launchLocation.subtitle)) {
				countryFrontTextView.setText(launchLocation.subtitle);
				countryBackTextView.setText(launchLocation.subtitle);
			}

			backTextDescription.setText(launchLocation.description);

			int width = getResources().getDimensionPixelSize(R.dimen.launch_pin_detail_size);
			int height  = (int)(width * ASPECT_HEIGHT / ASPECT_WIDTH);
			final String imageUrl = new Akeakamai(launchLocation.getImageUrl()).downsize(Akeakamai.pixels(width),
				Akeakamai.pixels(height)).quality(75).build();
			picassoTargetCallback = new PicassoTargetCallback();
			new PicassoHelper.Builder(getContext()).setPlaceholder(R.drawable.bg_launch_card_placeholder_travelocity)
				.setTarget(picassoTargetCallback).build().load(imageUrl);
		}
	}

	@OnClick(R.id.launch_title_front_container)
	public void onFrontContainerClicked() {
		if (animatorSet != null && animatorSet.isRunning()) {
			return;
		}
		if (null != currentToggledCard) {
			currentToggledCard.flipCard(false);
		}
		currentToggledCard = LaunchCard.this;
		flipCard(true);
	}

	@OnClick(R.id.launch_title_back_container)
	public void onBackContainerClicked() {
		if (animatorSet != null && animatorSet.isRunning()) {
			return;
		}
		flipCard(true);
		currentToggledCard = null;
	}


	@OnClick(R.id.button_explore_now)
	public void onExploreNowButtonClicked() {
		boolean fromLastSearch = launchLocation.id.equals(LaunchDb.YOUR_SEARCH_TILE_ID);
		launchLocation.location.setImageCode(launchLocation.imageCode);
		Events.post(new Events.SearchSuggestionSelected(launchLocation.location, fromLastSearch));
	}

	private void flipCard(boolean animate) {
		if (animate) {
			flipCardWithAnimation();
		}
		else {
			flipCardWithOutAnimation();
		}
	}

	private void flipCardWithOutAnimation() {
		if (animatorSet != null && animatorSet.isRunning()) {
			animatorSet.cancel();
		}
		frontContainer.setVisibility(VISIBLE);
		backContainer.setVisibility(GONE);
	}

	private void flipCardWithAnimation() {
		final View frontView;
		final View backView;
		if (backContainer.getVisibility() == GONE) {
			frontView = frontContainer;
			backView = backContainer;
		}
		else {
			frontView = backContainer;
			backView = frontContainer;
		}
		frontView.setPivotX(0f);
		frontView.setPivotY(launchCardSize.y / 2);
		backView.setPivotX(launchCardSize.x);
		backView.setPivotY(launchCardSize.y / 2);
		backView.setRotationY(-90);

		ObjectAnimator translateFrontAnimation = ObjectAnimator
			.ofFloat(frontView, "translationX", launchCardSize.x / 2);
		ObjectAnimator flipFrontAnimation = ObjectAnimator.ofFloat(frontView, "rotationY", 90);
		flipFrontAnimation.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				frontView.setVisibility(GONE);
				frontView.setRotationY(0);
				frontView.setTranslationX(0);
				backView.setVisibility(VISIBLE);
			}
		});

		ObjectAnimator translateBackAnimation = ObjectAnimator
			.ofFloat(backView, "translationX", -launchCardSize.x / 2, 0);
		ObjectAnimator flipBackAnimation = ObjectAnimator.ofFloat(backView, "rotationY", 0);
		translateBackAnimation.setStartDelay(FLIP_ANIMATION_TIME);
		flipBackAnimation.setStartDelay(FLIP_ANIMATION_TIME);

		animatorSet = new AnimatorSet();
		animatorSet
			.playTogether(flipFrontAnimation, flipBackAnimation, translateFrontAnimation, translateBackAnimation);
		animatorSet.setDuration(FLIP_ANIMATION_TIME);
		animatorSet.start();
		animatorSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationCancel(Animator animation) {
				frontView.setRotationY(0);
				frontView.setTranslationX(0);
			}
		});
	}

	private class PicassoTargetCallback extends PicassoTarget {
		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			super.onBitmapLoaded(bitmap, from);
			frontImageView.setImageBitmap(bitmap);
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {
			super.onPrepareLoad(placeHolderDrawable);
			frontImageView.setImageDrawable(placeHolderDrawable);
		}
	}
}
