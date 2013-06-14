package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LocalExpertAttraction;
import com.expedia.bookings.utils.Ui;

public class AttractionBubbleView extends LinearLayout {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final float LAYOUT_ASPECT = 1f / 1.1f; // width / height

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private int mHalfWidth;
	private int mRadius;
	private int mShadowOffsetY;

	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private Bitmap mShadowBitmap;

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

	public void setAttraction(LocalExpertAttraction attraction) {
		mFirstLineTextView.setText(attraction.getFirstLine());
		mSecondLineTextView.setText(attraction.getSecondLine());
		mIconImageView.setImageResource(attraction.getIconLarge());
	}

	private void init(Context context, AttributeSet attrs) {
		setWillNotDraw(false);
		setOrientation(VERTICAL);
		setGravity(Gravity.CENTER);

		mPaint.setColor(0xFFF2E2D4);
		mPaint.setStyle(Style.FILL);

		mShadowPaint.setColor(Color.BLACK);
		mShadowPaint.setStyle(Style.FILL);

		inflate(context, R.layout.widget_attraction_bubble, this);

		mFirstLineTextView = Ui.findView(this, R.id.first_line_text_view);
		mSecondLineTextView = Ui.findView(this, R.id.second_line_text_view);
		mIconImageView = Ui.findView(this, R.id.attraction_icon_image_view);
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

		setPadding(0, 0, 0, h / 10);
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
}
