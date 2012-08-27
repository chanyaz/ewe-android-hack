package com.expedia.bookings.widget;

import com.actionbarsherlock.app.ActionBar;
import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
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
	
	public static NavigationButton createNewInstance(Context context, int iconResId){
		return createNewInstance(context,context.getResources().getDrawable(iconResId));
	}
	
	public static NavigationButton createNewInstance(Context context, Drawable icon){
		NavigationButton nb = new NavigationButton(context);
		nb.getImageDropdown().setImageDrawable(icon);
		return nb;
	}
	
	public static NavigationButton createNewInstanceAndAttach(Context context, int iconResId, ActionBar actionBar){
		return createNewInstanceAndAttach(context,context.getResources().getDrawable(iconResId) ,actionBar);
	}
	public static NavigationButton createNewInstanceAndAttach(Context context, Drawable icon, ActionBar actionBar){
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		
		NavigationButton nb = createNewInstance(context, icon);
		actionBar.setCustomView(nb);
		return nb;
	}
	
	private NavigationButton(Context context) {
		super(context);
		init(context);
	}

	private NavigationButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private NavigationButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.widget_image_dropdown, this);
		
		mContext = context;
		mTitle = Ui.findView(this,R.id.image_dropdown_title);
		mImageDropdown = Ui.findView(this, R.id.image_dropdown);
		
		mDropDownContent = inflater.inflate(R.layout.snippet_nav_dropdown, null);
		mSideViews = Ui.findView(this, R.id.image_dropdown_side_container);	
		mImageDropdown.setDropdownView(mDropDownContent);
	}
	
	public void setDropdownAdapter(ListAdapter adapter){
		ListView dropDownList = Ui.findView(mDropDownContent, R.id.nav_dropdown_list);
		dropDownList.setAdapter(adapter);
	}
	
	public void setDrawable(Drawable drawable){
		mImageDropdown.setImageDrawable(drawable);
	}
	
	public void addSideView(View view){
		mSideViews.addView(view);
	}

	public void clearTitleAndCustomViews(){
		mSideViews.removeAllViews();
		mTitle.setText("");
		mTitle.setVisibility(View.GONE);
	}

	public String getTitle(){
		return (mTitle == null || mTitle.getText() == null) ? "" : mTitle.getText().toString();
	}
	
	public ImageDropdown getImageDropdown(){
		return mImageDropdown;
	}
	
	////////////////////////////////////////
	// These methods replicate the methods found in the action bar
	
	public void setTitle(CharSequence title){
		mTitle.setText(title);
		mTitle.setVisibility(View.VISIBLE);
	}
	public void setTitle(int resid){
		mTitle.setText(resid);
		mTitle.setVisibility(View.VISIBLE);
	}
	
	public void setCustomView(int resId){
		mSideViews.removeAllViews();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(resId, mSideViews);
	}
	
	public void setCustomView(View view){
		mSideViews.removeAllViews();
		mSideViews.addView(view);
	}
	
	public void setCustomView(View view, LayoutParams layoutParams){
		mSideViews.removeAllViews();
		mSideViews.addView(view, layoutParams);
	}
}
