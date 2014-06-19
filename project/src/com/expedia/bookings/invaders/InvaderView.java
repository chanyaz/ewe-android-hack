package com.expedia.bookings.invaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by jdrotos on 6/16/14.
 */
public abstract class InvaderView extends ImageView {

	private Bitmap mNormalBmap;
	private Bitmap mMirrorBmap;

	public InvaderView(Context context) {
		super(context);

		init(context);
	}

	public InvaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public InvaderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public void flip(boolean rot){
		//At this point we should mirror the image to give it a more space invaders feel
		setImageBitmap(rot ? mMirrorBmap : mNormalBmap);
	}

	private void init(Context context){
		setAdjustViewBounds(false);

		setBackgroundColor(getInvaderColor());
		mNormalBmap = BitmapFactory.decodeResource(context.getResources(), getImageResId());
		Matrix m = new Matrix();
		m.preScale(-1, 1);
		mMirrorBmap = Bitmap.createBitmap(mNormalBmap, 0, 0, mNormalBmap.getWidth(), mNormalBmap.getHeight(), m, false);

		setImageBitmap(mNormalBmap);
		setScaleType(ScaleType.FIT_XY);
	}

	public int getInvaderColor(){
		return Color.TRANSPARENT;
	}

	public abstract int getKillPoints();

	public abstract int getImageResId();
}
