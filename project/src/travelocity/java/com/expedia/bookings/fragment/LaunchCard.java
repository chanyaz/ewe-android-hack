package com.expedia.bookings.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.LaunchDb;
import com.expedia.bookings.data.LaunchLocation;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LaunchCard extends FrameLayout {

	private static final int FLIP_ANIMATION_TIME = 300;
	private static LaunchCard currentToggledCard = null;

	@InjectView(R.id.launch_title_front_text_view)
	TextView frontTextView;
	@InjectView(R.id.launch_title_front_container)
	FrameLayout frontContainer;
	@InjectView(R.id.launch_title_back_container)
	FrameLayout backContainer;
	@InjectView(R.id.launch_title_front_image_view)
	ImageView frontImageView;
	@InjectView(R.id.launch_card_back_text_title)
	TextView backTextTitle;
	@InjectView(R.id.launch_card_back_text_description)
	TextView backTextDescription;
	@InjectView(R.id.button_explore_now)
	TextView backExploreNow;
	private LaunchLocation launchLocation;
	private int launchCardSize;
	private AnimatorSet animatorSet;

	public LaunchCard(Context context) {
		super(context);
	}

	public LaunchCard(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LaunchCard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		launchCardSize = getResources().getDimensionPixelSize(R.dimen.tablet_destination_tile_size);

		FontCache.setTypeface(frontTextView, FontCache.Font.ROBOTO_REGULAR);
		FontCache.setTypeface(backTextTitle, FontCache.Font.ROBOTO_REGULAR);
		FontCache.setTypeface(backTextDescription, FontCache.Font.ROBOTO_REGULAR);
	}

	public void bind(final LaunchLocation launchLocation) {
		if (launchLocation != null) {
			this.launchLocation = launchLocation;
			String cardText = "";
			if (Strings.isEmpty(launchLocation.subtitle)) {
				cardText = launchLocation.title;
			}
			else {
				cardText = String.format(getResources().getString(R.string.destination_list_launch_card_title_text),
					launchLocation.title, launchLocation.subtitle);
			}
			frontTextView.setText(cardText);
			backTextTitle.setText(cardText);

			backTextDescription.setText(launchLocation.description);

			final String imageUrl = TabletLaunchPinDetailFragment.getResizedImageUrl(getContext(), launchLocation);
			new PicassoHelper.Builder(frontImageView).fit().setPlaceholder(R.drawable.bg_launch_link_tile)
				.build().load(imageUrl);
		}
	}

	@OnClick(R.id.launch_title_front_container)
	public void onFrontContainerClicked() {
		if (null != currentToggledCard) {
			currentToggledCard.flipCard(false);
		}
		currentToggledCard = LaunchCard.this;
		flipCard(true);
	}

	@OnClick(R.id.launch_title_back_container)
	public void onBackContainerClicked() {
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
		frontView.setPivotY(launchCardSize / 2);
		backView.setPivotX(launchCardSize);
		backView.setPivotY(launchCardSize / 2);
		backView.setRotationY(-90);

		ObjectAnimator translateFrontAnimation = ObjectAnimator
			.ofFloat(frontView, "translationX", launchCardSize / 2);
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
			.ofFloat(backView, "translationX", -launchCardSize / 2, 0);
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
}
