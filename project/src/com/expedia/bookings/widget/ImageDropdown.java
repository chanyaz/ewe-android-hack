package com.expedia.bookings.widget;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

/**
 * This class creates an imageview with that will toggle a dropdown on click
 *
 */
public class ImageDropdown extends LinearLayout {
	
	View mAnchor;
	ImageView mImageView;
	PopupWindow mPopupWindow;

	public ImageDropdown(Context context) {
		super(context);
		init(context);
	}

	public ImageDropdown(Context context, AttributeSet attr) {
		super(context, attr);
		init(context);
	}

	public ImageDropdown(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View imageDropdown = inflater.inflate(R.layout.widget_image_dropdown, this);

		mImageView = Ui.findView(imageDropdown, R.id.image_dropdown_image);
		mAnchor = mImageView;
		mImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleDisplayDropdown();
			}
		});
	}

	@Override
	protected void onDetachedFromWindow(){
		//We need to dismiss before the parent is destroyed
		setDisplayDropdown(false);
		super.onDetachedFromWindow();
	}
	
	@Override
	protected void onAttachedToWindow(){
		super.onAttachedToWindow();
	}

	public void setDisplayDropdown(boolean displayDropdown) {
		if (mPopupWindow != null) {
			if (displayDropdown) {
				if (!mPopupWindow.isShowing()) {
					mPopupWindow.showAsDropDown(mAnchor);
				}
			}
			else {
				if (mPopupWindow.isShowing()) {
					mPopupWindow.dismiss();
				}
			}
		}
	}

	public void toggleDisplayDropdown() {
		setDisplayDropdown(!mPopupWindow.isShowing());
	}

	public void setImageDrawable(Drawable drawable) {
		if (mImageView != null) {
			mImageView.setImageDrawable(drawable);
		}
	}
	public Drawable getImageDrawable(){
		if(mImageView != null){
			return mImageView.getDrawable();
		}
		return null;
	}

	public void setDropdownView( View dropDownView) {
		mPopupWindow = new PopupWindow(dropDownView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}

	public void setDropdownAnchor(View anchor) {
		mAnchor = anchor;
	}

	public void resetAnchor() {
		mAnchor = mImageView;
	}
}
