package com.expedia.bookings.widget;

import java.util.Random;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.OvershootInterpolator;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.bitmaps.UrlBitmapDrawable;
import com.expedia.bookings.data.LaunchLocation;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.utils.SpannableBuilder;
import com.expedia.bookings.utils.Ui;

public class LaunchPin extends FrameLayout {

	private static final Random mRandom = new Random();
	private RoundImageView mImageView;
	private TextView mTextView;

	private LaunchLocation mLocation;

	public LaunchPin(Context context) {
		super(context);
	}

	public LaunchPin(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LaunchPin(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mImageView = Ui.findView(this, R.id.launch_pin_image_view);
		mTextView = Ui.findView(this, R.id.launch_pin_text_view);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof LaunchPin) {
			LaunchPin p = (LaunchPin) o;
			if (mLocation != null && p.mLocation != null) {
				// TODO: better comparison than this?
				return mLocation.equals(p.mLocation);
			}
		}
		return false;
	}

	public void bind(LaunchLocation location) {
		mLocation = location;
		if (TextUtils.isEmpty(location.subtitle)) {
			setPinText(location.title);
		}
		else {
			setPinText(location.title, location.subtitle);
		}
	}

	public LaunchLocation getLaunchLocation() {
		return mLocation;
	}

	/**
	 * Returns a Rect matching the global location of the circle on the screen.
	 *
	 * @return
	 */
	public Rect getPinGlobalPosition() {
		Rect origin = ScreenPositionUtils.getGlobalScreenPosition(mImageView, true, true);
		return origin;
	}

	public void retrieveImageAndStartAnimation() {
		setVisibility(View.INVISIBLE);
		UrlBitmapDrawable bitmap = UrlBitmapDrawable.loadImageView(mLocation.getImageUrl(), mImageView);
		bitmap.setOnBitmapLoadedCallback(new L2ImageCache.OnBitmapLoaded() {
			@Override
			public void onBitmapLoaded(String url, Bitmap bitmap) {
				setPinBitmap(bitmap);
				getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
					@Override
					public boolean onPreDraw() {
						getViewTreeObserver().removeOnPreDrawListener(this);
						startPopinAnimation();
						return true;
					}
				});
			}

			@Override
			public void onBitmapLoadFailed(String url) {
				// Ignore
			}
		});
	}

	private void setPinBitmap(Bitmap bitmap) {
		mImageView.setImageBitmap(bitmap);
	}

	private void setPinText(String upper) {
		TextAppearanceSpan upperSpan = new TextAppearanceSpan(getContext(), R.style.MapPinUpperTextAppearance);

		SpannableBuilder sb = new SpannableBuilder();
		sb.append(upper, upperSpan);

		mTextView.setText(sb.build(), android.widget.TextView.BufferType.SPANNABLE);
	}

	private void setPinText(String upper, String lower) {
		TextAppearanceSpan upperSpan = new TextAppearanceSpan(getContext(), R.style.MapPinUpperTextAppearance);
		TextAppearanceSpan lowerSpan = new TextAppearanceSpan(getContext(), R.style.MapPinLowerTextAppearance);

		SpannableBuilder sb = new SpannableBuilder();
		sb.append(upper, upperSpan);
		sb.append("\n");
		sb.append(lower, lowerSpan);

		mTextView.setText(sb.build(), android.widget.TextView.BufferType.SPANNABLE);
	}

	private void startPopinAnimation() {
		setLayerType(View.LAYER_TYPE_HARDWARE, null);
		setPivotX(getWidth() / 2.0f);
		float mapPinImageSize = getContext().getResources().getDimension(R.dimen.launch_pin_size);
		setPivotY(mapPinImageSize / 2.0f);

		setScaleX(0.0f);
		setScaleY(0.0f);
		setVisibility(View.VISIBLE);

		ViewPropertyAnimator anim = animate();
		anim.scaleX(1.0f);
		anim.scaleY(1.0f);
		anim.setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator anim) {
				setLayerType(View.LAYER_TYPE_NONE, null);
			}
		});
		anim.setInterpolator(new OvershootInterpolator(2.0f));
		anim.setDuration(500);
		anim.setStartDelay(mRandom.nextInt(400));
		anim.start();
	}
}
