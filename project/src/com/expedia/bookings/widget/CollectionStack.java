package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.ColorUtils;
import com.expedia.bookings.utils.SpannableBuilder;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;

public class CollectionStack extends FrameLayout {
	public CollectionStack(Context context) {
		super(context);
		init(context);
	}

	public CollectionStack(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CollectionStack(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private float mBasePadding;
	private int mFirstDrawableBottom;
	private int mFirstDrawableTop;
	private int mSecondDrawableBottom;
	private int mSecondDrawableTop;

	private ImageView mImageView;
	private TextView mTextView;
	private AnimateInsetLayerDrawable mCollectionBackgroundDrawable;

	private int mBackgroundColor;

	private void init(Context context) {
		mBasePadding = context.getResources().getDimension(R.dimen.destination_stack_padding);
		mFirstDrawableBottom = (int) (mBasePadding * 4);
		mFirstDrawableTop = 0;
		mSecondDrawableBottom = (int) (mBasePadding * 3);
		mSecondDrawableTop = (int) (mBasePadding);

		LayoutInflater inflater = LayoutInflater.from(context);
		View root = inflater.inflate(R.layout.widget_collection_stack, this);

		mBackgroundColor = context.getResources().getColor(R.color.tablet_bg_tiles_blend);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		mImageView = Ui.findView(this, R.id.image);
		mTextView = Ui.findView(this, R.id.text);
	}

	private void setupStackBackground(int color) {
		if (mCollectionBackgroundDrawable == null) {
			Drawable[] drawables = new Drawable[2];
			drawables[0] = new ColorDrawable(ColorUtils.blend(color, mBackgroundColor, 0.70f));
			drawables[1] = new ColorDrawable(ColorUtils.blend(color, mBackgroundColor, 0.35f));
			mCollectionBackgroundDrawable = new AnimateInsetLayerDrawable(drawables);
		}

		mCollectionBackgroundDrawable.setPadding((int) (mBasePadding * 2));
		setBackgroundDrawable(mCollectionBackgroundDrawable);
		setStackPosition(-1.0f);
	}

	public void setStackBackgroundDrawable(int color, String url) {
		ColorDrawable drawable = new ColorDrawable(color);
		setStackBackgroundDrawable(color, drawable);
		UrlBitmapDrawable.loadImageView(url, mImageView);
	}

	public void setStackBackgroundDrawable(int color, Drawable bg) {
		setupStackBackground(color);
		if (mImageView != null) {
			mImageView.setImageDrawable(bg);
		}
	}

	public void setStackBackgroundResource(int color, int bgRes) {
		setupStackBackground(color);
		if (mImageView != null) {
			mImageView.setImageResource(bgRes);
		}
	}

	public void clearStackBackground() {
		mCollectionBackgroundDrawable = null;
		setBackgroundDrawable(null);
	}

	public void setText(String upper, String lower) {
		TextAppearanceSpan upperSpan = new TextAppearanceSpan(getContext(), R.style.DestinationStackUpperTextAppearance);
		TextAppearanceSpan lowerSpan = new TextAppearanceSpan(getContext(), R.style.DestinationStackLowerTextAppearance);

		SpannableBuilder sb = new SpannableBuilder();
		sb.append(upper, upperSpan);
		sb.append("\n");
		sb.append(lower, lowerSpan);

		mTextView.setText(sb.build(), TextView.BufferType.SPANNABLE);
	}

	// From [-1, 1] because that's how I roll
	// Used for animating the background stack effect
	public void setStackPosition(float amount) {
		if (amount < -1.0f || amount > 1.0f) {
			// Outside the bounds, just ignore
			// We're partially offscreen too,
			// don't waste time invalidating the drawables
			return;
		}

		// Pixel ranges based on 8dp base padding
		// 0Left = 0, 32
		// 0Right = 32, 0
		// 1Left = 8, 24
		// 1Right = 24, 8

		if (mCollectionBackgroundDrawable == null) {
			return;
		}

		float firstLeft = (amount + 1.0f) * mBasePadding * 2;
		float firstRight = mBasePadding * 4 - firstLeft;

		int secondLeft = (int) (firstLeft / 2.0f + mBasePadding);
		int secondRight = (int) (firstRight / 2.0f + mBasePadding);

		mCollectionBackgroundDrawable.setLayerInset(0, (int) firstLeft, mFirstDrawableTop, (int) firstRight, mFirstDrawableBottom);
		mCollectionBackgroundDrawable.setLayerInset(1, secondLeft, mSecondDrawableTop, secondRight, mSecondDrawableBottom);

		mCollectionBackgroundDrawable.invalidateSelf();
	}

	public static class AnimateInsetLayerDrawable extends LayerDrawable {
		private int mPadding = 0;

		public AnimateInsetLayerDrawable(Drawable[] drawables) {
			super(drawables);
		}

		@Override
		public void setLayerInset(int i, int left, int top, int right, int bottom) {
			super.setLayerInset(i, left, top, right, bottom);
		}

		public void setPadding(int padding) {
			mPadding = padding;
		}

		@Override
		public boolean getPadding(Rect rect) {
			rect.set(mPadding, mPadding, mPadding, mPadding);
			return mPadding == 0 ? false : true;
		}

		@Override
		public void invalidateSelf() {
			super.onBoundsChange(getBounds());
			super.invalidateSelf();
		}
	}
}
