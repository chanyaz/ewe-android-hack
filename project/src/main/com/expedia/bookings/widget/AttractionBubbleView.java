package com.expedia.bookings.widget;

import java.util.Locale;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LocalExpertAttraction;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Ui;

public class AttractionBubbleView extends LinearLayout {
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	public static final int SIZE_SMALL = 0;
	public static final int SIZE_LARGE = 1;

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final float LAYOUT_ASPECT = 1f / 1.1f; // width / height

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private LocalExpertAttraction mAttraction = null;

	private int mSize = SIZE_LARGE;

	private int mHalfWidth;
	private int mRadius;
	private int mShadowOffsetY;

	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private Bitmap mShadowBitmap;

	private float mFirstLineTextSize;
	private float mSecondLineTextSize;

	// Views

	private TextView mFirstLineTextView;
	private TextView mSecondLineTextView;
	private ImageView mIconImageView;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public AttractionBubbleView(Context context) {
		super(context);
		init(context, null);
	}

	public AttractionBubbleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public LocalExpertAttraction getAttraction() {
		return mAttraction;
	}

	public void setAttraction(final LocalExpertAttraction attraction) {
		AnimatorSet animatorSet = null;

		if (mAttraction != null) {
			animatorSet = getCloseAnimatorSet();
			if (attraction != null) {
				animatorSet.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animator) {
						bind(attraction);
						getOpenAnimatorSet().start();
						post(new Runnable() {
							@Override
							public void run() {
								requestLayout();
							}
						});
					}
				});
			}
		}
		else if (attraction != null) {
			bind(attraction);
			animatorSet = getOpenAnimatorSet();
		}
		else {
			animatorSet = getCloseAnimatorSet();
			animatorSet.setDuration(0);
		}

		mAttraction = attraction;

		if (animatorSet != null) {
			animatorSet.start();
		}
	}

	public int getSize() {
		return mSize;
	}

	public void setSize(int size) {
		mSize = size;
		invalidate();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mHalfWidth = w / 2;
		mRadius = (int) (mHalfWidth * 0.9f);
		mShadowOffsetY = mHalfWidth / 20;
		mShadowPaint.setMaskFilter(new BlurMaskFilter(w / 20, Blur.NORMAL));

		if (mShadowBitmap == null || (w != oldw && h != oldh)) {
			mShadowBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(mShadowBitmap);
			canvas.drawCircle(mHalfWidth, mHalfWidth + mShadowOffsetY, mRadius, mShadowPaint);
		}

		mFirstLineTextSize = h / 15;
		mSecondLineTextSize = h / 6;

		final int textViewPadding = w / 10;
		mFirstLineTextView.setPadding(textViewPadding, 0, textViewPadding, 0);
		mSecondLineTextView.setPadding(textViewPadding, 0, textViewPadding, 0);

		setPadding(0, 0, 0, h / 20);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int[] dimensions = Ui.measureRatio(widthMeasureSpec, heightMeasureSpec, LAYOUT_ASPECT);
		final int width = MeasureSpec.makeMeasureSpec(dimensions[0], MeasureSpec.EXACTLY);
		final int height = MeasureSpec.makeMeasureSpec(dimensions[1], MeasureSpec.EXACTLY);

		super.onMeasure(width, height);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawBitmap(mShadowBitmap, 0, 0, mShadowPaint);
		canvas.drawCircle(mHalfWidth, mHalfWidth, mRadius, mPaint);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void init(Context context, AttributeSet attrs) {
		setWillNotDraw(false);
		setOrientation(VERTICAL);
		setGravity(Gravity.CENTER);

		mPaint.setColor(0xFFF2E2D4);
		mPaint.setStyle(Style.FILL);

		mShadowPaint.setColor(Color.BLACK);
		mShadowPaint.setStyle(Style.FILL);

		inflate(context, R.layout.widget_attraction_bubble, this);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AttractionBubbleView);
		setSize(a.getInteger(R.styleable.AttractionBubbleView_size, mSize));
		a.recycle();

		mFirstLineTextView = Ui.findView(this, R.id.first_line_text_view);
		mSecondLineTextView = Ui.findView(this, R.id.second_line_text_view);
		mIconImageView = Ui.findView(this, R.id.attraction_icon_image_view);

		if ("vn".equals(Locale.getDefault().getCountry().toLowerCase(Locale.ENGLISH))) {
			FontCache.setTypeface(mSecondLineTextView, FontCache.Font.ROBOTO_LIGHT);
		}
		else {
			FontCache.setTypeface(mSecondLineTextView, FontCache.Font.BEBAS_NEUE);
		}

		setAttraction(mAttraction);
	}

	private void bind(LocalExpertAttraction attraction) {
		if (attraction == null) {
			return;
		}

		mFirstLineTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mFirstLineTextSize);
		mSecondLineTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSecondLineTextSize);

		mFirstLineTextView.setText(attraction.getFirstLine());
		mSecondLineTextView.setText(attraction.getSecondLine());
		mIconImageView.setImageResource(mSize == SIZE_SMALL ? attraction.getIconSmall() : attraction.getIconLarge());
	}

	private AnimatorSet getCloseAnimatorSet() {
		Animator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1, 0);
		Animator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1, 0);
		Animator alpha = ObjectAnimator.ofFloat(this, "alpha", 1, 0);

		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(scaleX, scaleY, alpha);
		animatorSet.setInterpolator(new DecelerateInterpolator());
		animatorSet.setDuration(250);

		return animatorSet;
	};

	private AnimatorSet getOpenAnimatorSet() {
		Animator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0, 1);
		Animator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0, 1);
		Animator alpha = ObjectAnimator.ofFloat(this, "alpha", 0, 1);

		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(scaleX, scaleY, alpha);
		animatorSet.setInterpolator(new OvershootInterpolator());
		animatorSet.setDuration(300);

		return animatorSet;
	};
}
