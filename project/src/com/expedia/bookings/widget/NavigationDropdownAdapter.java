package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.NavItem;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;

/**
 * An adapter for displaying NavItem objects which are just images, text, and an onclicklistener
 *
 */
public class NavigationDropdownAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private ArrayList<NavItem> navItems;
	private Context mContext;
	private NoOpButton mNoOpBtn = NoOpButton.NONE;

	public enum NoOpButton {
		HOME, FLIGHTS, HOTELS, ACCOUNT, NONE
	}

	public NavigationDropdownAdapter(Context context, NoOpButton noopbtn) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mNoOpBtn = noopbtn;
		initData(context);
	}

	public void addItem(NavItem item) {
		navItems.add(item);
	}

	public void clearItems() {
		navItems.clear();
	}

	private final OnClickListener mLaunchScreenClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mNoOpBtn != NoOpButton.HOME) {
				NavUtils.goToLaunchScreen(mContext);
			}
		}
	};

	private final OnClickListener mHotelsClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mNoOpBtn != NoOpButton.HOTELS) {
				NavUtils.goToHotels(mContext);
			}
		}
	};

	private final OnClickListener mFlightsClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mNoOpBtn != NoOpButton.FLIGHTS) {
				NavUtils.goToFlights(mContext);
			}
		}
	};

	private final OnClickListener mAccountClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mNoOpBtn != NoOpButton.ACCOUNT) {
				Ui.showToast(mContext, "Account");
			}
		}
	};

	private void initData(Context context) {
		navItems = new ArrayList<NavItem>();
		Resources res = context.getResources();

		addItem(new NavItem(res.getDrawable(R.drawable.ic_expedia_drop_down), res
				.getString(R.string.nav_home), mLaunchScreenClickListener));

		addItem(new NavItem(res.getDrawable(R.drawable.ic_hotels_drop_down), res
				.getString(R.string.nav_hotels), mHotelsClickListener));

		addItem(new NavItem(res.getDrawable(R.drawable.ic_flights_drop_down), res
				.getString(R.string.nav_flights), mFlightsClickListener));

		addItem(new NavItem(res.getDrawable(R.drawable.ic_account_drop_down), res
				.getString(R.string.nav_account), mAccountClickListener));
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
