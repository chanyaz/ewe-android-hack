package com.expedia.bookings.presenter;

import java.util.Stack;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.enums.CarsState;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.widget.FrameLayout;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarsPresenter extends FrameLayout {

	private Stack<CarsState> stateStack;

	@InjectView(R.id.widget_car_params)
	View widgetCarParams;

	@InjectView(R.id.cars_results_presenter)
	View carsResultsPresenter;

	@InjectView(R.id.car_checkout)
	View checkoutWidget;

	public CarsPresenter(Context context) {
		this(context, null);
	}

	public CarsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
		stateStack = new Stack<>();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	public void show(CarsState state) {
		if (stateStack.isEmpty() || state != stateStack.peek()) {
			stateStack.push(state);
		}
		setState(state);
	}

	/**
	 * @return true if consumed back press
	 */
	public boolean handleBackPress() {
		if (stateStack.isEmpty()) {
			return false;
		}
		else {
			stateStack.pop();
			if (stateStack.isEmpty()) {
				return false;
			}

			show(stateStack.peek());
			return true;
		}
	}

	private void setState(CarsState state) {
		widgetCarParams.setVisibility(View.GONE);
		carsResultsPresenter.setVisibility(View.GONE);
		checkoutWidget.setVisibility(View.GONE);

		switch (state) {
		case SEARCH:
			widgetCarParams.setVisibility(View.VISIBLE);
			break;
		case RESULTS:
			carsResultsPresenter.setVisibility(View.VISIBLE);
			break;
		case CHECKOUT:
			checkoutWidget.setVisibility(View.VISIBLE);
			break;
		default:
			throw new UnsupportedOperationException("CarsPresenter.show() invoked with unsupported state: " + state.toString());
		}
	}

	/**
	 * Events
	 */

	@Subscribe
	public void onNewCarSearchParams(Events.CarsNewSearchParams event) {
		show(CarsState.RESULTS);
	}

	@Subscribe
	public void onShowCheckout(Events.CarsShowCheckout event) {
		show(CarsState.CHECKOUT);
	}
}
