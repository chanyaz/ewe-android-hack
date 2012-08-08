package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.NavItem;
import com.expedia.bookings.utils.Ui;

/**
 * An adapter for displaying NavItem objects which are just images, text, and an onclicklistener
 *
 */
public class NavigationDropdownAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private ArrayList<NavItem> navItems;

	public NavigationDropdownAdapter(Context context) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		navItems = new ArrayList<NavItem>();
	}
	
	public void addItem(NavItem item){
		navItems.add(item);
	}
	
	public void clearItems(){
		navItems.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, R.layout.row_image_dropdown);
	}

	private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
		View view;

		if (convertView == null) {
			view = mInflater.inflate(resource, parent, false);
		}
		else {
			view = convertView;
		}

		TextView text = (TextView) Ui.findView(view, R.id.row_image_dropdown_text);
		ImageView img = (ImageView) Ui.findView(view, R.id.row_image_dropdown_image);

		img.setImageDrawable(navItems.get(position).getDrawable());
		text.setText(navItems.get(position).getText());
		view.setOnClickListener(navItems.get(position).getOnClickListener());
		
		return view;
	}

	@Override
	public int getCount() {
		return navItems.size();
	}

	@Override
	public Object getItem(int position) {
		return navItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	
}
