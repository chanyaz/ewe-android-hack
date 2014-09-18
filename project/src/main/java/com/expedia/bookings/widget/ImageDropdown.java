package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;

/**
 * This class creates an imageview with that will toggle a dropdown on click
 *
 */
public class ImageDropdown extends ImageView {

	View mAnchor;
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
		mAnchor = this;
		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleDisplayDropdown();
			}
		});
	}

	@Override
	protected void onDetachedFromWindow() {
		//We need to dismiss before the parent is destroyed
		setDisplayDropdown(false);
		super.onDetachedFromWindow();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	/**
	 * show or hide the dropdown
	 * @param displayDropdown - if true show the dropdown, if false hide it
	 */
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

	/**
	 * toggle the display of the dropdown
	 */
	public void toggleDisplayDropdown() {
		setDisplayDropdown(!mPopupWindow.isShowing());
	}

	/**
	 * Set the view to use for the dropdown
	 * @param dropDownView
	 */
	public void setDropdownView(View dropDownView) {
		mPopupWindow = new PopupWindow(dropDownView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}

	/**
	 * Set the drop down to anchor itself to a different view.
	 * So we still click the image, but the dropdown appears anchored to the view specified
	 * @param anchor
	 */
	public void setAlternateDropdownAnchor(View anchor) {
		mAnchor = anchor;
	}

	/**
	 * Reset the dropdown to anchor itself to this view
	 */
	public void resetAnchor() {
		mAnchor = this;
	}

	public PopupWindow getPopupWindow() {
		return mPopupWindow;
	}
}
