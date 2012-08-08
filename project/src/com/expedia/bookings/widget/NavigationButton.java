package com.expedia.bookings.widget;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.data.NavItem;
import com.expedia.bookings.utils.Ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * This class is intended for ActionBar navigation. It toggles a dropdown of navigation items.
 *
 */
public class NavigationButton extends ImageDropdown {

	private static NavigationButton mInstance;
	
	View mDropDownContent;
	ViewGroup mSideViews;
	Context mContext;
	TextView mTitle;
	
	/**
	 * This class is intended to be a global navigation object, so when we request a new instance, we expect it to have the same state
	 * as the instance on the previous activity. This is typically in the form of which icon is displayed.
	 * @param context
	 * @return
	 */
	public static NavigationButton getStatefulInstance(Context context){
		NavigationButton nav = new NavigationButton(context);
		restoreStateFromStaticInstance(nav);
		mInstance = nav;
		return mInstance;
	}
	
	private static void restoreStateFromStaticInstance(NavigationButton nav){
		if(mInstance != null){
			nav.setImageDrawable(mInstance.getImageDrawable());
		}
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

	private void init(final Context context) {
		mContext = context;
		mTitle = Ui.findView(this,R.id.image_dropdown_title);
		
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDropDownContent = inflater.inflate(R.layout.snippet_nav_dropdown, null);
		ListView dropDownList = Ui.findView(mDropDownContent, R.id.nav_dropdown_list);

		mSideViews = Ui.findView(this, R.id.image_dropdown_side_container);
		
		final Resources res = mContext.getResources();
		NavigationDropdownAdapter adapter = new NavigationDropdownAdapter(mContext);
		adapter.addItem(new NavItem(res.getDrawable(R.drawable.icon), res
				.getString(R.string.nav_home), new OnClickListener() {
			@Override
			public void onClick(View v) {

				NavigationButton.this.setDisplayDropdown(false);
				NavigationButton.this.setImageDrawable(res.getDrawable(R.drawable.icon));
				Intent intent = new Intent(mContext, FlightSearchActivity.class);
				mContext.startActivity(intent);
			}
		}));

		adapter.addItem(new NavItem(res.getDrawable(R.drawable.search_center_purple), res
				.getString(R.string.nav_hotels), new OnClickListener() {
			@Override
			public void onClick(View v) {
				NavigationButton.this.setDisplayDropdown(false);
				NavigationButton.this.setImageDrawable(res.getDrawable(R.drawable.search_center_purple));
				Intent intent = new Intent(mContext, SearchActivity.class);
				mContext.startActivity(intent);
			}
		}));

		adapter.addItem(new NavItem(res.getDrawable(R.drawable.radar), res
				.getString(R.string.nav_flights), new OnClickListener() {
			@Override
			public void onClick(View v) {
				NavigationButton.this.setDisplayDropdown(false);
				NavigationButton.this.setImageDrawable(res.getDrawable(R.drawable.radar));
				Intent intent = new Intent(mContext, FlightSearchActivity.class);
				mContext.startActivity(intent);
			}
		}));
		adapter.addItem(new NavItem(res.getDrawable(R.drawable.ic_logged_in_no_rewards), res
				.getString(R.string.nav_account), new OnClickListener() {
			@Override
			public void onClick(View v) {
				NavigationButton.this.setDisplayDropdown(false);
				NavigationButton.this.setImageDrawable(res.getDrawable(R.drawable.ic_logged_in_no_rewards));
				Ui.showToast(mContext, "Account");
			}
		}));
		dropDownList.setAdapter(adapter);

		setDropdownView(mDropDownContent);
	}
	
	
	public void addSideView(View view){
		mSideViews.addView(view);
	}
	
	public void resetSubViews(){
		mSideViews.removeAllViews();
		mTitle.setText("");
		mTitle.setVisibility(View.GONE);
	}
	
	public void setTitle(String title){
		mTitle.setText(title);
		mTitle.setVisibility(View.VISIBLE);
	}
	
	public String getTitle(){
		return (mTitle == null || mTitle.getText() == null) ? "" : mTitle.getText().toString();
	}
	
}
