package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.NavigationHelper;

public class NewLaunchLobWidget extends FrameLayout {

	public NewLaunchLobWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.widget_new_launch_lob, this);

		NewLaunchLobViewModel vm = new NewLaunchLobViewModel(context, new NavigationHelper(context));
		vm.bind(this, PointOfSale.getPointOfSale());
	}
}
