package com.expedia.account.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.account.R;
import com.expedia.account.animation.ResizeAnimator;
import com.expedia.account.data.Db;
import com.expedia.account.data.PartialUser;
import com.expedia.account.presenter.CenterHorizontalInParentTransition;
import com.expedia.account.presenter.CompoundTransition;
import com.expedia.account.presenter.FadeTransition;
import com.expedia.account.presenter.FlipTransition;
import com.expedia.account.presenter.LoadingAnimationTriggerTransition;
import com.expedia.account.presenter.LoadingScalingTransition;
import com.expedia.account.presenter.OffScreenBottomTransition;
import com.expedia.account.presenter.Presenter.DefaultTransition;
import com.expedia.account.presenter.Presenter.Transition;
import com.expedia.account.presenter.ScaleTransition;
import com.expedia.account.presenter.ScaleTransitionImageSwap;
import com.expedia.account.presenter.TrackBottomTransition;
import com.expedia.account.util.Events;
import com.expedia.account.util.PresenterUtils;
import com.expedia.account.util.Utils;
import com.squareup.otto.Subscribe;

public class HeaderLayout extends RelativeLayout {

	private static final float SCALE_ORIGINAL = 1f;
	private static final float SCALE_SMALL = 1.4f;
	private static final float SCALE_LARGE = 2f;
	private static boolean isSignInMessagingTestEnabled = false;
	private static final float[] FADE_IN = new float[] {0, 1};
	private static final float[] FADE_OUT = new float[] {1, 0};

	private View vLogoFaceContainer;
	private TextView vEmail;
	private TextView vGreeting;
	private AvatarView vUserImage;
	private View vUserInfoContainer;
	private ImageView vLogoImage;
	private ImageView vLogoText;
	private TextView vMessagingText;
	int vMessagingTextHeight = 0;
	int marginFromLogo = 0;

	private Drawable mSmallLogo;
	private Drawable mLargeLogo;

	private Transition logoToSmall;
	private Transition smallToLarge;
	private Transition largeToLoadingAccount;
	private Transition loadingAccountToDoneLoading;
	private Transition logoToLoadingSignIn;
	private Transition loadingSignInToDoneLoading;
	private Transition loadingFacebookToDoneLoading;
	private Transition loadingSinglePageToDoneLoading;
	private Transition largeToLogo;

	public DefaultTransition getDefaultTransition() {
		return logo;
	}

	public Transition getLargeToLoadingTransition() {
		return largeToLoadingAccount;
	}

	public Transition getLoadingSignInToDoneLoadingTransition() {
		return loadingSignInToDoneLoading;
	}

	public Transition getLoadingAccountToDoneLoadingTransition() {
		return loadingAccountToDoneLoading;
	}

	public Transition getLoadingSinglePageToDoneLoadingTransition() {
		return loadingSinglePageToDoneLoading;
	}

	public Transition getLoadingFacebookToDoneLoadingTransition() {
		return loadingFacebookToDoneLoading;
	}

	public Transition getLogoToSmallTransition() {
		return logoToSmall;
	}

	public Transition getLogoToHiddenTransition() {
		return logoToHidden;
	}

	public Transition getLogoToLoadingSignInTransition() {
		return logoToLoadingSignIn;
	}

	public Transition getSmallToLargeTransition() {
		return smallToLarge;
	}

	// Presenter states
	private static final String STATE_LOGO = "STATE_LOGO";

	private static final String STATE_SMALL = "STATE_SMALL";

	private static final String STATE_LARGE = "STATE_LARGE";

	private static final String STATE_LOADING_ACCOUNT = "STATE_LOADING_ACCOUNT";

	private static final String STATE_LOADING_SIGN_IN = "STATE_LOADING_SIGN_IN";

	private static final String STATE_LOADING_FACEBOOK = "STATE_LOADING_FACEBOOK";

	private static final String STATE_DONE_LOADING = "STATE_DONE_LOADING";

