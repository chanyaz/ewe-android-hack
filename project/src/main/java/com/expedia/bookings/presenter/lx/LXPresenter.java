package com.expedia.bookings.presenter.lx;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.widget.LXSearchParamsWidget;

import butterknife.InjectView;

public class LXPresenter extends Presenter {

	public LXPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.search_params_widget)
	LXSearchParamsWidget searchParamsWidget;

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		show(searchParamsWidget);

		Events.register(this);
	}

}
