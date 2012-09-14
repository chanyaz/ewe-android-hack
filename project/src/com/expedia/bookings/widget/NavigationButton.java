package com.expedia.bookings.widget;

import android.content.res.Resources;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import com.actionbarsherlock.app.ActionBar;
import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * This class is intended for ActionBar navigation. It toggles a dropdown of navigation items.
 *
 */
public class NavigationButton extends LinearLayout {

	View mDropDownContent;
	ViewGroup mSideViews;
	ImageDropdown mImageDropdown;
	Context mContext;
	TextView mTitle;

	public static NavigationButton createNewInstanceAndAttach(Context context, int iconResId, ActionBar actionBar) {
		Drawable icon = context.getResources().getDrawable(iconResId);

		return modifyActionBarPropertiesForCustomView(context, actionBar, icon);
	}

	public static NavigationButton createNewInstanceAndAttach(Context context, int iconResId, int cornerResId,
			ActionBar actionBar) {
		Resources res = context.getResources();
		Drawable icon = res.getDrawable(iconResId);
		Drawable corner = res.getDrawable(cornerResId);
		LayerDrawable iconAndCorner = createActionBarIconDrawable(context, icon, corner);

		return modifyActionBarPropertiesForCustomView(context, actionBar, iconAndCorner);
	}

	/**
	 * This updates the ActionBar attributes for setting the custom view. Additionally creates
	 * @param context
	 * @param actionBar
	 * @param drawable - background of the custom view
	 * @return the NavigationButton to be returned to the user of the constructor
	 */
	private static NavigationButton modifyActionBarPropertiesForCustomView(Context context, ActionBar actionBar,
			Drawable drawable) {
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);

		NavigationButton nb = createNewInstance(context, drawable);
		actionBar.setCustomView(nb);

		return nb;
	}

	private static NavigationButton createNewInstance(Context context, Drawable icon) {
		NavigationButton nb = new NavigationButton(context);
		nb.getImageDropdown().setImageDrawable(icon);
		return nb;
	}

	private static LayerDrawable createActionBarIconDrawable(Context context, Drawable icon, Drawable corner) {
		Resources res = context.getResources();

		Drawable[] da = new Drawable[2];
		da[0] = icon;

		int cornerInsetLeft = res.getDimensionPixelOffset(R.dimen.action_bar_nav_corner_inset_left);
		int cornerInsetTop = res.getDimensionPixelOffset(R.dimen.action_bar_nav_corner_inset_top);
		int cornerInsetRight = res.getDimensionPixelOffset(R.dimen.action_bar_nav_corner_inset_right);
		int cornerInsetBottom = res.getDimensionPixelOffset(R.dimen.action_bar_nav_corner_inset_bottom);
		InsetDrawable cornerInset = new InsetDrawable(corner, cornerInsetLeft, cornerInsetTop, cornerInsetRight,
				cornerInsetBottom);
		da[1] = cornerInset;

		return new LayerDrawable(da);
	}

	private NavigationButton(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		setOrientation(LinearLayout.HORIZONTAL);
		setGravity(Gravity.CENTER_VERTICAL);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.widget_image_dropdown, this);

		mContext = context;
		mTitle = Ui.findView(this, R.id.image_dropdown_title);
		mImageDropdown = Ui.findView(this, R.id.image_dropdown);

		mDropDownContent = inflater.inflate(R.layout.snippet_nav_dropdown, null);
		mSideViews = Ui.findView(this, R.id.image_dropdown_side_container);
		mImageDropdown.setDropdownView(mDropDownContent);
	}

	public void setDropdownAdapter(ListAdapter adapter) {
		ListView dropDownList = Ui.findView(mDropDownContent, R.id.nav_dropdown_list);
		dropDownList.setAdapter(adapter);
	}

	public void setDrawable(Drawable drawable) {
		mImageDropdown.setImageDrawable(drawable);
	}

	public void addSideView(View view) {
		mSideViews.addView(view);
	}

	public void clearTitleAndCustomViews() {
		mSideViews.removeAllViews();
		mTitle.setText("");
		mTitle.setVisibility(View.GONE);
	}

	public String getTitle() {
		return (mTitle == null || mTitle.getText() == null) ? "" : mTitle.getText().toString();
	}

	public ImageDropdown getImageDropdown() {
		return mImageDropdown;
	}

	public void setDisplayShowHomeEnabled(boolean enabled) {
		mImageDropdown.setVisibility(enabled ? View.VISIBLE : View.GONE);
	}

	////////////////////////////////////////
	// These methods replicate the methods found in the action bar

	public void setTitle(CharSequence title) {
		mTitle.setText(title);
		mTitle.setVisibility(View.VISIBLE);
	}

	public void setTitle(int resid) {
		mTitle.setText(resid);
		mTitle.setVisibility(View.VISIBLE);
	}

	public void setCustomView(int resId) {
		mSideViews.removeAllViews();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(resId, mSideViews);
	}

	public void setCustomView(View view) {
		mSideViews.removeAllViews();
		mSideViews.addView(view);
	}

	public void setCustomView(View view, LayoutParams layoutParams) {
		mSideViews.removeAllViews();
		mSideViews.addView(view, layoutParams);
	}
}