	public HeaderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.acct__widget_user_image, this);

		vLogoFaceContainer = findViewById(R.id.logo_face_container);
		vLogoImage = (ImageView) findViewById(R.id.logo_image);
		vLogoText = (ImageView) findViewById(R.id.logo_text);
		vMessagingText = (TextView) findViewById(R.id.special_messaging);
		vEmail = (TextView) findViewById(R.id.user_email);
		vGreeting = (TextView) findViewById(R.id.user_greeting);
		vUserImage = (AvatarView) findViewById(R.id.user_image);
		vUserInfoContainer = findViewById(R.id.user_info_container);
		marginFromLogo = getContext().getResources().getDimensionPixelOffset(R.dimen.acct__logo_text_vertical_margin);
		createTransitions();
	}

	public void showSpecialMessage(boolean visible) {
		if (isSignInMessagingTestEnabled) {
			if (visible) {
				ValueAnimator titleResizeAnimator = ResizeAnimator
					.buildResizeAnimator(vMessagingText, 0, vMessagingTextHeight);
				titleResizeAnimator.start();
				Animator fadeIn = ObjectAnimator.ofFloat(vMessagingText, "alpha", FADE_IN);
				fadeIn.start();
				vMessagingText.setVisibility(View.VISIBLE);
			}
			else {
				if (vMessagingTextHeight < marginFromLogo) {
					vMessagingTextHeight = vMessagingText.getHeight() + marginFromLogo;
				}
				Animator fadeOut = ObjectAnimator.ofFloat(vMessagingText, "alpha", FADE_OUT);
				fadeOut.start();
				vMessagingText.setVisibility(View.INVISIBLE);
				ValueAnimator titleResizeAnimator = ResizeAnimator
					.buildResizeAnimator(vMessagingText, vMessagingText.getHeight(), 0);
				titleResizeAnimator.start();
			}
		}
	}

	public void configurePOS(boolean enableSignInMessaging, CharSequence signInMessage) {
		vMessagingText.setText(signInMessage);
		vMessagingText.setVisibility(enableSignInMessaging ? VISIBLE : View.GONE);
		isSignInMessagingTestEnabled = enableSignInMessaging;
	}

	public void styleizeFromAccountView(TypedArray a) {
		int userInfoTextColor = a.getColor(R.styleable.acct__AccountView_acct__user_info_text_color,
			getResources().getColor(R.color.acct__default_user_info_text_color));
		vGreeting.setTextColor(userInfoTextColor);
		vEmail.setTextColor(userInfoTextColor);
		vMessagingText.setTextColor(userInfoTextColor);

		if (a.hasValue(R.styleable.acct__AccountView_acct__logo_text_drawable_margin_left)
			|| a.hasValue(R.styleable.acct__AccountView_acct__logo_text_drawable_margin_top)) {

			MarginLayoutParams lp = (MarginLayoutParams) vLogoText.getLayoutParams();
			lp.leftMargin = a.getDimensionPixelOffset(
				R.styleable.acct__AccountView_acct__logo_text_drawable_margin_left, 0);
			lp.topMargin = a.getDimensionPixelOffset(
				R.styleable.acct__AccountView_acct__logo_text_drawable_margin_top, 0);
			vLogoText.setLayoutParams(lp);
		}
		vLogoText.setImageDrawable(a.getDrawable(R.styleable.acct__AccountView_acct__logo_text_drawable));
		vLogoText.setContentDescription(a.getString(R.styleable.acct__AccountView_acct__brand));

		mSmallLogo = a.getDrawable(R.styleable.acct__AccountView_acct__logo_small_drawable);
		mLargeLogo = a.getDrawable(R.styleable.acct__AccountView_acct__logo_large_drawable);

		vLogoImage.setImageDrawable(mSmallLogo);

		createTransitions();
	}

	public void brandIt(String brand) {
	}

	// Since this relies on branded things, it must be called after styleizeFromAccountView
	// (or called at the end of it, which it is right now)
	private void createTransitions() {
		View vCenterForThoseThings = findViewById(R.id.container_for_centering_those_two_bastards);

		logoToLoadingSignIn = new CompoundTransition(
			STATE_LOGO,
			STATE_LOADING_SIGN_IN,
			new ScaleTransitionImageSwap(vLogoImage, mSmallLogo, mLargeLogo, SCALE_ORIGINAL, SCALE_LARGE),
			new LoadingScalingTransition(vLogoImage, true, true, false, mLoadingAnimationController, null),
			new OffScreenBottomTransition(vLogoText),
			new CenterHorizontalInParentTransition(vLogoFaceContainer, vCenterForThoseThings)
		);

		logoToSmall = new CompoundTransition(
			STATE_LOGO,
			STATE_SMALL,
			new FlipTransition(vLogoImage, vUserImage, 1),
			new ScaleTransition(vLogoFaceContainer, SCALE_ORIGINAL, SCALE_SMALL),
			new CenterHorizontalInParentTransition(vLogoText, vCenterForThoseThings),
			new CenterHorizontalInParentTransition(vUserInfoContainer, this),
			new CenterHorizontalInParentTransition(vLogoFaceContainer, vCenterForThoseThings),
			new Transition() {

				private float mTextStartTranslationY;
				private float mTextEndTranslationY;

				@Override
				public void startTransition(boolean forward) {
					updateUserGreeting();
					vLogoText.setVisibility(VISIBLE);
					vLogoText.setAlpha(1.0f);
					vUserInfoContainer.setVisibility(VISIBLE);
					vUserInfoContainer.setAlpha(0);
					vGreeting.setText(getContext().getString(R.string.acct__Hello_x, Db.getNewUser().firstName));
					vEmail.setText(Db.getNewUser().email);
					if (forward) {
						mTextStartTranslationY = 0;
						mTextEndTranslationY = vLogoFaceContainer.getHeight() * SCALE_SMALL;
					}
					else {
						mTextStartTranslationY = vLogoText.getTranslationY();
						mTextEndTranslationY = 0;
					}
				}

				@Override
				public void updateTransition(float f, boolean forward) {
					if (forward) {
						vLogoText.setAlpha(PresenterUtils.calculateStep(1, 0, f));
						vUserInfoContainer.setAlpha(PresenterUtils.calculateStep(0, 1, f));
					}
					else {
						vLogoText.setAlpha(PresenterUtils.calculateStep(0, 1, f));
						vUserInfoContainer.setAlpha(PresenterUtils.calculateStep(1, 0, f));
					}

					float step = PresenterUtils.calculateStep(mTextStartTranslationY, mTextEndTranslationY, f);
					vLogoText
						.setTranslationY(step);
					vUserInfoContainer.setTranslationY(step);
				}

				@Override
				public void finalizeTransition(boolean forward) {
					vLogoText.setVisibility(forward ? INVISIBLE : VISIBLE);
					vUserInfoContainer.setVisibility(forward ? VISIBLE : INVISIBLE);
					vLogoText.setAlpha(1.0f);
					vUserInfoContainer.setAlpha(1);
					vLogoText.setTranslationY(mTextEndTranslationY);
					vUserInfoContainer.setTranslationY(mTextEndTranslationY);
				}
			});

		smallToLarge = new CompoundTransition(
			STATE_SMALL,
			STATE_LARGE,
			new ScaleTransition(vLogoFaceContainer, SCALE_SMALL, SCALE_LARGE),
			new TrackBottomTransition(vLogoFaceContainer, vUserInfoContainer)
		);

		largeToLogo = new CompoundTransition(STATE_LARGE, STATE_LOGO,
			new FlipTransition(vUserImage, vLogoImage, -1),
			new FadeTransition(vUserInfoContainer, vLogoText),
			new ScaleTransition(vLogoFaceContainer, SCALE_LARGE, SCALE_ORIGINAL),
			new Transition() {

				private float mLogoTextStartTranslationX;
				private float mLogoFaceStartTranslationX;
				private float mUserInfoStartTranslationX;
				private float mLogoTextStartTranslationY;
				private float mGoalTranslation = 0;

				@Override
				public void startTransition(boolean forward) {
					super.startTransition(forward);
					mLogoFaceStartTranslationX = vLogoFaceContainer.getTranslationX();
					mLogoTextStartTranslationX = vLogoText.getTranslationX();
					mLogoTextStartTranslationY = vLogoText.getTranslationY();
					mUserInfoStartTranslationX = vUserInfoContainer.getTranslationX();
				}

				@Override
				public void updateTransition(float f, boolean forward) {
					super.updateTransition(f, forward);
					vLogoFaceContainer.setTranslationX(
						PresenterUtils.calculateStep(mLogoFaceStartTranslationX, mGoalTranslation, f));
					vLogoText
						.setTranslationX(PresenterUtils.calculateStep(mLogoTextStartTranslationX, mGoalTranslation, f));
					vLogoText.setTranslationY(
						PresenterUtils.calculateStep(mLogoTextStartTranslationY, mGoalTranslation, f));
					vUserInfoContainer
						.setTranslationX(PresenterUtils.calculateStep(mUserInfoStartTranslationX, mGoalTranslation, f));
				}

				@Override
				public void finalizeTransition(boolean forward) {
					super.finalizeTransition(forward);
					vLogoFaceContainer.setTranslationX(mGoalTranslation);
					vLogoText.setTranslationX(mGoalTranslation);
					vLogoText.setTranslationY(mGoalTranslation);
					vUserInfoContainer.setTranslationX(mGoalTranslation);
				}
			});

		largeToLoadingAccount = new CompoundTransition(
			STATE_LARGE,
			STATE_LOADING_ACCOUNT,
			new FlipTransition(vUserImage, vLogoImage, 1),
			new LoadingScalingTransition(vLogoImage, true, true, false, mLoadingAnimationController, null),
			new OffScreenBottomTransition(vUserInfoContainer)
		);

		loadingSignInToDoneLoading = new CompoundTransition(
			STATE_LOADING_SIGN_IN,
			STATE_DONE_LOADING,
			new LoadingScalingTransition(vLogoImage, false, true, false, mLoadingAnimationController, SCALE_LARGE)
		);

		loadingAccountToDoneLoading = new CompoundTransition(
			STATE_LOADING_ACCOUNT,
			STATE_DONE_LOADING,
			new LoadingScalingTransition(vLogoImage, false, true, false, mLoadingAnimationController, SCALE_ORIGINAL)
		);

		loadingSinglePageToDoneLoading = new CompoundTransition(
			STATE_LOADING_ACCOUNT,
			STATE_DONE_LOADING,
			new LoadingScalingTransition(vLogoImage, false, true, false, mLoadingAnimationController, SCALE_LARGE)
		);

		loadingFacebookToDoneLoading = new CompoundTransition(
			STATE_LOADING_FACEBOOK,
			STATE_DONE_LOADING,
			new LoadingScalingTransition(vLogoImage, false, true, false, mLoadingAnimationController, SCALE_LARGE)
		);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				recenter();
			}
		});
	}

	private View vAnchor;
	private float mMinHeight;
	private float mTrans;

	/**
	 * Ties this view to the passed view, so that when the anchor view moves,
	 * this one will move too.
	 *
	 * @param v
	 * @param minHeight Minimum height allowed for the view before fading it out
	 */
	public void anchorTo(View v, float minHeight) {
		vAnchor = v;
		mTrans = Float.NaN;
		mMinHeight = minHeight;
		recenter();
	}

	private void recenter() {
		float trans;
		boolean hide = false;

		if (vAnchor == null) {
			trans = 0f;
		}
		else {
			int statusBarHeight = Utils.getStatusBarHeight(getContext());
			float vtop = statusBarHeight;
			float vbottom = vAnchor.getTop() + statusBarHeight;
			float vmiddle = (vtop + vbottom) / 2f;
			float vheight = vbottom - vtop;

			if (vheight < mMinHeight) {
				hide = true;
			}

			float ttop = getTop();
			float theight = getHeight();
			float tmiddle = ttop + theight / 2f;

			trans = adjustTranslation(vmiddle - tmiddle);
		}

		if (trans != mTrans) {
			mTrans = trans;
			animate().translationY(mTrans).alpha(hide ? 0f : 1f)
				.setInterpolator(new DecelerateInterpolator());
		}
	}

	/**
	 * Adjust the translation of this view.
	 * <p/>
	 * TODO: make this a little less hacky after EB 6.3 is released.
	 *
	 * @return
	 */
	private float adjustTranslation(float trans) {
		trans = trans / 1.5f;

		if (vUserInfoContainer.getVisibility() == View.VISIBLE) {
			trans -= vUserInfoContainer.getHeight() / 2;
		}

		return trans;
	}

	public void resetCenter() {
		anchorTo(null, getResources().getDimension(R.dimen.acct__user_image_view_min_height_default));
	}

	@Subscribe
	public void otto(Events.PartialUserDataChanged e) {
		updateUserGreeting();
	}

	private void updateUserGreeting() {
		PartialUser user = Db.getNewUser();

		// Email address
		vEmail.setText(user.email);
		vEmail.setContentDescription(user.email);

		// First name / greeting
		if (user.firstName != null) {
			String greeting = getContext().getString(R.string.acct__Hello_x, user.firstName);
			vGreeting.setText(greeting);
			vGreeting.setContentDescription(greeting);
		}
		else {
			vGreeting.setText(getContext().getString(R.string.acct__Hello_x));
			vGreeting.setContentDescription(getContext().getString(R.string.acct__Hello_x));
		}

		// Avatar
		vUserImage.setContactOrInitials(null,
			Utils.generateInitials(user.firstName, user.lastName, null, user.email));
		vUserImage.setContentDescription(Utils.generateInitials(user.firstName, user.lastName, null, user.email));
	}

	///////////////////////////////////////////////////////////////////////////
	// Transitions
	///////////////////////////////////////////////////////////////////////////

	DefaultTransition logo = new DefaultTransition(STATE_LOGO) {
		@Override
		public void finalizeTransition(boolean forward) {
			//We want to keep it at invisible and not gone to avoid making the logo jump around when the
			// container size changes
			vUserImage.setVisibility(View.INVISIBLE);
			vUserInfoContainer.setVisibility(View.INVISIBLE);
		}
	};

	Transition logoToHidden = new Transition(null, null) {
		@Override
		public void startTransition(boolean forward) {
			super.startTransition(forward);
			setVisibility(View.VISIBLE);
			setAlpha(forward ? 1f : 0f);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			super.updateTransition(f, forward);
			float g = forward ? 1 - f : f;
			setAlpha(g);
		}

		@Override
		public void finalizeTransition(boolean forward) {
			super.finalizeTransition(forward);
			if (forward) {
				setVisibility(View.INVISIBLE);
			}
			setAlpha(1f);
		}
	};

	public Transition getLargeToLogoTransition() {
		return largeToLogo;
	}

	private LoadingAnimationTriggerTransition.AnimationController mLoadingAnimationController = new LoadingAnimationTriggerTransition.AnimationController() {

		private AnimatorSet mLoadingAnimation;

		@Override
		public synchronized void stopAnimation() {
			if (mLoadingAnimation != null) {
				mLoadingAnimation.cancel();
				mLoadingAnimation = null;
			}
		}

		@Override
		public synchronized void startAnimation() {
			if (mLoadingAnimation == null) {
				mLoadingAnimation = new AnimatorSet();
				float growFactor = 1.2f;
				ObjectAnimator scaleX = ObjectAnimator
					.ofFloat(vLogoImage, "scaleX", vLogoImage.getScaleX(), vLogoImage.getScaleX() * growFactor);
				ObjectAnimator scaleY = ObjectAnimator
					.ofFloat(vLogoImage, "scaleY", vLogoImage.getScaleY(), vLogoImage.getScaleY() * growFactor);
				scaleX.setRepeatCount(ValueAnimator.INFINITE);
				scaleX.setRepeatMode(ValueAnimator.REVERSE);
				scaleY.setRepeatCount(ValueAnimator.INFINITE);
				scaleY.setRepeatMode(ValueAnimator.REVERSE);
				mLoadingAnimation.playTogether(scaleX, scaleY);
				mLoadingAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
				mLoadingAnimation.setDuration(1000);
				mLoadingAnimation.start();
			}
			//Otherwise it's already running, so just chill.
		}
	};

	public float getTrueBottom() {
		return getTop() + vLogoImage.getHeight();
	}

}
